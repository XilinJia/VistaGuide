package ac.mdiq.vista.extractor.services.peertube

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import ac.mdiq.vista.downloader.DownloaderTestImpl.Companion.getInstance
import ac.mdiq.vista.extractor.ListExtractor.InfoItemsPage
import ac.mdiq.vista.extractor.Vista.init
import ac.mdiq.vista.extractor.ServiceList.PeerTube
import ac.mdiq.vista.extractor.comments.CommentsInfo.Companion.getInfo
import ac.mdiq.vista.extractor.comments.CommentsInfo.Companion.getMoreItems
import ac.mdiq.vista.extractor.comments.CommentsInfoItem
import ac.mdiq.vista.extractor.exceptions.ExtractionException
import ac.mdiq.vista.extractor.services.DefaultTests.defaultTestImageCollection
import ac.mdiq.vista.extractor.services.peertube.extractors.PeertubeCommentsExtractor
import java.io.IOException
import java.util.*
import java.util.function.Consumer

object PeertubeCommentsExtractorTest {
    private fun findCommentWithId(id: String, comments: List<CommentsInfoItem>): Optional<CommentsInfoItem> {
        return comments
            .stream()
            .filter { c: CommentsInfoItem -> c.commentId == id }
            .findFirst()
    }

    @Throws(IOException::class, ExtractionException::class)
    private fun findNestedCommentWithId(id: String, comment: CommentsInfoItem): Boolean {
        if (comment.commentId == id) return true
        return PeerTube
            .getCommentsExtractor(comment.url)
            ?.getPage(comment.replies)
            ?.items
            ?.stream()
            ?.map<Boolean> { c: CommentsInfoItem ->
                try {
                    return@map findNestedCommentWithId(id, c)
                } catch (ignored: Exception) {
                    return@map false
                }
            }
            ?.reduce { a: Boolean, b: Boolean -> a || b }
            ?.orElse(false) ?: false
    }

    class Default {
        @Test
        @Throws(IOException::class, ExtractionException::class)
        fun testGetComments() {
            val comment = "I love this"

            var comments = extractor!!.initialPage
            var result = findInComments(comments!!, comment)

            while (comments!!.hasNextPage() && !result) {
                comments = extractor!!.getPage(
                    comments.nextPage)
                result = findInComments(comments!!, comment)
            }

            Assertions.assertTrue(result)
        }

        @Test
        @Throws(IOException::class, ExtractionException::class)
        fun testGetCommentsFromCommentsInfo() {
            val comment = "I love this. ❤"

            val commentsInfo =
                getInfo("https://framatube.org/w/kkGMgK9ZtnKfYAgnEtQxbv")
            Assertions.assertEquals("Comments", commentsInfo!!.name)

            var result = findInComments(commentsInfo.relatedItems!!, comment)

            var nextPage = commentsInfo.nextPage
            var moreItems: InfoItemsPage<CommentsInfoItem>? = InfoItemsPage(listOf(), nextPage, listOf())
            while (moreItems!!.hasNextPage() && !result) {
                moreItems = getMoreItems(PeerTube, commentsInfo, nextPage)
                result = findInComments(moreItems!!.items!!, comment)
                nextPage = moreItems.nextPage
            }

            Assertions.assertTrue(result)
        }

        @Test
        @Throws(IOException::class, ExtractionException::class)
        fun testGetCommentsAllData() {
            extractor!!.initialPage
                ?.items
                ?.forEach(Consumer<CommentsInfoItem> { commentsInfoItem: CommentsInfoItem ->
                    Assertions.assertFalse(commentsInfoItem.uploaderUrl.isNullOrEmpty())
                    Assertions.assertFalse(commentsInfoItem.uploaderName.isNullOrEmpty())
                    defaultTestImageCollection(commentsInfoItem.uploaderAvatars)
                    Assertions.assertFalse(commentsInfoItem.commentId.isNullOrEmpty())
                    Assertions.assertFalse(commentsInfoItem.commentText.content.isNullOrEmpty())
                    Assertions.assertFalse(commentsInfoItem.name.isNullOrEmpty())
                    Assertions.assertFalse(commentsInfoItem.textualUploadDate.isNullOrEmpty())
                    defaultTestImageCollection(commentsInfoItem.thumbnails)
                    Assertions.assertFalse(commentsInfoItem.url.isNullOrEmpty())
                    Assertions.assertEquals(-1, commentsInfoItem.likeCount)
                    Assertions.assertTrue(commentsInfoItem.textualLikeCount.isNullOrEmpty())
                })
        }

        private fun findInComments(comments: InfoItemsPage<CommentsInfoItem>,
                                   comment: String
        ): Boolean {
            return findInComments(comments.items!!, comment)
        }

        private fun findInComments(comments: List<CommentsInfoItem>,
                                   comment: String
        ): Boolean {
            return comments.stream()
                .anyMatch { commentsInfoItem: CommentsInfoItem ->
                    commentsInfoItem.commentText.content!!.contains(comment)
                }
        }

        companion object {
            private var extractor: PeertubeCommentsExtractor? = null


            @BeforeAll
            @Throws(Exception::class)
            fun setUp(): Unit {
                init(getInstance()!!)
                extractor = PeerTube.getCommentsExtractor("https://framatube.org/w/kkGMgK9ZtnKfYAgnEtQxbv") as? PeertubeCommentsExtractor
            }
        }
    }

    class DeletedComments {
        @Test
        @Throws(IOException::class, ExtractionException::class)
        fun testGetComments() {
            val comments = extractor!!.initialPage!!
            Assertions.assertTrue(comments.errors!!.isEmpty())
        }

        @Test
        @Throws(IOException::class, ExtractionException::class)
        fun testGetCommentsFromCommentsInfo() {
            val commentsInfo = getInfo("https://framatube.org/videos/watch/217eefeb-883d-45be-b7fc-a788ad8507d3")
            Assertions.assertTrue(commentsInfo!!.errors.isEmpty())
        }

        companion object {
            private var extractor: PeertubeCommentsExtractor? = null


            @BeforeAll
            @Throws(Exception::class)
            fun setUp(): Unit {
                init(getInstance()!!)
                extractor = PeerTube.getCommentsExtractor("https://framatube.org/videos/watch/217eefeb-883d-45be-b7fc-a788ad8507d3") as? PeertubeCommentsExtractor
            }
        }
    }

    /**
     * Test a video that has comments with nested replies.
     */
    class NestedComments {
        @Test
        @Throws(IOException::class, ExtractionException::class)
        fun testGetComments() {
            Assertions.assertFalse(comments!!.items!!.isEmpty())
            val nestedCommentHeadOpt =
                findCommentWithId("9770", comments!!.items!!)
            Assertions.assertTrue(nestedCommentHeadOpt.isPresent)
            Assertions.assertTrue(findNestedCommentWithId("9773", nestedCommentHeadOpt.get()),
                "The nested comment replies were not found")
        }

        @Test
        fun testHasCreatorReply() {
            assertCreatorReply("9770", true)
            assertCreatorReply("9852", false)
            assertCreatorReply("11239", false)
        }

        companion object {
            private var extractor: PeertubeCommentsExtractor? = null
            private var comments: InfoItemsPage<CommentsInfoItem>? = null


            @BeforeAll
            @Throws(Exception::class)
            fun setUp(): Unit {
                init(getInstance()!!)
                extractor = PeerTube.getCommentsExtractor("https://share.tube/w/vxu4uTstUBAUromWwXGHrq") as? PeertubeCommentsExtractor
                comments = extractor!!.initialPage
            }

            private fun assertCreatorReply(id: String, expected: Boolean) {
                val comment = findCommentWithId(id, comments!!.items!!)
                Assertions.assertTrue(comment.isPresent)
                Assertions.assertEquals(expected, comment.get().hasCreatorReply())
            }
        }
    }
}
