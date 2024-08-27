package ac.mdiq.vista.extractor.services.soundcloud

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import ac.mdiq.vista.downloader.DownloaderTestImpl.Companion.getInstance
import ac.mdiq.vista.extractor.Vista.init
import ac.mdiq.vista.extractor.ServiceList.SoundCloud
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.services.BaseListExtractorTest
import ac.mdiq.vista.extractor.services.DefaultTests.defaultTestMoreItems
import ac.mdiq.vista.extractor.services.DefaultTests.defaultTestRelatedItems
import ac.mdiq.vista.extractor.services.soundcloud.extractors.SoundcloudChartsExtractor

class SoundcloudChartsExtractorTest {
    class NewAndHot : BaseListExtractorTest {
        @Test
        override fun testServiceId() {
            Assertions.assertEquals(SoundCloud.serviceId, extractor!!.serviceId)
        }

        @Test
        override fun testName() {
            Assertions.assertEquals("New & hot", extractor!!.getName())
        }

        @Test
        override fun testId() {
            Assertions.assertEquals("New & hot", extractor!!.id)
        }

        @Test
        @Throws(ParsingException::class)
        override fun testUrl() {
            Assertions.assertEquals("https://soundcloud.com/charts/new", extractor!!.url)
        }

        @Test
        @Throws(ParsingException::class)
        override fun testOriginalUrl() {
            Assertions.assertEquals("https://soundcloud.com/charts/new", extractor!!.originalUrl)
        }

        @Test
        @Throws(Exception::class)
        override fun testRelatedItems() {
            defaultTestRelatedItems(extractor!!)
        }

        @Test
        @Throws(Exception::class)
        override fun testMoreRelatedItems() {
            defaultTestMoreItems(extractor!!)
        }

        companion object {
            private var extractor: SoundcloudChartsExtractor? = null


            @BeforeAll
            @Throws(Exception::class)
            fun setUp(): Unit {
                init(getInstance()!!)
                extractor = SoundCloud.getKioskList()?.getExtractorById("New & hot", null) as? SoundcloudChartsExtractor
                extractor!!.fetchPage()
            }
        }
    }

    class Top50Charts : BaseListExtractorTest {
        @Test
        override fun testServiceId() {
            Assertions.assertEquals(SoundCloud.serviceId, extractor!!.serviceId)
        }

        @Test
        override fun testName() {
            Assertions.assertEquals("Top 50", extractor!!.getName())
        }

        @Test
        override fun testId() {
            Assertions.assertEquals("Top 50", extractor!!.id)
        }

        @Test
        @Throws(ParsingException::class)
        override fun testUrl() {
            Assertions.assertEquals("https://soundcloud.com/charts/top", extractor!!.url)
        }

        @Test
        @Throws(ParsingException::class)
        override fun testOriginalUrl() {
            Assertions.assertEquals("https://soundcloud.com/charts/top", extractor!!.originalUrl)
        }

        @Test
        @Throws(Exception::class)
        override fun testRelatedItems() {
            defaultTestRelatedItems(extractor!!)
        }

        @Test
        @Throws(Exception::class)
        override fun testMoreRelatedItems() {
            defaultTestMoreItems(extractor!!)
        }

        companion object {
            private var extractor: SoundcloudChartsExtractor? = null


            @BeforeAll
            @Throws(Exception::class)
            fun setUp(): Unit {
                init(getInstance()!!)
                extractor = SoundCloud.getKioskList()?.getExtractorById("Top 50", null) as? SoundcloudChartsExtractor
                extractor!!.fetchPage()
            }
        }
    }
}
