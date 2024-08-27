package ac.mdiq.vista.extractor.services.youtube

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import ac.mdiq.vista.downloader.DownloaderTestImpl.Companion.getInstance
import ac.mdiq.vista.extractor.ExtractorAsserts
import ac.mdiq.vista.extractor.Vista.init
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.services.youtube.YoutubeJavaScriptExtractor.extractJavaScriptPlayerCode
import ac.mdiq.vista.extractor.services.youtube.YoutubeJavaScriptExtractor.extractJavaScriptUrlWithEmbedWatchPage
import ac.mdiq.vista.extractor.services.youtube.YoutubeJavaScriptExtractor.extractJavaScriptUrlWithIframeResource
import java.io.IOException

class YoutubeJavaScriptExtractorTest {
    @BeforeEach
    @Throws(IOException::class)
    fun setup() {
        init(getInstance())
    }

    @Test
    @Throws(ParsingException::class)
    fun testExtractJavaScriptUrlIframe() {
        Assertions.assertTrue(extractJavaScriptUrlWithIframeResource()
            .endsWith("base.js"))
    }

    @Test
    @Throws(ParsingException::class)
    fun testExtractJavaScriptUrlEmbed() {
        Assertions.assertTrue(extractJavaScriptUrlWithEmbedWatchPage("d4IGg5dqeO8")
            .endsWith("base.js"))
    }

    @Test
    @Throws(ParsingException::class)
    fun testExtractJavaScript__success() {
        val playerJsCode = extractJavaScriptPlayerCode("d4IGg5dqeO8")
        assertPlayerJsCode(playerJsCode)
    }

    @Test
    @Throws(ParsingException::class)
    fun testExtractJavaScript__invalidVideoId__success() {
        var playerJsCode = extractJavaScriptPlayerCode("not_a_video_id")
        assertPlayerJsCode(playerJsCode)

        playerJsCode = extractJavaScriptPlayerCode("11-chars123")
        assertPlayerJsCode(playerJsCode)
    }

    private fun assertPlayerJsCode(playerJsCode: String) {
        ExtractorAsserts.assertContains(""" Copyright The Closure Library Authors.
 SPDX-License-Identifier: Apache-2.0""", playerJsCode)
        ExtractorAsserts.assertContains("var _yt_player", playerJsCode)
    }
}
