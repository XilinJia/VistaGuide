package ac.mdiq.vista.extractor

import ac.mdiq.vista.extractor.exceptions.ParsingException

interface InfoItemExtractor {
    @get:Throws(ParsingException::class)
    val name: String

    @get:Throws(ParsingException::class)
    val url: String

    @get:Throws(ParsingException::class)
    val thumbnails: List<Image>
}
