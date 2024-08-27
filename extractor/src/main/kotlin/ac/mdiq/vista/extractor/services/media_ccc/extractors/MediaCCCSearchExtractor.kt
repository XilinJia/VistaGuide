package ac.mdiq.vista.extractor.services.media_ccc.extractors

import com.grack.nanojson.JsonObject
import com.grack.nanojson.JsonParser
import com.grack.nanojson.JsonParserException
import ac.mdiq.vista.extractor.*
import ac.mdiq.vista.extractor.ListExtractor.InfoItemsPage.Companion.emptyPage
import ac.mdiq.vista.extractor.channel.ChannelInfoItem
import ac.mdiq.vista.extractor.channel.ChannelInfoItemExtractor
import ac.mdiq.vista.extractor.downloader.Downloader
import ac.mdiq.vista.extractor.exceptions.ExtractionException
import ac.mdiq.vista.extractor.linkhandler.SearchQueryHandler
import ac.mdiq.vista.extractor.search.SearchExtractor
import ac.mdiq.vista.extractor.services.media_ccc.extractors.infoItems.MediaCCCStreamInfoItemExtractor
import ac.mdiq.vista.extractor.services.media_ccc.linkHandler.MediaCCCConferencesListLinkHandlerFactory
import ac.mdiq.vista.extractor.services.media_ccc.linkHandler.MediaCCCSearchQueryHandlerFactory
import java.io.IOException
import java.util.*


class MediaCCCSearchExtractor(service: StreamingService, linkHandler: SearchQueryHandler) : SearchExtractor(service, linkHandler) {

    private var doc: JsonObject? = null
    private var conferenceKiosk: MediaCCCConferenceKiosk? = null

    init {
        try {
            conferenceKiosk = MediaCCCConferenceKiosk(service, MediaCCCConferencesListLinkHandlerFactory.instance.fromId("conferences"), "conferences")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    override val searchSuggestion: String
        get() = ""

    override val isCorrectedSearch: Boolean
        get() = false


    override val metaInfo: List<MetaInfo>
        get() = emptyList()


    override val initialPage: InfoItemsPage<InfoItem>
        get() {
            val searchItems = MultiInfoItemsCollector(serviceId)

            var handler = getLinkHandler()
            if (handler.contentFilters.contains(MediaCCCSearchQueryHandlerFactory.CONFERENCES)
                    || handler.contentFilters.contains(MediaCCCSearchQueryHandlerFactory.ALL) || handler.contentFilters.isEmpty()) {
                searchConferences(searchString, conferenceKiosk!!.initialPage.items, searchItems)
            }
//TODO: why check twice
            handler = getLinkHandler()
            if (handler.contentFilters.contains(MediaCCCSearchQueryHandlerFactory.EVENTS)
                    || handler.contentFilters.contains(MediaCCCSearchQueryHandlerFactory.ALL) || handler.contentFilters.isEmpty()) {
                val events = doc!!.getArray("events")
                for (i in events.indices) {
                    // Ensure only uploaded talks are shown in the search results.
                    // If the release date is null, the talk has not been held or uploaded yet
                    // and no streams are going to be available anyway.
                    if (events.getObject(i).getString("release_date") != null) searchItems.commit(MediaCCCStreamInfoItemExtractor(events.getObject(i)))
                }
            }
            return InfoItemsPage(searchItems, null)
        }

    override fun getPage(page: Page?): InfoItemsPage<InfoItem> {
        return emptyPage()
    }

    @Throws(IOException::class, ExtractionException::class)
    override fun onFetchPage(downloader: Downloader) {
        var handler = getLinkHandler()
        if (handler.contentFilters.contains(MediaCCCSearchQueryHandlerFactory.EVENTS)
                || handler.contentFilters.contains(MediaCCCSearchQueryHandlerFactory.ALL) || handler.contentFilters.isEmpty()) {
//            val url = url!!
            val site: String = downloader.get(url, extractorLocalization).responseBody()
            try {
                doc = JsonParser.`object`().from(site)
            } catch (jpe: JsonParserException) {
                throw ExtractionException("Could not parse JSON.", jpe)
            }
        }
        //TODO: why check twice
        handler = getLinkHandler()
        if (handler.contentFilters.contains(MediaCCCSearchQueryHandlerFactory.CONFERENCES)
                || handler.contentFilters.contains(MediaCCCSearchQueryHandlerFactory.ALL) || handler.contentFilters.isEmpty())
            conferenceKiosk!!.fetchPage()
    }

    private fun searchConferences(searchString: String, channelItems: List<ChannelInfoItem>?, collector: MultiInfoItemsCollector) {
        for (item in channelItems!!) {
            if (item.name.uppercase(Locale.getDefault()).contains(searchString.uppercase(Locale.getDefault()))) {
                collector.commit(object : ChannelInfoItemExtractor {
                    override fun getDescription(): String {
                        return item.description?:""
                    }

                    override fun getSubscriberCount(): Long {
                        return item.subscriberCount
                    }

                    override fun getStreamCount(): Long {
                        return item.streamCount
                    }

                    override fun isVerified(): Boolean {
                        return false
                    }

                    override val name: String
                        get() = item.name

                    override val url: String
                        get() = item.url


                    override val thumbnails: List<Image>
                        get() = item.thumbnails
                })
            }
        }
    }
}
