package ac.mdiq.vista.extractor

import ac.mdiq.vista.extractor.channel.ChannelInfoItemExtractor
import ac.mdiq.vista.extractor.channel.ChannelInfoItemsCollector
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.playlist.PlaylistInfoItemExtractor
import ac.mdiq.vista.extractor.playlist.PlaylistInfoItemsCollector
import ac.mdiq.vista.extractor.stream.StreamInfoItemExtractor
import ac.mdiq.vista.extractor.stream.StreamInfoItemsCollector
import java.util.*

/*
* Created by Christian Schabesberger on 12.02.17.
*
* Copyright (C) 2017 Christian Schabesberger <chris.schabesberger@mailbox.org>
* Copyright (C) 2024 Xilin Jia <https://github.com/XilinJia>
* InfoItemsSearchCollector.kt is part of Vista Guide.
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
 * A collector that can handle many extractor types, to be used when a list contains items of
 * different types (e.g. search)
 *
 * This collector can handle the following extractor types:
 *
 *  * [StreamInfoItemExtractor]
 *  * [ChannelInfoItemExtractor]
 *  * [PlaylistInfoItemExtractor]
 *
 * Calling [.extract] or [.commit] with any
 * other extractor type will raise an exception.
 */
class MultiInfoItemsCollector(serviceId: Int) : InfoItemsCollector<InfoItem, InfoItemExtractor>(serviceId) {

    private val streamCollector = StreamInfoItemsCollector(serviceId)
    private val userCollector = ChannelInfoItemsCollector(serviceId)
    private val playlistCollector = PlaylistInfoItemsCollector(serviceId)

    override val errors: MutableList<Throwable>
        get() {
            val errors: MutableList<Throwable> = ArrayList<Throwable>(super.errors)
            errors.addAll(streamCollector.errors)
            errors.addAll(userCollector.errors)
            errors.addAll(playlistCollector.errors)
            return errors.toMutableList()
        }

    override fun reset() {
        super.reset()
        streamCollector.reset()
        userCollector.reset()
        playlistCollector.reset()
    }

    @Throws(ParsingException::class)
    override fun extract(extractor: InfoItemExtractor): InfoItem {
        // Use the corresponding collector for each item extractor type
        return when (extractor) {
            is StreamInfoItemExtractor -> streamCollector.extract(extractor)
            is ChannelInfoItemExtractor -> userCollector.extract(extractor)
            is PlaylistInfoItemExtractor -> playlistCollector.extract(extractor)
            else -> throw IllegalArgumentException("Invalid extractor type: $extractor")
        }
    }
}
