package ac.mdiq.vista.extractor.services.peertube

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import ac.mdiq.vista.downloader.DownloaderTestImpl.Companion.getInstance
import ac.mdiq.vista.extractor.ExtractorAsserts
import ac.mdiq.vista.extractor.Vista.init
import ac.mdiq.vista.extractor.ServiceList.PeerTube
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.services.DefaultTests.defaultTestImageCollection
import ac.mdiq.vista.extractor.services.peertube.extractors.PeertubePlaylistExtractor

class PeertubePlaylistExtractorTest {
    class Shocking {
        @Test
        @Throws(ParsingException::class)
        fun testGetName() {
            Assertions.assertEquals("Shocking !", extractor!!.getName())
        }

        @Test
        @Throws(ParsingException::class)
        fun testGetThumbnails() {
            defaultTestImageCollection(extractor!!.thumbnails)
        }

        @Test
        fun testGetUploaderUrl() {
            Assertions.assertEquals("https://skeptikon.fr/accounts/metadechoc", extractor!!.uploaderUrl)
        }

        @Test
        @Throws(ParsingException::class)
        fun testGetUploaderAvatars() {
            defaultTestImageCollection(extractor!!.uploaderAvatars)
        }

        @Test
        fun testGetUploaderName() {
            Assertions.assertEquals("Méta de Choc", extractor!!.uploaderName)
        }

        @Test
        fun testGetStreamCount() {
            ExtractorAsserts.assertGreaterOrEqual(39, extractor!!.streamCount)
        }

        @Test
        @Throws(ParsingException::class)
        fun testGetDescription() {
            ExtractorAsserts.assertContains("épisodes de Shocking", extractor!!.description.content)
        }

        @Test
        fun testGetSubChannelUrl() {
            Assertions.assertEquals("https://skeptikon.fr/video-channels/metadechoc_channel", extractor!!.subChannelUrl)
        }

        @Test
        fun testGetSubChannelName() {
            Assertions.assertEquals("SHOCKING !", extractor!!.subChannelName)
        }

        @Test
        @Throws(ParsingException::class)
        fun testGetSubChannelAvatars() {
            defaultTestImageCollection(extractor!!.subChannelAvatars)
        }

        companion object {
            private var extractor: PeertubePlaylistExtractor? = null


            @BeforeAll
            @Throws(Exception::class)
            fun setUp(): Unit {
                init(getInstance()!!)
                extractor = PeerTube.getPlaylistExtractor("https://framatube.org/videos/watch/playlist/96b0ee2b-a5a7-4794-8769-58d8ccb79ab7") as? PeertubePlaylistExtractor
                extractor!!.fetchPage()
            }
        }
    }
}
