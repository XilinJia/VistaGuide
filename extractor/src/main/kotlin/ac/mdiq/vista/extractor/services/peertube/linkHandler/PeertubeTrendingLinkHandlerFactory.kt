package ac.mdiq.vista.extractor.services.peertube.linkHandler

import ac.mdiq.vista.extractor.ServiceList
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.linkhandler.ListLinkHandlerFactory
import java.net.MalformedURLException
import java.net.URL

class PeertubeTrendingLinkHandlerFactory private constructor() : ListLinkHandlerFactory() {
    @Throws(ParsingException::class, UnsupportedOperationException::class)
    override fun getUrl(id: String, contentFilters: List<String>, sortFilter: String?): String {
        return getUrl(id, contentFilters, sortFilter, ServiceList.PeerTube.baseUrl)
    }

    @Throws(ParsingException::class, UnsupportedOperationException::class)
    override fun getUrl(id: String, contentFilter: List<String>, sortFilter: String?, baseUrl: String?): String {
        val f = KIOSK_MAP[id] ?: return ""
        return String.format(f, baseUrl)
    }

    @Throws(ParsingException::class, UnsupportedOperationException::class)
    override fun getId(url: String): String {
        val cleanUrl = url.replace(ServiceList.PeerTube.baseUrl, "%s")
        return when {
            cleanUrl.contains("/videos/trending") -> KIOSK_TRENDING
            cleanUrl.contains("/videos/most-liked") -> KIOSK_MOST_LIKED
            cleanUrl.contains("/videos/recently-added") -> KIOSK_RECENT
            cleanUrl.contains("/videos/local") -> KIOSK_LOCAL
            else -> KIOSK_MAP.entries.stream()
                .filter { entry: Map.Entry<String?, String> -> cleanUrl == entry.value }
                .findFirst()
                .map<String> { it.key }
                .orElseThrow { ParsingException("no id found for this url") }
        }
    }

    override fun onAcceptUrl(url: String): Boolean {
        try {
            URL(url)
            return (url.contains("/videos?") || url.contains("/videos/trending")
                    || url.contains("/videos/most-liked") || url.contains("/videos/recently-added")
                    || url.contains("/videos/local"))
        } catch (e: MalformedURLException) {
            return false
        }
    }

    companion object {
        val instance
                : PeertubeTrendingLinkHandlerFactory = PeertubeTrendingLinkHandlerFactory()

        const val KIOSK_TRENDING: String = "Trending"
        const val KIOSK_MOST_LIKED: String = "Most liked"
        const val KIOSK_RECENT: String = "Recently added"
        const val KIOSK_LOCAL: String = "Local"

        val KIOSK_MAP: Map<String?, String> = java.util.Map.of(
            KIOSK_TRENDING, "%s/api/v1/videos?sort=-trending",
            KIOSK_MOST_LIKED, "%s/api/v1/videos?sort=-likes",
            KIOSK_RECENT, "%s/api/v1/videos?sort=-publishedAt",
            KIOSK_LOCAL, "%s/api/v1/videos?sort=-publishedAt&isLocal=true")
    }
}
