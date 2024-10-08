/**/// DO NOT MODIFY THIS FILE MANUALLY
/**/// This class was automatically generated by "GeneratePatternClasses.java",
/**/// modify the "unique_patterns.json" and re-generate instead.

package ac.mdiq.vista.extractor.timeago.patterns;

import ac.mdiq.vista.extractor.timeago.PatternsHolder;

public class sv extends PatternsHolder {
    private static final String WORD_SEPARATOR = " ";
    private static final String[]
            SECONDS  /**/ = {"sekund", "sekunder"},
            MINUTES  /**/ = {"minut", "minuter"},
            HOURS    /**/ = {"timmar", "timme"},
            DAYS     /**/ = {"dag", "dagar"},
            WEEKS    /**/ = {"vecka", "veckor"},
            MONTHS   /**/ = {"månad", "månader"},
            YEARS    /**/ = {"år"};

    private static final sv INSTANCE = new sv();

    public static sv getInstance() {
        return INSTANCE;
    }

    private sv() {
        super(WORD_SEPARATOR, SECONDS, MINUTES, HOURS, DAYS, WEEKS, MONTHS, YEARS);
    }
}
