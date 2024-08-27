// Created by Fynn Godau 2019, licensed GNU GPL version 3 or later
package ac.mdiq.vista.extractor.services.bandcamp

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import ac.mdiq.vista.downloader.DownloaderTestImpl.Companion.getInstance
import ac.mdiq.vista.extractor.Vista.init
import ac.mdiq.vista.extractor.ServiceList.Bandcamp
import ac.mdiq.vista.extractor.exceptions.ExtractionException
import ac.mdiq.vista.extractor.services.BaseListExtractorTest
import ac.mdiq.vista.extractor.services.DefaultTests.defaultTestRelatedItems
import ac.mdiq.vista.extractor.services.bandcamp.extractors.BandcampFeaturedExtractor
import java.io.IOException

/**
 * Tests for [BandcampFeaturedExtractor]
 */
class BandcampFeaturedExtractorTest : BaseListExtractorTest {
    @Test
    @Throws(ExtractionException::class, IOException::class)
    fun testFeaturedCount() {
        val list = extractor!!.initialPage.items
        Assertions.assertTrue(list!!.size > 5)
    }

    @Test
    @Throws(ExtractionException::class, IOException::class)
    fun testHttps() {
        val list = extractor!!.initialPage.items
        Assertions.assertTrue(list!![0].url.contains("https://"))
    }

    @Test
    @Throws(ExtractionException::class, IOException::class)
    fun testMorePages() {
        val page2 = extractor!!.initialPage.nextPage
        val page3 = extractor!!.getPage(
            page2)!!.nextPage

        Assertions.assertTrue(extractor!!.getPage(page2)!!.items!!.size > 5)

        // Compare first item of second page with first item of third page
        Assertions.assertNotEquals(
            extractor!!.getPage(page2)!!.items!![0],
            extractor!!.getPage(page3)!!.items!![0]
        )
    }

    @Throws(Exception::class)
    override fun testRelatedItems() {
        defaultTestRelatedItems(extractor!!)
    }

    @Throws(Exception::class)
    override fun testMoreRelatedItems() {
        // more items not implemented
    }

    override fun testServiceId() {
        Assertions.assertEquals(Bandcamp.serviceId, extractor!!.serviceId)
    }

    @Throws(Exception::class)
    override fun testName() {
        Assertions.assertEquals("Featured", extractor!!.getName())
    }

    override fun testId() {
        Assertions.assertEquals("", extractor!!.id)
    }

    @Throws(Exception::class)
    override fun testUrl() {
        Assertions.assertEquals("", extractor!!.url)
    }

    @Throws(Exception::class)
    override fun testOriginalUrl() {
        Assertions.assertEquals("", extractor!!.originalUrl)
    }

    companion object {
        private var extractor: BandcampFeaturedExtractor? = null


        @BeforeAll
        @Throws(ExtractionException::class, IOException::class)
        fun setUp(): Unit {
            init(getInstance()!!)
            extractor = Bandcamp.getKioskList()?.defaultKioskExtractor as? BandcampFeaturedExtractor
            extractor!!.fetchPage()
        }
    }
}
