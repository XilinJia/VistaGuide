package ac.mdiq.vista.extractor.services.soundcloud.extractors

import com.grack.nanojson.JsonObject
import ac.mdiq.vista.extractor.Image
import ac.mdiq.vista.extractor.channel.ChannelInfoItemExtractor
import ac.mdiq.vista.extractor.services.soundcloud.SoundcloudParsingHelper
import ac.mdiq.vista.extractor.utils.Utils.replaceHttpWithHttps


class SoundcloudChannelInfoItemExtractor(private val itemObject: JsonObject) : ChannelInfoItemExtractor {
    override val name: String
        get() = itemObject.getString("username")

    override val url: String
        get() = replaceHttpWithHttps(itemObject.getString("permalink_url"))


    override val thumbnails: List<Image>
        get() = SoundcloudParsingHelper.getAllImagesFromArtworkOrAvatarUrl(itemObject.getString("avatar_url"))

    override fun getSubscriberCount(): Long {
        return itemObject.getLong("followers_count")
    }

    override fun getStreamCount(): Long {
        return itemObject.getLong("track_count")
    }

    override fun isVerified(): Boolean {
        return itemObject.getBoolean("verified")
    }

    override fun getDescription(): String {
        return itemObject.getString("description", "")
    }
}
