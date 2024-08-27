package ac.mdiq.vista.extractor.services.media_ccc

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import ac.mdiq.vista.downloader.DownloaderTestImpl.Companion.getInstance
import ac.mdiq.vista.extractor.ExtractorAsserts
import ac.mdiq.vista.extractor.InfoItem
import ac.mdiq.vista.extractor.Vista.init
import ac.mdiq.vista.extractor.ServiceList.MediaCCC
import ac.mdiq.vista.extractor.kiosk.KioskExtractor

/**
 * Test [MediaCCCConferenceKiosk]
 */
class MediaCCCConferenceListExtractorTest {
    @Throws(Exception::class)
    @Test
    fun conferencesListTest() {
            ExtractorAsserts.assertGreaterOrEqual(174, extractor!!.initialPage!!.items!!.size.toLong())
        }

    @ParameterizedTest
    @ValueSource(strings = ["FrOSCon 2016", "ChaosWest @ 35c3", "CTreffOS chaOStalks", "Datenspuren 2015", "Chaos Singularity 2017", "SIGINT10", "Vintage Computing Festival Berlin 2015", "FIfFKon 2015", "33C3: trailers", "Blinkenlights"
    ])
    @Throws(Exception::class)
    fun conferenceTypeTest(name: String) {
        val itemList: List<InfoItem> = extractor!!.initialPage?.items ?: listOf()
        Assertions.assertTrue(itemList.stream().anyMatch { item: InfoItem -> name == item.name })
    }

    companion object {
        private var extractor: KioskExtractor<*>? = null


        @BeforeAll
        @Throws(Exception::class)
        fun setUpClass(): Unit {
            init(getInstance()!!)
            extractor = MediaCCC.getKioskList()?.getExtractorById("conferences", null)
            extractor!!.fetchPage()
        }
    }
}
