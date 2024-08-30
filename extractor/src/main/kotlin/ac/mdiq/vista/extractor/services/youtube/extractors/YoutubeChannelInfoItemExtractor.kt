/*
 * Created by Christian Schabesberger on 12.02.17.
 *
 * Copyright (C) 2017 Christian Schabesberger <chris.schabesberger@mailbox.org>
* Copyright (C) 2024 Xilin Jia <https://github.com/XilinJia>
 * YoutubeChannelInfoItemExtractor.kt is part of Vista Guide.
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
package ac.mdiq.vista.extractor.services.youtube.extractors

import com.grack.nanojson.JsonObject
import ac.mdiq.vista.extractor.Image
import ac.mdiq.vista.extractor.ListExtractor
import ac.mdiq.vista.extractor.channel.ChannelInfoItemExtractor
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.getTextFromObject
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.getThumbnailsFromInfoItem
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.isVerified
import ac.mdiq.vista.extractor.services.youtube.linkHandler.YoutubeChannelLinkHandlerFactory
import ac.mdiq.vista.extractor.utils.Utils.mixedNumberWordToLong
import ac.mdiq.vista.extractor.utils.Utils.removeNonDigitCharacters


class YoutubeChannelInfoItemExtractor(private val channelInfoItem: JsonObject) : ChannelInfoItemExtractor {
    /**
     * New layout:
     * "subscriberCountText": Channel handle
     * "videoCountText": Subscriber count
     */
    private val withHandle: Boolean

    init {
        var wHandle = false
        val subscriberCountText = getTextFromObject(channelInfoItem.getObject("subscriberCountText"))
        if (subscriberCountText != null) wHandle = subscriberCountText.startsWith("@")
        this.withHandle = wHandle
    }

    @get:Throws(ParsingException::class)

    override val thumbnails: List<Image>
        get() {
            try { return getThumbnailsFromInfoItem(channelInfoItem) } catch (e: Exception) { throw ParsingException("Could not get thumbnails", e) }
        }

    @get:Throws(ParsingException::class)
    override val name: String
        get() {
            try { return getTextFromObject(channelInfoItem.getObject("title")) ?: "" } catch (e: Exception) { throw ParsingException("Could not get name", e) }
        }

    @get:Throws(ParsingException::class)
    override val url: String
        get() {
            try {
                val id = "channel/" + channelInfoItem.getString("channelId")
                return YoutubeChannelLinkHandlerFactory.instance.getUrl(id)
            } catch (e: Exception) { throw ParsingException("Could not get url", e) }
        }

    @Throws(ParsingException::class)
    override fun getSubscriberCount(): Long {
        try {
            // Subscription count is not available for this channel item.
            if (!channelInfoItem.has("subscriberCountText")) return -1

            if (withHandle)
                return if (channelInfoItem.has("videoCountText")) mixedNumberWordToLong(getTextFromObject(channelInfoItem.getObject("videoCountText"))) else -1

            return mixedNumberWordToLong(getTextFromObject(channelInfoItem.getObject("subscriberCountText")))
        } catch (e: Exception) { throw ParsingException("Could not get subscriber count", e) }
    }

    @Throws(ParsingException::class)
    override fun getStreamCount(): Long {
        try {
            // Video count is not available, either the channel has no public uploads
            // or YouTube displays the channel handle instead.
            if (withHandle || !channelInfoItem.has("videoCountText")) return ListExtractor.ITEM_COUNT_UNKNOWN

            return removeNonDigitCharacters(getTextFromObject(channelInfoItem.getObject("videoCountText"))!!).toLong()
        } catch (e: Exception) { throw ParsingException("Could not get stream count", e) }
    }

    @Throws(ParsingException::class)
    override fun isVerified(): Boolean {
        return isVerified(channelInfoItem.getArray("ownerBadges"))
    }

    @Throws(ParsingException::class)
    override fun getDescription(): String {
        try {
            // Channel have no description.
            if (!channelInfoItem.has("descriptionSnippet")) return ""
            return getTextFromObject(channelInfoItem.getObject("descriptionSnippet")) ?: ""
        } catch (e: Exception) { throw ParsingException("Could not get description", e) }
    }
}
