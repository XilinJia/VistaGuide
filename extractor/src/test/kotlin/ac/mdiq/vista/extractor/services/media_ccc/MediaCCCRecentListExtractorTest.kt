package ac.mdiq.vista.extractor.services.media_ccc

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.Executable
import ac.mdiq.vista.downloader.DownloaderTestImpl.Companion.getInstance
import ac.mdiq.vista.extractor.ExtractorAsserts
import ac.mdiq.vista.extractor.Vista.init
import ac.mdiq.vista.extractor.ServiceList.MediaCCC
import ac.mdiq.vista.extractor.kiosk.KioskExtractor
import ac.mdiq.vista.extractor.stream.StreamInfoItem
import java.util.stream.Stream

class MediaCCCRecentListExtractorTest {
    @Test
    @Throws(Exception::class)
    fun testStreamList() {
        val items: List<StreamInfoItem> = (extractor!!.initialPage?.items ?: listOf()).map { it as StreamInfoItem }
        Assertions.assertFalse(items.isEmpty(), "No items returned")
        Assertions.assertAll(items.stream().flatMap { item: StreamInfoItem -> this.getAllConditionsForItem(item) })
    }

    private fun getAllConditionsForItem(item: StreamInfoItem): Stream<Executable?> {
        return Stream.of(
            Executable {
                Assertions.assertFalse(item.name.isNullOrEmpty(), "Name=[" + item.name + "] of " + item + " is empty or null")
            },
            Executable {
                ExtractorAsserts.assertGreater(0,
                    item.duration,
                    "Duration[=" + item.duration + "] of " + item + " is <= 0"
                )
            }
        )
    }

    companion object {
        private var extractor: KioskExtractor<*>? = null


        @BeforeAll
        @Throws(Exception::class)
        fun setUpClass(): Unit {
            init(getInstance()!!)
            extractor = MediaCCC.getKioskList()?.getExtractorById("recent", null)
            extractor!!.fetchPage()
        }
    }
}
