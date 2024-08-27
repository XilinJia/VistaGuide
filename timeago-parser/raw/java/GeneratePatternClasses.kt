import ac.mdiq.vista.extractor.timeago.TimeAgoUnit

object GeneratePatternClasses {
    @Throws(FileNotFoundException::class, JsonParserException::class)

    fun main(args: Array<String>) {
        val resourceAsStream: java.io.InputStream = FileInputStream("timeago-parser/raw/unique_patterns.json")

        val from: com.grack.nanojson.JsonObject = com.grack.nanojson.JsonParser.`object`().from(resourceAsStream)
        val map: TreeMap<String, Any> = TreeMap<String, Any>(from)

        for (entry: Map.Entry<String, Any> in map.entries) {
            val languageCode: String = entry.key.replace('-', '_')
            val unitsList: Map<String, Any> = entry.value as Map<String, Any>

            val wordSeparator: String? = unitsList.get("word_separator") as String?

            val seconds: com.grack.nanojson.JsonArray? = unitsList.get("seconds") as com.grack.nanojson.JsonArray?
            val minutes: com.grack.nanojson.JsonArray? = unitsList.get("minutes") as com.grack.nanojson.JsonArray?
            val hours: com.grack.nanojson.JsonArray? = unitsList.get("hours") as com.grack.nanojson.JsonArray?
            val days: com.grack.nanojson.JsonArray? = unitsList.get("days") as com.grack.nanojson.JsonArray?
            val weeks: com.grack.nanojson.JsonArray? = unitsList.get("weeks") as com.grack.nanojson.JsonArray?
            val months: com.grack.nanojson.JsonArray? = unitsList.get("months") as com.grack.nanojson.JsonArray?
            val years: com.grack.nanojson.JsonArray? = unitsList.get("years") as com.grack.nanojson.JsonArray?

            val specialCasesString: java.lang.StringBuilder = java.lang.StringBuilder()
            specialCasesConstruct(TimeAgoUnit.SECONDS, seconds, specialCasesString)
            specialCasesConstruct(TimeAgoUnit.MINUTES, minutes, specialCasesString)
            specialCasesConstruct(TimeAgoUnit.HOURS, hours, specialCasesString)
            specialCasesConstruct(TimeAgoUnit.DAYS, days, specialCasesString)
            specialCasesConstruct(TimeAgoUnit.WEEKS, weeks, specialCasesString)
            specialCasesConstruct(TimeAgoUnit.MONTHS, months, specialCasesString)
            specialCasesConstruct(TimeAgoUnit.YEARS, years, specialCasesString)

            println("Generating \"" + languageCode + "\" pattern class...")

            try {
                FileWriter("timeago-parser/src/main/java/ac/mdiq/vista/extractor/timeago/patterns/" + languageCode + ".kt").use { fileOut ->
                    val test: String = INFO_CLASS_GENERATED + "\n" +
                            "\n" +
                            "package ac.mdiq.vista.extractor.timeago.patterns;\n\n" +
                            "import ac.mdiq.vista.extractor.timeago.PatternsHolder;\n" +
                            (if (specialCasesString.length > 0) "import ac.mdiq.vista.extractor.timeago.TimeAgoUnit;\n" else "") +
                            "\n" +
                            "object " + languageCode + " : PatternsHolder() {\n" +
                            "    private const val WORD_SEPARATOR = \"" + wordSeparator + "\"\n" +
                            "    private val SECONDS  /**/ = arrayOf(" + join(seconds) + ")\n" +
                            "    private val MINUTES  /**/ = arrayOf(" + join(minutes) + ")\n" +
                            "    private val HOURS    /**/ = arrayOf(" + join(hours) + ")\n" +
                            "    private val DAYS     /**/ = arrayOf(" + join(days) + ")\n" +
                            "    private val WEEKS    /**/ = arrayOf(" + join(weeks) + ")\n" +
                            "    private val MONTHS   /**/ = arrayOf(" + join(months) + ")\n" +
                            "    private val YEARS    /**/ = arrayOf(" + join(years) + ")\n" +
                            "\n" +
                            "    val instance: " + languageCode " = " + languageCode + "()\n" +
                            "}"
                    fileOut.write(test)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun specialCasesConstruct(unit: TimeAgoUnit, array: com.grack.nanojson.JsonArray, stringBuilder: java.lang.StringBuilder) {
        val iterator: MutableIterator<Any> = array.iterator()
        while (iterator.hasNext()) {
            val o: Any = iterator.next()
            if (o is com.grack.nanojson.JsonObject) {
                val caseObject: com.grack.nanojson.JsonObject = o as com.grack.nanojson.JsonObject
                for (caseEntry: Map.Entry<String, Any> in caseObject.entries) {
                    val caseAmount: Int = caseEntry.key.toInt()
                    val caseText: String = caseEntry.value as String
                    iterator.remove()

                    stringBuilder.append("        ")
                        .append("putSpecialCase(TimeAgoUnit.").append(unit.name())
                        .append(", \"").append(caseText).append("\"")
                        .append(", ").append(caseAmount).append(");").append("\n")
                }
            }
        }
    }

    private val INFO_CLASS_GENERATED: String = "/**/// DO NOT MODIFY THIS FILE MANUALLY\n" +
            "/**/// This class was automatically generated by \"GeneratePatternClasses.java\",\n" +
            "/**/// modify the \"unique_patterns.json\" and re-generate instead."

    private fun join(list: List<Any>): String {
        val toReturn: java.lang.StringBuilder = java.lang.StringBuilder()

        for (o: Any in list) {
            toReturn.append('"').append(o).append('"').append(", ")
        }
        toReturn.setLength(max(toReturn.length - 2, 0))

        return toReturn.toString()
    }
}
