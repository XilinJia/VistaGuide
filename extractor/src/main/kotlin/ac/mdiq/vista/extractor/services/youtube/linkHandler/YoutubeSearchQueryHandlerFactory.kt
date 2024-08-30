package ac.mdiq.vista.extractor.services.youtube.linkHandler

import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.linkhandler.SearchQueryHandlerFactory
import ac.mdiq.vista.extractor.utils.Utils.encodeUrlUtf8


class YoutubeSearchQueryHandlerFactory : SearchQueryHandlerFactory() {

    override val availableContentFilter: Array<String>
        get() = arrayOf(ALL,
            VIDEOS,
            CHANNELS,
            PLAYLISTS,
            MUSIC_SONGS,
            MUSIC_VIDEOS,
            MUSIC_ALBUMS,
            MUSIC_PLAYLISTS // MUSIC_ARTISTS
        )

    @Throws(ParsingException::class, UnsupportedOperationException::class)
    override fun getUrl(id: String, contentFilters: List<String>, sortFilter: String?): String {
        val contentFilter = if (contentFilters.isNotEmpty()) contentFilters[0] else ""
        return when (contentFilter) {
            VIDEOS -> (SEARCH_URL + encodeUrlUtf8(id)) + "&sp=EgIQAfABAQ%253D%253D"
            CHANNELS -> (SEARCH_URL + encodeUrlUtf8(id)) + "&sp=EgIQAvABAQ%253D%253D"
            PLAYLISTS -> (SEARCH_URL + encodeUrlUtf8(id)) + "&sp=EgIQA_ABAQ%253D%253D"
            MUSIC_SONGS, MUSIC_VIDEOS, MUSIC_ALBUMS, MUSIC_PLAYLISTS, MUSIC_ARTISTS -> MUSIC_SEARCH_URL + encodeUrlUtf8(id)
            else -> (SEARCH_URL + encodeUrlUtf8(id)) + "&sp=8AEB"
        }
    }

    companion object {
        val instance: YoutubeSearchQueryHandlerFactory = YoutubeSearchQueryHandlerFactory()

        const val ALL: String = "all"
        const val VIDEOS: String = "videos"
        const val CHANNELS: String = "channels"
        const val PLAYLISTS: String = "playlists"

        const val MUSIC_SONGS: String = "music_songs"
        const val MUSIC_VIDEOS: String = "music_videos"
        const val MUSIC_ALBUMS: String = "music_albums"
        const val MUSIC_PLAYLISTS: String = "music_playlists"
        const val MUSIC_ARTISTS: String = "music_artists"

        private const val SEARCH_URL = "https://www.youtube.com/results?search_query="
        private const val MUSIC_SEARCH_URL = "https://music.youtube.com/search?q="

        fun getSearchParameter(contentFilter: String?): String {
            if (contentFilter.isNullOrEmpty()) return "8AEB"

            return when (contentFilter) {
                VIDEOS -> "EgIQAfABAQ%3D%3D"
                CHANNELS -> "EgIQAvABAQ%3D%3D"
                PLAYLISTS -> "EgIQA_ABAQ%3D%3D"
                MUSIC_SONGS, MUSIC_VIDEOS, MUSIC_ALBUMS, MUSIC_PLAYLISTS, MUSIC_ARTISTS -> ""
                else -> "8AEB"
            }
        }
    }
}
