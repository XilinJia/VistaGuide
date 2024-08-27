package ac.mdiq.vista.extractor.services.soundcloud.extractors

import com.grack.nanojson.JsonObject
import ac.mdiq.vista.extractor.Image
import ac.mdiq.vista.extractor.comments.CommentsInfoItemExtractor
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.localization.DateWrapper
import ac.mdiq.vista.extractor.services.soundcloud.SoundcloudParsingHelper
import ac.mdiq.vista.extractor.stream.Description
import java.util.*


class SoundcloudCommentsInfoItemExtractor(private val json: JsonObject, override val url: String) : CommentsInfoItemExtractor {

    override val commentId: String
        get() = Objects.toString(json.getLong("id"), null)


    override val commentText: Description
        get() = Description(json.getString("body"), Description.PLAIN_TEXT)

    override val uploaderName: String
        get() = json.getObject("user").getString("username")


    override val uploaderAvatars: List<Image>
        get() = SoundcloudParsingHelper.getAllImagesFromArtworkOrAvatarUrl(json.getObject("user")
            .getString("avatar_url"))

    @get:Throws(ParsingException::class)
    override val isUploaderVerified: Boolean
        get() = json.getObject("user").getBoolean("verified")

    override val streamPosition: Int
        get() = json.getInt("timestamp") / 1000 // convert milliseconds to seconds

    override val uploaderUrl: String
        get() = json.getObject("user").getString("permalink_url")

    override val textualUploadDate: String
        get() = json.getString("created_at")

    @get:Throws(ParsingException::class)
    override val uploadDate: DateWrapper
        get() = DateWrapper(SoundcloudParsingHelper.parseDateFrom(textualUploadDate))

    @get:Throws(ParsingException::class)
    override val name: String
        get() = json.getObject("user").getString("permalink")


    override val thumbnails: List<Image>
        get() = SoundcloudParsingHelper.getAllImagesFromArtworkOrAvatarUrl(json.getObject("user")
            .getString("avatar_url"))
}
