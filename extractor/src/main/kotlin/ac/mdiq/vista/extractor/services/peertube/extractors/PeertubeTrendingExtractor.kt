package ac.mdiq.vista.extractor.services.peertube.extractors

import com.grack.nanojson.JsonObject
import com.grack.nanojson.JsonParser
import ac.mdiq.vista.extractor.Page
import ac.mdiq.vista.extractor.StreamingService
import ac.mdiq.vista.extractor.downloader.Downloader
import ac.mdiq.vista.extractor.exceptions.ExtractionException
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.kiosk.KioskExtractor
import ac.mdiq.vista.extractor.linkhandler.ListLinkHandler
import ac.mdiq.vista.extractor.services.peertube.PeertubeParsingHelper.COUNT_KEY
import ac.mdiq.vista.extractor.services.peertube.PeertubeParsingHelper.ITEMS_PER_PAGE
import ac.mdiq.vista.extractor.services.peertube.PeertubeParsingHelper.START_KEY
import ac.mdiq.vista.extractor.services.peertube.PeertubeParsingHelper.collectItemsFrom
import ac.mdiq.vista.extractor.services.peertube.PeertubeParsingHelper.getNextPage
import ac.mdiq.vista.extractor.services.peertube.PeertubeParsingHelper.validate
import ac.mdiq.vista.extractor.stream.StreamInfoItem
import ac.mdiq.vista.extractor.stream.StreamInfoItemsCollector
import java.io.IOException


class PeertubeTrendingExtractor(
        streamingService: StreamingService,
        linkHandler: ListLinkHandler,
        kioskId: String)
    : KioskExtractor<StreamInfoItem>(streamingService, linkHandler, kioskId) {


    @Throws(ParsingException::class)
    override fun getName(): String {
        return id
    }

    @get:Throws(IOException::class, ExtractionException::class)

    override val initialPage: InfoItemsPage<StreamInfoItem>
        get() = getPage(Page("$url&$START_KEY=0&$COUNT_KEY=$ITEMS_PER_PAGE"))

    @Throws(IOException::class, ExtractionException::class)
    override fun getPage(page: Page?): InfoItemsPage<StreamInfoItem> {
        require(!(page == null || page.url.isNullOrEmpty())) { "Page doesn't contain an URL" }

        val response = downloader.get(page.url)

        var json: JsonObject? = null
        if (response != null && response.responseBody().isNotEmpty()) {
            try { json = JsonParser.`object`().from(response.responseBody()) } catch (e: Exception) { throw ParsingException("Could not parse json data for kiosk info", e) }
        }

        if (json != null) {
            validate(json)
            val total = json.getLong("total")

            val collector = StreamInfoItemsCollector(serviceId)
            collectItemsFrom(collector, json, baseUrl)

            return InfoItemsPage(collector, getNextPage(page.url, total))
        } else {
            throw ExtractionException("Unable to get PeerTube kiosk info")
        }
    }

    @Throws(IOException::class, ExtractionException::class)
    override fun onFetchPage(downloader: Downloader) {
    }
}
