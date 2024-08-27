package ac.mdiq.vista.extractor

import java.util.*
import java.util.function.Function
import java.util.stream.Collectors


/*
* Created by Adam Howard on 08/11/15.
*
* Copyright (c) 2015 Christian Schabesberger <chris.schabesberger@mailbox.org>
* Copyright (C) 2024 Xilin Jia <https://github.com/XilinJia>
*     and Adam Howard <achdisposable1@gmail.com>
*
* MediaFormat.kt is part of Vista Guide.
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

/**
 * Static data about various media formats support by Vista, eg mime type, extension
 */
// we want the media format table below to be aligned
enum class MediaFormat(
        @JvmField val id: Int,
        /**
         * Get the name of the format
         * @return the name of the format
         */
        // TODO: XJ
        val name_: String,
        @JvmField
        /**
         * Get the filename extension
         * @return the filename extension
         */
        val suffix: String,
        @JvmField
        /**
         * Get the mime type
         * @return the mime type
         */
        val mimeType: String) {
    // @formatter:off
 //video and audio combined formats
 //         id     name         suffix  mimeType
    MPEG_4(0x0, "MPEG-4", "mp4", "video/mp4"),
    v3GPP(0x10, "3GPP", "3gp", "video/3gpp"),
    WEBM(0x20, "WebM", "webm", "video/webm"),
     // audio formats
    M4A(0x100, "m4a", "m4a", "audio/mp4"),
    WEBMA(0x200, "WebM", "webm", "audio/webm"),
    MP3(0x300, "MP3", "mp3", "audio/mpeg"),
    MP2(0x310, "MP2", "mp2", "audio/mpeg"),
    OPUS(0x400, "opus", "opus", "audio/opus"),
    OGG(0x500, "ogg", "ogg", "audio/ogg"),
    WEBMA_OPUS(0x200, "WebM Opus", "webm", "audio/webm"),
    AIFF(0x600, "AIFF", "aiff", "audio/aiff"),
     /**
      * Same as {@link MediaFormat#AIFF}, just with the shorter suffix/file extension
 */
    AIF(0x600, "AIFF", "aif", "audio/aiff"),
    WAV(0x700, "WAV", "wav", "audio/wav"),
    FLAC(0x800, "FLAC", "flac", "audio/flac"),
    ALAC(0x900, "ALAC", "alac", "audio/alac"),
     // subtitles formats
    VTT(0x1000, "WebVTT", "vtt", "text/vtt"),
    TTML(0x2000, "Timed Text Markup Language", "ttml", "application/ttml+xml"),
    TRANSCRIPT1(0x3000, "TranScript v1", "srv1", "text/xml"),
    TRANSCRIPT2(0x4000, "TranScript v2", "srv2", "text/xml"),
    TRANSCRIPT3(0x5000, "TranScript v3", "srv3", "text/xml"),
    SRT(0x6000, "SubRip file format", "srt", "text/srt"); // @formatter:on

    fun getName(): String {
        return name_
    }

    companion object {
        private fun <T> getById(id: Int, field: Function<MediaFormat, T>, orElse: T): T {
            return Arrays.stream(entries.toTypedArray())
                .filter { mediaFormat: MediaFormat -> mediaFormat.id == id }
                .map(field)
                .findFirst()
                .orElse(orElse)
        }

        /**
         * Return the friendly name of the media format with the supplied id
         *
         * @param id the id of the media format. Currently an arbitrary, Vista-specific number.
         * @return the friendly name of the MediaFormat associated with this ids,
         * or an empty String if none match it.
         */
        fun getNameById(id: Int): String {
            return getById(id, { obj: MediaFormat -> obj.name }, "")
        }

        /**
         * Return the file extension of the media format with the supplied id
         *
         * @param id the id of the media format. Currently an arbitrary, Vista-specific number.
         * @return the file extension of the MediaFormat associated with this ids,
         * or an empty String if none match it.
         */
        fun getSuffixById(id: Int): String {
            return getById(id, { obj: MediaFormat -> obj.suffix }, "")
        }

        /**
         * Return the MIME type of the media format with the supplied id
         *
         * @param id the id of the media format. Currently an arbitrary, Vista-specific number.
         * @return the MIME type of the MediaFormat associated with this ids,
         * or an empty String if none match it.
         */
        fun getMimeById(id: Int): String? {
            return getById(id, { obj: MediaFormat -> obj.mimeType }, null)
        }

        /**
         * Return the first [MediaFormat] with the supplied mime type.
         * There might be more formats which have the same mime type.
         * To retrieve those, use [.getAllFromMimeType].
         *
         * @return MediaFormat associated with this mime type,
         * or null if none match it.
         */
        fun getFromMimeType(mimeType: String): MediaFormat? {
            return Arrays.stream(entries.toTypedArray())
                .filter { mediaFormat: MediaFormat? -> mediaFormat!!.mimeType == mimeType }
                .findFirst()
                .orElse(null)
        }

        /**
         * Get all media formats which have the given mime type.
         * @param mimeType the mime type to search for
         * @return a modifiable [List] which contains the [MediaFormat]s
         * that have the given mime type.
         */
        fun getAllFromMimeType(mimeType: String): List<MediaFormat> {
            return Arrays.stream(entries.toTypedArray())
                .filter { mediaFormat: MediaFormat -> mediaFormat.mimeType == mimeType }
                .collect(Collectors.toList())
        }

        /**
         * Get the media format by its id.
         *
         * @param id the id
         * @return the id of the media format or null.
         */
        fun getFormatById(id: Int): MediaFormat? {
            return getById(id, { mediaFormat: MediaFormat? -> mediaFormat }, null)
        }

        /**
         * Get the first media format that has the given suffix/file extension.
         * @return the matching [MediaFormat] or `null` if no associated format is found
         */

        fun getFromSuffix(suffix: String): MediaFormat? {
            return Arrays.stream(entries.toTypedArray())
                .filter { mediaFormat: MediaFormat? -> mediaFormat!!.suffix == suffix }
                .findFirst()
                .orElse(null)
        }
    }
}
