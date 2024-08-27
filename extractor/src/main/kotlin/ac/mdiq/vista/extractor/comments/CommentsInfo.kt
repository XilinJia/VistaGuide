package ac.mdiq.vista.extractor.comments

import ac.mdiq.vista.extractor.ListExtractor.InfoItemsPage
import ac.mdiq.vista.extractor.ListInfo
import ac.mdiq.vista.extractor.Vista.getService
import ac.mdiq.vista.extractor.Vista.getServiceByUrl
import ac.mdiq.vista.extractor.Page
import ac.mdiq.vista.extractor.StreamingService
import ac.mdiq.vista.extractor.exceptions.ExtractionException
import ac.mdiq.vista.extractor.linkhandler.ListLinkHandler
import ac.mdiq.vista.extractor.utils.ExtractorHelper
import java.io.IOException

class CommentsInfo private constructor(
        serviceId: Int,
        listUrlIdHandler: ListLinkHandler,
        name: String)
    : ListInfo<CommentsInfoItem>(serviceId, listUrlIdHandler, name) {

    @Transient
    var commentsExtractor: CommentsExtractor? = null
    /**
     * @return `true` if the comments are disabled otherwise `false` (default)
     * @see CommentsExtractor.isCommentsDisabled
     */
    /**
     * @param commentsDisabled `true` if the comments are disabled otherwise `false`
     */
    var isCommentsDisabled: Boolean = false
    /**
     * Returns the total number of comments.
     *
     * @return the total number of comments
     */
    /**
     * Sets the total number of comments.
     *
     * @param commentsCount the commentsCount to set.
     */
    var commentsCount: Int = 0

    companion object {

        @Throws(IOException::class, ExtractionException::class)
        fun getInfo(url: String?): CommentsInfo? {
            if (url == null) return null
            return getInfo(getServiceByUrl(url), url)
        }

        @Throws(ExtractionException::class, IOException::class)
        fun getInfo(service: StreamingService, url: String): CommentsInfo? {
            if (url == null) return null
            return getInfo(service.getCommentsExtractor(url))
        }

        @Throws(IOException::class, ExtractionException::class)
        fun getInfo(commentsExtractor: CommentsExtractor?): CommentsInfo? {
            // for services which do not have a comments extractor
            if (commentsExtractor == null) {
                return null
            }

            commentsExtractor.fetchPage()

            val name = commentsExtractor.getName()
            val serviceId = commentsExtractor.serviceId
            val listUrlIdHandler = commentsExtractor.getLinkHandler()

            val commentsInfo = CommentsInfo(serviceId, listUrlIdHandler, name)
            commentsInfo.commentsExtractor = commentsExtractor
            val initialCommentsPage = ExtractorHelper.getItemsPageOrLogError(commentsInfo, commentsExtractor)
            commentsInfo.isCommentsDisabled = commentsExtractor.isCommentsDisabled
            commentsInfo.relatedItems = initialCommentsPage.items
            try {
                commentsInfo.commentsCount = commentsExtractor.commentsCount
            } catch (e: Exception) {
                commentsInfo.addError(e)
            }
            commentsInfo.nextPage = initialCommentsPage.nextPage

            return commentsInfo
        }

        @Throws(ExtractionException::class, IOException::class)
        fun getMoreItems(commentsInfo: CommentsInfo, page: Page?): InfoItemsPage<CommentsInfoItem> {
            return getMoreItems(getService(commentsInfo.serviceId), commentsInfo.url, page)
        }


        @Throws(IOException::class, ExtractionException::class)
        fun getMoreItems(service: StreamingService, commentsInfo: CommentsInfo, page: Page?): InfoItemsPage<CommentsInfoItem> {
            return getMoreItems(service, commentsInfo.url, page)
        }

        @Throws(IOException::class, ExtractionException::class)
        fun getMoreItems(service: StreamingService, url: String, page: Page?): InfoItemsPage<CommentsInfoItem> {
            return service.getCommentsExtractor(url)!!.getPage(page)
        }
    }
}
