package ac.mdiq.vista.extractor

import ac.mdiq.vista.extractor.services.bandcamp.BandcampService
import ac.mdiq.vista.extractor.services.media_ccc.MediaCCCService
import ac.mdiq.vista.extractor.services.peertube.PeertubeService
import ac.mdiq.vista.extractor.services.soundcloud.SoundcloudService
import ac.mdiq.vista.extractor.services.youtube.YoutubeService

/*
* Copyright (C) 2018 Christian Schabesberger <chris.schabesberger@mailbox.org>
* Copyright (C) 2024 Xilin Jia <https://github.com/XilinJia>
* ServiceList.kt is part of Vista Guide.
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
 * A list of supported services.
 */
// keep unusual names and inner assignments
object ServiceList {
    @JvmField
    val YouTube: YoutubeService = YoutubeService(0)
    @JvmField
    val SoundCloud: SoundcloudService = SoundcloudService(1)
    @JvmField
    val MediaCCC: MediaCCCService = MediaCCCService(2)
    @JvmField
    val PeerTube: PeertubeService = PeertubeService(3)
    @JvmField
    val Bandcamp: BandcampService = BandcampService(4)

    /**
     * When creating a new service, put this service in the end of this list,
     * and give it the next free id.
     */
    private val SERVICES: List<StreamingService> = listOf(YouTube, SoundCloud, MediaCCC, PeerTube, Bandcamp)

    /**
     * Get all the supported services.
     *
     * @return a unmodifiable list of all the supported services
     */

    fun all(): List<StreamingService> {
        return SERVICES
    }
}
