package ac.mdiq.vista.extractor.services.soundcloud.extractors

import ac.mdiq.vista.extractor.InfoItem
import ac.mdiq.vista.extractor.MultiInfoItemsCollector
import ac.mdiq.vista.extractor.Page
import ac.mdiq.vista.extractor.StreamingService
import ac.mdiq.vista.extractor.channel.tabs.ChannelTabExtractor
import ac.mdiq.vista.extractor.channel.tabs.ChannelTabs
import ac.mdiq.vista.extractor.downloader.Downloader
import ac.mdiq.vista.extractor.exceptions.ExtractionException
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.linkhandler.ListLinkHandler
import ac.mdiq.vista.extractor.services.soundcloud.SoundcloudParsingHelper
import java.io.IOException


class SoundcloudChannelTabExtractor(service: StreamingService, linkHandler: ListLinkHandler) : ChannelTabExtractor(service, linkHandler) {


    override val id: String = getLinkHandler().id

    @get:Throws(ParsingException::class)

    private val endpoint: String
        get() {
            when (getName()) {
                ChannelTabs.TRACKS -> return "/tracks"
                ChannelTabs.PLAYLISTS -> return "/playlists_without_albums"
                ChannelTabs.ALBUMS -> return "/albums"
            }
            throw ParsingException("Unsupported tab: ${getName()}")
        }

    override fun onFetchPage(downloader: Downloader) {}

    @get:Throws(IOException::class, ExtractionException::class)

    override val initialPage: InfoItemsPage<InfoItem>
        get() = getPage(Page("$USERS_ENDPOINT$id$endpoint?client_id=${SoundcloudParsingHelper.clientId()}&limit=20&linked_partitioning=1"))

    @Throws(IOException::class, ExtractionException::class)
    override fun getPage(page: Page?): InfoItemsPage<InfoItem> {
        require(!(page == null || page.url.isNullOrEmpty())) { "Page doesn't contain an URL" }

        val collector = MultiInfoItemsCollector(serviceId)
        val nextPageUrl = SoundcloudParsingHelper.getInfoItemsFromApi(collector, page.url)

        return InfoItemsPage(collector, Page(nextPageUrl))
    }

    companion object {
        private const val USERS_ENDPOINT = SoundcloudParsingHelper.SOUNDCLOUD_API_V2_URL + "users/"
    }
}
