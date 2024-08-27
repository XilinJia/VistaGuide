package ac.mdiq.vista.extractor.services.bandcamp.extractors

import com.grack.nanojson.JsonArray
import com.grack.nanojson.JsonObject
import com.grack.nanojson.JsonWriter
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
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
import ac.mdiq.vista.extractor.utils.JsonUtils.toJsonObject
import java.io.IOException
import java.nio.charset.StandardCharsets


class BandcampCommentsExtractor(service: StreamingService, linkHandler: ListLinkHandler) : CommentsExtractor(service, linkHandler) {
    private var document: Document? = null

    @Throws(IOException::class, ExtractionException::class)
    override fun onFetchPage(downloader: Downloader) {
        document = Jsoup.parse(downloader.get(getLinkHandler().url).responseBody())
    }

    @get:Throws(IOException::class, ExtractionException::class)

    override val initialPage: InfoItemsPage<CommentsInfoItem>
        get() {
            val collector = CommentsInfoItemsCollector(serviceId)

            val collectorsData = toJsonObject(document?.getElementById("collectors-data")?.attr("data-blob"))
            val reviews = collectorsData.getArray("reviews")

            for (review in reviews) {
                collector.commit(
                    BandcampCommentsInfoItemExtractor(review as JsonObject, url))
            }

            if (!collectorsData.getBoolean("more_reviews_available")) {
                return InfoItemsPage(collector, null)
            }

            val trackId = trackId
            val token = getNextPageToken(reviews)
            return InfoItemsPage(collector, Page(listOf(trackId, token)))
        }

    @Throws(IOException::class, ExtractionException::class)
    override fun getPage(page: Page?): InfoItemsPage<CommentsInfoItem> {
        val collector = CommentsInfoItemsCollector(serviceId)

        val pageIds = page!!.ids
        val trackId = pageIds!![0]
        val token = pageIds[1]
        val reviewsData = fetchReviewsData(trackId, token)
        val reviews = reviewsData.getArray("results")

        for (review in reviews) {
            collector.commit(BandcampCommentsInfoItemExtractor(review as JsonObject, url))
        }

        if (!reviewsData.getBoolean("more_available")) return InfoItemsPage(collector, null)

        return InfoItemsPage(collector, Page(listOf(trackId, getNextPageToken(reviews))))
    }

    @Throws(ParsingException::class)
    private fun fetchReviewsData(trackId: String?, token: String?): JsonObject {
        try {
            return toJsonObject(downloader.postWithContentTypeJson(
                REVIEWS_API_URL,
                emptyMap(),
                JsonWriter.string().`object`()
                    .value("tralbum_type", "t")
                    .value("tralbum_id", trackId)
                    .value("token", token)
                    .value("count", 7)
                    .array("exclude_fan_ids").end()
                    .end().done().toByteArray(StandardCharsets.UTF_8)).responseBody())
        } catch (e: IOException) {
            throw ParsingException("Could not fetch reviews", e)
        } catch (e: ReCaptchaException) {
            throw ParsingException("Could not fetch reviews", e)
        }
    }

    @Throws(ParsingException::class)
    private fun getNextPageToken(reviews: JsonArray): String {
        return reviews.stream()
            .filter { o: Any? -> JsonObject::class.java.isInstance(o) }
            .map { obj: Any? -> JsonObject::class.java.cast(obj) }
            .map { review: JsonObject -> review.getString("token") }
            .reduce { _: String?, b: String -> b } // keep only the last element
            .orElseThrow { ParsingException("Could not get token") }
    }

    @get:Throws(ParsingException::class)
    private val trackId: String
        get() {
            val pageProperties = toJsonObject(document?.selectFirst("meta[name=bc-page-properties]")?.attr("content"))
            return pageProperties.getLong("item_id").toString()
        }

    @get:Throws(ExtractionException::class)
    override val isCommentsDisabled: Boolean
        get() = BandcampExtractorHelper.isRadioUrl(url)

    companion object {
        private const val REVIEWS_API_URL = BandcampExtractorHelper.BASE_API_URL + "/tralbumcollectors/2/reviews"
    }
}
