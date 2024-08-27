package ac.mdiq.vista.extractor.services

import org.junit.jupiter.api.Test

interface BasePlaylistExtractorTest : BaseListExtractorTest {
    @Test
    @Throws(Exception::class)
    fun testThumbnails()

    @Test
    @Throws(Exception::class)
    fun testBanners()

    @Test
    @Throws(Exception::class)
    fun testUploaderName()

    @Test
    @Throws(Exception::class)
    fun testUploaderAvatars()

    @Test
    @Throws(Exception::class)
    fun testStreamCount()

    @Test
    @Throws(Exception::class)
    fun testUploaderVerified()
}
