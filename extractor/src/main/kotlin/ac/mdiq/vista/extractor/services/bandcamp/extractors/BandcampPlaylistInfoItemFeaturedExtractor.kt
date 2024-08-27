package ac.mdiq.vista.extractor.services.bandcamp.extractors

import com.grack.nanojson.JsonObject
import ac.mdiq.vista.extractor.Image
import ac.mdiq.vista.extractor.playlist.PlaylistInfoItemExtractor
import ac.mdiq.vista.extractor.services.bandcamp.extractors.BandcampExtractorHelper.getImagesFromImageId
import ac.mdiq.vista.extractor.utils.Utils.replaceHttpWithHttps


class BandcampPlaylistInfoItemFeaturedExtractor(private val featuredStory: JsonObject) : PlaylistInfoItemExtractor {

    override fun getUploaderName(): String? {
        return featuredStory.getString("band_name")
    }

    override fun getUploaderUrl(): String? {
        return null
    }

    override fun isUploaderVerified(): Boolean {
        return false
    }

    override fun getStreamCount(): Long {
        return featuredStory.getInt("num_streamable_tracks").toLong()
    }

    override val name: String
        get() = featuredStory.getString("album_title")

    override val url: String
        get() = replaceHttpWithHttps(featuredStory.getString("item_url"))


    override val thumbnails: List<Image>
        get() = if (featuredStory.has("art_id")
        ) getImagesFromImageId(featuredStory.getLong("art_id"), true)
        else getImagesFromImageId(featuredStory.getLong("item_art_id"), true)
}
