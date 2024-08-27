object GenerateOverview {
    @Throws(java.lang.Exception::class)
    
    fun main(args: Array<String>) {
        val outMap: MutableMap<String, MutableMap<String, MutableCollection<String>>> =
            TreeMap<String, Map<String, Collection<String>>>(object : java.util.Comparator<String?> {
                override fun compare(o1: String, o2: String): Int {
                    return o1.compareTo(o2, ignoreCase = true)
                }
            })
        for (unitName: String in mutableListOf<String>("seconds", "minutes", "hours", "days", "weeks", "months", "years")) {
            val `object`: com.grack.nanojson.JsonObject = com.grack.nanojson.JsonParser.`object`()
                .from(FileInputStream(File("timeago-parser/raw/times/" + unitName + ".json")))

            for (timeKeyValue: Map.Entry<String?, Any> in `object`.entries) {
                val timeObject: com.grack.nanojson.JsonObject = timeKeyValue.value as com.grack.nanojson.JsonObject
                for (langsKeyValue: Map.Entry<String, Any> in timeObject.entries) {
                    val langKey: String = langsKeyValue.key
                    val langValue: String = langsKeyValue.value.toString()

                    var langUnitsMap: MutableMap<String, MutableCollection<String>>
                    if (outMap.containsKey(langKey)) {
                        langUnitsMap = outMap.get(langKey)!!
                    } else {
                        langUnitsMap = TreeMap<Any?, Any?>(Utils.compareByUnitName())
                        outMap.put(langKey, langUnitsMap)
                    }

                    var langUnitListValues: MutableCollection<String>
                    if (langUnitsMap.containsKey(unitName)) {
                        langUnitListValues = langUnitsMap.get(unitName)!!
                    } else {
                        langUnitListValues = TreeSet<Any?>(Utils.compareByNumber())
                        langUnitsMap.put(unitName, langUnitListValues)
                    }

                    langUnitListValues.add(langValue)
                }
            }
        }

        writeMapTo(outMap, com.grack.nanojson.JsonWriter.indent("  ").on(FileOutputStream(File("timeago-parser/raw/overview.json"))))
    }

    fun writeMapTo(outMap: Map<String, MutableMap<String, MutableCollection<String>>>, out: JsonAppendableWriter) {
        out.`object`()
        for (langMapEntry: Map.Entry<String, Map<String, Collection<String>>> in outMap.entries) {
            val langName: String = langMapEntry.key
            out.`object`(langName)
            for (langValuesEntry: Map.Entry<String, Collection<String>> in langMapEntry.value.entries) {
                val unitName: String = langValuesEntry.key
                out.array(unitName)
                for (timeValue: String? in langValuesEntry.value) out.value(timeValue)
                out.end()
            }
            out.end()
        }
        out.end().done()
    }
}