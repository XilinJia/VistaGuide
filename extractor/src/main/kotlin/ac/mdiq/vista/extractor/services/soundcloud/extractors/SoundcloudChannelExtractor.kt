package ac.mdiq.vista.extractor.services.soundcloud.extractors

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
import ac.mdiq.vista.extractor.services.soundcloud.SoundcloudParsingHelper
import ac.mdiq.vista.extractor.services.soundcloud.linkHandler.SoundcloudChannelTabLinkHandlerFactory.Companion.getUrlSuffix
import java.io.IOException


class SoundcloudChannelExtractor(service: StreamingService, linkHandler: ListLinkHandler) : ChannelExtractor(service, linkHandler) {


    override var id: String = ""
        private set

    private var user: JsonObject? = null

    @Throws(IOException::class, ExtractionException::class)
    override fun onFetchPage(downloader: Downloader) {
        id = getLinkHandler().id
        val apiUrl = (USERS_ENDPOINT + id + "?client_id=" + SoundcloudParsingHelper.clientId())

        val response = downloader.get(apiUrl, extractorLocalization).responseBody()
        try {
            user = JsonParser.`object`().from(response)
        } catch (e: JsonParserException) {
            throw ParsingException("Could not parse json response", e)
        }
    }


    override fun getName(): String {
        return user!!.getString("username")
    }


    override fun getAvatars(): List<Image> {
        return SoundcloudParsingHelper.getAllImagesFromArtworkOrAvatarUrl(user!!.getString("avatar_url"))
    }


    override fun getBanners(): List<Image> {
        return SoundcloudParsingHelper.getAllImagesFromVisualUrl(user!!.getObject("visuals")
            .getArray("visuals")
            .getObject(0)
            .getString("visual_url"))
    }

    override fun getFeedUrl(): String? {
        return null
    }

    override fun getSubscriberCount(): Long {
        return user!!.getLong("followers_count", 0)
    }

    override fun getDescription(): String {
        return user?.getString("description", "") ?: ""
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
        return user!!.getBoolean("verified")
    }


    @Throws(ParsingException::class)
    override fun getTabs(): List<ListLinkHandler> {
//        val url = url
        val urlTracks = (url + getUrlSuffix(ChannelTabs.TRACKS))
        val urlPlaylists = (url + getUrlSuffix(ChannelTabs.PLAYLISTS))
        val urlAlbums = (url + getUrlSuffix(ChannelTabs.ALBUMS))
        val id = id

        return listOf(
            ListLinkHandler(urlTracks, urlTracks, id, listOf(ChannelTabs.TRACKS), ""),
            ListLinkHandler(urlPlaylists, urlPlaylists, id, listOf(ChannelTabs.PLAYLISTS), ""),
            ListLinkHandler(urlAlbums, urlAlbums, id, listOf(ChannelTabs.ALBUMS), ""))
    }

    companion object {
        private const val USERS_ENDPOINT = SoundcloudParsingHelper.SOUNDCLOUD_API_V2_URL + "users/"
    }
}
