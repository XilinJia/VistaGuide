package ac.mdiq.vista.extractor.services.youtube

import ac.mdiq.vista.extractor.StreamingService
import ac.mdiq.vista.extractor.StreamingService.ServiceInfo.MediaCapability
import ac.mdiq.vista.extractor.channel.ChannelExtractor
import ac.mdiq.vista.extractor.channel.tabs.ChannelTabExtractor
import ac.mdiq.vista.extractor.comments.CommentsExtractor
import ac.mdiq.vista.extractor.exceptions.ExtractionException
import ac.mdiq.vista.extractor.feed.FeedExtractor
import ac.mdiq.vista.extractor.kiosk.KioskExtractor
import ac.mdiq.vista.extractor.kiosk.KioskList
import ac.mdiq.vista.extractor.kiosk.KioskList.KioskExtractorFactory
import ac.mdiq.vista.extractor.linkhandler.*
import ac.mdiq.vista.extractor.localization.ContentCountry
import ac.mdiq.vista.extractor.localization.Localization
import ac.mdiq.vista.extractor.playlist.PlaylistExtractor
import ac.mdiq.vista.extractor.search.SearchExtractor
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.isYoutubeMixId
import ac.mdiq.vista.extractor.services.youtube.extractors.*
import ac.mdiq.vista.extractor.services.youtube.linkHandler.*
import ac.mdiq.vista.extractor.stream.StreamExtractor
import ac.mdiq.vista.extractor.subscription.SubscriptionExtractor
import ac.mdiq.vista.extractor.suggestion.SuggestionExtractor
import java.util.*


/*
* Created by Christian Schabesberger on 23.08.15.
*
* Copyright (C) 2018 Christian Schabesberger <chris.schabesberger@mailbox.org>
* Copyright (C) 2024 Xilin Jia <https://github.com/XilinJia>
* YoutubeService.kt is part of Vista Guide.
*
* Vista Guide is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* Vista Guide is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with Vista Guide.  If not, see <http://www.gnu.org/licenses/>.
*/
class YoutubeService(id: Int) : StreamingService(id, "YouTube",
    listOf(MediaCapability.AUDIO, MediaCapability.VIDEO, MediaCapability.LIVE, MediaCapability.COMMENTS)) {

    override val baseUrl: String
        get() = "https://youtube.com"

    override fun getStreamLHFactory(): LinkHandlerFactory {
        return YoutubeStreamLinkHandlerFactory.instance
    }

    override fun getChannelLHFactory(): ListLinkHandlerFactory {
        return YoutubeChannelLinkHandlerFactory.instance
    }

    override fun getChannelTabLHFactory(): ListLinkHandlerFactory {
        return YoutubeChannelTabLinkHandlerFactory.instance
    }

    override fun getPlaylistLHFactory(): ListLinkHandlerFactory {
        return YoutubePlaylistLinkHandlerFactory.instance
    }

    override fun getSearchQHFactory(): SearchQueryHandlerFactory {
        return YoutubeSearchQueryHandlerFactory.instance
    }

    override fun getStreamExtractor(linkHandler: LinkHandler): StreamExtractor {
        return YoutubeStreamExtractor(this, linkHandler)
    }

    override fun getChannelExtractor(linkHandler: ListLinkHandler): ChannelExtractor {
        return YoutubeChannelExtractor(this, linkHandler)
    }

    override fun getChannelTabExtractor(linkHandler: ListLinkHandler): ChannelTabExtractor {
        return if (linkHandler is ReadyChannelTabListLinkHandler) linkHandler.getChannelTabExtractor(this)
        else { YoutubeChannelTabExtractor(this, linkHandler) }
    }

    override fun getPlaylistExtractor(linkHandler: ListLinkHandler): PlaylistExtractor {
        return if (isYoutubeMixId(linkHandler.id)) YoutubeMixPlaylistExtractor(this, linkHandler)
        else { YoutubePlaylistExtractor(this, linkHandler) }
    }

    override fun getSearchExtractor(queryHandler: SearchQueryHandler): SearchExtractor {
        val contentFilters = queryHandler.contentFilters
        return if (contentFilters.isNotEmpty() && contentFilters[0].startsWith("music_")) YoutubeMusicSearchExtractor(this, queryHandler)
        else YoutubeSearchExtractor(this, queryHandler)
    }

    override fun getSuggestionExtractor(): SuggestionExtractor {
        return YoutubeSuggestionExtractor(this)
    }

    @Throws(ExtractionException::class)
    override fun getKioskList(): KioskList {
        val list = KioskList(this)
        val h: ListLinkHandlerFactory = YoutubeTrendingLinkHandlerFactory.instance

        // add kiosks here e.g.:
        try {
            list.addKioskEntry(object: KioskExtractorFactory {
                    override fun createNewKiosk(streamingService: StreamingService, url: String, kioskId: String): KioskExtractor<*> {
                        return YoutubeTrendingExtractor(this@YoutubeService, h.fromUrl(url), kioskId)
                    }
                }, h, YoutubeTrendingExtractor.KIOSK_ID)
            list.setDefaultKiosk(YoutubeTrendingExtractor.KIOSK_ID)
        } catch (e: Exception) { throw ExtractionException(e) }
        return list
    }

    override fun getSubscriptionExtractor(): SubscriptionExtractor {
        return YoutubeSubscriptionExtractor(this)
    }


    @Throws(ExtractionException::class)
    override fun getFeedExtractor(url: String): FeedExtractor {
        return YoutubeFeedExtractor(this, getChannelLHFactory().fromUrl(url))
    }

    override fun getCommentsLHFactory(): ListLinkHandlerFactory {
        return YoutubeCommentsLinkHandlerFactory.instance
    }

    @Throws(ExtractionException::class)
    override fun getCommentsExtractor(linkHandler: ListLinkHandler): CommentsExtractor {
        return YoutubeCommentsExtractor(this, linkHandler)
    }

    companion object {
        // https://www.youtube.com/picker_ajax?action_language_json=1
        val supportedLocalizations: List<Localization> = Localization.listFrom(
            "en-GB" /*"af", "am", "ar", "az", "be", "bg", "bn", "bs", "ca", "cs", "da", "de",
            "el", "en", "en-GB", "es", "es-419", "es-US", "et", "eu", "fa", "fi", "fil", "fr",
            "fr-CA", "gl", "gu", "hi", "hr", "hu", "hy", "id", "is", "it", "iw", "ja",
            "ka", "kk", "km", "kn", "ko", "ky", "lo", "lt", "lv", "mk", "ml", "mn",
            "mr", "ms", "my", "ne", "nl", "no", "pa", "pl", "pt", "pt-PT", "ro", "ru",
            "si", "sk", "sl", "sq", "sr", "sr-Latn", "sv", "sw", "ta", "te", "th", "tr",
            "uk", "ur", "uz", "vi", "zh-CN", "zh-HK", "zh-TW", "zu"*/
        )

        // https://www.youtube.com/picker_ajax?action_country_json=1
        val supportedCountries: List<ContentCountry> = ContentCountry.listFrom(
            "DZ", "AR", "AU", "AT", "AZ", "BH", "BD", "BY", "BE", "BO", "BA", "BR", "BG", "KH",
            "CA", "CL", "CO", "CR", "HR", "CY", "CZ", "DK", "DO", "EC", "EG", "SV", "EE", "FI",
            "FR", "GE", "DE", "GH", "GR", "GT", "HN", "HK", "HU", "IS", "IN", "ID", "IQ", "IE",
            "IL", "IT", "JM", "JP", "JO", "KZ", "KE", "KW", "LA", "LV", "LB", "LY", "LI", "LT",
            "LU", "MY", "MT", "MX", "ME", "MA", "NP", "NL", "NZ", "NI", "NG", "MK", "NO", "OM",
            "PK", "PA", "PG", "PY", "PE", "PH", "PL", "PT", "PR", "QA", "RO", "RU", "SA", "SN",
            "RS", "SG", "SK", "SI", "ZA", "KR", "ES", "LK", "SE", "CH", "TW", "TZ", "TH", "TN",
            "TR", "UG", "UA", "AE", "GB", "US", "UY", "VE", "VN", "YE", "ZW"
        )
    }
}
