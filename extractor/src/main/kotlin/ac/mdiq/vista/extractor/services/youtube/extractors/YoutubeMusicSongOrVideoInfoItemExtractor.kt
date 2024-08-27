package ac.mdiq.vista.extractor.services.youtube.extractors

import com.grack.nanojson.JsonArray
import com.grack.nanojson.JsonObject
import ac.mdiq.vista.extractor.Image
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.localization.DateWrapper
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.getImagesFromThumbnailsArray
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.getTextFromObject
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.getUrlFromNavigationEndpoint
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.parseDurationString
import ac.mdiq.vista.extractor.services.youtube.linkHandler.YoutubeSearchQueryHandlerFactory
import ac.mdiq.vista.extractor.stream.StreamInfoItemExtractor
import ac.mdiq.vista.extractor.stream.StreamType
import ac.mdiq.vista.extractor.utils.Parser.RegexException
import ac.mdiq.vista.extractor.utils.Utils.mixedNumberWordToLong


class YoutubeMusicSongOrVideoInfoItemExtractor(
        private val songOrVideoInfoItem: JsonObject,
        private val descriptionElements: JsonArray,
        private val searchType: String)
    : StreamInfoItemExtractor {

    @get:Throws(ParsingException::class)
    override val url: String
        get() {
            val id = songOrVideoInfoItem.getObject("playlistItemData").getString("videoId")
            if (!id.isNullOrEmpty()) return "https://music.youtube.com/watch?v=$id"
            throw ParsingException("Could not get URL")
        }

    @get:Throws(ParsingException::class)
    override val name: String
        get() {
            val name = getTextFromObject(songOrVideoInfoItem.getArray("flexColumns")
                .getObject(0)
                .getObject("musicResponsiveListItemFlexColumnRenderer")
                .getObject("text"))
            if (!name.isNullOrEmpty()) return name
            throw ParsingException("Could not get name")
        }

    override fun getStreamType(): StreamType {
        return StreamType.VIDEO_STREAM
    }

    override fun isAd(): Boolean {
        return false
    }

    @Throws(ParsingException::class)
    override fun getDuration(): Long {
        val duration = descriptionElements.getObject(descriptionElements.size - 1).getString("text")
        if (!duration.isNullOrEmpty()) return parseDurationString(duration).toLong()
        throw ParsingException("Could not get duration")
    }

    @Throws(ParsingException::class)
    override fun getUploaderName(): String {
        val name = descriptionElements.getObject(0).getString("text")
        if (!name.isNullOrEmpty()) return name
        throw ParsingException("Could not get uploader name")
    }

    @Throws(ParsingException::class)
    override fun getUploaderUrl(): String? {
        if (searchType == YoutubeSearchQueryHandlerFactory.MUSIC_VIDEOS) {
            val items = songOrVideoInfoItem.getObject("menu")
                .getObject("menuRenderer")
                .getArray("items")
            for (item in items) {
                val menuNavigationItemRenderer = (item as JsonObject).getObject("menuNavigationItemRenderer")
                if (menuNavigationItemRenderer.getObject("icon").getString("iconType", "") == "ARTIST")
                    return getUrlFromNavigationEndpoint(menuNavigationItemRenderer.getObject("navigationEndpoint"))
            }
            return null
        } else {
            val navigationEndpointHolder = songOrVideoInfoItem.getArray("flexColumns")
                .getObject(1)
                .getObject("musicResponsiveListItemFlexColumnRenderer")
                .getObject("text")
                .getArray("runs")
                .getObject(0)

            if (!navigationEndpointHolder.has("navigationEndpoint")) return null
            val url = getUrlFromNavigationEndpoint(navigationEndpointHolder.getObject("navigationEndpoint"))
            if (!url.isNullOrEmpty()) return url
            throw ParsingException("Could not get uploader URL")
        }
    }

    override fun isUploaderVerified(): Boolean {
        // We don't have the ability to know this information on YouTube Music
        return false
    }

    override fun getTextualUploadDate(): String? {
        return null
    }

    override fun getUploadDate(): DateWrapper? {
        return null
    }

    @Throws(ParsingException::class)
    override fun getViewCount(): Long {
        if (searchType == YoutubeSearchQueryHandlerFactory.MUSIC_SONGS) return -1
        val viewCount = descriptionElements
            .getObject(descriptionElements.size - 3)
            .getString("text")
        if (!viewCount.isNullOrEmpty()) {
            return try {
                mixedNumberWordToLong(viewCount)
            } catch (e: RegexException) {
                // probably viewCount == "No views" or similar
                0
            }
        }
        throw ParsingException("Could not get view count")
    }

    @get:Throws(ParsingException::class)

    override val thumbnails: List<Image>
        get() {
            try {
                return getImagesFromThumbnailsArray(
                    songOrVideoInfoItem.getObject("thumbnail")
                        .getObject("musicThumbnailRenderer")
                        .getObject("thumbnail")
                        .getArray("thumbnails"))
            } catch (e: Exception) {
                throw ParsingException("Could not get thumbnails", e)
            }
        }
}
