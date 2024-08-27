package ac.mdiq.vista.extractor.services.youtube.stream

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import ac.mdiq.vista.downloader.DownloaderFactory
import ac.mdiq.vista.downloader.DownloaderFactory.getDownloader
import ac.mdiq.vista.extractor.ExtractorAsserts
import ac.mdiq.vista.extractor.InfoItem
import ac.mdiq.vista.extractor.Vista.init
import ac.mdiq.vista.extractor.ServiceList.YouTube
import ac.mdiq.vista.extractor.StreamingService
import ac.mdiq.vista.extractor.playlist.PlaylistInfo.PlaylistType
import ac.mdiq.vista.extractor.playlist.PlaylistInfoItem
import ac.mdiq.vista.extractor.services.DefaultStreamExtractorTest
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.isConsentAccepted
import ac.mdiq.vista.extractor.services.youtube.YoutubeTestsUtils
import ac.mdiq.vista.extractor.services.youtube.stream.YoutubeStreamExtractorDefaultTest.YOUTUBE_LICENCE
import ac.mdiq.vista.extractor.stream.StreamExtractor
import ac.mdiq.vista.extractor.stream.StreamType
import java.util.*
import java.util.function.Consumer
import java.util.stream.Collectors

class YoutubeStreamExtractorRelatedMixTest : DefaultStreamExtractorTest() {
    // @formatter:off
    override fun extractor(): StreamExtractor {
        return extractor!!}
    override fun expectedService(): StreamingService {
        return YouTube}
    override fun expectedName(): String? {
        return TITLE}
    override fun expectedId(): String? {
        return ID}
    override fun expectedUrlContains(): String? {
        return URL}
    override fun expectedOriginalUrlContains(): String? {
        return URL}

    override fun expectedStreamType(): StreamType? {
        return StreamType.VIDEO_STREAM}
    override fun expectedUploaderName(): String? {
        return "NoCopyrightSounds"}
    override fun expectedUploaderUrl(): String? {
        return "https://www.youtube.com/channel/UC_aEa8K-EOJ3D6gOs7HcyNg"}
    override fun expectedDescriptionContains(): List<String?> {
        return mutableListOf<String?>("https://www.youtube.com/user/danielleviband/", "©")
    }
    override fun expectedUploaderVerified(): Boolean {
        return true}
    override fun expectedUploaderSubscriberCountAtLeast(): Long {
        return 32000000}
    override fun expectedLength(): Long {
        return 208}
    override fun expectedViewCountAtLeast(): Long {
        return 449000000}
    override fun expectedUploadDate(): String? {
        return "2015-07-09 16:34:35.000"}
    override fun expectedTextualUploadDate(): String? {
        return "2015-07-09T09:34:35-07:00"}
    override fun expectedLikeCountAtLeast(): Long {
        return 6400000}
    override fun expectedDislikeCountAtLeast(): Long {
        return -1}
    override fun expectedStreamSegmentsCount(): Int {
        return 0}
    override fun expectedLicence(): String {
        return YOUTUBE_LICENCE}
    override fun expectedCategory(): String {
        return "Music"}
    override fun expectedTags(): List<String?> {
        return mutableListOf<String?>("Cartoon", "Cartoon - On & On", "Cartoon On & On (feat. Daniel Levi)",
            "Daniel Levi", "NCS Best Songs", "NCS Cartoon Daniel Levi", "NCS Cartoon On & On",
            "NCS On & On", "NCS On and On", "NCS Release Daniel Levi", "NCS release Cartoon",
            "On & On", "best music", "club music", "copyright free music", "dance music", "edm",
            "electronic music", "electronic pop", "free music", "gaming music", "music", "ncs",
            "no copyright music", "no copyright sounds", "nocopyrightsounds",
            "royalty free music", "songs", "top music")
    }

     // @formatter:on
     @Test
     @Throws(Exception::class)
     override fun testRelatedItems() {
         super.testRelatedItems()

         val playlists = Objects.requireNonNull(extractor!!.relatedItems)!!
             .getItems()!!
             .stream()
             .filter { o: Any? -> PlaylistInfoItem::class.java.isInstance(o) }
             .map { obj: Any? -> PlaylistInfoItem::class.java.cast(obj) }
             .collect(Collectors.toList())
         playlists.forEach(Consumer { item: PlaylistInfoItem ->
             Assertions.assertNotEquals(PlaylistType.NORMAL, item.playlistType,
                 "Unexpected normal playlist in related items")
         })

         val streamMixes = playlists.stream()
             .filter { item: PlaylistInfoItem -> item.playlistType == PlaylistType.MIX_STREAM }
             .collect(Collectors.toList())
         ExtractorAsserts.assertGreaterOrEqual(1,
             streamMixes.size.toLong(),
             "Not found one or more stream mix in related items")

         val streamMix = streamMixes[0]
         Assertions.assertSame(InfoItem.InfoType.PLAYLIST, streamMix.infoType)
         Assertions.assertEquals(YouTube.serviceId, streamMix.serviceId)
         ExtractorAsserts.assertContains(URL, streamMix.url)
         ExtractorAsserts.assertContains("list=RD" + ID, streamMix.url)
         Assertions.assertEquals("Mix – " + TITLE, streamMix.name)
         YoutubeTestsUtils.testImages(streamMix.thumbnails)
     }

    companion object {
        private const val RESOURCE_PATH = DownloaderFactory.RESOURCE_PATH + "services/youtube/extractor/stream/"
        private const val ID = "K4DyBUG242c"
        private const val URL = YoutubeStreamExtractorDefaultTest.BASE_URL + ID
        private const val TITLE = "Cartoon, Jéja - On & On (feat. Daniel Levi) | Electronic Pop | NCS - Copyright Free Music"
        private var extractor: StreamExtractor? = null


        @BeforeAll
        @Throws(Exception::class)
        fun setUp(): Unit {
            YoutubeTestsUtils.ensureStateless()
            isConsentAccepted = true
            init(getDownloader(RESOURCE_PATH + "relatedMix"))
            extractor = YouTube.getStreamExtractor(URL)
            extractor!!.fetchPage()
        }
    }
}
