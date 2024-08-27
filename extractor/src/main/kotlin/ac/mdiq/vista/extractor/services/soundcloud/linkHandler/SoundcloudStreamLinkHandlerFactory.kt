package ac.mdiq.vista.extractor.services.soundcloud.linkHandler

import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.linkhandler.LinkHandlerFactory
import ac.mdiq.vista.extractor.services.soundcloud.SoundcloudParsingHelper
import ac.mdiq.vista.extractor.utils.Parser.isMatch
import ac.mdiq.vista.extractor.utils.Parser.matchGroup1
import ac.mdiq.vista.extractor.utils.Utils.checkUrl
import java.util.*

class SoundcloudStreamLinkHandlerFactory private constructor() : LinkHandlerFactory() {
    @Throws(ParsingException::class, UnsupportedOperationException::class)
    override fun getUrl(id: String): String {
        try {
            return SoundcloudParsingHelper.resolveUrlWithEmbedPlayer("https://api.soundcloud.com/tracks/$id")
        } catch (e: Exception) {
            throw ParsingException(e.message, e)
        }
    }

    @Throws(ParsingException::class, UnsupportedOperationException::class)
    override fun getId(url: String): String {
        if (isMatch(API_URL_PATTERN, url)) return matchGroup1(API_URL_PATTERN, url)
        checkUrl(URL_PATTERN, url)

        try {
            return SoundcloudParsingHelper.resolveIdWithWidgetApi(url)
        } catch (e: Exception) {
            throw ParsingException(e.message, e)
        }
    }

    @Throws(ParsingException::class)
    override fun onAcceptUrl(url: String): Boolean {
        return isMatch(URL_PATTERN, url.lowercase(Locale.getDefault()))
    }

    companion object {
        val instance: SoundcloudStreamLinkHandlerFactory = SoundcloudStreamLinkHandlerFactory()
        private const val URL_PATTERN = ("^https?://(www\\.|m\\.|on\\.)?" + "soundcloud.com/[0-9a-z_-]+"
                + "/(?!(tracks|albums|sets|reposts|followers|following)/?$)[0-9a-z_-]+/?([#?].*)?$");
        private const val API_URL_PATTERN = ("^https?://api-v2\\.soundcloud.com"
                + "/(tracks|albums|sets|reposts|followers|following)/([0-9a-z_-]+)/")
    }
}
