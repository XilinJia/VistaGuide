package ac.mdiq.vista.extractor.services.media_ccc.linkHandler

import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.linkhandler.ListLinkHandlerFactory

class MediaCCCConferencesListLinkHandlerFactory private constructor() : ListLinkHandlerFactory() {
    @Throws(ParsingException::class, UnsupportedOperationException::class)
    override fun getId(url: String): String {
        return "conferences"
    }

    @Throws(ParsingException::class, UnsupportedOperationException::class)
    override fun getUrl(id: String, contentFilters: List<String>, sortFilter: String?): String {
        return "https://media.ccc.de/public/conferences"
    }

    override fun onAcceptUrl(url: String): Boolean {
        return url == "https://media.ccc.de/b/conferences" || url == "https://media.ccc.de/public/conferences" || url == "https://api.media.ccc.de/public/conferences"
    }

    companion object {
        val instance: MediaCCCConferencesListLinkHandlerFactory = MediaCCCConferencesListLinkHandlerFactory()
    }
}
