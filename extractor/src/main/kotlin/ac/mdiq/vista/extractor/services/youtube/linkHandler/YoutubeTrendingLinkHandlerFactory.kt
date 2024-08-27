/*
 * Created by Christian Schabesberger on 12.08.17.
 *
 * Copyright (C) 2018 Christian Schabesberger <chris.schabesberger@mailbox.org>
* Copyright (C) 2024 Xilin Jia <https://github.com/XilinJia>
 * YoutubeTrendingLinkHandlerFactory.kt is part of Vista Guide.
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
package ac.mdiq.vista.extractor.services.youtube.linkHandler

import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.linkhandler.ListLinkHandlerFactory
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.isInvidiousURL
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.isYoutubeURL
import ac.mdiq.vista.extractor.utils.Utils.isHTTP
import ac.mdiq.vista.extractor.utils.Utils.stringToURL
import java.net.MalformedURLException
import java.net.URL

class YoutubeTrendingLinkHandlerFactory private constructor() : ListLinkHandlerFactory() {
    @Throws(ParsingException::class, UnsupportedOperationException::class)
    override fun getUrl(id: String, contentFilters: List<String>, sortFilter: String?): String {
        return "https://www.youtube.com/feed/trending"
    }

    @Throws(ParsingException::class, UnsupportedOperationException::class)
    override fun getId(url: String): String {
        return "Trending"
    }

    override fun onAcceptUrl(url: String): Boolean {
        val urlObj: URL
        try {
            urlObj = stringToURL(url)
        } catch (e: MalformedURLException) {
            return false
        }

        val urlPath = urlObj.path
        return isHTTP(urlObj) && (isYoutubeURL(urlObj) || isInvidiousURL(urlObj)) && urlPath == "/feed/trending"
    }

    companion object {
        val instance: YoutubeTrendingLinkHandlerFactory = YoutubeTrendingLinkHandlerFactory()
    }
}
