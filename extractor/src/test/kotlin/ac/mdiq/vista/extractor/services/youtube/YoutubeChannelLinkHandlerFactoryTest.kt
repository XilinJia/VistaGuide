package ac.mdiq.vista.extractor.services.youtube

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import ac.mdiq.vista.downloader.DownloaderTestImpl.Companion.getInstance
import ac.mdiq.vista.extractor.Vista.init
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.services.youtube.linkHandler.YoutubeChannelLinkHandlerFactory

/**
 * Test for [YoutubeChannelLinkHandlerFactory]
 */
class YoutubeChannelLinkHandlerFactoryTest {
    @Test
    @Throws(ParsingException::class)
    fun acceptUrlTest() {
        Assertions.assertTrue(linkHandler!!.acceptUrl("https://www.youtube.com/user/Gronkh"))
        Assertions.assertTrue(linkHandler!!.acceptUrl("https://www.youtube.com/user/Netzkino/videos"))

        Assertions.assertTrue(linkHandler!!.acceptUrl("https://www.youtube.com/c/creatoracademy"))
        Assertions.assertTrue(linkHandler!!.acceptUrl("https://www.youtube.com/c/%EB%85%B8%EB%A7%88%EB%93%9C%EC%BD%94%EB%8D%94NomadCoders"))

        Assertions.assertTrue(linkHandler!!.acceptUrl("https://youtube.com/DIMENSI0N"))

        Assertions.assertTrue(linkHandler!!.acceptUrl("https://www.youtube.com/channel/UClq42foiSgl7sSpLupnugGA"))
        Assertions.assertTrue(linkHandler!!.acceptUrl("https://www.youtube.com/channel/UClq42foiSgl7sSpLupnugGA/videos?disable_polymer=1"))

        Assertions.assertTrue(linkHandler!!.acceptUrl("https://hooktube.com/user/Gronkh"))
        Assertions.assertTrue(linkHandler!!.acceptUrl("https://hooktube.com/user/Netzkino/videos"))

        Assertions.assertTrue(linkHandler!!.acceptUrl("https://hooktube.com/channel/UClq42foiSgl7sSpLupnugGA"))
        Assertions.assertTrue(linkHandler!!.acceptUrl("https://hooktube.com/channel/UClq42foiSgl7sSpLupnugGA/videos?disable_polymer=1"))

        Assertions.assertTrue(linkHandler!!.acceptUrl("https://invidio.us/user/Gronkh"))
        Assertions.assertTrue(linkHandler!!.acceptUrl("https://invidio.us/user/Netzkino/videos"))

        Assertions.assertTrue(linkHandler!!.acceptUrl("https://invidio.us/channel/UClq42foiSgl7sSpLupnugGA"))
        Assertions.assertTrue(linkHandler!!.acceptUrl("https://invidio.us/channel/UClq42foiSgl7sSpLupnugGA/videos?disable_polymer=1"))
        Assertions.assertTrue(linkHandler!!.acceptUrl("https://www.youtube.com/watchismo"))

        Assertions.assertTrue(linkHandler!!.acceptUrl("https://www.youtube.com/@YouTube"))

        // do not accept URLs which are not channels
        Assertions.assertFalse(linkHandler!!.acceptUrl("https://www.youtube.com/watch?v=jZViOEv90dI&t=100"))
        Assertions.assertFalse(linkHandler!!.acceptUrl("http://www.youtube.com/watch_popup?v=uEJuoEs1UxY"))
        Assertions.assertFalse(linkHandler!!.acceptUrl("http://www.youtube.com/attribution_link?a=JdfC0C9V6ZI&u=%2Fwatch%3Fv%3DEhxJLojIE_o%26feature%3Dshare"))
        Assertions.assertFalse(linkHandler!!.acceptUrl("https://www.youtube.com/playlist?list=PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1d"))
        Assertions.assertFalse(linkHandler!!.acceptUrl("https://www.youtube.com/embed/jZViOEv90dI"))
        Assertions.assertFalse(linkHandler!!.acceptUrl("https://www.youtube.com/feed/subscriptions?list=PLz8YL4HVC87WJQDzVoY943URKQCsHS9XV"))
        Assertions.assertFalse(linkHandler!!.acceptUrl("https://www.youtube.com/?app=desktop&persist_app=1"))
        Assertions.assertFalse(linkHandler!!.acceptUrl("https://m.youtube.com/select_site"))
    }

    @Throws(ParsingException::class)
    @Test
    fun idFromUrl() {
            Assertions.assertEquals("user/Gronkh",
                linkHandler!!.fromUrl("https://www.youtube.com/user/Gronkh").id)
            Assertions.assertEquals("user/Netzkino",
                linkHandler!!.fromUrl("https://www.youtube.com/user/Netzkino/videos").id)

            Assertions.assertEquals("channel/UClq42foiSgl7sSpLupnugGA",
                linkHandler!!.fromUrl("https://www.youtube.com/channel/UClq42foiSgl7sSpLupnugGA").id)
            Assertions.assertEquals("channel/UClq42foiSgl7sSpLupnugGA",
                linkHandler!!.fromUrl("https://www.youtube.com/channel/UClq42foiSgl7sSpLupnugGA/videos?disable_polymer=1").id)

            Assertions.assertEquals("user/Gronkh",
                linkHandler!!.fromUrl("https://hooktube.com/user/Gronkh").id)
            Assertions.assertEquals("user/Netzkino",
                linkHandler!!.fromUrl("https://hooktube.com/user/Netzkino/videos").id)

            Assertions.assertEquals("channel/UClq42foiSgl7sSpLupnugGA",
                linkHandler!!.fromUrl("https://hooktube.com/channel/UClq42foiSgl7sSpLupnugGA").id)
            Assertions.assertEquals("channel/UClq42foiSgl7sSpLupnugGA",
                linkHandler!!.fromUrl("https://hooktube.com/channel/UClq42foiSgl7sSpLupnugGA/videos?disable_polymer=1").id)

            Assertions.assertEquals("user/Gronkh",
                linkHandler!!.fromUrl("https://invidio.us/user/Gronkh").id)
            Assertions.assertEquals("user/Netzkino",
                linkHandler!!.fromUrl("https://invidio.us/user/Netzkino/videos").id)

            Assertions.assertEquals("channel/UClq42foiSgl7sSpLupnugGA",
                linkHandler!!.fromUrl("https://invidio.us/channel/UClq42foiSgl7sSpLupnugGA").id)
            Assertions.assertEquals("channel/UClq42foiSgl7sSpLupnugGA",
                linkHandler!!.fromUrl("https://invidio.us/channel/UClq42foiSgl7sSpLupnugGA/videos?disable_polymer=1").id)

            Assertions.assertEquals("c/creatoracademy",
                linkHandler!!.fromUrl("https://www.youtube.com/c/creatoracademy").id)
            Assertions.assertEquals("c/YouTubeCreators",
                linkHandler!!.fromUrl("https://www.youtube.com/c/YouTubeCreators").id)
            Assertions.assertEquals("c/%EB%85%B8%EB%A7%88%EB%93%9C%EC%BD%94%EB%8D%94NomadCoders",
                linkHandler!!.fromUrl("https://www.youtube.com/c/%EB%85%B8%EB%A7%88%EB%93%9C%EC%BD%94%EB%8D%94NomadCoders").id)

            Assertions.assertEquals("@Gronkh",
                linkHandler!!.fromUrl("https://www.youtube.com/@Gronkh?ucbcb=1").id)
            Assertions.assertEquals("@YouTubeCreators",
                linkHandler!!.fromUrl("https://www.youtube.com/@YouTubeCreators/shorts").id)
        }

    companion object {
        private var linkHandler: YoutubeChannelLinkHandlerFactory? = null


        @BeforeAll
        fun setUp(): Unit {
            linkHandler = YoutubeChannelLinkHandlerFactory.instance
            init(getInstance())
        }
    }
}
