/*
 * Created by Christian Schabesberger on 31.07.16.
 *
 * Copyright (C) 2016 Christian Schabesberger <chris.schabesberger@mailbox.org>
* Copyright (C) 2024 Xilin Jia <https://github.com/XilinJia>
 * ChannelInfo.kt is part of Vista Guide.
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

import ac.mdiq.vista.extractor.Image
import ac.mdiq.vista.extractor.Info
import ac.mdiq.vista.extractor.Vista.getServiceByUrl
import ac.mdiq.vista.extractor.StreamingService
import ac.mdiq.vista.extractor.exceptions.ExtractionException
import ac.mdiq.vista.extractor.linkhandler.ListLinkHandler
import java.io.IOException


class ChannelInfo(
        serviceId: Int,
        id: String,
        url: String,
        originalUrl: String,
        name: String)
    : Info(serviceId, id, url, originalUrl, name) {

    var parentChannelName: String? = null
    var parentChannelUrl: String? = null
    var feedUrl: String? = null
    var subscriberCount: Long = -1
    var description: String? = null
    var donationLinks: Array<String> = arrayOf()

    var avatars: List<Image> = listOf()

    var banners: List<Image> = listOf()

    var parentChannelAvatars: List<Image> = listOf()
    var isVerified: Boolean = false

    var tabs: List<ListLinkHandler> = listOf()

    var tags: List<String> = listOf()

    companion object {
        @Throws(IOException::class, ExtractionException::class)
        fun getInfo(url: String): ChannelInfo {
            return getInfo(getServiceByUrl(url), url)
        }

        @Throws(IOException::class, ExtractionException::class)
        fun getInfo(service: StreamingService, url: String): ChannelInfo {
            val extractor = service.getChannelExtractor(url)
            extractor.fetchPage()
            return getInfo(extractor)
        }

        @Throws(IOException::class, ExtractionException::class)
        fun getInfo(extractor: ChannelExtractor): ChannelInfo {
            val serviceId = extractor.serviceId
            val id = extractor.id
            val url = extractor.url
            val originalUrl = extractor.originalUrl
            val name = extractor.getName()

            val info = ChannelInfo(serviceId, id, url, originalUrl, name)
            try { info.avatars = extractor.getAvatars() } catch (e: Exception) { info.addError(e) }
            try { info.banners = extractor.getBanners() } catch (e: Exception) { info.addError(e) }
            try { info.feedUrl = extractor.getFeedUrl() } catch (e: Exception) { info.addError(e) }
            try { info.subscriberCount = extractor.getSubscriberCount() } catch (e: Exception) { info.addError(e) }
            try { info.description = extractor.getDescription() } catch (e: Exception) { info.addError(e) }
            try { info.parentChannelName = extractor.getParentChannelName() } catch (e: Exception) { info.addError(e) }
            try { info.parentChannelUrl = extractor.getParentChannelUrl() } catch (e: Exception) { info.addError(e) }
            try { info.parentChannelAvatars = extractor.getParentChannelAvatars() } catch (e: Exception) { info.addError(e) }
            try { info.isVerified = extractor.isVerified() } catch (e: Exception) { info.addError(e) }
            try { info.tabs = extractor.getTabs() } catch (e: Exception) { info.addError(e) }
            try { info.tags = extractor.getTags() } catch (e: Exception) { info.addError(e) }

            return info
        }
    }
}
