package ac.mdiq.vista.extractor.channel.tabs

import ac.mdiq.vista.extractor.InfoItem
import ac.mdiq.vista.extractor.ListExtractor.InfoItemsPage
import ac.mdiq.vista.extractor.ListInfo
import ac.mdiq.vista.extractor.Page
import ac.mdiq.vista.extractor.StreamingService
import ac.mdiq.vista.extractor.exceptions.ExtractionException
import ac.mdiq.vista.extractor.linkhandler.ListLinkHandler
import ac.mdiq.vista.extractor.utils.ExtractorHelper
import java.io.IOException


class ChannelTabInfo(serviceId: Int, linkHandler: ListLinkHandler) : ListInfo<InfoItem>(serviceId, linkHandler, linkHandler.contentFilters[0]) {

    companion object {
        /**
         * Get a [ChannelTabInfo] instance from the given service and tab handler.
         * @param service streaming service
         * @param linkHandler Channel tab handler (from [ChannelInfo])
         * @return the extracted [ChannelTabInfo]
         */

        @Throws(ExtractionException::class, IOException::class)
        fun getInfo(service: StreamingService, linkHandler: ListLinkHandler): ChannelTabInfo {
            val extractor = service.getChannelTabExtractor(linkHandler)
            extractor.fetchPage()
            return getInfo(extractor)
        }

        /**
         * Get a [ChannelTabInfo] instance from a [ChannelTabExtractor].
         * @param extractor an extractor where `fetchPage()` was already got called on
         * @return the extracted [ChannelTabInfo]
         */

        fun getInfo(extractor: ChannelTabExtractor): ChannelTabInfo {
            val info = ChannelTabInfo(extractor.serviceId, extractor.getLinkHandler())

            try { info.originalUrl = extractor.originalUrl } catch (e: Exception) { info.addError(e) }

            val page = ExtractorHelper.getItemsPageOrLogError(info, extractor)
            info.relatedItems = (page.items)
            info.nextPage = page.nextPage

            return info
        }

        @Throws(ExtractionException::class, IOException::class)
        fun getMoreItems(service: StreamingService, linkHandler: ListLinkHandler, page: Page?): InfoItemsPage<InfoItem> {
            return service.getChannelTabExtractor(linkHandler).getPage(page)
        }
    }
}
