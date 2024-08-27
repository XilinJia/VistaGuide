// Created by Fynn Godau 2019, licensed GNU GPL version 3 or later
package ac.mdiq.vista.extractor.services.bandcamp

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import ac.mdiq.vista.downloader.DownloaderTestImpl.Companion.getInstance
import ac.mdiq.vista.extractor.Vista.init
import ac.mdiq.vista.extractor.ServiceList.Bandcamp
import ac.mdiq.vista.extractor.StreamingService
import ac.mdiq.vista.extractor.exceptions.ExtractionException
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.services.DefaultStreamExtractorTest
import ac.mdiq.vista.extractor.services.bandcamp.extractors.BandcampExtractorHelper.getStreamUrlFromIds
import ac.mdiq.vista.extractor.services.bandcamp.extractors.BandcampStreamExtractor
import ac.mdiq.vista.extractor.stream.StreamExtractor
import ac.mdiq.vista.extractor.stream.StreamType
import java.io.IOException

/**
 * Tests for [BandcampStreamExtractor]
 */
class BandcampStreamExtractorTest : DefaultStreamExtractorTest() {
    override fun extractor(): StreamExtractor {
        return extractor!!
    }

    override fun expectedService(): StreamingService {
        return Bandcamp
    }

    override fun expectedName(): String? {
        return "Just for the Halibut"
    }

    override fun expectedId(): String? {
        return "https://teaganbear.bandcamp.com/track/just-for-the-halibut"
    }

    override fun expectedUrlContains(): String? {
        return "https://teaganbear.bandcamp.com/track/just-for-the-halibut"
    }

    override fun expectedOriginalUrlContains(): String? {
        return "https://teaganbear.bandcamp.com/track/just-for-the-halibut"
    }

    override fun expectedStreamType(): StreamType? {
        return StreamType.AUDIO_STREAM
    }

    override fun expectedUploaderName(): String? {
        return "Teaganbear"
    }

    override fun expectedUploaderUrl(): String? {
        return "https://teaganbear.bandcamp.com/"
    }

    override fun expectedDescriptionContains(): List<String?> {
        return listOf("it's Creative Commons so feel free to use it in whatever")
    }

    override fun expectedLength(): Long {
        return 124
    }

    override fun expectedViewCountAtLeast(): Long {
        return Long.MIN_VALUE
    }

    override fun expectedUploadDate(): String? {
        return "2019-03-10 23:00:42.000"
    }

    override fun expectedTextualUploadDate(): String? {
        return "10 Mar 2019 23:00:42 GMT"
    }

    override fun expectedLikeCountAtLeast(): Long {
        return Long.MIN_VALUE
    }

    override fun expectedDislikeCountAtLeast(): Long {
        return Long.MIN_VALUE
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

    override fun expectedLicence(): String? {
        return "CC BY 3.0"
    }

    override fun expectedCategory(): String? {
        return "dance"
    }

    @Test
    fun testArtistProfilePictures() {
        BandcampTestUtils.testImages(extractor!!.uploaderAvatars)
    }

    @Test
    @Throws(ParsingException::class)
    fun testTranslateIdsToUrl() {
        // To add tests: look at website's source, search for `band_id` and `item_id`
        Assertions.assertEquals(
            "https://teaganbear.bandcamp.com/track/just-for-the-halibut",
            getStreamUrlFromIds(3877364987L, 3486455278L, "track")
        )
    }

    companion object {
        private var extractor: BandcampStreamExtractor? = null


        @BeforeAll
        @Throws(ExtractionException::class, IOException::class)
        fun setUp(): Unit {
            init(getInstance()!!)

            extractor = Bandcamp.getStreamExtractor("https://teaganbear.bandcamp.com/track/just-for-the-halibut") as? BandcampStreamExtractor
            extractor!!.fetchPage()
        }
    }
}
