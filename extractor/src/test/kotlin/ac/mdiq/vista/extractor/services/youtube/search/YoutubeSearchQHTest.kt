package ac.mdiq.vista.extractor.services.youtube.search

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import ac.mdiq.vista.extractor.ServiceList.YouTube
import ac.mdiq.vista.extractor.services.youtube.linkHandler.YoutubeSearchQueryHandlerFactory
import java.util.*

class YoutubeSearchQHTest {
    @Test
    @Throws(Exception::class)
    fun testRegularValues() {
        Assertions.assertEquals("https://www.youtube.com/results?search_query=asdf&sp=8AEB",
            YouTube.getSearchQHFactory().fromQuery("asdf").url)
        Assertions.assertEquals("https://www.youtube.com/results?search_query=hans&sp=8AEB",
            YouTube.getSearchQHFactory().fromQuery("hans").url)
        Assertions.assertEquals("https://www.youtube.com/results?search_query=Poifj%26jaijf&sp=8AEB",
            YouTube.getSearchQHFactory().fromQuery("Poifj&jaijf").url)
        Assertions.assertEquals("https://www.youtube.com/results?search_query=G%C3%BCl%C3%BCm&sp=8AEB",
            YouTube.getSearchQHFactory().fromQuery("Gülüm").url)
        Assertions.assertEquals("https://www.youtube.com/results?search_query=%3Fj%24%29H%C2%A7B&sp=8AEB",
            YouTube.getSearchQHFactory().fromQuery("?j$)H§B").url)

        Assertions.assertEquals("https://music.youtube.com/search?q=asdf",
            YouTube.getSearchQHFactory().fromQuery("asdf", listOf(*arrayOf(
                YoutubeSearchQueryHandlerFactory.MUSIC_SONGS)), "").url)
        Assertions.assertEquals("https://music.youtube.com/search?q=hans",
            YouTube.getSearchQHFactory().fromQuery("hans", listOf(*arrayOf(
                YoutubeSearchQueryHandlerFactory.MUSIC_SONGS)), "").url)
        Assertions.assertEquals("https://music.youtube.com/search?q=Poifj%26jaijf",
            YouTube.getSearchQHFactory().fromQuery("Poifj&jaijf", listOf(*arrayOf(
                YoutubeSearchQueryHandlerFactory.MUSIC_SONGS)), "").url)
        Assertions.assertEquals("https://music.youtube.com/search?q=G%C3%BCl%C3%BCm",
            YouTube.getSearchQHFactory().fromQuery("Gülüm", listOf(*arrayOf(
                YoutubeSearchQueryHandlerFactory.MUSIC_SONGS)), "").url)
        Assertions.assertEquals("https://music.youtube.com/search?q=%3Fj%24%29H%C2%A7B",
            YouTube.getSearchQHFactory().fromQuery("?j$)H§B", listOf(*arrayOf(
                YoutubeSearchQueryHandlerFactory.MUSIC_SONGS)), "").url)
    }

    @Test
    @Throws(Exception::class)
    fun testGetContentFilter() {
        Assertions.assertEquals(YoutubeSearchQueryHandlerFactory.VIDEOS, YouTube.getSearchQHFactory()
            .fromQuery("",
                listOf(*arrayOf(YoutubeSearchQueryHandlerFactory.VIDEOS)),
                "").contentFilters[0])
        Assertions.assertEquals(YoutubeSearchQueryHandlerFactory.CHANNELS, YouTube.getSearchQHFactory()
            .fromQuery("asdf",
                listOf(*arrayOf(YoutubeSearchQueryHandlerFactory.CHANNELS)),
                "").contentFilters[0])

        Assertions.assertEquals(YoutubeSearchQueryHandlerFactory.MUSIC_SONGS, YouTube.getSearchQHFactory()
            .fromQuery("asdf",
                listOf(*arrayOf(YoutubeSearchQueryHandlerFactory.MUSIC_SONGS)),
                "").contentFilters[0])
    }

    @Test
    @Throws(Exception::class)
    fun testWithContentfilter() {
        Assertions.assertEquals("https://www.youtube.com/results?search_query=asdf&sp=EgIQAfABAQ%253D%253D",
            YouTube.getSearchQHFactory()
                .fromQuery("asdf",
                    listOf(*arrayOf(YoutubeSearchQueryHandlerFactory.VIDEOS)),
                    "").url)
        Assertions.assertEquals("https://www.youtube.com/results?search_query=asdf&sp=EgIQAvABAQ%253D%253D",
            YouTube.getSearchQHFactory()
                .fromQuery("asdf",
                    listOf(*arrayOf(YoutubeSearchQueryHandlerFactory.CHANNELS)),
                    "").url)
        Assertions.assertEquals("https://www.youtube.com/results?search_query=asdf&sp=EgIQA_ABAQ%253D%253D",
            YouTube.getSearchQHFactory()
                .fromQuery("asdf",
                    listOf(*arrayOf(YoutubeSearchQueryHandlerFactory.PLAYLISTS)),
                    "").url)
        Assertions.assertEquals("https://www.youtube.com/results?search_query=asdf&sp=8AEB",
            YouTube.getSearchQHFactory()
                .fromQuery("asdf", listOf(*arrayOf("fjiijie")), "").url)

        Assertions.assertEquals("https://music.youtube.com/search?q=asdf", YouTube.getSearchQHFactory()
            .fromQuery("asdf",
                listOf(*arrayOf(YoutubeSearchQueryHandlerFactory.MUSIC_SONGS)),
                "").url)
    }

    @Test
    fun testGetAvailableContentFilter() {
        val contentFilter: Array<String> = YouTube.getSearchQHFactory().availableContentFilter
        Assertions.assertEquals(8, contentFilter.size)
        Assertions.assertEquals("all", contentFilter[0])
        Assertions.assertEquals("videos", contentFilter[1])
        Assertions.assertEquals("channels", contentFilter[2])
        Assertions.assertEquals("playlists", contentFilter[3])
        Assertions.assertEquals("music_songs", contentFilter[4])
        Assertions.assertEquals("music_videos", contentFilter[5])
        Assertions.assertEquals("music_albums", contentFilter[6])
        Assertions.assertEquals("music_playlists", contentFilter[7])
    }

    @Test
    fun testGetAvailableSortFilter() {
        val contentFilter: Array<String> = (YouTube.getSearchQHFactory().availableSortFilter?.filterNotNull()?: listOf()).toTypedArray()
        Assertions.assertEquals(0, contentFilter.size)
    }
}
