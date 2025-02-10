package ac.mdiq.vista.extractor.localization

import ac.mdiq.vista.extractor.timeago.PatternsHolder
import ac.mdiq.vista.extractor.timeago.PatternsManager.getPatterns
import java.time.OffsetDateTime


object TimeAgoPatternsManager {
    private fun getPatternsFor(localization: Localization): PatternsHolder? {
        return getPatterns(localization.languageCode,
            localization.getCountryCode())
    }

    fun getTimeAgoParserFor(localization: Localization): TimeAgoParser? {
        val holder = getPatternsFor(localization) ?: return null
        return TimeAgoParser(holder)
    }

    fun getTimeAgoParserFor(localization: Localization, now: OffsetDateTime): TimeAgoParser? {
        val holder = getPatternsFor(localization)
        if (holder == null) return null
        return TimeAgoParser(holder, now)
    }
}
