package ac.mdiq.vista.extractor.services.peertube

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import ac.mdiq.vista.downloader.DownloaderTestImpl.Companion.getInstance
import ac.mdiq.vista.extractor.InfoItem.InfoType
import ac.mdiq.vista.extractor.Vista.init
import ac.mdiq.vista.extractor.ServiceList.PeerTube
import ac.mdiq.vista.extractor.StreamingService
import ac.mdiq.vista.extractor.channel.tabs.ChannelTabExtractor
import ac.mdiq.vista.extractor.channel.tabs.ChannelTabs
import ac.mdiq.vista.extractor.exceptions.ExtractionException
import ac.mdiq.vista.extractor.services.DefaultListExtractorTest
import ac.mdiq.vista.extractor.services.DefaultTests.defaultTestGetPageInNewExtractor
import ac.mdiq.vista.extractor.services.peertube.extractors.PeertubeChannelTabExtractor
import java.io.IOException

internal class PeertubeChannelTabExtractorTest {
    internal class Videos : DefaultListExtractorTest<ChannelTabExtractor?>() {
        @Throws(Exception::class)
        override fun extractor(): ChannelTabExtractor {
            return extractor!!
        }

        @Throws(Exception::class)
        override fun expectedService(): StreamingService {
            return PeerTube
        }

        @Throws(Exception::class)
        override fun expectedName(): String? {
            return ChannelTabs.VIDEOS
        }

        @Throws(Exception::class)
        override fun expectedId(): String? {
            return "video-channels/lqdn_channel@video.lqdn.fr"
        }

        @Throws(Exception::class)
        override fun expectedUrlContains(): String? {
            return "https://framatube.org/video-channels/lqdn_channel@video.lqdn.fr/videos"
        }

        @Throws(Exception::class)
        override fun expectedOriginalUrlContains(): String? {
            return "https://framatube.org/video-channels/lqdn_channel@video.lqdn.fr/videos"
        }

        override fun expectedInfoItemType(): InfoType? {
            return InfoType.STREAM
        }

        override fun expectedHasMoreItems(): Boolean {
            return true
        }

        @Test
        @Throws(Exception::class)
        fun testGetPageInNewExtractor() {
            val newTabExtractor: ChannelTabExtractor = PeerTube.getChannelTabExtractorFromId(
                "video-channels/lqdn_channel@video.lqdn.fr", ChannelTabs.VIDEOS)
            defaultTestGetPageInNewExtractor(extractor!!, newTabExtractor)
        }

        companion object {
            private var extractor: PeertubeChannelTabExtractor? = null


            @BeforeAll
            @Throws(Exception::class)
            fun setUp(): Unit {
                init(getInstance()!!)
                // setting instance might break test when running in parallel
                PeerTube.instance = PeertubeInstance("https://framatube.org", "Framatube")
                extractor = PeerTube.getChannelTabExtractorFromId("video-channels/lqdn_channel@video.lqdn.fr", ChannelTabs.VIDEOS) as? PeertubeChannelTabExtractor
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
            return PeerTube
        }

        @Throws(Exception::class)
        override fun expectedName(): String? {
            return ChannelTabs.PLAYLISTS
        }

        @Throws(Exception::class)
        override fun expectedId(): String? {
            return "video-channels/lqdn_channel@video.lqdn.fr"
        }

        @Throws(Exception::class)
        override fun expectedUrlContains(): String? {
            return "https://framatube.org/video-channels/lqdn_channel@video.lqdn.fr/video-playlists"
        }

        @Throws(Exception::class)
        override fun expectedOriginalUrlContains(): String? {
            return "https://framatube.org/video-channels/lqdn_channel@video.lqdn.fr/video-playlists"
        }

        override fun expectedInfoItemType(): InfoType? {
            return InfoType.PLAYLIST
        }

        override fun expectedHasMoreItems(): Boolean {
            return false
        }

        companion object {
            private var extractor: PeertubeChannelTabExtractor? = null


            @BeforeAll
            @Throws(IOException::class, ExtractionException::class)
            fun setUp(): Unit {
                init(getInstance()!!)
                extractor = PeerTube.getChannelTabExtractorFromIdAndBaseUrl("video-channels/lqdn_channel@video.lqdn.fr", ChannelTabs.PLAYLISTS,
                    "https://framatube.org") as? PeertubeChannelTabExtractor
                extractor!!.fetchPage()
            }
        }
    }

    internal class Channels : DefaultListExtractorTest<ChannelTabExtractor?>() {
        @Throws(Exception::class)
        override fun extractor(): ChannelTabExtractor {
            return extractor!!
        }

        @Throws(Exception::class)
        override fun expectedService(): StreamingService {
            return PeerTube
        }

        @Throws(Exception::class)
        override fun expectedName(): String? {
            return ChannelTabs.CHANNELS
        }

        @Throws(Exception::class)
        override fun expectedId(): String? {
            return "accounts/framasoft"
        }

        @Throws(Exception::class)
        override fun expectedUrlContains(): String? {
            return "https://framatube.org/accounts/framasoft/video-channels"
        }

        @Throws(Exception::class)
        override fun expectedOriginalUrlContains(): String? {
            return "https://framatube.org/accounts/framasoft/video-channels"
        }

        override fun expectedInfoItemType(): InfoType? {
            return InfoType.CHANNEL
        }

        override fun expectedHasMoreItems(): Boolean {
            return true
        }

        companion object {
            private var extractor: PeertubeChannelTabExtractor? = null


            @BeforeAll
            @Throws(IOException::class, ExtractionException::class)
            fun setUp(): Unit {
                init(getInstance()!!)
                extractor = PeerTube.getChannelTabExtractorFromIdAndBaseUrl("accounts/framasoft", ChannelTabs.CHANNELS, "https://framatube.org") as? PeertubeChannelTabExtractor
                extractor!!.fetchPage()
            }
        }
    }
}
