package ac.mdiq.vista.extractor.services.peertube

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import ac.mdiq.vista.downloader.DownloaderTestImpl.Companion.getInstance
import ac.mdiq.vista.extractor.Vista.init
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.services.peertube.PeertubeLinkHandlerFactoryTestHelper.assertDoNotAcceptNonURLs
import ac.mdiq.vista.extractor.services.peertube.linkHandler.PeertubePlaylistLinkHandlerFactory

/**
 * Test for [PeertubePlaylistLinkHandlerFactory]
 */
class PeertubePlaylistLinkHandlerFactoryTest {
    @Test
    @Throws(ParsingException::class)
    fun acceptUrlTest() {
        Assertions.assertTrue(linkHandler!!.acceptUrl("https://framatube.org/videos/watch/playlist/d8ca79f9-e4c7-4269-8183-d78ed269c909"))
        Assertions.assertTrue(linkHandler!!.acceptUrl("https://framatube.org/w/p/d8ca79f9-e4c7-4269-8183-d78ed269c909"))
        Assertions.assertTrue(linkHandler!!.acceptUrl("https://framatube.org/videos/watch/playlist/d8ca79f9-e4c7-4269-8183-d78ed269c909/videos"))
        Assertions.assertTrue(linkHandler!!.acceptUrl("https://framatube.org/videos/watch/playlist/dacdc4ef-5160-4846-9b70-a655880da667"))
        Assertions.assertTrue(linkHandler!!.acceptUrl("https://framatube.org/w/p/dacdc4ef-5160-4846-9b70-a655880da667"))
        Assertions.assertTrue(linkHandler!!.acceptUrl("https://framatube.org/videos/watch/playlist/96b0ee2b-a5a7-4794-8769-58d8ccb79ab7"))
        Assertions.assertTrue(linkHandler!!.acceptUrl("https://framatube.org/w/p/96b0ee2b-a5a7-4794-8769-58d8ccb79ab7"))

        assertDoNotAcceptNonURLs(linkHandler!!)
    }

    @get:Throws(ParsingException::class)
    @get:Test
    val idFromUrl: Unit
        get() {
            Assertions.assertEquals("d8ca79f9-e4c7-4269-8183-d78ed269c909",
                linkHandler!!.getId("https://framatube.org/videos/watch/playlist/d8ca79f9-e4c7-4269-8183-d78ed269c909"))
            Assertions.assertEquals("d8ca79f9-e4c7-4269-8183-d78ed269c909",
                linkHandler!!.getId("https://framatube.org/w/p/d8ca79f9-e4c7-4269-8183-d78ed269c909"))
            Assertions.assertEquals("dacdc4ef-5160-4846-9b70-a655880da667",
                linkHandler!!.getId("https://framatube.org/videos/watch/playlist/dacdc4ef-5160-4846-9b70-a655880da667"))
            Assertions.assertEquals("dacdc4ef-5160-4846-9b70-a655880da667",
                linkHandler!!.getId("https://framatube.org/w/p/dacdc4ef-5160-4846-9b70-a655880da667"))
            Assertions.assertEquals("bfc145f5-1be7-48a6-9b9e-4f1967199dad",
                linkHandler!!.getId("https://framatube.org/videos/watch/playlist/bfc145f5-1be7-48a6-9b9e-4f1967199dad"))
            Assertions.assertEquals("bfc145f5-1be7-48a6-9b9e-4f1967199dad",
                linkHandler!!.getId("https://framatube.org/w/p/bfc145f5-1be7-48a6-9b9e-4f1967199dad"))
            Assertions.assertEquals("96b0ee2b-a5a7-4794-8769-58d8ccb79ab7",
                linkHandler!!.getId("https://framatube.org/videos/watch/playlist/96b0ee2b-a5a7-4794-8769-58d8ccb79ab7"))
            Assertions.assertEquals("96b0ee2b-a5a7-4794-8769-58d8ccb79ab7",
                linkHandler!!.getId("https://framatube.org/w/p/96b0ee2b-a5a7-4794-8769-58d8ccb79ab7"))
        }

    @get:Test
    val url: Unit
        get() {
            Assertions.assertDoesNotThrow<String> {
                linkHandler!!.fromUrl("https://framatube.org/videos/watch/playlist/d8ca79f9-e4c7-4269-8183-d78ed269c909").url
            }
            Assertions.assertDoesNotThrow<String> {
                linkHandler!!.fromUrl("https://framatube.org/w/p/d8ca79f9-e4c7-4269-8183-d78ed269c909").url
            }
            Assertions.assertDoesNotThrow<String> {
                linkHandler!!.fromUrl("https://framatube.org/w/p/sLFbqXsw7sPR3AfvqQSBZB").url
            }
        }

    companion object {
        private var linkHandler: PeertubePlaylistLinkHandlerFactory? = null

        @BeforeAll
        fun setUp() {
            linkHandler = PeertubePlaylistLinkHandlerFactory.instance
            init(getInstance()!!)
        }
    }
}
