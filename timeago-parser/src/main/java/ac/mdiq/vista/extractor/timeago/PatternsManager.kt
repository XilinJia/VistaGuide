package ac.mdiq.vista.extractor.timeago

import java.lang.reflect.InvocationTargetException

object PatternsManager {
    /**
     * Return an holder object containing all the patterns array.
     *
     * @return an object containing the patterns. If not existent, `null`.
     */

    fun getPatterns(languageCode: String, countryCode: String?): PatternsHolder? {
        val targetLocalizationClassName = languageCode + (if (countryCode == null || countryCode.isEmpty()) "" else "_$countryCode")
        return PatternMap.getPattern(targetLocalizationClassName);
    }
}
