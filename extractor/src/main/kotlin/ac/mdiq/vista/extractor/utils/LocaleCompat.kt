package ac.mdiq.vista.extractor.utils

import java.util.*

/**
 * This class contains a simple implementation of [Locale.forLanguageTag] for Android
 * API levels below 21 (Lollipop). This is needed as core library desugaring does not backport that
 * method as of this writing.
 * <br></br>
 * Relevant issue: https://issuetracker.google.com/issues/171182330
 */
object LocaleCompat {
    // Source: The AndroidX LocaleListCompat class's private forLanguageTagCompat() method.
    // Use Locale.forLanguageTag() on Android API level >= 21 / Java instead.

    fun forLanguageTag(str: String): Locale? {
        when {
            str.contains("-") -> {
                val args = str.split("-".toRegex()).toTypedArray()
                when {
                    args.size > 2 -> return Locale(args[0], args[1], args[2])
                    args.size > 1 -> return Locale(args[0], args[1])
                    args.size == 1 -> return Locale(args[0])
                }
            }
            str.contains("_") -> {
                val args = str.split("_".toRegex()).toTypedArray()
                when {
                    args.size > 2 -> return Locale(args[0], args[1], args[2])
                    args.size > 1 -> return Locale(args[0], args[1])
                    args.size == 1 -> return Locale(args[0])
                }
            }
            else -> return Locale(str)
        }
        return null
    }
}
