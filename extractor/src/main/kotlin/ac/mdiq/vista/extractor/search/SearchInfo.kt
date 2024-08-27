package ac.mdiq.vista.extractor.search

import ac.mdiq.vista.extractor.*
import ac.mdiq.vista.extractor.ListExtractor.InfoItemsPage
import ac.mdiq.vista.extractor.exceptions.ExtractionException
import ac.mdiq.vista.extractor.linkhandler.SearchQueryHandler
import ac.mdiq.vista.extractor.utils.ExtractorHelper
import java.io.IOException


class SearchInfo(
        serviceId: Int,
        qIHandler: SearchQueryHandler,
        val searchString: String)
    : ListInfo<InfoItem>(serviceId, qIHandler, "Search") {

    var searchSuggestion: String? = null
    var isCorrectedSearch: Boolean = false

    var metaInfo: List<MetaInfo?> = listOf<MetaInfo>()

    companion object {
        @Throws(ExtractionException::class, IOException::class)
        fun getInfo(service: StreamingService, searchQuery: SearchQueryHandler): SearchInfo {
            val extractor = service.getSearchExtractor(searchQuery)
            extractor.fetchPage()
            return getInfo(extractor)
        }

        @Throws(ExtractionException::class, IOException::class)
        fun getInfo(extractor: SearchExtractor): SearchInfo {
            val info = SearchInfo(extractor.serviceId, extractor.getLinkHandler(), extractor.searchString)

            try {
                info.originalUrl = extractor.originalUrl
            } catch (e: Exception) {
                info.addError(e)
            }
            try {
                info.searchSuggestion = extractor.searchSuggestion
            } catch (e: Exception) {
                info.addError(e)
            }
            try {
                info.isCorrectedSearch = extractor.isCorrectedSearch
            } catch (e: Exception) {
                info.addError(e)
            }
            try {
                info.metaInfo = extractor.metaInfo
            } catch (e: Exception) {
                info.addError(e)
            }

            val page = ExtractorHelper.getItemsPageOrLogError(info, extractor)
            info.relatedItems = (page.items)
            info.nextPage = page.nextPage

            return info
        }


        @Throws(IOException::class, ExtractionException::class)
        fun getMoreItems(service: StreamingService, query: SearchQueryHandler, page: Page?): InfoItemsPage<InfoItem> {
            return service.getSearchExtractor(query).getPage(page)
        }
    }
}
