package ac.mdiq.vista.extractor.services.bandcamp.extractors

import com.grack.nanojson.JsonObject
import ac.mdiq.vista.extractor.Image
import ac.mdiq.vista.extractor.ListExtractor
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.playlist.PlaylistInfoItemExtractor


class BandcampAlbumInfoItemExtractor(
        private val albumInfoItem: JsonObject,
        private val uploaderUrl: String) : PlaylistInfoItemExtractor {

    @get:Throws(ParsingException::class)
    override val name: String
        get() = albumInfoItem.getString("title")

    @get:Throws(ParsingException::class)
    override val url: String
        get() = BandcampExtractorHelper.getStreamUrlFromIds(
            albumInfoItem.getLong("band_id"),
            albumInfoItem.getLong("item_id"),
            albumInfoItem.getString("item_type")) ?:""

    @get:Throws(ParsingException::class)

    override val thumbnails: List<Image>
        get() = BandcampExtractorHelper.getImagesFromImageId(albumInfoItem.getLong("art_id"), true)

    @Throws(ParsingException::class)
    override fun getUploaderName(): String? {
        return albumInfoItem.getString("band_name")
    }

    override fun getUploaderUrl(): String {
        return uploaderUrl
    }

    override fun isUploaderVerified(): Boolean {
        return false
    }

    override fun getStreamCount(): Long {
        return ListExtractor.ITEM_COUNT_UNKNOWN
    }
}
