package ac.mdiq.vista.extractor.services.peertube.linkHandler

import ac.mdiq.vista.extractor.ServiceList
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.linkhandler.ListLinkHandlerFactory
import ac.mdiq.vista.extractor.utils.Parser.matchGroup
import ac.mdiq.vista.extractor.utils.Parser.matchGroup1
import java.net.MalformedURLException
import java.net.URL


class PeertubePlaylistLinkHandlerFactory private constructor() : ListLinkHandlerFactory() {
    @Throws(ParsingException::class, UnsupportedOperationException::class)
    override fun getUrl(id: String, contentFilters: List<String>, sortFilter: String?): String {
        return getUrl(id, contentFilters, sortFilter, ServiceList.PeerTube.baseUrl)
    }

    @Throws(ParsingException::class, UnsupportedOperationException::class)
    override fun getUrl(id: String, contentFilter: List<String>, sortFilter: String?, baseUrl: String?): String {
        return "$baseUrl/api/v1/video-playlists/$id"
    }

    @Throws(ParsingException::class, UnsupportedOperationException::class)
    override fun getId(url: String): String {
        try {
            return matchGroup(ID_PATTERN, url, 2)
        } catch (ignored: ParsingException) {
            // might also be an API url, no reason to throw an exception here
        }
        return matchGroup1(API_ID_PATTERN, url)
    }

    override fun onAcceptUrl(url: String): Boolean {
        try {
            URL(url)
            getId(url)
            return true
        } catch (e: ParsingException) {
            return false
        } catch (e: MalformedURLException) {
            return false
        }
    }

    companion object {
        val instance
                : PeertubePlaylistLinkHandlerFactory = PeertubePlaylistLinkHandlerFactory()
        private const val ID_PATTERN = "(/videos/watch/playlist/|/w/p/)([^/?&#]*)"
        private const val API_ID_PATTERN = "/video-playlists/([^/?&#]*)"
    }
}
