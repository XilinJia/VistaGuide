package ac.mdiq.vista.extractor.services.media_ccc.extractors

import com.grack.nanojson.JsonObject
import ac.mdiq.vista.extractor.Image
import ac.mdiq.vista.extractor.MediaFormat.Companion.getFromSuffix
import ac.mdiq.vista.extractor.StreamingService
import ac.mdiq.vista.extractor.downloader.Downloader
import ac.mdiq.vista.extractor.exceptions.ExtractionException
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.linkhandler.LinkHandler
import ac.mdiq.vista.extractor.stream.*
import java.io.IOException
import java.util.function.Function
import java.util.stream.Collectors


class MediaCCCLiveStreamExtractor(service: StreamingService, linkHandler: LinkHandler) : StreamExtractor(service, linkHandler) {
    private var conference: JsonObject? = null


    override var category: String = ""
        private set
    private var room: JsonObject? = null

    @Throws(IOException::class, ExtractionException::class)
    override fun onFetchPage(downloader: Downloader) {
        val doc = MediaCCCParsingHelper.getLiveStreams(downloader, extractorLocalization) ?: return
        // Find the correct room
        for (c in doc.indices) {
            val conferenceObject = doc.getObject(c)
            val groups = conferenceObject.getArray("groups")
            for (g in groups.indices) {
                val groupObject = groups.getObject(g).getString("group")
                val rooms = groups.getObject(g).getArray("rooms")
                for (r in rooms.indices) {
                    val roomObject = rooms.getObject(r)
                    if (id == (conferenceObject.getString("slug") + "/"
                                    + roomObject.getString("slug"))) {
                        conference = conferenceObject
                        this.category = groupObject
                        room = roomObject
                        return
                    }
                }
            }
        }
        throw ExtractionException("Could not find room matching id: '$id'")
    }


    @Throws(ParsingException::class)
    override fun getName(): String {
        return room!!.getString("display")
    }

    @get:Throws(ParsingException::class)

    override val thumbnails: List<Image>
        get() = MediaCCCParsingHelper.getThumbnailsFromLiveStreamItem(room!!)

    @get:Throws(ParsingException::class)

    override val description: Description
        get() = Description(conference!!.getString("description")
                + " - " + this.category, Description.PLAIN_TEXT)

    override val viewCount: Long
        get() = -1

    @get:Throws(ParsingException::class)

    override val uploaderUrl: String
        get() = "https://streaming.media.ccc.de/" + conference!!.getString("slug")

    @get:Throws(ParsingException::class)

    override val uploaderName: String
        get() = conference!!.getString("conference")

    @get:Throws(ParsingException::class)

    override val dashMpdUrl: String
        /**
         * Get the URL of the first DASH stream found.
         *
         *
         *
         * There can be several DASH streams, so the URL of the first one found is returned by this
         * method.
         *
         *
         *
         *
         * You can find the other DASH video streams by using [.getVideoStreams]
         *
         */
        get() = getManifestOfDeliveryMethodWanted("dash")


    override val hlsUrl: String
        /**
         * Get the URL of the first HLS stream found.
         *
         *
         *
         * There can be several HLS streams, so the URL of the first one found is returned by this
         * method.
         *
         *
         *
         *
         * You can find the other HLS video streams by using [.getVideoStreams]
         *
         */
        get() = getManifestOfDeliveryMethodWanted("hls")


    private fun getManifestOfDeliveryMethodWanted(deliveryMethod: String): String {
        return room!!.getArray(STREAMS).stream()
            .filter { o: Any? -> JsonObject::class.java.isInstance(o) }
            .map { obj: Any? -> JsonObject::class.java.cast(obj) }
            .map { streamObject: JsonObject -> streamObject.getObject(URLS) }
            .filter { urls: JsonObject -> urls.has(deliveryMethod) }
            .map { urls: JsonObject -> urls.getObject(deliveryMethod).getString(URL, "") }
            .findFirst()
            .orElse("")
    }

    @get:Throws(IOException::class, ExtractionException::class)
    override val audioStreams: List<AudioStream>
        get() = getStreams("audio"
        ) { dto: MediaCCCLiveStreamMapperDTO ->
            val builder = AudioStream.Builder()
                .setId(dto.urlValue.getString("tech", Stream.ID_UNKNOWN))
                .setContent(dto.urlValue.getString(URL), true)
                .setAverageBitrate(AudioStream.UNKNOWN_BITRATE)
            if ("hls" == dto.urlKey) {
                // We don't know with the type string what media format will
                // have HLS streams.
                // However, the tech string may contain some information
                // about the media format used.
                return@getStreams builder.setDeliveryMethod(DeliveryMethod.HLS)
                    .build()
            }
            builder.setMediaFormat(getFromSuffix(dto.urlKey))
                .build()
        }

    @get:Throws(IOException::class, ExtractionException::class)
    override val videoStreams: List<VideoStream>
        get() = getStreams("video"
        ) { dto: MediaCCCLiveStreamMapperDTO ->
            val videoSize = dto.streamJsonObj.getArray("videoSize")
            val builder = VideoStream.Builder()
                .setId(dto.urlValue.getString("tech", Stream.ID_UNKNOWN))
                .setContent(dto.urlValue.getString(URL), true)
                .setIsVideoOnly(false)
                .setResolution(videoSize.getInt(0).toString() + "x" + videoSize.getInt(1))

            if ("hls" == dto.urlKey) {
                // We don't know with the type string what media format will
                // have HLS streams.
                // However, the tech string may contain some information
                // about the media format used.
                return@getStreams builder.setDeliveryMethod(DeliveryMethod.HLS)
                    .build()
            }
            builder.setMediaFormat(getFromSuffix(dto.urlKey))
                .build()
        }


    /**
     * This is just an internal class used in [.getStreams] to tie together
     * the stream json object, its URL key and its URL value. An object of this class would be
     * temporary and the three values it holds would be **convert**ed to a proper [Stream]
     * object based on the wanted stream type.
     */
    private class MediaCCCLiveStreamMapperDTO(val streamJsonObj: JsonObject,
                                              val urlKey: String,
                                              val urlValue: JsonObject
    )

    private fun <T : Stream?> getStreams(
            streamType: String,
            converter: Function<MediaCCCLiveStreamMapperDTO, T>
    ): List<T> {
        return room!!.getArray(STREAMS).stream() // Ensure that we use only process JsonObjects
            .filter { o: Any? -> JsonObject::class.java.isInstance(o) }
            .map { obj: Any? -> JsonObject::class.java.cast(obj) } // Only process streams of requested type
            .filter { streamJsonObj: JsonObject -> streamType == streamJsonObj.getString("type") } // Flatmap Urls and ensure that we use only process JsonObjects
            .flatMap { streamJsonObj: JsonObject ->
                streamJsonObj.getObject(URLS).entries.stream()
                    .filter { e: Map.Entry<String?, Any?> -> e.value is JsonObject }
                    .map { e: Map.Entry<String, Any> ->
                        MediaCCCLiveStreamMapperDTO(
                            streamJsonObj,
                            e.key,
                            e.value as JsonObject)
                    }
            } // The DASH manifest will be extracted with getDashMpdUrl
            .filter { dto: MediaCCCLiveStreamMapperDTO -> "dash" != dto.urlKey } // Convert
            .map(converter)
            .collect(Collectors.toList())
    }

    override val videoOnlyStreams: List<VideoStream>
        get() = emptyList()

    @get:Throws(ParsingException::class)
    override val streamType: StreamType
        get() = StreamType.LIVE_STREAM

    companion object {
        private const val STREAMS = "streams"
        private const val URLS = "urls"
        private const val URL = "url"
    }
}
