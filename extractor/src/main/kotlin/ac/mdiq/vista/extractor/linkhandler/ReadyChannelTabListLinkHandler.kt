package ac.mdiq.vista.extractor.linkhandler

import ac.mdiq.vista.extractor.StreamingService
import ac.mdiq.vista.extractor.channel.tabs.ChannelTabExtractor
import java.io.Serializable


/**
 * A [ListLinkHandler] which can be used to be returned from [ ][ac.mdiq.vista.extractor.channel.ChannelInfo.getTabs] when a
 * specific tab's data has already been fetched.
 *
 * This class allows passing a builder for a [ChannelTabExtractor] that can hold references
 * to variables.
 *
 * Note: a service that wishes to use this class in one of its [ ]s must also add the
 * following snippet of code in the service's
 * [StreamingService.getChannelTabExtractor]:
 * <pre>
 * if (linkHandler instanceof ReadyChannelTabListLinkHandler) {
 * return ((ReadyChannelTabListLinkHandler) linkHandler).getChannelTabExtractor(this);
 * }
</pre> *
 *
 */
class ReadyChannelTabListLinkHandler(
        url: String,
        channelId: String,
        channelTab: String,
        private val extractorBuilder: ChannelTabExtractorBuilder)
    : ListLinkHandler(url, url, channelId, listOf(channelTab), "") {

    interface ChannelTabExtractorBuilder : Serializable {
        fun build(service: StreamingService, linkHandler: ListLinkHandler): ChannelTabExtractor
    }

    fun getChannelTabExtractor(service: StreamingService): ChannelTabExtractor {
        return extractorBuilder.build(service, ListLinkHandler(this))
    }
}
