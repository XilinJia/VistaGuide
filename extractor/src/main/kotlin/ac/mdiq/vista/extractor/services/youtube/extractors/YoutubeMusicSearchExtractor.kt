package ac.mdiq.vista.extractor.services.youtube.extractors

import com.grack.nanojson.*
import ac.mdiq.vista.extractor.*
import ac.mdiq.vista.extractor.downloader.Downloader
import ac.mdiq.vista.extractor.exceptions.ExtractionException
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.linkhandler.SearchQueryHandler
import ac.mdiq.vista.extractor.search.SearchExtractor
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.DISABLE_PRETTY_PRINT_PARAMETER
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.getTextFromObject
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.getValidJsonResponseBody
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.getYoutubeMusicClientVersion
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.youtubeMusicHeaders
import ac.mdiq.vista.extractor.services.youtube.linkHandler.YoutubeSearchQueryHandlerFactory
import ac.mdiq.vista.extractor.utils.JsonUtils.getArray
import ac.mdiq.vista.extractor.utils.JsonUtils.getString
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.stream.Collectors


class YoutubeMusicSearchExtractor(service: StreamingService, linkHandler: SearchQueryHandler) : SearchExtractor(service, linkHandler) {
    private var initialData: JsonObject? = null

    @Throws(IOException::class, ExtractionException::class)
    override fun onFetchPage(downloader: Downloader) {
        val url = ("https://music.youtube.com/youtubei/v1/search?$DISABLE_PRETTY_PRINT_PARAMETER")

        val params = when (getLinkHandler().contentFilters[0]) {
            YoutubeSearchQueryHandlerFactory.MUSIC_SONGS -> "Eg-KAQwIARAAGAAgACgAMABqChAEEAUQAxAKEAk%3D"
            YoutubeSearchQueryHandlerFactory.MUSIC_VIDEOS -> "Eg-KAQwIABABGAAgACgAMABqChAEEAUQAxAKEAk%3D"
            YoutubeSearchQueryHandlerFactory.MUSIC_ALBUMS -> "Eg-KAQwIABAAGAEgACgAMABqChAEEAUQAxAKEAk%3D"
            YoutubeSearchQueryHandlerFactory.MUSIC_PLAYLISTS -> "Eg-KAQwIABAAGAAgACgBMABqChAEEAUQAxAKEAk%3D"
            YoutubeSearchQueryHandlerFactory.MUSIC_ARTISTS -> "Eg-KAQwIABAAGAAgASgAMABqChAEEAUQAxAKEAk%3D"
            else -> null
        }
        // @formatter:off
         val json = JsonWriter.string()
        .`object`()
        .`object`("context")
        .`object`("client")
        .value("clientName", "WEB_REMIX")
        .value("clientVersion", getYoutubeMusicClientVersion())
        .value("hl", "en-GB")
        .value("gl", extractorContentCountry.countryCode)
        .value("platform", "DESKTOP")
        .value("utcOffsetMinutes", 0)
        .end()
        .`object`("request")
        .array("internalExperimentFlags")
        .end()
        .value("useSsl", true)
        .end()
        .`object`("user") // TODO: provide a way to enable restricted mode with:
 //  .value("enableSafetyMode", boolean)
        .value("lockedSafetyMode", false)
        .end()
        .end()
        .value("query", searchString)
        .value("params", params)
        .end().done().toByteArray(StandardCharsets.UTF_8)

                // @formatter:on
        val responseBody = getValidJsonResponseBody(downloader.postWithContentTypeJson(url, youtubeMusicHeaders, json))

        try {
            initialData = JsonParser.`object`().from(responseBody)
        } catch (e: JsonParserException) {
            throw ParsingException("Could not parse JSON", e)
        }
    }

    private val itemSectionRendererContents: List<JsonObject>
        get() = if (initialData == null) listOf() else initialData!!
            .getObject("contents")
            .getObject("tabbedSearchResultsRenderer")
            .getArray("tabs")
            .getObject(0)
            .getObject("tabRenderer")
            .getObject("content")
            .getObject("sectionListRenderer")
            .getArray("contents")
            .stream()
            .filter { o: Any? -> JsonObject::class.java.isInstance(o) }
            .map { obj: Any? -> JsonObject::class.java.cast(obj) }
            .map { c: JsonObject -> c.getObject("itemSectionRenderer") }
            .filter { isr: JsonObject -> !isr.isEmpty() }
            .map { isr: JsonObject ->
                isr.getArray("contents").getObject(0)
            }
            .collect(Collectors.toList())

    @get:Throws(ParsingException::class)

    override val searchSuggestion: String
        get() {
            for (obj in itemSectionRendererContents) {
                val didYouMeanRenderer = obj.getObject("didYouMeanRenderer")
                val showingResultsForRenderer = obj.getObject("showingResultsForRenderer")

                if (!didYouMeanRenderer.isEmpty()) return getTextFromObject(didYouMeanRenderer.getObject("correctedQuery")) ?:""
                if (!showingResultsForRenderer.isEmpty()) return getString(showingResultsForRenderer, "correctedQueryEndpoint.searchEndpoint.query")
            }

            return ""
        }

    @get:Throws(ParsingException::class)
    override val isCorrectedSearch: Boolean
        get() = itemSectionRendererContents
            .stream()
            .anyMatch { obj: JsonObject -> obj.has("showingResultsForRenderer") }


    override val metaInfo: List<MetaInfo>
        get() = emptyList()

    @get:Throws(IOException::class, ExtractionException::class)

    override val initialPage: InfoItemsPage<InfoItem>
        get() {
            val collector = MultiInfoItemsCollector(serviceId)

            val contents = getArray(getArray(initialData!!,
                "contents.tabbedSearchResultsRenderer.tabs").getObject(0),
                "tabRenderer.content.sectionListRenderer.contents")

            var nextPage: Page? = null

            for (content in contents) {
                if ((content as JsonObject).has("musicShelfRenderer")) {
                    val musicShelfRenderer = content.getObject("musicShelfRenderer")
                    collectMusicStreamsFrom(collector, musicShelfRenderer.getArray("contents"))
                    nextPage = getNextPageFrom(musicShelfRenderer.getArray("continuations"))
                }
            }
            return InfoItemsPage(collector, nextPage)
        }

    @Throws(IOException::class, ExtractionException::class)
    override fun getPage(page: Page?): InfoItemsPage<InfoItem> {
        require(!(page == null || page.url.isNullOrEmpty())) { "Page doesn't contain an URL" }
        val collector = MultiInfoItemsCollector(serviceId)

        // @formatter:off
         val json = JsonWriter.string()
        .`object`()
        .`object`("context")
        .`object`("client")
        .value("clientName", "WEB_REMIX")
        .value("clientVersion", getYoutubeMusicClientVersion())
        .value("hl", "en-GB")
        .value("gl", extractorContentCountry.countryCode)
        .value("platform", "DESKTOP")
        .value("utcOffsetMinutes", 0)
        .end()
        .`object`("request")
        .array("internalExperimentFlags")
        .end()
        .value("useSsl", true)
        .end()
        .`object`("user") // TODO: provide a way to enable restricted mode with:
 //  .value("enableSafetyMode", boolean)
        .value("lockedSafetyMode", false)
        .end()
        .end()
        .end().done().toByteArray(StandardCharsets.UTF_8)

                // @formatter:on
        val responseBody = getValidJsonResponseBody(downloader.postWithContentTypeJson(page.url, youtubeMusicHeaders, json))

        val ajaxJson: JsonObject
        try {
            ajaxJson = JsonParser.`object`().from(responseBody)
        } catch (e: JsonParserException) {
            throw ParsingException("Could not parse JSON", e)
        }

        val musicShelfContinuation = ajaxJson.getObject("continuationContents").getObject("musicShelfContinuation")

        collectMusicStreamsFrom(collector, musicShelfContinuation.getArray("contents"))
        val continuations = musicShelfContinuation.getArray("continuations")

        return InfoItemsPage(collector, getNextPageFrom(continuations))
    }

    private fun collectMusicStreamsFrom(collector: MultiInfoItemsCollector,  videos: JsonArray) {
        val searchType = getLinkHandler().contentFilters[0]
        videos.stream()
            .filter { o: Any? -> JsonObject::class.java.isInstance(o) }
            .map { obj: Any? -> JsonObject::class.java.cast(obj) }
            .map { item: JsonObject -> item.getObject("musicResponsiveListItemRenderer", null) }
            .filter { obj: JsonObject? -> Objects.nonNull(obj) }
            .forEachOrdered { infoItem: JsonObject ->
                val displayPolicy = infoItem.getString("musicItemRendererDisplayPolicy", "")
                // No info about URL available
                if (displayPolicy == "MUSIC_ITEM_RENDERER_DISPLAY_POLICY_GREY_OUT") return@forEachOrdered

                val descriptionElements = infoItem.getArray("flexColumns")
                    .getObject(1)
                    .getObject("musicResponsiveListItemFlexColumnRenderer")
                    .getObject("text")
                    .getArray("runs")
                when (searchType) {
                    YoutubeSearchQueryHandlerFactory.MUSIC_SONGS, YoutubeSearchQueryHandlerFactory.MUSIC_VIDEOS ->
                        collector.commit(YoutubeMusicSongOrVideoInfoItemExtractor(infoItem, descriptionElements, searchType))
                    YoutubeSearchQueryHandlerFactory.MUSIC_ARTISTS -> collector.commit(YoutubeMusicArtistInfoItemExtractor(infoItem))
                    YoutubeSearchQueryHandlerFactory.MUSIC_ALBUMS, YoutubeSearchQueryHandlerFactory.MUSIC_PLAYLISTS ->
                        collector.commit(YoutubeMusicAlbumOrPlaylistInfoItemExtractor(infoItem, descriptionElements, searchType))
                }
            }
    }

    private fun getNextPageFrom(continuations: JsonArray): Page? {
        if (continuations.isEmpty()) return null

        val nextContinuationData = continuations.getObject(0).getObject("nextContinuationData")
        val continuation = nextContinuationData.getString("continuation")

        return Page(((("https://music.youtube.com/youtubei/v1/search?ctoken=$continuation").toString() + "&continuation=" + continuation) + "&" + DISABLE_PRETTY_PRINT_PARAMETER))
    }
}
