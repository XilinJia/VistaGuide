/**/// DO NOT MODIFY THIS FILE MANUALLY
/**/// This class was automatically generated by "GeneratePatternClasses.java",
/**/// modify the "unique_patterns.json" and re-generate instead.

package ac.mdiq.vista.extractor.timeago.patterns;

import ac.mdiq.vista.extractor.timeago.PatternsHolder;

public class ta extends PatternsHolder {
    private static final String WORD_SEPARATOR = " ";
    private static final String[]
            SECONDS  /**/ = {"வினாடி", "வினாடிகளுக்கு"},
            MINUTES  /**/ = {"நிமிடங்கள்", "நிமிடம்"},
            HOURS    /**/ = {"மணிநேரத்திற்கு"},
            DAYS     /**/ = {"நாட்களுக்கு", "நாளுக்கு"},
            WEEKS    /**/ = {"வாரங்களுக்கு", "வாரம்"},
            MONTHS   /**/ = {"மாதங்கள்", "மாதம்"},
            YEARS    /**/ = {"ஆண்டு", "ஆண்டுகளுக்கு"};

    private static final ta INSTANCE = new ta();

    public static ta getInstance() {
        return INSTANCE;
    }

    private ta() {
        super(WORD_SEPARATOR, SECONDS, MINUTES, HOURS, DAYS, WEEKS, MONTHS, YEARS);
    }
}
