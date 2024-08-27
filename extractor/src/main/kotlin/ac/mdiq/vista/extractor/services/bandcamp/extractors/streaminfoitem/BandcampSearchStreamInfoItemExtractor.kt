package ac.mdiq.vista.extractor.services.bandcamp.extractors.streaminfoitem

import org.jsoup.nodes.Element
import ac.mdiq.vista.extractor.Image
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.services.bandcamp.extractors.BandcampExtractorHelper


class BandcampSearchStreamInfoItemExtractor(
        private val searchResult: Element,
        uploaderUrl: String)
    : BandcampStreamInfoItemExtractor(uploaderUrl) {
    private val resultInfo: Element = searchResult.getElementsByClass("result-info").first()

    override fun getUploaderName(): String? {
        val subhead = resultInfo.getElementsByClass("subhead").text()
        val splitBy = subhead.split("by ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        return if (splitBy.size > 1) {
            splitBy[1]
        } else {
            splitBy[0]
        }
    }

    @get:Throws(ParsingException::class)
    override val name: String
        get() = resultInfo.getElementsByClass("heading").text()

    @get:Throws(ParsingException::class)
    override val url: String
        get() = resultInfo.getElementsByClass("itemurl").text()

    @get:Throws(ParsingException::class)

    override val thumbnails: List<Image>
        get() = BandcampExtractorHelper.getImagesFromSearchResult(searchResult)

    override fun getDuration(): Long {
        return -1
    }
}
