package ac.mdiq.vista.extractor.services.bandcamp.extractors

import com.grack.nanojson.JsonObject
import ac.mdiq.vista.extractor.Image
import ac.mdiq.vista.extractor.comments.CommentsInfoItemExtractor
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.stream.Description


class BandcampCommentsInfoItemExtractor(private val review: JsonObject, override val url: String) : CommentsInfoItemExtractor {

    @get:Throws(ParsingException::class)
    override val name: String
        get() = commentText.content

    @get:Throws(ParsingException::class)

    override val thumbnails: List<Image>
        get() = uploaderAvatars

    @get:Throws(ParsingException::class)

    override val commentText: Description
        get() = Description(review.getString("why"), Description.PLAIN_TEXT)

    @get:Throws(ParsingException::class)
    override val uploaderName: String
        get() = review.getString("name")


    override val uploaderAvatars: List<Image>
        get() = BandcampExtractorHelper.getImagesFromImageId(review.getLong("image_id"), false)
}
