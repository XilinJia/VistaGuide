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
import ac.mdiq.vista.extractor.services.peertube.extractors.PeertubeChannelExtractor

/**
 * Test for [PeertubeChannelExtractor]
 */
class PeertubeChannelExtractorTest {
    class LaQuadratureDuNet : BaseChannelExtractorTest {
        @Test
        override fun testServiceId() {
            Assertions.assertEquals(PeerTube.serviceId, extractor!!.serviceId)
        }

        @Test
        @Throws(ParsingException::class)
        override fun testName() {
            Assertions.assertEquals("La Quadrature du Net", extractor!!.getName())
        }

        @Test
        @Throws(ParsingException::class)
        override fun testId() {
            Assertions.assertEquals("video-channels/lqdn_channel@video.lqdn.fr", extractor!!.id)
        }

        @Test
        @Throws(ParsingException::class)
        override fun testUrl() {
            Assertions.assertEquals("https://framatube.org/video-channels/lqdn_channel@video.lqdn.fr", extractor!!.url)
        }

        @Test
        @Throws(ParsingException::class)
        override fun testOriginalUrl() {
            Assertions.assertEquals("https://framatube.org/video-channels/lqdn_channel@video.lqdn.fr/videos",
                extractor!!.originalUrl)
        }

        @Test
        override fun testDescription() {
            Assertions.assertNotNull(extractor!!.getDescription())
        }

        @Test
        @Throws(ParsingException::class)
        fun testParentChannelName() {
            Assertions.assertEquals("lqdn", extractor!!.getParentChannelName())
        }

        @Test
        @Throws(ParsingException::class)
        fun testParentChannelUrl() {
            Assertions.assertEquals("https://video.lqdn.fr/accounts/lqdn", extractor!!.getParentChannelUrl())
        }

        @Test
        fun testParentChannelAvatarUrl() {
            defaultTestImageCollection(extractor!!.getParentChannelAvatars())
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
            Assertions.assertEquals("https://framatube.org/feeds/videos.xml?videoChannelId=1126",
                extractor!!.getFeedUrl())
        }

        @Test
        override fun testSubscriberCount() {
            ExtractorAsserts.assertGreaterOrEqual(230, extractor!!.getSubscriberCount())
        }

        @Test
        @Throws(Exception::class)
        override fun testVerified() {
            Assertions.assertFalse(extractor!!.isVerified())
        }

        @Test
        @Throws(Exception::class)
        override fun testTabs() {
            ExtractorAsserts.assertTabsContain(extractor!!.getTabs(), ChannelTabs.VIDEOS, ChannelTabs.PLAYLISTS)
        }

        @Test
        @Throws(Exception::class)
        override fun testTags() {
            Assertions.assertTrue(extractor!!.getTags().isEmpty())
        }

        companion object {
            private var extractor: PeertubeChannelExtractor? = null


            @BeforeAll
            @Throws(Exception::class)
            fun setUp(): Unit {
                init(getInstance()!!)
                // setting instance might break test when running in parallel
                PeerTube.instance = PeertubeInstance("https://framatube.org", "Framatube")
                extractor = PeerTube.getChannelExtractor("https://framatube.org/video-channels/lqdn_channel@video.lqdn.fr/videos") as? PeertubeChannelExtractor
                extractor!!.fetchPage()
            }
        }
    }

    class ChatSceptique : BaseChannelExtractorTest {
        @Test
        override fun testServiceId() {
            Assertions.assertEquals(PeerTube.serviceId, extractor!!.serviceId)
        }

        @Test
        @Throws(ParsingException::class)
        override fun testName() {
            Assertions.assertEquals("Chat Sceptique", extractor!!.getName())
        }

        @Test
        @Throws(ParsingException::class)
        override fun testId() {
            Assertions.assertEquals("video-channels/chatsceptique@skeptikon.fr", extractor!!.id)
        }

        @Test
        @Throws(ParsingException::class)
        override fun testUrl() {
            Assertions.assertEquals("https://framatube.org/video-channels/chatsceptique@skeptikon.fr", extractor!!.url)
        }

        @Test
        @Throws(ParsingException::class)
        override fun testOriginalUrl() {
            Assertions.assertEquals("https://framatube.org/api/v1/video-channels/chatsceptique@skeptikon.fr",
                extractor!!.originalUrl)
        }

        @Test
        override fun testDescription() {
            Assertions.assertNotNull(extractor!!.getDescription())
        }

        @Test
        @Throws(ParsingException::class)
        fun testParentChannelName() {
            Assertions.assertEquals("nathan", extractor!!.getParentChannelName())
        }

        @Test
        @Throws(ParsingException::class)
        fun testParentChannelUrl() {
            Assertions.assertEquals("https://skeptikon.fr/accounts/nathan", extractor!!.getParentChannelUrl())
        }

        @Test
        fun testParentChannelAvatars() {
            defaultTestImageCollection(extractor!!.getParentChannelAvatars())
        }

        @Test
        override fun testAvatars() {
            defaultTestImageCollection(extractor!!.getAvatars())
        }

        @Test
        @Throws(ParsingException::class)
        override fun testBanners() {
            ExtractorAsserts.assertEmpty(extractor!!.getBanners())
        }

        @Test
        @Throws(ParsingException::class)
        override fun testFeedUrl() {
            Assertions.assertEquals("https://framatube.org/feeds/videos.xml?videoChannelId=137",
                extractor!!.getFeedUrl())
        }

        @Test
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
            ExtractorAsserts.assertTabsContain(extractor!!.getTabs(), ChannelTabs.VIDEOS, ChannelTabs.PLAYLISTS)
        }

        @Test
        @Throws(Exception::class)
        override fun testTags() {
            Assertions.assertTrue(extractor!!.getTags().isEmpty())
        }

        companion object {
            private var extractor: PeertubeChannelExtractor? = null


            @BeforeAll
            @Throws(Exception::class)
            fun setUp(): Unit {
                init(getInstance()!!)
                // setting instance might break test when running in parallel
                PeerTube.instance = PeertubeInstance("https://framatube.org", "Framatube")
                extractor = PeerTube.getChannelExtractor("https://framatube.org/api/v1/video-channels/chatsceptique@skeptikon.fr") as? PeertubeChannelExtractor
                extractor!!.fetchPage()
            }
        }
    }
}
