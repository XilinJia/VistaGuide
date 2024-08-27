/*
 * Created by Christian Schabesberger on 26.08.15.
 *
 * Copyright (C) 2016 Christian Schabesberger <chris.schabesberger@mailbox.org>
* Copyright (C) 2024 Xilin Jia <https://github.com/XilinJia>
 * StreamInfo.kt is part of Vista Guide.
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
import ac.mdiq.vista.extractor.Vista.getServiceByUrl
import ac.mdiq.vista.extractor.exceptions.ContentNotAvailableException
import ac.mdiq.vista.extractor.exceptions.ContentNotSupportedException
import ac.mdiq.vista.extractor.exceptions.ExtractionException
import ac.mdiq.vista.extractor.localization.DateWrapper
import ac.mdiq.vista.extractor.stream.StreamExtractor.Privacy
import ac.mdiq.vista.extractor.utils.ExtractorHelper
import java.io.IOException
import java.util.*


/**
 * Info object for opened contents, i.e. the content ready to play.
 */
class StreamInfo(
        serviceId: Int,
        url: String,
        originalUrl: String,
        /**
         * Get the stream type
         * @return the stream type
         */
        var streamType: StreamType,
        id: String,
        name: String,
        var ageLimit: Int)
    : Info(serviceId, id, url, originalUrl, name) {

    /**
     * Get the thumbnail url
     * @return the thumbnail url as a string
     */
    var thumbnails: List<Image> = listOf()

    var textualUploadDate: String? = null
    var uploadDate: DateWrapper? = null

    /**
     * Get the duration in seconds
     * @return the duration in seconds
     */
    var duration: Long = -1
    var description: Description? = null

    var viewCount: Long = -1

    /**
     * Get the number of likes.
     * @return The number of likes or -1 if this information is not available
     */
    var likeCount: Long = -1

    /**
     * Get the number of dislikes.
     *
     * @return The number of likes or -1 if this information is not available
     */
    var dislikeCount: Long = -1

    var uploaderName: String = ""
    var uploaderUrl: String = ""

    var uploaderAvatars: List<Image> = listOf()
    var isUploaderVerified: Boolean = false
    var uploaderSubscriberCount: Long = -1

    var subChannelName: String = ""
    var subChannelUrl: String = ""

    var subChannelAvatars: List<Image> = listOf()

    var videoStreams: List<VideoStream> = listOf()
    var audioStreams: List<AudioStream> = listOf()
    var videoOnlyStreams: List<VideoStream> = listOf()

    var dashMpdUrl: String = ""
    var hlsUrl: String = ""
    var relatedItems: List<InfoItem> = listOf()

    var startPosition: Long = 0
    var subtitles: List<SubtitlesStream> = listOf()

    var host: String = ""
    var privacy: Privacy? = null
    var category: String = ""
    var licence: String = ""
    var supportInfo: String = ""
    var languageInfo: Locale? = null
    var tags: List<String> = listOf()
    var streamSegments: List<StreamSegment> = listOf()

    var metaInfo: List<MetaInfo> = listOf()
    var isShortFormContent: Boolean = false

    /**
     * Preview frames, e.g. for the storyboard / seekbar thumbnail preview
     */
    var previewFrames: List<Frameset> = listOf()

    @get:Deprecated("Use {@link #getRelatedItems()}")
    @set:Deprecated("Use {@link #setRelatedItems(List)}")
    var relatedStreams: List<InfoItem>
        get() = relatedItems
        set(relatedItemsToSet) {
            relatedItems = relatedItemsToSet
        }

    class StreamExtractException internal constructor(message: String?) : ExtractionException(message)

    companion object {
        @Throws(IOException::class, ExtractionException::class)
        fun getInfo(url: String): StreamInfo {
            return getInfo(getServiceByUrl(url), url)
        }

        @Throws(IOException::class, ExtractionException::class)
        fun getInfo(service: StreamingService, url: String): StreamInfo {
            return getInfo(service.getStreamExtractor(url))
        }

        @Throws(ExtractionException::class, IOException::class)
        fun getInfo(extractor: StreamExtractor): StreamInfo {
            extractor.fetchPage()
            val streamInfo: StreamInfo
            try {
                streamInfo = extractImportantData(extractor)
                extractStreams(streamInfo, extractor)
                extractOptionalData(streamInfo, extractor)
                return streamInfo
            } catch (e: ExtractionException) {
                // Currently, YouTube does not distinguish between age restricted videos and videos
                // blocked by country. This means that during the initialisation of the extractor, the
                // extractor will assume that a video is age restricted while in reality it is blocked
                // by country.
                //
                // We will now detect whether the video is blocked by country or not.

                val errorMessage = extractor.errorMessage
                if (errorMessage.isNullOrEmpty()) throw e
                else throw ContentNotAvailableException(errorMessage, e)
            }
        }

        @Throws(ExtractionException::class)
        private fun extractImportantData(extractor: StreamExtractor): StreamInfo {
            // Important data, without it the content can't be displayed.
            // If one of these is not available, the frontend will receive an exception directly.

            val url = extractor.url
            val streamType = extractor.streamType
            val id = extractor.id
            val name = extractor.getName()
            val ageLimit = extractor.ageLimit

            // Suppress always-non-null warning as here we double-check it really is not null
            if ((streamType == StreamType.NONE || url.isEmpty() || id.isEmpty()) || name.isEmpty() || ageLimit == -1)
                throw ExtractionException("Some important stream information was not given.")
            return StreamInfo(extractor.serviceId, url, extractor.originalUrl, streamType, id, name, ageLimit)
        }

        @Throws(ExtractionException::class)
        private fun extractStreams(streamInfo: StreamInfo, extractor: StreamExtractor) {
            /* ---- Stream extraction goes here ---- */
            // At least one type of stream has to be available, otherwise an exception will be thrown
            // directly into the frontend.

            try { streamInfo.dashMpdUrl = extractor.dashMpdUrl } catch (e: Exception) { streamInfo.addError(ExtractionException("Couldn't get DASH manifest", e)) }

            try { streamInfo.hlsUrl = extractor.hlsUrl } catch (e: Exception) { streamInfo.addError(ExtractionException("Couldn't get HLS manifest", e)) }

            try { streamInfo.audioStreams = extractor.audioStreams } catch (e: ContentNotSupportedException) { throw e } catch (e: Exception) { streamInfo.addError(ExtractionException("Couldn't get audio streams", e)) }

            try { streamInfo.videoStreams = extractor.videoStreams } catch (e: Exception) { streamInfo.addError(ExtractionException("Couldn't get video streams", e)) }

            try { streamInfo.videoOnlyStreams = extractor.videoOnlyStreams } catch (e: Exception) { streamInfo.addError(ExtractionException("Couldn't get video only streams", e)) }

            // Either audio or video has to be available, otherwise we didn't get a stream (since
            // videoOnly are optional, they don't count).
            if ((streamInfo.videoStreams.isEmpty()) && (streamInfo.audioStreams.isEmpty()))
                throw StreamExtractException("Could not get any stream. See error variable to get further details.")
        }

        private fun extractOptionalData(streamInfo: StreamInfo, extractor: StreamExtractor) {
            /* ---- Optional data goes here: ---- */
            // If one of these fails, the frontend needs to handle that they are not available.
            // Exceptions are therefore not thrown into the frontend, but stored into the error list,
            // so the frontend can afterwards check where errors happened.

            try { streamInfo.thumbnails = extractor.thumbnails } catch (e: Exception) { streamInfo.addError(e) }
            try { streamInfo.duration = extractor.length } catch (e: Exception) { streamInfo.addError(e) }
            try { streamInfo.uploaderName = extractor.uploaderName } catch (e: Exception) { streamInfo.addError(e) }
            try { streamInfo.uploaderUrl = extractor.uploaderUrl } catch (e: Exception) { streamInfo.addError(e) }
            try { streamInfo.uploaderAvatars = extractor.uploaderAvatars } catch (e: Exception) { streamInfo.addError(e) }
            try { streamInfo.isUploaderVerified = extractor.isUploaderVerified } catch (e: Exception) { streamInfo.addError(e) }
            try { streamInfo.uploaderSubscriberCount = extractor.uploaderSubscriberCount } catch (e: Exception) { streamInfo.addError(e) }

            try { streamInfo.subChannelName = extractor.subChannelName } catch (e: Exception) { streamInfo.addError(e) }
            try { streamInfo.subChannelUrl = extractor.subChannelUrl } catch (e: Exception) { streamInfo.addError(e) }
            try { streamInfo.subChannelAvatars = extractor.subChannelAvatars } catch (e: Exception) { streamInfo.addError(e) }

            try { streamInfo.description = extractor.description } catch (e: Exception) { streamInfo.addError(e) }
            try { streamInfo.viewCount = extractor.viewCount } catch (e: Exception) { streamInfo.addError(e) }
            try { streamInfo.textualUploadDate = extractor.textualUploadDate } catch (e: Exception) { streamInfo.addError(e) }
            try { streamInfo.uploadDate = extractor.uploadDate } catch (e: Exception) { streamInfo.addError(e) }
            try { streamInfo.startPosition = extractor.timeStamp } catch (e: Exception) { streamInfo.addError(e) }
            try { streamInfo.likeCount = extractor.likeCount } catch (e: Exception) { streamInfo.addError(e) }
            try { streamInfo.dislikeCount = extractor.dislikeCount } catch (e: Exception) { streamInfo.addError(e) }
            try { streamInfo.subtitles = extractor.subtitlesDefault } catch (e: Exception) { streamInfo.addError(e) }

            // Additional info
            try { streamInfo.host = extractor.host } catch (e: Exception) { streamInfo.addError(e) }
            try { streamInfo.privacy = extractor.privacy } catch (e: Exception) { streamInfo.addError(e) }
            try { streamInfo.category = extractor.category } catch (e: Exception) { streamInfo.addError(e) }
            try { streamInfo.licence = extractor.licence } catch (e: Exception) { streamInfo.addError(e) }
            try { streamInfo.languageInfo = extractor.languageInfo } catch (e: Exception) { streamInfo.addError(e) }
            try { streamInfo.tags = extractor.tags } catch (e: Exception) { streamInfo.addError(e) }
            try { streamInfo.supportInfo = extractor.supportInfo } catch (e: Exception) { streamInfo.addError(e) }
            try { streamInfo.streamSegments = extractor.streamSegments } catch (e: Exception) { streamInfo.addError(e) }
            try { streamInfo.metaInfo = extractor.metaInfo } catch (e: Exception) { streamInfo.addError(e) }
            try { streamInfo.previewFrames = extractor.frames } catch (e: Exception) { streamInfo.addError(e) }
            try { streamInfo.isShortFormContent = extractor.isShortFormContent } catch (e: Exception) { streamInfo.addError(e) }

            streamInfo.relatedItems = ExtractorHelper.getRelatedItemsOrLogError(streamInfo, extractor) ?: listOf()
        }
    }
}
