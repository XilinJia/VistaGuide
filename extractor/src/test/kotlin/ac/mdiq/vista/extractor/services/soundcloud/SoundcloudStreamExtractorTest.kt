package ac.mdiq.vista.extractor.services.soundcloud

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import ac.mdiq.vista.downloader.DownloaderTestImpl.Companion.getInstance
import ac.mdiq.vista.extractor.ExtractorAsserts
import ac.mdiq.vista.extractor.MediaFormat
import ac.mdiq.vista.extractor.Vista.init
import ac.mdiq.vista.extractor.ServiceList.SoundCloud
import ac.mdiq.vista.extractor.StreamingService
import ac.mdiq.vista.extractor.exceptions.GeographicRestrictionException
import ac.mdiq.vista.extractor.exceptions.SoundCloudGoPlusContentException
import ac.mdiq.vista.extractor.services.DefaultStreamExtractorTest
import ac.mdiq.vista.extractor.stream.AudioStream
import ac.mdiq.vista.extractor.stream.DeliveryMethod
import ac.mdiq.vista.extractor.stream.StreamExtractor
import ac.mdiq.vista.extractor.stream.StreamType
import java.util.function.Consumer

object SoundcloudStreamExtractorTest {
    private const val SOUNDCLOUD = "https://soundcloud.com/"

    class SoundcloudGeoRestrictedTrack : DefaultStreamExtractorTest() {
        override fun extractor(): StreamExtractor {
            return extractor!!
        }

        override fun expectedService(): StreamingService {
            return SoundCloud
        }

        override fun expectedName(): String {
            return "One Touch"
        }

        override fun expectedId(): String? {
            return "621612588"
        }

        override fun expectedUrlContains(): String? {
            return UPLOADER + "/" + ID
        }

        override fun expectedOriginalUrlContains(): String? {
            return URL
        }

        override fun expectedStreamType(): StreamType? {
            return StreamType.AUDIO_STREAM
        }

        override fun expectedUploaderName(): String? {
            return "Jess Glynne"
        }

        override fun expectedUploaderUrl(): String? {
            return UPLOADER
        }

        override fun expectedUploaderVerified(): Boolean {
            return true
        }

        override fun expectedDescriptionIsEmpty(): Boolean {
            return true
        }

        override fun expectedDescriptionContains(): List<String?> {
            return emptyList<String>()
        }

        override fun expectedLength(): Long {
            return 197
        }

        override fun expectedTimestamp(): Long {
            return TIMESTAMP.toLong()
        }

        override fun expectedViewCountAtLeast(): Long {
            return 43000
        }

        override fun expectedUploadDate(): String? {
            return "2019-05-16 16:28:45.000"
        }

        override fun expectedTextualUploadDate(): String? {
            return "2019-05-16 16:28:45"
        }

        override fun expectedLikeCountAtLeast(): Long {
            return 600
        }

        override fun expectedDislikeCountAtLeast(): Long {
            return -1
        }

        override fun expectedHasAudioStreams(): Boolean {
            return false
        }

        override fun expectedHasVideoStreams(): Boolean {
            return false
        }

        override fun expectedHasSubtitles(): Boolean {
            return false
        }

        override fun expectedHasFrames(): Boolean {
            return false
        }

        override fun expectedStreamSegmentsCount(): Int {
            return 0
        }

        override fun expectedLicence(): String? {
            return "all-rights-reserved"
        }

        override fun expectedCategory(): String? {
            return "Pop"
        }

        @Test
        @Disabled("Unreliable, sometimes it has related items, sometimes it does not")
        @Throws(
            Exception::class)
        override fun testRelatedItems() {
            super.testRelatedItems()
        }

        companion object {
            private const val ID = "one-touch"
            private const val UPLOADER = SOUNDCLOUD + "jessglynne"
            private const val TIMESTAMP = 0
            private const val URL = UPLOADER + "/" + ID + "#t=" + TIMESTAMP
            private var extractor: StreamExtractor? = null


            @BeforeAll
            @Throws(Exception::class)
            fun setUp() {
                init(getInstance()!!)
                extractor = SoundCloud.getStreamExtractor(URL)
                try {
                    extractor!!.fetchPage()
                } catch (e: GeographicRestrictionException) {
                    // expected
                }
            }
        }
    }

    class SoundcloudGoPlusTrack : DefaultStreamExtractorTest() {
        @Test
        @Disabled(("Unreliable, sometimes it has related items, sometimes it does not. See " +
                "https://github.com/XilinJia/VistaGuide/runs/2280013723#step:5:263 " +
                "https://github.com/XilinJia/VistaGuide/pull/601"))
        @Throws(Exception::class)
        override fun testRelatedItems() {
            super.testRelatedItems()
        }

        override fun extractor(): StreamExtractor {
            return extractor!!
        }

        override fun expectedService(): StreamingService {
            return SoundCloud
        }

        override fun expectedName(): String {
            return "Places (feat. Ina Wroldsen)"
        }

        override fun expectedId(): String? {
            return "292479564"
        }

        override fun expectedUrlContains(): String? {
            return UPLOADER + "/" + ID
        }

        override fun expectedOriginalUrlContains(): String? {
            return URL
        }

        override fun expectedStreamType(): StreamType? {
            return StreamType.AUDIO_STREAM
        }

        override fun expectedUploaderName(): String? {
            return "martinsolveig"
        }

        override fun expectedUploaderUrl(): String? {
            return UPLOADER
        }

        override fun expectedUploaderVerified(): Boolean {
            return true
        }

        override fun expectedDescriptionIsEmpty(): Boolean {
            return true
        }

        override fun expectedDescriptionContains(): List<String?> {
            return emptyList<String>()
        }

        override fun expectedLength(): Long {
            return 30
        }

        override fun expectedTimestamp(): Long {
            return TIMESTAMP.toLong()
        }

        override fun expectedViewCountAtLeast(): Long {
            return 386000
        }

        override fun expectedUploadDate(): String? {
            return "2016-11-11 01:16:37.000"
        }

        override fun expectedTextualUploadDate(): String? {
            return "2016-11-11 01:16:37"
        }

        override fun expectedLikeCountAtLeast(): Long {
            return 7350
        }

        override fun expectedDislikeCountAtLeast(): Long {
            return -1
        }

        override fun expectedHasAudioStreams(): Boolean {
            return false
        }

        override fun expectedHasVideoStreams(): Boolean {
            return false
        }

        override fun expectedHasRelatedItems(): Boolean {
            return true
        }

        override fun expectedHasSubtitles(): Boolean {
            return false
        }

        override fun expectedHasFrames(): Boolean {
            return false
        }

        override fun expectedStreamSegmentsCount(): Int {
            return 0
        }

        override fun expectedLicence(): String? {
            return "all-rights-reserved"
        }

        override fun expectedCategory(): String? {
            return "Dance"
        }

        companion object {
            private const val ID = "places"
            private const val UPLOADER = SOUNDCLOUD + "martinsolveig"
            private const val TIMESTAMP = 0
            private const val URL = UPLOADER + "/" + ID + "#t=" + TIMESTAMP
            private var extractor: StreamExtractor? = null


            @BeforeAll
            @Throws(Exception::class)
            fun setUp(): Unit {
                init(getInstance()!!)
                extractor = SoundCloud.getStreamExtractor(URL)
                try {
                    extractor!!.fetchPage()
                } catch (e: SoundCloudGoPlusContentException) {
                    // expected
                }
            }
        }
    }

    class CreativeCommonsPlaysWellWithOthers : DefaultStreamExtractorTest() {
        override fun extractor(): StreamExtractor {
            return extractor!!
        }

        override fun expectedService(): StreamingService {
            return SoundCloud
        }

        override fun expectedName(): String? {
            return "Plays Well with Others, Ep 2: What Do an Army of Ants and an Online Encyclopedia Have in Common?"
        }

        override fun expectedId(): String? {
            return "597253485"
        }

        override fun expectedUrlContains(): String? {
            return UPLOADER + "/" + ID
        }

        override fun expectedOriginalUrlContains(): String? {
            return URL
        }

        override fun expectedStreamType(): StreamType? {
            return StreamType.AUDIO_STREAM
        }

        override fun expectedUploaderName(): String? {
            return "Creative Commons"
        }

        override fun expectedUploaderUrl(): String? {
            return UPLOADER
        }

        override fun expectedDescriptionContains(): List<String?> {
            return mutableListOf<String?>("Stigmergy is a mechanism of indirect coordination",
                "All original content in Plays Well with Others is available under a Creative Commons BY license.")
        }

        override fun expectedLength(): Long {
            return 1400
        }

        override fun expectedTimestamp(): Long {
            return TIMESTAMP.toLong()
        }

        override fun expectedViewCountAtLeast(): Long {
            return 27000
        }

        override fun expectedUploadDate(): String? {
            return "2019-03-28 13:36:18.000"
        }

        override fun expectedTextualUploadDate(): String? {
            return "2019-03-28 13:36:18"
        }

        override fun expectedLikeCountAtLeast(): Long {
            return 25
        }

        override fun expectedDislikeCountAtLeast(): Long {
            return -1
        }

        override fun expectedHasVideoStreams(): Boolean {
            return false
        }

        override fun expectedHasSubtitles(): Boolean {
            return false
        }

        override fun expectedHasFrames(): Boolean {
            return false
        }

        override fun expectedStreamSegmentsCount(): Int {
            return 0
        }

        override fun expectedLicence(): String? {
            return "cc-by"
        }

        override fun expectedCategory(): String? {
            return "Podcast"
        }

        override fun expectedTags(): List<String?>? {
            return mutableListOf<String?>("ants",
                "collaboration",
                "creative commons",
                "stigmergy",
                "storytelling",
                "wikipedia")
        }

        @Test
        @Throws(Exception::class)
        override fun testAudioStreams() {
            super.testAudioStreams()
            val audioStreams = extractor!!.audioStreams
            Assertions.assertEquals(2, audioStreams!!.size)
            audioStreams.forEach(Consumer { audioStream: AudioStream ->
                val deliveryMethod = audioStream.deliveryMethod
                val mediaUrl = audioStream.content
                if (audioStream.format == MediaFormat.OPUS) {
                    // Assert that it's an OPUS 64 kbps media URL with a single range which comes
                    // from an HLS SoundCloud CDN
                    ExtractorAsserts.assertContains("-hls-opus-media.sndcdn.com", mediaUrl)
                    ExtractorAsserts.assertContains(".64.opus", mediaUrl)
                    Assertions.assertSame(DeliveryMethod.HLS, deliveryMethod,
                        ("Wrong delivery method for stream " + audioStream.id + ": "
                                + deliveryMethod))
                } else if (audioStream.format == MediaFormat.MP3) {
                    // Assert that it's a MP3 128 kbps media URL which comes from a progressive
                    // SoundCloud CDN
                    ExtractorAsserts.assertContains("-media.sndcdn.com/bKOA7Pwbut93.128.mp3",
                        mediaUrl)
                    Assertions.assertSame(DeliveryMethod.PROGRESSIVE_HTTP, deliveryMethod,
                        ("Wrong delivery method for stream " + audioStream.id + ": "
                                + deliveryMethod))
                }
            })
        }

        companion object {
            private const val ID =
                "plays-well-with-others-ep-2-what-do-an-army-of-ants-and-an-online-encyclopedia-have-in-common"
            private const val UPLOADER = SOUNDCLOUD + "wearecc"
            private const val TIMESTAMP = 69
            private const val URL = UPLOADER + "/" + ID + "#t=" + TIMESTAMP
            private var extractor: StreamExtractor? = null


            @BeforeAll
            @Throws(Exception::class)
            fun setUp(): Unit {
                init(getInstance()!!)
                extractor = SoundCloud.getStreamExtractor(URL)
                extractor!!.fetchPage()
            }
        }
    }
}
