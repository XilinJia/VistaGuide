/*
 * Created by Christian Schabesberger on 28.09.16.
 *
 * Copyright (C) 2015 Christian Schabesberger <chris.schabesberger@mailbox.org>
* Copyright (C) 2024 Xilin Jia <https://github.com/XilinJia>
 * YoutubeSuggestionExtractor.kt is part of Vista Guide.
 *
 * Vista Guide is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Vista Guide is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Vista Guide.  If not, see <https://www.gnu.org/licenses/>.
 */
package ac.mdiq.vista.extractor.services.youtube.extractors

import com.grack.nanojson.JsonArray
import com.grack.nanojson.JsonParser
import com.grack.nanojson.JsonParserException
import ac.mdiq.vista.extractor.Vista.downloader
import ac.mdiq.vista.extractor.StreamingService
import ac.mdiq.vista.extractor.exceptions.ExtractionException
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.suggestion.SuggestionExtractor
import ac.mdiq.vista.extractor.utils.Utils.encodeUrlUtf8
import java.io.IOException
import java.util.stream.Collectors

class YoutubeSuggestionExtractor(service: StreamingService) : SuggestionExtractor(service) {
    @Throws(IOException::class, ExtractionException::class)
    override fun suggestionList(query: String): List<String> {
        val url = ("https://suggestqueries-clients6.youtube.com/complete/search"
                + "?client=" + "youtube"
                + "&ds=" + "yt"
                + "&gl=" + encodeUrlUtf8(extractorContentCountry.countryCode)
                + "&q=" + encodeUrlUtf8(query)
                + "&xhr=t")

        val headers: MutableMap<String, List<String>> = HashMap()
        headers["Origin"] = listOf("https://www.youtube.com")
        headers["Referer"] = listOf("https://www.youtube.com")

        val response = downloader.get(url, headers, extractorLocalization)

        val contentTypeHeader = response.getHeader("Content-Type")
        if (contentTypeHeader.isNullOrEmpty() || !contentTypeHeader.contains("application/json"))
            throw ExtractionException("Invalid response type (got \"$contentTypeHeader\", excepted a JSON response) (response code ${response.responseCode()})")

        val responseBody = response.responseBody()

        if (responseBody.isEmpty()) throw ExtractionException("Empty response received")

        try {
            val suggestions = JsonParser.array()
                .from(responseBody)
                .getArray(1) // 0: search query, 1: search suggestions, 2: tracking data?
            return suggestions.stream()
                .filter { o: Any? -> JsonArray::class.java.isInstance(o) }
                .map { obj: Any? -> JsonArray::class.java.cast(obj) }
                .map { suggestion: JsonArray -> suggestion.getString(0) } // 0 is the search suggestion
                .filter { suggestion: String? -> !suggestion.isNullOrEmpty() } // Filter blank suggestions
                .collect(Collectors.toUnmodifiableList())
        } catch (e: JsonParserException) { throw ParsingException("Could not parse JSON response", e) }
    }
}
