package ac.mdiq.vista.extractor.linkhandler

import java.util.*

open class ListLinkHandler(
        originalUrl: String,
        url: String, id: String,
        contentFilters: List<String>?,
        @JvmField val sortFilter: String)
    : LinkHandler(originalUrl, url, id) {

    @JvmField
    val contentFilters: List<String> = contentFilters ?: listOf()

    constructor(handler: ListLinkHandler) : this(handler.originalUrl, handler.url, handler.id, handler.contentFilters, handler.sortFilter)

    constructor(handler: LinkHandler) : this(handler.originalUrl, handler.url, handler.id, emptyList<String>(), "")
}
