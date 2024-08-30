package ac.mdiq.vista.extractor.services.peertube.extractors

import com.grack.nanojson.JsonObject
import com.grack.nanojson.JsonParser
import ac.mdiq.vista.extractor.*
import ac.mdiq.vista.extractor.downloader.Downloader
import ac.mdiq.vista.extractor.exceptions.ExtractionException
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.linkhandler.SearchQueryHandler
import ac.mdiq.vista.extractor.search.SearchExtractor
import ac.mdiq.vista.extractor.services.peertube.PeertubeParsingHelper.COUNT_KEY
import ac.mdiq.vista.extractor.services.peertube.PeertubeParsingHelper.ITEMS_PER_PAGE
import ac.mdiq.vista.extractor.services.peertube.PeertubeParsingHelper.START_KEY
import ac.mdiq.vista.extractor.services.peertube.PeertubeParsingHelper.collectItemsFrom
import ac.mdiq.vista.extractor.services.peertube.PeertubeParsingHelper.getNextPage
import ac.mdiq.vista.extractor.services.peertube.PeertubeParsingHelper.validate
import java.io.IOException


// if we should use PeertubeSepiaStreamInfoItemExtractor
class PeertubeSearchExtractor @JvmOverloads constructor(
        service: StreamingService,
        linkHandler: SearchQueryHandler,
        private val sepia: Boolean = false)
    : SearchExtractor(service, linkHandler) {


    override val searchSuggestion: String
        get() = ""

    override val isCorrectedSearch: Boolean
        get() = false


    override val metaInfo: List<MetaInfo>
        get() = emptyList()

    @get:Throws(IOException::class, ExtractionException::class)

    override val initialPage: InfoItemsPage<InfoItem>
        get() = getPage(Page("$url&$START_KEY=0&$COUNT_KEY=$ITEMS_PER_PAGE"))

    @Throws(IOException::class, ExtractionException::class)
    override fun getPage(page: Page?): InfoItemsPage<InfoItem> {
        require(!(page == null || page.url.isNullOrEmpty())) { "Page doesn't contain an URL" }

        val response = downloader.get(page.url)

        var json: JsonObject? = null
        if (response != null && response.responseBody().isNotEmpty()) {
            try { json = JsonParser.`object`().from(response.responseBody()) } catch (e: Exception) { throw ParsingException("Could not parse json data for search info", e) }
        }

        if (json != null) {
            validate(json)
            val total = json.getLong("total")

            val collector = MultiInfoItemsCollector(serviceId)
            collectItemsFrom(collector, json, baseUrl, sepia)

            return InfoItemsPage(collector, getNextPage(page.url, total))
        } else {
            throw ExtractionException("Unable to get PeerTube search info")
        }
    }

    @Throws(IOException::class, ExtractionException::class)
    override fun onFetchPage(downloader: Downloader) {
    }
}
