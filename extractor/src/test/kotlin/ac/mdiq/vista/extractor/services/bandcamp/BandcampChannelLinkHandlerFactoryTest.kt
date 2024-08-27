// Created by Fynn Godau 2019, licensed GNU GPL version 3 or later
package ac.mdiq.vista.extractor.services.bandcamp

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import ac.mdiq.vista.downloader.DownloaderTestImpl.Companion.getInstance
import ac.mdiq.vista.extractor.Vista.init
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.services.bandcamp.linkHandler.BandcampChannelLinkHandlerFactory

/**
 * Test for [BandcampChannelLinkHandlerFactory]
 */
class BandcampChannelLinkHandlerFactoryTest {
    @Test
    @Throws(ParsingException::class)
    fun testAcceptUrl() {
        // Bandcamp URLs
        Assertions.assertTrue(linkHandler!!.acceptUrl("http://zachbenson.bandcamp.com"))
        Assertions.assertTrue(linkHandler!!.acceptUrl("https://zachbenson.bandcamp.com/"))
        Assertions.assertTrue(linkHandler!!.acceptUrl("https://billwurtz.bandcamp.com/releases"))
        Assertions.assertTrue(linkHandler!!.acceptUrl("https://interovgm.bandcamp.com/releases"))
        Assertions.assertTrue(linkHandler!!.acceptUrl("https://interovgm.bandcamp.com/releases/"))

        Assertions.assertTrue(linkHandler!!.acceptUrl("http://zachbenson.bandcamp.com/"))

        Assertions.assertFalse(linkHandler!!.acceptUrl("https://bandcamp.com"))
        Assertions.assertFalse(linkHandler!!.acceptUrl("https://zachbenson.bandcamp.com/track/kitchen"))
        Assertions.assertFalse(linkHandler!!.acceptUrl("https://daily.bandcamp.com/"))
        Assertions.assertFalse(linkHandler!!.acceptUrl("https://DAILY.BANDCAMP.COM"))
        Assertions.assertFalse(linkHandler!!.acceptUrl("https://daily.bandcamp.com/best-of-2020/bandcamp-daily-staffers-on-their-favorite-albums-of-2020"))

        // External URLs
        Assertions.assertTrue(linkHandler!!.acceptUrl("https://lobstertheremin.com"))
        Assertions.assertTrue(linkHandler!!.acceptUrl("https://lobstertheremin.com/music"))
        Assertions.assertTrue(linkHandler!!.acceptUrl("https://lobstertheremin.com/music/"))
        Assertions.assertTrue(linkHandler!!.acceptUrl("https://diskak.usopop.com/"))
        Assertions.assertTrue(linkHandler!!.acceptUrl("https://diskak.usopop.com/releases"))
        Assertions.assertTrue(linkHandler!!.acceptUrl("https://diskak.usopop.com/RELEASES"))

        Assertions.assertFalse(linkHandler!!.acceptUrl("https://example.com/releases"))
    }

    @Test
    @Throws(ParsingException::class)
    fun testGetId() {
        Assertions.assertEquals("1196681540", linkHandler!!.getId("https://macbenson.bandcamp.com/"))
        Assertions.assertEquals("1196681540", linkHandler!!.getId("http://macbenson.bandcamp.com/"))
        Assertions.assertEquals("1581461772", linkHandler!!.getId("https://shirakumon.bandcamp.com/releases"))
        Assertions.assertEquals("3321800855", linkHandler!!.getId("https://infiniteammo.bandcamp.com/"))
        Assertions.assertEquals("3775652329", linkHandler!!.getId("https://npet.bandcamp.com/"))

        Assertions.assertEquals("2735462545", linkHandler!!.getId("http://lobstertheremin.com/"))
        Assertions.assertEquals("2735462545", linkHandler!!.getId("https://lobstertheremin.com/music/"))
        Assertions.assertEquals("3826445168", linkHandler!!.getId("https://diskak.usopop.com/releases"))
    }

    @Test
    @Throws(ParsingException::class)
    fun testGetUrl() {
        Assertions.assertEquals("https://macbenson.bandcamp.com", linkHandler!!.getUrl("1196681540"))
        Assertions.assertEquals("https://shirakumon.bandcamp.com", linkHandler!!.getUrl("1581461772"))
        Assertions.assertEquals("https://infiniteammo.bandcamp.com", linkHandler!!.getUrl("3321800855"))

        Assertions.assertEquals("https://lobstertheremin.com", linkHandler!!.getUrl("2735462545"))
    }

    @Test
    fun testGetUrlWithInvalidId() {
        Assertions.assertThrows(ParsingException::class.java
        ) { linkHandler!!.getUrl("0") }
    }

    @Test
    fun testGetIdWithInvalidUrl() {
        Assertions.assertThrows(ParsingException::class.java
        ) { linkHandler!!.getUrl("https://bandcamp.com") }
    }

    companion object {
        private var linkHandler: BandcampChannelLinkHandlerFactory? = null

        @BeforeAll
        fun setUp() {
            linkHandler = BandcampChannelLinkHandlerFactory.instance
            init(getInstance())
        }
    }
}
