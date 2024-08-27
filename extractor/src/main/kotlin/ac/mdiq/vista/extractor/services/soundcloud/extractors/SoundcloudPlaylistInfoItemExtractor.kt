package ac.mdiq.vista.extractor.services.soundcloud.extractors

import com.grack.nanojson.JsonObject
import ac.mdiq.vista.extractor.Image
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.playlist.PlaylistInfoItemExtractor
import ac.mdiq.vista.extractor.services.soundcloud.SoundcloudParsingHelper
import ac.mdiq.vista.extractor.utils.Utils.replaceHttpWithHttps


class SoundcloudPlaylistInfoItemExtractor(private val itemObject: JsonObject) : PlaylistInfoItemExtractor {
    override val name: String
        get() = itemObject.getString("title")

    override val url: String
        get() = replaceHttpWithHttps(itemObject.getString("permalink_url"))

    @get:Throws(ParsingException::class)

    override val thumbnails: List<Image>
        get() {
            // Over-engineering at its finest
            if (itemObject.isString(ARTWORK_URL_KEY)) {
                val artworkUrl = itemObject.getString(ARTWORK_URL_KEY)
                if (!artworkUrl.isNullOrEmpty()) return SoundcloudParsingHelper.getAllImagesFromArtworkOrAvatarUrl(artworkUrl)
            }

            try {
                // Look for artwork URL inside the track list
                for (track in itemObject.getArray("tracks")) {
                    val trackObject = track as JsonObject

                    // First look for track artwork URL
                    if (trackObject.isString(ARTWORK_URL_KEY)) {
                        val artworkUrl = trackObject.getString(ARTWORK_URL_KEY)
                        if (!artworkUrl.isNullOrEmpty()) return SoundcloudParsingHelper.getAllImagesFromArtworkOrAvatarUrl(artworkUrl)
                    }

                    // Then look for track creator avatar URL
                    val creator = trackObject.getObject(USER_KEY)
                    val creatorAvatar = creator.getString(AVATAR_URL_KEY)
                    if (!creatorAvatar.isNullOrEmpty()) return SoundcloudParsingHelper.getAllImagesFromArtworkOrAvatarUrl(creatorAvatar)
                }
            } catch (ignored: Exception) {
                // Try other method
            }

            try {
                // Last resort, use user avatar URL. If still not found, then throw an exception.
                return SoundcloudParsingHelper.getAllImagesFromArtworkOrAvatarUrl(itemObject.getObject(USER_KEY).getString(AVATAR_URL_KEY))
            } catch (e: Exception) {
                throw ParsingException("Failed to extract playlist thumbnails", e)
            }
        }

    @Throws(ParsingException::class)
    override fun getUploaderName(): String? {
        try {
            return itemObject.getObject(USER_KEY).getString("username")
        } catch (e: Exception) {
            throw ParsingException("Failed to extract playlist uploader", e)
        }
    }

    override fun getUploaderUrl(): String? {
        return itemObject.getObject(USER_KEY).getString("permalink_url")
    }

    override fun isUploaderVerified(): Boolean {
        return itemObject.getObject(USER_KEY).getBoolean("verified")
    }

    override fun getStreamCount(): Long {
        return itemObject.getLong("track_count")
    }

    companion object {
        private const val USER_KEY = "user"
        private const val AVATAR_URL_KEY = "avatar_url"
        private const val ARTWORK_URL_KEY = "artwork_url"
    }
}
