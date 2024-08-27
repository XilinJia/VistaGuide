package ac.mdiq.vista.extractor.services.soundcloud

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import ac.mdiq.vista.downloader.DownloaderTestImpl.Companion.getInstance
import ac.mdiq.vista.extractor.Vista.init
import ac.mdiq.vista.extractor.services.soundcloud.SoundcloudParsingHelper.resolveIdWithWidgetApi
import ac.mdiq.vista.extractor.services.soundcloud.SoundcloudParsingHelper.resolveUrlWithEmbedPlayer

internal class SoundcloudParsingHelperTest {
    @Test
    @Throws(Exception::class)
    fun resolveUrlWithEmbedPlayerTest() {
        assertEquals("https://soundcloud.com/trapcity",
            resolveUrlWithEmbedPlayer("https://api.soundcloud.com/users/26057743"))
        assertEquals("https://soundcloud.com/nocopyrightsounds",
            resolveUrlWithEmbedPlayer("https://api.soundcloud.com/users/16069159"))
        assertEquals("https://soundcloud.com/trapcity",
            resolveUrlWithEmbedPlayer("https://api-v2.soundcloud.com/users/26057743"))
        assertEquals("https://soundcloud.com/nocopyrightsounds",
            resolveUrlWithEmbedPlayer("https://api-v2.soundcloud.com/users/16069159"))
    }

    @Test
    @Throws(Exception::class)
    fun resolveIdWithWidgetApiTest() {
        assertEquals("26057743", resolveIdWithWidgetApi("https://soundcloud.com/trapcity"))
        assertEquals("16069159", resolveIdWithWidgetApi("https://soundcloud.com/nocopyrightsounds"))

        assertEquals("26057743", resolveIdWithWidgetApi("https://on.soundcloud.com/Rr2JyfFcYwbawpw49"))
        assertEquals("1818813498", resolveIdWithWidgetApi("https://on.soundcloud.com/a8QmYdMnmxnsSTEp9"))
        assertEquals("1468401502", resolveIdWithWidgetApi("https://on.soundcloud.com/rdt7e"))
    }

    companion object {

        @BeforeAll
        fun setUp() {
            init(getInstance())
        }
    }
}
