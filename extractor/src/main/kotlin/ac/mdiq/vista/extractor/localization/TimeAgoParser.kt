package ac.mdiq.vista.extractor.localization

import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.timeago.PatternsHolder
import ac.mdiq.vista.extractor.utils.Parser.isMatch
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import java.util.*
import java.util.regex.MatchResult
import java.util.regex.Pattern

/**
 * A helper class that is meant to be used by services that need to parse durations such as
 * `23 seconds` and/or upload dates in the format `2 days ago` or similar.
 */
class TimeAgoParser(private val patternsHolder: PatternsHolder) {
    private val now: OffsetDateTime = OffsetDateTime.now(ZoneOffset.UTC)

    /**
     * Parses a textual date in the format '2 days ago' into a Calendar representation which is then
     * wrapped in a [DateWrapper] object.
     *
     *
     * Beginning with days ago, the date is considered as an approximation.
     *
     * @param textualDate The original date as provided by the streaming service
     * @return The parsed time (can be approximated)
     * @throws ParsingException if the time unit could not be recognized
     */
    @Throws(ParsingException::class)
    fun parse(textualDate: String): DateWrapper {
        for ((chronoUnit, value)
        in patternsHolder.specialCases()) {
            for ((caseText, caseAmount) in value) {
                if (textualDateMatches(textualDate, caseText)) return getResultFor(caseAmount, chronoUnit)
            }
        }
        return getResultFor(parseTimeAgoAmount(textualDate), parseChronoUnit(textualDate))
    }

    /**
     * Parses a textual duration into a duration computer number.
     *
     * @param textualDuration the textual duration to parse
     * @return the textual duration parsed, as a primitive `long`
     * @throws ParsingException if the textual duration could not be parsed
     */
    @Throws(ParsingException::class)
    fun parseDuration(textualDuration: String): Long {
        // We can't use Matcher.results, as it is only available on Android 14 and above
        val matcher = DURATION_PATTERN.matcher(textualDuration)
        val results: MutableList<MatchResult> = ArrayList()
        while (matcher.find()) {
            results.add(matcher.toMatchResult())
        }

        return results.stream()
            .map { match: MatchResult ->
                val digits = match.group(1)
                val word = match.group(2)
                val amount = try {
                    digits.toInt()
                } catch (ignored: NumberFormatException) {
                    1
                }

                val unit: ChronoUnit
                try {
                    unit = parseChronoUnit(word)
                } catch (ignored: ParsingException) {
                    return@map 0L
                }
                amount * unit.duration.seconds
            }
            .filter { n: Long -> n > 0 }
            .reduce { a: Long, b: Long -> java.lang.Long.sum(a, b) }
            .orElseThrow {
                ParsingException("Could not parse duration \"$textualDuration\"")
            }
    }

    private fun parseTimeAgoAmount(textualDate: String): Int {
        return try {
            textualDate.replace("\\D+".toRegex(), "").toInt()
        } catch (ignored: NumberFormatException) {
            // If there is no valid number in the textual date,
            // assume it is 1 (as in 'a second ago').
            1
        }
    }

    @Throws(ParsingException::class)
    private fun parseChronoUnit(textualDate: String): ChronoUnit {
        return patternsHolder.asMap().entries.stream()
            .filter { e: Map.Entry<ChronoUnit?, Collection<String>> ->
                e.value.stream().anyMatch { agoPhrase: String -> textualDateMatches(textualDate, agoPhrase) }
            }
            .map { it.key }
            .findFirst()
            .orElseThrow { ParsingException("Unable to parse the date: $textualDate") }
    }

    private fun textualDateMatches(textualDate: String, agoPhrase: String): Boolean {
        if (textualDate == agoPhrase) return true
        if (patternsHolder.wordSeparator().isEmpty()) return textualDate.lowercase(Locale.getDefault()).contains(agoPhrase.lowercase(Locale.getDefault()))

        val escapedPhrase = Pattern.quote(agoPhrase.lowercase(Locale.getDefault()))
        val escapedSeparator =
            if (patternsHolder.wordSeparator() == " ") "[ \\t\\xA0\\u1680\\u180e\\u2000-\\u200a\\u202f\\u205f\\u3000\\d]"
            else Pattern.quote(patternsHolder.wordSeparator())

        // (^|separator)pattern($|separator)
        // Check if the pattern is surrounded by separators or start/end of the string.
        val pattern = "(^|$escapedSeparator)$escapedPhrase($|$escapedSeparator)"
        return isMatch(pattern, textualDate.lowercase(Locale.getDefault()))
    }

    private fun getResultFor(timeAgoAmount: Int, chronoUnit: ChronoUnit): DateWrapper {
        var offsetDateTime = now
        var isApproximation = false

        when (chronoUnit) {
            ChronoUnit.SECONDS, ChronoUnit.MINUTES, ChronoUnit.HOURS -> offsetDateTime = offsetDateTime.minus(timeAgoAmount.toLong(), chronoUnit)
            ChronoUnit.DAYS, ChronoUnit.WEEKS, ChronoUnit.MONTHS -> {
                offsetDateTime = offsetDateTime.minus(timeAgoAmount.toLong(), chronoUnit)
                isApproximation = true
            }
            ChronoUnit.YEARS -> {
                // minusDays is needed to prevent `PrettyTime` from showing '12 months ago'.
                offsetDateTime = offsetDateTime.minusYears(timeAgoAmount.toLong()).minusDays(1)
                isApproximation = true
            }
            else -> {}
        }
        if (isApproximation) offsetDateTime = offsetDateTime.truncatedTo(ChronoUnit.HOURS)
        return DateWrapper(offsetDateTime, isApproximation)
    }

    companion object {
        private val DURATION_PATTERN: Pattern = Pattern.compile("(?:(\\d+) )?([A-z]+)")
    }
}
