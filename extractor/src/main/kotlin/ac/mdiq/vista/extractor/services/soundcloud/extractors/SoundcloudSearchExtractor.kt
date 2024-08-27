package ac.mdiq.vista.extractor.services.soundcloud.extractors

import com.grack.nanojson.JsonArray
import com.grack.nanojson.JsonObject
import com.grack.nanojson.JsonParser
import com.grack.nanojson.JsonParserException
import ac.mdiq.vista.extractor.*
import ac.mdiq.vista.extractor.downloader.Downloader
import ac.mdiq.vista.extractor.exceptions.ExtractionException
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.linkhandler.SearchQueryHandler
import ac.mdiq.vista.extractor.search.SearchExtractor
import ac.mdiq.vista.extractor.services.soundcloud.linkHandler.SoundcloudSearchQueryHandlerFactory
import ac.mdiq.vista.extractor.utils.Parser.compatParseMap
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.net.MalformedURLException
import java.net.URL
import java.util.function.IntUnaryOperator


class SoundcloudSearchExtractor(service: StreamingService, linkHandler: SearchQueryHandler) : SearchExtractor(service, linkHandler) {
    private var initialSearchObject: JsonObject? = null


    override val searchSuggestion: String
        get() = ""

    override val isCorrectedSearch: Boolean
        get() = false


    override val metaInfo: List<MetaInfo>
        get() = emptyList()

    @get:Throws(IOException::class, ExtractionException::class)

    override val initialPage: InfoItemsPage<InfoItem>
        get() = if (initialSearchObject!!.getInt(TOTAL_RESULTS) > SoundcloudSearchQueryHandlerFactory.ITEMS_PER_PAGE)
            InfoItemsPage(collectItems(initialSearchObject!!.getArray(COLLECTION)),
                getNextPageFromCurrentUrl(url) { SoundcloudSearchQueryHandlerFactory.ITEMS_PER_PAGE })
        else InfoItemsPage(collectItems(initialSearchObject!!.getArray(COLLECTION)), null)

    @Throws(IOException::class, ExtractionException::class)
    override fun getPage(page: Page?): InfoItemsPage<InfoItem> {
        require(!(page == null || page.url.isNullOrEmpty())) { "Page doesn't contain an URL" }

        val dl = downloader
        val searchCollection: JsonArray
        val totalResults: Int
        try {
            val response = dl.get(page.url, extractorLocalization).responseBody()
            val result = JsonParser.`object`().from(response)
            searchCollection = result.getArray(COLLECTION)
            totalResults = result.getInt(TOTAL_RESULTS)
        } catch (e: JsonParserException) {
            throw ParsingException("Could not parse json response", e)
        }

        if (getOffsetFromUrl(page.url) + SoundcloudSearchQueryHandlerFactory.ITEMS_PER_PAGE < totalResults) {
            return InfoItemsPage(collectItems(searchCollection), getNextPageFromCurrentUrl(page.url)
                { currentOffset: Int -> currentOffset + SoundcloudSearchQueryHandlerFactory.ITEMS_PER_PAGE })
        }
        return InfoItemsPage(collectItems(searchCollection), null)
    }

    @Throws(IOException::class, ExtractionException::class)
    override fun onFetchPage(downloader: Downloader) {
        val dl = downloader
        val url = url
        try {
            val response = dl.get(url, extractorLocalization).responseBody()
            initialSearchObject = JsonParser.`object`().from(response)
        } catch (e: JsonParserException) {
            throw ParsingException("Could not parse json response", e)
        }
        if (initialSearchObject == null || initialSearchObject!!.getArray(COLLECTION).isEmpty()) throw NothingFoundException("Nothing found")
    }

    private fun collectItems(searchCollection: JsonArray): InfoItemsCollector<InfoItem, InfoItemExtractor> {
        val collector = MultiInfoItemsCollector(serviceId)

        for (result in searchCollection) {
            if (result !is JsonObject) continue
            val kind = result.getString("kind", "")
            when (kind) {
                "user" -> collector.commit(SoundcloudChannelInfoItemExtractor(result))
                "track" -> collector.commit(SoundcloudStreamInfoItemExtractor(result))
                "playlist" -> collector.commit(SoundcloudPlaylistInfoItemExtractor(result))
            }
        }
        return collector
    }

    @Throws(ParsingException::class)
    private fun getNextPageFromCurrentUrl(currentUrl: String, newPageOffsetCalculator: IntUnaryOperator): Page {
        val currentPageOffset = getOffsetFromUrl(currentUrl)
        return Page(currentUrl.replace("&offset=$currentPageOffset", "&offset=" + newPageOffsetCalculator.applyAsInt(currentPageOffset)))
    }

    @Throws(ParsingException::class)
    private fun getOffsetFromUrl(url: String): Int {
        try {
            return compatParseMap(URL(url).query)["offset"]?.toInt() ?: 0
        } catch (e: MalformedURLException) {
            throw ParsingException("Could not get offset from page URL", e)
        } catch (e: UnsupportedEncodingException) {
            throw ParsingException("Could not get offset from page URL", e)
        }
    }

    companion object {
        private const val COLLECTION = "collection"
        private const val TOTAL_RESULTS = "total_results"
    }
}
