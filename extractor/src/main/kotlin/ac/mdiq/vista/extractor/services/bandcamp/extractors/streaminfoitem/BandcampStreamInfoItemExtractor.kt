package ac.mdiq.vista.extractor.services.bandcamp.extractors.streaminfoitem

import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.localization.DateWrapper
import ac.mdiq.vista.extractor.stream.StreamInfoItemExtractor
import ac.mdiq.vista.extractor.stream.StreamType

/**
 * Implements methods that return a constant value in subclasses for better readability.
 */
abstract class BandcampStreamInfoItemExtractor(private val uploaderUrl: String) : StreamInfoItemExtractor {
    override fun getStreamType(): StreamType {
        return StreamType.AUDIO_STREAM
    }

    override fun getViewCount(): Long {
        return -1
    }

    override fun getUploaderUrl(): String {
        return uploaderUrl
    }

    override fun getTextualUploadDate(): String? {
        return null
    }

    override fun getUploadDate(): DateWrapper? {
        return null
    }

    @Throws(ParsingException::class)
    override fun isUploaderVerified(): Boolean {
        return false
    }

    override fun isAd(): Boolean {
        return false
    }
}
