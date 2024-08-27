package ac.mdiq.vista.extractor.services.youtube.extractors

import ac.mdiq.vista.extractor.services.youtube.ItagItem
import java.io.Serializable


/**
 * Class to build easier [ac.mdiq.vista.extractor.stream.Stream]s for
 * [YoutubeStreamExtractor].
 *
 *
 *
 * It stores, per stream:
 *
 *  * its content (the URL/the base URL of streams);
 *  * whether its content is the URL the content itself or the base URL;
 *  * its associated [ItagItem].
 *
 *
 */
/**
 * Creates a new `ItagInfo` instance.
 *
 * @param content  the content of the stream, which must be not null
 * @param itagItem the [ItagItem] associated with the stream, which must be not null
 */
internal class ItagInfo (
        /**
         * Gets the content stored in this `ItagInfo` instance, which is either the URL to the
         * content itself or the base URL.
         * @return the content stored in this `ItagInfo` instance
         */
        @JvmField val content: String,

        /**
         * Gets the [ItagItem] associated with this `ItagInfo` instance.
         * @return the [ItagItem] associated with this `ItagInfo` instance, which is not
         * null
         */
        @JvmField val itagItem: ItagItem) : Serializable {

    /**
     * Gets whether the content stored is the URL to the content itself or the base URL of it.
     *
     * @return whether the content stored is the URL to the content itself or the base URL of it
     * @see .getContent
     */
    /**
     * Sets whether the stream is a URL.
     *
     * @param isUrl whether the content is a URL
     */
    @JvmField
    var isUrl: Boolean = false
}
