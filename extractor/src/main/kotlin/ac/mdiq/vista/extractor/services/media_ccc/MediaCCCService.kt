package ac.mdiq.vista.extractor.services.media_ccc

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
import ac.mdiq.vista.extractor.services.media_ccc.extractors.*
import ac.mdiq.vista.extractor.services.media_ccc.linkHandler.MediaCCCConferenceLinkHandlerFactory
import ac.mdiq.vista.extractor.services.media_ccc.linkHandler.MediaCCCConferencesListLinkHandlerFactory
import ac.mdiq.vista.extractor.services.media_ccc.linkHandler.MediaCCCSearchQueryHandlerFactory
import ac.mdiq.vista.extractor.services.media_ccc.linkHandler.MediaCCCStreamLinkHandlerFactory
import ac.mdiq.vista.extractor.stream.StreamExtractor
import ac.mdiq.vista.extractor.subscription.SubscriptionExtractor
import ac.mdiq.vista.extractor.suggestion.SuggestionExtractor
import java.util.*

class MediaCCCService(id: Int) :
    StreamingService(id, "media.ccc.de", listOf(MediaCapability.AUDIO, MediaCapability.VIDEO)) {
    override fun getSearchExtractor(queryHandler: SearchQueryHandler): SearchExtractor {
        return MediaCCCSearchExtractor(this, queryHandler)
    }

    override fun getStreamLHFactory(): LinkHandlerFactory {
        return MediaCCCStreamLinkHandlerFactory.instance
    }

    override fun getChannelLHFactory(): ListLinkHandlerFactory {
        return MediaCCCConferenceLinkHandlerFactory.instance
    }

    override fun getChannelTabLHFactory(): ListLinkHandlerFactory {
        // there is just one channel tab in MediaCCC, the one containing conferences, so there is
        // no need for a specific channel tab link handler, but we can just use the channel one
        return MediaCCCConferenceLinkHandlerFactory.instance
    }

    override fun getPlaylistLHFactory(): ListLinkHandlerFactory? {
        return null
    }

    override fun getSearchQHFactory(): SearchQueryHandlerFactory {
        return MediaCCCSearchQueryHandlerFactory.instance
    }

    override fun getStreamExtractor(linkHandler: LinkHandler): StreamExtractor {
        if (MediaCCCParsingHelper.isLiveStreamId(linkHandler.id)) {
            return MediaCCCLiveStreamExtractor(this, linkHandler)
        }
        return MediaCCCStreamExtractor(this, linkHandler)
    }

    override fun getChannelExtractor(linkHandler: ListLinkHandler): ChannelExtractor {
        return MediaCCCConferenceExtractor(this, linkHandler)
    }

    override fun getChannelTabExtractor(linkHandler: ListLinkHandler): ChannelTabExtractor {
        return if (linkHandler is ReadyChannelTabListLinkHandler) {
            // conference data has already been fetched, let the ReadyChannelTabListLinkHandler
            // create a MediaCCCChannelTabExtractor with that data
            linkHandler.getChannelTabExtractor(this)
        } else {
            // conference data has not been fetched yet, so pass null instead
            MediaCCCChannelTabExtractor(this, linkHandler, null)
        }
    }

    override fun getPlaylistExtractor(linkHandler: ListLinkHandler): PlaylistExtractor? {
        return null
    }

    override fun getSuggestionExtractor(): SuggestionExtractor? {
        return null
    }

    @Throws(ExtractionException::class)
    override fun getKioskList(): KioskList {
        val list = KioskList(this)
        val h: ListLinkHandlerFactory = MediaCCCConferencesListLinkHandlerFactory.instance

        // add kiosks here e.g.:
        try {
            list.addKioskEntry(
                object: KioskExtractorFactory {
                    override fun createNewKiosk(streamingService: StreamingService, url: String, kioskId: String): KioskExtractor<*> {
                        return MediaCCCConferenceKiosk(this@MediaCCCService, h.fromUrl(url), kioskId)
                    }
                },
                h,
                MediaCCCConferenceKiosk.KIOSK_ID
            )

            list.addKioskEntry(
                object: KioskExtractorFactory {
                    override fun createNewKiosk(streamingService: StreamingService, url: String, kioskId: String): KioskExtractor<*> {
                        return MediaCCCRecentKiosk(this@MediaCCCService, h.fromUrl(url), kioskId)
                    }
                },
                h,
                MediaCCCRecentKiosk.KIOSK_ID
            )

            list.addKioskEntry(
                object: KioskExtractorFactory {
                    override fun createNewKiosk(streamingService: StreamingService, url: String, kioskId: String): KioskExtractor<*> {
                        return MediaCCCLiveStreamKiosk(this@MediaCCCService, h.fromUrl(url), kioskId)
                    }
                },
                h,
                MediaCCCLiveStreamKiosk.KIOSK_ID
            )

            list.setDefaultKiosk(MediaCCCRecentKiosk.KIOSK_ID)
        } catch (e: Exception) {
            throw ExtractionException(e)
        }

        return list
    }

    override fun getSubscriptionExtractor(): SubscriptionExtractor? {
        return null
    }

    override fun getCommentsLHFactory(): ListLinkHandlerFactory? {
        return null
    }

    override fun getCommentsExtractor(linkHandler: ListLinkHandler): CommentsExtractor? {
        return null
    }

    override val baseUrl: String
        get() = "https://media.ccc.de"
}
