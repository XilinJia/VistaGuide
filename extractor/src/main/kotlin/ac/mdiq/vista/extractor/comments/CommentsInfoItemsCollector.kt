package ac.mdiq.vista.extractor.comments

import ac.mdiq.vista.extractor.InfoItemsCollector
import ac.mdiq.vista.extractor.exceptions.ParsingException

class CommentsInfoItemsCollector(serviceId: Int) : InfoItemsCollector<CommentsInfoItem, CommentsInfoItemExtractor>(serviceId) {
    @Throws(ParsingException::class)
    override fun extract(extractor: CommentsInfoItemExtractor): CommentsInfoItem {
        val resultItem = CommentsInfoItem(serviceId, extractor.url, extractor.name)

        // optional information
        try {
            resultItem.commentId = extractor.commentId
        } catch (e: Exception) {
            addError(e)
        }
        try {
            resultItem.commentText = extractor.commentText
        } catch (e: Exception) {
            addError(e)
        }
        try {
            resultItem.uploaderName = extractor.uploaderName
        } catch (e: Exception) {
            addError(e)
        }
        try {
            resultItem.uploaderAvatars = (extractor.uploaderAvatars)
        } catch (e: Exception) {
            addError(e)
        }
        try {
            resultItem.uploaderUrl = extractor.uploaderUrl
        } catch (e: Exception) {
            addError(e)
        }
        try {
            resultItem.textualUploadDate = extractor.textualUploadDate
        } catch (e: Exception) {
            addError(e)
        }
        try {
            resultItem.uploadDate = extractor.uploadDate
        } catch (e: Exception) {
            addError(e)
        }
        try {
            resultItem.likeCount = extractor.likeCount
        } catch (e: Exception) {
            addError(e)
        }
        try {
            resultItem.textualLikeCount = extractor.textualLikeCount
        } catch (e: Exception) {
            addError(e)
        }
        try {
            resultItem.thumbnails = (extractor.thumbnails)
        } catch (e: Exception) {
            addError(e)
        }

        try {
            resultItem.isHeartedByUploader = extractor.isHeartedByUploader
        } catch (e: Exception) {
            addError(e)
        }

        try {
            resultItem.isPinned = extractor.isPinned
        } catch (e: Exception) {
            addError(e)
        }

        try {
            resultItem.streamPosition = extractor.streamPosition
        } catch (e: Exception) {
            addError(e)
        }

        try {
            resultItem.replyCount = extractor.replyCount
        } catch (e: Exception) {
            addError(e)
        }

        try {
            resultItem.replies = extractor.replies
        } catch (e: Exception) {
            addError(e)
        }

        try {
            resultItem.isChannelOwner = extractor.isChannelOwner
        } catch (e: Exception) {
            addError(e)
        }


        try {
            resultItem.setCreatorReply(extractor.hasCreatorReply())
        } catch (e: Exception) {
            addError(e)
        }


        return resultItem
    }

    override fun commit(extractor: CommentsInfoItemExtractor) {
        try {
            addItem(extract(extractor))
        } catch (e: Exception) {
            addError(e)
        }
    }

    val commentsInfoItemList: List<CommentsInfoItem>
        get() = ArrayList(super.getItems())
}
