package ac.mdiq.vista.extractor.services.youtube.extractors

import com.grack.nanojson.JsonObject
import ac.mdiq.vista.extractor.Image
import ac.mdiq.vista.extractor.channel.ChannelInfoItemExtractor
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.getImagesFromThumbnailsArray
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.getTextFromObject
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.getUrlFromNavigationEndpoint
import ac.mdiq.vista.extractor.utils.Parser.RegexException
import ac.mdiq.vista.extractor.utils.Utils.mixedNumberWordToLong


class YoutubeMusicArtistInfoItemExtractor(private val artistInfoItem: JsonObject) : ChannelInfoItemExtractor {
    @get:Throws(ParsingException::class)

    override val thumbnails: List<Image>
        get() {
            try {
                return getImagesFromThumbnailsArray(
                    artistInfoItem.getObject("thumbnail")
                        .getObject("musicThumbnailRenderer")
                        .getObject("thumbnail")
                        .getArray("thumbnails"))
            } catch (e: Exception) {
                throw ParsingException("Could not get thumbnails", e)
            }
        }

    @get:Throws(ParsingException::class)
    override val name: String
        get() {
            val name = getTextFromObject(artistInfoItem.getArray("flexColumns")
                .getObject(0)
                .getObject("musicResponsiveListItemFlexColumnRenderer")
                .getObject("text"))
            if (!name.isNullOrEmpty()) return name
            throw ParsingException("Could not get name")
        }

    @get:Throws(ParsingException::class)
    override val url: String
        get() {
            val url_ = getUrlFromNavigationEndpoint(artistInfoItem.getObject("navigationEndpoint"))
            if (!url_.isNullOrEmpty()) return url_
            throw ParsingException("Could not get URL")
        }

    @Throws(ParsingException::class)
    override fun getSubscriberCount(): Long {
        val subscriberCount = getTextFromObject(artistInfoItem.getArray("flexColumns")
            .getObject(2)
            .getObject("musicResponsiveListItemFlexColumnRenderer")
            .getObject("text"))
        if (!subscriberCount.isNullOrEmpty()) {
            return try {
                mixedNumberWordToLong(subscriberCount)
            } catch (ignored: RegexException) {
                // probably subscriberCount == "No subscribers" or similar
                0
            }
        }
        throw ParsingException("Could not get subscriber count")
    }

    override fun getStreamCount(): Long {
        return -1
    }

    override fun isVerified(): Boolean {
        // An artist on YouTube Music is always verified
        return true
    }

    override fun getDescription(): String {
        return ""
    }
}
