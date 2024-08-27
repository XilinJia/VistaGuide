// Created by Fynn Godau 2021, licensed GNU GPL version 3 or later
package ac.mdiq.vista.extractor.services.bandcamp.extractors

import org.jsoup.nodes.Element
import ac.mdiq.vista.extractor.Image
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.playlist.PlaylistInfoItemExtractor
import ac.mdiq.vista.extractor.services.bandcamp.extractors.BandcampExtractorHelper.getImagesFromImageUrl


/**
 * Extracts recommended albums from tracks' website
 */
class BandcampRelatedPlaylistInfoItemExtractor(
        private val relatedAlbum: Element)
    : PlaylistInfoItemExtractor {

    @get:Throws(ParsingException::class)
    override val name: String
        get() = relatedAlbum.getElementsByClass("release-title").text()

    @get:Throws(ParsingException::class)
    override val url: String
        get() = relatedAlbum.getElementsByClass("album-link").attr("abs:href")

    @get:Throws(ParsingException::class)

    override val thumbnails: List<Image>
        get() = getImagesFromImageUrl(relatedAlbum.getElementsByClass("album-art").attr("src"))

    @Throws(ParsingException::class)
    override fun getUploaderName(): String {
        return relatedAlbum.getElementsByClass("by-artist").text().replace("by ", "")
    }

    @Throws(ParsingException::class)
    override fun getUploaderUrl(): String? {
        return null
    }

    @Throws(ParsingException::class)
    override fun isUploaderVerified(): Boolean {
        return false
    }

    @Throws(ParsingException::class)
    override fun getStreamCount(): Long {
        return -1
    }
}
