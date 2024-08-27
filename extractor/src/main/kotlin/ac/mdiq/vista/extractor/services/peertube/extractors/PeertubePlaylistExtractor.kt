package ac.mdiq.vista.extractor.services.peertube.extractors

import com.grack.nanojson.JsonObject
import com.grack.nanojson.JsonParser
import com.grack.nanojson.JsonParserException
import ac.mdiq.vista.extractor.Image
import ac.mdiq.vista.extractor.Page
import ac.mdiq.vista.extractor.StreamingService
import ac.mdiq.vista.extractor.downloader.Downloader
import ac.mdiq.vista.extractor.exceptions.ExtractionException
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.linkhandler.ListLinkHandler
import ac.mdiq.vista.extractor.playlist.PlaylistExtractor
import ac.mdiq.vista.extractor.services.peertube.PeertubeParsingHelper.COUNT_KEY
import ac.mdiq.vista.extractor.services.peertube.PeertubeParsingHelper.ITEMS_PER_PAGE
import ac.mdiq.vista.extractor.services.peertube.PeertubeParsingHelper.START_KEY
import ac.mdiq.vista.extractor.services.peertube.PeertubeParsingHelper.collectItemsFrom
import ac.mdiq.vista.extractor.services.peertube.PeertubeParsingHelper.getAvatarsFromOwnerAccountOrVideoChannelObject
import ac.mdiq.vista.extractor.services.peertube.PeertubeParsingHelper.getNextPage
import ac.mdiq.vista.extractor.services.peertube.PeertubeParsingHelper.getThumbnailsFromPlaylistOrVideoItem
import ac.mdiq.vista.extractor.services.peertube.PeertubeParsingHelper.validate
import ac.mdiq.vista.extractor.stream.Description
import ac.mdiq.vista.extractor.stream.StreamInfoItem
import ac.mdiq.vista.extractor.stream.StreamInfoItemsCollector
import java.io.IOException


class PeertubePlaylistExtractor(service: StreamingService, linkHandler: ListLinkHandler) : PlaylistExtractor(service, linkHandler) {
    private var playlistInfo: JsonObject? = null

    @get:Throws(ParsingException::class)

    override val thumbnails: List<Image>
        get() = getThumbnailsFromPlaylistOrVideoItem(baseUrl, playlistInfo!!)

    override val uploaderUrl: String
        get() = playlistInfo!!.getObject("ownerAccount").getString("url")

    override val uploaderName: String
        get() = playlistInfo!!.getObject("ownerAccount").getString("displayName")

    @get:Throws(ParsingException::class)

    override val uploaderAvatars: List<Image>
        get() = getAvatarsFromOwnerAccountOrVideoChannelObject(baseUrl, playlistInfo!!.getObject("ownerAccount"))

    @get:Throws(ParsingException::class)
    override val isUploaderVerified: Boolean
        get() = false

    override val streamCount: Long
        get() = playlistInfo!!.getLong("videosLength")

    @get:Throws(ParsingException::class)

    override val description: Description
        get() {
            val description = playlistInfo!!.getString("description")
            if (description.isNullOrEmpty()) {
                return Description.EMPTY_DESCRIPTION
            }
            return Description(description, Description.PLAIN_TEXT)
        }


    override val subChannelName: String
        get() = playlistInfo!!.getObject("videoChannel").getString("displayName")


    override val subChannelUrl: String
        get() = playlistInfo!!.getObject("videoChannel").getString("url")

    @get:Throws(ParsingException::class)

    override val subChannelAvatars: List<Image>
        get() = getAvatarsFromOwnerAccountOrVideoChannelObject(baseUrl,
            playlistInfo!!.getObject("videoChannel"))

    @get:Throws(IOException::class, ExtractionException::class)

    override val initialPage: InfoItemsPage<StreamInfoItem>
        get() = getPage(Page("$url/videos?$START_KEY=0&$COUNT_KEY=$ITEMS_PER_PAGE"))

    @Throws(IOException::class, ExtractionException::class)
    override fun getPage(page: Page?): InfoItemsPage<StreamInfoItem> {
        require(!(page == null || page.url.isNullOrEmpty())) { "Page doesn't contain an URL" }

        val response = downloader.get(page.url)

        var json: JsonObject? = null
        if (response != null && response.responseBody().isNotEmpty()) {
            try {
                json = JsonParser.`object`().from(response.responseBody())
            } catch (e: Exception) {
                throw ParsingException("Could not parse json data for playlist info", e)
            }
        }

        if (json != null) {
            validate(json)
            val total = json.getLong("total")

            val collector = StreamInfoItemsCollector(serviceId)
            collectItemsFrom(collector, json, baseUrl)

            return InfoItemsPage(collector,
                getNextPage(page.url, total))
        } else {
            throw ExtractionException("Unable to get PeerTube playlist info")
        }
    }

    @Throws(IOException::class, ExtractionException::class)
    override fun onFetchPage(downloader: Downloader) {
        val response = downloader.get(url)
        try {
            playlistInfo = JsonParser.`object`().from(response.responseBody())
        } catch (jpe: JsonParserException) {
            throw ExtractionException("Could not parse json", jpe)
        }
        if (playlistInfo != null) validate(playlistInfo!!)
    }


    @Throws(ParsingException::class)
    override fun getName(): String {
        return playlistInfo!!.getString("displayName")
    }
}
