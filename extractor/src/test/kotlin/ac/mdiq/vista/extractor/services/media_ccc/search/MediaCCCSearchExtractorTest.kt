package ac.mdiq.vista.extractor.services.media_ccc.search

import org.junit.jupiter.api.BeforeAll
import ac.mdiq.vista.downloader.DownloaderTestImpl.Companion.getInstance
import ac.mdiq.vista.extractor.InfoItem.InfoType
import ac.mdiq.vista.extractor.Vista.init
import ac.mdiq.vista.extractor.ServiceList.MediaCCC
import ac.mdiq.vista.extractor.StreamingService
import ac.mdiq.vista.extractor.search.SearchExtractor
import ac.mdiq.vista.extractor.services.DefaultSearchExtractorTest
import ac.mdiq.vista.extractor.services.media_ccc.linkHandler.MediaCCCSearchQueryHandlerFactory

class MediaCCCSearchExtractorTest {
    class All : DefaultSearchExtractorTest() {
        override fun extractor(): SearchExtractor? {
            return extractor
        }

        override fun expectedService(): StreamingService {
            return MediaCCC
        }

        override fun expectedName(): String {
            return QUERY
        }

        override fun expectedId(): String {
            return QUERY
        }

        override fun expectedUrlContains(): String {
            return "media.ccc.de/public/events/search?q=" + QUERY
        }

        override fun expectedOriginalUrlContains(): String {
            return "media.ccc.de/public/events/search?q=" + QUERY
        }

        override fun expectedSearchString(): String {
            return QUERY
        }

        override fun expectedSearchSuggestion(): String? {
            return null
        }

        override fun expectedHasMoreItems(): Boolean {
            return false
        }

        companion object {
            private var extractor: SearchExtractor? = null
            private const val QUERY = "kde"


            @BeforeAll
            @Throws(Exception::class)
            fun setUp(): Unit {
                init(getInstance())
                extractor = MediaCCC.getSearchExtractor(QUERY)
                extractor!!.fetchPage()
            }
        }
    }

    class Conferences : DefaultSearchExtractorTest() {
        override fun extractor(): SearchExtractor? {
            return extractor
        }

        override fun expectedService(): StreamingService {
            return MediaCCC
        }

        override fun expectedName(): String? {
            return QUERY
        }

        override fun expectedId(): String? {
            return QUERY
        }

        override fun expectedUrlContains(): String? {
            return "media.ccc.de/public/events/search?q=" + QUERY
        }

        override fun expectedOriginalUrlContains(): String? {
            return "media.ccc.de/public/events/search?q=" + QUERY
        }

        override fun expectedSearchString(): String? {
            return QUERY
        }

        override fun expectedSearchSuggestion(): String? {
            return null
        }

        override fun expectedInfoItemType(): InfoType? {
            return InfoType.CHANNEL
        }

        override fun expectedHasMoreItems(): Boolean {
            return false
        }

        companion object {
            private var extractor: SearchExtractor? = null
            private const val QUERY = "c3"


            @BeforeAll
            @Throws(Exception::class)
            fun setUp(): Unit {
                init(getInstance())
                extractor = MediaCCC.getSearchExtractor(QUERY,
                    listOf<String>(MediaCCCSearchQueryHandlerFactory.CONFERENCES),
                    "")
                extractor!!.fetchPage()
            }
        }
    }

    class Events : DefaultSearchExtractorTest() {
        override fun extractor(): SearchExtractor? {
            return extractor
        }

        override fun expectedService(): StreamingService {
            return MediaCCC
        }

        override fun expectedName(): String? {
            return QUERY
        }

        override fun expectedId(): String? {
            return QUERY
        }

        override fun expectedUrlContains(): String? {
            return "media.ccc.de/public/events/search?q=" + QUERY
        }

        override fun expectedOriginalUrlContains(): String? {
            return "media.ccc.de/public/events/search?q=" + QUERY
        }

        override fun expectedSearchString(): String? {
            return QUERY
        }

        override fun expectedSearchSuggestion(): String? {
            return null
        }

        override fun expectedInfoItemType(): InfoType? {
            return InfoType.STREAM
        }

        override fun expectedHasMoreItems(): Boolean {
            return false
        }

        companion object {
            private var extractor: SearchExtractor? = null
            private const val QUERY = "linux"


            @BeforeAll
            @Throws(Exception::class)
            fun setUp(): Unit {
                init(getInstance())
                extractor = MediaCCC.getSearchExtractor(QUERY, listOf<String>(MediaCCCSearchQueryHandlerFactory.EVENTS), "")
                extractor!!.fetchPage()
            }
        }
    }
}
