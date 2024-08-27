package ac.mdiq.vista.extractor.utils

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class ManifestCreatorCacheTest {
    @Test
    fun basicMaximumSizeAndResetTest() {
        val cache = ManifestCreatorCache<String, String>()

        // 30 elements set -> cache resized to 23 -> 5 new elements set to the cache -> 28
        cache.setMaximumSize(30)
        setCacheContent(cache)
        Assertions.assertEquals(28, cache.size(),
            "Wrong cache size with default clear factor and 30 as the maximum size")
        cache.reset()

        Assertions.assertEquals(0, cache.size(),
            "The cache has been not cleared after a reset call (wrong cache size)")
        Assertions.assertEquals(ManifestCreatorCache.DEFAULT_MAXIMUM_SIZE.toLong(), cache.getMaximumSize(),
            "Wrong maximum size after cache reset")
        Assertions.assertEquals(ManifestCreatorCache.DEFAULT_CLEAR_FACTOR, cache.getClearFactor(),
            "Wrong clear factor after cache reset")
    }

    @Test
    fun maximumSizeAndClearFactorSettersAndResettersTest() {
        val cache = ManifestCreatorCache<String, String>()
        cache.setMaximumSize(20)
        cache.setClearFactor(0.5)

        setCacheContent(cache)
        // 30 elements set -> cache resized to 10 -> 5 new elements set to the cache -> 15
        Assertions.assertEquals(15, cache.size(),
            "Wrong cache size with 0.5 as the clear factor and 20 as the maximum size")

        // Clear factor and maximum size getters tests
        Assertions.assertEquals(0.5, cache.getClearFactor(),
            "Wrong clear factor gotten from clear factor getter")
        Assertions.assertEquals(20, cache.getMaximumSize(),
            "Wrong maximum cache size gotten from maximum size getter")

        // Resetters tests
        cache.resetMaximumSize()
        Assertions.assertEquals(ManifestCreatorCache.DEFAULT_MAXIMUM_SIZE.toLong(), cache.getMaximumSize(),
            "Wrong maximum cache size gotten from maximum size getter after maximum size "
                    + "resetter call")

        cache.resetClearFactor()
        Assertions.assertEquals(ManifestCreatorCache.DEFAULT_CLEAR_FACTOR, cache.getClearFactor(),
            "Wrong clear factor gotten from clear factor getter after clear factor resetter "
                    + "call")
    }

    companion object {
        /**
         * Adds sample strings to the provided manifest creator cache, in order to test clear factor and
         * maximum size.
         * @param cache the cache to fill with some data
         */
        private fun setCacheContent(cache: ManifestCreatorCache<String, String>) {
            var i = 0
            while (i < 26) {
                cache.put(('a'.code + i).toChar().toString(), "V")
                ++i
            }

            i = 0
            while (i < 9) {
                cache.put("a" + ('a'.code + i).toChar(), "V")
                ++i
            }
        }
    }
}
