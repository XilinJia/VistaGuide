package ac.mdiq.vista.extractor

import ac.mdiq.vista.extractor.Page.Companion.isValid
import ac.mdiq.vista.extractor.exceptions.ExtractionException
import ac.mdiq.vista.extractor.linkhandler.ListLinkHandler
import java.io.IOException


/**
 * Base class to extractors that have a list (e.g. playlists, users).
 * @param <R> the info item type this list extractor provides
</R> */
abstract class ListExtractor<R : InfoItem>(service: StreamingService, linkHandler: ListLinkHandler) : Extractor(service, linkHandler) {

    @get:Throws(IOException::class, ExtractionException::class)
    abstract val initialPage: InfoItemsPage<R>

    /**
     * Get a list of items corresponding to the specific requested page.
     *
     * @param page any page got from the exclusive implementation of the list extractor
     * @return a [InfoItemsPage] corresponding to the requested page
     * @see InfoItemsPage.getNextPage
     */
    @Throws(IOException::class, ExtractionException::class)
    abstract fun getPage(page: Page?): InfoItemsPage<R>

    override fun getLinkHandler(): ListLinkHandler {
        return super.getLinkHandler() as ListLinkHandler
    }

    /**
     * A class that is used to wrap a list of gathered items and eventual errors, it
     * also contains a field that points to the next available page ([.nextPage]).
     * @param <T> the info item type that this page is supposed to store and provide
    </T> */
    class InfoItemsPage<T : InfoItem>(
            /**
             * The current list of items of this page
             */
            val items: List<T>,
            /**
             * Url pointing to the next page relative to this one
             * @see ListExtractor.getPage
             * @see Page
             */
            @JvmField val nextPage: Page?,
            /**
             * Errors that happened during the extraction
             */
            @JvmField val errors: List<Throwable>) {
        constructor(collector: InfoItemsCollector<T, *>, nextPage: Page?) : this(collector.getItems(), nextPage, collector.errors)

        fun hasNextPage(): Boolean {
            return isValid(nextPage)
        }

        companion object {
            private val EMPTY = InfoItemsPage(emptyList(), null, emptyList())

            /**
             * A convenient method that returns a representation of an empty page.
             * @return a type-safe page with the list of items and errors empty and the nextPage set to
             * `null`.
             */

            fun <T : InfoItem> emptyPage(): InfoItemsPage<T> {
                return EMPTY as InfoItemsPage<T>
            }
        }
    }

    companion object {
        /**
         * Constant that should be returned whenever
         * a list has an unknown number of items.
         */
        const val ITEM_COUNT_UNKNOWN: Long = -1

        /**
         * Constant that should be returned whenever a list has an
         * infinite number of items. For example a YouTube mix.
         */
        const val ITEM_COUNT_INFINITE: Long = -2

        /**
         * Constant that should be returned whenever a list
         * has an unknown number of items bigger than 100.
         */
        const val ITEM_COUNT_MORE_THAN_100: Long = -3
    }
}
