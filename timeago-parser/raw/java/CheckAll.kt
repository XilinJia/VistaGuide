object CheckAll {
    @Throws(java.lang.Exception::class)
    
    fun main(args: Array<String>) {
        val SECONDS: Int = 59
        var currentSeconds: Int = 0
        val MINUTES: Int = 59
        var currentMinutes: Int = 0
        val HOURS: Int = 23
        var currentHours: Int = 0
        val DAYS: Int = 6
        var currentDays: Int = 0
        val WEEKS: Int = 4
        var currentWeeks: Int = 0
        val MONTHS: Int = 11
        var currentMonths: Int = 0
        val YEARS: Int = 12
        var currentYears: Int = 0

        for (name: String in mutableListOf<String>("seconds", "minutes", "hours", "days", "weeks", "months", "years")) {
            val `object`: com.grack.nanojson.JsonObject = com.grack.nanojson.JsonParser.`object`()
                .from(FileInputStream(File("timeago-parser/raw/times/" + name + ".json")))

            for (entry: Map.Entry<String, Any> in `object`.entries) {
                val value: com.grack.nanojson.JsonObject = entry.value as com.grack.nanojson.JsonObject

                val size: Int = value.keys.size
                if (size >= 80) {
                    if (name == "seconds") currentSeconds++
                    if (name == "minutes") currentMinutes++
                    if (name == "hours") currentHours++
                    if (name == "days") currentDays++
                    if (name == "weeks") currentWeeks++
                    if (name == "months") currentMonths++
                    if (name == "years") currentYears++
                } else {
                    java.lang.System.err.println("Missing some units in: " + name + " → " + entry.key + " (current size = " + size + ")")
                }

                val number: String = entry.key.replace("\\D".toRegex(), "")
                for (langsKeys: Map.Entry<String, Any> in value.entries) {
                    val lang: String = langsKeys.key
                    val langValue: String = langsKeys.value.toString()

                    val langValueNumber: String = langValue.replace("\\D".toRegex(), "")
                    if (langValueNumber != number) {
                        val msg: String = if (langValueNumber.isEmpty()) "doesn't contain number" else "different number"
                        java.lang.System.out.printf("%-20s[!]   %22s: %10s   = %s \n", entry.key, msg, lang, langValue)
                    }
                }
            }
        }
        println("\n\nHow many:\n")

        if (currentSeconds == SECONDS) println("seconds: " + currentSeconds)
        else println("[!] missing seconds: " + currentSeconds)

        if (currentMinutes == MINUTES) println("minutes: " + currentMinutes)
        else println("[!] missing minutes: " + currentMinutes)

        if (currentHours == HOURS) println("hours: " + currentHours)
        else println("[!] missing hours: " + currentHours)

        if (currentDays == DAYS) println("days: " + currentDays)
        else println("[!] missing days: " + currentDays)

        if (currentWeeks == WEEKS) println("weeks: " + currentWeeks)
        else println("[!] missing weeks: " + currentWeeks)

        if (currentMonths == MONTHS) println("months: " + currentMonths)
        else println("[!] missing months: " + currentMonths)

        if (currentYears == YEARS) println("years: " + currentYears)
        else println("[!] missing years: " + currentYears)
    }
}