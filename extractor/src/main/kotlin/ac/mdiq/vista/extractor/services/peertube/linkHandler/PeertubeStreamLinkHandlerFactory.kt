package ac.mdiq.vista.extractor.services.peertube.linkHandler

import ac.mdiq.vista.extractor.ServiceList
import ac.mdiq.vista.extractor.exceptions.FoundAdException
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.linkhandler.LinkHandlerFactory
import ac.mdiq.vista.extractor.utils.Parser.matchGroup
import java.net.MalformedURLException
import java.net.URL

class PeertubeStreamLinkHandlerFactory private constructor() : LinkHandlerFactory() {
    @Throws(ParsingException::class, UnsupportedOperationException::class)
    override fun getUrl(id: String): String {
        return getUrl(id, ServiceList.PeerTube.baseUrl)
    }

    override fun getUrl(id: String, baseUrl: String): String {
        return baseUrl + VIDEO_PATH + id
    }

    @Throws(ParsingException::class, UnsupportedOperationException::class)
    override fun getId(url: String): String {
        return matchGroup(ID_PATTERN, url, 4)
    }

    @Throws(FoundAdException::class)
    override fun onAcceptUrl(url: String): Boolean {
        if (url.contains("/playlist/")) {
            return false
        }
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
                : PeertubeStreamLinkHandlerFactory = PeertubeStreamLinkHandlerFactory()
        private const val ID_PATTERN = "(/w/|(/videos/(watch/|embed/)?))(?!p/)([^/?&#]*)"

        // we exclude p/ because /w/p/ is playlist, not video
        const val VIDEO_API_ENDPOINT: String = "/api/v1/videos/"

        // From PeerTube 3.3.0, the default path is /w/.
        // We still use /videos/watch/ for compatibility reasons:
        // /videos/watch/ is still accepted by >=3.3.0 but /w/ isn't by <3.3.0
        private const val VIDEO_PATH = "/videos/watch/"
    }
}
