package ac.mdiq.vista.extractor.services.media_ccc

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import ac.mdiq.vista.downloader.DownloaderTestImpl.Companion.getInstance
import ac.mdiq.vista.extractor.Vista.init
import ac.mdiq.vista.extractor.ServiceList.MediaCCC
import ac.mdiq.vista.extractor.stream.StreamExtractor

/**
 * Test [MediaCCCStreamExtractor]
 */
class MediaCCCOggTest {
    @Throws(Exception::class)
    @Test
    fun audioStreamsCount() {
            assertEquals(1, extractor!!.audioStreams!!.size)
        }

    @Throws(Exception::class)
    @Test
    fun audioStreamsContainOgg() {
            for (stream in extractor!!.audioStreams!!) {
                Assertions.assertEquals("OGG", stream.format.toString())
            }
        }

    companion object {
        // test against https://media.ccc.de/public/events/1317
        private var extractor: StreamExtractor? = null


        @BeforeAll
        @Throws(Exception::class)
        fun setUpClass(): Unit {
            init(getInstance()!!)

            extractor = MediaCCC.getStreamExtractor("https://media.ccc.de/public/events/1317")
            extractor!!.fetchPage()
        }
    }
}
