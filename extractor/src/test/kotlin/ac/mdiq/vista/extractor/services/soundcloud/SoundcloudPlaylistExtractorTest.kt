package ac.mdiq.vista.extractor.services.soundcloud

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import ac.mdiq.vista.downloader.DownloaderTestImpl.Companion.getInstance
import ac.mdiq.vista.extractor.ExtractorAsserts
import ac.mdiq.vista.extractor.InfoItem
import ac.mdiq.vista.extractor.ListExtractor.InfoItemsPage
import ac.mdiq.vista.extractor.Vista.init
import ac.mdiq.vista.extractor.ServiceList.SoundCloud
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.playlist.PlaylistExtractor
import ac.mdiq.vista.extractor.services.BasePlaylistExtractorTest
import ac.mdiq.vista.extractor.services.DefaultTests.defaultTestGetPageInNewExtractor
import ac.mdiq.vista.extractor.services.DefaultTests.defaultTestImageCollection
import ac.mdiq.vista.extractor.services.DefaultTests.defaultTestListOfItems
import ac.mdiq.vista.extractor.services.DefaultTests.defaultTestMoreItems
import ac.mdiq.vista.extractor.services.DefaultTests.defaultTestRelatedItems
import ac.mdiq.vista.extractor.services.soundcloud.extractors.SoundcloudPlaylistExtractor
import ac.mdiq.vista.extractor.stream.StreamInfoItem

/**
 * Test for [PlaylistExtractor]
 */
class SoundcloudPlaylistExtractorTest {
    class LuvTape : BasePlaylistExtractorTest {
        @Test
        override fun testServiceId() {
            Assertions.assertEquals(SoundCloud.serviceId, extractor!!.serviceId)
        }

        @Test
        override fun testName() {
            Assertions.assertEquals("THE PERFECT LUV TAPE®️", extractor!!.getName())
        }

        @Test
        override fun testId() {
            Assertions.assertEquals("246349810", extractor!!.id)
        }

        @Test
        @Throws(Exception::class)
        override fun testUrl() {
            Assertions.assertEquals("https://soundcloud.com/liluzivert/sets/the-perfect-luv-tape-r", extractor!!.url)
        }

        @Test
        @Throws(Exception::class)
        override fun testOriginalUrl() {
            Assertions.assertEquals("https://soundcloud.com/liluzivert/sets/the-perfect-luv-tape-r?test=123",
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
        override fun testThumbnails() {
            defaultTestImageCollection(extractor!!.thumbnails)
        }

        @Test
        @Throws(ParsingException::class)
        override fun testBanners() {
            // SoundCloud playlists do not have a banner
            ExtractorAsserts.assertEmpty(extractor!!.banners)
        }

        @Test
        fun testUploaderUrl() {
            val uploaderUrl = extractor!!.uploaderUrl
            ExtractorAsserts.assertIsSecureUrl(uploaderUrl)
            ExtractorAsserts.assertContains("liluzivert", uploaderUrl)
        }

        @Test
        override fun testUploaderName() {
            Assertions.assertTrue(extractor!!.uploaderName.contains("Lil Uzi Vert"))
        }

        @Test
        override fun testUploaderAvatars() {
            defaultTestImageCollection(extractor!!.uploaderAvatars)
        }

        @Test
        override fun testStreamCount() {
            ExtractorAsserts.assertGreaterOrEqual(10, extractor!!.streamCount)
        }

        @Test
        @Throws(Exception::class)
        override fun testUploaderVerified() {
            Assertions.assertTrue(extractor!!.isUploaderVerified)
        }

        companion object {
            private var extractor: SoundcloudPlaylistExtractor? = null


            @BeforeAll
            @Throws(Exception::class)
            fun setUp(): Unit {
                init(getInstance()!!)
                extractor = SoundCloud.getPlaylistExtractor("https://soundcloud.com/liluzivert/sets/the-perfect-luv-tape-r?test=123") as? SoundcloudPlaylistExtractor
                extractor!!.fetchPage()
            }
        }
    }

    class RandomHouseMusic : BasePlaylistExtractorTest {
        @Test
        override fun testServiceId() {
            Assertions.assertEquals(SoundCloud.serviceId, extractor!!.serviceId)
        }

        @Test
        override fun testName() {
            Assertions.assertEquals("House", extractor!!.getName())
        }

        @Test
        override fun testId() {
            Assertions.assertEquals("123062856", extractor!!.id)
        }

        @Test
        @Throws(Exception::class)
        override fun testUrl() {
            Assertions.assertEquals("https://soundcloud.com/micky96/sets/house", extractor!!.url)
        }

        @Test
        @Throws(Exception::class)
        override fun testOriginalUrl() {
            Assertions.assertEquals("https://soundcloud.com/micky96/sets/house", extractor!!.originalUrl)
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
        override fun testThumbnails() {
            defaultTestImageCollection(extractor!!.thumbnails)
        }

        @Test
        @Throws(ParsingException::class)
        override fun testBanners() {
            // SoundCloud playlists do not have a banner
            ExtractorAsserts.assertEmpty(extractor!!.banners)
        }

        @Test
        fun testUploaderUrl() {
            val uploaderUrl = extractor!!.uploaderUrl
            ExtractorAsserts.assertIsSecureUrl(uploaderUrl)
            ExtractorAsserts.assertContains("micky96", uploaderUrl)
        }

        @Test
        override fun testUploaderName() {
            Assertions.assertEquals("_mickyyy", extractor!!.uploaderName)
        }

        @Test
        override fun testUploaderAvatars() {
            defaultTestImageCollection(extractor!!.uploaderAvatars)
        }

        @Test
        override fun testStreamCount() {
            ExtractorAsserts.assertGreaterOrEqual(10, extractor!!.streamCount)
        }

        @Test
        @Throws(Exception::class)
        override fun testUploaderVerified() {
            Assertions.assertFalse(extractor!!.isUploaderVerified)
        }

        companion object {
            private var extractor: SoundcloudPlaylistExtractor? = null


            @BeforeAll
            @Throws(Exception::class)
            fun setUp(): Unit {
                init(getInstance()!!)
                extractor = SoundCloud.getPlaylistExtractor("https://soundcloud.com/micky96/sets/house") as? SoundcloudPlaylistExtractor
                extractor!!.fetchPage()
            }
        }
    }

    class EDMxxx : BasePlaylistExtractorTest {
        @Test
        @Throws(Exception::class)
        fun testGetPageInNewExtractor() {
            val newExtractor: PlaylistExtractor = SoundCloud?.getPlaylistExtractor(extractor!!.url)!!
            defaultTestGetPageInNewExtractor(extractor!!, newExtractor)
        }

        @Test
        override fun testServiceId() {
            Assertions.assertEquals(SoundCloud.serviceId, extractor!!.serviceId)
        }

        @Test
        override fun testName() {
            Assertions.assertEquals("EDM xXx", extractor!!.getName())
        }

        @Test
        override fun testId() {
            Assertions.assertEquals("136000376", extractor!!.id)
        }

        @Test
        @Throws(Exception::class)
        override fun testUrl() {
            Assertions.assertEquals("https://soundcloud.com/user350509423/sets/edm-xxx", extractor!!.url)
        }

        @Test
        @Throws(Exception::class)
        override fun testOriginalUrl() {
            Assertions.assertEquals("https://soundcloud.com/user350509423/sets/edm-xxx", extractor!!.originalUrl)
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
            // Test for 2 more levels
            for (i in 0..1) {
                currentPage = extractor!!.getPage(currentPage!!.nextPage)
                defaultTestListOfItems(SoundCloud, currentPage!!.items!!, currentPage.errors!!)
            }
        }

        @Test
        override fun testThumbnails() {
            defaultTestImageCollection(extractor!!.thumbnails)
        }

        @Test
        @Throws(ParsingException::class)
        override fun testBanners() {
            // SoundCloud playlists do not have a banner
            ExtractorAsserts.assertEmpty(extractor!!.banners)
        }

        @Test
        fun testUploaderUrl() {
            val uploaderUrl = extractor!!.uploaderUrl
            ExtractorAsserts.assertIsSecureUrl(uploaderUrl)
            ExtractorAsserts.assertContains("user350509423", uploaderUrl)
        }

        @Test
        override fun testUploaderName() {
            assertEquals("Chaazyy", extractor!!.uploaderName);
        }

        @Test
        override fun testUploaderAvatars() {
            defaultTestImageCollection(extractor!!.uploaderAvatars)
        }

        @Test
        override fun testStreamCount() {
            ExtractorAsserts.assertGreaterOrEqual(370, extractor!!.streamCount)
        }

        @Test
        @Throws(Exception::class)
        override fun testUploaderVerified() {
            Assertions.assertFalse(extractor!!.isUploaderVerified)
        }

        companion object {
            private var extractor: SoundcloudPlaylistExtractor? = null


            @BeforeAll
            @Throws(Exception::class)
            fun setUp(): Unit {
                init(getInstance()!!)
                extractor = SoundCloud.getPlaylistExtractor("https://soundcloud.com/user350509423/sets/edm-xxx") as? SoundcloudPlaylistExtractor
                extractor!!.fetchPage()
            }
        }
    }

    class SmallPlaylist : BasePlaylistExtractorTest {
        @Test
        override fun testServiceId() {
            Assertions.assertEquals(SoundCloud.serviceId, extractor!!.serviceId)
        }

        @Test
        override fun testName() {
            Assertions.assertEquals("EMPTY PLAYLIST", extractor!!.getName())
        }

        @Test
        override fun testId() {
            Assertions.assertEquals("23483459", extractor!!.id)
        }

        @Test
        @Throws(Exception::class)
        override fun testUrl() {
            Assertions.assertEquals("https://soundcloud.com/breezy-123/sets/empty-playlist", extractor!!.url)
        }

        @Test
        @Throws(Exception::class)
        override fun testOriginalUrl() {
            Assertions.assertEquals("https://soundcloud.com/breezy-123/sets/empty-playlist?test=123",
                extractor!!.originalUrl)
        }

        @Test
        @Throws(Exception::class)
        override fun testRelatedItems() {
            defaultTestRelatedItems(extractor!!)
        }

        @Test
        @Disabled("Test broken? Playlist has 2 entries, each page has 1 entry meaning it has 2 pages.")
        @Throws(
            Exception::class)
        override fun testMoreRelatedItems() {
            try {
                defaultTestMoreItems(extractor!!)
            } catch (ignored: Throwable) {
                return
            }

            Assertions.fail<Any>("This playlist doesn't have more items, it should throw an error")
        }

        @Test
        override fun testThumbnails() {
            defaultTestImageCollection(extractor!!.thumbnails)
        }

        @Test
        @Throws(ParsingException::class)
        override fun testBanners() {
            // SoundCloud playlists do not have a banner
            ExtractorAsserts.assertEmpty(extractor!!.banners)
        }

        @Test
        fun testUploaderUrl() {
            val uploaderUrl = extractor!!.uploaderUrl
            ExtractorAsserts.assertIsSecureUrl(uploaderUrl)
            ExtractorAsserts.assertContains("breezy-123", uploaderUrl)
        }

        @Test
        override fun testUploaderName() {
            Assertions.assertEquals("breezy-123", extractor!!.uploaderName)
        }

        @Test
        override fun testUploaderAvatars() {
            defaultTestImageCollection(extractor!!.uploaderAvatars)
        }

        @Test
        override fun testStreamCount() {
            Assertions.assertEquals(2, extractor!!.streamCount)
        }

        @Test
        @Throws(Exception::class)
        override fun testUploaderVerified() {
            Assertions.assertFalse(extractor!!.isUploaderVerified)
        }

        companion object {
            private var extractor: SoundcloudPlaylistExtractor? = null


            @BeforeAll
            @Throws(Exception::class)
            fun setUp(): Unit {
                init(getInstance()!!)
                extractor = SoundCloud.getPlaylistExtractor("https://soundcloud.com/breezy-123/sets/empty-playlist?test=123") as? SoundcloudPlaylistExtractor
                extractor!!.fetchPage()
            }
        }
    }
}
