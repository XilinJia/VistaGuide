package ac.mdiq.vista.extractor.services.soundcloud.extractors

import com.grack.nanojson.JsonObject
import com.grack.nanojson.JsonParser
import com.grack.nanojson.JsonParserException
import ac.mdiq.vista.extractor.Image
import ac.mdiq.vista.extractor.Vista
import ac.mdiq.vista.extractor.Page
import ac.mdiq.vista.extractor.StreamingService
import ac.mdiq.vista.extractor.downloader.Downloader
import ac.mdiq.vista.extractor.exceptions.ExtractionException
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.linkhandler.ListLinkHandler
import ac.mdiq.vista.extractor.playlist.PlaylistExtractor
import ac.mdiq.vista.extractor.services.soundcloud.SoundcloudParsingHelper
import ac.mdiq.vista.extractor.stream.Description
import ac.mdiq.vista.extractor.stream.StreamInfoItem
import ac.mdiq.vista.extractor.stream.StreamInfoItemsCollector
import java.io.IOException
import java.util.*


class SoundcloudPlaylistExtractor(service: StreamingService, linkHandler: ListLinkHandler) : PlaylistExtractor(service, linkHandler) {

    override var id: String = ""
        private set

    private var playlist: JsonObject? = null

    @Throws(IOException::class, ExtractionException::class)
    override fun onFetchPage(downloader: Downloader) {
        id = getLinkHandler().id
        val apiUrl = (SoundcloudParsingHelper.SOUNDCLOUD_API_V2_URL + "playlists/" + id + "?client_id="
                + SoundcloudParsingHelper.clientId() + "&representation=compact")

        val response = downloader.get(apiUrl, extractorLocalization).responseBody()
        try {
            playlist = JsonParser.`object`().from(response)
        } catch (e: JsonParserException) {
            throw ParsingException("Could not parse json response", e)
        }
    }


    override fun getName(): String {
        return playlist!!.getString("title")
    }


    override val thumbnails: List<Image>
        get() {
            val artworkUrl = playlist!!.getString("artwork_url")

            if (!artworkUrl.isNullOrEmpty()) {
                return SoundcloudParsingHelper.getAllImagesFromArtworkOrAvatarUrl(artworkUrl)
            }

            // If the thumbnail is null or empty, traverse the items list and get a valid one
            // If it also fails, return an empty list
            try {
                val infoItems = initialPage
                for (item in infoItems.items) {
                    val thumbnails: List<Image> = item.thumbnails
                    if (thumbnails.isNotEmpty()) return thumbnails
                }
            } catch (ignored: Exception) { }

            return listOf()
        }

    override val uploaderUrl: String
        get() = if (playlist != null) SoundcloudParsingHelper.getUploaderUrl(playlist!!) else ""

    override val uploaderName: String
        get() = if (playlist != null) SoundcloudParsingHelper.getUploaderName(playlist!!) else ""


    override val uploaderAvatars: List<Image>
        get() = if (playlist != null) SoundcloudParsingHelper.getAllImagesFromArtworkOrAvatarUrl(SoundcloudParsingHelper.getAvatarUrl(playlist!!)) else listOf()

    @get:Throws(ParsingException::class)
    override val isUploaderVerified: Boolean
        get() = playlist!!.getObject("user").getBoolean("verified")

    override val streamCount: Long
        get() = playlist!!.getLong("track_count")

    @get:Throws(ParsingException::class)

    override val description: Description
        get() {
            val description = playlist!!.getString("description")
            if (description.isNullOrEmpty()) return Description.EMPTY_DESCRIPTION
            return Description(description, Description.PLAIN_TEXT)
        }


    override val initialPage: InfoItemsPage<StreamInfoItem>
        get() {
            val streamInfoItemsCollector =
                StreamInfoItemsCollector(serviceId)
            val ids: MutableList<String?> = ArrayList()

            playlist!!.getArray("tracks")
                .stream()
                .filter { o: Any? -> JsonObject::class.java.isInstance(o) }
                .map { obj: Any? -> JsonObject::class.java.cast(obj) }
                .forEachOrdered { track: JsonObject ->
                    // i.e. if full info is available
                    if (track.has("title")) streamInfoItemsCollector.commit(SoundcloudStreamInfoItemExtractor(track))
                    // %09d would be enough, but a 0 before the number does not create
                    // problems, so let's be sure
                    else ids.add(String.format("%010d", track.getInt("id")))
                }

            return InfoItemsPage(streamInfoItemsCollector, Page(ids))
        }

    @Throws(IOException::class, ExtractionException::class)
    override fun getPage(page: Page?): InfoItemsPage<StreamInfoItem> {
        require(!(page == null || page.ids.isNullOrEmpty())) { "Page doesn't contain IDs" }

        val currentIds: List<String?>?
        val nextIds: List<String?>?
        if (page.ids.size <= STREAMS_PER_REQUESTED_PAGE) {
            // Fetch every remaining stream, there are less than the max
            currentIds = page.ids
            nextIds = null
        } else {
            currentIds = page.ids.subList(0, STREAMS_PER_REQUESTED_PAGE)
            nextIds = page.ids.subList(STREAMS_PER_REQUESTED_PAGE, page.ids.size)
        }

        val currentPageUrl = (SoundcloudParsingHelper.SOUNDCLOUD_API_V2_URL + "tracks?client_id="
                + SoundcloudParsingHelper.clientId() + "&ids=" + java.lang.String.join(",", currentIds))

        val collector = StreamInfoItemsCollector(serviceId)
        val response = Vista.downloader.get(currentPageUrl,
            extractorLocalization).responseBody()

        try {
            val tracks = JsonParser.array().from(response)
            // Response may not contain tracks in the same order as currentIds.
            // The streams are displayed in the order which is used in currentIds on SoundCloud.
            val idToTrack = HashMap<Int, JsonObject>()
            for (track in tracks) {
                if (track is JsonObject) idToTrack[track.getInt("id")] = track
            }
            for (strId in currentIds) {
                val id = strId!!.toInt()
                try {
                    val idt = idToTrack[id]
                    if (idt != null) collector.commit(SoundcloudStreamInfoItemExtractor(Objects.requireNonNull(idt, "no track with id $id in response")))
                } catch (e: NullPointerException) {
                    throw ParsingException("Could not parse json response", e)
                }
            }
        } catch (e: JsonParserException) {
            throw ParsingException("Could not parse json response", e)
        }

        return InfoItemsPage(collector, Page(nextIds))
    }

    companion object {
        private const val STREAMS_PER_REQUESTED_PAGE = 15
    }
}
