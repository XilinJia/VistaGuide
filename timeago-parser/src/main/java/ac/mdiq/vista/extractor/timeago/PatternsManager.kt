package ac.mdiq.vista.extractor.timeago

import java.lang.reflect.InvocationTargetException

object PatternsManager {
    /**
     * Return an holder object containing all the patterns array.
     *
     * @return an object containing the patterns. If not existent, `null`.
     */

    fun getPatterns(languageCode: String, countryCode: String?): PatternsHolder? {
        val targetLocalizationClassName = languageCode + (if (countryCode == null || countryCode.isEmpty()) "" else "_$countryCode")
        try {
            val targetClass = Class.forName("ac.mdiq.vista.extractor.timeago.patterns.$targetLocalizationClassName")

            return targetClass.getDeclaredMethod("getInstance").invoke(null) as PatternsHolder
        } catch (ignored: ClassNotFoundException) {
            // Target localization is not supported
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        }

        return null
    }
}
