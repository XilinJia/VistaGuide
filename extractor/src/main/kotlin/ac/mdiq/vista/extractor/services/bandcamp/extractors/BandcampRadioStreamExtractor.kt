package ac.mdiq.vista.extractor.services.bandcamp.extractors

import com.grack.nanojson.JsonObject
import com.grack.nanojson.JsonParser
import com.grack.nanojson.JsonParserException
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import ac.mdiq.vista.extractor.Image
import ac.mdiq.vista.extractor.Image.ResolutionLevel
import ac.mdiq.vista.extractor.MediaFormat
import ac.mdiq.vista.extractor.Vista.downloader
import ac.mdiq.vista.extractor.StreamingService
import ac.mdiq.vista.extractor.downloader.Downloader
import ac.mdiq.vista.extractor.exceptions.ContentNotSupportedException
import ac.mdiq.vista.extractor.exceptions.ExtractionException
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.exceptions.ReCaptchaException
import ac.mdiq.vista.extractor.linkhandler.LinkHandler
import ac.mdiq.vista.extractor.playlist.PlaylistInfoItemsCollector
import ac.mdiq.vista.extractor.services.bandcamp.extractors.BandcampExtractorHelper.BASE_API_URL
import ac.mdiq.vista.extractor.services.bandcamp.extractors.BandcampExtractorHelper.BASE_URL
import ac.mdiq.vista.extractor.services.bandcamp.extractors.BandcampExtractorHelper.getImageUrl
import ac.mdiq.vista.extractor.services.bandcamp.extractors.BandcampExtractorHelper.getImagesFromImageId
import ac.mdiq.vista.extractor.stream.AudioStream
import ac.mdiq.vista.extractor.stream.Description
import ac.mdiq.vista.extractor.stream.StreamSegment
import java.io.IOException


class BandcampRadioStreamExtractor(service: StreamingService, linkHandler: LinkHandler) : BandcampStreamExtractor(service, linkHandler) {
    private var showInfo: JsonObject? = null

    @Throws(IOException::class, ExtractionException::class)
    override fun onFetchPage(downloader: Downloader) {
        showInfo = query(id.toInt())
    }


    @Throws(ParsingException::class)
    override fun getName(): String {
        /* Select "subtitle" and not "audio_title", as the latter would cause a lot of
         * items to show the same title, e.g. "Bandcamp Weekly".
         */
        return showInfo!!.getString("subtitle")
    }

    @get:Throws(ContentNotSupportedException::class)

    override val uploaderUrl: String
        get() {
            throw ContentNotSupportedException("Fan pages are not supported")
        }

    @get:Throws(ParsingException::class)

    override val url: String
        get() = getLinkHandler().url

    @get:Throws(ParsingException::class)

    override val uploaderName: String
        get() = Jsoup.parse(showInfo!!.getString("image_caption")).getElementsByTag("a").stream()
            .map { obj: Element -> obj.text() }
            .findFirst()
            .orElseThrow { ParsingException("Could not get uploader name") }

    override val textualUploadDate: String?
        get() = showInfo!!.getString("published_date")

    @get:Throws(ParsingException::class)

    override val thumbnails: List<Image>
        get() = getImagesFromImageId(showInfo!!.getLong("show_image_id"), false)


    override val uploaderAvatars: List<Image>
        get() = listOf(Image("$BASE_URL/img/buttons/bandcamp-button-circle-whitecolor-512.png", 512, 512, ResolutionLevel.MEDIUM))


    override val description: Description
        get() = Description(showInfo!!.getString("desc"), Description.PLAIN_TEXT)

    override val length: Long
        get() = showInfo!!.getLong("audio_duration")

    override val audioStreams: List<AudioStream>
        get() {
            val audioStreams: MutableList<AudioStream> = ArrayList()
            val streams = showInfo!!.getObject("audio_stream")

            if (streams.has(MP3_128)) {
                audioStreams.add(AudioStream.Builder()
                    .setId(MP3_128)
                    .setContent(streams.getString(MP3_128), true)
                    .setMediaFormat(MediaFormat.MP3)
                    .setAverageBitrate(128)
                    .build())
            }

            if (streams.has(OPUS_LO)) {
                audioStreams.add(AudioStream.Builder()
                    .setId(OPUS_LO)
                    .setContent(streams.getString(OPUS_LO), true)
                    .setMediaFormat(MediaFormat.OPUS)
                    .setAverageBitrate(100).build())
            }

            return audioStreams
        }

    @get:Throws(ParsingException::class)

    override val streamSegments: List<StreamSegment>
        get() {
            val tracks = showInfo!!.getArray("tracks")
            val segments: MutableList<StreamSegment> = ArrayList(tracks.size)
            for (t in tracks) {
                val track = t as JsonObject
                val segment = StreamSegment(
                    track.getString("title"), track.getInt("timecode"))
                // "track art" is the track's album cover
                segment.previewUrl = getImageUrl(track.getLong("track_art_id"), true)
                segment.channelName = track.getString("artist")
                segments.add(segment)
            }
            return segments
        }


    override val licence: String
        get() =// Contrary to other Bandcamp streams, radio streams don't have a license
            ""


    override val category: String
        get() =// Contrary to other Bandcamp streams, radio streams don't have categories
            ""


    override val tags: List<String>
        get() =// Contrary to other Bandcamp streams, radio streams don't have tags
            emptyList()

    // Contrary to other Bandcamp streams, radio streams don't have related items
    override val relatedItems: PlaylistInfoItemsCollector?
        get() = null

    companion object {
        private const val OPUS_LO = "opus-lo"
        private const val MP3_128 = "mp3-128"
        @Throws(ParsingException::class)
        fun query(id: Int): JsonObject {
            try {
                return JsonParser.`object`().from(downloader.get("$BASE_API_URL/bcweekly/1/get?id=$id").responseBody())
            } catch (e: IOException) {
                throw ParsingException("could not get show data", e)
            } catch (e: ReCaptchaException) {
                throw ParsingException("could not get show data", e)
            } catch (e: JsonParserException) {
                throw ParsingException("could not get show data", e)
            }
        }
    }
}
