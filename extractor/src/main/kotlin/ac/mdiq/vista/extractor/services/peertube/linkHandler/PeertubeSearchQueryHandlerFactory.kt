package ac.mdiq.vista.extractor.services.peertube.linkHandler

import ac.mdiq.vista.extractor.ServiceList
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.linkhandler.SearchQueryHandlerFactory
import ac.mdiq.vista.extractor.utils.Utils.encodeUrlUtf8

class PeertubeSearchQueryHandlerFactory private constructor() : SearchQueryHandlerFactory() {
    @Throws(ParsingException::class, UnsupportedOperationException::class)
    override fun getUrl(id: String, contentFilters: List<String>, sortFilter: String?): String {
        val baseUrl = if (contentFilters.isNotEmpty() && contentFilters[0].startsWith("sepia_")) SEPIA_BASE_URL else ServiceList.PeerTube.baseUrl
        return getUrl(id, contentFilters, sortFilter, baseUrl)
    }

    @Throws(ParsingException::class, UnsupportedOperationException::class)
    override fun getUrl(id: String, contentFilter: List<String>, sortFilter: String?, baseUrl: String?): String {
        val endpoint = when {
            contentFilter.isEmpty() || contentFilter[0] == VIDEOS || contentFilter[0] == SEPIA_VIDEOS -> SEARCH_ENDPOINT_VIDEOS
            contentFilter[0] == CHANNELS -> SEARCH_ENDPOINT_CHANNELS
            else -> SEARCH_ENDPOINT_PLAYLISTS
        }
        return baseUrl + endpoint + "?search=" + encodeUrlUtf8(id)
    }

    override val availableContentFilter: Array<String>
        get() = arrayOf(VIDEOS, PLAYLISTS, CHANNELS, SEPIA_VIDEOS)

    companion object {
        val instance: PeertubeSearchQueryHandlerFactory = PeertubeSearchQueryHandlerFactory()

        const val VIDEOS: String = "videos"
        const val SEPIA_VIDEOS: String = "sepia_videos" // sepia is the global index
        const val PLAYLISTS: String = "playlists"
        const val CHANNELS: String = "channels"
        const val SEPIA_BASE_URL: String = "https://sepiasearch.org"
        const val SEARCH_ENDPOINT_PLAYLISTS: String = "/api/v1/search/video-playlists"
        const val SEARCH_ENDPOINT_VIDEOS: String = "/api/v1/search/videos"
        const val SEARCH_ENDPOINT_CHANNELS: String = "/api/v1/search/video-channels"
    }
}
