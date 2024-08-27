package ac.mdiq.vista.extractor.services.youtube

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import ac.mdiq.vista.downloader.DownloaderFactory
import ac.mdiq.vista.downloader.DownloaderFactory.getDownloader
import ac.mdiq.vista.extractor.Vista.init
import ac.mdiq.vista.extractor.ServiceList.YouTube
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.services.BaseListExtractorTest
import ac.mdiq.vista.extractor.services.DefaultTests.assertNoMoreItems
import ac.mdiq.vista.extractor.services.DefaultTests.defaultTestRelatedItems
import ac.mdiq.vista.extractor.services.youtube.extractors.YoutubeTrendingExtractor

object YoutubeKioskExtractorTest {
    private const val RESOURCE_PATH = DownloaderFactory.RESOURCE_PATH + "services/youtube/extractor/kiosk/"

    class Trending : BaseListExtractorTest {
        @Test
        override fun testServiceId() {
            Assertions.assertEquals(YouTube.serviceId, extractor!!.serviceId)
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
            Assertions.assertEquals("https://www.youtube.com/feed/trending", extractor!!.url)
        }

        @Test
        @Throws(ParsingException::class)
        override fun testOriginalUrl() {
            Assertions.assertEquals("https://www.youtube.com/feed/trending", extractor!!.originalUrl)
        }

        @Test
        @Throws(Exception::class)
        override fun testRelatedItems() {
            defaultTestRelatedItems(extractor!!)
        }

        @Test
        @Throws(Exception::class)
        override fun testMoreRelatedItems() {
            assertNoMoreItems(extractor!!)
        }

        companion object {
            private var extractor: YoutubeTrendingExtractor? = null


            @BeforeAll
            @Throws(Exception::class)
            fun setUp(): Unit {
                YoutubeTestsUtils.ensureStateless()
                init(getDownloader(RESOURCE_PATH + "trending"))
                extractor = YouTube.getKioskList()?.defaultKioskExtractor as? YoutubeTrendingExtractor
                extractor!!.fetchPage()
            }
        }
    }
}
