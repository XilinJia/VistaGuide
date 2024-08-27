// Created by Fynn Godau 2019, licensed GNU GPL version 3 or later
package ac.mdiq.vista.extractor.services.bandcamp.linkHandler

import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.linkhandler.ListLinkHandlerFactory
import ac.mdiq.vista.extractor.services.bandcamp.extractors.BandcampExtractorHelper
import ac.mdiq.vista.extractor.services.bandcamp.extractors.BandcampFeaturedExtractor
import ac.mdiq.vista.extractor.services.bandcamp.extractors.BandcampRadioExtractor
import ac.mdiq.vista.extractor.utils.Utils.replaceHttpWithHttps

class BandcampFeaturedLinkHandlerFactory private constructor() : ListLinkHandlerFactory() {

    @Throws(ParsingException::class, UnsupportedOperationException::class)
    override fun getUrl(id: String, contentFilters: List<String>, sortFilter: String?): String {
        return when (id) {
            BandcampFeaturedExtractor.KIOSK_FEATURED -> BandcampFeaturedExtractor.FEATURED_API_URL // doesn't have a website
            BandcampRadioExtractor.KIOSK_RADIO -> BandcampRadioExtractor.RADIO_API_URL // doesn't have its own website
            else -> ""
        }
    }

    @Throws(ParsingException::class, UnsupportedOperationException::class)
    override fun getId(url: String): String {
        val fixedUrl = replaceHttpWithHttps(url)
        return when {
            BandcampExtractorHelper.isRadioUrl(fixedUrl) || fixedUrl == BandcampRadioExtractor.RADIO_API_URL -> BandcampRadioExtractor.KIOSK_RADIO
            fixedUrl == BandcampFeaturedExtractor.FEATURED_API_URL -> BandcampFeaturedExtractor.KIOSK_FEATURED
            else -> ""
        }
    }

    override fun onAcceptUrl(url: String): Boolean {
        if (url.isEmpty()) return false
        val fixedUrl = replaceHttpWithHttps(url)
        return fixedUrl == BandcampFeaturedExtractor.FEATURED_API_URL || fixedUrl == BandcampRadioExtractor.RADIO_API_URL || BandcampExtractorHelper.isRadioUrl(fixedUrl)
    }

    companion object {
        val instance: BandcampFeaturedLinkHandlerFactory = BandcampFeaturedLinkHandlerFactory()
    }
}
