package ac.mdiq.vista.extractor.services.youtube

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import ac.mdiq.vista.downloader.DownloaderTestImpl.Companion.getInstance
import ac.mdiq.vista.extractor.Vista.init
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.services.youtube.linkHandler.YoutubeCommentsLinkHandlerFactory

class YoutubeCommentsLinkHandlerFactoryTest {
    @Test
    fun idWithNullAsUrl() {
            Assertions.assertThrows(NullPointerException::class.java
            ) {
                linkHandler!!.fromId("")
            }
        }

    @Throws(ParsingException::class)
    @Test
    fun idFromYt() {
            Assertions.assertEquals("VM_6n762j6M",
                linkHandler!!.fromUrl("https://www.youtube.com/watch?v=VM_6n762j6M").id)
            Assertions.assertEquals("VM_6n762j6M",
                linkHandler!!.fromUrl("https://m.youtube.com/watch?v=VM_6n762j6M").id)
            Assertions.assertEquals("VM_6n762j6M",
                linkHandler!!.fromUrl("https://youtube.com/watch?v=VM_6n762j6M").id)
            Assertions.assertEquals("VM_6n762j6M",
                linkHandler!!.fromUrl("https://WWW.youtube.com/watch?v=VM_6n762j6M").id)
            Assertions.assertEquals("VM_6n762j6M",
                linkHandler!!.fromUrl("https://youtu.be/VM_6n762j6M").id)
            Assertions.assertEquals("VM_6n762j6M",
                linkHandler!!.fromUrl("https://youtu.be/VM_6n762j6M&t=20").id)
        }

    @Test
    @Throws(ParsingException::class)
    fun testAcceptUrl() {
        Assertions.assertTrue(linkHandler!!.acceptUrl("https://www.youtube.com/watch?v=VM_6n762j6M&t=20"))
        Assertions.assertTrue(linkHandler!!.acceptUrl("https://WWW.youtube.com/watch?v=VM_6n762j6M&t=20"))
        Assertions.assertTrue(linkHandler!!.acceptUrl("https://youtube.com/watch?v=VM_6n762j6M&t=20"))
        Assertions.assertTrue(linkHandler!!.acceptUrl("https://youtu.be/VM_6n762j6M&t=20"))
    }

    @Test
    @Throws(ParsingException::class)
    fun testDeniesUrl() {
        Assertions.assertFalse(linkHandler!!.acceptUrl("https://www.you com/watch?v=VM_6n762j6M"))
        Assertions.assertFalse(linkHandler!!.acceptUrl("https://com/watch?v=VM_6n762j6M"))
        Assertions.assertFalse(linkHandler!!.acceptUrl("htt ://com/watch?v=VM_6n762j6M"))
        Assertions.assertFalse(linkHandler!!.acceptUrl("ftp://www.youtube.com/watch?v=VM_6n762j6M"))
    }

    @Throws(ParsingException::class)
    @Test
    fun idFromInvidious() {
            Assertions.assertEquals("VM_6n762j6M",
                linkHandler!!.fromUrl("https://www.invidio.us/watch?v=VM_6n762j6M").id)
            Assertions.assertEquals("VM_6n762j6M",
                linkHandler!!.fromUrl("https://invidio.us/watch?v=VM_6n762j6M").id)
            Assertions.assertEquals("VM_6n762j6M",
                linkHandler!!.fromUrl("https://INVIDIO.US/watch?v=VM_6n762j6M").id)
            Assertions.assertEquals("VM_6n762j6M",
                linkHandler!!.fromUrl("https://invidio.us/VM_6n762j6M").id)
            Assertions.assertEquals("VM_6n762j6M",
                linkHandler!!.fromUrl("https://invidio.us/VM_6n762j6M&t=20").id)
        }

    @Throws(ParsingException::class)
    @Test
    fun idFromY2ube() {
            Assertions.assertEquals("VM_6n762j6M",
                linkHandler!!.fromUrl("https://y2u.be/VM_6n762j6M").id)
            Assertions.assertEquals("VM_6n762j6M",
                linkHandler!!.fromUrl("https://Y2U.Be/VM_6n762j6M").id)
        }

    companion object {
        private var linkHandler: YoutubeCommentsLinkHandlerFactory? = null


        @BeforeAll
        fun setUp() {
            init(getInstance())
            linkHandler = YoutubeCommentsLinkHandlerFactory.instance
        }
    }
}
