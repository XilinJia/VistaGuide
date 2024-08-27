package ac.mdiq.vista.extractor.services.youtube.extractors

import org.jsoup.nodes.Element
import ac.mdiq.vista.extractor.Image
import ac.mdiq.vista.extractor.Image.ResolutionLevel.Companion.fromHeight
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.localization.DateWrapper
import ac.mdiq.vista.extractor.stream.StreamInfoItemExtractor
import ac.mdiq.vista.extractor.stream.StreamType
import java.time.OffsetDateTime
import java.time.format.DateTimeParseException


class YoutubeFeedInfoItemExtractor(private val entryElement: Element) : StreamInfoItemExtractor {
    override fun getStreamType(): StreamType {
        // It is not possible to determine the stream type using the feed endpoint.
        // All entries are considered a video stream.
        return StreamType.VIDEO_STREAM
    }

    override fun isAd(): Boolean {
        return false
    }

    override fun getDuration(): Long {
        // Not available when fetching through the feed endpoint.
        return -1
    }

    override fun getViewCount(): Long {
        return entryElement.getElementsByTag("media:statistics").first()?.attr("views")?.toLong() ?: 0
    }

    override fun getUploaderName(): String? {
        return entryElement.select("author > name").first()?.text()
    }

    override fun getUploaderUrl(): String? {
        return entryElement.select("author > uri").first()?.text()
    }

    @Throws(ParsingException::class)
    override fun isUploaderVerified(): Boolean {
        return false
    }

    override fun getTextualUploadDate(): String? {
        return entryElement.getElementsByTag("published").first()?.text()
    }

    @Throws(ParsingException::class)
    override fun getUploadDate(): DateWrapper? {
        try {
            val tDate = getTextualUploadDate() ?: return null
            return DateWrapper(OffsetDateTime.parse(tDate))
        } catch (e: DateTimeParseException) {
            throw ParsingException("Could not parse date (\"" + getTextualUploadDate() + "\")", e)
        }
    }

    override val name: String
        get() = entryElement.getElementsByTag("title").first()?.text() ?: ""

    override val url: String
        get() = entryElement.getElementsByTag("link").first()?.attr("href") ?: ""


    override val thumbnails: List<Image>
        get() {
            val thumbnailElement = entryElement.getElementsByTag("media:thumbnail").first() ?: return listOf()
            val feedThumbnailUrl = thumbnailElement.attr("url")
            // If the thumbnail URL is empty, it means that no thumbnail is available, return an empty
            // list in this case
            if (feedThumbnailUrl.isEmpty()) return listOf()

            // The hqdefault thumbnail has some black bars at the top and at the bottom, while the
            // mqdefault doesn't, so return the mqdefault one. It should always exist, according to
            // https://stackoverflow.com/a/20542029/9481500.
            val newFeedThumbnailUrl = feedThumbnailUrl.replace("hqdefault", "mqdefault")

            val height: Int
            val width: Int

            // If the new thumbnail URL is equal to the feed one, it means that a different image
            // resolution is used on feeds, so use the height and width provided instead of the
            // mqdefault ones
            if (newFeedThumbnailUrl == feedThumbnailUrl) {
                height = try {
                    thumbnailElement.attr("height").toInt()
                } catch (e: NumberFormatException) {
                    Image.HEIGHT_UNKNOWN
                }

                width = try {
                    thumbnailElement.attr("width").toInt()
                } catch (e: NumberFormatException) {
                    Image.WIDTH_UNKNOWN
                }
            } else {
                height = 320
                width = 180
            }

            return listOf(Image(newFeedThumbnailUrl, height, width, fromHeight(height)))
        }
}
