package ac.mdiq.vista.extractor.playlist

import ac.mdiq.vista.extractor.*
import ac.mdiq.vista.extractor.ListExtractor.InfoItemsPage
import ac.mdiq.vista.extractor.Vista.getServiceByUrl
import ac.mdiq.vista.extractor.exceptions.ExtractionException
import ac.mdiq.vista.extractor.linkhandler.ListLinkHandler
import ac.mdiq.vista.extractor.stream.Description
import ac.mdiq.vista.extractor.stream.StreamInfoItem
import ac.mdiq.vista.extractor.utils.ExtractorHelper
import java.io.IOException


class PlaylistInfo private constructor(serviceId: Int, linkHandler: ListLinkHandler, name: String) : ListInfo<StreamInfoItem>(serviceId, linkHandler, name) {
    /**
     * Mixes are handled as particular playlists in VistaGuide. [PlaylistType.NORMAL] is
     * for non-mixes, while other values are for the different types of mixes. The type of a mix
     * depends on how its contents are autogenerated.
     */
    enum class PlaylistType {
        /**
         * A normal playlist (not a mix)
         */
        NORMAL,

        /**
         * A mix made only of streams related to a particular stream, for example YouTube mixes
         */
        MIX_STREAM,

        /**
         * A mix made only of music streams related to a particular stream, for example YouTube
         * music mixes
         */
        MIX_MUSIC,

        /**
         * A mix made only of streams from (or related to) the same channel, for example YouTube
         * channel mixes
         */
        MIX_CHANNEL,

        /**
         * A mix made only of streams related to a particular (musical) genre, for example YouTube
         * genre mixes
         */
        MIX_GENRE,
    }

    var uploaderUrl: String = ""
    var uploaderName: String = ""
    var subChannelUrl: String? = null
    var subChannelName: String? = null
    var description: Description? = null

    var banners: List<Image> = listOf()

    var subChannelAvatars: List<Image> = listOf()

    var thumbnails: List<Image> = listOf()

    var uploaderAvatars: List<Image> = listOf()
    var streamCount: Long = 0
    var playlistType: PlaylistType? = null

    companion object {
        @Throws(IOException::class, ExtractionException::class)
        fun getInfo(url: String?): PlaylistInfo? {
            if (url == null) return null
            return getInfo(getServiceByUrl(url), url)
        }

        @Throws(IOException::class, ExtractionException::class)
        fun getInfo(service: StreamingService, url: String?): PlaylistInfo? {
            if (url == null) return null
            val extractor = service.getPlaylistExtractor(url) ?: return null
            extractor.fetchPage()
            return getInfo(extractor)
        }

        @Throws(IOException::class, ExtractionException::class)
        fun getMoreItems(service: StreamingService, url: String, page: Page?): InfoItemsPage<StreamInfoItem>? {
            return service.getPlaylistExtractor(url)?.getPage(page)
        }

        /**
         * Get PlaylistInfo from PlaylistExtractor
         * @param extractor an extractor where fetchPage() was already got called on.
         */
        @Throws(ExtractionException::class)
        fun getInfo(extractor: PlaylistExtractor): PlaylistInfo {
            val info = PlaylistInfo(extractor.serviceId, extractor.getLinkHandler(), extractor.getName())
            // collect uploader extraction failures until we are sure this is not
            // just a playlist without an uploader
            val uploaderParsingErrors: MutableList<Throwable> = ArrayList()

            try { info.originalUrl = extractor.originalUrl } catch (e: Exception) { info.addError(e) }
            try { info.streamCount = extractor.streamCount } catch (e: Exception) { info.addError(e) }
            try { info.description = extractor.description } catch (e: Exception) { info.addError(e) }
            try { info.thumbnails = extractor.thumbnails } catch (e: Exception) { info.addError(e) }
            try { info.uploaderUrl = extractor.uploaderUrl ?: "" } catch (e: Exception) { uploaderParsingErrors.add(e) }
            try { info.uploaderName = extractor.uploaderName ?: "No name" } catch (e: Exception) { uploaderParsingErrors.add(e) }
            try { info.uploaderAvatars = extractor.uploaderAvatars } catch (e: Exception) { uploaderParsingErrors.add(e) }
            try { info.subChannelUrl = extractor.subChannelUrl } catch (e: Exception) { uploaderParsingErrors.add(e) }
            try { info.subChannelName = extractor.subChannelName } catch (e: Exception) { uploaderParsingErrors.add(e) }
            try { info.subChannelAvatars = extractor.subChannelAvatars } catch (e: Exception) { uploaderParsingErrors.add(e) }
            try { info.banners = extractor.banners } catch (e: Exception) { info.addError(e) }
            try { info.playlistType = extractor.playlistType } catch (e: Exception) { info.addError(e) }

            // do not fail if everything but the uploader infos could be collected (TODO better comment)
            if (uploaderParsingErrors.isNotEmpty() && (info.errors.isNotEmpty() || uploaderParsingErrors.size < 3)) info.addAllErrors(uploaderParsingErrors)

            val itemsPage = ExtractorHelper.getItemsPageOrLogError(info, extractor)
            info.relatedItems = (itemsPage.items)
            info.nextPage = itemsPage.nextPage

            return info
        }
    }
}
