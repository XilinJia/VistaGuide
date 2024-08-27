package ac.mdiq.vista.extractor.services.soundcloud

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import ac.mdiq.vista.downloader.DownloaderTestImpl.Companion.getInstance
import ac.mdiq.vista.extractor.InfoItem.InfoType
import ac.mdiq.vista.extractor.Vista.init
import ac.mdiq.vista.extractor.ServiceList.SoundCloud
import ac.mdiq.vista.extractor.StreamingService
import ac.mdiq.vista.extractor.channel.tabs.ChannelTabExtractor
import ac.mdiq.vista.extractor.channel.tabs.ChannelTabs
import ac.mdiq.vista.extractor.exceptions.ExtractionException
import ac.mdiq.vista.extractor.services.DefaultListExtractorTest
import ac.mdiq.vista.extractor.services.DefaultTests.defaultTestGetPageInNewExtractor
import ac.mdiq.vista.extractor.services.soundcloud.extractors.SoundcloudChannelTabExtractor
import java.io.IOException

internal class SoundcloudChannelTabExtractorTest {
    internal class Tracks : DefaultListExtractorTest<ChannelTabExtractor?>() {
        @Throws(Exception::class)
        override fun extractor(): ChannelTabExtractor {
            return extractor!!
        }

        @Throws(Exception::class)
        override fun expectedService(): StreamingService {
            return SoundCloud
        }

        @Throws(Exception::class)
        override fun expectedName(): String? {
            return ChannelTabs.TRACKS
        }

        @Throws(Exception::class)
        override fun expectedId(): String? {
            return "10494998"
        }

        @Throws(Exception::class)
        override fun expectedUrlContains(): String? {
            return "https://soundcloud.com/liluzivert/tracks"
        }

        @Throws(Exception::class)
        override fun expectedOriginalUrlContains(): String? {
            return "https://soundcloud.com/liluzivert/tracks"
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
            val newTabExtractor: ChannelTabExtractor =
                SoundCloud.getChannelTabExtractorFromId("10494998", ChannelTabs.TRACKS)
            defaultTestGetPageInNewExtractor(extractor!!, newTabExtractor)
        }

        companion object {
            private var extractor: SoundcloudChannelTabExtractor? = null


            @BeforeAll
            @Throws(IOException::class, ExtractionException::class)
            fun setUp(): Unit {
                init(getInstance()!!)
                extractor = SoundCloud.getChannelTabExtractorFromId("10494998", ChannelTabs.TRACKS) as? SoundcloudChannelTabExtractor
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
            return SoundCloud
        }

        @Throws(Exception::class)
        override fun expectedName(): String? {
            return ChannelTabs.PLAYLISTS
        }

        @Throws(Exception::class)
        override fun expectedId(): String? {
            return "323371733"
        }

        @Throws(Exception::class)
        override fun expectedUrlContains(): String? {
            return "https://soundcloud.com/trackaholic/sets"
        }

        @Throws(Exception::class)
        override fun expectedOriginalUrlContains(): String? {
            return "https://soundcloud.com/trackaholic/sets"
        }

        override fun expectedInfoItemType(): InfoType? {
            return InfoType.PLAYLIST
        }

        override fun expectedHasMoreItems(): Boolean {
            return true
        }

        companion object {
            private var extractor: SoundcloudChannelTabExtractor? = null


            @BeforeAll
            @Throws(IOException::class, ExtractionException::class)
            fun setUp(): Unit {
                init(getInstance()!!)
                extractor = SoundCloud.getChannelTabExtractorFromId("323371733", ChannelTabs.PLAYLISTS) as? SoundcloudChannelTabExtractor
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
            return SoundCloud
        }

        @Throws(Exception::class)
        override fun expectedName(): String? {
            return ChannelTabs.ALBUMS
        }

        @Throws(Exception::class)
        override fun expectedId(): String? {
            return "4803918"
        }

        @Throws(Exception::class)
        override fun expectedUrlContains(): String? {
            return "https://soundcloud.com/bigsean-1/albums"
        }

        @Throws(Exception::class)
        override fun expectedOriginalUrlContains(): String? {
            return "https://soundcloud.com/bigsean-1/albums"
        }

        override fun expectedInfoItemType(): InfoType? {
            return InfoType.PLAYLIST
        }

        override fun expectedHasMoreItems(): Boolean {
            return true
        }

        companion object {
            private var extractor: SoundcloudChannelTabExtractor? = null


            @BeforeAll
            @Throws(IOException::class, ExtractionException::class)
            fun setUp(): Unit {
                init(getInstance()!!)
                extractor = SoundCloud.getChannelTabExtractorFromId("4803918", ChannelTabs.ALBUMS) as? SoundcloudChannelTabExtractor
                extractor!!.fetchPage()
            }
        }
    }
}
