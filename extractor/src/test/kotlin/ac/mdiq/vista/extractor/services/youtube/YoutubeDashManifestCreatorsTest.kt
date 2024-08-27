package ac.mdiq.vista.extractor.services.youtube

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.Executable
import ac.mdiq.vista.downloader.DownloaderTestImpl.Companion.getInstance
import ac.mdiq.vista.extractor.ExtractorAsserts
import ac.mdiq.vista.extractor.Vista.init
import ac.mdiq.vista.extractor.ServiceList.YouTube
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.setNumberGenerator
import ac.mdiq.vista.extractor.services.youtube.dashmanifestcreators.CreationException
import ac.mdiq.vista.extractor.services.youtube.dashmanifestcreators.YoutubeDashManifestCreatorsUtils.ADAPTATION_SET
import ac.mdiq.vista.extractor.services.youtube.dashmanifestcreators.YoutubeDashManifestCreatorsUtils.AUDIO_CHANNEL_CONFIGURATION
import ac.mdiq.vista.extractor.services.youtube.dashmanifestcreators.YoutubeDashManifestCreatorsUtils.INITIALIZATION
import ac.mdiq.vista.extractor.services.youtube.dashmanifestcreators.YoutubeDashManifestCreatorsUtils.MPD
import ac.mdiq.vista.extractor.services.youtube.dashmanifestcreators.YoutubeDashManifestCreatorsUtils.PERIOD
import ac.mdiq.vista.extractor.services.youtube.dashmanifestcreators.YoutubeDashManifestCreatorsUtils.REPRESENTATION
import ac.mdiq.vista.extractor.services.youtube.dashmanifestcreators.YoutubeDashManifestCreatorsUtils.ROLE
import ac.mdiq.vista.extractor.services.youtube.dashmanifestcreators.YoutubeDashManifestCreatorsUtils.SEGMENT_BASE
import ac.mdiq.vista.extractor.services.youtube.dashmanifestcreators.YoutubeDashManifestCreatorsUtils.SEGMENT_TEMPLATE
import ac.mdiq.vista.extractor.services.youtube.dashmanifestcreators.YoutubeDashManifestCreatorsUtils.SEGMENT_TIMELINE
import ac.mdiq.vista.extractor.services.youtube.dashmanifestcreators.YoutubeOtfDashManifestCreator.fromOtfStreamingUrl
import ac.mdiq.vista.extractor.services.youtube.dashmanifestcreators.YoutubeProgressiveDashManifestCreator.fromProgressiveStreamingUrl
import ac.mdiq.vista.extractor.services.youtube.extractors.YoutubeStreamExtractor
import ac.mdiq.vista.extractor.services.youtube.stream.YoutubeStreamExtractorDefaultTest.BASE_URL
import ac.mdiq.vista.extractor.stream.AudioTrackType
import ac.mdiq.vista.extractor.stream.DeliveryMethod
import ac.mdiq.vista.extractor.stream.Stream
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.xml.sax.InputSource
import java.io.StringReader
import java.util.*
import java.util.function.Consumer
import java.util.stream.Collectors
import java.util.stream.IntStream

import javax.xml.parsers.DocumentBuilderFactory

/**
 * Test for YouTube DASH manifest creators.
 *
 * Tests the generation of OTF and progressive manifests.
 *
 * We cannot test the generation of DASH manifests for ended livestreams because these videos will
 * be re-encoded as normal videos later, so we can't use a specific video.
 *
 * The generation of DASH manifests for OTF streams, which can be tested, uses a video licenced
 * under the Creative Commons Attribution licence (reuse allowed): `A New Era of Open?
 * COVID-19 and the Pursuit for Equitable Solutions` ([https://www.youtube.com/watch?v=DJ8GQUNUXGM](https://www.youtube.com/watch?v=DJ8GQUNUXGM))
 *
 * We couldn't use mocks for these tests because the streaming URLs needs to fetched and the IP
 * address used to get these URLs is required (used as a param in the URLs; without it, video
 * servers return 403/Forbidden HTTP response code).
 *
 * So the real downloader will be used everytime on this test class.
 *
 */
internal class YoutubeDashManifestCreatorsTest {
    @Test
    @Throws(Exception::class)
    fun testOtfStreams() {
        assertDashStreams(extractor!!.videoOnlyStreams)
        assertDashStreams(extractor!!.audioStreams)

        // no video stream with audio uses the DASH delivery method (YouTube OTF stream type)
        Assertions.assertEquals(0, assertFilterStreams(extractor!!.videoStreams, DeliveryMethod.DASH).size)
    }

    @Test
    @Throws(Exception::class)
    fun testProgressiveStreams() {
        assertProgressiveStreams(extractor!!.videoOnlyStreams)
        assertProgressiveStreams(extractor!!.audioStreams)

        // we are not able to generate DASH manifests of video formats with audio
        Assertions.assertThrows(CreationException::class.java) { assertProgressiveStreams(extractor!!.videoStreams) }
    }

    @Throws(Exception::class)
    private fun assertDashStreams(streams: List<Stream?>) {
        for (stream in assertFilterStreams(streams, DeliveryMethod.DASH)) {
            if (stream == null) continue
            val itagItem_ = stream.getItagItem() ?: throw Exception("stream itagItem is null")
            val manifest = fromOtfStreamingUrl(stream.content, itagItem_, videoLength)
            ExtractorAsserts.assertNotBlank(manifest)
            assertManifestGenerated(manifest, stream.getItagItem()!!) { document: Document ->
                Assertions.assertAll(
                    Executable { assertSegmentTemplateElement(document) },
                    Executable { assertSegmentTimelineAndSElements(document) }
                )
            }
        }
    }

    @Throws(Exception::class)
    private fun assertProgressiveStreams(streams: List<Stream?>) {
        for (stream in assertFilterStreams(streams, DeliveryMethod.PROGRESSIVE_HTTP)) {
            val manifest = fromProgressiveStreamingUrl(stream!!.content, stream.getItagItem()!!, videoLength)
            ExtractorAsserts.assertNotBlank(manifest)

            assertManifestGenerated(manifest, stream.getItagItem()!!) { document: Document ->
                Assertions.assertAll(
                    Executable { assertBaseUrlElement(document) },
                    Executable { assertSegmentBaseElement(document, stream.getItagItem()!!) },
                    Executable { assertInitializationElement(document, stream.getItagItem()!!) }
                )
            }
        }
    }


    private fun assertFilterStreams(streams: List<Stream?>, deliveryMethod: DeliveryMethod): List<Stream?> {
        val filteredStreams = streams.stream()
            .filter { stream: Stream? -> stream!!.deliveryMethod == deliveryMethod }
            .limit(MAX_STREAMS_TO_TEST_PER_METHOD.toLong())
            .collect(Collectors.toList())

        Assertions.assertAll(filteredStreams.stream()
            .flatMap { stream: Stream? ->
                java.util.stream.Stream.of(
                    Executable { ExtractorAsserts.assertNotBlank(stream!!.content) },
                    Executable { Assertions.assertNotNull(stream?.getItagItem()) }
                )
            }
        )

        return filteredStreams
    }

    @Throws(Exception::class)
    private fun assertManifestGenerated(dashManifest: String, itagItem: ItagItem, additionalAsserts: Consumer<Document>) {
        val documentBuilderFactory = DocumentBuilderFactory.newInstance()
        val documentBuilder = documentBuilderFactory.newDocumentBuilder()
        val document = documentBuilder.parse(InputSource(StringReader(dashManifest)))

        Assertions.assertAll(
            Executable { assertMpdElement(document) },
            Executable { assertPeriodElement(document) },
            Executable { assertAdaptationSetElement(document, itagItem) },
            Executable { assertRoleElement(document, itagItem) },
            Executable { assertRepresentationElement(document, itagItem) },
            Executable { if (itagItem.itagType == ItagItem.ItagType.AUDIO) assertAudioChannelConfigurationElement(document, itagItem) },
            Executable { additionalAsserts.accept(document) }
        )
    }

    private fun assertMpdElement(document: Document) {
        val element = document.getElementsByTagName(MPD).item(0) as Element
        Assertions.assertNotNull(element)
        Assertions.assertNull(element.parentNode.nodeValue)

        val mediaPresentationDuration = element.getAttribute("mediaPresentationDuration")
        Assertions.assertNotNull(mediaPresentationDuration)
        Assertions.assertTrue(mediaPresentationDuration.startsWith("PT"))
    }

    private fun assertPeriodElement(document: Document) {
        assertGetElement(document, PERIOD, MPD)
    }

    private fun assertAdaptationSetElement(document: Document, itagItem: ItagItem) {
        val element = assertGetElement(document, ADAPTATION_SET, PERIOD)
        assertAttrEquals(itagItem.mediaFormat.mimeType, element, "mimeType")

        if (itagItem.itagType == ItagItem.ItagType.AUDIO) {
            val itagAudioLocale = itagItem.audioLocale
            if (itagAudioLocale != null) assertAttrEquals(itagAudioLocale.language, element, "lang")
        }
    }

    private fun assertRoleElement(document: Document, itagItem: ItagItem) {
        val element = assertGetElement(document, ROLE, ADAPTATION_SET)
        val expect = if (itagItem.audioTrackType == null) "main"
        else {
            when (itagItem.audioTrackType) {
                AudioTrackType.ORIGINAL -> "main"
                AudioTrackType.DUBBED -> "dub"
                AudioTrackType.DESCRIPTIVE -> "description"
                else -> "alternate"
            }
        }

        assertAttrEquals(expect, element, "value")
    }

    private fun assertRepresentationElement(document: Document, itagItem: ItagItem) {
        val element = assertGetElement(document, REPRESENTATION, ADAPTATION_SET)

        assertAttrEquals(itagItem.bitrate, element, "bandwidth")
        assertAttrEquals(itagItem.codec, element, "codecs")

        if (itagItem.itagType == ItagItem.ItagType.VIDEO_ONLY || itagItem.itagType == ItagItem.ItagType.VIDEO) {
            assertAttrEquals(itagItem.getFps(), element, "frameRate")
            assertAttrEquals(itagItem.height, element, "height")
            assertAttrEquals(itagItem.width, element, "width")
        }

        assertAttrEquals(itagItem.id, element, "id")
    }

    private fun assertAudioChannelConfigurationElement(document: Document, itagItem: ItagItem) {
        val element = assertGetElement(document, AUDIO_CHANNEL_CONFIGURATION, REPRESENTATION)
        assertAttrEquals(itagItem.getAudioChannels(), element, "value")
    }

    private fun assertSegmentTemplateElement(document: Document) {
        val element = assertGetElement(document, SEGMENT_TEMPLATE, REPRESENTATION)

        val initializationValue = element.getAttribute("initialization")
        ExtractorAsserts.assertIsValidUrl(initializationValue)
        Assertions.assertTrue(initializationValue.endsWith("&sq=0"))

        val mediaValue = element.getAttribute("media")
        ExtractorAsserts.assertIsValidUrl(mediaValue)
        Assertions.assertTrue(mediaValue.endsWith("&sq=\$Number$"))

        Assertions.assertEquals("1", element.getAttribute("startNumber"))
    }

    private fun assertSegmentTimelineAndSElements(document: Document) {
        val element = assertGetElement(document, SEGMENT_TIMELINE, SEGMENT_TEMPLATE)
        val childNodes = element.childNodes
        ExtractorAsserts.assertGreater(0, childNodes.length.toLong())

        Assertions.assertAll(IntStream.range(0, childNodes.length)
            .mapToObj { i: Int -> childNodes.item(i) }
            .map { obj: Node? -> Element::class.java.cast(obj) }
            .flatMap { sElement: Element ->
                java.util.stream.Stream.of(
                    Executable { Assertions.assertEquals("S", sElement.tagName) },
                    Executable { ExtractorAsserts.assertGreater(0, sElement.getAttribute("d").toInt().toLong()) },
                    Executable {
                        val rValue = sElement.getAttribute("r")
                        // A segment duration can or can't be repeated, so test the next segment
                        // if there is no r attribute
                        if (!rValue.isNullOrEmpty()) ExtractorAsserts.assertGreater(0, rValue.toInt().toLong())
                    }
                )
            }
        )
    }

    private fun assertBaseUrlElement(document: Document) {
        val element = assertGetElement(document, BASE_URL, REPRESENTATION)
        ExtractorAsserts.assertIsValidUrl(element.textContent)
    }

    private fun assertSegmentBaseElement(document: Document, itagItem: ItagItem) {
        val element = assertGetElement(document, SEGMENT_BASE, REPRESENTATION)
        assertRangeEquals(itagItem.indexStart, itagItem.indexEnd, element, "indexRange")
    }

    private fun assertInitializationElement(document: Document, itagItem: ItagItem) {
        val element = assertGetElement(document, INITIALIZATION, SEGMENT_BASE)
        assertRangeEquals(itagItem.initStart, itagItem.initEnd, element, "range")
    }


    private fun assertAttrEquals(expected: Int, element: Element, attribute: String) {
        val actual = element.getAttribute(attribute).toInt()
        Assertions.assertAll(
            Executable { ExtractorAsserts.assertGreater(0, actual.toLong()) },
            Executable { Assertions.assertEquals(expected, actual) }
        )
    }

    private fun assertAttrEquals(expected: String?, element: Element, attribute: String) {
        val actual = element.getAttribute(attribute)
        Assertions.assertAll(
            Executable { ExtractorAsserts.assertNotBlank(actual) },
            Executable { Assertions.assertEquals(expected, actual) }
        )
    }

    private fun assertRangeEquals(expectedStart: Int, expectedEnd: Int, element: Element, attribute: String) {
        val range = element.getAttribute(attribute)
        ExtractorAsserts.assertNotBlank(range)
        val rangeParts = range.split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        Assertions.assertEquals(2, rangeParts.size)

        val actualStart = rangeParts[0].toInt()
        val actualEnd = rangeParts[1].toInt()

        Assertions.assertAll(
            Executable { ExtractorAsserts.assertGreaterOrEqual(0, actualStart.toLong()) },
            Executable { Assertions.assertEquals(expectedStart, actualStart) },
            Executable { ExtractorAsserts.assertGreater(0, actualEnd.toLong()) },
            Executable { Assertions.assertEquals(expectedEnd, actualEnd) }
        )
    }


    private fun assertGetElement(document: Document, tagName: String, expectedParentTagName: String): Element {
        val element = document.getElementsByTagName(tagName).item(0) as Element
        Assertions.assertNotNull(element)
        Assertions.assertTrue(element.parentNode.isEqualNode(document.getElementsByTagName(expectedParentTagName).item(0)),
            ("Element with tag name \"$tagName\" does not have a parent node with tag name \"$expectedParentTagName\""))
        return element
    }

    companion object {
        // Setting a higher number may let Google video servers return 403s
        private const val MAX_STREAMS_TO_TEST_PER_METHOD = 3
        private const val url = "https://www.youtube.com/watch?v=DJ8GQUNUXGM"
        private var extractor: YoutubeStreamExtractor? = null
        private var videoLength: Long = 0


        @BeforeAll
        @Throws(Exception::class)
        fun setUp() {
            YoutubeParsingHelper.resetClientVersion();
            setNumberGenerator(Random(1))
            init(getInstance())

            extractor = YouTube.getStreamExtractor(url) as? YoutubeStreamExtractor
            extractor!!.fetchPage()
            videoLength = extractor!!.length
        }
    }
}
