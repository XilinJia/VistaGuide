package ac.mdiq.vista.extractor.services

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import ac.mdiq.vista.extractor.Extractor
import ac.mdiq.vista.extractor.ExtractorAsserts
import ac.mdiq.vista.extractor.StreamingService

abstract class DefaultExtractorTest<T : Extractor?> : BaseExtractorTest {
    @Throws(Exception::class)
    abstract fun extractor(): T

    @Throws(Exception::class)
    abstract fun expectedService(): StreamingService

    @Throws(Exception::class)
    abstract fun expectedName(): String?

    @Throws(Exception::class)
    abstract fun expectedId(): String?

    @Throws(Exception::class)
    abstract fun expectedUrlContains(): String?

    @Throws(Exception::class)
    abstract fun expectedOriginalUrlContains(): String?

    @Test
    @Throws(Exception::class)
    override fun testServiceId() {
        assertEquals(expectedService().serviceId, extractor()!!.serviceId)
    }

    @Test
    @Throws(Exception::class)
    override fun testName() {
        assertEquals(expectedName(), extractor()?.getName())
    }

    @Test
    @Throws(Exception::class)
    override fun testId() {
        assertEquals(expectedId(), extractor()!!.id)
    }

    @Test
    @Throws(Exception::class)
    override fun testUrl() {
        val url = extractor()!!.url
        ExtractorAsserts.assertIsSecureUrl(url)
        ExtractorAsserts.assertContains(expectedUrlContains()!!, url)
    }

    @Test
    @Throws(Exception::class)
    override fun testOriginalUrl() {
        val originalUrl = extractor()!!.originalUrl
        ExtractorAsserts.assertIsSecureUrl(originalUrl)
        ExtractorAsserts.assertContains(expectedOriginalUrlContains()!!, originalUrl)
    }
}
