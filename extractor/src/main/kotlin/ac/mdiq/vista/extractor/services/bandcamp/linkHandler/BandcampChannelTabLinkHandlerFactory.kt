package ac.mdiq.vista.extractor.services.bandcamp.linkHandler

import ac.mdiq.vista.extractor.channel.tabs.ChannelTabs
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.exceptions.UnsupportedTabException
import ac.mdiq.vista.extractor.linkhandler.ListLinkHandlerFactory


class BandcampChannelTabLinkHandlerFactory private constructor() : ListLinkHandlerFactory() {

    @Throws(ParsingException::class, UnsupportedOperationException::class)
    override fun getId(url: String): String {
        return BandcampChannelLinkHandlerFactory.instance.getId(url)
    }

    @Throws(ParsingException::class, UnsupportedOperationException::class)
    override fun getUrl(id: String, contentFilters: List<String>, sortFilter: String?): String {
        return (BandcampChannelLinkHandlerFactory.instance.getUrl(id) + getUrlSuffix(contentFilters!![0]?:""))
    }

    @Throws(ParsingException::class)
    override fun onAcceptUrl(url: String): Boolean {
        return BandcampChannelLinkHandlerFactory.instance.onAcceptUrl(url)
    }

    override val availableContentFilter: Array<String>
        get() = arrayOf(
            ChannelTabs.TRACKS,
            ChannelTabs.ALBUMS,
        )

    companion object {
        val instance
                : BandcampChannelTabLinkHandlerFactory = BandcampChannelTabLinkHandlerFactory()

        /**
         * Get a tab's URL suffix.
         *
         *
         *
         * These URLs don't actually exist on the Bandcamp website, as both albums and tracks are
         * listed on the main page, but redirect to the main page, which is perfect for us as we need a
         * unique URL for each tab.
         *
         *
         * @param tab the tab value, which must not be null
         * @return a URL suffix
         * @throws UnsupportedTabException if the tab is not supported
         */


        @Throws(UnsupportedTabException::class)
        fun getUrlSuffix(tab: String): String {
            when (tab) {
                ChannelTabs.TRACKS -> return "/track"
                ChannelTabs.ALBUMS -> return "/album"
            }
            throw UnsupportedTabException(tab)
        }
    }
}
