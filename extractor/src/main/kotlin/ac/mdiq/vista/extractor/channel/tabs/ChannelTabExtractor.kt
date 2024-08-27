package ac.mdiq.vista.extractor.channel.tabs

import ac.mdiq.vista.extractor.InfoItem
import ac.mdiq.vista.extractor.ListExtractor
import ac.mdiq.vista.extractor.StreamingService
import ac.mdiq.vista.extractor.linkhandler.ListLinkHandler


/**
 * A [ListExtractor] of [InfoItem]s for tabs of channels.
 */
abstract class ChannelTabExtractor protected constructor(
         service: StreamingService,
         linkHandler: ListLinkHandler)
    : ListExtractor<InfoItem>(service, linkHandler) {

    override fun getName(): String {
        return getLinkHandler().contentFilters[0]
    }
}
