package ac.mdiq.vista.extractor.services.soundcloud.linkHandler

import ac.mdiq.vista.extractor.channel.tabs.ChannelTabs
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.exceptions.UnsupportedTabException
import ac.mdiq.vista.extractor.linkhandler.ListLinkHandlerFactory


class SoundcloudChannelTabLinkHandlerFactory private constructor() : ListLinkHandlerFactory() {
    @Throws(ParsingException::class)
    override fun getId(url: String): String {
        return SoundcloudChannelLinkHandlerFactory.instance.getId(url)
    }

    @Throws(ParsingException::class)
    override fun getUrl(id: String, contentFilters: List<String>, sortFilter: String?): String {
        return (SoundcloudChannelLinkHandlerFactory.instance.getUrl(id)
                + (if (contentFilters.isNotEmpty()) getUrlSuffix(contentFilters[0]) else ""))
    }

    @Throws(ParsingException::class)
    override fun onAcceptUrl(url: String): Boolean {
        return SoundcloudChannelLinkHandlerFactory.instance.onAcceptUrl(url)
    }

    override val availableContentFilter: Array<String>
        get() = arrayOf(
            ChannelTabs.TRACKS,
            ChannelTabs.PLAYLISTS,
            ChannelTabs.ALBUMS,
        )

    companion object {
        val instance: SoundcloudChannelTabLinkHandlerFactory = SoundcloudChannelTabLinkHandlerFactory()



        @Throws(UnsupportedOperationException::class)
        fun getUrlSuffix(tab: String?): String {
            when (tab) {
                ChannelTabs.TRACKS -> return "/tracks"
                ChannelTabs.PLAYLISTS -> return "/sets"
                ChannelTabs.ALBUMS -> return "/albums"
            }
            throw UnsupportedTabException(tab!!)
        }
    }
}
