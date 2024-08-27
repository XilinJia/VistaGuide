package ac.mdiq.vista.extractor.linkhandler

import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.utils.Utils.followGoogleRedirectIfNeeded
import ac.mdiq.vista.extractor.utils.Utils.getBaseUrl
import java.util.*

abstract class ListLinkHandlerFactory : LinkHandlerFactory() {
    /**
     * Will returns content filter the corresponding extractor can handle like "channels", "videos", "music", etc.
     * @return filter that can be applied when building a query for getting a list
     */
    open val availableContentFilter: Array<String>
        get() = arrayOf("")

    /**
     * Will returns sort filter the corresponding extractor can handle like "A-Z", "oldest first", "size", etc.
     * @return filter that can be applied when building a query for getting a list
     */
    open val availableSortFilter: Array<String?>?
        get() = arrayOfNulls(0)

    @Throws(ParsingException::class, UnsupportedOperationException::class)
    abstract fun getUrl(id: String, contentFilters: List<String>, sortFilter: String?): String

    @Throws(ParsingException::class, UnsupportedOperationException::class)
    open fun getUrl(id: String, contentFilter: List<String>, sortFilter: String?, baseUrl: String?): String {
        return getUrl(id, contentFilter, sortFilter)
    }

    @Throws(ParsingException::class)
    override fun fromUrl(url: String): ListLinkHandler {
        val polishedUrl = followGoogleRedirectIfNeeded(url)
        val baseUrl = getBaseUrl(polishedUrl)
        return fromUrl(polishedUrl, baseUrl)
    }

    @Throws(ParsingException::class)
    override fun fromUrl(url: String, baseUrl: String?): ListLinkHandler {
        Objects.requireNonNull(url, "URL may not be null")
        return ListLinkHandler(super.fromUrl(url, baseUrl))
    }

    @Throws(ParsingException::class)
    override fun fromId(id: String): ListLinkHandler {
        return ListLinkHandler(super.fromId(id))
    }

    @Throws(ParsingException::class)
    override fun fromId(id: String, baseUrl: String?): ListLinkHandler {
        return ListLinkHandler(super.fromId(id, baseUrl))
    }

    @Throws(ParsingException::class)
    open fun fromQuery(id: String, contentFilters: List<String>, sortFilter: String): ListLinkHandler {
        val url = getUrl(id, contentFilters, sortFilter)
        return ListLinkHandler(url, url, id, contentFilters, sortFilter)
    }

    @Throws(ParsingException::class)
    fun fromQuery(id: String, contentFilters: List<String>, sortFilter: String?, baseUrl: String?): ListLinkHandler {
        val url = getUrl(id, contentFilters, sortFilter, baseUrl)
        return ListLinkHandler(url, url, id, contentFilters, sortFilter?:"")
    }

    /**
     * For making ListLinkHandlerFactory compatible with LinkHandlerFactory we need to override
     * this, however it should not be overridden by the actual implementation.
     * @return the url corresponding to id without any filters applied
     */
    @Throws(ParsingException::class, UnsupportedOperationException::class)
    override fun getUrl(id: String): String {
        return getUrl(id, ArrayList(0), "")
    }

    @Throws(ParsingException::class)
    override fun getUrl(id: String, baseUrl: String): String {
        return getUrl(id, ArrayList(0), "", baseUrl)
    }
}
