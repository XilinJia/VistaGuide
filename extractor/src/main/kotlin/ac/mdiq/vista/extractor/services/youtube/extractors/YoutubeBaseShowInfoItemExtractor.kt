package ac.mdiq.vista.extractor.services.youtube.extractors

import com.grack.nanojson.JsonObject
import ac.mdiq.vista.extractor.Image
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.playlist.PlaylistInfoItemExtractor
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.getTextFromObject
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.getThumbnailsFromInfoItem
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.getUrlFromNavigationEndpoint
import ac.mdiq.vista.extractor.utils.Utils.removeNonDigitCharacters


/**
 * The base [PlaylistInfoItemExtractor] for shows playlists UI elements.
 */
internal abstract class YoutubeBaseShowInfoItemExtractor(protected val showRenderer: JsonObject) : PlaylistInfoItemExtractor {

    @get:Throws(ParsingException::class)
    override val name: String
        get() = showRenderer.getString("title")

    @get:Throws(ParsingException::class)
    override val url: String
        get() = getUrlFromNavigationEndpoint(showRenderer.getObject("navigationEndpoint"))!!

    @get:Throws(ParsingException::class)

    override val thumbnails: List<Image>
        get() = getThumbnailsFromInfoItem(showRenderer.getObject("thumbnailRenderer")
            .getObject("showCustomThumbnailRenderer"))

    @Throws(ParsingException::class)
    override fun getStreamCount(): Long {
        // The stream count should be always returned in the first text object for English
        // localizations, but the complete text is parsed for reliability purposes
        val streamCountText = getTextFromObject(
            showRenderer.getObject("thumbnailOverlays")
                .getObject("thumbnailOverlayBottomPanelRenderer")
                .getObject("text"))
        if (streamCountText == null) {
            throw ParsingException("Could not get stream count")
        }

        try {
            // The data returned could be a human/shortened number, but no show with more than 1000
            // videos has been found at the time this code was written
            return removeNonDigitCharacters(streamCountText).toLong()
        } catch (e: NumberFormatException) {
            throw ParsingException("Could not convert stream count to a long", e)
        }
    }
}
