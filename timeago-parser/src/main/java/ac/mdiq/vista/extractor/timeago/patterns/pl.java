/**/// DO NOT MODIFY THIS FILE MANUALLY
/**/// This class was automatically generated by "GeneratePatternClasses.java",
/**/// modify the "unique_patterns.json" and re-generate instead.

package ac.mdiq.vista.extractor.timeago.patterns;

import ac.mdiq.vista.extractor.timeago.PatternsHolder;

public class pl extends PatternsHolder {
    private static final String WORD_SEPARATOR = " ";
    private static final String[]
            SECONDS  /**/ = {"sekund", "sekundy", "sekundę"},
            MINUTES  /**/ = {"minut", "minuty", "minutę"},
            HOURS    /**/ = {"godzin", "godziny", "godzinę"},
            DAYS     /**/ = {"dni", "dzień"},
            WEEKS    /**/ = {"tydzień", "tygodnie"},
            MONTHS   /**/ = {"miesiąc", "miesiące", "miesięcy"},
            YEARS    /**/ = {"lat", "lata", "rok"};

    private static final pl INSTANCE = new pl();

    public static pl getInstance() {
        return INSTANCE;
    }

    private pl() {
        super(WORD_SEPARATOR, SECONDS, MINUTES, HOURS, DAYS, WEEKS, MONTHS, YEARS);
    }
}
