package ac.mdiq.vista.extractor.services.youtube

import ac.mdiq.vista.extractor.MediaFormat
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.stream.AudioTrackType
import java.io.Serializable
import java.util.*


class ItagItem : Serializable {
    enum class ItagType {
        AUDIO,
        VIDEO,
        VIDEO_ONLY
    }

    /**
     * Call [.ItagItem] with the fps set to 30.
     */
    constructor(id: Int, type: ItagType, format: MediaFormat, resolution: String?) {
        this.id = id
        this.itagType = type
        this.mediaFormat = format
        this.resolutionString = resolution
        this.fps = 30
    }

    /**
     * Constructor for videos.
     */
    constructor(id: Int, type: ItagType, format: MediaFormat, resolution: String?, fps: Int) {
        this.id = id
        this.itagType = type
        this.mediaFormat = format
        this.resolutionString = resolution
        this.fps = fps
    }

    constructor(id: Int, type: ItagType, format: MediaFormat, avgBitrate: Int) {
        this.id = id
        this.itagType = type
        this.mediaFormat = format
        this.averageBitrate = avgBitrate
    }

    /**
     * Copy constructor of the [ItagItem] class.
     *
     * @param itagItem the [ItagItem] to copy its properties into a new [ItagItem]
     */
    constructor(itagItem: ItagItem) {
        this.mediaFormat = itagItem.mediaFormat
        this.id = itagItem.id
        this.itagType = itagItem.itagType
        this.averageBitrate = itagItem.averageBitrate
        this.sampleRate = itagItem.sampleRate
        this.audioChannels = itagItem.audioChannels
        this.resolutionString = itagItem.resolutionString
        this.fps = itagItem.fps
        this.bitrate = itagItem.bitrate
        this.width = itagItem.width
        this.height = itagItem.height
        this.initStart = itagItem.initStart
        this.initEnd = itagItem.initEnd
        this.indexStart = itagItem.indexStart
        this.indexEnd = itagItem.indexEnd
        this.quality = itagItem.quality
        this.codec = itagItem.codec
        this.targetDurationSec = itagItem.targetDurationSec
        this.approxDurationMs = itagItem.approxDurationMs
        this.contentLength = itagItem.contentLength
        this.audioTrackId = itagItem.audioTrackId
        this.audioTrackName = itagItem.audioTrackName
        this.audioTrackType = itagItem.audioTrackType
        this.audioLocale = itagItem.audioLocale
    }

    @JvmField
    val mediaFormat: MediaFormat

    @JvmField
    val id: Int
    @JvmField
    val itagType: ItagType

    // Audio fields

    /**
     * Get the average bitrate.
     *
     * It is only known for audio itags, so [.AVERAGE_BITRATE_UNKNOWN] is always returned for
     * other itag types.
     *
     * Bitrate of video itags and precise bitrate of audio itags can be known using
     * [.getBitrate].
     *
     * @return the average bitrate or [.AVERAGE_BITRATE_UNKNOWN]
     * @see .getBitrate
     */
    var averageBitrate: Int = AVERAGE_BITRATE_UNKNOWN
    private var sampleRate = SAMPLE_RATE_UNKNOWN
    private var audioChannels = AUDIO_CHANNELS_NOT_APPLICABLE_OR_UNKNOWN

    // Video fields

    /**
     * Get the resolution string associated with this `ItagItem`.
     *
     * It is only known for video itags.
     *
     * @return the resolution string associated with this `ItagItem` or
     * `null`.
     */
    @JvmField
    var resolutionString: String? = null

    private var fps: Int = FPS_NOT_APPLICABLE_OR_UNKNOWN

    // Fields for Dash
    @JvmField
    var bitrate: Int = 0
    @JvmField
    var width: Int = 0
    @JvmField
    var height: Int = 0
    @JvmField
    var initStart: Int = 0
    @JvmField
    var initEnd: Int = 0
    @JvmField
    var indexStart: Int = 0
    @JvmField
    var indexEnd: Int = 0
    @JvmField
    var quality: String? = null
    @JvmField
    var codec: String? = null
    private var targetDurationSec = TARGET_DURATION_SEC_UNKNOWN
    private var approxDurationMs = APPROX_DURATION_MS_UNKNOWN
    private var contentLength = CONTENT_LENGTH_UNKNOWN
    /**
     * Get the `audioTrackId` of the stream, if present.
     * @return the `audioTrackId` of the stream or null
     * Set the `audioTrackId` of the stream.
     * @param audioTrackId the `audioTrackId` of the stream
     */
    @JvmField
    var audioTrackId: String? = null

    /**
     * Get the `audioTrackName` of the stream, if present.
     * @return the `audioTrackName` of the stream or `null`
     */
    /**
     * Set the `audioTrackName` of the stream, if present.
     * @param audioTrackName the `audioTrackName` of the stream or `null`
     */
    @JvmField
    var audioTrackName: String? = null

    /**
     * Get the [AudioTrackType] of the stream.
     *
     * @return the [AudioTrackType] of the stream or `null`
     */
    /**
     * Set the [AudioTrackType] of the stream, if present.
     *
     * @param audioTrackType the [AudioTrackType] of the stream or `null`
     */
    @JvmField
    var audioTrackType: AudioTrackType? = null

    /**
     * Return the audio [Locale] of the stream, if known.
     *
     * @return the audio [Locale] of the stream, if known, or `null` if that's not the
     * case
     */
    /**
     * Set the audio [Locale] of the stream.
     *
     *
     *
     * If it is unknown, `null` could be passed, which is the default value.
     *
     *
     * @param audioLocale the audio [Locale] of the stream, which could be `null`
     */
    @JvmField
    var audioLocale: Locale? = null

    /**
     * Get the frame rate.
     *
     * It is set to the `fps` value returned in the corresponding itag in the YouTube player response.
     *
     * It defaults to the standard value associated with this itag.
     *
     * Note that this value is only known for video itags, so [ ][.FPS_NOT_APPLICABLE_OR_UNKNOWN] is returned for non video itags.
     *
     * @return the frame rate or [.FPS_NOT_APPLICABLE_OR_UNKNOWN]
     */
    fun getFps(): Int {
        return fps
    }

    /**
     * Set the frame rate.
     *
     * It is only known for video itags, so [.FPS_NOT_APPLICABLE_OR_UNKNOWN] is set/used for
     * non video itags or if the sample rate value is less than or equal to 0.
     *
     * @param fps the frame rate
     */
    fun setFps(fps: Int) {
        this.fps = if (fps > 0) fps else FPS_NOT_APPLICABLE_OR_UNKNOWN
    }

    /**
     * Get the sample rate.
     *
     * It is only known for audio itags, so [.SAMPLE_RATE_UNKNOWN] is returned for non audio
     * itags, or if the sample rate is unknown.
     *
     * @return the sample rate or [.SAMPLE_RATE_UNKNOWN]
     */
    fun getSampleRate(): Int {
        return sampleRate
    }

    /**
     * Set the sample rate.
     *
     * It is only known for audio itags, so [.SAMPLE_RATE_UNKNOWN] is set/used for non audio
     * itags, or if the sample rate value is less than or equal to 0.
     *
     * @param sampleRate the sample rate of an audio itag
     */
    fun setSampleRate(sampleRate: Int) {
        this.sampleRate = if (sampleRate > 0) sampleRate else SAMPLE_RATE_UNKNOWN
    }

    /**
     * Get the number of audio channels.
     *
     * It is only known for audio itags, so [.AUDIO_CHANNELS_NOT_APPLICABLE_OR_UNKNOWN] is
     * returned for non audio itags, or if it is unknown.
     *
     * @return the number of audio channels or [.AUDIO_CHANNELS_NOT_APPLICABLE_OR_UNKNOWN]
     */
    fun getAudioChannels(): Int {
        return audioChannels
    }

    /**
     * Set the number of audio channels.
     *
     * It is only known for audio itags, so [.AUDIO_CHANNELS_NOT_APPLICABLE_OR_UNKNOWN] is
     * set/used for non audio itags, or if the `audioChannels` value is less than or equal to 0.
     *
     * @param audioChannels the number of audio channels of an audio itag
     */
    fun setAudioChannels(audioChannels: Int) {
        this.audioChannels = if (audioChannels > 0) audioChannels else AUDIO_CHANNELS_NOT_APPLICABLE_OR_UNKNOWN
    }

    /**
     * Get the `targetDurationSec` value.
     *
     *
     *
     * This value is the average time in seconds of the duration of sequences of livestreams and
     * ended livestreams. It is only returned by YouTube for these stream types, and makes no sense
     * for videos, so [.TARGET_DURATION_SEC_UNKNOWN] is returned for those.
     *
     *
     * @return the `targetDurationSec` value or [.TARGET_DURATION_SEC_UNKNOWN]
     */
    fun getTargetDurationSec(): Int {
        return targetDurationSec
    }

    /**
     * Set the `targetDurationSec` value.
     *
     *
     *
     * This value is the average time in seconds of the duration of sequences of livestreams and
     * ended livestreams.
     *
     *
     *
     *
     * It is only returned for these stream types by YouTube and makes no sense for videos, so
     * [.TARGET_DURATION_SEC_UNKNOWN] will be set/used for video streams or if this value is
     * less than or equal to 0.
     *
     *
     * @param targetDurationSec the target duration of a segment of streams which are using the
     * live delivery method type
     */
    fun setTargetDurationSec(targetDurationSec: Int) {
        this.targetDurationSec = if (targetDurationSec > 0) targetDurationSec else TARGET_DURATION_SEC_UNKNOWN
    }

    /**
     * Get the `approxDurationMs` value.
     *
     *
     *
     * It is only known for DASH progressive streams, so [.APPROX_DURATION_MS_UNKNOWN] is
     * returned for other stream types or if this value is less than or equal to 0.
     *
     *
     * @return the `approxDurationMs` value or [.APPROX_DURATION_MS_UNKNOWN]
     */
    fun getApproxDurationMs(): Long {
        return approxDurationMs
    }

    /**
     * Set the `approxDurationMs` value.
     *
     * It is only known for DASH progressive streams, so [.APPROX_DURATION_MS_UNKNOWN] is
     * set/used for other stream types or if this value is less than or equal to 0.
     *
     * @param approxDurationMs the approximate duration of a DASH progressive stream, in
     * milliseconds
     */
    fun setApproxDurationMs(approxDurationMs: Long) {
        this.approxDurationMs = if (approxDurationMs > 0) approxDurationMs else APPROX_DURATION_MS_UNKNOWN
    }

    /**
     * Get the `contentLength` value.
     *
     * It is only known for DASH progressive streams, so [.CONTENT_LENGTH_UNKNOWN] is
     * returned for other stream types or if this value is less than or equal to 0.
     *
     * @return the `contentLength` value or [.CONTENT_LENGTH_UNKNOWN]
     */
    fun getContentLength(): Long {
        return contentLength
    }

    /**
     * Set the content length of stream.
     *
     * It is only known for DASH progressive streams, so [.CONTENT_LENGTH_UNKNOWN] is
     * set/used for other stream types or if this value is less than or equal to 0.
     *
     * @param contentLength the content length of a DASH progressive stream
     */
    fun setContentLength(contentLength: Long) {
        this.contentLength = if (contentLength > 0) contentLength else CONTENT_LENGTH_UNKNOWN
    }

    companion object {
        /**
         * List can be found here:
         * https://github.com/ytdl-org/youtube-dl/blob/e988fa4/youtube_dl/extractor/youtube.py#L1195
         */
        private val ITAG_LIST = arrayOf(
            // VIDEO     ID  Type   Format  Resolution  FPS  ////
            ItagItem(17, ItagType.VIDEO, MediaFormat.v3GPP, "144p"),
            ItagItem(36, ItagType.VIDEO, MediaFormat.v3GPP, "240p"),

            ItagItem(18, ItagType.VIDEO, MediaFormat.MPEG_4, "360p"),
            ItagItem(34, ItagType.VIDEO, MediaFormat.MPEG_4, "360p"),
            ItagItem(35, ItagType.VIDEO, MediaFormat.MPEG_4, "480p"),
            ItagItem(59, ItagType.VIDEO, MediaFormat.MPEG_4, "480p"),
            ItagItem(78, ItagType.VIDEO, MediaFormat.MPEG_4, "480p"),
            ItagItem(22, ItagType.VIDEO, MediaFormat.MPEG_4, "720p"),
            ItagItem(37, ItagType.VIDEO, MediaFormat.MPEG_4, "1080p"),
            ItagItem(38, ItagType.VIDEO, MediaFormat.MPEG_4, "1080p"),

            ItagItem(43, ItagType.VIDEO, MediaFormat.WEBM, "360p"),
            ItagItem(44, ItagType.VIDEO, MediaFormat.WEBM, "480p"),
            ItagItem(45, ItagType.VIDEO, MediaFormat.WEBM, "720p"),
            ItagItem(46, ItagType.VIDEO, MediaFormat.WEBM, "1080p"),

            // AUDIO     ID      ItagType          Format        Bitrate    //
            ItagItem(171, ItagType.AUDIO, MediaFormat.WEBMA, 128),
            ItagItem(172, ItagType.AUDIO, MediaFormat.WEBMA, 256),
            ItagItem(599, ItagType.AUDIO, MediaFormat.M4A, 32),
            ItagItem(139, ItagType.AUDIO, MediaFormat.M4A, 48),
            ItagItem(140, ItagType.AUDIO, MediaFormat.M4A, 128),
            ItagItem(141, ItagType.AUDIO, MediaFormat.M4A, 256),
            ItagItem(600, ItagType.AUDIO, MediaFormat.WEBMA_OPUS, 35),
            ItagItem(249, ItagType.AUDIO, MediaFormat.WEBMA_OPUS, 50),
            ItagItem(250, ItagType.AUDIO, MediaFormat.WEBMA_OPUS, 70),
            ItagItem(251, ItagType.AUDIO, MediaFormat.WEBMA_OPUS, 160),
            /// VIDEO ONLY  ID      Type     Format  Resolution  FPS  ////

            ItagItem(160, ItagType.VIDEO_ONLY, MediaFormat.MPEG_4, "144p"),
            ItagItem(394, ItagType.VIDEO_ONLY, MediaFormat.MPEG_4, "144p"),
            ItagItem(133, ItagType.VIDEO_ONLY, MediaFormat.MPEG_4, "240p"),
            ItagItem(395, ItagType.VIDEO_ONLY, MediaFormat.MPEG_4, "240p"),
            ItagItem(134, ItagType.VIDEO_ONLY, MediaFormat.MPEG_4, "360p"),
            ItagItem(396, ItagType.VIDEO_ONLY, MediaFormat.MPEG_4, "360p"),
            ItagItem(135, ItagType.VIDEO_ONLY, MediaFormat.MPEG_4, "480p"),
            ItagItem(212, ItagType.VIDEO_ONLY, MediaFormat.MPEG_4, "480p"),
            ItagItem(397, ItagType.VIDEO_ONLY, MediaFormat.MPEG_4, "480p"),
            ItagItem(136, ItagType.VIDEO_ONLY, MediaFormat.MPEG_4, "720p"),
            ItagItem(398, ItagType.VIDEO_ONLY, MediaFormat.MPEG_4, "720p"),
            ItagItem(298, ItagType.VIDEO_ONLY, MediaFormat.MPEG_4, "720p60", 60),
            ItagItem(137, ItagType.VIDEO_ONLY, MediaFormat.MPEG_4, "1080p"),
            ItagItem(399, ItagType.VIDEO_ONLY, MediaFormat.MPEG_4, "1080p"),
            ItagItem(299, ItagType.VIDEO_ONLY, MediaFormat.MPEG_4, "1080p60", 60),
            ItagItem(400, ItagType.VIDEO_ONLY, MediaFormat.MPEG_4, "1440p"),
            ItagItem(266, ItagType.VIDEO_ONLY, MediaFormat.MPEG_4, "2160p"),
            ItagItem(401, ItagType.VIDEO_ONLY, MediaFormat.MPEG_4, "2160p"),

            ItagItem(278, ItagType.VIDEO_ONLY, MediaFormat.WEBM, "144p"),
            ItagItem(242, ItagType.VIDEO_ONLY, MediaFormat.WEBM, "240p"),
            ItagItem(243, ItagType.VIDEO_ONLY, MediaFormat.WEBM, "360p"),
            ItagItem(244, ItagType.VIDEO_ONLY, MediaFormat.WEBM, "480p"),
            ItagItem(245, ItagType.VIDEO_ONLY, MediaFormat.WEBM, "480p"),
            ItagItem(246, ItagType.VIDEO_ONLY, MediaFormat.WEBM, "480p"),
            ItagItem(247, ItagType.VIDEO_ONLY, MediaFormat.WEBM, "720p"),
            ItagItem(248, ItagType.VIDEO_ONLY, MediaFormat.WEBM, "1080p"),
            ItagItem(271, ItagType.VIDEO_ONLY, MediaFormat.WEBM, "1440p"),
            // #272 is either 3840x2160 (e.g. RtoitU2A-3E) or 7680x4320 (sLprVF6d7Ug)
            ItagItem(272, ItagType.VIDEO_ONLY, MediaFormat.WEBM, "2160p"),
            ItagItem(302, ItagType.VIDEO_ONLY, MediaFormat.WEBM, "720p60", 60),
            ItagItem(303, ItagType.VIDEO_ONLY, MediaFormat.WEBM, "1080p60", 60),
            ItagItem(308, ItagType.VIDEO_ONLY, MediaFormat.WEBM, "1440p60", 60),
            ItagItem(313, ItagType.VIDEO_ONLY, MediaFormat.WEBM, "2160p"),
            ItagItem(315, ItagType.VIDEO_ONLY, MediaFormat.WEBM, "2160p60", 60)
        )

        fun isSupported(itag: Int): Boolean {
            for (item in ITAG_LIST) {
                if (itag == item.id) return true
            }
            return false
        }



        @Throws(ParsingException::class)
        fun getItag(itagId: Int): ItagItem {
            for (item in ITAG_LIST) {
                if (itagId == item.id) return ItagItem(item)
            }
            throw ParsingException("itag $itagId is not supported")
        }

        const val AVERAGE_BITRATE_UNKNOWN: Int = -1
        const val SAMPLE_RATE_UNKNOWN: Int = -1
        const val FPS_NOT_APPLICABLE_OR_UNKNOWN: Int = -1
        const val TARGET_DURATION_SEC_UNKNOWN: Int = -1
        const val AUDIO_CHANNELS_NOT_APPLICABLE_OR_UNKNOWN: Int = -1
        const val CONTENT_LENGTH_UNKNOWN: Long = -1
        const val APPROX_DURATION_MS_UNKNOWN: Long = -1
    }
}
