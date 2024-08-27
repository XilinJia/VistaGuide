package ac.mdiq.vista.extractor.services.peertube.search

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import ac.mdiq.vista.extractor.ServiceList.PeerTube
import ac.mdiq.vista.extractor.services.peertube.PeertubeInstance
import ac.mdiq.vista.extractor.services.peertube.linkHandler.PeertubeSearchQueryHandlerFactory

class PeertubeSearchQHTest {
    @Test
    @Throws(Exception::class)
    fun testVideoSearch() {
        Assertions.assertEquals("https://peertube.mastodon.host/api/v1/search/videos?search=asdf",
            PeerTube.getSearchQHFactory().fromQuery("asdf").url)
        Assertions.assertEquals("https://peertube.mastodon.host/api/v1/search/videos?search=hans",
            PeerTube.getSearchQHFactory().fromQuery("hans").url)
        Assertions.assertEquals("https://peertube.mastodon.host/api/v1/search/videos?search=Poifj%26jaijf",
            PeerTube.getSearchQHFactory().fromQuery("Poifj&jaijf").url)
        Assertions.assertEquals("https://peertube.mastodon.host/api/v1/search/videos?search=G%C3%BCl%C3%BCm",
            PeerTube.getSearchQHFactory().fromQuery("Gülüm").url)
        Assertions.assertEquals("https://peertube.mastodon.host/api/v1/search/videos?search=%3Fj%24%29H%C2%A7B",
            PeerTube.getSearchQHFactory().fromQuery("?j$)H§B").url)
    }

    @Test
    @Throws(Exception::class)
    fun testSepiaVideoSearch() {
        Assertions.assertEquals("https://sepiasearch.org/api/v1/search/videos?search=%3Fj%24%29H%C2%A7B",
            PeerTube.getSearchQHFactory()
                .fromQuery("?j$)H§B", listOf(PeertubeSearchQueryHandlerFactory.SEPIA_VIDEOS), "").url)
        Assertions.assertEquals("https://anotherpeertubeindex.com/api/v1/search/videos?search=%3Fj%24%29H%C2%A7B",
            PeerTube.getSearchQHFactory().fromQuery("?j$)H§B",
                listOf(PeertubeSearchQueryHandlerFactory.SEPIA_VIDEOS),
                "",
                "https://anotherpeertubeindex.com").url)
    }

    @Test
    @Throws(Exception::class)
    fun testPlaylistSearch() {
        Assertions.assertEquals("https://peertube.mastodon.host/api/v1/search/video-playlists?search=asdf",
            PeerTube.getSearchQHFactory()
                .fromQuery("asdf", listOf(PeertubeSearchQueryHandlerFactory.PLAYLISTS), "").url)
        Assertions.assertEquals("https://peertube.mastodon.host/api/v1/search/video-playlists?search=hans",
            PeerTube.getSearchQHFactory()
                .fromQuery("hans", listOf(PeertubeSearchQueryHandlerFactory.PLAYLISTS), "").url)
    }

    @Test
    @Throws(Exception::class)
    fun testChannelSearch() {
        Assertions.assertEquals("https://peertube.mastodon.host/api/v1/search/video-channels?search=asdf",
            PeerTube.getSearchQHFactory()
                .fromQuery("asdf", listOf(PeertubeSearchQueryHandlerFactory.CHANNELS), "").url)
        Assertions.assertEquals("https://peertube.mastodon.host/api/v1/search/video-channels?search=hans",
            PeerTube.getSearchQHFactory()
                .fromQuery("hans", listOf(PeertubeSearchQueryHandlerFactory.CHANNELS), "").url)
    }

    companion object {

        @BeforeAll
        @Throws(Exception::class)
        fun setUpClass(): Unit {
            // setting instance might break test when running in parallel
            PeerTube.instance = PeertubeInstance("https://peertube.mastodon.host", "PeerTube on Mastodon.host")
        }
    }
}
