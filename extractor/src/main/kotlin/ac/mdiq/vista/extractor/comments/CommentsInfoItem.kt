package ac.mdiq.vista.extractor.comments

import ac.mdiq.vista.extractor.Image
import ac.mdiq.vista.extractor.InfoItem
import ac.mdiq.vista.extractor.Page
import ac.mdiq.vista.extractor.localization.DateWrapper
import ac.mdiq.vista.extractor.stream.Description


class CommentsInfoItem(
        serviceId: Int,
        url: String,
        name: String)
    : InfoItem(InfoType.COMMENT, serviceId, url, name) {

    @JvmField
    var commentId: String? = null

    @JvmField

    var commentText: Description = Description.EMPTY_DESCRIPTION
    @JvmField
    var uploaderName: String? = null

    @JvmField

    var uploaderAvatars: List<Image> = listOf()
    @JvmField
    var uploaderUrl: String? = null
    var isUploaderVerified: Boolean = false
    @JvmField
    var textualUploadDate: String? = null
    @JvmField
    var uploadDate: DateWrapper? = null

    /**
     * @return the comment's like count or [CommentsInfoItem.NO_LIKE_COUNT] if it is
     * unavailable
     */
    @JvmField
    var likeCount: Int = 0
    @JvmField
    var textualLikeCount: String? = null
    var isHeartedByUploader: Boolean = false
    var isPinned: Boolean = false

    /**
     * Get the playback position of the stream to which this comment belongs.
     * This is not supported by all services.
     *
     * @return the playback position in seconds or [.NO_STREAM_POSITION] if not available
     */
    @JvmField
    var streamPosition: Int = 0
    @JvmField
    var replyCount: Int = 0
    @JvmField
    var replies: Page? = null
    @JvmField
    var isChannelOwner: Boolean = false
    private var creatorReply = false


    fun setCreatorReply(creatorReply: Boolean) {
        this.creatorReply = creatorReply
    }

    fun hasCreatorReply(): Boolean {
        return creatorReply
    }

    companion object {
        const val NO_LIKE_COUNT: Int = -1
        const val NO_STREAM_POSITION: Int = -1

        const val UNKNOWN_REPLY_COUNT: Int = -1
    }
}
