package ac.mdiq.vista.extractor.services.youtube.stream

import org.junit.jupiter.api.BeforeAll
import ac.mdiq.vista.downloader.DownloaderFactory
import ac.mdiq.vista.downloader.DownloaderFactory.getDownloader
import ac.mdiq.vista.extractor.Vista.init
import ac.mdiq.vista.extractor.ServiceList.YouTube
import ac.mdiq.vista.extractor.StreamingService
import ac.mdiq.vista.extractor.services.DefaultStreamExtractorTest
import ac.mdiq.vista.extractor.services.youtube.YoutubeTestsUtils
import ac.mdiq.vista.extractor.stream.StreamExtractor
import ac.mdiq.vista.extractor.stream.StreamType

/**
 * Test for [YoutubeStreamLinkHandlerFactory]
 */
class YoutubeStreamExtractorControversialTest : DefaultStreamExtractorTest() {
    override fun extractor(): StreamExtractor {
        return extractor!!
    }

    override fun expectedService(): StreamingService {
        return YouTube
    }

    override fun expectedName(): String? {
        return "Burning Everyone's Koran"
    }

    override fun expectedId(): String? {
        return ID
    }

    override fun expectedUrlContains(): String? {
        return URL
    }

    override fun expectedOriginalUrlContains(): String? {
        return URL
    }

    override fun expectedStreamType(): StreamType? {
        return StreamType.VIDEO_STREAM
    }

    override fun expectedUploaderName(): String? {
        return "Amazing Atheist"
    }

    override fun expectedUploaderUrl(): String? {
        return "https://www.youtube.com/channel/UCjNxszyFPasDdRoD9J6X-sw"
    }

    override fun expectedUploaderSubscriberCountAtLeast(): Long {
        return 900000
    }

    override fun expectedDescriptionContains(): List<String?> {
        return mutableListOf<String?>("http://www.huffingtonpost.com/2010/09/09/obama-gma-interview-quran_n_710282.html",
            "freedom")
    }

    override fun expectedLength(): Long {
        return 219
    }

    override fun expectedViewCountAtLeast(): Long {
        return 285000
    }

    override fun expectedUploadDate(): String? {
        return "2010-09-09 15:40:44.000"
    }

    override fun expectedTextualUploadDate(): String? {
        return "2010-09-09T08:40:44-07:00"
    }

    override fun expectedLikeCountAtLeast(): Long {
        return 13300
    }

    override fun expectedDislikeCountAtLeast(): Long {
        return -1
    }

    override fun expectedTags(): List<String?>? {
        return mutableListOf<String?>("Books", "Burning", "Jones", "Koran", "Qur'an", "Terry", "the amazing atheist")
    }

    override fun expectedCategory(): String? {
        return "Entertainment"
    }

    override fun expectedLicence(): String? {
        return "YouTube licence"
    }

    companion object {
        private const val RESOURCE_PATH = DownloaderFactory.RESOURCE_PATH + "services/youtube/extractor/stream/"
        private const val ID = "T4XJQO3qol8"
        private const val URL = YoutubeStreamExtractorDefaultTest.BASE_URL + ID
        private var extractor: StreamExtractor? = null


        @BeforeAll
        @Throws(Exception::class)
        fun setUp(): Unit {
            YoutubeTestsUtils.ensureStateless()
            init(getDownloader(RESOURCE_PATH + "controversial"))
            extractor = YouTube.getStreamExtractor(URL)
            extractor!!.fetchPage()
        }
    }
}
