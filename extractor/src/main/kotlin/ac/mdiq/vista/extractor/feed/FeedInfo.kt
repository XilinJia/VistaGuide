package ac.mdiq.vista.extractor.feed

import ac.mdiq.vista.extractor.ListInfo
import ac.mdiq.vista.extractor.Vista.getServiceByUrl
import ac.mdiq.vista.extractor.StreamingService
import ac.mdiq.vista.extractor.exceptions.ExtractionException
import ac.mdiq.vista.extractor.stream.StreamInfoItem
import ac.mdiq.vista.extractor.utils.ExtractorHelper
import java.io.IOException

class FeedInfo(
        serviceId: Int,
        id: String,
        url: String,
        originalUrl: String,
        name: String,
        contentFilter: List<String>?,
        sortFilter: String?)
    : ListInfo<StreamInfoItem>(serviceId, id, url, originalUrl, name, contentFilter?: listOf(), sortFilter?:"") {

    companion object {
        @Throws(IOException::class, ExtractionException::class)
        fun getInfo(url: String): FeedInfo {
            return getInfo(getServiceByUrl(url), url)
        }

        @Throws(IOException::class, ExtractionException::class)
        fun getInfo(service: StreamingService, url: String): FeedInfo {
            val extractor = service.getFeedExtractor(url)
                ?: throw IllegalArgumentException("Service \"" + service.serviceInfo.name + "\" doesn't support FeedExtractor.")

            extractor.fetchPage()
            return getInfo(extractor)
        }

        @Throws(IOException::class, ExtractionException::class)
        fun getInfo(extractor: FeedExtractor): FeedInfo {
            extractor.fetchPage()

            val serviceId = extractor.serviceId
            val id = extractor.id
            val url = extractor.url
            val originalUrl = extractor.originalUrl
            val name = extractor.getName()

            val info = FeedInfo(serviceId, id, url, originalUrl, name, null, null)

            val itemsPage = ExtractorHelper.getItemsPageOrLogError(info, extractor)
            info.relatedItems = itemsPage.items
            info.nextPage = itemsPage.nextPage

            return info
        }
    }
}
