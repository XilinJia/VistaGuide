package ac.mdiq.vista.extractor.services.youtube.extractors

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import ac.mdiq.vista.extractor.ListExtractor.InfoItemsPage.Companion.emptyPage
import ac.mdiq.vista.extractor.Page
import ac.mdiq.vista.extractor.StreamingService
import ac.mdiq.vista.extractor.downloader.Downloader
import ac.mdiq.vista.extractor.exceptions.ContentNotAvailableException
import ac.mdiq.vista.extractor.exceptions.ExtractionException
import ac.mdiq.vista.extractor.feed.FeedExtractor
import ac.mdiq.vista.extractor.linkhandler.ListLinkHandler
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.getFeedUrlFrom
import ac.mdiq.vista.extractor.stream.StreamInfoItem
import ac.mdiq.vista.extractor.stream.StreamInfoItemsCollector
import java.io.IOException


class YoutubeFeedExtractor(service: StreamingService, linkHandler: ListLinkHandler) : FeedExtractor(service, linkHandler) {
    private var document: Document? = null

    @Throws(IOException::class, ExtractionException::class)
    override fun onFetchPage(downloader: Downloader) {
        val channelIdOrUser = getLinkHandler().id
        val feedUrl = getFeedUrlFrom(channelIdOrUser)

        val response = downloader.get(feedUrl)
        if (response.responseCode() == 404) throw ContentNotAvailableException("Could not get feed: 404 - not found")
        document = Jsoup.parse(response.responseBody())
    }


    override val initialPage: InfoItemsPage<StreamInfoItem>
        get() {
            val entries = document!!.select("feed > entry")
            val collector = StreamInfoItemsCollector(serviceId)

            for (entryElement in entries) collector.commit(YoutubeFeedInfoItemExtractor(entryElement))
            return InfoItemsPage(collector, null)
        }


    override val id: String
        get() = url.replace(WEBSITE_CHANNEL_BASE_URL, "")


    override val url: String
        get() {
            val authorUriElement = document!!.select("feed > author > uri").first()
            if (authorUriElement != null) {
                val authorUriElementText = authorUriElement.text()
                if (authorUriElementText != "") return authorUriElementText
            }

            val linkElement = document!!.select("feed > link[rel*=alternate]").first()
            if (linkElement != null) return linkElement.attr("href")
            return ""
        }


    override fun getName(): String {
        val nameElement = document!!.select("feed > author > name").first() ?: return ""
        return nameElement.text()
    }

    override fun getPage(page: Page?): InfoItemsPage<StreamInfoItem> {
        return emptyPage()
    }

    companion object {
        private const val WEBSITE_CHANNEL_BASE_URL = "https://www.youtube.com/channel/"
    }
}
