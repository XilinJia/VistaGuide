package ac.mdiq.vista.extractor.services.youtube.extractors

import ac.mdiq.vista.extractor.InfoItem
import ac.mdiq.vista.extractor.ListExtractor.InfoItemsPage.Companion.emptyPage
import ac.mdiq.vista.extractor.Page
import ac.mdiq.vista.extractor.StreamingService
import ac.mdiq.vista.extractor.channel.tabs.ChannelTabExtractor
import ac.mdiq.vista.extractor.channel.tabs.ChannelTabs
import ac.mdiq.vista.extractor.downloader.Downloader
import ac.mdiq.vista.extractor.exceptions.ContentNotAvailableException
import ac.mdiq.vista.extractor.exceptions.ExtractionException
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.linkhandler.ListLinkHandler
import ac.mdiq.vista.extractor.playlist.PlaylistExtractor
import ac.mdiq.vista.extractor.services.youtube.extractors.YoutubeChannelTabPlaylistExtractor.SystemPlaylistUrlCreationException
import ac.mdiq.vista.extractor.services.youtube.linkHandler.YoutubePlaylistLinkHandlerFactory
import java.io.IOException
import java.lang.IllegalArgumentException


/**
 * A [ChannelTabExtractor] for YouTube system playlists using a
 * [YoutubePlaylistExtractor] instance.
 *
 *
 *
 * It is currently used to bypass age-restrictions on channels marked as age-restricted by their
 * owner(s).
 *
 */
class YoutubeChannelTabPlaylistExtractor(service: StreamingService, linkHandler: ListLinkHandler) : ChannelTabExtractor(service, linkHandler) {

    private val playlistExtractorInstance: PlaylistExtractor
    private var playlistExisting = false

    /**
     * Construct a [YoutubeChannelTabPlaylistExtractor] instance.
     *
     * @param service     a [StreamingService] implementation, which must be the YouTube
     * one
     * @param linkHandler a [ListLinkHandler] which must have a valid channel ID (starting
     * with `UC`) and one of the given and supported content filters:
     * [ChannelTabs.VIDEOS], [ChannelTabs.SHORTS],
     * [ChannelTabs.LIVESTREAMS]
     * @throws IllegalArgumentException if the given [ListLinkHandler] doesn't have the
     * required arguments
     * @throws SystemPlaylistUrlCreationException if the system playlist URL could not be created,
     * which should never happen
     */
    init {
        val playlistLinkHandler = getPlaylistLinkHandler(linkHandler)
        this.playlistExtractorInstance = YoutubePlaylistExtractor(service, playlistLinkHandler)
    }

    @Throws(IOException::class, ExtractionException::class)
    override fun onFetchPage(downloader: Downloader) {
        try {
            playlistExtractorInstance.onFetchPage(downloader)
            if (!playlistExisting) playlistExisting = true
        } catch (e: ContentNotAvailableException) {
            // If a channel has no content of the type requested, the corresponding system playlist
            // won't exist, so a ContentNotAvailableException would be thrown
            // Ignore such issues in this case
        }
    }

    @get:Throws(IOException::class, ExtractionException::class)

    override val initialPage: InfoItemsPage<InfoItem>
        get() {
            if (!playlistExisting) return emptyPage()
            return playlistExtractorInstance.initialPage as InfoItemsPage<InfoItem>
        }

    @Throws(IOException::class, ExtractionException::class)
    override fun getPage(page: Page?): InfoItemsPage<InfoItem> {
        if (!playlistExisting) return emptyPage()
        return playlistExtractorInstance.getPage(page) as InfoItemsPage<InfoItem>
    }

    /**
     * Get a playlist [ListLinkHandler] from a channel tab one.
     *
     * This method converts a channel ID without its `UC` prefix into a YouTube system
     * playlist, depending on the first content filter provided in the given
     * [ListLinkHandler].
     *
     * The first content filter must be a channel tabs one among the
     * [videos][ChannelTabs.VIDEOS], [shorts][ChannelTabs.SHORTS] and
     * [ChannelTabs.LIVESTREAMS] ones, which would be converted respectively into playlists
     * with the ID `UULF`, `UUSH` and `UULV` on which the channel ID without the
     * `UC` part is appended.
     *
     * @param originalLinkHandler the original [ListLinkHandler] with which a
     * [YoutubeChannelTabPlaylistExtractor] instance is being constructed
     *
     * @return a [ListLinkHandler] to use for the [YoutubePlaylistExtractor] instance
     * needed to extract channel tabs data from a system playlist
     * @throws IllegalArgumentException if the original [ListLinkHandler] does not meet the
     * required criteria above
     * @throws SystemPlaylistUrlCreationException if the system playlist URL could not be created,
     * which should never happen
     */

    @Throws(IllegalArgumentException::class, SystemPlaylistUrlCreationException::class)
    private fun getPlaylistLinkHandler(originalLinkHandler: ListLinkHandler): ListLinkHandler {
        val contentFilters = originalLinkHandler.contentFilters
        require(contentFilters.isNotEmpty()) { "A content filter is required" }

        val channelId = originalLinkHandler.id
        require(!(channelId.isEmpty() || !channelId.startsWith("UC"))) { "Invalid channel ID" }

        val channelIdWithoutUc = channelId.substring(2)
        val playlistId = when (contentFilters[0]) {
            ChannelTabs.VIDEOS -> "UULF$channelIdWithoutUc"
            ChannelTabs.SHORTS -> "UUSH$channelIdWithoutUc"
            ChannelTabs.LIVESTREAMS -> "UULV$channelIdWithoutUc"
            else -> throw IllegalArgumentException("Only Videos, Shorts and Livestreams tabs can extracted as playlists")
        }
        try {
            val newUrl: String = YoutubePlaylistLinkHandlerFactory.instance.getUrl(playlistId)
            return ListLinkHandler(newUrl, newUrl, playlistId, listOf(), "")
        } catch (e: ParsingException) {
            // This should be not reachable, as the given playlist ID should be valid and
            // YoutubePlaylistLinkHandlerFactory doesn't throw any exception
            throw SystemPlaylistUrlCreationException("Could not create a YouTube playlist from a valid playlist ID", e)
        }
    }

    /**
     * Exception thrown when a YouTube system playlist URL could not be created.
     *
     * This exception should be never thrown, as given playlist IDs should be always valid.
     *
     */
    class SystemPlaylistUrlCreationException internal constructor(message: String?, cause: Throwable?) : RuntimeException(message, cause)
}
