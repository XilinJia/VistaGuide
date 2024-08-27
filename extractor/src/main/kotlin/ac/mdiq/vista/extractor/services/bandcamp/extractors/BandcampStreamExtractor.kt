// Created by Fynn Godau 2019, licensed GNU GPL version 3 or later
package ac.mdiq.vista.extractor.services.bandcamp.extractors

import com.grack.nanojson.JsonObject
import com.grack.nanojson.JsonParserException
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import ac.mdiq.vista.extractor.Image
import ac.mdiq.vista.extractor.MediaFormat
import ac.mdiq.vista.extractor.StreamingService
import ac.mdiq.vista.extractor.downloader.Downloader
import ac.mdiq.vista.extractor.exceptions.ExtractionException
import ac.mdiq.vista.extractor.exceptions.PaidContentException
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.linkhandler.LinkHandler
import ac.mdiq.vista.extractor.localization.DateWrapper
import ac.mdiq.vista.extractor.playlist.PlaylistInfoItemsCollector
import ac.mdiq.vista.extractor.services.bandcamp.extractors.BandcampExtractorHelper.getImagesFromImageId
import ac.mdiq.vista.extractor.services.bandcamp.extractors.BandcampExtractorHelper.getImagesFromImageUrl
import ac.mdiq.vista.extractor.services.bandcamp.extractors.BandcampExtractorHelper.parseDate
import ac.mdiq.vista.extractor.stream.*
import ac.mdiq.vista.extractor.utils.JsonUtils.getJsonData
import ac.mdiq.vista.extractor.utils.Utils.HTTPS
import ac.mdiq.vista.extractor.utils.Utils.nonEmptyAndNullJoin
import ac.mdiq.vista.extractor.utils.Utils.replaceHttpWithHttps
import java.io.IOException
import java.util.stream.Collectors


open class BandcampStreamExtractor(service: StreamingService, linkHandler: LinkHandler) : StreamExtractor(service, linkHandler) {
    private var albumJson: JsonObject? = null
    private var current: JsonObject? = null
    private var document: Document? = null

    @Throws(IOException::class, ExtractionException::class)
    override fun onFetchPage(downloader: Downloader) {
        val html = downloader.get(getLinkHandler().url).responseBody()
        document = Jsoup.parse(html)
        albumJson = getAlbumInfoJson(html)
        current = albumJson!!.getObject("current")

        if (albumJson!!.getArray("trackinfo").size > 1) {
            // In this case, we are actually viewing an album page!
            throw ExtractionException("Page is actually an album, not a track")
        }

        if (albumJson!!.getArray("trackinfo").getObject(0).isNull("file")) {
            throw PaidContentException("This track is not available without being purchased")
        }
    }


    @Throws(ParsingException::class)
    override fun getName(): String {
        return current!!.getString("title")
    }

    @get:Throws(ParsingException::class)

    override val uploaderUrl: String
        get() {
            val parts = url.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            // https: (/) (/) * .bandcamp.com (/) and leave out the rest
            return HTTPS + parts[2] + "/"
        }

    @get:Throws(ParsingException::class)

    override val url: String
        get() = replaceHttpWithHttps(albumJson!!.getString("url"))

    @get:Throws(ParsingException::class)

    override val uploaderName: String
        get() = albumJson!!.getString("artist")

    override val textualUploadDate: String?
        get() = current!!.getString("publish_date")

    @get:Throws(ParsingException::class)
    override val uploadDate: DateWrapper?
        get() = parseDate(textualUploadDate!!)

    @get:Throws(ParsingException::class)

    override val thumbnails: List<Image>
        get() {
            if (albumJson!!.isNull("art_id")) {
                return listOf()
            }

            return getImagesFromImageId(albumJson!!.getLong("art_id"), true)
        }


    override val uploaderAvatars: List<Image>
        get() = getImagesFromImageUrl(document!!.getElementsByClass("band-photo")
            .stream()
            .map { element: Element -> element.attr("src") }
            .findFirst()
            .orElse(""))


    override val description: Description
        get() {
            val s = nonEmptyAndNullJoin("\n\n", current!!.getString("about"),
                current!!.getString("lyrics"), current!!.getString("credits"))
            return Description(s, Description.PLAIN_TEXT)
        }

    override val audioStreams: List<AudioStream>
        get() = listOf(AudioStream.Builder()
            .setId("mp3-128")
            .setContent(albumJson!!.getArray("trackinfo")
                .getObject(0)
                .getObject("file")
                .getString("mp3-128"), true)
            .setMediaFormat(MediaFormat.MP3)
            .setAverageBitrate(128)
            .build())

    @get:Throws(ParsingException::class)
    override val length: Long
        get() = albumJson!!.getArray("trackinfo").getObject(0)
            .getDouble("duration").toLong()

    override val videoStreams: List<VideoStream>
        get() = emptyList()

    override val videoOnlyStreams: List<VideoStream>
        get() = emptyList()

    override val streamType: StreamType
        get() = StreamType.AUDIO_STREAM

    override val relatedItems: PlaylistInfoItemsCollector?
        get() {
            val collector = PlaylistInfoItemsCollector(serviceId)
            document!!.getElementsByClass("recommended-album")
                .stream()
                .map { relatedAlbum: Element? ->
                    BandcampRelatedPlaylistInfoItemExtractor(
                        relatedAlbum!!)
                }
                .forEach { extractor: BandcampRelatedPlaylistInfoItemExtractor -> collector.commit(extractor) }

            return collector
        }


    override val category: String
        get() =// Get first tag from html, which is the artist's Genre
            document!!.getElementsByClass("tralbum-tags").stream()
                .flatMap { element: Element -> element.getElementsByClass("tag").stream() }
                .map { obj: Element -> obj.text() }
                .findFirst()
                .orElse("")

    /*
Tests resulted in this mapping of ints to licence:
https://cloud.disroot.org/s/ZTWBxbQ9fKRmRWJ/preview (screenshot from a Bandcamp artist's
account)
*/

    override val licence: String
        get() = when (current!!.getInt("license_type")) {
            1 -> "All rights reserved ©"
            2 -> "CC BY-NC-ND 3.0"
            3 -> "CC BY-NC-SA 3.0"
            4 -> "CC BY-NC 3.0"
            5 -> "CC BY-ND 3.0"
            6 -> "CC BY 3.0"
            8 -> "CC BY-SA 3.0"
            else -> "Unknown"
        }


    override val tags: List<String>
        get() = document!!.getElementsByAttributeValue("itemprop", "keywords")
            .stream()
            .map { obj: Element -> obj.text() }
            .collect(Collectors.toList())

    companion object {
        /**
         * Get the JSON that contains album's metadata from page
         *
         * @param html Website
         * @return Album metadata JSON
         * @throws ParsingException In case of a faulty website
         */
        @Throws(ParsingException::class)
        fun getAlbumInfoJson(html: String): JsonObject {
            try {
                return getJsonData(html, "data-tralbum")
            } catch (e: JsonParserException) {
                throw ParsingException("Faulty JSON; page likely does not contain album data", e)
            } catch (e: ArrayIndexOutOfBoundsException) {
                throw ParsingException("JSON does not exist", e)
            }
        }
    }
}
