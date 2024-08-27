package ac.mdiq.vista.extractor.services.media_ccc.extractors

import com.grack.nanojson.JsonObject
import com.grack.nanojson.JsonParser
import com.grack.nanojson.JsonParserException
import ac.mdiq.vista.extractor.ListExtractor.InfoItemsPage.Companion.emptyPage
import ac.mdiq.vista.extractor.Page
import ac.mdiq.vista.extractor.StreamingService
import ac.mdiq.vista.extractor.channel.ChannelInfoItem
import ac.mdiq.vista.extractor.channel.ChannelInfoItemsCollector
import ac.mdiq.vista.extractor.downloader.Downloader
import ac.mdiq.vista.extractor.exceptions.ExtractionException
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.kiosk.KioskExtractor
import ac.mdiq.vista.extractor.linkhandler.ListLinkHandler
import ac.mdiq.vista.extractor.services.media_ccc.extractors.infoItems.MediaCCCConferenceInfoItemExtractor
import java.io.IOException


class MediaCCCConferenceKiosk(
        streamingService: StreamingService,
        linkHandler: ListLinkHandler,
        kioskId: String)
    : KioskExtractor<ChannelInfoItem>(streamingService, linkHandler, kioskId) {

    private var doc: JsonObject? = null


    override val initialPage: InfoItemsPage<ChannelInfoItem>
        get() {
            val conferences = doc!!.getArray("conferences")
            val collector = ChannelInfoItemsCollector(serviceId)
            for (i in conferences.indices) {
                collector.commit(MediaCCCConferenceInfoItemExtractor(conferences.getObject(i)))
            }

            return InfoItemsPage(collector, null)
        }

    override fun getPage(page: Page?): InfoItemsPage<ChannelInfoItem> {
        return emptyPage()
    }

    @Throws(IOException::class, ExtractionException::class)
    override fun onFetchPage(downloader: Downloader) {
        val site = downloader.get(getLinkHandler().url, extractorLocalization)
            .responseBody()
        try {
            doc = JsonParser.`object`().from(site)
        } catch (jpe: JsonParserException) {
            throw ExtractionException("Could not parse json.", jpe)
        }
    }


    @Throws(ParsingException::class)
    override fun getName(): String {
        return doc!!.getString("Conferences")
    }

    companion object {
        const val KIOSK_ID: String = "conferences"
    }
}
