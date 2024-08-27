package ac.mdiq.vista.extractor.services.youtube

import ac.mdiq.vista.extractor.exceptions.ContentNotAvailableException
import ac.mdiq.vista.extractor.exceptions.ExtractionException
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.localization.ContentCountry
import ac.mdiq.vista.extractor.localization.Localization
import ac.mdiq.vista.extractor.services.youtube.YoutubeChannelHelper.ChannelHeader.HeaderType
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.defaultAlertsCheck
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.getJsonPostResponse
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.getTextFromObject
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.isVerified
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.prepareDesktopJsonBuilder
import com.grack.nanojson.JsonObject
import com.grack.nanojson.JsonWriter
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.util.*


/**
 * Shared functions for extracting YouTube channel pages and tabs.
 */
object YoutubeChannelHelper {

    private const val BROWSE_ENDPOINT: String = "browseEndpoint"
    private const val BROWSE_ID: String = "browseId"
    private const val CAROUSEL_HEADER_RENDERER: String = "carouselHeaderRenderer"
    private const val C4_TABBED_HEADER_RENDERER: String = "c4TabbedHeaderRenderer"
    private const val CONTENT: String = "content"
    private const val CONTENTS: String = "contents"
    private const val HEADER: String = "header"
    private const val PAGE_HEADER_VIEW_MODEL: String = "pageHeaderViewModel"
    private const val TAB_RENDERER: String = "tabRenderer"
    private const val TITLE: String = "title"
    private const val TOPIC_CHANNEL_DETAILS_RENDERER: String = "topicChannelDetailsRenderer"

    /**
     * Take a YouTube channel ID or URL path, resolve it if necessary and return a channel ID.
     *
     * @param idOrPath a YouTube channel ID or URL path
     * @return a YouTube channel ID
     * @throws IOException if a channel resolve request failed
     * @throws ExtractionException if a channel resolve request response could not be parsed or is
     * invalid
     */


    @Throws(ExtractionException::class, IOException::class)
    fun resolveChannelId(idOrPath: String): String {
        val channelId = idOrPath.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        if (channelId[0].startsWith("UC")) return channelId[0]

        // If the URL is not a /channel URL, we need to use the navigation/resolve_url endpoint of
        // the InnerTube API to get the channel id. If this fails or if the URL is not a /channel
        // URL, then no information about the channel associated with this URL was found,
        // so the unresolved url will be returned.
        if (channelId[0] != "channel") {
            val body = JsonWriter.string(
                prepareDesktopJsonBuilder(Localization.DEFAULT, ContentCountry.DEFAULT)
                    .value("url", "https://www.youtube.com/$idOrPath")
                    .done())
                .toByteArray(StandardCharsets.UTF_8)

            val jsonResponse = getJsonPostResponse("navigation/resolve_url", body, Localization.DEFAULT)

            checkIfChannelResponseIsValid(jsonResponse)

            val endpoint = jsonResponse.getObject("endpoint")

            val webPageType = endpoint.getObject("commandMetadata")
                .getObject("webCommandMetadata")
                .getString("webPageType", "")

            val browseEndpoint = endpoint.getObject(BROWSE_ENDPOINT)
            val browseId = browseEndpoint.getString(BROWSE_ID, "")

            if (webPageType.equals("WEB_PAGE_TYPE_BROWSE", ignoreCase = true) || webPageType.equals("WEB_PAGE_TYPE_CHANNEL", ignoreCase = true)
                    && browseId.isNotEmpty()) {
                if (!browseId.startsWith("UC")) throw ExtractionException("Redirected id is not pointing to a channel")
                return browseId
            }
        }

        // return the unresolved URL
        return channelId[1]
    }

    /**
     * Fetch a YouTube channel tab response, using the given channel ID and tab parameters.
     *
     * Redirections to other channels are supported to up to 3 redirects, which could happen for
     * instance for localized channels or for auto-generated ones. For instance, there are three IDs
     * of the auto-generated "Movies and Shows" channel, i.e. `UCuJcl0Ju-gPDoksRjK1ya-w`,
     * `UChBfWrfBXL9wS6tQtgjt_OQ` and `UCok7UTQQEP1Rsctxiv3gwSQ`, and they all redirect
     * to the `UClgRkhTL3_hImCAmdLfDE4g` one.
     *
     *
     * @param channelId    a valid YouTube channel ID
     * @param parameters   the parameters to specify the YouTube channel tab; if invalid ones are
     * specified, YouTube should return the `Home` tab
     * @param localization the [Localization] to use
     * @param country      the [ContentCountry] to use
     * @return a [channel response data][ChannelResponseData]
     * @throws IOException if a channel request failed
     * @throws ExtractionException if a channel request response could not be parsed or is invalid
     */


    @Throws(ExtractionException::class, IOException::class)
    fun getChannelResponse(channelId: String, parameters: String, localization: Localization, country: ContentCountry): ChannelResponseData {
        var id = channelId
        var ajaxJson: JsonObject? = null

        var level = 0
        while (level < 3) {
            val body = JsonWriter.string(prepareDesktopJsonBuilder(localization, country)
                .value(BROWSE_ID, id)
                .value("params", parameters)
                .done())
                .toByteArray(StandardCharsets.UTF_8)

            val jsonResponse = getJsonPostResponse("browse", body, localization)

            checkIfChannelResponseIsValid(jsonResponse)

            val endpoint = jsonResponse.getArray("onResponseReceivedActions")
                .getObject(0)
                .getObject("navigateAction")
                .getObject("endpoint")

            val webPageType = endpoint.getObject("commandMetadata")
                .getObject("webCommandMetadata")
                .getString("webPageType", "")

            val browseId = endpoint.getObject(BROWSE_ENDPOINT)
                .getString(BROWSE_ID, "")

            if (webPageType.equals("WEB_PAGE_TYPE_BROWSE", ignoreCase = true) || webPageType.equals("WEB_PAGE_TYPE_CHANNEL", ignoreCase = true)
                    && browseId.isNotEmpty()) {
                if (!browseId.startsWith("UC")) throw ExtractionException("Redirected id is not pointing to a channel")

                id = browseId
                level++
            } else {
                ajaxJson = jsonResponse
                break
            }
        }

        if (ajaxJson == null) throw ExtractionException("Got no channel response after 3 redirects")

        defaultAlertsCheck(ajaxJson)

        return ChannelResponseData(ajaxJson, id)
    }

    /**
     * Assert that a channel JSON response does not contain an `error` JSON object.
     *
     * @param jsonResponse a channel JSON response
     * @throws ContentNotAvailableException if the channel was not found
     */
    @Throws(ContentNotAvailableException::class)
    private fun checkIfChannelResponseIsValid(jsonResponse: JsonObject) {
        if (!jsonResponse.getObject("error").isNullOrEmpty()) {
            val errorJsonObject = jsonResponse.getObject("error")
            val errorCode = errorJsonObject.getInt("code")
            if (errorCode == 404) throw ContentNotAvailableException("This channel doesn't exist.")
            else throw ContentNotAvailableException("Got error:\"${errorJsonObject.getString("status")}\": ${errorJsonObject.getString("message")}")
        }
    }

    /**
     * Get a channel header if exists.
     * @param channelResponse a full channel JSON response
     * @return a [ChannelHeader] or null
     * if no supported header has been found
     */

    fun getChannelHeader(channelResponse: JsonObject): ChannelHeader? {
        val header = channelResponse.getObject(HEADER)

        return when {
            header.has(C4_TABBED_HEADER_RENDERER) ->
                header.getObject(C4_TABBED_HEADER_RENDERER)?.let { json: JsonObject -> ChannelHeader(json, HeaderType.C4_TABBED) }
            header.has(CAROUSEL_HEADER_RENDERER) -> {
                header.getObject(C4_TABBED_HEADER_RENDERER)
                    ?.getArray(CONTENTS)
                    ?.filterIsInstance<JsonObject>()
                    ?.firstOrNull { it.has(TOPIC_CHANNEL_DETAILS_RENDERER) }
                    ?.getObject(TOPIC_CHANNEL_DETAILS_RENDERER)
                    ?.let { ChannelHeader(it, HeaderType.CAROUSEL) }
            }
            header.has("pageHeaderRenderer") -> header.getObject("pageHeaderRenderer")?.let { json: JsonObject -> ChannelHeader(json, HeaderType.PAGE) }
            header.has("interactiveTabbedHeaderRenderer") ->
                header.getObject("interactiveTabbedHeaderRenderer")?.let { json: JsonObject -> ChannelHeader(json, HeaderType.INTERACTIVE_TABBED) }
            else -> null
        }
    }

    /**
     * Check if a channel is verified by using its header.
     *
     * The header is mandatory, so the verified status of age-restricted channels with a
     * `channelAgeGateRenderer` cannot be checked.
     *
     * @param channelHeader the [ChannelHeader] of a non age-restricted channel
     * @return whether the channel is verified
     */
    fun isChannelVerified(channelHeader: ChannelHeader): Boolean {
        when (channelHeader.headerType) {
            HeaderType.CAROUSEL -> return true
            HeaderType.PAGE -> {
                val pageHeaderViewModel = channelHeader.json.getObject(CONTENT).getObject(PAGE_HEADER_VIEW_MODEL)
                val hasCircleOrMusicIcon = pageHeaderViewModel.getObject(TITLE)
                    .getObject("dynamicTextViewModel")
                    .getObject("text")
                    .getArray("attachmentRuns")
                    .stream()
                    .filter { o: Any? -> JsonObject::class.java.isInstance(o) }
                    .map { obj: Any? -> JsonObject::class.java.cast(obj) }
                    .anyMatch { attachmentRun: JsonObject ->
                        attachmentRun.getObject("element")
                            .getObject("type")
                            .getObject("imageType")
                            .getObject("image")
                            .getArray("sources")
                            .stream()
                            .filter { o: Any? -> JsonObject::class.java.isInstance(o) }
                            .map { obj: Any? -> JsonObject::class.java.cast(obj) }
                            .anyMatch { source: JsonObject ->
                                val imageName = source.getObject("clientResource").getString("imageName")
                                "CHECK_CIRCLE_FILLED" == imageName || "MUSIC_FILLED" == imageName
                            }
                    }
                // If a pageHeaderRenderer has no object in which a check verified may be
                // contained and if it has a contentPreviewImageViewModel, it should mean
                // that the header is coming from a system channel, which we can assume to
                // be verified
                if (!hasCircleOrMusicIcon && pageHeaderViewModel.getObject("image").has("contentPreviewImageViewModel")) return true
                return hasCircleOrMusicIcon
            }
            // If the header has an autoGenerated property, it should mean that the channel has
            // been auto generated by YouTube: we can assume the channel to be verified in this case
            HeaderType.INTERACTIVE_TABBED -> return channelHeader.json.has("autoGenerated")
            else -> return isVerified(channelHeader.json.getArray("badges"))
        }
    }

    /**
     * Get the ID of a channel from its response.
     *
     * For [c4TabbedHeaderRenderer][ChannelHeader.HeaderType.C4_TABBED] and
     * [carouselHeaderRenderer][ChannelHeader.HeaderType.CAROUSEL] channel headers, the ID is
     * get from the header.
     *
     * For other headers or if it cannot be got, the ID from the `channelMetadataRenderer`
     * in the channel response is used.
     *
     * If the ID cannot still be get, the fallback channel ID, if provided, will be used.
     *
     * @param header the channel header
     * @param fallbackChannelId the fallback channel ID, which can be null
     * @return the ID of the channel
     * @throws ParsingException if the channel ID cannot be got from the channel header, the
     * channel response and the fallback channel ID
     */
    @Throws(ParsingException::class)
    fun getChannelId(header: ChannelHeader?, jsonResponse: JsonObject,  fallbackChannelId: String?): String {
        if (header != null) {
            val channelHeader = header
            when (channelHeader.headerType) {
                HeaderType.C4_TABBED -> {
                    val channelId = channelHeader.json.getObject(HEADER)
                        .getObject(C4_TABBED_HEADER_RENDERER)
                        .getString("channelId", "")
                    if (!channelId.isNullOrEmpty()) return channelId
                    val navigationC4TabChannelId = channelHeader.json
                        .getObject("navigationEndpoint")
                        .getObject(BROWSE_ENDPOINT)
                        .getString(BROWSE_ID)
                    if (!navigationC4TabChannelId.isNullOrEmpty()) return navigationC4TabChannelId
                }
                HeaderType.CAROUSEL -> {
                    val navigationCarouselChannelId = channelHeader.json.getObject(HEADER)
                        .getObject(CAROUSEL_HEADER_RENDERER)
                        .getArray(CONTENTS)
                        .stream()
                        .filter { o: Any? -> JsonObject::class.java.isInstance(o) }
                        .map { obj: Any? -> JsonObject::class.java.cast(obj) }
                        .filter { item: JsonObject -> item.has(TOPIC_CHANNEL_DETAILS_RENDERER) }
                        .findFirst()
                        .orElse(JsonObject())
                        .getObject(TOPIC_CHANNEL_DETAILS_RENDERER)
                        .getObject("navigationEndpoint")
                        .getObject(BROWSE_ENDPOINT)
                        .getString(BROWSE_ID)
                    if (!navigationCarouselChannelId.isNullOrEmpty()) return navigationCarouselChannelId
                }
                else -> {}
            }
        }

        val externalChannelId = jsonResponse.getObject("metadata")
            .getObject("channelMetadataRenderer")
            .getString("externalChannelId")
        if (!externalChannelId.isNullOrEmpty()) return externalChannelId

        if (!fallbackChannelId.isNullOrEmpty()) return fallbackChannelId
        else throw ParsingException("Could not get channel ID")
    }

    @Throws(ParsingException::class)
    fun getChannelName(channelHeader: ChannelHeader?, jsonResponse: JsonObject,  channelAgeGateRenderer: JsonObject?): String {
        if (channelAgeGateRenderer != null) {
            val title = channelAgeGateRenderer.getString("channelTitle")
            if (title.isNullOrEmpty()) throw ParsingException("Could not get channel name")
            return title
        }

        val metadataRendererTitle = jsonResponse.getObject("metadata")
            .getObject("channelMetadataRenderer")
            .getString(TITLE)
        if (!metadataRendererTitle.isNullOrEmpty()) return metadataRendererTitle

        return channelHeader?.let { header: ChannelHeader ->
            val channelJson = header.json
            when (header.headerType) {
                HeaderType.PAGE -> return@let channelJson.getObject(CONTENT)
                    .getObject(PAGE_HEADER_VIEW_MODEL)
                    .getObject(TITLE)
                    .getObject("dynamicTextViewModel")
                    .getObject("text")
                    .getString(CONTENT, channelJson.getString("pageTitle"))
                HeaderType.CAROUSEL, HeaderType.INTERACTIVE_TABBED -> return@let getTextFromObject(channelJson.getObject(TITLE))
                HeaderType.C4_TABBED -> return@let channelJson.getString(TITLE)
                else -> return@let channelJson.getString(TITLE)
            }
        } // The channel name from a microformatDataRenderer may be different from the one
            // displayed, especially for auto-generated channels, depending on the language
            // requested for the interface (hl parameter of InnerTube requests' payload)
            ?: jsonResponse.getObject("microformat")
                .getObject("microformatDataRenderer")
                .getString(TITLE)
            ?: throw ParsingException("Could not get channel name")
    }

    fun getChannelAgeGateRenderer(jsonResponse: JsonObject): JsonObject? {
        return jsonResponse.getObject(CONTENTS)
            .getObject("twoColumnBrowseResultsRenderer")
            .getArray("tabs")
            .stream()
            .filter { o: Any? -> JsonObject::class.java.isInstance(o) }
            .map { obj: Any? -> JsonObject::class.java.cast(obj) }
            .flatMap { tab: JsonObject ->
                tab.getObject(TAB_RENDERER)
                    .getObject(CONTENT)
                    .getObject("sectionListRenderer")
                    .getArray(CONTENTS)
                    .stream()
                    .filter { o: Any? -> JsonObject::class.java.isInstance(o) }
                    .map { obj: Any? -> JsonObject::class.java.cast(obj) }
            }
            .filter { content: JsonObject -> content.has("channelAgeGateRenderer") }
            .map { content: JsonObject -> content.getObject("channelAgeGateRenderer") }
            .findFirst()
            .orElse(null)
    }

    /**
     * Response data object for [.getChannelResponse], after any redirection in the allowed redirects count (`3`).
     */
    class ChannelResponseData(
            /**
             * The channel response as a JSON object, after all redirects.
             */
            @JvmField val jsonResponse: JsonObject,
            /**
             * The channel ID after all redirects.
             */
            @JvmField val channelId: String)

    /**
     * A channel header response.
     *
     * This class allows the distinction between a classic header and a carousel one, used for
     * auto-generated ones like the gaming or music topic channels and for big events such as the
     * Coachella music festival, which have a different data structure and do not return the same
     * properties.
     *
     */
    class ChannelHeader(
            /**
             * The channel header JSON response.
             */
            @JvmField val json: JsonObject,
            /**
             * The type of the channel header.
             * See the documentation of the [HeaderType] class for more details.
             */
            @JvmField val headerType: HeaderType) {
        /**
         * Types of supported YouTube channel headers.
         */
        enum class HeaderType {
            /**
             * A `c4TabbedHeaderRenderer` channel header type.
             * This header is returned on the majority of channels and contains the channel's name,
             * its banner and its avatar and its subscriber count in most cases.
             */
            C4_TABBED,

            /**
             * An `interactiveTabbedHeaderRenderer` channel header type.
             * This header is returned for gaming topic channels, and only contains the channel's
             * name, its banner and a poster as its "avatar".
             */
            INTERACTIVE_TABBED,

            /**
             * A `carouselHeaderRenderer` channel header type.
             * This header returns only the channel's name, its avatar and its subscriber count.
             */
            CAROUSEL,

            /**
             * A `pageHeaderRenderer` channel header type.
             * This header returns only the channel's name and its avatar.
             */
            PAGE
        }
    }
}
