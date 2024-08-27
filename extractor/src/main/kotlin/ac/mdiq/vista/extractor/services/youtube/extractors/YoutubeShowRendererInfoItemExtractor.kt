package ac.mdiq.vista.extractor.services.youtube.extractors

import com.grack.nanojson.JsonObject
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.getTextFromObject
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.getUrlFromObject


/**
 * A [YoutubeBaseShowInfoItemExtractor] implementation for `showRenderer`s.
 */
internal class YoutubeShowRendererInfoItemExtractor(showRenderer: JsonObject) :
    YoutubeBaseShowInfoItemExtractor(showRenderer) {

    private val shortBylineText: JsonObject = showRenderer.getObject("shortBylineText")


    private val longBylineText: JsonObject = showRenderer.getObject("longBylineText")

    @Throws(ParsingException::class)
    override fun getUploaderName(): String {
        var name = getTextFromObject(longBylineText)
        if (name.isNullOrEmpty()) {
            name = getTextFromObject(shortBylineText)
            if (name.isNullOrEmpty()) throw ParsingException("Could not get uploader name")
        }
        return name
    }

    @Throws(ParsingException::class)
    override fun getUploaderUrl(): String {
        var uploaderUrl = getUrlFromObject(longBylineText)
        if (uploaderUrl == null) {
            uploaderUrl = getUrlFromObject(shortBylineText)
            if (uploaderUrl == null) throw ParsingException("Could not get uploader URL")
        }
        return uploaderUrl
    }

    @Throws(ParsingException::class)
    override fun isUploaderVerified(): Boolean {
        // We do not have this information in showRenderers
        return false
    }
}
