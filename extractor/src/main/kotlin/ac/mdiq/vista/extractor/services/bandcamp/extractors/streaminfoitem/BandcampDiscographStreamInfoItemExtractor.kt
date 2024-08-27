package ac.mdiq.vista.extractor.services.bandcamp.extractors.streaminfoitem

import com.grack.nanojson.JsonObject
import ac.mdiq.vista.extractor.Image
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.services.bandcamp.extractors.BandcampExtractorHelper


class BandcampDiscographStreamInfoItemExtractor(
        private val discograph: JsonObject,
        uploaderUrl: String)
    : BandcampStreamInfoItemExtractor(uploaderUrl) {

    override fun getUploaderName(): String? {
        return discograph.getString("band_name")
    }

    override val name: String
        get() = discograph.getString("title")

    @get:Throws(ParsingException::class)
    override val url: String
        get() = BandcampExtractorHelper.getStreamUrlFromIds(discograph.getLong("band_id"), discograph.getLong("item_id"), discograph.getString("item_type")) ?:""

    @get:Throws(ParsingException::class)

    override val thumbnails: List<Image>
        get() = BandcampExtractorHelper.getImagesFromImageId(discograph.getLong("art_id"), true)

    override fun getDuration(): Long {
        return -1
    }
}
