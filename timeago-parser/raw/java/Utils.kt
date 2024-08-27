object Utils {
    fun compareByNumber(): java.util.Comparator<String> {
        return object : java.util.Comparator<String?> {
            override fun compare(o1: String, o2: String): Int {
                return extractInt(o1) - extractInt(o2)
            }

            fun extractInt(s: String): Int {
                val num: String = s.replace("\\D".toRegex(), "")
                return if (num.isEmpty()) 0 else num.toInt()
            }
        }
    }

    fun compareByUnitName(): java.util.Comparator<Any> {
        return object : java.util.Comparator<Any?> {
            private val ORDER: List<String> =
                mutableListOf<String>("seconds", "minutes", "hours", "days", "weeks", "months", "years")

            override fun compare(o1: Any, o2: Any): Int {
                return java.lang.Integer.compare(ORDER.indexOf(o1.toString()), ORDER.indexOf(o2.toString()))
            }
        }
    }
}