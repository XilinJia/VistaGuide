package ac.mdiq.vista.extractor.services.youtube.extractors

import ac.mdiq.vista.extractor.Image
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.localization.DateWrapper
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.getImagesFromThumbnailsArray
import ac.mdiq.vista.extractor.services.youtube.linkHandler.YoutubeStreamLinkHandlerFactory
import ac.mdiq.vista.extractor.stream.StreamInfoItemExtractor
import ac.mdiq.vista.extractor.stream.StreamType
import ac.mdiq.vista.extractor.utils.Utils
import com.grack.nanojson.JsonObject
import java.util.*


/**
 * A [StreamInfoItemExtractor] for YouTube's `shortsLockupViewModel`s.
 *
 *
 *
 * `shortsLockupViewModel`s are returned on YouTube for their short-form contents on almost
 * every place and every major client. They provide a limited amount of information and do not
 * provide the exact view count, any uploader info (name, URL, avatar, verified status) and the
 * upload date.
 *
 *
 *
 *
 * At the time this documentation has been written, this data UI type is not fully used (rolled
 * out), so `reelItemRenderer`s are also returned. See [YoutubeReelInfoItemExtractor]
 * for an extractor for this UI data type.
 *
 */
open class YoutubeShortsLockupInfoItemExtractor(private val shortsLockupViewModel: JsonObject) : StreamInfoItemExtractor {
    @get:Throws(ParsingException::class)
    override val name: String
        get() = shortsLockupViewModel.getObject("overlayMetadata")
            .getObject("primaryText")
            .getString("content")

    @get:Throws(ParsingException::class)
    override val url: String
        get() {
            var videoId = shortsLockupViewModel.getObject("onTap")
                .getObject("innertubeCommand")
                .getObject("reelWatchEndpoint")
                .getString("videoId")

            if (videoId.isNullOrEmpty()) {
                videoId = shortsLockupViewModel.getObject("inlinePlayerData")
                    .getObject("onVisible")
                    .getObject("innertubeCommand")
                    .getObject("watchEndpoint")
                    .getString("videoId")
            }

            if (videoId.isNullOrEmpty()) {
                throw ParsingException("Could not get video ID")
            }

            try {
                return YoutubeStreamLinkHandlerFactory.instance.getUrl(videoId)
            } catch (e: Exception) {
                throw ParsingException("Could not get URL", e)
            }
        }

    @get:Throws(ParsingException::class)
    override val thumbnails: MutableList<Image>
        get() = getImagesFromThumbnailsArray(shortsLockupViewModel.getObject("thumbnail").getArray("sources")).toMutableList()

    @Throws(ParsingException::class)
    override fun getStreamType(): StreamType {
        return StreamType.VIDEO_STREAM
    }

    @Throws(ParsingException::class)
    override fun getViewCount(): Long {
        val viewCountText = shortsLockupViewModel.getObject("overlayMetadata").getObject("secondaryText").getString("content")
        if (!viewCountText.isNullOrEmpty()) {
            // This approach is language dependent
            if (viewCountText.lowercase(Locale.getDefault()).contains("no views")) return 0
            return Utils.mixedNumberWordToLong(viewCountText)
        }

        throw ParsingException("Could not get short view count")
    }

    override fun isShortFormContent(): Boolean {
        return true
    }

    // All the following properties cannot be obtained from shortsLockupViewModels
    @Throws(ParsingException::class)
    override fun isAd(): Boolean {
        return false
    }

    @Throws(ParsingException::class)
    override fun getDuration(): Long {
        return -1
    }

    @Throws(ParsingException::class)
    override fun getUploaderName(): String? {
        return null
    }

    @Throws(ParsingException::class)
    override fun getUploaderUrl(): String? {
        return null
    }

    @Throws(ParsingException::class)
    override fun isUploaderVerified(): Boolean {
        return false
    }

    @Throws(ParsingException::class)
    override fun getTextualUploadDate(): String? {
        return null
    }

    @Throws(ParsingException::class)
    override fun getUploadDate(): DateWrapper? {
        return null
    }
}
