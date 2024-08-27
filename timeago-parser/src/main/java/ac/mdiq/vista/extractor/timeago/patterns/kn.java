/**/// DO NOT MODIFY THIS FILE MANUALLY
/**/// This class was automatically generated by "GeneratePatternClasses.java",
/**/// modify the "unique_patterns.json" and re-generate instead.

package ac.mdiq.vista.extractor.timeago.patterns;

import ac.mdiq.vista.extractor.timeago.PatternsHolder;

public class kn extends PatternsHolder {
    private static final String WORD_SEPARATOR = " ";
    private static final String[]
            SECONDS  /**/ = {"ಸೆಕೆಂಡುಗಳ", "ಸೆಕೆಂಡ್"},
            MINUTES  /**/ = {"ನಿಮಿಷಗಳ", "ನಿಮಿಷದ"},
            HOURS    /**/ = {"ಗಂಟೆಗಳ", "ಗಂಟೆಯ"},
            DAYS     /**/ = {"ದಿನಗಳ", "ದಿನದ"},
            WEEKS    /**/ = {"ವಾರಗಳ", "ವಾರದ"},
            MONTHS   /**/ = {"ತಿಂಗಳ", "ತಿಂಗಳುಗಳ"},
            YEARS    /**/ = {"ವರ್ಷಗಳ", "ವರ್ಷದ"};

    private static final kn INSTANCE = new kn();

    public static kn getInstance() {
        return INSTANCE;
    }

    private kn() {
        super(WORD_SEPARATOR, SECONDS, MINUTES, HOURS, DAYS, WEEKS, MONTHS, YEARS);
    }
}
