// Created by Fynn Godau 2019, licensed GNU GPL version 3 or later
package ac.mdiq.vista.extractor.services.bandcamp

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import ac.mdiq.vista.downloader.DownloaderTestImpl.Companion.getInstance
import ac.mdiq.vista.extractor.ExtractorAsserts.assertTabsContain
import ac.mdiq.vista.extractor.Vista.init
import ac.mdiq.vista.extractor.ServiceList.Bandcamp
import ac.mdiq.vista.extractor.channel.ChannelExtractor
import ac.mdiq.vista.extractor.channel.tabs.ChannelTabs
import ac.mdiq.vista.extractor.services.BaseChannelExtractorTest

class BandcampChannelExtractorTest : BaseChannelExtractorTest {
    @Test
    @Throws(Exception::class)
    override fun testDescription() {
        assertEquals("making music:)", extractor!!.getDescription())
    }

    @Test
    @Throws(Exception::class)
    override fun testAvatars() {
        BandcampTestUtils.testImages(extractor!!.getAvatars())
    }

    @Test
    @Throws(Exception::class)
    override fun testBanners() {
        BandcampTestUtils.testImages(extractor!!.getBanners())
    }

    @Test
    @Throws(Exception::class)
    override fun testFeedUrl() {
        Assertions.assertNull(extractor!!.getFeedUrl())
    }

    @Test
    @Throws(Exception::class)
    override fun testSubscriberCount() {
        assertEquals(-1, extractor!!.getSubscriberCount())
    }

    @Test
    @Throws(Exception::class)
    override fun testVerified() {
        assertFalse(extractor!!.isVerified())
    }

    @Test
    override fun testServiceId() {
        assertEquals(Bandcamp.serviceId, extractor!!.serviceId)
    }

    @Test
    @Throws(Exception::class)
    override fun testName() {
        assertEquals("toupie", extractor!!.getName())
    }

    @Test
    @Throws(Exception::class)
    override fun testId() {
        assertEquals("2450875064", extractor!!.id)
    }

    @Test
    @Throws(Exception::class)
    override fun testUrl() {
        assertEquals("https://toupie.bandcamp.com", extractor!!.url)
    }

    @Test
    @Throws(Exception::class)
    override fun testOriginalUrl() {
        assertEquals("https://toupie.bandcamp.com", extractor!!.url)
    }

    @Test
    @Throws(Exception::class)
    override fun testTabs() {
        assertTabsContain(extractor!!.getTabs(), ChannelTabs.ALBUMS)
    }

    @Test
    @Throws(Exception::class)
    override fun testTags() {
        Assertions.assertTrue(extractor!!.getTags().isEmpty())
    }

    companion object {
        private var extractor: ChannelExtractor? = null


        @BeforeAll
        @Throws(Exception::class)
        fun setUp(): Unit {
            init(getInstance()!!)
            extractor = Bandcamp.getChannelExtractor("https://toupie.bandcamp.com/releases")
            extractor!!.fetchPage()
        }
    }
}
