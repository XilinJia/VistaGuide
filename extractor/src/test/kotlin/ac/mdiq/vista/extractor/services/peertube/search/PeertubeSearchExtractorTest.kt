package ac.mdiq.vista.extractor.services.peertube.search

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import ac.mdiq.vista.downloader.DownloaderTestImpl.Companion.getInstance
import ac.mdiq.vista.extractor.Vista.init
import ac.mdiq.vista.extractor.ServiceList.PeerTube
import ac.mdiq.vista.extractor.StreamingService
import ac.mdiq.vista.extractor.search.SearchExtractor
import ac.mdiq.vista.extractor.services.DefaultSearchExtractorTest
import ac.mdiq.vista.extractor.services.DefaultTests.assertNoDuplicatedItems
import ac.mdiq.vista.extractor.services.peertube.PeertubeInstance
import ac.mdiq.vista.extractor.services.peertube.linkHandler.PeertubeSearchQueryHandlerFactory

class PeertubeSearchExtractorTest {
    class All : DefaultSearchExtractorTest() {
        override fun extractor(): SearchExtractor? {
            return extractor
        }

        override fun expectedService(): StreamingService {
            return PeerTube
        }

        override fun expectedName(): String? {
            return QUERY
        }

        override fun expectedId(): String? {
            return QUERY
        }

        override fun expectedUrlContains(): String? {
            return "/search/videos?search=" + QUERY
        }

        override fun expectedOriginalUrlContains(): String? {
            return "/search/videos?search=" + QUERY
        }

        override fun expectedSearchString(): String? {
            return QUERY
        }

        override fun expectedSearchSuggestion(): String? {
            return null
        }

        companion object {
            private var extractor: SearchExtractor? = null
            private const val QUERY = "fsf"


            @BeforeAll
            @Throws(Exception::class)
            fun setUp(): Unit {
                init(getInstance()!!)
                // setting instance might break test when running in parallel
                PeerTube.instance = PeertubeInstance("https://framatube.org", "Framatube")
                extractor = PeerTube.getSearchExtractor(QUERY)
                extractor!!.fetchPage()
            }
        }
    }

    class SepiaSearch : DefaultSearchExtractorTest() {
        override fun extractor(): SearchExtractor? {
            return extractor
        }

        override fun expectedService(): StreamingService {
            return PeerTube
        }

        override fun expectedName(): String? {
            return QUERY
        }

        override fun expectedId(): String? {
            return QUERY
        }

        override fun expectedUrlContains(): String? {
            return "/search/videos?search=" + QUERY
        }

        override fun expectedOriginalUrlContains(): String? {
            return "/search/videos?search=" + QUERY
        }

        override fun expectedSearchString(): String? {
            return QUERY
        }

        override fun expectedSearchSuggestion(): String? {
            return null
        }

        companion object {
            private var extractor: SearchExtractor? = null
            private const val QUERY = "kde"


            @BeforeAll
            @Throws(Exception::class)
            fun setUp(): Unit {
                init(getInstance()!!)
                // setting instance might break test when running in parallel
                PeerTube.instance = PeertubeInstance("https://framatube.org", "Framatube")
                extractor = PeerTube.getSearchExtractor(QUERY,
                    listOf<String>(PeertubeSearchQueryHandlerFactory.SEPIA_VIDEOS),
                    "")
                extractor!!.fetchPage()
            }
        }
    }

    class PagingTest {
        @Test
        @Disabled("Exception in CI: javax.net.ssl.SSLHandshakeException: PKIX path validation failed: java.security.cert.CertPathValidatorException: validity check failed")
        @Throws(
            Exception::class)
        fun duplicatedItemsCheck() {
            init(getInstance()!!)
            val extractor: SearchExtractor = PeerTube.getSearchExtractor("internet", listOf<String>(
                PeertubeSearchQueryHandlerFactory.VIDEOS), "")
            extractor.fetchPage()

            val page1 = extractor.initialPage
            val page2 = extractor.getPage(page1!!.nextPage)

            assertNoDuplicatedItems(PeerTube, page1, page2!!)
        }
    }
}
