package ac.mdiq.vista.extractor.playlist

import ac.mdiq.vista.extractor.Image
import ac.mdiq.vista.extractor.ListExtractor
import ac.mdiq.vista.extractor.StreamingService
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.linkhandler.ListLinkHandler
import ac.mdiq.vista.extractor.playlist.PlaylistInfo.PlaylistType
import ac.mdiq.vista.extractor.stream.Description
import ac.mdiq.vista.extractor.stream.StreamInfoItem


abstract class PlaylistExtractor(service: StreamingService, linkHandler: ListLinkHandler) : ListExtractor<StreamInfoItem>(service, linkHandler) {

    @get:Throws(ParsingException::class)
    abstract val uploaderUrl: String?

    @get:Throws(ParsingException::class)
    abstract val uploaderName: String?

    @get:Throws(ParsingException::class)
    abstract val uploaderAvatars: List<Image>

    @get:Throws(ParsingException::class)
    abstract val isUploaderVerified: Boolean

    @get:Throws(ParsingException::class)
    abstract val streamCount: Long

    @get:Throws(ParsingException::class)
    abstract val description: Description

    @get:Throws(ParsingException::class)
    open val thumbnails: List<Image>
        get() = emptyList()

    @get:Throws(ParsingException::class)
    val banners: List<Image>
        get() = listOf()

    @get:Throws(ParsingException::class)
    open val subChannelName: String
        get() = ""

    @get:Throws(ParsingException::class)
    open val subChannelUrl: String
        get() = ""

    @get:Throws(ParsingException::class)
    open val subChannelAvatars: List<Image>
        get() = listOf()

    @get:Throws(ParsingException::class)
    open val playlistType: PlaylistType
        get() = PlaylistType.NORMAL
}
