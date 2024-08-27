package ac.mdiq.vista.extractor.services

import org.junit.jupiter.api.Test

interface BaseExtractorTest {
    @Test
    @Throws(Exception::class)
    fun testServiceId()

    @Test
    @Throws(Exception::class)
    fun testName()

    @Test
    @Throws(Exception::class)
    fun testId()

    @Test
    @Throws(Exception::class)
    fun testUrl()

    @Test
    @Throws(Exception::class)
    fun testOriginalUrl()
}
