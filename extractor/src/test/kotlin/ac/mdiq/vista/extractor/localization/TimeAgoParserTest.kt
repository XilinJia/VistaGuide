package ac.mdiq.vista.extractor.localization

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.localization.TimeAgoPatternsManager.getTimeAgoParserFor

internal class TimeAgoParserTest {
    @Test
    @Throws(ParsingException::class)
    fun testGetDuration() {
        Assertions.assertEquals(1, timeAgoParser!!.parseDuration("one second"))
        Assertions.assertEquals(1, timeAgoParser!!.parseDuration("second"))
        Assertions.assertEquals(49, timeAgoParser!!.parseDuration("49 seconds"))
        Assertions.assertEquals(61, timeAgoParser!!.parseDuration("1 minute, 1 second"))
    }

    @Test
    fun testGetDurationError() {
        Assertions.assertThrows(ParsingException::class.java
        ) { timeAgoParser!!.parseDuration("abcd") }
        Assertions.assertThrows(ParsingException::class.java
        ) { timeAgoParser!!.parseDuration("12 abcd") }
    }

    companion object {
        private var timeAgoParser: TimeAgoParser? = null

        @BeforeAll
        fun setUp() {
            timeAgoParser = getTimeAgoParserFor(Localization.DEFAULT)
        }
    }
}
