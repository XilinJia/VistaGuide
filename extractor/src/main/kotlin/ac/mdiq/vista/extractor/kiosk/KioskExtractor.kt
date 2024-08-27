package ac.mdiq.vista.extractor.kiosk

import ac.mdiq.vista.extractor.InfoItem
import ac.mdiq.vista.extractor.ListExtractor
import ac.mdiq.vista.extractor.StreamingService
import ac.mdiq.vista.extractor.linkhandler.ListLinkHandler


/*
* Created by Christian Schabesberger on 12.08.17.
*
* Copyright (C) 2017 Christian Schabesberger <chris.schabesberger@mailbox.org>
* Copyright (C) 2024 Xilin Jia <https://github.com/XilinJia>
* KioskExtractor.kt is part of Vista Guide.
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

abstract class KioskExtractor<T : InfoItem>(
        streamingService: StreamingService,
        linkHandler: ListLinkHandler,
        override val id: String)
    : ListExtractor<T>(streamingService, linkHandler) {
//
//    @Throws(ParsingException::class)
//    abstract fun getName(): String
}
