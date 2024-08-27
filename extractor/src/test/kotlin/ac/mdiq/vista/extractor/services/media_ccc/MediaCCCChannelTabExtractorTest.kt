package ac.mdiq.vista.extractor.services.media_ccc

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import ac.mdiq.vista.downloader.DownloaderTestImpl.Companion.getInstance
import ac.mdiq.vista.extractor.Vista.init
import ac.mdiq.vista.extractor.ServiceList.MediaCCC
import ac.mdiq.vista.extractor.channel.tabs.ChannelTabExtractor
import ac.mdiq.vista.extractor.channel.tabs.ChannelTabs

/**
 * Test that it is possible to create and use a channel tab extractor ([ ]) without
 * passing through the conference extractor
 */
class MediaCCCChannelTabExtractorTest {
    class CCCamp2023 {
        @Test
        fun testName() {
            Assertions.assertEquals(ChannelTabs.VIDEOS, extractor!!.getName())
        }

        @Test
        @Throws(Exception::class)
        fun testGetUrl() {
            Assertions.assertEquals("https://media.ccc.de/c/camp2023", extractor!!.url)
        }

        @Test
        @Throws(Exception::class)
        fun testGetOriginalUrl() {
            Assertions.assertEquals("https://media.ccc.de/c/camp2023", extractor!!.originalUrl)
        }

        @Test
        @Throws(Exception::class)
        fun testGetInitalPage() {
            assertEquals(177, extractor!!.initialPage?.items?.size)
        }

        companion object {
            private var extractor: ChannelTabExtractor? = null


            @BeforeAll
            @Throws(Exception::class)
            fun setUpClass(): Unit {
                init(getInstance()!!)
                extractor = MediaCCC.getChannelTabExtractorFromId("camp2023", ChannelTabs.VIDEOS)
                extractor!!.fetchPage()
            }
        }
    }
}
