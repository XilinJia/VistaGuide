package ac.mdiq.vista.extractor.services.media_ccc.extractors

import com.grack.nanojson.JsonObject
import com.grack.nanojson.JsonParser
import com.grack.nanojson.JsonParserException
import ac.mdiq.vista.extractor.ListExtractor.InfoItemsPage.Companion.emptyPage
import ac.mdiq.vista.extractor.Page
import ac.mdiq.vista.extractor.StreamingService
import ac.mdiq.vista.extractor.downloader.Downloader
import ac.mdiq.vista.extractor.exceptions.ExtractionException
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.kiosk.KioskExtractor
import ac.mdiq.vista.extractor.linkhandler.ListLinkHandler
import ac.mdiq.vista.extractor.localization.DateWrapper
import ac.mdiq.vista.extractor.stream.StreamInfoItem
import ac.mdiq.vista.extractor.stream.StreamInfoItemsCollector
import java.io.IOException


class MediaCCCRecentKiosk(
        streamingService: StreamingService,
        linkHandler: ListLinkHandler,
        kioskId: String)
    : KioskExtractor<StreamInfoItem>(streamingService, linkHandler, kioskId) {

    private var doc: JsonObject? = null

    @Throws(IOException::class, ExtractionException::class)
    override fun onFetchPage(downloader: Downloader) {
        val site = downloader.get("https://api.media.ccc.de/public/events/recent",
            extractorLocalization).responseBody()
        try {
            doc = JsonParser.`object`().from(site)
        } catch (jpe: JsonParserException) {
            throw ExtractionException("Could not parse json.", jpe)
        }
    }

    @get:Throws(IOException::class, ExtractionException::class)

    override val initialPage: InfoItemsPage<StreamInfoItem>
        get() {
            val events = doc!!.getArray("events")

            // Streams in the recent kiosk are not ordered by the release date.
            // Sort them to have the latest stream at the beginning of the list.
            val comparator: Comparator<StreamInfoItem> = Comparator
                .comparing<StreamInfoItem, DateWrapper>(StreamInfoItem::uploadDate, Comparator
                    .nullsLast(Comparator.comparing { obj: DateWrapper -> obj.offsetDateTime() }))
                .reversed()
            val collector = StreamInfoItemsCollector(serviceId,
                comparator)

            events.stream()
                .filter { o: Any? -> JsonObject::class.java.isInstance(o) }
                .map { obj: Any? -> JsonObject::class.java.cast(obj) }
                .map { event: JsonObject -> MediaCCCRecentKioskExtractor(event) } // #813 / voc/voctoweb#609 -> returns faulty data -> filter it out
                .filter { extractor: MediaCCCRecentKioskExtractor -> extractor.getDuration() > 0 }
                .forEach { extractor: MediaCCCRecentKioskExtractor? ->
                    collector.commit(
                        extractor!!)
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
        const val KIOSK_ID: String = "recent"
    }
}
