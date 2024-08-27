package ac.mdiq.vista.extractor.services.youtube.linkHandler

import ac.mdiq.vista.extractor.channel.tabs.ChannelTabs
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.exceptions.UnsupportedTabException
import ac.mdiq.vista.extractor.linkhandler.ListLinkHandlerFactory

class YoutubeChannelTabLinkHandlerFactory private constructor() : ListLinkHandlerFactory() {

    override val availableContentFilter: Array<String>
        get() = arrayOf(
            ChannelTabs.VIDEOS,
            ChannelTabs.SHORTS,
            ChannelTabs.LIVESTREAMS,
            ChannelTabs.ALBUMS,
            ChannelTabs.PLAYLISTS
        )

    @Throws(ParsingException::class, UnsupportedOperationException::class)
    override fun getUrl(id: String, contentFilters: List<String>, sortFilter: String?): String {
        return "https://www.youtube.com/$id" + getUrlSuffix(contentFilters[0])
    }

    @Throws(ParsingException::class, UnsupportedOperationException::class)
    override fun getId(url: String): String {
        return YoutubeChannelLinkHandlerFactory.instance.getId(url)
    }

    @Throws(ParsingException::class)
    override fun onAcceptUrl(url: String): Boolean {
        try {
            getId(url)
        } catch (e: ParsingException) {
            return false
        }
        return true
    }

    companion object {
        val instance: YoutubeChannelTabLinkHandlerFactory = YoutubeChannelTabLinkHandlerFactory()


        @Throws(UnsupportedTabException::class)
        fun getUrlSuffix(tab: String): String {
            return when (tab) {
                ChannelTabs.VIDEOS -> "/videos"
                ChannelTabs.SHORTS -> "/shorts"
                ChannelTabs.LIVESTREAMS -> "/streams"
                ChannelTabs.ALBUMS -> "/releases"
                ChannelTabs.PLAYLISTS -> "/playlists"
                else -> throw UnsupportedTabException(tab)
            }
        }
    }
}
