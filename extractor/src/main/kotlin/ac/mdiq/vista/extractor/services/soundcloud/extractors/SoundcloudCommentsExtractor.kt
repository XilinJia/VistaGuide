package ac.mdiq.vista.extractor.services.soundcloud.extractors

import com.grack.nanojson.JsonArray
import com.grack.nanojson.JsonObject
import com.grack.nanojson.JsonParser
import com.grack.nanojson.JsonParserException
import ac.mdiq.vista.extractor.Vista
import ac.mdiq.vista.extractor.Page
import ac.mdiq.vista.extractor.StreamingService
import ac.mdiq.vista.extractor.comments.CommentsExtractor
import ac.mdiq.vista.extractor.comments.CommentsInfoItem
import ac.mdiq.vista.extractor.comments.CommentsInfoItemsCollector
import ac.mdiq.vista.extractor.downloader.Downloader
import ac.mdiq.vista.extractor.exceptions.ExtractionException
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.exceptions.ReCaptchaException
import ac.mdiq.vista.extractor.linkhandler.ListLinkHandler
import java.io.IOException


class SoundcloudCommentsExtractor(service: StreamingService, uiHandler: ListLinkHandler) : CommentsExtractor(service, uiHandler) {
    @get:Throws(ExtractionException::class, IOException::class)

    override val initialPage: InfoItemsPage<CommentsInfoItem>
        get() = getPage(url)

    @Throws(ExtractionException::class, IOException::class)
    override fun getPage(page: Page?): InfoItemsPage<CommentsInfoItem> {
        require(!(page == null || page.url.isNullOrEmpty())) { "Page doesn't contain an URL" }
        return getPage(page.url)
    }


    @Throws(ParsingException::class, IOException::class, ReCaptchaException::class)
    private fun getPage(url: String): InfoItemsPage<CommentsInfoItem> {
        val downloader = Vista.downloader
        val response = downloader.get(url)

        val json: JsonObject
        try {
            json = JsonParser.`object`().from(response.responseBody())
        } catch (e: JsonParserException) {
            throw ParsingException("Could not parse json", e)
        }

        val collector = CommentsInfoItemsCollector(
            serviceId)

        collectStreamsFrom(collector, json.getArray("collection"))
        return InfoItemsPage(collector, Page(json.getString("next_href", null)))
    }

    override fun onFetchPage(downloader: Downloader) {}

    @Throws(ParsingException::class)
    private fun collectStreamsFrom(collector: CommentsInfoItemsCollector, entries: JsonArray) {
        for (comment in entries) {
            collector.commit(SoundcloudCommentsInfoItemExtractor(comment as JsonObject, url))
        }
    }
}
