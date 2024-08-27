package ac.mdiq.vista.extractor.services.peertube

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import ac.mdiq.vista.downloader.DownloaderTestImpl.Companion.getInstance
import ac.mdiq.vista.extractor.Vista.init
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.services.peertube.linkHandler.PeertubeCommentsLinkHandlerFactory

/**
 * Test for [PeertubeCommentsLinkHandlerFactory]
 */
class PeertubeCommentsLinkHandlerFactoryTest {
    @Test
    @Throws(ParsingException::class)
    fun acceptUrlTest() {
        Assertions.assertTrue(linkHandler!!.acceptUrl("https://framatube.org/videos/watch/kkGMgK9ZtnKfYAgnEtQxbv"))
        Assertions.assertTrue(linkHandler!!.acceptUrl("https://framatube.org/w/kkGMgK9ZtnKfYAgnEtQxbv"))
        Assertions.assertTrue(linkHandler!!.acceptUrl("https://framatube.org/api/v1/videos/kkGMgK9ZtnKfYAgnEtQxbv/comment-threads?start=0&count=10&sort=-createdAt"))
        Assertions.assertTrue(linkHandler!!.acceptUrl("https://framatube.org/videos/watch/9c9de5e8-0a1e-484a-b099-e80766180a6d"))
        Assertions.assertTrue(linkHandler!!.acceptUrl("https://framatube.org/w/9c9de5e8-0a1e-484a-b099-e80766180a6d"))
        Assertions.assertTrue(linkHandler!!.acceptUrl("https://framatube.org/api/v1/videos/9c9de5e8-0a1e-484a-b099-e80766180a6d/comment-threads?start=0&count=10&sort=-createdAt"))

        PeertubeLinkHandlerFactoryTestHelper.assertDoNotAcceptNonURLs(linkHandler!!)
    }

    @get:Throws(ParsingException::class)
    @get:Test
    val idFromUrl: Unit
        get() {
            Assertions.assertEquals("kkGMgK9ZtnKfYAgnEtQxbv",
                linkHandler!!.fromUrl("https://framatube.org/w/kkGMgK9ZtnKfYAgnEtQxbv").id)
            Assertions.assertEquals("kkGMgK9ZtnKfYAgnEtQxbv",
                linkHandler!!.fromUrl("https://framatube.org/videos/watch/kkGMgK9ZtnKfYAgnEtQxbv").id)
            Assertions.assertEquals("kkGMgK9ZtnKfYAgnEtQxbv",
                linkHandler!!.fromUrl("https://framatube.org/api/v1/videos/kkGMgK9ZtnKfYAgnEtQxbv/comment-threads").id)
            Assertions.assertEquals("kkGMgK9ZtnKfYAgnEtQxbv",
                linkHandler!!.fromUrl("https://framatube.org/api/v1/videos/kkGMgK9ZtnKfYAgnEtQxbv/comment-threads?start=0&count=10&sort=-createdAt").id)

            Assertions.assertEquals("9c9de5e8-0a1e-484a-b099-e80766180a6d",
                linkHandler!!.fromUrl("https://framatube.org/w/9c9de5e8-0a1e-484a-b099-e80766180a6d").id)
            Assertions.assertEquals("9c9de5e8-0a1e-484a-b099-e80766180a6d",
                linkHandler!!.fromUrl("https://framatube.org/videos/watch/9c9de5e8-0a1e-484a-b099-e80766180a6d").id)
            Assertions.assertEquals("9c9de5e8-0a1e-484a-b099-e80766180a6d",
                linkHandler!!.fromUrl("https://framatube.org/api/v1/videos/9c9de5e8-0a1e-484a-b099-e80766180a6d/comment-threads").id)
            Assertions.assertEquals("9c9de5e8-0a1e-484a-b099-e80766180a6d",
                linkHandler!!.fromUrl("https://framatube.org/api/v1/videos/9c9de5e8-0a1e-484a-b099-e80766180a6d/comment-threads?start=0&count=10&sort=-createdAt").id)
        }

    companion object {
        private var linkHandler: PeertubeCommentsLinkHandlerFactory? = null


        @BeforeAll
        fun setUp(): Unit {
            linkHandler = PeertubeCommentsLinkHandlerFactory.instance
            init(getInstance()!!)
        }
    }
}
