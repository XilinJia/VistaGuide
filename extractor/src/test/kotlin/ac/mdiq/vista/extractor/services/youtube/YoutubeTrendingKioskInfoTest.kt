package ac.mdiq.vista.extractor.services.youtube

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import ac.mdiq.vista.downloader.DownloaderFactory
import ac.mdiq.vista.downloader.DownloaderFactory.getDownloader
import ac.mdiq.vista.extractor.Vista.init
import ac.mdiq.vista.extractor.ServiceList.YouTube
import ac.mdiq.vista.extractor.StreamingService
import ac.mdiq.vista.extractor.kiosk.KioskInfo
import ac.mdiq.vista.extractor.kiosk.KioskInfo.Companion.getInfo
import ac.mdiq.vista.extractor.linkhandler.LinkHandlerFactory
import ac.mdiq.vista.extractor.services.youtube.YoutubeTestsUtils.ensureStateless

/*
* Created by Christian Schabesberger on 12.08.17.
*
* Copyright (C) 2017 Christian Schabesberger <chris.schabesberger@mailbox.org>
* Copyright (C) 2024 Xilin Jia <https://github.com/XilinJia>
* YoutubeTrendingKioskInfoTest.kt is part of Vista Guide.
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
 * Test for [KioskInfo]
 */
class YoutubeTrendingKioskInfoTest {
    @Test
    fun streams() {
            Assertions.assertFalse(kioskInfo!!.relatedItems!!.isEmpty())
        }

    @Test
    fun id() {
            Assertions.assertTrue(kioskInfo!!.id == "Trending" || kioskInfo!!.id == "Trends")
        }

    @Test
    fun name() {
            Assertions.assertFalse(kioskInfo!!.name.isEmpty())
        }

    companion object {
        private const val RESOURCE_PATH = DownloaderFactory.RESOURCE_PATH + "kiosk"

        var kioskInfo: KioskInfo? = null


        @BeforeAll
        @Throws(Exception::class)
        fun setUp() {
            ensureStateless()
            init(getDownloader(RESOURCE_PATH))
            val LinkHandlerFactory: LinkHandlerFactory =
                (YouTube as StreamingService).getKioskList()!!.getListLinkHandlerFactoryByType("Trending")

            kioskInfo = getInfo(YouTube, LinkHandlerFactory.fromId("Trending").url)
        }
    }
}
