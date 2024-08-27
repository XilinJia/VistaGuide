package ac.mdiq.vista.extractor.services.youtube.extractors

import com.grack.nanojson.JsonObject
import ac.mdiq.vista.extractor.Image
import ac.mdiq.vista.extractor.Page
import ac.mdiq.vista.extractor.comments.CommentsInfoItem
import ac.mdiq.vista.extractor.comments.CommentsInfoItemExtractor
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.localization.DateWrapper
import ac.mdiq.vista.extractor.localization.TimeAgoParser
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper
import ac.mdiq.vista.extractor.stream.Description
import ac.mdiq.vista.extractor.utils.JsonUtils.getArray
import ac.mdiq.vista.extractor.utils.JsonUtils.getObject
import ac.mdiq.vista.extractor.utils.JsonUtils.getString
import ac.mdiq.vista.extractor.utils.Utils.mixedNumberWordToLong
import ac.mdiq.vista.extractor.utils.Utils.removeNonDigitCharacters
import ac.mdiq.vista.extractor.utils.Utils.removeUTF8BOM


class YoutubeCommentsInfoItemExtractor(
        private val commentRenderer: JsonObject,
        private val commentRepliesRenderer: JsonObject?,
        @get:Throws(ParsingException::class) override val url: String,
        private val timeAgoParser: TimeAgoParser)
    : CommentsInfoItemExtractor {

    @get:Throws(ParsingException::class)

    private val authorThumbnails: List<Image>
        get() {
            try {
                return YoutubeParsingHelper.getImagesFromThumbnailsArray(getArray(commentRenderer, "authorThumbnail.thumbnails"))
            } catch (e: Exception) {
                throw ParsingException("Could not get author thumbnails", e)
            }
        }

    @get:Throws(ParsingException::class)

    override val thumbnails: List<Image>
        get() = authorThumbnails

    @get:Throws(ParsingException::class)
    override val name: String
        get() = try {
            YoutubeParsingHelper.getTextFromObject(getObject(commentRenderer, "authorText")) ?: ""
        } catch (e: Exception) {
            ""
        }

    override val textualUploadDate: String
        get() = try {
            YoutubeParsingHelper.getTextFromObject(getObject(commentRenderer, "publishedTimeText"))!!
        } catch (e: Exception) {
            throw ParsingException("Could not get publishedTimeText", e)
        }



    override val uploadDate: DateWrapper?
        get() {
            val textualPublishedTime = textualUploadDate
            return if (textualPublishedTime.isNotEmpty()) timeAgoParser.parse(textualPublishedTime) else null
        }

    /**
     * @implNote The method tries first to get the exact like count by using the accessibility data
     * returned. But if the parsing of this accessibility data fails, the method parses internally
     * a localized string.
     * <br></br>
     *
     *  * More than 1k likes will result in an inaccurate number
     *  * This will fail for other languages than English. However as long as the Extractor
     * only uses "en-GB" (as seen in [     ][ac.mdiq.vista.extractor.services.youtube.YoutubeService.getSupportedLocalizations])
     * , everything will work fine.
     *
     * <br></br>
     * Consider using [.getTextualLikeCount]
     */

    override val likeCount: Int
        get() {
            // Try first to get the exact like count by using the accessibility data
            val likeCount: String
            try {
                likeCount = removeNonDigitCharacters(getString(commentRenderer,
                    "actionButtons.commentActionButtonsRenderer.likeButton.toggleButtonRenderer.accessibilityData.accessibilityData.label"))
            } catch (e: Exception) {
                // Use the approximate like count returned into the voteCount object
                // This may return a language dependent version, e.g. in German: 3,3 Mio
                val textualLikeCount = textualLikeCount
                try {
                    if (textualLikeCount.isEmpty()) return 0
                    return mixedNumberWordToLong(textualLikeCount).toInt()
                } catch (i: Exception) {
                    throw ParsingException("Unexpected error while converting textual like count to like count", i)
                }
            }

            try {
                if (likeCount.isEmpty()) return 0
                return likeCount.toInt()
            } catch (e: Exception) {
                throw ParsingException("Unexpected error while parsing like count as Integer", e)
            }
        }


    /*
     * Example results as of 2021-05-20:
     * Language = English
     * 3.3M
     * 48K
     * 1.4K
     * 270K
     * 19
     * 6
     *
     * Language = German
     * 3,3 Mio
     * 48.189
     * 1419
     * 270.984
     * 19
     * 6
     */
    override val textualLikeCount: String
        get() {
            try {
                // If a comment has no likes voteCount is not set
                if (!commentRenderer.has("voteCount")) return ""

                val voteCountObj = getObject(commentRenderer, "voteCount")
                if (voteCountObj.isEmpty()) return ""
                return YoutubeParsingHelper.getTextFromObject(voteCountObj) ?: ""
            } catch (e: Exception) {
                throw ParsingException("Could not get the vote count", e)
            }
        }


    override val commentText: Description
        get() {
            try {
                val contentText = getObject(commentRenderer, "contentText")
                // completely empty comments as described in
                // https://github.com/XilinJia/VistaGuide/issues/380#issuecomment-668808584
                if (contentText.isEmpty()) return Description.EMPTY_DESCRIPTION
                val commentText = YoutubeParsingHelper.getTextFromObject(contentText, true)
                // YouTube adds U+FEFF in some comments.
                // eg. https://www.youtube.com/watch?v=Nj4F63E59io<feff>
                val commentTextBomRemoved = removeUTF8BOM(commentText!!)
                return Description(commentTextBomRemoved, Description.HTML)
            } catch (e: Exception) {
                throw ParsingException("Could not get comment text", e)
            }
        }


    override val commentId: String
        get() {
            try {
                return getString(commentRenderer, "commentId")
            } catch (e: Exception) {
                throw ParsingException("Could not get comment id", e)
            }
        }


    override val uploaderAvatars: List<Image>
        get() = authorThumbnails

    override val isHeartedByUploader: Boolean
        get() {
            val commentActionButtonsRenderer = commentRenderer.getObject("actionButtons").getObject("commentActionButtonsRenderer")
            return commentActionButtonsRenderer.has("creatorHeart")
        }

    override val isPinned: Boolean
        get() = commentRenderer.has("pinnedCommentBadge")

    override val isUploaderVerified: Boolean
        get() = commentRenderer.has("authorCommentBadge")

    override val uploaderName: String
        get() {
            return try {
                YoutubeParsingHelper.getTextFromObject(getObject(commentRenderer, "authorText"))!!
            } catch (e: Exception) {
                ""
            }
        }

    override val uploaderUrl: String
        get() {
            return try {
                "https://www.youtube.com/channel/" + getString(commentRenderer, "authorEndpoint.browseEndpoint.browseId")
            } catch (e: Exception) {
                ""
            }
        }

    override val replyCount: Int
        get() {
            if (commentRenderer.has("replyCount")) return commentRenderer.getInt("replyCount")
            return CommentsInfoItem.UNKNOWN_REPLY_COUNT
        }

    override val replies: Page?
        get() {
            if (commentRepliesRenderer == null) return null
            try {
                val id = getString(getArray(commentRepliesRenderer, "contents").getObject(0),
                    "continuationItemRenderer.continuationEndpoint.continuationCommand.token")
                return Page(url, id)
            } catch (e: Exception) {
                return null
            }
        }

    override val isChannelOwner: Boolean
        get() = commentRenderer.getBoolean("authorIsChannelOwner")

    override fun hasCreatorReply(): Boolean {
        if (commentRepliesRenderer == null) return false
        return commentRepliesRenderer.has("viewRepliesCreatorThumbnail")
    }
}
