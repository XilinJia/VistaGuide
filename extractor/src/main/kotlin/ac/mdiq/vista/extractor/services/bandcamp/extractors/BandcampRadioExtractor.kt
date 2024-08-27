// Created by Fynn Godau 2019, licensed GNU GPL version 3 or later
package ac.mdiq.vista.extractor.services.bandcamp.extractors

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
import ac.mdiq.vista.extractor.services.bandcamp.extractors.BandcampExtractorHelper.BASE_API_URL
import ac.mdiq.vista.extractor.stream.StreamInfoItem
import ac.mdiq.vista.extractor.stream.StreamInfoItemsCollector
import java.io.IOException


class BandcampRadioExtractor(
        streamingService: StreamingService,
        linkHandler: ListLinkHandler,
        kioskId: String)
    : KioskExtractor<StreamInfoItem>(streamingService, linkHandler, kioskId) {

    private var json: JsonObject? = null

    @Throws(IOException::class, ExtractionException::class)
    override fun onFetchPage(downloader: Downloader) {
        try {
            json = JsonParser.`object`().from(downloader.get(RADIO_API_URL).responseBody())
        } catch (e: JsonParserException) {
            throw ExtractionException("Could not parse Bandcamp Radio API response", e)
        }
    }


    @Throws(ParsingException::class)
    override fun getName(): String {
        return KIOSK_RADIO
    }


    override val initialPage: InfoItemsPage<StreamInfoItem>
        get() {
            val collector = StreamInfoItemsCollector(serviceId)
            val radioShows = json!!.getArray("results")

            for (i in radioShows.indices) {
                val radioShow = radioShows.getObject(i)
                collector.commit(BandcampRadioInfoItemExtractor(radioShow))
            }
            return InfoItemsPage(collector, null)
        }

    override fun getPage(page: Page?): InfoItemsPage<StreamInfoItem> {
        return emptyPage()
    }

    companion object {
        const val KIOSK_RADIO: String = "Radio"
        const val RADIO_API_URL: String = "$BASE_API_URL/bcweekly/3/list"
    }
}
