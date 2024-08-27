// Created by Fynn Godau 2019, licensed GNU GPL version 3 or later
package ac.mdiq.vista.extractor.services.bandcamp.linkHandler

import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.linkhandler.LinkHandlerFactory
import ac.mdiq.vista.extractor.services.bandcamp.extractors.BandcampExtractorHelper
import ac.mdiq.vista.extractor.utils.Utils.replaceHttpWithHttps
import java.util.*

/**
 *
 * Tracks don't have standalone ids, they are always in combination with the band id.
 * That's why id = url.
 *
 *
 * Radio (bandcamp weekly) shows do have ids.
 */
class BandcampStreamLinkHandlerFactory private constructor() : LinkHandlerFactory() {
    /**
     * @see BandcampStreamLinkHandlerFactory
     */
    @Throws(ParsingException::class, UnsupportedOperationException::class)
    override fun getId(url: String): String {
        return if (BandcampExtractorHelper.isRadioUrl(url))
            url.split("bandcamp.com/\\?show=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1]
        else getUrl(url)?:""
    }

    /**
     * Clean up url
     * @see BandcampStreamLinkHandlerFactory
     */
    @Throws(ParsingException::class, UnsupportedOperationException::class)
    override fun getUrl(id: String): String {
        return if (id.matches("\\d+".toRegex())) "${BandcampExtractorHelper.BASE_URL}/?show=$id" else replaceHttpWithHttps(id)
    }

    /**
     * Accepts URLs that point to a bandcamp radio show or that are a bandcamp
     * domain and point to a track.
     */
    @Throws(ParsingException::class)
    override fun onAcceptUrl(url: String): Boolean {
        // Accept Bandcamp radio

        if (BandcampExtractorHelper.isRadioUrl(url)) return true

        // Don't accept URLs that don't point to a track
        if (!url.lowercase(Locale.getDefault()).matches("https?://.+\\..+/track/.+".toRegex())) return false

        // Test whether domain is supported
        return BandcampExtractorHelper.isArtistDomain(url)
    }

    companion object {
        val instance: BandcampStreamLinkHandlerFactory = BandcampStreamLinkHandlerFactory()
    }
}
