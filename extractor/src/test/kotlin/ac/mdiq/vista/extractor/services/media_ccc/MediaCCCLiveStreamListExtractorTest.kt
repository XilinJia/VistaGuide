package ac.mdiq.vista.extractor.services.media_ccc

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import ac.mdiq.vista.downloader.DownloaderTestImpl.Companion.getInstance
import ac.mdiq.vista.extractor.InfoItem
import ac.mdiq.vista.extractor.Vista.init
import ac.mdiq.vista.extractor.ServiceList.MediaCCC
import ac.mdiq.vista.extractor.kiosk.KioskExtractor

class MediaCCCLiveStreamListExtractorTest {
    @Throws(Exception::class)
    @Test
    fun conferencesListTest() {
            val items: List<InfoItem> = extractor!!.initialPage?.items ?: listOf()
            // just test if there is an exception thrown
        }

    companion object {
        private var extractor: KioskExtractor<*>? = null


        @BeforeAll
        @Throws(Exception::class)
        fun setUpClass(): Unit {
            init(getInstance()!!)
            extractor = MediaCCC.getKioskList()?.getExtractorById("live", null)
            extractor!!.fetchPage()
        }
    }
}
