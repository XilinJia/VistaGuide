package ac.mdiq.vista.extractor.services.youtube.stream

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import ac.mdiq.vista.downloader.DownloaderFactory
import ac.mdiq.vista.downloader.DownloaderFactory.getDownloader
import ac.mdiq.vista.extractor.Vista.init
import ac.mdiq.vista.extractor.ServiceList.YouTube
import ac.mdiq.vista.extractor.StreamingService
import ac.mdiq.vista.extractor.services.DefaultStreamExtractorTest
import ac.mdiq.vista.extractor.services.youtube.YoutubeTestsUtils
import ac.mdiq.vista.extractor.stream.StreamExtractor
import ac.mdiq.vista.extractor.stream.StreamType

class YoutubeStreamExtractorLivestreamTest : DefaultStreamExtractorTest() {
    @Test
    @Throws(Exception::class)
    override fun testUploaderName() {
        super.testUploaderName()
    }

    override fun extractor(): StreamExtractor {
        return extractor!!
    }

    override fun expectedService(): StreamingService {
        return YouTube
    }

    override fun expectedName(): String {
        return "lofi hip hop radio \uD83D\uDCDA - beats to relax/study to"
    }

    override fun expectedId(): String? {
        return ID
    }

    override fun expectedUrlContains(): String? {
        return YoutubeStreamExtractorDefaultTest.BASE_URL + ID
    }

    override fun expectedOriginalUrlContains(): String? {
        return URL
    }

    override fun expectedStreamType(): StreamType? {
        return StreamType.LIVE_STREAM
    }

    override fun expectedUploaderName(): String? {
        return "Lofi Girl"
    }

    override fun expectedUploaderUrl(): String? {
        return "https://www.youtube.com/channel/UCSJ4gkVC6NrvII8umztf0Ow"
    }

    override fun expectedUploaderSubscriberCountAtLeast(): Long {
        return 9800000
    }

    override fun expectedDescriptionContains(): List<String?> {
        return mutableListOf<String?>("Lofi Girl merch",
            "Thank you for listening, I hope you will have a good time here")
    }

    override fun expectedUploaderVerified(): Boolean {
        return true
    }

    override fun expectedLength(): Long {
        return 0
    }

    override fun expectedTimestamp(): Long {
        return TIMESTAMP.toLong()
    }

    override fun expectedViewCountAtLeast(): Long {
        return 0
    }

    override fun expectedUploadDate(): String? {
        return "2022-07-12 12:12:29.000"
    }

    override fun expectedTextualUploadDate(): String? {
        return "2022-07-12T05:12:29-07:00"
    }

    override fun expectedLikeCountAtLeast(): Long {
        return 340000
    }

    override fun expectedDislikeCountAtLeast(): Long {
        return -1
    }

    override fun expectedHasSubtitles(): Boolean {
        return false
    }

    override fun expectedDashMpdUrlContains(): String? {
        return "https://manifest.googlevideo.com/api/manifest/dash/"
    }

    override fun expectedHasFrames(): Boolean {
        return false
    }

    override fun expectedLicence(): String? {
        return "YouTube licence"
    }

    override fun expectedCategory(): String? {
        return "Music"
    }

    override fun expectedTags(): List<String?>? {
        return mutableListOf<String?>("beats to relax",
            "chilled cow",
            "chilled cow radio",
            "chilledcow",
            "chilledcow radio",
            "chilledcow station",
            "chillhop",
            "hip hop",
            "hiphop",
            "lo fi",
            "lo fi hip hop",
            "lo fi hip hop radio",
            "lo fi hiphop",
            "lo fi radio",
            "lo-fi",
            "lo-fi hip hop",
            "lo-fi hip hop radio",
            "lo-fi hiphop",
            "lo-fi radio",
            "lofi",
            "lofi hip hop",
            "lofi hip hop radio",
            "lofi hiphop",
            "lofi radio",
            "music",
            "lofi radio chilledcow",
            "music to study",
            "playlist",
            "radio",
            "relaxing music",
            "study music",
            "lofi hip hop radio - beats to relax\\/study to")
    }

    companion object {
        private const val RESOURCE_PATH = DownloaderFactory.RESOURCE_PATH + "services/youtube/extractor/stream/"
        private const val ID = "jfKfPfyJRdk"
        private const val TIMESTAMP = 1737
        private const val URL = YoutubeStreamExtractorDefaultTest.BASE_URL + ID + "&t=" + TIMESTAMP
        private var extractor: StreamExtractor? = null


        @BeforeAll
        @Throws(Exception::class)
        fun setUp(): Unit {
            YoutubeTestsUtils.ensureStateless()
            init(getDownloader(RESOURCE_PATH + "live"))
            extractor = YouTube.getStreamExtractor(URL)
            extractor!!.fetchPage()
        }
    }
}
