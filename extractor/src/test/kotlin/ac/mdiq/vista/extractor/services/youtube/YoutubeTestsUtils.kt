package ac.mdiq.vista.extractor.services.youtube

import ac.mdiq.vista.extractor.ExtractorAsserts
import ac.mdiq.vista.extractor.Image
import ac.mdiq.vista.extractor.services.DefaultTests
import ac.mdiq.vista.extractor.services.DefaultTests.defaultTestImageCollection
import ac.mdiq.vista.extractor.services.youtube.YoutubeJavaScriptPlayerManager.clearAllCaches
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.isConsentAccepted
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.setNumberGenerator
import java.util.*
import java.util.function.Consumer

/**
 * Utility class for YouTube tests.
 */
object YoutubeTestsUtils {
    /**
     * Clears static YT states.
     * This method needs to be called to generate all mocks of a test when running different tests
     * at the same time.
     *
     */

    fun ensureStateless() {
        isConsentAccepted = false
        YoutubeParsingHelper.resetClientVersion();
        setNumberGenerator(Random(1))
        clearAllCaches()
    }

    /**
     * Test that YouTube images of a [Collection] respect
     * [default requirements][DefaultTests.defaultTestImageCollection] and contain
     * the string `yt` in their URL.
     *
     * @param images a YouTube [Image] [Collection]
     */
    fun testImages(images: Collection<Image?>?) {
        defaultTestImageCollection(images)
        // Disable NPE warning because if the collection is null, an AssertionError would be thrown
        // by DefaultTests.defaultTestImageCollection
        images!!.forEach(Consumer { image: Image? -> ExtractorAsserts.assertContains("yt", image!!.url) })
    }
}
