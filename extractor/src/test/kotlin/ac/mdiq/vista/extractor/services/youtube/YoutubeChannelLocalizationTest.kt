package ac.mdiq.vista.extractor.services.youtube

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import ac.mdiq.vista.downloader.DownloaderFactory
import ac.mdiq.vista.downloader.DownloaderFactory.getDownloader
import ac.mdiq.vista.extractor.InfoItem
import ac.mdiq.vista.extractor.ListExtractor.InfoItemsPage
import ac.mdiq.vista.extractor.Vista.init
import ac.mdiq.vista.extractor.ServiceList.YouTube
import ac.mdiq.vista.extractor.channel.ChannelExtractor
import ac.mdiq.vista.extractor.channel.tabs.ChannelTabExtractor
import ac.mdiq.vista.extractor.localization.Localization
import ac.mdiq.vista.extractor.services.DefaultTests.defaultTestRelatedItems
import ac.mdiq.vista.extractor.stream.StreamInfoItem
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.stream.Collectors

/**
 * A class that tests multiple channels and ranges of "time ago".
 */
class YoutubeChannelLocalizationTest {
    private val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

    @Test
    @Throws(Exception::class)
    fun testAllSupportedLocalizations() {
        YoutubeTestsUtils.ensureStateless()
        init(getDownloader(RESOURCE_PATH + "localization"))

        testLocalizationsFor("https://www.youtube.com/user/NBCNews")
        testLocalizationsFor("https://www.youtube.com/channel/UCcmpeVbSSQlZRvHfdC-CRwg/videos")
        testLocalizationsFor("https://www.youtube.com/channel/UC65afEgL62PGFWXY7n6CUbA")
        testLocalizationsFor("https://www.youtube.com/channel/UCEOXxzW2vU0P-0THehuIIeg")
    }

    @Throws(Exception::class)
    private fun testLocalizationsFor(channelUrl: String) {
        val supportedLocalizations: List<Localization> = YouTube.supportedLocalizations
        // final List<Localization> supportedLocalizations = Arrays.asList(Localization.DEFAULT, new Localization("sr"));
        val results: MutableMap<Localization, List<StreamInfoItem>> = LinkedHashMap()

        for (currentLocalization in supportedLocalizations) {
            if (DEBUG) println("Testing localization = $currentLocalization")

            var itemsPage: InfoItemsPage<InfoItem>
            try {
                val extractor: ChannelExtractor = YouTube.getChannelExtractor(channelUrl)
                extractor.forceLocalization(currentLocalization)
                extractor.fetchPage()

                // Use Videos tab only
                val tabExtractor: ChannelTabExtractor = YouTube.getChannelTabExtractor(
                    extractor.getTabs()[0])
                tabExtractor.fetchPage()
                itemsPage = defaultTestRelatedItems(tabExtractor)
            } catch (e: Throwable) {
                println("[!] $currentLocalization → failed")
                throw e
            }

            val items = itemsPage.items!!.stream()
                .filter { o: InfoItem? -> StreamInfoItem::class.java.isInstance(o) }
                .map { obj: InfoItem? -> StreamInfoItem::class.java.cast(obj) }
                .collect(Collectors.toUnmodifiableList())
            for (i in items.indices) {
                val item = items[i]

                var debugMessage = ("""[${
                    String.format("%02d",
                        i)
                }] ${currentLocalization.localizationCode} → ${item.name}
:::: ${item.streamType}, views = ${item.viewCount}""")
                val uploadDate = item.uploadDate
                if (uploadDate != null) {
                    val dateAsText = dateTimeFormatter.format(uploadDate.offsetDateTime())
                    debugMessage += """

                        :::: ${item.textualUploadDate}
                        :::: $dateAsText
                        """.trimIndent()
                }
                if (DEBUG) println(debugMessage + "\n")
            }
            results[currentLocalization] = items

            if (DEBUG) println("\n===============================\n")
        }


        // Check results
        val referenceList = results[Localization.DEFAULT]!!
        var someFail = false

        for ((key, currentList) in results) {
            if (key == Localization.DEFAULT) continue

            val currentLocalizationCode = key.localizationCode
            val referenceLocalizationCode = Localization.DEFAULT.localizationCode
            if (DEBUG) {
                println("Comparing " + referenceLocalizationCode + " with " +
                        currentLocalizationCode)
            }

            if (referenceList.size != currentList.size) {
                if (DEBUG) println("[!] $currentLocalizationCode → Lists are not equal")
                someFail = true
                continue
            }

            for (i in 0 until referenceList.size - 1) {
                val referenceItem = referenceList[i]
                val currentItem = currentList[i]

                val referenceUploadDate = referenceItem.uploadDate
                val currentUploadDate = currentItem.uploadDate

                val referenceDateString =
                    if (referenceUploadDate == null) "null" else dateTimeFormatter.format(referenceUploadDate.offsetDateTime())
                val currentDateString =
                    if (currentUploadDate == null) "null" else dateTimeFormatter.format(currentUploadDate.offsetDateTime())

                var difference: Long = -1
                if (referenceUploadDate != null && currentUploadDate != null) {
                    difference = ChronoUnit.MILLIS.between(referenceUploadDate.offsetDateTime(),
                        currentUploadDate.offsetDateTime())
                }

                val areTimeEquals = difference < 5 * 60 * 1000L

                if (!areTimeEquals) {
                    println("""      [!] $currentLocalizationCode → [$i] dates are not equal
          $referenceLocalizationCode: $referenceDateString → ${referenceItem.textualUploadDate}
          $currentLocalizationCode: $currentDateString → ${currentItem.textualUploadDate}""")
                }
            }
        }

        if (someFail) {
            Assertions.fail<Any>("Some localization failed")
        } else {
            if (DEBUG) print("""
    All tests passed

    ===============================


    """.trimIndent())
        }
    }

    companion object {
        private const val RESOURCE_PATH = DownloaderFactory.RESOURCE_PATH + "services/youtube/extractor/channel/"
        private const val DEBUG = false
    }
}
