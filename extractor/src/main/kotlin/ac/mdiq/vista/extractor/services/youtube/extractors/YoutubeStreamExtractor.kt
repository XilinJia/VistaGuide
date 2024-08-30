/*
 * Created by Christian Schabesberger on 06.08.15.
 *
 * Copyright (C) 2019 Christian Schabesberger <chris.schabesberger@mailbox.org>
* Copyright (C) 2024 Xilin Jia <https://github.com/XilinJia>
 * YoutubeStreamExtractor.kt is part of Vista Guide.
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
package ac.mdiq.vista.extractor.services.youtube.extractors

import com.grack.nanojson.JsonArray
import com.grack.nanojson.JsonObject
import com.grack.nanojson.JsonWriter
import ac.mdiq.vista.extractor.*
import ac.mdiq.vista.extractor.channel.ChannelExtractor.Companion.UNKNOWN_SUBSCRIBER_COUNT
import ac.mdiq.vista.extractor.downloader.Downloader
import ac.mdiq.vista.extractor.exceptions.*
import ac.mdiq.vista.extractor.linkhandler.LinkHandler
import ac.mdiq.vista.extractor.localization.*
import ac.mdiq.vista.extractor.localization.TimeAgoPatternsManager.getTimeAgoParserFor
import ac.mdiq.vista.extractor.services.youtube.ItagItem
import ac.mdiq.vista.extractor.services.youtube.ItagItem.Companion.getItag
import ac.mdiq.vista.extractor.services.youtube.ItagItem.ItagType
import ac.mdiq.vista.extractor.services.youtube.YoutubeDescriptionHelper.attributedDescriptionToHtml
import ac.mdiq.vista.extractor.services.youtube.YoutubeJavaScriptPlayerManager.deobfuscateSignature
import ac.mdiq.vista.extractor.services.youtube.YoutubeJavaScriptPlayerManager.getSignatureTimestamp
import ac.mdiq.vista.extractor.services.youtube.YoutubeJavaScriptPlayerManager.getUrlWithThrottlingParameterDeobfuscated
import ac.mdiq.vista.extractor.services.youtube.YoutubeMetaInfoHelper.getMetaInfo
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.CONTENT_CHECK_OK
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.CPN
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.RACY_CHECK_OK
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.VIDEO_ID
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.createTvHtml5EmbedPlayerBody
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.extractAudioTrackType
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.fixThumbnailUrl
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.generateContentPlaybackNonce
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.generateTParameter
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.getImagesFromThumbnailsArray
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.getJsonAndroidPostResponse
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.getJsonIosPostResponse
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.getJsonPostResponse
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.getTextFromObject
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.getWebPlayerResponse
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.isVerified
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.parseDateFrom
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.prepareAndroidMobileJsonBuilder
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.prepareDesktopJsonBuilder
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.prepareIosMobileJsonBuilder
import ac.mdiq.vista.extractor.services.youtube.linkHandler.YoutubeChannelLinkHandlerFactory
import ac.mdiq.vista.extractor.stream.*
import ac.mdiq.vista.extractor.utils.JsonUtils.getObject
import ac.mdiq.vista.extractor.utils.JsonUtils.getStringListFromJsonArray
import ac.mdiq.vista.extractor.utils.LocaleCompat.forLanguageTag
import ac.mdiq.vista.extractor.utils.Pair
import ac.mdiq.vista.extractor.utils.Parser.compatParseMap
import ac.mdiq.vista.extractor.utils.Utils.mixedNumberWordToLong
import ac.mdiq.vista.extractor.utils.Utils.removeNonDigitCharacters
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.function.Function
import java.util.stream.Collectors

import kotlin.math.ceil

class YoutubeStreamExtractor(service: StreamingService, linkHandler: LinkHandler) : StreamExtractor(service, linkHandler) {
    private var playerResponse: JsonObject? = null
    private var nextResponse: JsonObject? = null

    private var iosStreamingData: JsonObject? = null
    private var androidStreamingData: JsonObject? = null
    private var tvHtml5SimplyEmbedStreamingData: JsonObject? = null


    private var videoPrimaryInfoRenderer: JsonObject? = null
        get() {
            if (field != null) return field!!
            field = getVideoInfoRenderer("videoPrimaryInfoRenderer")
            return field!!
        }

    private var videoSecondaryInfoRenderer: JsonObject? = null
    private var playerMicroFormatRenderer: JsonObject? = null
    private var playerCaptionsTracklistRenderer: JsonObject? = null

//    @Throws(ParsingException::class)
    override var ageLimit: Int = -1
        get() {
            if (field != -1) return field

            val ageRestricted: Boolean = getVideoSecondaryInfoRenderer()
                .getObject("metadataRowContainer")
                .getObject("metadataRowContainerRenderer")
                .getArray("rows")
                .stream() // Only JsonObjects allowed
                .filter { o: Any? -> JsonObject::class.java.isInstance(o) }
                .map { obj: Any? -> JsonObject::class.java.cast(obj) }
                .flatMap { metadataRow: JsonObject ->
                    metadataRow.getObject("metadataRowRenderer")
                        .getArray("contents")
                        .stream() // Only JsonObjects allowed
                        .filter { o: Any? -> JsonObject::class.java.isInstance(o) }
                        .map { obj: Any? -> JsonObject::class.java.cast(obj) }
                }
                .flatMap { content: JsonObject ->
                    content.getArray("runs")
                        .stream() // Only JsonObjects allowed
                        .filter { o: Any? -> JsonObject::class.java.isInstance(o) }
                        .map { obj: Any? -> JsonObject::class.java.cast(obj) }
                }
                .map { run: JsonObject -> run.getString("text", "") }
                .anyMatch { rowText: String -> rowText.contains("Age-restricted") }

            field = if (ageRestricted) 18 else NO_AGE_LIMIT
            return field
        }


    override var streamType: StreamType = StreamType.LIVE_STREAM
        get() {
            assertPageFetched()
            return field
        }

    // We need to store the contentPlaybackNonces because we need to append them to videoplayback
    // URLs (with the cpn parameter).
    // Also because a nonce should be unique, it should be different between clients used, so
    // three different strings are used.
    private var iosCpn: String? = null
    private var androidCpn: String? = null
    private var tvHtml5SimplyEmbedCpn: String? = null

    @Throws(ParsingException::class)
    override fun getName(): String {
        assertPageFetched()
        var title: String?

        // Try to get the video's original title, which is untranslated
        title = playerResponse!!.getObject("videoDetails").getString("title")

        if (title.isNullOrEmpty()) {
            title = getTextFromObject(videoPrimaryInfoRenderer!!.getObject("title"))
            if (title.isNullOrEmpty()) throw ParsingException("Could not get name")
        }
        return title
    }

    @get:Throws(ParsingException::class)
    override val textualUploadDate: String?
        get() {
            if (playerMicroFormatRenderer!!.getString("uploadDate", "").isNotEmpty()) return playerMicroFormatRenderer!!.getString("uploadDate")
            else if (playerMicroFormatRenderer!!.getString("publishDate", "").isNotEmpty()) return playerMicroFormatRenderer!!.getString("publishDate")

            val liveDetails: JsonObject = playerMicroFormatRenderer!!.getObject("liveBroadcastDetails")
            // TODO: this parses English formatted dates only, we need a better approach to
            //  parse the textual date
            // Premiered on 21 Feb 2020
            // Premiered Feb 21, 2020
            // Premiered 20 hours ago
            when {
                // an ended live stream
                liveDetails.getString("endTimestamp", "").isNotEmpty() -> return liveDetails.getString("endTimestamp")
                // a running live stream
                liveDetails.getString("startTimestamp", "").isNotEmpty() -> return liveDetails.getString("startTimestamp")
                // this should never be reached, but a live stream without upload date is valid
                streamType == StreamType.LIVE_STREAM -> return null
                else -> {
                    val videoPrimaryInfoRendererDateText: String? = getTextFromObject(videoPrimaryInfoRenderer!!.getObject("dateText"))

                    if (videoPrimaryInfoRendererDateText != null) {
                        if (videoPrimaryInfoRendererDateText.startsWith("Premiered")) {
                            val time: String = videoPrimaryInfoRendererDateText.substring(13)

                            try { // Premiered 20 hours ago
                                val timeAgoParser: TimeAgoParser? = getTimeAgoParserFor(Localization("en"))
                                val parsedTime: OffsetDateTime = timeAgoParser!!.parse(time).offsetDateTime()
                                return DateTimeFormatter.ISO_LOCAL_DATE.format(parsedTime)
                            } catch (ignored: Exception) { }

                            try { // Premiered Feb 21, 2020
                                val localDate: LocalDate = LocalDate.parse(time, DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.ENGLISH))
                                return DateTimeFormatter.ISO_LOCAL_DATE.format(localDate)
                            } catch (ignored: Exception) { }

                            try { // Premiered on 21 Feb 2020
                                val localDate: LocalDate = LocalDate.parse(time, DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH))
                                return DateTimeFormatter.ISO_LOCAL_DATE.format(localDate)
                            } catch (ignored: Exception) { }
                        }

                        try {
                            // TODO: this parses English formatted dates only, we need a better approach to
                            //  parse the textual date
                            val localDate: LocalDate = LocalDate.parse(videoPrimaryInfoRendererDateText, DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH))
                            return DateTimeFormatter.ISO_LOCAL_DATE.format(localDate)
                        } catch (e: Exception) { throw ParsingException("Could not get upload date", e) }
                    }
                    throw ParsingException("Could not get upload date")
                }
            }
        }

    @get:Throws(ParsingException::class)
    override val uploadDate: DateWrapper?
        get() {
            val textualUploadDate: String? = textualUploadDate
            if (textualUploadDate.isNullOrEmpty()) return null
            return DateWrapper(parseDateFrom(textualUploadDate), true)
        }

    @get:Throws(ParsingException::class)

    override val thumbnails: List<Image>
        get() {
            assertPageFetched()
            try {
                return getImagesFromThumbnailsArray(playerResponse!!.getObject("videoDetails")
                    .getObject("thumbnail")
                    .getArray("thumbnails"))
            } catch (e: Exception) { throw ParsingException("Could not get thumbnails") }
        }

    @get:Throws(ParsingException::class)

    override val description: Description
        get() {
            assertPageFetched()
            // Description with more info on links
            val videoSecondaryInfoRendererDescription: String? = getTextFromObject(getVideoSecondaryInfoRenderer().getObject("description"), true)
            if (!videoSecondaryInfoRendererDescription.isNullOrEmpty())
                return Description(videoSecondaryInfoRendererDescription, Description.HTML)

            val attributedDescription: String? = attributedDescriptionToHtml(getVideoSecondaryInfoRenderer().getObject("attributedDescription"))
            if (!attributedDescription.isNullOrEmpty()) return Description(attributedDescription, Description.HTML)

            var description: String? = playerResponse!!.getObject("videoDetails").getString("shortDescription")
            if (description == null) {
                val descriptionObject: JsonObject = playerMicroFormatRenderer!!.getObject("description")
                description = getTextFromObject(descriptionObject)
            }

            // Raw non-html description
            return Description(description, Description.PLAIN_TEXT)
        }

    @get:Throws(ParsingException::class)
    override val length: Long
        get() {
            assertPageFetched()
            try {
                val duration: String = playerResponse?.getObject("videoDetails")?.getString("lengthSeconds") ?: "0"
                return duration.toLong()
            } catch (e: Exception) {
                return getDurationFromFirstAdaptiveFormat(listOfNotNull(iosStreamingData, androidStreamingData, tvHtml5SimplyEmbedStreamingData)).toLong()
            }
        }

    @Throws(ParsingException::class)
    private fun getDurationFromFirstAdaptiveFormat(streamingDatas: List<JsonObject>): Int {
        for (streamingData: JsonObject in streamingDatas) {
            val adaptiveFormats: JsonArray = streamingData.getArray(ADAPTIVE_FORMATS)
            if (adaptiveFormats.isEmpty()) continue

            val durationMs: String = adaptiveFormats.getObject(0).getString("approxDurationMs")
            try { return Math.round(durationMs.toLong() / 1000f) } catch (ignored: NumberFormatException) { }
        }

        throw ParsingException("Could not get duration")
    }

    /**
     * Attempts to parse (and return) the offset to start playing the video from.
     * @return the offset (in seconds), or 0 if no timestamp is found.
     */
    @get:Throws(ParsingException::class)
    override val timeStamp: Long
        get() {
            val timestamp: Long = getTimestampSeconds("((#|&|\\?)t=\\d*h?\\d*m?\\d+s?)")
            // Regex for timestamp was not found
            if (timestamp == -2L) return 0
            return timestamp
        }

    @get:Throws(ParsingException::class)
    override val viewCount: Long
        get() {
            var views: String? = getTextFromObject(videoPrimaryInfoRenderer!!.getObject("viewCount")
                .getObject("videoViewCountRenderer").getObject("viewCount"))

            if (views.isNullOrEmpty()) {
                views = playerResponse!!.getObject("videoDetails").getString("viewCount")
                if (views.isNullOrEmpty()) throw ParsingException("Could not get view count")
            }
            if (views.lowercase(Locale.getDefault()).contains("no views")) return 0
            return removeNonDigitCharacters(views).toLong()
        }

    @get:Throws(ParsingException::class)
    override val likeCount: Long
        get() {
            assertPageFetched()
            // If ratings are not allowed, there is no like count available
            if (!playerResponse!!.getObject("videoDetails").getBoolean("allowRatings")) return -1L

            val topLevelButtons: JsonArray = videoPrimaryInfoRenderer!!
                .getObject("videoActions")
                .getObject("menuRenderer")
                .getArray("topLevelButtons")

            // A segmentedLikeDislikeButtonRenderer could be returned instead of a
            // segmentedLikeDislikeButtonViewModel, so ignore extraction errors relative to
            // segmentedLikeDislikeButtonViewModel object
            try { return parseLikeCountFromLikeButtonViewModel(topLevelButtons) } catch (ignored: ParsingException) { }

            try { return parseLikeCountFromLikeButtonRenderer(topLevelButtons) } catch (e: ParsingException) { throw ParsingException("Could not get like count", e) }
        }

    @get:Throws(ParsingException::class)

    override val uploaderUrl: String
        get() {
            assertPageFetched()
            // Don't use the id in the videoSecondaryRenderer object to get real id of the uploader
            // The difference between the real id of the channel and the displayed id is especially
            // visible for music channels and autogenerated channels.
            val uploaderId: String = playerResponse!!.getObject("videoDetails").getString("channelId")
            if (uploaderId.isNotEmpty()) return YoutubeChannelLinkHandlerFactory.instance.getUrl("channel/$uploaderId")
            throw ParsingException("Could not get uploader url")
        }

    @get:Throws(ParsingException::class)

    override val uploaderName: String
        get() {
            assertPageFetched()

            // Don't use the name in the videoSecondaryRenderer object to get real name of the uploader
            // The difference between the real name of the channel and the displayed name is especially
            // visible for music channels and autogenerated channels.
            val uploaderName: String = playerResponse!!.getObject("videoDetails").getString("author")
            if (uploaderName.isEmpty()) throw ParsingException("Could not get uploader name")
            return uploaderName
        }

    @get:Throws(ParsingException::class)
    override val isUploaderVerified: Boolean
        get() = isVerified(getVideoSecondaryInfoRenderer().getObject("owner").getObject("videoOwnerRenderer").getArray("badges"))

    @get:Throws(ParsingException::class)
    override val uploaderAvatars: List<Image>
        get() {
            assertPageFetched()
            val imageList: List<Image> = getImagesFromThumbnailsArray(
                getVideoSecondaryInfoRenderer().getObject("owner")
                    .getObject("videoOwnerRenderer")
                    .getObject("thumbnail")
                    .getArray("thumbnails"))

            if (imageList.isEmpty() && ageLimit == NO_AGE_LIMIT) throw ParsingException("Could not get uploader avatars")
            return imageList
        }

    @get:Throws(ParsingException::class)
    override val uploaderSubscriberCount: Long
        get() {
            val videoOwnerRenderer: JsonObject = getObject(videoSecondaryInfoRenderer!!, "owner.videoOwnerRenderer")
            if (!videoOwnerRenderer.has("subscriberCountText")) return UNKNOWN_SUBSCRIBER_COUNT
            try { return mixedNumberWordToLong(getTextFromObject(videoOwnerRenderer.getObject("subscriberCountText"))) } catch (e: NumberFormatException) { throw ParsingException("Could not get uploader subscriber count", e) }
        }

    @get:Throws(ParsingException::class)

    override val dashMpdUrl: String
        get() {
            assertPageFetched()
            // There is no DASH manifest available in the iOS clients and the DASH manifest of the
            // Android client doesn't contain all available streams (mainly the WEBM ones)
            return getManifestUrl("dash", listOf(androidStreamingData, tvHtml5SimplyEmbedStreamingData))
        }

    @get:Throws(ParsingException::class)
    override val hlsUrl: String
        get() {
            assertPageFetched()
            // Return HLS manifest of the iOS client first because on livestreams, the HLS manifest
            // returned has separated audio and video streams
            // Also, on videos, non-iOS clients don't have an HLS manifest URL in their player response
            return getManifestUrl("hls", listOf(iosStreamingData, androidStreamingData, tvHtml5SimplyEmbedStreamingData))
        }

    @get:Throws(ExtractionException::class)
    override val audioStreams: List<AudioStream>
        get() {
            assertPageFetched()
            return getItags(ADAPTIVE_FORMATS, ItagType.AUDIO, audioStreamBuilderHelper, "audio")
        }

    @get:Throws(ExtractionException::class)
    override val videoStreams: List<VideoStream>
        get() {
            assertPageFetched()
            return getItags(FORMATS, ItagType.VIDEO, getVideoStreamBuilderHelper(false), "video")
        }

    @get:Throws(ExtractionException::class)
    override val videoOnlyStreams: List<VideoStream>
        get() {
            assertPageFetched()
            return getItags(ADAPTIVE_FORMATS, ItagType.VIDEO_ONLY,
                getVideoStreamBuilderHelper(true), "video-only")
        }

    @get:Throws(ParsingException::class)
    override val subtitlesDefault: List<SubtitlesStream>
        get() = getSubtitles(MediaFormat.TTML)

    @Throws(ParsingException::class)
    override fun getSubtitles(format: MediaFormat): List<SubtitlesStream> {
        assertPageFetched()

        // We cannot store the subtitles list because the media format may change
        val subtitlesToReturn: MutableList<SubtitlesStream> = ArrayList()
        val captionsArray: JsonArray = playerCaptionsTracklistRenderer!!.getArray("captionTracks")

        // TODO: use this to apply auto translation to different language from a source language
        // final JsonArray autoCaptionsArray = renderer.getArray("translationLanguages");
        for (i in captionsArray.indices) {
            val languageCode: String? = captionsArray.getObject(i).getString("languageCode")
            val baseUrl: String? = captionsArray.getObject(i).getString("baseUrl")
            val vssId: String? = captionsArray.getObject(i).getString("vssId")

            if (languageCode != null && baseUrl != null && vssId != null) {
                val isAutoGenerated: Boolean = vssId.startsWith("a.")
                val cleanUrl: String = baseUrl // Remove preexisting format if exists
                    .replace("&fmt=[^&]*".toRegex(), "") // Remove translation language
                    .replace("&tlang=[^&]*".toRegex(), "")

                subtitlesToReturn.add(SubtitlesStream.Builder()
                    .setContent(cleanUrl + "&fmt=" + format.suffix, true)
                    .setMediaFormat(format)
                    .setLanguageCode(languageCode)
                    .setAutoGenerated(isAutoGenerated)
                    .build())
            }
        }

        return subtitlesToReturn
    }

    private fun setStreamType() {
        streamType = when {
            playerResponse!!.getObject("playabilityStatus").has("liveStreamability") -> StreamType.LIVE_STREAM
            playerResponse!!.getObject("videoDetails").getBoolean("isPostLiveDvr", false) -> StreamType.POST_LIVE_STREAM
            else -> StreamType.VIDEO_STREAM
        }
    }

    @get:Throws(ExtractionException::class)
    override val relatedItems: MultiInfoItemsCollector?
        get() {
            assertPageFetched()
            if (ageLimit != NO_AGE_LIMIT) return null
            try {
                val collector = MultiInfoItemsCollector(serviceId)

                if (nextResponse != null) {
                    val results: JsonArray = nextResponse!!
                        .getObject("contents")
                        .getObject("twoColumnWatchNextResults")
                        .getObject("secondaryResults")
                        .getObject("secondaryResults")
                        .getArray("results")

                    val timeAgoParser: TimeAgoParser = timeAgoParser
                    results.stream()
                        .filter { o: Any? -> JsonObject::class.java.isInstance(o) }
                        .map { obj: Any? -> JsonObject::class.java.cast(obj) }
                        .map { result: JsonObject ->
                            when {
                                result.has("compactVideoRenderer") ->
                                    return@map YoutubeStreamInfoItemExtractor(result.getObject("compactVideoRenderer"), timeAgoParser)
                                result.has("compactRadioRenderer") ->
                                    return@map YoutubeMixOrPlaylistInfoItemExtractor(result.getObject("compactRadioRenderer"))
                                result.has("compactPlaylistRenderer") ->
                                    return@map YoutubeMixOrPlaylistInfoItemExtractor(result.getObject("compactPlaylistRenderer"))
                                else -> null
                            }
                        }
                        .filter { obj: InfoItemExtractor? -> Objects.nonNull(obj) }
                        .forEach { extractor: InfoItemExtractor? -> if (extractor != null) collector.commit(extractor) }
                }
                return collector
            } catch (e: Exception) { throw ParsingException("Could not get related videos", e) }
        }

    /**
     * {@inheritDoc}
     */
    override val errorMessage: String?
        get() {
            return try {
                getTextFromObject(playerResponse!!.getObject("playabilityStatus")
                    .getObject("errorScreen").getObject("playerErrorMessageRenderer")
                    .getObject("reason"))
            } catch (e: NullPointerException) { null /* No error message */ }
        }

    @Throws(IOException::class, ExtractionException::class)
    override fun onFetchPage(downloader: Downloader) {
        val videoId: String = id

        val localization: Localization = extractorLocalization
        val contentCountry: ContentCountry = extractorContentCountry

        val webPlayerResponse: JsonObject = getWebPlayerResponse(localization, contentCountry, videoId)

        if (isPlayerResponseNotValid(webPlayerResponse, videoId)) {
            // Check the playability status, as private and deleted videos and invalid video IDs do
            // not return the ID provided in the player response
            // When the requested video is playable and a different video ID is returned, it has
            // the OK playability status, meaning the ExtractionException after this check will be
            // thrown
            checkPlayabilityStatus(webPlayerResponse, webPlayerResponse.getObject("playabilityStatus"))
            throw ExtractionException("Initial WEB player response is not valid")
        }

        // Save the webPlayerResponse into playerResponse in the case the video cannot be played,
        // so some metadata can be retrieved
        playerResponse = webPlayerResponse

        // Use the player response from the player endpoint of the desktop internal API because
        // there can be restrictions on videos in the embedded player.
        // E.g. if a video is age-restricted, the embedded player's playabilityStatus says that
        // the video cannot be played outside of YouTube, but does not show the original message.
        val playabilityStatus: JsonObject = webPlayerResponse.getObject("playabilityStatus")

        val isAgeRestricted: Boolean = "login_required".equals(playabilityStatus.getString("status"), ignoreCase = true)
                && playabilityStatus.getString("reason", "").contains("age")

        setStreamType()

        if (isAgeRestricted) {
            fetchTvHtml5EmbedJsonPlayer(contentCountry, localization, videoId)

            // If no streams can be fetched in the TVHTML5 simply embed client, the video should be
            // age-restricted, therefore throw an AgeRestrictedContentException explicitly.
            if (tvHtml5SimplyEmbedStreamingData == null) throw AgeRestrictedContentException("This age-restricted video cannot be watched.")

            // Refresh the stream type because the stream type may be not properly known for
            // age-restricted videos
            setStreamType()
        } else {
            checkPlayabilityStatus(webPlayerResponse, playabilityStatus)

            // Fetching successfully the iOS player is mandatory to get streams
            fetchIosMobileJsonPlayer(contentCountry, localization, videoId)

            // Ignore exceptions related to ANDROID client fetch or parsing, as it is not
            // compulsory to play contents
            try { fetchAndroidMobileJsonPlayer(contentCountry, localization, videoId) } catch (ignored: Exception) { }
        }

        // The microformat JSON object of the content is only returned on the WEB client,
        // so we need to store it instead of getting it directly from the playerResponse
        playerMicroFormatRenderer = webPlayerResponse.getObject("microformat").getObject("playerMicroformatRenderer")

        val body: ByteArray = JsonWriter.string(
            prepareDesktopJsonBuilder(localization, contentCountry)
                .value(VIDEO_ID, videoId)
                .value(CONTENT_CHECK_OK, true)
                .value(RACY_CHECK_OK, true)
                .done())
            .toByteArray(StandardCharsets.UTF_8)
        nextResponse = getJsonPostResponse(NEXT, body, localization)
    }

    @Throws(ParsingException::class)
    private fun checkPlayabilityStatus(youtubePlayerResponse: JsonObject, playabilityStatus: JsonObject) {
        var status: String? = playabilityStatus.getString("status")
        if (status == null || status.equals("ok", ignoreCase = true)) return

        // If status exist, and is not "OK", throw the specific exception based on error message
        // or a ContentNotAvailableException with the reason text if it's an unknown reason.
        val newPlayabilityStatus: JsonObject = youtubePlayerResponse.getObject("playabilityStatus")
        status = newPlayabilityStatus.getString("status")
        val reason: String? = newPlayabilityStatus.getString("reason")

        if (status.equals("login_required", ignoreCase = true) && reason == null) {
            val message: String? = newPlayabilityStatus.getArray("messages").getString(0)
            if (message != null && message.contains("private")) throw PrivateContentException("This video is private.")
        }

        if ((status.equals("unplayable", ignoreCase = true) || status.equals("error", ignoreCase = true)) && reason != null) {
            if (reason.contains("Music Premium")) throw YoutubeMusicPremiumContentException()
            if (reason.contains("payment")) throw PaidContentException("This video is a paid video")
            if (reason.contains("members-only")) throw PaidContentException("This video is only available" + " for members of the channel of this video")

            if (reason.contains("unavailable")) {
                val detailedErrorMessage: String? = getTextFromObject(newPlayabilityStatus
                    .getObject("errorScreen")
                    .getObject("playerErrorMessageRenderer")
                    .getObject("subreason"))
                if (detailedErrorMessage != null && detailedErrorMessage.contains("country"))
                    throw GeographicRestrictionException("This video is not available in client's country.")
                else throw ContentNotAvailableException(Objects.requireNonNullElse(detailedErrorMessage, reason))
            }
        }
        throw ContentNotAvailableException("Got error: \"$reason\"")
    }

    /**
     * Fetch the Android Mobile API and assign the streaming data to the androidStreamingData JSON
     * object.
     */
    @Throws(IOException::class, ExtractionException::class)
    private fun fetchAndroidMobileJsonPlayer(contentCountry: ContentCountry, localization: Localization, videoId: String) {
        androidCpn = generateContentPlaybackNonce()
        val mobileBody: ByteArray = JsonWriter.string(
            prepareAndroidMobileJsonBuilder(localization, contentCountry)
                .`object`("playerRequest")
                .value(VIDEO_ID, videoId)
                .end()
                .value("disablePlayerResponse", false)
                .value(VIDEO_ID, videoId)
                .value(CPN, androidCpn)
                .value(CONTENT_CHECK_OK, true)
                .value(RACY_CHECK_OK, true)
                .done())
            .toByteArray(StandardCharsets.UTF_8)

        val androidPlayerResponse: JsonObject = getJsonAndroidPostResponse("reel/reel_item_watch", mobileBody, localization,
            "&t=" + generateTParameter() + "&id=" + videoId + "&\$fields=playerResponse")

        val playerResponseObject: JsonObject = androidPlayerResponse.getObject("playerResponse")
        if (isPlayerResponseNotValid(playerResponseObject, videoId)) return

//        println("fetchAndroidMobileJsonPlayer playerResponseObject: $playerResponseObject")
        val streamingData: JsonObject = playerResponseObject.getObject(STREAMING_DATA)
        if (!streamingData.isEmpty()) {
            androidStreamingData = streamingData
            if (playerCaptionsTracklistRenderer.isNullOrEmpty())
                playerCaptionsTracklistRenderer = playerResponseObject.getObject("captions").getObject("playerCaptionsTracklistRenderer")
        }
    }

    /**
     * Fetch the iOS Mobile API and assign the streaming data to the iosStreamingData JSON
     * object.
     */
    @Throws(IOException::class, ExtractionException::class)
    private fun fetchIosMobileJsonPlayer(contentCountry: ContentCountry, localization: Localization, videoId: String) {
        iosCpn = generateContentPlaybackNonce()
        val mobileBody: ByteArray = JsonWriter.string(
            prepareIosMobileJsonBuilder(localization, contentCountry)
                .value(VIDEO_ID, videoId)
                .value(CPN, iosCpn)
                .value(CONTENT_CHECK_OK, true)
                .value(RACY_CHECK_OK, true)
                .done())
            .toByteArray(StandardCharsets.UTF_8)

        val iosPlayerResponse: JsonObject = getJsonIosPostResponse(PLAYER, mobileBody, localization, ("&t=" + generateTParameter() + "&id=" + videoId))

        if (isPlayerResponseNotValid(iosPlayerResponse, videoId)) throw ExtractionException("IOS player response is not valid")

        val streamingData: JsonObject = iosPlayerResponse.getObject(STREAMING_DATA)
        if (!streamingData.isEmpty()) {
            iosStreamingData = streamingData
            playerCaptionsTracklistRenderer = iosPlayerResponse.getObject("captions").getObject("playerCaptionsTracklistRenderer")
        }
    }

    /**
     * Download the `TVHTML5_SIMPLY_EMBEDDED_PLAYER` JSON player as an embed client to bypass
     * some age-restrictions and assign the streaming data to the `html5StreamingData` JSON
     * object.
     *
     * @param contentCountry the content country to use
     * @param localization   the localization to use
     * @param videoId        the video id
     */
    @Throws(IOException::class, ExtractionException::class)
    private fun fetchTvHtml5EmbedJsonPlayer(contentCountry: ContentCountry, localization: Localization, videoId: String) {
        tvHtml5SimplyEmbedCpn = generateContentPlaybackNonce()

        val tvHtml5EmbedPlayerResponse: JsonObject = getJsonPostResponse(PLAYER,
            createTvHtml5EmbedPlayerBody(localization, contentCountry, videoId, getSignatureTimestamp(videoId)!!, tvHtml5SimplyEmbedCpn!!), localization)

        if (isPlayerResponseNotValid(tvHtml5EmbedPlayerResponse, videoId)) throw ExtractionException("TVHTML5 embed player response is not valid")

        val streamingData: JsonObject = tvHtml5EmbedPlayerResponse.getObject(STREAMING_DATA)
        if (!streamingData.isEmpty()) {
            playerResponse = tvHtml5EmbedPlayerResponse
            tvHtml5SimplyEmbedStreamingData = streamingData
            playerCaptionsTracklistRenderer = playerResponse!!.getObject("captions").getObject("playerCaptionsTracklistRenderer")
        }
    }


    private fun getVideoSecondaryInfoRenderer(): JsonObject {
        if (videoSecondaryInfoRenderer != null) return videoSecondaryInfoRenderer!!
        videoSecondaryInfoRenderer = getVideoInfoRenderer("videoSecondaryInfoRenderer")
        return videoSecondaryInfoRenderer!!
    }


    private fun getVideoInfoRenderer(videoRendererName: String): JsonObject {
        return nextResponse!!.getObject("contents")
            .getObject("twoColumnWatchNextResults")
            .getObject("results")
            .getObject("results")
            .getArray("contents")
            .stream()
            .filter { o: Any? -> JsonObject::class.java.isInstance(o) }
            .map { obj: Any? -> JsonObject::class.java.cast(obj) }
            .filter { content: JsonObject -> content.has(videoRendererName) }
            .map { content: JsonObject -> content.getObject(videoRendererName) }
            .findFirst()
            .orElse(JsonObject())
    }


    @Throws(ParsingException::class)
    private fun <T : Stream> getItags(streamingDataKey: String, itagTypeWanted: ItagType, streamBuilderHelper: Function<ItagInfo, T>,
                                       streamTypeExceptionMessage: String): List<T> {
        try {
            val videoId: String = id
            val streamList: MutableList<T> = ArrayList()

            /*
                Use the iosStreamingData object first because there is no n param and no
                signatureCiphers in streaming URLs of the iOS client
                The androidStreamingData is used as second way as it isn't used on livestreams,
                it doesn't return all available streams, and the Android client extraction is
                more likely to break
                As age-restricted videos are not common, use tvHtml5SimplyEmbedStreamingData
                last, which will be the only one not empty for age-restricted content
                 */
            java.util.stream.Stream.of(Pair(iosStreamingData, iosCpn), Pair(androidStreamingData, androidCpn), Pair(tvHtml5SimplyEmbedStreamingData, tvHtml5SimplyEmbedCpn))
                .flatMap { pair: Pair<JsonObject?, String?> ->
                    getStreamsFromStreamingDataKey(videoId, pair.first, streamingDataKey, itagTypeWanted, pair.second?:"") }
                .map(streamBuilderHelper)
                .forEachOrdered { stream: T -> if (!Stream.containSimilarStream(stream, streamList)) streamList.add(stream) }
            return streamList
        } catch (e: Exception) { throw ParsingException("Could not get $streamTypeExceptionMessage streams", e) }
    }

    /**
     * Get the stream builder helper which will be used to build [AudioStream]s in
     * [.getItags]
     *
     * The `StreamBuilderHelper` will set the following attributes in the
     * [AudioStream]s built:
     *
     *  * the [ItagItem]'s id of the stream as its id;
     *  * [ItagInfo.getContent] and [ItagInfo.getIsUrl] as its content and
     * and as the value of `isUrl`;
     *  * the media format returned by the [ItagItem] as its media format;
     *  * its average bitrate with the value returned by [     ][ItagItem.getAverageBitrate];
     *  * the [ItagItem];
     *  * the [DASH delivery method][DeliveryMethod.DASH], for OTF streams, live streams
     * and ended streams.
     *
     * Note that the [ItagItem] comes from an [ItagInfo] instance.
     *
     * @return a stream builder helper to build [AudioStream]s
     */
    private val audioStreamBuilderHelper: Function<ItagInfo, AudioStream>
        get() {
            return Function<ItagInfo, AudioStream> { itagInfo: ItagInfo ->
                val itagItem: ItagItem = itagInfo.itagItem
                val builder: AudioStream.Builder = AudioStream.Builder()
                    .setId(itagItem.id.toString())
                    .setContent(itagInfo.content, itagInfo.isUrl)
                    .setMediaFormat(itagItem.mediaFormat)
                    .setAverageBitrate(itagItem.averageBitrate)
                    .setAudioTrackId(itagItem.audioTrackId)
                    .setAudioTrackName(itagItem.audioTrackName)
                    .setAudioLocale(itagItem.audioLocale)
                    .setAudioTrackType(itagItem.audioTrackType)
                    .setItagItem(itagItem)

                // For YouTube videos on OTF streams and for all streams of post-live streams
                // and live streams, only the DASH delivery method can be used.
                if (streamType == StreamType.LIVE_STREAM || streamType == StreamType.POST_LIVE_STREAM || !itagInfo.isUrl)
                    builder.setDeliveryMethod(DeliveryMethod.DASH)
                builder.build()
            }
        }

    /**
     * Get the stream builder helper which will be used to build [VideoStream]s in
     * [.getItags]
     *
     * The `StreamBuilderHelper` will set the following attributes in the
     * [VideoStream]s built:
     *
     *  * the [ItagItem]'s id of the stream as its id;
     *  * [ItagInfo.getContent] and [ItagInfo.getIsUrl] as its content and
     * and as the value of `isUrl`;
     *  * the media format returned by the [ItagItem] as its media format;
     *  * whether it is video-only with the `areStreamsVideoOnly` parameter
     *  * the [ItagItem];
     *  * the resolution, by trying to use, in this order:
     *
     *  1. the height returned by the [ItagItem] + `p` + the frame rate if
     * it is more than 30;
     *  1. the default resolution string from the [ItagItem];
     *  1. an empty string.
     *
     *  * the [DASH delivery method][DeliveryMethod.DASH], for OTF streams, live streams
     * and ended streams.
     *
     * Note that the [ItagItem] comes from an [ItagInfo] instance.
     *
     * @param areStreamsVideoOnly whether the stream builder helper will set the video
     * streams as video-only streams
     * @return a stream builder helper to build [VideoStream]s
     */
    private fun getVideoStreamBuilderHelper(areStreamsVideoOnly: Boolean): Function<ItagInfo, VideoStream> {
        return Function<ItagInfo, VideoStream> { itagInfo: ItagInfo ->
            val itagItem: ItagItem = itagInfo.itagItem
            val builder: VideoStream.Builder = VideoStream.Builder()
                .setId(itagItem.id.toString())
                .setContent(itagInfo.content, itagInfo.isUrl)
                .setMediaFormat(itagItem.mediaFormat)
                .setIsVideoOnly(areStreamsVideoOnly)
                .setItagItem(itagItem)

            builder.setResolution(itagItem.resolutionString ?: "")

            // For YouTube videos on OTF streams and for all streams of post-live streams
            // and live streams, only the DASH delivery method can be used.
            if (streamType != StreamType.VIDEO_STREAM || !itagInfo.isUrl) builder.setDeliveryMethod(DeliveryMethod.DASH)
            builder.build()
        }
    }


    private fun getStreamsFromStreamingDataKey(videoId: String, streamingData: JsonObject?, streamingDataKey: String,
                                               itagTypeWanted: ItagType, contentPlaybackNonce: String): java.util.stream.Stream<ItagInfo> {
        if (streamingData == null || !streamingData.has(streamingDataKey)) return java.util.stream.Stream.empty()

//        return streamingData.getArray(streamingDataKey).stream()
//            .filter { o: Any? -> JsonObject::class.java.isInstance(o) }
//            .map { obj: Any? -> JsonObject::class.java.cast(obj) }
//            .map { formatData: JsonObject ->
//                try {
//                    val itagItem: ItagItem = getItag(formatData.getInt("itag"))
//                    if (itagItem.itagType == itagTypeWanted)
//                        return@map buildAndAddItagInfoToList(videoId, formatData, itagItem, itagItem.itagType, contentPlaybackNonce)
//                } catch (ignored: ExtractionException) {
//                    // If the itag is not supported, the n parameter of HTML5 clients cannot be
//                    // decoded or buildAndAddItagInfoToList fails, we end up here
//                }
//                null
//            }
//            .filter { obj: ItagInfo? -> Objects.nonNull(obj) }
//            .map {it!!}

        return streamingData.getArray(streamingDataKey).stream()
            .filter { it is JsonObject }  // Filter elements to ensure they're JsonObject
            .map { it as JsonObject }      // Cast elements to JsonObject
            .map { formatData: JsonObject ->
                try {
//                    println("getStreamsFromStreamingDataKey formatData: $formatData")
                    val itagItem: ItagItem = getItag(formatData.getInt("itag"))
                    if (itagItem.itagType == itagTypeWanted)
                        return@map buildAndAddItagInfoToList(videoId, formatData, itagItem, itagItem.itagType, contentPlaybackNonce)
                } catch (ignored: ExtractionException) { }
                null
            }
            .filter { it != null }  // Filter out null values
            .map { it!! }
    }

    @Throws(ExtractionException::class)
    private fun buildAndAddItagInfoToList(videoId: String, formatData: JsonObject, itagItem: ItagItem,
                                          itagType: ItagType, contentPlaybackNonce: String): ItagInfo {
        var streamUrl: String?
        if (formatData.has("url")) streamUrl = formatData.getString("url")
        else {
            // This url has an obfuscated signature
            val cipherString: String = formatData.getString(CIPHER, formatData.getString(SIGNATURE_CIPHER))
            val cipher: Map<String, String> = compatParseMap(cipherString)
            val signature: String = deobfuscateSignature(videoId, cipher.getOrDefault("s", ""))
            streamUrl = cipher["url"] + "&" + cipher["sp"] + "=" + signature
        }

        // Add the content playback nonce to the stream URL
        streamUrl += "&$CPN=$contentPlaybackNonce"

        // Decode the n parameter if it is present
        // If it cannot be decoded, the stream cannot be used as streaming URLs return HTTP 403
        // responses if it has not the right value
        // Exceptions thrown by
        // YoutubeJavaScriptPlayerManager.getUrlWithThrottlingParameterDeobfuscated are so
        // propagated to the parent which ignores streams in this case
        streamUrl = getUrlWithThrottlingParameterDeobfuscated(videoId, streamUrl!!)

        val initRange: JsonObject = formatData.getObject("initRange")
        val indexRange: JsonObject = formatData.getObject("indexRange")
        val mimeType: String = formatData.getString("mimeType", "")
        val codec: String = if (mimeType.contains("codecs")) mimeType.split("\"".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1] else ""

        itagItem.bitrate = (formatData.getInt("bitrate"))
        itagItem.width = (formatData.getInt("width"))
        itagItem.height = (formatData.getInt("height"))
        itagItem.initStart = (initRange.getString("start", "-1").toInt())
        itagItem.initEnd = (initRange.getString("end", "-1").toInt())
        itagItem.indexStart = (indexRange.getString("start", "-1").toInt())
        itagItem.indexEnd = (indexRange.getString("end", "-1").toInt())
        itagItem.quality = (formatData.getString("quality"))
        itagItem.codec = (codec)

        if (streamType == StreamType.LIVE_STREAM || streamType == StreamType.POST_LIVE_STREAM)
            itagItem.setTargetDurationSec(formatData.getInt("targetDurationSec"))

        if (itagType == ItagType.VIDEO || itagType == ItagType.VIDEO_ONLY) itagItem.setFps(formatData.getInt("fps"))
        else if (itagType == ItagType.AUDIO) {
            // YouTube return the audio sample rate as a string
            itagItem.setSampleRate(formatData.getString("audioSampleRate").toInt())
            // Most audio streams have two audio channels, so use this value if the real
            // count cannot be extracted
            // Doing this prevents an exception when generating the
            // AudioChannelConfiguration element of DASH manifests of audio streams in
            // YoutubeDashManifestCreatorUtils
            itagItem.setAudioChannels(formatData.getInt("audioChannels", 2))

            val aObj = formatData.getObject("audioTrack")
//            println("buildAndAddItagInfoToList aObj: $aObj")
            val audioTrackId: String? = aObj?.getString("id")
            if (!audioTrackId.isNullOrEmpty()) {
                itagItem.audioTrackId = audioTrackId
                val audioTrackIdLastLocaleCharacter: Int = audioTrackId.indexOf(".")
                // Audio tracks IDs are in the form LANGUAGE_CODE.TRACK_NUMBER
                if (audioTrackIdLastLocaleCharacter != -1)
                    forLanguageTag(audioTrackId.substring(0, audioTrackIdLastLocaleCharacter))?.let { locale -> itagItem.audioLocale = locale }
                itagItem.audioTrackType = (extractAudioTrackType(streamUrl))
            }

            itagItem.audioTrackName = aObj?.getString("displayName")
        }

        // YouTube return the content length and the approximate duration as strings
        itagItem.setContentLength(formatData.getString("contentLength", ItagItem.CONTENT_LENGTH_UNKNOWN.toString()).toLong())
        itagItem.setApproxDurationMs(formatData.getString("approxDurationMs", ItagItem.APPROX_DURATION_MS_UNKNOWN.toString()).toLong())

        val itagInfo = ItagInfo(streamUrl, itagItem)

        if (streamType == StreamType.VIDEO_STREAM)
            itagInfo.isUrl = (!formatData.getString("type", "").equals("FORMAT_STREAM_TYPE_OTF", ignoreCase = true))
        // We are currently not able to generate DASH manifests for running
        // livestreams, so because of the requirements of StreamInfo
        // objects, return these streams as DASH URL streams (even if they
        // are not playable).
        // Ended livestreams are returned as non URL streams
        else itagInfo.isUrl = (streamType != StreamType.POST_LIVE_STREAM)
        return itagInfo
    }

    @get:Throws(ExtractionException::class)
    override val frames: List<Frameset>
        get() {
            try {
                val storyboards: JsonObject = playerResponse!!.getObject("storyboards")
                val storyboardsRenderer: JsonObject? = storyboards.getObject(
                    if (storyboards.has("playerLiveStoryboardSpecRenderer")) "playerLiveStoryboardSpecRenderer" else "playerStoryboardSpecRenderer")

                if (storyboardsRenderer == null) return emptyList()

                val storyboardsRendererSpec: String = storyboardsRenderer.getString("spec") ?: return emptyList()

                val spec: Array<String> = storyboardsRendererSpec.split("\\|".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val url: String = spec[0]
                val result: MutableList<Frameset> = ArrayList(spec.size - 1)

                for (i in 1 until spec.size) {
                    val parts: Array<String> = spec[i].split("#".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    if (parts.size != 8 || parts[5].toInt() == 0) continue
                    val totalCount: Int = parts[2].toInt()
                    val framesPerPageX: Int = parts[3].toInt()
                    val framesPerPageY: Int = parts[4].toInt()
                    val baseUrl: String = url.replace("\$L", (i - 1).toString()).replace("\$N",
                        parts[6]) + "&sigh=" + parts[7]
                    val urls: List<String>
                    if (baseUrl.contains("\$M")) {
                        val totalPages: Int = ceil(totalCount / (framesPerPageX * framesPerPageY).toDouble()).toInt()
                        urls = ArrayList(totalPages)
                        for (j in 0 until totalPages) {
                            urls.add(baseUrl.replace("\$M", j.toString()))
                        }
                    } else urls = listOf(baseUrl)

                    result.add(Frameset(urls, parts[0].toInt(), parts[1].toInt(), totalCount, parts[5].toInt(), framesPerPageX, framesPerPageY))
                }
                return result
            } catch (e: Exception) {
                throw ExtractionException("Could not get frames", e)
            }
        }

    override val privacy: Privacy
        get() = if (playerMicroFormatRenderer!!.getBoolean("isUnlisted")) Privacy.UNLISTED else Privacy.PUBLIC

    override val category: String
        get() = playerMicroFormatRenderer!!.getString("category", "")

    @get:Throws(ParsingException::class)
    override val licence: String
        get() {
            val metadataRowRenderer: JsonObject = getVideoSecondaryInfoRenderer()
                .getObject("metadataRowContainer")
                .getObject("metadataRowContainerRenderer")
                .getArray("rows")
                .getObject(0)
                .getObject("metadataRowRenderer")

            val contents: JsonArray = metadataRowRenderer.getArray("contents")
            val license: String? = getTextFromObject(contents.getObject(0))
            return if (license != null && "Licence" == getTextFromObject(metadataRowRenderer.getObject("title"))) license else "YouTube licence"
        }

    override val languageInfo: Locale?
        get() =  null

    override val tags: List<String>
        get() = getStringListFromJsonArray(playerResponse!!.getObject("videoDetails").getArray("keywords"))

    @get:Throws(ParsingException::class)
    override val streamSegments: List<StreamSegment>
        get() {
            if (!nextResponse!!.has("engagementPanels")) return emptyList()

            val segmentsArray: JsonArray? = nextResponse!!.getArray("engagementPanels")
                .stream() // Check if object is a JsonObject
                .filter { o: Any? -> JsonObject::class.java.isInstance(o) }
                .map { obj: Any? -> JsonObject::class.java.cast(obj) }  // Check if the panel is the correct one
                .filter { panel: JsonObject ->
                    "engagement-panel-macro-markers-description-chapters" == panel
                        .getObject("engagementPanelSectionListRenderer")
                        .getString("panelIdentifier")
                }  // Extract the data
                .map { panel: JsonObject ->
                    panel.getObject("engagementPanelSectionListRenderer")
                        .getObject("content")
                        .getObject("macroMarkersListRenderer")
                        .getArray("contents")
                }
                .findFirst()
                .orElse(null)

            // If no data was found exit
            if (segmentsArray == null) return emptyList()

            val duration: Long = length
            val segments: MutableList<StreamSegment> = ArrayList()
            for (segmentJson: JsonObject in segmentsArray.stream()
                .filter { o: Any? -> JsonObject::class.java.isInstance(o) }
                .map { obj: Any? -> JsonObject::class.java.cast(obj) }
                .map { `object`: JsonObject -> `object`.getObject("macroMarkersListItemRenderer") }
                .collect(Collectors.toList())) {
                val startTimeSeconds: Int = segmentJson.getObject("onTap").getObject("watchEndpoint").getInt("startTimeSeconds", -1)

                if (startTimeSeconds == -1) throw ParsingException("Could not get stream segment start time.")
                if (startTimeSeconds > duration) break

                val title: String? = getTextFromObject(segmentJson.getObject("title"))
                if (title.isNullOrEmpty()) throw ParsingException("Could not get stream segment title.")

                val segment = StreamSegment(title, startTimeSeconds)
                segment.url = ("$url?t=$startTimeSeconds")
                if (segmentJson.has("thumbnail")) {
                    val previewsArray: JsonArray = segmentJson.getObject("thumbnail").getArray("thumbnails")
                    if (!previewsArray.isEmpty()) {
                        // Assume that the thumbnail with the highest resolution is at the last position
                        val url: String = previewsArray.getObject(previewsArray.size - 1).getString("url")
                        segment.previewUrl = (fixThumbnailUrl(url))
                    }
                }
                segments.add(segment)
            }
            return segments
        }

    @get:Throws(ParsingException::class)
    override val metaInfo: List<MetaInfo>
        get() {
            if(nextResponse == null) return listOf()
            return getMetaInfo(nextResponse!!
                .getObject("contents")
                .getObject("twoColumnWatchNextResults")
                .getObject("results")
                .getObject("results")
                .getArray("contents"))
        }

    companion object {
        @Throws(ParsingException::class)
        private fun parseLikeCountFromLikeButtonRenderer(topLevelButtons: JsonArray): Long {
            var likesString: String? = null
            val likeToggleButtonRenderer: JsonObject? = topLevelButtons.stream()
                .filter { o: Any? -> JsonObject::class.java.isInstance(o) }
                .map { obj: Any? -> JsonObject::class.java.cast(obj) }
                .map { button: JsonObject ->
                    button.getObject("segmentedLikeDislikeButtonRenderer")
                        .getObject("likeButton")
                        .getObject("toggleButtonRenderer")
                }
                .filter { toggleButtonRenderer: JsonObject? -> !toggleButtonRenderer.isNullOrEmpty() }
                .findFirst()
                .orElse(null)

            if (likeToggleButtonRenderer != null) {
                // Use one of the accessibility strings available (this one has the same path as the
                // one used for comments' like count extraction)
                likesString = likeToggleButtonRenderer.getObject("accessibilityData")
                    .getObject("accessibilityData")
                    .getString("label")

                // Use the other accessibility string available which contains the exact like count
                if (likesString == null) likesString = likeToggleButtonRenderer.getObject("accessibility").getString("label")

                // Last method: use the defaultText's accessibility data, which contains the exact like
                // count too, except when it is equal to 0, where a localized string is returned instead
                if (likesString == null)
                    likesString = likeToggleButtonRenderer.getObject("defaultText")
                        .getObject("accessibility")
                        .getObject("accessibilityData")
                        .getString("label")


                // This check only works with English localizations!
                if (likesString != null && likesString.lowercase(Locale.getDefault()).contains("no likes")) return 0
            }

            // If ratings are allowed and the likes string is null, it means that we couldn't extract
            // the full like count from accessibility data
            if (likesString == null) throw ParsingException("Could not get like count from accessibility data")

            try { return removeNonDigitCharacters(likesString).toLong() } catch (e: NumberFormatException) { throw ParsingException("Could not parse \"$likesString\" as a long", e) }
        }

        @Throws(ParsingException::class)
        private fun parseLikeCountFromLikeButtonViewModel(topLevelButtons: JsonArray): Long {
            // Try first with the current video actions buttons data structure
            val likeToggleButtonViewModel: JsonObject? = topLevelButtons.stream()
                .filter { o: Any? -> JsonObject::class.java.isInstance(o) }
                .map { obj: Any? -> JsonObject::class.java.cast(obj) }
                .map { button: JsonObject ->
                    button.getObject("segmentedLikeDislikeButtonViewModel")
                        .getObject("likeButtonViewModel")
                        .getObject("likeButtonViewModel")
                        .getObject("toggleButtonViewModel")
                        .getObject("toggleButtonViewModel")
                        .getObject("defaultButtonViewModel")
                        .getObject("buttonViewModel")
                }
                .filter { buttonViewModel: JsonObject? -> !buttonViewModel.isNullOrEmpty() }
                .findFirst()
                .orElse(null)

            if (likeToggleButtonViewModel == null) throw ParsingException("Could not find buttonViewModel object")

            val accessibilityText: String = likeToggleButtonViewModel.getString("accessibilityText")
                ?: throw ParsingException("Could not find buttonViewModel's accessibilityText string")

            // The like count is always returned as a number in this element, even for videos with no
            // likes
            try { return removeNonDigitCharacters(accessibilityText).toLong() } catch (e: NumberFormatException) { throw ParsingException("Could not parse \"$accessibilityText\" as a long", e) }
        }

        private fun getManifestUrl(manifestType: String, streamingDataObjects: List<JsonObject?>): String {
            val manifestKey: String = manifestType + "ManifestUrl"
            return streamingDataObjects.stream()
                .filter { obj: JsonObject? -> Objects.nonNull(obj) }
                .map { streamingDataObject: JsonObject? -> streamingDataObject!!.getString(manifestKey) }
                .filter { obj: String? -> Objects.nonNull(obj) }
                .findFirst()
                .orElse("")
        }

        private const val FORMATS: String = "formats"
        private const val ADAPTIVE_FORMATS: String = "adaptiveFormats"
        private const val STREAMING_DATA: String = "streamingData"
        private const val PLAYER: String = "player"
        private const val NEXT: String = "next"
        private const val SIGNATURE_CIPHER: String = "signatureCipher"
        private const val CIPHER: String = "cipher"

        /**
         * Checks whether a player response is invalid.
         *
         * If YouTube detect that requests come from a third party client, they may replace the real
         * player response by another one of a video saying that this content is not available on this
         * app and to watch it on the latest version of YouTube. This behavior has been observed on the
         * `ANDROID` client, see
         * [
 * https://github.com/XilinJia/Vista/issues/8713](https://github.com/XilinJia/Vista/issues/8713).
         *
         * YouTube may also sometimes for currently unknown reasons rate-limit an IP, and replace the
         * real one by a player response with a video that says that the requested video is
         * unavailable. This behaviour has been observed in Piped on the InnerTube clients used by the
         * extractor (`ANDROID` and `WEB` clients) which should apply for all clients, see
         * [
 * https://github.com/TeamPiped/Piped/issues/2487](https://github.com/TeamPiped/Piped/issues/2487).
         *
         * We can detect this by checking whether the video ID of the player response returned is the
         * same as the one requested by the extractor.
         *
         * @param playerResponse a player response from any client
         * @param videoId        the video ID of the content requested
         * @return whether the video ID of the player response is not equal to the one requested
         */
        private fun isPlayerResponseNotValid(playerResponse: JsonObject, videoId: String): Boolean {
            return videoId != playerResponse.getObject("videoDetails").getString("videoId")
        }
    }
}
