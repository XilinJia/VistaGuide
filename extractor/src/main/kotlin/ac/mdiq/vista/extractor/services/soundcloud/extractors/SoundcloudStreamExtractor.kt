package ac.mdiq.vista.extractor.services.soundcloud.extractors

import com.grack.nanojson.JsonArray
import com.grack.nanojson.JsonObject
import com.grack.nanojson.JsonParser
import com.grack.nanojson.JsonParserException
import ac.mdiq.vista.extractor.Image
import ac.mdiq.vista.extractor.MediaFormat
import ac.mdiq.vista.extractor.Vista
import ac.mdiq.vista.extractor.StreamingService
import ac.mdiq.vista.extractor.downloader.Downloader
import ac.mdiq.vista.extractor.exceptions.*
import ac.mdiq.vista.extractor.linkhandler.LinkHandler
import ac.mdiq.vista.extractor.localization.DateWrapper
import ac.mdiq.vista.extractor.services.soundcloud.SoundcloudParsingHelper
import ac.mdiq.vista.extractor.services.soundcloud.SoundcloudParsingHelper.SOUNDCLOUD_API_V2_URL
import ac.mdiq.vista.extractor.services.soundcloud.SoundcloudParsingHelper.clientId
import ac.mdiq.vista.extractor.stream.*
import ac.mdiq.vista.extractor.utils.Utils.encodeUrlUtf8
import java.io.IOException


class SoundcloudStreamExtractor(service: StreamingService, linkHandler: LinkHandler) : StreamExtractor(service, linkHandler) {
    private var track: JsonObject? = null
    private var isAvailable = true

    @Throws(IOException::class, ExtractionException::class)
    override fun onFetchPage(downloader: Downloader) {
        track = SoundcloudParsingHelper.resolveFor(downloader, url)

        val policy = track?.getString("policy", "")
        if (policy != "ALLOW" && policy != "MONETIZE") {
            isAvailable = false

            if (policy == "SNIP") throw SoundCloudGoPlusContentException()
            if (policy == "BLOCK") throw GeographicRestrictionException("This track is not available in user's country")

            throw ContentNotAvailableException("Content not available: policy $policy")
        }
    }


    override val id: String
        get() = track!!.getInt("id").toString()


    override fun getName(): String {
        return track!!.getString("title")
    }


    override val textualUploadDate: String
        get() = track!!.getString("created_at")
            .replace("T", " ")
            .replace("Z", "")

    @get:Throws(ParsingException::class)

    override val uploadDate: DateWrapper
        get() = DateWrapper(SoundcloudParsingHelper.parseDateFrom(track!!.getString("created_at")))

    @get:Throws(ParsingException::class)

    override val thumbnails: List<Image>
        get() = SoundcloudParsingHelper.getAllImagesFromTrackObject(track!!)


    override val description: Description
        get() = Description(track!!.getString("description"), Description.PLAIN_TEXT)

    override val length: Long
        get() = track!!.getLong("duration") / 1000L

    @get:Throws(ParsingException::class)
    override val timeStamp: Long
        get() = getTimestampSeconds("(#t=\\d{0,3}h?\\d{0,3}m?\\d{1,3}s?)")

    override val viewCount: Long
        get() = track!!.getLong("playback_count")

    override val likeCount: Long
        get() = track!!.getLong("likes_count", -1)


    override val uploaderUrl: String
        get() = if (track != null) SoundcloudParsingHelper.getUploaderUrl(track!!) else ""


    override val uploaderName: String
        get() = if (track != null) SoundcloudParsingHelper.getUploaderName(track!!) else ""

    @get:Throws(ParsingException::class)
    override val isUploaderVerified: Boolean
        get() = track!!.getObject("user").getBoolean("verified")


    override val uploaderAvatars: List<Image>
        get() = if (track != null) SoundcloudParsingHelper.getAllImagesFromArtworkOrAvatarUrl(SoundcloudParsingHelper.getAvatarUrl(track!!)) else listOf()

    @get:Throws(ExtractionException::class)
    override val audioStreams: List<AudioStream>
        get() {
            val audioStreams: MutableList<AudioStream> = ArrayList()

            // Streams can be streamable and downloadable - or explicitly not.
            // For playing the track, it is only necessary to have a streamable track.
            // If this is not the case, this track might not be published yet.
            if (!track!!.getBoolean("streamable") || !isAvailable) return audioStreams

            try {
                val transcodings = track!!.getObject("media").getArray("transcodings")
                // Get information about what stream formats are available
                if (!transcodings.isNullOrEmpty()) extractAudioStreams(transcodings, checkMp3ProgressivePresence(transcodings), audioStreams)
                extractDownloadableFileIfAvailable(audioStreams)
            } catch (e: NullPointerException) {
                throw ExtractionException("Could not get audio streams", e)
            }
            return audioStreams
        }


    @Throws(IOException::class, ExtractionException::class)
    private fun getTranscodingUrl(endpointUrl: String): String {
        val apiStreamUrl = endpointUrl + "?client_id=" + clientId()
        val response = Vista.downloader.get(apiStreamUrl).responseBody()
        val urlObject: JsonObject
        try {
            urlObject = JsonParser.`object`().from(response)
        } catch (e: JsonParserException) {
            throw ParsingException("Could not parse streamable URL", e)
        }

        return urlObject.getString("url")
    }

    @Throws(IOException::class, ExtractionException::class)
    private fun getDownloadUrl(trackId: String): String? {
        val response = Vista.downloader.get(SOUNDCLOUD_API_V2_URL + "tracks/"
                + trackId + "/download" + "?client_id=" + clientId()).responseBody()

        val downloadJsonObject: JsonObject
        try {
            downloadJsonObject = JsonParser.`object`().from(response)
        } catch (e: JsonParserException) {
            throw ParsingException("Could not parse download URL", e)
        }
        val redirectUri = downloadJsonObject.getString("redirectUri")
        if (!redirectUri.isNullOrEmpty()) return redirectUri
        return null
    }

    private fun extractAudioStreams(transcodings: JsonArray, mp3ProgressiveInStreams: Boolean, audioStreams: MutableList<AudioStream>) {
        transcodings.stream()
            .filter { o: Any? -> JsonObject::class.java.isInstance(o) }
            .map { obj: Any? -> JsonObject::class.java.cast(obj) }
            .forEachOrdered { transcoding: JsonObject ->
                val url = transcoding.getString("url")
                if (url.isNullOrEmpty()) return@forEachOrdered
                try {
                    val preset = transcoding.getString("preset", Stream.ID_UNKNOWN)
                    val protocol = transcoding.getObject("format").getString("protocol")
                    val builder = AudioStream.Builder().setId(preset)

                    val isHls = protocol == "hls"
                    if (isHls) builder.setDeliveryMethod(DeliveryMethod.HLS)

                    builder.setContent(getTranscodingUrl(url), true)

                    when {
                        preset.contains("mp3") -> {
                            // Don't add the MP3 HLS stream if there is a progressive stream
                            // present because both have the same bitrate
                            if (mp3ProgressiveInStreams && isHls) return@forEachOrdered
                            builder.setMediaFormat(MediaFormat.MP3)
                            builder.setAverageBitrate(128)
                        }
                        preset.contains("opus") -> {
                            builder.setMediaFormat(MediaFormat.OPUS)
                            builder.setAverageBitrate(64)
                            builder.setDeliveryMethod(DeliveryMethod.HLS)
                        }
                        else -> {
                            // Unknown format, skip to the next audio stream
                            return@forEachOrdered
                        }
                    }
                    val audioStream = builder.build()
                    if (!Stream.containSimilarStream(audioStream, audioStreams)) audioStreams.add(audioStream)
                } catch (ignored: ExtractionException) {
                    // Something went wrong when trying to get and add this audio stream,
                    // skip to the next one
                } catch (ignored: IOException) { }
            }
    }

    /**
     * Add the downloadable format if it is available.
     * A track can have the `downloadable` boolean set to `true`, but it doesn't mean we can download it.
     *
     * If the value of the `has_download_left` boolean is `true`, the track can be
     * downloaded, and not otherwise.
     *
     * @param audioStreams the audio streams to which the downloadable file is added
     */
    fun extractDownloadableFileIfAvailable(audioStreams: MutableList<AudioStream>) {
        if (track!!.getBoolean("downloadable") && track!!.getBoolean("has_downloads_left")) {
            try {
                val downloadUrl = getDownloadUrl(id)
                if (!downloadUrl.isNullOrEmpty()) {
                    audioStreams.add(AudioStream.Builder()
                        .setId("original-format")
                        .setContent(downloadUrl, true)
                        .setAverageBitrate(AudioStream.UNKNOWN_BITRATE)
                        .build())
                }
            } catch (ignored: Exception) {
                // If something went wrong when trying to get the download URL, ignore the
                // exception throw because this "stream" is not necessary to play the track
            }
        }
    }

    override val videoStreams: List<VideoStream>
        get() = emptyList()

    override val videoOnlyStreams: List<VideoStream>
        get() = emptyList()

    override val streamType: StreamType
        get() = StreamType.AUDIO_STREAM

    @get:Throws(IOException::class, ExtractionException::class)
    override val relatedItems: StreamInfoItemsCollector
        get() {
            val collector = StreamInfoItemsCollector(serviceId)
            val apiUrl = ((SOUNDCLOUD_API_V2_URL + "tracks/" + encodeUrlUtf8(id)) + "/related?client_id=" + encodeUrlUtf8(clientId()))
            SoundcloudParsingHelper.getStreamsFromApi(collector, apiUrl)
            return collector
        }

    override val privacy: Privacy
        get() = if (track!!.getString("sharing") == "public") Privacy.PUBLIC else Privacy.PRIVATE


    override val category: String
        get() = track!!.getString("genre")


    override val licence: String
        get() = track!!.getString("license")


    override val tags: List<String>
        get() {
            // Tags are separated by spaces, but they can be multiple words escaped by quotes "
            val tagList =
                track!!.getString("tag_list").split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val tags: MutableList<String> = ArrayList()
            val escapedTag = StringBuilder()
            var isEscaped = false
            for (tag in tagList) {
                if (tag.startsWith("\"")) {
                    escapedTag.append(tag.replace("\"", ""))
                    isEscaped = true
                } else if (isEscaped) {
                    if (tag.endsWith("\"")) {
                        escapedTag.append(" ").append(tag.replace("\"", ""))
                        isEscaped = false
                        tags.add(escapedTag.toString())
                    } else escapedTag.append(" ").append(tag)
                } else if (tag.isNotEmpty()) tags.add(tag)
            }
            return tags
        }

    companion object {
        private fun checkMp3ProgressivePresence(transcodings: JsonArray): Boolean {
            return transcodings.stream()
                .filter { o: Any? -> JsonObject::class.java.isInstance(o) }
                .map { obj: Any? -> JsonObject::class.java.cast(obj) }
                .anyMatch { transcodingJsonObject: JsonObject ->
                    transcodingJsonObject.getString("preset")
                        .contains("mp3") && transcodingJsonObject.getObject("format")
                        .getString("protocol") == "progressive"
                }
        }
    }
}
