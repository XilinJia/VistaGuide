package ac.mdiq.vista.extractor.services.youtube.extractors

import com.grack.nanojson.JsonArray
import com.grack.nanojson.JsonObject
import com.grack.nanojson.JsonWriter
import ac.mdiq.vista.extractor.*
import ac.mdiq.vista.extractor.downloader.Downloader
import ac.mdiq.vista.extractor.exceptions.ExtractionException
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.linkhandler.SearchQueryHandler
import ac.mdiq.vista.extractor.search.SearchExtractor
import ac.mdiq.vista.extractor.services.youtube.YoutubeMetaInfoHelper.getMetaInfo
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.DISABLE_PRETTY_PRINT_PARAMETER
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.YOUTUBEI_V1_URL
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.getJsonPostResponse
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.getTextFromObject
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.prepareDesktopJsonBuilder
import ac.mdiq.vista.extractor.services.youtube.linkHandler.YoutubeSearchQueryHandlerFactory
import ac.mdiq.vista.extractor.services.youtube.linkHandler.YoutubeSearchQueryHandlerFactory.Companion.getSearchParameter
import ac.mdiq.vista.extractor.utils.JsonUtils.getString
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.util.*

/*
* Created by Christian Schabesberger on 22.07.2018
*
* Copyright (C) 2018 Christian Schabesberger <chris.schabesberger@mailbox.org>
* Copyright (C) 2024 Xilin Jia <https://github.com/XilinJia>
* YoutubeSearchExtractor.kt is part of Vista Guide.
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
* along with Vista Guide.  If not, see <http://www.gnu.org/licenses/>.
*/
class YoutubeSearchExtractor(service: StreamingService, linkHandler: SearchQueryHandler) : SearchExtractor(service, linkHandler) {

    private val searchType: String?
    private val extractVideoResults: Boolean
    private val extractChannelResults: Boolean
    private val extractPlaylistResults: Boolean

    private var initialData: JsonObject? = null

    @get:Throws(ParsingException::class)
    override val url: String
        get() = super.url + "&gl=" + extractorContentCountry.countryCode

    @get:Throws(ParsingException::class)
    override val searchSuggestion: String
        get() {
            val itemSectionRenderer = initialData!!.getObject("contents")
                .getObject("twoColumnSearchResultsRenderer")
                .getObject("primaryContents")
                .getObject("sectionListRenderer")
                .getArray("contents")
                .getObject(0)
                .getObject("itemSectionRenderer")
            val didYouMeanRenderer = itemSectionRenderer.getArray("contents")
                .getObject(0)
                .getObject("didYouMeanRenderer")

            if (!didYouMeanRenderer.isEmpty()) return getString(didYouMeanRenderer, "correctedQueryEndpoint.searchEndpoint.query")

            return Objects.requireNonNullElse(
                getTextFromObject(itemSectionRenderer.getArray("contents")
                    .getObject(0)
                    .getObject("showingResultsForRenderer")
                    .getObject("correctedQuery")), "")
        }

    override val isCorrectedSearch: Boolean
        get() {
            val showingResultsForRenderer = initialData!!.getObject("contents")
                .getObject("twoColumnSearchResultsRenderer").getObject("primaryContents")
                .getObject("sectionListRenderer").getArray("contents").getObject(0)
                .getObject("itemSectionRenderer").getArray("contents").getObject(0)
                .getObject("showingResultsForRenderer")
            return !showingResultsForRenderer.isEmpty()
        }

    @get:Throws(ParsingException::class)
    override val metaInfo: List<MetaInfo>
        get() = getMetaInfo(
            initialData!!.getObject("contents")
                .getObject("twoColumnSearchResultsRenderer")
                .getObject("primaryContents")
                .getObject("sectionListRenderer")
                .getArray("contents"))

    @get:Throws(IOException::class, ExtractionException::class)
    override val initialPage: InfoItemsPage<InfoItem>
        get() {
            val collector = MultiInfoItemsCollector(serviceId)
            val sections = initialData!!.getObject("contents")
                .getObject("twoColumnSearchResultsRenderer")
                .getObject("primaryContents")
                .getObject("sectionListRenderer")
                .getArray("contents")
            var nextPage: Page? = null
            for (section in sections) {
                val sectionJsonObject = section as JsonObject
                if (sectionJsonObject.has("itemSectionRenderer")) {
                    val itemSectionRenderer = sectionJsonObject.getObject("itemSectionRenderer")
                    collectStreamsFrom(collector, itemSectionRenderer.getArray("contents"))
                } else if (sectionJsonObject.has("continuationItemRenderer"))
                    nextPage = getNextPageFrom(sectionJsonObject.getObject("continuationItemRenderer"))
            }
            return InfoItemsPage(collector, nextPage)
        }

    init {
        val contentFilters: List<String?> = linkHandler.contentFilters
        searchType = if (contentFilters.isEmpty()) null else contentFilters[0]
        // Save whether we should extract video, channel and playlist results depending on the
        // requested search type, as YouTube returns sometimes videos inside channel search results
        // If no search type is provided or ALL filter is requested, extract everything
        extractVideoResults = searchType == null || YoutubeSearchQueryHandlerFactory.ALL == searchType || YoutubeSearchQueryHandlerFactory.VIDEOS == searchType
        extractChannelResults = searchType == null || YoutubeSearchQueryHandlerFactory.ALL == searchType || YoutubeSearchQueryHandlerFactory.CHANNELS == searchType
        extractPlaylistResults = searchType == null || YoutubeSearchQueryHandlerFactory.ALL == searchType || YoutubeSearchQueryHandlerFactory.PLAYLISTS == searchType
    }

    @Throws(IOException::class, ExtractionException::class)
    override fun onFetchPage(downloader: Downloader) {
        val query = super.searchString
        val localization = extractorLocalization
        val params = getSearchParameter(searchType)

        val jsonBody = prepareDesktopJsonBuilder(localization, extractorContentCountry).value("query", query)
        if (params.isNotEmpty()) { jsonBody.value("params", params) }

        val body = JsonWriter.string(jsonBody.done()).toByteArray(StandardCharsets.UTF_8)
        initialData = getJsonPostResponse("search", body, localization)
    }

    @Throws(IOException::class, ExtractionException::class)
    override fun getPage(page: Page?): InfoItemsPage<InfoItem> {
        require(!(page == null || page.url.isNullOrEmpty())) { "Page doesn't contain an URL" }

        val localization = extractorLocalization
        val collector = MultiInfoItemsCollector(serviceId)

        // @formatter:off
         val json = JsonWriter.string(prepareDesktopJsonBuilder(localization,
        extractorContentCountry)
        .value("continuation", page.id)
        .done())
        .toByteArray(StandardCharsets.UTF_8)

                // @formatter:on
        val ajaxJson = getJsonPostResponse("search", json, localization)
        val continuationItems = ajaxJson.getArray("onResponseReceivedCommands")
            .getObject(0)
            .getObject("appendContinuationItemsAction")
            .getArray("continuationItems")

        val contents = continuationItems.getObject(0)
            .getObject("itemSectionRenderer")
            .getArray("contents")
        collectStreamsFrom(collector, contents)

        return InfoItemsPage(collector, getNextPageFrom(continuationItems.getObject(1).getObject("continuationItemRenderer")))
    }

    @Throws(NothingFoundException::class)
    private fun collectStreamsFrom(collector: MultiInfoItemsCollector,  contents: JsonArray) {
        val timeAgoParser = timeAgoParser
        for (content in contents) {
            val item = content as JsonObject
//            println("YoutubeSearchExtractor collectStreamsFrom $extractVideoResults $extractChannelResults $extractPlaylistResults item: ${item}")
            when {
                item.has("backgroundPromoRenderer") -> throw NothingFoundException(getTextFromObject(item.getObject("backgroundPromoRenderer").getObject("bodyText")))
                extractVideoResults && item.has("videoRenderer") -> collector.commit(YoutubeStreamInfoItemExtractor(item.getObject("videoRenderer"), timeAgoParser))
                extractChannelResults && item.has("channelRenderer") -> collector.commit(YoutubeChannelInfoItemExtractor(item.getObject("channelRenderer")))
                extractPlaylistResults && item.has("playlistRenderer") -> collector.commit(YoutubePlaylistInfoItemExtractor(item.getObject("playlistRenderer")))
            }
//            println("YoutubeSearchExtractor collectStreamsFrom collector: ${collector.getItems().size}")
        }
    }

    private fun getNextPageFrom(continuationItemRenderer: JsonObject): Page? {
        if (continuationItemRenderer.isEmpty()) return null

        val token = continuationItemRenderer.getObject("continuationEndpoint")
            .getObject("continuationCommand")
            .getString("token")

        val url: String = YOUTUBEI_V1_URL + "search?" + DISABLE_PRETTY_PRINT_PARAMETER

        return Page(url, token)
    }
}
