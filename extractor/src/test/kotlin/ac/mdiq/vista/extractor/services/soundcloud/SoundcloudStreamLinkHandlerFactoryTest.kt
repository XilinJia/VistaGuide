package ac.mdiq.vista.extractor.services.soundcloud

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.Executable
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource
import ac.mdiq.vista.downloader.DownloaderTestImpl.Companion.getInstance
import ac.mdiq.vista.extractor.Vista.init
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.services.soundcloud.linkHandler.SoundcloudStreamLinkHandlerFactory

/**
 * Test for [SoundcloudStreamLinkHandlerFactory]
 */
class SoundcloudStreamLinkHandlerFactoryTest {
    @Test
    fun idWithNullAsUrl() {
            Assertions.assertThrows(IllegalArgumentException::class.java) { linkHandler!!.fromUrl("") }
        }

    @ParameterizedTest
    @ValueSource(strings = ["https://soundcloud.com/liluzivert/t.e.s.t", "https://soundcloud.com/liluzivert/tracks", "https://soundcloud.com/"
    ])
    fun getIdForInvalidUrls(invalidUrl: String) {
        Assertions.assertThrows(ParsingException::class.java, Executable { linkHandler!!.fromUrl(invalidUrl).id })
    }

    @ParameterizedTest
    @CsvSource(value = ["309689103,https://soundcloud.com/liluzivert/15-ysl", "309689082,https://www.soundcloud.com/liluzivert/15-luv-scars-ko", "309689035,http://soundcloud.com/liluzivert/15-boring-shit", "259273264,https://soundcloud.com/liluzivert/ps-qs-produced-by-don-cannon/", "294488599,http://www.soundcloud.com/liluzivert/secure-the-bag-produced-by-glohan-beats", "245710200,HtTpS://sOuNdClOuD.cOm/lIeuTeNaNt_rAe/bOtS-wAs-wOlLeN-wIr-tRinKeN", "294488147,https://soundcloud.com/liluzivert/fresh-produced-by-zaytoven#t=69", "294487876,https://soundcloud.com/liluzivert/threesome-produced-by-zaytoven#t=1:09", "294487684,https://soundcloud.com/liluzivert/blonde-brigitte-produced-manny-fresh#t=1:9", "294487428,https://soundcloud.com/liluzivert/today-produced-by-c-note#t=1m9s", "294487157,https://soundcloud.com/liluzivert/changed-my-phone-produced-by-c-note#t=1m09s", "44556776,https://soundcloud.com/kechuspider-sets-1/last-days"
    ])
    @Throws(ParsingException::class)
    fun getId(expectedId: String?, url: String) {
        Assertions.assertEquals(expectedId, linkHandler!!.fromUrl(url).id)
    }


    @ParameterizedTest
    @ValueSource(strings = ["https://soundcloud.com/liluzivert/15-ysl", "https://www.soundcloud.com/liluzivert/15-luv-scars-ko", "http://soundcloud.com/liluzivert/15-boring-shit", "http://www.soundcloud.com/liluzivert/secure-the-bag-produced-by-glohan-beats", "HtTpS://sOuNdClOuD.cOm/LiLuZiVeRt/In-O4-pRoDuCeD-bY-dP-bEaTz", "https://soundcloud.com/liluzivert/fresh-produced-by-zaytoven#t=69", "https://soundcloud.com/liluzivert/threesome-produced-by-zaytoven#t=1:09", "https://soundcloud.com/liluzivert/blonde-brigitte-produced-manny-fresh#t=1:9", "https://soundcloud.com/liluzivert/today-produced-by-c-note#t=1m9s", "https://soundcloud.com/liluzivert/changed-my-phone-produced-by-c-note#t=1m09s"
    ])
    @Throws(ParsingException::class)
    fun testAcceptUrl(url: String) {
        Assertions.assertTrue(linkHandler!!.acceptUrl(url))
    }

    companion object {
        private var linkHandler: SoundcloudStreamLinkHandlerFactory? = null


        @BeforeAll
        @Throws(Exception::class)
        fun setUp(): Unit {
            linkHandler = SoundcloudStreamLinkHandlerFactory.instance
            init(getInstance()!!)
        }
    }
}
