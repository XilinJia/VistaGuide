package ac.mdiq.vista.extractor.services.youtube.extractors

import com.grack.nanojson.JsonObject
import ac.mdiq.vista.extractor.Image
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.playlist.PlaylistInfoItemExtractor
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.getImagesFromThumbnailsArray
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.getTextFromObject
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.getUrlFromObject
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.isVerified
import ac.mdiq.vista.extractor.services.youtube.linkHandler.YoutubePlaylistLinkHandlerFactory
import ac.mdiq.vista.extractor.utils.Utils.removeNonDigitCharacters


open class YoutubePlaylistInfoItemExtractor(private val playlistInfoItem: JsonObject) : PlaylistInfoItemExtractor {
    @get:Throws(ParsingException::class)

    override val thumbnails: List<Image>
        get() {
            try {
                var thumbnails = playlistInfoItem.getArray("thumbnails").getObject(0).getArray("thumbnails")
                if (thumbnails.isEmpty()) thumbnails = playlistInfoItem.getObject("thumbnail").getArray("thumbnails")
                return getImagesFromThumbnailsArray(thumbnails)
            } catch (e: Exception) {
                throw ParsingException("Could not get thumbnails", e)
            }
        }

    @get:Throws(ParsingException::class)
    override val name: String
        get() {
            try {
                return getTextFromObject(playlistInfoItem.getObject("title")) ?: ""
            } catch (e: Exception) {
                throw ParsingException("Could not get name", e)
            }
        }

    @get:Throws(ParsingException::class)
    override val url: String
        get() {
            try {
                val id = playlistInfoItem.getString("playlistId")
                return YoutubePlaylistLinkHandlerFactory.instance.getUrl(id)
            } catch (e: Exception) {
                throw ParsingException("Could not get url", e)
            }
        }

    @Throws(ParsingException::class)
    override fun getUploaderName(): String? {
        try {
            return getTextFromObject(playlistInfoItem.getObject("longBylineText"))
        } catch (e: Exception) {
            throw ParsingException("Could not get uploader name", e)
        }
    }

    @Throws(ParsingException::class)
    override fun getUploaderUrl(): String? {
        try {
            return getUrlFromObject(playlistInfoItem.getObject("longBylineText"))
        } catch (e: Exception) {
            throw ParsingException("Could not get uploader url", e)
        }
    }

    @Throws(ParsingException::class)
    override fun isUploaderVerified(): Boolean {
        try {
            return isVerified(playlistInfoItem.getArray("ownerBadges"))
        } catch (e: Exception) {
            throw ParsingException("Could not get uploader verification info", e)
        }
    }

    @Throws(ParsingException::class)
    override fun getStreamCount(): Long {
        var videoCountText = playlistInfoItem.getString("videoCount")
        if (videoCountText == null) videoCountText = getTextFromObject(playlistInfoItem.getObject("videoCountText"))

        if (videoCountText == null) videoCountText = getTextFromObject(playlistInfoItem.getObject("videoCountShortText"))

        if (videoCountText == null) throw ParsingException("Could not get stream count")

        try {
            return removeNonDigitCharacters(videoCountText).toLong()
        } catch (e: Exception) {
            throw ParsingException("Could not get stream count", e)
        }
    }
}
