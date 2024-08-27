package ac.mdiq.vista.extractor.services.peertube.extractors

import com.grack.nanojson.JsonObject
import ac.mdiq.vista.extractor.Image
import ac.mdiq.vista.extractor.ServiceList
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.localization.DateWrapper
import ac.mdiq.vista.extractor.services.peertube.PeertubeParsingHelper.getAvatarsFromOwnerAccountOrVideoChannelObject
import ac.mdiq.vista.extractor.services.peertube.PeertubeParsingHelper.getThumbnailsFromPlaylistOrVideoItem
import ac.mdiq.vista.extractor.services.peertube.PeertubeParsingHelper.parseDateFrom
import ac.mdiq.vista.extractor.stream.StreamInfoItemExtractor
import ac.mdiq.vista.extractor.stream.StreamType
import ac.mdiq.vista.extractor.utils.JsonUtils.getString


open class PeertubeStreamInfoItemExtractor(
        protected val item: JsonObject,
        private var baseUrl: String)
    : StreamInfoItemExtractor {

    @get:Throws(ParsingException::class)
    override val url: String
        get() {
            val uuid = getString(item, "uuid")
            return ServiceList.PeerTube.getStreamLHFactory().fromId(uuid, baseUrl).url
        }

    @get:Throws(ParsingException::class)

    override val thumbnails: List<Image>
        get() = getThumbnailsFromPlaylistOrVideoItem(baseUrl, item)

    @get:Throws(ParsingException::class)
    override val name: String
        get() = getString(item, "name")

    override fun isAd(): Boolean {
        return false
    }

    override fun getViewCount(): Long {
        return item.getLong("views")
    }

    @Throws(ParsingException::class)
    override fun getUploaderUrl(): String? {
        val name = getString(item, "account.name")
        val host = getString(item, "account.host")

        return ServiceList.PeerTube.getChannelLHFactory().fromId("accounts/$name@$host", baseUrl).url
    }


    override fun getUploaderAvatars(): List<Image> {
        return getAvatarsFromOwnerAccountOrVideoChannelObject(baseUrl, item.getObject("account"))
    }

    @Throws(ParsingException::class)
    override fun isUploaderVerified(): Boolean {
        return false
    }

    @Throws(ParsingException::class)
    override fun getUploaderName(): String? {
        return getString(item, "account.displayName")
    }

    @Throws(ParsingException::class)
    override fun getTextualUploadDate(): String? {
        return getString(item, "publishedAt")
    }

    @Throws(ParsingException::class)
    override fun getUploadDate(): DateWrapper? {
        val textualUploadDate = getTextualUploadDate() ?: return null
        return DateWrapper(parseDateFrom(textualUploadDate))
    }

    override fun getStreamType(): StreamType {
        return if (item.getBoolean("isLive")) StreamType.LIVE_STREAM else StreamType.VIDEO_STREAM
    }

    override fun getDuration(): Long {
        return item.getLong("duration")
    }

    protected fun setBaseUrl(baseUrl: String) {
        this.baseUrl = baseUrl
    }
}
