package ac.mdiq.vista.extractor.services.soundcloud.extractors

import ac.mdiq.vista.extractor.Page
import ac.mdiq.vista.extractor.ServiceList.SoundCloud
import ac.mdiq.vista.extractor.StreamingService
import ac.mdiq.vista.extractor.downloader.Downloader
import ac.mdiq.vista.extractor.exceptions.ExtractionException
import ac.mdiq.vista.extractor.kiosk.KioskExtractor
import ac.mdiq.vista.extractor.linkhandler.ListLinkHandler
import ac.mdiq.vista.extractor.localization.ContentCountry
import ac.mdiq.vista.extractor.services.soundcloud.SoundcloudParsingHelper
import ac.mdiq.vista.extractor.stream.StreamInfoItem
import ac.mdiq.vista.extractor.stream.StreamInfoItemsCollector
import java.io.IOException


class SoundcloudChartsExtractor(service: StreamingService, linkHandler: ListLinkHandler, kioskId: String)
    : KioskExtractor<StreamInfoItem>(service, linkHandler, kioskId) {

    override fun onFetchPage(downloader: Downloader) {}


    override fun getName(): String {
        return id
    }

    @Throws(IOException::class, ExtractionException::class)
    override fun getPage(page: Page?): InfoItemsPage<StreamInfoItem> {
        require(!(page == null || page.url.isNullOrEmpty())) { "Page doesn't contain an URL" }

        val collector = StreamInfoItemsCollector(serviceId)
        val nextPageUrl = SoundcloudParsingHelper.getStreamsFromApi(collector,
            page.url, true)

        return InfoItemsPage(collector, Page(nextPageUrl))
    }

    @get:Throws(IOException::class, ExtractionException::class)

    override val initialPage: InfoItemsPage<StreamInfoItem>
        get() {
            val collector = StreamInfoItemsCollector(serviceId)

            var apiUrl =
                (SoundcloudParsingHelper.SOUNDCLOUD_API_V2_URL + "charts" + "?genre=soundcloud:genres:all-music"
                        + "&client_id=" + SoundcloudParsingHelper.clientId())

            apiUrl += if (id == "Top 50") {
                "&kind=top"
            } else {
                "&kind=trending"
            }

            val contentCountry: ContentCountry = SoundCloud.contentCountry
            var apiUrlWithRegion: String? = null
            if (service.supportedCountries.contains(contentCountry)) {
                apiUrlWithRegion = (apiUrl + "&region=soundcloud:regions:"
                        + contentCountry.countryCode)
            }
            val nextPageUrl = try {
                SoundcloudParsingHelper.getStreamsFromApi(collector, apiUrlWithRegion ?: apiUrl, true)
            } catch (e: IOException) {
                // Request to other region may be geo-restricted.
                // See https://github.com/XilinJia/VistaGuide/issues/537.
                // We retry without the specified region.
                SoundcloudParsingHelper.getStreamsFromApi(collector, apiUrl, true)
            }

            return InfoItemsPage(collector, Page(nextPageUrl))
        }
}
