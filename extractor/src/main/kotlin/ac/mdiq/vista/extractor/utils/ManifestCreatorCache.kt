package ac.mdiq.vista.extractor.utils

import java.io.Serializable
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Consumer


/**
 * A [serializable][Serializable] cache class used by the extractor to cache manifests
 * generated with extractor's manifests generators.
 *
 *
 *
 * It relies internally on a [ConcurrentHashMap] to allow concurrent access to the cache.
 *
 *
 * @param <K> the type of cache keys, which must be [serializable][Serializable]
 * @param <V> the type of the second element of [pairs][Pair] used as values of the cache,
 * which must be [serializable][Serializable]
</V></K> */
class ManifestCreatorCache<K : Serializable, V : Serializable> : Serializable {
    /**
     * The [ConcurrentHashMap] used internally as the cache of manifests.
     */
    private val concurrentHashMap = ConcurrentHashMap<K, Pair<Int, V>>()

    /**
     * The maximum size of the cache.
     * The default value is [.DEFAULT_MAXIMUM_SIZE].
     *
     */
    private var maximumSize = DEFAULT_MAXIMUM_SIZE

    /**
     * The clear factor of the cache, which is a double between `0` and `1` excluded.
     * The default value is [.DEFAULT_CLEAR_FACTOR].
     *
     */
    private var clearFactor = DEFAULT_CLEAR_FACTOR

    /**
     * Tests if the specified key is in the cache.
     *
     * @param key the key to test its presence in the cache
     * @return `true` if the key is in the cache, `false` otherwise.
     */
    fun containsKey(key: K): Boolean {
        return concurrentHashMap.containsKey(key)
    }

    /**
     * Returns the value to which the specified key is mapped, or `null` if the cache
     * contains no mapping for the key.
     *
     * @param key the key to which getting its value
     * @return the value to which the specified key is mapped, or `null`
     */
    fun get(key: K): Pair<Int, V>? {
        return concurrentHashMap[key]
    }

    /**
     * Adds a new element to the cache.
     *
     *
     *
     * If the cache limit is reached, oldest elements will be cleared first using the load factor
     * and the maximum size.
     *
     *
     * @param key   the key to put
     * @param value the value to associate to the key
     *
     * @return the previous value associated with the key, or `null` if there was no mapping
     * for the key (note that a null return can also indicate that the cache previously associated
     * `null` with the key).
     */
    fun put(key: K, value: V): V? {
        if (!concurrentHashMap.containsKey(key) && concurrentHashMap.size == maximumSize) {
            val newCacheSize = Math.round(maximumSize * clearFactor).toInt()
            keepNewestEntries(if (newCacheSize != 0) newCacheSize else 1)
        }

        val returnValue = concurrentHashMap.put(key, Pair(concurrentHashMap.size, value))
        return returnValue?.second
    }

    /**
     * Clears the cached manifests.
     *
     *
     *
     * The cache will be empty after this method is called.
     *
     */
    fun clear() {
        concurrentHashMap.clear()
    }

    /**
     * Resets the cache.
     * The cache will be empty and the clear factor and the maximum size will be reset to their
     * default values.
     *
     * @see .clear
     * @see .resetClearFactor
     * @see .resetMaximumSize
     */
    fun reset() {
        clear()
        resetClearFactor()
        resetMaximumSize()
    }

    /**
     * @return the number of cached manifests in the cache
     */
    fun size(): Int {
        return concurrentHashMap.size
    }

    /**
     * @return the maximum size of the cache
     */
    fun getMaximumSize(): Long {
        return maximumSize.toLong()
    }

    /**
     * Sets the maximum size of the cache.
     *
     * If the current cache size is more than the new maximum size, the percentage of one less the
     * clear factor of the maximum new size of manifests in the cache will be removed.
     *
     * @param maximumSize the new maximum size of the cache
     * @throws IllegalArgumentException if `maximumSize` is less than or equal to 0
     */
    fun setMaximumSize(maximumSize: Int) {
        require(maximumSize > 0) { "Invalid maximum size" }

        if (maximumSize < this.maximumSize && !concurrentHashMap.isEmpty()) {
            val newCacheSize = Math.round(maximumSize * clearFactor).toInt()
            keepNewestEntries(if (newCacheSize != 0) newCacheSize else 1)
        }

        this.maximumSize = maximumSize
    }

    /**
     * Resets the maximum size of the cache to its [default value][.DEFAULT_MAXIMUM_SIZE].
     */
    fun resetMaximumSize() {
        this.maximumSize = DEFAULT_MAXIMUM_SIZE
    }

    /**
     * @return the current clear factor of the cache, used when the cache limit size is reached
     */
    fun getClearFactor(): Double {
        return clearFactor
    }

    /**
     * Sets the clear factor of the cache, used when the cache limit size is reached.
     * The clear factor must be a double between `0` excluded and `1` excluded.
     * Note that it will be only used the next time the cache size limit is reached.
     * @param clearFactor the new clear factor of the cache
     * @throws IllegalArgumentException if the clear factor passed a parameter is invalid
     */
    fun setClearFactor(clearFactor: Double) {
        require(!(clearFactor <= 0 || clearFactor >= 1)) { "Invalid clear factor" }

        this.clearFactor = clearFactor
    }

    /**
     * Resets the clear factor to its [default value][.DEFAULT_CLEAR_FACTOR].
     */
    fun resetClearFactor() {
        this.clearFactor = DEFAULT_CLEAR_FACTOR
    }


    override fun toString(): String {
        return ("ManifestCreatorCache[clearFactor=$clearFactor, maximumSize=$maximumSize, concurrentHashMap=$concurrentHashMap]")
    }

    /**
     * Keeps only the newest entries in a cache.
     *
     *
     *
     * This method will first collect the entries to remove by looping through the concurrent hash
     * map
     *
     *
     * @param newLimit the new limit of the cache
     */
    private fun keepNewestEntries(newLimit: Int) {
        val difference = concurrentHashMap.size - newLimit
        val entriesToRemove = ArrayList<Map.Entry<K, Pair<Int, V>>>()

        concurrentHashMap.entries.forEach(Consumer<Map.Entry<K, Pair<Int, V>>> { entry: Map.Entry<K, Pair<Int, V>> ->
            val value = entry.value
            if (value.first < difference) entriesToRemove.add(entry)
            else value.first -= difference
        })

        entriesToRemove.forEach(Consumer { entry: Map.Entry<K, Pair<Int, V>> ->
            concurrentHashMap.remove(entry.key, entry.value)
        })
    }

    companion object {
        /**
         * The default maximum size of a manifest cache.
         */
        const val DEFAULT_MAXIMUM_SIZE: Int = Int.MAX_VALUE

        /**
         * The default clear factor of a manifest cache.
         */
        const val DEFAULT_CLEAR_FACTOR: Double = 0.75
    }
}
