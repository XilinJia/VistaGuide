// Created by Fynn Godau 2019, licensed GNU GPL version 3 or later
package ac.mdiq.vista.extractor.services.bandcamp.extractors

import org.jsoup.nodes.Element
import ac.mdiq.vista.extractor.Image
import ac.mdiq.vista.extractor.channel.ChannelInfoItemExtractor
import ac.mdiq.vista.extractor.exceptions.ParsingException


class BandcampChannelInfoItemExtractor(private val searchResult: Element) : ChannelInfoItemExtractor {
    private val resultInfo: Element? = searchResult.getElementsByClass("result-info").first()

    @get:Throws(ParsingException::class)
    override val name: String
        get() = resultInfo?.getElementsByClass("heading")?.text() ?: ""

    @get:Throws(ParsingException::class)
    override val url: String
        get() = resultInfo?.getElementsByClass("itemurl")?.text() ?: ""

    @get:Throws(ParsingException::class)

    override val thumbnails: List<Image>
        get() = BandcampExtractorHelper.getImagesFromSearchResult(searchResult)

    override fun getDescription(): String {
        return resultInfo?.getElementsByClass("subhead")?.text() ?:""
    }

    override fun getSubscriberCount(): Long {
        return -1
    }

    override fun getStreamCount(): Long {
        return -1
    }

    @Throws(ParsingException::class)
    override fun isVerified(): Boolean {
        return false
    }
}
