package ac.mdiq.vista.extractor.services.youtube

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import ac.mdiq.vista.downloader.DownloaderTestImpl.Companion.getInstance
import ac.mdiq.vista.extractor.Vista.init
import ac.mdiq.vista.extractor.ServiceList.YouTube
import ac.mdiq.vista.extractor.StreamingService
import ac.mdiq.vista.extractor.kiosk.KioskList
import ac.mdiq.vista.extractor.playlist.PlaylistExtractor
import ac.mdiq.vista.extractor.services.youtube.extractors.YoutubeMixPlaylistExtractor
import ac.mdiq.vista.extractor.services.youtube.extractors.YoutubePlaylistExtractor

/*
* Created by Christian Schabesberger on 29.12.15.
*
* Copyright (C) 2015 Christian Schabesberger <chris.schabesberger@mailbox.org>
* Copyright (C) 2024 Xilin Jia <https://github.com/XilinJia>
* YoutubeSearchExtractorStreamTest.kt is part of Vista Guide.
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
* along with Vista Guide.  If not, see <http://www.gnu.org/licenses/>.
*/

/**
 * Test for [YoutubeService]
 */
class YoutubeServiceTest {
    @Test
    fun testGetKioskAvailableKiosks() {
        Assertions.assertFalse(kioskList!!.availableKiosks.isEmpty(), "No kiosk got returned")
    }

    @Test
    @Throws(Exception::class)
    fun testGetDefaultKiosk() {
        Assertions.assertEquals(kioskList!!.getDefaultKioskExtractor(null)!!.id, "Trending")
    }


    @Throws(Exception::class)
    @Test
    fun playListExtractorIsNormalPlaylist() {
            val extractor = service!!.getPlaylistExtractor("https://www.youtube.com/watch?v=JhqtYOnNrTs&list=PL-EkZZikQIQVqk9rBWzEo5b-2GeozElS")
            Assertions.assertTrue(extractor is YoutubePlaylistExtractor)
        }

    @Throws(Exception::class)
    @Test
    fun playlistExtractorIsMix() {
            val videoId = "_AzeUSL9lZc"
            var extractor: PlaylistExtractor? = YouTube.getPlaylistExtractor("https://www.youtube.com/watch?v=$videoId&list=RD$videoId")
            Assertions.assertTrue(extractor is YoutubeMixPlaylistExtractor)

            extractor = YouTube.getPlaylistExtractor("https://www.youtube.com/watch?v=$videoId&list=RDMM$videoId")
            Assertions.assertTrue(extractor is YoutubeMixPlaylistExtractor)

            val mixVideoId = "qHtzO49SDmk"

            extractor = YouTube.getPlaylistExtractor("https://www.youtube.com/watch?v=$mixVideoId&list=RD$videoId")
            Assertions.assertTrue(extractor is YoutubeMixPlaylistExtractor)
        }

    companion object {
        var service: StreamingService? = null
        var kioskList: KioskList? = null


        @BeforeAll
        @Throws(Exception::class)
        fun setUp() {
            init(getInstance())
            service = YouTube
            kioskList = service!!.getKioskList()
        }
    }
}
