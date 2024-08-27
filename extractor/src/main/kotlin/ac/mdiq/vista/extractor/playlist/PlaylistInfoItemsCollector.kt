package ac.mdiq.vista.extractor.playlist

import ac.mdiq.vista.extractor.InfoItemsCollector
import ac.mdiq.vista.extractor.exceptions.ParsingException

class PlaylistInfoItemsCollector(serviceId: Int) : InfoItemsCollector<PlaylistInfoItem, PlaylistInfoItemExtractor>(serviceId) {

    @Throws(ParsingException::class)
    override fun extract(extractor: PlaylistInfoItemExtractor): PlaylistInfoItem {
        val resultItem = PlaylistInfoItem(serviceId, extractor.url, extractor.name)

        try {
            resultItem.uploaderName = extractor.getUploaderName()
        } catch (e: Exception) {
            addError(e)
        }
        try {
            resultItem.uploaderUrl = extractor.getUploaderUrl()
        } catch (e: Exception) {
            addError(e)
        }
        try {
            resultItem.isUploaderVerified = extractor.isUploaderVerified()
        } catch (e: Exception) {
            addError(e)
        }
        try {
            resultItem.thumbnails = (extractor.thumbnails)
        } catch (e: Exception) {
            addError(e)
        }
        try {
            resultItem.streamCount = extractor.getStreamCount()
        } catch (e: Exception) {
            addError(e)
        }
        try {
            resultItem.description = extractor.getDescription()
        } catch (e: Exception) {
            addError(e)
        }
        try {
            resultItem.playlistType = extractor.getPlaylistType()
        } catch (e: Exception) {
            addError(e)
        }
        return resultItem
    }
}
