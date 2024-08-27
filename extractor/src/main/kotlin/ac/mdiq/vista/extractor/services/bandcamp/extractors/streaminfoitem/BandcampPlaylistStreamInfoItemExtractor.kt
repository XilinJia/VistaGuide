// Created by Fynn Godau 2019, licensed GNU GPL version 3 or later
package ac.mdiq.vista.extractor.services.bandcamp.extractors.streaminfoitem

import com.grack.nanojson.JsonObject
import ac.mdiq.vista.extractor.Image
import ac.mdiq.vista.extractor.StreamingService
import ac.mdiq.vista.extractor.exceptions.ExtractionException
import ac.mdiq.vista.extractor.exceptions.ParsingException
import java.io.IOException


class BandcampPlaylistStreamInfoItemExtractor(
        private val track: JsonObject,
        uploaderUrl: String,
        private val service: StreamingService?)
    : BandcampStreamInfoItemExtractor(uploaderUrl) {

    private var substituteCovers: List<Image>

    init {
        substituteCovers = emptyList()
    }

    constructor(track: JsonObject, uploaderUrl: String, substituteCovers: List<Image>) : this(track, uploaderUrl, null as StreamingService?) {
        this.substituteCovers = substituteCovers
    }

    override val name: String
        get() = track.getString("title")

    override val url: String
        get() {
            val relativeUrl = track.getString("title_link")
            return if (relativeUrl != null) getUploaderUrl() + relativeUrl else ""
        }

    override fun getDuration(): Long {
        return track.getLong("duration")
    }

    override fun getUploaderName(): String? {
        /* Tracks can have an individual artist name, but it is not included in the
         * given JSON.
         */
        return ""
    }

    @get:Throws(ParsingException::class)

    override val thumbnails: List<Image>
        /**
         * Each track can have its own cover art. Therefore, unless a substitute is provided,
         * the thumbnail is extracted using a stream extractor.
         */
        get() {
            if (substituteCovers.isEmpty()) {
                try {
                    val extractor = service!!.getStreamExtractor(url)
                    extractor.fetchPage()
                    return extractor.thumbnails
                } catch (e: ExtractionException) {
                    throw ParsingException("Could not download cover art location", e)
                } catch (e: IOException) {
                    throw ParsingException("Could not download cover art location", e)
                }
            }

            return substituteCovers
        }
}
