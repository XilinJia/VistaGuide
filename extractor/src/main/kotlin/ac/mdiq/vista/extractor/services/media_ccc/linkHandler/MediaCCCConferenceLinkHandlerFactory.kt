package ac.mdiq.vista.extractor.services.media_ccc.linkHandler

import ac.mdiq.vista.extractor.channel.tabs.ChannelTabs
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.linkhandler.ListLinkHandlerFactory
import ac.mdiq.vista.extractor.utils.Parser.matchGroup1

/**
 * Since MediaCCC does not really have channel tabs (i.e. it only has one single "tab" with videos),
 * this link handler acts both as the channel link handler and the channel tab link handler. That's
 * why [.getAvailableContentFilter] has been overridden.
 */
class MediaCCCConferenceLinkHandlerFactory private constructor() : ListLinkHandlerFactory() {
    @Throws(ParsingException::class, UnsupportedOperationException::class)
    override fun getUrl(id: String, contentFilters: List<String>, sortFilter: String?): String {
        return CONFERENCE_PATH + id
    }

    @Throws(ParsingException::class, UnsupportedOperationException::class)
    override fun getId(url: String): String {
        return matchGroup1(ID_PATTERN, url)
    }

    override fun onAcceptUrl(url: String): Boolean {
        return try {
            getId(url) != null
        } catch (e: ParsingException) {
            false
        }
    }

    override val availableContentFilter: Array<String>
        /**
         * @see MediaCCCConferenceLinkHandlerFactory
         *
         * @return MediaCCC's only channel "tab", i.e. [ChannelTabs.VIDEOS]
         */
        get() = arrayOf(
            ChannelTabs.VIDEOS,
        )

    companion object {
        val instance: MediaCCCConferenceLinkHandlerFactory = MediaCCCConferenceLinkHandlerFactory()

        const val CONFERENCE_API_ENDPOINT: String = "https://api.media.ccc.de/public/conferences/"
        const val CONFERENCE_PATH: String = "https://media.ccc.de/c/"
        private const val ID_PATTERN = ("(?:(?:(?:api\\.)?media\\.ccc\\.de/public/conferences/)|(?:media\\.ccc\\.de/[bc]/))([^/?&#]*)")
    }
}
