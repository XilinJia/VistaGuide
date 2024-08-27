package ac.mdiq.vista.extractor.localization

import ac.mdiq.vista.extractor.timeago.PatternsHolder
import ac.mdiq.vista.extractor.timeago.PatternsManager.getPatterns


object TimeAgoPatternsManager {

    private fun getPatternsFor(localization: Localization): PatternsHolder? {
        return getPatterns(localization.languageCode,
            localization.getCountryCode())
    }


    fun getTimeAgoParserFor(localization: Localization): TimeAgoParser? {
        val holder = getPatternsFor(localization) ?: return null

        return TimeAgoParser(holder)
    }
}
