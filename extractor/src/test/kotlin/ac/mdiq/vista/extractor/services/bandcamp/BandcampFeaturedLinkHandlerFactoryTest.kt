// Created by Fynn Godau 2019, licensed GNU GPL version 3 or later
package ac.mdiq.vista.extractor.services.bandcamp

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.services.bandcamp.linkHandler.BandcampFeaturedLinkHandlerFactory

/**
 * Tests for [BandcampFeaturedLinkHandlerFactory]
 */
class BandcampFeaturedLinkHandlerFactoryTest {
    @Test
    @Throws(ParsingException::class)
    fun testAcceptUrl() {
        Assertions.assertTrue(linkHandler!!.acceptUrl("http://bandcamp.com/?show=1"))
        Assertions.assertTrue(linkHandler!!.acceptUrl("https://bandcamp.com/?show=1"))
        Assertions.assertTrue(linkHandler!!.acceptUrl("http://bandcamp.com/?show=2"))
        Assertions.assertTrue(linkHandler!!.acceptUrl("https://bandcamp.com/api/mobile/24/bootstrap_data"))
        assertTrue(linkHandler!!.acceptUrl("https://bandcamp.com/api/bcweekly/3/list"));

        Assertions.assertFalse(linkHandler!!.acceptUrl("https://bandcamp.com/?show="))
        Assertions.assertFalse(linkHandler!!.acceptUrl("https://bandcamp.com/?show=a"))
        Assertions.assertFalse(linkHandler!!.acceptUrl("https://bandcamp.com/"))
    }

    @Test
    @Throws(ParsingException::class)
    fun testGetUrl() {
        Assertions.assertEquals("https://bandcamp.com/api/mobile/24/bootstrap_data", linkHandler!!.getUrl("Featured"))
        assertEquals("https://bandcamp.com/api/bcweekly/3/list", linkHandler!!.getUrl("Radio"));
    }

    @Test
    @Throws(ParsingException::class)
    fun testGetId() {
        Assertions.assertEquals("Featured", linkHandler!!.getId("http://bandcamp.com/api/mobile/24/bootstrap_data"))
        Assertions.assertEquals("Featured", linkHandler!!.getId("https://bandcamp.com/api/mobile/24/bootstrap_data"))
        Assertions.assertEquals("Radio", linkHandler!!.getId("http://bandcamp.com/?show=1"))
        assertEquals("Radio", linkHandler!!.getId("https://bandcamp.com/api/bcweekly/3/list"));
    }

    companion object {
        private var linkHandler: BandcampFeaturedLinkHandlerFactory? = null

        @BeforeAll
        fun setUp() {
            linkHandler = BandcampFeaturedLinkHandlerFactory.instance
        }
    }
}
