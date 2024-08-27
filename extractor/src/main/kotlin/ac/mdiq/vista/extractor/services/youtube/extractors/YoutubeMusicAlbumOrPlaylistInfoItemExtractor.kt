package ac.mdiq.vista.extractor.services.youtube.extractors

import com.grack.nanojson.JsonArray
import com.grack.nanojson.JsonObject
import ac.mdiq.vista.extractor.Image
import ac.mdiq.vista.extractor.ListExtractor
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.playlist.PlaylistInfoItemExtractor
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.getImagesFromThumbnailsArray
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.getTextFromObject
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.getUrlFromNavigationEndpoint
import ac.mdiq.vista.extractor.services.youtube.linkHandler.YoutubeSearchQueryHandlerFactory
import ac.mdiq.vista.extractor.utils.Utils.removeNonDigitCharacters


class YoutubeMusicAlbumOrPlaylistInfoItemExtractor(
        private val albumOrPlaylistInfoItem: JsonObject,
        private val descriptionElements: JsonArray,
        private val searchType: String)
    : PlaylistInfoItemExtractor {

    @get:Throws(ParsingException::class)

    override val thumbnails: List<Image>
        get() {
            try {
                return getImagesFromThumbnailsArray(
                    albumOrPlaylistInfoItem.getObject("thumbnail")
                        .getObject("musicThumbnailRenderer")
                        .getObject("thumbnail")
                        .getArray("thumbnails"))
            } catch (e: Exception) {
                throw ParsingException("Could not get thumbnails", e)
            }
        }

    @get:Throws(ParsingException::class)
    override val name: String
        get() {
            val name = getTextFromObject(albumOrPlaylistInfoItem.getArray("flexColumns")
                .getObject(0)
                .getObject("musicResponsiveListItemFlexColumnRenderer")
                .getObject("text"))

            if (!name.isNullOrEmpty()) return name
            throw ParsingException("Could not get name")
        }

    @get:Throws(ParsingException::class)
    override val url: String
        get() {
            var playlistId = albumOrPlaylistInfoItem.getObject("menu")
                .getObject("menuRenderer")
                .getArray("items")
                .getObject(4)
                .getObject("toggleMenuServiceItemRenderer")
                .getObject("toggledServiceEndpoint")
                .getObject("likeEndpoint")
                .getObject("target")
                .getString("playlistId")

            if (playlistId.isNullOrEmpty()) {
                playlistId = albumOrPlaylistInfoItem.getObject("overlay")
                    .getObject("musicItemThumbnailOverlayRenderer")
                    .getObject("content")
                    .getObject("musicPlayButtonRenderer")
                    .getObject("playNavigationEndpoint")
                    .getObject("watchPlaylistEndpoint")
                    .getString("playlistId")
            }

            if (!playlistId.isNullOrEmpty()) return "https://music.youtube.com/playlist?list=$playlistId"
            throw ParsingException("Could not get URL")
        }

    @Throws(ParsingException::class)
    override fun getUploaderName(): String {
        val name = if (searchType == YoutubeSearchQueryHandlerFactory.MUSIC_ALBUMS) descriptionElements.getObject(2).getString("text")
        else descriptionElements.getObject(0).getString("text")
        if (!name.isNullOrEmpty()) return name
        throw ParsingException("Could not get uploader name")
    }

    @Throws(ParsingException::class)
    override fun getUploaderUrl(): String? {
        if (searchType == YoutubeSearchQueryHandlerFactory.MUSIC_PLAYLISTS) return null

        val items = albumOrPlaylistInfoItem.getObject("menu")
            .getObject("menuRenderer")
            .getArray("items")
        for (item in items) {
            val menuNavigationItemRenderer = (item as JsonObject).getObject("menuNavigationItemRenderer")
            if (menuNavigationItemRenderer.getObject("icon").getString("iconType", "") == "ARTIST")
                return getUrlFromNavigationEndpoint(menuNavigationItemRenderer.getObject("navigationEndpoint"))
        }
        throw ParsingException("Could not get uploader URL")
    }

    @Throws(ParsingException::class)
    override fun isUploaderVerified(): Boolean {
        return false
    }

    @Throws(ParsingException::class)
    override fun getStreamCount(): Long {
        if (searchType == YoutubeSearchQueryHandlerFactory.MUSIC_ALBUMS) return ListExtractor.ITEM_COUNT_UNKNOWN
        val count = descriptionElements.getObject(2).getString("text")

        if (!count.isNullOrEmpty())
            return if (count.contains("100+")) ListExtractor.ITEM_COUNT_MORE_THAN_100 else { removeNonDigitCharacters(count).toLong() }

        throw ParsingException("Could not get stream count")
    }
}
