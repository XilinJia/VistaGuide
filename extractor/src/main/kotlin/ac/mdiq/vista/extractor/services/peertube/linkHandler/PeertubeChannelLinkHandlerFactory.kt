package ac.mdiq.vista.extractor.services.peertube.linkHandler

import ac.mdiq.vista.extractor.ServiceList
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.linkhandler.ListLinkHandlerFactory
import ac.mdiq.vista.extractor.utils.Parser.matchGroup
import java.net.MalformedURLException
import java.net.URL

class PeertubeChannelLinkHandlerFactory private constructor() : ListLinkHandlerFactory() {
    @Throws(ParsingException::class, UnsupportedOperationException::class)
    override fun getId(url: String): String {
        return fixId(matchGroup(ID_PATTERN, url, 0))
    }

    @Throws(ParsingException::class, UnsupportedOperationException::class)
    override fun getUrl(id: String, contentFilters: List<String>, sortFilter: String?): String {
        return getUrl(id, contentFilters, sortFilter, ServiceList.PeerTube.baseUrl)
    }

    @Throws(ParsingException::class, UnsupportedOperationException::class)
    override fun getUrl(id: String, contentFilter: List<String>, sortFilter: String?, baseUrl: String?): String {
        // This is needed for compatibility with older versions were we didn't support
        // video channels yet
        return if (id.matches(ID_PATTERN.toRegex())) baseUrl + "/" + fixId(id) else "$baseUrl/accounts/$id"
    }

    override fun onAcceptUrl(url: String): Boolean {
        try {
            URL(url)
            return (url.contains("/accounts/") || url.contains("/a/")
                    || url.contains("/video-channels/") || url.contains("/c/"))
        } catch (e: MalformedURLException) {
            return false
        }
    }

    /**
     * Fix id
     *
     *
     *
     * a/:accountName and c/:channelName ids are supported
     * by the PeerTube web client (>= v3.3.0)
     * but not by the API.
     *
     *
     * @param id the id to fix
     * @return the fixed id
     */
    private fun fixId(id: String): String {
        if (id.startsWith("a/")) {
            return "accounts" + id.substring(1)
        } else if (id.startsWith("c/")) {
            return "video-channels" + id.substring(1)
        }
        return id
    }

    companion object {
        val instance
                : PeertubeChannelLinkHandlerFactory = PeertubeChannelLinkHandlerFactory()
        private const val ID_PATTERN = "((accounts|a)|(video-channels|c))/([^/?&#]*)"
        const val API_ENDPOINT: String = "/api/v1/"
    }
}
