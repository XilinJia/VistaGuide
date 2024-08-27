package ac.mdiq.vista.extractor.services.youtube

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import ac.mdiq.vista.FileUtils.resolveTestResource
import ac.mdiq.vista.downloader.DownloaderTestImpl.Companion.getInstance
import ac.mdiq.vista.extractor.Vista.init
import ac.mdiq.vista.extractor.ServiceList
import ac.mdiq.vista.extractor.linkhandler.LinkHandlerFactory
import ac.mdiq.vista.extractor.services.youtube.extractors.YoutubeSubscriptionExtractor
import ac.mdiq.vista.extractor.subscription.SubscriptionExtractor.InvalidSourceException
import ac.mdiq.vista.extractor.subscription.SubscriptionItem
import java.io.ByteArrayInputStream
import java.io.FileInputStream
import java.nio.charset.StandardCharsets

/**
 * Test for [YoutubeSubscriptionExtractor]
 */
class YoutubeSubscriptionExtractorTest {
    @Test
    @Throws(Exception::class)
    fun testFromInputStream() {
        val subscriptionItems = subscriptionExtractor!!.fromInputStream(
            FileInputStream(resolveTestResource("youtube_takeout_import_test.json")))
        Assertions.assertEquals(7, subscriptionItems.size)

        for (item in subscriptionItems) {
            if (item == null) continue
            Assertions.assertNotNull(item?.name)
            Assertions.assertNotNull(item?.url)
            Assertions.assertTrue(urlHandler!!.acceptUrl(item!!.url))
            Assertions.assertEquals(ServiceList.YouTube.serviceId, item?.serviceId)
        }
    }

    @Test
    @Throws(Exception::class)
    fun testEmptySourceException() {
        val items = subscriptionExtractor!!.fromInputStream(ByteArrayInputStream("[]".toByteArray(StandardCharsets.UTF_8)))
        Assertions.assertTrue(items.isEmpty())
    }

    @Test
    @Throws(Exception::class)
    fun testSubscriptionWithEmptyTitleInSource() {
        val source = "[{\"snippet\":{\"resourceId\":{\"channelId\":\"UCEOXxzW2vU0P-0THehuIIeg\"}}}]"
        val items = subscriptionExtractor!!.fromInputStream(
            ByteArrayInputStream(source.toByteArray(StandardCharsets.UTF_8)))

        Assertions.assertEquals(1, items.size)
        Assertions.assertEquals(ServiceList.YouTube.serviceId, items[0]!!.serviceId)
        Assertions.assertEquals("https://www.youtube.com/channel/UCEOXxzW2vU0P-0THehuIIeg", items[0]!!.url)
        Assertions.assertEquals("", items[0]!!.name)
    }

    @Test
    @Throws(Exception::class)
    fun testSubscriptionWithInvalidUrlInSource() {
        val source = "[{\"snippet\":{\"resourceId\":{\"channelId\":\"gibberish\"},\"title\":\"name1\"}}," +
                "{\"snippet\":{\"resourceId\":{\"channelId\":\"UCEOXxzW2vU0P-0THehuIIeg\"},\"title\":\"name2\"}}]"
        val items = subscriptionExtractor!!.fromInputStream(
            ByteArrayInputStream(source.toByteArray(StandardCharsets.UTF_8)))

        Assertions.assertEquals(1, items.size)
        Assertions.assertEquals(ServiceList.YouTube.serviceId, items[0]!!.serviceId)
        Assertions.assertEquals("https://www.youtube.com/channel/UCEOXxzW2vU0P-0THehuIIeg", items[0]!!.url)
        Assertions.assertEquals("name2", items[0]!!.name)
    }

    @Test
    fun testInvalidSourceException() {
        val invalidList: List<String> = mutableListOf(
            "<xml><notvalid></notvalid></xml>",
            "<opml><notvalid></notvalid></opml>",
            "{\"a\":\"b\"}",
            "[{}]",
            "[\"\", 5]",
            "[{\"snippet\":{\"title\":\"name\"}}]",
            "[{\"snippet\":{\"resourceId\":{\"channelId\":\"gibberish\"}}}]",
            "",
            "\uD83D\uDC28\uD83D\uDC28\uD83D\uDC28",
            "gibberish")

        for (invalidContent in invalidList) {
            try {
                val bytes = invalidContent.toByteArray(StandardCharsets.UTF_8)
                subscriptionExtractor!!.fromInputStream(ByteArrayInputStream(bytes))
                Assertions.fail<Any>("Extracting from \"$invalidContent\" didn't throw an exception")
            } catch (e: Exception) {
                val correctType = e is InvalidSourceException
                if (!correctType) {
                    e.printStackTrace()
                }
                Assertions.assertTrue(correctType, e.javaClass.simpleName + " is not InvalidSourceException")
            }
        }
    }

    @Test
    @Throws(Exception::class)
    fun fromZipInputStream() {
        val zipPaths: List<String> = mutableListOf(
            "youtube_takeout_import_test_1.zip",
            "youtube_takeout_import_test_2.zip"
        )

        for (path in zipPaths) {
            val file = resolveTestResource(path)
            val fileInputStream = FileInputStream(file)
            val subscriptionItems = subscriptionExtractor!!.fromZipInputStream(fileInputStream).filterNotNull()
            assertSubscriptionItems(subscriptionItems)
        }
    }

    @Test
    @Throws(Exception::class)
    fun fromCsvInputStream() {
        val csvPaths: List<String> = mutableListOf(
            "youtube_takeout_import_test_1.csv",
            "youtube_takeout_import_test_2.csv"
        )

        for (path in csvPaths) {
            val file = resolveTestResource(path)
            val fileInputStream = FileInputStream(file)
            val subscriptionItems = subscriptionExtractor!!.fromCsvInputStream(fileInputStream).filterNotNull()
            assertSubscriptionItems(subscriptionItems)
        }
    }

    companion object {
        private var subscriptionExtractor: YoutubeSubscriptionExtractor? = null
        private var urlHandler: LinkHandlerFactory? = null


        @BeforeAll
        fun setupClass() {
            //Doesn't make network requests
            init(getInstance())
            subscriptionExtractor = YoutubeSubscriptionExtractor(ServiceList.YouTube)
            urlHandler = ServiceList.YouTube.getChannelLHFactory()
        }

        @Throws(Exception::class)
        private fun assertSubscriptionItems(subscriptionItems: List<SubscriptionItem>) {
            Assertions.assertTrue(subscriptionItems.isNotEmpty())

            for (item in subscriptionItems) {
                Assertions.assertNotNull(item.name)
                Assertions.assertNotNull(item.url)
                Assertions.assertTrue(urlHandler!!.acceptUrl(item.url))
                Assertions.assertEquals(ServiceList.YouTube.serviceId, item.serviceId)
            }
        }
    }
}
