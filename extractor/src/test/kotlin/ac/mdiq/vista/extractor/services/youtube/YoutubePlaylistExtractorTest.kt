package ac.mdiq.vista.extractor.services.youtube

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import ac.mdiq.vista.downloader.DownloaderFactory
import ac.mdiq.vista.downloader.DownloaderFactory.getDownloader
import ac.mdiq.vista.extractor.ExtractorAsserts
import ac.mdiq.vista.extractor.ListExtractor.InfoItemsPage
import ac.mdiq.vista.extractor.Vista.init
import ac.mdiq.vista.extractor.ServiceList.YouTube
import ac.mdiq.vista.extractor.exceptions.ContentNotAvailableException
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.playlist.PlaylistExtractor
import ac.mdiq.vista.extractor.playlist.PlaylistInfo
import ac.mdiq.vista.extractor.services.BasePlaylistExtractorTest
import ac.mdiq.vista.extractor.services.DefaultTests.assertNoMoreItems
import ac.mdiq.vista.extractor.services.DefaultTests.defaultTestGetPageInNewExtractor
import ac.mdiq.vista.extractor.services.DefaultTests.defaultTestListOfItems
import ac.mdiq.vista.extractor.services.DefaultTests.defaultTestMoreItems
import ac.mdiq.vista.extractor.services.DefaultTests.defaultTestRelatedItems
import ac.mdiq.vista.extractor.services.youtube.extractors.YoutubePlaylistExtractor
import ac.mdiq.vista.extractor.stream.StreamInfoItem
import java.io.IOException

/**
 * Test for [YoutubePlaylistExtractor]
 */
object YoutubePlaylistExtractorTest {
    private const val RESOURCE_PATH = DownloaderFactory.RESOURCE_PATH + "services/youtube/extractor/playlist/"

    class NotAvailable {
        @Test
        @Throws(Exception::class)
        fun nonExistentFetch() {
            val extractor: PlaylistExtractor? = YouTube.getPlaylistExtractor("https://www.youtube.com/playlist?list=PL11111111111111111111111111111111")
            Assertions.assertThrows(ContentNotAvailableException::class.java) { extractor?.fetchPage() }
        }

        @Test
        @Throws(Exception::class)
        fun invalidId() {
            val extractor: PlaylistExtractor? = YouTube.getPlaylistExtractor("https://www.youtube.com/playlist?list=INVALID_ID")
            Assertions.assertThrows(ContentNotAvailableException::class.java) { extractor?.fetchPage() }
        }

        companion object {

            @BeforeAll
            @Throws(IOException::class)
            fun setUp() {
                YoutubeTestsUtils.ensureStateless()
                init(getDownloader(RESOURCE_PATH + "notAvailable"))
            }
        }
    }

    class TimelessPopHits : BasePlaylistExtractorTest {
        @Test
        override fun testServiceId() {
            assertEquals(YouTube.serviceId, extractor!!.serviceId)
        }

        @Test
        @Throws(Exception::class)
        override fun testName() {
            Assertions.assertTrue(extractor!!.getName().startsWith("Pop Music Playlist"))
        }

        @Test
        @Throws(Exception::class)
        override fun testId() {
            assertEquals("PLMC9KNkIncKtPzgY-5rmhvj7fax8fdxoj", extractor!!.id)
        }

        @Test
        @Throws(ParsingException::class)
        override fun testUrl() {
            assertEquals("https://www.youtube.com/playlist?list=PLMC9KNkIncKtPzgY-5rmhvj7fax8fdxoj",
                extractor!!.url)
        }

        @Test
        @Throws(ParsingException::class)
        override fun testOriginalUrl() {
            assertEquals("http://www.youtube.com/watch?v=lp-EO5I60KA&list=PLMC9KNkIncKtPzgY-5rmhvj7fax8fdxoj",
                extractor!!.originalUrl)
        }

        @Test
        @Throws(Exception::class)
        override fun testRelatedItems() {
            defaultTestRelatedItems(extractor!!)
        }

        @Test
        @Throws(Exception::class)
        override fun testMoreRelatedItems() {
            defaultTestMoreItems(extractor!!)
        }

        @Test
        @Throws(Exception::class)
        override fun testThumbnails() {
            YoutubeTestsUtils.testImages(extractor!!.thumbnails)
        }

        @Test
        @Throws(ParsingException::class)
        override fun testBanners() {
            YoutubeTestsUtils.testImages(extractor!!.banners)
        }

        @Test
        @Throws(Exception::class)
        fun testUploaderUrl() {
            assertEquals("https://www.youtube.com/channel/UCs72iRpTEuwV3y6pdWYLgiw", extractor!!.uploaderUrl)
        }

        @Test
        @Throws(Exception::class)
        override fun testUploaderName() {
            val uploaderName = extractor!!.uploaderName
            ExtractorAsserts.assertContains("Just Hits", uploaderName!!)
        }

        @Test
        @Throws(Exception::class)
        override fun testUploaderAvatars() {
            YoutubeTestsUtils.testImages(extractor!!.uploaderAvatars)
        }

        @Test
        @Throws(Exception::class)
        override fun testStreamCount() {
            ExtractorAsserts.assertGreater(100, extractor!!.streamCount)
        }

        @Test
        @Throws(Exception::class)
        override fun testUploaderVerified() {
            assertFalse(extractor!!.isUploaderVerified)
        }

        @Throws(ParsingException::class)
        @Test
        fun playlistType() {
                assertEquals(PlaylistInfo.PlaylistType.NORMAL,
                    extractor!!.playlistType)
            }

        @Test
        @Throws(ParsingException::class)
        fun testDescription() {
            val description = extractor!!.description
            ExtractorAsserts.assertContains("pop songs list", description.content)
        }

        companion object {
            private var extractor: YoutubePlaylistExtractor? = null


            @BeforeAll
            @Throws(Exception::class)
            fun setUp() {
                YoutubeTestsUtils.ensureStateless()
                init(getDownloader(RESOURCE_PATH + "TimelessPopHits"))
                extractor = YouTube.getPlaylistExtractor("http://www.youtube.com/watch?v=lp-EO5I60KA&list=PLMC9KNkIncKtPzgY-5rmhvj7fax8fdxoj") as? YoutubePlaylistExtractor
                extractor!!.fetchPage()
            }
        }
    }

    class HugePlaylist : BasePlaylistExtractorTest {
        @Test
        @Throws(Exception::class)
        fun testGetPageInNewExtractor() {
            val newExtractor: PlaylistExtractor? = YouTube.getPlaylistExtractor(extractor!!.url)
            defaultTestGetPageInNewExtractor(extractor!!, newExtractor!!)
        }

        @Test
        override fun testServiceId() {
            assertEquals(YouTube.serviceId, extractor!!.serviceId)
        }

        @Test
        @Throws(Exception::class)
        override fun testName() {
            val name = extractor!!.getName()
            assertEquals("I Wanna Rock Super Gigantic Playlist 1: Hardrock, AOR, Metal and more !!! 5000 music videos !!!",
                name)
        }

        @Test
        @Throws(Exception::class)
        override fun testId() {
            assertEquals("PLWwAypAcFRgKAIIFqBr9oy-ZYZnixa_Fj", extractor!!.id)
        }

        @Test
        @Throws(ParsingException::class)
        override fun testUrl() {
            assertEquals("https://www.youtube.com/playlist?list=PLWwAypAcFRgKAIIFqBr9oy-ZYZnixa_Fj",
                extractor!!.url)
        }

        @Test
        @Throws(ParsingException::class)
        override fun testOriginalUrl() {
            assertEquals("https://www.youtube.com/watch?v=8SbUC-UaAxE&list=PLWwAypAcFRgKAIIFqBr9oy-ZYZnixa_Fj",
                extractor!!.originalUrl)
        }

        @Test
        @Throws(Exception::class)
        override fun testRelatedItems() {
            defaultTestRelatedItems(extractor!!)
        }

        @Test
        @Throws(Exception::class)
        override fun testMoreRelatedItems() {
            var currentPage: InfoItemsPage<StreamInfoItem>? = defaultTestMoreItems(extractor!!)

            // test for 2 more levels
            for (i in 0..1) {
                currentPage = extractor!!.getPage(currentPage!!.nextPage)
                defaultTestListOfItems(YouTube, currentPage.items!!, currentPage.errors!!)
            }
        }

        @Test
        @Throws(Exception::class)
        override fun testThumbnails() {
            YoutubeTestsUtils.testImages(extractor!!.thumbnails)
        }

        @Test
        @Throws(ParsingException::class)
        override fun testBanners() {
            YoutubeTestsUtils.testImages(extractor!!.banners)
        }

        @Test
        @Throws(Exception::class)
        fun testUploaderUrl() {
            assertEquals("https://www.youtube.com/channel/UCHSPWoY1J5fbDVbcnyeqwdw", extractor!!.uploaderUrl)
        }

        @Test
        @Throws(Exception::class)
        override fun testUploaderName() {
            assertEquals("Tomas Nilsson TOMPA571", extractor!!.uploaderName)
        }

        @Test
        @Throws(Exception::class)
        override fun testUploaderAvatars() {
            YoutubeTestsUtils.testImages(extractor!!.uploaderAvatars)
        }

        @Test
        @Throws(Exception::class)
        override fun testStreamCount() {
            ExtractorAsserts.assertGreater(100, extractor!!.streamCount)
        }

        @Test
        @Throws(Exception::class)
        override fun testUploaderVerified() {
            assertFalse(extractor!!.isUploaderVerified)
        }

        @Throws(ParsingException::class)
        @Test
        fun playlistType() {
                assertEquals(PlaylistInfo.PlaylistType.NORMAL, extractor!!.playlistType)
            }

        @Test
        @Throws(ParsingException::class)
        fun testDescription() {
            val description = extractor!!.description
            ExtractorAsserts.assertContains("I Wanna Rock Super Gigantic Playlist", description.content)
        }

        companion object {
            private var extractor: YoutubePlaylistExtractor? = null


            @BeforeAll
            @Throws(Exception::class)
            fun setUp() {
                YoutubeTestsUtils.ensureStateless()
                init(getDownloader(RESOURCE_PATH + "huge"))
                extractor = YouTube.getPlaylistExtractor("https://www.youtube.com/watch?v=8SbUC-UaAxE&list=PLWwAypAcFRgKAIIFqBr9oy-ZYZnixa_Fj") as? YoutubePlaylistExtractor
                extractor!!.fetchPage()
            }
        }
    }

    class LearningPlaylist : BasePlaylistExtractorTest {
        @Test
        override fun testServiceId() {
            assertEquals(YouTube.serviceId, extractor!!.serviceId)
        }

        @Test
        @Throws(Exception::class)
        override fun testName() {
            Assertions.assertTrue(extractor!!.getName().startsWith("Anatomy & Physiology"))
        }

        @Test
        @Throws(Exception::class)
        override fun testId() {
            assertEquals("PL8dPuuaLjXtOAKed_MxxWBNaPno5h3Zs8", extractor!!.id)
        }

        @Test
        @Throws(ParsingException::class)
        override fun testUrl() {
            assertEquals("https://www.youtube.com/playlist?list=PL8dPuuaLjXtOAKed_MxxWBNaPno5h3Zs8",
                extractor!!.url)
        }

        @Test
        @Throws(ParsingException::class)
        override fun testOriginalUrl() {
            assertEquals("https://www.youtube.com/playlist?list=PL8dPuuaLjXtOAKed_MxxWBNaPno5h3Zs8",
                extractor!!.originalUrl)
        }

        @Test
        @Throws(Exception::class)
        override fun testRelatedItems() {
            defaultTestRelatedItems(extractor!!)
        }

        @Test
        @Throws(Exception::class)
        override fun testMoreRelatedItems() {
            assertFalse(extractor!!.initialPage.hasNextPage())
        }

        @Test
        @Throws(Exception::class)
        override fun testThumbnails() {
            YoutubeTestsUtils.testImages(extractor!!.thumbnails)
        }

        @Test
        @Throws(ParsingException::class)
        override fun testBanners() {
            YoutubeTestsUtils.testImages(extractor!!.banners)
        }

        @Test
        @Throws(Exception::class)
        fun testUploaderUrl() {
            assertEquals("https://www.youtube.com/channel/UCX6b17PVsYBQ0ip5gyeme-Q", extractor!!.uploaderUrl)
        }

        @Test
        @Throws(Exception::class)
        override fun testUploaderName() {
            val uploaderName = extractor!!.uploaderName
            ExtractorAsserts.assertContains("CrashCourse", uploaderName!!)
        }

        @Test
        @Throws(Exception::class)
        override fun testUploaderAvatars() {
            YoutubeTestsUtils.testImages(extractor!!.uploaderAvatars)
        }

        @Test
        @Throws(Exception::class)
        override fun testStreamCount() {
            ExtractorAsserts.assertGreater(40, extractor!!.streamCount)
        }

        @Test
        @Throws(Exception::class)
        override fun testUploaderVerified() {
            assertFalse(extractor!!.isUploaderVerified)
        }

        @Throws(ParsingException::class)
        @Test
        fun playlistType() {
                assertEquals(PlaylistInfo.PlaylistType.NORMAL,
                    extractor!!.playlistType)
            }

        @Test
        @Throws(ParsingException::class)
        fun testDescription() {
            val description = extractor!!.description
            ExtractorAsserts.assertContains("47 episodes", description.content)
        }

        companion object {
            private var extractor: YoutubePlaylistExtractor? = null


            @BeforeAll
            @Throws(Exception::class)
            fun setUp() {
                YoutubeTestsUtils.ensureStateless()
                init(getDownloader(RESOURCE_PATH + "learning"))
                extractor = YouTube.getPlaylistExtractor("https://www.youtube.com/playlist?list=PL8dPuuaLjXtOAKed_MxxWBNaPno5h3Zs8") as? YoutubePlaylistExtractor
                extractor!!.fetchPage()
            }
        }
    }

    internal class ShortsUI : BasePlaylistExtractorTest {
        @Test
        @Throws(Exception::class)
        override fun testServiceId() {
            assertEquals(YouTube.serviceId, extractor!!.serviceId)
        }

        @Test
        @Throws(Exception::class)
        override fun testName() {
            assertEquals("Short videos", extractor?.getName())
        }

        @Test
        @Throws(Exception::class)
        override fun testId() {
            assertEquals("UUSHBR8-60-B28hp2BmDPdntcQ", extractor!!.id)
        }

        @Test
        @Throws(Exception::class)
        override fun testUrl() {
            assertEquals("https://www.youtube.com/playlist?list=UUSHBR8-60-B28hp2BmDPdntcQ", extractor!!.url)
        }

        @Test
        @Throws(Exception::class)
        override fun testOriginalUrl() {
            assertEquals("https://www.youtube.com/playlist?list=UUSHBR8-60-B28hp2BmDPdntcQ",
                extractor!!.originalUrl)
        }

        @Test
        @Throws(Exception::class)
        override fun testRelatedItems() {
            defaultTestRelatedItems(extractor!!)
        }

        // TODO: enable test when continuations are available
        @Disabled(("Shorts UI doesn't return any continuation, even if when there are more than 100 "
                + "items: this is a bug on YouTube's side, which is not related to the requirement "
                + "of a valid visitorData like it is for Shorts channel tab"))
        @Test
        @Throws(Exception::class)
        override fun testMoreRelatedItems() {
            defaultTestMoreItems(extractor!!)
        }

        @Test
        @Throws(Exception::class)
        override fun testThumbnails() {
            YoutubeTestsUtils.testImages(extractor!!.thumbnails)
        }

        @Test
        @Throws(Exception::class)
        override fun testBanners() {
            YoutubeTestsUtils.testImages(extractor!!.banners)
        }

        @Test
        @Throws(Exception::class)
        override fun testUploaderName() {
            assertEquals("YouTube", extractor!!.uploaderName)
        }

        @Test
        @Throws(Exception::class)
        override fun testUploaderAvatars() {
            YoutubeTestsUtils.testImages(extractor!!.uploaderAvatars)
        }

        @Test
        @Throws(Exception::class)
        override fun testStreamCount() {
            ExtractorAsserts.assertGreater(250, extractor!!.streamCount)
        }

        @Test
        @Throws(Exception::class)
        override fun testUploaderVerified() {
            // YouTube doesn't provide this information for playlists
            assertFalse(extractor!!.isUploaderVerified)
        }

        @Throws(ParsingException::class)
        @Test
        fun playlistType() {
                assertEquals(PlaylistInfo.PlaylistType.NORMAL, extractor!!.playlistType)
            }

        @Test
        @Throws(ParsingException::class)
        fun testDescription() {
            Assertions.assertTrue(extractor!!.description.content.isEmpty())
        }

        companion object {
            private var extractor: PlaylistExtractor? = null


            @BeforeAll
            @Throws(Exception::class)
            fun setUp() {
                YoutubeTestsUtils.ensureStateless()
                init(getDownloader(RESOURCE_PATH + "shortsUI"))
                extractor = YouTube.getPlaylistExtractor("https://www.youtube.com/playlist?list=UUSHBR8-60-B28hp2BmDPdntcQ")
                extractor!!.fetchPage()
            }
        }
    }

    class ContinuationsTests {
        @Test
        @Throws(Exception::class)
        fun testNoContinuations() {
            val extractor = YouTube.getPlaylistExtractor("https://www.youtube.com/playlist?list=PLXJg25X-OulsVsnvZ7RVtSDW-id9_RzAO") as YoutubePlaylistExtractor
            extractor.fetchPage()

            assertNoMoreItems(extractor)
        }

        @Test
        @Throws(Exception::class)
        fun testOnlySingleContinuation() {
            val extractor = YouTube
                .getPlaylistExtractor(
                    "https://www.youtube.com/playlist?list=PLoumn5BIsUDeGF1vy5Nylf_RJKn5aL_nr") as YoutubePlaylistExtractor
            extractor.fetchPage()

            val page: InfoItemsPage<StreamInfoItem> = defaultTestMoreItems(
                extractor)
            assertFalse(page.hasNextPage(), "More items available when it shouldn't")
        }

        companion object {

            @BeforeAll
            @Throws(IOException::class)
            fun setUp() {
                YoutubeTestsUtils.ensureStateless()
                init(getDownloader(RESOURCE_PATH + "continuations"))
            }
        }
    }
}
