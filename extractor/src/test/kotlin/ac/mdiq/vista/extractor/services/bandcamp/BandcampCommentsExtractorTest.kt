package ac.mdiq.vista.extractor.services.bandcamp

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import ac.mdiq.vista.downloader.DownloaderTestImpl.Companion.getInstance
import ac.mdiq.vista.extractor.Vista.init
import ac.mdiq.vista.extractor.ServiceList.Bandcamp
import ac.mdiq.vista.extractor.comments.CommentsExtractor
import ac.mdiq.vista.extractor.exceptions.ExtractionException
import ac.mdiq.vista.extractor.services.DefaultTests.defaultTestListOfItems
import java.io.IOException

class BandcampCommentsExtractorTest {
    @Test
    @Throws(IOException::class, ExtractionException::class)
    fun hasComments() {
        Assertions.assertTrue(extractor!!.initialPage!!.items!!.size >= 3)
    }

    @Test
    @Throws(IOException::class, ExtractionException::class)
    fun testGetCommentsAllData() {
        val comments = extractor!!.initialPage
        Assertions.assertTrue(comments!!.hasNextPage())

        defaultTestListOfItems(Bandcamp, comments.items!!, comments.errors!!)
        for (c in comments.items!!) {
            Assertions.assertFalse(c.uploaderName.isNullOrEmpty())
            BandcampTestUtils.testImages(c.uploaderAvatars)
            Assertions.assertFalse(c.commentText.content.isNullOrEmpty())
            Assertions.assertFalse(c.name.isNullOrEmpty())
            BandcampTestUtils.testImages(c.thumbnails)
            Assertions.assertFalse(c.url.isNullOrEmpty())
            Assertions.assertEquals(-1, c.likeCount)
            Assertions.assertTrue(c.textualLikeCount.isNullOrEmpty())
        }
    }

    companion object {
        private var extractor: CommentsExtractor? = null


        @BeforeAll
        @Throws(ExtractionException::class, IOException::class)
        fun setUp(): Unit {
            init(getInstance())
            extractor = Bandcamp.getCommentsExtractor("https://floatingpoints.bandcamp.com/album/promises")
            extractor!!.fetchPage()
        }
    }
}
