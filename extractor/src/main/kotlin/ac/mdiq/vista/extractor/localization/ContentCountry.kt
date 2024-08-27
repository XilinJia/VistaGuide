package ac.mdiq.vista.extractor.localization

import java.io.Serializable
import java.util.*


/**
 * Represents a country that should be used when fetching content.
 *
 *
 * YouTube, for example, give different results in their feed depending on which country is
 * selected.
 *
 */
class ContentCountry(@JvmField val countryCode: String) : Serializable {
    override fun toString(): String {
        return countryCode
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ContentCountry) return false

        return countryCode == other.countryCode
    }

    override fun hashCode(): Int {
        return countryCode.hashCode()
    }

    companion object {
        @JvmField
        val DEFAULT: ContentCountry = ContentCountry(Localization.DEFAULT.getCountryCode())


        fun listFrom(vararg countryCodeList: String): List<ContentCountry> {
            val toReturn: MutableList<ContentCountry> = ArrayList()
            for (countryCode in countryCodeList) {
                toReturn.add(ContentCountry(countryCode))
            }
            return Collections.unmodifiableList(toReturn)
        }
    }
}
