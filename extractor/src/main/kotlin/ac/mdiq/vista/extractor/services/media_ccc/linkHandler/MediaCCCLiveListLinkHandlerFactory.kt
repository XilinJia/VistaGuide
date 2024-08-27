package ac.mdiq.vista.extractor.services.media_ccc.linkHandler

import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.linkhandler.ListLinkHandlerFactory
import java.util.regex.Pattern

class MediaCCCLiveListLinkHandlerFactory private constructor() : ListLinkHandlerFactory() {
    @Throws(ParsingException::class, UnsupportedOperationException::class)
    override fun getId(url: String): String {
        return "live"
    }

    @Throws(ParsingException::class)
    override fun onAcceptUrl(url: String): Boolean {
        return Pattern.matches(STREAM_PATTERN, url)
    }

    @Throws(ParsingException::class, UnsupportedOperationException::class)
    override fun getUrl(id: String, contentFilters: List<String>, sortFilter: String?): String {
        // FIXME: wrong URL; should be https://streaming.media.ccc.de/{conference_slug}/{room_slug}
        return "https://media.ccc.de/live"
    }

    companion object {
        val instance: MediaCCCLiveListLinkHandlerFactory = MediaCCCLiveListLinkHandlerFactory()

        private const val STREAM_PATTERN = "^(?:https?://)?media\\.ccc\\.de/live$"
    }
}
