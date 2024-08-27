/*
 * Created by Christian Schabesberger on 30.12.15.
 *
 * Copyright (C) 2015 Christian Schabesberger <chris.schabesberger@mailbox.org>
* Copyright (C) 2024 Xilin Jia <https://github.com/XilinJia>
 * YoutubeVideoExtractorDefault.kt is part of Vista Guide.
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
package ac.mdiq.vista.extractor.services.youtube.stream

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import ac.mdiq.vista.downloader.DownloaderFactory
import ac.mdiq.vista.downloader.DownloaderFactory.getDownloader
import ac.mdiq.vista.downloader.DownloaderTestImpl.Companion.getInstance
import ac.mdiq.vista.extractor.MetaInfo
import ac.mdiq.vista.extractor.Vista.init
import ac.mdiq.vista.extractor.ServiceList.YouTube
import ac.mdiq.vista.extractor.StreamingService
import ac.mdiq.vista.extractor.exceptions.*
import ac.mdiq.vista.extractor.services.DefaultStreamExtractorTest
import ac.mdiq.vista.extractor.services.youtube.YoutubeTestsUtils
import ac.mdiq.vista.extractor.services.youtube.extractors.YoutubeStreamExtractor
import ac.mdiq.vista.extractor.stream.*
import java.io.IOException
import java.net.MalformedURLException
import java.net.URL
import java.util.*

object YoutubeStreamExtractorDefaultTest {
    private const val RESOURCE_PATH = DownloaderFactory.RESOURCE_PATH + "services/youtube/extractor/stream/"
    const val BASE_URL: String = "https://www.youtube.com/watch?v="
    const val YOUTUBE_LICENCE: String = "YouTube licence"

    class NotAvailable {
        @Test
        @Throws(Exception::class)
        fun geoRestrictedContent() {
            val extractor: StreamExtractor = YouTube.getStreamExtractor(BASE_URL + "_PL2HJKxnOM")
            Assertions.assertThrows(GeographicRestrictionException::class.java) { extractor.fetchPage() }
        }

        @Test
        @Throws(Exception::class)
        fun nonExistentFetch() {
            val extractor: StreamExtractor = YouTube.getStreamExtractor(BASE_URL + "don-t-exist")
            Assertions.assertThrows(ContentNotAvailableException::class.java) { extractor.fetchPage() }
        }

        @Test
        @Throws(Exception::class)
        fun invalidId() {
            val extractor: StreamExtractor = YouTube.getStreamExtractor(BASE_URL + "INVALID_ID_INVALID_ID")
            Assertions.assertThrows(ParsingException::class.java
            ) { extractor.fetchPage() }
        }

        @Test
        @Throws(Exception::class)
        fun paidContent() {
            val extractor: StreamExtractor =
                YouTube.getStreamExtractor(BASE_URL + "ayI2iBwGdxw")
            Assertions.assertThrows(PaidContentException::class.java) { extractor.fetchPage() }
        }

        @Test
        @Throws(Exception::class)
        fun privateContent() {
            val extractor: StreamExtractor =
                YouTube.getStreamExtractor(BASE_URL + "8VajtrESJzA")
            Assertions.assertThrows(PrivateContentException::class.java
            ) { extractor.fetchPage() }
        }

        @Test
        @Throws(Exception::class)
        fun youtubeMusicPremiumContent() {
            val extractor: StreamExtractor =
                YouTube.getStreamExtractor(BASE_URL + "sMJ8bRN2dak")
            Assertions.assertThrows(YoutubeMusicPremiumContentException::class.java
            ) { extractor.fetchPage() }
        }

        companion object {

            @BeforeAll
            @Throws(IOException::class)
            fun setUp(): Unit {
                YoutubeTestsUtils.ensureStateless()
                init(getDownloader(RESOURCE_PATH + "notAvailable"))
            }
        }
    }

    class DescriptionTestPewdiepie : DefaultStreamExtractorTest() {
        // @formatter:off
        override fun extractor(): StreamExtractor {
            return extractor!!}
        override fun expectedService(): StreamingService {
            return YouTube}
        override fun expectedName(): String? {
            return "Marzia & Felix - Wedding 19.08.2019"}
        override fun expectedId(): String? {
            return ID}
        override fun expectedUrlContains(): String? {
            return BASE_URL + ID}
        override fun expectedOriginalUrlContains(): String? {
            return URL}

        override fun expectedStreamType(): StreamType? {
            return StreamType.VIDEO_STREAM}
        override fun expectedUploaderName(): String? {
            return "PewDiePie"}
        override fun expectedUploaderUrl(): String? {
            return "https://www.youtube.com/channel/UC-lHJZR3Gqxm24_Vd_AJ5Yw"}
        override fun expectedUploaderSubscriberCountAtLeast(): Long {
            return 110000000}
        override fun expectedDescriptionContains(): List<String?> {
            return mutableListOf<String?>("https://www.youtube.com/channel/UC7l23W7gFi4Uho6WSzckZRA",
            "https://www.handcraftpictures.com/")
        }
        override fun expectedUploaderVerified(): Boolean {
            return true}
        override fun expectedLength(): Long {
            return 381}
        override fun expectedTimestamp(): Long {
            return TIMESTAMP.toLong()}
        override fun expectedViewCountAtLeast(): Long {
            return 26682500}
        override fun expectedUploadDate(): String? {
            return "2019-08-24 15:39:57.000"}
        override fun expectedTextualUploadDate(): String? {
            return "2019-08-24T08:39:57-07:00"}
        override fun expectedLikeCountAtLeast(): Long {
            return 5212900}
        override fun expectedDislikeCountAtLeast(): Long {
            return -1}
        override fun expectedStreamSegmentsCount(): Int {
            return 0}
        override fun expectedLicence(): String? {
            return YOUTUBE_LICENCE}
        override fun expectedCategory(): String? {
            return "Entertainment"
        } // @formatter:on

        companion object {
            private const val ID = "7PIMiDcwNvc"
            private const val TIMESTAMP = 7483
            private const val URL = BASE_URL + ID + "&t=" + TIMESTAMP + "s"
            private var extractor: StreamExtractor? = null


            @BeforeAll
            @Throws(Exception::class)
            fun setUp(): Unit {
                YoutubeTestsUtils.ensureStateless()
                init(getDownloader(RESOURCE_PATH + "pewdiepie"))
                extractor = YouTube.getStreamExtractor(URL)
                extractor!!.fetchPage()
            }
        }
    }

    class DescriptionTestUnboxing : DefaultStreamExtractorTest() {
        // @formatter:off
        override fun extractor(): StreamExtractor {
            return extractor!!}
        override fun expectedService(): StreamingService {
            return YouTube}
        override fun expectedName(): String? {
            return "This Smartphone Changes Everything..."}
        override fun expectedId(): String? {
            return ID}
        override fun expectedUrlContains(): String? {
            return URL}
        override fun expectedOriginalUrlContains(): String? {
            return URL}

        override fun expectedStreamType(): StreamType? {
            return StreamType.VIDEO_STREAM}
        override fun expectedUploaderName(): String? {
            return "Unbox Therapy"}
        override fun expectedUploaderUrl(): String? {
            return "https://www.youtube.com/channel/UCsTcErHg8oDvUnTzoqsYeNw"}
        override fun expectedUploaderSubscriberCountAtLeast(): Long {
            return 18000000}
        override fun expectedDescriptionContains(): List<String?> {
            return mutableListOf<String?>("https://www.youtube.com/watch?v=X7FLCHVXpsA&amp;list=PL7u4lWXQ3wfI_7PgX0C-VTiwLeu0S4v34",
            "https://www.youtube.com/watch?v=Lqv6G0pDNnw&amp;list=PL7u4lWXQ3wfI_7PgX0C-VTiwLeu0S4v34",
            "https://www.youtube.com/watch?v=XxaRBPyrnBU&amp;list=PL7u4lWXQ3wfI_7PgX0C-VTiwLeu0S4v34",
            "https://www.youtube.com/watch?v=U-9tUEOFKNU&amp;list=PL7u4lWXQ3wfI_7PgX0C-VTiwLeu0S4v34")
        }
        override fun expectedLength(): Long {
            return 434}
        override fun expectedViewCountAtLeast(): Long {
            return 21229200}
        override fun expectedUploadDate(): String? {
            return "2018-06-19 19:41:34.000"}
        override fun expectedTextualUploadDate(): String? {
            return "2018-06-19T12:41:34-07:00"}
        override fun expectedLikeCountAtLeast(): Long {
            return 340100}
        override fun expectedDislikeCountAtLeast(): Long {
            return -1}
        override fun expectedUploaderVerified(): Boolean {
            return true}
        override fun expectedLicence(): String? {
            return YOUTUBE_LICENCE}
        override fun expectedCategory(): String? {
            return "Science & Technology"}
        override fun expectedTags(): List<String?>? {
            return mutableListOf<String?>("2018", "8 plus", "apple", "apple iphone", "apple iphone x", "best", "best android",
            "best smartphone", "cool gadgets", "find", "find x", "find x review", "find x unboxing", "findx",
            "galaxy s9", "galaxy s9+", "hands on", "iphone 8", "iphone 8 plus", "iphone x", "new iphone", "nex",
            "oneplus 6", "oppo", "oppo find x", "oppo find x hands on", "oppo find x review",
            "oppo find x unboxing", "oppo findx", "pixel 2 xl", "review", "samsung", "samsung galaxy",
            "samsung galaxy s9", "smartphone", "unbox therapy", "unboxing", "vivo", "vivo apex", "vivo nex")
        } // @formatter:on

        companion object {
            private const val ID = "cV5TjZCJkuA"
            private const val URL = BASE_URL + ID
            private var extractor: StreamExtractor? = null


            @BeforeAll
            @Throws(Exception::class)
            fun setUp(): Unit {
                YoutubeTestsUtils.ensureStateless()
                init(getDownloader(RESOURCE_PATH + "unboxing"))
                extractor = YouTube.getStreamExtractor(URL)
                extractor!!.fetchPage()
            }
        }
    }

    class RatingsDisabledTest : DefaultStreamExtractorTest() {
        // @formatter:off
        override fun extractor(): StreamExtractor {
            return extractor!!}
        override fun expectedService(): StreamingService {
            return YouTube}
        override fun expectedName(): String? {
            return "Introduction to Doodle for Google 2023"}
        override fun expectedId(): String? {
            return ID}
        override fun expectedUrlContains(): String? {
            return BASE_URL + ID}
        override fun expectedOriginalUrlContains(): String? {
            return URL}

        override fun expectedStreamType(): StreamType? {
            return StreamType.VIDEO_STREAM}
        override fun expectedUploaderName(): String? {
            return "GoogleDoodles"}
        override fun expectedUploaderUrl(): String? {
            return "https://www.youtube.com/channel/UCdq61m8s_48EhJ5OM_MCeGw"}
        override fun expectedUploaderSubscriberCountAtLeast(): Long {
            return 2200000}
        override fun expectedDescriptionContains(): List<String?> {
            return mutableListOf<String?>("Doodle", "Google", "video")}
        override fun expectedLength(): Long {
            return 145}
        override fun expectedTimestamp(): Long {
            return TIMESTAMP.toLong()}
        override fun expectedViewCountAtLeast(): Long {
            return 40000}
        override fun expectedUploadDate(): String? {
            return "2023-01-13 21:53:57.000"}
        override fun expectedTextualUploadDate(): String? {
            return "2023-01-13T13:53:57-08:00"}
        override fun expectedLikeCountAtLeast(): Long {
            return -1}
        override fun expectedDislikeCountAtLeast(): Long {
            return -1}
        override fun expectedLicence(): String? {
            return YOUTUBE_LICENCE}
        override fun expectedCategory(): String? {
            return "Education"
        } // @formatter:on

        companion object {
            private const val ID = "it3OtbTxQk0"
            private const val TIMESTAMP = 17
            private const val URL = BASE_URL + ID + "&t=" + TIMESTAMP
            private var extractor: StreamExtractor? = null


            @BeforeAll
            @Throws(Exception::class)
            fun setUp(): Unit {
                YoutubeTestsUtils.ensureStateless()
                init(getDownloader(RESOURCE_PATH + "ratingsDisabled"))
                extractor = YouTube.getStreamExtractor(URL)
                extractor!!.fetchPage()
            }
        }
    }

    class StreamSegmentsTestTagesschau : DefaultStreamExtractorTest() {
        // @formatter:off
        override fun extractor(): StreamExtractor {
            return extractor!!}
        override fun expectedService(): StreamingService {
            return YouTube}
        override fun expectedName(): String? {
            return "tagesschau 20:00 Uhr, 17.03.2021"}
        override fun expectedId(): String? {
            return ID}
        override fun expectedUrlContains(): String? {
            return BASE_URL + ID}
        override fun expectedOriginalUrlContains(): String? {
            return URL}

        override fun expectedStreamType(): StreamType? {
            return StreamType.VIDEO_STREAM}
        override fun expectedUploaderName(): String? {
            return "tagesschau"}
        override fun expectedUploaderUrl(): String? {
            return "https://www.youtube.com/channel/UC5NOEUbkLheQcaaRldYW5GA"}
        override fun expectedUploaderSubscriberCountAtLeast(): Long {
            return 1000000}
        override fun expectedUploaderVerified(): Boolean {
            return true}
        override fun expectedDescriptionContains(): List<String?> {
            return mutableListOf<String?>("Themen der Sendung", "07:15", "Wetter", "Sendung nachträglich bearbeitet")
        }
        override fun expectedLength(): Long {
            return 953}
        override fun expectedViewCountAtLeast(): Long {
            return 270000}
        override fun expectedUploadDate(): String? {
            return "2021-03-17 19:56:59.000"}
        override fun expectedTextualUploadDate(): String? {
            return "2021-03-17T12:56:59-07:00"}
        override fun expectedLikeCountAtLeast(): Long {
            return 2300}
        override fun expectedDislikeCountAtLeast(): Long {
            return -1}
        override fun expectedHasSubtitles(): Boolean {
            return false}
        override fun expectedStreamSegmentsCount(): Int {
            return 13}
        override fun expectedLicence(): String? {
            return YOUTUBE_LICENCE}
        override fun expectedCategory(): String? {
            return "News & Politics"}

         // @formatter:on
         @Test
         @Throws(Exception::class)
         fun testStreamSegment0() {
             val segment = extractor!!.streamSegments!![0]
             Assertions.assertEquals(0, segment.startTimeSeconds)
             Assertions.assertEquals("Guten Abend", segment.title)
             Assertions.assertEquals(BASE_URL + ID + "?t=0", segment.url)
             Assertions.assertNotNull(segment.previewUrl)
         }

        @Test
        @Throws(Exception::class)
        fun testStreamSegment3() {
            val segment = extractor!!.streamSegments!![3]
            Assertions.assertEquals(224, segment.startTimeSeconds)
            Assertions.assertEquals("Pandemie dämpft Konjunkturprognose für 2021", segment.title)
            Assertions.assertEquals(BASE_URL + ID + "?t=224", segment.url)
            Assertions.assertNotNull(segment.previewUrl)
        }

        companion object {
            // StreamSegment example with single macro-makers panel
            private const val ID = "KI7fMGRg0Wk"
            private const val URL = BASE_URL + ID
            private var extractor: StreamExtractor? = null


            @BeforeAll
            @Throws(Exception::class)
            fun setUp(): Unit {
                YoutubeTestsUtils.ensureStateless()
                init(getDownloader(RESOURCE_PATH + "streamSegmentsMaithinkX"))
                extractor = YouTube.getStreamExtractor(URL)
                extractor!!.fetchPage()
            }
        }
    }

    class StreamSegmentsTestMaiLab : DefaultStreamExtractorTest() {
        // @formatter:off
        override fun extractor(): StreamExtractor {
            return extractor!!}
        override fun expectedService(): StreamingService {
            return YouTube}
        override fun expectedName(): String? {
            return "Vitamin D wissenschaftlich gepr\u00fcft"}
        override fun expectedId(): String? {
            return ID}
        override fun expectedUrlContains(): String? {
            return BASE_URL + ID}
        override fun expectedOriginalUrlContains(): String? {
            return URL}

        override fun expectedStreamType(): StreamType? {
            return StreamType.VIDEO_STREAM}
        override fun expectedUploaderName(): String? {
            return "MAITHINK X"}
        override fun expectedUploaderUrl(): String? {
            return "https://www.youtube.com/channel/UCyHDQ5C6z1NDmJ4g6SerW8g"}
        override fun expectedUploaderSubscriberCountAtLeast(): Long {
            return 1400000}
        override fun expectedDescriptionContains(): List<String?> {
            return mutableListOf<String?>("Vitamin", "2:44", "Was ist Vitamin D?")}
        override fun expectedUploaderVerified(): Boolean {
            return true}
        override fun expectedLength(): Long {
            return 1010}
        override fun expectedViewCountAtLeast(): Long {
            return 815500}
        override fun expectedUploadDate(): String? {
            return "2020-11-19 05:30:01.000"}
        override fun expectedTextualUploadDate(): String? {
            return "2020-11-18T21:30:01-08:00"}
        override fun expectedLikeCountAtLeast(): Long {
            return 48500}
        override fun expectedDislikeCountAtLeast(): Long {
            return -1}
        override fun expectedStreamSegmentsCount(): Int {
            return 7}
        override fun expectedLicence(): String? {
            return YOUTUBE_LICENCE}
        override fun expectedCategory(): String? {
            return "Science & Technology"}
        override fun expectedTags(): List<String?>? {
            return mutableListOf<String?>("Diabetes", "Erkältung", "Gesundheit", "Immunabwehr", "Immunsystem", "Infektion",
            "Komisch alles chemisch", "Krebs", "Lab", "Lesch", "Mai", "Mai Thi", "Mai Thi Nguyen-Kim",
            "Mangel", "Nahrungsergänzungsmittel", "Nguyen", "Nguyen Kim", "Nguyen-Kim", "Quarks", "Sommer",
            "Supplemente", "Supplements", "Tabletten", "Terra X", "TerraX", "The Secret Life Of Scientists",
            "Tropfen", "Vitamin D", "Vitamin-D-Mangel", "Vitamine", "Winter", "einnehmen", "maiLab", "nehmen",
            "supplementieren", "Überdosis", "Überschuss")
        }

         // @formatter:on
         @Test
         @Throws(Exception::class)
         fun testStreamSegment() {
             val segment = extractor!!.streamSegments!![1]
             Assertions.assertEquals(164, segment.startTimeSeconds)
             Assertions.assertEquals("Was ist Vitamin D?", segment.title)
             Assertions.assertEquals(BASE_URL + ID + "?t=164", segment.url)
             Assertions.assertNotNull(segment.previewUrl)
         }

        @Test
        @Disabled("encoding problem")
        override fun testName() {
        }

        @Test
        @Disabled("encoding problem")
        override fun testTags() {
        }

        companion object {
            // StreamSegment example with macro-makers panel and transcription panel
            private const val ID = "ud9d5cMDP_0"
            private const val URL = BASE_URL + ID
            private var extractor: StreamExtractor? = null


            @BeforeAll
            @Throws(Exception::class)
            fun setUp(): Unit {
                YoutubeTestsUtils.ensureStateless()
                init(getDownloader(RESOURCE_PATH + "streamSegmentsMaiLab"))
                extractor = YouTube.getStreamExtractor(URL)
                extractor!!.fetchPage()
            }
        }
    }

    class PublicBroadcasterTest : DefaultStreamExtractorTest() {
        // @formatter:off
        override fun extractor(): StreamExtractor {
            return extractor!!}
        override fun expectedService(): StreamingService {
            return YouTube}
        override fun expectedName(): String? {
            return "Merci pour les 3 millions d'abonnés \uD83C\uDF89| ARTE"}
        override fun expectedId(): String? {
            return ID}
        override fun expectedUrlContains(): String? {
            return BASE_URL + ID}
        override fun expectedOriginalUrlContains(): String? {
            return URL}

        override fun expectedStreamType(): StreamType? {
            return StreamType.VIDEO_STREAM}
        override fun expectedUploaderName(): String? {
            return "ARTE"}
        override fun expectedUploaderUrl(): String? {
            return "https://www.youtube.com/channel/UCwI-JbGNsojunnHbFAc0M4Q"}
        override fun expectedUploaderSubscriberCountAtLeast(): Long {
            return 3000000}
        override fun expectedDescriptionContains(): List<String?> {
            return mutableListOf<String?>("sommets", "fans", "cadeau")}
        override fun expectedLength(): Long {
            return 45}
        override fun expectedTimestamp(): Long {
            return TIMESTAMP.toLong()}
        override fun expectedViewCountAtLeast(): Long {
            return 20000}
        override fun expectedUploadDate(): String? {
            return "2023-07-07 15:30:08.000"}
        override fun expectedTextualUploadDate(): String? {
            return "2023-07-07T08:30:08-07:00"}
        override fun expectedLikeCountAtLeast(): Long {
            return 1000}
        override fun expectedDislikeCountAtLeast(): Long {
            return -1}
        @Throws(MalformedURLException::class)  override fun expectedMetaInfo(): List<MetaInfo> {
            return listOf(MetaInfo(
            "",
            Description("Arte is a French/German public broadcast service.",
            Description.PLAIN_TEXT),
            mutableListOf(URL("https://en.wikipedia.org/wiki/Arte?wprov=yicw1")),
            mutableListOf("Wikipedia")
            ))
        }
        override fun expectedUploaderVerified(): Boolean {
            return true}
        override fun expectedLicence(): String? {
            return YOUTUBE_LICENCE}
        override fun expectedCategory(): String? {
            return "News & Politics"}
        override fun expectedTags(): List<String?>? {
            return mutableListOf<String?>("arte", "arte 3 millions", "arte remerciement",
            "documentaire arte", "arte documentaire", "fan d'arte", "arte youtube")
        } // @formatter:on

        companion object {
            private const val ID = "cJ9to6EmElQ"
            private const val TIMESTAMP = 0
            private const val URL = BASE_URL + ID
            private var extractor: StreamExtractor? = null


            @BeforeAll
            @Throws(Exception::class)
            fun setUp(): Unit {
                YoutubeTestsUtils.ensureStateless()
                init(getDownloader(RESOURCE_PATH + "publicBroadcast"))
                extractor = YouTube.getStreamExtractor(URL)
                extractor!!.fetchPage()
            }
        }
    }

    class NoVisualMetadataVideoTest : DefaultStreamExtractorTest() {
        override fun expectedStreamType(): StreamType? {
            return StreamType.VIDEO_STREAM
        }

        override fun expectedUploaderName(): String? {
            return "Makani"
        }

        override fun expectedUploaderUrl(): String? {
            return "https://www.youtube.com/channel/UC-iMZJ8NppwT2fLwzFWJKOQ"
        }

        override fun expectedDescriptionContains(): List<String?> {
            return mutableListOf<String?>("Makani", "prototype", "rotors")
        }

        override fun expectedLength(): Long {
            return 175
        }

        override fun expectedViewCountAtLeast(): Long {
            return 88000
        }

        override fun expectedUploadDate(): String? {
            return "2017-05-16 14:50:53.000"
        }

        override fun expectedTextualUploadDate(): String? {
            return "2017-05-16T07:50:53-07:00"
        }

        override fun expectedLikeCountAtLeast(): Long {
            return -1
        }

        override fun expectedDislikeCountAtLeast(): Long {
            return -1
        }

        override fun extractor(): StreamExtractor {
            return extractor!!
        }

        override fun expectedService(): StreamingService {
            return YouTube
        }

        override fun expectedName(): String? {
            return "Makani’s first commercial-scale energy kite"
        }

        override fun expectedId(): String? {
            return "An8vtD1FDqs"
        }

        override fun expectedUrlContains(): String? {
            return BASE_URL + ID
        }

        override fun expectedOriginalUrlContains(): String? {
            return URL
        }

        override fun expectedCategory(): String? {
            return "Science & Technology"
        }

        override fun expectedLicence(): String? {
            return YOUTUBE_LICENCE
        }

        override fun expectedTags(): List<String?>? {
            return mutableListOf<String?>("Makani", "Moonshot", "Moonshot Factory", "Prototyping",
                "california", "california wind", "clean", "clean energy", "climate change",
                "climate crisis", "energy", "energy kite", "google", "google x", "green",
                "green energy", "kite", "kite power", "kite power solutions",
                "kite power systems", "makani power", "power", "renewable", "renewable energy",
                "renewable energy engineering", "renewable energy projects",
                "renewable energy sources", "renewables", "solutions", "tech", "technology",
                "turbine", "wind", "wind energy", "wind power", "wind turbine", "windmill")
        }

        @Test
        override fun testSubscriberCount() {
            Assertions.assertThrows(ParsingException::class.java
            ) { extractor!!.uploaderSubscriberCount }
        }

        @Test
        override fun testLikeCount() {
            Assertions.assertThrows(ParsingException::class.java
            ) { extractor!!.likeCount }
        }

        @Test
        override fun testUploaderAvatars() {
            Assertions.assertThrows(ParsingException::class.java
            ) { extractor!!.uploaderAvatars }
        }

        companion object {
            // Video without visual metadata on YouTube clients (video title, upload date, channel name,
            // comments, ...)
            private const val ID = "An8vtD1FDqs"
            private const val URL = BASE_URL + ID
            private var extractor: StreamExtractor? = null


            @BeforeAll
            @Throws(Exception::class)
            fun setUp(): Unit {
                YoutubeTestsUtils.ensureStateless()
                init(getDownloader(RESOURCE_PATH + "noVisualMetadata"))
                extractor = YouTube.getStreamExtractor(URL)
                extractor!!.fetchPage()
            }
        }
    }

    class UnlistedTest {
        @Test
        fun testGetUnlisted() {
            Assertions.assertEquals(StreamExtractor.Privacy.UNLISTED, extractor!!.privacy)
        }

        companion object {
            private var extractor: YoutubeStreamExtractor? = null


            @BeforeAll
            @Throws(Exception::class)
            fun setUp(): Unit {
                YoutubeTestsUtils.ensureStateless()
                init(getDownloader(RESOURCE_PATH + "unlistedDefaultTest"))
                extractor = YouTube.getStreamExtractor("https://www.youtube.com/watch?v=tjz2u2DiveM") as? YoutubeStreamExtractor
                extractor!!.fetchPage()
            }
        }
    }

    class CCLicensed {
        @Test
        @Throws(ParsingException::class)
        fun testGetLicence() {
            Assertions.assertEquals("Creative Commons Attribution licence (reuse allowed)", extractor!!.licence)
        }

        companion object {
            private const val ID = "M4gD1WSo5mA"
            private const val URL = BASE_URL + ID
            private var extractor: StreamExtractor? = null


            @BeforeAll
            @Throws(Exception::class)
            fun setUp(): Unit {
                YoutubeTestsUtils.ensureStateless()
                init(getDownloader(RESOURCE_PATH + "ccLicensed"))
                extractor = YouTube.getStreamExtractor(URL)
                extractor!!.fetchPage()
            }
        }
    }

    class AudioTrackLanguage {
        @Test
        @Throws(Exception::class)
        fun testCheckAudioStreams() {
            val audioStreams = extractor!!.audioStreams
            Assertions.assertFalse(audioStreams!!.isEmpty())

            for (stream in audioStreams) {
                Assertions.assertNotNull(stream.audioTrackName)
            }

            Assertions.assertTrue(audioStreams.stream()
                .anyMatch { audioStream: AudioStream -> "English original" == audioStream.audioTrackName })

            val hindiLocale = Locale.forLanguageTag("hi")
            Assertions.assertTrue(audioStreams.stream()
                .anyMatch { audioStream: AudioStream -> audioStream.audioLocale == hindiLocale })
        }

        companion object {
            private const val ID = "kX3nB4PpJko"
            private const val URL = BASE_URL + ID
            private var extractor: StreamExtractor? = null


            @BeforeAll
            @Throws(Exception::class)
            fun setUp(): Unit {
                YoutubeTestsUtils.ensureStateless()
                init(getDownloader(RESOURCE_PATH + "audioTrack"))
                extractor = YouTube.getStreamExtractor(URL)
                extractor!!.fetchPage()
            }
        }
    }

    class AudioTrackTypes {
        @Test
        @Throws(Exception::class)
        fun testCheckOriginalAudio() {
            assertFalse(extractor!!.audioStreams!!.isEmpty())

            assertTrue(extractor!!.audioStreams!!
                .stream()
                .anyMatch { s -> s.audioTrackType === AudioTrackType.ORIGINAL })
        }

        @Test
        @Throws(Exception::class)
        fun testCheckDubbedAudio() {
            assertTrue(extractor!!.audioStreams!!
                .stream()
                .anyMatch { s -> s.audioTrackType === AudioTrackType.DUBBED })
        }

        @Test
        @Throws(Exception::class)
        fun testCheckDescriptiveAudio() {
            assertTrue(extractor!!.audioStreams!!
                .stream()
                .anyMatch { s -> s.audioTrackType === AudioTrackType.DESCRIPTIVE })
        }

        companion object {
            private const val ID = "Kn56bMZ9OE8"
            private const val URL = BASE_URL + ID
            private var extractor: StreamExtractor? = null


            @BeforeAll
            @Throws(Exception::class)
            fun setUp(): Unit {
                YoutubeTestsUtils.ensureStateless()
                init(getDownloader(RESOURCE_PATH + "audioTrackType"))
                extractor = YouTube.getStreamExtractor(URL)
                extractor!!.fetchPage()
            }
        }
    }
}
