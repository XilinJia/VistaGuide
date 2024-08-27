package ac.mdiq.vista.extractor.suggestion

import ac.mdiq.vista.extractor.StreamingService
import ac.mdiq.vista.extractor.exceptions.ExtractionException
import ac.mdiq.vista.extractor.localization.ContentCountry
import ac.mdiq.vista.extractor.localization.Localization
import java.io.IOException


abstract class SuggestionExtractor(val service: StreamingService) {

    private var forcedLocalization: Localization? = null
    private var forcedContentCountry: ContentCountry? = null

    @Throws(IOException::class, ExtractionException::class)
    abstract fun suggestionList(query: String): List<String>

    val serviceId: Int
        get() = service.serviceId

    // TODO: Create a more general Extractor class
    fun forceLocalization(localization: Localization?) {
        this.forcedLocalization = localization
    }

    fun forceContentCountry(contentCountry: ContentCountry?) {
        this.forcedContentCountry = contentCountry
    }


    val extractorLocalization: Localization
        get() = if (forcedLocalization == null) service.localization else forcedLocalization!!


    val extractorContentCountry: ContentCountry
        get() = if (forcedContentCountry == null) service.contentCountry else forcedContentCountry!!
}
