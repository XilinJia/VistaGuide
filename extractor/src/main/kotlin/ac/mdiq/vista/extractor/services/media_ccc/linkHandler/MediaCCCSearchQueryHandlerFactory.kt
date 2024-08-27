package ac.mdiq.vista.extractor.services.media_ccc.linkHandler

import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.linkhandler.SearchQueryHandlerFactory
import ac.mdiq.vista.extractor.utils.Utils.encodeUrlUtf8

class MediaCCCSearchQueryHandlerFactory private constructor() : SearchQueryHandlerFactory() {
    override val availableContentFilter: Array<String>
        get() = arrayOf(ALL, CONFERENCES, EVENTS)

    override val availableSortFilter: Array<String?>
        get() = arrayOfNulls(0)

    @Throws(ParsingException::class, UnsupportedOperationException::class)
    override fun getUrl(id: String, contentFilters: List<String>, sortFilter: String?): String {
        return "https://media.ccc.de/public/events/search?q=" + encodeUrlUtf8(id)
    }

    companion object {
        val instance: MediaCCCSearchQueryHandlerFactory = MediaCCCSearchQueryHandlerFactory()

        const val ALL: String = "all"
        const val CONFERENCES: String = "conferences"
        const val EVENTS: String = "events"
    }
}
