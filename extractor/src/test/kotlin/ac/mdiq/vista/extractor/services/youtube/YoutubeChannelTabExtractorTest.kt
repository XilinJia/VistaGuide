package ac.mdiq.vista.extractor.services.youtube

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import ac.mdiq.vista.downloader.DownloaderFactory
import ac.mdiq.vista.downloader.DownloaderFactory.getDownloader
import ac.mdiq.vista.extractor.InfoItem.InfoType
import ac.mdiq.vista.extractor.Vista.init
import ac.mdiq.vista.extractor.ServiceList.YouTube
import ac.mdiq.vista.extractor.StreamingService
import ac.mdiq.vista.extractor.channel.ChannelExtractor
import ac.mdiq.vista.extractor.channel.tabs.ChannelTabExtractor
import ac.mdiq.vista.extractor.channel.tabs.ChannelTabs
import ac.mdiq.vista.extractor.exceptions.ExtractionException
import ac.mdiq.vista.extractor.services.DefaultListExtractorTest
import ac.mdiq.vista.extractor.services.youtube.extractors.YoutubeChannelTabExtractor
import java.io.IOException

internal object YoutubeChannelTabExtractorTest {
    private const val RESOURCE_PATH = (DownloaderFactory.RESOURCE_PATH + "services/youtube/extractor/channelTabs/")

    internal class Videos : DefaultListExtractorTest<ChannelTabExtractor?>() {
        @Throws(Exception::class)
        override fun extractor(): ChannelTabExtractor {
            return extractor!!
        }

        @Throws(Exception::class)
        override fun expectedService(): StreamingService {
            return YouTube
        }

        @Throws(Exception::class)
        override fun expectedName(): String? {
            return ChannelTabs.VIDEOS
        }

        @Throws(Exception::class)
        override fun expectedId(): String? {
            return "UCTwECeGqMZee77BjdoYtI2Q"
        }

        @Throws(Exception::class)
        override fun expectedUrlContains(): String? {
            return "https://www.youtube.com/channel/UCTwECeGqMZee77BjdoYtI2Q/videos"
        }

        @Throws(Exception::class)
        override fun expectedOriginalUrlContains(): String? {
            return "https://www.youtube.com/user/creativecommons/videos"
        }

        override fun expectedInfoItemType(): InfoType? {
            return InfoType.STREAM
        }

        override fun expectedHasMoreItems(): Boolean {
            return true
        }

        companion object {
            private var extractor: YoutubeChannelTabExtractor? = null


            @BeforeAll
            @Throws(IOException::class, ExtractionException::class)
            fun setUp(): Unit {
                YoutubeTestsUtils.ensureStateless()
                init(getDownloader(RESOURCE_PATH + "videos"))
                extractor = YouTube.getChannelTabExtractorFromId("user/creativecommons", ChannelTabs.VIDEOS) as? YoutubeChannelTabExtractor
                extractor!!.fetchPage()
            }
        }
    }

    internal class Playlists : DefaultListExtractorTest<ChannelTabExtractor?>() {
        @Throws(Exception::class)
        override fun extractor(): ChannelTabExtractor {
            return extractor!!
        }

        @Throws(Exception::class)
        override fun expectedService(): StreamingService {
            return YouTube
        }

        @Throws(Exception::class)
        override fun expectedName(): String? {
            return ChannelTabs.PLAYLISTS
        }

        @Throws(Exception::class)
        override fun expectedId(): String? {
            return "UC2DjFE7Xf11URZqWBigcVOQ"
        }

        @Throws(Exception::class)
        override fun expectedUrlContains(): String? {
            return "https://www.youtube.com/channel/UC2DjFE7Xf11URZqWBigcVOQ/playlists"
        }

        @Throws(Exception::class)
        override fun expectedOriginalUrlContains(): String? {
            return "https://www.youtube.com/@EEVblog/playlists"
        }

        override fun expectedInfoItemType(): InfoType? {
            return InfoType.PLAYLIST
        }

        override fun expectedHasMoreItems(): Boolean {
            return true
        }

        companion object {
            private var extractor: YoutubeChannelTabExtractor? = null


            @BeforeAll
            @Throws(IOException::class, ExtractionException::class)
            fun setUp(): Unit {
                YoutubeTestsUtils.ensureStateless()
                init(getDownloader(RESOURCE_PATH + "playlists"))
                extractor = YouTube.getChannelTabExtractorFromId("@EEVblog", ChannelTabs.PLAYLISTS) as? YoutubeChannelTabExtractor
                extractor!!.fetchPage()
            }
        }
    }

    internal class Livestreams : DefaultListExtractorTest<ChannelTabExtractor?>() {
        @Throws(Exception::class)
        override fun extractor(): ChannelTabExtractor {
            return extractor!!
        }

        @Throws(Exception::class)
        override fun expectedService(): StreamingService {
            return YouTube
        }

        @Throws(Exception::class)
        override fun expectedName(): String? {
            return ChannelTabs.LIVESTREAMS
        }

        @Throws(Exception::class)
        override fun expectedId(): String? {
            return "UCR-DXc1voovS8nhAvccRZhg"
        }

        @Throws(Exception::class)
        override fun expectedUrlContains(): String? {
            return "https://www.youtube.com/channel/UCR-DXc1voovS8nhAvccRZhg/streams"
        }

        @Throws(Exception::class)
        override fun expectedOriginalUrlContains(): String? {
            return "https://www.youtube.com/c/JeffGeerling/streams"
        }

        override fun expectedInfoItemType(): InfoType? {
            return InfoType.STREAM
        }

        override fun expectedHasMoreItems(): Boolean {
            return true
        }

        companion object {
            private var extractor: YoutubeChannelTabExtractor? = null


            @BeforeAll
            @Throws(IOException::class, ExtractionException::class)
            fun setUp(): Unit {
                YoutubeTestsUtils.ensureStateless()
                init(getDownloader(RESOURCE_PATH + "livestreams"))
                extractor = YouTube.getChannelTabExtractorFromId("c/JeffGeerling", ChannelTabs.LIVESTREAMS) as? YoutubeChannelTabExtractor
                extractor!!.fetchPage()
            }
        }
    }

    internal class Shorts : DefaultListExtractorTest<ChannelTabExtractor?>() {
        @Throws(Exception::class)
        override fun extractor(): ChannelTabExtractor {
            return extractor!!
        }

        @Throws(Exception::class)
        override fun expectedService(): StreamingService {
            return YouTube
        }

        @Throws(Exception::class)
        override fun expectedName(): String? {
            return ChannelTabs.SHORTS
        }

        @Throws(Exception::class)
        override fun expectedId(): String? {
            return "UCh8gHdtzO2tXd593_bjErWg"
        }

        @Throws(Exception::class)
        override fun expectedUrlContains(): String? {
            return "https://www.youtube.com/channel/UCh8gHdtzO2tXd593_bjErWg/shorts"
        }

        @Throws(Exception::class)
        override fun expectedOriginalUrlContains(): String? {
            return "https://www.youtube.com/channel/UCh8gHdtzO2tXd593_bjErWg/shorts"
        }

        override fun expectedInfoItemType(): InfoType? {
            return InfoType.STREAM
        }

        override fun expectedHasMoreItems(): Boolean {
            return true
        }

        companion object {
            private var extractor: YoutubeChannelTabExtractor? = null


            @BeforeAll
            @Throws(IOException::class, ExtractionException::class)
            fun setUp(): Unit {
                YoutubeTestsUtils.ensureStateless()
                init(getDownloader(RESOURCE_PATH + "shorts"))
                extractor = YouTube.getChannelTabExtractorFromId("channel/UCh8gHdtzO2tXd593_bjErWg", ChannelTabs.SHORTS) as? YoutubeChannelTabExtractor
                extractor!!.fetchPage()
            }
        }
    }

    internal class Albums : DefaultListExtractorTest<ChannelTabExtractor?>() {
        @Throws(Exception::class)
        override fun extractor(): ChannelTabExtractor {
            return extractor!!
        }

        @Throws(Exception::class)
        override fun expectedService(): StreamingService {
            return YouTube
        }

        @Throws(Exception::class)
        override fun expectedName(): String? {
            return ChannelTabs.ALBUMS
        }

        @Throws(Exception::class)
        override fun expectedId(): String? {
            return "UCq19-LqvG35A-30oyAiPiqA"
        }

        @Throws(Exception::class)
        override fun expectedUrlContains(): String? {
            return "https://www.youtube.com/channel/UCq19-LqvG35A-30oyAiPiqA/releases"
        }

        @Throws(Exception::class)
        override fun expectedOriginalUrlContains(): String? {
            return "https://www.youtube.com/@Radiohead/releases"
        }

        override fun expectedInfoItemType(): InfoType? {
            return InfoType.PLAYLIST
        }

        override fun expectedHasMoreItems(): Boolean {
            return true
        }

        companion object {
            private var extractor: YoutubeChannelTabExtractor? = null


            @BeforeAll
            @Throws(IOException::class, ExtractionException::class)
            fun setUp(): Unit {
                YoutubeTestsUtils.ensureStateless()
                init(getDownloader(RESOURCE_PATH + "albums"))
                extractor = YouTube.getChannelTabExtractorFromId("@Radiohead", ChannelTabs.ALBUMS) as? YoutubeChannelTabExtractor
                extractor!!.fetchPage()
            }
        }
    }


    // TESTS FOR TABS OF AGE RESTRICTED CHANNELS
    // Fetching the tabs individually would use the standard tabs without fallback to
    // system playlists for stream tabs, we need to fetch the channel extractor to get the
    // channel playlist tabs
    // TODO: implement system playlists fallback in YoutubeChannelTabExtractor for stream
    //  tabs
    internal class AgeRestrictedTabsVideos : DefaultListExtractorTest<ChannelTabExtractor?>() {
        @Throws(Exception::class)
        override fun extractor(): ChannelTabExtractor {
            return extractor!!
        }

        @Throws(Exception::class)
        override fun expectedService(): StreamingService {
            return YouTube
        }

        @Throws(Exception::class)
        override fun expectedName(): String? {
            return ChannelTabs.VIDEOS
        }

        @Throws(Exception::class)
        override fun expectedId(): String? {
            return "UCbfnHqxXs_K3kvaH-WlNlig"
        }

        @Throws(Exception::class)
        override fun expectedUrlContains(): String? {
            return "https://www.youtube.com/channel/UCbfnHqxXs_K3kvaH-WlNlig/videos"
        }

        @Throws(Exception::class)
        override fun expectedOriginalUrlContains(): String? {
            return "https://www.youtube.com/channel/UCbfnHqxXs_K3kvaH-WlNlig/videos"
        }

        override fun expectedInfoItemType(): InfoType? {
            return InfoType.STREAM
        }

        override fun expectedHasMoreItems(): Boolean {
            return true
        }

        companion object {
            private var extractor: ChannelTabExtractor? = null


            @BeforeAll
            @Throws(IOException::class, ExtractionException::class)
            fun setUp(): Unit {
                YoutubeTestsUtils.ensureStateless()
                init(getDownloader(RESOURCE_PATH + "ageRestrictedTabsVideos"))
                val channelExtractor: ChannelExtractor = YouTube.getChannelExtractor(
                    "https://www.youtube.com/channel/UCbfnHqxXs_K3kvaH-WlNlig")
                channelExtractor.fetchPage()

                // the videos tab is the first one
                extractor = YouTube.getChannelTabExtractor(channelExtractor.getTabs().get(0))
                extractor!!.fetchPage()
            }
        }
    }

   @Nested
    internal class AgeRestrictedTabsShorts : DefaultListExtractorTest<ChannelTabExtractor?>() {
        @Throws(Exception::class)
        override fun extractor(): ChannelTabExtractor {
            return extractor!!
        }

        @Throws(Exception::class)
        override fun expectedService(): StreamingService {
            return YouTube
        }

        @Throws(Exception::class)
        override fun expectedName(): String? {
            return ChannelTabs.SHORTS
        }

        @Throws(Exception::class)
        override fun expectedId(): String? {
            return "UCbfnHqxXs_K3kvaH-WlNlig"
        }

        @Throws(Exception::class)
        override fun expectedUrlContains(): String? {
            return "https://www.youtube.com/channel/UCbfnHqxXs_K3kvaH-WlNlig/shorts"
        }

        @Throws(Exception::class)
        override fun expectedOriginalUrlContains(): String? {
            return "https://www.youtube.com/channel/UCbfnHqxXs_K3kvaH-WlNlig/shorts"
        }

        override fun expectedHasMoreItems(): Boolean {
            return false
        }

        @Test
        @Throws(Exception::class)
        override fun testRelatedItems() {
            // this channel has no shorts, so an empty page is returned by the playlist extractor
            assertTrue(extractor!!.initialPage?.items.isNullOrEmpty())
            assertTrue(extractor!!.initialPage!!.errors!!.isEmpty())
        }

        companion object {
            private var extractor: ChannelTabExtractor? = null


            @BeforeAll
            @Throws(IOException::class, ExtractionException::class)
            fun setUp(): Unit {
                YoutubeTestsUtils.ensureStateless()
                init(getDownloader(RESOURCE_PATH + "ageRestrictedTabsShorts"))
                val channelExtractor: ChannelExtractor = YouTube.getChannelExtractor(
                    "https://www.youtube.com/channel/UCbfnHqxXs_K3kvaH-WlNlig")
                channelExtractor.fetchPage()

                // the shorts tab is the second one
                extractor = YouTube.getChannelTabExtractor(channelExtractor.getTabs().get(1))
                extractor!!.fetchPage()
            }
        }
    }
}
