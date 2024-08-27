package ac.mdiq.vista.extractor.services.bandcamp

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import ac.mdiq.vista.downloader.DownloaderTestImpl.Companion.getInstance
import ac.mdiq.vista.extractor.Vista.init
import ac.mdiq.vista.extractor.ServiceList.Bandcamp
import ac.mdiq.vista.extractor.exceptions.ExtractionException
import ac.mdiq.vista.extractor.exceptions.PaidContentException
import ac.mdiq.vista.extractor.stream.StreamExtractor

class BandcampPaidStreamExtractorTest {
    @Test
    @Throws(ExtractionException::class)
    fun testPaidTrack() {
        val extractor: StreamExtractor =
            Bandcamp.getStreamExtractor("https://radicaldreamland.bandcamp.com/track/hackmud-continuous-mix")
        Assertions.assertThrows(PaidContentException::class.java) { extractor.fetchPage() }
    }

    companion object {

        @BeforeAll
        fun setUp(): Unit {
            init(getInstance()!!)
        }
    }
}
