package ac.mdiq.vista.extractor.services.youtube.extractors

import com.grack.nanojson.JsonArray
import com.grack.nanojson.JsonObject
import com.grack.nanojson.JsonWriter
import ac.mdiq.vista.extractor.InfoItem
import ac.mdiq.vista.extractor.MultiInfoItemsCollector
import ac.mdiq.vista.extractor.Page
import ac.mdiq.vista.extractor.StreamingService
import ac.mdiq.vista.extractor.channel.tabs.ChannelTabExtractor
import ac.mdiq.vista.extractor.channel.tabs.ChannelTabs
import ac.mdiq.vista.extractor.downloader.Downloader
import ac.mdiq.vista.extractor.exceptions.ExtractionException
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.linkhandler.ListLinkHandler
import ac.mdiq.vista.extractor.localization.TimeAgoParser
import ac.mdiq.vista.extractor.services.youtube.YoutubeChannelHelper.ChannelHeader
import ac.mdiq.vista.extractor.services.youtube.YoutubeChannelHelper.getChannelAgeGateRenderer
import ac.mdiq.vista.extractor.services.youtube.YoutubeChannelHelper.getChannelHeader
import ac.mdiq.vista.extractor.services.youtube.YoutubeChannelHelper.getChannelId
import ac.mdiq.vista.extractor.services.youtube.YoutubeChannelHelper.getChannelName
import ac.mdiq.vista.extractor.services.youtube.YoutubeChannelHelper.getChannelResponse
import ac.mdiq.vista.extractor.services.youtube.YoutubeChannelHelper.isChannelVerified
import ac.mdiq.vista.extractor.services.youtube.YoutubeChannelHelper.resolveChannelId
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.DISABLE_PRETTY_PRINT_PARAMETER
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.YOUTUBEI_V1_URL
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.getJsonPostResponse
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.prepareDesktopJsonBuilder
import ac.mdiq.vista.extractor.services.youtube.linkHandler.YoutubeChannelTabLinkHandlerFactory
import ac.mdiq.vista.extractor.services.youtube.linkHandler.YoutubeChannelTabLinkHandlerFactory.Companion.getUrlSuffix
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.util.*


/**
 * A [ChannelTabExtractor] implementation for the YouTube service.
 *
 * It currently supports `Videos`, `Shorts`, `Live`, `Playlists`,
 * `Albums` and `Channels` tabs.
 *
 */
open class YoutubeChannelTabExtractor(service: StreamingService, linkHandler: ListLinkHandler) : ChannelTabExtractor(service, linkHandler) {
    /**
     * Whether the visitor data extracted from the initial channel response is required to be used for continuations.
     * A valid `visitorData` is required to get continuations of shorts in channels.
     * It should be not used when it is not needed, in order to reduce YouTube's tracking.
     *
     */
    private val useVisitorData = getName() == ChannelTabs.SHORTS
    private var jsonResponse: JsonObject? = null
    private var channelId: String? = null
    private var visitorData: String? = null
    protected open var channelHeader: ChannelHeader? = null

    @get:Throws(ParsingException::class)

    private val channelTabsParameters: String
        get() {
            return when (val name = getName()) {
                ChannelTabs.VIDEOS -> "EgZ2aWRlb3PyBgQKAjoA"
                ChannelTabs.SHORTS -> "EgZzaG9ydHPyBgUKA5oBAA%3D%3D"
                ChannelTabs.LIVESTREAMS -> "EgdzdHJlYW1z8gYECgJ6AA%3D%3D"
                ChannelTabs.ALBUMS -> "EghyZWxlYXNlc_IGBQoDsgEA"
                ChannelTabs.PLAYLISTS -> "EglwbGF5bGlzdHPyBgQKAkIA"
                else -> throw ParsingException("Unsupported channel tab: $name")
            }
        }

    @Throws(IOException::class, ExtractionException::class)
    override fun onFetchPage(downloader: Downloader) {
        val channelIdFromId = resolveChannelId(super.id)
        val params = channelTabsParameters
        val data = getChannelResponse(channelIdFromId, params, extractorLocalization, extractorContentCountry)

        jsonResponse = data.jsonResponse
        channelHeader = getChannelHeader(jsonResponse!!)
        channelId = data.channelId
        if (useVisitorData) visitorData = jsonResponse!!.getObject("responseContext").getString("visitorData")
    }

    @get:Throws(ParsingException::class)

    override val url: String
        get() = try {
            YoutubeChannelTabLinkHandlerFactory.instance.getUrl("channel/$id", listOf(getName()), "")
        } catch (e: ParsingException) {
            super.url
        }

    @get:Throws(ParsingException::class)

    override val id: String
        get() = getChannelId(channelHeader!!, jsonResponse!!, channelId!!)

    @get:Throws(ParsingException::class)
    protected open val channelName: String
        get() = getChannelName(channelHeader!!, jsonResponse!!, getChannelAgeGateRenderer(jsonResponse!!))

    @get:Throws(IOException::class, ExtractionException::class)

    override val initialPage: InfoItemsPage<InfoItem>
        get() {
            val collector = MultiInfoItemsCollector(serviceId)

            var items = JsonArray()
            val tab = tabData

            if (tab.isPresent) {
                val tabContent = tab.get().getObject("content")

                items = tabContent.getObject("sectionListRenderer")
                    .getArray("contents")
                    .getObject(0)
                    .getObject("itemSectionRenderer")
                    .getArray("contents")
                    .getObject(0)
                    .getObject("gridRenderer")
                    .getArray("items")

                if (items.isEmpty()) {
                    items = tabContent.getObject("richGridRenderer").getArray("contents")
                    if (items.isEmpty()) items = tabContent.getObject("sectionListRenderer").getArray("contents")
                }
            }

            val verifiedStatus = channelHeader?.let { header: ChannelHeader ->
                if (isChannelVerified(header)) VerifiedStatus.VERIFIED else VerifiedStatus.UNVERIFIED }
                ?: VerifiedStatus.UNKNOWN

            // If a channel tab is fetched, the next page requires channel ID and name, as channel
            // streams don't have their channel specified.
            // We also need to set the visitor data here when it should be enabled, as it is required
            // to get continuations on some channel tabs, and we need a way to pass it between pages
            val channelName = channelName
            val channelUrl = url

            val continuation = collectItemsFrom(collector, items, verifiedStatus, channelName, channelUrl).orElse(null)

            val nextPage = getNextPageFrom(continuation,
                if (useVisitorData && !visitorData.isNullOrEmpty()) listOf(channelName, channelUrl, verifiedStatus.toString(), visitorData)
                else listOf(channelName, channelUrl, verifiedStatus.toString()))

            return InfoItemsPage(collector, nextPage)
        }

    @Throws(IOException::class, ExtractionException::class)
    override fun getPage(page: Page?): InfoItemsPage<InfoItem> {
        require(!(page?.url.isNullOrEmpty())) { "Page doesn't contain an URL" }

        val channelIds: List<String?>? = page!!.ids
        val collector = MultiInfoItemsCollector(serviceId)
        val ajaxJson = getJsonPostResponse("browse", page.body, extractorLocalization)

        val sectionListContinuation = ajaxJson.getArray("onResponseReceivedActions")
            .stream()
            .filter { o: Any? -> JsonObject::class.java.isInstance(o) }
            .map { obj: Any? -> JsonObject::class.java.cast(obj) }
            .filter { jsonObject: JsonObject -> jsonObject.has("appendContinuationItemsAction") }
            .map { jsonObject: JsonObject -> jsonObject.getObject("appendContinuationItemsAction") }
            .findFirst()
            .orElse(JsonObject())

        val continuation = collectItemsFrom(collector, sectionListContinuation.getArray("continuationItems"), channelIds!!).orElse(null)
        return InfoItemsPage(collector, getNextPageFrom(continuation, channelIds))
    }

    open val tabData: Optional<JsonObject>
        get() {
            val urlSuffix = getUrlSuffix(getName())

            return jsonResponse!!.getObject("contents")
                .getObject("twoColumnBrowseResultsRenderer")
                .getArray("tabs")
                .stream()
                .filter { o: Any? -> JsonObject::class.java.isInstance(o) }
                .map { obj: Any? -> JsonObject::class.java.cast(obj) }
                .filter { tab: JsonObject -> tab.has("tabRenderer") }
                .map { tab: JsonObject -> tab.getObject("tabRenderer") }
                .filter { tabRenderer: JsonObject ->
                    tabRenderer.getObject("endpoint")
                        .getObject("commandMetadata").getObject("webCommandMetadata")
                        .getString("url", "").endsWith(urlSuffix)
                }
                .findFirst() // Check if tab has no content
                .filter { tabRenderer: JsonObject ->
                    val tabContents = tabRenderer.getObject("content")
                        .getObject("sectionListRenderer")
                        .getArray("contents")
                        .getObject(0)
                        .getObject("itemSectionRenderer")
                        .getArray("contents")
                    tabContents.size != 1 || !tabContents.getObject(0).has("messageRenderer")
                }
        }

    private fun collectItemsFrom(collector: MultiInfoItemsCollector, items: JsonArray, channelIds: List<String?>): Optional<JsonObject> {
        val channelName: String?
        val channelUrl: String?
        val verifiedStatus: VerifiedStatus

        if (channelIds.size >= 3) {
            channelName = channelIds[0]
            channelUrl = channelIds[1]
            verifiedStatus = try {
                VerifiedStatus.valueOf(channelIds[2]!!)
            } catch (e: IllegalArgumentException) {
                // An IllegalArgumentException can be thrown if someone passes a third channel ID
                // which is not of the enum type in the getPage method, use the UNKNOWN
                // VerifiedStatus enum value in this case
                VerifiedStatus.UNKNOWN
            }
        } else {
            channelName = null
            channelUrl = null
            verifiedStatus = VerifiedStatus.UNKNOWN
        }
        return collectItemsFrom(collector, items, verifiedStatus, channelName, channelUrl)
    }

    private fun collectItemsFrom(collector: MultiInfoItemsCollector, items: JsonArray, verifiedStatus: VerifiedStatus,
                                 channelName: String?, channelUrl: String?): Optional<JsonObject> {
        return items.stream()
            .filter { o: Any? -> JsonObject::class.java.isInstance(o) }
            .map { obj: Any? -> JsonObject::class.java.cast(obj) }
            .map { item: JsonObject -> collectItem(collector, item, verifiedStatus, channelName, channelUrl) }
            .reduce(Optional.empty()) { c1: Optional<JsonObject>, c2: Optional<JsonObject>? -> c1.or { c2 } }
    }

    private fun collectItem(collector: MultiInfoItemsCollector, item: JsonObject, channelVerifiedStatus: VerifiedStatus,
                            channelName: String?, channelUrl: String?): Optional<JsonObject> {
        val timeAgoParser = timeAgoParser

        when {
            item.has("richItemRenderer") -> {
                val richItem = item.getObject("richItemRenderer").getObject("content")
                when {
                    richItem.has("videoRenderer") ->
                        commitVideo(collector, timeAgoParser, richItem.getObject("videoRenderer"), channelVerifiedStatus, channelName, channelUrl)
                    richItem.has("reelItemRenderer") ->
                        commitReel(collector, richItem.getObject("reelItemRenderer"), channelVerifiedStatus, channelName, channelUrl)
                    richItem.has("playlistRenderer") ->
                        commitPlaylist(collector, richItem.getObject("playlistRenderer"), channelVerifiedStatus, channelName, channelUrl)
                }
            }
            item.has("gridVideoRenderer") ->
                commitVideo(collector, timeAgoParser, item.getObject("gridVideoRenderer"), channelVerifiedStatus, channelName, channelUrl)
            item.has("gridPlaylistRenderer") ->
                commitPlaylist(collector, item.getObject("gridPlaylistRenderer"), channelVerifiedStatus, channelName, channelUrl)
            item.has("gridShowRenderer") ->
                collector.commit(YoutubeGridShowRendererChannelInfoItemExtractor(item.getObject("gridShowRenderer"), channelVerifiedStatus, channelName, channelUrl))
            item.has("shelfRenderer") ->
                return collectItem(collector, item.getObject("shelfRenderer").getObject("content"), channelVerifiedStatus, channelName, channelUrl)
            item.has("itemSectionRenderer") ->
                return collectItemsFrom(collector, item.getObject("itemSectionRenderer").getArray("contents"), channelVerifiedStatus, channelName, channelUrl)
            item.has("horizontalListRenderer") ->
                return collectItemsFrom(collector, item.getObject("horizontalListRenderer").getArray("items"), channelVerifiedStatus, channelName, channelUrl)
            item.has("expandedShelfContentsRenderer") ->
                return collectItemsFrom(collector, item.getObject("expandedShelfContentsRenderer").getArray("items"), channelVerifiedStatus, channelName, channelUrl)
            item.has("continuationItemRenderer") -> return Optional.ofNullable(item.getObject("continuationItemRenderer"))
        }

        return Optional.empty()
    }

    private fun commitVideo(collector: MultiInfoItemsCollector, timeAgoParser: TimeAgoParser, jsonObject: JsonObject,
                            channelVerifiedStatus: VerifiedStatus, channelName: String?, channelUrl: String?) {
        collector.commit(object : YoutubeStreamInfoItemExtractor(jsonObject, timeAgoParser) {
            @Throws(ParsingException::class)
            override fun getUploaderName(): String? {
                return if (channelName.isNullOrEmpty()) super.getUploaderName() else channelName
            }
            @Throws(ParsingException::class)
            override fun getUploaderUrl(): String? {
                return if (channelUrl.isNullOrEmpty()) super.getUploaderName() else channelUrl
            }
            @Throws(ParsingException::class)
            override fun isUploaderVerified(): Boolean {
                return when (channelVerifiedStatus) {
                    VerifiedStatus.VERIFIED -> true
                    VerifiedStatus.UNVERIFIED -> false
                    else -> super.isUploaderVerified()
                }
            }
        })
    }

    private fun commitPlaylist(collector: MultiInfoItemsCollector, jsonObject: JsonObject,
                               channelVerifiedStatus: VerifiedStatus, channelName: String?, channelUrl: String?) {
        collector.commit(object : YoutubePlaylistInfoItemExtractor(jsonObject) {
            @Throws(ParsingException::class)
            override fun getUploaderName(): String? {
                return if (channelName.isNullOrEmpty()) super.getUploaderName() else channelName
            }
            @Throws(ParsingException::class)
            override fun getUploaderUrl(): String? {
                return if (channelUrl.isNullOrEmpty()) super.getUploaderName() else channelUrl
            }
            @Throws(ParsingException::class)
            override fun isUploaderVerified(): Boolean {
                return when (channelVerifiedStatus) {
                    VerifiedStatus.VERIFIED -> true
                    VerifiedStatus.UNVERIFIED -> false
                    else -> super.isUploaderVerified()
                }
            }
        })
    }

    @Throws(IOException::class, ExtractionException::class)
    private fun getNextPageFrom(continuations: JsonObject?, channelIds: List<String?>): Page? {
        if (continuations.isNullOrEmpty()) return null

        val continuationEndpoint = continuations.getObject("continuationEndpoint")
        val continuation = continuationEndpoint.getObject("continuationCommand").getString("token")

        val body = JsonWriter.string(prepareDesktopJsonBuilder(extractorLocalization, extractorContentCountry,
            if (useVisitorData && channelIds.size >= 3) channelIds[2] else null)
            .value("continuation", continuation)
            .done())
            .toByteArray(StandardCharsets.UTF_8)

        return Page(YOUTUBEI_V1_URL + "browse?" + DISABLE_PRETTY_PRINT_PARAMETER, null, channelIds, null, body)
    }

    /**
     * A [YoutubeChannelTabExtractor] for the `Videos` tab, if it has been already
     * fetched.
     */
    class VideosTabExtractor internal constructor(
            service: StreamingService,
            linkHandler: ListLinkHandler,
            private val tabRenderer: JsonObject,
            override var channelHeader: ChannelHeader?,
            override val channelName: String,
            private val channelId: String,
            private val channelUrl: String)
        : YoutubeChannelTabExtractor(service, linkHandler) {

        override val id: String
            get() = channelId

        override val url: String
            get() = channelUrl

        override val tabData: Optional<JsonObject>
            get() = Optional.of(tabRenderer)

        override fun onFetchPage(downloader: Downloader) {
            // Nothing to do, the initial data was already fetched and is stored in the link handler
        }
    }

    /**
     * Enum representing the verified state of a channel
     */
    private enum class VerifiedStatus {
        VERIFIED,
        UNVERIFIED,
        UNKNOWN
    }

    private class YoutubeGridShowRendererChannelInfoItemExtractor(
            gridShowRenderer: JsonObject,
            private val verifiedStatus: VerifiedStatus,
            private val channelName: String?,
            private val channelUrl: String?)
        : YoutubeBaseShowInfoItemExtractor(gridShowRenderer) {

        override fun getUploaderName(): String? {
            return channelName
        }
        override fun getUploaderUrl(): String? {
            return channelUrl
        }
        @Throws(ParsingException::class)
        override fun isUploaderVerified(): Boolean {
            return when (verifiedStatus) {
                VerifiedStatus.VERIFIED -> true
                VerifiedStatus.UNVERIFIED -> false
                else -> throw ParsingException("Could not get uploader verification status")
            }
        }
    }

    companion object {
        private fun commitReel(collector: MultiInfoItemsCollector, reelItemRenderer: JsonObject,
                               channelVerifiedStatus: VerifiedStatus, channelName: String?, channelUrl: String?) {
            collector.commit(object : YoutubeReelInfoItemExtractor(reelItemRenderer) {
                @Throws(ParsingException::class)
                override fun getUploaderName(): String? {
                    return if (channelName.isNullOrEmpty()) super.getUploaderName() else channelName
                }
                @Throws(ParsingException::class)
                override fun getUploaderUrl(): String? {
                    return if (channelUrl.isNullOrEmpty()) super.getUploaderName() else channelUrl
                }
                override fun isUploaderVerified(): Boolean {
                    return channelVerifiedStatus == VerifiedStatus.VERIFIED
                }
            })
        }
    }
}
