package ac.mdiq.vista.extractor.services.media_ccc.extractors

import com.grack.nanojson.JsonObject
import com.grack.nanojson.JsonParser
import com.grack.nanojson.JsonParserException
import ac.mdiq.vista.extractor.Image
import ac.mdiq.vista.extractor.MediaFormat
import ac.mdiq.vista.extractor.StreamingService
import ac.mdiq.vista.extractor.downloader.Downloader
import ac.mdiq.vista.extractor.exceptions.ExtractionException
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.linkhandler.LinkHandler
import ac.mdiq.vista.extractor.localization.DateWrapper
import ac.mdiq.vista.extractor.localization.Localization.Companion.getLocaleFromThreeLetterCode
import ac.mdiq.vista.extractor.services.media_ccc.extractors.MediaCCCParsingHelper.getImageListFromLogoImageUrl
import ac.mdiq.vista.extractor.services.media_ccc.extractors.MediaCCCParsingHelper.getThumbnailsFromStreamItem
import ac.mdiq.vista.extractor.services.media_ccc.extractors.MediaCCCParsingHelper.parseDateFrom
import ac.mdiq.vista.extractor.services.media_ccc.linkHandler.MediaCCCConferenceLinkHandlerFactory
import ac.mdiq.vista.extractor.services.media_ccc.linkHandler.MediaCCCStreamLinkHandlerFactory
import ac.mdiq.vista.extractor.stream.*
import ac.mdiq.vista.extractor.utils.JsonUtils.getStringListFromJsonArray
import ac.mdiq.vista.extractor.utils.LocaleCompat.forLanguageTag
import java.io.IOException
import java.util.*


class MediaCCCStreamExtractor(service: StreamingService, linkHandler: LinkHandler) : StreamExtractor(service, linkHandler) {
    private var data: JsonObject? = null
    private var conferenceData: JsonObject? = null

    override val textualUploadDate: String
        get() = data!!.getString("release_date")

    @get:Throws(ParsingException::class)
    override val uploadDate: DateWrapper
        get() = DateWrapper(parseDateFrom(textualUploadDate))

    override val thumbnails: List<Image>
        get() = getThumbnailsFromStreamItem(data!!)

    override val description: Description
        get() = Description(data!!.getString("description"), Description.PLAIN_TEXT)

    override val length: Long
        get() = data!!.getInt("length").toLong()

    override val viewCount: Long
        get() = data!!.getInt("view_count").toLong()

    override val uploaderUrl: String
        get() = MediaCCCConferenceLinkHandlerFactory.CONFERENCE_PATH + uploaderName

    override val uploaderName: String
        get() = data!!.getString("conference_url")
            .replaceFirst("https://(api\\.)?media\\.ccc\\.de/public/conferences/".toRegex(), "")

    override val uploaderAvatars: List<Image>
        get() = getImageListFromLogoImageUrl(conferenceData!!.getString("logo_url"))

    @get:Throws(ExtractionException::class)
    override val audioStreams: List<AudioStream>
        get() {
            val recordings = data!!.getArray("recordings")
            val audioStreams: MutableList<AudioStream> = ArrayList()
            for (i in recordings.indices) {
                val recording = recordings.getObject(i)
                val mimeType = recording.getString("mime_type")
                if (mimeType.startsWith("audio")) {
                    // First we need to resolve the actual video data from the CDN
                    val mediaFormat = when {
                        mimeType.endsWith("opus") -> MediaFormat.OPUS
                        mimeType.endsWith("mpeg") -> MediaFormat.MP3
                        mimeType.endsWith("ogg") -> MediaFormat.OGG
                        else -> null
                    }

                    val builder = AudioStream.Builder()
                        .setId(recording.getString("filename", Stream.ID_UNKNOWN))
                        .setContent(recording.getString("recording_url"), true)
                        .setMediaFormat(mediaFormat)
                        .setAverageBitrate(AudioStream.UNKNOWN_BITRATE)

                    val language = recording.getString("language")
                    // If the language contains a - symbol, this means that the stream has an audio
                    // track with multiple languages, so there is no specific language for this stream
                    // Don't set the audio language in this case
                    if (language != null && !language.contains("-"))
                        builder.setAudioLocale(forLanguageTag(language) ?: throw ParsingException("Cannot convert this language to a locale: $language"))

                    // Not checking containsSimilarStream here, since MediaCCC does not provide enough
                    // information to decide whether two streams are similar. Hence that method would
                    // always return false, e.g. even for different language variations.
                    audioStreams.add(builder.build())
                }
            }
            return audioStreams
        }

    @get:Throws(ExtractionException::class)
    override val videoStreams: List<VideoStream>
        get() {
            val recordings = data!!.getArray("recordings")
            val videoStreams: MutableList<VideoStream> = ArrayList()
            for (i in recordings.indices) {
                val recording = recordings.getObject(i)
                val mimeType = recording.getString("mime_type")
                if (mimeType.startsWith("video")) {
                    // First we need to resolve the actual video data from the CDN
                    val mediaFormat = when {
                        mimeType.endsWith("webm") -> MediaFormat.WEBM
                        mimeType.endsWith("mp4") -> MediaFormat.MPEG_4
                        else -> null
                    }

                    // Not checking containsSimilarStream here, since MediaCCC does not provide enough
                    // information to decide whether two streams are similar. Hence that method would
                    // always return false, e.g. even for different language variations.
                    videoStreams.add(VideoStream.Builder()
                        .setId(recording.getString("filename", Stream.ID_UNKNOWN))
                        .setContent(recording.getString("recording_url"), true)
                        .setIsVideoOnly(false)
                        .setMediaFormat(mediaFormat)
                        .setResolution(recording.getInt("height").toString() + "p")
                        .build())
                }
            }

            return videoStreams
        }

    override val videoOnlyStreams: List<VideoStream>
        get() = emptyList()

    override val streamType: StreamType
        get() = StreamType.VIDEO_STREAM

    @Throws(IOException::class, ExtractionException::class)
    override fun onFetchPage(downloader: Downloader) {
        val videoUrl = MediaCCCStreamLinkHandlerFactory.VIDEO_API_ENDPOINT + id
        try {
            data = JsonParser.`object`().from(downloader.get(videoUrl).responseBody())
            if (data != null) conferenceData = JsonParser.`object`().from(downloader.get(data!!.getString("conference_url")).responseBody())
        } catch (jpe: JsonParserException) { throw ExtractionException("Could not parse json returned by URL: $videoUrl", jpe) }
    }


    @Throws(ParsingException::class)
    override fun getName(): String {
        return data!!.getString("title")
    }


    override val originalUrl: String
        get() = data!!.getString("frontend_link")

    @get:Throws(ParsingException::class)
    override val languageInfo: Locale?
        get() = getLocaleFromThreeLetterCode(data!!.getString("original_language"))


    override val tags: List<String>
        get() = getStringListFromJsonArray(data!!.getArray("tags"))
}
