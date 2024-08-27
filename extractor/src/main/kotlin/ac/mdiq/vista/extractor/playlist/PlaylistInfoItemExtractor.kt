package ac.mdiq.vista.extractor.playlist

import ac.mdiq.vista.extractor.InfoItemExtractor
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.playlist.PlaylistInfo.PlaylistType
import ac.mdiq.vista.extractor.stream.Description


interface PlaylistInfoItemExtractor : InfoItemExtractor {
    /**
     * Get the uploader name
     * @return the uploader name
     */
    @Throws(ParsingException::class)
    fun getUploaderName(): String?

    /**
     * Get the uploader url
     * @return the uploader url
     */
    @Throws(ParsingException::class)
    fun getUploaderUrl(): String?

    /**
     * Get whether the uploader is verified
     * @return whether the uploader is verified
     */
    @Throws(ParsingException::class)
    fun isUploaderVerified(): Boolean

    /**
     * Get the number of streams
     * @return the number of streams
     */
    @Throws(ParsingException::class)
    fun getStreamCount(): Long

    /**
     * Get the description of the playlist if there is any.
     * Otherwise, an [EMPTY_DESCRIPTION][Description.EMPTY_DESCRIPTION] is returned.
     * @return the playlist's description
     */

    @Throws(ParsingException::class)
    fun getDescription(): Description {
        return Description.EMPTY_DESCRIPTION
    }

    /**
     * @return the type of this playlist, see [PlaylistInfo.PlaylistType] for a description
     * of types. If not overridden always returns [PlaylistInfo.PlaylistType.NORMAL].
     */

    @Throws(ParsingException::class)
    fun getPlaylistType(): PlaylistType {
        return PlaylistType.NORMAL
    }
}
