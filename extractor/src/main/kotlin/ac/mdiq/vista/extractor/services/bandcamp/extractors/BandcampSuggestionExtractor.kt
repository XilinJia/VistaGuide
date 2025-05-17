// Created by Fynn Godau 2019, licensed GNU GPL version 3 or later
package ac.mdiq.vista.extractor.services.bandcamp.extractors

import ac.mdiq.vista.extractor.StreamingService
import ac.mdiq.vista.extractor.Vista.downloader
import ac.mdiq.vista.extractor.exceptions.ExtractionException
import ac.mdiq.vista.extractor.services.bandcamp.extractors.BandcampExtractorHelper.BASE_API_URL
import ac.mdiq.vista.extractor.suggestion.SuggestionExtractor
import ac.mdiq.vista.extractor.utils.Utils.encodeUrlUtf8
import com.grack.nanojson.JsonObject
import com.grack.nanojson.JsonParser
import com.grack.nanojson.JsonParserException
import com.grack.nanojson.JsonWriter
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.util.Collections
import java.util.stream.Collectors

class BandcampSuggestionExtractor(service: StreamingService) : SuggestionExtractor(service) {
    @Throws(IOException::class, ExtractionException::class)
    override fun suggestionList(query: String): List<String> {
        val downloader = downloader

        try {
            val fuzzyResults = JsonParser.`object`().from(downloader
                .postWithContentTypeJson(
                    AUTOCOMPLETE_URL,
                    Collections.emptyMap(),
                    JsonWriter.string()
                        .`object`()
                        .value("fan_id", null as String?)
                        .value("full_page", false)
                        .value("search_filter", "")
                        .value("search_text", query)
                        .end()
                        .done()
                        .toByteArray(StandardCharsets.UTF_8)).responseBody())

            return fuzzyResults.getObject("auto").getArray("results").stream()
                .filter { o: Any? -> JsonObject::class.java.isInstance(o) }
                .map { obj: Any? -> JsonObject::class.java.cast(obj) }
                .map { jsonObject: JsonObject -> jsonObject.getString("name") }
                .distinct()
                .collect(Collectors.toList())
        } catch (e: JsonParserException) {
            return emptyList()
        }
    }

    companion object {
        private val AUTOCOMPLETE_URL: String = (BASE_API_URL + "/bcsearch_public_api/1/autocomplete_elastic")
    }
}
