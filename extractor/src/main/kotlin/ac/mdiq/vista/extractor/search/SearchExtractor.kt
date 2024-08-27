package ac.mdiq.vista.extractor.search

import ac.mdiq.vista.extractor.InfoItem
import ac.mdiq.vista.extractor.ListExtractor
import ac.mdiq.vista.extractor.MetaInfo
import ac.mdiq.vista.extractor.StreamingService
import ac.mdiq.vista.extractor.exceptions.ExtractionException
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.linkhandler.SearchQueryHandler


abstract class SearchExtractor(service: StreamingService, linkHandler: SearchQueryHandler) : ListExtractor<InfoItem>(service, linkHandler) {

    val searchString: String
        get() = getLinkHandler().searchString

    @get:Throws(ParsingException::class)
    abstract val searchSuggestion: String

    @get:Throws(ParsingException::class)
    abstract val isCorrectedSearch: Boolean

    @get:Throws(ParsingException::class)
    abstract val metaInfo: List<MetaInfo>

    override fun getLinkHandler(): SearchQueryHandler {
        return super.getLinkHandler() as SearchQueryHandler
    }

    override fun getName(): String {
        return getLinkHandler().searchString
    }

    class NothingFoundException(message: String?) : ExtractionException(message)

}
