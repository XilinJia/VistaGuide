package ac.mdiq.vista.extractor.services.bandcamp.extractors

import com.grack.nanojson.JsonArray
import com.grack.nanojson.JsonObject
import ac.mdiq.vista.extractor.InfoItem
import ac.mdiq.vista.extractor.ListExtractor.InfoItemsPage.Companion.emptyPage
import ac.mdiq.vista.extractor.MultiInfoItemsCollector
import ac.mdiq.vista.extractor.Page
import ac.mdiq.vista.extractor.StreamingService
import ac.mdiq.vista.extractor.channel.tabs.ChannelTabExtractor
import ac.mdiq.vista.extractor.channel.tabs.ChannelTabs
import ac.mdiq.vista.extractor.downloader.Downloader
import ac.mdiq.vista.extractor.exceptions.ExtractionException
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.linkhandler.ListLinkHandler
import ac.mdiq.vista.extractor.services.bandcamp.extractors.streaminfoitem.BandcampDiscographStreamInfoItemExtractor
import java.io.IOException


class BandcampChannelTabExtractor(service: StreamingService, linkHandler: ListLinkHandler) : ChannelTabExtractor(service, linkHandler) {

    private var discography: JsonArray? = null
    private var filter: String? = null

    init {
        val tab = linkHandler.contentFilters[0]
        filter = when (tab) {
            ChannelTabs.TRACKS -> "track"
            ChannelTabs.ALBUMS -> "album"
            else -> throw IllegalArgumentException("Unsupported channel tab: $tab")
        }
    }

    @Throws(ParsingException::class)
    override fun onFetchPage(downloader: Downloader) {
        if (discography == null) discography = BandcampExtractorHelper.getArtistDetails(id).getArray("discography")
    }

    @get:Throws(IOException::class, ExtractionException::class)

    override val initialPage: InfoItemsPage<InfoItem>
        get() {
            val collector = MultiInfoItemsCollector(serviceId)

            for (discograph in discography!!) {
                // A discograph is as an item appears in a discography
                if (discograph !is JsonObject) continue

                val discographJsonObject = discograph
                val itemType = discographJsonObject.getString("item_type", "")

                if (itemType != filter) continue

                when (itemType) {
                    "track" -> collector.commit(BandcampDiscographStreamInfoItemExtractor(discographJsonObject, url))
                    "album" -> collector.commit(BandcampAlbumInfoItemExtractor(discographJsonObject, url))
                }
            }

            return InfoItemsPage(collector, null)
        }

    override fun getPage(page: Page?): InfoItemsPage<InfoItem> {
        return emptyPage()
    }

    companion object {
        fun fromDiscography(service: StreamingService, linkHandler: ListLinkHandler, discography: JsonArray?): BandcampChannelTabExtractor {
            val tabExtractor = BandcampChannelTabExtractor(service, linkHandler)
            tabExtractor.discography = discography
            return tabExtractor
        }
    }
}
