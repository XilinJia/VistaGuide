package ac.mdiq.vista.extractor.services

import org.junit.jupiter.api.Test

interface BaseSearchExtractorTest : BaseListExtractorTest {
    @Test
    @Throws(Exception::class)
    fun testSearchString()

    @Test
    @Throws(Exception::class)
    fun testSearchSuggestion()

    @Test
    @Throws(Exception::class)
    fun testSearchCorrected()
}
