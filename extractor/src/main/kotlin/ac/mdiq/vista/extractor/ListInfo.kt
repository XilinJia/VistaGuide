package ac.mdiq.vista.extractor

import ac.mdiq.vista.extractor.Page.Companion.isValid
import ac.mdiq.vista.extractor.linkhandler.ListLinkHandler

abstract class ListInfo<T : InfoItem> : Info {
    @JvmField
    var relatedItems: List<T> = listOf()
    @JvmField
    var nextPage: Page? = null
    val contentFilters: List<String>
    val sortFilter: String

    constructor(serviceId: Int,
                id: String,
                url: String,
                originalUrl: String,
                name: String,
                contentFilter: List<String>,
                sortFilter: String)
            : super(serviceId, id, url, originalUrl, name) {
        this.contentFilters = contentFilter
        this.sortFilter = sortFilter
    }

    constructor(serviceId: Int, listUrlIdHandler: ListLinkHandler, name: String) : super(serviceId, listUrlIdHandler, name) {
        this.contentFilters = listUrlIdHandler.contentFilters
        this.sortFilter = listUrlIdHandler.sortFilter
    }

    fun hasNextPage(): Boolean {
        return isValid(nextPage)
    }
}
