package ac.mdiq.vista.extractor

import ac.mdiq.vista.extractor.downloader.Downloader
import ac.mdiq.vista.extractor.exceptions.ExtractionException
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.linkhandler.LinkHandler
import ac.mdiq.vista.extractor.localization.ContentCountry
import ac.mdiq.vista.extractor.localization.Localization
import ac.mdiq.vista.extractor.localization.TimeAgoParser
import java.io.IOException
import java.util.*


abstract class Extractor protected constructor(service: StreamingService, linkHandler: LinkHandler) {
    /**
     * [StreamingService] currently related to this extractor.<br></br>
     * Useful for getting other things from a service (like the url handlers for
     * cleaning/accepting/get id from urls).
     */
    @JvmField
    val service: StreamingService = Objects.requireNonNull(service, "service is null")
    private val linkHandler: LinkHandler = Objects.requireNonNull(linkHandler, "LinkHandler is null")

    private var forcedLocalization: Localization? = null
    private var forcedContentCountry: ContentCountry? = null

    protected var isPageFetched: Boolean = false
        private set

    @JvmField
    val downloader: Downloader = Objects.requireNonNull(Vista.downloader, "downloader is null")

    val extractorLocalization: Localization
        get() = if (forcedLocalization == null) service.localization else forcedLocalization!!

    val extractorContentCountry: ContentCountry
        get() = if (forcedContentCountry == null) service.contentCountry
        else forcedContentCountry!!

    val timeAgoParser: TimeAgoParser
        get() = service.getTimeAgoParser(extractorLocalization)

    @get:Throws(ParsingException::class)
    open val id: String
        get() = linkHandler.id

    @get:Throws(ParsingException::class)
    open val originalUrl: String
        get() = linkHandler.originalUrl

    @get:Throws(ParsingException::class)
    open val url: String
        get() = linkHandler.url

    @get:Throws(ParsingException::class)
    open val baseUrl: String
        get() = linkHandler.baseUrl

    val serviceId: Int
        get() = service.serviceId

    /**
     * @return The [LinkHandler] of the current extractor object (e.g. a ChannelExtractor
     * should return a channel url handler).
     */
    open fun getLinkHandler(): LinkHandler {
        return linkHandler
    }

    /**
     * Fetch the current page.
     * @throws IOException         if the page can not be loaded
     * @throws ExtractionException if the pages content is not understood
     */
    @Throws(IOException::class, ExtractionException::class)
    fun fetchPage() {
        if (isPageFetched) return
        onFetchPage(downloader)
        isPageFetched = true
    }

    protected fun assertPageFetched() {
        check(isPageFetched) { "Page is not fetched. Make sure you call fetchPage()" }
    }

    /**
     * Fetch the current page.
     * @param downloader the downloader to use
     * @throws IOException         if the page can not be loaded
     * @throws ExtractionException if the pages content is not understood
     */
    @Throws(IOException::class, ExtractionException::class)
    abstract fun onFetchPage(downloader: Downloader)

    @Throws(ParsingException::class)
    abstract fun getName(): String

    fun forceLocalization(localization: Localization) {
        this.forcedLocalization = localization
    }

    fun forceContentCountry(contentCountry: ContentCountry) {
        this.forcedContentCountry = contentCountry
    }
}
