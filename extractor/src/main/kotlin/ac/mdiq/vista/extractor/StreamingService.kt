package ac.mdiq.vista.extractor

import ac.mdiq.vista.extractor.Vista.getPreferredContentCountry
import ac.mdiq.vista.extractor.Vista.getPreferredLocalization
import ac.mdiq.vista.extractor.StreamingService.ServiceInfo.MediaCapability
import ac.mdiq.vista.extractor.channel.ChannelExtractor
import ac.mdiq.vista.extractor.channel.tabs.ChannelTabExtractor
import ac.mdiq.vista.extractor.comments.CommentsExtractor
import ac.mdiq.vista.extractor.exceptions.ExtractionException
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.feed.FeedExtractor
import ac.mdiq.vista.extractor.kiosk.KioskList
import ac.mdiq.vista.extractor.linkhandler.*
import ac.mdiq.vista.extractor.localization.ContentCountry
import ac.mdiq.vista.extractor.localization.Localization
import ac.mdiq.vista.extractor.localization.TimeAgoParser
import ac.mdiq.vista.extractor.localization.TimeAgoPatternsManager
import ac.mdiq.vista.extractor.playlist.PlaylistExtractor
import ac.mdiq.vista.extractor.search.SearchExtractor
import ac.mdiq.vista.extractor.stream.StreamExtractor
import ac.mdiq.vista.extractor.subscription.SubscriptionExtractor
import ac.mdiq.vista.extractor.suggestion.SuggestionExtractor
import ac.mdiq.vista.extractor.utils.Utils
import java.util.*

/*
* Copyright (C) 2018 Christian Schabesberger <chris.schabesberger@mailbox.org>
* Copyright (C) 2024 Xilin Jia <https://github.com/XilinJia>
* StreamingService.kt is part of Vista Guide.
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
abstract class StreamingService(
        @JvmField val serviceId: Int,
        name: String,
        capabilities: List<MediaCapability>?) {

    @JvmField
    val serviceInfo: ServiceInfo = ServiceInfo(name, capabilities)

    abstract val baseUrl: String?

    /**
     * Returns a list of localizations that this service supports.
     */
    open val supportedLocalizations: List<Localization>
        get() = listOf(Localization.DEFAULT)

    /**
     * Returns a list of countries that this service supports.<br></br>
     */
    open val supportedCountries: List<ContentCountry?>
        get() = listOf(ContentCountry.DEFAULT)

    val localization: Localization
        /**
         * Returns the localization that should be used in this service. It will get which localization
         * the user prefer (using [Vista.getPreferredLocalization]), then it will:
         *
         *  * Check if the exactly localization is supported by this service.
         *  * If not, check if a less specific localization is available, using only the language
         * code.
         *  * Fallback to the [default][Localization.DEFAULT] localization.
         *
         */
        get() {
            val preferredLocalization = getPreferredLocalization()

            // Check the localization's language and country
            if (supportedLocalizations.contains(preferredLocalization)) return preferredLocalization

            // Fallback to the first supported language that matches the preferred language
            for (supportedLanguage in supportedLocalizations) {
                if (supportedLanguage.languageCode == preferredLocalization.languageCode) return supportedLanguage
            }

            return Localization.DEFAULT
        }

    val contentCountry: ContentCountry
        /**
         * Returns the country that should be used to fetch content in this service. It will get which
         * country the user prefer (using [Vista.getPreferredContentCountry]), then it will:
         *
         *  * Check if the country is supported by this service.
         *  * If not, fallback to the [default][ContentCountry.DEFAULT] country.
         *
         */
        get() {
            val preferredContentCountry = getPreferredContentCountry()
            if (supportedCountries.contains(preferredContentCountry)) return preferredContentCountry
            return ContentCountry.DEFAULT
        }

    override fun toString(): String {
        return serviceId.toString() + ":" + serviceInfo.name
    }

    /**
     * Must return a new instance of an implementation of LinkHandlerFactory for streams.
     * @return an instance of a LinkHandlerFactory for streams
     */
    abstract fun getStreamLHFactory(): LinkHandlerFactory

    /**
     * Must return a new instance of an implementation of ListLinkHandlerFactory for channels.
     * If support for channels is not given null must be returned.
     * @return an instance of a ListLinkHandlerFactory for channels or null
     */
    abstract fun getChannelLHFactory(): ListLinkHandlerFactory

    /**
     * Must return a new instance of an implementation of ListLinkHandlerFactory for channel tabs.
     * If support for channel tabs is not given null must be returned.
     *
     * @return an instance of a ListLinkHandlerFactory for channels or null
     */
    abstract fun getChannelTabLHFactory(): ListLinkHandlerFactory

    /**
     * Must return a new instance of an implementation of ListLinkHandlerFactory for playlists.
     * If support for playlists is not given null must be returned.
     * @return an instance of a ListLinkHandlerFactory for playlists or null
     */
    abstract fun getPlaylistLHFactory(): ListLinkHandlerFactory?

    /**
     * Must return an instance of an implementation of SearchQueryHandlerFactory.
     * @return an instance of a SearchQueryHandlerFactory
     */
    abstract fun getSearchQHFactory(): SearchQueryHandlerFactory
    abstract fun getCommentsLHFactory(): ListLinkHandlerFactory?

    /**
     * Must create a new instance of a SearchExtractor implementation.
     * @param queryHandler specifies the keyword lock for, and the filters which should be applied.
     * @return a new SearchExtractor instance
     */
    abstract fun getSearchExtractor(queryHandler: SearchQueryHandler): SearchExtractor

    /**
     * Must create a new instance of a SuggestionExtractor implementation.
     * @return a new SuggestionExtractor instance
     */
    abstract fun getSuggestionExtractor(): SuggestionExtractor?

    /**
     * Outdated or obsolete. null can be returned.
     * @return just null
     */
    abstract fun getSubscriptionExtractor(): SubscriptionExtractor?

    /**
     * This method decides which strategy will be chosen to fetch the feed. In YouTube, for example,
     * a separate feed exists which is lightweight and made specifically to be used like this.
     * In services which there's no other way to retrieve them, null should be returned.
     *
     * @return a [FeedExtractor] instance or null.
     */
    @Throws(ExtractionException::class)
    open fun getFeedExtractor(url: String): FeedExtractor? {
        return null
    }

    @Throws(ExtractionException::class)
    abstract fun getKioskList(): KioskList

    /**
     * Must create a new instance of a ChannelExtractor implementation.
     * @param linkHandler is pointing to the channel which should be handled by this new instance.
     * @return a new ChannelExtractor
     */
    @Throws(ExtractionException::class)
    abstract fun getChannelExtractor(linkHandler: ListLinkHandler): ChannelExtractor

    /**
     * Must create a new instance of a ChannelTabExtractor implementation.
     *
     * @param linkHandler is pointing to the channel which should be handled by this new instance.
     * @return a new ChannelTabExtractor
     */
    @Throws(ExtractionException::class)
    abstract fun getChannelTabExtractor(linkHandler: ListLinkHandler): ChannelTabExtractor

    /**
     * Must crete a new instance of a PlaylistExtractor implementation.
     * @param linkHandler is pointing to the playlist which should be handled by this new instance.
     * @return a new PlaylistExtractor
     */
    @Throws(ExtractionException::class)
    abstract fun getPlaylistExtractor(linkHandler: ListLinkHandler): PlaylistExtractor?

    /**
     * Must create a new instance of a StreamExtractor implementation.
     * @param linkHandler is pointing to the stream which should be handled by this new instance.
     * @return a new StreamExtractor
     */
    @Throws(ExtractionException::class)
    abstract fun getStreamExtractor(linkHandler: LinkHandler): StreamExtractor

    @Throws(ExtractionException::class)
    abstract fun getCommentsExtractor(linkHandler: ListLinkHandler): CommentsExtractor?

    @Throws(ExtractionException::class)
    fun getSearchExtractor(query: String, contentFilter: List<String>, sortFilter: String): SearchExtractor {
        return getSearchExtractor(getSearchQHFactory().fromQuery(query, contentFilter, sortFilter))
    }

    @Throws(ExtractionException::class)
    fun getChannelExtractor(id: String, contentFilter: List<String>, sortFilter: String): ChannelExtractor {
        return getChannelExtractor(getChannelLHFactory().fromQuery(id, contentFilter, sortFilter))
    }

    @Throws(ExtractionException::class)
    fun getPlaylistExtractor(id: String, contentFilter: List<String>, sortFilter: String): PlaylistExtractor? {
        val h = getPlaylistLHFactory()?.fromQuery(id, contentFilter, sortFilter) ?: return null
        return getPlaylistExtractor(h)
    }

    @Throws(ExtractionException::class)
    fun getSearchExtractor(query: String): SearchExtractor {
        return getSearchExtractor(getSearchQHFactory().fromQuery(query))
    }

    @Throws(ExtractionException::class)
    fun getChannelExtractor(url: String): ChannelExtractor {
        return getChannelExtractor(getChannelLHFactory().fromUrl(url))
    }

    @Throws(ExtractionException::class)
    fun getChannelTabExtractorFromId(id: String, tab: String): ChannelTabExtractor {
        return getChannelTabExtractor(getChannelTabLHFactory().fromQuery(id, listOf(tab), ""))
    }

    @Throws(ExtractionException::class)
    fun getChannelTabExtractorFromIdAndBaseUrl(id: String, tab: String, baseUrl: String): ChannelTabExtractor {
        return getChannelTabExtractor(getChannelTabLHFactory().fromQuery(id, listOf(tab), "", baseUrl))
    }

    @Throws(ExtractionException::class)
    fun getPlaylistExtractor(url: String): PlaylistExtractor? {
        val h = getPlaylistLHFactory()?.fromUrl(url) ?: return null
        return getPlaylistExtractor(h)
    }

    @Throws(ExtractionException::class)
    fun getStreamExtractor(url: String): StreamExtractor {
        return getStreamExtractor(getStreamLHFactory().fromUrl(url))
    }

    @Throws(ExtractionException::class)
    fun getCommentsExtractor(url: String): CommentsExtractor? {
        val listLinkHandlerFactory = getCommentsLHFactory() ?: return null
        return getCommentsExtractor(listLinkHandlerFactory.fromUrl(url))
    }

    /**
     * Figures out where the link is pointing to (a channel, a video, a playlist, etc.)
     * @param url the url on which it should be decided of which link type it is
     * @return the link type of url
     */
    @Throws(ParsingException::class)
    fun getLinkTypeByUrl(url: String): LinkType {
        val polishedUrl = Utils.followGoogleRedirectIfNeeded(url)

        val sH = getStreamLHFactory()
        val cH: LinkHandlerFactory? = getChannelLHFactory()
        val pH: LinkHandlerFactory? = getPlaylistLHFactory()

        return when {
            sH != null && sH.acceptUrl(polishedUrl) -> LinkType.STREAM
            cH != null && cH.acceptUrl(polishedUrl) -> LinkType.CHANNEL
            pH != null && pH.acceptUrl(polishedUrl) -> LinkType.PLAYLIST
            else -> LinkType.NONE
        }
    }

    /**
     * Get an instance of the time ago parser using the patterns related to the passed localization.
     * <br></br><br></br>
     * Just like [.getLocalization], it will also try to fallback to a less specific
     * localization if the exact one is not available/supported.
     *
     * @throws IllegalArgumentException if the localization is not supported (parsing patterns are
     * not present).
     */
    fun getTimeAgoParser(localization: Localization): TimeAgoParser {
        val targetParser = TimeAgoPatternsManager.getTimeAgoParserFor(localization)

        if (targetParser != null) return targetParser

        if (localization.getCountryCode().isNotEmpty()) {
            val lessSpecificLocalization = Localization(localization.languageCode)
            val lessSpecificParser = TimeAgoPatternsManager.getTimeAgoParserFor(lessSpecificLocalization)

            if (lessSpecificParser != null) return lessSpecificParser
        }

        throw IllegalArgumentException("Localization is not supported (\"$localization\")")
    }

    /**
     * This class holds meta information about the service implementation.
     */
    class ServiceInfo(@JvmField val name: String, mediaCapabilities: List<MediaCapability>?) {
        val mediaCapabilities: List<MediaCapability> = mediaCapabilities ?: listOf()

        enum class MediaCapability {
            AUDIO, VIDEO, LIVE, COMMENTS
        }
    }

    /**
     * LinkType will be used to determine which type of URL you are handling, and therefore which
     * part of Vista should handle a certain URL.
     */
    enum class LinkType {
        NONE,
        STREAM,
        CHANNEL,
        PLAYLIST
    }
}
