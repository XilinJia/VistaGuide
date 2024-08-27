/*
 * Copyright (C) 2016 Christian Schabesberger <chris.schabesberger@mailbox.org>
* Copyright (C) 2024 Xilin Jia <https://github.com/XilinJia>
 * YoutubeStreamInfoItemExtractor.kt is part of Vista Guide.
 *
 * Vista Guide is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Vista Guide is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Vista Guide.  If not, see <https://www.gnu.org/licenses/>.
 */
package ac.mdiq.vista.extractor.services.youtube.extractors

import com.grack.nanojson.JsonObject
import ac.mdiq.vista.extractor.Image
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.localization.DateWrapper
import ac.mdiq.vista.extractor.localization.TimeAgoParser
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.getImagesFromThumbnailsArray
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.getTextFromObject
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.getThumbnailsFromInfoItem
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.getUrlFromNavigationEndpoint
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.isVerified
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.parseDurationString
import ac.mdiq.vista.extractor.services.youtube.linkHandler.YoutubeStreamLinkHandlerFactory
import ac.mdiq.vista.extractor.stream.StreamInfoItemExtractor
import ac.mdiq.vista.extractor.stream.StreamType
import ac.mdiq.vista.extractor.utils.JsonUtils.getArray
import ac.mdiq.vista.extractor.utils.Parser.RegexException
import ac.mdiq.vista.extractor.utils.Parser.matchGroup1
import ac.mdiq.vista.extractor.utils.Utils.mixedNumberWordToLong
import ac.mdiq.vista.extractor.utils.Utils.removeNonDigitCharacters
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.regex.Pattern


/**
 * Creates an extractor of StreamInfoItems from a YouTube page.
 *
 * @param videoInfoItem The JSON page element
 * @param timeAgoParser A parser of the textual dates or `null`.
 */
open class YoutubeStreamInfoItemExtractor(
        private val videoInfo: JsonObject,
        private val timeAgoParser: TimeAgoParser?)
    : StreamInfoItemExtractor {

    private var cachedStreamType: StreamType? = null
    private var isPremiere: Boolean? = null
        get() {
            if (field == null) field = videoInfo.has("upcomingEventData")
            return field
        }

    override fun getStreamType(): StreamType {
        if (cachedStreamType != null) return cachedStreamType!!

        val badges = videoInfo.getArray("badges")
        for (badge in badges) {
            if (badge !is JsonObject) continue

            val badgeRenderer = badge.getObject("metadataBadgeRenderer")
            if (badgeRenderer.getString("style", "") == "BADGE_STYLE_TYPE_LIVE_NOW" || badgeRenderer.getString("label", "") == "LIVE NOW") {
                cachedStreamType = StreamType.LIVE_STREAM
                return cachedStreamType!!
            }
        }

        for (overlay in videoInfo.getArray("thumbnailOverlays")) {
            if (overlay !is JsonObject) continue

            val style = overlay
                .getObject("thumbnailOverlayTimeStatusRenderer")
                .getString("style", "")
            if (style.equals("LIVE", ignoreCase = true)) {
                cachedStreamType = StreamType.LIVE_STREAM
                return cachedStreamType!!
            }
        }

        cachedStreamType = StreamType.VIDEO_STREAM
        return cachedStreamType!!
    }

    @Throws(ParsingException::class)
    override fun isAd(): Boolean {
        return isPremium || name == "[Private video]" || name == "[Deleted video]"
    }

    @get:Throws(ParsingException::class)
    override val url: String
        get() {
            try {
                val videoId = videoInfo.getString("videoId") ?: ""
                return YoutubeStreamLinkHandlerFactory.instance.getUrl(videoId)
            } catch (e: Exception) {
                throw ParsingException("Could not get url", e)
            }
        }

    @get:Throws(ParsingException::class)
    override val name: String
        get() {
            val name = getTextFromObject(videoInfo.getObject("title"))
            if (!name.isNullOrEmpty()) return name
            throw ParsingException("Could not get name")
        }

    @Throws(ParsingException::class)
    override fun getDuration(): Long {
        if (getStreamType() == StreamType.LIVE_STREAM) return -1

        var duration = getTextFromObject(videoInfo.getObject("lengthText"))

        if (duration.isNullOrEmpty()) {
            // Available in playlists for videos
            duration = videoInfo.getString("lengthSeconds")

            if (duration.isNullOrEmpty()) {
                val timeOverlay = videoInfo.getArray("thumbnailOverlays")
                    .stream()
                    .filter { o: Any? -> JsonObject::class.java.isInstance(o) }
                    .map { obj: Any? -> JsonObject::class.java.cast(obj) }
                    .filter { thumbnailOverlay: JsonObject? -> thumbnailOverlay!!.has("thumbnailOverlayTimeStatusRenderer") }
                    .findFirst()
                    .orElse(null)

                if (timeOverlay != null) duration = getTextFromObject(timeOverlay.getObject("thumbnailOverlayTimeStatusRenderer").getObject("text"))
            }

            if (duration.isNullOrEmpty()) {
                // Premieres can be livestreams, so the duration is not available in this
                // case
                if (isPremiere!!) return -1
                throw ParsingException("Could not get duration")
            }
        }
        return parseDurationString(duration).toLong()
    }

    @Throws(ParsingException::class)
    override fun getUploaderName(): String? {
        var name = getTextFromObject(videoInfo.getObject("longBylineText"))
        if (name.isNullOrEmpty()) {
            name = getTextFromObject(videoInfo.getObject("ownerText"))
            if (name.isNullOrEmpty()) {
                name = getTextFromObject(videoInfo.getObject("shortBylineText"))
                if (name.isNullOrEmpty()) throw ParsingException("Could not get uploader name")
            }
        }
        return name
    }

    @Throws(ParsingException::class)
    override fun getUploaderUrl(): String? {
        var url = getUrlFromNavigationEndpoint(videoInfo.getObject("longBylineText").getArray("runs").getObject(0).getObject("navigationEndpoint"))

        if (url.isNullOrEmpty()) {
            url = getUrlFromNavigationEndpoint(videoInfo.getObject("ownerText").getArray("runs").getObject(0).getObject("navigationEndpoint"))

            if (url.isNullOrEmpty()) {
                url = getUrlFromNavigationEndpoint(videoInfo.getObject("shortBylineText").getArray("runs").getObject(0).getObject("navigationEndpoint"))
                if (url.isNullOrEmpty()) throw ParsingException("Could not get uploader url")
            }
        }

        return url
    }


    @Throws(ParsingException::class)
    override fun getUploaderAvatars(): List<Image> {
        if (videoInfo.has("channelThumbnailSupportedRenderers"))
            return getImagesFromThumbnailsArray(getArray(videoInfo, "channelThumbnailSupportedRenderers.channelThumbnailWithLinkRenderer.thumbnail.thumbnails"))

        if (videoInfo.has("channelThumbnail")) return getImagesFromThumbnailsArray(getArray(videoInfo, "channelThumbnail.thumbnails"))

        return listOf()
    }

    @Throws(ParsingException::class)
    override fun isUploaderVerified(): Boolean {
        return isVerified(videoInfo.getArray("ownerBadges"))
    }

    @Throws(ParsingException::class)
    override fun getTextualUploadDate(): String? {
        if (getStreamType() == StreamType.LIVE_STREAM) return null

        if (isPremiere!!) return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").format(dateFromPremiere)

        var publishedTimeText = getTextFromObject(videoInfo.getObject("publishedTimeText"))

        /*
        Returned in playlists, in the form: view count separator upload date
        */
        if (publishedTimeText.isNullOrEmpty() && videoInfo.has("videoInfo"))
            publishedTimeText = videoInfo.getObject("videoInfo").getArray("runs").getObject(2).getString("text")

        return if (publishedTimeText.isNullOrEmpty()) null else publishedTimeText
    }

    @Throws(ParsingException::class)
    override fun getUploadDate(): DateWrapper? {
        if (getStreamType() == StreamType.LIVE_STREAM) return null

        if (isPremiere!!) return DateWrapper(dateFromPremiere)

        val textualUploadDate = getTextualUploadDate()
        if (timeAgoParser != null && !textualUploadDate.isNullOrEmpty()) {
            try {
                return timeAgoParser.parse(textualUploadDate)
            } catch (e: ParsingException) {
                throw ParsingException("Could not get upload date", e)
            }
        }
        return null
    }

    @Throws(ParsingException::class)
    override fun getViewCount(): Long {
        if (isPremium || isPremiere!!) return -1

        // Ignore all exceptions, as the view count can be hidden by creators, and so cannot be
        // found in this case
        val viewCountText = getTextFromObject(videoInfo.getObject("viewCountText"))
        if (!viewCountText.isNullOrEmpty()) {
            try {
                return getViewCountFromViewCountText(viewCountText, false)
            } catch (ignored: Exception) { }
        }

        // Try parsing the real view count from accessibility data, if that's not a running
        // livestream (the view count is returned and not the count of people watching currently
        // the livestream)
        if (getStreamType() != StreamType.LIVE_STREAM) {
            try {
                return viewCountFromAccessibilityData
            } catch (ignored: Exception) {
            }
        }

        // Fallback to a short view count, always used for livestreams (see why above)
        if (videoInfo.has("videoInfo")) {
            // Returned in playlists, in the form: view count separator upload date
            try {
                return getViewCountFromViewCountText(videoInfo.getObject("videoInfo")
                    .getArray("runs")
                    .getObject(0)
                    .getString("text", ""), true)
            } catch (ignored: Exception) { }
        }

        if (videoInfo.has("shortViewCountText")) {
            // Returned everywhere but in playlists, used by the website to show view counts
            try {
                val shortViewCountText = getTextFromObject(videoInfo.getObject("shortViewCountText"))
                if (!shortViewCountText.isNullOrEmpty()) return getViewCountFromViewCountText(shortViewCountText, true)
            } catch (ignored: Exception) { }
        }

        // No view count extracted: return -1, as the view count can be hidden by creators on videos
        return -1
    }

    @Throws(NumberFormatException::class, ParsingException::class)
    private fun getViewCountFromViewCountText(viewCountText: String, isMixedNumber: Boolean): Long {
        // These approaches are language dependent
        if (viewCountText.lowercase(Locale.getDefault()).contains(NO_VIEWS_LOWERCASE)) return 0
        else if (viewCountText.lowercase(Locale.getDefault()).contains("recommended")) return -1
        return if (isMixedNumber) mixedNumberWordToLong(viewCountText)
        else removeNonDigitCharacters(viewCountText).toLong()
    }

    @get:Throws(NumberFormatException::class, RegexException::class)
    private val viewCountFromAccessibilityData: Long
        get() {
            // These approaches are language dependent
            val videoInfoTitleAccessibilityData = videoInfo.getObject("title")
                .getObject("accessibility")
                .getObject("accessibilityData")
                .getString("label", "")

            if (videoInfoTitleAccessibilityData.lowercase(Locale.getDefault()).endsWith(NO_VIEWS_LOWERCASE)) return 0

            return removeNonDigitCharacters(matchGroup1(ACCESSIBILITY_DATA_VIEW_COUNT_REGEX, videoInfoTitleAccessibilityData)).toLong()
        }

    @get:Throws(ParsingException::class)

    override val thumbnails: List<Image>
        get() = getThumbnailsFromInfoItem(videoInfo)

    private val isPremium: Boolean
        get() {
            val badges = videoInfo.getArray("badges")
            for (badge in badges) {
                if ((badge as JsonObject).getObject("metadataBadgeRenderer").getString("label", "") == "Premium") return true
            }
            return false
        }

    @get:Throws(ParsingException::class)
    private val dateFromPremiere: OffsetDateTime
        get() {
            val upcomingEventData = videoInfo.getObject("upcomingEventData")
            val startTime = upcomingEventData.getString("startTime")

            try {
                return OffsetDateTime.ofInstant(Instant.ofEpochSecond(startTime.toLong()), ZoneOffset.UTC)
            } catch (e: Exception) {
                throw ParsingException("Could not parse date from premiere: \"$startTime\"")
            }
        }

    @Throws(ParsingException::class)
    override fun getShortDescription(): String? {
        if (videoInfo.has("detailedMetadataSnippets"))
            return getTextFromObject(videoInfo.getArray("detailedMetadataSnippets").getObject(0).getObject("snippetText"))

        if (videoInfo.has("descriptionSnippet")) return getTextFromObject(videoInfo.getObject("descriptionSnippet"))

        return null
    }

    @Throws(ParsingException::class)
    override fun isShortFormContent(): Boolean {
        try {
            val webPageType = videoInfo.getObject("navigationEndpoint")
                .getObject("commandMetadata").getObject("webCommandMetadata")
                .getString("webPageType")

            var isShort = !webPageType.isNullOrEmpty() && webPageType == "WEB_PAGE_TYPE_SHORTS"

            if (!isShort) isShort = videoInfo.getObject("navigationEndpoint").has("reelWatchEndpoint")

            if (!isShort) {
                val thumbnailTimeOverlay = videoInfo.getArray("thumbnailOverlays")
                    .stream()
                    .filter { o: Any? -> JsonObject::class.java.isInstance(o) }
                    .map { obj: Any? -> JsonObject::class.java.cast(obj) }
                    .filter { thumbnailOverlay: JsonObject ->
                        thumbnailOverlay.has("thumbnailOverlayTimeStatusRenderer")
                    }
                    .map { thumbnailOverlay: JsonObject ->
                        thumbnailOverlay.getObject("thumbnailOverlayTimeStatusRenderer")
                    }
                    .findFirst()
                    .orElse(null)

                if (!thumbnailTimeOverlay.isNullOrEmpty())
                    isShort = (thumbnailTimeOverlay.getString("style", "").equals("SHORTS", ignoreCase = true)
                            || thumbnailTimeOverlay.getObject("icon").getString("iconType", "").lowercase(Locale.getDefault()).contains("shorts"))
            }

            return isShort
        } catch (e: Exception) {
            throw ParsingException("Could not determine if this is short-form content", e)
        }
    }

    companion object {
        private val ACCESSIBILITY_DATA_VIEW_COUNT_REGEX: Pattern = Pattern.compile("([\\d,]+) views$")
        private const val NO_VIEWS_LOWERCASE = "no views"
    }
}
