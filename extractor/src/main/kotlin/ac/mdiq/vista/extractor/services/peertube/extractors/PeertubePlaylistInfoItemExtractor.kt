package ac.mdiq.vista.extractor.services.peertube.extractors

import com.grack.nanojson.JsonObject
import ac.mdiq.vista.extractor.Image
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.playlist.PlaylistInfoItemExtractor
import ac.mdiq.vista.extractor.services.peertube.PeertubeParsingHelper.getThumbnailsFromPlaylistOrVideoItem
import ac.mdiq.vista.extractor.stream.Description


class PeertubePlaylistInfoItemExtractor(
        private val item: JsonObject,
        private val baseUrl: String)
    : PlaylistInfoItemExtractor {

    private val uploader: JsonObject = item.getObject("uploader")

    @get:Throws(ParsingException::class)
    override val name: String
        get() = item.getString("displayName")

    @get:Throws(ParsingException::class)
    override val url: String
        get() = item.getString("url")

    @get:Throws(ParsingException::class)

    override val thumbnails: List<Image>
        get() = getThumbnailsFromPlaylistOrVideoItem(baseUrl, item)

    @Throws(ParsingException::class)
    override fun getUploaderName(): String? {
        return uploader.getString("displayName")
    }

    @Throws(ParsingException::class)
    override fun getUploaderUrl(): String? {
        return uploader.getString("url")
    }

    @Throws(ParsingException::class)
    override fun isUploaderVerified(): Boolean {
        return false
    }

    @Throws(ParsingException::class)
    override fun getStreamCount(): Long {
        return item.getInt("videosLength").toLong()
    }


    @Throws(ParsingException::class)
    override fun getDescription(): Description {
        val description = item.getString("description")
        if (description.isNullOrEmpty()) {
            return Description.EMPTY_DESCRIPTION
        }
        return Description(description, Description.PLAIN_TEXT)
    }
}
