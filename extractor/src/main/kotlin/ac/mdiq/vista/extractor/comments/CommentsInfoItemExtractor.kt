package ac.mdiq.vista.extractor.comments

import ac.mdiq.vista.extractor.Image
import ac.mdiq.vista.extractor.InfoItemExtractor
import ac.mdiq.vista.extractor.Page
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.localization.DateWrapper
import ac.mdiq.vista.extractor.stream.Description


interface CommentsInfoItemExtractor : InfoItemExtractor {
    /**
     * Return the like count of the comment,
     * or [CommentsInfoItem.NO_LIKE_COUNT] if it is unavailable.
     *
     * <br></br>
     *
     *
     * NOTE: Currently only implemented for YT [ ][YoutubeCommentsInfoItemExtractor.getLikeCount]
     * with limitations (only approximate like count is returned)
     *
     * @return the comment's like count
     * or [CommentsInfoItem.NO_LIKE_COUNT] if it is unavailable
     * @see StreamExtractor.getLikeCount
     */
    @get:Throws(ParsingException::class)
    val likeCount: Int
        get() = CommentsInfoItem.NO_LIKE_COUNT

    /**
     * The unmodified like count given by the service
     * <br></br>
     * It may be language dependent
     */
    @get:Throws(ParsingException::class)
    val textualLikeCount: String
        get() = ""

    /**
     * The text of the comment
     */
    @get:Throws(ParsingException::class)

    val commentText: Description
        get() = Description.EMPTY_DESCRIPTION

    /**
     * The upload date given by the service, unmodified
     *
     * @see StreamExtractor.getTextualUploadDate
     */
    @get:Throws(ParsingException::class)
    val textualUploadDate: String
        get() = ""

    /**
     * The upload date wrapped with DateWrapper class
     *
     * @see StreamExtractor.getUploadDate
     */
    @get:Throws(ParsingException::class)
    val uploadDate: DateWrapper?
        get() = null

    @get:Throws(ParsingException::class)
    val commentId: String
        get() = ""

    @get:Throws(ParsingException::class)
    val uploaderUrl: String
        get() = ""

    @get:Throws(ParsingException::class)
    val uploaderName: String
        get() = ""

    @get:Throws(ParsingException::class)

    val uploaderAvatars: List<Image>
        get() = listOf()

    /**
     * Whether the comment has been hearted by the uploader
     */
    @get:Throws(ParsingException::class)
    val isHeartedByUploader: Boolean
        get() = false

    /**
     * Whether the comment is pinned
     */
    @get:Throws(ParsingException::class)
    val isPinned: Boolean
        get() = false

    /**
     * Whether the uploader is verified by the service
     */
    @get:Throws(ParsingException::class)
    val isUploaderVerified: Boolean
        get() = false

    /**
     * The playback position of the stream to which this comment belongs.
     *
     * @see CommentsInfoItem.getStreamPosition
     */
    @get:Throws(ParsingException::class)
    val streamPosition: Int
        get() = CommentsInfoItem.NO_STREAM_POSITION

    /**
     * The count of comment replies.
     *
     * @return the count of the replies
     * or [CommentsInfoItem.UNKNOWN_REPLY_COUNT] if replies are not supported
     */
    @get:Throws(ParsingException::class)
    val replyCount: Int
        get() = CommentsInfoItem.UNKNOWN_REPLY_COUNT

    /**
     * The continuation page which is used to get comment replies from.
     *
     * @return the continuation Page for the replies, or null if replies are not supported
     */
    @get:Throws(ParsingException::class)
    val replies: Page?
        get() = null

    /**
     * Whether the comment was made by the channel owner.
     */
    @get:Throws(ParsingException::class)
    val isChannelOwner: Boolean
        get() = false

    /**
     * Whether the comment was replied to by the creator.
     */
    @Throws(ParsingException::class)
    fun hasCreatorReply(): Boolean {
        return false
    }
}
