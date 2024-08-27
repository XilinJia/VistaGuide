package ac.mdiq.vista.extractor.services.peertube

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
import ac.mdiq.vista.extractor.playlist.PlaylistExtractor
import ac.mdiq.vista.extractor.search.SearchExtractor
import ac.mdiq.vista.extractor.services.peertube.extractors.*
import ac.mdiq.vista.extractor.services.peertube.linkHandler.*
import ac.mdiq.vista.extractor.stream.StreamExtractor
import ac.mdiq.vista.extractor.subscription.SubscriptionExtractor
import ac.mdiq.vista.extractor.suggestion.SuggestionExtractor
import java.util.*

class PeertubeService @JvmOverloads constructor(
        id: Int,
        @JvmField var instance: PeertubeInstance = PeertubeInstance.DEFAULT_INSTANCE)
    : StreamingService(id, "PeerTube", listOf(MediaCapability.VIDEO, MediaCapability.COMMENTS)) {

    override fun getStreamLHFactory(): LinkHandlerFactory {
        return PeertubeStreamLinkHandlerFactory.instance
    }

    override fun getChannelLHFactory(): ListLinkHandlerFactory {
        return PeertubeChannelLinkHandlerFactory.instance
    }

    override fun getChannelTabLHFactory(): ListLinkHandlerFactory {
        return PeertubeChannelTabLinkHandlerFactory.instance
    }

    override fun getPlaylistLHFactory(): ListLinkHandlerFactory {
        return PeertubePlaylistLinkHandlerFactory.instance
    }

    override fun getSearchQHFactory(): SearchQueryHandlerFactory {
        return PeertubeSearchQueryHandlerFactory.instance
    }

    override fun getCommentsLHFactory(): ListLinkHandlerFactory {
        return PeertubeCommentsLinkHandlerFactory.instance
    }

    override fun getSearchExtractor(queryHandler: SearchQueryHandler): SearchExtractor {
        val contentFilters = queryHandler.contentFilters
        return PeertubeSearchExtractor(this, queryHandler, contentFilters.isNotEmpty() && contentFilters[0].startsWith("sepia_"))
    }

    override fun getSuggestionExtractor(): SuggestionExtractor {
        return PeertubeSuggestionExtractor(this)
    }

    override fun getSubscriptionExtractor(): SubscriptionExtractor? {
        return null
    }

    @Throws(ExtractionException::class)
    override fun getChannelExtractor(linkHandler: ListLinkHandler): ChannelExtractor {
        return if (linkHandler.url.contains("/video-channels/")) PeertubeChannelExtractor(this, linkHandler)
        else PeertubeAccountExtractor(this, linkHandler)
    }

    @Throws(ExtractionException::class)
    override fun getChannelTabExtractor(linkHandler: ListLinkHandler): ChannelTabExtractor {
        return PeertubeChannelTabExtractor(this, linkHandler)
    }

    @Throws(ExtractionException::class)
    override fun getPlaylistExtractor(linkHandler: ListLinkHandler): PlaylistExtractor {
        return PeertubePlaylistExtractor(this, linkHandler)
    }

    @Throws(ExtractionException::class)
    override fun getStreamExtractor(linkHandler: LinkHandler): StreamExtractor {
        return PeertubeStreamExtractor(this, linkHandler)
    }

    @Throws(ExtractionException::class)
    override fun getCommentsExtractor(linkHandler: ListLinkHandler): CommentsExtractor {
        return PeertubeCommentsExtractor(this, linkHandler)
    }

    override val baseUrl: String
        get() = instance.url

    @Throws(ExtractionException::class)
    override fun getKioskList(): KioskList {
        val h: PeertubeTrendingLinkHandlerFactory = PeertubeTrendingLinkHandlerFactory.instance

        val kioskFactory: KioskExtractorFactory = object: KioskExtractorFactory {
            override fun createNewKiosk(streamingService: StreamingService, url: String, kioskId: String): KioskExtractor<*> {
                return PeertubeTrendingExtractor(this@PeertubeService, h.fromId(kioskId), kioskId)
            }
        }
        val list = KioskList(this)

        // add kiosks here e.g.:
        try {
            list.addKioskEntry(kioskFactory, h, PeertubeTrendingLinkHandlerFactory.KIOSK_TRENDING)
            list.addKioskEntry(kioskFactory, h, PeertubeTrendingLinkHandlerFactory.KIOSK_MOST_LIKED)
            list.addKioskEntry(kioskFactory, h, PeertubeTrendingLinkHandlerFactory.KIOSK_RECENT)
            list.addKioskEntry(kioskFactory, h, PeertubeTrendingLinkHandlerFactory.KIOSK_LOCAL)
            list.setDefaultKiosk(PeertubeTrendingLinkHandlerFactory.KIOSK_TRENDING)
        } catch (e: Exception) {
            throw ExtractionException(e)
        }

        return list
    }
}
