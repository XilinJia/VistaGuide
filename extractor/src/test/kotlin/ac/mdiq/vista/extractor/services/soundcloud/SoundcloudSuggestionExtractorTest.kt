package ac.mdiq.vista.extractor.services.soundcloud

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import ac.mdiq.vista.downloader.DownloaderTestImpl.Companion.getInstance
import ac.mdiq.vista.extractor.Vista.init
import ac.mdiq.vista.extractor.ServiceList.SoundCloud
import ac.mdiq.vista.extractor.exceptions.ExtractionException
import ac.mdiq.vista.extractor.suggestion.SuggestionExtractor
import java.io.IOException

/**
 * Test for [SuggestionExtractor]
 */
class SoundcloudSuggestionExtractorTest {
    @Test
    @Throws(IOException::class, ExtractionException::class)
    fun testIfSuggestions() {
        Assertions.assertFalse(suggestionExtractor!!.suggestionList("lil uzi vert")!!
            .isEmpty())
    }

    companion object {
        private var suggestionExtractor: SuggestionExtractor? = null

        @BeforeAll
        fun setUp() {
            init(getInstance()!!)
            suggestionExtractor = SoundCloud.getSuggestionExtractor()
        }
    }
}
