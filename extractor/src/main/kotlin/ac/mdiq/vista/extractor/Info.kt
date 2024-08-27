package ac.mdiq.vista.extractor

import ac.mdiq.vista.extractor.exceptions.ExtractionException
import ac.mdiq.vista.extractor.linkhandler.LinkHandler
import java.io.Serializable

abstract class Info(
        @JvmField val serviceId: Int,
        /**
         * Id of this Info object <br></br>
         * e.g. Youtube:  https://www.youtube.com/watch?v=RER5qCTzZ7     &gt;    RER5qCTzZ7
         */
        @JvmField val id: String,
        /**
         * Different than the [.originalUrl] in the sense that it *may* be set as a cleaned
         * url.
         * @see LinkHandler.getUrl
         * @see Extractor.getOriginalUrl
         */
        @JvmField val url: String,
        /**
         * The url used to start the extraction of this [Info] object.
         * @see Extractor.getOriginalUrl
         */
        @JvmField var originalUrl: String,
        @JvmField val name: String) : Serializable {
    // if you use an api and want to handle the website url
    // overriding original url is essential

    val errors: MutableList<Throwable> = ArrayList()

    val service: StreamingService
        get() {
            try {
                return Vista.getService(serviceId)
            } catch (e: ExtractionException) {
                // this should be unreachable, as serviceId certainly refers to a valid service
                throw RuntimeException("Info object has invalid service id", e)
            }
        }

    fun addError(throwable: Throwable) {
        errors.add(throwable)
    }

    fun addAllErrors(throwables: Collection<Throwable>?) {
        errors.addAll(throwables!!)
    }

    constructor(serviceId: Int, linkHandler: LinkHandler, name: String) : this(serviceId,
        linkHandler.id,
        linkHandler.url,
        linkHandler.originalUrl,
        name)

    override fun toString(): String {
        val ifDifferentString = if (url == originalUrl) "" else " (originalUrl=\"$originalUrl\")"
        return ("${javaClass.simpleName}[url=\"$url\"$ifDifferentString, name=\"$name\"]")
    }

}
