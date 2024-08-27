package ac.mdiq.vista.extractor.timeago

import java.time.temporal.ChronoUnit
import java.util.*

abstract class PatternsHolder protected constructor(private val wordSeparator: String,
                                                    private val seconds: Collection<String>,
                                                    private val minutes: Collection<String>,
                                                    private val hours: Collection<String>,
                                                    private val days: Collection<String>,
                                                    private val weeks: Collection<String>,
                                                    private val months: Collection<String>,
                                                    private val years: Collection<String>) {
    private val specialCases: MutableMap<ChronoUnit, MutableMap<String, Int>> = EnumMap(ChronoUnit::class.java)

    protected constructor(wordSeparator: String,
                          seconds: Array<String?>,
                          minutes: Array<String?>,
                          hours: Array<String?>,
                          days: Array<String?>,
                          weeks: Array<String?>,
                          months: Array<String?>,
                          years: Array<String?>)
            : this(wordSeparator,
        Arrays.asList<String>(*seconds),
        Arrays.asList<String>(*minutes),
        Arrays.asList<String>(*hours),
        Arrays.asList<String>(*days),
        Arrays.asList<String>(*weeks),
        Arrays.asList<String>(*months),
        Arrays.asList<String>(*years))

    fun wordSeparator(): String {
        return wordSeparator
    }

    fun seconds(): Collection<String> {
        return seconds
    }

    fun minutes(): Collection<String> {
        return minutes
    }

    fun hours(): Collection<String> {
        return hours
    }

    fun days(): Collection<String> {
        return days
    }

    fun weeks(): Collection<String> {
        return weeks
    }

    fun months(): Collection<String> {
        return months
    }

    fun years(): Collection<String> {
        return years
    }

    fun specialCases(): Map<ChronoUnit, MutableMap<String, Int>> {
        return specialCases
    }

    protected fun putSpecialCase(unit: ChronoUnit, caseText: String, caseAmount: Int) {
        val item = specialCases.computeIfAbsent(unit) { k: ChronoUnit? -> LinkedHashMap() }
        item[caseText] = caseAmount
    }

    fun asMap(): Map<ChronoUnit, Collection<String>> {
        val returnMap: MutableMap<ChronoUnit, Collection<String>> = EnumMap(ChronoUnit::class.java)
        returnMap[ChronoUnit.SECONDS] = seconds()
        returnMap[ChronoUnit.MINUTES] = minutes()
        returnMap[ChronoUnit.HOURS] = hours()
        returnMap[ChronoUnit.DAYS] = days()
        returnMap[ChronoUnit.WEEKS] = weeks()
        returnMap[ChronoUnit.MONTHS] = months()
        returnMap[ChronoUnit.YEARS] = years()

        return returnMap
    }
}
