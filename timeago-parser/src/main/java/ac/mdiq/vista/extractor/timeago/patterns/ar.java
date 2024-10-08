/**/// DO NOT MODIFY THIS FILE MANUALLY
/**/// This class was automatically generated by "GeneratePatternClasses.java",
/**/// modify the "unique_patterns.json" and re-generate instead.

package ac.mdiq.vista.extractor.timeago.patterns;

import ac.mdiq.vista.extractor.timeago.PatternsHolder;

public class ar extends PatternsHolder {
    private static final String WORD_SEPARATOR = " ";
    private static final String[]
            SECONDS  /**/ = {"ثانية", "ثانيتين", "ثوانٍ"},
            MINUTES  /**/ = {"دقائق", "دقيقة", "دقيقتين"},
            HOURS    /**/ = {"ساعات", "ساعة", "ساعتين"},
            DAYS     /**/ = {"أيام", "يوم", "يومين"},
            WEEKS    /**/ = {"أسابيع", "أسبوع", "أسبوعين"},
            MONTHS   /**/ = {"أشهر", "شهر", "شهرين", "شهرًا"},
            YEARS    /**/ = {"سنة", "سنتين", "سنوات"};

    private static final ar INSTANCE = new ar();

    public static ar getInstance() {
        return INSTANCE;
    }

    private ar() {
        super(WORD_SEPARATOR, SECONDS, MINUTES, HOURS, DAYS, WEEKS, MONTHS, YEARS);
    }
}
