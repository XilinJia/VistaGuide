package ac.mdiq.vista.extractor.services.media_ccc

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import ac.mdiq.vista.downloader.DownloaderTestImpl.Companion.getInstance
import ac.mdiq.vista.extractor.Vista.init
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.services.media_ccc.linkHandler.MediaCCCStreamLinkHandlerFactory

class MediaCCCStreamLinkHandlerFactoryTest {
    @get:Throws(ParsingException::class)
    @get:Test
    val id: Unit
        get() {
            Assertions.assertEquals("jhremote20-3001-abschlusspraesentation_jugend_hackt_remote_2020",
                linkHandler!!.fromUrl("https://media.ccc.de/v/jhremote20-3001-abschlusspraesentation_jugend_hackt_remote_2020").id)
            Assertions.assertEquals("jhremote20-3001-abschlusspraesentation_jugend_hackt_remote_2020",
                linkHandler!!.fromUrl("https://media.ccc.de/v/jhremote20-3001-abschlusspraesentation_jugend_hackt_remote_2020?a=b").id)
            Assertions.assertEquals("jhremote20-3001-abschlusspraesentation_jugend_hackt_remote_2020",
                linkHandler!!.fromUrl("https://media.ccc.de/v/jhremote20-3001-abschlusspraesentation_jugend_hackt_remote_2020#3").id)
            Assertions.assertEquals("jhremote20-3001-abschlusspraesentation_jugend_hackt_remote_2020",
                linkHandler!!.fromUrl("https://api.media.ccc.de/public/events/jhremote20-3001-abschlusspraesentation_jugend_hackt_remote_2020&a=b").id)
        }

    @get:Throws(ParsingException::class)
    @get:Test
    val url: Unit
        get() {
            Assertions.assertEquals("https://media.ccc.de/v/jhremote20-3001-abschlusspraesentation_jugend_hackt_remote_2020",
                linkHandler!!.fromUrl("https://media.ccc.de/v/jhremote20-3001-abschlusspraesentation_jugend_hackt_remote_2020").url)
            Assertions.assertEquals("https://media.ccc.de/v/jhremote20-3001-abschlusspraesentation_jugend_hackt_remote_2020",
                linkHandler!!.fromUrl("https://api.media.ccc.de/public/events/jhremote20-3001-abschlusspraesentation_jugend_hackt_remote_2020?b=a&a=b").url)
            Assertions.assertEquals("https://media.ccc.de/v/jhremote20-3001-abschlusspraesentation_jugend_hackt_remote_2020",
                linkHandler!!.fromId("jhremote20-3001-abschlusspraesentation_jugend_hackt_remote_2020").url)
        }

    companion object {
        private var linkHandler: MediaCCCStreamLinkHandlerFactory? = null


        @BeforeAll
        fun setUp(): Unit {
            linkHandler = MediaCCCStreamLinkHandlerFactory.instance
            init(getInstance()!!)
        }
    }
}
