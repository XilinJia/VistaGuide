/*
 * Created by Christian Schabesberger on 25.07.16.
 *
 * Copyright (C) 2018 Christian Schabesberger <chrźis.schabesberger@mailbox.org>
* Copyright (C) 2024 Xilin Jia <https://github.com/XilinJia>
 * YoutubeChannelLinkHandlerFactory.kt is part of Vista Guide.
 *
 * Vista Guide is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Vista Guide is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Vista Guide. If not, see <https://www.gnu.org/licenses/>.
 */
package ac.mdiq.vista.extractor.services.youtube.linkHandler

import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.linkhandler.ListLinkHandlerFactory
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.isHooktubeURL
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.isInvidiousURL
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.isYoutubeURL
import ac.mdiq.vista.extractor.utils.Utils.isHTTP
import ac.mdiq.vista.extractor.utils.Utils.stringToURL
import java.util.regex.Pattern


class YoutubeChannelLinkHandlerFactory private constructor() : ListLinkHandlerFactory() {
    /**
     * Returns the URL to a channel from an ID.
     * @param id the channel ID including e.g. 'channel/'
     * @return the URL to the channel
     */
    @Throws(ParsingException::class, UnsupportedOperationException::class)
    override fun getUrl(id: String, contentFilters: List<String>, sortFilter: String?): String {
        return "https://www.youtube.com/$id"
    }

    /**
     * Checks whether the given path conforms to custom short channel URLs like
     * `youtube.com/yourcustomname`.
     *
     * @param splitPath the path segments array
     * @return whether the value conform to short channel URLs
     */
    private fun isCustomShortChannelUrl(splitPath: Array<String>): Boolean {
        return splitPath.size == 1 && !EXCLUDED_SEGMENTS.matcher(splitPath[0]).matches()
    }

    /**
     * Checks whether the given path conforms to handle URLs like `youtube.com/@yourhandle`.
     * @param splitPath the path segments array
     * @return whether the value conform to handle URLs
     */
    private fun isHandle(splitPath: Array<String>): Boolean {
        return splitPath.isNotEmpty() && splitPath[0].startsWith("@")
    }

    @Throws(ParsingException::class, UnsupportedOperationException::class)
    override fun getId(url: String): String {
        try {
            val urlObj = stringToURL(url)
            var path = urlObj.path

            if (!isHTTP(urlObj) || !(isYoutubeURL(urlObj) || isInvidiousURL(urlObj) || isHooktubeURL(urlObj))) throw ParsingException("The URL given is not a YouTube URL")

            // Remove leading "/"
            path = path.substring(1)
            var splitPath = path.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            // Handle YouTube handle URLs like youtube.com/@yourhandle
            if (isHandle(splitPath)) return splitPath[0]
            if (isCustomShortChannelUrl(splitPath)) {
                // Handle custom short channel URLs like youtube.com/yourcustomname
                path = "c/$path"
                splitPath = path.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            }

            if (!path.startsWith("user/") && !path.startsWith("channel/") && !path.startsWith("c/"))
                throw ParsingException("The given URL is not a channel, a user or a handle URL")

            val id = splitPath[1]
            if (id.isEmpty()) throw ParsingException("The given ID is not a YouTube channel or user ID")
            return splitPath[0] + "/" + id
        } catch (e: Exception) {
            throw ParsingException("Could not parse URL :" + e.message, e)
        }
    }

    override fun onAcceptUrl(url: String): Boolean {
        try {
            getId(url)
        } catch (e: ParsingException) {
            return false
        }
        return true
    }

    companion object {
        val instance: YoutubeChannelLinkHandlerFactory = YoutubeChannelLinkHandlerFactory()
        private val EXCLUDED_SEGMENTS = Pattern.compile("playlist|watch|attribution_link|watch_popup|embed|feed|select_site|account|reporthistory|redirect")
    }
}
