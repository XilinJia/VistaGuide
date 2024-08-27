package ac.mdiq.vista.extractor.services.peertube.extractors

import com.grack.nanojson.JsonArray
import com.grack.nanojson.JsonObject
import com.grack.nanojson.JsonWriter
import org.jsoup.Jsoup
import ac.mdiq.vista.extractor.Image
import ac.mdiq.vista.extractor.Page
import ac.mdiq.vista.extractor.ServiceList
import ac.mdiq.vista.extractor.comments.CommentsInfoItemExtractor
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.localization.DateWrapper
import ac.mdiq.vista.extractor.services.peertube.PeertubeParsingHelper.getAvatarsFromOwnerAccountOrVideoChannelObject
import ac.mdiq.vista.extractor.services.peertube.PeertubeParsingHelper.parseDateFrom
import ac.mdiq.vista.extractor.stream.Description
import ac.mdiq.vista.extractor.utils.JsonUtils.getNumber
import ac.mdiq.vista.extractor.utils.JsonUtils.getString
import java.nio.charset.StandardCharsets
import java.util.*


class PeertubeCommentsInfoItemExtractor(
        private val item: JsonObject,
        private val children: JsonArray?,
        url: String,
        private val baseUrl: String,
        private val isReply: Boolean)
    : CommentsInfoItemExtractor {

    override var replyCount: Int = -1
        get() {
            // The totalReplies field is inaccurate for nested replies and sometimes returns 0
            // although there are replies to that reply stored in children.
            if (field < 0) {
                field = if (!children.isNullOrEmpty()) children.size else getNumber(item, "totalReplies").toInt()
            }
            return field
        }

    override var url: String = url
        get() = "${field}/$commentId"


    override val thumbnails: List<Image>
        get() = uploaderAvatars

    @get:Throws(ParsingException::class)
    override val name: String
        get() = getString(item, "account.displayName")

    @get:Throws(ParsingException::class)
    override val textualUploadDate: String
        get() = getString(item, "createdAt")

    @get:Throws(ParsingException::class)
    override val uploadDate: DateWrapper
        get() {
            val textualUploadDate = textualUploadDate
            return DateWrapper(parseDateFrom(textualUploadDate))
        }

    @get:Throws(ParsingException::class)

    override val commentText: Description
        get() {
            val htmlText = getString(item, "text")
            try {
                val doc = Jsoup.parse(htmlText)
                val text = doc.body().text()
                return Description(text, Description.PLAIN_TEXT)
            } catch (e: Exception) {
                val text = htmlText.replace("(?s)<[^>]*>(\\s*<[^>]*>)*".toRegex(), "")
                return Description(text, Description.PLAIN_TEXT)
            }
        }

    override val commentId: String
        get() = Objects.toString(item.getLong("id"), null)


    override val uploaderAvatars: List<Image>
        get() = getAvatarsFromOwnerAccountOrVideoChannelObject(baseUrl, item.getObject("account"))

    @get:Throws(ParsingException::class)
    override val uploaderName: String
        get() = (getString(item, "account.name") + "@"
                + getString(item, "account.host"))

    @get:Throws(ParsingException::class)
    override val uploaderUrl: String
        get() {
            val name = getString(item, "account.name")
            val host = getString(item, "account.host")
            return ServiceList.PeerTube.getChannelLHFactory().fromId("accounts/$name@$host", baseUrl).url
        }

    @get:Throws(ParsingException::class)
    override val replies: Page?
        get() {
            if (replyCount == 0) {
                return null
            }
            val threadId = getNumber(item, "threadId").toString()
            val repliesUrl = "$url/$threadId"
            if (isReply && !children.isNullOrEmpty()) {
                // Nested replies are already included in the original thread's request.
                // Wrap the replies into a JsonObject, because the original thread's request body
                // is also structured like a JsonObject.
                val pageContent = JsonObject()
                pageContent[PeertubeCommentsExtractor.CHILDREN] = children
                return Page(repliesUrl, threadId, JsonWriter.string(pageContent).toByteArray(StandardCharsets.UTF_8))
            }
            return Page(repliesUrl, threadId)
        }

//    override val replyCount: Int
//        get() {
//        if (replyCount == null) {
//            replyCount = if (children != null && !children.isEmpty()) {
//                // The totalReplies field is inaccurate for nested replies and sometimes returns 0
//                // although there are replies to that reply stored in children.
//                children.size
//            } else {
//                getNumber(item, "totalReplies").toInt()
//            }
//        }
//        return replyCount!!
//    }

    override fun hasCreatorReply(): Boolean {
        return (item.has("totalRepliesFromVideoAuthor") && item.getInt("totalRepliesFromVideoAuthor") > 0)
    }
}
