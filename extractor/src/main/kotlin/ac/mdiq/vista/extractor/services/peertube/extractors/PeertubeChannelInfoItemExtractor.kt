package ac.mdiq.vista.extractor.services.peertube.extractors

import com.grack.nanojson.JsonObject
import ac.mdiq.vista.extractor.Image
import ac.mdiq.vista.extractor.ListExtractor
import ac.mdiq.vista.extractor.channel.ChannelInfoItemExtractor
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.services.peertube.PeertubeParsingHelper.getAvatarsFromOwnerAccountOrVideoChannelObject


class PeertubeChannelInfoItemExtractor(
        private val item: JsonObject,
        private val baseUrl: String)
    : ChannelInfoItemExtractor {

    @get:Throws(ParsingException::class)
    override val name: String
        get() = item.getString("displayName")

    @get:Throws(ParsingException::class)
    override val url: String
        get() = item.getString("url")

    @get:Throws(ParsingException::class)

    override val thumbnails: List<Image>
        get() = getAvatarsFromOwnerAccountOrVideoChannelObject(baseUrl, item)

    @Throws(ParsingException::class)
    override fun getDescription(): String {
        return item.getString("description") ?: ""
    }

    @Throws(ParsingException::class)
    override fun getSubscriberCount(): Long {
        return item.getInt("followersCount").toLong()
    }

    @Throws(ParsingException::class)
    override fun getStreamCount(): Long {
        return ListExtractor.ITEM_COUNT_UNKNOWN
    }

    @Throws(ParsingException::class)
    override fun isVerified(): Boolean {
        return false
    }
}
