/*
 * Created by Christian Schabesberger on 11.02.17.
 *
 * Copyright (C) 2017 Christian Schabesberger <chris.schabesberger@mailbox.org>
* Copyright (C) 2024 Xilin Jia <https://github.com/XilinJia>
 * InfoItem.kt is part of Vista Guide.
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
package ac.mdiq.vista.extractor

import java.io.Serializable

abstract class InfoItem(
        @JvmField val infoType: InfoType,
        @JvmField val serviceId: Int,
        @JvmField val url: String,
        @JvmField val name: String) : Serializable {

    @JvmField
    var thumbnails: List<Image> = listOf()

    override fun toString(): String {
        return javaClass.simpleName + "[url=\"" + url + "\", name=\"" + name + "\"]"
    }

    enum class InfoType {
        STREAM,
        PLAYLIST,
        CHANNEL,
        COMMENT
    }
}
