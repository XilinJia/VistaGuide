package ac.mdiq.vista.extractor.services.bandcamp.extractors

import org.jsoup.nodes.Element
import ac.mdiq.vista.extractor.Image
import ac.mdiq.vista.extractor.playlist.PlaylistInfoItemExtractor
import ac.mdiq.vista.extractor.services.bandcamp.extractors.BandcampExtractorHelper.getImagesFromSearchResult


class BandcampPlaylistInfoItemExtractor(private val searchResult: Element) : PlaylistInfoItemExtractor {

    private val resultInfo: Element? = searchResult.getElementsByClass("result-info").first()

    override fun getUploaderName(): String? {
        if (resultInfo == null) return null
        return resultInfo.getElementsByClass("subhead").text().split(" by".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
    }

    override fun getUploaderUrl(): String? {
        return null
    }

    override fun isUploaderVerified(): Boolean {
        return false
    }

    override fun getStreamCount(): Long {
        val length = resultInfo?.getElementsByClass("length")?.text() ?: "0"
        return length.split(" track".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0].toInt().toLong()
    }

    override val name: String
        get() = resultInfo?.getElementsByClass("heading")?.text() ?: ""

    override val url: String
        get() = resultInfo?.getElementsByClass("itemurl")?.text() ?: ""


    override val thumbnails: List<Image>
        get() = getImagesFromSearchResult(searchResult)
}
