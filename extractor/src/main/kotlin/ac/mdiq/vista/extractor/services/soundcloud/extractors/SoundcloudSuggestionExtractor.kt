package ac.mdiq.vista.extractor.services.soundcloud.extractors

import com.grack.nanojson.JsonObject
import com.grack.nanojson.JsonParser
import com.grack.nanojson.JsonParserException
import ac.mdiq.vista.extractor.Vista.downloader
import ac.mdiq.vista.extractor.StreamingService
import ac.mdiq.vista.extractor.exceptions.ExtractionException
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.services.soundcloud.SoundcloudParsingHelper
import ac.mdiq.vista.extractor.suggestion.SuggestionExtractor
import ac.mdiq.vista.extractor.utils.Utils.encodeUrlUtf8
import java.io.IOException

class SoundcloudSuggestionExtractor(service: StreamingService) : SuggestionExtractor(service) {

    @Throws(IOException::class, ExtractionException::class)
    override fun suggestionList(query: String): List<String> {
        val suggestions: MutableList<String> = ArrayList()
        val dl = downloader
        val url = ("${SoundcloudParsingHelper.SOUNDCLOUD_API_V2_URL}search/queries?q=${encodeUrlUtf8(query)}&client_id=${SoundcloudParsingHelper.clientId()}&limit=10")
        val response = dl.get(url, extractorLocalization).responseBody()

        try {
            val collection = JsonParser.`object`().from(response).getArray("collection")
            for (suggestion in collection) {
                if (suggestion is JsonObject) suggestions.add(suggestion.getString("query"))
            }
            return suggestions
        } catch (e: JsonParserException) {
            throw ParsingException("Could not parse json response", e)
        }
    }
}
