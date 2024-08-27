package ac.mdiq.vista.extractor.services.media_ccc.extractors

import com.grack.nanojson.JsonArray
import com.grack.nanojson.JsonObject
import com.grack.nanojson.JsonParser
import com.grack.nanojson.JsonParserException
import ac.mdiq.vista.extractor.Image
import ac.mdiq.vista.extractor.Image.ResolutionLevel
import ac.mdiq.vista.extractor.downloader.Downloader
import ac.mdiq.vista.extractor.exceptions.ExtractionException
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.exceptions.ReCaptchaException
import ac.mdiq.vista.extractor.localization.Localization
import java.io.IOException
import java.time.OffsetDateTime
import java.time.format.DateTimeParseException
import java.util.*
import java.util.regex.Pattern


object MediaCCCParsingHelper {
    // conference_slug/room_slug
    private val LIVE_STREAM_ID_PATTERN: Pattern = Pattern.compile("\\w+/\\w+")
    private var liveStreams: JsonArray? = null


    @Throws(ParsingException::class)
    fun parseDateFrom(textualUploadDate: String): OffsetDateTime {
        try {
            return OffsetDateTime.parse(textualUploadDate)
        } catch (e: DateTimeParseException) {
            throw ParsingException("Could not parse date: \"$textualUploadDate\"", e)
        }
    }

    /**
     * Check whether an id is a live stream id
     * @param id the `id` to check
     * @return returns `true` if the `id` is formatted like
     * `{conference_slug}/{room_slug}`; `false` otherwise
     */
    fun isLiveStreamId(id: String): Boolean {
        return LIVE_STREAM_ID_PATTERN.matcher(id).find()
    }

    /**
     * Get currently available live streams from
     * [
 * https://streaming.media.ccc.de/streams/v2.json](https://streaming.media.ccc.de/streams/v2.json).
     * Use this method to cache requests, because they can get quite big.
     * TODO: implement better caching policy (max-age: 3 min)
     * @param downloader The downloader to use for making the request
     * @param localization The localization to be used. Will most likely be ignored.
     * @return [JsonArray] containing current conferences and info about their rooms and
     * streams.
     * @throws ExtractionException if the data could not be fetched or the retrieved data could not
     * be parsed to a [JsonArray]
     */
    @Throws(ExtractionException::class)
    fun getLiveStreams(downloader: Downloader,
                       localization: Localization?
    ): JsonArray? {
        if (liveStreams == null) {
            try {
                val site = downloader.get("https://streaming.media.ccc.de/streams/v2.json",
                    localization).responseBody()
                liveStreams = JsonParser.array().from(site)
            } catch (e: IOException) {
                throw ExtractionException("Could not get live stream JSON.", e)
            } catch (e: ReCaptchaException) {
                throw ExtractionException("Could not get live stream JSON.", e)
            } catch (e: JsonParserException) {
                throw ExtractionException("Could not parse JSON.", e)
            }
        }
        return liveStreams
    }

    /**
     * Get an [Image] list from a given image logo URL.
     *
     *
     *
     * If the image URL is null or empty, an empty list is returned; otherwise, a singleton list is
     * returned containing an [Image] with the image URL with its height, width and
     * resolution unknown.
     *
     *
     * @param logoImageUrl a logo image URL, which can be null or empty
     * @return an unmodifiable list of [Image]s, which is always empty or a singleton
     */


    fun getImageListFromLogoImageUrl(logoImageUrl: String?): List<Image> {
        if (logoImageUrl.isNullOrEmpty()) {
            return listOf()
        }

        return listOf(Image(logoImageUrl, Image.HEIGHT_UNKNOWN, Image.WIDTH_UNKNOWN,
            ResolutionLevel.UNKNOWN))
    }

    /**
     * Get the [Image] list of thumbnails from a given stream item.
     *
     *
     *
     * MediaCCC API provides two thumbnails for a stream item: a `thumb_url` one, which is
     * medium quality and a `poster_url` one, which is high quality in most cases.
     *
     *
     * @param streamItem a stream JSON item of MediaCCC's API, which must not be null
     * @return an unmodifiable list, which is never null but can be empty.
     */


    fun getThumbnailsFromStreamItem(streamItem: JsonObject): List<Image> {
        return getThumbnailsFromObject(streamItem, "thumb_url", "poster_url")
    }

    /**
     * Get the [Image] list of thumbnails from a given live stream item.
     *
     *
     *
     * MediaCCC API provides two URL thumbnails for a livestream item: a `thumb` one,
     * which should be medium quality and a `poster_url` one, which should be high quality.
     *
     *
     * @param liveStreamItem a stream JSON item of MediaCCC's API, which must not be null
     * @return an unmodifiable list, which is never null but can be empty.
     */

    fun getThumbnailsFromLiveStreamItem(
            liveStreamItem: JsonObject
    ): List<Image> {
        return getThumbnailsFromObject(liveStreamItem, "thumb", "poster")
    }

    /**
     * Utility method to get an [Image] list of thumbnails from a stream or a livestream.
     *
     *
     *
     * MediaCCC's API thumbnails come from two elements: a `thumb` element, which links to a
     * medium thumbnail and a `poster` element, which links to a high thumbnail.
     *
     *
     *
     * Thumbnails are only added if their URLs are not null or empty.
     *
     *
     * @param streamOrLivestreamItem a (live)stream JSON item of MediaCCC's API, which must not be
     * null
     * @param thumbUrlKey  the name of the `thumb` URL key
     * @param posterUrlKey the name of the `poster` URL key
     * @return an unmodifiable list, which is never null but can be empty.
     */

    private fun getThumbnailsFromObject(
            streamOrLivestreamItem: JsonObject,
            thumbUrlKey: String,
            posterUrlKey: String
    ): List<Image> {
        val imageList: MutableList<Image> = ArrayList(2)

        val thumbUrl = streamOrLivestreamItem.getString(thumbUrlKey)
        if (!thumbUrl.isNullOrEmpty()) {
            imageList.add(Image(thumbUrl, Image.HEIGHT_UNKNOWN, Image.WIDTH_UNKNOWN,
                ResolutionLevel.MEDIUM))
        }

        val posterUrl = streamOrLivestreamItem.getString(posterUrlKey)
        if (!posterUrl.isNullOrEmpty()) {
            imageList.add(Image(posterUrl, Image.HEIGHT_UNKNOWN, Image.WIDTH_UNKNOWN,
                ResolutionLevel.HIGH))
        }

        return Collections.unmodifiableList(imageList)
    }
}
