package ac.mdiq.vista.extractor.services.soundcloud

import ac.mdiq.vista.extractor.StreamingService
import ac.mdiq.vista.extractor.StreamingService.ServiceInfo.MediaCapability
import ac.mdiq.vista.extractor.channel.ChannelExtractor
import ac.mdiq.vista.extractor.channel.tabs.ChannelTabExtractor
import ac.mdiq.vista.extractor.comments.CommentsExtractor
import ac.mdiq.vista.extractor.exceptions.ExtractionException
import ac.mdiq.vista.extractor.kiosk.KioskExtractor
import ac.mdiq.vista.extractor.kiosk.KioskList
import ac.mdiq.vista.extractor.kiosk.KioskList.KioskExtractorFactory
import ac.mdiq.vista.extractor.linkhandler.*
import ac.mdiq.vista.extractor.localization.ContentCountry
import ac.mdiq.vista.extractor.localization.ContentCountry.Companion.listFrom
import ac.mdiq.vista.extractor.playlist.PlaylistExtractor
import ac.mdiq.vista.extractor.search.SearchExtractor
import ac.mdiq.vista.extractor.services.soundcloud.extractors.*
import ac.mdiq.vista.extractor.services.soundcloud.linkHandler.*
import ac.mdiq.vista.extractor.stream.StreamExtractor
import ac.mdiq.vista.extractor.subscription.SubscriptionExtractor
import java.util.*

class SoundcloudService(id: Int) : StreamingService(id, "SoundCloud", listOf(MediaCapability.AUDIO, MediaCapability.COMMENTS)) {

    override val baseUrl: String
        get() = "https://soundcloud.com"

    override fun getSearchQHFactory(): SearchQueryHandlerFactory {
        return SoundcloudSearchQueryHandlerFactory.instance
    }

    override fun getStreamLHFactory(): LinkHandlerFactory {
        return SoundcloudStreamLinkHandlerFactory.instance
    }

    override fun getChannelLHFactory(): ListLinkHandlerFactory {
        return SoundcloudChannelLinkHandlerFactory.instance
    }

    override fun getChannelTabLHFactory(): ListLinkHandlerFactory {
        return SoundcloudChannelTabLinkHandlerFactory.instance
    }

    override fun getPlaylistLHFactory(): ListLinkHandlerFactory {
        return SoundcloudPlaylistLinkHandlerFactory.instance
    }

    override val supportedCountries: List<ContentCountry>
        get() =// Country selector here: https://soundcloud.com/charts/top?genre=all-music
            listFrom(
                "AU", "CA", "DE", "FR", "GB", "IE", "NL", "NZ", "US"
            )

    override fun getStreamExtractor(linkHandler: LinkHandler): StreamExtractor {
        return SoundcloudStreamExtractor(this, linkHandler)
    }

    override fun getChannelExtractor(linkHandler: ListLinkHandler): ChannelExtractor {
        return SoundcloudChannelExtractor(this, linkHandler)
    }

    override fun getChannelTabExtractor(linkHandler: ListLinkHandler): ChannelTabExtractor {
        return SoundcloudChannelTabExtractor(this, linkHandler)
    }

    override fun getPlaylistExtractor(linkHandler: ListLinkHandler): PlaylistExtractor {
        return SoundcloudPlaylistExtractor(this, linkHandler)
    }

    override fun getSearchExtractor(queryHandler: SearchQueryHandler): SearchExtractor {
        return SoundcloudSearchExtractor(this, queryHandler)
    }

    override fun getSuggestionExtractor(): SoundcloudSuggestionExtractor {
        return SoundcloudSuggestionExtractor(this)
    }

    @Throws(ExtractionException::class)
    override fun getKioskList(): KioskList {
        val list = KioskList(this)

        val h: SoundcloudChartsLinkHandlerFactory =
            SoundcloudChartsLinkHandlerFactory.instance
        val chartsFactory: KioskExtractorFactory = object: KioskExtractorFactory {
            override fun createNewKiosk(streamingService: StreamingService, url: String, kioskId: String): KioskExtractor<*> {
                return SoundcloudChartsExtractor(this@SoundcloudService, h.fromUrl(url), kioskId)
            }
        }
        // add kiosks here e.g.:
        try {
            list.addKioskEntry(chartsFactory, h, "Top 50")
            list.addKioskEntry(chartsFactory, h, "New & hot")
            list.setDefaultKiosk("New & hot")
        } catch (e: Exception) {
            throw ExtractionException(e)
        }

        return list
    }

    override fun getSubscriptionExtractor(): SubscriptionExtractor {
        return SoundcloudSubscriptionExtractor(this)
    }

    override fun getCommentsLHFactory(): ListLinkHandlerFactory {
        return SoundcloudCommentsLinkHandlerFactory.instance
    }

    @Throws(ExtractionException::class)
    override fun getCommentsExtractor(linkHandler: ListLinkHandler): CommentsExtractor {
        return SoundcloudCommentsExtractor(this, linkHandler)
    }
}
