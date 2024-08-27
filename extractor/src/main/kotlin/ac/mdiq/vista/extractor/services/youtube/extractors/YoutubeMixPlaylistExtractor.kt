package ac.mdiq.vista.extractor.services.youtube.extractors

import com.grack.nanojson.JsonObject
import com.grack.nanojson.JsonWriter
import ac.mdiq.vista.extractor.Image
import ac.mdiq.vista.extractor.Image.ResolutionLevel
import ac.mdiq.vista.extractor.Page
import ac.mdiq.vista.extractor.StreamingService
import ac.mdiq.vista.extractor.downloader.Downloader
import ac.mdiq.vista.extractor.exceptions.ContentNotAvailableException
import ac.mdiq.vista.extractor.exceptions.ExtractionException
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.linkhandler.ListLinkHandler
import ac.mdiq.vista.extractor.playlist.PlaylistExtractor
import ac.mdiq.vista.extractor.playlist.PlaylistInfo.PlaylistType
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.DISABLE_PRETTY_PRINT_PARAMETER
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.YOUTUBEI_V1_URL
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.extractCookieValue
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.extractPlaylistTypeFromPlaylistId
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.extractVideoIdFromMixId
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.getTextAtKey
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.getValidJsonResponseBody
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.isConsentAccepted
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.prepareDesktopJsonBuilder
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.youTubeHeaders
import ac.mdiq.vista.extractor.stream.Description
import ac.mdiq.vista.extractor.stream.StreamInfoItem
import ac.mdiq.vista.extractor.stream.StreamInfoItemsCollector
import ac.mdiq.vista.extractor.utils.ImageSuffix
import ac.mdiq.vista.extractor.utils.JsonUtils.toJsonObject
import ac.mdiq.vista.extractor.utils.Utils.getQueryValue
import ac.mdiq.vista.extractor.utils.Utils.stringToURL
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.stream.Collectors


/**
 * A [YoutubePlaylistExtractor] for a mix (auto-generated playlist).
 * It handles URLs in the format of
 * `youtube.com/watch?v=videoId&list=playlistId`
 */
class YoutubeMixPlaylistExtractor(service: StreamingService, linkHandler: ListLinkHandler) : PlaylistExtractor(service, linkHandler) {
    private var initialData: JsonObject? = null
    private var playlistData: JsonObject? = null
    private var cookieValue: String? = null

    @Throws(IOException::class, ExtractionException::class)
    override fun onFetchPage(downloader: Downloader) {
        val localization = extractorLocalization
        val url = stringToURL(url)
        val mixPlaylistId = id
        val videoId = getQueryValue(url, "v")
        val playlistIndexString = getQueryValue(url, "index")

        val jsonBody = prepareDesktopJsonBuilder(localization, extractorContentCountry).value("playlistId", mixPlaylistId)
        if (videoId != null) jsonBody.value("videoId", videoId)
        if (playlistIndexString != null) jsonBody.value("playlistIndex", playlistIndexString.toInt())

        val body = JsonWriter.string(jsonBody.done()).toByteArray(StandardCharsets.UTF_8)

        // Cookie is required due to consent
        val headers = youTubeHeaders

        val response = downloader.postWithContentTypeJson(YOUTUBEI_V1_URL + "next?" + DISABLE_PRETTY_PRINT_PARAMETER, headers, body, localization)

        initialData = toJsonObject(getValidJsonResponseBody(response))
        playlistData = initialData!!
            .getObject("contents")
            .getObject("twoColumnWatchNextResults")
            .getObject("playlist")
            .getObject("playlist")
        if (playlistData.isNullOrEmpty()) {
            val ex = ExtractionException("Could not get playlistData")
            if (!isConsentAccepted) throw ContentNotAvailableException("Consent is required in some countries to view Mix playlists", ex)
            throw ex
        }
        cookieValue = extractCookieValue(COOKIE_NAME, response)
    }


    @Throws(ParsingException::class)
    override fun getName(): String {
        val name = getTextAtKey(playlistData!!, "title")
        if (name.isNullOrEmpty()) throw ParsingException("Could not get playlist name")
        return name
    }

    @get:Throws(ParsingException::class)

    override val thumbnails: List<Image>
        get() {
            try {
                return getThumbnailsFromPlaylistId(playlistData!!.getString("playlistId"))
            } catch (e: Exception) {
                try {
                    // Fallback to thumbnail of current video. Always the case for channel mixes
                    return getThumbnailsFromVideoId(initialData!!.getObject("currentVideoEndpoint").getObject("watchEndpoint").getString("videoId"))
                } catch (ignored: Exception) { }
                throw ParsingException("Could not get playlist thumbnails", e)
            }
        }

    // YouTube mixes are auto-generated by YouTube
    override val uploaderUrl: String
        get() = ""

    // YouTube mixes are auto-generated by YouTube
    override val uploaderName: String
        get() = "YouTube"


    // YouTube mixes are auto-generated by YouTube

    override val uploaderAvatars: List<Image>
        get() = listOf()

    @get:Throws(ParsingException::class)
    override val isUploaderVerified: Boolean
        get() = false

    // Auto-generated playlists always start with 25 videos and are endless
    override val streamCount: Long
        get() = ITEM_COUNT_INFINITE

    @get:Throws(ParsingException::class)

    override val description: Description
        get() = Description.EMPTY_DESCRIPTION

    @get:Throws(IOException::class, ExtractionException::class)

    override val initialPage: InfoItemsPage<StreamInfoItem>
        get() {
            val collector = StreamInfoItemsCollector(serviceId)
            collectStreamsFrom(collector, playlistData!!.getArray("contents"))

            val cookies: MutableMap<String, String> = HashMap()
            cookies[COOKIE_NAME] = cookieValue ?: ""
            return InfoItemsPage(collector, getNextPageFrom(playlistData!!, cookies))
        }


    @Throws(IOException::class, ExtractionException::class)
    private fun getNextPageFrom(playlistJson: JsonObject, cookies: Map<String, String>): Page {
        val lastStream = (playlistJson.getArray("contents")[playlistJson.getArray("contents").size - 1] as JsonObject)
        if (lastStream.getObject("playlistPanelVideoRenderer") == null) throw ExtractionException("Could not extract next page url")

        val watchEndpoint = lastStream.getObject("playlistPanelVideoRenderer").getObject("navigationEndpoint").getObject("watchEndpoint")
        val playlistId = watchEndpoint.getString("playlistId")
        val videoId = watchEndpoint.getString("videoId")
        val index = watchEndpoint.getInt("index")
        val params = watchEndpoint.getString("params")
        val body = JsonWriter.string(prepareDesktopJsonBuilder(extractorLocalization, extractorContentCountry)
            .value("videoId", videoId)
            .value("playlistId", playlistId)
            .value("playlistIndex", index)
            .value("params", params)
            .done())
            .toByteArray(StandardCharsets.UTF_8)

        return Page(YOUTUBEI_V1_URL + "next?" + DISABLE_PRETTY_PRINT_PARAMETER, null, null, cookies, body)
    }

    @Throws(IOException::class, ExtractionException::class)
    override fun getPage(page: Page?): InfoItemsPage<StreamInfoItem> {
        require(!(page == null || page.url.isNullOrEmpty())) { "Page doesn't contain an URL" }
        require(page.cookies!!.containsKey(COOKIE_NAME)) { "Cookie '$COOKIE_NAME' is missing" }

        val collector = StreamInfoItemsCollector(serviceId)
        // Cookie is required due to consent
        val headers = youTubeHeaders

        val response = downloader.postWithContentTypeJson(page.url, headers, page.body, extractorLocalization)
        val ajaxJson = toJsonObject(getValidJsonResponseBody(response))
        val playlistJson = ajaxJson.getObject("contents").getObject("twoColumnWatchNextResults").getObject("playlist").getObject("playlist")
        val allStreams = playlistJson.getArray("contents")
        // Sublist because YouTube returns up to 24 previous streams in the mix
        // +1 because the stream of "currentIndex" was already extracted in previous request
        val newStreams: List<Any> = allStreams.subList(playlistJson.getInt("currentIndex") + 1, allStreams.size)

        collectStreamsFrom(collector, newStreams)
        return InfoItemsPage(collector, getNextPageFrom(playlistJson, page.cookies))
    }

    private fun collectStreamsFrom(collector: StreamInfoItemsCollector, streams: List<Any>?) {
        if (streams == null) return

        val timeAgoParser = timeAgoParser

        streams.stream()
            .filter { o: Any? -> JsonObject::class.java.isInstance(o) }
            .map { obj: Any? -> JsonObject::class.java.cast(obj) }
            .map { stream: JsonObject -> stream.getObject("playlistPanelVideoRenderer") }
            .filter { obj: JsonObject? -> Objects.nonNull(obj) }
            .map { streamInfo: JsonObject -> YoutubeStreamInfoItemExtractor(streamInfo, timeAgoParser) }
            .forEachOrdered { extractor: YoutubeStreamInfoItemExtractor? ->
                collector.commit(extractor!!)
            }
    }


    @Throws(ParsingException::class)
    private fun getThumbnailsFromPlaylistId(playlistId: String): List<Image> {
        return getThumbnailsFromVideoId(extractVideoIdFromMixId(playlistId))
    }


    private fun getThumbnailsFromVideoId(videoId: String): List<Image> {
        val baseUrl = "https://i.ytimg.com/vi/$videoId/"
        return IMAGE_URL_SUFFIXES_AND_RESOLUTIONS.stream()
            .map { imageSuffix: ImageSuffix ->
                Image(baseUrl + imageSuffix.suffix, imageSuffix.height, imageSuffix.width, imageSuffix.resolutionLevel)
            }
            .collect(Collectors.toUnmodifiableList())
    }

    @get:Throws(ParsingException::class)

    override val playlistType: PlaylistType
        get() = extractPlaylistTypeFromPlaylistId(playlistData!!.getString("playlistId"))

    companion object {
        private val IMAGE_URL_SUFFIXES_AND_RESOLUTIONS: List<ImageSuffix> =
            listOf( // sqdefault and maxresdefault image resolutions are not available on all
                // videos, so don't add them in the list of available resolutions
                ImageSuffix("default.jpg", 90, 120, ResolutionLevel.LOW),
                ImageSuffix("mqdefault.jpg", 180, 320, ResolutionLevel.MEDIUM),
                ImageSuffix("hqdefault.jpg", 360, 480, ResolutionLevel.MEDIUM))

        /**
         * YouTube identifies mixes based on this cookie. With this information it can generate
         * continuations without duplicates.
         */
        const val COOKIE_NAME: String = "VISITOR_INFO1_LIVE"
    }
}
