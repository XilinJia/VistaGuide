package ac.mdiq.vista.extractor.services.youtube.extractors

import ac.mdiq.vista.extractor.Image
import ac.mdiq.vista.extractor.ListExtractor
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.playlist.PlaylistInfo.PlaylistType
import ac.mdiq.vista.extractor.playlist.PlaylistInfoItemExtractor
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.extractPlaylistTypeFromPlaylistId
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.getImagesFromThumbnailsArray
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.getUrlFromNavigationEndpoint
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.hasArtistOrVerifiedIconBadgeAttachment
import ac.mdiq.vista.extractor.services.youtube.linkHandler.YoutubePlaylistLinkHandlerFactory
import ac.mdiq.vista.extractor.utils.Utils
import com.grack.nanojson.JsonObject
import java.util.function.Supplier


open class YoutubeMixOrPlaylistLockupInfoItemExtractor(private val lockupViewModel: JsonObject) : PlaylistInfoItemExtractor {
    private val thumbnailViewModel: JsonObject = lockupViewModel.getObject("contentImage")
        .getObject("collectionThumbnailViewModel")
        .getObject("primaryThumbnail")
        .getObject("thumbnailViewModel")

    private val lockupMetadataViewModel: JsonObject = lockupViewModel.getObject("metadata").getObject("lockupMetadataViewModel")

    /*
    The metadata rows are structured in the following way:
    1st part: uploader info, playlist type, playlist updated date
    2nd part: space row
    3rd element: first video
    4th (not always returned for playlists with less than 2 items?): second video
    5th element (always returned, but at a different index for playlists with less than 2
    items?): Show full playlist

    The first metadata row has the following structure:
    1st array element: uploader info
    2nd element: playlist type (course, playlist, podcast)
    3rd element (not always returned): playlist updated date
     */
    private val firstMetadataRow: JsonObject = lockupMetadataViewModel.getObject("metadata").getObject("contentMetadataViewModel").getArray("metadataRows").getObject(0)

    private var playlistType: PlaylistType

    init {
        // If we cannot extract the playlist type, fall back to the normal one
        try { this.playlistType = extractPlaylistTypeFromPlaylistId(this.playlistId) } catch (e: ParsingException) { this.playlistType = PlaylistType.NORMAL }
    }

    @Throws(ParsingException::class)
    override fun getUploaderName(): String? {
        return firstMetadataRow.getArray("metadataParts")
            .getObject(0)
            .getObject("text")
            .getString("content")
    }

    @Throws(ParsingException::class)
    override fun getUploaderUrl(): String? {
        // If the playlist is a mix, there is no uploader as they are auto-generated
        if (playlistType != PlaylistType.NORMAL) return null

        return getUrlFromNavigationEndpoint(
            firstMetadataRow.getArray("metadataParts")
                .getObject(0)
                .getObject("text")
                .getArray("commandRuns")
                .getObject(0)
                .getObject("onTap")
                .getObject("innertubeCommand"))
    }

    @Throws(ParsingException::class)
    override fun isUploaderVerified(): Boolean {
        // If the playlist is a mix, there is no uploader as they are auto-generated
        if (playlistType != PlaylistType.NORMAL) return false
        return hasArtistOrVerifiedIconBadgeAttachment(firstMetadataRow.getArray("metadataParts").getObject(0).getObject("text").getArray("attachmentRuns"))
    }

    @Throws(ParsingException::class)
    override fun getStreamCount(): Long {
        if (playlistType != PlaylistType.NORMAL) {
            // If the playlist is a mix, we are not able to get its stream count
            return ListExtractor.ITEM_COUNT_INFINITE
        }

        try {
            return Utils.removeNonDigitCharacters(
                thumbnailViewModel.getArray("overlays")
                    .stream()
                    .filter { o: Any? -> JsonObject::class.java.isInstance(o) }
                    .map<JsonObject?> { obj: Any? -> JsonObject::class.java.cast(obj) }
                    .filter { overlay: JsonObject? -> overlay!!.has("thumbnailOverlayBadgeViewModel") }
                    .findFirst()
                    .orElseThrow(Supplier { ParsingException("Could not get thumbnailOverlayBadgeViewModel") })
                    .getObject("thumbnailOverlayBadgeViewModel")
                    .getArray("thumbnailBadges")
                    .stream()
                    .filter { o: Any? -> JsonObject::class.java.isInstance(o) }
                    .map<JsonObject?> { obj: Any? -> JsonObject::class.java.cast(obj) }
                    .filter { badge: JsonObject? -> badge!!.has("thumbnailBadgeViewModel") }
                    .findFirst()
                    .orElseThrow(Supplier { ParsingException("Could not get thumbnailBadgeViewModel") })
                    .getObject("thumbnailBadgeViewModel")
                    .getString("text")).toLong()
        } catch (e: Exception) {
            throw ParsingException("Could not get playlist stream count", e)
        }
    }

    @get:Throws(ParsingException::class)
    override val name: String
        get() = lockupMetadataViewModel.getObject("title").getString("content")

    @get:Throws(ParsingException::class)
    override val url: String
        get() {
            // If the playlist item is a mix, we cannot return just its playlist ID as mix playlists
            // are not viewable in playlist pages
            // Use directly getUrlFromNavigationEndpoint in this case, which returns the watch URL with
            // the mix playlist
            if (playlistType == PlaylistType.NORMAL)
                try { return YoutubePlaylistLinkHandlerFactory.instance.getUrl(this.playlistId) } catch (ignored: Exception) { }

            return getUrlFromNavigationEndpoint(lockupViewModel.getObject("rendererContext").getObject("commandContext").getObject("onTap").getObject("innertubeCommand")) ?: ""
        }

    @get:Throws(ParsingException::class)
    override val thumbnails: MutableList<Image>
        get() = getImagesFromThumbnailsArray(thumbnailViewModel.getObject("image").getArray("sources")).toMutableList()

    @Throws(ParsingException::class)
    override fun getPlaylistType(): PlaylistType {
        return playlistType
    }

    @get:Throws(ParsingException::class)
    private val playlistId: String
        get() {
            var id = lockupViewModel.getString("contentId")
            if (id.isNullOrEmpty()) {
                id = lockupViewModel.getObject("rendererContext")
                    .getObject("commandContext")
                    .getObject("watchEndpoint")
                    .getString("playlistId")
            }

            if (id.isNullOrEmpty()) {
                throw ParsingException("Could not get playlist ID")
            }

            return id
        }
}
