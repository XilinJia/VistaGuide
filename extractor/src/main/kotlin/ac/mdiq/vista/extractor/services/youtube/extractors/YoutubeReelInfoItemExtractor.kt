package ac.mdiq.vista.extractor.services.youtube.extractors

import com.grack.nanojson.JsonObject
import ac.mdiq.vista.extractor.Image
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.localization.DateWrapper
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.getTextFromObject
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.getThumbnailsFromInfoItem
import ac.mdiq.vista.extractor.services.youtube.linkHandler.YoutubeStreamLinkHandlerFactory
import ac.mdiq.vista.extractor.stream.StreamInfoItemExtractor
import ac.mdiq.vista.extractor.stream.StreamType
import ac.mdiq.vista.extractor.utils.Utils.mixedNumberWordToLong
import java.util.*


/**
 * A [StreamInfoItemExtractor] for YouTube's `reelItemRenderers`.
 *
 *
 *
 * `reelItemRenderers` are returned on YouTube for their short-form contents on almost every
 * place and every major client. They provide a limited amount of information and do not provide
 * the exact view count, any uploader info (name, URL, avatar, verified status) and the upload date.
 *
 */
open class YoutubeReelInfoItemExtractor(private val reelInfo: JsonObject) : StreamInfoItemExtractor {
    @get:Throws(ParsingException::class)
    override val name: String
        get() = getTextFromObject(reelInfo.getObject("headline")) ?: ""

    @get:Throws(ParsingException::class)
    override val url: String
        get() {
            try {
                val videoId = reelInfo.getString("videoId")
                return YoutubeStreamLinkHandlerFactory.instance.getUrl(videoId)
            } catch (e: Exception) {
                throw ParsingException("Could not get URL", e)
            }
        }

    @get:Throws(ParsingException::class)

    override val thumbnails: List<Image>
        get() = getThumbnailsFromInfoItem(reelInfo)

    @Throws(ParsingException::class)
    override fun getStreamType(): StreamType {
        return StreamType.VIDEO_STREAM
    }

    @Throws(ParsingException::class)
    override fun getViewCount(): Long {
        val viewCountText = getTextFromObject(reelInfo.getObject("viewCountText"))
        if (!viewCountText.isNullOrEmpty()) {
            // This approach is language dependent
            if (viewCountText.lowercase(Locale.getDefault()).contains("no views")) return 0
            return mixedNumberWordToLong(viewCountText)
        }
        throw ParsingException("Could not get short view count")
    }

    override fun isShortFormContent(): Boolean {
        return true
    }

    // All the following properties cannot be obtained from reelItemRenderers
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
