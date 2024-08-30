/*
 * Created by Christian Schabesberger on 25.07.16.
 *
 * Copyright (C) 2018 Christian Schabesberger <chris.schabesberger@mailbox.org>
* Copyright (C) 2024 Xilin Jia <https://github.com/XilinJia>
 * YoutubeChannelExtractor.kt is part of Vista Guide.
 *
 * Vista Guide is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Vista Guide is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Vista Guide.  If not, see <https://www.gnu.org/licenses/>.
 */
package ac.mdiq.vista.extractor.services.youtube.extractors

import com.grack.nanojson.JsonArray
import com.grack.nanojson.JsonObject
import ac.mdiq.vista.extractor.Image
import ac.mdiq.vista.extractor.StreamingService
import ac.mdiq.vista.extractor.channel.ChannelExtractor
import ac.mdiq.vista.extractor.channel.tabs.ChannelTabExtractor
import ac.mdiq.vista.extractor.channel.tabs.ChannelTabs
import ac.mdiq.vista.extractor.downloader.Downloader
import ac.mdiq.vista.extractor.exceptions.ExtractionException
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.linkhandler.ListLinkHandler
import ac.mdiq.vista.extractor.linkhandler.ReadyChannelTabListLinkHandler
import ac.mdiq.vista.extractor.linkhandler.ReadyChannelTabListLinkHandler.ChannelTabExtractorBuilder
import ac.mdiq.vista.extractor.services.youtube.YoutubeChannelHelper.ChannelHeader
import ac.mdiq.vista.extractor.services.youtube.YoutubeChannelHelper.ChannelHeader.HeaderType
import ac.mdiq.vista.extractor.services.youtube.YoutubeChannelHelper.getChannelAgeGateRenderer
import ac.mdiq.vista.extractor.services.youtube.YoutubeChannelHelper.getChannelHeader
import ac.mdiq.vista.extractor.services.youtube.YoutubeChannelHelper.getChannelId
import ac.mdiq.vista.extractor.services.youtube.YoutubeChannelHelper.getChannelName
import ac.mdiq.vista.extractor.services.youtube.YoutubeChannelHelper.getChannelResponse
import ac.mdiq.vista.extractor.services.youtube.YoutubeChannelHelper.isChannelVerified
import ac.mdiq.vista.extractor.services.youtube.YoutubeChannelHelper.resolveChannelId
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.getFeedUrlFrom
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.getTextFromObject
import ac.mdiq.vista.extractor.services.youtube.extractors.YoutubeChannelTabExtractor.VideosTabExtractor
import ac.mdiq.vista.extractor.services.youtube.linkHandler.YoutubeChannelLinkHandlerFactory
import ac.mdiq.vista.extractor.services.youtube.linkHandler.YoutubeChannelTabLinkHandlerFactory
import ac.mdiq.vista.extractor.utils.Utils.mixedNumberWordToLong
import java.io.IOException
import java.util.*
import java.util.function.Consumer
import java.util.stream.Collectors



class YoutubeChannelExtractor(service: StreamingService, linkHandler: ListLinkHandler) : ChannelExtractor(service, linkHandler) {
    private var jsonResponse: JsonObject? = null

    private var channelHeader: ChannelHeader? = null

    private var channelId: String? = null

    /**
     * If a channel is age-restricted, its pages are only accessible to logged-in and
     * age-verified users, we get an `channelAgeGateRenderer` in this case, containing only
     * the following metadata: channel name and channel avatar.
     *
     * This restriction doesn't seem to apply to all countries.
     *
     */
    private var channelAgeGateRenderer: JsonObject? = null

    @get:Throws(ParsingException::class)
    private val tabsForNonAgeRestrictedChannels: List<ListLinkHandler>
        get() {
            val responseTabs = jsonResponse!!.getObject(CONTENTS)
                .getObject("twoColumnBrowseResultsRenderer")
                .getArray("tabs")

            val tabs: MutableList<ListLinkHandler> = ArrayList()
            val addNonVideosTab = Consumer<String> { tabName: String ->
                try {
                    tabs.add(YoutubeChannelTabLinkHandlerFactory.instance.fromQuery(channelId?:"", listOf(tabName), ""))
                } catch (ignored: ParsingException) {/* Do not add the tab if we couldn't create the LinkHandler */ }
            }

            val name = getName()
            val url = url
            val id = id

            responseTabs.stream()
                .filter { o: Any? -> JsonObject::class.java.isInstance(o) }
                .map { obj: Any? -> JsonObject::class.java.cast(obj) }
                .filter { tab: JsonObject -> tab.has(TAB_RENDERER) }
                .map { tab: JsonObject -> tab.getObject(TAB_RENDERER) }
                .forEach { tabRenderer: JsonObject ->
                    val tabUrl = tabRenderer.getObject("endpoint")
                        .getObject("commandMetadata")
                        .getObject("webCommandMetadata")
                        .getString("url")
                    if (tabUrl != null) {
                        val urlParts = tabUrl.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                        if (urlParts.isEmpty()) return@forEach

                        val urlSuffix = urlParts[urlParts.size - 1]

                        when (urlSuffix) {
                            "videos" ->                                 // Since the Videos tab has already its contents fetched, make
                                // sure it is in the first position
                                // YoutubeChannelTabExtractor still supports fetching this tab
                                tabs.add(0, ReadyChannelTabListLinkHandler(tabUrl, channelId!!, ChannelTabs.VIDEOS,
                                    object: ChannelTabExtractorBuilder {
                                        override fun build(service: StreamingService, linkHandler: ListLinkHandler): ChannelTabExtractor {
                                            return VideosTabExtractor(service, linkHandler, tabRenderer, channelHeader, name, id, url)
                                        }
                                    }))

                            "shorts" -> addNonVideosTab.accept(ChannelTabs.SHORTS)
                            "streams" -> addNonVideosTab.accept(ChannelTabs.LIVESTREAMS)
                            "releases" -> addNonVideosTab.accept(ChannelTabs.ALBUMS)
                            "playlists" -> addNonVideosTab.accept(ChannelTabs.PLAYLISTS)
                            else -> {}
                        }
                    }
                }

            return Collections.unmodifiableList(tabs)
        }

    @get:Throws(ParsingException::class)
    private val tabsForAgeRestrictedChannels: List<ListLinkHandler>
        get() {
            // As we don't have access to the channel tabs list, consider that the channel has videos,
            // shorts and livestreams, the data only accessible without login on YouTube's desktop
            // client using uploads system playlists
            // The playlists channel tab is still available on YouTube Music, but this is not
            // implemented in the extractor

            val tabs: MutableList<ListLinkHandler> = ArrayList()
            val channelUrl = url

            val addTab =
                Consumer { tabName: String ->
                    tabs.add(ReadyChannelTabListLinkHandler("$channelUrl/$tabName", channelId!!, tabName,
                        object: ChannelTabExtractorBuilder {
                            override fun build(service: StreamingService, linkHandler: ListLinkHandler): ChannelTabExtractor {
                                return YoutubeChannelTabPlaylistExtractor(service, linkHandler)
                            }
                        }
                    ))
                }

            addTab.accept(ChannelTabs.VIDEOS)
            addTab.accept(ChannelTabs.SHORTS)
            addTab.accept(ChannelTabs.LIVESTREAMS)
            return Collections.unmodifiableList(tabs)
        }

    @Throws(IOException::class, ExtractionException::class)
    override fun onFetchPage(downloader: Downloader) {
        val channelPath = super.id
        val id = resolveChannelId(channelPath)
        // Fetch Videos tab
        val data = getChannelResponse(id, "EgZ2aWRlb3PyBgQKAjoA", extractorLocalization, extractorContentCountry)

        jsonResponse = data.jsonResponse
        channelHeader = getChannelHeader(jsonResponse!!)
        channelId = data.channelId
        channelAgeGateRenderer = getChannelAgeGateRenderer(jsonResponse!!)
    }

    @get:Throws(ParsingException::class)
    override val url: String
        get() = try { YoutubeChannelLinkHandlerFactory.instance.getUrl("channel/$id") } catch (e: ParsingException) { super.url }

    @get:Throws(ParsingException::class)
    override val id: String
        get() {
            assertPageFetched()
            return getChannelId(channelHeader!!, jsonResponse!!, channelId!!)
        }

    @Throws(ParsingException::class)
    override fun getName(): String {
        assertPageFetched()
        return getChannelName(channelHeader!!, jsonResponse!!, channelAgeGateRenderer)
    }

    @Throws(ParsingException::class)
    override fun getAvatars(): List<Image> {
        assertPageFetched()
        if (channelAgeGateRenderer != null) {
            return channelAgeGateRenderer!!.getObject(AVATAR)
                ?.getArray(THUMBNAILS)
                ?.let { YoutubeParsingHelper.getImagesFromThumbnailsArray(it) }
                ?: throw ParsingException("Could not get avatars")
        }

        return channelHeader?.let { header: ChannelHeader? ->
            when (header!!.headerType) {
                HeaderType.PAGE -> {
                    val imageObj = header.json.getObject(CONTENT).getObject(PAGE_HEADER_VIEW_MODEL).getObject(IMAGE)
                    if (imageObj.has(CONTENT_PREVIEW_IMAGE_VIEW_MODEL))
                        return@let imageObj.getObject(CONTENT_PREVIEW_IMAGE_VIEW_MODEL).getObject(IMAGE).getArray(SOURCES)
                    if (imageObj.has("decoratedAvatarViewModel"))
                        return@let imageObj.getObject("decoratedAvatarViewModel")
                            .getObject(AVATAR)
                            .getObject("avatarViewModel")
                            .getObject(IMAGE)
                            .getArray(SOURCES)
                    // Return an empty avatar array as a fallback
                    return@let JsonArray()
                }
                HeaderType.INTERACTIVE_TABBED -> return@let header.json.getObject("boxArt").getArray(THUMBNAILS)
                HeaderType.C4_TABBED, HeaderType.CAROUSEL -> return@let header.json.getObject(AVATAR).getArray(THUMBNAILS)
                else -> return@let header.json.getObject(AVATAR).getArray(THUMBNAILS)
            }
        }
            ?.let {YoutubeParsingHelper.getImagesFromThumbnailsArray(it)}
//            .map<List<Image>>(Function<JsonArray, List<Image>> { obj: JsonArray -> obj.getImagesFromThumbnailsArray() })
            ?: throw ParsingException("Could not get avatars")
    }

    override fun getBanners(): List<Image> {
        assertPageFetched()
        if (channelAgeGateRenderer != null) return listOf()

        return channelHeader?.let { header: ChannelHeader ->
            if (header.headerType == HeaderType.PAGE) {
                val pageHeaderViewModel = header.json.getObject(CONTENT).getObject(PAGE_HEADER_VIEW_MODEL)
                if (pageHeaderViewModel.has(BANNER))
                    return@let pageHeaderViewModel.getObject(BANNER).getObject("imageBannerViewModel").getObject(IMAGE).getArray(SOURCES)

                // No banner is available (this should happen on pageHeaderRenderers of
                // system channels), use an empty JsonArray instead
                return@let JsonArray()
            }
            header.json.getObject(BANNER).getArray(THUMBNAILS)
        }
            ?.let {YoutubeParsingHelper.getImagesFromThumbnailsArray(it)}
//            .map<List<Image>>(Function<JsonArray, List<Image>> { obj: JsonArray -> obj.getImagesFromThumbnailsArray() })
            ?: listOf()
    }

    @Throws(ParsingException::class)
    override fun getFeedUrl(): String {
        // RSS feeds are accessible for age-restricted channels, no need to check whether a channel
        // has a channelAgeGateRenderer
        try { return getFeedUrlFrom(id) } catch (e: Exception) { throw ParsingException("Could not get feed URL", e) }
    }

    @Throws(ParsingException::class)
    override fun getSubscriberCount(): Long {
        assertPageFetched()
        if (channelAgeGateRenderer != null) return UNKNOWN_SUBSCRIBER_COUNT

        if (channelHeader != null) {
            val header = channelHeader!!

            // No subscriber count is available on interactiveTabbedHeaderRenderer header
            if (header.headerType == HeaderType.INTERACTIVE_TABBED) return UNKNOWN_SUBSCRIBER_COUNT

            val headerJson = header.json
            if (header.headerType == HeaderType.PAGE) return getSubscriberCountFromPageChannelHeader(headerJson)

            var textObject: JsonObject? = null

            when {
                headerJson.has("subscriberCountText") -> textObject = headerJson.getObject("subscriberCountText")
                headerJson.has("subtitle") -> textObject = headerJson.getObject("subtitle")
            }

            if (textObject != null) {
                try { return mixedNumberWordToLong(getTextFromObject(textObject)) } catch (e: NumberFormatException) { throw ParsingException("Could not get subscriber count", e) }
            }
        }

        return UNKNOWN_SUBSCRIBER_COUNT
    }

    @Throws(ParsingException::class)
    private fun getSubscriberCountFromPageChannelHeader(headerJson: JsonObject): Long {
        val metadataObject = headerJson.getObject(CONTENT)
            .getObject(PAGE_HEADER_VIEW_MODEL)
            .getObject(METADATA)
        if (metadataObject.has("contentMetadataViewModel")) {
            val metadataPart = metadataObject.getObject("contentMetadataViewModel")
                .getArray("metadataRows")
                .stream()
                .filter { o: Any? -> JsonObject::class.java.isInstance(o) }
                .map { obj: Any? -> JsonObject::class.java.cast(obj) }
                .map { metadataRow: JsonObject -> metadataRow.getArray("metadataParts") }  /*
                    Find metadata parts which have two elements: channel handle and subscriber
                    count.

                    On autogenerated music channels, the subscriber count is not shown with this
                    header.

                    Use the first metadata parts object found.
                     */
                .filter { metadataParts: JsonArray? -> metadataParts!!.size == 2 }
                .findFirst()
                .orElse(null)
            // As the parsing of the metadata parts object needed to get the subscriber count
            // is fragile, return UNKNOWN_SUBSCRIBER_COUNT when it cannot be got
            if (metadataPart == null) return UNKNOWN_SUBSCRIBER_COUNT

            try {
                // The subscriber count is at the same position for all languages as of 02/03/2024
                return mixedNumberWordToLong(metadataPart.getObject(0)
                    .getObject("text")
                    .getString(CONTENT))
            } catch (e: NumberFormatException) { throw ParsingException("Could not get subscriber count", e) }
        }

        // If the channel header has no contentMetadataViewModel (which is the case for system
        // channels using this header), return UNKNOWN_SUBSCRIBER_COUNT
        return UNKNOWN_SUBSCRIBER_COUNT
    }

    @Throws(ParsingException::class)
    override fun getDescription(): String {
        assertPageFetched()
        if (channelAgeGateRenderer != null) return ""

        try {
            if (channelHeader != null) {
                val header = channelHeader!!
                /*
                In an interactiveTabbedHeaderRenderer, the real description, is only available
                in its header
                The other one returned in non-About tabs accessible in the
                microformatDataRenderer object of the response may be completely different
                The description extracted is incomplete and the original one can be only
                accessed from the About tab
                 */
                if (header.headerType == HeaderType.INTERACTIVE_TABBED) return getTextFromObject(header.json.getObject("description")) ?:""
            }

            return jsonResponse!!.getObject(METADATA)
                .getObject("channelMetadataRenderer")
                .getString("description") ?: ""
        } catch (e: Exception) { throw ParsingException("Could not get channel description", e) }
    }

    override fun getParentChannelName(): String {
        return ""
    }

    override fun getParentChannelUrl(): String {
        return ""
    }

    override fun getParentChannelAvatars(): List<Image> {
        return listOf()
    }

    @Throws(ParsingException::class)
    override fun isVerified(): Boolean {
        assertPageFetched()
        // Verified status is unknown with channelAgeGateRenderers, return false in this case
        if (channelAgeGateRenderer != null) return false

        return isChannelVerified(channelHeader ?: throw ParsingException("Could not get verified status"))
    }

    @Throws(ParsingException::class)
    override fun getTabs(): List<ListLinkHandler> {
        assertPageFetched()
        if (channelAgeGateRenderer == null) return tabsForNonAgeRestrictedChannels
        return tabsForAgeRestrictedChannels
    }

    @Throws(ParsingException::class)
    override fun getTags(): List<String> {
        assertPageFetched()
        if (channelAgeGateRenderer != null) return listOf()

        return jsonResponse!!.getObject("microformat")
            .getObject("microformatDataRenderer")
            .getArray("tags")
            .stream()
            .filter { o: Any? -> String::class.java.isInstance(o) }
            .map { obj: Any? -> String::class.java.cast(obj) }
            .collect(Collectors.toUnmodifiableList())
    }

    companion object {
        // Constants of objects used multiples from channel responses
        private const val IMAGE = "image"
        private const val CONTENTS = "contents"
        private const val CONTENT_PREVIEW_IMAGE_VIEW_MODEL = "contentPreviewImageViewModel"
        private const val PAGE_HEADER_VIEW_MODEL = "pageHeaderViewModel"
        private const val TAB_RENDERER = "tabRenderer"
        private const val CONTENT = "content"
        private const val METADATA = "metadata"
        private const val AVATAR = "avatar"
        private const val THUMBNAILS = "thumbnails"
        private const val SOURCES = "sources"
        private const val BANNER = "banner"
    }
}
