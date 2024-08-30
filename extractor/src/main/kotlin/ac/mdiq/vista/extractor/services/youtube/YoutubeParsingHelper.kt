/*
 * Created by Christian Schabesberger on 02.03.16.
 *
 * Copyright (C) 2016 Christian Schabesberger <chris.schabesberger@mailbox.org>
* Copyright (C) 2024 Xilin Jia <https://github.com/XilinJia>
 * YoutubeParsingHelper.kt is part of Vista Guide.
 *
 * Vista Guide is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Vista Guide is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Vista Guide. If not, see <https://www.gnu.org/licenses/>.
 */
package ac.mdiq.vista.extractor.services.youtube

import com.grack.nanojson.*
import org.jsoup.nodes.Entities
import ac.mdiq.vista.extractor.Image
import ac.mdiq.vista.extractor.Image.ResolutionLevel.Companion.fromHeight
import ac.mdiq.vista.extractor.Vista.downloader
import ac.mdiq.vista.extractor.downloader.Response
import ac.mdiq.vista.extractor.exceptions.*
import ac.mdiq.vista.extractor.exceptions.ExtractionException
import ac.mdiq.vista.extractor.localization.ContentCountry
import ac.mdiq.vista.extractor.localization.Localization
import ac.mdiq.vista.extractor.playlist.PlaylistInfo
import ac.mdiq.vista.extractor.playlist.PlaylistInfo.PlaylistType
import ac.mdiq.vista.extractor.stream.AudioTrackType
import ac.mdiq.vista.extractor.utils.JsonUtils.toJsonObject
import ac.mdiq.vista.extractor.utils.Parser.RegexException
import ac.mdiq.vista.extractor.utils.Parser.isMatch
import ac.mdiq.vista.extractor.utils.RandomStringFromAlphabetGenerator.generate
import ac.mdiq.vista.extractor.utils.Utils.HTTP
import ac.mdiq.vista.extractor.utils.Utils.HTTPS
import ac.mdiq.vista.extractor.utils.Utils.decodeUrlUtf8
import ac.mdiq.vista.extractor.utils.Utils.getQueryValue
import ac.mdiq.vista.extractor.utils.Utils.getStringResultFromRegexArray
import ac.mdiq.vista.extractor.utils.Utils.removeNonDigitCharacters
import ac.mdiq.vista.extractor.utils.Utils.replaceHttpWithHttps
import ac.mdiq.vista.extractor.utils.Utils.stringToURL
import java.io.IOException
import java.net.MalformedURLException
import java.net.URL
import java.nio.charset.StandardCharsets
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeParseException
import java.util.*
import java.util.regex.Pattern
import java.util.stream.Collectors
import java.util.stream.Stream

object YoutubeParsingHelper {
    /**
     * The base URL of requests of the `WEB` clients to the InnerTube internal API.
     */
    const val YOUTUBEI_V1_URL: String = "https://www.youtube.com/youtubei/v1/"

    /**
     * The base URL of requests of non-web clients to the InnerTube internal API.
     */
    const val YOUTUBEI_V1_GAPIS_URL: String = "https://youtubei.googleapis.com/youtubei/v1/"

    /**
     * The base URL of YouTube Music.
     */
    private const val YOUTUBE_MUSIC_URL = "https://music.youtube.com"

    /**
     * A parameter to disable pretty-printed response of InnerTube requests, to reduce response sizes.
     *
     * Sent in query parameters of the requests, **after** the API key.
     */
    const val DISABLE_PRETTY_PRINT_PARAMETER: String = "prettyPrint=false"

    /**
     * A parameter sent by official clients named `contentPlaybackNonce`.
     *
     * It is sent by official clients on videoplayback requests, and by all clients (except the
     * `WEB` one to the player requests.
     *
     * It is composed of 16 characters which are generated from
     * [this alphabet][.CONTENT_PLAYBACK_NONCE_ALPHABET], with the use of strong random values.
     *
     * @see .generateContentPlaybackNonce
     */
    const val CPN: String = "cpn"
    const val VIDEO_ID: String = "videoId"

    /**
     * A parameter sent by official clients named `contentCheckOk`.
     *
     * Setting it to `true` allows us to get streaming data on videos with a warning about
     * what the sensible content they contain.
     */
    const val CONTENT_CHECK_OK: String = "contentCheckOk"

    /**
     * A parameter which may be sent by official clients named `racyCheckOk`.
     *
     * What this parameter does is not really known, but it seems to be linked to sensitive
     * contents such as age-restricted content.
     */
    const val RACY_CHECK_OK: String = "racyCheckOk"

    /**
     * The hardcoded client ID used for InnerTube requests with the `WEB` client.
     */
    private const val WEB_CLIENT_ID: String = "1"

    /**
     * The client version for InnerTube requests with the `WEB` client, used as the last
     * fallback if the extraction of the real one failed.
     */
    private const val HARDCODED_CLIENT_VERSION: String = "2.20240718.01.00"

    /**
     * The InnerTube API key which should be used by YouTube's desktop website, used as a fallback
     * if the extraction of the real one failed.
     */
//    private const val HARDCODED_KEY = "AIzaSyAO_FJ2SlqU8Q4STEHLGCilw_Y9_11qcW8"

    /**
     * The hardcoded client version of the Android app used for InnerTube requests with this client.
     *
     * It can be extracted by getting the latest release version of the app in an APK repository
     * such as [APKMirror](https://www.apkmirror.com/apk/google-inc/youtube/).
     *
     */
    private const val ANDROID_YOUTUBE_CLIENT_VERSION = "19.28.35"

    /**
     * The InnerTube API key used by the `ANDROID` client. Found with the help of
     * reverse-engineering app network requests.
     */
//    private const val ANDROID_YOUTUBE_KEY = "AIzaSyA8eiZmM1FaDVjRy-df2KTyQ_vz_yYM39w"

    /**
     * The hardcoded client version of the iOS app used for InnerTube requests with this client.
     *
     * It can be extracted by getting the latest release version of the app on
     * [the App Store page of the YouTube app](https://apps.apple.com/us/app/youtube-watch-listen-stream/id544007664/), in the `What’s New` section.
     */
    private const val IOS_YOUTUBE_CLIENT_VERSION = "19.28.1"

    /**
     * The InnerTube API key used by the `iOS` client. Found with the help of
     * reverse-engineering app network requests.
     */
//    private const val IOS_YOUTUBE_KEY = "AIzaSyB-63vPrdThhKuerbB2N_l7Kwwcxj6yUAc"

    /**
     * The hardcoded client version used for InnerTube requests with the TV HTML5 embed client.
     */
    private const val TVHTML5_SIMPLY_EMBED_CLIENT_VERSION = "2.0"

    /**
     * The hardcoded client ID used for InnerTube requests with the YouTube Music desktop client.
     */
    private const val YOUTUBE_MUSIC_CLIENT_ID: String = "67"

    /**
     * The hardcoded client version used for InnerTube requests with the YouTube Music desktop client.
     */
    private const val HARDCODED_YOUTUBE_MUSIC_CLIENT_VERSION: String = "1.20240715.01.00"

    private var clientVersion: String? = null
    private var key: String? = null

    //    private val HARDCODED_YOUTUBE_MUSIC_KEY = arrayOf("AIzaSyC9XL3ZjWddXya6X74dJoCTL-WEYFDNX30", "67", "1.20231204.01.00")
//    private var youtubeMusicKey: Array<String>? = null
    private var youtubeMusicClientVersion: String? = null

    private var clientVersionExtracted: Boolean = false
    private var hardcodedClientVersionValid: Boolean? = null

    private val INNERTUBE_CONTEXT_CLIENT_VERSION_REGEXES =
        arrayOf<String?>("INNERTUBE_CONTEXT_CLIENT_VERSION\":\"([0-9\\.]+?)\"",
            "innertube_context_client_version\":\"([0-9\\.]+?)\"",
            "client.version=([0-9\\.]+)")
    private val INITIAL_DATA_REGEXES = arrayOf<String?>("window\\[\"ytInitialData\"\\]\\s*=\\s*(\\{.*?\\});",
        "var\\s*ytInitialData\\s*=\\s*(\\{.*?\\});")

    private const val CONTENT_PLAYBACK_NONCE_ALPHABET =
        "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_"

    /**
     * The device machine id for the iPhone 15, used to get 60fps with the `iOS` client.
     * See [this GitHub Gist](https://gist.github.com/adamawolf/3048717) for more
     * information.
     *
     */
    private const val IOS_DEVICE_MODEL: String = "iPhone16,2"

    /**
     * Spoofing an iPhone 15 Pro Max running iOS 17.5.1 with the hardcoded version of the iOS app.
     * To be used for the `"osVersion"` field in JSON POST requests.
     *
     *
     * The value of this field seems to use the following structure:
     * "iOS major version.minor version.patch version.build version", where
     * "patch version" is equal to 0 if it isn't set
     * The build version corresponding to the iOS version used can be found on
     * [
 * https://theapplewiki.com/wiki/Firmware/iPhone/17.x#iPhone_15_Pro_Max](https://theapplewiki.com/wiki/Firmware/iPhone/17.x#iPhone_15_Pro_Max)
     *
     *
     * @see .IOS_USER_AGENT_VERSION
     */
    private const val IOS_OS_VERSION: String = "17.5.1.21F90"

    /**
     * Spoofing an iPhone 15 running iOS 17.5.1 with the hardcoded version of the iOS app. To be
     * used in the user agent for requests.
     *
     * @see .IOS_OS_VERSION
     */
    private const val IOS_USER_AGENT_VERSION: String = "17_5_1"

    private var numberGenerator = Random()

    private const val FEED_BASE_CHANNEL_ID = "https://www.youtube.com/feeds/videos.xml?channel_id="
    private const val FEED_BASE_USER = "https://www.youtube.com/feeds/videos.xml?user="
    private val C_WEB_PATTERN: Pattern = Pattern.compile("&c=WEB")
    private val C_TVHTML5_SIMPLY_EMBEDDED_PLAYER_PATTERN: Pattern = Pattern.compile("&c=TVHTML5_SIMPLY_EMBEDDED_PLAYER")
    private val C_ANDROID_PATTERN: Pattern = Pattern.compile("&c=ANDROID")
    private val C_IOS_PATTERN: Pattern = Pattern.compile("&c=IOS")

    private val GOOGLE_URLS = setOf("google.", "m.google.", "www.google.")
    private val INVIDIOUS_URLS = setOf("invidio.us", "dev.invidio.us",
        "www.invidio.us", "redirect.invidious.io", "invidious.snopyta.org", "yewtu.be",
        "tube.connect.cafe", "tubus.eduvid.org", "invidious.kavin.rocks", "invidious.site",
        "invidious-us.kavin.rocks", "piped.kavin.rocks", "vid.mint.lgbt", "invidiou.site",
        "invidious.fdn.fr", "invidious.048596.xyz", "invidious.zee.li", "vid.puffyan.us",
        "ytprivate.com", "invidious.namazso.eu", "invidious.silkky.cloud", "ytb.trom.tf",
        "invidious.exonip.de", "inv.riverside.rocks", "invidious.blamefran.net", "y.com.cm",
        "invidious.moomoo.me", "yt.cyberhost.uk")
    private val YOUTUBE_URLS = setOf("youtube.com", "www.youtube.com", "m.youtube.com", "music.youtube.com")

    /**
     * Get the value of the consent's acceptance.
     *
     * @see .setConsentAccepted
     * @return the consent's acceptance value
     */
    /**
     * Determines how the consent cookie that is required for YouTube, `SOCS`, will be generated.
     *
     *  * `false` (the default value) will use `CAE=`;
     *  * `true` will use `CAISAiAD`.
     *
     * Setting this value to `true` is needed to extract mixes and some YouTube Music
     * playlists in some countries such as the EU ones.
     *
     */
    var isConsentAccepted: Boolean = false

    /**
     * Returns a [Map] containing the required YouTube Music headers.
     */
    val youtubeMusicHeaders: Map<String, List<String>>
        get() {
            val headers = HashMap(getOriginReferrerHeaders(YOUTUBE_MUSIC_URL))
            headers.putAll(getClientHeaders(YOUTUBE_MUSIC_CLIENT_ID, youtubeMusicClientVersion?:""))
            return headers
        }

    /**
     * Returns a [Map] containing the required YouTube headers, including the
     * `CONSENT` cookie to prevent redirects to `consent.youtube.com`
     */
    @get:Throws(ExtractionException::class, IOException::class)
    val youTubeHeaders: Map<String, List<String>>
        get() {
            val headers = clientInfoHeaders
            headers["Cookie"] = listOf(generateConsentCookie())
            return headers
        }

    /**
     * Returns a [Map] containing the `X-YouTube-Client-Name`,
     * `X-YouTube-Client-Version`, `Origin`, and `Referer` headers.
     */
    @get:Throws(ExtractionException::class, IOException::class)
    val clientInfoHeaders: MutableMap<String, List<String>>
        get() {
            val headers = HashMap(getOriginReferrerHeaders("https://www.youtube.com"))
            headers.putAll(getClientHeaders(WEB_CLIENT_ID, getClientVersion()?:""))
            return headers
        }

    /**
     * Create a map with the required cookie header.
     * @return A singleton map containing the header.
     */
    val cookieHeader: Map<String, List<String>>
        get() = java.util.Map.of("Cookie", listOf(generateConsentCookie()))

    @get:Throws(IOException::class, ReCaptchaException::class)
    val isHardcodedYoutubeMusicClientVersionValid: Boolean
        get() {
            val url = ("https://music.youtube.com/youtubei/v1/music/get_search_suggestions?$DISABLE_PRETTY_PRINT_PARAMETER")

            // @formatter:off
            val json = JsonWriter.string()
                .`object`()
                .`object`("context")
                .`object`("client")
                .value("clientName", "WEB_REMIX")
                .value("clientVersion", HARDCODED_YOUTUBE_MUSIC_CLIENT_VERSION)
                .value("hl", "en-GB")
                .value("gl", "GB")
                .value("platform", "DESKTOP")
                .value("utcOffsetMinutes", 0)
                .end()
                .`object`("request")
                .array("internalExperimentFlags")
                .end()
                .value("useSsl", true)
                .end()
                .`object`("user") // TODO: provide a way to enable restricted mode with:
                //  .value("enableSafetyMode", boolean)
                .value("lockedSafetyMode", false)
                .end()
                .end()
                .value("input", "")
                .end().done().toByteArray(StandardCharsets.UTF_8)

            // @formatter:on
            val headers = HashMap(getOriginReferrerHeaders(YOUTUBE_MUSIC_URL))
            headers.putAll(getClientHeaders(YOUTUBE_MUSIC_CLIENT_ID, HARDCODED_YOUTUBE_MUSIC_CLIENT_VERSION))

            val response = downloader.postWithContentTypeJson(url, headers, json)
            // Ensure to have a valid response
            return response.responseBody().length > 500 && response.responseCode() == 200
        }

    fun isGoogleURL(url: String?): Boolean {
        val cachedUrl = extractCachedUrlIfNeeded(url)
        try {
            val u = URL(cachedUrl)
            return GOOGLE_URLS.stream().anyMatch { item: String? -> u.host.startsWith(item!!) }
        } catch (e: MalformedURLException) { return false }
    }

    fun isYoutubeURL(url: URL): Boolean {
        return YOUTUBE_URLS.contains(url.host.lowercase())
    }

    fun isYoutubeServiceURL(url: URL): Boolean {
        val host = url.host
        return (host.equals("www.youtube-nocookie.com", ignoreCase = true) || host.equals("youtu.be", ignoreCase = true))
    }

    fun isHooktubeURL(url: URL): Boolean {
        val host = url.host
        return host.equals("hooktube.com", ignoreCase = true)
    }

    fun isInvidiousURL(url: URL): Boolean {
        return INVIDIOUS_URLS.contains(url.host.lowercase())
    }

    fun isY2ubeURL(url: URL): Boolean {
        return url.host.equals("y2u.be", ignoreCase = true)
    }

    /**
     * Parses the duration string of the video expecting ":" or "." as separators
     *
     * @return the duration in seconds
     * @throws ParsingException when more than 3 separators are found
     */
    @Throws(ParsingException::class, NumberFormatException::class)
    fun parseDurationString(input: String): Int {
        // If time separator : is not detected, try . instead
        val splitInput = if (input.contains(":")) input.split(":".toRegex())
            .dropLastWhile { it.isEmpty() }.toTypedArray() else input.split("\\.".toRegex())
            .dropLastWhile { it.isEmpty() }.toTypedArray()

        val units = intArrayOf(24, 60, 60, 1)
        val offset = units.size - splitInput.size
        if (offset < 0) throw ParsingException("Error duration string with unknown format: $input")
        var duration = 0
        for (i in splitInput.indices) {
            duration = units[i + offset] * (duration + convertDurationToInt(splitInput[i]))
        }
        return duration
    }

    /**
     * Tries to convert a duration string to an integer without throwing an exception.
     * <br></br>
     * Helper method for [.parseDurationString].
     * <br></br>
     * Note: This method is also used as a workaround for Vista#8034 (YT shorts no longer
     * display any duration in channels).
     *
     * @param input The string to process
     * @return The converted integer or 0 if the conversion failed.
     */
    private fun convertDurationToInt(input: String?): Int {
        if (input.isNullOrEmpty()) return 0
        val clearedInput = removeNonDigitCharacters(input)
        return try { clearedInput.toInt() } catch (ex: NumberFormatException) { 0 }
    }

    fun getFeedUrlFrom(channelIdOrUser: String): String {
        return when {
            channelIdOrUser.startsWith("user/") -> FEED_BASE_USER + channelIdOrUser.replace("user/", "")
            channelIdOrUser.startsWith("channel/") -> FEED_BASE_CHANNEL_ID + channelIdOrUser.replace("channel/", "")
            else -> FEED_BASE_CHANNEL_ID + channelIdOrUser
        }
    }

    @Throws(ParsingException::class)
    fun parseDateFrom(textualUploadDate: String): OffsetDateTime {
        return try {
            OffsetDateTime.parse(textualUploadDate)
        } catch (e: DateTimeParseException) {
            try { LocalDate.parse(textualUploadDate).atStartOfDay().atOffset(ZoneOffset.UTC)
            } catch (e1: DateTimeParseException) { throw ParsingException("Could not parse date: \"$textualUploadDate\"", e1) }
        }
    }

    /**
     * Checks if the given playlist id is a YouTube Mix (auto-generated playlist)
     * Ids from a YouTube Mix start with "RD"
     *
     * @param playlistId the playlist id
     * @return Whether given id belongs to a YouTube Mix
     */
    fun isYoutubeMixId(playlistId: String): Boolean {
        return playlistId.startsWith("RD")
    }

    /**
     * Checks if the given playlist id is a YouTube My Mix (auto-generated playlist)
     * Ids from a YouTube My Mix start with "RDMM"
     *
     * @param playlistId the playlist id
     * @return Whether given id belongs to a YouTube My Mix
     */
    fun isYoutubeMyMixId(playlistId: String): Boolean {
        return playlistId.startsWith("RDMM")
    }

    /**
     * Checks if the given playlist id is a YouTube Music Mix (auto-generated playlist)
     * Ids from a YouTube Music Mix start with "RDAMVM" or "RDCLAK"
     *
     * @param playlistId the playlist id
     * @return Whether given id belongs to a YouTube Music Mix
     */
    fun isYoutubeMusicMixId(playlistId: String): Boolean {
        return playlistId.startsWith("RDAMVM") || playlistId.startsWith("RDCLAK")
    }

    /**
     * Checks if the given playlist id is a YouTube Channel Mix (auto-generated playlist)
     * Ids from a YouTube channel Mix start with "RDCM"
     *
     * @return Whether given id belongs to a YouTube Channel Mix
     */
    fun isYoutubeChannelMixId(playlistId: String): Boolean {
        return playlistId.startsWith("RDCM")
    }

    /**
     * Checks if the given playlist id is a YouTube Genre Mix (auto-generated playlist)
     * Ids from a YouTube Genre Mix start with "RDGMEM"
     *
     * @return Whether given id belongs to a YouTube Genre Mix
     */
    fun isYoutubeGenreMixId(playlistId: String): Boolean {
        return playlistId.startsWith("RDGMEM")
    }

    /**
     * @param playlistId the playlist id to parse
     * @return the [PlaylistInfo.PlaylistType] extracted from the playlistId (mix playlist
     * types included)
     * @throws ParsingException if the playlistId is null or empty, if the playlistId is not a mix,
     * if it is a mix but it's not based on a specific stream (this is the
     * case for channel or genre mixes)
     */
    @Throws(ParsingException::class)
    fun extractVideoIdFromMixId(playlistId: String): String {
        when {
            playlistId.isEmpty() -> throw ParsingException("Video id could not be determined from empty playlist id")
            isYoutubeMyMixId(playlistId) -> return playlistId.substring(4)
            isYoutubeMusicMixId(playlistId) -> return playlistId.substring(6)
            // Channel mixes are of the form RMCM{channelId}, so videoId can't be determined
            isYoutubeChannelMixId(playlistId) -> throw ParsingException("Video id could not be determined from channel mix id: $playlistId")
            // Genre mixes are of the form RDGMEM{garbage}, so videoId can't be determined
            isYoutubeGenreMixId(playlistId) -> throw ParsingException("Video id could not be determined from genre mix id: $playlistId")
            isYoutubeMixId(playlistId) -> { // normal mix
                // Stream YouTube mixes are of the form RD{videoId}, but if videoId is not exactly
                // 11 characters then it can't be a video id, hence we are dealing with a different
                // type of mix (e.g. genre mixes handled above, of the form RDGMEM{garbage})
                if (playlistId.length != 13) throw ParsingException("Video id could not be determined from mix id: $playlistId")
                return playlistId.substring(2)
            }
            // not a mix
            else -> throw ParsingException("Video id could not be determined from playlist id: $playlistId")
        }
    }

    /**
     * @param playlistId the playlist id to parse
     * @return the [PlaylistInfo.PlaylistType] extracted from the playlistId (mix playlist
     * types included)
     * @throws ParsingException if the playlistId is null or empty
     */
    @Throws(ParsingException::class)
    fun extractPlaylistTypeFromPlaylistId(playlistId: String?): PlaylistType {
        return when {
            playlistId.isNullOrEmpty() -> throw ParsingException("Could not extract playlist type from empty playlist id")
            isYoutubeMusicMixId(playlistId) -> PlaylistType.MIX_MUSIC
            isYoutubeChannelMixId(playlistId) -> PlaylistType.MIX_CHANNEL
            isYoutubeGenreMixId(playlistId) -> PlaylistType.MIX_GENRE
            // Either a normal mix based on a stream, or a "my mix" (still based on a stream).
            // NOTE: if YouTube introduces even more types of mixes that still start with RD,
            // they will default to this, even though they might not be based on a stream.
            isYoutubeMixId(playlistId) -> { /* normal mix */ PlaylistType.MIX_STREAM
            }
            // not a known type of mix: just consider it a normal playlist
            else -> PlaylistType.NORMAL
        }
    }

    /**
     * @param playlistUrl the playlist url to parse
     * @return the [PlaylistInfo.PlaylistType] extracted from the playlistUrl's list param
     * (mix playlist types included)
     * @throws ParsingException if the playlistUrl is malformed, if has no list param or if the list
     * param is empty
     */
    @Throws(ParsingException::class)
    fun extractPlaylistTypeFromPlaylistUrl(playlistUrl: String?): PlaylistType {
        try {
            return extractPlaylistTypeFromPlaylistId(getQueryValue(stringToURL(playlistUrl!!), "list"))
        } catch (e: MalformedURLException) { throw ParsingException("Could not extract playlist type from malformed url", e) }
    }

    @Throws(ParsingException::class)
    private fun getInitialData(html: String): JsonObject {
        try {
            return JsonParser.`object`().from(getStringResultFromRegexArray(html, INITIAL_DATA_REGEXES, 1))
        } catch (e: JsonParserException) { throw ParsingException("Could not get ytInitialData", e)
        } catch (e: RegexException) { throw ParsingException("Could not get ytInitialData", e) }
    }

    @Throws(IOException::class, ExtractionException::class)
    fun isHardcodedClientVersionValid(): Boolean {
        if (hardcodedClientVersionValid != null) return hardcodedClientVersionValid!!
        // @formatter:off
         val body = JsonWriter.string()
        .`object`()
        .`object`("context")
        .`object`("client")
        .value("hl", "en-GB")
        .value("gl", "GB")
        .value("clientName", "WEB")
        .value("clientVersion", HARDCODED_CLIENT_VERSION)
        .value("platform", "DESKTOP")
        .value("utcOffsetMinutes", 0)
        .end()
        .`object`("request")
        .array("internalExperimentFlags")
        .end()
        .value("useSsl", true)
        .end()
        .`object`("user") // TODO: provide a way to enable restricted mode with:
 //  .value("enableSafetyMode", boolean)
        .value("lockedSafetyMode", false)
        .end()
        .end()
        .value("fetchLiveState", true)
        .end().done().toByteArray(StandardCharsets.UTF_8)

        val headers = getClientHeaders(WEB_CLIENT_ID, HARDCODED_CLIENT_VERSION)

        // This endpoint is fetched by the YouTube website to get the items of its main menu and is
        // pretty lightweight (around 30kB)
        val response = downloader.postWithContentTypeJson(YOUTUBEI_V1_URL + "guide?" + DISABLE_PRETTY_PRINT_PARAMETER, headers, body)

        val responseBody = response.responseBody()
        val responseCode = response.responseCode()

        hardcodedClientVersionValid = responseBody.length > 5000 && responseCode == 200 // Ensure to have a valid response
        return hardcodedClientVersionValid!!
    }

    @Throws(IOException::class, ExtractionException::class)
    private fun extractClientVersionFromSwJs() {
        if (clientVersionExtracted) return
        val url = "https://www.youtube.com/sw.js"
        val headers = getOriginReferrerHeaders("https://www.youtube.com")
        val response: String = downloader.get(url, headers).responseBody()
        try {
            clientVersion = getStringResultFromRegexArray(response, INNERTUBE_CONTEXT_CLIENT_VERSION_REGEXES, 1)
        } catch (e: RegexException) { throw ParsingException("Could not extract YouTube WEB InnerTube client version " + "from sw.js", e) }
        clientVersionExtracted = true
    }

    @Throws(IOException::class, ExtractionException::class)
    private fun extractClientVersionFromHtmlSearchResultsPage() {
        // Don't extract the client version and the InnerTube key if it has been already extracted
        if (clientVersionExtracted) return

        // Don't provide a search term in order to have a smaller response
        val url = "https://www.youtube.com/results?search_query=&ucbcb=1"
        val html = downloader.get(url, cookieHeader).responseBody()
        val initialData = getInitialData(html)
        val serviceTrackingParams = initialData.getObject("responseContext").getArray("serviceTrackingParams")

        // Try to get version from initial data first
        val serviceTrackingParamsStream = serviceTrackingParams.stream()
            .filter { o: Any? -> JsonObject::class.java.isInstance(o) }
            .map { obj: Any? -> JsonObject::class.java.cast(obj) }

        clientVersion = getClientVersionFromServiceTrackingParam(serviceTrackingParamsStream, "CSI", "cver")

        if (clientVersion == null) {
            try { clientVersion = getStringResultFromRegexArray(html, INNERTUBE_CONTEXT_CLIENT_VERSION_REGEXES, 1) } catch (ignored: RegexException) { }
        }

        // Fallback to get a shortened client version which does not contain the last two
        // digits
        if (clientVersion.isNullOrEmpty())
            clientVersion = getClientVersionFromServiceTrackingParam(serviceTrackingParamsStream, "ECATCHER", "client.version")
        if (clientVersion == null) throw ParsingException("Could not extract YouTube WEB InnerTube client version from HTML search results page")
        clientVersionExtracted = true
    }

    private fun getClientVersionFromServiceTrackingParam(serviceTrackingParamsStream: Stream<JsonObject>, serviceName: String,
                                                         clientVersionKey: String): String? {
        return serviceTrackingParamsStream.filter { serviceTrackingParam: JsonObject -> (serviceTrackingParam.getString("service", "") == serviceName) }
            .flatMap { serviceTrackingParam: JsonObject -> serviceTrackingParam.getArray("params").stream() }
            .filter { o: Any? -> JsonObject::class.java.isInstance(o) }
            .map { obj: Any? -> JsonObject::class.java.cast(obj) }
            .filter { param: JsonObject -> (param.getString("key", "") == clientVersionKey) }
            .map { param: JsonObject -> param.getString("value") }
            .filter { paramValue: String? -> !paramValue.isNullOrEmpty() }
            .findFirst()
            .orElse(null)
    }

    /**
     * Get the client version used by YouTube website on InnerTube requests.
     */
    @Throws(IOException::class, ExtractionException::class)
    fun getClientVersion(): String? {
        if (!clientVersion.isNullOrEmpty()) return clientVersion!!

        // Always extract the latest client version, by trying first to extract it from the
        // JavaScript service worker, then from HTML search results page as a fallback, to prevent
        // fingerprinting based on the client version used
        try { extractClientVersionFromSwJs() } catch (e: Exception) { extractClientVersionFromHtmlSearchResultsPage() }

        if (clientVersionExtracted) return clientVersion

        // Fallback to the hardcoded one if it is valid
        if (isHardcodedClientVersionValid()) {
            clientVersion = HARDCODED_CLIENT_VERSION
            return clientVersion
        }

        throw ExtractionException("Could not get YouTube WEB client version")
    }

    /**
     * **Only used in tests.**
     *
     * Quick-and-dirty solution to reset global state in between test classes.
     *
     * This is needed for the mocks because in order to reach that state a network request has to
     * be made. If the global state is not reset and the RecordingDownloader is used,
     * then only the first test class has that request recorded. Meaning running the other
     * tests with mocks will fail, because the mock is missing.
     *
     */
    fun resetClientVersion() {
        clientVersion = null
        clientVersionExtracted = false
    }

    /**
     * **Only used in tests.**
     */
    fun setNumberGenerator(random: Random) {
        numberGenerator = random
    }

    @Throws(IOException::class, ReCaptchaException::class, RegexException::class)
    fun getYoutubeMusicClientVersion(): String? {
        if (!youtubeMusicClientVersion.isNullOrEmpty()) return youtubeMusicClientVersion
        if (isHardcodedYoutubeMusicClientVersionValid) {
            youtubeMusicClientVersion = HARDCODED_YOUTUBE_MUSIC_CLIENT_VERSION
            return youtubeMusicClientVersion
        }

        try {
            val url = "https://music.youtube.com/sw.js"
            val headers = getOriginReferrerHeaders(YOUTUBE_MUSIC_URL)
            val response: String = downloader.get(url, headers).responseBody()
            youtubeMusicClientVersion = getStringResultFromRegexArray(response, INNERTUBE_CONTEXT_CLIENT_VERSION_REGEXES, 1)
        } catch (e: Exception) {
            val url = "https://music.youtube.com/?ucbcb=1"
            val html = downloader.get(url, cookieHeader).responseBody()
            youtubeMusicClientVersion = getStringResultFromRegexArray(html, INNERTUBE_CONTEXT_CLIENT_VERSION_REGEXES, 1)
        }
        return youtubeMusicClientVersion
    }

    fun getUrlFromNavigationEndpoint(navigationEndpoint: JsonObject): String? {
        if (navigationEndpoint.has("urlEndpoint")) {
            var internUrl = navigationEndpoint.getObject("urlEndpoint").getString("url")
            // remove https://www.youtube.com part to fall in the next if block
            if (internUrl.startsWith("https://www.youtube.com/redirect?")) internUrl = internUrl.substring(23)
            when {
                internUrl.startsWith("/redirect?") -> {
                    // q parameter can be the first parameter
                    internUrl = internUrl.substring(10)
                    val params = internUrl.split("&".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    for (param in params) {
//                        if (param.split("=")[0].equals("q")) {
                        if (param.split("=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0] == "q")
                            return decodeUrlUtf8(param.split("=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1])
                    }
                }
                internUrl.startsWith("http") -> return internUrl
                internUrl.startsWith("/channel") || internUrl.startsWith("/user") || internUrl.startsWith("/watch") ->
                    return "https://www.youtube.com$internUrl"
            }
        }

        if (navigationEndpoint.has("browseEndpoint")) {
            val browseEndpoint = navigationEndpoint.getObject("browseEndpoint")
            val canonicalBaseUrl = browseEndpoint.getString("canonicalBaseUrl")
            val browseId = browseEndpoint.getString("browseId")

            if (browseId != null) {
                // All channel IDs are prefixed with UC
                if (browseId.startsWith("UC")) return "https://www.youtube.com/channel/$browseId"
                // All playlist IDs are prefixed with VL, which needs to be removed from the
                // playlist ID
                if (browseId.startsWith("VL")) return "https://www.youtube.com/playlist?list=" + browseId.substring(2)
            }
            if (!canonicalBaseUrl.isNullOrEmpty()) return "https://www.youtube.com$canonicalBaseUrl"
        }

        if (navigationEndpoint.has("watchEndpoint")) {
            val url = StringBuilder()
            url.append("https://www.youtube.com/watch?v=").append(navigationEndpoint.getObject("watchEndpoint").getString(VIDEO_ID))
            if (navigationEndpoint.getObject("watchEndpoint").has("playlistId"))
                url.append("&list=").append(navigationEndpoint.getObject("watchEndpoint").getString("playlistId"))

            if (navigationEndpoint.getObject("watchEndpoint").has("startTimeSeconds"))
                url.append("&t=").append(navigationEndpoint.getObject("watchEndpoint").getInt("startTimeSeconds"))

            return url.toString()
        }

        if (navigationEndpoint.has("watchPlaylistEndpoint"))
            return ("https://www.youtube.com/playlist?list=" + navigationEndpoint.getObject("watchPlaylistEndpoint").getString("playlistId"))

        if (navigationEndpoint.has("commandMetadata")) {
            val metadata = navigationEndpoint.getObject("commandMetadata").getObject("webCommandMetadata")
            if (metadata.has("url")) return "https://www.youtube.com" + metadata.getString("url")
        }
        return null
    }

    /**
     * Get the text from a JSON object that has either a `simpleText` or a `runs`
     * array.
     *
     * @param textObject JSON object to get the text from
     * @param html       whether to return HTML, by parsing the `navigationEndpoint`
     * @return text in the JSON object or `null`
     */
    fun getTextFromObject(textObject: JsonObject, html: Boolean): String? {
        if (textObject.isEmpty()) return null

        if (textObject.has("simpleText")) return textObject.getString("simpleText")

        val runs = textObject.getArray("runs")
        if (runs.isEmpty()) return null

        val textBuilder = StringBuilder()
        for (o in runs) {
            val run = o as JsonObject
            var text = run.getString("text")

            if (html) {
                if (run.has("navigationEndpoint")) {
                    val url = getUrlFromNavigationEndpoint(run.getObject("navigationEndpoint"))
                    if (!url.isNullOrEmpty()) text = ("<a href=\"" + Entities.escape(url) + "\">" + Entities.escape(text) + "</a>")
                }

                val bold = (run.has("bold") && run.getBoolean("bold"))
                val italic = (run.has("italics") && run.getBoolean("italics"))
                val strikethrough = (run.has("strikethrough") && run.getBoolean("strikethrough"))

                if (bold) textBuilder.append("<b>")
                if (italic) textBuilder.append("<i>")
                if (strikethrough) textBuilder.append("<s>")

                textBuilder.append(text)

                if (strikethrough) textBuilder.append("</s>")
                if (italic) textBuilder.append("</i>")
                if (bold) textBuilder.append("</b>")
            } else textBuilder.append(text)
        }

        var text = textBuilder.toString()

        if (html) {
            text = text.replace("\\n".toRegex(), "<br>")
            text = text.replace(" {2}".toRegex(), " &nbsp;")
        }
        return text
    }

    @Throws(ParsingException::class)
    fun getTextFromObjectOrThrow(textObject: JsonObject, error: String): String {
        val result = getTextFromObject(textObject) ?: throw ParsingException("Could not extract text: $error")
        return result
    }

    fun getTextFromObject(textObject: JsonObject): String? {
        return getTextFromObject(textObject, false)
    }

    fun getUrlFromObject(textObject: JsonObject): String? {
        if (textObject.isEmpty()) return null

        val runs = textObject.getArray("runs")
        if (runs.isEmpty()) return null

        for (textPart in runs) {
            val url = getUrlFromNavigationEndpoint((textPart as JsonObject).getObject("navigationEndpoint"))
            if (!url.isNullOrEmpty()) return url
        }
        return null
    }

    fun getTextAtKey(jsonObject: JsonObject, theKey: String): String? {
        return if (jsonObject.isString(theKey)) jsonObject.getString(theKey) else getTextFromObject(jsonObject.getObject(theKey))
    }

    fun fixThumbnailUrl(thumbnailUrl: String): String {
        var result = thumbnailUrl
        if (result.startsWith("//")) result = result.substring(2)

        if (result.startsWith(HTTP)) result = replaceHttpWithHttps(result) else if (!result.startsWith(HTTPS)) result = "https://$result"

        return result
    }

    /**
     * Get thumbnails from a [JsonObject] representing a YouTube
     * [InfoItem][ac.mdiq.vista.extractor.InfoItem].
     *
     * Thumbnails are got from the `thumbnails` [JsonArray] inside the `thumbnail`
     * [JsonObject] of the YouTube [InfoItem][ac.mdiq.vista.extractor.InfoItem],
     * using [.getImagesFromThumbnailsArray].
     *
     * @param infoItem a YouTube [InfoItem][ac.mdiq.vista.extractor.InfoItem]
     * @return an unmodifiable list of [Image]s found in the `thumbnails`
     * [JsonArray]
     * @throws ParsingException if an exception occurs when
     * [.getImagesFromThumbnailsArray] is executed
     */
    @Throws(ParsingException::class)
    fun getThumbnailsFromInfoItem(infoItem: JsonObject): List<Image> {
        try { return getImagesFromThumbnailsArray(infoItem.getObject("thumbnail").getArray("thumbnails")) } catch (e: Exception) { throw ParsingException("Could not get thumbnails from InfoItem", e) }
    }

    /**
     * Get images from a YouTube `thumbnails` [JsonArray].
     *
     * The properties of the [Image]s created will be set using the corresponding ones of
     * thumbnail items.
     *
     * @param thumbnails a YouTube `thumbnails` [JsonArray]
     * @return an unmodifiable list of [Image]s extracted from the given [JsonArray]
     */
    fun getImagesFromThumbnailsArray(thumbnails: JsonArray): List<Image> {
        return thumbnails.stream()
            .filter { o: Any? -> JsonObject::class.java.isInstance(o) }
            .map { obj: Any? -> JsonObject::class.java.cast(obj) }
            .filter { thumbnail: JsonObject -> !thumbnail.getString("url").isNullOrEmpty() }
            .map { thumbnail: JsonObject ->
                val height = thumbnail.getInt("height", Image.HEIGHT_UNKNOWN)
                Image(fixThumbnailUrl(thumbnail.getString("url")), height, thumbnail.getInt("width", Image.WIDTH_UNKNOWN), fromHeight(height))
            }
            .collect(Collectors.toUnmodifiableList())
    }

    @Throws(ParsingException::class, MalformedURLException::class)
    fun getValidJsonResponseBody(response: Response): String {
        if (response.responseCode() == 404)
            throw ContentNotAvailableException("Not found" + " (\"" + response.responseCode() + " " + response.responseMessage() + "\")")

        val responseBody = response.responseBody()
        // Ensure to have a valid response
        if (responseBody.length < 50) throw ParsingException("JSON response is too short")

        // Check if the request was redirected to the error page.
        val latestUrl = URL(response.latestUrl())
        if (latestUrl.host.equals("www.youtube.com", ignoreCase = true)) {
            val path = latestUrl.path
            if (path.equals("/oops", ignoreCase = true) || path.equals("/error", ignoreCase = true))
                throw ContentNotAvailableException("Content unavailable")
        }

        val responseContentType = response.getHeader("Content-Type")
        if (responseContentType != null && responseContentType.lowercase(Locale.getDefault()).contains("text/html"))
            throw ParsingException("Got HTML document, expected JSON response" + " (latest url was: \"" + response.latestUrl() + "\")")

        return responseBody
    }

    @Throws(IOException::class, ExtractionException::class)
    fun getJsonPostResponse(endpoint: String, body: ByteArray?, localization: Localization?): JsonObject {
        val headers = youTubeHeaders

        return toJsonObject(getValidJsonResponseBody(downloader.postWithContentTypeJson(YOUTUBEI_V1_URL + endpoint + "?"
                    + DISABLE_PRETTY_PRINT_PARAMETER, headers, body, localization)))
    }

    @Throws(IOException::class, ExtractionException::class)
    fun getJsonAndroidPostResponse(endpoint: String, body: ByteArray, localization: Localization, endPartOfUrlRequest: String?): JsonObject {
        return getMobilePostResponse(endpoint, body, localization, getAndroidUserAgent(localization), endPartOfUrlRequest)
    }

    @Throws(IOException::class, ExtractionException::class)
    fun getJsonIosPostResponse(endpoint: String, body: ByteArray, localization: Localization, endPartOfUrlRequest: String?): JsonObject {
        return getMobilePostResponse(endpoint, body, localization, getIosUserAgent(localization), endPartOfUrlRequest)
    }

    @Throws(IOException::class, ExtractionException::class)
    private fun getMobilePostResponse(endpoint: String, body: ByteArray, localization: Localization, userAgent: String,
                                       endPartOfUrlRequest: String?): JsonObject {
        val headers = java.util.Map.of("User-Agent", listOf(userAgent), "X-Goog-Api-Format-Version", listOf("2"))

        val baseEndpointUrl = ((YOUTUBEI_V1_GAPIS_URL + endpoint) + "?" + DISABLE_PRETTY_PRINT_PARAMETER)

        return toJsonObject(getValidJsonResponseBody(downloader.postWithContentTypeJson(
            if (endPartOfUrlRequest.isNullOrEmpty()) baseEndpointUrl else baseEndpointUrl + endPartOfUrlRequest,
            headers, body, localization)))
    }

    @Throws(IOException::class, ExtractionException::class)
    fun prepareDesktopJsonBuilder(localization: Localization, contentCountry: ContentCountry): JsonBuilder<JsonObject> {
        return prepareDesktopJsonBuilder(localization, contentCountry, null)
    }

    @Throws(IOException::class, ExtractionException::class)
    fun prepareDesktopJsonBuilder(localization: Localization, contentCountry: ContentCountry, visitorData: String?): JsonBuilder<JsonObject> {
        // @formatter:off
         val builder = JsonObject.builder()
        .`object`("context")
        .`object`("client")
        .value("hl", localization.localizationCode)
        .value("gl", contentCountry.countryCode)
        .value("clientName", "WEB")
        .value("clientVersion", getClientVersion())
        .value("originalUrl", "https://www.youtube.com")
        .value("platform", "DESKTOP")
        .value("utcOffsetMinutes", 0)

        if (visitorData != null) builder.value("visitorData", visitorData)

        return builder.end()
        .`object`("request")
        .array("internalExperimentFlags")
        .end()
        .value("useSsl", true)
        .end()
        .`object`("user") // TODO: provide a way to enable restricted mode with:
 //  .value("enableSafetyMode", boolean)
        .value("lockedSafetyMode", false)
        .end()
        .end()
            // @formatter:on
    }

    fun prepareAndroidMobileJsonBuilder(localization: Localization, contentCountry: ContentCountry): JsonBuilder<JsonObject> {
        // @formatter:off
        return JsonObject.builder()
        .`object`("context")
        .`object`("client")
        .value("clientName", "ANDROID")
        .value("clientVersion", ANDROID_YOUTUBE_CLIENT_VERSION)
        .value("platform", "MOBILE")
        .value("osName", "Android")
        .value("osVersion", "14") /*
                        A valid Android SDK version is required to be sure to get a valid player
                        response
                        If this parameter is not provided, the player response is replaced by an
                        error saying the message "The following content is not available on this
                        app. Watch this content on the latest version on YouTube" (it was
                        previously a 5-minute video with this message)
                        See https://github.com/XilinJia/Vista/issues/8713
                        The Android SDK version corresponding to the Android version used in
                        requests is sent
                        */
        .value("androidSdkVersion", 34)
        .value("hl", localization.localizationCode)
        .value("gl", contentCountry.countryCode)
        .value("utcOffsetMinutes", 0)
        .end()
        .`object`("request")
        .array("internalExperimentFlags")
        .end()
        .value("useSsl", true)
        .end()
        .`object`("user") // TODO: provide a way to enable restricted mode with:
 //  .value("enableSafetyMode", boolean)
        .value("lockedSafetyMode", false)
        .end()
        .end()
            // @formatter:on
    }

    fun prepareIosMobileJsonBuilder(localization: Localization, contentCountry: ContentCountry): JsonBuilder<JsonObject> {
        // @formatter:off
        return JsonObject.builder()
        .`object`("context")
        .`object`("client")
        .value("clientName", "IOS")
        .value("clientVersion", IOS_YOUTUBE_CLIENT_VERSION)
        .value("deviceMake", "Apple") // Device model is required to get 60fps streams
        .value("deviceModel", IOS_DEVICE_MODEL)
        .value("platform", "MOBILE")
        .value("osName", "iOS") /*
                        The value of this field seems to use the following structure:
                        "iOS major version.minor version.patch version.build version", where
                        "patch version" is equal to 0 if it isn't set
                        The build version corresponding to the iOS version used can be found on
                        https://theapplewiki.com/wiki/Firmware/iPhone/17.x#iPhone_15
                         */
        .value("osVersion", IOS_OS_VERSION)
        .value("hl", localization.localizationCode)
        .value("gl", contentCountry.countryCode)
        .value("utcOffsetMinutes", 0)
        .end()
        .`object`("request")
        .array("internalExperimentFlags")
        .end()
        .value("useSsl", true)
        .end()
        .`object`("user") // TODO: provide a way to enable restricted mode with:
 //  .value("enableSafetyMode", boolean)
        .value("lockedSafetyMode", false)
        .end()
        .end()
            // @formatter:on
    }

    fun prepareTvHtml5EmbedJsonBuilder(localization: Localization, contentCountry: ContentCountry, videoId: String): JsonBuilder<JsonObject> {
        // @formatter:off
        return JsonObject.builder()
        .`object`("context")
        .`object`("client")
        .value("clientName", "TVHTML5_SIMPLY_EMBEDDED_PLAYER")
        .value("clientVersion", TVHTML5_SIMPLY_EMBED_CLIENT_VERSION)
        .value("clientScreen", "EMBED")
        .value("platform", "TV")
        .value("hl", localization.localizationCode)
        .value("gl", contentCountry.countryCode)
        .value("utcOffsetMinutes", 0)
        .end()
        .`object`("thirdParty")
        .value("embedUrl", "https://www.youtube.com/watch?v=$videoId")
        .end()
        .`object`("request")
        .array("internalExperimentFlags")
        .end()
        .value("useSsl", true)
        .end()
        .`object`("user") // TODO: provide a way to enable restricted mode with:
 //  .value("enableSafetyMode", boolean)
        .value("lockedSafetyMode", false)
        .end()
        .end()
            // @formatter:on
    }

    @Throws(IOException::class, ExtractionException::class)
    fun getWebPlayerResponse(localization: Localization, contentCountry: ContentCountry, videoId: String): JsonObject {
        val body = JsonWriter.string(
            prepareDesktopJsonBuilder(localization, contentCountry)
                .value(VIDEO_ID, videoId)
                .value(CONTENT_CHECK_OK, true)
                .value(RACY_CHECK_OK, true)
                .done())
            .toByteArray(StandardCharsets.UTF_8)
        val url = (YOUTUBEI_V1_URL + "player" + "?" + DISABLE_PRETTY_PRINT_PARAMETER + "&\$fields=microformat,playabilityStatus,storyboards,videoDetails")

        return toJsonObject(getValidJsonResponseBody(downloader.postWithContentTypeJson(url, youTubeHeaders, body, localization)))
    }

    fun createTvHtml5EmbedPlayerBody(localization: Localization, contentCountry: ContentCountry, videoId: String,
                                sts: Int, contentPlaybackNonce: String): ByteArray {
        // @formatter:off
        return JsonWriter.string(prepareTvHtml5EmbedJsonBuilder(localization, contentCountry, videoId)
        .`object`("playbackContext")
        .`object`("contentPlaybackContext") // Signature timestamp from the JavaScript base player is needed to get
 // working obfuscated URLs
        .value("signatureTimestamp", sts)
        .value("referer", "https://www.youtube.com/watch?v=$videoId")
        .end()
        .end()
        .value(CPN, contentPlaybackNonce)
        .value(VIDEO_ID, videoId)
        .value(CONTENT_CHECK_OK, true)
        .value(RACY_CHECK_OK, true)
        .done())
        .toByteArray(StandardCharsets.UTF_8)
            // @formatter:on
    }

    /**
     * Get the user-agent string used as the user-agent for InnerTube requests with the Android client.
     * If the [Localization] provided is `null`, fallbacks to
     * [the default one][Localization.DEFAULT].
     * @param localization the [Localization] to set in the user-agent
     * @return the Android user-agent used for InnerTube requests with the Android client,
     * depending on the [Localization] provided
     */
    fun getAndroidUserAgent(localization: Localization?): String {
        // Spoofing an Android 14 device with the hardcoded version of the Android app
        return ("com.google.android.youtube/$ANDROID_YOUTUBE_CLIENT_VERSION (Linux; U; Android 14; ${(localization ?: Localization.DEFAULT).getCountryCode()}) gzip")
    }

    /**
     * Get the user-agent string used as the user-agent for InnerTube requests with the iOS client.
     * If the [Localization] provided is `null`, fallbacks to
     * [the default one][Localization.DEFAULT].
     * @param localization the [Localization] to set in the user-agent
     * @return the iOS user-agent used for InnerTube requests with the iOS client, depending on the
     * [Localization] provided
     */
    fun getIosUserAgent(localization: Localization?): String {
        // Spoofing an iPhone 15 running iOS 17.1.2 with the hardcoded version of the iOS app
        return ((("com.google.ios.youtube/$IOS_YOUTUBE_CLIENT_VERSION").toString() + "(" + IOS_DEVICE_MODEL)
                + (localization ?: Localization.DEFAULT).getCountryCode() + ")")
    }

    /**
     * Returns an unmodifiable [Map] containing the `Origin` and `Referer`
     * headers set to the given URL.
     *
     * @param url The URL to be set as the origin and referrer.
     */
    private fun getOriginReferrerHeaders(url: String): Map<String, List<String>> {
        val urlList = listOf(url)
        return java.util.Map.of("Origin", urlList, "Referer", urlList)
    }

    /**
     * Returns an unmodifiable [Map] containing the `X-YouTube-Client-Name` and
     * `X-YouTube-Client-Version` headers.
     *
     * @param name The X-YouTube-Client-Name value.
     * @param version X-YouTube-Client-Version value.
     */
    private fun getClientHeaders(name: String, version: String): Map<String, List<String>> {
        return java.util.Map.of("X-YouTube-Client-Name", listOf(name), "X-YouTube-Client-Version", listOf(version))
    }

    // CAISAiAD means that the user configured manually cookies YouTube, regardless of
    // the consent values
    // This value surprisingly allows to extract mixes and some YouTube Music playlists
    // in the same way when a user allows all cookies
    // CAE= means that the user rejected all non-necessary cookies with the "Reject
    // all" button on the consent page
    //
    fun generateConsentCookie(): String {
        return "SOCS=" + (if (isConsentAccepted) "CAISAiAD" else "CAE=")
    }

    fun extractCookieValue(cookieName: String, response: Response): String {
        val cookies = response.responseHeaders()["set-cookie"] ?: return ""

        var result = ""
        for (cookie in cookies) {
            val startIndex = cookie.indexOf(cookieName)
            if (startIndex != -1) result = cookie.substring(startIndex + cookieName.length + "=".length, cookie.indexOf(";", startIndex))
        }
        return result
    }

    /**
     * Shared alert detection function, multiple endpoints return the error similarly structured.
     * Will check if the object has an alert of the type "ERROR".
     * @param initialData the object which will be checked if an alert is present
     * @throws ContentNotAvailableException if an alert is detected
     */
    @Throws(ParsingException::class)
    fun defaultAlertsCheck(initialData: JsonObject) {
        val alerts = initialData.getArray("alerts")
        if (!alerts.isNullOrEmpty()) {
            val alertRenderer = alerts.getObject(0).getObject("alertRenderer")
            val alertText = getTextFromObject(alertRenderer.getObject("text"))
            val alertType = alertRenderer.getString("type", "")
            if (alertType.equals("ERROR", ignoreCase = true)) {
                if (alertText != null
                        && (alertText.contains("This account has been terminated") || alertText.contains("This channel was removed"))) {
                    // Possible error messages:
                    // "This account has been terminated for a violation of YouTube's Terms of
                    //     Service."
                    // "This account has been terminated due to multiple or severe violations of
                    //     YouTube's policy prohibiting hate speech."
                    // "This account has been terminated due to multiple or severe violations of
                    //     YouTube's policy prohibiting content designed to harass, bully or
                    //     threaten."
                    // "This account has been terminated due to multiple or severe violations
                    //     of YouTube's policy against spam, deceptive practices and misleading
                    //     content or other Terms of Service violations."
                    // "This account has been terminated due to multiple or severe violations of
                    //     YouTube's policy on nudity or sexual content."
                    // "This account has been terminated for violating YouTube's Community
                    //     Guidelines."
                    // "This account has been terminated because we received multiple
                    //     third-party claims of copyright infringement regarding material that
                    //     the user posted."
                    // "This account has been terminated because it is linked to an account that
                    //     received multiple third-party claims of copyright infringement."
                    // "This channel was removed because it violated our Community Guidelines."
                    if (alertText.matches(".*violat(ed|ion|ing).*".toRegex()) || alertText.contains("infringement"))
                        throw AccountTerminatedException(alertText, AccountTerminatedException.Reason.VIOLATION)
                    else throw AccountTerminatedException(alertText)
                }
                throw ContentNotAvailableException("Got error: \"$alertText\"")
            }
        }
    }

    /**
     * Sometimes, YouTube provides URLs which use Google's cache. They look like
     * `https://webcache.googleusercontent.com/search?q=cache:CACHED_URL`
     *
     * @param url the URL which might refer to the Google's webcache
     * @return the URL which is referring to the original site
     */
    fun extractCachedUrlIfNeeded(url: String?): String? {
        if (url == null) return null
        if (url.contains("webcache.googleusercontent.com")) return url.split("cache:".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1]
        return url
    }

    fun isVerified(badges: JsonArray): Boolean {
        if (badges.isEmpty()) return false

        for (badge in badges) {
            val style = (badge as JsonObject).getObject("metadataBadgeRenderer").getString("style")
            if (style != null && (style == "BADGE_STYLE_TYPE_VERIFIED" || style == "BADGE_STYLE_TYPE_VERIFIED_ARTIST")) return true
        }

        return false
    }

    /**
     * Generate a content playback nonce (also called `cpn`), sent by YouTube clients in
     * playback requests (and also for some clients, in the player request body).
     *
     * @return a content playback nonce string
     */
    fun generateContentPlaybackNonce(): String {
        return generate(CONTENT_PLAYBACK_NONCE_ALPHABET, 16, numberGenerator)
    }

    /**
     * Try to generate a `t` parameter, sent by mobile clients as a query of the player request.
     * Some researches needs to be done to know how this parameter, unique at each request, is generated.
     * @return a 12 characters string to try to reproduce the `` parameter
     */
    fun generateTParameter(): String {
        return generate(CONTENT_PLAYBACK_NONCE_ALPHABET, 12, numberGenerator)
    }

    /**
     * Check if the streaming URL is from the YouTube `WEB` client.
     *
     * @param url the streaming URL to be checked.
     * @return true if it's a `WEB` streaming URL, false otherwise
     */
    fun isWebStreamingUrl(url: String): Boolean {
        return isMatch(C_WEB_PATTERN, url)
    }

    /**
     * Check if the streaming URL is a URL from the YouTube `TVHTML5_SIMPLY_EMBEDDED_PLAYER`
     * client.
     *
     * @param url the streaming URL on which check if it's a `TVHTML5_SIMPLY_EMBEDDED_PLAYER`
     * streaming URL.
     * @return true if it's a `TVHTML5_SIMPLY_EMBEDDED_PLAYER` streaming URL, false otherwise
     */
    fun isTvHtml5SimplyEmbeddedPlayerStreamingUrl(url: String): Boolean {
        return isMatch(C_TVHTML5_SIMPLY_EMBEDDED_PLAYER_PATTERN, url)
    }

    /**
     * Check if the streaming URL is a URL from the YouTube `ANDROID` client.
     *
     * @param url the streaming URL to be checked.
     * @return true if it's a `ANDROID` streaming URL, false otherwise
     */
    fun isAndroidStreamingUrl(url: String): Boolean {
        return isMatch(C_ANDROID_PATTERN, url)
    }

    /**
     * Check if the streaming URL is a URL from the YouTube `IOS` client.
     *
     * @param url the streaming URL on which check if it's a `IOS` streaming URL.
     * @return true if it's a `IOS` streaming URL, false otherwise
     */
    fun isIosStreamingUrl(url: String): Boolean {
        return isMatch(C_IOS_PATTERN, url)
    }

    /**
     * Extract the audio track type from a YouTube stream URL.
     *
     *
     * The track type is parsed from the `xtags` URL parameter
     * (Example: `acont=original:lang=en`).
     *
     * @param streamUrl YouTube stream URL
     * @return [AudioTrackType] or `null` if no track type was found
     */
    fun extractAudioTrackType(streamUrl: String?): AudioTrackType? {
        val xtags: String?
        try { xtags = getQueryValue(URL(streamUrl), "xtags") } catch (e: MalformedURLException) { return null }
        if (xtags == null) return null

        var atype: String? = null
        for (param in xtags.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {
            val kv = param.split("=".toRegex(), limit = 2).toTypedArray()
            if (kv.size > 1 && kv[0] == "acont") {
                atype = kv[1]
                break
            }
        }
        if (atype == null) return null

        return when (atype) {
            "original" -> AudioTrackType.ORIGINAL
            "dubbed" -> AudioTrackType.DUBBED
            "descriptive" -> AudioTrackType.DESCRIPTIVE
            else -> null
        }
    }
}
