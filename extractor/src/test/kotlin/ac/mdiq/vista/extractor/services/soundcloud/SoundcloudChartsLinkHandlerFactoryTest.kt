package ac.mdiq.vista.extractor.services.soundcloud

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import ac.mdiq.vista.downloader.DownloaderTestImpl.Companion.getInstance
import ac.mdiq.vista.extractor.Vista.init
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.services.soundcloud.linkHandler.SoundcloudChartsLinkHandlerFactory

/**
 * Test for [SoundcloudChartsLinkHandlerFactory]
 */
class SoundcloudChartsLinkHandlerFactoryTest {
    @get:Throws(Exception::class)
    @get:Test
    val url: Unit
        get() {
            Assertions.assertEquals(linkHandler!!.fromId("Top 50").url,
                "https://soundcloud.com/charts/top")
            Assertions.assertEquals(linkHandler!!.fromId("New & hot").url,
                "https://soundcloud.com/charts/new")
        }

    @get:Throws(ParsingException::class)
    @get:Test
    val id: Unit
        get() {
            Assertions.assertEquals(linkHandler!!.fromUrl("http://soundcloud.com/charts/top?genre=all-music").id,
                "Top 50")
            Assertions.assertEquals(linkHandler!!.fromUrl("HTTP://www.soundcloud.com/charts/new/?genre=all-music&country=all-countries").id,
                "New & hot")
        }

    @Test
    @Throws(ParsingException::class)
    fun acceptUrl() {
        Assertions.assertTrue(linkHandler!!.acceptUrl("https://soundcloud.com/charts"))
        Assertions.assertTrue(linkHandler!!.acceptUrl("https://soundcloud.com/charts/"))
        Assertions.assertTrue(linkHandler!!.acceptUrl("https://www.soundcloud.com/charts/new"))
        Assertions.assertTrue(linkHandler!!.acceptUrl("http://soundcloud.com/charts/top?genre=all-music"))
        Assertions.assertTrue(linkHandler!!.acceptUrl("HTTP://www.soundcloud.com/charts/new/?genre=all-music&country=all-countries"))

        Assertions.assertFalse(linkHandler!!.acceptUrl("kdskjfiiejfia"))
        Assertions.assertFalse(linkHandler!!.acceptUrl("soundcloud.com/charts askjkf"))
        Assertions.assertFalse(linkHandler!!.acceptUrl("    soundcloud.com/charts"))
        Assertions.assertFalse(linkHandler!!.acceptUrl(""))
    }

    companion object {
        private var linkHandler: SoundcloudChartsLinkHandlerFactory? = null

        @BeforeAll
        fun setUp() {
            linkHandler = SoundcloudChartsLinkHandlerFactory.instance
            init(getInstance()!!)
        }
    }
}
