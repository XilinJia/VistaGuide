package ac.mdiq.vista.extractor.services.youtube

import ac.mdiq.vista.extractor.Vista.downloader
import ac.mdiq.vista.extractor.exceptions.ExtractionException
import ac.mdiq.vista.extractor.localization.ContentCountry
import ac.mdiq.vista.extractor.localization.Localization
import ac.mdiq.vista.extractor.services.youtube.ClientsConstants.WEB_EMBEDDED_CLIENT_ID
import ac.mdiq.vista.extractor.services.youtube.ClientsConstants.WEB_EMBEDDED_CLIENT_VERSION
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.CONTENT_CHECK_OK
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.CPN
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.DISABLE_PRETTY_PRINT_PARAMETER
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.RACY_CHECK_OK
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.VIDEO_ID
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.YOUTUBEI_V1_GAPIS_URL
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.YOUTUBEI_V1_URL
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.generateTParameter
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.getAndroidUserAgent
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.getClientHeaders
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.getClientVersion
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.getIosUserAgent
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.getOriginReferrerHeaders
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.getValidJsonResponseBody
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.getVisitorDataFromInnertube
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.prepareJsonBuilder
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.youTubeHeaders
import ac.mdiq.vista.extractor.utils.JsonUtils.toJsonObject
import com.grack.nanojson.JsonBuilder
import com.grack.nanojson.JsonObject
import com.grack.nanojson.JsonWriter
import java.io.IOException
import java.nio.charset.StandardCharsets


object YoutubeStreamHelper {
    private const val PLAYER = "player"
    private const val SERVICE_INTEGRITY_DIMENSIONS = "serviceIntegrityDimensions"
    private const val PO_TOKEN = "poToken"
    private const val BASE_YT_DESKTOP_WATCH_URL = "https://www.youtube.com/watch?v="

    @Throws(IOException::class, ExtractionException::class)
    fun getWebMetadataPlayerResponse(localization: Localization, contentCountry: ContentCountry, videoId: String): JsonObject {
        val innertubeClientRequestInfo = InnertubeClientRequestInfo.ofWebClient()
        innertubeClientRequestInfo.clientInfo.clientVersion = getClientVersion()

        val headers: MutableMap<String, List<String>> = youTubeHeaders.toMutableMap()

        // We must always pass a valid visitorData to get valid player responses, which needs to be
        // got from YouTube
        innertubeClientRequestInfo.clientInfo.visitorData = getVisitorDataFromInnertube(innertubeClientRequestInfo,
            localization, contentCountry, headers, YOUTUBEI_V1_URL, null, false)

        val builder: JsonBuilder<JsonObject?> = prepareJsonBuilder(localization, contentCountry, innertubeClientRequestInfo, null)

        addVideoIdCpnAndOkChecks(builder, videoId, null)

        val body: ByteArray? = JsonWriter.string(builder.done()).toByteArray(StandardCharsets.UTF_8)

        val url: String = ("$YOUTUBEI_V1_URL$PLAYER?$DISABLE_PRETTY_PRINT_PARAMETER&\$fields=microformat,playabilityStatus,storyboards,videoDetails")

        return toJsonObject(getValidJsonResponseBody(downloader.postWithContentTypeJson(url, headers, body, localization)))
    }

    @Throws(IOException::class, ExtractionException::class)
    fun getWebEmbeddedPlayerResponse(localization: Localization, contentCountry: ContentCountry,
            videoId: String, cpn: String, webEmbeddedPoTokenResult: PoTokenResult?, signatureTimestamp: Int): JsonObject {
        val innertubeClientRequestInfo = InnertubeClientRequestInfo.ofWebEmbeddedPlayerClient()

        val headers: MutableMap<String, List<String>> = getClientHeaders(WEB_EMBEDDED_CLIENT_ID, WEB_EMBEDDED_CLIENT_VERSION).toMutableMap()
        headers.putAll(getOriginReferrerHeaders("https://www.youtube.com"))

        val embedUrl = BASE_YT_DESKTOP_WATCH_URL + videoId

        // We must always pass a valid visitorData to get valid player responses, which needs to be
        // got from YouTube
        innertubeClientRequestInfo.clientInfo.visitorData = webEmbeddedPoTokenResult?.visitorData ?: getVisitorDataFromInnertube(innertubeClientRequestInfo,
            localization, contentCountry, headers, YOUTUBEI_V1_URL, embedUrl, false)

        val builder: JsonBuilder<JsonObject?> = prepareJsonBuilder(localization, contentCountry, innertubeClientRequestInfo, embedUrl)

        addVideoIdCpnAndOkChecks(builder, videoId, cpn)

        addPlaybackContext(builder, embedUrl, signatureTimestamp)

        if (webEmbeddedPoTokenResult != null) addPoToken(builder, webEmbeddedPoTokenResult.playerRequestPoToken)

        val body: ByteArray? = JsonWriter.string(builder.done()).toByteArray(StandardCharsets.UTF_8)
        val url = "$YOUTUBEI_V1_URL$PLAYER?$DISABLE_PRETTY_PRINT_PARAMETER"

        return toJsonObject(getValidJsonResponseBody(downloader.postWithContentTypeJson(url, headers, body, localization)))
    }

    @Throws(IOException::class, ExtractionException::class)
    fun getAndroidPlayerResponse(contentCountry: ContentCountry, localization: Localization,
                                 videoId: String, cpn: String, androidPoTokenResult: PoTokenResult): JsonObject {
        val innertubeClientRequestInfo = InnertubeClientRequestInfo.ofAndroidClient()
        innertubeClientRequestInfo.clientInfo.visitorData = androidPoTokenResult.visitorData

        val headers = getMobileClientHeaders(getAndroidUserAgent(localization))

        val builder: JsonBuilder<JsonObject?> = prepareJsonBuilder(localization, contentCountry, innertubeClientRequestInfo, null)

        addVideoIdCpnAndOkChecks(builder, videoId, cpn)

        addPoToken(builder, androidPoTokenResult.playerRequestPoToken)

        val body: ByteArray? = JsonWriter.string(builder.done()).toByteArray(StandardCharsets.UTF_8)

        val url = (YOUTUBEI_V1_GAPIS_URL + PLAYER + "?" + DISABLE_PRETTY_PRINT_PARAMETER + "&t=" + generateTParameter() + "&id=" + videoId)

        return toJsonObject(getValidJsonResponseBody(downloader.postWithContentTypeJson(url, headers, body, localization)))
    }

    @Throws(IOException::class, ExtractionException::class)
    fun getAndroidReelPlayerResponse(contentCountry: ContentCountry, localization: Localization, videoId: String, cpn: String): JsonObject {
        val innertubeClientRequestInfo = InnertubeClientRequestInfo.ofAndroidClient()

        val headers = getMobileClientHeaders(getAndroidUserAgent(localization))

        // We must always pass a valid visitorData to get valid player responses, which needs to be
        // got from YouTube
        innertubeClientRequestInfo.clientInfo.visitorData =
            getVisitorDataFromInnertube(innertubeClientRequestInfo, localization, contentCountry, headers, YOUTUBEI_V1_GAPIS_URL, null, false)

        val builder: JsonBuilder<JsonObject?> = prepareJsonBuilder(localization, contentCountry, innertubeClientRequestInfo, null)
        builder.`object`("playerRequest")
        addVideoIdCpnAndOkChecks(builder, videoId, cpn)
        builder.end().value("disablePlayerResponse", false)

        val body: ByteArray? = JsonWriter.string(builder.done()).toByteArray(StandardCharsets.UTF_8)

        val url = (YOUTUBEI_V1_GAPIS_URL + "reel/reel_item_watch" + "?"
                + DISABLE_PRETTY_PRINT_PARAMETER + "&t=" + generateTParameter() + "&id=" + videoId
                + "&\$fields=playerResponse")

        return toJsonObject(getValidJsonResponseBody(downloader.postWithContentTypeJson(url, headers, body, localization))).getObject("playerResponse")
    }

    @Throws(IOException::class, ExtractionException::class)
    fun getIosPlayerResponse(contentCountry: ContentCountry, localization: Localization,
                             videoId: String, cpn: String, iosPoTokenResult: PoTokenResult?): JsonObject {
        val innertubeClientRequestInfo = InnertubeClientRequestInfo.ofIosClient()

        val headers = getMobileClientHeaders(getIosUserAgent(localization))

        // We must always pass a valid visitorData to get valid player responses, which needs to be
        // got from YouTube
        innertubeClientRequestInfo.clientInfo.visitorData = iosPoTokenResult?.visitorData ?: getVisitorDataFromInnertube(innertubeClientRequestInfo, localization, contentCountry, headers, YOUTUBEI_V1_URL, null, false)

        val builder: JsonBuilder<JsonObject?> = prepareJsonBuilder(localization, contentCountry, innertubeClientRequestInfo, null)

        addVideoIdCpnAndOkChecks(builder, videoId, cpn)

        if (iosPoTokenResult != null) addPoToken(builder, iosPoTokenResult.playerRequestPoToken)

        val body: ByteArray? = JsonWriter.string(builder.done()).toByteArray(StandardCharsets.UTF_8)

        val url = (YOUTUBEI_V1_GAPIS_URL + PLAYER + "?" + DISABLE_PRETTY_PRINT_PARAMETER + "&t=" + generateTParameter() + "&id=" + videoId)

        return toJsonObject(getValidJsonResponseBody(downloader.postWithContentTypeJson(url, headers, body, localization)))
    }

    private fun addVideoIdCpnAndOkChecks(builder: JsonBuilder<JsonObject?>, videoId: String, cpn: String?) {
        builder.value(VIDEO_ID, videoId)

        if (cpn != null) builder.value(CPN, cpn)

        builder.value(CONTENT_CHECK_OK, true).value(RACY_CHECK_OK, true)
    }

    private fun addPlaybackContext(builder: JsonBuilder<JsonObject?>, referer: String, signatureTimestamp: Int) {
        builder.`object`("playbackContext")
            .`object`("contentPlaybackContext")
            .value("signatureTimestamp", signatureTimestamp)
            .value("referer", referer)
            .end()
            .end()
    }

    private fun addPoToken(builder: JsonBuilder<JsonObject?>, poToken: String) {
        builder.`object`(SERVICE_INTEGRITY_DIMENSIONS).value(PO_TOKEN, poToken).end()
    }

    private fun getMobileClientHeaders(userAgent: String): MutableMap<String, List<String>> {
        return mutableMapOf(
            "User-Agent" to mutableListOf(userAgent),
            "X-Goog-Api-Format-Version" to mutableListOf("2"))
    }
}
