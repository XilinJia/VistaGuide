package ac.mdiq.vista.extractor.services.peertube

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import ac.mdiq.vista.downloader.DownloaderTestImpl.Companion.getInstance
import ac.mdiq.vista.extractor.Vista.init
import ac.mdiq.vista.extractor.ServiceList.PeerTube
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.services.peertube.PeertubeLinkHandlerFactoryTestHelper.assertDoNotAcceptNonURLs
import ac.mdiq.vista.extractor.services.peertube.linkHandler.PeertubeStreamLinkHandlerFactory

/**
 * Test for [PeertubeStreamLinkHandlerFactory]
 */
class PeertubeStreamLinkHandlerFactoryTest {
    @get:Throws(Exception::class)
    @get:Test
    val id: Unit
        get() {
            Assertions.assertEquals("986aac60-1263-4f73-9ce5-36b18225cb60",
                linkHandler!!.fromUrl("https://peertube.mastodon.host/videos/watch/986aac60-1263-4f73-9ce5-36b18225cb60").id)
            Assertions.assertEquals("986aac60-1263-4f73-9ce5-36b18225cb60",
                linkHandler!!.fromUrl("https://peertube.mastodon.host/videos/watch/986aac60-1263-4f73-9ce5-36b18225cb60?fsdafs=fsafa").id)
            Assertions.assertEquals("986aac60-1263-4f73-9ce5-36b18225cb60",
                linkHandler!!.fromUrl("https://peertube.mastodon.host/api/v1/videos/watch/986aac60-1263-4f73-9ce5-36b18225cb60").id)
            Assertions.assertEquals("986aac60-1263-4f73-9ce5-36b18225cb60",
                linkHandler!!.fromUrl("https://peertube.mastodon.host/api/v1/videos/watch/986aac60-1263-4f73-9ce5-36b18225cb60?fsdafs=fsafa").id)

            Assertions.assertEquals("9c9de5e8-0a1e-484a-b099-e80766180a6d",
                linkHandler!!.fromUrl("https://framatube.org/videos/watch/9c9de5e8-0a1e-484a-b099-e80766180a6d").id)
            Assertions.assertEquals("9c9de5e8-0a1e-484a-b099-e80766180a6d",
                linkHandler!!.fromUrl("https://framatube.org/videos/embed/9c9de5e8-0a1e-484a-b099-e80766180a6d").id)
            Assertions.assertEquals("9c9de5e8-0a1e-484a-b099-e80766180a6d",
                linkHandler!!.fromUrl("https://framatube.org/w/9c9de5e8-0a1e-484a-b099-e80766180a6d").id)
        }

    @get:Throws(Exception::class)
    @get:Test
    val url: Unit
        get() {
            Assertions.assertEquals("https://framatube.org/videos/watch/9c9de5e8-0a1e-484a-b099-e80766180a6d",
                linkHandler!!.fromId("9c9de5e8-0a1e-484a-b099-e80766180a6d").url)
            Assertions.assertEquals("https://framatube.org/videos/watch/9c9de5e8-0a1e-484a-b099-e80766180a6d",
                linkHandler!!.fromUrl("https://framatube.org/api/v1/videos/9c9de5e8-0a1e-484a-b099-e80766180a6d").url)
            Assertions.assertEquals("https://framatube.org/videos/watch/9c9de5e8-0a1e-484a-b099-e80766180a6d",
                linkHandler!!.fromUrl("https://framatube.org/videos/embed/9c9de5e8-0a1e-484a-b099-e80766180a6d").url)
            Assertions.assertEquals("https://framatube.org/videos/watch/9c9de5e8-0a1e-484a-b099-e80766180a6d",
                linkHandler!!.fromUrl("https://framatube.org/videos/watch/9c9de5e8-0a1e-484a-b099-e80766180a6d").url)
            Assertions.assertEquals("https://framatube.org/videos/watch/9c9de5e8-0a1e-484a-b099-e80766180a6d",
                linkHandler!!.fromUrl("https://framatube.org/w/9c9de5e8-0a1e-484a-b099-e80766180a6d").url)
        }


    @Test
    @Throws(ParsingException::class)
    fun testAcceptUrl() {
        Assertions.assertTrue(linkHandler!!.acceptUrl("https://peertube.mastodon.host/videos/watch/986aac60-1263-4f73-9ce5-36b18225cb60"))
        Assertions.assertTrue(linkHandler!!.acceptUrl("https://peertube.mastodon.host/videos/watch/986aac60-1263-4f73-9ce5-36b18225cb60?fsdafs=fsafa"))

        Assertions.assertTrue(linkHandler!!.acceptUrl("https://framatube.org/api/v1/videos/9c9de5e8-0a1e-484a-b099-e80766180a6d"))
        Assertions.assertTrue(linkHandler!!.acceptUrl("https://framatube.org/videos/embed/9c9de5e8-0a1e-484a-b099-e80766180a6d"))
        Assertions.assertTrue(linkHandler!!.acceptUrl("https://framatube.org/videos/watch/9c9de5e8-0a1e-484a-b099-e80766180a6d"))
        Assertions.assertTrue(linkHandler!!.acceptUrl("https://framatube.org/w/9c9de5e8-0a1e-484a-b099-e80766180a6d"))

        // make sure playlists aren't accepted
        Assertions.assertFalse(linkHandler!!.acceptUrl("https://framatube.org/w/p/dacdc4ef-5160-4846-9b70-a655880da667"))
        Assertions.assertFalse(linkHandler!!.acceptUrl("https://framatube.org/videos/watch/playlist/dacdc4ef-5160-4846-9b70-a655880da667"))

        assertDoNotAcceptNonURLs(linkHandler!!)
    }

    companion object {
        private var linkHandler: PeertubeStreamLinkHandlerFactory? = null


        @BeforeAll
        fun setUp(): Unit {
            PeerTube.instance = PeertubeInstance("https://framatube.org", "Framatube")
            linkHandler = PeertubeStreamLinkHandlerFactory.instance
            init(getInstance()!!)
        }
    }
}
