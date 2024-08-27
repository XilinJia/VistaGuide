package ac.mdiq.vista.extractor.services.youtube.extractors

import com.grack.nanojson.JsonArray
import com.grack.nanojson.JsonObject
import com.grack.nanojson.JsonWriter
import ac.mdiq.vista.extractor.Image
import ac.mdiq.vista.extractor.Page
import ac.mdiq.vista.extractor.StreamingService
import ac.mdiq.vista.extractor.downloader.Downloader
import ac.mdiq.vista.extractor.exceptions.ExtractionException
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.linkhandler.ListLinkHandler
import ac.mdiq.vista.extractor.playlist.PlaylistExtractor
import ac.mdiq.vista.extractor.playlist.PlaylistInfo.PlaylistType
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.DISABLE_PRETTY_PRINT_PARAMETER
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.YOUTUBEI_V1_URL
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.defaultAlertsCheck
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.extractPlaylistTypeFromPlaylistUrl
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.getImagesFromThumbnailsArray
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.getJsonPostResponse
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.getTextFromObject
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.getUrlFromNavigationEndpoint
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.prepareDesktopJsonBuilder
import ac.mdiq.vista.extractor.stream.Description
import ac.mdiq.vista.extractor.stream.StreamInfoItem
import ac.mdiq.vista.extractor.stream.StreamInfoItemsCollector
import ac.mdiq.vista.extractor.utils.Utils.removeNonDigitCharacters
import java.io.IOException
import java.nio.charset.StandardCharsets


class YoutubePlaylistExtractor(service: StreamingService, linkHandler: ListLinkHandler) : PlaylistExtractor(service, linkHandler) {
    private var browseResponse: JsonObject? = null

    private var playlistInfo: JsonObject? = null

    @get:Throws(ParsingException::class)

    private var uploaderInfo: JsonObject? = null
        get() {
            if (field == null) {
                field = browseResponse!!.getObject(SIDEBAR)
                    .getObject("playlistSidebarRenderer")
                    .getArray("items")
                    .stream()
                    .filter { o: Any? -> JsonObject::class.java.isInstance(o) }
                    .map { obj: Any? -> JsonObject::class.java.cast(obj) }
                    .filter { item: JsonObject ->
                        item.getObject("playlistSidebarSecondaryInfoRenderer")
                            .getObject("videoOwner")
                            .has(VIDEO_OWNER_RENDERER)
                    }
                    .map { item: JsonObject ->
                        item.getObject("playlistSidebarSecondaryInfoRenderer")
                            .getObject("videoOwner")
                            .getObject(VIDEO_OWNER_RENDERER)
                    }
                    .findFirst()
                    .orElseThrow { ParsingException("Could not get uploader info") }
            }

            return field
        }


    private var playlistHeader: JsonObject? = null
        get() {
            if (field == null) field = browseResponse!!.getObject("header").getObject("playlistHeaderRenderer")
            return field
        }

    private var isNewPlaylistInterface = false

    @Throws(IOException::class, ExtractionException::class)
    override fun onFetchPage(downloader: Downloader) {
        val localization = extractorLocalization
        val body = JsonWriter.string(prepareDesktopJsonBuilder(localization,
            extractorContentCountry)
            .value("browseId", "VL$id")
            .value("params", "wgYCCAA%3D") // Show unavailable videos
            .done())
            .toByteArray(StandardCharsets.UTF_8)

        browseResponse = getJsonPostResponse("browse", body, localization)
        defaultAlertsCheck(browseResponse!!)
        isNewPlaylistInterface = checkIfResponseIsNewPlaylistInterface()
    }

    /**
     * Whether the playlist response is using only the new playlist design.
     *
     * This new response changes how metadata is returned, and does not provide author thumbnails.
     *
     * The new response can be detected by checking whether a header JSON object is returned in the
     * browse response (the old returns instead a sidebar one).
     *
     * @return Whether the playlist response is using only the new playlist design
     */
    private fun checkIfResponseIsNewPlaylistInterface(): Boolean {
        // The "old" playlist UI can be also returned with the new one
        return browseResponse!!.has("header") && !browseResponse!!.has(SIDEBAR)
    }


    @Throws(ParsingException::class)
    private fun getPlaylistInfo(): JsonObject? {
        if (playlistInfo == null) {
            playlistInfo = browseResponse!!.getObject(SIDEBAR)
                .getObject("playlistSidebarRenderer")
                .getArray("items")
                .stream()
                .filter { o: Any? -> JsonObject::class.java.isInstance(o) }
                .map { obj: Any? -> JsonObject::class.java.cast(obj) }
                .filter { item: JsonObject -> item.has("playlistSidebarPrimaryInfoRenderer") }
                .map { item: JsonObject -> item.getObject("playlistSidebarPrimaryInfoRenderer") }
                .findFirst()
                .orElseThrow { ParsingException("Could not get playlist info") }
        }
        return playlistInfo
    }


    @Throws(ParsingException::class)
    override fun getName(): String {
        val name = getTextFromObject(getPlaylistInfo()!!.getObject("title"))
        if (!name.isNullOrEmpty()) return name

        return browseResponse!!.getObject("microformat")
            .getObject("microformatDataRenderer")
            .getString("title")
    }

    @get:Throws(ParsingException::class)

    override val thumbnails: List<Image>
        get() {
            val playlistMetadataThumbnailsArray = if (isNewPlaylistInterface) {
                playlistHeader!!.getObject("playlistHeaderBanner")
                    .getObject("heroPlaylistThumbnailRenderer")
                    .getObject("thumbnail")
                    .getArray("thumbnails")
            } else {
                playlistInfo!!.getObject("thumbnailRenderer")
                    .getObject("playlistVideoThumbnailRenderer")
                    .getObject("thumbnail")
                    .getArray("thumbnails")
            }

            if (!playlistMetadataThumbnailsArray.isNullOrEmpty()) return getImagesFromThumbnailsArray(playlistMetadataThumbnailsArray)

            // This data structure is returned in both layouts
            val microFormatThumbnailsArray = browseResponse!!.getObject("microformat")
                .getObject("microformatDataRenderer")
                .getObject("thumbnail")
                .getArray("thumbnails")

            if (!microFormatThumbnailsArray.isNullOrEmpty()) return getImagesFromThumbnailsArray(microFormatThumbnailsArray)
            throw ParsingException("Could not get playlist thumbnails")
        }

    @get:Throws(ParsingException::class)
    override val uploaderUrl: String?
        get() {
            try {
                return getUrlFromNavigationEndpoint(if (isNewPlaylistInterface) playlistHeader!!.getObject("ownerText")
                    .getArray("runs")
                    .getObject(0)
                    .getObject("navigationEndpoint")
                else uploaderInfo!!.getObject("navigationEndpoint"))
            } catch (e: Exception) {
                throw ParsingException("Could not get playlist uploader url", e)
            }
        }

    @get:Throws(ParsingException::class)
    override val uploaderName: String?
        get() {
            try {
                return getTextFromObject(if (isNewPlaylistInterface) playlistHeader!!.getObject("ownerText")
                else uploaderInfo!!.getObject("title"))
            } catch (e: Exception) {
                throw ParsingException("Could not get playlist uploader name", e)
            }
        }

    @get:Throws(ParsingException::class)

    override val uploaderAvatars: List<Image>
        get() {
            // The new playlist interface doesn't provide an uploader avatar
            if (isNewPlaylistInterface) return listOf()

            try {
                return getImagesFromThumbnailsArray(uploaderInfo!!.getObject("thumbnail").getArray("thumbnails"))
            } catch (e: Exception) {
                throw ParsingException("Could not get playlist uploader avatars", e)
            }
        }

    // YouTube doesn't provide this information
    @get:Throws(ParsingException::class)
    override val isUploaderVerified: Boolean
        get() = false

    @get:Throws(ParsingException::class)
    override val streamCount: Long
        get() {
            if (isNewPlaylistInterface) {
                val numVideosText = getTextFromObject(playlistHeader!!.getObject("numVideosText"))
                if (numVideosText != null) {
                    try {
                        return removeNonDigitCharacters(numVideosText).toLong()
                    } catch (ignored: NumberFormatException) { }
                }

                val firstByLineRendererText = getTextFromObject(playlistHeader!!.getArray("byline").getObject(0).getObject("text"))

                if (firstByLineRendererText != null) {
                    try {
                        return removeNonDigitCharacters(firstByLineRendererText).toLong()
                    } catch (ignored: NumberFormatException) { }
                }
            }

            // These data structures are returned in both layouts
            val briefStats = (if (isNewPlaylistInterface) playlistHeader else getPlaylistInfo())?.getArray("briefStats")
            if (!briefStats.isNullOrEmpty()) {
                val briefsStatsText = getTextFromObject(briefStats.getObject(0))
                if (briefsStatsText != null) return removeNonDigitCharacters(briefsStatsText).toLong()
            }

            val stats = (if (isNewPlaylistInterface) playlistHeader else getPlaylistInfo())?.getArray("stats")
            if (!stats.isNullOrEmpty()) {
                val statsText = getTextFromObject(stats.getObject(0))
                if (statsText != null) return removeNonDigitCharacters(statsText).toLong()
            }

            return ITEM_COUNT_UNKNOWN
        }

    @get:Throws(ParsingException::class)

    override val description: Description
        get() {
            val description = getTextFromObject(getPlaylistInfo()!!.getObject("description"), true)
            return Description(description, Description.HTML)
        }

    @get:Throws(IOException::class, ExtractionException::class)

    override val initialPage: InfoItemsPage<StreamInfoItem>
        get() {
            val collector = StreamInfoItemsCollector(serviceId)
            var nextPage: Page? = null

            val contents = browseResponse!!.getObject("contents")
                .getObject("twoColumnBrowseResultsRenderer")
                .getArray("tabs")
                .getObject(0)
                .getObject("tabRenderer")
                .getObject("content")
                .getObject("sectionListRenderer")
                .getArray("contents")

            val videoPlaylistObject = contents.stream()
                .filter { o: Any? -> JsonObject::class.java.isInstance(o) }
                .map { obj: Any? -> JsonObject::class.java.cast(obj) }
                .map { content: JsonObject ->
                    content.getObject("itemSectionRenderer")
                        .getArray("contents")
                        .getObject(0)
                }
                .filter { content: JsonObject? ->
                    (content!!.has(PLAYLIST_VIDEO_LIST_RENDERER) || content.has(RICH_GRID_RENDERER))
                }
                .findFirst()
                .orElse(null)

            if (videoPlaylistObject != null) {
                val renderer = when {
                    videoPlaylistObject.has(PLAYLIST_VIDEO_LIST_RENDERER) -> videoPlaylistObject.getObject(PLAYLIST_VIDEO_LIST_RENDERER)
                    videoPlaylistObject.has(RICH_GRID_RENDERER) -> videoPlaylistObject.getObject(RICH_GRID_RENDERER)
                    else -> return InfoItemsPage(collector, null)
                }
                val videosArray = renderer.getArray("contents")
                collectStreamsFrom(collector, videosArray)
                nextPage = getNextPageFrom(videosArray)
            }

            return InfoItemsPage(collector, nextPage)
        }

    @Throws(IOException::class, ExtractionException::class)
    override fun getPage(page: Page?): InfoItemsPage<StreamInfoItem> {
        require(!(page == null || page.url.isNullOrEmpty())) { "Page doesn't contain an URL" }

        val collector = StreamInfoItemsCollector(serviceId)

        val ajaxJson = getJsonPostResponse("browse", page.body, extractorLocalization)

        val continuation = ajaxJson.getArray("onResponseReceivedActions")
            .getObject(0)
            .getObject("appendContinuationItemsAction")
            .getArray("continuationItems")

        collectStreamsFrom(collector, continuation)

        return InfoItemsPage(collector, getNextPageFrom(continuation))
    }

    @Throws(IOException::class, ExtractionException::class)
    private fun getNextPageFrom(contents: JsonArray): Page? {
        if (contents.isEmpty()) return null

        val lastElement = contents.getObject(contents.size - 1)
        if (lastElement.has("continuationItemRenderer")) {
            val continuation = lastElement
                .getObject("continuationItemRenderer")
                .getObject("continuationEndpoint")
                .getObject("continuationCommand")
                .getString("token")

            val body = JsonWriter.string(prepareDesktopJsonBuilder(
                extractorLocalization, extractorContentCountry)
                .value("continuation", continuation)
                .done())
                .toByteArray(StandardCharsets.UTF_8)

            return Page(YOUTUBEI_V1_URL + "browse?" + DISABLE_PRETTY_PRINT_PARAMETER, body)
        } else {
            return null
        }
    }

    private fun collectStreamsFrom(collector: StreamInfoItemsCollector, videos: JsonArray) {
        val timeAgoParser = timeAgoParser
        videos.stream()
            .filter { o: Any? -> JsonObject::class.java.isInstance(o) }
            .map { obj: Any? -> JsonObject::class.java.cast(obj) }
            .forEach { video: JsonObject ->
                if (video.has(PLAYLIST_VIDEO_RENDERER))
                    collector.commit(YoutubeStreamInfoItemExtractor(video.getObject(PLAYLIST_VIDEO_RENDERER), timeAgoParser))
                else if (video.has(RICH_ITEM_RENDERER)) {
                    val richItemRenderer = video.getObject(RICH_ITEM_RENDERER)
                    if (richItemRenderer.has("content")) {
                        val richItemRendererContent = richItemRenderer.getObject("content")
                        if (richItemRendererContent.has(REEL_ITEM_RENDERER))
                            collector.commit(YoutubeReelInfoItemExtractor(richItemRendererContent.getObject(REEL_ITEM_RENDERER)))
                    }
                }
            }
    }

    @get:Throws(ParsingException::class)

    override val playlistType: PlaylistType
        get() = extractPlaylistTypeFromPlaylistUrl(url)

    companion object {
        // Names of some objects in JSON response frequently used in this class
        private const val PLAYLIST_VIDEO_RENDERER = "playlistVideoRenderer"
        private const val PLAYLIST_VIDEO_LIST_RENDERER = "playlistVideoListRenderer"
        private const val RICH_GRID_RENDERER = "richGridRenderer"
        private const val RICH_ITEM_RENDERER = "richItemRenderer"
        private const val REEL_ITEM_RENDERER = "reelItemRenderer"
        private const val SIDEBAR = "sidebar"
        private const val VIDEO_OWNER_RENDERER = "videoOwnerRenderer"
    }
}
