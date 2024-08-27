package ac.mdiq.vista.extractor.services.peertube

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import ac.mdiq.vista.downloader.DownloaderTestImpl.Companion.getInstance
import ac.mdiq.vista.extractor.Vista.init
import ac.mdiq.vista.extractor.ServiceList.PeerTube
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.services.BaseListExtractorTest
import ac.mdiq.vista.extractor.services.DefaultTests.defaultTestMoreItems
import ac.mdiq.vista.extractor.services.DefaultTests.defaultTestRelatedItems
import ac.mdiq.vista.extractor.services.peertube.extractors.PeertubeTrendingExtractor

class PeertubeTrendingExtractorTest {
    class Trending : BaseListExtractorTest {
        @Test
        override fun testServiceId() {
            Assertions.assertEquals(PeerTube.serviceId, extractor!!.serviceId)
        }

        @Test
        @Throws(Exception::class)
        override fun testName() {
            Assertions.assertEquals("Trending", extractor!!.getName())
        }

        @Test
        @Throws(Exception::class)
        override fun testId() {
            Assertions.assertEquals("Trending", extractor!!.id)
        }

        @Test
        @Throws(ParsingException::class)
        override fun testUrl() {
            Assertions.assertEquals("https://framatube.org/api/v1/videos?sort=-trending", extractor!!.url)
        }

        @Test
        @Throws(ParsingException::class)
        override fun testOriginalUrl() {
            Assertions.assertEquals("https://framatube.org/api/v1/videos?sort=-trending", extractor!!.originalUrl)
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
            private var extractor: PeertubeTrendingExtractor? = null


            @BeforeAll
            @Throws(Exception::class)
            fun setUp(): Unit {
                init(getInstance()!!)
                // setting instance might break test when running in parallel
                PeerTube.instance = PeertubeInstance("https://framatube.org", "Framatube")
                extractor = PeerTube.getKioskList()?.getExtractorById("Trending", null) as? PeertubeTrendingExtractor
                extractor!!.fetchPage()
            }
        }
    }
}
