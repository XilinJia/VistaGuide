// Created by Fynn Godau 2019, licensed GNU GPL version 3 or later
package ac.mdiq.vista.extractor.services.bandcamp.extractors

import com.grack.nanojson.JsonObject
import ac.mdiq.vista.extractor.Image
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.localization.DateWrapper
import ac.mdiq.vista.extractor.services.bandcamp.extractors.BandcampExtractorHelper.BASE_URL
import ac.mdiq.vista.extractor.services.bandcamp.extractors.BandcampExtractorHelper.getImagesFromImageId
import ac.mdiq.vista.extractor.services.bandcamp.extractors.BandcampExtractorHelper.parseDate
import ac.mdiq.vista.extractor.stream.StreamInfoItemExtractor
import ac.mdiq.vista.extractor.stream.StreamType

class BandcampRadioInfoItemExtractor(private val show: JsonObject) : StreamInfoItemExtractor {
    override fun getDuration(): Long {
        /* Duration is only present in the more detailed information that has to be queried
        separately. Therefore, over 300 queries would be needed every time the kiosk is opened if we
        were to display the real value. */
        //return query(show.getInt("id")).getLong("audio_duration");
        return 0
    }

    override fun getTextualUploadDate(): String? {
        return show.getString("date")
    }



    override fun getShortDescription(): String? {
        return show.getString("desc")
    }

    @Throws(ParsingException::class)
    override fun getUploadDate(): DateWrapper {
        return parseDate(getTextualUploadDate()!!)
    }

    @get:Throws(ParsingException::class)
    override val name: String
        get() = show.getString("subtitle")

    override val url: String
        get() = BASE_URL + "/?show=" + show.getInt("id")


    override val thumbnails: List<Image>
        get() = getImagesFromImageId(show.getLong("image_id"), false)

    override fun getStreamType(): StreamType {
        return StreamType.AUDIO_STREAM
    }

    override fun getViewCount(): Long {
        return -1
    }

    override fun getUploaderName(): String? {
        // JSON does not contain uploader name
        return show.getString("title")
    }

    override fun getUploaderUrl(): String {
        return ""
    }

    @Throws(ParsingException::class)
    override fun isUploaderVerified(): Boolean {
        return false
    }

    override fun isAd(): Boolean {
        return false
    }
}
