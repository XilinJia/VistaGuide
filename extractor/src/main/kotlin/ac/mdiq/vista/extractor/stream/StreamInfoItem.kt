/*
 * Created by Christian Schabesberger on 26.08.15.
 *
 * Copyright (C) 2016 Christian Schabesberger <chris.schabesberger@mailbox.org>
* Copyright (C) 2024 Xilin Jia <https://github.com/XilinJia>
 * StreamInfoItem.kt is part of Vista Guide.
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
package ac.mdiq.vista.extractor.stream

import ac.mdiq.vista.extractor.Image
import ac.mdiq.vista.extractor.InfoItem
import ac.mdiq.vista.extractor.localization.DateWrapper


/**
 * Info object for previews of unopened videos, e.g. search results, related videos.
 */
class StreamInfoItem(
        serviceId: Int,
        url: String,
        name: String,
        @JvmField val streamType: StreamType)
    : InfoItem(InfoType.STREAM, serviceId, url, name) {

    @JvmField
    var uploaderName: String? = null
    @JvmField
    var shortDescription: String? = null
    @JvmField
    var textualUploadDate: String? = null
    @JvmField
    var uploadDate: DateWrapper? = null
    @JvmField
    var viewCount: Long = -1
    @JvmField
    var duration: Long = -1

    @JvmField
    var uploaderUrl: String? = null

    @JvmField
    var uploaderAvatars: List<Image> = listOf()
    var isUploaderVerified: Boolean = false
    var isShortFormContent: Boolean = false

    override fun toString(): String {
        return ("StreamInfoItem{"
                + "streamType=" + streamType
                + ", uploaderName='" + uploaderName + '\''
                + ", textualUploadDate='" + textualUploadDate + '\''
                + ", viewCount=" + viewCount
                + ", duration=" + duration
                + ", uploaderUrl='" + uploaderUrl + '\''
                + ", infoType=" + infoType
                + ", serviceId=" + serviceId
                + ", url='" + url + '\''
                + ", name='" + name + '\''
                + ", thumbnails='" + thumbnails + '\''
                + ", uploaderVerified='" + isUploaderVerified + '\''
                + '}')
    }
}
