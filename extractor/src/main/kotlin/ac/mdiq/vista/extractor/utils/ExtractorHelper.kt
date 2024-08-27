package ac.mdiq.vista.extractor.utils

import ac.mdiq.vista.extractor.Info
import ac.mdiq.vista.extractor.InfoItem
import ac.mdiq.vista.extractor.ListExtractor
import ac.mdiq.vista.extractor.ListExtractor.InfoItemsPage
import ac.mdiq.vista.extractor.ListExtractor.InfoItemsPage.Companion.emptyPage
import ac.mdiq.vista.extractor.stream.StreamExtractor
import ac.mdiq.vista.extractor.stream.StreamInfo

object ExtractorHelper {
    fun <T : InfoItem> getItemsPageOrLogError(info: Info, extractor: ListExtractor<T>): InfoItemsPage<T> {
        try {
            val page = extractor.initialPage
            info.addAllErrors(page.errors)
            return page
        } catch (e: Exception) {
            info.addError(e)
            return emptyPage()
        }
    }

    fun getRelatedItemsOrLogError(info: StreamInfo, extractor: StreamExtractor): List<InfoItem>? {
        try {
            val collector = extractor.relatedItems ?: return emptyList()
            info.addAllErrors(collector.errors)
            return collector.getItems()
        } catch (e: Exception) {
            info.addError(e)
            return emptyList()
        }
    }

    @Deprecated("Use {@link #getRelatedItemsOrLogError(StreamInfo, StreamExtractor)}")
    fun getRelatedVideosOrLogError(info: StreamInfo, extractor: StreamExtractor): List<InfoItem>? {
        return getRelatedItemsOrLogError(info, extractor)
    }
}
