/**/// DO NOT MODIFY THIS FILE MANUALLY
/**/// This class was automatically generated by "GeneratePatternClasses.java",
/**/// modify the "unique_patterns.json" and re-generate instead.

package ac.mdiq.vista.extractor.timeago.patterns;

import ac.mdiq.vista.extractor.timeago.PatternsHolder;

public class ur extends PatternsHolder {
    private static final String WORD_SEPARATOR = " ";
    private static final String[]
            SECONDS  /**/ = {"سیکنڈ", "سیکنڈز"},
            MINUTES  /**/ = {"منٹ", "منٹس"},
            HOURS    /**/ = {"گھنٹہ", "گھنٹے"},
            DAYS     /**/ = {"دن"},
            WEEKS    /**/ = {"ہفتہ", "ہفتے"},
            MONTHS   /**/ = {"ماہ"},
            YEARS    /**/ = {"سال"};

    private static final ur INSTANCE = new ur();

    public static ur getInstance() {
        return INSTANCE;
    }

    private ur() {
        super(WORD_SEPARATOR, SECONDS, MINUTES, HOURS, DAYS, WEEKS, MONTHS, YEARS);
    }
}
