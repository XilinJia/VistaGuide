package ac.mdiq.vista.extractor.services

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import ac.mdiq.vista.extractor.ExtractorAsserts
import ac.mdiq.vista.extractor.MetaInfo
import ac.mdiq.vista.extractor.search.SearchExtractor
import java.net.MalformedURLException
import java.util.stream.Collectors

abstract class DefaultSearchExtractorTest : DefaultListExtractorTest<SearchExtractor?>(), BaseSearchExtractorTest {
    abstract fun expectedSearchString(): String?
    abstract fun expectedSearchSuggestion(): String?

    open val isCorrectedSearch: Boolean
        get() = false

    @Throws(MalformedURLException::class)
    open fun expectedMetaInfo(): List<MetaInfo> {
        return emptyList()
    }

    @Test
    @Throws(Exception::class)
    override fun testSearchString() {
        assertEquals(expectedSearchString(), extractor()!!.searchString)
    }

    @Test
    @Throws(Exception::class)
    override fun testSearchSuggestion() {
        val expectedSearchSuggestion = expectedSearchSuggestion()
        if (expectedSearchSuggestion.isNullOrEmpty()) {
            ExtractorAsserts.assertEmpty("Suggestion was expected to be empty", extractor()!!.searchSuggestion)
        } else {
            assertEquals(expectedSearchSuggestion, extractor()!!.searchSuggestion)
        }
    }

    @Test
    @Throws(Exception::class)
    override fun testSearchCorrected() {
        assertEquals(isCorrectedSearch, extractor()!!.isCorrectedSearch)
    }

    /**
     * @see DefaultStreamExtractorTest.testMetaInfo
     */
    @Test
    @Throws(Exception::class)
    open fun testMetaInfo() {
        val metaInfoList = extractor()!!.metaInfo
        val expectedMetaInfoList = expectedMetaInfo()

        for (expectedMetaInfo in expectedMetaInfoList) {
            val texts = metaInfoList!!.stream()
                .map { metaInfo: MetaInfo? -> metaInfo!!.content!!.content }
                .collect(Collectors.toList())
            val titles = metaInfoList.stream().map{it?.title}.collect(Collectors.toList<Any>())
            val urls = metaInfoList.stream().flatMap { info: MetaInfo? -> info!!.getUrls().stream() }
                .collect(Collectors.toList())
            val urlTexts = metaInfoList.stream().flatMap { info: MetaInfo? -> info!!.getUrlTexts().stream() }
                .collect(Collectors.toList())

            Assertions.assertTrue(texts.contains(expectedMetaInfo.content!!.content))
            Assertions.assertTrue(titles.contains(expectedMetaInfo.title))

            for (expectedUrlText in expectedMetaInfo.getUrlTexts()) {
                Assertions.assertTrue(urlTexts.contains(expectedUrlText))
            }
            for (expectedUrl in expectedMetaInfo.getUrls()) {
                Assertions.assertTrue(urls.contains(expectedUrl))
            }
        }
    }
}
