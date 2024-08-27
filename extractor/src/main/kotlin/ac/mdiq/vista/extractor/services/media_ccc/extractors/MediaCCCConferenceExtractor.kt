package ac.mdiq.vista.extractor.services.media_ccc.extractors

import com.grack.nanojson.JsonObject
import com.grack.nanojson.JsonParser
import com.grack.nanojson.JsonParserException
import ac.mdiq.vista.extractor.Image
import ac.mdiq.vista.extractor.StreamingService
import ac.mdiq.vista.extractor.channel.ChannelExtractor
import ac.mdiq.vista.extractor.channel.tabs.ChannelTabExtractor
import ac.mdiq.vista.extractor.channel.tabs.ChannelTabs
import ac.mdiq.vista.extractor.downloader.Downloader
import ac.mdiq.vista.extractor.exceptions.ExtractionException
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.linkhandler.ListLinkHandler
import ac.mdiq.vista.extractor.linkhandler.ReadyChannelTabListLinkHandler
import ac.mdiq.vista.extractor.linkhandler.ReadyChannelTabListLinkHandler.ChannelTabExtractorBuilder
import ac.mdiq.vista.extractor.services.media_ccc.linkHandler.MediaCCCConferenceLinkHandlerFactory
import java.io.IOException



class MediaCCCConferenceExtractor(service: StreamingService, linkHandler: ListLinkHandler) : ChannelExtractor(service, linkHandler) {
    private var conferenceData: JsonObject? = null


    override fun getAvatars(): List<Image> {
        return MediaCCCParsingHelper.getImageListFromLogoImageUrl(conferenceData!!.getString("logo_url"))
    }


    override fun getBanners(): List<Image> {
        return emptyList()
    }

    override fun getFeedUrl(): String? {
        return null
    }

    override fun getSubscriberCount(): Long {
        return -1
    }

    override fun getDescription(): String {
        return ""
    }

    override fun getParentChannelName(): String {
        return ""
    }

    override fun getParentChannelUrl(): String {
        return ""
    }


    override fun getParentChannelAvatars(): List<Image> {
        return emptyList()
    }

    override fun isVerified(): Boolean {
        return false
    }


    @Throws(ParsingException::class)
    override fun getTabs(): List<ListLinkHandler> {
        // avoid keeping a reference to MediaCCCConferenceExtractor inside the lambda
        val theConferenceData = conferenceData
        return listOf<ListLinkHandler>(ReadyChannelTabListLinkHandler(url, id, ChannelTabs.VIDEOS,
            object: ChannelTabExtractorBuilder {
                override fun build(service: StreamingService, linkHandler: ListLinkHandler): ChannelTabExtractor {
                    return MediaCCCChannelTabExtractor(service, linkHandler, theConferenceData)
                }
            }))
    }

    @Throws(IOException::class, ExtractionException::class)
    override fun onFetchPage(downloader: Downloader) {
        conferenceData = fetchConferenceData(downloader, id)
    }


    @Throws(ParsingException::class)
    override fun getName(): String {
        return conferenceData!!.getString("title")
    }

    companion object {
        @Throws(IOException::class, ExtractionException::class)
        fun fetchConferenceData(downloader: Downloader, conferenceId: String): JsonObject {
            val conferenceUrl = MediaCCCConferenceLinkHandlerFactory.CONFERENCE_API_ENDPOINT + conferenceId
            try {
                return JsonParser.`object`().from(downloader.get(conferenceUrl).responseBody())
            } catch (jpe: JsonParserException) {
                throw ExtractionException("Could not parse json returned by URL: $conferenceUrl")
            }
        }
    }
}
