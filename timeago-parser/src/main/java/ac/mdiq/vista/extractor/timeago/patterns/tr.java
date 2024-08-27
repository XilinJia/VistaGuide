/**/// DO NOT MODIFY THIS FILE MANUALLY
/**/// This class was automatically generated by "GeneratePatternClasses.java",
/**/// modify the "unique_patterns.json" and re-generate instead.

package ac.mdiq.vista.extractor.timeago.patterns;

import ac.mdiq.vista.extractor.timeago.PatternsHolder;

public class tr extends PatternsHolder {
    private static final String WORD_SEPARATOR = " ";
    private static final String[]
            SECONDS  /**/ = {"saniye"},
            MINUTES  /**/ = {"dakika"},
            HOURS    /**/ = {"saat"},
            DAYS     /**/ = {"gün"},
            WEEKS    /**/ = {"hafta"},
            MONTHS   /**/ = {"ay"},
            YEARS    /**/ = {"yıl"};

    private static final tr INSTANCE = new tr();

    public static tr getInstance() {
        return INSTANCE;
    }

    private tr() {
        super(WORD_SEPARATOR, SECONDS, MINUTES, HOURS, DAYS, WEEKS, MONTHS, YEARS);
    }
}
