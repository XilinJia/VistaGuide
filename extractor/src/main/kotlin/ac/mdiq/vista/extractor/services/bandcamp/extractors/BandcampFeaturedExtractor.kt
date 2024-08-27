// Created by Fynn Godau 2019, licensed GNU GPL version 3 or later
package ac.mdiq.vista.extractor.services.bandcamp.extractors

import com.grack.nanojson.JsonArray
import com.grack.nanojson.JsonObject
import com.grack.nanojson.JsonParser
import com.grack.nanojson.JsonParserException
import ac.mdiq.vista.extractor.ListExtractor.InfoItemsPage.Companion.emptyPage
import ac.mdiq.vista.extractor.Page
import ac.mdiq.vista.extractor.Page.Companion.isValid
import ac.mdiq.vista.extractor.StreamingService
import ac.mdiq.vista.extractor.downloader.Downloader
import ac.mdiq.vista.extractor.exceptions.ExtractionException
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.kiosk.KioskExtractor
import ac.mdiq.vista.extractor.linkhandler.ListLinkHandler
import ac.mdiq.vista.extractor.playlist.PlaylistInfoItem
import ac.mdiq.vista.extractor.playlist.PlaylistInfoItemsCollector
import ac.mdiq.vista.extractor.services.bandcamp.extractors.BandcampExtractorHelper.BASE_API_URL
import java.io.IOException
import java.nio.charset.StandardCharsets


class BandcampFeaturedExtractor(
        streamingService: StreamingService,
        listLinkHandler: ListLinkHandler,
        kioskId: String)
    : KioskExtractor<PlaylistInfoItem>(streamingService, listLinkHandler, kioskId) {

    private var json: JsonObject? = null

    @Throws(IOException::class, ExtractionException::class)
    override fun onFetchPage(downloader: Downloader) {
        try {
            json = JsonParser.`object`().from(downloader.postWithContentTypeJson(FEATURED_API_URL, emptyMap(),
                "{\"platform\":\"\",\"version\":0}".toByteArray(StandardCharsets.UTF_8)).responseBody())
        } catch (e: JsonParserException) {
            throw ParsingException("Could not parse Bandcamp featured API response", e)
        }
    }


    @Throws(ParsingException::class)
    override fun getName(): String {
        return KIOSK_FEATURED
    }

    @get:Throws(IOException::class, ExtractionException::class)

    override val initialPage: InfoItemsPage<PlaylistInfoItem>
        get() {
            val featuredStories = json!!.getObject("feed_content")
                .getObject("stories")
                .getArray("featured")
            return extractItems(featuredStories)
        }

    private fun extractItems(featuredStories: JsonArray): InfoItemsPage<PlaylistInfoItem> {
        val c = PlaylistInfoItemsCollector(serviceId)

        for (i in featuredStories.indices) {
            val featuredStory = featuredStories.getObject(i)
            // Is not an album, ignore
            if (featuredStory.isNull("album_title")) continue
            c.commit(BandcampPlaylistInfoItemFeaturedExtractor(featuredStory))
        }

        val lastFeaturedStory = featuredStories.getObject(featuredStories.size - 1)
        return InfoItemsPage(c, getNextPageFrom(lastFeaturedStory))
    }

    /**
     * Next Page can be generated from metadata of last featured story
     */
    private fun getNextPageFrom(lastFeaturedStory: JsonObject): Page {
        val lastStoryDate = lastFeaturedStory.getLong("story_date")
        val lastStoryId = lastFeaturedStory.getLong("ntid")
        val lastStoryType = lastFeaturedStory.getString("story_type")
        return Page("$MORE_FEATURED_API_URL?story_groups=featured:$lastStoryDate:$lastStoryType:$lastStoryId")
    }

    @Throws(IOException::class, ExtractionException::class)
    override fun getPage(page: Page?): InfoItemsPage<PlaylistInfoItem> {
        if (!isValid(page)) return emptyPage()
        val response: JsonObject
        try {
            response = JsonParser.`object`().from(downloader.get(page!!.url!!).responseBody())
        } catch (e: JsonParserException) {
            throw ParsingException("Could not parse Bandcamp featured API response", e)
        }

        return extractItems(response.getObject("stories").getArray("featured"))
    }

    companion object {
        const val KIOSK_FEATURED: String = "Featured"
        const val FEATURED_API_URL: String = "$BASE_API_URL/mobile/24/bootstrap_data"
        const val MORE_FEATURED_API_URL: String = "$BASE_API_URL/mobile/24/feed_older_logged_out"
    }
}
