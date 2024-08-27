// Created by Fynn Godau 2019, licensed GNU GPL version 3 or later
package ac.mdiq.vista.extractor.services.bandcamp

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import ac.mdiq.vista.downloader.DownloaderTestImpl.Companion.getInstance
import ac.mdiq.vista.extractor.Vista.init
import ac.mdiq.vista.extractor.ServiceList.Bandcamp
import ac.mdiq.vista.extractor.exceptions.ExtractionException
import ac.mdiq.vista.extractor.services.BaseListExtractorTest
import ac.mdiq.vista.extractor.services.DefaultTests
import ac.mdiq.vista.extractor.services.bandcamp.extractors.BandcampRadioExtractor
import java.io.IOException

/**
 * Tests for [BandcampRadioExtractor]
 */
class BandcampRadioExtractorTest : BaseListExtractorTest {
    @Test
    fun testRadioCount() {
        val list = extractor!!.initialPage.items
        Assertions.assertTrue(list!!.size > 300)
    }

    @Test
    @Throws(Exception::class)
    override fun testRelatedItems() {
        DefaultTests.defaultTestRelatedItems(extractor!!)
    }

    @Test
    @Throws(Exception::class)
    override fun testMoreRelatedItems() {
        // All items are on one page
    }

    @Test
    override fun testServiceId() {
        Assertions.assertEquals(Bandcamp.serviceId, extractor!!.serviceId)
    }

    @Test
    @Throws(Exception::class)
    override fun testName() {
        Assertions.assertEquals("Radio", extractor!!.getName())
    }

    @Test
    override fun testId() {
        Assertions.assertEquals("Radio", extractor!!.id)
    }

    @Test
    @Throws(Exception::class)
    override fun testUrl() {
        assertEquals("https://bandcamp.com/api/bcweekly/3/list", extractor?.url);
    }

    @Test
    @Throws(Exception::class)
    override fun testOriginalUrl() {
        assertEquals("https://bandcamp.com/api/bcweekly/3/list", extractor?.originalUrl);
    }

    companion object {
        private var extractor: BandcampRadioExtractor? = null


        @BeforeAll
        @Throws(ExtractionException::class, IOException::class)
        fun setUp() {
            init(getInstance())
            extractor = Bandcamp.getKioskList().getExtractorById("Radio", null) as? BandcampRadioExtractor
            extractor!!.fetchPage()
        }
    }
}
