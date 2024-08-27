/*
 * Created by Christian Schabesberger on 18.11.16.
 *
 * Copyright (C) 2016 Christian Schabesberger <chris.schabesberger@mailbox.org>
* Copyright (C) 2024 Xilin Jia <https://github.com/XilinJia>
 * YoutubeSuggestionExtractorTest.kt is part of Vista Guide.
 *
 * Vista Guide is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Vista Guide is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Vista Guide.  If not, see <https://www.gnu.org/licenses/>.
 */
package ac.mdiq.vista.extractor.services.youtube

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import ac.mdiq.vista.downloader.DownloaderFactory
import ac.mdiq.vista.downloader.DownloaderFactory.getDownloader
import ac.mdiq.vista.extractor.Vista.init
import ac.mdiq.vista.extractor.ServiceList.YouTube
import ac.mdiq.vista.extractor.exceptions.ExtractionException
import ac.mdiq.vista.extractor.localization.Localization
import ac.mdiq.vista.extractor.suggestion.SuggestionExtractor
import java.io.IOException

/**
 * Test for [YoutubeSuggestionExtractor]
 */
internal class YoutubeSuggestionExtractorTest {
    @Test
    @Throws(IOException::class, ExtractionException::class)
    fun testIfSuggestions() {
        Assertions.assertFalse(suggestionExtractor!!.suggestionList("hello")!!.isEmpty())
    }

    companion object {
        private const val RESOURCE_PATH = DownloaderFactory.RESOURCE_PATH + "services/youtube/extractor/suggestions/"

        private var suggestionExtractor: SuggestionExtractor? = null


        @BeforeAll
        @Throws(Exception::class)
        fun setUp() {
            YoutubeTestsUtils.ensureStateless()
            init(getDownloader(RESOURCE_PATH), Localization("de", "DE"))
            suggestionExtractor = YouTube.getSuggestionExtractor()
        }
    }
}
