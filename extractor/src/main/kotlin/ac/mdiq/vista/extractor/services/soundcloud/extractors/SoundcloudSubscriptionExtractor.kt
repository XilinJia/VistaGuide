package ac.mdiq.vista.extractor.services.soundcloud.extractors

import ac.mdiq.vista.extractor.channel.ChannelInfoItem
import ac.mdiq.vista.extractor.channel.ChannelInfoItemsCollector
import ac.mdiq.vista.extractor.exceptions.ExtractionException
import ac.mdiq.vista.extractor.services.soundcloud.SoundcloudParsingHelper
import ac.mdiq.vista.extractor.services.soundcloud.SoundcloudService
import ac.mdiq.vista.extractor.subscription.SubscriptionExtractor
import ac.mdiq.vista.extractor.subscription.SubscriptionItem
import ac.mdiq.vista.extractor.utils.Utils.HTTPS
import ac.mdiq.vista.extractor.utils.Utils.replaceHttpWithHttps
import java.io.IOException

/**
 * Extract the "followings" from a user in SoundCloud.
 */
class SoundcloudSubscriptionExtractor(service: SoundcloudService?) : SubscriptionExtractor(service!!, listOf(ContentSource.CHANNEL_URL)) {
    override val relatedUrl: String
        get() = "https://soundcloud.com/you"

    @Throws(IOException::class, ExtractionException::class)
    override fun fromChannelUrl(channelUrl: String?): List<SubscriptionItem?> {
        if (channelUrl == null) throw InvalidSourceException("Channel url is null")

        val id: String
        try {
            id = service.getChannelLHFactory().fromUrl(getUrlFrom(channelUrl)).id
        } catch (e: ExtractionException) {
            throw InvalidSourceException(e)
        }

        val apiUrl = (SoundcloudParsingHelper.SOUNDCLOUD_API_V2_URL + "users/" + id + "/followings" + "?client_id=" + SoundcloudParsingHelper.clientId() + "&limit=200")
        val collector = ChannelInfoItemsCollector(service.serviceId)
        // ± 2000 is the limit of followings on SoundCloud, so this minimum should be enough
        SoundcloudParsingHelper.getUsersFromApiMinItems(2500, collector, apiUrl)

        return toSubscriptionItems(collector.getItems())
    }

    private fun getUrlFrom(channelUrl: String): String {
        val fixedUrl = replaceHttpWithHttps(channelUrl)
        return when {
            fixedUrl.startsWith(HTTPS) -> channelUrl
            !fixedUrl.contains("soundcloud.com/") -> "https://soundcloud.com/$fixedUrl"
            else -> HTTPS + fixedUrl
        }
    }

    private fun toSubscriptionItems(items: List<ChannelInfoItem>?): List<SubscriptionItem?> {
        val result: MutableList<SubscriptionItem?> = ArrayList(items!!.size)
        for (item in items) {
            result.add(SubscriptionItem(item.serviceId, item.url, item.name))
        }
        return result
    }
}
