package ac.mdiq.vista.extractor.services.soundcloud

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import ac.mdiq.vista.downloader.DownloaderTestImpl.Companion.getInstance
import ac.mdiq.vista.extractor.ExtractorAsserts
import ac.mdiq.vista.extractor.Vista.init
import ac.mdiq.vista.extractor.ServiceList.SoundCloud
import ac.mdiq.vista.extractor.channel.tabs.ChannelTabs
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.services.BaseChannelExtractorTest
import ac.mdiq.vista.extractor.services.DefaultTests.defaultTestImageCollection
import ac.mdiq.vista.extractor.services.soundcloud.extractors.SoundcloudChannelExtractor

/**
 * Test for [SoundcloudChannelExtractor]
 */
class SoundcloudChannelExtractorTest {
    class LilUzi : BaseChannelExtractorTest {
        @Test
        override fun testServiceId() {
            Assertions.assertEquals(SoundCloud.serviceId, extractor!!.serviceId)
        }

        @Test
        override fun testName() {
            Assertions.assertEquals("Lil Uzi Vert", extractor!!.getName())
        }

        @Test
        override fun testId() {
            Assertions.assertEquals("10494998", extractor!!.id)
        }

        @Test
        @Throws(ParsingException::class)
        override fun testUrl() {
            Assertions.assertEquals("https://soundcloud.com/liluzivert", extractor!!.url)
        }

        @Test
        @Throws(ParsingException::class)
        override fun testOriginalUrl() {
            Assertions.assertEquals("http://soundcloud.com/liluzivert/sets", extractor!!.originalUrl)
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
            defaultTestImageCollection(extractor!!.getBanners())
        }

        @Test
        override fun testFeedUrl() {
            ExtractorAsserts.assertEmpty(extractor!!.getFeedUrl())
        }

        @Test
        override fun testSubscriberCount() {
            Assertions.assertTrue(extractor!!.getSubscriberCount() >= 1e6, "Wrong subscriber count")
        }

        @Throws(Exception::class)
        override fun testVerified() {
            Assertions.assertTrue(extractor!!.isVerified())
        }

        @Test
        @Throws(Exception::class)
        override fun testTabs() {
            ExtractorAsserts.assertTabsContain(extractor!!.getTabs(), ChannelTabs.TRACKS, ChannelTabs.PLAYLISTS,
                ChannelTabs.ALBUMS)
        }

        @Test
        @Throws(Exception::class)
        override fun testTags() {
            Assertions.assertTrue(extractor!!.getTags().isEmpty())
        }

        companion object {
            private var extractor: SoundcloudChannelExtractor? = null


            @BeforeAll
            @Throws(Exception::class)
            fun setUp(): Unit {
                init(getInstance()!!)
                extractor = SoundCloud.getChannelExtractor("http://soundcloud.com/liluzivert/sets") as? SoundcloudChannelExtractor
                extractor!!.fetchPage()
            }
        }
    }

    class DubMatix : BaseChannelExtractorTest {
        @Test
        override fun testServiceId() {
            Assertions.assertEquals(SoundCloud.serviceId, extractor!!.serviceId)
        }

        @Test
        override fun testName() {
            Assertions.assertEquals("dubmatix", extractor!!.getName())
        }

        @Test
        override fun testId() {
            Assertions.assertEquals("542134", extractor!!.id)
        }

        @Test
        @Throws(ParsingException::class)
        override fun testUrl() {
            Assertions.assertEquals("https://soundcloud.com/dubmatix", extractor!!.url)
        }

        @Test
        @Throws(ParsingException::class)
        override fun testOriginalUrl() {
            Assertions.assertEquals("https://soundcloud.com/dubmatix", extractor!!.originalUrl)
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
            defaultTestImageCollection(extractor!!.getBanners())
        }

        @Test
        override fun testFeedUrl() {
            ExtractorAsserts.assertEmpty(extractor!!.getFeedUrl())
        }

        @Test
        override fun testSubscriberCount() {
            Assertions.assertTrue(extractor!!.getSubscriberCount() >= 2e6, "Wrong subscriber count")
        }

        @Throws(Exception::class)
        override fun testVerified() {
            Assertions.assertTrue(extractor!!.isVerified())
        }

        @Test
        @Throws(Exception::class)
        override fun testTabs() {
            ExtractorAsserts.assertTabsContain(extractor!!.getTabs(), ChannelTabs.TRACKS, ChannelTabs.PLAYLISTS,
                ChannelTabs.ALBUMS)
        }

        @Test
        @Throws(Exception::class)
        override fun testTags() {
            Assertions.assertTrue(extractor!!.getTags().isEmpty())
        }

        companion object {
            private var extractor: SoundcloudChannelExtractor? = null


            @BeforeAll
            @Throws(Exception::class)
            fun setUp(): Unit {
                init(getInstance()!!)
                extractor = SoundCloud.getChannelExtractor("https://soundcloud.com/dubmatix") as? SoundcloudChannelExtractor
                extractor!!.fetchPage()
            }
        }
    }
}
