package ac.mdiq.vista.extractor.localization

import java.io.Serializable
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.*



/**
 * A wrapper class that provides a field to describe if the date/time is precise or just an
 * approximation.
 */
class DateWrapper @JvmOverloads constructor(
        offsetDateTime: OffsetDateTime,
        /**
         * @return if the date is considered is precise or just an approximation (e.g. service only
         * returns an approximation like 2 weeks ago instead of a precise date).
         */
        val isApproximation: Boolean = false) : Serializable {


    private val offsetDateTime: OffsetDateTime = offsetDateTime.withOffsetSameInstant(ZoneOffset.UTC)


    @Deprecated("Use {@link #DateWrapper(OffsetDateTime)} instead.")
    constructor(calendar: Calendar) : this(calendar, false)


    @Deprecated("Use {@link #DateWrapper(OffsetDateTime, boolean)} instead.")
    constructor(calendar: Calendar,
                isApproximation: Boolean
    ) : this(OffsetDateTime.ofInstant(calendar.toInstant(), ZoneOffset.UTC), isApproximation)

    /**
     * @return the wrapped date/time as a [Calendar].
     */

    @Deprecated("use {@link #offsetDateTime()} instead.")
    fun date(): Calendar {
        return GregorianCalendar.from(offsetDateTime.toZonedDateTime())
    }

    /**
     * @return the wrapped date/time.
     */

    fun offsetDateTime(): OffsetDateTime {
        return offsetDateTime
    }
}
