package ac.mdiq.vista.extractor.services.soundcloud.linkHandler

import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.linkhandler.ListLinkHandlerFactory
import ac.mdiq.vista.extractor.utils.Parser.isMatch
import java.util.*

class SoundcloudChartsLinkHandlerFactory private constructor() : ListLinkHandlerFactory() {
    @Throws(ParsingException::class, UnsupportedOperationException::class)
    override fun getId(url: String): String {
        return if (isMatch(TOP_URL_PATTERN, url.lowercase(Locale.getDefault()))) "Top 50"  else "New & hot"
    }

    @Throws(ParsingException::class, UnsupportedOperationException::class)
    override fun getUrl(id: String, contentFilters: List<String>, sortFilter: String?): String {
        return if (id == "Top 50") "https://soundcloud.com/charts/top" else  "https://soundcloud.com/charts/new"
    }

    override fun onAcceptUrl(url: String): Boolean {
        return isMatch(URL_PATTERN, url.lowercase(Locale.getDefault()))
    }

    companion object {
        val instance: SoundcloudChartsLinkHandlerFactory = SoundcloudChartsLinkHandlerFactory()

        private const val TOP_URL_PATTERN = "^https?://(www\\.|m\\.)?soundcloud.com/charts(/top)?/?([#?].*)?$"
        private const val URL_PATTERN = "^https?://(www\\.|m\\.)?soundcloud.com/charts(/top|/new)?/?([#?].*)?$"
    }
}
