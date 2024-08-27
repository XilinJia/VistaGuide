package ac.mdiq.vista.extractor.services.youtube.search

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Disabled
import ac.mdiq.vista.downloader.DownloaderTestImpl.Companion.getInstance
import ac.mdiq.vista.extractor.InfoItem.InfoType
import ac.mdiq.vista.extractor.Vista.init
import ac.mdiq.vista.extractor.ServiceList.YouTube
import ac.mdiq.vista.extractor.StreamingService
import ac.mdiq.vista.extractor.search.SearchExtractor
import ac.mdiq.vista.extractor.services.DefaultSearchExtractorTest
import ac.mdiq.vista.extractor.services.youtube.linkHandler.YoutubeSearchQueryHandlerFactory
import java.net.URLEncoder

// Doesn't work with mocks. Makes request with different `dataToSend` I think
class YoutubeMusicSearchExtractorTest {
    class MusicSongs : DefaultSearchExtractorTest() {
        override fun extractor(): SearchExtractor? {
            return extractor
        }

        override fun expectedService(): StreamingService {
            return YouTube
        }

        override fun expectedName(): String {
            return QUERY
        }

        override fun expectedId(): String {
            return QUERY
        }

        override fun expectedUrlContains(): String {
            return "music.youtube.com/search?q=$QUERY"
        }

        override fun expectedOriginalUrlContains(): String {
            return "music.youtube.com/search?q=$QUERY"
        }

        override fun expectedSearchString(): String {
            return QUERY
        }

        override fun expectedSearchSuggestion(): String? {
            return null
        }

        override fun expectedInfoItemType(): InfoType {
            return InfoType.STREAM
        }

        companion object {
            private var extractor: SearchExtractor? = null
            private const val QUERY = "mocromaniac"


            @BeforeAll
            @Throws(Exception::class)
            fun setUp(): Unit {
                init(getInstance())
                extractor = YouTube.getSearchExtractor(QUERY, listOf(YoutubeSearchQueryHandlerFactory.MUSIC_SONGS), "")
                extractor!!.fetchPage()
            }
        }
    }

    class MusicVideos : DefaultSearchExtractorTest() {
        override fun extractor(): SearchExtractor? {
            return extractor
        }

        override fun expectedService(): StreamingService {
            return YouTube
        }

        override fun expectedName(): String {
            return QUERY
        }

        override fun expectedId(): String {
            return QUERY
        }

        override fun expectedUrlContains(): String {
            return "music.youtube.com/search?q=$QUERY"
        }

        override fun expectedOriginalUrlContains(): String {
            return "music.youtube.com/search?q=$QUERY"
        }

        override fun expectedSearchString(): String {
            return QUERY
        }

        override fun expectedSearchSuggestion(): String? {
            return null
        }

        override fun expectedInfoItemType(): InfoType {
            return InfoType.STREAM
        }

        companion object {
            private var extractor: SearchExtractor? = null
            private const val QUERY = "fresku"


            @BeforeAll
            @Throws(Exception::class)
            fun setUp(): Unit {
                init(getInstance())
                extractor =
                    YouTube.getSearchExtractor(QUERY, listOf(YoutubeSearchQueryHandlerFactory.MUSIC_VIDEOS), "")
                extractor!!.fetchPage()
            }
        }
    }

    class MusicAlbums : DefaultSearchExtractorTest() {
        override fun extractor(): SearchExtractor? {
            return extractor
        }

        override fun expectedService(): StreamingService {
            return YouTube
        }

        override fun expectedName(): String {
            return QUERY
        }

        override fun expectedId(): String {
            return QUERY
        }

        override fun expectedUrlContains(): String {
            return "music.youtube.com/search?q=" + URLEncoder.encode(QUERY)
        }

        override fun expectedOriginalUrlContains(): String {
            return "music.youtube.com/search?q=" + URLEncoder.encode(QUERY)
        }

        override fun expectedSearchString(): String {
            return QUERY
        }

        override fun expectedSearchSuggestion(): String? {
            return null
        }

        override fun expectedInfoItemType(): InfoType {
            return InfoType.PLAYLIST
        }

        companion object {
            private var extractor: SearchExtractor? = null
            private const val QUERY = "johnny sellah"


            @BeforeAll
            @Throws(Exception::class)
            fun setUp(): Unit {
                init(getInstance())
                extractor =
                    YouTube.getSearchExtractor(QUERY, listOf(YoutubeSearchQueryHandlerFactory.MUSIC_ALBUMS), "")
                extractor!!.fetchPage()
            }
        }
    }

    class MusicPlaylists : DefaultSearchExtractorTest() {
        override fun extractor(): SearchExtractor? {
            return extractor
        }

        override fun expectedService(): StreamingService {
            return YouTube
        }

        override fun expectedName(): String {
            return QUERY
        }

        override fun expectedId(): String {
            return QUERY
        }

        override fun expectedUrlContains(): String {
            return "music.youtube.com/search?q=$QUERY"
        }

        override fun expectedOriginalUrlContains(): String {
            return "music.youtube.com/search?q=$QUERY"
        }

        override fun expectedSearchString(): String {
            return QUERY
        }

        override fun expectedSearchSuggestion(): String? {
            return null
        }

        override fun expectedInfoItemType(): InfoType {
            return InfoType.PLAYLIST
        }

        companion object {
            private var extractor: SearchExtractor? = null
            private const val QUERY = "louivos"


            @BeforeAll
            @Throws(Exception::class)
            fun setUp(): Unit {
                init(getInstance())
                extractor = YouTube.getSearchExtractor(QUERY,
                    listOf(YoutubeSearchQueryHandlerFactory.MUSIC_PLAYLISTS),
                    "")
                extractor!!.fetchPage()
            }
        }
    }

    @Disabled
    class MusicArtists : DefaultSearchExtractorTest() {
        override fun extractor(): SearchExtractor? {
            return extractor
        }

        override fun expectedService(): StreamingService {
            return YouTube
        }

        override fun expectedName(): String {
            return QUERY
        }

        override fun expectedId(): String {
            return QUERY
        }

        override fun expectedUrlContains(): String {
            return "music.youtube.com/search?q=$QUERY"
        }

        override fun expectedOriginalUrlContains(): String {
            return "music.youtube.com/search?q=$QUERY"
        }

        override fun expectedSearchString(): String {
            return QUERY
        }

        override fun expectedSearchSuggestion(): String? {
            return null
        }

        override fun expectedInfoItemType(): InfoType {
            return InfoType.CHANNEL
        }

        companion object {
            private var extractor: SearchExtractor? = null
            private const val QUERY = "kevin"


            @BeforeAll
            @Throws(Exception::class)
            fun setUp(): Unit {
                init(getInstance())
                extractor = YouTube.getSearchExtractor(QUERY,
                    listOf(YoutubeSearchQueryHandlerFactory.MUSIC_ARTISTS),
                    "")
                extractor!!.fetchPage()
            }
        }
    }

    @Disabled("Currently constantly switching between \"Did you mean\" and \"Showing results for ...\" occurs")
    class Suggestion : DefaultSearchExtractorTest() {
        override fun extractor(): SearchExtractor? {
            return extractor
        }

        override fun expectedService(): StreamingService {
            return YouTube
        }

        override fun expectedName(): String {
            return QUERY
        }

        override fun expectedId(): String {
            return QUERY
        }

        override fun expectedUrlContains(): String {
            return "music.youtube.com/search?q=" + URLEncoder.encode(QUERY)
        }

        override fun expectedOriginalUrlContains(): String {
            return "music.youtube.com/search?q=" + URLEncoder.encode(QUERY)
        }

        override fun expectedSearchString(): String {
            return QUERY
        }

        override fun expectedSearchSuggestion(): String {
            return "mega man x3"
        }

        override fun expectedInfoItemType(): InfoType {
            return InfoType.STREAM
        }

        companion object {
            private var extractor: SearchExtractor? = null
            private const val QUERY = "megaman x3"
            const val isCorrectedSearch: Boolean = true


            @BeforeAll
            @Throws(Exception::class)
            fun setUp(): Unit {
                init(getInstance())
                extractor =
                    YouTube.getSearchExtractor(QUERY, listOf(YoutubeSearchQueryHandlerFactory.MUSIC_SONGS), "")
                extractor!!.fetchPage()
            }
        }
    }

    class CorrectedSearch : DefaultSearchExtractorTest() {
        override fun extractor(): SearchExtractor? {
            return extractor
        }

        override fun expectedService(): StreamingService {
            return YouTube
        }

        override fun expectedName(): String {
            return QUERY
        }

        override fun expectedId(): String {
            return QUERY
        }

        override fun expectedUrlContains(): String {
            return "music.youtube.com/search?q=" + URLEncoder.encode(QUERY)
        }

        override fun expectedOriginalUrlContains(): String {
            return "music.youtube.com/search?q=" + URLEncoder.encode(QUERY)
        }

        override fun expectedSearchString(): String {
            return QUERY
        }

        override fun expectedSearchSuggestion(): String {
            return EXPECTED_SUGGESTION
        }

        override fun expectedInfoItemType(): InfoType {
            return InfoType.STREAM
        }

        override val isCorrectedSearch: Boolean
            get() = true

        companion object {
            private var extractor: SearchExtractor? = null
            private const val QUERY = "no copyrigh sounds"
            private const val EXPECTED_SUGGESTION = "no copyright sounds"


            @BeforeAll
            @Throws(Exception::class)
            fun setUp(): Unit {
                init(getInstance())
                extractor =
                    YouTube.getSearchExtractor(QUERY, listOf(YoutubeSearchQueryHandlerFactory.MUSIC_SONGS), "")
                extractor!!.fetchPage()
            }
        }
    }
}
