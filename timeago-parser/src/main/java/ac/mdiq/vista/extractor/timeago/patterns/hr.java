/**/// DO NOT MODIFY THIS FILE MANUALLY
/**/// This class was automatically generated by "GeneratePatternClasses.java",
/**/// modify the "unique_patterns.json" and re-generate instead.

package ac.mdiq.vista.extractor.timeago.patterns;

import ac.mdiq.vista.extractor.timeago.PatternsHolder;

public class hr extends PatternsHolder {
    private static final String WORD_SEPARATOR = " ";
    private static final String[]
            SECONDS  /**/ = {"sekunde", "sekundi", "sekundu"},
            MINUTES  /**/ = {"minuta", "minute", "minutu"},
            HOURS    /**/ = {"sat", "sata", "sati"},
            DAYS     /**/ = {"dan", "dana"},
            WEEKS    /**/ = {"tjedan", "tjedna"},
            MONTHS   /**/ = {"mjesec", "mjeseca", "mjeseci"},
            YEARS    /**/ = {"godina", "godine", "godinu"};

    private static final hr INSTANCE = new hr();

    public static hr getInstance() {
        return INSTANCE;
    }

    private hr() {
        super(WORD_SEPARATOR, SECONDS, MINUTES, HOURS, DAYS, WEEKS, MONTHS, YEARS);
    }
}
