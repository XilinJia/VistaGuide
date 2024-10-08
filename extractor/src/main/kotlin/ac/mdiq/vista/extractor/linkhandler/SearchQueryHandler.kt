package ac.mdiq.vista.extractor.linkhandler

class SearchQueryHandler(
        originalUrl: String,
        url: String,
        searchString: String,
        contentFilters: List<String>?,
        sortFilter: String?)
    : ListLinkHandler(originalUrl, url, searchString, contentFilters, sortFilter?: "") {

    constructor(handler: ListLinkHandler) : this(handler.originalUrl, handler.url, handler.id, handler.contentFilters, handler.sortFilter)

    /**
     * Returns the search string. Since ListQIHandler is based on ListLinkHandler
     * getSearchString() is equivalent to calling getId().
     * @return the search string
     */
    val searchString: String
        get() = id
}
