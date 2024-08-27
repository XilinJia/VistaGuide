// Created by Fynn Godau 2019, licensed GNU GPL version 3 or later
package ac.mdiq.vista.extractor.services.bandcamp

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import ac.mdiq.vista.downloader.DownloaderTestImpl.Companion.getInstance
import ac.mdiq.vista.extractor.Vista.init
import ac.mdiq.vista.extractor.ServiceList.Bandcamp
import ac.mdiq.vista.extractor.exceptions.ExtractionException
import ac.mdiq.vista.extractor.services.bandcamp.extractors.BandcampSuggestionExtractor
import java.io.IOException

/**
 * Tests for [BandcampSuggestionExtractor]
 */
class BandcampSuggestionExtractorTest {
    @Test
    @Throws(IOException::class, ExtractionException::class)
    fun testSearchExample() {
        val c418 = extractor!!.suggestionList("c418")

        Assertions.assertTrue(c418!!.contains("C418"))

        // There should be five results, but we can't be sure of that forever
        Assertions.assertTrue(c418.size > 2)
    }

    companion object {
        private var extractor: BandcampSuggestionExtractor? = null


        @BeforeAll
        fun setUp(): Unit {
            init(getInstance()!!)
            extractor = Bandcamp.getSuggestionExtractor() as? BandcampSuggestionExtractor
        }
    }
}
