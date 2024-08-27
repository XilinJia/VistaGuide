package ac.mdiq.vista.extractor.services.media_ccc.extractors.infoItems

import com.grack.nanojson.JsonObject
import ac.mdiq.vista.extractor.Image
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.localization.DateWrapper
import ac.mdiq.vista.extractor.services.media_ccc.extractors.MediaCCCParsingHelper
import ac.mdiq.vista.extractor.stream.StreamInfoItemExtractor
import ac.mdiq.vista.extractor.stream.StreamType


class MediaCCCStreamInfoItemExtractor(private val event: JsonObject) : StreamInfoItemExtractor {
    override fun getStreamType(): StreamType {
        return StreamType.VIDEO_STREAM
    }

    override fun isAd(): Boolean {
        return false
    }

    override fun getDuration(): Long {
        return event.getInt("length").toLong()
    }

    override fun getViewCount(): Long {
        return event.getInt("view_count").toLong()
    }

    override fun getUploaderName(): String? {
        return event.getString("conference_title")
    }

    override fun getUploaderUrl(): String? {
        return event.getString("conference_url")
    }

    @Throws(ParsingException::class)
    override fun isUploaderVerified(): Boolean {
        return false
    }

    override fun getTextualUploadDate(): String? {
        return event.getString("release_date")
    }

    @Throws(ParsingException::class)
    override fun getUploadDate(): DateWrapper? {
        val date = getTextualUploadDate() ?: return null // event is in the future...
        return DateWrapper(MediaCCCParsingHelper.parseDateFrom(date))
    }

    @get:Throws(ParsingException::class)
    override val name: String
        get() = event.getString("title")

    @get:Throws(ParsingException::class)
    override val url: String
        get() = ("https://media.ccc.de/public/events/"
                + event.getString("guid"))


    override val thumbnails: List<Image>
        get() = MediaCCCParsingHelper.getThumbnailsFromStreamItem(event)
}
