package ac.mdiq.vista.extractor.services.peertube.extractors

import com.grack.nanojson.JsonObject
import com.grack.nanojson.JsonParser
import com.grack.nanojson.JsonParserException
import ac.mdiq.vista.extractor.Image
import ac.mdiq.vista.extractor.StreamingService
import ac.mdiq.vista.extractor.channel.ChannelExtractor
import ac.mdiq.vista.extractor.channel.tabs.ChannelTabs
import ac.mdiq.vista.extractor.downloader.Downloader
import ac.mdiq.vista.extractor.exceptions.ExtractionException
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.exceptions.ReCaptchaException
import ac.mdiq.vista.extractor.linkhandler.ListLinkHandler
import ac.mdiq.vista.extractor.services.peertube.PeertubeParsingHelper.getAvatarsFromOwnerAccountOrVideoChannelObject
import ac.mdiq.vista.extractor.services.peertube.PeertubeParsingHelper.getBannersFromAccountOrVideoChannelObject
import ac.mdiq.vista.extractor.services.peertube.linkHandler.PeertubeChannelLinkHandlerFactory
import ac.mdiq.vista.extractor.services.peertube.linkHandler.PeertubeChannelTabLinkHandlerFactory
import ac.mdiq.vista.extractor.utils.JsonUtils.getString
import java.io.IOException


class PeertubeAccountExtractor(service: StreamingService, linkHandler: ListLinkHandler) : ChannelExtractor(service, linkHandler) {
    private var json: JsonObject? = null
//    override val baseUrl: String
//
//    init {
//        this.baseUrl = baseUrl
//    }


    override fun getAvatars(): List<Image> {
        return getAvatarsFromOwnerAccountOrVideoChannelObject(baseUrl, json!!)
    }


    override fun getBanners(): List<Image> {
        return getBannersFromAccountOrVideoChannelObject(baseUrl, json!!)
    }

    @Throws(ParsingException::class)
    override fun getFeedUrl(): String {
        return baseUrl + "/feeds/videos.xml?accountId=" + json!!["id"]
    }

    @Throws(ParsingException::class)
    override fun getSubscriberCount(): Long {
        // The subscriber count cannot be retrieved directly. It needs to be calculated.
        // An accounts subscriber count is the number of the channel owner's subscriptions
        // plus the sum of all sub channels subscriptions.
        var subscribersCount = json!!.getLong("followersCount")
        var accountVideoChannelUrl: String = baseUrl + PeertubeChannelLinkHandlerFactory.API_ENDPOINT
        accountVideoChannelUrl += if (id.contains(ACCOUNTS)) id else ACCOUNTS + id
        accountVideoChannelUrl += "/video-channels"

        try {
            val responseBody = downloader.get(accountVideoChannelUrl).responseBody()
            val jsonResponse = JsonParser.`object`().from(responseBody)
            val videoChannels = jsonResponse.getArray("data")
            for (videoChannel in videoChannels) {
                val videoChannelJsonObject = videoChannel as JsonObject
                subscribersCount += videoChannelJsonObject.getInt("followersCount").toLong()
            }
        } catch (ignored: IOException) {
            // something went wrong during video channels extraction,
            // only return subscribers of ownerAccount
        } catch (ignored: JsonParserException) {
        } catch (ignored: ReCaptchaException) {
        }
        return subscribersCount
    }

    override fun getDescription(): String {
        return json?.getString("description")?: ""
    }

    override fun getParentChannelName(): String {
        return ""
    }

    override fun getParentChannelUrl(): String {
        return ""
    }


    override fun getParentChannelAvatars(): List<Image> {
        return listOf()
    }

    @Throws(ParsingException::class)
    override fun isVerified(): Boolean {
        return false
    }


    @Throws(ParsingException::class)
    override fun getTabs(): List<ListLinkHandler> {
        return listOf(
            PeertubeChannelTabLinkHandlerFactory.instance.fromQuery(id, listOf(ChannelTabs.VIDEOS), "", baseUrl),
            PeertubeChannelTabLinkHandlerFactory.instance.fromQuery(id, listOf(ChannelTabs.CHANNELS), "", baseUrl))
    }

    @Throws(IOException::class, ExtractionException::class)
    override fun onFetchPage(downloader: Downloader) {
        val response = downloader.get(baseUrl + PeertubeChannelLinkHandlerFactory.API_ENDPOINT + id)
        if (response != null) {
            setInitialData(response.responseBody())
        } else {
            throw ExtractionException("Unable to extract PeerTube account data")
        }
    }

    @Throws(ExtractionException::class)
    private fun setInitialData(responseBody: String) {
        try {
            json = JsonParser.`object`().from(responseBody)
        } catch (e: JsonParserException) {
            throw ExtractionException("Unable to extract PeerTube account data", e)
        }
        if (json == null) {
            throw ExtractionException("Unable to extract PeerTube account data")
        }
    }


    @Throws(ParsingException::class)
    override fun getName(): String {
        return getString(json!!, "displayName")
    }

    companion object {
        private const val ACCOUNTS = "accounts/"
    }
}
