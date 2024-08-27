package ac.mdiq.vista.extractor.services.youtube

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import ac.mdiq.vista.downloader.DownloaderFactory
import ac.mdiq.vista.downloader.DownloaderFactory.getDownloader
import ac.mdiq.vista.extractor.Vista.init
import ac.mdiq.vista.extractor.ServiceList.YouTube
import ac.mdiq.vista.extractor.exceptions.ContentNotAvailableException
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.services.BaseListExtractorTest
import ac.mdiq.vista.extractor.services.DefaultTests.assertNoMoreItems
import ac.mdiq.vista.extractor.services.DefaultTests.defaultTestRelatedItems
import ac.mdiq.vista.extractor.services.youtube.extractors.YoutubeFeedExtractor
import java.io.IOException

object YoutubeFeedExtractorTest {
    private const val RESOURCE_PATH = DownloaderFactory.RESOURCE_PATH + "services/youtube/extractor/feed/"

    class Kurzgesagt : BaseListExtractorTest {
        @Test
        override fun testServiceId() {
            Assertions.assertEquals(YouTube.serviceId, extractor!!.serviceId)
        }

        @Test
        override fun testName() {
            Assertions.assertTrue(extractor!!.getName().startsWith("Kurzgesagt"))
        }

        @Test
        override fun testId() {
            Assertions.assertEquals("UCsXVk37bltHxD1rDPwtNM8Q", extractor!!.id)
        }

        @Test
        override fun testUrl() {
            Assertions.assertEquals("https://www.youtube.com/channel/UCsXVk37bltHxD1rDPwtNM8Q", extractor!!.url)
        }

        @Test
        @Throws(ParsingException::class)
        override fun testOriginalUrl() {
            Assertions.assertEquals("https://www.youtube.com/user/Kurzgesagt", extractor!!.originalUrl)
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
            private var extractor: YoutubeFeedExtractor? = null


            @BeforeAll
            @Throws(Exception::class)
            fun setUp() {
                YoutubeTestsUtils.ensureStateless()
                init(getDownloader(RESOURCE_PATH))
                extractor = YouTube.getFeedExtractor("https://www.youtube.com/user/Kurzgesagt") as? YoutubeFeedExtractor
                extractor!!.fetchPage()
            }
        }
    }

    class NotAvailable {
        @Test
        @Throws(Exception::class)
        fun AccountTerminatedFetch() {
            val extractor = YouTube
                .getFeedExtractor("https://www.youtube.com/channel/UCTGjY2I-ZUGnwVoWAGRd7XQ") as YoutubeFeedExtractor
            Assertions.assertThrows(ContentNotAvailableException::class.java
            ) { extractor.fetchPage() }
        }

        companion object {

            @BeforeAll
            @Throws(IOException::class)
            fun setUp(): Unit {
                init(getDownloader(RESOURCE_PATH + "notAvailable/"))
            }
        }
    }
}
