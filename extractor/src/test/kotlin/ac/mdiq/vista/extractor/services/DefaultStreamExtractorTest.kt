package ac.mdiq.vista.extractor.services

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import ac.mdiq.vista.extractor.ExtractorAsserts
import ac.mdiq.vista.extractor.MediaFormat
import ac.mdiq.vista.extractor.MetaInfo
import ac.mdiq.vista.extractor.stream.StreamExtractor
import ac.mdiq.vista.extractor.stream.StreamExtractor.Privacy
import ac.mdiq.vista.extractor.stream.StreamType
import java.net.MalformedURLException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.stream.Collectors

/**
 * Test for [StreamExtractor]
 */
abstract class DefaultStreamExtractorTest : DefaultExtractorTest<StreamExtractor>(), BaseStreamExtractorTest {
    abstract fun expectedStreamType(): StreamType?
    abstract fun expectedUploaderName(): String?
    abstract fun expectedUploaderUrl(): String?
    open fun expectedUploaderVerified(): Boolean {
        return false
    }

    open fun expectedUploaderSubscriberCountAtLeast(): Long {
        return 0
//        TODO: what's this
//        return uploaderSubscriberCount
    }

    open fun expectedSubChannelName(): String {
        return ""
    } // default: there is no subchannel

    open fun expectedSubChannelUrl(): String {
        return ""
    } // default: there is no subchannel

    open fun expectedDescriptionIsEmpty(): Boolean {
        return false
    } // default: description is not empty

    abstract fun expectedDescriptionContains(): List<String?> // e.g. for full links
    abstract fun expectedLength(): Long
    open fun expectedTimestamp(): Long {
        return 0
    } // default: there is no timestamp

    abstract fun expectedViewCountAtLeast(): Long
    abstract fun expectedUploadDate(): String? // format: "yyyy-MM-dd HH:mm:ss.SSS"
    abstract fun expectedTextualUploadDate(): String?
    abstract fun expectedLikeCountAtLeast(): Long // return -1 if ratings are disabled
    abstract fun expectedDislikeCountAtLeast(): Long // return -1 if ratings are disabled
    open fun expectedHasRelatedItems(): Boolean {
        return true
    } // default: there are related videos

    open fun expectedAgeLimit(): Int {
        return 0
        //        TODO: what's this
//        return StreamExtractor.ageLimit
    } // default: no limit

    fun expectedErrorMessage(): String? {
        return null
    } // default: no error message

    open fun expectedHasVideoStreams(): Boolean {
        return true
    } // default: there are video streams

    open fun expectedHasAudioStreams(): Boolean {
        return true
    } // default: there are audio streams

    open fun expectedHasSubtitles(): Boolean {
        return true
    } // default: there are subtitles streams

    open fun expectedDashMpdUrlContains(): String? {
        return null
    } // default: no dash mpd

    open fun expectedHasFrames(): Boolean {
        return true
    } // default: there are frames

    open fun expectedHost(): String? {
        return ""
    } // default: no host for centralized platforms

    open fun expectedPrivacy(): Privacy? {
        return Privacy.PUBLIC
    } // default: public

    open fun expectedCategory(): String? {
        return ""
    } // default: no category

    open fun expectedLicence(): String? {
        return ""
    } // default: no licence

    open fun expectedLanguageInfo(): Locale? {
        return null
    } // default: no language info available

    open fun expectedTags(): List<String?>? {
        return emptyList<String>()
    } // default: no tags

    fun expectedSupportInfo(): String {
        return ""
    } // default: no support info available

    open fun expectedStreamSegmentsCount(): Int {
        return -1
    } // return 0 or greater to test (default is -1 to ignore)

    @Throws(MalformedURLException::class)
    open fun expectedMetaInfo(): List<MetaInfo> {
        return emptyList()
    } // default: no metadata info available

    @Test
    @Throws(Exception::class)
    override fun testStreamType() {
        assertEquals(expectedStreamType(), extractor().streamType)
    }

    @Test
    @Throws(Exception::class)
    override fun testUploaderName() {
        assertEquals(expectedUploaderName(), extractor().uploaderName)
    }

    @Test
    @Throws(Exception::class)
    override fun testUploaderUrl() {
        val uploaderUrl = extractor().uploaderUrl
        ExtractorAsserts.assertIsSecureUrl(uploaderUrl)
        assertEquals(expectedUploaderUrl(), uploaderUrl)
    }

    @Test
    @Throws(Exception::class)
    override fun testUploaderAvatars() {
        DefaultTests.defaultTestImageCollection(extractor().uploaderAvatars)
    }

    @Test
    @Throws(Exception::class)
    fun testUploaderVerified() {
        assertEquals(expectedUploaderVerified(), extractor().isUploaderVerified)
    }

    @Test
    @Throws(Exception::class)
    override fun testSubscriberCount() {
        //        TODO: what's this
//        if (expectedUploaderSubscriberCountAtLeast() == uploaderSubscriberCount) {
//            assertEquals(uploaderSubscriberCount, extractor()!!.uploaderSubscriberCount)
//        } else {
            ExtractorAsserts.assertGreaterOrEqual(expectedUploaderSubscriberCountAtLeast(),
                extractor().uploaderSubscriberCount)
//        }
    }

    @Test
    @Throws(Exception::class)
    override fun testSubChannelName() {
        assertEquals(expectedSubChannelName(), extractor().subChannelName)
    }

    @Test
    @Throws(Exception::class)
    override fun testSubChannelUrl() {
        val subChannelUrl = extractor().subChannelUrl
        assertEquals(expectedSubChannelUrl(), subChannelUrl)

        if (expectedSubChannelUrl().isNotEmpty()) {
            // this stream has a subchannel
            ExtractorAsserts.assertIsSecureUrl(subChannelUrl)
        }
    }

    @Test
    @Throws(Exception::class)
    override fun testSubChannelAvatars() {
        if (expectedSubChannelName().isEmpty() && expectedSubChannelUrl().isEmpty()) {
            // this stream has no subchannel
            ExtractorAsserts.assertEmpty(extractor().subChannelAvatars)
        } else {
            // this stream has a subchannel
            DefaultTests.defaultTestImageCollection(extractor().subChannelAvatars)
        }
    }

    @Test
    @Throws(Exception::class)
    override fun testThumbnails() {
        DefaultTests.defaultTestImageCollection(extractor().thumbnails)
    }

    @Test
    @Throws(Exception::class)
    override fun testDescription() {
        val description = extractor().description
        Assertions.assertNotNull(description)

        if (expectedDescriptionIsEmpty()) {
            Assertions.assertTrue(description.content.isEmpty(), "description is not empty")
        } else {
            Assertions.assertFalse(description.content.isEmpty(), "description is empty")
        }

        for (s in expectedDescriptionContains()) {
            ExtractorAsserts.assertContains(s!!, description.content)
        }
    }

    @Test
    @Throws(Exception::class)
    override fun testLength() {
        assertEquals(expectedLength(), extractor().length)
    }

    @Test
    @Throws(Exception::class)
    override fun testTimestamp() {
        assertEquals(expectedTimestamp(), extractor().timeStamp)
    }

    @Test
    @Throws(Exception::class)
    override fun testViewCount() {
        ExtractorAsserts.assertGreaterOrEqual(expectedViewCountAtLeast(), extractor().viewCount)
    }

    @Test
    @Throws(Exception::class)
    override fun testUploadDate() {
        val dateWrapper = extractor().uploadDate

        val expectedData = expectedUploadDate()
        if (expectedData == null) {
            Assertions.assertNull(dateWrapper)
        } else {
            Assertions.assertNotNull(dateWrapper)
            val expectedDateTime = LocalDateTime.parse(expectedData, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"))
            val actualDateTime = dateWrapper!!.offsetDateTime().toLocalDateTime()
            assertEquals(expectedDateTime, actualDateTime)
        }
    }

    @Test
    @Throws(Exception::class)
    override fun testTextualUploadDate() {
        assertEquals(expectedTextualUploadDate(), extractor().textualUploadDate)
    }

    @Test
    @Throws(Exception::class)
    override fun testLikeCount() {
        if (expectedLikeCountAtLeast() == -1L) {
            assertEquals(-1, extractor().likeCount)
        } else {
            ExtractorAsserts.assertGreaterOrEqual(expectedLikeCountAtLeast(), extractor().likeCount)
        }
    }

    @Test
    @Throws(Exception::class)
    override fun testDislikeCount() {
        if (expectedDislikeCountAtLeast() == -1L) {
            assertEquals(-1, extractor().dislikeCount)
        } else {
            ExtractorAsserts.assertGreaterOrEqual(expectedDislikeCountAtLeast(), extractor().dislikeCount)
        }
    }

    @Test
    @Throws(Exception::class)
    override fun testRelatedItems() {
        val relatedStreams = extractor().relatedItems

        if (expectedHasRelatedItems()) {
            Assertions.assertNotNull(relatedStreams)
            DefaultTests.defaultTestListOfItems(extractor().service, relatedStreams!!.getItems(),
                relatedStreams.errors)
        } else {
            Assertions.assertTrue(relatedStreams == null || relatedStreams.getItems().isEmpty())
        }
    }

    @Test
    @Throws(Exception::class)
    override fun testAgeLimit() {
        assertEquals(expectedAgeLimit(), extractor().ageLimit)
    }

    @Test
    @Throws(Exception::class)
    override fun testErrorMessage() {
        assertEquals(expectedErrorMessage(), extractor().errorMessage)
    }

    @Test
    @Throws(Exception::class)
    override fun testVideoStreams() {
        val videoStreams = extractor().videoStreams?.toMutableList()
        val videoOnlyStreams = extractor().videoOnlyStreams
        Assertions.assertNotNull(videoStreams)
        Assertions.assertNotNull(videoOnlyStreams)
        videoStreams?.addAll(videoOnlyStreams!!)

        if (expectedHasVideoStreams()) {
            Assertions.assertFalse(videoStreams!!.isEmpty())

            for (stream in videoStreams) {
                if (stream.isUrl) {
                    ExtractorAsserts.assertIsSecureUrl(stream.content)
                }
                val streamType = extractor().streamType
                // On some video streams, the resolution can be empty and the format be unknown,
                // especially on livestreams (like streams with HLS master playlists)
                if (streamType != StreamType.LIVE_STREAM && streamType != StreamType.AUDIO_LIVE_STREAM) {
                    Assertions.assertFalse(stream.resolution.isEmpty())
                    val formatId = stream.getFormatId()
                    // see MediaFormat: video stream formats range from 0 to 0x100
                    Assertions.assertTrue(formatId in 0..0xff, "Format id does not fit a video stream: $formatId")
                }
            }
        } else {
            Assertions.assertTrue(videoStreams!!.isEmpty())
        }
    }

    @Test
    @Throws(Exception::class)
    override fun testAudioStreams() {
        val audioStreams = extractor().audioStreams
        Assertions.assertNotNull(audioStreams)

        if (expectedHasAudioStreams()) {
            Assertions.assertFalse(audioStreams!!.isEmpty())

            for (stream in audioStreams) {
                if (stream.isUrl) {
                    ExtractorAsserts.assertIsSecureUrl(stream.content)
                }

                // The media format can be unknown on some audio streams
                if (stream.format != null) {
                    val formatId = stream.format!!.id
                    // see MediaFormat: audio stream formats range from 0x100 to 0x1000
                    Assertions.assertTrue(formatId in 0x100..0xfff,
                        "Format id does not fit an audio stream: $formatId")
                }
            }
        } else {
            Assertions.assertTrue(audioStreams!!.isEmpty())
        }
    }

    @Test
    @Throws(Exception::class)
    override fun testSubtitles() {
        val subtitles = extractor().subtitlesDefault
        Assertions.assertNotNull(subtitles)

        if (expectedHasSubtitles()) {
            Assertions.assertFalse(subtitles.isEmpty())

            for (stream in subtitles) {
                if (stream.isUrl) {
                    ExtractorAsserts.assertIsSecureUrl(stream.content)
                }

                val formatId = stream.getFormatId()
                // see MediaFormat: video stream formats range from 0x1000 to 0x10000
                Assertions.assertTrue(formatId in 0x1000..0xffff,
                    "Format id does not fit a subtitles stream: $formatId")
            }
        } else {
            Assertions.assertTrue(subtitles.isEmpty())

            val formats = arrayOf(MediaFormat.VTT, MediaFormat.TTML, MediaFormat.SRT,
                MediaFormat.TRANSCRIPT1, MediaFormat.TRANSCRIPT2, MediaFormat.TRANSCRIPT3)
            for (format in formats) {
                val formatSubtitles = extractor().getSubtitles(format)
                Assertions.assertNotNull(formatSubtitles)
                Assertions.assertTrue(formatSubtitles.isEmpty())
            }
        }
    }

    @Throws(Exception::class)
    override fun testGetDashMpdUrl() {
        val dashMpdUrl = extractor().dashMpdUrl
        if (expectedDashMpdUrlContains() == null) {
            Assertions.assertNotNull(dashMpdUrl)
            Assertions.assertTrue(dashMpdUrl.isEmpty())
        } else {
            ExtractorAsserts.assertIsSecureUrl(dashMpdUrl)
            ExtractorAsserts.assertContains(expectedDashMpdUrlContains()!!, extractor().dashMpdUrl)
        }
    }

    @Test
    @Throws(Exception::class)
    override fun testFrames() {
        val frames = extractor().frames
        Assertions.assertNotNull(frames)

        if (expectedHasFrames()) {
            Assertions.assertFalse(frames.isEmpty())
            for (f in frames) {
                for (url in f.urls) {
                    ExtractorAsserts.assertIsValidUrl(url)
                    ExtractorAsserts.assertIsSecureUrl(url)
                }
                Assertions.assertTrue(f.durationPerFrame > 0)
                assertEquals(f.getFrameBoundsAt(0)[3], f.frameWidth)
            }
        } else {
            Assertions.assertTrue(frames.isEmpty())
        }
    }

    @Test
    @Throws(Exception::class)
    override fun testHost() {
        assertEquals(expectedHost(), extractor().host)
    }

    @Test
    @Throws(Exception::class)
    override fun testPrivacy() {
        assertEquals(expectedPrivacy(), extractor().privacy)
    }

    @Test
    @Throws(Exception::class)
    override fun testCategory() {
        assertEquals(expectedCategory(), extractor().category)
    }

    @Test
    @Throws(Exception::class)
    override fun testLicence() {
        assertEquals(expectedLicence(), extractor().licence)
    }

    @Test
    @Throws(Exception::class)
    override fun testLanguageInfo() {
        assertEquals(expectedLanguageInfo(), extractor().languageInfo)
    }

    @Test
    @Throws(Exception::class)
    override fun testTags() {
        ExtractorAsserts.assertEqualsOrderIndependent(expectedTags()?.filterNotNull(), extractor().tags)
    }

    @Test
    @Throws(Exception::class)
    override fun testSupportInfo() {
        assertEquals(expectedSupportInfo(), extractor().supportInfo)
    }

    @Test
    @Throws(Exception::class)
    fun testStreamSegmentsCount() {
        if (expectedStreamSegmentsCount() >= 0) {
            assertEquals(expectedStreamSegmentsCount(), extractor().streamSegments.size)
        }
    }

    /**
     * @see DefaultSearchExtractorTest.testMetaInfo
     */
    @Test
    @Throws(Exception::class)
    fun testMetaInfo() {
        val metaInfoList = extractor().metaInfo
        val expectedMetaInfoList = expectedMetaInfo()

        for (expectedMetaInfo in expectedMetaInfoList) {
            val texts = metaInfoList.stream()
                .map { metaInfo: MetaInfo -> metaInfo.content!!.content }
                .collect(Collectors.toList())
            val titles = metaInfoList.stream().map { it.title }.collect(Collectors.toList<Any>())
            val urls = metaInfoList.stream().flatMap { info: MetaInfo -> info.getUrls().stream() }
                .collect(Collectors.toList())
            val urlTexts = metaInfoList.stream().flatMap { info: MetaInfo -> info.getUrlTexts().stream() }
                .collect(Collectors.toList())

            Assertions.assertTrue(texts.contains(expectedMetaInfo.content!!.content))
            Assertions.assertTrue(titles.contains(expectedMetaInfo.title))

            for (expectedUrlText in expectedMetaInfo.getUrlTexts()) {
                Assertions.assertTrue(urlTexts.contains(expectedUrlText))
            }
            for (expectedUrl in expectedMetaInfo.getUrls()) {
                Assertions.assertTrue(urls.contains(expectedUrl))
            }
        }
    }
}
