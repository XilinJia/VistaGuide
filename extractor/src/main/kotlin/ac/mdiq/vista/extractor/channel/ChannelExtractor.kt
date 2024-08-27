/*
 * Created by Christian Schabesberger on 25.07.16.
 *
 * Copyright (C) 2016 Christian Schabesberger <chris.schabesberger@mailbox.org>
* Copyright (C) 2024 Xilin Jia <https://github.com/XilinJia>
 * ChannelExtractor.kt is part of Vista Guide.
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
package ac.mdiq.vista.extractor.channel

import ac.mdiq.vista.extractor.Extractor
import ac.mdiq.vista.extractor.Image
import ac.mdiq.vista.extractor.StreamingService
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.linkhandler.ListLinkHandler


abstract class ChannelExtractor protected constructor(service: StreamingService, linkHandler: ListLinkHandler) : Extractor(service, linkHandler) {

    @Throws(ParsingException::class)
    abstract fun getAvatars(): List<Image>

    @Throws(ParsingException::class)
    abstract fun getBanners(): List<Image>
    @Throws(ParsingException::class)
    abstract fun getFeedUrl(): String?
    @Throws(ParsingException::class)
    abstract fun getSubscriberCount(): Long
    @Throws(ParsingException::class)
    abstract fun getDescription(): String
    @Throws(ParsingException::class)
    abstract fun getParentChannelName(): String
    @Throws(ParsingException::class)
    abstract fun getParentChannelUrl(): String?

    @Throws(ParsingException::class)
    abstract fun getParentChannelAvatars(): List<Image>
    @Throws(ParsingException::class)
    abstract fun isVerified(): Boolean

    @Throws(ParsingException::class)
    abstract fun getTabs(): List<ListLinkHandler>

    @Throws(ParsingException::class)
    open fun getTags(): List<String> {
        return listOf()
    }
    companion object {
        const val UNKNOWN_SUBSCRIBER_COUNT: Long = -1
    }
}
