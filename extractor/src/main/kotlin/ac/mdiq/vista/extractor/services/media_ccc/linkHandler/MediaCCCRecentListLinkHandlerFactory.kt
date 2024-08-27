package ac.mdiq.vista.extractor.services.media_ccc.linkHandler

import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.linkhandler.ListLinkHandlerFactory
import java.util.regex.Pattern

class MediaCCCRecentListLinkHandlerFactory private constructor() : ListLinkHandlerFactory() {
    @Throws(ParsingException::class, UnsupportedOperationException::class)
    override fun getId(url: String): String {
        return "recent"
    }

    override fun onAcceptUrl(url: String): Boolean {
        return Pattern.matches(PATTERN, url)
    }

    @Throws(ParsingException::class, UnsupportedOperationException::class)
    override fun getUrl(id: String, contentFilters: List<String>, sortFilter: String?): String {
        return "https://media.ccc.de/recent"
    }

    companion object {
        val instance: MediaCCCRecentListLinkHandlerFactory = MediaCCCRecentListLinkHandlerFactory()

        private const val PATTERN = "^(https?://)?media\\.ccc\\.de/recent/?$"
    }
}
