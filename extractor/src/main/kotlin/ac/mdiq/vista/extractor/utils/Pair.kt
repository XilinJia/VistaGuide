package ac.mdiq.vista.extractor.utils

import java.io.Serializable
import java.util.*

/**
 * Serializable class to create a pair of objects.
 *
 * The two objects of the pair must be [serializable][Serializable] and can be of the same
 * type.
 *
 * Note that this class is not intended to be used as a general-purpose pair and should only be
 * used when interfacing with the extractor.
 *
 *
 * @param <F> the type of the first object, which must be [Serializable]
 * @param <S> the type of the second object, which must be [Serializable]
</S></F> */
/**
 * Creates a new [Pair] object.
 *
 * @param first  the first object of the pair
 * @param second the second object of the pair
 */
class Pair<F : Serializable?, S : Serializable?>(
        var first: F,
        var second: S) : Serializable {
    /**
     * Gets the first object of the pair.
     *
     * @return the first object of the pair
     */
    /**
     * Sets the first object, which must be of the [F] type.
     *
     * @param first the new first object of the pair
     */

    /**
     * Gets the second object of the pair.
     *
     * @return the second object of the pair
     */
    /**
     * Sets the first object, which must be of the [S] type.
     *
     * @param second the new first object of the pair
     */

    /**
     * Returns a string representation of the current `Pair`.
     *
     * The string representation will look like this:
     * `
     * {*firstObject.toString()*, *secondObject.toString()*}
    ` *
     *
     *
     * @return a string representation of the current `Pair`
     */
    override fun toString(): String {
        return "{$first, $second}"
    }

    /**
     * Reveals whether an object is equal to this `Pair` instance.
     *
     * @param other the object to compare with this `Pair` instance
     * @return whether an object is equal to this `Pair` instance
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true

        if (other == null || javaClass != other.javaClass) return false

        val pair = other as Pair<*, *>
        return first == pair.first && second == pair.second
    }

    /**
     * Returns a hash code of the current `Pair` by using the first and second object.
     *
     * @return a hash code of the current `Pair`
     */
    override fun hashCode(): Int {
        return Objects.hash(first, second)
    }
}
