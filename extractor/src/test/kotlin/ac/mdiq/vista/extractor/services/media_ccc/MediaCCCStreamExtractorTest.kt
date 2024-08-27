package ac.mdiq.vista.extractor.services.media_ccc

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import ac.mdiq.vista.downloader.DownloaderTestImpl.Companion.getInstance
import ac.mdiq.vista.extractor.ExtractorAsserts
import ac.mdiq.vista.extractor.Vista.init
import ac.mdiq.vista.extractor.ServiceList.MediaCCC
import ac.mdiq.vista.extractor.StreamingService
import ac.mdiq.vista.extractor.services.DefaultStreamExtractorTest
import ac.mdiq.vista.extractor.stream.AudioStream
import ac.mdiq.vista.extractor.stream.StreamExtractor
import ac.mdiq.vista.extractor.stream.StreamType
import java.util.*

/**
 * Test [MediaCCCStreamExtractor]
 */
object MediaCCCStreamExtractorTest {
    private const val BASE_URL = "https://media.ccc.de/v/"

    class Gpn18Tmux : DefaultStreamExtractorTest() {
        override fun extractor(): StreamExtractor {
            return extractor!!
        }

        override fun expectedService(): StreamingService {
            return MediaCCC
        }

        override fun expectedName(): String? {
            return "tmux - Warum ein schwarzes Fenster am Bildschirm reicht"
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
            return "gpn18"
        }

        override fun expectedUploaderUrl(): String? {
            return "https://media.ccc.de/c/gpn18"
        }

        override fun expectedDescriptionContains(): List<String?> {
            return mutableListOf<String?>("SSH-Sessions", "\"Terminal Multiplexer\"")
        }

        override fun expectedLength(): Long {
            return 3097
        }

        override fun expectedViewCountAtLeast(): Long {
            return 2380
        }

        override fun expectedUploadDate(): String? {
            return "2018-05-11 00:00:00.000"
        }

        override fun expectedTextualUploadDate(): String? {
            return "2018-05-11T02:00:00.000+02:00"
        }

        override fun expectedLikeCountAtLeast(): Long {
            return -1
        }

        override fun expectedDislikeCountAtLeast(): Long {
            return -1
        }

        override fun expectedHasRelatedItems(): Boolean {
            return false
        }

        override fun expectedHasSubtitles(): Boolean {
            return false
        }

        override fun expectedHasFrames(): Boolean {
            return false
        }

        override fun expectedTags(): List<String?>? {
            return mutableListOf<String?>("gpn18", "105")
        }

        override fun expectedStreamSegmentsCount(): Int {
            return 0
        }

        override fun expectedLanguageInfo(): Locale? {
            return Locale("de")
        }

        @Test
        @Throws(Exception::class)
        override fun testThumbnails() {
            super.testThumbnails()
            ExtractorAsserts.assertContainsImageUrlInImageCollection(
                "https://static.media.ccc.de/media/events/gpn/gpn18/105-hd_preview.jpg",
                extractor!!.thumbnails)
        }

        @Test
        @Throws(Exception::class)
        override fun testUploaderAvatars() {
            super.testUploaderAvatars()
            ExtractorAsserts.assertContainsImageUrlInImageCollection(
                "https://static.media.ccc.de/media/events/gpn/gpn18/logo.png",
                extractor!!.uploaderAvatars)
        }

        @Test
        @Throws(Exception::class)
        override fun testVideoStreams() {
            super.testVideoStreams()
            assertEquals(4, extractor!!.videoStreams!!.size)
        }

        @Test
        @Throws(Exception::class)
        override fun testAudioStreams() {
            super.testAudioStreams()
            val audioStreams = extractor!!.audioStreams
            Assertions.assertEquals(2, audioStreams!!.size)
            val expectedLocale = Locale.forLanguageTag("deu")
            Assertions.assertTrue(audioStreams.stream()
                .allMatch { audioStream: AudioStream -> audioStream.audioLocale == expectedLocale })
        }

        companion object {
            private const val ID = "gpn18-105-tmux-warum-ein-schwarzes-fenster-am-bildschirm-reicht"
            private const val URL = BASE_URL + ID
            private var extractor: StreamExtractor? = null


            @BeforeAll
            @Throws(Exception::class)
            fun setUp(): Unit {
                init(getInstance()!!)
                extractor = MediaCCC.getStreamExtractor(URL)
                extractor!!.fetchPage()
            }
        }
    }

    class _36c3PrivacyMessaging : DefaultStreamExtractorTest() {
        override fun extractor(): StreamExtractor {
            return extractor!!
        }

        override fun expectedService(): StreamingService {
            return MediaCCC
        }

        override fun expectedName(): String? {
            return "What's left for private messaging?"
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
            return "36c3"
        }

        override fun expectedUploaderUrl(): String? {
            return "https://media.ccc.de/c/36c3"
        }

        override fun expectedDescriptionContains(): List<String?> {
            return mutableListOf<String?>("WhatsApp", "Signal")
        }

        override fun expectedLength(): Long {
            return 3603
        }

        override fun expectedViewCountAtLeast(): Long {
            return 2380
        }

        override fun expectedUploadDate(): String? {
            return "2020-01-11 00:00:00.000"
        }

        override fun expectedTextualUploadDate(): String? {
            return "2020-01-11T01:00:00.000+01:00"
        }

        override fun expectedLikeCountAtLeast(): Long {
            return -1
        }

        override fun expectedDislikeCountAtLeast(): Long {
            return -1
        }

        override fun expectedHasRelatedItems(): Boolean {
            return false
        }

        override fun expectedHasSubtitles(): Boolean {
            return false
        }

        override fun expectedHasFrames(): Boolean {
            return false
        }

        override fun expectedTags(): List<String?>? {
            return mutableListOf<String?>("36c3", "10565", "2019", "Security", "Main")
        }

        @Test
        @Throws(Exception::class)
        override fun testThumbnails() {
            super.testThumbnails()
            ExtractorAsserts.assertContainsImageUrlInImageCollection(
                "https://static.media.ccc.de/media/congress/2019/10565-hd_preview.jpg",
                extractor!!.thumbnails)
        }

        @Test
        @Throws(Exception::class)
        override fun testUploaderAvatars() {
            super.testUploaderAvatars()
            ExtractorAsserts.assertContainsImageUrlInImageCollection(
                "https://static.media.ccc.de/media/congress/2019/logo.png",
                extractor!!.uploaderAvatars)
        }

        @Test
        @Throws(Exception::class)
        override fun testVideoStreams() {
            super.testVideoStreams()
            assertEquals(8, extractor!!.videoStreams!!.size)
        }

        @Test
        @Throws(Exception::class)
        override fun testAudioStreams() {
            super.testAudioStreams()
            val audioStreams = extractor!!.audioStreams
            Assertions.assertEquals(2, audioStreams!!.size)
            val expectedLocale = Locale.forLanguageTag("eng")
            Assertions.assertTrue(audioStreams.stream()
                .allMatch { audioStream: AudioStream -> audioStream.audioLocale == expectedLocale })
        }

        override fun expectedLanguageInfo(): Locale? {
            return Locale("en")
        }

        companion object {
            private const val ID = "36c3-10565-what_s_left_for_private_messaging"
            private const val URL = BASE_URL + ID
            private var extractor: StreamExtractor? = null


            @BeforeAll
            @Throws(Exception::class)
            fun setUp(): Unit {
                init(getInstance()!!)
                extractor = MediaCCC.getStreamExtractor(URL)
                extractor!!.fetchPage()
            }
        }
    }
}
