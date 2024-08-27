package ac.mdiq.vista.extractor.services.youtube.extractors

import com.grack.nanojson.JsonObject
import ac.mdiq.vista.extractor.Image
import ac.mdiq.vista.extractor.Page
import ac.mdiq.vista.extractor.comments.CommentsInfoItemExtractor
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.localization.DateWrapper
import ac.mdiq.vista.extractor.localization.TimeAgoParser
import ac.mdiq.vista.extractor.services.youtube.YoutubeDescriptionHelper.attributedDescriptionToHtml
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.getImagesFromThumbnailsArray
import ac.mdiq.vista.extractor.stream.Description
import ac.mdiq.vista.extractor.utils.Utils.mixedNumberWordToLong
import java.util.*


/**
 * A [CommentsInfoItemExtractor] for YouTube comment data returned in a view model and entity
 * updates.
 */
internal class YoutubeCommentsEUVMInfoItemExtractor(
        private val commentViewModel: JsonObject,
        private val commentRepliesRenderer: JsonObject?,
        private val commentEntityPayload: JsonObject,
        private val engagementToolbarStateEntityPayload: JsonObject,
        @get:Throws(ParsingException::class) override val url: String,
        private val timeAgoParser: TimeAgoParser)
    : CommentsInfoItemExtractor {

    @get:Throws(ParsingException::class)
    override val name: String
        get() = uploaderName

    @get:Throws(ParsingException::class)

    override val thumbnails: List<Image>
        get() = uploaderAvatars

    @get:Throws(ParsingException::class)
    override val likeCount: Int
        get() {
            val textualLikeCount: String = textualLikeCount
            try {
                if (textualLikeCount.isBlank()) return 0
                return mixedNumberWordToLong(textualLikeCount).toInt()
            } catch (e: Exception) {
                throw ParsingException("Unexpected error while converting textual like count to like count", e)
            }
        }

    override val textualLikeCount: String
        get() = commentEntityPayload.getObject("toolbar").getString("likeCountNotliked")

    // Comments' text work in the same way as an attributed video description
    @get:Throws(ParsingException::class)
    override val commentText: Description
        get() = Description(attributedDescriptionToHtml(commentEntityPayload.getObject(PROPERTIES).getObject("content")), Description.HTML)

    @get:Throws(ParsingException::class)
    override val textualUploadDate: String
        get() = commentEntityPayload.getObject(PROPERTIES).getString("publishedTime")

    @get:Throws(ParsingException::class)
    override val uploadDate: DateWrapper?
        get() {
            val textualPublishedTime: String = textualUploadDate
            if (textualPublishedTime.isEmpty()) return null
            return timeAgoParser.parse(textualPublishedTime)
        }

    @get:Throws(ParsingException::class)
    override val commentId: String
        get() {
            var commentId: String = commentEntityPayload.getObject(PROPERTIES).getString("commentId")
            if (commentId.isEmpty()) {
                commentId = commentViewModel.getString("commentId")
                if (commentId.isNullOrEmpty()) throw ParsingException("Could not get comment ID")
            }
            return commentId
        }

    @get:Throws(ParsingException::class)
    override val uploaderUrl: String
        get() {
            val author: JsonObject = commentEntityPayload.getObject(AUTHOR)
            var channelId: String = author.getString("channelId")
            if (channelId.isEmpty()) {
                channelId = author.getObject("channelCommand")
                    .getObject("innertubeCommand")
                    .getObject("browseEndpoint")
                    .getString("browseId")
                if (channelId.isNullOrEmpty()) {
                    channelId = author.getObject("avatar")
                        .getObject("endpoint")
                        .getObject("innertubeCommand")
                        .getObject("browseEndpoint")
                        .getString("browseId")
                    if (channelId.isNullOrEmpty()) throw ParsingException("Could not get channel ID")
                }
            }
            return "https://www.youtube.com/channel/$channelId"
        }

    @get:Throws(ParsingException::class)
    override val uploaderName: String
        get() = commentEntityPayload.getObject(AUTHOR).getString("displayName")

    @get:Throws(ParsingException::class)

    override val uploaderAvatars: List<Image>
        get() = getImagesFromThumbnailsArray(commentEntityPayload.getObject("avatar").getObject("image").getArray("sources"))

    override val isHeartedByUploader: Boolean
        get() = "TOOLBAR_HEART_STATE_HEARTED" == engagementToolbarStateEntityPayload.getString("heartState")

    override val isPinned: Boolean
        get() = commentViewModel.has("pinnedText")

    @get:Throws(ParsingException::class)
    override val isUploaderVerified: Boolean
        get() {
            val author: JsonObject = commentEntityPayload.getObject(AUTHOR)
            return author.getBoolean("isVerified") || author.getBoolean("isArtist")
        }

    @get:Throws(ParsingException::class)
    override val replyCount: Int
        get() {
            // As YouTube allows replies up to 750 comments, we cannot check if the count returned is a
            // mixed number or a real number
            // Assume it is a mixed one, as it matches how numbers of most properties are returned
            val replyCountString: String = commentEntityPayload.getObject("toolbar").getString("replyCount")
            if (replyCountString.isEmpty()) return 0
            return mixedNumberWordToLong(replyCountString).toInt()
        }

    @get:Throws(ParsingException::class)
    override val replies: Page?
        get() {
            if (commentRepliesRenderer.isNullOrEmpty()) return null

            val continuation: String = commentRepliesRenderer.getArray("contents")
                .stream()
                .filter { o: Any? -> JsonObject::class.java.isInstance(o) }
                .map { obj: Any? -> JsonObject::class.java.cast(obj) }
                .map { content: JsonObject -> content.getObject("continuationItemRenderer", null) }
                .filter { obj: JsonObject? -> Objects.nonNull(obj) }
                .findFirst()
                .map { continuationItemRenderer: JsonObject ->
                    continuationItemRenderer.getObject("continuationEndpoint").getObject("continuationCommand").getString("token") }
                .orElseThrow { ParsingException("Could not get comment replies continuation") }
            return Page(url, continuation)
        }

    override val isChannelOwner: Boolean
        get() = commentEntityPayload.getObject(AUTHOR).getBoolean("isCreator")

    override fun hasCreatorReply(): Boolean {
        return commentRepliesRenderer != null && commentRepliesRenderer.has("viewRepliesCreatorThumbnail")
    }

    companion object {
        private const val AUTHOR: String = "author"
        private const val PROPERTIES: String = "properties"
    }
}
