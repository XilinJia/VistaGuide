/*
 * Created by Christian Schabesberger on 12.08.17.
 *
 * Copyright (C) 2018 Christian Schabesberger <chris.schabesberger@mailbox.org>
* Copyright (C) 2024 Xilin Jia <https://github.com/XilinJia>
 * YoutubeTrendingExtractor.kt is part of Vista Guide.
 *
 * Vista Guide is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Vista Guide is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Vista Guide. If not, see <https://www.gnu.org/licenses/>.
 */
package ac.mdiq.vista.extractor.services.youtube.extractors

import com.grack.nanojson.JsonObject
import com.grack.nanojson.JsonWriter
import ac.mdiq.vista.extractor.ListExtractor.InfoItemsPage.Companion.emptyPage
import ac.mdiq.vista.extractor.Page
import ac.mdiq.vista.extractor.StreamingService
import ac.mdiq.vista.extractor.downloader.Downloader
import ac.mdiq.vista.extractor.exceptions.ExtractionException
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.kiosk.KioskExtractor
import ac.mdiq.vista.extractor.linkhandler.ListLinkHandler
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.getJsonPostResponse
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.getTextAtKey
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.prepareDesktopJsonBuilder
import ac.mdiq.vista.extractor.stream.StreamInfoItem
import ac.mdiq.vista.extractor.stream.StreamInfoItemsCollector
import java.io.IOException
import java.nio.charset.StandardCharsets


class YoutubeTrendingExtractor(
        service: StreamingService,
        linkHandler: ListLinkHandler,
        kioskId: String)
    : KioskExtractor<StreamInfoItem>(service, linkHandler, kioskId) {

    private var initialData: JsonObject? = null

    @Throws(IOException::class, ExtractionException::class)
    override fun onFetchPage(downloader: Downloader) {
        // @formatter:off
         val body = JsonWriter.string(prepareDesktopJsonBuilder(extractorLocalization,
        extractorContentCountry)
        .value("browseId", "FEtrending")
        .value("params", VIDEOS_TAB_PARAMS)
        .done())
        .toByteArray(StandardCharsets.UTF_8)

                // @formatter:on
        initialData = getJsonPostResponse("browse", body, extractorLocalization)
    }

    override fun getPage(page: Page?): InfoItemsPage<StreamInfoItem> {
        return emptyPage()
    }


    @Throws(ParsingException::class)
    override fun getName(): String {
        val header = initialData!!.getObject("header")
        var name: String? = null
        when {
            header.has("feedTabbedHeaderRenderer") -> name = getTextAtKey(header.getObject("feedTabbedHeaderRenderer"), "title")
            header.has("c4TabbedHeaderRenderer") -> name = getTextAtKey(header.getObject("c4TabbedHeaderRenderer"), "title")
            header.has("pageHeaderRenderer") -> name = getTextAtKey(header.getObject("pageHeaderRenderer"), "pageTitle")
        }

        if (name.isNullOrEmpty()) throw ParsingException("Could not get Trending name")
        return name
    }

    @get:Throws(ParsingException::class)

    override val initialPage: InfoItemsPage<StreamInfoItem>
        get() {
            val collector = StreamInfoItemsCollector(serviceId)
            val timeAgoParser = timeAgoParser
            val tab = trendingTab
            val tabContent = tab.getObject("content")
            val isVideoTab = tab.getObject("endpoint").getObject("browseEndpoint").getString("params", "") == VIDEOS_TAB_PARAMS

            if (tabContent.has("richGridRenderer")) {
                tabContent.getObject("richGridRenderer")
                    .getArray("contents")
                    .stream()
                    .filter { o: Any? -> JsonObject::class.java.isInstance(o) }
                    .map { obj: Any? -> JsonObject::class.java.cast(obj) } // Filter Trending shorts and Recently trending sections
                    .filter { content: JsonObject -> content.has("richItemRenderer") }
                    .map { content: JsonObject ->
                        content.getObject("richItemRenderer")
                            .getObject("content")
                            .getObject("videoRenderer")
                    }
                    .forEachOrdered { videoRenderer: JsonObject -> collector.commit(YoutubeStreamInfoItemExtractor(videoRenderer, timeAgoParser)) }
            } else if (tabContent.has("sectionListRenderer")) {
                val shelves = tabContent.getObject("sectionListRenderer")
                    .getArray("contents")
                    .stream()
                    .filter { o: Any? -> JsonObject::class.java.isInstance(o) }
                    .map { obj: Any? -> JsonObject::class.java.cast(obj) }
                    .flatMap { content: JsonObject -> content.getObject("itemSectionRenderer").getArray("contents").stream() }
                    .filter { o: Any? -> JsonObject::class.java.isInstance(o) }
                    .map { obj: Any? -> JsonObject::class.java.cast(obj) }
                    .map { content: JsonObject -> content.getObject("shelfRenderer") }
                // The first shelf of the Videos tab contains the normal trends
                // Filter Trending shorts and Recently trending sections which have a title,
                // contrary to normal trends
                val items = if (isVideoTab) shelves.findFirst().stream() else shelves.filter { shelfRenderer: JsonObject -> !shelfRenderer.has("title") }

                items.flatMap { shelfRenderer: JsonObject ->
                    shelfRenderer.getObject("content")
                        .getObject("expandedShelfContentsRenderer")
                        .getArray("items")
                        .stream()
                }
                    .filter { o: Any? -> JsonObject::class.java.isInstance(o) }
                    .map { obj: Any? -> JsonObject::class.java.cast(obj) }
                    .map { item: JsonObject -> item.getObject("videoRenderer") }
                    .forEachOrdered { videoRenderer: JsonObject ->
                        collector.commit(YoutubeStreamInfoItemExtractor(videoRenderer, timeAgoParser))
                    }
            }
            return InfoItemsPage(collector, null)
        }

    @get:Throws(ParsingException::class)
    private val trendingTab: JsonObject
        get() = initialData!!.getObject("contents")
            .getObject("twoColumnBrowseResultsRenderer")
            .getArray("tabs")
            .stream()
            .filter { o: Any? -> JsonObject::class.java.isInstance(o) }
            .map { obj: Any? -> JsonObject::class.java.cast(obj) }
            .map { tab: JsonObject -> tab.getObject("tabRenderer") }
            .filter { tabRenderer: JsonObject -> tabRenderer.getBoolean("selected") }
            .filter { tabRenderer: JsonObject -> tabRenderer.has("content") } // There should be at most one tab selected
            .findFirst()
            .orElseThrow { ParsingException("Could not get \"Now\" or \"Videos\" trending tab") }

    companion object {
        const val KIOSK_ID: String = "Trending"

        private const val VIDEOS_TAB_PARAMS = "4gIOGgxtb3N0X3BvcHVsYXI%3D"
    }
}
