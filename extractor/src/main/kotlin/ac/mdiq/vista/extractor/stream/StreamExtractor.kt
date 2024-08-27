/*
 * Created by Christian Schabesberger on 10.08.18.
 *
 * Copyright (C) 2016 Christian Schabesberger <chris.schabesberger@mailbox.org>
* Copyright (C) 2024 Xilin Jia <https://github.com/XilinJia>
 * StreamExtractor.kt is part of Vista Guide.
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
package ac.mdiq.vista.extractor.stream

import ac.mdiq.vista.extractor.*
import ac.mdiq.vista.extractor.exceptions.ExtractionException
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.linkhandler.LinkHandler
import ac.mdiq.vista.extractor.localization.DateWrapper
import ac.mdiq.vista.extractor.utils.Parser.RegexException
import ac.mdiq.vista.extractor.utils.Parser.matchGroup1
import java.io.IOException
import java.util.*

/**
 * Scrapes information from a video/audio streaming service (eg, YouTube).
 */
abstract class StreamExtractor(service: StreamingService, linkHandler: LinkHandler) : Extractor(service, linkHandler) {

    /**
     * The original textual date provided by the service. Should be used as a fallback if
     * [.getUploadDate] isn't provided by the service, or it fails for some reason.
     * If the stream is a live stream, `null` should be returned.
     *
     * @return The original textual date provided by the service, or `null`.
     * @throws ParsingException if there is an error in the extraction
     * @see .getUploadDate
     */
    @get:Throws(ParsingException::class)
    open val textualUploadDate: String?
        get() = null

    /**
     * A more general `Calendar` instance set to the date provided by the service.<br></br>
     * Implementations usually will just parse the date returned from the [ ][.getTextualUploadDate].
     * If the stream is a live stream, `null` should be returned.
     * @return The date this item was uploaded, or `null`.
     * @throws ParsingException if there is an error in the extraction
     * or the extracted date couldn't be parsed.
     * @see .getTextualUploadDate
     */
    @get:Throws(ParsingException::class)
    open val uploadDate: DateWrapper?
        get() = null

    @get:Throws(ParsingException::class)
    abstract val thumbnails: List<Image>

    /**
     * This is the stream description.
     *
     * @return The description of the stream/video or [Description.EMPTY_DESCRIPTION] if the
     * description is empty.
     */
    @get:Throws(ParsingException::class)
    open val description: Description
        get() = Description.EMPTY_DESCRIPTION

    /**
     * This should return the length of a video in seconds.
     *
     * @return The length of the stream in seconds or 0 when it has no length (e.g. a livestream).
     */
    @get:Throws(ParsingException::class)
    open val length: Long
        get() = 0

    /**
     * If the url you are currently handling contains a time stamp/seek, you can return the
     * position it represents here.
     * If the url has no time stamp simply return zero.
     *
     * @return the timestamp in seconds or 0 when there is no timestamp
     */
    @get:Throws(ParsingException::class)
    open val timeStamp: Long
        get() = 0

    /**
     * The count of how many people have watched the video/listened to the audio stream.
     * If the current stream has no view count or its not available simply return -1
     *
     * @return amount of views or -1 if not available.
     */
    @get:Throws(ParsingException::class)
    open val viewCount: Long
        get() = -1

    /**
     * The amount of likes a video/audio stream got.
     * If the current stream has no likes or its not available simply return -1
     *
     * @return the amount of likes the stream got or -1 if not available.
     */
    @get:Throws(ParsingException::class)
    open val likeCount: Long
        get() = -1

    /**
     * The amount of dislikes a video/audio stream got.
     * If the current stream has no dislikes or its not available simply return -1
     *
     * @return the amount of likes the stream got or -1 if not available.
     */
    @get:Throws(ParsingException::class)
    open val dislikeCount: Long
        get() = -1

    @get:Throws(ParsingException::class)
    abstract val uploaderUrl: String

    @get:Throws(ParsingException::class)
    abstract val uploaderName: String

    /**
     * Whether the uploader has been verified by the service's provider.
     * If there is no verification implemented, return `false`.
     *
     * @return whether the uploader has been verified by the service's provider
     */
    @get:Throws(ParsingException::class)
    open val isUploaderVerified: Boolean
        get() = false

    /**
     * The image files/profile pictures/avatars of the creator/uploader of the stream.
     *
     * If they are not available in the stream on specific cases, you must return an empty list for
     * these ones, like it is made by default.
     *
     * @return the avatars of the sub-channel of the stream or an empty list (default)
     */
    @get:Throws(ParsingException::class)
    open val uploaderAvatars: List<Image>
        get() = listOf()

    /**
     * The Url to the page of the sub-channel of the stream. This must not be a homepage,
     * but the page offered by the service the extractor handles. This url will be handled by the
     * [ChannelExtractor], so be sure to implement that one before you return a value here,
     * otherwise Vista will crash if one selects this url.
     *
     * @return the url to the page of the sub-channel of the stream or an empty String
     */
    @get:Throws(ParsingException::class)
    open val subChannelUrl: String
        get() = ""

    /**
     * The name of the sub-channel of the stream.
     * If the name is not available you can simply return an empty string.
     *
     * @return the name of the sub-channel of the stream or an empty String
     */
    @get:Throws(ParsingException::class)
    open val subChannelName: String
        get() = ""

    /**
     * The avatars of the sub-channel of the stream.
     * If they are not available in the stream on specific cases, you must return an empty list for
     * these ones, like it is made by default
     * If the concept of sub-channels doesn't apply to the stream's service, keep the default
     * implementation.
     * @return the avatars of the sub-channel of the stream or an empty list (default)
     */
    @get:Throws(ParsingException::class)
    open val subChannelAvatars: List<Image>
        get() = listOf()

    /**
     * Get the dash mpd url. If you don't know what a dash MPD is you can read about it
     * [here](https://www.brendanlong.com/the-structure-of-an-mpeg-dash-mpd.html).
     *
     * @return the url as a string or an empty string or an empty string if not available
     * @throws ParsingException if an error occurs while reading
     */
    @get:Throws(ParsingException::class)
    open val dashMpdUrl: String
        get() = ""

    /**
     * I am not sure if this is in use, and how this is used. However the frontend is missing
     * support for HLS streams. Prove me if I am wrong. Please open an
     * [issue](https://github.com/XilinJia/VistaGuide/issues),
     * or fix this description if you know whats up with this.
     *
     * @return The Url to the hls stream or an empty string if not available.
     */
    @get:Throws(ParsingException::class)
    open val hlsUrl: String
        get() = ""

    @get:Throws(IOException::class, ExtractionException::class)
    abstract val audioStreams: List<AudioStream>

    @get:Throws(IOException::class, ExtractionException::class)
    abstract val videoStreams: List<VideoStream>

    @get:Throws(IOException::class, ExtractionException::class)
    abstract val videoOnlyStreams: List<VideoStream>

    /**
     * This will return a list of available [SubtitlesStream]s.
     * If no subtitles are available an empty list can be returned.
     *
     * @return a list of available subtitles or an empty list
     */
    @get:Throws(IOException::class, ExtractionException::class)
    open val subtitlesDefault: List<SubtitlesStream>
        get() = listOf()

    /**
     * This will return a list of available [SubtitlesStream]s given by a specific type.
     * If no subtitles in that specific format are available an empty list can be returned.
     *
     * @param format the media format by which the subtitles should be filtered
     * @return a list of available subtitles or an empty list
     */
    @Throws(IOException::class, ExtractionException::class)
    open fun getSubtitles(format: MediaFormat): List<SubtitlesStream> {
        return emptyList()
    }

    @get:Throws(ParsingException::class)
    abstract val streamType: StreamType

    /**
     * Should return a list of streams related to the current handled. Many services show suggested
     * streams. If you don't like suggested streams you should implement them anyway since they can
     * be disabled by the user later in the frontend. The first related stream might be what was
     * previously known as a next stream.
     * If related streams aren't available simply return `null`.
     *
     * @return a list of InfoItems showing the related videos/streams
     */
    @get:Throws(IOException::class, ExtractionException::class)
    open val relatedItems: InfoItemsCollector<out InfoItem, out InfoItemExtractor>?
        get() = null

    /**
     * @return The result of [.getRelatedItems] if it is a
     * [StreamInfoItemsCollector], `null` otherwise
     */
    @get:Throws(IOException::class, ExtractionException::class)
    @get:Deprecated("Use {@link #getRelatedItems()}. May be removed in a future version.")
    val relatedStreams: StreamInfoItemsCollector?
        get() {
            val collector = relatedItems
            return if (collector is StreamInfoItemsCollector) collector else { null }
        }

    /**
     * Should return a list of Frameset object that contains preview of stream frames
     *
     * @return list of preview frames or empty list if frames preview is not supported or not found
     * for specified stream
     */
    @get:Throws(ExtractionException::class)
    open val frames: List<Frameset>
        get() = emptyList()

    /**
     * Should analyse the webpage's document and extracts any error message there might be.
     * @return Error message; `null` if there is no error message.
     */
    open val errorMessage: String?
        get() = null

    /**
     * The host of the stream (Eg. peertube.cpy.re).
     * If the host is not available, or if the service doesn't use
     * a federated system, but a centralised system,
     * you can simply return an empty string.
     *
     * @return the host of the stream or an empty string.
     */
    @get:Throws(ParsingException::class)
    open val host: String
        get() = ""

    /**
     * The privacy of the stream (Eg. Public, Private, Unlisted…).
     * @return the privacy of the stream.
     */
    @get:Throws(ParsingException::class)
    open val privacy: Privacy?
        get() = Privacy.PUBLIC

    /**
     * The name of the category of the stream.
     * If the category is not available you can simply return an empty string.
     *
     * @return the category of the stream or an empty string.
     */
    @get:Throws(ParsingException::class)
    open val category: String
        get() = ""

    /**
     * The name of the licence of the stream.
     * If the licence is not available you can simply return an empty string.
     * @return the licence of the stream or an empty String.
     */
    @get:Throws(ParsingException::class)
    open val licence: String
        get() = ""

    /**
     * The locale language of the stream.
     * If the language is not available you can simply return null.
     * If the language is provided by a language code, you can return
     * new Locale(language_code);
     *
     * @return the locale language of the stream or `null`.
     */
    @get:Throws(ParsingException::class)
    open val languageInfo: Locale?
        get() = null

    /**
     * The list of tags of the stream.
     * If the tag list is not available you can simply return an empty list.
     *
     * @return the list of tags of the stream or Collections.emptyList().
     */
    @get:Throws(ParsingException::class)
    open val tags: List<String>
        get() = emptyList()

    /**
     * The support information of the stream.
     * see: https://framatube.org/videos/watch/ee408ec8-07cd-4e35-b884-fb681a4b9d37
     * (support button).
     * If the support information are not available,
     * you can simply return an empty String.
     * @return the support information of the stream or an empty string.
     */
    @get:Throws(ParsingException::class)
    open val supportInfo: String
        get() = ""

    /**
     * The list of stream segments by timestamps for the stream.
     * If the segment list is not available you can simply return an empty list.
     * @return The list of segments of the stream or an empty list.
     */
    @get:Throws(ParsingException::class)
    open val streamSegments: List<StreamSegment>
        get() = emptyList()

    /**
     * Meta information about the stream.
     *
     * This can be information about the stream creator (e.g. if the creator is a public
     * broadcaster) or further information on the topic (e.g. hints that the video might contain
     * conspiracy theories or contains information about a current health situation like the
     * Covid-19 pandemic).
     *
     * The meta information often contains links to external sources like Wikipedia or the WHO.
     *
     * @return The meta info of the stream or an empty list if not provided.
     */
    @get:Throws(ParsingException::class)
    open val metaInfo: List<MetaInfo>
        get() = emptyList()

    /**
     * Whether the stream is a short-form content.
     * Short-form contents are contents in the style of TikTok, YouTube Shorts, or Instagram Reels videos.
     * @return whether the stream is a short-form content
     */
    @get:Throws(ParsingException::class)
    val isShortFormContent: Boolean
        get() = false

    open val ageLimit: Int = 0
    open val uploaderSubscriberCount: Long = -1

    /**
     * Override this function if the format of timestamp in the url is not the same format as that from youtube.
     * @return the time stamp/seek for the video in seconds
     */
    @Throws(ParsingException::class)
    protected fun getTimestampSeconds(regexPattern: String): Long {
        val timestamp: String
        try {
            timestamp = matchGroup1(regexPattern, originalUrl)
        } catch (e: RegexException) {
            // catch this instantly since a url does not necessarily have a timestamp
            // -2 because the testing system will consequently know that the regex failed
            // not good, I know
            return -2
        }

        if (timestamp.isNotEmpty()) {
            try {
                var secondsString = ""
                var minutesString = ""
                var hoursString = ""
                try {
                    secondsString = matchGroup1("(\\d+)s", timestamp)
                    minutesString = matchGroup1("(\\d+)m", timestamp)
                    hoursString = matchGroup1("(\\d+)h", timestamp)
                } catch (e: Exception) {
                    // it could be that time is given in another method
                    // if nothing was obtained, treat as unlabelled seconds
                    if (secondsString.isEmpty() && minutesString.isEmpty()) secondsString = matchGroup1("t=(\\d+)", timestamp)
                }

                val seconds = if (secondsString.isEmpty()) 0 else secondsString.toInt()
                val minutes = if (minutesString.isEmpty()) 0 else minutesString.toInt()
                val hours = if (hoursString.isEmpty()) 0 else hoursString.toInt()

                return seconds + (60L * minutes) + (3600L * hours)
            } catch (e: ParsingException) {
                throw ParsingException("Could not get timestamp.", e)
            }
        } else return 0
    }

    enum class Privacy {
        PUBLIC,
        UNLISTED,
        PRIVATE,
        INTERNAL,
        OTHER
    }

    companion object {
        const val NO_AGE_LIMIT: Int = 0
    }
}
