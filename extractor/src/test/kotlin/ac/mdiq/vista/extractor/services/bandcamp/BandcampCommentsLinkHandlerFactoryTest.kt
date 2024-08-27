// Created by Fynn Godau 2019, licensed GNU GPL version 3 or later
package ac.mdiq.vista.extractor.services.bandcamp

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import ac.mdiq.vista.downloader.DownloaderTestImpl.Companion.getInstance
import ac.mdiq.vista.extractor.Vista.init
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.services.bandcamp.linkHandler.BandcampCommentsLinkHandlerFactory

/**
 * Test for [BandcampCommentsLinkHandlerFactory]
 */
class BandcampCommentsLinkHandlerFactoryTest {
    @Test
    @Throws(ParsingException::class)
    fun testAcceptUrl() {
        Assertions.assertFalse(linkHandler!!.acceptUrl("http://interovgm.com/releases/"))
        Assertions.assertFalse(linkHandler!!.acceptUrl("https://interovgm.com/releases"))
        Assertions.assertFalse(linkHandler!!.acceptUrl("http://zachbenson.bandcamp.com"))
        Assertions.assertFalse(linkHandler!!.acceptUrl("https://bandcamp.com"))
        Assertions.assertFalse(linkHandler!!.acceptUrl("https://zachbenson.bandcamp.com/"))
        Assertions.assertFalse(linkHandler!!.acceptUrl("https://example.com/track/sampletrack"))

        Assertions.assertTrue(linkHandler!!.acceptUrl("http://bandcamP.com/?show=38"))
        Assertions.assertTrue(linkHandler!!.acceptUrl("https://powertothequeerkids.bandcamp.com/album/power-to-the-queer-kids"))
        Assertions.assertTrue(linkHandler!!.acceptUrl("https://zachbenson.bandcamp.com/track/kitchen"))
        Assertions.assertTrue(linkHandler!!.acceptUrl("http://ZachBenson.Bandcamp.COM/Track/U-I-Tonite/"))
        Assertions.assertTrue(linkHandler!!.acceptUrl("https://interovgm.bandcamp.com/track/title"))
        Assertions.assertTrue(linkHandler!!.acceptUrl("https://goodgoodblood-tl.bandcamp.com/track/when-it-all-wakes-up"))
    }

    companion object {
        private var linkHandler: BandcampCommentsLinkHandlerFactory? = null

        @BeforeAll
        fun setUp() {
            linkHandler = BandcampCommentsLinkHandlerFactory.instance
            init(getInstance()!!)
        }
    }
}
