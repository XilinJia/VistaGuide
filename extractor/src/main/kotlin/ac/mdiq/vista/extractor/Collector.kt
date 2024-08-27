package ac.mdiq.vista.extractor

import ac.mdiq.vista.extractor.exceptions.ParsingException


/**
 * Collectors are used to simplify the collection of information
 * from extractors
 * @param <I> the item type
 * @param <E> the extractor type
</E></I> */
interface Collector<I, E> {
    /**
     * Try to add an extractor to the collection
     * @param extractor the extractor to add
     */
    fun commit(extractor: E)

    /**
     * Try to extract the item from an extractor without adding it to the collection
     * @param extractor the extractor to use
     * @return the item
     * @throws ParsingException thrown if there is an error extracting the
     * **required** fields of the item.
     */
    @Throws(ParsingException::class)
    fun extract(extractor: E): I

    /**
     * Get all items
     * @return the items
     */
    fun getItems(): List<I>

    /**
     * Get all errors
     * @return the errors
     */
//    fun getErrors(): List<Throwable>

    /**
     * Reset all collected items and errors
     */
    fun reset()
}
