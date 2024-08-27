package ac.mdiq.vista.extractor.playlist

import ac.mdiq.vista.extractor.InfoItem
import ac.mdiq.vista.extractor.playlist.PlaylistInfo.PlaylistType
import ac.mdiq.vista.extractor.stream.Description

class PlaylistInfoItem(
        serviceId: Int,
        url: String,
        name: String)
    : InfoItem(InfoType.PLAYLIST, serviceId, url, name) {

    @JvmField
    var uploaderName: String? = null
    @JvmField
    var uploaderUrl: String? = null
    var isUploaderVerified: Boolean = false

    /**
     * How many streams this playlist have
     */
    @JvmField
    var streamCount: Long = 0
    @JvmField
    var description: Description? = null
    @JvmField
    var playlistType: PlaylistType? = null
}
