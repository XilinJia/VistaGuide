// Created by Fynn Godau 2019, licensed GNU GPL version 3 or later
package ac.mdiq.vista.extractor.services.bandcamp

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import ac.mdiq.vista.downloader.DownloaderTestImpl.Companion.getInstance
import ac.mdiq.vista.extractor.ExtractorAsserts
import ac.mdiq.vista.extractor.Vista.init
import ac.mdiq.vista.extractor.ServiceList.Bandcamp
import ac.mdiq.vista.extractor.exceptions.ContentNotAvailableException
import ac.mdiq.vista.extractor.exceptions.ExtractionException
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.playlist.PlaylistExtractor
import ac.mdiq.vista.extractor.services.BasePlaylistExtractorTest
import ac.mdiq.vista.extractor.stream.Description
import ac.mdiq.vista.extractor.stream.StreamInfoItem
import java.io.IOException

/**
 * Tests for [BandcampPlaylistExtractor]
 */
class BandcampPlaylistExtractorTest {
    /**
     * Test whether playlists contain the correct amount of items
     */
    @Test
    @Throws(ExtractionException::class, IOException::class)
    fun testCount() {
        val extractor: PlaylistExtractor? = Bandcamp.getPlaylistExtractor("https://macbenson.bandcamp.com/album/coming-of-age")
        extractor?.fetchPage()
        assertEquals(5, extractor?.streamCount)
    }

    /**
     * Tests whether different stream thumbnails (track covers) get loaded correctly
     */
    @Test
    @Throws(ExtractionException::class, IOException::class)
    fun testDifferentTrackCovers() {
        val extractor: PlaylistExtractor? = Bandcamp.getPlaylistExtractor("https://zachbensonarchive.bandcamp.com/album/results-of-boredom")
        extractor?.fetchPage()

        val l: List<StreamInfoItem> = extractor?.initialPage?.items ?: listOf()
        ExtractorAsserts.assertContainsOnlyEquivalentImages(extractor?.thumbnails, l[0].thumbnails)
        ExtractorAsserts.assertNotOnlyContainsEquivalentImages(extractor?.thumbnails, l[5].thumbnails)
    }

    /**
     * Tests that no attempt to load every track's cover individually is made
     */
    @Test
    @Timeout(10)
    @Throws(ExtractionException::class, IOException::class)
    fun testDifferentTrackCoversDuration() {
        val extractor: PlaylistExtractor? = Bandcamp.getPlaylistExtractor("https://infiniteammo.bandcamp.com/album/night-in-the-woods-vol-1-at-the-end-of-everything")
        extractor?.fetchPage()

        /* All tracks on this album have the same cover art, but I don't know any albums with more
         * than 10 tracks that has at least one track with a cover art different from the rest.
         */
        val l: List<StreamInfoItem> = extractor?.initialPage?.items ?: listOf()
        ExtractorAsserts.assertContainsOnlyEquivalentImages(extractor?.thumbnails, l[0].thumbnails)
        ExtractorAsserts.assertContainsOnlyEquivalentImages(extractor?.thumbnails, l[5].thumbnails)
    }

    /**
     * Test playlists with locked content
     */
    @Test
    @Throws(ExtractionException::class)
    fun testLockedContent() {
        val extractor: PlaylistExtractor? = Bandcamp.getPlaylistExtractor("https://billwurtz.bandcamp.com/album/high-enough")
        Assertions.assertThrows(ContentNotAvailableException::class.java) { extractor?.fetchPage() }
    }

    /**
     * Test playlist with just one track
     */
    @Test
    @Throws(ExtractionException::class, IOException::class)
    fun testSingleStreamPlaylist() {
        val extractor: PlaylistExtractor? = Bandcamp.getPlaylistExtractor("https://zachjohnson1.bandcamp.com/album/endless")
        extractor?.fetchPage()
        assertEquals(1, extractor?.streamCount)
    }

    class ComingOfAge : BasePlaylistExtractorTest {
        @Test
        @Throws(ParsingException::class)
        override fun testThumbnails() {
            BandcampTestUtils.testImages(extractor!!.thumbnails)
        }

        @Test
        @Throws(ParsingException::class)
        override fun testBanners() {
            ExtractorAsserts.assertEmpty(extractor!!.banners)
        }

        @Test
        @Throws(ParsingException::class)
        fun testUploaderUrl() {
            assertTrue(extractor!!.uploaderUrl!!.contains("macbenson.bandcamp.com"))
        }

        @Test
        @Throws(ParsingException::class)
        override fun testUploaderName() {
            assertEquals("mac benson", extractor!!.uploaderName)
        }

        @Test
        @Throws(ParsingException::class)
        override fun testUploaderAvatars() {
            BandcampTestUtils.testImages(extractor!!.uploaderAvatars)
        }

        @Test
        @Throws(ParsingException::class)
        override fun testStreamCount() {
            assertEquals(5, extractor!!.streamCount)
        }

        @Test
        @Throws(ParsingException::class)
        fun testDescription() {
            val description = extractor!!.description
            Assertions.assertNotEquals(Description.EMPTY_DESCRIPTION, description)
            ExtractorAsserts.assertContains("Artwork by Shona Radcliffe", description!!.content) // about
            ExtractorAsserts.assertContains("All tracks written, produced and recorded by Mac Benson", description.content) // credits
            ExtractorAsserts.assertContains("all rights reserved", description.content) // license
        }

        @Test
        @Throws(Exception::class)
        override fun testUploaderVerified() {
            assertFalse(extractor!!.isUploaderVerified)
        }

        @Test
        @Throws(IOException::class, ExtractionException::class)
        fun testInitialPage() {
            Assertions.assertNotNull(extractor!!.initialPage?.items?.get(0))
        }

        @Test
        override fun testServiceId() {
            Assertions.assertEquals(Bandcamp.serviceId, extractor!!.serviceId)
        }

        @Test
        @Throws(ParsingException::class)
        override fun testName() {
            assertEquals("Coming of Age", extractor!!.getName())
        }

        @Test
        @Throws(Exception::class)
        override fun testId() {
            Assertions.assertEquals("https://macbenson.bandcamp.com/album/coming-of-age", extractor!!.id)
        }

        @Test
        @Throws(Exception::class)
        override fun testUrl() {
            Assertions.assertEquals("https://macbenson.bandcamp.com/album/coming-of-age", extractor!!.url)
        }

        @Test
        @Throws(Exception::class)
        override fun testOriginalUrl() {
            Assertions.assertEquals("https://macbenson.bandcamp.com/album/coming-of-age", extractor!!.originalUrl)
        }

        @Test
        @Throws(IOException::class, ExtractionException::class)
        fun testNextPageUrl() {
            Assertions.assertNull(extractor!!.getPage(extractor!!.initialPage!!.nextPage))
        }

        @Test
        @Throws(Exception::class)
        override fun testRelatedItems() {
            // DefaultTests.defaultTestRelatedItems(extractor);
            // Would fail because BandcampPlaylistStreamInfoItemExtractor.getUploaderName() returns an empty String
        }

        @Test
        @Throws(Exception::class)
        override fun testMoreRelatedItems() {
        }

        companion object {
            private var extractor: PlaylistExtractor? = null


            @BeforeAll
            @Throws(ExtractionException::class, IOException::class)
            fun setUp(): Unit {
                init(getInstance()!!)
                extractor = Bandcamp.getPlaylistExtractor("https://macbenson.bandcamp.com/album/coming-of-age")
                extractor!!.fetchPage()
            }
        }
    }

    companion object {

        @BeforeAll
        fun setUp(): Unit {
            init(getInstance()!!)
        }
    }
}
