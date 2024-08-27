// Created by Fynn Godau 2019, licensed GNU GPL version 3 or later
package ac.mdiq.vista.extractor.services.bandcamp.linkHandler

import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.linkhandler.ListLinkHandlerFactory
import ac.mdiq.vista.extractor.services.bandcamp.extractors.BandcampExtractorHelper
import ac.mdiq.vista.extractor.utils.Utils.replaceHttpWithHttps
import java.util.*

/**
 * Just as with streams, the album ids are essentially useless for us.
 */
class BandcampPlaylistLinkHandlerFactory private constructor() : ListLinkHandlerFactory() {

    @Throws(ParsingException::class, UnsupportedOperationException::class)
    override fun getId(url: String): String {
        return getUrl(url)?:""
    }

    @Throws(ParsingException::class, UnsupportedOperationException::class)
    override fun getUrl(id: String, contentFilters: List<String>, sortFilter: String?): String {
        return replaceHttpWithHttps(id)
    }

    /**
     * Accepts all bandcamp URLs that contain /album/ behind their domain name.
     */
    @Throws(ParsingException::class)
    override fun onAcceptUrl(url: String): Boolean {
        // Exclude URLs which do not lead to an album

        if (!url.lowercase(Locale.getDefault()).matches("https?://.+\\..+/album/.+".toRegex())) return false

        // Test whether domain is supported
        return BandcampExtractorHelper.isArtistDomain(url)
    }

    companion object {
        val instance: BandcampPlaylistLinkHandlerFactory = BandcampPlaylistLinkHandlerFactory()
    }
}
