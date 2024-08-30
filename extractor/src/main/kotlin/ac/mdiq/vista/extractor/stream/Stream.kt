package ac.mdiq.vista.extractor.stream

import ac.mdiq.vista.extractor.MediaFormat
import ac.mdiq.vista.extractor.services.youtube.ItagItem
import java.io.Serializable


/**
 * Abstract class which represents streams in the extractor.
 *
 * Instantiates a new `Stream` object.
 * @param id             the identifier which uniquely identifies the file, e.g. for YouTube
 * this would be the itag
 * @param content        the content or URL, depending on whether isUrl is true
 * @param isUrl          whether content is the URL or the actual content of e.g. a DASH manifest
 * @param format         the [MediaFormat], which can be null
 * @param deliveryMethod the delivery method of the stream
 * @param manifestUrl    the URL of the manifest this stream comes from (if applicable, otherwise null)
 */
abstract class Stream (
        /**
         * Gets the identifier of this stream, e.g. the itag for YouTube.
         * It should normally be unique, but [.ID_UNKNOWN] may be returned as the identifier if
         * the one used by the stream extractor cannot be extracted, which could happen if the
         * extractor uses a value from a streaming service.
         *
         * @return the identifier (which may be [.ID_UNKNOWN])
         */
        @JvmField val id: String,
        /**
         * Gets the content or URL.
         * @return the content or URL
         */
        @JvmField val content: String,
        val isUrl: Boolean,
        val format: MediaFormat?,
        @JvmField val deliveryMethod: DeliveryMethod,
        /**
         * Gets the URL of the manifest this stream comes from (if applicable, otherwise null).
         * @return the URL of the manifest this stream comes from or `null`
         */
        val manifestUrl: String?) : Serializable {
    /**
     * Reveals whether two streams have the same statistics ([media format][MediaFormat] and
     * [delivery method][DeliveryMethod]).
     * If the [media format][MediaFormat] of the stream is unknown, the streams are compared
     * by using only the [delivery method][DeliveryMethod] and their ID.
     * Note: This method always returns false if the stream passed is null.
     * @param other the stream object to be compared to this stream object
     * @return whether the stream have the same stats or not, based on the criteria above
     */
    open fun equalStats(other: Stream?): Boolean {
        if (other == null || format == null || other.format == null) return false
        return format.id == other.format.id && deliveryMethod == other.deliveryMethod && isUrl == other.isUrl
    }

    /**
     * Gets the URL of this stream if the content is a URL, or `null` otherwise.
     * @return the URL if the content is a URL, `null` otherwise
     */
    @Deprecated("Use {@link #getContent()} instead.")
    fun getUrl(): String? {
        return if (isUrl) content else null
    }

    /**
     * Gets the format ID, which can be unknown.
     * @return the format ID or [.FORMAT_ID_UNKNOWN]
     */
    fun getFormatId(): Int {
        if (format != null) return format.id
        return FORMAT_ID_UNKNOWN
    }

    /**
     * Gets the [ItagItem] of a stream.
     * If the stream is not from YouTube, this value will always be null.
     * @return the [ItagItem] of the stream or `null`
     */
    abstract fun getItagItem(): ItagItem?

    companion object {
        const val FORMAT_ID_UNKNOWN: Int = -1
        const val ID_UNKNOWN: String = " "

        /**
         * An integer to represent that the itag ID returned is not available (only for YouTube; this
         * should never happen) or not applicable (for other services than YouTube).
         * An itag should not have a negative value, so `-1` is used for this constant.
         *
         */
        const val ITAG_NOT_AVAILABLE_OR_NOT_APPLICABLE: Int = -1

        /**
         * Checks if the list already contains a stream with the same statistics.
         *
         * @param stream the stream to be compared against the streams in the stream list
         * @param streamList the list of [Stream]s which will be compared
         * @return whether the list already contains one stream with equals stats
         */
        fun containSimilarStream(stream: Stream, streamList: List<Stream?>): Boolean {
            if (streamList.isEmpty()) return false
            for (cmpStream in streamList) {
                if (stream.equalStats(cmpStream)) return true
            }
            return false
        }
    }
}
