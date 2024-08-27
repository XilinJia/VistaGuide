package ac.mdiq.vista.extractor.services.peertube

import com.grack.nanojson.JsonArray
import com.grack.nanojson.JsonObject
import ac.mdiq.vista.extractor.*
import ac.mdiq.vista.extractor.Image.ResolutionLevel
import ac.mdiq.vista.extractor.exceptions.ContentNotAvailableException
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.services.peertube.extractors.PeertubeChannelInfoItemExtractor
import ac.mdiq.vista.extractor.services.peertube.extractors.PeertubePlaylistInfoItemExtractor
import ac.mdiq.vista.extractor.services.peertube.extractors.PeertubeSepiaStreamInfoItemExtractor
import ac.mdiq.vista.extractor.services.peertube.extractors.PeertubeStreamInfoItemExtractor
import ac.mdiq.vista.extractor.utils.JsonUtils.getValue
import ac.mdiq.vista.extractor.utils.Parser.RegexException
import ac.mdiq.vista.extractor.utils.Parser.matchGroup1
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeParseException
import java.util.*
import java.util.stream.Collectors


object PeertubeParsingHelper {
    const val START_KEY: String = "start"
    const val COUNT_KEY: String = "count"
    const val ITEMS_PER_PAGE: Int = 12
    private const val START_PATTERN: String = "start=(\\d*)"


    @Throws(ContentNotAvailableException::class)
    fun validate(json: JsonObject) {
        val error = json.getString("error")
        if (!error.isNullOrEmpty()) throw ContentNotAvailableException(error)
    }


    @Throws(ParsingException::class)
    fun parseDateFrom(textualUploadDate: String): OffsetDateTime {
        try {
            return OffsetDateTime.ofInstant(Instant.parse(textualUploadDate), ZoneOffset.UTC)
        } catch (e: DateTimeParseException) {
            throw ParsingException("Could not parse date: \"$textualUploadDate\"", e)
        }
    }


    fun getNextPage(prevPageUrl: String, total: Long): Page? {
        val prevStart: String
        try {
            prevStart = matchGroup1(START_PATTERN, prevPageUrl)
        } catch (e: RegexException) {
            return null
        }
        if (prevStart.isEmpty()) return null

        val nextStart: Long
        try {
            nextStart = prevStart.toLong() + ITEMS_PER_PAGE
        } catch (e: NumberFormatException) {
            return null
        }
        return if (nextStart >= total) null else  Page(prevPageUrl.replace("$START_KEY=$prevStart", "$START_KEY=$nextStart"))
    }

    /**
     * Collect items from the given JSON object with the given collector.
     *
     * Supported info item types are streams with their Sepia variant, channels and playlists.
     *
     * @param collector the collector used to collect information
     * @param json      the JSOn response to retrieve data from
     * @param baseUrl   the base URL of the instance
     * @param sepia     if we should use `PeertubeSepiaStreamInfoItemExtractor` to extract
     * streams or `PeertubeStreamInfoItemExtractor` otherwise
     */

    @JvmOverloads
    @Throws(ParsingException::class)
    fun <I: InfoItem, E: InfoItemExtractor>collectItemsFrom(collector: InfoItemsCollector<I, E>, json: JsonObject?, baseUrl: String, sepia: Boolean = false) {

        val contents: JsonArray
        try {
            contents = getValue(json!!, "data") as JsonArray
        } catch (e: Exception) {
            throw ParsingException("Unable to extract list info", e)
        }

        for (c in contents) {
            if (c is JsonObject) {
                var item: JsonObject = c

                // PeerTube playlists have the stream info encapsulated in an "video" object
                if (item.has("video")) item = item.getObject("video")
                val isPlaylistInfoItem = item.has("videosLength")
                val isChannelInfoItem = item.has("followersCount")
                val extractor = when {
                    sepia -> PeertubeSepiaStreamInfoItemExtractor(item, baseUrl)
                    isPlaylistInfoItem -> PeertubePlaylistInfoItemExtractor(item, baseUrl)
                    isChannelInfoItem -> PeertubeChannelInfoItemExtractor(item, baseUrl)
                    else -> PeertubeStreamInfoItemExtractor(item, baseUrl)
                }
                collector.commit(extractor as E)
            }
        }
    }

    /**
     * Get avatars from a `ownerAccount` or a `videoChannel` [JsonObject].
     *
     * If the `avatars` [JsonArray] is present and non null or empty, avatars will be
     * extracted from this array using [.getImagesFromAvatarOrBannerArray].
     *
     * If that's not the case, an avatar will extracted using the `avatar` [JsonObject].
     *
     * Note that only images for which paths are not null and not empty will be added to the
     * unmodifiable [Image] list returned.
     *
     * @param baseUrl                          the base URL of the PeerTube instance
     * @param ownerAccountOrVideoChannelObject the `ownerAccount` or `videoChannel`
     * [JsonObject]
     * @return an unmodifiable list of [Image]s, which may be empty but never null
     */


    fun getAvatarsFromOwnerAccountOrVideoChannelObject(baseUrl: String, ownerAccountOrVideoChannelObject: JsonObject): List<Image> {
        return getImagesFromAvatarsOrBanners(baseUrl, ownerAccountOrVideoChannelObject, "avatars", "avatar")
    }

    /**
     * Get banners from a `ownerAccount` or a `videoChannel` [JsonObject].
     *
     * If the `banners` [JsonArray] is present and non null or empty, banners will be
     * extracted from this array using [.getImagesFromAvatarOrBannerArray].
     *
     * If that's not the case, a banner will extracted using the `banner` [JsonObject].
     *
     * Note that only images for which paths are not null and not empty will be added to the
     * unmodifiable [Image] list returned.
     *
     * @param baseUrl                          the base URL of the PeerTube instance
     * @param ownerAccountOrVideoChannelObject the `ownerAccount` or `videoChannel`
     * [JsonObject]
     * @return an unmodifiable list of [Image]s, which may be empty but never null
     */


    fun getBannersFromAccountOrVideoChannelObject(baseUrl: String, ownerAccountOrVideoChannelObject: JsonObject): List<Image> {
        return getImagesFromAvatarsOrBanners(baseUrl, ownerAccountOrVideoChannelObject, "banners", "banner")
    }

    /**
     * Get thumbnails from a playlist or a video item [JsonObject].
     *
     * PeerTube provides two thumbnails in its API: a low one, represented by the value of the
     * `thumbnailPath` key, and a medium one, represented by the value of the`previewPath` key.
     *
     * If a value is not null or empty, an [Image] will be added to the list returned with
     * the URL to the thumbnail (`baseUrl + value`), a height and a width unknown and the
     * corresponding resolution level (see above).
     *
     * @param baseUrl                   the base URL of the PeerTube instance
     * @param playlistOrVideoItemObject the playlist or the video item [JsonObject], which
     * must not be null
     * @return an unmodifiable list of [Image]s, which is never null but can be empty
     */


    fun getThumbnailsFromPlaylistOrVideoItem(baseUrl: String, playlistOrVideoItemObject: JsonObject): List<Image> {
        val imageList: MutableList<Image> = ArrayList(2)

        val thumbnailPath = playlistOrVideoItemObject.getString("thumbnailPath")
        if (!thumbnailPath.isNullOrEmpty()) imageList.add(Image(baseUrl + thumbnailPath, Image.HEIGHT_UNKNOWN, Image.WIDTH_UNKNOWN, ResolutionLevel.LOW))

        val previewPath = playlistOrVideoItemObject.getString("previewPath")
        if (!previewPath.isNullOrEmpty()) imageList.add(Image(baseUrl + previewPath, Image.HEIGHT_UNKNOWN, Image.WIDTH_UNKNOWN, ResolutionLevel.MEDIUM))

        return Collections.unmodifiableList(imageList)
    }

    /**
     * Utility method to get avatars and banners from video channels and accounts from given name
     * keys.
     *
     * Only images for which paths are not null and not empty will be added to the unmodifiable
     * [Image] list returned and only the width of avatars or banners is provided by the API,
     * and so is the only image dimension known.
     *
     * @param baseUrl                          the base URL of the PeerTube instance
     * @param ownerAccountOrVideoChannelObject the `ownerAccount` or `videoChannel`
     * [JsonObject]
     * @param jsonArrayName                    the key name of the [JsonArray] to which
     * extract all images available (`avatars` or
     * `banners`)
     * @param jsonObjectName                   the key name of the [JsonObject] to which
     * extract a single image (`avatar` or
     * `banner`), used as a fallback if the images
     * [JsonArray] is null or empty
     * @return an unmodifiable list of [Image]s, which may be empty but never null
     */

    private fun getImagesFromAvatarsOrBanners(baseUrl: String, ownerAccountOrVideoChannelObject: JsonObject,
                                              jsonArrayName: String, jsonObjectName: String): List<Image> {
        val images = ownerAccountOrVideoChannelObject.getArray(jsonArrayName)

        if (!images.isNullOrEmpty()) return getImagesFromAvatarOrBannerArray(baseUrl, images)

        val image = ownerAccountOrVideoChannelObject.getObject(jsonObjectName)
        val path = image.getString("path")
        if (!path.isNullOrEmpty())
            return listOf(Image(baseUrl + path, Image.HEIGHT_UNKNOWN, image.getInt("width", Image.WIDTH_UNKNOWN), ResolutionLevel.UNKNOWN))

        return listOf()
    }

    /**
     * Get [Image]s from an `avatars` or a `banners` [JsonArray].
     *
     * Only images for which paths are not null and not empty will be added to the unmodifiable [Image] list returned.
     *
     * Note that only the width of avatars or banners is provided by the API, and so only is the
     * only dimension known of images.
     *
     * @param baseUrl               the base URL of the PeerTube instance from which the
     * `avatarsOrBannersArray` [JsonArray] comes from
     * @param avatarsOrBannersArray an `avatars` or `banners` [JsonArray]
     * @return an unmodifiable list of [Image]s, which may be empty but never null
     */

    private fun getImagesFromAvatarOrBannerArray(baseUrl: String, avatarsOrBannersArray: JsonArray): List<Image> {
        return avatarsOrBannersArray.stream()
            .filter { o: Any? -> JsonObject::class.java.isInstance(o) }
            .map { obj: Any? -> JsonObject::class.java.cast(obj) }
            .filter { image: JsonObject -> !image.getString("path").isNullOrEmpty() }
            .map { image: JsonObject ->
                Image(baseUrl + image.getString("path"), Image.HEIGHT_UNKNOWN, image.getInt("width", Image.WIDTH_UNKNOWN), ResolutionLevel.UNKNOWN) }
            .collect(Collectors.toUnmodifiableList())
    }
}
