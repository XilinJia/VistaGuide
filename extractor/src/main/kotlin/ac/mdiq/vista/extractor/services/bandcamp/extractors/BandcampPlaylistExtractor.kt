package ac.mdiq.vista.extractor.services.bandcamp.extractors

import com.grack.nanojson.JsonArray
import com.grack.nanojson.JsonObject
import com.grack.nanojson.JsonParserException
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import ac.mdiq.vista.extractor.Image
import ac.mdiq.vista.extractor.ListExtractor.InfoItemsPage.Companion.emptyPage
import ac.mdiq.vista.extractor.Page
import ac.mdiq.vista.extractor.StreamingService
import ac.mdiq.vista.extractor.downloader.Downloader
import ac.mdiq.vista.extractor.exceptions.ExtractionException
import ac.mdiq.vista.extractor.exceptions.PaidContentException
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.linkhandler.ListLinkHandler
import ac.mdiq.vista.extractor.playlist.PlaylistExtractor
import ac.mdiq.vista.extractor.services.bandcamp.extractors.BandcampExtractorHelper.getImagesFromImageId
import ac.mdiq.vista.extractor.services.bandcamp.extractors.BandcampExtractorHelper.getImagesFromImageUrl
import ac.mdiq.vista.extractor.services.bandcamp.extractors.streaminfoitem.BandcampPlaylistStreamInfoItemExtractor
import ac.mdiq.vista.extractor.stream.Description
import ac.mdiq.vista.extractor.stream.StreamInfoItem
import ac.mdiq.vista.extractor.stream.StreamInfoItemsCollector
import ac.mdiq.vista.extractor.utils.JsonUtils.getJsonData
import ac.mdiq.vista.extractor.utils.Utils.HTTPS
import java.io.IOException
import java.util.*


class BandcampPlaylistExtractor(service: StreamingService, linkHandler: ListLinkHandler) : PlaylistExtractor(service, linkHandler) {

    private var document: Document? = null
    private var albumJson: JsonObject? = null
    private var trackInfo: JsonArray? = null
    private var name: String = ""

    @Throws(IOException::class, ExtractionException::class)
    override fun onFetchPage(downloader: Downloader) {
        val html = downloader.get(getLinkHandler().url).responseBody()
        document = Jsoup.parse(html)
        albumJson = BandcampStreamExtractor.getAlbumInfoJson(html)
        trackInfo = albumJson?.getArray("trackinfo")

        try {
            name = getJsonData(html, "data-embed").getString("album_title")
        } catch (e: JsonParserException) {
            throw ParsingException("Faulty JSON; page likely does not contain album data", e)
        } catch (e: ArrayIndexOutOfBoundsException) {
            throw ParsingException("JSON does not exist", e)
        }

        if (trackInfo == null|| trackInfo!!.isEmpty()) {
            // Albums without trackInfo need to be purchased before they can be played
            throw PaidContentException("Album needs to be purchased")
        }
    }

    @get:Throws(ParsingException::class)

    override val thumbnails: List<Image>
        get() = if (albumJson!!.isNull("art_id")) {
            listOf()
        } else {
            getImagesFromImageId(albumJson!!.getLong("art_id"), true)
        }

    @get:Throws(ParsingException::class)
    override val uploaderUrl: String
        get() {
            val parts = url.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            // https: (/) (/) * .bandcamp.com (/) and leave out the rest
            return HTTPS + parts[2] + "/"
        }

    override val uploaderName: String
        get() = albumJson!!.getString("artist")


    override val uploaderAvatars: List<Image>
        get() = getImagesFromImageUrl(document!!.getElementsByClass("band-photo")
            .stream()
            .map { element: Element -> element.attr("src") }
            .findFirst()
            .orElse(""))

    @get:Throws(ParsingException::class)
    override val isUploaderVerified: Boolean
        get() = false

    override val streamCount: Long
        get() = trackInfo!!.size.toLong()

    @get:Throws(ParsingException::class)

    override val description: Description
        get() {
            val tInfo = document!!.getElementById("trackInfo")
                ?: throw ParsingException("Could not find trackInfo in document")
            val about = tInfo.getElementsByClass("tralbum-about")
            val credits = tInfo.getElementsByClass("tralbum-credits")
            val license = document!!.getElementById("license")
            if (about.isEmpty() && credits.isEmpty() && license == null) {
                return Description.EMPTY_DESCRIPTION
            }
            val sb = StringBuilder()
            if (!about.isEmpty()) {
                sb.append(Objects.requireNonNull(about.first()).html())
            }
            if (!credits.isEmpty()) {
                sb.append(Objects.requireNonNull(credits.first()).html())
            }
            if (license != null) {
                sb.append(license.html())
            }
            return Description(sb.toString(), Description.HTML)
        }

    @get:Throws(ExtractionException::class)

    override val initialPage: InfoItemsPage<StreamInfoItem>
        get() {
            val collector = StreamInfoItemsCollector(serviceId)

            for (i in trackInfo!!.indices) {
                val track = trackInfo!!.getObject(i)

                if (trackInfo!!.size < MAXIMUM_INDIVIDUAL_COVER_ARTS) {
                    // Load cover art of every track individually
                    collector.commit(BandcampPlaylistStreamInfoItemExtractor(
                        track, uploaderUrl, service))
                } else {
                    // Pretend every track has the same cover art as the album
                    collector.commit(BandcampPlaylistStreamInfoItemExtractor(
                        track, uploaderUrl, thumbnails))
                }
            }

            return InfoItemsPage(collector, null)
        }

    override fun getPage(page: Page?): InfoItemsPage<StreamInfoItem> {
        return emptyPage()
    }


    @Throws(ParsingException::class)
    override fun getName(): String {
        return name
    }

    companion object {
        /**
         * An arbitrarily chosen number above which cover arts won't be fetched individually for each
         * track; instead, it will be assumed that every track has the same cover art as the album,
         * which is not always the case.
         */
        private const val MAXIMUM_INDIVIDUAL_COVER_ARTS = 10
    }
}
