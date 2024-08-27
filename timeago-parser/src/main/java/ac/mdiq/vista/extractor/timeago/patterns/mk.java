/**/// DO NOT MODIFY THIS FILE MANUALLY
/**/// This class was automatically generated by "GeneratePatternClasses.java",
/**/// modify the "unique_patterns.json" and re-generate instead.

package ac.mdiq.vista.extractor.timeago.patterns;

import ac.mdiq.vista.extractor.timeago.PatternsHolder;

public class mk extends PatternsHolder {
    private static final String WORD_SEPARATOR = " ";
    private static final String[]
            SECONDS  /**/ = {"секунда", "секунди"},
            MINUTES  /**/ = {"минута", "минути"},
            HOURS    /**/ = {"час", "часа"},
            DAYS     /**/ = {"ден", "дена"},
            WEEKS    /**/ = {"недела", "недели"},
            MONTHS   /**/ = {"месец", "месеци"},
            YEARS    /**/ = {"година", "години"};

    private static final mk INSTANCE = new mk();

    public static mk getInstance() {
        return INSTANCE;
    }

    private mk() {
        super(WORD_SEPARATOR, SECONDS, MINUTES, HOURS, DAYS, WEEKS, MONTHS, YEARS);
    }
}
