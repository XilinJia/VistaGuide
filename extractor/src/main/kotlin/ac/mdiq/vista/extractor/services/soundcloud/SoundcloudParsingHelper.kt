package ac.mdiq.vista.extractor.services.soundcloud

//import java.util.Map
import com.grack.nanojson.JsonObject
import com.grack.nanojson.JsonParser
import com.grack.nanojson.JsonParserException
import org.jsoup.Jsoup
import ac.mdiq.vista.extractor.Image
import ac.mdiq.vista.extractor.Image.ResolutionLevel
import ac.mdiq.vista.extractor.MultiInfoItemsCollector
import ac.mdiq.vista.extractor.Vista.downloader
import ac.mdiq.vista.extractor.ServiceList.SoundCloud
import ac.mdiq.vista.extractor.channel.ChannelInfoItemsCollector
import ac.mdiq.vista.extractor.downloader.Downloader
import ac.mdiq.vista.extractor.downloader.Response
import ac.mdiq.vista.extractor.exceptions.ExtractionException
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.exceptions.ReCaptchaException
import ac.mdiq.vista.extractor.services.soundcloud.extractors.SoundcloudChannelInfoItemExtractor
import ac.mdiq.vista.extractor.services.soundcloud.extractors.SoundcloudPlaylistInfoItemExtractor
import ac.mdiq.vista.extractor.services.soundcloud.extractors.SoundcloudStreamInfoItemExtractor
import ac.mdiq.vista.extractor.stream.StreamInfoItemsCollector
import ac.mdiq.vista.extractor.utils.ImageSuffix
import ac.mdiq.vista.extractor.utils.JsonUtils.getValue
import ac.mdiq.vista.extractor.utils.Parser.RegexException
import ac.mdiq.vista.extractor.utils.Parser.matchGroup1
import ac.mdiq.vista.extractor.utils.Utils.encodeUrlUtf8
import ac.mdiq.vista.extractor.utils.Utils.removeMAndWWWFromUrl
import ac.mdiq.vista.extractor.utils.Utils.replaceHttpWithHttps
import ac.mdiq.vista.extractor.utils.Utils.stringToURL
import java.io.IOException
import java.net.MalformedURLException
import java.net.URL
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.*
import java.util.regex.Pattern
import java.util.stream.Collectors



object SoundcloudParsingHelper {
    // From https://web.archive.org/web/20210214185000/https://developers.soundcloud.com/docs/api/reference#tracks
    // and researches on images used by the websites
    /*
    SoundCloud avatars and artworks are almost always squares.

    When we get non-square pictures, all these images variants are still squares, except the
    original and the crop versions provides images which are respecting aspect ratios.
    The websites only use the square variants.

    t2400x2400 and t3000x3000 variants also exists, but are not returned as several images are
    uploaded with a lower size than these variants: in this case, these variants return an upscaled
    version of the original image.
    */
    private val ALBUMS_AND_ARTWORKS_IMAGE_SUFFIXES: List<ImageSuffix> =
        listOf(ImageSuffix("mini", 16, 16, ResolutionLevel.LOW),
            ImageSuffix("t20x20", 20, 20, ResolutionLevel.LOW),
            ImageSuffix("small", 32, 32, ResolutionLevel.LOW),
            ImageSuffix("badge", 47, 47, ResolutionLevel.LOW),
            ImageSuffix("t50x50", 50, 50, ResolutionLevel.LOW),
            ImageSuffix("t60x60", 60, 60, ResolutionLevel.LOW),  // Seems to work also on avatars, even if it is written to be not the case in
            // the old API docs
            ImageSuffix("t67x67", 67, 67, ResolutionLevel.LOW),
            ImageSuffix("t80x80", 80, 80, ResolutionLevel.LOW),
            ImageSuffix("large", 100, 100, ResolutionLevel.LOW),
            ImageSuffix("t120x120", 120, 120, ResolutionLevel.LOW),
            ImageSuffix("t200x200", 200, 200, ResolutionLevel.MEDIUM),
            ImageSuffix("t240x240", 240, 240, ResolutionLevel.MEDIUM),
            ImageSuffix("t250x250", 250, 250, ResolutionLevel.MEDIUM),
            ImageSuffix("t300x300", 300, 300, ResolutionLevel.MEDIUM),
            ImageSuffix("t500x500", 500, 500, ResolutionLevel.MEDIUM))

    private val VISUALS_IMAGE_SUFFIXES: List<ImageSuffix> =
        listOf(ImageSuffix("t1240x260", 1240, 260, ResolutionLevel.MEDIUM),
            ImageSuffix("t2480x520", 2480, 520, ResolutionLevel.MEDIUM))

    private var clientId: String? = null
    const val SOUNDCLOUD_API_V2_URL: String = "https://api-v2.soundcloud.com/"


    private val ON_URL_PATTERN: Pattern = Pattern.compile("^https?://on.soundcloud.com/[0-9a-zA-Z]+$")

    @Synchronized
    @Throws(ExtractionException::class, IOException::class)
    fun clientId(): String? {
        if (!clientId.isNullOrEmpty()) return clientId

        val dl = downloader

        val download = dl.get("https://soundcloud.com")
        val responseBody = download.responseBody()
        val clientIdPattern = ",client_id:\"(.*?)\""

        val doc = Jsoup.parse(responseBody)
        val possibleScripts = doc.select("script[src*=\"sndcdn.com/assets/\"][src$=\".js\"]")
        // The one containing the client id will likely be the last one
        Collections.reverse(possibleScripts)

        val headers = mapOf("Range" to listOf("bytes=0-50000"))

        for (element in possibleScripts) {
            val srcUrl = element.attr("src")
            if (!srcUrl.isNullOrEmpty()) {
                try {
                    clientId = matchGroup1(clientIdPattern, dl.get(srcUrl, headers).responseBody())
                    return clientId
                } catch (ignored: RegexException) {
                    // Ignore it and proceed to try searching other script
                }
            }
        }

        // Officially give up
        throw ExtractionException("Couldn't extract client id")
    }

    @Throws(ParsingException::class)
    fun parseDateFrom(textualUploadDate: String): OffsetDateTime {
        return try {
            OffsetDateTime.parse(textualUploadDate)
        } catch (e1: DateTimeParseException) {
            try {
                OffsetDateTime.parse(textualUploadDate, DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss +0000"))
            } catch (e2: DateTimeParseException) {
                throw ParsingException("Could not parse date: \"" + textualUploadDate + "\"" + ", " + e1.message, e2)
            }
        }
    }

    /**
     * Call the endpoint "/resolve" of the API.
     * See https://developers.soundcloud.com/docs/api/reference#resolve
     */
    @Throws(IOException::class, ExtractionException::class)
    fun resolveFor(downloader: Downloader, url: String?): JsonObject {
        val apiUrl = ("${SOUNDCLOUD_API_V2_URL}resolve?url=${encodeUrlUtf8(url)}&client_id=${clientId()}")

        try {
            val response: String = downloader.get(apiUrl, SoundCloud.localization).responseBody()
            return JsonParser.`object`().from(response)
        } catch (e: JsonParserException) {
            throw ParsingException("Could not parse json response", e)
        }
    }

    /**
     * Fetch the embed player with the apiUrl and return the canonical url (like the permalink_url
     * from the json API).
     *
     * @return the url resolved
     */

    @Throws(IOException::class, ReCaptchaException::class)
    fun resolveUrlWithEmbedPlayer(apiUrl: String?): String {
        val response: String = downloader.get("https://w.soundcloud.com/player/?url=" + encodeUrlUtf8(apiUrl), SoundCloud.localization).responseBody()
        return Jsoup.parse(response).select("link[rel=\"canonical\"]").first()?.attr("abs:href") ?: ""
    }

    /**
     * Fetch the widget API with the url and return the id (like the id from the json API).
     *
     * @return the resolved id
     */

    @Throws(IOException::class, ParsingException::class)
    fun resolveIdWithWidgetApi(urlString: String): String {
        // Remove the tailing slash from URLs due to issues with the SoundCloud API
        var fixedUrl = urlString

        // if URL is an on.soundcloud link, do a request to resolve the redirect
        if (ON_URL_PATTERN.matcher(fixedUrl).find()) {
            try {
                fixedUrl = downloader.head(fixedUrl).latestUrl()
                // remove tracking params which are in the query string
                fixedUrl = fixedUrl.split("\\?".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
            } catch (e: ExtractionException) {
                throw ParsingException("Could not follow on.soundcloud.com redirect", e)
            }
        }

        if (fixedUrl[fixedUrl.length - 1] == '/') fixedUrl = fixedUrl.substring(0, fixedUrl.length - 1)
        // Make URL lower case and remove m. and www. if it exists.
        // Without doing this, the widget API does not recognize the URL.
        fixedUrl = removeMAndWWWFromUrl(fixedUrl.lowercase(Locale.getDefault()))

        val url: URL
        try {
            url = stringToURL(fixedUrl)
        } catch (e: MalformedURLException) {
            throw IllegalArgumentException("The given URL is not valid")
        }

        try {
            val widgetUrl = ("https://api-widget.soundcloud.com/resolve?url=${encodeUrlUtf8(url.toString())}&format=json&client_id=${clientId()}")
            val response: String = downloader.get(widgetUrl, SoundCloud.localization).responseBody()
            val o = JsonParser.`object`().from(response)
            return getValue(o, "id").toString()
        } catch (e: JsonParserException) {
            throw ParsingException("Could not parse JSON response", e)
        } catch (e: ExtractionException) {
            throw ParsingException("Could not resolve id with embedded player. ClientId not extracted", e)
        }
    }

    /**
     * Fetch the users from the given API and commit each of them to the collector.
     *
     *
     * This differ from [.getUsersFromApi] in the sense
     * that they will always get MIN_ITEMS or more.
     *
     * @param minItems the method will return only when it have extracted that many items
     * (equal or more)
     */
    @Throws(IOException::class, ReCaptchaException::class, ParsingException::class)
    fun getUsersFromApiMinItems(minItems: Int, collector: ChannelInfoItemsCollector, apiUrl: String): String {
        var nextPageUrl = getUsersFromApi(collector, apiUrl)

        while (nextPageUrl.isNotEmpty() && collector.getItems().size < minItems) {
            nextPageUrl = getUsersFromApi(collector, nextPageUrl)
        }
        return nextPageUrl
    }

    /**
     * Fetch the user items from the given API and commit each of them to the collector.
     *
     * @return the next streams url, empty if don't have
     */

    @Throws(IOException::class, ReCaptchaException::class, ParsingException::class)
    fun getUsersFromApi(collector: ChannelInfoItemsCollector, apiUrl: String): String {
        val response: String = downloader.get(apiUrl, SoundCloud.localization).responseBody()
        val responseObject: JsonObject

        try {
            responseObject = JsonParser.`object`().from(response)
        } catch (e: JsonParserException) {
            throw ParsingException("Could not parse json response", e)
        }

        val responseCollection = responseObject.getArray("collection")
        for (o in responseCollection) {
            if (o is JsonObject) collector.commit(SoundcloudChannelInfoItemExtractor(o))
        }

        return getNextPageUrl(responseObject)
    }

    /**
     * Fetch the streams from the given API and commit each of them to the collector.
     *
     *
     * This differ from [.getStreamsFromApi] in the sense
     * that they will always get MIN_ITEMS or more items.
     *
     * @param minItems the method will return only when it have extracted that many items
     * (equal or more)
     */
    @Throws(IOException::class, ReCaptchaException::class, ParsingException::class)
    fun getStreamsFromApiMinItems(minItems: Int, collector: StreamInfoItemsCollector, apiUrl: String): String {
        var nextPageUrl = getStreamsFromApi(collector, apiUrl)
        while (nextPageUrl.isNotEmpty() && collector.getItems().size < minItems) {
            nextPageUrl = getStreamsFromApi(collector, nextPageUrl)
        }
        return nextPageUrl
    }

    /**
     * Fetch the streams from the given API and commit each of them to the collector.
     *
     * @return the next streams url, empty if don't have
     */

    @Throws(IOException::class, ReCaptchaException::class, ParsingException::class)
    fun getStreamsFromApi(collector: StreamInfoItemsCollector, apiUrl: String, charts: Boolean): String {
        val response: Response = downloader.get(apiUrl, SoundCloud.localization)
        if (response.responseCode() >= 400) throw IOException("Could not get streams from API, HTTP " + response.responseCode())

        val responseObject: JsonObject
        try {
            responseObject = JsonParser.`object`().from(response.responseBody())
        } catch (e: JsonParserException) {
            throw ParsingException("Could not parse json response", e)
        }

        val responseCollection = responseObject.getArray("collection")
        for (o in responseCollection) {
            if (o is JsonObject) collector.commit(SoundcloudStreamInfoItemExtractor(if (charts) o.getObject("track") else o))
        }

        return getNextPageUrl(responseObject)
    }


    private fun getNextPageUrl(response: JsonObject): String {
        try {
            var nextPageUrl = response.getString("next_href")
            if (!nextPageUrl.contains("client_id=")) nextPageUrl += "&client_id=" + clientId()
            return nextPageUrl
        } catch (ignored: Exception) {
            return ""
        }
    }

    @Throws(ReCaptchaException::class, ParsingException::class, IOException::class)
    fun getStreamsFromApi(collector: StreamInfoItemsCollector, apiUrl: String): String {
        return getStreamsFromApi(collector, apiUrl, false)
    }

    @Throws(ReCaptchaException::class, ParsingException::class, IOException::class)
    fun getInfoItemsFromApi(collector: MultiInfoItemsCollector, apiUrl: String): String {
        val response: Response = downloader.get(apiUrl, SoundCloud.localization)
        if (response.responseCode() >= 400) throw IOException("Could not get streams from API, HTTP " + response.responseCode())

        val responseObject: JsonObject
        try {
            responseObject = JsonParser.`object`().from(response.responseBody())
        } catch (e: JsonParserException) {
            throw ParsingException("Could not parse json response", e)
        }

        responseObject.getArray("collection")
            .stream()
            .filter { o: Any? -> JsonObject::class.java.isInstance(o) }
            .map { obj: Any? -> JsonObject::class.java.cast(obj) }
            .forEach { searchResult: JsonObject ->
                val kind = searchResult.getString("kind", "")
                when (kind) {
                    "user" -> collector.commit(SoundcloudChannelInfoItemExtractor(searchResult))
                    "track" -> collector.commit(SoundcloudStreamInfoItemExtractor(searchResult))
                    "playlist" -> collector.commit(SoundcloudPlaylistInfoItemExtractor(searchResult))
                }
            }

        var nextPageUrl: String
        try {
            nextPageUrl = responseObject.getString("next_href")
            if (!nextPageUrl.contains("client_id=")) nextPageUrl += "&client_id=" + clientId()
        } catch (ignored: Exception) {
            nextPageUrl = ""
        }

        return nextPageUrl
    }


    fun getUploaderUrl(`object`: JsonObject): String {
        val url = `object`.getObject("user").getString("permalink_url", "")
        return replaceHttpWithHttps(url)
    }


    fun getAvatarUrl(`object`: JsonObject): String {
        val url = `object`.getObject("user").getString("avatar_url", "")
        return replaceHttpWithHttps(url)
    }


    fun getUploaderName(`object`: JsonObject): String {
        return `object`.getObject("user").getString("username", "")
    }


    @Throws(ParsingException::class)
    fun getAllImagesFromTrackObject(trackObject: JsonObject): List<Image> {
        val artworkUrl = trackObject.getString("artwork_url")
        if (artworkUrl != null) return getAllImagesFromArtworkOrAvatarUrl(artworkUrl)
        val avatarUrl = trackObject.getObject("user").getString("avatar_url")
        if (avatarUrl != null) return getAllImagesFromArtworkOrAvatarUrl(avatarUrl)

        throw ParsingException("Could not get track or track user's thumbnails")
    }


    fun getAllImagesFromArtworkOrAvatarUrl(originalArtworkOrAvatarUrl: String?): List<Image> {
        if (originalArtworkOrAvatarUrl.isNullOrEmpty()) return listOf()

        // Artwork and avatars are originally returned with the "large" resolution, which
        // is 100px wide
        return getAllImagesFromImageUrlReturned(originalArtworkOrAvatarUrl.replace("-large.", "-%s."), ALBUMS_AND_ARTWORKS_IMAGE_SUFFIXES)
    }


    fun getAllImagesFromVisualUrl(originalVisualUrl: String?): List<Image> {
        if (originalVisualUrl.isNullOrEmpty()) return listOf()

        // Images are originally returned with the "original" resolution, which may be
        // huge so don't include it for size purposes
        return getAllImagesFromImageUrlReturned(originalVisualUrl.replace("-original.", "-%s."), VISUALS_IMAGE_SUFFIXES)
    }

    private fun getAllImagesFromImageUrlReturned(baseImageUrlFormat: String, imageSuffixes: List<ImageSuffix>): List<Image> {
        return imageSuffixes.stream()
            .map { imageSuffix: ImageSuffix ->
                Image(String.format(baseImageUrlFormat, imageSuffix.suffix), imageSuffix.height, imageSuffix.width, imageSuffix.resolutionLevel) }
            .collect(Collectors.toUnmodifiableList())
    }
}
