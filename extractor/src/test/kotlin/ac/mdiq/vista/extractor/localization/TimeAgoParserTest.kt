
import TimeAgoParserTest.ParseTimeAgoTestData.Companion.greaterThanDay
import TimeAgoParserTest.ParseTimeAgoTestData.Companion.lessThanDay
import ac.mdiq.vista.extractor.localization.Localization
import ac.mdiq.vista.extractor.localization.TimeAgoParser
import ac.mdiq.vista.extractor.localization.TimeAgoPatternsManager.getTimeAgoParserFor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.time.Duration
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import java.util.*
import java.util.stream.Stream

internal class TimeAgoParserTest {
    @ParameterizedTest
    @MethodSource
    fun parseTimeAgo(testData: ParseTimeAgoTestData) {
        val now = OffsetDateTime.of(LocalDateTime.of(2020, 1, 1, 1, 1, 1), ZoneOffset.UTC)
        val parser = Objects.requireNonNull<TimeAgoParser?>(getTimeAgoParserFor(Localization.DEFAULT, now))

        val expected: OffsetDateTime? = testData.expectedApplyToNow.invoke(now)

        assertAll(
            Stream.of(testData.textualDateLong, testData.textualDateShort)
                .map { textualDate -> { assertEquals(expected, parser.parse(textualDate).offsetDateTime(), "Expected " + expected + " for " + textualDate) } }
        )
    }

    internal class ParseTimeAgoTestData(
            val expectedApplyToNow: (OffsetDateTime) -> OffsetDateTime,
            val textualDateLong: String?,
            val textualDateShort: String?) {

//        fun getExpectedApplyToNow(): (OffsetDateTime) -> OffsetDateTime {
//            return expectedApplyToNow
//        }

        companion object {
            const val AGO_SUFFIX: String = " ago"
            fun lessThanDay(duration: Duration?, textualDateLong: String?, textualDateShort: String?): ParseTimeAgoTestData {
                return ParseTimeAgoTestData({ d -> d.minus(duration) }, textualDateLong + AGO_SUFFIX, textualDateShort + AGO_SUFFIX)
            }

            fun greaterThanDay(expectedApplyToNow: (OffsetDateTime) -> OffsetDateTime, textualDateLong: String?, textualDateShort: String?): ParseTimeAgoTestData {
                return ParseTimeAgoTestData({ d -> expectedApplyToNow(d).truncatedTo(ChronoUnit.HOURS) }, textualDateLong + AGO_SUFFIX, textualDateShort + AGO_SUFFIX)
            }
        }
    }

    companion object {
        @JvmStatic
        fun parseTimeAgo(): Stream<Arguments?> {
            return Stream.of(
                lessThanDay(Duration.ofSeconds(1), "1 second", "1 sec"),
                lessThanDay(Duration.ofSeconds(12), "12 second", "12 sec"),
                lessThanDay(Duration.ofMinutes(1), "1 minute", "1 min"),
                lessThanDay(Duration.ofMinutes(23), "23 minutes", "23 min"),
                lessThanDay(Duration.ofHours(1), "1 hour", "1 hr"),
                lessThanDay(Duration.ofHours(8), "8 hour", "8 hr"),
                greaterThanDay({ d -> d.minusDays(1) }, "1 day", "1 day"),
                greaterThanDay({ d -> d.minusDays(3) }, "3 days", "3 day"),
                greaterThanDay({ d -> d.minusWeeks(1) }, "1 week", "1 wk"),
                greaterThanDay({ d -> d.minusWeeks(3) }, "3 weeks", "3 wk"),
                greaterThanDay({ d -> d.minusMonths(1) }, "1 month", "1 mo"),
                greaterThanDay({ d -> d.minusMonths(3) }, "3 months", "3 mo"),
                greaterThanDay({ d -> d.minusYears(1).minusDays(1) }, "1 year", "1 yr"),
                greaterThanDay({ d -> d.minusYears(3).minusDays(1) }, "3 years", "3 yr")
            ).map(Arguments::of)
        }
    }
}