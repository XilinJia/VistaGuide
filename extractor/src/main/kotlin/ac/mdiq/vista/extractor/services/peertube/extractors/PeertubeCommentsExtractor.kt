package ac.mdiq.vista.extractor.services.peertube.extractors

import com.grack.nanojson.JsonObject
import com.grack.nanojson.JsonParser
import com.grack.nanojson.JsonParserException
import ac.mdiq.vista.extractor.Page
import ac.mdiq.vista.extractor.StreamingService
import ac.mdiq.vista.extractor.comments.CommentsExtractor
import ac.mdiq.vista.extractor.comments.CommentsInfoItem
import ac.mdiq.vista.extractor.comments.CommentsInfoItemsCollector
import ac.mdiq.vista.extractor.downloader.Downloader
import ac.mdiq.vista.extractor.exceptions.ExtractionException
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.linkhandler.ListLinkHandler
import ac.mdiq.vista.extractor.services.peertube.PeertubeParsingHelper.COUNT_KEY
import ac.mdiq.vista.extractor.services.peertube.PeertubeParsingHelper.ITEMS_PER_PAGE
import ac.mdiq.vista.extractor.services.peertube.PeertubeParsingHelper.START_KEY
import ac.mdiq.vista.extractor.services.peertube.PeertubeParsingHelper.getNextPage
import ac.mdiq.vista.extractor.services.peertube.PeertubeParsingHelper.validate
import java.io.IOException
import java.nio.charset.StandardCharsets


class PeertubeCommentsExtractor(service: StreamingService, uiHandler: ListLinkHandler) : CommentsExtractor(service, uiHandler) {
    /**
     * Use [.isReply] to access this variable.
     */
    private var isReply: Boolean? = null

    @get:Throws(IOException::class, ExtractionException::class)

    override val initialPage: InfoItemsPage<CommentsInfoItem>
        get() = if (isReply()) getPage(Page(originalUrl)) else getPage(Page("$url?$START_KEY=0&$COUNT_KEY=$ITEMS_PER_PAGE"))

    @Throws(ParsingException::class)
    private fun isReply(): Boolean {
        if (isReply == null) {
            isReply = if (originalUrl.contains("/videos/watch/")) {
                false
            } else {
                originalUrl.contains("/comment-threads/")
            }
        }
        return isReply!!
    }

    @Throws(ParsingException::class)
    private fun collectCommentsFrom(collector: CommentsInfoItemsCollector,
                                     json: JsonObject
    ) {
        val contents = json.getArray("data")

        for (c in contents) {
            if (c is JsonObject) {
                if (!c.getBoolean(IS_DELETED)) collector.commit(PeertubeCommentsInfoItemExtractor(c, null, url, baseUrl, isReply()))
            }
        }
    }

    @Throws(ParsingException::class)
    private fun collectRepliesFrom(collector: CommentsInfoItemsCollector,  json: JsonObject) {
        val contents = json.getArray(CHILDREN)

        for (c in contents) {
            if (c is JsonObject) {
                val item = c.getObject("comment")
                val children = c.getArray(CHILDREN)
                if (!item.getBoolean(IS_DELETED)) collector.commit(PeertubeCommentsInfoItemExtractor(item, children, url, baseUrl, isReply()))
            }
        }
    }

    @Throws(IOException::class, ExtractionException::class)
    override fun getPage(page: Page?): InfoItemsPage<CommentsInfoItem> {
        require(!(page == null || page.url.isNullOrEmpty())) { "Page doesn't contain an URL" }

        var json: JsonObject? = null
        val collector = CommentsInfoItemsCollector(serviceId)
        val total: Long
        if (page.body == null) {
            val response = downloader.get(page.url)
            if (response != null && response.responseBody().isNotEmpty()) {
                try { json = JsonParser.`object`().from(response.responseBody()) } catch (e: Exception) { throw ParsingException("Could not parse json data for comments info", e) }
            }
            if (json != null) {
                validate(json)
                if (isReply() || json.has(CHILDREN)) {
                    total = json.getArray(CHILDREN).size.toLong()
                    collectRepliesFrom(collector, json)
                } else {
                    total = json.getLong(TOTAL)
                    collectCommentsFrom(collector, json)
                }
            } else {
                throw ExtractionException("Unable to get PeerTube kiosk info")
            }
        } else {
            try {
                json = JsonParser.`object`().from(String(page.body, StandardCharsets.UTF_8))
                isReply = true
                total = json.getArray(CHILDREN).size.toLong()
                collectRepliesFrom(collector, json)
            } catch (e: JsonParserException) {
                throw ParsingException(
                    "Could not parse json data for nested comments  info", e)
            }
        }

        return InfoItemsPage(collector,
            getNextPage(page.url, total))
    }

    override fun onFetchPage(downloader: Downloader) {
    }

    companion object {
        const val CHILDREN: String = "children"
        private const val IS_DELETED = "isDeleted"
        private const val TOTAL = "total"
    }
}
