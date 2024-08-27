package ac.mdiq.vista.extractor.utils

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.localization.Localization
import ac.mdiq.vista.extractor.localization.TimeAgoParser
import ac.mdiq.vista.extractor.localization.TimeAgoPatternsManager.getTimeAgoParserFor
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import kotlin.math.abs

class TimeagoTest {
    @Test
    @Throws(ParsingException::class)
    fun parseTimeago() {
        assertTimeWithin1s(
            now!!.minus(1, ChronoUnit.SECONDS),
            parser!!.parse("1 second ago").offsetDateTime()
        )
        assertTimeWithin1s(
            now!!.minus(12, ChronoUnit.SECONDS),
            parser!!.parse("12 second ago").offsetDateTime()
        )
        assertTimeWithin1s(
            now!!.minus(1, ChronoUnit.MINUTES),
            parser!!.parse("1 minute ago").offsetDateTime()
        )
        assertTimeWithin1s(
            now!!.minus(23, ChronoUnit.MINUTES),
            parser!!.parse("23 minutes ago").offsetDateTime()
        )
        assertTimeWithin1s(
            now!!.minus(1, ChronoUnit.HOURS),
            parser!!.parse("1 hour ago").offsetDateTime()
        )
        assertTimeWithin1s(
            now!!.minus(8, ChronoUnit.HOURS),
            parser!!.parse("8 hours ago").offsetDateTime()
        )
        Assertions.assertEquals(
            now!!.minus(1, ChronoUnit.DAYS).truncatedTo(ChronoUnit.HOURS),
            parser!!.parse("1 day ago").offsetDateTime()
        )
        Assertions.assertEquals(
            now!!.minus(3, ChronoUnit.DAYS).truncatedTo(ChronoUnit.HOURS),
            parser!!.parse("3 days ago").offsetDateTime()
        )
        Assertions.assertEquals(
            now!!.minus(1, ChronoUnit.WEEKS).truncatedTo(ChronoUnit.HOURS),
            parser!!.parse("1 week ago").offsetDateTime()
        )
        Assertions.assertEquals(
            now!!.minus(3, ChronoUnit.WEEKS).truncatedTo(ChronoUnit.HOURS),
            parser!!.parse("3 weeks ago").offsetDateTime()
        )
        Assertions.assertEquals(
            now!!.minus(1, ChronoUnit.MONTHS).truncatedTo(ChronoUnit.HOURS),
            parser!!.parse("1 month ago").offsetDateTime()
        )
        Assertions.assertEquals(
            now!!.minus(3, ChronoUnit.MONTHS).truncatedTo(ChronoUnit.HOURS),
            parser!!.parse("3 months ago").offsetDateTime()
        )
        Assertions.assertEquals(
            now!!.minus(1, ChronoUnit.YEARS).minusDays(1).truncatedTo(ChronoUnit.HOURS),
            parser!!.parse("1 year ago").offsetDateTime()
        )
        Assertions.assertEquals(
            now!!.minus(3, ChronoUnit.YEARS).minusDays(1).truncatedTo(ChronoUnit.HOURS),
            parser!!.parse("3 years ago").offsetDateTime()
        )
    }

    @Test
    @Throws(ParsingException::class)
    fun parseTimeagoShort() {
        val parser = getTimeAgoParserFor(Localization.DEFAULT)
        val now = OffsetDateTime.now(ZoneOffset.UTC)

        assertTimeWithin1s(
            now.minus(1, ChronoUnit.SECONDS),
            parser!!.parse("1 sec ago").offsetDateTime()
        )
        assertTimeWithin1s(
            now.minus(12, ChronoUnit.SECONDS),
            parser.parse("12 sec ago").offsetDateTime()
        )
        assertTimeWithin1s(
            now.minus(1, ChronoUnit.MINUTES),
            parser.parse("1 min ago").offsetDateTime()
        )
        assertTimeWithin1s(
            now.minus(23, ChronoUnit.MINUTES),
            parser.parse("23 min ago").offsetDateTime()
        )
        assertTimeWithin1s(
            now.minus(1, ChronoUnit.HOURS),
            parser.parse("1 hr ago").offsetDateTime()
        )
        assertTimeWithin1s(
            now.minus(8, ChronoUnit.HOURS),
            parser.parse("8 hr ago").offsetDateTime()
        )
        Assertions.assertEquals(
            now.minus(1, ChronoUnit.DAYS).truncatedTo(ChronoUnit.HOURS),
            parser.parse("1 day ago").offsetDateTime()
        )
        Assertions.assertEquals(
            now.minus(3, ChronoUnit.DAYS).truncatedTo(ChronoUnit.HOURS),
            parser.parse("3 days ago").offsetDateTime()
        )
        Assertions.assertEquals(
            now.minus(1, ChronoUnit.WEEKS).truncatedTo(ChronoUnit.HOURS),
            parser.parse("1 wk ago").offsetDateTime()
        )
        Assertions.assertEquals(
            now.minus(3, ChronoUnit.WEEKS).truncatedTo(ChronoUnit.HOURS),
            parser.parse("3 wk ago").offsetDateTime()
        )
        Assertions.assertEquals(
            now.minus(1, ChronoUnit.MONTHS).truncatedTo(ChronoUnit.HOURS),
            parser.parse("1 mo ago").offsetDateTime()
        )
        Assertions.assertEquals(
            now.minus(3, ChronoUnit.MONTHS).truncatedTo(ChronoUnit.HOURS),
            parser.parse("3 mo ago").offsetDateTime()
        )
        Assertions.assertEquals(
            now.minus(1, ChronoUnit.YEARS).minusDays(1).truncatedTo(ChronoUnit.HOURS),
            parser.parse("1 yr ago").offsetDateTime()
        )
        Assertions.assertEquals(
            now.minus(3, ChronoUnit.YEARS).minusDays(1).truncatedTo(ChronoUnit.HOURS),
            parser.parse("3 yr ago").offsetDateTime()
        )
    }

    fun assertTimeWithin1s(expected: OffsetDateTime, actual: OffsetDateTime) {
        val delta = abs((expected.toEpochSecond() - actual.toEpochSecond()).toDouble()).toLong()
        Assertions.assertTrue(delta <= 1, String.format("Expected: %s\nActual:   %s", expected, actual))
    }

    companion object {
        private var parser: TimeAgoParser? = null
        private var now: OffsetDateTime? = null

        @BeforeAll
        fun setUp() {
            parser = getTimeAgoParserFor(Localization.DEFAULT)
            now = OffsetDateTime.now(ZoneOffset.UTC)
        }
    }
}
