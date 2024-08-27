package ac.mdiq.vista.extractor.services.media_ccc.extractors

import com.grack.nanojson.JsonObject
import ac.mdiq.vista.extractor.*
import ac.mdiq.vista.extractor.ListExtractor.InfoItemsPage.Companion.emptyPage
import ac.mdiq.vista.extractor.channel.tabs.ChannelTabExtractor
import ac.mdiq.vista.extractor.downloader.Downloader
import ac.mdiq.vista.extractor.exceptions.ExtractionException
import ac.mdiq.vista.extractor.linkhandler.ListLinkHandler
import ac.mdiq.vista.extractor.services.media_ccc.extractors.infoItems.MediaCCCStreamInfoItemExtractor
import java.io.IOException
import java.util.*


/**
 * MediaCCC does not really have channel tabs, but rather a list of videos for each conference,
 * so this class just acts as a videos channel tab extractor.
 */
/**
 * @param conferenceData will be not-null if conference data has already been fetched by
 * [MediaCCCConferenceExtractor]. Otherwise, if this parameter is
 * `null`, conference data will be fetched anew.
*/
class MediaCCCChannelTabExtractor(
        service: StreamingService,
        linkHandler: ListLinkHandler,
        private var conferenceData: JsonObject?)
    : ChannelTabExtractor(service, linkHandler) {

    @Throws(ExtractionException::class, IOException::class)
    override fun onFetchPage(downloader: Downloader) {
        // only fetch conference data if we don't have it already
        if (conferenceData == null) conferenceData = MediaCCCConferenceExtractor.fetchConferenceData(downloader, id)
    }


    override val initialPage: InfoItemsPage<InfoItem>
        get() {
            val collector = MultiInfoItemsCollector(serviceId)
            // will surely be != null after onFetchPage
            Objects.requireNonNull(conferenceData)!!
                .getArray("events")
                .stream()
                .filter { o: Any? -> JsonObject::class.java.isInstance(o) }
                .map { obj: Any? -> JsonObject::class.java.cast(obj) }
                .forEach { event: JsonObject ->
                    collector.commit(MediaCCCStreamInfoItemExtractor(event))
                }
            return InfoItemsPage(collector, null)
        }

    override fun getPage(page: Page?): InfoItemsPage<InfoItem> {
        return emptyPage()
    }
}
