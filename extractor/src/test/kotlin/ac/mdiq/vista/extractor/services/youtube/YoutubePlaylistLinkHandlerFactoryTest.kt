package ac.mdiq.vista.extractor.services.youtube

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource
import ac.mdiq.vista.downloader.DownloaderTestImpl.Companion.getInstance
import ac.mdiq.vista.extractor.Vista.init
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.services.youtube.linkHandler.YoutubePlaylistLinkHandlerFactory

/**
 * Test for [YoutubePlaylistLinkHandlerFactory]
 */
class YoutubePlaylistLinkHandlerFactoryTest {
    @Test
    fun idWithNullAsUrl() {
            Assertions.assertThrows(NullPointerException::class.java) { linkHandler!!.fromId("") }
        }

    @ParameterizedTest
    @CsvSource("PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC,https://www.youtube.com/playlist?list=PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC",
        "PLz8YL4HVC87WJQDzVoY943URKQCsHS9XV,https://www.youtube.com/playlist?list=PLz8YL4HVC87WJQDzVoY943URKQCsHS9XV",
        "PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC,https://www.youtube.com/playlist?list=PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC&t=100",
        "PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC,https://WWW.youtube.com/playlist?list=PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC&t=100",
        "PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC,HTTPS://www.youtube.com/playlist?list=PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC&t=100",
        "PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC,https://www.youtube.com/watch?v=0JFM3PRZH-k&index=8&list=PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC",
        "PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC,http://www.youtube.com/watch?v=0JFM3PRZH-k&index=8&list=PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC",
        "PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC,https://m.youtube.com/playlist?list=PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC",
        "PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC,https://youtube.com/playlist?list=PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC",
        "PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC,www.youtube.com/playlist?list=PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC",
        "PLz8YL4HVC87WJQDzVoY943URKQCsHS9XV,www.youtube.com/playlist?list=PLz8YL4HVC87WJQDzVoY943URKQCsHS9XV",
        "OLAK5uy_lEBUW9iTwqf0IlYPxZ8LrzpgqjAHZgZpM,https://music.youtube.com/playlist?list=OLAK5uy_lEBUW9iTwqf0IlYPxZ8LrzpgqjAHZgZpM"
    )
    @Throws(ParsingException::class)
    fun getIdfromYt(expectedId: String?, url: String) {
        Assertions.assertEquals(expectedId, linkHandler!!.fromUrl(url).id)
    }

    @ParameterizedTest
    @ValueSource(strings = ["https://www.youtube.com/playlist?list=PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC", "https://www.youtube.com/playlist?list=PLz8YL4HVC87WJQDzVoY943URKQCsHS9XV", "https://WWW.youtube.com/playlist?list=PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dCI", "HTTPS://www.youtube.com/playlist?list=PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC", "https://www.youtube.com/watch?v=0JFM3PRZH-k&index=8&list=PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC", "http://www.youtube.com/watch?v=0JFM3PRZH-k&index=8&list=PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC", "https://m.youtube.com/playlist?list=PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC", "https://youtube.com/playlist?list=PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC", "www.youtube.com/playlist?list=PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC", "www.youtube.com/playlist?list=PLz8YL4HVC87WJQDzVoY943URKQCsHS9XV", "https://music.youtube.com/playlist?list=OLAK5uy_lEBUW9iTwqf0IlYPxZ8LrzpgqjAHZgZpM", "https://www.youtube.com/playlist?list=RDCLAK5uy_ly6s4irLuZAcjEDwJmqcA_UtSipMyGgbQ" // YouTube Music playlist
        , "https://www.youtube.com/watch?v=2kZVEUGLgy4&list=RDdoEcQv1wlsI&index=2, " // YouTube Mix
    ])
    @Throws(ParsingException::class)
    fun acceptYtUrl(url: String) {
        Assertions.assertTrue(linkHandler!!.acceptUrl(url))
    }

    @ParameterizedTest
    @ValueSource(strings = ["https://www.youtube.com/feed/trending?list=PLz8YL4HVC87WJQDzVoY943URKQCsHS9XV", "https://www.youtube.com/feed/subscriptions?list=PLz8YL4HVC87WJQDzVoY943URKQCsHS9XV", "ftp://www.youtube.com/feed/trending?list=PLz8YL4HVC87WJQDzVoY943URKQCsHS9XV", "www.youtube.com:22/feed/trending?list=PLz8YL4HVC87WJQDzVoY943URKQCsHS9XV", "youtube  .    com/feed/trending?list=PLz8YL4HVC87WJQDzVoY943URKQCsHS9XV", "?list=PLz8YL4HVC87WJQDzVoY943URKQCsHS9XV"
    ])
    @Throws(ParsingException::class)
    fun deniesInvalidYtUrl(url: String) {
        Assertions.assertFalse(linkHandler!!.acceptUrl(url))
    }

    @ParameterizedTest
    @ValueSource(strings = ["https://www.invidio.us/playlist?list=PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC", "https://www.invidio.us/playlist?list=PLz8YL4HVC87WJQDzVoY943URKQCsHS9XV", "https://WWW.invidio.us/playlist?list=PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dCI", "HTTPS://www.invidio.us/playlist?list=PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC", "https://www.invidio.us/watch?v=0JFM3PRZH-k&index=8&list=PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC", "http://www.invidio.us/watch?v=0JFM3PRZH-k&index=8&list=PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC", "https://invidio.us/playlist?list=PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC", "www.invidio.us/playlist?list=PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC", "www.invidio.us/playlist?list=PLz8YL4HVC87WJQDzVoY943URKQCsHS9XV"
    ])
    @Throws(ParsingException::class)
    fun acceptInvidioUrl(url: String) {
        Assertions.assertTrue(linkHandler!!.acceptUrl(url))
    }

    @ParameterizedTest
    @ValueSource(strings = ["https://invidio.us/feed/trending?list=PLz8YL4HVC87WJQDzVoY943URKQCsHS9XV", "https://invidio.us/feed/subscriptions?list=PLz8YL4HVC87WJQDzVoY943URKQCsHS9XV", "ftp:/invidio.us/feed/trending?list=PLz8YL4HVC87WJQDzVoY943URKQCsHS9XV", "invidio.us:22/feed/trending?list=PLz8YL4HVC87WJQDzVoY943URKQCsHS9XV", "invidio  .    us/feed/trending?list=PLz8YL4HVC87WJQDzVoY943URKQCsHS9XV", "?list=PLz8YL4HVC87WJQDzVoY943URKQCsHS9XV"
    ])
    @Throws(ParsingException::class)
    fun deniesInvalidInvidioUrl(url: String) {
        Assertions.assertFalse(linkHandler!!.acceptUrl(url))
    }

    @ParameterizedTest
    @CsvSource("PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC,https://www.invidio.us/playlist?list=PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC",
        "PLz8YL4HVC87WJQDzVoY943URKQCsHS9XV,https://www.invidio.us/playlist?list=PLz8YL4HVC87WJQDzVoY943URKQCsHS9XV",
        "PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC,https://www.invidio.us/playlist?list=PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC&t=100",
        "PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC,https://WWW.invidio.us/playlist?list=PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC&t=100",
        "PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC,HTTPS://www.invidio.us/playlist?list=PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC&t=100",
        "PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC,https://www.invidio.us/watch?v=0JFM3PRZH-k&index=8&list=PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC",
        "PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC,http://www.invidio.us/watch?v=0JFM3PRZH-k&index=8&list=PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC",
        "PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC,https://invidio.us/playlist?list=PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC",
        "PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC,www.invidio.us/playlist?list=PLW5y1tjAOzI3orQNF1yGGVL5x-pR2K1dC",
        "PLz8YL4HVC87WJQDzVoY943URKQCsHS9XV,www.invidio.us/playlist?list=PLz8YL4HVC87WJQDzVoY943URKQCsHS9XV"
    )
    @Throws(ParsingException::class)
    fun getInvidioIdfromUrl(expectedId: String?, url: String) {
        Assertions.assertEquals(expectedId, linkHandler!!.fromUrl(url).id)
    }

    @Test
    @Throws(Exception::class)
    fun fromUrlIsMixVideo() {
        val videoId = "_AzeUSL9lZc"
        var url = "https://www.youtube.com/watch?v=$videoId&list=RD$videoId"
        Assertions.assertEquals(url, linkHandler!!.fromUrl(url).url)

        val mixVideoId = "qHtzO49SDmk"
        url = "https://www.youtube.com/watch?v=$mixVideoId&list=RD$videoId"
        Assertions.assertEquals(url, linkHandler!!.fromUrl(url).url)
    }

    @Test
    @Throws(Exception::class)
    fun fromUrlIsMixPlaylist() {
        val videoId = "_AzeUSL9lZc"
        val url = "https://www.youtube.com/watch?v=$videoId&list=RD$videoId"
        Assertions.assertEquals(url,
            linkHandler!!.fromUrl("https://www.youtube.com/watch?list=RD$videoId").url)
    }

    companion object {
        private var linkHandler: YoutubePlaylistLinkHandlerFactory? = null


        @BeforeAll
        fun setUp() {
            init(getInstance())
            linkHandler = YoutubePlaylistLinkHandlerFactory.instance
        }
    }
}
