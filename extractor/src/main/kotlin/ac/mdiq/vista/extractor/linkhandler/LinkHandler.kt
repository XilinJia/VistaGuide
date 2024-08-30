package ac.mdiq.vista.extractor.linkhandler

import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.utils.Utils.getBaseUrl
import java.io.Serializable

open class LinkHandler(
        @JvmField val originalUrl: String,
        @JvmField val url: String,
        @JvmField val id: String) : Serializable {

    @get:Throws(ParsingException::class)
    val baseUrl: String
        get() = getBaseUrl(url)

    constructor(handler: LinkHandler) : this(handler.originalUrl, handler.url, handler.id)
}
