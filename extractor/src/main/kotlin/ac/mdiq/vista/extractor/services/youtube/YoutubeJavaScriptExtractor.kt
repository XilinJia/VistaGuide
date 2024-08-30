package ac.mdiq.vista.extractor.services.youtube

import org.jsoup.Jsoup
import ac.mdiq.vista.extractor.Vista.downloader
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.localization.Localization
import ac.mdiq.vista.extractor.utils.Parser.RegexException
import ac.mdiq.vista.extractor.utils.Parser.matchGroup1
import java.net.MalformedURLException
import java.net.URL
import java.util.regex.Pattern


/**
 * The extractor of YouTube's base JavaScript player file.
 *
 * This class handles fetching of this base JavaScript player file in order to allow other classes
 * to extract the needed data.
 *
 * It will try to get the player URL from YouTube's IFrame resource first, and from a YouTube embed
 * watch page as a fallback.
 *
 */
internal object YoutubeJavaScriptExtractor {
    private const val HTTPS = "https:"
    private const val BASE_JS_PLAYER_URL_FORMAT = "https://www.youtube.com/s/player/%s/player_ias.vflset/en_GB/base.js"
    private val IFRAME_RES_JS_BASE_PLAYER_HASH_PATTERN: Pattern = Pattern.compile("player\\\\/([a-z0-9]{8})\\\\/")
    private val EMBEDDED_WATCH_PAGE_JS_BASE_PLAYER_URL_PATTERN: Pattern = Pattern.compile(
        "\"jsUrl\":\"(/s/player/[A-Za-z0-9]+/player_ias\\.vflset/[A-Za-z_-]+/base\\.js)\"")

    /**
     * Extracts the JavaScript base player file.
     *
     * @param videoId the video ID used to get the JavaScript base player file (an empty one can be
     * passed, even it is not recommend in order to spoof better official YouTube
     * clients)
     * @return the whole JavaScript base player file as a string
     * @throws ParsingException if the extraction of the file failed
     */
    @Throws(ParsingException::class)
    fun extractJavaScriptPlayerCode(videoId: String): String {
        var url: String
        try {
            url = extractJavaScriptUrlWithIframeResource()
            val playerJsUrl = cleanJavaScriptUrl(url)
            // Assert that the URL we extracted and built is valid
            URL(playerJsUrl)
            return downloadJavaScriptCode(playerJsUrl)
        } catch (e: Exception) {
            url = extractJavaScriptUrlWithEmbedWatchPage(videoId)
            val playerJsUrl = cleanJavaScriptUrl(url)

            // Assert that the URL we extracted and built is valid
            try { URL(playerJsUrl) } catch (exception: MalformedURLException) { throw ParsingException("The extracted and built JavaScript URL is invalid", exception) }
            return downloadJavaScriptCode(playerJsUrl)
        }
    }

    @Throws(ParsingException::class)
    fun extractJavaScriptUrlWithIframeResource(): String {
        val iframeUrl: String
        val iframeContent: String
        try {
            iframeUrl = "https://www.youtube.com/iframe_api"
            iframeContent = downloader.get(iframeUrl, Localization.DEFAULT).responseBody()
        } catch (e: Exception) { throw ParsingException("Could not fetch IFrame resource", e) }

        try {
            val hash = matchGroup1(IFRAME_RES_JS_BASE_PLAYER_HASH_PATTERN, iframeContent)
            return String.format(BASE_JS_PLAYER_URL_FORMAT, hash)
        } catch (e: RegexException) { throw ParsingException("IFrame resource didn't provide JavaScript base player's hash", e) }
    }

    @Throws(ParsingException::class)
    fun extractJavaScriptUrlWithEmbedWatchPage(videoId: String): String {
        val embedUrl: String
        val embedPageContent: String
        try {
            embedUrl = "https://www.youtube.com/embed/$videoId"
            embedPageContent = downloader.get(embedUrl, Localization.DEFAULT).responseBody()
        } catch (e: Exception) { throw ParsingException("Could not fetch embedded watch page", e) }

        // Parse HTML response with jsoup and look at script elements first
        val doc = Jsoup.parse(embedPageContent)
        val elems = doc.select("script").attr("name", "player/base")
        for (elem in elems) {
            // Script URLs should be relative and not absolute
            val playerUrl = elem.attr("src")
            if (playerUrl.contains("base.js")) return playerUrl
        }

        // Use regexes to match the URL in an embedded script of the HTML page
        try { return matchGroup1(EMBEDDED_WATCH_PAGE_JS_BASE_PLAYER_URL_PATTERN, embedPageContent) } catch (e: RegexException) { throw ParsingException("Embedded watch page didn't provide JavaScript base player's URL", e) }
    }

    private fun cleanJavaScriptUrl(javaScriptPlayerUrl: String): String {
        return when {
            // https part has to be added manually if the URL is protocol-relative
            javaScriptPlayerUrl.startsWith("//") -> HTTPS + javaScriptPlayerUrl
            // https://www.youtube.com part has to be added manually if the URL is relative to
            // YouTube's domain
            javaScriptPlayerUrl.startsWith("/") -> "$HTTPS//www.youtube.com$javaScriptPlayerUrl"
            else -> javaScriptPlayerUrl
        }
    }

    @Throws(ParsingException::class)
    private fun downloadJavaScriptCode(javaScriptPlayerUrl: String): String {
        try { return downloader.get(javaScriptPlayerUrl, Localization.DEFAULT).responseBody() } catch (e: Exception) { throw ParsingException("Could not get JavaScript base player's code", e) }
    }
}
