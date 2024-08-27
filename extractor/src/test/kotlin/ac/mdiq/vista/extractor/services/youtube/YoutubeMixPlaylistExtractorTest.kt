package ac.mdiq.vista.extractor.services.youtube

import com.grack.nanojson.JsonWriter
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import ac.mdiq.vista.downloader.DownloaderFactory
import ac.mdiq.vista.downloader.DownloaderFactory.getDownloader
import ac.mdiq.vista.extractor.*
import ac.mdiq.vista.extractor.ListExtractor.InfoItemsPage
import ac.mdiq.vista.extractor.Vista.getPreferredContentCountry
import ac.mdiq.vista.extractor.Vista.getPreferredLocalization
import ac.mdiq.vista.extractor.Vista.init
import ac.mdiq.vista.extractor.ServiceList.YouTube
import ac.mdiq.vista.extractor.exceptions.ExtractionException
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.playlist.PlaylistInfo
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.DISABLE_PRETTY_PRINT_PARAMETER
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.YOUTUBEI_V1_URL
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.isConsentAccepted
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.prepareDesktopJsonBuilder
import ac.mdiq.vista.extractor.services.youtube.extractors.YoutubeMixPlaylistExtractor
import ac.mdiq.vista.extractor.stream.StreamInfoItem
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.util.function.Consumer

object YoutubeMixPlaylistExtractorTest {
    private const val RESOURCE_PATH = DownloaderFactory.RESOURCE_PATH + "services/youtube/extractor/mix/"
    private val dummyCookie: Map<String, String> = java.util.Map.of(YoutubeMixPlaylistExtractor.COOKIE_NAME, "whatever")
    private var extractor: YoutubeMixPlaylistExtractor? = null

    class Mix {
        @Test
        fun serviceId() {
                Assertions.assertEquals(YouTube.serviceId, extractor!!.serviceId)
            }

        @Throws(Exception::class)
        @Test
        fun name() {
                val name = extractor!!.getName()
                ExtractorAsserts.assertContains("Mix", name)
                ExtractorAsserts.assertContains(VIDEO_TITLE, name)
            }

        @Throws(Exception::class)
        @Test
        fun thumbnails() {
                YoutubeTestsUtils.testImages(extractor!!.thumbnails)
                extractor!!.thumbnails.forEach(Consumer { thumbnail: Image ->
                    ExtractorAsserts.assertContains(
                        VIDEO_ID, thumbnail.url)
                })
            }

        @Throws(Exception::class)
        @Test
        fun initialPage() {
                val streams = extractor!!.initialPage
                Assertions.assertFalse(streams.items!!.isEmpty())
                Assertions.assertTrue(streams.hasNextPage())
            }

        @Throws(Exception::class)
        @Test
        fun page() {
                val body = JsonWriter.string(prepareDesktopJsonBuilder(
                    getPreferredLocalization(), getPreferredContentCountry())
                    .value("videoId", VIDEO_ID)
                    .value("playlistId", "RD$VIDEO_ID")
                    .value("params", "OAE%3D")
                    .done())
                    .toByteArray(StandardCharsets.UTF_8)

                val streams =
                    extractor!!.getPage(Page(
                        YOUTUBEI_V1_URL + "next?" + DISABLE_PRETTY_PRINT_PARAMETER,
                        null, null, dummyCookie, body))
                Assertions.assertFalse(streams.items!!.isEmpty())
                Assertions.assertTrue(streams.hasNextPage())
            }

        @Throws(Exception::class)
        @Test
        fun continuations() {
                var streams: InfoItemsPage<StreamInfoItem>? = extractor!!.initialPage
                val urls: MutableSet<String> = HashSet()

                // Should work infinitely, but for testing purposes only 3 times
                for (i in 0..2) {
                    Assertions.assertTrue(streams!!.hasNextPage())
                    Assertions.assertFalse(streams.items!!.isEmpty())

                    for (item in streams.items!!) {
                        // TODO Duplicates are appearing
                        // assertFalse(urls.contains(item.getUrl()));
                        urls.add(item.url)
                    }

                    streams = extractor!!.getPage(streams.nextPage)
                }
                Assertions.assertTrue(streams!!.hasNextPage())
                Assertions.assertFalse(streams.items!!.isEmpty())
            }

        @Test
        fun streamCount() {
                Assertions.assertEquals(ListExtractor.ITEM_COUNT_INFINITE,
                    extractor!!.streamCount)
            }

        @Throws(ParsingException::class)
        @Test
        fun playlistType() {
                Assertions.assertEquals(PlaylistInfo.PlaylistType.MIX_STREAM,
                    extractor!!.playlistType)
            }

        companion object {
            private const val VIDEO_ID = "FAqYW76GLPA"
            private const val VIDEO_TITLE = "Mix – "


            @BeforeAll
            @Throws(Exception::class)
            fun setUp() {
                YoutubeTestsUtils.ensureStateless()
                isConsentAccepted = true
                init(getDownloader(RESOURCE_PATH + "mix"))
                extractor = YouTube.getPlaylistExtractor(("https://www.youtube.com/watch?v=$VIDEO_ID&list=RD$VIDEO_ID")) as? YoutubeMixPlaylistExtractor
                extractor!!.fetchPage()
            }
        }
    }

    class MixWithIndex {
        @Throws(Exception::class)
        @Test
        fun name() {
                val name = extractor!!.getName()
                ExtractorAsserts.assertContains("Mix", name)
                ExtractorAsserts.assertContains(VIDEO_TITLE, name)
            }

        @Throws(Exception::class)
        @Test
        fun thumbnails() {
                YoutubeTestsUtils.testImages(extractor!!.thumbnails)
                extractor!!.thumbnails.forEach(Consumer { thumbnail: Image ->
                    ExtractorAsserts.assertContains(
                        VIDEO_ID, thumbnail.url)
                })
            }

        @Throws(Exception::class)
        @Test
        fun initialPage() {
                val streams = extractor!!.initialPage
                Assertions.assertFalse(streams.items!!.isEmpty())
                Assertions.assertTrue(streams.hasNextPage())
            }

        @Throws(Exception::class)
        @Test
        fun page() {
                val body = JsonWriter.string(prepareDesktopJsonBuilder(
                    getPreferredLocalization(), getPreferredContentCountry())
                    .value("videoId", VIDEO_ID)
                    .value("playlistId", "RD$VIDEO_ID")
                    .value("playlistIndex", INDEX)
                    .value("params", "OAE%3D")
                    .done())
                    .toByteArray(StandardCharsets.UTF_8)

                val streams =
                    extractor!!.getPage(Page(
                        YOUTUBEI_V1_URL + "next?" + DISABLE_PRETTY_PRINT_PARAMETER,
                        null, null, dummyCookie, body))
                Assertions.assertFalse(streams.items!!.isEmpty())
                Assertions.assertTrue(streams.hasNextPage())
            }

        @Throws(Exception::class)
        @Test
        fun continuations() {
                var streams: InfoItemsPage<StreamInfoItem>? = extractor!!.initialPage
                val urls: MutableSet<String> = HashSet()

                // Should work infinitely, but for testing purposes only 3 times
                for (i in 0..2) {
                    Assertions.assertTrue(streams!!.hasNextPage())
                    Assertions.assertFalse(streams.items!!.isEmpty())
                    for (item in streams.items!!) {
                        // TODO Duplicates are appearing
                        // assertFalse(urls.contains(item.getUrl()));
                        urls.add(item.url)
                    }

                    streams = extractor!!.getPage(streams.nextPage)
                }
                Assertions.assertTrue(streams!!.hasNextPage())
                Assertions.assertFalse(streams.items!!.isEmpty())
            }

        @Test
        fun streamCount() {
                Assertions.assertEquals(ListExtractor.ITEM_COUNT_INFINITE,
                    extractor!!.streamCount)
            }

        @Throws(ParsingException::class)
        @Test
        fun playlistType() {
                Assertions.assertEquals(PlaylistInfo.PlaylistType.MIX_STREAM,
                    extractor!!.playlistType)
            }

        companion object {
            private const val VIDEO_ID = "FAqYW76GLPA"
            private const val VIDEO_TITLE = "Mix – "
            private const val INDEX = 7 // YT starts the index with 1...
            private const val VIDEO_ID_AT_INDEX = "F90Cw4l-8NY"


            @BeforeAll
            @Throws(Exception::class)
            fun setUp() {
                YoutubeTestsUtils.ensureStateless()
                isConsentAccepted = true
                init(getDownloader(RESOURCE_PATH + "mixWithIndex"))
                extractor = YouTube.getPlaylistExtractor(("https://www.youtube.com/watch?v=$VIDEO_ID_AT_INDEX&list=RD$VIDEO_ID&index=$INDEX")) as? YoutubeMixPlaylistExtractor
                extractor!!.fetchPage()
            }
        }
    }

    class MyMix {
        @Test
        fun serviceId() {
                Assertions.assertEquals(YouTube.serviceId, extractor!!.serviceId)
            }

        @Throws(Exception::class)
        @Test
        fun name() {
                val name = extractor!!.getName()
                Assertions.assertEquals("My Mix", name)
            }

        @Throws(Exception::class)
        @Test
        fun thumbnails() {
                YoutubeTestsUtils.testImages(extractor!!.thumbnails)
                extractor!!.thumbnails.forEach(Consumer { thumbnail: Image ->
                    ExtractorAsserts.assertContains(
                        VIDEO_ID, thumbnail.url)
                })
            }

        @Throws(Exception::class)
        @Test
        fun initialPage() {
                val streams = extractor!!.initialPage
                Assertions.assertFalse(streams.items!!.isEmpty())
                Assertions.assertTrue(streams.hasNextPage())
            }

        @Throws(Exception::class)
        @Test
        fun page() {
                val body = JsonWriter.string(prepareDesktopJsonBuilder(
                    getPreferredLocalization(), getPreferredContentCountry())
                    .value("videoId", VIDEO_ID)
                    .value("playlistId", "RDMM$VIDEO_ID")
                    .value("params", "OAE%3D")
                    .done())
                    .toByteArray(StandardCharsets.UTF_8)

                val streams =
                    extractor!!.getPage(Page(
                        YOUTUBEI_V1_URL + "next?" + DISABLE_PRETTY_PRINT_PARAMETER,
                        null, null, dummyCookie, body))
                Assertions.assertFalse(streams.items!!.isEmpty())
                Assertions.assertTrue(streams.hasNextPage())
            }

        @Throws(Exception::class)
        @Test
        fun continuations() {
                var streams: InfoItemsPage<StreamInfoItem>? = extractor!!.initialPage
                val urls: MutableSet<String> = HashSet()

                // Should work infinitely, but for testing purposes only 3 times
                for (i in 0..2) {
                    Assertions.assertTrue(streams!!.hasNextPage())
                    Assertions.assertFalse(streams.items!!.isEmpty())

                    for (item in streams.items!!) {
                        // TODO Duplicates are appearing
                        // assertFalse(urls.contains(item.getUrl()));
                        urls.add(item.url)
                    }

                    streams = extractor!!.getPage(streams.nextPage)
                }
                Assertions.assertTrue(streams!!.hasNextPage())
                Assertions.assertFalse(streams.items!!.isEmpty())
            }

        @Test
        fun streamCount() {
                Assertions.assertEquals(ListExtractor.ITEM_COUNT_INFINITE,
                    extractor!!.streamCount)
            }

        @Throws(ParsingException::class)
        @Test
        fun playlistType() {
                Assertions.assertEquals(PlaylistInfo.PlaylistType.MIX_STREAM,
                    extractor!!.playlistType)
            }

        companion object {
            private const val VIDEO_ID = "YVkUvmDQ3HY"


            @BeforeAll
            @Throws(Exception::class)
            fun setUp() {
                YoutubeTestsUtils.ensureStateless()
                isConsentAccepted = true
                init(getDownloader(RESOURCE_PATH + "myMix"))
                extractor = YouTube.getPlaylistExtractor(("https://www.youtube.com/watch?v=$VIDEO_ID&list=RDMM$VIDEO_ID")) as? YoutubeMixPlaylistExtractor
                extractor!!.fetchPage()
            }
        }
    }

    class Invalid {
        @Throws(Exception::class)
        @Test
        fun pageEmptyUrl() {
                extractor = YouTube.getPlaylistExtractor(("https://www.youtube.com/watch?v=$VIDEO_ID&list=RD$VIDEO_ID")) as? YoutubeMixPlaylistExtractor

                extractor!!.fetchPage()
                Assertions.assertThrows(IllegalArgumentException::class.java
                ) {
                    extractor!!.getPage(Page(""))
                }
            }

        @Test
        @Throws(Exception::class)
        fun invalidVideoId() {
            extractor = YouTube.getPlaylistExtractor(("https://www.youtube.com/watch?v=" + "abcde" + "&list=RD" + "abcde")) as? YoutubeMixPlaylistExtractor

            Assertions.assertThrows(ExtractionException::class.java
            ) { extractor!!.fetchPage() }
        }

        companion object {
            private const val VIDEO_ID = "QMVCAPd5cwBcg"


            @BeforeAll
            @Throws(IOException::class)
            fun setUp() {
                YoutubeTestsUtils.ensureStateless()
                isConsentAccepted = true
                init(getDownloader(RESOURCE_PATH + "invalid"))
            }
        }
    }

    class ChannelMix {
        @Throws(Exception::class)
        @Test
        fun name() {
                val name = extractor!!.getName()
                ExtractorAsserts.assertContains("Mix", name)
                ExtractorAsserts.assertContains(CHANNEL_TITLE, name)
            }

        @Throws(Exception::class)
        @Test
        fun thumbnails() {
                YoutubeTestsUtils.testImages(extractor!!.thumbnails)
                extractor!!.thumbnails.forEach(Consumer { thumbnail: Image ->
                    ExtractorAsserts.assertContains(
                        VIDEO_ID_OF_CHANNEL, thumbnail.url)
                })
            }

        @Throws(Exception::class)
        @Test
        fun initialPage() {
                val streams = extractor!!.initialPage
                Assertions.assertFalse(streams.items!!.isEmpty())
                Assertions.assertTrue(streams.hasNextPage())
            }

        @Throws(Exception::class)
        @Test
        fun page() {
                val body = JsonWriter.string(prepareDesktopJsonBuilder(
                    getPreferredLocalization(), getPreferredContentCountry())
                    .value("videoId", VIDEO_ID_OF_CHANNEL)
                    .value("playlistId", "RDCM$CHANNEL_ID")
                    .value("params", "OAE%3D")
                    .done())
                    .toByteArray(StandardCharsets.UTF_8)

                val streams =
                    extractor!!.getPage(Page(
                        YOUTUBEI_V1_URL + "next?" + DISABLE_PRETTY_PRINT_PARAMETER,
                        null, null, dummyCookie, body))
                Assertions.assertFalse(streams.items!!.isEmpty())
                Assertions.assertTrue(streams.hasNextPage())
            }

        @Test
        fun streamCount() {
                Assertions.assertEquals(ListExtractor.ITEM_COUNT_INFINITE,
                    extractor!!.streamCount)
            }

        @Throws(ParsingException::class)
        @Test
        fun playlistType() {
                Assertions.assertEquals(PlaylistInfo.PlaylistType.MIX_CHANNEL,
                    extractor!!.playlistType)
            }

        companion object {
            private const val CHANNEL_ID = "UCXuqSBlHAE6Xw-yeJA0Tunw"
            private const val VIDEO_ID_OF_CHANNEL = "mnk6gnOBYIo"
            private const val CHANNEL_TITLE = "Linus Tech Tips"



            @BeforeAll
            @Throws(Exception::class)
            fun setUp() {
                YoutubeTestsUtils.ensureStateless()
                isConsentAccepted = true
                init(getDownloader(RESOURCE_PATH + "channelMix"))
                extractor = YouTube.getPlaylistExtractor(("https://www.youtube.com/watch?v=$VIDEO_ID_OF_CHANNEL&list=RDCM$CHANNEL_ID")) as? YoutubeMixPlaylistExtractor
                extractor!!.fetchPage()
            }
        }
    }

    class GenreMix {
        @Test
        fun serviceId() {
                Assertions.assertEquals(YouTube.serviceId, extractor!!.serviceId)
            }

        @Throws(Exception::class)
        @Test
        fun name() {
                Assertions.assertEquals(MIX_TITLE,
                    extractor!!.getName())
            }

        @Throws(Exception::class)
        @Test
        fun thumbnails() {
                YoutubeTestsUtils.testImages(extractor!!.thumbnails)
                extractor!!.thumbnails.forEach(Consumer { thumbnail: Image ->
                    ExtractorAsserts.assertContains(
                        VIDEO_ID, thumbnail.url)
                })
            }

        @Throws(Exception::class)
        @Test
        fun initialPage() {
                val streams = extractor!!.initialPage
                Assertions.assertFalse(streams.items!!.isEmpty())
                Assertions.assertTrue(streams.hasNextPage())
            }

        @Throws(Exception::class)
        @Test
        fun page() {
                val body = JsonWriter.string(prepareDesktopJsonBuilder(
                    getPreferredLocalization(), getPreferredContentCountry())
                    .value("videoId", VIDEO_ID)
                    .value("playlistId", "RD$VIDEO_ID")
                    .value("params", "OAE%3D")
                    .done())
                    .toByteArray(StandardCharsets.UTF_8)

                val streams =
                    extractor!!.getPage(Page(
                        YOUTUBEI_V1_URL + "next?" + DISABLE_PRETTY_PRINT_PARAMETER,
                        null, null, dummyCookie, body))
                Assertions.assertFalse(streams.items!!.isEmpty())
                Assertions.assertTrue(streams.hasNextPage())
            }

        @Throws(Exception::class)
        @Test
        fun continuations() {
                var streams: InfoItemsPage<StreamInfoItem>? = extractor!!.initialPage
                val urls: MutableSet<String> = HashSet()

                // Should work infinitely, but for testing purposes only 3 times
                for (i in 0..2) {
                    Assertions.assertTrue(streams!!.hasNextPage())
                    Assertions.assertFalse(streams.items!!.isEmpty())

                    for (item in streams.items!!) {
                        // TODO Duplicates are appearing
                        // assertFalse(urls.contains(item.getUrl()));
                        urls.add(item.url)
                    }

                    streams = extractor!!.getPage(streams.nextPage)
                }
                Assertions.assertTrue(streams!!.hasNextPage())
                Assertions.assertFalse(streams.items!!.isEmpty())
            }

        @Test
        fun streamCount() {
                Assertions.assertEquals(ListExtractor.ITEM_COUNT_INFINITE,
                    extractor!!.streamCount)
            }

        @Throws(ParsingException::class)
        @Test
        fun playlistType() {
                Assertions.assertEquals(PlaylistInfo.PlaylistType.MIX_GENRE,
                    extractor!!.playlistType)
            }

        companion object {
            private const val VIDEO_ID = "kINJeTNFbpg"
            private const val MIX_TITLE = "Mix – Electronic music"


            @BeforeAll
            @Throws(Exception::class)
            fun setUp() {
                YoutubeTestsUtils.ensureStateless()
                isConsentAccepted = true
                init(getDownloader(RESOURCE_PATH + "genreMix"))
                extractor = YouTube.getPlaylistExtractor(("https://www.youtube.com/watch?v=" + VIDEO_ID
                            + "&list=RDGMEMYH9CUrFO7CfLJpaD7UR85w")) as? YoutubeMixPlaylistExtractor
                extractor!!.fetchPage()
            }
        }
    }

    class Music {
        @Test
        fun serviceId() {
                Assertions.assertEquals(YouTube.serviceId, extractor!!.serviceId)
            }

        @Throws(Exception::class)
        @Test
        fun name() {
                Assertions.assertEquals(MIX_TITLE, extractor!!.getName())
            }

        @Throws(Exception::class)
        @Test
        fun thumbnailUrl() {
                YoutubeTestsUtils.testImages(extractor!!.thumbnails)
                extractor!!.thumbnails.forEach(Consumer { thumbnail: Image ->
                    ExtractorAsserts.assertContains(
                        VIDEO_ID, thumbnail.url)
                })
            }

        @Throws(Exception::class)
        @Test
        fun initialPage() {
                val streams = extractor!!.initialPage
                Assertions.assertFalse(streams.items!!.isEmpty())
                Assertions.assertTrue(streams.hasNextPage())
            }

        @Throws(Exception::class)
        @Test
        fun page() {
                val body = JsonWriter.string(prepareDesktopJsonBuilder(
                    getPreferredLocalization(), getPreferredContentCountry())
                    .value("videoId", VIDEO_ID)
                    .value("playlistId", "RD$VIDEO_ID")
                    .value("params", "OAE%3D")
                    .done())
                    .toByteArray(StandardCharsets.UTF_8)

                val streams =
                    extractor!!.getPage(Page(
                        YOUTUBEI_V1_URL + "next?" + DISABLE_PRETTY_PRINT_PARAMETER,
                        null, null, dummyCookie, body))
                Assertions.assertFalse(streams.items!!.isEmpty())
                Assertions.assertTrue(streams.hasNextPage())
            }

        @Throws(Exception::class)
        @Test
        fun continuations() {
                var streams: InfoItemsPage<StreamInfoItem>? = extractor!!.initialPage
                val urls: MutableSet<String> = HashSet()

                // Should work infinitely, but for testing purposes only 3 times
                for (i in 0..2) {
                    Assertions.assertTrue(streams!!.hasNextPage())
                    Assertions.assertFalse(streams.items!!.isEmpty())

                    for (item in streams.items!!) {
                        // TODO Duplicates are appearing
                        // assertFalse(urls.contains(item.getUrl()));
                        urls.add(item.url)
                    }

                    streams = extractor!!.getPage(streams.nextPage)
                }
                Assertions.assertTrue(streams!!.hasNextPage())
                Assertions.assertFalse(streams.items!!.isEmpty())
            }

        @Test
        fun streamCount() {
                Assertions.assertEquals(ListExtractor.ITEM_COUNT_INFINITE,
                    extractor!!.streamCount)
            }

        @Throws(ParsingException::class)
        @Test
        fun playlistType() {
                Assertions.assertEquals(PlaylistInfo.PlaylistType.MIX_MUSIC,
                    extractor!!.playlistType)
            }

        companion object {
            private const val VIDEO_ID = "dQw4w9WgXcQ"
            private const val MIX_TITLE = "Mix – Rick Astley - Never Gonna Give You Up (Official Music Video)"


            @BeforeAll
            @Throws(Exception::class)
            fun setUp() {
                YoutubeTestsUtils.ensureStateless()
                isConsentAccepted = true
                init(getDownloader(RESOURCE_PATH + "musicMix"))
                extractor = YouTube.getPlaylistExtractor(("https://m.youtube.com/watch?v=$VIDEO_ID&list=RDAMVM$VIDEO_ID")) as? YoutubeMixPlaylistExtractor
                extractor!!.fetchPage()
            }
        }
    }
}
