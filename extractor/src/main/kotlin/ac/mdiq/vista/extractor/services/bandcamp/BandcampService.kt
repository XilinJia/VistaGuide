// Created by Fynn Godau 2019, licensed GNU GPL version 3 or later
package ac.mdiq.vista.extractor.services.bandcamp

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
import ac.mdiq.vista.extractor.services.bandcamp.extractors.*
import ac.mdiq.vista.extractor.services.bandcamp.extractors.BandcampExtractorHelper.BASE_URL
import ac.mdiq.vista.extractor.services.bandcamp.extractors.BandcampExtractorHelper.isRadioUrl
import ac.mdiq.vista.extractor.services.bandcamp.linkHandler.*
import ac.mdiq.vista.extractor.stream.StreamExtractor
import ac.mdiq.vista.extractor.subscription.SubscriptionExtractor
import ac.mdiq.vista.extractor.suggestion.SuggestionExtractor
import java.util.*

class BandcampService(id: Int) : StreamingService(id, "Bandcamp", listOf(MediaCapability.AUDIO, MediaCapability.COMMENTS)) {

    override val baseUrl: String
        get() = BASE_URL

    override fun getStreamLHFactory(): LinkHandlerFactory {
        return BandcampStreamLinkHandlerFactory.instance
    }

    override fun getChannelLHFactory(): ListLinkHandlerFactory {
        return BandcampChannelLinkHandlerFactory.instance
    }

    override fun getChannelTabLHFactory(): ListLinkHandlerFactory {
        return BandcampChannelTabLinkHandlerFactory.instance
    }

    override fun getPlaylistLHFactory(): ListLinkHandlerFactory {
        return BandcampPlaylistLinkHandlerFactory.instance
    }

    override fun getSearchQHFactory(): SearchQueryHandlerFactory {
        return BandcampSearchQueryHandlerFactory.instance
    }

    override fun getCommentsLHFactory(): ListLinkHandlerFactory {
        return BandcampCommentsLinkHandlerFactory.instance
    }

    override fun getSearchExtractor(queryHandler: SearchQueryHandler): SearchExtractor {
        return BandcampSearchExtractor(this, queryHandler)
    }

    override fun getSuggestionExtractor(): SuggestionExtractor {
        return BandcampSuggestionExtractor(this)
    }

    override fun getSubscriptionExtractor(): SubscriptionExtractor? {
        return null
    }

    @Throws(ExtractionException::class)
    override fun getKioskList(): KioskList {
        val kioskList = KioskList(this)
        val h: ListLinkHandlerFactory = BandcampFeaturedLinkHandlerFactory.instance

        try {
            kioskList.addKioskEntry(
                object: KioskExtractorFactory {
                    override fun createNewKiosk(streamingService: StreamingService, url: String, kioskId: String): KioskExtractor<*> {
                        return BandcampFeaturedExtractor(this@BandcampService, h.fromUrl(BandcampFeaturedExtractor.FEATURED_API_URL), kioskId)
                    }
                },
                h,
                BandcampFeaturedExtractor.KIOSK_FEATURED
            )

            kioskList.addKioskEntry(
                object : KioskExtractorFactory {
                    override fun createNewKiosk(streamingService: StreamingService, url: String, kioskId: String): KioskExtractor<*> {
                        return BandcampRadioExtractor(this@BandcampService, h.fromUrl(BandcampRadioExtractor.RADIO_API_URL), kioskId)
                    }
                },
                h,
                BandcampRadioExtractor.KIOSK_RADIO
            )

            kioskList.setDefaultKiosk(BandcampFeaturedExtractor.KIOSK_FEATURED)
        } catch (e: Exception) {
            throw ExtractionException(e)
        }

        return kioskList
    }

    override fun getChannelExtractor(linkHandler: ListLinkHandler): ChannelExtractor {
        return BandcampChannelExtractor(this, linkHandler)
    }

    override fun getChannelTabExtractor(linkHandler: ListLinkHandler): ChannelTabExtractor {
        return if (linkHandler is ReadyChannelTabListLinkHandler) {
            linkHandler.getChannelTabExtractor(this)
        } else {
            BandcampChannelTabExtractor(this, linkHandler)
        }
    }

    override fun getPlaylistExtractor(linkHandler: ListLinkHandler): PlaylistExtractor {
        return BandcampPlaylistExtractor(this, linkHandler)
    }

    override fun getStreamExtractor(linkHandler: LinkHandler): StreamExtractor {
        if (isRadioUrl(linkHandler.url)) {
            return BandcampRadioStreamExtractor(this, linkHandler)
        }
        return BandcampStreamExtractor(this, linkHandler)
    }

    override fun getCommentsExtractor(linkHandler: ListLinkHandler): CommentsExtractor {
        return BandcampCommentsExtractor(this, linkHandler)
    }
}
