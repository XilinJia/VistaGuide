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
import ac.mdiq.vista.extractor.linkhandler.ListLinkHandler
import ac.mdiq.vista.extractor.services.peertube.PeertubeParsingHelper.getAvatarsFromOwnerAccountOrVideoChannelObject
import ac.mdiq.vista.extractor.services.peertube.PeertubeParsingHelper.getBannersFromAccountOrVideoChannelObject
import ac.mdiq.vista.extractor.services.peertube.linkHandler.PeertubeChannelLinkHandlerFactory
import ac.mdiq.vista.extractor.services.peertube.linkHandler.PeertubeChannelTabLinkHandlerFactory
import ac.mdiq.vista.extractor.utils.JsonUtils.getString
import java.io.IOException


class PeertubeChannelExtractor(service: StreamingService, linkHandler: ListLinkHandler) : ChannelExtractor(service, linkHandler) {
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
        return baseUrl + "/feeds/videos.xml?videoChannelId=" + json!!["id"]
    }

    override fun getSubscriberCount(): Long {
        return json!!.getLong("followersCount")
    }

    override fun getDescription(): String {
        return json?.getString("description")?: ""
    }

    @Throws(ParsingException::class)
    override fun getParentChannelName(): String {
        return getString(json!!, "ownerAccount.name")
    }

    @Throws(ParsingException::class)
    override fun getParentChannelUrl(): String {
        return getString(json!!, "ownerAccount.url")
    }


    override fun getParentChannelAvatars(): List<Image> {
        return getAvatarsFromOwnerAccountOrVideoChannelObject(
            baseUrl, json!!.getObject("ownerAccount"))
    }

    @Throws(ParsingException::class)
    override fun isVerified(): Boolean {
        return false
    }


    @Throws(ParsingException::class)
    override fun getTabs(): List<ListLinkHandler> {
        return listOf(
            PeertubeChannelTabLinkHandlerFactory.instance.fromQuery(id, listOf(ChannelTabs.VIDEOS), "", baseUrl),
            PeertubeChannelTabLinkHandlerFactory.instance.fromQuery(id, listOf(ChannelTabs.PLAYLISTS), "", baseUrl))
    }

    @Throws(IOException::class, ExtractionException::class)
    override fun onFetchPage(downloader: Downloader) {
        val response = downloader.get(
            baseUrl + PeertubeChannelLinkHandlerFactory.API_ENDPOINT + id)
        if (response != null) {
            setInitialData(response.responseBody())
        } else {
            throw ExtractionException("Unable to extract PeerTube channel data")
        }
    }

    @Throws(ExtractionException::class)
    private fun setInitialData(responseBody: String) {
        try {
            json = JsonParser.`object`().from(responseBody)
        } catch (e: JsonParserException) {
            throw ExtractionException("Unable to extract PeerTube channel data", e)
        }
        if (json == null) {
            throw ExtractionException("Unable to extract PeerTube channel data")
        }
    }


    @Throws(ParsingException::class)
    override fun getName(): String {
        return getString(json!!, "displayName")
    }
}
