package ac.mdiq.vista.extractor.services.peertube

import org.junit.jupiter.api.BeforeAll
import ac.mdiq.vista.downloader.DownloaderTestImpl.Companion.getInstance
import ac.mdiq.vista.extractor.InfoItem.InfoType
import ac.mdiq.vista.extractor.Vista.init
import ac.mdiq.vista.extractor.ServiceList.PeerTube
import ac.mdiq.vista.extractor.StreamingService
import ac.mdiq.vista.extractor.channel.tabs.ChannelTabExtractor
import ac.mdiq.vista.extractor.channel.tabs.ChannelTabs
import ac.mdiq.vista.extractor.services.DefaultListExtractorTest
import ac.mdiq.vista.extractor.services.peertube.extractors.PeertubeChannelTabExtractor

internal class PeertubeAccountTabExtractorTest {
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
            return "accounts/framasoft"
        }

        @Throws(Exception::class)
        override fun expectedUrlContains(): String? {
            return "https://framatube.org/accounts/framasoft/videos"
        }

        @Throws(Exception::class)
        override fun expectedOriginalUrlContains(): String? {
            return "https://framatube.org/accounts/framasoft/videos"
        }

        override fun expectedInfoItemType(): InfoType? {
            return InfoType.STREAM
        }

        override fun expectedHasMoreItems(): Boolean {
            return true
        }

        companion object {
            private var extractor: PeertubeChannelTabExtractor? = null


            @BeforeAll
            @Throws(Exception::class)
            fun setUp(): Unit {
                init(getInstance()!!)
                // setting instance might break test when running in parallel
                PeerTube.instance = PeertubeInstance("https://framatube.org", "Framatube")
                extractor = PeerTube.getChannelTabExtractorFromId("accounts/framasoft", ChannelTabs.VIDEOS) as? PeertubeChannelTabExtractor
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
            @Throws(Exception::class)
            fun setUp(): Unit {
                init(getInstance()!!)
                // setting instance might break test when running in parallel
                PeerTube.instance = PeertubeInstance("https://framatube.org", "Framatube")
                extractor = PeerTube.getChannelTabExtractorFromId("accounts/framasoft", ChannelTabs.CHANNELS) as? PeertubeChannelTabExtractor
                extractor!!.fetchPage()
            }
        }
    }
}
