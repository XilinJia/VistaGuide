package ac.mdiq.vista.extractor.services.peertube.linkHandler

import ac.mdiq.vista.extractor.channel.tabs.ChannelTabs
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.exceptions.UnsupportedTabException
import ac.mdiq.vista.extractor.linkhandler.ListLinkHandlerFactory


class PeertubeChannelTabLinkHandlerFactory private constructor() : ListLinkHandlerFactory() {
    @Throws(ParsingException::class, UnsupportedOperationException::class)
    override fun getId(url: String): String {
        return PeertubeChannelLinkHandlerFactory.instance.getId(url)
    }

    @Throws(ParsingException::class, UnsupportedOperationException::class)
    override fun getUrl(id: String, contentFilters: List<String>, sortFilter: String?): String {
        return (PeertubeChannelLinkHandlerFactory.instance.getUrl(id)
                + (if (contentFilters.isNotEmpty()) getUrlSuffix(contentFilters[0]) else ""))
    }

    @Throws(ParsingException::class, UnsupportedOperationException::class)
    override fun getUrl(id: String, contentFilter: List<String>, sortFilter: String?, baseUrl: String?): String {
        return (PeertubeChannelLinkHandlerFactory.instance.getUrl(id, listOf(), null, baseUrl)
                + (if (contentFilter.isNotEmpty()) getUrlSuffix(contentFilter[0]) else ""))
    }

    @Throws(ParsingException::class)
    override fun onAcceptUrl(url: String): Boolean {
        return PeertubeChannelLinkHandlerFactory.instance.onAcceptUrl(url)
    }

    override val availableContentFilter: Array<String>
        get() = arrayOf(
            ChannelTabs.VIDEOS,
            ChannelTabs.CHANNELS,
            ChannelTabs.PLAYLISTS,
        )

    companion object {
        val instance: PeertubeChannelTabLinkHandlerFactory = PeertubeChannelTabLinkHandlerFactory()



        @Throws(UnsupportedTabException::class)
        fun getUrlSuffix(tab: String): String {
            when (tab) {
                ChannelTabs.VIDEOS -> return "/videos"
                ChannelTabs.CHANNELS -> return "/video-channels"
                ChannelTabs.PLAYLISTS -> return "/video-playlists"
            }
            throw UnsupportedTabException(tab)
        }
    }
}
