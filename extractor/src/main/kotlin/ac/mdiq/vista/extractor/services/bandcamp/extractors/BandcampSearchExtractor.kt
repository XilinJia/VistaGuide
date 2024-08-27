// Created by Fynn Godau 2019, licensed GNU GPL version 3 or later
package ac.mdiq.vista.extractor.services.bandcamp.extractors

import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import ac.mdiq.vista.extractor.*
import ac.mdiq.vista.extractor.ListExtractor.InfoItemsPage.Companion.emptyPage
import ac.mdiq.vista.extractor.Page.Companion.isValid
import ac.mdiq.vista.extractor.downloader.Downloader
import ac.mdiq.vista.extractor.exceptions.ExtractionException
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.linkhandler.SearchQueryHandler
import ac.mdiq.vista.extractor.search.SearchExtractor
import ac.mdiq.vista.extractor.services.bandcamp.extractors.streaminfoitem.BandcampSearchStreamInfoItemExtractor
import java.io.IOException
import java.util.function.Supplier


class BandcampSearchExtractor(service: StreamingService, linkHandler: SearchQueryHandler) : SearchExtractor(service, linkHandler) {


    override val searchSuggestion: String
        get() = ""

    override val isCorrectedSearch: Boolean
        get() = false

    @get:Throws(ParsingException::class)

    override val metaInfo: List<MetaInfo>
        get() = emptyList()

    @Throws(IOException::class, ExtractionException::class)
    override fun getPage(page: Page?): InfoItemsPage<InfoItem> {
        if (!isValid(page)) return emptyPage()
        val collector = MultiInfoItemsCollector(serviceId)
        val d = Jsoup.parse(downloader.get(page!!.url!!).responseBody())

        for (searchResult in d.getElementsByClass("searchresult")) {
            val type = searchResult.getElementsByClass("result-info").stream()
                .flatMap { element: Element -> element.getElementsByClass("itemtype").stream() }
                .map { obj: Element -> obj.text() }
                .findFirst()
                .orElse("")

            when (type) {
                "ARTIST" -> collector.commit(BandcampChannelInfoItemExtractor(searchResult))
                "ALBUM" -> collector.commit(BandcampPlaylistInfoItemExtractor(searchResult))
                "TRACK" -> collector.commit(BandcampSearchStreamInfoItemExtractor(searchResult, ""))
                else -> {}
            }
        }

        // Count pages
        val pageLists = d.getElementsByClass("pagelist")
        if (pageLists.isEmpty()) {
            return InfoItemsPage(collector, null)
        }

        val pages = pageLists.stream()
            .map<Elements> { element: Element -> element.getElementsByTag("li") }
            .findFirst()
            .orElseGet(Supplier<Elements> { Elements() })

        // Find current page
        var currentPage = -1
        for (i in pages.indices) {
            val pageElement = pages[i]
            if (!pageElement.getElementsByTag("span").isEmpty()) {
                currentPage = i + 1
                break
            }
        }

        // Search results appear to be capped at six pages
        assert(pages.size < 10)

        var nextUrl: String? = null
        if (currentPage < pages.size) {
            nextUrl = page.url!!.substring(0, page.url!!.length - 1) + (currentPage + 1)
        }

        return InfoItemsPage(collector, Page(nextUrl))
    }

    @get:Throws(IOException::class, ExtractionException::class)

    override val initialPage: InfoItemsPage<InfoItem>
        get() = getPage(Page(url))

    @Throws(IOException::class, ExtractionException::class)
    override fun onFetchPage(downloader: Downloader) {}
}
