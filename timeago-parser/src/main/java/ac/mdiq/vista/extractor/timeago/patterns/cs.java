/**/// DO NOT MODIFY THIS FILE MANUALLY
/**/// This class was automatically generated by "GeneratePatternClasses.java",
/**/// modify the "unique_patterns.json" and re-generate instead.

package ac.mdiq.vista.extractor.timeago.patterns;

import ac.mdiq.vista.extractor.timeago.PatternsHolder;

public class cs extends PatternsHolder {
    private static final String WORD_SEPARATOR = " ";
    private static final String[]
            SECONDS  /**/ = {"sekundami", "sekundou"},
            MINUTES  /**/ = {"minutami", "minutou"},
            HOURS    /**/ = {"hodinami", "hodinou"},
            DAYS     /**/ = {"dny", "včera"},
            WEEKS    /**/ = {"týdnem", "týdny"},
            MONTHS   /**/ = {"měsícem", "měsíci"},
            YEARS    /**/ = {"rokem", "roky", "lety"};

    private static final cs INSTANCE = new cs();

    public static cs getInstance() {
        return INSTANCE;
    }

    private cs() {
        super(WORD_SEPARATOR, SECONDS, MINUTES, HOURS, DAYS, WEEKS, MONTHS, YEARS);
    }
}
