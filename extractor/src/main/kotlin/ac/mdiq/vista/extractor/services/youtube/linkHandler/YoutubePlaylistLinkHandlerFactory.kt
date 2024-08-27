package ac.mdiq.vista.extractor.services.youtube.linkHandler

import ac.mdiq.vista.extractor.exceptions.ContentNotSupportedException
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.linkhandler.LinkHandler
import ac.mdiq.vista.extractor.linkhandler.ListLinkHandler
import ac.mdiq.vista.extractor.linkhandler.ListLinkHandlerFactory
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.extractVideoIdFromMixId
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.isInvidiousURL
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.isYoutubeChannelMixId
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.isYoutubeMixId
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.isYoutubeURL
import ac.mdiq.vista.extractor.utils.Utils.getQueryValue
import ac.mdiq.vista.extractor.utils.Utils.isHTTP
import ac.mdiq.vista.extractor.utils.Utils.stringToURL
import java.net.MalformedURLException

class YoutubePlaylistLinkHandlerFactory private constructor() : ListLinkHandlerFactory() {
    @Throws(ParsingException::class, UnsupportedOperationException::class)
    override fun getUrl(id: String, contentFilters: List<String>, sortFilter: String?): String {
        return "https://www.youtube.com/playlist?list=$id"
    }

    @Throws(ParsingException::class, UnsupportedOperationException::class)
    override fun getId(url: String): String {
        try {
            val urlObj = stringToURL(url)

            if (!isHTTP(urlObj) || !(isYoutubeURL(urlObj) || isInvidiousURL(urlObj))) throw ParsingException("the url given is not a YouTube-URL")

            val path = urlObj.path
            if (path != "/watch" && path != "/playlist") throw ParsingException("the url given is neither a video nor a playlist URL")

            val listID = getQueryValue(urlObj, "list") ?: throw ParsingException("the URL given does not include a playlist")

            if (!listID.matches("[a-zA-Z0-9_-]{10,}".toRegex())) throw ParsingException("the list-ID given in the URL does not match the list pattern")

            // Video id can't be determined from the channel mix id.
            // See YoutubeParsingHelper#extractVideoIdFromMixId
            if (isYoutubeChannelMixId(listID) && getQueryValue(urlObj, "v") == null) throw ContentNotSupportedException("Channel Mix without a video id are not supported")

            return listID
        } catch (exception: Exception) {
            throw ParsingException("Error could not parse URL: " + exception.message, exception)
        }
    }

    override fun onAcceptUrl(url: String): Boolean {
        try {
            getId(url)
        } catch (e: ParsingException) {
            return false
        }
        return true
    }

    /**
     * If it is a mix (auto-generated playlist) URL, return a [LinkHandler] where the URL is
     * like `https://youtube.com/watch?v=videoId&list=playlistId`
     *
     * Otherwise use super
     */
    @Throws(ParsingException::class)
    override fun fromUrl(url: String): ListLinkHandler {
        try {
            val urlObj = stringToURL(url)
            val listID = getQueryValue(urlObj, "list")
            if (listID != null && isYoutubeMixId(listID)) {
                var videoID = getQueryValue(urlObj, "v")
                if (videoID == null) videoID = extractVideoIdFromMixId(listID)
                val newUrl = ("https://www.youtube.com/watch?v=$videoID&list=$listID")
                return ListLinkHandler(LinkHandler(url, newUrl, listID))
            }
        } catch (exception: MalformedURLException) {
            throw ParsingException("Error could not parse URL: " + exception.message, exception)
        }
        return super.fromUrl(url)
    }

    companion object {
        val instance: YoutubePlaylistLinkHandlerFactory = YoutubePlaylistLinkHandlerFactory()
    }
}
