package ac.mdiq.vista.extractor.services.media_ccc.extractors

import com.grack.nanojson.JsonObject
import ac.mdiq.vista.extractor.Image
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.localization.DateWrapper
import ac.mdiq.vista.extractor.stream.StreamInfoItemExtractor
import ac.mdiq.vista.extractor.stream.StreamType


class MediaCCCLiveStreamKioskExtractor(
        private val conferenceInfo: JsonObject,
        private val group: String,
        private val roomInfo: JsonObject) : StreamInfoItemExtractor {

    @get:Throws(ParsingException::class)
    override val name: String
        get() = roomInfo.getObject("talks").getObject("current").getString("title")

    @get:Throws(ParsingException::class)
    override val url: String
        get() = roomInfo.getString("link")

    @get:Throws(ParsingException::class)

    override val thumbnails: List<Image>
        get() = MediaCCCParsingHelper.getThumbnailsFromLiveStreamItem(roomInfo)

    @Throws(ParsingException::class)
    override fun getStreamType(): StreamType {
        var isVideo = false
        for (stream in roomInfo.getArray("streams")) {
            if ("video" == (stream as JsonObject).getString("type")) {
                isVideo = true
                break
            }
        }
        return if (isVideo) StreamType.LIVE_STREAM else StreamType.AUDIO_LIVE_STREAM
    }

    @Throws(ParsingException::class)
    override fun isAd(): Boolean {
        return false
    }

    @Throws(ParsingException::class)
    override fun getDuration(): Long {
        return 0
    }

    @Throws(ParsingException::class)
    override fun getViewCount(): Long {
        return -1
    }

    @Throws(ParsingException::class)
    override fun getUploaderName(): String {
        return (conferenceInfo.getString("conference") + " - " + group
                + " - " + roomInfo.getString("display"))
    }

    @Throws(ParsingException::class)
    override fun getUploaderUrl(): String {
        return "https://media.ccc.de/c/" + conferenceInfo.getString("slug")
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
