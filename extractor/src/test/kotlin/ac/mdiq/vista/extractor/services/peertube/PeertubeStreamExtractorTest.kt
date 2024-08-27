package ac.mdiq.vista.extractor.services.peertube

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import ac.mdiq.vista.downloader.DownloaderTestImpl.Companion.getInstance
import ac.mdiq.vista.extractor.Vista.init
import ac.mdiq.vista.extractor.ServiceList.PeerTube
import ac.mdiq.vista.extractor.StreamingService
import ac.mdiq.vista.extractor.exceptions.ExtractionException
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.services.DefaultStreamExtractorTest
import ac.mdiq.vista.extractor.stream.StreamExtractor
import ac.mdiq.vista.extractor.stream.StreamType
import java.io.IOException
import java.util.*

abstract class PeertubeStreamExtractorTest : DefaultStreamExtractorTest() {
    override fun expectedHasAudioStreams(): Boolean {
        return false
    }

    class WhatIsPeertube : PeertubeStreamExtractorTest() {
        @Test
        @Throws(ParsingException::class)
        fun testGetLanguageInformation() {
            Assertions.assertEquals(Locale("en"), extractor!!.languageInfo)
        }

        override fun extractor(): StreamExtractor {
            return extractor!!
        }

        override fun expectedService(): StreamingService {
            return PeerTube
        }

        override fun expectedName(): String? {
            return "What is PeerTube?"
        }

        override fun expectedId(): String? {
            return ID
        }

        override fun expectedUrlContains(): String? {
            return INSTANCE + BASE_URL + ID
        }

        override fun expectedOriginalUrlContains(): String? {
            return URL
        }

        override fun expectedStreamType(): StreamType? {
            return StreamType.VIDEO_STREAM
        }

        override fun expectedUploaderName(): String? {
            return "Framasoft"
        }

        override fun expectedUploaderUrl(): String? {
            return "https://framatube.org/accounts/framasoft@framatube.org"
        }

        override fun expectedSubChannelName(): String {
            return "A propos de PeerTube"
        }

        override fun expectedSubChannelUrl(): String {
            return "https://framatube.org/video-channels/joinpeertube"
        }

        override fun expectedDescriptionContains(): List<String?> { // CRLF line ending
            return Arrays.asList(("""
    **[Want to help to translate this video?](https://weblate.framasoft.org/projects/what-is-peertube-video/)**

    **Take back the control of your videos! [#JoinPeertube](https://joinpeertube.org)**
    *A decentralized video hosting network, based on free/libre software!*

    **Animation Produced by:** [LILA](https://libreart.info) - [ZeMarmot Team](https://film.zemarmot.net)
    *Directed by* Aryeom
    *Assistant* Jehan
    **Licence**: [CC-By-SA 4.0](https://creativecommons.org/licenses/by-sa/4.0/)

    **Sponsored by** [Framasoft](https://framasoft.org)

    **Music**: [Red Step Forward](http://play.dogmazic.net/song.php?song_id=52491) - CC-By Ken Bushima

    **Movie Clip**: [Caminades 3: Llamigos](http://www.caminandes.com/) CC-By Blender Institute

    **Video sources**: https://gitlab.gnome.org/Jehan/what-is-peertube/
    """.trimIndent()))
        }

        override fun expectedLength(): Long {
            return 113
        }

        override fun expectedTimestamp(): Long {
            return (TIMESTAMP_MINUTE * 60 + TIMESTAMP_SECOND).toLong()
        }

        override fun expectedViewCountAtLeast(): Long {
            return 38600
        }

        override fun expectedUploadDate(): String? {
            return "2018-10-01 10:52:46.396"
        }

        override fun expectedTextualUploadDate(): String? {
            return "2018-10-01T10:52:46.396Z"
        }

        override fun expectedLikeCountAtLeast(): Long {
            return 18
        }

        override fun expectedDislikeCountAtLeast(): Long {
            return 0
        }

        override fun expectedHost(): String? {
            return "framatube.org"
        }

        override fun expectedCategory(): String? {
            return "Science & Technology"
        }

        override fun expectedLicence(): String? {
            return "Attribution - Share Alike"
        }

        override fun expectedLanguageInfo(): Locale? {
            return Locale.forLanguageTag("en")
        }

        override fun expectedTags(): List<String?>? {
            return mutableListOf<String?>("framasoft", "peertube")
        }

        override fun expectedHasFrames(): Boolean {
            return false
        } // not yet supported by instance

        companion object {
            private const val ID = "9c9de5e8-0a1e-484a-b099-e80766180a6d"
            private const val INSTANCE = "https://framatube.org"
            private const val TIMESTAMP_MINUTE = 1
            private const val TIMESTAMP_SECOND = 21
            private const val URL =
                INSTANCE + BASE_URL + ID + "?start=" + TIMESTAMP_MINUTE + "m" + TIMESTAMP_SECOND + "s"
            private var extractor: StreamExtractor? = null


            @BeforeAll
            @Throws(Exception::class)
            fun setUp(): Unit {
                init(getInstance()!!)
                // setting instance might break test when running in parallel (!)
                PeerTube.instance = PeertubeInstance(INSTANCE, "FramaTube")
                extractor = PeerTube.getStreamExtractor(URL)
                extractor!!.fetchPage()
            }
        }
    }

    class HlsOnlyStreams : PeertubeStreamExtractorTest() {
        override fun extractor(): StreamExtractor {
            return extractor!!
        }

        override fun expectedService(): StreamingService {
            return PeerTube
        }

        override fun expectedName(): String? {
            return "A Goodbye to Flash Games"
        }

        override fun expectedId(): String? {
            return ID
        }

        override fun expectedUrlContains(): String? {
            return INSTANCE + BASE_URL + ID
        }

        override fun expectedOriginalUrlContains(): String? {
            return URL
        }

        override fun expectedStreamType(): StreamType? {
            return StreamType.VIDEO_STREAM
        }

        override fun expectedUploaderName(): String? {
            return "Marinauts"
        }

        override fun expectedUploaderUrl(): String? {
            return "https://tilvids.com/accounts/marinauts@tilvids.com"
        }

        override fun expectedSubChannelName(): String {
            return "Main marinauts channel"
        }

        override fun expectedSubChannelUrl(): String {
            return "https://tilvids.com/video-channels/marinauts_channel"
        }

        override fun expectedDescriptionContains(): List<String?> { // CRLF line ending
            return mutableListOf<String?>("Goodbye", "Flash Games", "Anthony takes a minute", "Songs used:")
        }

        override fun expectedLength(): Long {
            return 362
        }

        override fun expectedViewCountAtLeast(): Long {
            return 20
        }

        override fun expectedUploadDate(): String? {
            return "2021-04-08 20:15:32.434"
        }

        override fun expectedTextualUploadDate(): String? {
            return "2021-04-08T20:15:32.434Z"
        }

        override fun expectedLikeCountAtLeast(): Long {
            return 6
        }

        override fun expectedDislikeCountAtLeast(): Long {
            return 0
        }

        override fun expectedHasSubtitles(): Boolean {
            return false
        }

        override fun expectedHost(): String? {
            return "tilvids.com"
        }

        override fun expectedCategory(): String? {
            return "Entertainment"
        }

        override fun expectedLicence(): String? {
            return "Unknown"
        }

        override fun expectedLanguageInfo(): Locale? {
            return null
        }

        override fun expectedTags(): List<String?>? {
            return mutableListOf<String?>("Marinauts",
                "adobe flash",
                "adobe flash player",
                "flash games",
                "the marinauts")
        }

        override fun expectedHasFrames(): Boolean {
            return false
        } // not yet supported by instance

        companion object {
            private const val ID = "41342cb4-6fa8-402d-a116-1f63a7f438a3"
            private const val INSTANCE = "https://tilvids.com"

            private const val URL = INSTANCE + BASE_URL + ID
            private var extractor: StreamExtractor? = null


            @BeforeAll
            @Throws(Exception::class)
            fun setUp(): Unit {
                init(getInstance()!!)
                // setting instance might break test when running in parallel (!)
                PeerTube.instance = PeertubeInstance(INSTANCE, "TILvids")
                extractor = PeerTube.getStreamExtractor(URL)
                extractor!!.fetchPage()
            }
        }
    }

    @Disabled("Test broken, SSL problem")
    class AgeRestricted : PeertubeStreamExtractorTest() {
        override fun extractor(): StreamExtractor {
            return extractor!!
        }

        override fun expectedService(): StreamingService {
            return PeerTube
        }

        override fun expectedName(): String? {
            return "Covid-19 ? [Court-métrage]"
        }

        override fun expectedId(): String? {
            return ID
        }

        override fun expectedUrlContains(): String? {
            return INSTANCE + BASE_URL + ID
        }

        override fun expectedOriginalUrlContains(): String? {
            return URL
        }

        override fun expectedStreamType(): StreamType? {
            return StreamType.VIDEO_STREAM
        }

        override fun expectedUploaderName(): String? {
            return "Résilience humaine"
        }

        override fun expectedUploaderUrl(): String? {
            return "https://nocensoring.net/accounts/gmt@nocensoring.net"
        }

        override fun expectedSubChannelName(): String {
            return "SYSTEM FAILURE Quel à-venir ?"
        }

        override fun expectedSubChannelUrl(): String {
            return "https://nocensoring.net/video-channels/systemfailure_quel"
        }

        override fun expectedDescriptionContains(): List<String?> { // LF line ending
            return mutableListOf<String?>("2020, le monde est frappé par une pandémie, beaucoup d'humains sont confinés.",
                "System Failure Quel à-venir ? - Covid-19   / 2020")
        }

        override fun expectedLength(): Long {
            return 667
        }

        override fun expectedViewCountAtLeast(): Long {
            return 138
        }

        override fun expectedUploadDate(): String? {
            return "2020-05-14 17:24:35.580"
        }

        override fun expectedTextualUploadDate(): String? {
            return "2020-05-14T17:24:35.580Z"
        }

        override fun expectedLikeCountAtLeast(): Long {
            return 1
        }

        override fun expectedDislikeCountAtLeast(): Long {
            return 0
        }

        override fun expectedAgeLimit(): Int {
            return 18
        }

        override fun expectedHost(): String? {
            return "nocensoring.net"
        }

        override fun expectedCategory(): String? {
            return "Art"
        }

        override fun expectedLicence(): String? {
            return "Attribution"
        }

        override fun expectedTags(): List<String?>? {
            return mutableListOf<String?>("Covid-19",
                "Gérôme-Mary trebor",
                "Horreur et beauté",
                "court-métrage",
                "nue artistique")
        }

        companion object {
            private const val ID = "dbd8e5e1-c527-49b6-b70c-89101dbb9c08"
            private const val INSTANCE = "https://nocensoring.net"
            private const val URL = INSTANCE + "/videos/embed/" + ID
            private var extractor: StreamExtractor? = null


            @BeforeAll
            @Throws(Exception::class)
            fun setUp(): Unit {
                init(getInstance()!!)

                // setting instance might break test when running in parallel (!)
                PeerTube.instance = PeertubeInstance(INSTANCE)
                extractor = PeerTube.getStreamExtractor(URL)
                extractor!!.fetchPage()
            }
        }
    }

    class Segments : PeertubeStreamExtractorTest() {
        override fun extractor(): StreamExtractor {
            return extractor!!
        }

        override fun expectedService(): StreamingService {
            return PeerTube
        }

        override fun expectedName(): String? {
            return "Bauinformatik 11 – Objekte und Methoden"
        }

        override fun expectedId(): String? {
            return ID
        }

        override fun expectedUrlContains(): String? {
            return INSTANCE + BASE_URL + ID
        }

        override fun expectedOriginalUrlContains(): String? {
            return URL
        }

        override fun expectedStreamType(): StreamType? {
            return StreamType.VIDEO_STREAM
        }

        override fun expectedUploaderName(): String? {
            return "Martin Vogel"
        }

        override fun expectedUploaderUrl(): String? {
            return "https://tube.tchncs.de/accounts/martin_vogel@tube.tchncs.de"
        }

        override fun expectedSubChannelName(): String {
            return "Bauinformatik mit Python"
        }

        override fun expectedSubChannelUrl(): String {
            return "https://tube.tchncs.de/video-channels/python"
        }

        override fun expectedDescriptionContains(): List<String?> { // CRLF line ending
            return mutableListOf<String?>("Um",
                "Programme",
                "Variablen",
                "Funktionen",
                "Objekte",
                "Skript",
                "Wiederholung",
                "Listen")
        }

        override fun expectedLength(): Long {
            return 1017
        }

        override fun expectedViewCountAtLeast(): Long {
            return 20
        }

        override fun expectedUploadDate(): String? {
            return "2023-12-08 15:57:04.142"
        }

        override fun expectedTextualUploadDate(): String? {
            return "2023-12-08T15:57:04.142Z"
        }

        override fun expectedLikeCountAtLeast(): Long {
            return 0
        }

        override fun expectedDislikeCountAtLeast(): Long {
            return 0
        }

        override fun expectedHasSubtitles(): Boolean {
            return false
        }

        override fun expectedHost(): String? {
            return "tube.tchncs.de"
        }

        override fun expectedCategory(): String? {
            return "Unknown"
        }

        override fun expectedLicence(): String? {
            return "Unknown"
        }

        override fun expectedLanguageInfo(): Locale? {
            return null
        }

        override fun expectedTags(): List<String?>? {
            return mutableListOf<String?>("Attribute", "Bauinformatik", "Klassen", "Objekte", "Python")
        }

        companion object {
            private const val ID = "vqABGP97fEjo7RhPuDnSZk"
            private const val INSTANCE = "https://tube.tchncs.de"

            private const val URL = INSTANCE + BASE_URL + ID
            private var extractor: StreamExtractor? = null


            @BeforeAll
            @Throws(Exception::class)
            fun setUp(): Unit {
                init(getInstance()!!)
                // setting instance might break test when running in parallel (!)
                PeerTube.instance = PeertubeInstance(INSTANCE, "tchncs.de")
                extractor = PeerTube.getStreamExtractor(URL)
                extractor!!.fetchPage()
            }
        }
    }

    @Test
    @Throws(Exception::class)
    fun testGetEmptyDescription() {
        val extractorEmpty: StreamExtractor =
            PeerTube.getStreamExtractor("https://framatube.org/api/v1/videos/d5907aad-2252-4207-89ec-a4b687b9337d")
        extractorEmpty.fetchPage()
        Assertions.assertEquals("", extractorEmpty.description!!.content)
    }

    @Test
    @Throws(Exception::class)
    fun testGetSmallDescription() {
        val extractorSmall: StreamExtractor =
            PeerTube.getStreamExtractor("https://peertube.cpy.re/videos/watch/d2a5ec78-5f85-4090-8ec5-dc1102e022ea")
        extractorSmall.fetchPage()
        Assertions.assertEquals("https://www.kickstarter.com/projects/1587081065/nothing-to-hide-the-documentary",
            extractorSmall.description!!.content)
    }

    @Test
    @Throws(ExtractionException::class, IOException::class)
    fun testGetSupportInformation() {
        val supportInfoExtractor: StreamExtractor =
            PeerTube.getStreamExtractor("https://framatube.org/videos/watch/ee408ec8-07cd-4e35-b884-fb681a4b9d37")
        supportInfoExtractor.fetchPage()
        Assertions.assertEquals("https://utip.io/chatsceptique", supportInfoExtractor.supportInfo)
    }

    companion object {
        private const val BASE_URL = "/videos/watch/"


        @BeforeAll
        @Throws(Exception::class)
        fun setUp(): Unit {
            init(getInstance()!!)
            PeerTube.instance = PeertubeInstance("https://peertube.cpy.re", "PeerTube test server")
        }
    }
}
