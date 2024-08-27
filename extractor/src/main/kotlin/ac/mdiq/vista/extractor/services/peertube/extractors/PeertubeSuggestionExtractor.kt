package ac.mdiq.vista.extractor.services.peertube.extractors

import ac.mdiq.vista.extractor.StreamingService
import ac.mdiq.vista.extractor.suggestion.SuggestionExtractor

class PeertubeSuggestionExtractor(service: StreamingService) : SuggestionExtractor(service) {

    override fun suggestionList(query: String): List<String> {
        return emptyList()
    }
}
