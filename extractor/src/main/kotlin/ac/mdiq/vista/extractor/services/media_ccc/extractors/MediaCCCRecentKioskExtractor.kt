package ac.mdiq.vista.extractor.services.media_ccc.extractors

import com.grack.nanojson.JsonObject
import ac.mdiq.vista.extractor.Image
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.localization.DateWrapper
import ac.mdiq.vista.extractor.services.media_ccc.extractors.MediaCCCParsingHelper.getImageListFromLogoImageUrl
import ac.mdiq.vista.extractor.services.media_ccc.linkHandler.MediaCCCConferenceLinkHandlerFactory
import ac.mdiq.vista.extractor.stream.StreamInfoItemExtractor
import ac.mdiq.vista.extractor.stream.StreamType
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter


class MediaCCCRecentKioskExtractor(private val event: JsonObject) : StreamInfoItemExtractor {
    @get:Throws(ParsingException::class)
    override val name: String
        get() = event.getString("title")

    @get:Throws(ParsingException::class)
    override val url: String
        get() = event.getString("frontend_link")

    @get:Throws(ParsingException::class)

    override val thumbnails: List<Image>
        get() = getImageListFromLogoImageUrl(event.getString("poster_url"))

    @Throws(ParsingException::class)
    override fun getStreamType(): StreamType {
        return StreamType.VIDEO_STREAM
    }

    override fun isAd(): Boolean {
        return false
    }

    override fun getDuration(): Long {
        // duration and length have the same value, see
        // https://github.com/voc/voctoweb/blob/master/app/views/public/shared/_event.json.jbuilder
        return event.getInt("duration").toLong()
    }

    @Throws(ParsingException::class)
    override fun getViewCount(): Long {
        return event.getInt("view_count").toLong()
    }

    @Throws(ParsingException::class)
    override fun getUploaderName(): String? {
        return event.getString("conference_title")
    }

    @Throws(ParsingException::class)
    override fun getUploaderUrl(): String {
        return MediaCCCConferenceLinkHandlerFactory.instance.fromUrl(event.getString("conference_url")).url // web URL
    }

    @Throws(ParsingException::class)
    override fun isUploaderVerified(): Boolean {
        return false
    }

    @Throws(ParsingException::class)
    override fun getTextualUploadDate(): String? {
        return event.getString("date")
    }

    @Throws(ParsingException::class)
    override fun getUploadDate(): DateWrapper {
        val zonedDateTime = ZonedDateTime.parse(event.getString("date"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSzzzz"))
        return DateWrapper(zonedDateTime.toOffsetDateTime(), false)
    }
}
