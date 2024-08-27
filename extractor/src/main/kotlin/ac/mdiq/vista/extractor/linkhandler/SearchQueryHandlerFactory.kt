package ac.mdiq.vista.extractor.linkhandler

import ac.mdiq.vista.extractor.exceptions.ParsingException

abstract class SearchQueryHandlerFactory : ListLinkHandlerFactory() {
//    @Throws(ParsingException::class, UnsupportedOperationException::class)
//    abstract override fun getUrl(id: String, contentFilter: List<String>, sortFilter: String?): String?

    @Suppress("unused")
    fun getSearchString(url: String?): String {
        return ""
    }

    @Throws(ParsingException::class, UnsupportedOperationException::class)
    override fun getId(url: String): String {
        return getSearchString(url)
    }

    @Throws(ParsingException::class)
    override fun fromQuery(id: String, contentFilters: List<String>, sortFilter: String): SearchQueryHandler {
        val handler = super.fromQuery(id, contentFilters, sortFilter)
        return SearchQueryHandler(handler)
    }

    @Throws(ParsingException::class)
    fun fromQuery(query: String): SearchQueryHandler {
        return fromQuery(query, emptyList(), "")
    }

    /**
     * It's not mandatory for Vista to handle the Url
     */
    override fun onAcceptUrl(url: String): Boolean {
        return false
    }
}
