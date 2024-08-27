package ac.mdiq.vista.extractor.services.peertube.extractors

import com.grack.nanojson.JsonArray
import com.grack.nanojson.JsonObject
import com.grack.nanojson.JsonParser
import com.grack.nanojson.JsonParserException
import ac.mdiq.vista.extractor.Image
import ac.mdiq.vista.extractor.MediaFormat
import ac.mdiq.vista.extractor.MediaFormat.Companion.getFromSuffix
import ac.mdiq.vista.extractor.Vista
import ac.mdiq.vista.extractor.StreamingService
import ac.mdiq.vista.extractor.downloader.Downloader
import ac.mdiq.vista.extractor.exceptions.ExtractionException
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.exceptions.ReCaptchaException
import ac.mdiq.vista.extractor.linkhandler.LinkHandler
import ac.mdiq.vista.extractor.localization.DateWrapper
import ac.mdiq.vista.extractor.services.peertube.PeertubeParsingHelper.getAvatarsFromOwnerAccountOrVideoChannelObject
import ac.mdiq.vista.extractor.services.peertube.PeertubeParsingHelper.getThumbnailsFromPlaylistOrVideoItem
import ac.mdiq.vista.extractor.services.peertube.PeertubeParsingHelper.parseDateFrom
import ac.mdiq.vista.extractor.services.peertube.PeertubeParsingHelper.validate
import ac.mdiq.vista.extractor.services.peertube.linkHandler.PeertubeSearchQueryHandlerFactory
import ac.mdiq.vista.extractor.services.peertube.linkHandler.PeertubeStreamLinkHandlerFactory
import ac.mdiq.vista.extractor.stream.*
import ac.mdiq.vista.extractor.utils.JsonUtils.getArray
import ac.mdiq.vista.extractor.utils.JsonUtils.getBoolean
import ac.mdiq.vista.extractor.utils.JsonUtils.getNumber
import ac.mdiq.vista.extractor.utils.JsonUtils.getString
import ac.mdiq.vista.extractor.utils.JsonUtils.getStringListFromJsonArray
import ac.mdiq.vista.extractor.utils.JsonUtils.getValue
import ac.mdiq.vista.extractor.utils.Utils.encodeUrlUtf8
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.util.*
import java.util.stream.Collectors


class PeertubeStreamExtractor(service: StreamingService, linkHandler: LinkHandler) : StreamExtractor(service, linkHandler) {
//    override val baseUrl: String
    private var json: JsonObject? = null

    private val subtitles: MutableList<SubtitlesStream> = ArrayList()

    /*
    Some videos have audio streams; others don't.
    So an audio stream may be available if a video stream is available.
    Audio streams are also not returned as separated streams for livestreams.
    That's why the extraction of audio streams is only run when there are video streams
    extracted and when the content is not a livestream.
     */
    override val audioStreams: MutableList<AudioStream> = mutableListOf()
        get() {
            assertPageFetched()
            if (field.isEmpty() && videoStreams.isEmpty() && streamType == StreamType.VIDEO_STREAM) streams
            return field
        }

    override val videoStreams: MutableList<VideoStream> = mutableListOf()
        get() {
            assertPageFetched()
            if (field.isEmpty()) {
                if (streamType == StreamType.VIDEO_STREAM) streams else extractLiveVideoStreams()
            }
            return field
        }

    private var subtitlesException: ParsingException? = null

//    init {
//        this.baseUrl = baseUrl
//    }

    @get:Throws(ParsingException::class)
    override val textualUploadDate: String
        get() = getString(json!!, "publishedAt")

    @get:Throws(ParsingException::class)
    override val uploadDate: DateWrapper
        get() {
            val textualUploadDate = textualUploadDate
            return DateWrapper(parseDateFrom(textualUploadDate))
        }

    @get:Throws(ParsingException::class)

    override val thumbnails: List<Image>
        get() = getThumbnailsFromPlaylistOrVideoItem(baseUrl, json!!)

    @get:Throws(ParsingException::class)

    override val description: Description
        get() {
            var text: String
            try {
                text = getString(json!!, "description")
            } catch (e: ParsingException) {
                return Description.EMPTY_DESCRIPTION
            }
            if (text.length == 250 && text.substring(247) == "...") {
                // If description is shortened, get full description
                val dl = Vista.downloader
                try {
                    val response = dl.get(baseUrl
                            + PeertubeStreamLinkHandlerFactory.VIDEO_API_ENDPOINT
                            + id + "/description")
                    val jsonObject = JsonParser.`object`().from(response.responseBody())
                    text = getString(jsonObject, "description")
                } catch (ignored: IOException) {
                    // Something went wrong when getting the full description, use the shortened one
                } catch (ignored: ReCaptchaException) {
                } catch (ignored: JsonParserException) { }
            }
            return Description(text, Description.MARKDOWN)
        }

    @get:Throws(ParsingException::class)
    override val ageLimit: Int = -1
        get() {
            val isNSFW = getBoolean(json!!, "nsfw")
            return if (isNSFW) 18 else field
        }

    override val length: Long
        get() = json!!.getLong("duration")

    @get:Throws(ParsingException::class)
    override val timeStamp: Long
        get() {
            val timestamp = getTimestampSeconds("((#|&|\\?)start=\\d{0,3}h?\\d{0,3}m?\\d{1,3}s?)")
            // regex for timestamp was not found
            return if (timestamp == -2L) 0 else { timestamp }
        }

    override val viewCount: Long
        get() = json!!.getLong("views")

    override val likeCount: Long
        get() = json!!.getLong("likes")

    override val dislikeCount: Long
        get() = json!!.getLong("dislikes")

    @get:Throws(ParsingException::class)

    override val uploaderUrl: String
        get() {
            val name = getString(json!!, ACCOUNT_NAME)
            val host = getString(json!!, ACCOUNT_HOST)
            return service.getChannelLHFactory().fromId("accounts/$name@$host", baseUrl).url
        }

    @get:Throws(ParsingException::class)

    override val uploaderName: String
        get() = getString(json!!, "account.displayName")


    override val uploaderAvatars: List<Image>
        get() = getAvatarsFromOwnerAccountOrVideoChannelObject(baseUrl, json!!.getObject("account"))

    @get:Throws(ParsingException::class)

    override val subChannelUrl: String
        get() = getString(json!!, "channel.url")

    @get:Throws(ParsingException::class)

    override val subChannelName: String
        get() = getString(json!!, "channel.displayName")


    override val subChannelAvatars: List<Image>
        get() = getAvatarsFromOwnerAccountOrVideoChannelObject(baseUrl, json!!.getObject("channel"))


    override val hlsUrl: String
        get() {
            assertPageFetched()
            if (streamType == StreamType.VIDEO_STREAM && !json!!.getObject(FILES).isNullOrEmpty()) return json!!.getObject(FILES).getString(PLAYLIST_URL, "")
            return json!!.getArray(STREAMING_PLAYLISTS).getObject(0).getString(PLAYLIST_URL, "")
        }

    override val videoOnlyStreams: List<VideoStream>
        get() = emptyList()

    @get:Throws(ParsingException::class)

    override val subtitlesDefault: List<SubtitlesStream>
        get() {
            if (subtitlesException != null) throw subtitlesException!!
            return subtitles
        }


    @Throws(ParsingException::class)
    override fun getSubtitles(format: MediaFormat): List<SubtitlesStream> {
        if (subtitlesException != null) throw subtitlesException!!

        return subtitles.stream()
            .filter { sub: SubtitlesStream? -> sub!!.format == format }
            .collect(Collectors.toList())
    }

    override val streamType: StreamType
        get() = if (json!!.getBoolean("isLive")) StreamType.LIVE_STREAM else StreamType.VIDEO_STREAM

    @get:Throws(IOException::class, ExtractionException::class)
    override val relatedItems: StreamInfoItemsCollector?
        get() {
            val tags = tags
            val apiUrl = if (tags.isEmpty()) ("$baseUrl/api/v1/accounts/${getString(json!!, ACCOUNT_NAME)}@${getString(json!!, ACCOUNT_HOST)}/videos?start=0&count=8")
            else getRelatedItemsUrl(tags)

            if (apiUrl.isEmpty()) return null
            else {
                val collector = StreamInfoItemsCollector(serviceId)
                getStreamsFromApi(collector, apiUrl)
                return collector
            }
        }


    override val tags: List<String>
        get() = getStringListFromJsonArray(json!!.getArray("tags"))


    override val supportInfo: String
        get() = try {
            getString(json!!, "support")
        } catch (e: ParsingException) {
            ""
        }

    @get:Throws(ParsingException::class)

    override val streamSegments: List<StreamSegment>
        get() {
            val segments: MutableList<StreamSegment> = ArrayList()
            val segmentsJson: JsonObject?
            try {
                segmentsJson = fetchSubApiContent("chapters")
            } catch (e: IOException) {
                throw ParsingException("Could not get stream segments", e)
            } catch (e: ReCaptchaException) {
                throw ParsingException("Could not get stream segments", e)
            }
            if (segmentsJson != null && segmentsJson.has("chapters")) {
                val segmentsArray = segmentsJson.getArray("chapters")
                for (i in segmentsArray.indices) {
                    val segmentObject = segmentsArray.getObject(i)
                    segments.add(StreamSegment(segmentObject.getString("title"), segmentObject.getInt("timecode")))
                }
            }

            return segments
        }

    @get:Throws(ExtractionException::class)

    override val frames: List<Frameset>
        get() {
            val framesets: MutableList<Frameset> = ArrayList()
            val storyboards: JsonObject?
            try {
                storyboards = fetchSubApiContent("storyboards")
            } catch (e: IOException) {
                throw ExtractionException("Could not get frames", e)
            } catch (e: ReCaptchaException) {
                throw ExtractionException("Could not get frames", e)
            }
            if (storyboards != null && storyboards.has("storyboards")) {
                val storyboardsArray = storyboards.getArray("storyboards")
                for (storyboard in storyboardsArray) {
                    if (storyboard is JsonObject) {
                        val url = storyboard.getString("storyboardPath")
                        val width = storyboard.getInt("spriteWidth")
                        val height = storyboard.getInt("spriteHeight")
                        val totalWidth = storyboard.getInt("totalWidth")
                        val totalHeight = storyboard.getInt("totalHeight")
                        val framesPerPageX = totalWidth / width
                        val framesPerPageY = totalHeight / height
                        val count = framesPerPageX * framesPerPageY
                        val durationPerFrame = storyboard.getInt("spriteDuration") * 1000

                        // there is only one composite image per video containing all frames
                        framesets.add(Frameset(listOf(baseUrl + url),
                            width,
                            height,
                            count,
                            durationPerFrame,
                            framesPerPageX,
                            framesPerPageY))
                    }
                }
            }

            return framesets
        }


    @Throws(UnsupportedEncodingException::class)
    private fun getRelatedItemsUrl(tags: List<String>): String {
        val url = baseUrl + PeertubeSearchQueryHandlerFactory.SEARCH_ENDPOINT_VIDEOS
        val params = StringBuilder()
        params.append("start=0&count=8&sort=-createdAt")
        for (tag in tags) {
            params.append("&tagsOneOf=").append(encodeUrlUtf8(tag))
        }
        return "$url?$params"
    }

    @Throws(IOException::class, ReCaptchaException::class, ParsingException::class)
    private fun getStreamsFromApi(collector: StreamInfoItemsCollector, apiUrl: String) {
        val response = downloader.get(apiUrl)
        var relatedVideosJson: JsonObject? = null
        if (response != null && response.responseBody().isNotEmpty()) {
            try {
                relatedVideosJson = JsonParser.`object`().from(response.responseBody())
            } catch (e: JsonParserException) {
                throw ParsingException("Could not parse json data for related videos", e)
            }
        }
        if (relatedVideosJson != null) collectStreamsFrom(collector, relatedVideosJson)
    }

    @Throws(ParsingException::class)
    private fun collectStreamsFrom(collector: StreamInfoItemsCollector, jsonObject: JsonObject) {
        val contents: JsonArray
        try {
            contents = getValue(jsonObject, "data") as JsonArray
        } catch (e: Exception) {
            throw ParsingException("Could not extract related videos", e)
        }

        for (c in contents) {
            if (c is JsonObject) {
                val extractor = PeertubeStreamInfoItemExtractor(c, baseUrl)
                // Do not add the same stream in related streams
                if (extractor.url != url) collector.commit(extractor)
            }
        }
    }

    @Throws(IOException::class, ExtractionException::class)
    override fun onFetchPage(downloader: Downloader) {
        val response = downloader.get(baseUrl + PeertubeStreamLinkHandlerFactory.VIDEO_API_ENDPOINT + id)
        if (response != null) setInitialData(response.responseBody())
        else throw ExtractionException("Could not extract PeerTube channel data")

        loadSubtitles()
    }

    @Throws(ExtractionException::class)
    private fun setInitialData(responseBody: String) {
        try {
            json = JsonParser.`object`().from(responseBody)
        } catch (e: JsonParserException) {
            throw ExtractionException("Could not extract PeerTube stream data", e)
        }
        if (json == null) throw ExtractionException("Could not extract PeerTube stream data")
        validate(json!!)
    }

    private fun loadSubtitles() {
        if (subtitles.isEmpty()) {
            try {
                val response = downloader.get("$baseUrl${PeertubeStreamLinkHandlerFactory.VIDEO_API_ENDPOINT}$id/captions")
                val captionsJson = JsonParser.`object`().from(response.responseBody())
                val captions = getArray(captionsJson, "data")
                for (c in captions) {
                    if (c is JsonObject) {
                        val url = baseUrl + getString(c, "captionPath")
                        val languageCode = getString(c, "language.id")
                        val ext = url.substring(url.lastIndexOf(".") + 1)
                        val fmt = getFromSuffix(ext)
                        if (fmt != null && languageCode.isNotEmpty()) {
                            subtitles.add(SubtitlesStream.Builder()
                                .setContent(url, true)
                                .setMediaFormat(fmt)
                                .setLanguageCode(languageCode)
                                .setAutoGenerated(false)
                                .build())
                        }
                    }
                }
            } catch (e: Exception) {
                subtitlesException = ParsingException("Could not get subtitles", e)
            }
        }
    }

    @Throws(ParsingException::class)
    private fun extractLiveVideoStreams() {
        try {
            val streamingPlaylists = json!!.getArray(STREAMING_PLAYLISTS)
            streamingPlaylists.stream()
                .filter { o: Any? -> JsonObject::class.java.isInstance(o) }
                .map { obj: Any? -> JsonObject::class.java.cast(obj) }
                .map { stream: JsonObject ->
                    VideoStream.Builder()
                        .setId(stream.getInt("id", -1).toString())
                        .setContent(stream.getString(PLAYLIST_URL, ""), true)
                        .setIsVideoOnly(false)
                        .setResolution("")
                        .setMediaFormat(MediaFormat.MPEG_4)
                        .setDeliveryMethod(DeliveryMethod.HLS)
                        .build()
                } // Don't use the containsSimilarStream method because it will always return
                // false so if there are multiples HLS URLs returned, only the first will be
                // extracted in this case.
                .forEachOrdered { e: VideoStream? -> if (e != null) videoStreams.add(e) }
        } catch (e: Exception) {
            throw ParsingException("Could not get video streams", e)
        }
    }

    @get:Throws(ParsingException::class)
    private val streams: Unit
        get() {
            // Progressive streams
            getStreamsFromArray(json!!.getArray(FILES), "")

            // HLS streams
            try {
                for (playlist in json!!.getArray(STREAMING_PLAYLISTS).stream()
                    .filter { o: Any? -> JsonObject::class.java.isInstance(o) }
                    .map { obj: Any? -> JsonObject::class.java.cast(obj) }
                    .collect(Collectors.toList())) {
                    getStreamsFromArray(playlist.getArray(FILES), playlist.getString(PLAYLIST_URL))
                }
            } catch (e: Exception) {
                throw ParsingException("Could not get streams", e)
            }
        }

    @Throws(ParsingException::class)
    private fun getStreamsFromArray(streams: JsonArray, playlistUrl: String) {
        try {
            /*
            Starting with version 3.4.0 of PeerTube, the HLS playlist of stream resolutions
            contains the UUID of the streams, so we can't use the same method to get the URL of
            the HLS playlist without fetching the master playlist.
            These UUIDs are the same as the ones returned into the fileUrl and fileDownloadUrl
            strings.
            */
            val isInstanceUsingRandomUuidsForHlsStreams = (playlistUrl.isNotEmpty()
                    && playlistUrl.endsWith("-master.m3u8"))

            for (stream in streams.stream()
                .filter { o: Any? -> JsonObject::class.java.isInstance(o) }
                .map { obj: Any? -> JsonObject::class.java.cast(obj) }
                .collect(Collectors.toList())) {
                // Extract stream version of streams first

                val url = getString(stream, if (stream.has(FILE_URL)) FILE_URL else FILE_DOWNLOAD_URL)
                // Not a valid stream URL
                if (url.isEmpty()) return

                val resolution = getString(stream, "resolution.label")
                val idSuffix = if (stream.has(FILE_URL)) FILE_URL else FILE_DOWNLOAD_URL

                // An audio stream
                if (resolution.lowercase(Locale.getDefault()).contains("audio"))
                    addNewAudioStream(stream, isInstanceUsingRandomUuidsForHlsStreams, resolution, idSuffix, url, playlistUrl)
                // A video stream
                else addNewVideoStream(stream, isInstanceUsingRandomUuidsForHlsStreams, resolution, idSuffix, url, playlistUrl)
            }
        } catch (e: Exception) {
            throw ParsingException("Could not get streams from array", e)
        }
    }


    @Throws(ParsingException::class)
    private fun getHlsPlaylistUrlFromFragmentedFileUrl(streamJsonObject: JsonObject, idSuffix: String, format: String,
                                                       url: String): String {
        val streamUrl = if (FILE_DOWNLOAD_URL == idSuffix) getString(streamJsonObject, FILE_URL) else url
        return streamUrl.replace("-fragmented.$format", ".m3u8")
    }


    @Throws(ParsingException::class)
    private fun getHlsPlaylistUrlFromMasterPlaylist(streamJsonObject: JsonObject, playlistUrl: String): String {
        return playlistUrl.replace("master", getNumber(streamJsonObject, RESOLUTION_ID).toString())
    }

    @Throws(ParsingException::class)
    private fun addNewAudioStream(streamJsonObject: JsonObject, isInstanceUsingRandomUuidsForHlsStreams: Boolean, resolution: String,
                                  idSuffix: String, url: String, playlistUrl: String) {
        val extension = url.substring(url.lastIndexOf(".") + 1)
        val format = getFromSuffix(extension)
        val id = "$resolution-$extension"

        // Add progressive HTTP streams first
        audioStreams.add(AudioStream.Builder()
            .setId(id + "-" + idSuffix + "-" + DeliveryMethod.PROGRESSIVE_HTTP)
            .setContent(url, true)
            .setMediaFormat(format)
            .setAverageBitrate(AudioStream.UNKNOWN_BITRATE)
            .build())

        // Then add HLS streams
        if (playlistUrl.isNotEmpty()) {
            val hlsStreamUrl = if (isInstanceUsingRandomUuidsForHlsStreams) getHlsPlaylistUrlFromFragmentedFileUrl(streamJsonObject, idSuffix, extension, url)
            else getHlsPlaylistUrlFromMasterPlaylist(streamJsonObject, playlistUrl)
            val audioStream = AudioStream.Builder()
                .setId(id + "-" + DeliveryMethod.HLS)
                .setContent(hlsStreamUrl, true)
                .setDeliveryMethod(DeliveryMethod.HLS)
                .setMediaFormat(format)
                .setAverageBitrate(AudioStream.UNKNOWN_BITRATE)
                .setManifestUrl(playlistUrl)
                .build()
            if (!Stream.containSimilarStream(audioStream, audioStreams)) audioStreams.add(audioStream)
        }

        // Finally, add torrent URLs
        val torrentUrl = getString(streamJsonObject, "torrentUrl")
        if (torrentUrl.isNotEmpty()) {
            audioStreams.add(AudioStream.Builder()
                .setId(id + "-" + idSuffix + "-" + DeliveryMethod.TORRENT)
                .setContent(torrentUrl, true)
                .setDeliveryMethod(DeliveryMethod.TORRENT)
                .setMediaFormat(format)
                .setAverageBitrate(AudioStream.UNKNOWN_BITRATE)
                .build())
        }
    }

    @Throws(ParsingException::class)
    private fun addNewVideoStream(streamJsonObject: JsonObject, isInstanceUsingRandomUuidsForHlsStreams: Boolean, resolution: String,
                                  idSuffix: String, url: String, playlistUrl: String?) {
        val extension = url.substring(url.lastIndexOf(".") + 1)
        val format = getFromSuffix(extension)
        val id = "$resolution-$extension"

        // Add progressive HTTP streams first
        videoStreams.add(VideoStream.Builder()
            .setId(id + "-" + idSuffix + "-" + DeliveryMethod.PROGRESSIVE_HTTP)
            .setContent(url, true)
            .setIsVideoOnly(false)
            .setResolution(resolution)
            .setMediaFormat(format)
            .build())

        // Then add HLS streams
        if (!playlistUrl.isNullOrEmpty()) {
            val hlsStreamUrl = if (isInstanceUsingRandomUuidsForHlsStreams) getHlsPlaylistUrlFromFragmentedFileUrl(streamJsonObject, idSuffix, extension, url)
            else getHlsPlaylistUrlFromMasterPlaylist(streamJsonObject, playlistUrl)

            val videoStream = VideoStream.Builder()
                .setId(id + "-" + DeliveryMethod.HLS)
                .setContent(hlsStreamUrl, true)
                .setIsVideoOnly(false)
                .setDeliveryMethod(DeliveryMethod.HLS)
                .setResolution(resolution)
                .setMediaFormat(format)
                .setManifestUrl(playlistUrl)
                .build()
            if (!Stream.containSimilarStream(videoStream, videoStreams)) videoStreams.add(videoStream)
        }

        // Add finally torrent URLs
        val torrentUrl = getString(streamJsonObject, "torrentUrl")
        if (torrentUrl.isNotEmpty()) {
            videoStreams.add(VideoStream.Builder()
                .setId(id + "-" + idSuffix + "-" + DeliveryMethod.TORRENT)
                .setContent(torrentUrl, true)
                .setIsVideoOnly(false)
                .setDeliveryMethod(DeliveryMethod.TORRENT)
                .setResolution(resolution)
                .setMediaFormat(format)
                .build())
        }
    }

    /**
     * Fetch content from a sub-API of the video.
     * @param subPath the API subpath after the video id,
     * e.g. "storyboards" for "/api/v1/videos/{id}/storyboards"
     * @return the [JsonObject] of the sub-API or null if the API does not exist
     * which is the case if the instance has an outdated PeerTube version.
     * @throws ParsingException if the API response could not be parsed to a [JsonObject]
     * @throws IOException if the API response could not be fetched
     * @throws ReCaptchaException if the API response is a reCaptcha
     */
    @Throws(ParsingException::class, IOException::class, ReCaptchaException::class)
    private fun fetchSubApiContent(subPath: String): JsonObject? {
        val apiUrl = (baseUrl + PeertubeStreamLinkHandlerFactory.VIDEO_API_ENDPOINT + id + "/" + subPath)
        val response = downloader.get(apiUrl)
        // Chapter or segments support was added with PeerTube v6.0.0
        // This instance does not support it yet.
        if (response.responseCode() == 400) return null
        if (response.responseCode() != 200) throw ParsingException("Could not get segments from API. Response code: " + response.responseCode())
        try {
            return JsonParser.`object`().from(response.responseBody())
        } catch (e: JsonParserException) {
            throw ParsingException("Could not parse json data for segments", e)
        }
    }


    @Throws(ParsingException::class)
    override fun getName(): String {
        return getString(json!!, "name")
    }

    @get:Throws(ParsingException::class)

    override val host: String
        get() = getString(json!!, ACCOUNT_HOST)


    override val privacy: Privacy
        get() = when (json!!.getObject("privacy").getInt("id")) {
            1 -> Privacy.PUBLIC
            2 -> Privacy.UNLISTED
            3 -> Privacy.PRIVATE
            4 -> Privacy.INTERNAL
            else -> Privacy.OTHER
        }

    @get:Throws(ParsingException::class)

    override val category: String
        get() = getString(json!!, "category.label")

    @get:Throws(ParsingException::class)

    override val licence: String
        get() = getString(json!!, "licence.label")

    override val languageInfo: Locale?
        get() = try {
            Locale(getString(json!!, "language.id"))
        } catch (e: ParsingException) {
            null
        }

    companion object {
        private const val ACCOUNT_HOST = "account.host"
        private const val ACCOUNT_NAME = "account.name"
        private const val FILES = "files"
        private const val FILE_DOWNLOAD_URL = "fileDownloadUrl"
        private const val FILE_URL = "fileUrl"
        private const val PLAYLIST_URL = "playlistUrl"
        private const val RESOLUTION_ID = "resolution.id"
        private const val STREAMING_PLAYLISTS = "streamingPlaylists"
    }
}
