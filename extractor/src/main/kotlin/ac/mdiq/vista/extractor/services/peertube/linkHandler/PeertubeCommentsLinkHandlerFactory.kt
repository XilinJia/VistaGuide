package ac.mdiq.vista.extractor.services.peertube.linkHandler

import ac.mdiq.vista.extractor.ServiceList
import ac.mdiq.vista.extractor.exceptions.FoundAdException
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.linkhandler.ListLinkHandlerFactory
import java.net.MalformedURLException
import java.net.URL

class PeertubeCommentsLinkHandlerFactory private constructor() : ListLinkHandlerFactory() {
    @Throws(ParsingException::class, UnsupportedOperationException::class)
    override fun getId(url: String): String {
        return PeertubeStreamLinkHandlerFactory.instance.getId(url) // the same id is needed
    }

    @Throws(FoundAdException::class)
    override fun onAcceptUrl(url: String): Boolean {
        try {
            URL(url)
            return url.contains("/videos/") || url.contains("/w/")
        } catch (e: MalformedURLException) {
            return false
        }
    }

    @Throws(ParsingException::class, UnsupportedOperationException::class)
    override fun getUrl(id: String, contentFilters: List<String>, sortFilter: String?): String {
        return getUrl(id, contentFilters, sortFilter, ServiceList.PeerTube.baseUrl)
    }

    @Throws(ParsingException::class, UnsupportedOperationException::class)
    override fun getUrl(id: String, contentFilter: List<String>, sortFilter: String?, baseUrl: String?): String {
        return baseUrl + String.format(COMMENTS_ENDPOINT, id)
    }

    companion object {
        val instance: PeertubeCommentsLinkHandlerFactory = PeertubeCommentsLinkHandlerFactory()
        private const val COMMENTS_ENDPOINT = "/api/v1/videos/%s/comment-threads"
    }
}
