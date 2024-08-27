package ac.mdiq.vista.extractor.services.bandcamp.linkHandler

import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.linkhandler.ListLinkHandlerFactory
import ac.mdiq.vista.extractor.services.bandcamp.extractors.BandcampExtractorHelper
import ac.mdiq.vista.extractor.utils.Utils.replaceHttpWithHttps
import java.util.*

/**
 * Like in [BandcampStreamLinkHandlerFactory], tracks have no meaningful IDs except for
 * their URLs
 */
class BandcampCommentsLinkHandlerFactory private constructor() : ListLinkHandlerFactory() {

    @Throws(ParsingException::class, UnsupportedOperationException::class)
    override fun getId(url: String): String {
        return replaceHttpWithHttps(url)
    }

    @Throws(ParsingException::class)
    override fun onAcceptUrl(url: String): Boolean {
        if (BandcampExtractorHelper.isRadioUrl(url)) return true

        // Don't accept URLs that don't point to a track
        if (!url.lowercase(Locale.getDefault()).matches("https?://.+\\..+/(track|album)/.+".toRegex())) return false

        // Test whether domain is supported
        return BandcampExtractorHelper.isArtistDomain(url)
    }

    @Throws(ParsingException::class, UnsupportedOperationException::class)
    override fun getUrl(id: String, contentFilters: List<String>, sortFilter: String?): String {
        return replaceHttpWithHttps(id)
    }

    companion object {
        val instance: BandcampCommentsLinkHandlerFactory = BandcampCommentsLinkHandlerFactory()
    }
}
