package ac.mdiq.vista.extractor.channel

import ac.mdiq.vista.extractor.InfoItem

/*
* Created by Christian Schabesberger on 11.02.17.
*
* Copyright (C) 2017 Christian Schabesberger <chris.schabesberger@mailbox.org>
* Copyright (C) 2024 Xilin Jia <https://github.com/XilinJia>
* ChannelInfoItem.kt is part of Vista Guide.
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
class ChannelInfoItem(
        serviceId: Int,
        url: String,
        name: String)
    : InfoItem(InfoType.CHANNEL, serviceId, url, name) {

    @JvmField
    var description: String? = null
    @JvmField
    var subscriberCount: Long = -1
    @JvmField
    var streamCount: Long = -1
    var isVerified: Boolean = false
}
