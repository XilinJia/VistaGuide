package ac.mdiq.vista.extractor.services.peertube

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import ac.mdiq.vista.downloader.DownloaderTestImpl.Companion.getInstance
import ac.mdiq.vista.extractor.ExtractorAsserts
import ac.mdiq.vista.extractor.Vista.init
import ac.mdiq.vista.extractor.ServiceList.PeerTube
import ac.mdiq.vista.extractor.channel.tabs.ChannelTabs
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.services.BaseChannelExtractorTest
import ac.mdiq.vista.extractor.services.DefaultTests.defaultTestImageCollection
import ac.mdiq.vista.extractor.services.peertube.extractors.PeertubeAccountExtractor

/**
 * Test for [PeertubeAccountExtractor]
 */
class PeertubeAccountExtractorTest {
    class Framasoft : BaseChannelExtractorTest {
        @Test
        override fun testServiceId() {
            Assertions.assertEquals(PeerTube.serviceId, extractor!!.serviceId)
        }

        @Test
        @Throws(ParsingException::class)
        override fun testName() {
            Assertions.assertEquals("Framasoft", extractor!!.getName())
        }

        @Test
        @Throws(ParsingException::class)
        override fun testId() {
            Assertions.assertEquals("accounts/framasoft", extractor!!.id)
        }

        @Test
        @Throws(ParsingException::class)
        override fun testUrl() {
            Assertions.assertEquals("https://framatube.org/accounts/framasoft", extractor!!.url)
        }

        @Test
        @Throws(ParsingException::class)
        override fun testOriginalUrl() {
            Assertions.assertEquals("https://framatube.org/accounts/framasoft", extractor!!.originalUrl)
        }

        @Test
        @Throws(ParsingException::class)
        override fun testDescription() {
            Assertions.assertNull(extractor!!.getDescription())
        }

        @Test
        override fun testAvatars() {
            defaultTestImageCollection(extractor!!.getAvatars())
        }

        @Test
        override fun testBanners() {
            ExtractorAsserts.assertEmpty(extractor!!.getBanners())
        }

        @Test
        @Throws(ParsingException::class)
        override fun testFeedUrl() {
            Assertions.assertEquals("https://framatube.org/feeds/videos.xml?accountId=3", extractor!!.getFeedUrl())
        }

        @Test
        @Throws(ParsingException::class)
        override fun testSubscriberCount() {
            ExtractorAsserts.assertGreaterOrEqual(700, extractor!!.getSubscriberCount())
        }

        @Test
        @Throws(Exception::class)
        override fun testVerified() {
            Assertions.assertFalse(extractor!!.isVerified())
        }

        @Test
        @Throws(Exception::class)
        override fun testTabs() {
            ExtractorAsserts.assertTabsContain(extractor!!.getTabs(), ChannelTabs.VIDEOS, ChannelTabs.CHANNELS)
        }

        @Test
        @Throws(Exception::class)
        override fun testTags() {
            Assertions.assertTrue(extractor!!.getTags().isEmpty())
        }

        companion object {
            private var extractor: PeertubeAccountExtractor? = null


            @BeforeAll
            @Throws(Exception::class)
            fun setUp(): Unit {
                init(getInstance()!!)
                // setting instance might break test when running in parallel
                PeerTube.instance = PeertubeInstance("https://framatube.org", "Framatube")
                extractor = PeerTube.getChannelExtractor("https://framatube.org/accounts/framasoft") as? PeertubeAccountExtractor
                extractor!!.fetchPage()
            }
        }
    }

    class FreeSoftwareFoundation : BaseChannelExtractorTest {
        @Test
        override fun testServiceId() {
            Assertions.assertEquals(PeerTube.serviceId, extractor!!.serviceId)
        }

        @Test
        @Throws(ParsingException::class)
        override fun testName() {
            Assertions.assertEquals("Free Software Foundation", extractor!!.getName())
        }

        @Test
        @Throws(ParsingException::class)
        override fun testId() {
            Assertions.assertEquals("accounts/fsf", extractor!!.id)
        }

        @Test
        @Throws(ParsingException::class)
        override fun testUrl() {
            Assertions.assertEquals("https://framatube.org/accounts/fsf", extractor!!.url)
        }

        @Test
        @Throws(ParsingException::class)
        override fun testOriginalUrl() {
            Assertions.assertEquals("https://framatube.org/api/v1/accounts/fsf", extractor!!.originalUrl)
        }

        @Test
        override fun testDescription() {
            Assertions.assertNotNull(extractor!!.getDescription())
        }

        @Test
        override fun testAvatars() {
            defaultTestImageCollection(extractor!!.getAvatars())
        }

        @Test
        override fun testBanners() {
            ExtractorAsserts.assertEmpty(extractor!!.getBanners())
        }

        @Test
        @Throws(ParsingException::class)
        override fun testFeedUrl() {
            Assertions.assertEquals("https://framatube.org/feeds/videos.xml?accountId=8178", extractor!!.getFeedUrl())
        }

        @Test
        @Throws(ParsingException::class)
        override fun testSubscriberCount() {
            ExtractorAsserts.assertGreaterOrEqual(100, extractor!!.getSubscriberCount())
        }

        @Test
        @Throws(Exception::class)
        override fun testVerified() {
            Assertions.assertFalse(extractor!!.isVerified())
        }

        @Test
        @Throws(Exception::class)
        override fun testTabs() {
            ExtractorAsserts.assertTabsContain(extractor!!.getTabs(), ChannelTabs.VIDEOS, ChannelTabs.CHANNELS)
        }

        @Test
        @Throws(Exception::class)
        override fun testTags() {
            Assertions.assertTrue(extractor!!.getTags().isEmpty())
        }

        companion object {
            private var extractor: PeertubeAccountExtractor? = null


            @BeforeAll
            @Throws(Exception::class)
            fun setUp(): Unit {
                init(getInstance()!!)
                // setting instance might break test when running in parallel
                PeerTube.instance = PeertubeInstance("https://framatube.org", "Framatube")
                extractor = PeerTube.getChannelExtractor("https://framatube.org/api/v1/accounts/fsf") as? PeertubeAccountExtractor
                extractor!!.fetchPage()
            }
        }
    }
}
