// Created by Fynn Godau 2019, licensed GNU GPL version 3 or later
package ac.mdiq.vista.extractor.services.bandcamp.linkHandler

import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.linkhandler.SearchQueryHandlerFactory
import ac.mdiq.vista.extractor.services.bandcamp.extractors.BandcampExtractorHelper
import ac.mdiq.vista.extractor.utils.Utils.encodeUrlUtf8

class BandcampSearchQueryHandlerFactory private constructor() : SearchQueryHandlerFactory() {

    @Throws(ParsingException::class, UnsupportedOperationException::class)
    override fun getUrl(id: String, contentFilters: List<String>, sortFilter: String?): String {
        return BandcampExtractorHelper.BASE_URL + "/search?q=" + encodeUrlUtf8(id) + "&page=1"
    }

    companion object {
        val instance: BandcampSearchQueryHandlerFactory = BandcampSearchQueryHandlerFactory()
    }
}
