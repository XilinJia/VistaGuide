package ac.mdiq.vista.extractor.services.youtube.extractors

import com.grack.nanojson.JsonObject
import ac.mdiq.vista.extractor.Image
import ac.mdiq.vista.extractor.ListExtractor
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.playlist.PlaylistInfo.PlaylistType
import ac.mdiq.vista.extractor.playlist.PlaylistInfoItemExtractor
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.extractPlaylistTypeFromPlaylistUrl
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.getTextFromObject
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.getThumbnailsFromInfoItem


class YoutubeMixOrPlaylistInfoItemExtractor(private val mixInfoItem: JsonObject) : PlaylistInfoItemExtractor {
    @get:Throws(ParsingException::class)
    override val name: String
        get() {
            val name = getTextFromObject(mixInfoItem.getObject("title"))
            if (name.isNullOrEmpty()) throw ParsingException("Could not get name")
            return name
        }

    @get:Throws(ParsingException::class)
    override val url: String
        get() {
            val url = mixInfoItem.getString("shareUrl")
            if (url.isNullOrEmpty()) throw ParsingException("Could not get url")
            return url
        }

    @get:Throws(ParsingException::class)

    override val thumbnails: List<Image>
        get() = getThumbnailsFromInfoItem(mixInfoItem)

    @Throws(ParsingException::class)
    override fun getUploaderName(): String? {
        // this will be a list of uploaders for mixes
        return getTextFromObject(mixInfoItem.getObject("longBylineText"))
    }

    @Throws(ParsingException::class)
    override fun getUploaderUrl(): String? {
        // They're auto-generated, so there's no uploader
        return null
    }

    @Throws(ParsingException::class)
    override fun isUploaderVerified(): Boolean {
        // They're auto-generated, so there's no uploader
        return false
    }

    @Throws(ParsingException::class)
    override fun getStreamCount(): Long {
        val countString = getTextFromObject(mixInfoItem.getObject("videoCountShortText"))
            ?: throw ParsingException("Could not extract item count for playlist/mix info item")

        return try {
            countString.toInt().toLong()
        } catch (ignored: NumberFormatException) {
            // un-parsable integer: this is a mix with infinite items and "50+" as count string
            // (though YouTube Music mixes do not necessarily have an infinite count of songs)
            ListExtractor.ITEM_COUNT_INFINITE
        }
    }


    @Throws(ParsingException::class)
    override fun getPlaylistType(): PlaylistType {
        return extractPlaylistTypeFromPlaylistUrl(url)
    }
}
