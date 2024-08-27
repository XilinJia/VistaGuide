package ac.mdiq.vista.extractor.services.media_ccc.extractors

import com.grack.nanojson.JsonArray
import ac.mdiq.vista.extractor.ListExtractor.InfoItemsPage.Companion.emptyPage
import ac.mdiq.vista.extractor.Page
import ac.mdiq.vista.extractor.StreamingService
import ac.mdiq.vista.extractor.downloader.Downloader
import ac.mdiq.vista.extractor.exceptions.ExtractionException
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.kiosk.KioskExtractor
import ac.mdiq.vista.extractor.linkhandler.ListLinkHandler
import ac.mdiq.vista.extractor.stream.StreamInfoItem
import ac.mdiq.vista.extractor.stream.StreamInfoItemsCollector
import java.io.IOException


class MediaCCCLiveStreamKiosk(streamingService: StreamingService, linkHandler: ListLinkHandler, kioskId: String)
    : KioskExtractor<StreamInfoItem>(streamingService, linkHandler, kioskId) {

    private var doc: JsonArray? = null

    @Throws(IOException::class, ExtractionException::class)
    override fun onFetchPage(downloader: Downloader) {
        doc = MediaCCCParsingHelper.getLiveStreams(downloader, extractorLocalization)
    }

    @get:Throws(IOException::class, ExtractionException::class)

    override val initialPage: InfoItemsPage<StreamInfoItem>
        get() {
            val collector = StreamInfoItemsCollector(serviceId)
            for (c in doc!!.indices) {
                val conference = doc!!.getObject(c)
                if (conference.getBoolean("isCurrentlyStreaming")) {
                    val groups = conference.getArray("groups")
                    for (g in groups.indices) {
                        val group = groups.getObject(g).getString("group")
                        val rooms = groups.getObject(g).getArray("rooms")
                        for (r in rooms.indices) {
                            val room = rooms.getObject(r)
                            collector.commit(MediaCCCLiveStreamKioskExtractor(
                                conference, group, room))
                        }
                    }
                }
            }
            return InfoItemsPage(collector, null)
        }

    @Throws(IOException::class, ExtractionException::class)
    override fun getPage(page: Page?): InfoItemsPage<StreamInfoItem> {
        return emptyPage()
    }


    @Throws(ParsingException::class)
    override fun getName(): String {
        return KIOSK_ID
    }

    companion object {
        const val KIOSK_ID: String = "live"
    }
}
