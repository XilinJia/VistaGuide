package ac.mdiq.vista.extractor.comments

import ac.mdiq.vista.extractor.ListExtractor
import ac.mdiq.vista.extractor.StreamingService
import ac.mdiq.vista.extractor.exceptions.ExtractionException
import ac.mdiq.vista.extractor.linkhandler.ListLinkHandler

abstract class CommentsExtractor(service: StreamingService, uiHandler: ListLinkHandler)
    : ListExtractor<CommentsInfoItem>(service, uiHandler) {
    @get:Throws(ExtractionException::class)
    open val isCommentsDisabled: Boolean
        /**
         * @apiNote Warning: This method is experimental and may get removed in a future release.
         * @return `true` if the comments are disabled otherwise `false` (default)
         */
        get() = false

    @get:Throws(ExtractionException::class)
    open val commentsCount: Int
        /**
         * @return the total number of comments
         */
        get() = -1

    override fun getName(): String {
        return "Comments"
    }
}
