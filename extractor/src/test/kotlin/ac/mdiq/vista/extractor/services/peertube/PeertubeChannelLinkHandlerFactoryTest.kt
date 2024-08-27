package ac.mdiq.vista.extractor.services.peertube

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import ac.mdiq.vista.downloader.DownloaderTestImpl.Companion.getInstance
import ac.mdiq.vista.extractor.Vista.init
import ac.mdiq.vista.extractor.ServiceList.PeerTube
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.services.peertube.linkHandler.PeertubeChannelLinkHandlerFactory

/**
 * Test for [PeertubeChannelLinkHandlerFactory]
 */
class PeertubeChannelLinkHandlerFactoryTest {
    @Test
    @Throws(ParsingException::class)
    fun acceptUrlTest() {
        Assertions.assertTrue(linkHandler!!.acceptUrl("https://peertube.stream/accounts/kranti@videos.squat.net"))
        Assertions.assertTrue(linkHandler!!.acceptUrl("https://peertube.stream/a/kranti@videos.squat.net"))
        Assertions.assertTrue(linkHandler!!.acceptUrl("https://peertube.stream/api/v1/accounts/kranti@videos.squat.net/videos"))
        Assertions.assertTrue(linkHandler!!.acceptUrl("https://peertube.stream/video-channels/kranti_channel@videos.squat.net/videos"))
        Assertions.assertTrue(linkHandler!!.acceptUrl("https://peertube.stream/c/kranti_channel@videos.squat.net/videos"))
        Assertions.assertTrue(linkHandler!!.acceptUrl("https://peertube.stream/api/v1/video-channels/7682d9f2-07be-4622-862e-93ec812e2ffa"))

        PeertubeLinkHandlerFactoryTestHelper.assertDoNotAcceptNonURLs(linkHandler!!)
    }

    @get:Throws(ParsingException::class)
    @get:Test
    val id: Unit
        get() {
            Assertions.assertEquals("accounts/kranti@videos.squat.net",
                linkHandler!!.fromUrl("https://peertube.stream/accounts/kranti@videos.squat.net").id)
            Assertions.assertEquals("accounts/kranti@videos.squat.net",
                linkHandler!!.fromUrl("https://peertube.stream/a/kranti@videos.squat.net").id)
            Assertions.assertEquals("accounts/kranti@videos.squat.net",
                linkHandler!!.fromUrl("https://peertube.stream/accounts/kranti@videos.squat.net/videos").id)
            Assertions.assertEquals("accounts/kranti@videos.squat.net",
                linkHandler!!.fromUrl("https://peertube.stream/a/kranti@videos.squat.net/videos").id)
            Assertions.assertEquals("accounts/kranti@videos.squat.net",
                linkHandler!!.fromUrl("https://peertube.stream/api/v1/accounts/kranti@videos.squat.net").id)
            Assertions.assertEquals("accounts/kranti@videos.squat.net",
                linkHandler!!.fromUrl("https://peertube.stream/api/v1/accounts/kranti@videos.squat.net/videos").id)

            Assertions.assertEquals("video-channels/kranti_channel@videos.squat.net",
                linkHandler!!.fromUrl("https://peertube.stream/video-channels/kranti_channel@videos.squat.net/videos").id)
            Assertions.assertEquals("video-channels/kranti_channel@videos.squat.net",
                linkHandler!!.fromUrl("https://peertube.stream/c/kranti_channel@videos.squat.net/videos").id)
            Assertions.assertEquals("video-channels/kranti_channel@videos.squat.net",
                linkHandler!!.fromUrl("https://peertube.stream/c/kranti_channel@videos.squat.net/video-playlists").id)
            Assertions.assertEquals("video-channels/kranti_channel@videos.squat.net",
                linkHandler!!.fromUrl("https://peertube.stream/api/v1/video-channels/kranti_channel@videos.squat.net").id)
        }

    @get:Throws(ParsingException::class)
    @get:Test
    val url: Unit
        get() {
            Assertions.assertEquals("https://peertube.stream/video-channels/kranti_channel@videos.squat.net",
                linkHandler!!.fromId("video-channels/kranti_channel@videos.squat.net").url)
            Assertions.assertEquals("https://peertube.stream/accounts/kranti@videos.squat.net",
                linkHandler!!.fromId("accounts/kranti@videos.squat.net").url)
            Assertions.assertEquals("https://peertube.stream/accounts/kranti@videos.squat.net",
                linkHandler!!.fromId("kranti@videos.squat.net").url)
            Assertions.assertEquals("https://peertube.stream/video-channels/kranti_channel@videos.squat.net",
                linkHandler!!.fromUrl("https://peertube.stream/api/v1/video-channels/kranti_channel@videos.squat.net").url)
        }

    companion object {
        private var linkHandler: PeertubeChannelLinkHandlerFactory? = null


        @BeforeAll
        fun setUp(): Unit {
            PeerTube.instance = PeertubeInstance("https://peertube.stream", "PeerTube on peertube.stream")
            linkHandler = PeertubeChannelLinkHandlerFactory.instance
            init(getInstance()!!)
        }
    }
}
