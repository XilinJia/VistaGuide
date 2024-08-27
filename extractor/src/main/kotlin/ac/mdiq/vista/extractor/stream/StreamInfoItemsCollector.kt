/*
 * Created by Christian Schabesberger on 28.02.16.
 *
 * Copyright (C) 2016 Christian Schabesberger <chris.schabesberger@mailbox.org>
* Copyright (C) 2024 Xilin Jia <https://github.com/XilinJia>
 * StreamInfoItemsCollector.kt is part of Vista Guide.
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

import ac.mdiq.vista.extractor.InfoItemsCollector
import ac.mdiq.vista.extractor.exceptions.FoundAdException
import ac.mdiq.vista.extractor.exceptions.ParsingException

class StreamInfoItemsCollector : InfoItemsCollector<StreamInfoItem, StreamInfoItemExtractor> {
    constructor(serviceId: Int) : super(serviceId)

    constructor(serviceId: Int, comparator: Comparator<StreamInfoItem>?) : super(serviceId, comparator)

    @Throws(ParsingException::class)
    override fun extract(extractor: StreamInfoItemExtractor): StreamInfoItem {
        if (extractor.isAd()) throw FoundAdException("Found ad")
        val resultItem = StreamInfoItem(serviceId, extractor.url, extractor.name, extractor.getStreamType())

        // optional information
        try { resultItem.duration = extractor.getDuration() } catch (e: Exception) { addError(e) }
        try { resultItem.uploaderName = extractor.getUploaderName() } catch (e: Exception) { addError(e) }
        try { resultItem.textualUploadDate = extractor.getTextualUploadDate() } catch (e: Exception) { addError(e) }
        try { resultItem.uploadDate = extractor.getUploadDate() } catch (e: ParsingException) { addError(e) }
        try { resultItem.viewCount = extractor.getViewCount() } catch (e: Exception) { addError(e) }
        try { resultItem.thumbnails = extractor.thumbnails } catch (e: Exception) { addError(e) }
        try { resultItem.uploaderUrl = extractor.getUploaderUrl() } catch (e: Exception) { addError(e) }
        try { resultItem.uploaderAvatars = extractor.getUploaderAvatars() } catch (e: Exception) { addError(e) }
        try { resultItem.isUploaderVerified = extractor.isUploaderVerified() } catch (e: Exception) { addError(e) }
        try { resultItem.shortDescription = extractor.getShortDescription() } catch (e: Exception) { addError(e) }
        try { resultItem.isShortFormContent = extractor.isShortFormContent() } catch (e: Exception) { addError(e) }

        return resultItem
    }

    override fun commit(extractor: StreamInfoItemExtractor) {
        try { addItem(extract(extractor)) } catch (ignored: FoundAdException) { } catch (e: Exception) { addError(e) }
    }
}
