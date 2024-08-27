package ac.mdiq.vista.extractor.services

import org.junit.jupiter.api.Test

@Suppress("unused")
interface BaseStreamExtractorTest : BaseExtractorTest {
    @Test
    @Throws(Exception::class)
    fun testStreamType()

    @Test
    @Throws(Exception::class)
    fun testUploaderName()

    @Test
    @Throws(Exception::class)
    fun testUploaderUrl()

    @Test
    @Throws(Exception::class)
    fun testUploaderAvatars()

    @Test
    @Throws(Exception::class)
    fun testSubscriberCount()

    @Test
    @Throws(Exception::class)
    fun testSubChannelName()

    @Test
    @Throws(Exception::class)
    fun testSubChannelUrl()

    @Test
    @Throws(Exception::class)
    fun testSubChannelAvatars()

    @Test
    @Throws(Exception::class)
    fun testThumbnails()

    @Test
    @Throws(Exception::class)
    fun testDescription()

    @Test
    @Throws(Exception::class)
    fun testLength()

    @Test
    @Throws(Exception::class)
    fun testTimestamp()

    @Test
    @Throws(Exception::class)
    fun testViewCount()

    @Test
    @Throws(Exception::class)
    fun testUploadDate()

    @Test
    @Throws(Exception::class)
    fun testTextualUploadDate()

    @Test
    @Throws(Exception::class)
    fun testLikeCount()

    @Test
    @Throws(Exception::class)
    fun testDislikeCount()

    @Test
    @Throws(Exception::class)
    fun testRelatedItems()

    @Test
    @Throws(Exception::class)
    fun testAgeLimit()

    @Test
    @Throws(Exception::class)
    fun testErrorMessage()

    @Test
    @Throws(Exception::class)
    fun testAudioStreams()

    @Test
    @Throws(Exception::class)
    fun testVideoStreams()

    @Test
    @Throws(Exception::class)
    fun testSubtitles()

    @Test
    @Throws(Exception::class)
    fun testGetDashMpdUrl()

    @Test
    @Throws(Exception::class)
    fun testFrames()

    @Test
    @Throws(Exception::class)
    fun testHost()

    @Test
    @Throws(Exception::class)
    fun testPrivacy()

    @Test
    @Throws(Exception::class)
    fun testCategory()

    @Test
    @Throws(Exception::class)
    fun testLicence()

    @Test
    @Throws(Exception::class)
    fun testLanguageInfo()

    @Test
    @Throws(Exception::class)
    fun testTags()

    @Test
    @Throws(Exception::class)
    fun testSupportInfo()
}
