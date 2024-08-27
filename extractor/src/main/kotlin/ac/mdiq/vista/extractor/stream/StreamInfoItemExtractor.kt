/*
 * Created by Christian Schabesberger on 28.02.16.
 *
 * Copyright (C) 2016 Christian Schabesberger <chris.schabesberger@mailbox.org>
* Copyright (C) 2024 Xilin Jia <https://github.com/XilinJia>
 * StreamInfoItemExtractor.kt is part of Vista Guide.
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
import ac.mdiq.vista.extractor.InfoItemExtractor
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.localization.DateWrapper


interface StreamInfoItemExtractor : InfoItemExtractor {
    @Throws(ParsingException::class)
    fun getStreamType(): StreamType

    @Throws(ParsingException::class)
    fun isAd(): Boolean

    @Throws(ParsingException::class)
    fun getDuration(): Long

    @Throws(ParsingException::class)
    fun getViewCount(): Long

    @Throws(ParsingException::class)
    fun getUploaderName(): String?

    @Throws(ParsingException::class)
    fun getUploaderUrl(): String?

    @Throws(ParsingException::class)
    fun getUploaderAvatars(): List<Image> {
        return listOf()
    }

    @Throws(ParsingException::class)
    fun isUploaderVerified(): Boolean

    @Throws(ParsingException::class)
    fun getTextualUploadDate(): String?

    @Throws(ParsingException::class)
    fun getUploadDate(): DateWrapper?

    @Throws(ParsingException::class)
    fun getShortDescription(): String? {
        return null
    }

    /**
     * Whether the stream is a short-form content.
     * Short-form contents are contents in the style of TikTok, YouTube Shorts, or Instagram Reels videos.
     * @return whether the stream is a short-form content
     * @throws ParsingException if there is an error in the extraction
     */
    @Throws(ParsingException::class)
    fun isShortFormContent(): Boolean {
        return false
    }
}
