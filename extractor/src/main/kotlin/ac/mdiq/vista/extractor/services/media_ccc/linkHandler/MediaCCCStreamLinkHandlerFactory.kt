package ac.mdiq.vista.extractor.services.media_ccc.linkHandler

import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.linkhandler.LinkHandlerFactory
import ac.mdiq.vista.extractor.services.media_ccc.extractors.MediaCCCParsingHelper
import ac.mdiq.vista.extractor.utils.Parser.RegexException
import ac.mdiq.vista.extractor.utils.Parser.matchGroup1

class MediaCCCStreamLinkHandlerFactory private constructor() : LinkHandlerFactory() {
    @Throws(ParsingException::class, UnsupportedOperationException::class)
    override fun getId(url: String): String {
        var streamId: String? = null
        try {
            streamId = matchGroup1(LIVE_STREAM_ID_PATTERN, url!!)
        } catch (ignored: RegexException) { }

        if (streamId == null) return matchGroup1(RECORDING_ID_PATTERN, url!!)
        return streamId
    }

    @Throws(ParsingException::class, UnsupportedOperationException::class)
    override fun getUrl(id: String): String {
        if (MediaCCCParsingHelper.isLiveStreamId(id)) return LIVE_STREAM_PATH + id
        return VIDEO_PATH + id
    }

    override fun onAcceptUrl(url: String): Boolean {
        return try {
            getId(url) != null
        } catch (e: ParsingException) {
            false
        }
    }

    companion object {
        val instance: MediaCCCStreamLinkHandlerFactory = MediaCCCStreamLinkHandlerFactory()

        const val VIDEO_API_ENDPOINT: String = "https://api.media.ccc.de/public/events/"
        private const val VIDEO_PATH = "https://media.ccc.de/v/"
        private const val RECORDING_ID_PATTERN = ("(?:(?:(?:api\\.)?media\\.ccc\\.de/public/events/)"
                + "|(?:media\\.ccc\\.de/v/))([^/?&#]*)")
        private const val LIVE_STREAM_PATH = "https://streaming.media.ccc.de/"
        private const val LIVE_STREAM_ID_PATTERN = "streaming\\.media\\.ccc\\.de\\/(\\w+\\/\\w+)"
    }
}
