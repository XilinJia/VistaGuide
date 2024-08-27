// Created by Fynn Godau 2019, licensed GNU GPL version 3 or later
package ac.mdiq.vista.extractor.services.bandcamp

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import ac.mdiq.vista.downloader.DownloaderTestImpl.Companion.getInstance
import ac.mdiq.vista.extractor.InfoItem
import ac.mdiq.vista.extractor.Vista.init
import ac.mdiq.vista.extractor.ServiceList.Bandcamp
import ac.mdiq.vista.extractor.StreamingService
import ac.mdiq.vista.extractor.exceptions.ExtractionException
import ac.mdiq.vista.extractor.playlist.PlaylistInfoItem
import ac.mdiq.vista.extractor.search.SearchExtractor
import ac.mdiq.vista.extractor.services.DefaultSearchExtractorTest
import ac.mdiq.vista.extractor.stream.StreamInfoItem
import java.io.IOException

/**
 * Test for [BandcampSearchExtractor]
 */
class BandcampSearchExtractorTest {
    /**
     * Tests whether searching bandcamp for "best friend's basement" returns
     * the accordingly named song by Zach Benson
     */
    @Test
    @Throws(ExtractionException::class, IOException::class)
    fun testStreamSearch() {
        val extractor: SearchExtractor = Bandcamp.getSearchExtractor("best friend's basement")

        val page = extractor.initialPage
        val bestFriendsBasement = page!!.items!![0] as StreamInfoItem

        // The track by Zach Benson should be the first result, no?
        Assertions.assertEquals("Best Friend's Basement", bestFriendsBasement.name)
        Assertions.assertEquals("Zach Benson", bestFriendsBasement.uploaderName)
        BandcampTestUtils.testImages(bestFriendsBasement.thumbnails)
        Assertions.assertEquals(InfoItem.InfoType.STREAM, bestFriendsBasement.infoType)
    }

    /**
     * Tests whether searching bandcamp for "C418" returns the artist's profile
     */
    @Test
    @Throws(ExtractionException::class, IOException::class)
    fun testChannelSearch() {
        val extractor: SearchExtractor = Bandcamp.getSearchExtractor("C418")
        val c418: InfoItem? = extractor.initialPage?.items?.get(0)

        // C418's artist profile should be the first result, no?
        Assertions.assertEquals("C418", c418?.name)
        BandcampTestUtils.testImages(c418?.thumbnails)
        Assertions.assertEquals("https://c418.bandcamp.com", c418?.url)
    }

    /**
     * Tests whether searching bandcamp for "minecraft volume alpha" returns the corresponding album
     */
    @Test
    @Throws(ExtractionException::class, IOException::class)
    fun testAlbumSearch() {
        val extractor: SearchExtractor = Bandcamp.getSearchExtractor("minecraft volume alpha")
        val minecraft: InfoItem? = extractor.initialPage?.items?.get(0)

        // Minecraft volume alpha should be the first result, no?
        Assertions.assertEquals("Minecraft - Volume Alpha", minecraft?.name)
        BandcampTestUtils.testImages(minecraft?.thumbnails)
        Assertions.assertEquals("https://c418.bandcamp.com/album/minecraft-volume-alpha", minecraft?.url)

        // Verify that playlist tracks counts get extracted correctly
        Assertions.assertEquals(24, (minecraft as PlaylistInfoItem).streamCount)
    }

    /**
     * Tests searches with multiple pages
     */
    @Test
    @Throws(ExtractionException::class, IOException::class)
    fun testMultiplePages() {
        // A query practically guaranteed to have the maximum amount of pages
        val extractor: SearchExtractor = Bandcamp.getSearchExtractor("e")

        val page2 = extractor.initialPage!!.nextPage
        Assertions.assertEquals("https://bandcamp.com/search?q=e&page=2", page2!!.url)

        val page3 = extractor.getPage(page2)!!.nextPage
        Assertions.assertEquals("https://bandcamp.com/search?q=e&page=3", page3!!.url)
    }

    class DefaultTest : DefaultSearchExtractorTest() {
        override fun extractor(): SearchExtractor? {
            return extractor
        }

        override fun expectedService(): StreamingService {
            return Bandcamp
        }

        override fun expectedName(): String? {
            return QUERY
        }

        override fun expectedId(): String? {
            return QUERY
        }

        override fun expectedUrlContains(): String? {
            return "bandcamp.com/search?q=" + QUERY
        }

        override fun expectedOriginalUrlContains(): String? {
            return "bandcamp.com/search?q=" + QUERY
        }

        override fun expectedSearchString(): String? {
            return QUERY
        }

        override fun expectedSearchSuggestion(): String? {
            return null
        }

        companion object {
            private var extractor: SearchExtractor? = null
            private const val QUERY = "noise"


            @BeforeAll
            @Throws(Exception::class)
            fun setUp(): Unit {
                init(getInstance()!!)
                extractor = Bandcamp.getSearchExtractor(QUERY)
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
