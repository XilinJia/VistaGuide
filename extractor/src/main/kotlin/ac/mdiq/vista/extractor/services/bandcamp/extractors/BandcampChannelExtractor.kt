// Created by Fynn Godau 2019, licensed GNU GPL version 3 or later
package ac.mdiq.vista.extractor.services.bandcamp.extractors

import com.grack.nanojson.JsonArray
import com.grack.nanojson.JsonObject
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import ac.mdiq.vista.extractor.Image
import ac.mdiq.vista.extractor.Image.ResolutionLevel
import ac.mdiq.vista.extractor.StreamingService
import ac.mdiq.vista.extractor.channel.ChannelExtractor
import ac.mdiq.vista.extractor.channel.tabs.ChannelTabExtractor
import ac.mdiq.vista.extractor.channel.tabs.ChannelTabs
import ac.mdiq.vista.extractor.downloader.Downloader
import ac.mdiq.vista.extractor.exceptions.ExtractionException
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.exceptions.ReCaptchaException
import ac.mdiq.vista.extractor.linkhandler.ListLinkHandler
import ac.mdiq.vista.extractor.linkhandler.ReadyChannelTabListLinkHandler
import ac.mdiq.vista.extractor.linkhandler.ReadyChannelTabListLinkHandler.ChannelTabExtractorBuilder
import ac.mdiq.vista.extractor.services.bandcamp.linkHandler.BandcampChannelTabLinkHandlerFactory.Companion.getUrlSuffix
import ac.mdiq.vista.extractor.utils.Utils.replaceHttpWithHttps
import java.io.IOException
import java.util.*
import java.util.stream.Collectors
import java.util.stream.Stream


class BandcampChannelExtractor(service: StreamingService, linkHandler: ListLinkHandler) : ChannelExtractor(service, linkHandler) {

    private var channelInfo: JsonObject? = null


    override fun getAvatars(): List<Image> {
        return BandcampExtractorHelper.getImagesFromImageId(channelInfo!!.getLong("bio_image_id"), false)
    }


    @Throws(ParsingException::class)
    override fun getBanners(): List<Image> {
        /*
         * Mobile API does not return the header or not the correct header.
         * Therefore, we need to query the website
         */
        try {
            val html = downloader
                .get(replaceHttpWithHttps(channelInfo!!.getString("bandcamp_url")))
                .responseBody()

            return Stream.of(Jsoup.parse(html).getElementById("customHeader"))
                .filter { obj: Element? -> Objects.nonNull(obj) }
                .flatMap { element: Element -> element.getElementsByTag("img").stream() }
                .map { element: Element -> element.attr("src") }
                .filter { url: String -> url.isNotEmpty() }
                .map { url: String ->
                    Image(replaceHttpWithHttps(url), Image.HEIGHT_UNKNOWN, Image.WIDTH_UNKNOWN, ResolutionLevel.UNKNOWN)
                }
                .collect(Collectors.toUnmodifiableList())
        } catch (e: IOException) {
            throw ParsingException("Could not download artist web site", e)
        } catch (e: ReCaptchaException) {
            throw ParsingException("Could not download artist web site", e)
        }
    }

    /**
     * Bandcamp discontinued their RSS feeds because it hadn't been used enough.
     */
    override fun getFeedUrl(): String? {
        return null
    }

    override fun getSubscriberCount(): Long {
        return -1
    }

    override fun getDescription(): String {
        return channelInfo?.getString("bio") ?: ""
    }

    override fun getParentChannelName(): String {
        return ""
    }

    override fun getParentChannelUrl(): String? {
        return null
    }


    override fun getParentChannelAvatars(): List<Image> {
        return listOf()
    }

    @Throws(ParsingException::class)
    override fun isVerified(): Boolean {
        return false
    }


    @Throws(ParsingException::class)
    override fun getTabs(): List<ListLinkHandler> {
        val discography = channelInfo!!.getArray("discography")
        val builder = TabExtractorBuilder(discography)

        val tabs: MutableList<ListLinkHandler> = ArrayList()

        var foundTrackItem = false
        var foundAlbumItem = false

        for (discographyItem in discography) {
            if (foundTrackItem && foundAlbumItem) {
                break
            }

            if (discographyItem !is JsonObject) {
                continue
            }

            val itemType = discographyItem.getString("item_type")

            if (!foundTrackItem && "track" == itemType) {
                foundTrackItem = true
                tabs.add(ReadyChannelTabListLinkHandler(url + getUrlSuffix(ChannelTabs.TRACKS), id, ChannelTabs.TRACKS, builder))
            }

            if (!foundAlbumItem && "album" == itemType) {
                foundAlbumItem = true
                tabs.add(ReadyChannelTabListLinkHandler(url + getUrlSuffix(ChannelTabs.ALBUMS), id, ChannelTabs.ALBUMS, builder))
            }
        }

        return Collections.unmodifiableList(tabs)
    }

    @Throws(IOException::class, ExtractionException::class)
    override fun onFetchPage(downloader: Downloader) {
        channelInfo = BandcampExtractorHelper.getArtistDetails(id)
    }

    override fun getName(): String {
        return channelInfo!!.getString("name")
    }

    private class TabExtractorBuilder(private val discography: JsonArray) : ChannelTabExtractorBuilder {
        override fun build(service: StreamingService, linkHandler: ListLinkHandler): ChannelTabExtractor {
            return BandcampChannelTabExtractor.fromDiscography(service, linkHandler, discography)
        }
    }
}
