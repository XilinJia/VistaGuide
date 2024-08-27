package ac.mdiq.vista.extractor.services

import org.junit.jupiter.api.Test

interface BaseChannelExtractorTest : BaseExtractorTest {
    @Test
    @Throws(Exception::class)
    fun testDescription()

    @Test
    @Throws(Exception::class)
    fun testAvatars()

    @Test
    @Throws(Exception::class)
    fun testBanners()

    @Test
    @Throws(Exception::class)
    fun testFeedUrl()

    @Test
    @Throws(Exception::class)
    fun testSubscriberCount()

    @Test
    @Throws(Exception::class)
    fun testVerified()

    @Test
    @Throws(Exception::class)
    fun testTabs()

    @Test
    @Throws(Exception::class)
    fun testTags()
}
