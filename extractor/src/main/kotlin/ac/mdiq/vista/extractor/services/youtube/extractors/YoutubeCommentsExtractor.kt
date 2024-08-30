package ac.mdiq.vista.extractor.services.youtube.extractors

import com.grack.nanojson.JsonArray
import com.grack.nanojson.JsonObject
import com.grack.nanojson.JsonWriter
import ac.mdiq.vista.extractor.Page
import ac.mdiq.vista.extractor.StreamingService
import ac.mdiq.vista.extractor.comments.CommentsExtractor
import ac.mdiq.vista.extractor.comments.CommentsInfoItem
import ac.mdiq.vista.extractor.comments.CommentsInfoItemsCollector
import ac.mdiq.vista.extractor.downloader.Downloader
import ac.mdiq.vista.extractor.exceptions.ExtractionException
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.linkhandler.ListLinkHandler
import ac.mdiq.vista.extractor.localization.TimeAgoParser
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.getJsonPostResponse
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.getTextFromObject
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.prepareDesktopJsonBuilder
import ac.mdiq.vista.extractor.utils.JsonUtils.getArray
import ac.mdiq.vista.extractor.utils.JsonUtils.getString
import ac.mdiq.vista.extractor.utils.Utils.removeNonDigitCharacters
import java.io.IOException
import java.nio.charset.StandardCharsets


class YoutubeCommentsExtractor(service: StreamingService, uiHandler: ListLinkHandler) : CommentsExtractor(service, uiHandler) {

    /**
     * Whether comments are disabled on video.
     */
    override var isCommentsDisabled: Boolean = false
        private set

    /**
     * The second ajax <b>/next</b> response.
    */
    private var ajaxJson: JsonObject? = null

    @get:Throws(IOException::class, ExtractionException::class)

    override val initialPage: InfoItemsPage<CommentsInfoItem>
        get() {
            if (isCommentsDisabled) return infoItemsPageForDisabledComments
            return extractComments(ajaxJson)
        }

    /**
     * Finds the initial comments token and initializes commentsDisabled.
     * <br></br>
     * Also sets [.commentsDisabled].
     *
     * @return the continuation token or null if none was found
     */
    private fun findInitialCommentsToken(nextResponse: JsonObject): String? {
        val contents = getJsonContents(nextResponse) ?: return null

        // For videos where comments are unavailable, this would be null

        val token = contents.stream() // Only use JsonObjects
            .filter { o: Any? -> JsonObject::class.java.isInstance(o) }
            .map { obj: Any? -> JsonObject::class.java.cast(obj) } // Check if the comment-section is present
            .filter { jObj: JsonObject? ->
                try { return@filter "comments-section" == getString(jObj!!, "itemSectionRenderer.targetId") } catch (ignored: ParsingException) { return@filter false }
            }
            .findFirst() // Extract the token (or null in case of error)
            .map { itemSectionRenderer: JsonObject ->
                try {
                    return@map getString(itemSectionRenderer.getObject("itemSectionRenderer").getArray("contents").getObject(0),
                        "continuationItemRenderer.continuationEndpoint" + ".continuationCommand.token")
                } catch (ignored: ParsingException) { return@map null }
            }
            .orElse(null)

        // The comments are disabled if we couldn't get a token
        isCommentsDisabled = token == null
        return token
    }

    private fun getJsonContents(nextResponse: JsonObject): JsonArray? {
        return try { getArray(nextResponse, "contents.twoColumnWatchNextResults.results.results.contents") } catch (e: ParsingException) { null }
    }


    @Throws(ParsingException::class)
    private fun getMutationPayloadFromEntityKey(mutations: JsonArray, commentKey: String): JsonObject {
        return mutations.stream()
            .filter { o: Any? -> JsonObject::class.java.isInstance(o) }
            .map { obj: Any? -> JsonObject::class.java.cast(obj) }
            .filter { mutation: JsonObject -> commentKey == mutation.getString("entityKey") }
            .findFirst()
            .orElseThrow {
                ParsingException("Could not get comment entity payload mutation")
            }
            .getObject("payload")
    }


    private val infoItemsPageForDisabledComments: InfoItemsPage<CommentsInfoItem>
        get() = InfoItemsPage(emptyList(), null, emptyList())

    @Throws(ExtractionException::class)
    private fun getNextPage(jsonObject: JsonObject): Page? {
        val onResponseReceivedEndpoints = jsonObject.getArray("onResponseReceivedEndpoints")

        // Prevent ArrayIndexOutOfBoundsException
        if (onResponseReceivedEndpoints.isEmpty()) return null

        val continuationItemsArray: JsonArray
        try {
            val endpoint = onResponseReceivedEndpoints.getObject(onResponseReceivedEndpoints.size - 1)
            continuationItemsArray = endpoint
                .getObject("reloadContinuationItemsCommand", endpoint.getObject("appendContinuationItemsAction"))
                .getArray("continuationItems")
        } catch (e: Exception) { return null }
        // Prevent ArrayIndexOutOfBoundsException
        if (continuationItemsArray.isEmpty()) return null

        val continuationItemRenderer = continuationItemsArray
            .getObject(continuationItemsArray.size - 1)
            .getObject("continuationItemRenderer")

        val jsonPath = if (continuationItemRenderer.has("button")) "button.buttonRenderer.command.continuationCommand.token" else "continuationEndpoint.continuationCommand.token"

        val continuation: String
        try { continuation = getString(continuationItemRenderer, jsonPath) } catch (e: Exception) { return null }
        return getNextPage(continuation)
    }


    @Throws(ParsingException::class)
    private fun getNextPage(continuation: String): Page {
        return Page(url, continuation) // URL is ignored tho
    }

    @Throws(IOException::class, ExtractionException::class)
    override fun getPage(page: Page?): InfoItemsPage<CommentsInfoItem> {
        if (isCommentsDisabled) return infoItemsPageForDisabledComments

        require(!(page == null || page.id.isNullOrEmpty())) { "Page doesn't have the continuation." }

        val localization = extractorLocalization
        // @formatter:off
         val body = JsonWriter.string(prepareDesktopJsonBuilder(localization, extractorContentCountry).value("continuation", page.id).done()).toByteArray(StandardCharsets.UTF_8)
                // @formatter:on
        val jsonObject = getJsonPostResponse("next", body, localization)

        return extractComments(jsonObject)
    }

    @Throws(ExtractionException::class)
    private fun extractComments(jsonObject: JsonObject?): InfoItemsPage<CommentsInfoItem> {
        if (jsonObject == null) throw ExtractionException("jsonObject is null")
        val collector = CommentsInfoItemsCollector(serviceId)
        collectCommentsFrom(collector, jsonObject)
        return InfoItemsPage(collector, getNextPage(jsonObject))
    }

    @Throws(ParsingException::class)
    private fun collectCommentsFrom(collector: CommentsInfoItemsCollector, jsonObject: JsonObject) {
        val onResponseReceivedEndpoints = jsonObject.getArray("onResponseReceivedEndpoints")
        // Prevent ArrayIndexOutOfBoundsException
        if (onResponseReceivedEndpoints.isEmpty()) return
        val commentsEndpoint = onResponseReceivedEndpoints.getObject(onResponseReceivedEndpoints.size - 1)

        val path = when {
            commentsEndpoint.has("reloadContinuationItemsCommand") -> "reloadContinuationItemsCommand.continuationItems"
            commentsEndpoint.has("appendContinuationItemsAction") -> "appendContinuationItemsAction.continuationItems"
            else -> return
        }

        val contents: JsonArray
        // A copy of the array is needed, otherwise the continuation item is removed from the
        // original object which is used to get the continuation
        // No comments
        try { contents = JsonArray(getArray(commentsEndpoint, path)) } catch (e: Exception) { return }

        val index = contents.size - 1
        if (!contents.isEmpty() && contents.getObject(index).has("continuationItemRenderer")) contents.removeAt(index)

        // The mutations object, which is returned in the comments' continuation
        // It contains parts of comment data when comments are returned with a view model
        val mutations = jsonObject.getObject("frameworkUpdates")
            .getObject("entityBatchUpdate")
            .getArray("mutations")
        val videoUrl = url
        val timeAgoParser = timeAgoParser

        for (o in contents) {
            if (o !is JsonObject) continue
            collectCommentItem(mutations, o, collector, videoUrl, timeAgoParser)
        }
    }

    @Throws(ParsingException::class)
    private fun collectCommentItem(mutations: JsonArray, content: JsonObject, collector: CommentsInfoItemsCollector, videoUrl: String, timeAgoParser: TimeAgoParser) {
        when {
            content.has("commentThreadRenderer") -> {
                val commentThreadRenderer = content.getObject("commentThreadRenderer")
                if (commentThreadRenderer.has(COMMENT_VIEW_MODEL_KEY)) {
                    val commentViewModel = commentThreadRenderer.getObject(COMMENT_VIEW_MODEL_KEY).getObject(COMMENT_VIEW_MODEL_KEY)
                    collector.commit(YoutubeCommentsEUVMInfoItemExtractor(commentViewModel,
                        commentThreadRenderer.getObject("replies").getObject("commentRepliesRenderer"),
                        getMutationPayloadFromEntityKey(mutations, commentViewModel.getString("commentKey", "")).getObject("commentEntityPayload"),
                        getMutationPayloadFromEntityKey(mutations, commentViewModel.getString("toolbarStateKey", "")).getObject("engagementToolbarStateEntityPayload"),
                        videoUrl, timeAgoParser))
                } else if (commentThreadRenderer.has("comment")) {
                    collector.commit(YoutubeCommentsInfoItemExtractor(
                        commentThreadRenderer.getObject("comment").getObject(COMMENT_RENDERER_KEY),
                        commentThreadRenderer.getObject("replies").getObject("commentRepliesRenderer"),
                        videoUrl, timeAgoParser))
                }
            }
            content.has(COMMENT_VIEW_MODEL_KEY) -> {
                val commentViewModel = content.getObject(COMMENT_VIEW_MODEL_KEY)
                collector.commit(YoutubeCommentsEUVMInfoItemExtractor(commentViewModel, null,
                    getMutationPayloadFromEntityKey(mutations, commentViewModel.getString("commentKey", "")).getObject("commentEntityPayload"),
                    getMutationPayloadFromEntityKey(mutations, commentViewModel.getString("toolbarStateKey", "")).getObject("engagementToolbarStateEntityPayload"),
                    videoUrl, timeAgoParser))
            }
            content.has(COMMENT_RENDERER_KEY) -> {
                // commentRenderers are directly returned for comment replies, so there is no
                // commentRepliesRenderer to provide
                // Also, YouTube has only one comment reply level
                collector.commit(YoutubeCommentsInfoItemExtractor(content.getObject(COMMENT_RENDERER_KEY), null, videoUrl, timeAgoParser))
            }
        }
    }

    @Throws(IOException::class, ExtractionException::class)
    override fun onFetchPage(downloader: Downloader) {
        val localization = extractorLocalization
        // @formatter:off
         val body = JsonWriter.string(
        prepareDesktopJsonBuilder(localization, extractorContentCountry)
        .value("videoId", id)
        .done())
        .toByteArray(StandardCharsets.UTF_8)

        // @formatter:on
        val initialToken = findInitialCommentsToken(getJsonPostResponse("next", body, localization)) ?: return

        // @formatter:off
         val ajaxBody = JsonWriter.string(
        prepareDesktopJsonBuilder(localization, extractorContentCountry)
        .value("continuation", initialToken)
        .done())
        .toByteArray(StandardCharsets.UTF_8)

        // @formatter:on
        ajaxJson = getJsonPostResponse("next", ajaxBody, localization)
    }


    @get:Throws(ExtractionException::class)
    override val commentsCount: Int
        get() {
            assertPageFetched()
            if (isCommentsDisabled) return -1
            val countText = ajaxJson!!.getArray("onResponseReceivedEndpoints")
                .getObject(0)
                .getObject("reloadContinuationItemsCommand")
                .getArray("continuationItems")
                .getObject(0)
                .getObject("commentsHeaderRenderer")
                .getObject("countText")

            try { return removeNonDigitCharacters(getTextFromObject(countText)!!).toInt() } catch (e: Exception) { throw ExtractionException("Unable to get comments count", e) }
        }

    companion object {
        private const val COMMENT_VIEW_MODEL_KEY = "commentViewModel"
        private const val COMMENT_RENDERER_KEY = "commentRenderer"
    }
}
