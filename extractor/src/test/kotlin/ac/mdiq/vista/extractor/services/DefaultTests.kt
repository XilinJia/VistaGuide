package ac.mdiq.vista.extractor.services

import org.junit.jupiter.api.Assertions
import ac.mdiq.vista.extractor.*
import ac.mdiq.vista.extractor.InfoItem.InfoType
import ac.mdiq.vista.extractor.ListExtractor.InfoItemsPage
import ac.mdiq.vista.extractor.StreamingService.LinkType
import ac.mdiq.vista.extractor.channel.ChannelInfoItem
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.playlist.PlaylistInfoItem
import ac.mdiq.vista.extractor.stream.StreamInfoItem
import java.util.function.Consumer

object DefaultTests {
    @Throws(ParsingException::class)
    fun defaultTestListOfItems(expectedService: StreamingService, itemsList: List<InfoItem?>, errors: List<Throwable?>) {
        Assertions.assertFalse(itemsList.isEmpty(), "List of items is empty")
        Assertions.assertFalse(itemsList.contains(null), "List of items contains a null element")
        ExtractorAsserts.assertEmptyErrors("Errors during extraction", errors.filterNotNull())

        for (item in itemsList) {
            ExtractorAsserts.assertIsSecureUrl(item!!.url)

            val thumbnails: List<Image> = item.thumbnails
            if (thumbnails.isNotEmpty()) defaultTestImageCollection(thumbnails)
            Assertions.assertNotNull(item.infoType, "InfoItem type not set: $item")
            Assertions.assertEquals(expectedService.serviceId, item.serviceId, "Unexpected item service id")
            ExtractorAsserts.assertNotEmpty("Item name not set: $item", item.name)

            if (item is StreamInfoItem) {
                val streamInfoItem = item
                val uploaderUrl = streamInfoItem.uploaderUrl
                if (!uploaderUrl.isNullOrEmpty()) {
                    ExtractorAsserts.assertIsSecureUrl(uploaderUrl)
                    assertExpectedLinkType(expectedService, uploaderUrl, LinkType.CHANNEL)
                }

                val uploaderAvatars: List<Image?> = streamInfoItem.uploaderAvatars
                if (uploaderAvatars.isNotEmpty()) {
                    defaultTestImageCollection(uploaderAvatars)
                }

                assertExpectedLinkType(expectedService, streamInfoItem.url, LinkType.STREAM)

                if (!streamInfoItem.textualUploadDate.isNullOrEmpty()) {
                    val uploadDate = streamInfoItem.uploadDate
                    Assertions.assertNotNull(uploadDate, "No parsed upload date")
                }
            } else if (item is ChannelInfoItem) {
                assertExpectedLinkType(expectedService, item.url, LinkType.CHANNEL)
            } else if (item is PlaylistInfoItem) {
                assertExpectedLinkType(expectedService, item.url, LinkType.PLAYLIST)
            }
        }
    }

    @Throws(ParsingException::class)
    private fun assertExpectedLinkType(expectedService: StreamingService, url: String, expectedLinkType: LinkType) {
        val linkTypeByUrl = expectedService.getLinkTypeByUrl(url)

        Assertions.assertNotEquals(LinkType.NONE, linkTypeByUrl,
            "Url is not recognized by its own service: \"$url\"")
        Assertions.assertEquals(expectedLinkType, linkTypeByUrl,
            "Service returned wrong link type for: \"$url\"")
    }

    fun assertOnlyContainsType(items: InfoItemsPage<out InfoItem>, expectedType: InfoType?) {
        for (item in items.items!!) {
            Assertions.assertEquals(expectedType, item.infoType,
                "Item list contains unexpected info types")
        }
    }

    @Throws(Exception::class)
    fun <T : InfoItem> assertNoMoreItems(extractor: ListExtractor<T>) {
        val initialPage = extractor.initialPage
        Assertions.assertFalse(initialPage.hasNextPage(), "More items available when it shouldn't")
    }

    @Throws(Exception::class)
    fun assertNoDuplicatedItems(expectedService: StreamingService,
                                page1: InfoItemsPage<InfoItem>,
                                page2: InfoItemsPage<InfoItem>
    ) {
        defaultTestListOfItems(expectedService, page1.items!!, page1.errors!!)
        defaultTestListOfItems(expectedService, page2.items!!, page2.errors!!)

        val urlsSet: MutableSet<String> = HashSet()
        for (item in page1.items!!) {
            urlsSet.add(item.url)
        }

        for (item in page2.items!!) {
            val wasAdded = urlsSet.add(item.url)
            if (!wasAdded) {
                Assertions.fail<Any>("Same item was on the first and second page item list")
            }
        }
    }

    @Throws(Exception::class)
    fun <T : InfoItem> defaultTestRelatedItems(extractor: ListExtractor<T>): InfoItemsPage<T> {
        val page = extractor.initialPage
        val itemsList = page.items ?: listOf()
        val errors: List<Throwable?>? = page.errors

        defaultTestListOfItems(extractor.service, itemsList, errors!!)
        return page
    }

    @Throws(Exception::class)
    fun <T : InfoItem> defaultTestMoreItems(extractor: ListExtractor<T>): InfoItemsPage<T> {
        val initialPage = extractor.initialPage
        Assertions.assertTrue(initialPage.hasNextPage(), "Doesn't have more items")
        val nextPage = extractor.getPage(initialPage.nextPage)
        val items = nextPage!!.items
        Assertions.assertFalse(items!!.isEmpty(), "Next page is empty")
        ExtractorAsserts.assertEmptyErrors("Next page have errors", nextPage.errors!!)

        defaultTestListOfItems(extractor.service, nextPage.items!!, nextPage.errors!!)
        return nextPage
    }

    @Throws(Exception::class)
    fun defaultTestGetPageInNewExtractor(extractor: ListExtractor<out InfoItem>,
                                         newExtractor: ListExtractor<out InfoItem>
    ) {
        val nextPage = extractor.initialPage.nextPage

        val page = newExtractor.getPage(nextPage)
        defaultTestListOfItems(extractor.service, page!!.items!!, page.errors!!)
    }

    fun defaultTestImageCollection(
            imageCollection: Collection<Image?>?
    ) {
        Assertions.assertNotNull(imageCollection)
        imageCollection!!.forEach(Consumer { image: Image? ->
            ExtractorAsserts.assertIsSecureUrl(image!!.url)
            ExtractorAsserts.assertGreaterOrEqual(Image.HEIGHT_UNKNOWN.toLong(), image.height.toLong(),
                "Unexpected image height: " + image.height)
            ExtractorAsserts.assertGreaterOrEqual(Image.WIDTH_UNKNOWN.toLong(), image.width.toLong(),
                "Unexpected image width: " + image.width)
        })
    }
}
