package ac.mdiq.vista.extractor.kiosk

import ac.mdiq.vista.extractor.*
import ac.mdiq.vista.extractor.ListExtractor.InfoItemsPage
import ac.mdiq.vista.extractor.Vista.getServiceByUrl
import ac.mdiq.vista.extractor.exceptions.ExtractionException
import ac.mdiq.vista.extractor.linkhandler.ListLinkHandler
import ac.mdiq.vista.extractor.stream.StreamInfoItem
import ac.mdiq.vista.extractor.utils.ExtractorHelper
import java.io.IOException

/*
* Created by Christian Schabesberger on 12.08.17.
*
* Copyright (C) 2017 Christian Schabesberger <chris.schabesberger@mailbox.org>
* Copyright (C) 2024 Xilin Jia <https://github.com/XilinJia>
* KioskInfo.kt is part of Vista Guide.
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

class KioskInfo private constructor(
        serviceId: Int,
        linkHandler: ListLinkHandler,
        name: String)
    : ListInfo<StreamInfoItem>(serviceId, linkHandler, name) {

    companion object {
        @Throws(IOException::class, ExtractionException::class)
        fun getMoreItems(service: StreamingService, url: String, page: Page?): InfoItemsPage<StreamInfoItem>? {
            val extr = service.getKioskList().getExtractorByUrl(url, page)
            return if (extr == null) null else if (isInstanceOf<InfoItemsPage<StreamInfoItem>>(extr)) (extr.getPage(page) as InfoItemsPage<StreamInfoItem>) else null
        }

        @Throws(IOException::class, ExtractionException::class)
        fun getInfo(url: String?): KioskInfo {
            return getInfo(getServiceByUrl(url!!), url)
        }

        inline fun <reified T> isInstanceOf(obj: Any): Boolean {
            return obj is T
        }


        @Throws(IOException::class, ExtractionException::class)
        fun getInfo(service: StreamingService, url: String): KioskInfo {
            val extractor = service.getKioskList().getExtractorByUrl(url, null) as KioskExtractor<StreamInfoItem>
            extractor.fetchPage()
            return getInfo(extractor)
        }

        /**
         * Get KioskInfo from KioskExtractor
         *
         * @param extractor an extractor where fetchPage() was already got called on.
         */
        @Throws(ExtractionException::class)
        fun getInfo(extractor: KioskExtractor<StreamInfoItem>): KioskInfo {
            val info = KioskInfo(extractor.serviceId, extractor.getLinkHandler(), extractor.getName())

            val itemsPage = ExtractorHelper.getItemsPageOrLogError(info, extractor)
            info.relatedItems = itemsPage.items
            info.nextPage = itemsPage.nextPage

            return info
        }
    }
}
