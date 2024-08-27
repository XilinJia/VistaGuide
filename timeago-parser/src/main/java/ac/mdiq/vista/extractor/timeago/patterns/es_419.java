/**/// DO NOT MODIFY THIS FILE MANUALLY
/**/// This class was automatically generated by "GeneratePatternClasses.java",
/**/// modify the "unique_patterns.json" and re-generate instead.

package ac.mdiq.vista.extractor.timeago.patterns;

import ac.mdiq.vista.extractor.timeago.PatternsHolder;

public class es_419 extends PatternsHolder {
    private static final String WORD_SEPARATOR = " ";
    private static final String[]
            SECONDS  /**/ = {"segundo", "segundos"},
            MINUTES  /**/ = {"minuto", "minutos"},
            HOURS    /**/ = {"hora", "horas"},
            DAYS     /**/ = {"día", "días"},
            WEEKS    /**/ = {"semana", "semanas"},
            MONTHS   /**/ = {"mes", "meses"},
            YEARS    /**/ = {"año", "años"};

    private static final es_419 INSTANCE = new es_419();

    public static es_419 getInstance() {
        return INSTANCE;
    }

    private es_419() {
        super(WORD_SEPARATOR, SECONDS, MINUTES, HOURS, DAYS, WEEKS, MONTHS, YEARS);
    }
}
