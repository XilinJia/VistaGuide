package ac.mdiq.vista.extractor.services.media_ccc

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import ac.mdiq.vista.downloader.DownloaderTestImpl.Companion.getInstance
import ac.mdiq.vista.extractor.ExtractorAsserts
import ac.mdiq.vista.extractor.Vista.init
import ac.mdiq.vista.extractor.ServiceList.MediaCCC
import ac.mdiq.vista.extractor.channel.tabs.ChannelTabExtractor
import ac.mdiq.vista.extractor.services.media_ccc.extractors.MediaCCCConferenceExtractor

/**
 * Test [MediaCCCConferenceExtractor] and [ ]
 */
class MediaCCCConferenceExtractorTest {
    class FrOSCon2017 {
        @Test
        @Throws(Exception::class)
        fun testName() {
            Assertions.assertEquals("FrOSCon 2017", extractor!!.getName())
        }

        @Test
        @Throws(Exception::class)
        fun testGetUrl() {
            Assertions.assertEquals("https://media.ccc.de/c/froscon2017", extractor!!.url)
        }

        @Test
        @Throws(Exception::class)
        fun testGetOriginalUrl() {
            Assertions.assertEquals("https://media.ccc.de/c/froscon2017", extractor!!.originalUrl)
        }

        @Test
        fun testGetThumbnails() {
            ExtractorAsserts.assertContainsImageUrlInImageCollection(
                "https://static.media.ccc.de/media/events/froscon/2017/logo.png",
                extractor!!.getAvatars())
        }

        @Test
        @Throws(Exception::class)
        fun testGetInitalPage() {
            assertEquals(97, tabExtractor!!.initialPage?.items?.size)
        }

        companion object {
            private var extractor: MediaCCCConferenceExtractor? = null
            private var tabExtractor: ChannelTabExtractor? = null


            @BeforeAll
            @Throws(Exception::class)
            fun setUpClass(): Unit {
                init(getInstance()!!)
                extractor = MediaCCC.getChannelExtractor("https://media.ccc.de/c/froscon2017") as? MediaCCCConferenceExtractor
                extractor!!.fetchPage()

                tabExtractor = MediaCCC.getChannelTabExtractor(extractor!!.getTabs()[0])
                tabExtractor!!.fetchPage()
            }
        }
    }

    class Oscal2019 {
        @Test
        @Throws(Exception::class)
        fun testName() {
            Assertions.assertEquals("Open Source Conference Albania 2019", extractor!!.getName())
        }

        @Test
        @Throws(Exception::class)
        fun testGetUrl() {
            Assertions.assertEquals("https://media.ccc.de/c/oscal19", extractor!!.url)
        }

        @Test
        @Throws(Exception::class)
        fun testGetOriginalUrl() {
            Assertions.assertEquals("https://media.ccc.de/c/oscal19", extractor!!.originalUrl)
        }

        @Test
        fun testGetThumbnailUrl() {
            ExtractorAsserts.assertContainsImageUrlInImageCollection(
                "https://static.media.ccc.de/media/events/oscal/2019/oscal-19.png",
                extractor!!.getAvatars())
        }

        @Test
        @Throws(Exception::class)
        fun testGetInitalPage() {
            Assertions.assertTrue(tabExtractor!!.initialPage!!.items!!.size >= 21)
        }

        companion object {
            private var extractor: MediaCCCConferenceExtractor? = null
            private var tabExtractor: ChannelTabExtractor? = null


            @BeforeAll
            @Throws(Exception::class)
            fun setUpClass(): Unit {
                init(getInstance()!!)
                extractor = MediaCCC.getChannelExtractor("https://media.ccc.de/c/oscal19") as? MediaCCCConferenceExtractor
                extractor!!.fetchPage()

                tabExtractor = MediaCCC.getChannelTabExtractor(extractor!!.getTabs()[0])
                tabExtractor!!.fetchPage()
            }
        }
    }
}
