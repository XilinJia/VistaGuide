package ac.mdiq.vista.extractor.services.soundcloud.extractors

import com.grack.nanojson.JsonObject
import ac.mdiq.vista.extractor.Image
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.localization.DateWrapper
import ac.mdiq.vista.extractor.services.soundcloud.SoundcloudParsingHelper
import ac.mdiq.vista.extractor.stream.StreamInfoItemExtractor
import ac.mdiq.vista.extractor.stream.StreamType
import ac.mdiq.vista.extractor.utils.Utils.replaceHttpWithHttps


class SoundcloudStreamInfoItemExtractor(private val itemObject: JsonObject) : StreamInfoItemExtractor {
    override val url: String
        get() = replaceHttpWithHttps(itemObject.getString("permalink_url"))

    override val name: String
        get() = itemObject.getString("title")

    override fun getDuration(): Long {
        return itemObject.getLong("duration") / 1000L
    }

    override fun getUploaderName(): String? {
        return itemObject.getObject("user").getString("username")
    }

    override fun getUploaderUrl(): String {
        return replaceHttpWithHttps(itemObject.getObject("user").getString("permalink_url"))
    }


    override fun getUploaderAvatars(): List<Image> {
        return SoundcloudParsingHelper.getAllImagesFromArtworkOrAvatarUrl(
            itemObject.getObject("user").getString("avatar_url"))
    }

    @Throws(ParsingException::class)
    override fun isUploaderVerified(): Boolean {
        return itemObject.getObject("user").getBoolean("verified")
    }

    override fun getTextualUploadDate(): String? {
        return itemObject.getString("created_at")
    }

    @Throws(ParsingException::class)
    override fun getUploadDate(): DateWrapper {
        return DateWrapper(SoundcloudParsingHelper.parseDateFrom(getTextualUploadDate()?:""))
    }

    override fun getViewCount(): Long {
        return itemObject.getLong("playback_count")
    }

    @get:Throws(ParsingException::class)

    override val thumbnails: List<Image>
        get() = SoundcloudParsingHelper.getAllImagesFromTrackObject(itemObject)

    override fun getStreamType(): StreamType {
        return StreamType.AUDIO_STREAM
    }

    override fun isAd(): Boolean {
        return false
    }
}
