package ac.mdiq.vista.extractor.services.soundcloud.linkHandler

import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.linkhandler.ListLinkHandlerFactory
import ac.mdiq.vista.extractor.services.soundcloud.SoundcloudParsingHelper
import ac.mdiq.vista.extractor.utils.Parser.isMatch
import ac.mdiq.vista.extractor.utils.Utils.checkUrl
import java.util.*

class SoundcloudChannelLinkHandlerFactory private constructor() : ListLinkHandlerFactory() {
    @Throws(ParsingException::class, UnsupportedOperationException::class)
    override fun getId(url: String): String {
        checkUrl(URL_PATTERN, url)

        try {
            return SoundcloudParsingHelper.resolveIdWithWidgetApi(url)
        } catch (e: Exception) {
            throw ParsingException(e.message, e)
        }
    }

    @Throws(ParsingException::class, UnsupportedOperationException::class)
    override fun getUrl(id: String, contentFilters: List<String>, sortFilter: String?): String {
        try {
            return SoundcloudParsingHelper.resolveUrlWithEmbedPlayer("https://api.soundcloud.com/users/$id")
        } catch (e: Exception) {
            throw ParsingException(e.message, e)
        }
    }

    override fun onAcceptUrl(url: String): Boolean {
        return isMatch(URL_PATTERN, url.lowercase(Locale.getDefault()))
    }

    companion object {
        val instance: SoundcloudChannelLinkHandlerFactory = SoundcloudChannelLinkHandlerFactory()
        private const val URL_PATTERN = ("^https?://(www\\.|m\\.)?soundcloud.com/[0-9a-z_-]+"
                + "(/((tracks|albums|sets|reposts|followers|following)/?)?)?([#?].*)?$")
    }
}
