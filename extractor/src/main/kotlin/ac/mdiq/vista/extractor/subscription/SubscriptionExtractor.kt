package ac.mdiq.vista.extractor.subscription

import ac.mdiq.vista.extractor.StreamingService
import ac.mdiq.vista.extractor.exceptions.ExtractionException
import ac.mdiq.vista.extractor.exceptions.ParsingException
import java.io.IOException
import java.io.InputStream
import java.util.*


abstract class SubscriptionExtractor(
        protected val service: StreamingService,
        supportedSources: List<ContentSource>) {

    val supportedSources: List<ContentSource> = Collections.unmodifiableList(supportedSources)

    /**
     * Returns an url that can help/guide the user to the file (or channel url) to extract the
     * subscriptions.
     * For example, in YouTube, the export subscriptions url is a good choice to return here.
     */
    abstract val relatedUrl: String?

    /**
     * Reads and parse a list of [SubscriptionItem] from the given channel url.
     *
     * @throws InvalidSourceException when the channelUrl doesn't exist or is invalid
     */
    @Throws(IOException::class, ExtractionException::class)
    open fun fromChannelUrl(channelUrl: String?): List<SubscriptionItem?> {
        throw UnsupportedOperationException("Service ${service.serviceInfo.name} doesn't support extracting from a channel url")
    }

    /**
     * Reads and parse a list of [SubscriptionItem] from the given InputStream.
     *
     * @throws InvalidSourceException when the content read from the InputStream is invalid and can
     * not be parsed
     */
    @Throws(ExtractionException::class)
    open fun fromInputStream(contentInputStream: InputStream): List<SubscriptionItem?> {
        throw UnsupportedOperationException("Service ${service.serviceInfo.name} doesn't support extracting from an InputStream")
    }

    /**
     * Reads and parse a list of [SubscriptionItem] from the given InputStream.
     *
     * @throws InvalidSourceException when the content read from the InputStream is invalid and can
     * not be parsed
     */
    @Throws(ExtractionException::class)
    open fun fromInputStream(contentInputStream: InputStream,  contentType: String): List<SubscriptionItem?> {
        throw UnsupportedOperationException("Service ${service.serviceInfo.name} doesn't support extracting from an InputStream")
    }

    /**
     * Exception that should be thrown when the input **do not** contain valid content that the
     * extractor can parse (e.g. nonexistent user in case of a url extraction).
     */
    class InvalidSourceException @JvmOverloads constructor(
            detailMessage: String? = null,
            cause: Throwable? = null)
        : ParsingException("Not a valid source${if (detailMessage == null) "" else " ($detailMessage)"}", cause) {
        constructor(cause: Throwable?) : this(null, cause)
    }

    enum class ContentSource {
        CHANNEL_URL, INPUT_STREAM
    }
}
