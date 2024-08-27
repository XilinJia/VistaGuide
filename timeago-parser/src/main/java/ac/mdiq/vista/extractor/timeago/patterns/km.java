/**/// DO NOT MODIFY THIS FILE MANUALLY
/**/// This class was automatically generated by "GeneratePatternClasses.java",
/**/// modify the "unique_patterns.json" and re-generate instead.

package ac.mdiq.vista.extractor.timeago.patterns;

import ac.mdiq.vista.extractor.timeago.PatternsHolder;

public class km extends PatternsHolder {
    private static final String WORD_SEPARATOR = "";
    private static final String[]
            SECONDS  /**/ = {"វិនាទី​មុន", "១វិនាទីមុន"},
            MINUTES  /**/ = {"នាទីមុន", "១នាទីមុន"},
            HOURS    /**/ = {"ម៉ោង​មុន", "១ម៉ោង​មុន"},
            DAYS     /**/ = {"ថ្ងៃមុន", "១ថ្ងៃ​មុន"},
            WEEKS    /**/ = {"ស​ប្តា​ហ៍​មុន", "១ស​ប្តា​ហ៍​មុន"},
            MONTHS   /**/ = {"ខែមុន", "១ខែមុន"},
            YEARS    /**/ = {"ឆ្នាំ​មុន", "១ឆ្នាំមុន"};

    private static final km INSTANCE = new km();

    public static km getInstance() {
        return INSTANCE;
    }

    private km() {
        super(WORD_SEPARATOR, SECONDS, MINUTES, HOURS, DAYS, WEEKS, MONTHS, YEARS);
    }
}
