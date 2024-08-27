package ac.mdiq.vista.extractor

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import ac.mdiq.vista.extractor.linkhandler.ListLinkHandler
import java.net.MalformedURLException
import java.net.URL
import java.util.*
import java.util.function.Consumer
import java.util.stream.Collectors



object ExtractorAsserts {
    fun assertEmptyErrors(message: String, errors: List<Throwable>) {
        if (!errors.isEmpty()) {
            val messageBuilder = StringBuilder(message)
            for (e in errors) {
                messageBuilder.append("\n  * ").append(e.message)
            }
            messageBuilder.append(" ")
            throw AssertionError(messageBuilder.toString(), errors[0])
        }
    }


    private fun urlFromString(url: String): URL {
        try {
            return URL(url)
        } catch (e: MalformedURLException) {
            throw AssertionError("Invalid url: \"$url\"", e)
        }
    }

    fun assertIsValidUrl(url: String) {
        urlFromString(url)
    }

    fun assertIsSecureUrl(urlToCheck: String) {
        val url = urlFromString(urlToCheck)
        Assertions.assertEquals("https", url.protocol, "Protocol of URL is not secure")
    }

    fun assertNotEmpty(stringToCheck: String) {
        assertNotEmpty(null, stringToCheck)
    }

    fun assertNotEmpty(message: String?, stringToCheck: String) {
        Assertions.assertNotNull(stringToCheck, message)
        Assertions.assertFalse(stringToCheck.isEmpty(), message)
    }

    fun assertNotEmpty(collectionToCheck: Collection<*>?) {
        assertNotEmpty(null, collectionToCheck)
    }

    fun assertNotEmpty(message: String?, collectionToCheck: Collection<*>?) {
        Assertions.assertNotNull(collectionToCheck)
        Assertions.assertFalse(collectionToCheck!!.isEmpty(), message)
    }

    fun assertEmpty(stringToCheck: String?) {
        assertEmpty(null, stringToCheck)
    }

    fun assertEmpty(message: String?, stringToCheck: String?) {
        if (stringToCheck != null) Assertions.assertTrue(stringToCheck.isEmpty(), message)
    }

    fun assertEmpty(collectionToCheck: Collection<*>?) {
        if (collectionToCheck != null) Assertions.assertTrue(collectionToCheck.isEmpty())
    }

    @JvmOverloads
    fun assertNotBlank(stringToCheck: String?, message: String? = null) {
        Assertions.assertFalse(stringToCheck.isNullOrEmpty(), message)
    }

    @JvmOverloads
    fun assertGreater(expected: Long, actual: Long, message: String? = "$actual is not > $expected") {
        Assertions.assertTrue(actual > expected, message)
    }

    @JvmOverloads
    fun assertGreaterOrEqual(expected: Long, actual: Long, message: String? = "$actual is not >= $expected") {
        Assertions.assertTrue(actual >= expected, message)
    }

    @JvmOverloads
    fun assertLess(expected: Long, actual: Long, message: String? = "$actual is not < $expected") {
        Assertions.assertTrue(actual < expected, message)
    }

    @JvmOverloads
    fun assertLessOrEqual(expected: Long, actual: Long, message: String? = "$actual is not <= $expected") {
        Assertions.assertTrue(actual <= expected, message)
    }

    // this assumes that sorting a and b in-place is not an issue, so it's only intended for tests
    fun assertEqualsOrderIndependent(expected: List<String>?, actual: List<String>?) {
        if (expected == null) {
            Assertions.assertNull(actual)
            return
        } else {
            Assertions.assertNotNull(actual)
        }

        Collections.sort(expected)
        Collections.sort(actual)
        // using new ArrayList<> to make sure the type is the same
        Assertions.assertEquals(ArrayList(expected), ArrayList(actual))
    }

    fun assertContains(shouldBeContained: String, container: String) {
        Assertions.assertNotNull(shouldBeContained, "shouldBeContained is null")
        Assertions.assertNotNull(container, "container is null")
        Assertions.assertTrue(container.contains(shouldBeContained),
            "'$shouldBeContained' should be contained inside '$container'")
    }

    fun assertTabsContain(tabs: List<ListLinkHandler>, vararg expectedTabs: String) {
        val tabSet: Set<String> = tabs.stream()
            .map { linkHandler ->
                assertEquals(1, linkHandler.contentFilters.size,
                    "Unexpected content filters for channel tab: " + linkHandler.contentFilters)
                linkHandler.contentFilters.get(0)
            }
            .collect(Collectors.toUnmodifiableSet())

        assertEquals(expectedTabs.size, tabSet.size,
            ("""
                Different amount of tabs returned:
                Expected: ${Arrays.toString(expectedTabs)}
                Actual: $tabSet
                """.trimIndent()))
        Arrays.stream(expectedTabs)
            .forEach { expectedTab: String ->
                Assertions.assertTrue(tabSet.contains(expectedTab), "Missing $expectedTab tab (got $tabSet)")
            }
    }

    fun assertContainsImageUrlInImageCollection(exceptedImageUrlContained: String?, imageCollection: Collection<Image>?) {
        Assertions.assertNotNull(exceptedImageUrlContained, "exceptedImageUrlContained is null")
        Assertions.assertNotNull(imageCollection, "imageCollection is null")
        Assertions.assertTrue(imageCollection!!.stream().anyMatch { image: Image -> image.url == exceptedImageUrlContained })
    }

    fun assertContainsOnlyEquivalentImages(firstImageCollection: Collection<Image>?, secondImageCollection: Collection<Image>?) {
        Assertions.assertNotNull(firstImageCollection)
        Assertions.assertNotNull(secondImageCollection)
        Assertions.assertEquals(firstImageCollection!!.size, secondImageCollection!!.size)

        firstImageCollection.forEach(Consumer { exceptedImage: Image ->
            Assertions.assertTrue(
                secondImageCollection.stream().anyMatch { image: Image ->
                    exceptedImage.url == image.url && exceptedImage.height == image.height && exceptedImage.width == image.width })
        })
    }

    fun assertNotOnlyContainsEquivalentImages(firstImageCollection: Collection<Image>?, secondImageCollection: Collection<Image>?) {
        Assertions.assertNotNull(firstImageCollection)
        Assertions.assertNotNull(secondImageCollection)

        if (secondImageCollection!!.size != firstImageCollection!!.size) return

        for (unexpectedImage in firstImageCollection) {
            for (image in secondImageCollection) {
                if (image.url != unexpectedImage.url || image.height != unexpectedImage.height || image.width != unexpectedImage.width) return
            }
        }

        throw AssertionError("All excepted images have an equivalent in the image list")
    }
}
