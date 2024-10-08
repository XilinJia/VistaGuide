/**/// DO NOT MODIFY THIS FILE MANUALLY
/**/// This class was automatically generated by "GeneratePatternClasses.java",
/**/// modify the "unique_patterns.json" and re-generate instead.

package ac.mdiq.vista.extractor.timeago.patterns;

import ac.mdiq.vista.extractor.timeago.PatternsHolder;

public class el extends PatternsHolder {
    private static final String WORD_SEPARATOR = " ";
    private static final String[]
            SECONDS  /**/ = {"δευτερόλεπτα", "δευτερόλεπτο"},
            MINUTES  /**/ = {"λεπτά", "λεπτό"},
            HOURS    /**/ = {"ώρα", "ώρες"},
            DAYS     /**/ = {"ημέρα", "ημέρες"},
            WEEKS    /**/ = {"εβδομάδα", "εβδομάδες"},
            MONTHS   /**/ = {"μήνα", "μήνες"},
            YEARS    /**/ = {"χρόνια", "χρόνο"};

    private static final el INSTANCE = new el();

    public static el getInstance() {
        return INSTANCE;
    }

    private el() {
        super(WORD_SEPARATOR, SECONDS, MINUTES, HOURS, DAYS, WEEKS, MONTHS, YEARS);
    }
}
