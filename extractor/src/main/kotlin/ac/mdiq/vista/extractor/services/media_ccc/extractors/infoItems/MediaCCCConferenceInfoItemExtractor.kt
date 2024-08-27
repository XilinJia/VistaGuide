package ac.mdiq.vista.extractor.services.media_ccc.extractors.infoItems

import com.grack.nanojson.JsonObject
import ac.mdiq.vista.extractor.Image
import ac.mdiq.vista.extractor.ListExtractor
import ac.mdiq.vista.extractor.channel.ChannelInfoItemExtractor
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.services.media_ccc.extractors.MediaCCCParsingHelper


class MediaCCCConferenceInfoItemExtractor(private val conference: JsonObject) : ChannelInfoItemExtractor {
    override fun getDescription(): String {
        return ""
    }

    override fun getSubscriberCount(): Long {
        return -1
    }

    override fun getStreamCount(): Long {
        return ListExtractor.ITEM_COUNT_UNKNOWN
    }

    @Throws(ParsingException::class)
    override fun isVerified(): Boolean {
        return false
    }

    @get:Throws(ParsingException::class)
    override val name: String
        get() = conference.getString("title")

    @get:Throws(ParsingException::class)
    override val url: String
        get() = conference.getString("url")


    override val thumbnails: List<Image>
        get() = MediaCCCParsingHelper.getImageListFromLogoImageUrl(conference.getString("logo_url"))
}
