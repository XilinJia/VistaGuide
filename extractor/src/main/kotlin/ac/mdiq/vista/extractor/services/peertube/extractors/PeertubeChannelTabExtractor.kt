package ac.mdiq.vista.extractor.services.peertube.extractors

import com.grack.nanojson.JsonObject
import com.grack.nanojson.JsonParser
import ac.mdiq.vista.extractor.InfoItem
import ac.mdiq.vista.extractor.MultiInfoItemsCollector
import ac.mdiq.vista.extractor.Page
import ac.mdiq.vista.extractor.StreamingService
import ac.mdiq.vista.extractor.channel.tabs.ChannelTabExtractor
import ac.mdiq.vista.extractor.downloader.Downloader
import ac.mdiq.vista.extractor.exceptions.ExtractionException
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.linkhandler.ListLinkHandler
import ac.mdiq.vista.extractor.services.peertube.PeertubeParsingHelper.COUNT_KEY
import ac.mdiq.vista.extractor.services.peertube.PeertubeParsingHelper.ITEMS_PER_PAGE
import ac.mdiq.vista.extractor.services.peertube.PeertubeParsingHelper.START_KEY
import ac.mdiq.vista.extractor.services.peertube.PeertubeParsingHelper.collectItemsFrom
import ac.mdiq.vista.extractor.services.peertube.PeertubeParsingHelper.getNextPage
import ac.mdiq.vista.extractor.services.peertube.PeertubeParsingHelper.validate
import ac.mdiq.vista.extractor.services.peertube.linkHandler.PeertubeChannelLinkHandlerFactory
import ac.mdiq.vista.extractor.services.peertube.linkHandler.PeertubeChannelTabLinkHandlerFactory.Companion.getUrlSuffix
import java.io.IOException


class PeertubeChannelTabExtractor(service: StreamingService, linkHandler: ListLinkHandler) : ChannelTabExtractor(service, linkHandler) {
//    override val baseUrl: String
//
//    init {
//        baseUrl = baseUrl
//    }

    override fun onFetchPage(downloader: Downloader) {
    }

    @get:Throws(IOException::class, ExtractionException::class)

    override val initialPage: InfoItemsPage<InfoItem>
        get() = getPage(Page("$baseUrl${PeertubeChannelLinkHandlerFactory.API_ENDPOINT}$id${getUrlSuffix(getName())}?$START_KEY=0&$COUNT_KEY=$ITEMS_PER_PAGE"))

    @Throws(IOException::class, ExtractionException::class)
    override fun getPage(page: Page?): InfoItemsPage<InfoItem> {
        require(!(page == null || page.url.isNullOrEmpty())) { "Page doesn't contain an URL" }

        val response = downloader.get(page.url)

        var pageJson: JsonObject? = null
        if (response != null && response.responseBody().isNotEmpty()) {
            try {
                pageJson = JsonParser.`object`().from(response.responseBody())
            } catch (e: Exception) {
                throw ParsingException("Could not parse json data for account info", e)
            }
        }

        if (pageJson == null) {
            throw ExtractionException("Unable to get account channel list")
        }
        validate(pageJson)

        val collector = MultiInfoItemsCollector(serviceId)
        collectItemsFrom(collector, pageJson, baseUrl)

        return InfoItemsPage(collector, getNextPage(page.url, pageJson.getLong("total")))
    }
}
