package ac.mdiq.vista.extractor.services.youtube.dashmanifestcreators

import ac.mdiq.vista.extractor.Vista.downloader
import ac.mdiq.vista.extractor.downloader.Downloader
import ac.mdiq.vista.extractor.downloader.Response
import ac.mdiq.vista.extractor.exceptions.ExtractionException
import ac.mdiq.vista.extractor.services.youtube.DeliveryType
import ac.mdiq.vista.extractor.services.youtube.ItagItem
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.clientInfoHeaders
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.getAndroidUserAgent
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.getIosUserAgent
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.isAndroidStreamingUrl
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.isIosStreamingUrl
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.isTvHtml5SimplyEmbeddedPlayerStreamingUrl
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.isWebStreamingUrl
import ac.mdiq.vista.extractor.services.youtube.dashmanifestcreators.CreationException
import ac.mdiq.vista.extractor.services.youtube.dashmanifestcreators.CreationException.Companion.couldNotAddElement
import ac.mdiq.vista.extractor.stream.AudioTrackType
import ac.mdiq.vista.extractor.utils.ManifestCreatorCache
import org.w3c.dom.DOMException
import org.w3c.dom.Document
import org.w3c.dom.Element
import java.io.IOException
import java.io.StringWriter
import java.nio.charset.StandardCharsets
import java.util.*

import javax.xml.XMLConstants
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerException
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

/**
 * Utilities and constants for YouTube DASH manifest creators.
 *
 *
 *
 * This class includes common methods of manifest creators and useful constants.
 *
 *
 *
 *
 * Generation of DASH documents and their conversion as a string is done using external classes
 * from [org.w3c.dom] and [javax.xml] packages.
 *
 */
object YoutubeDashManifestCreatorsUtils {
    /**
     * The redirect count limit that this class uses, which is the same limit as OkHttp.
     */
    const val MAXIMUM_REDIRECT_COUNT: Int = 20

    /**
     * URL parameter of the first sequence for live, post-live-DVR and OTF streams.
     */
    const val SQ_0: String = "&sq=0"

    /**
     * URL parameter of the first stream request made by official clients.
     */
    const val RN_0: String = "&rn=0"

    /**
     * URL parameter specific to web clients. When this param is added, if a redirection occurs,
     * the server will not redirect clients to the redirect URL. Instead, it will provide this URL
     * as the response body.
     */
    const val ALR_YES: String = "&alr=yes"

    // XML elements of DASH MPD manifests
    // see https://www.brendanlong.com/the-structure-of-an-mpeg-dash-mpd.html
    const val MPD: String = "MPD"
    const val PERIOD: String = "Period"
    const val ADAPTATION_SET: String = "AdaptationSet"
    const val ROLE: String = "Role"
    const val REPRESENTATION: String = "Representation"
    const val AUDIO_CHANNEL_CONFIGURATION: String = "AudioChannelConfiguration"
    const val SEGMENT_TEMPLATE: String = "SegmentTemplate"
    const val SEGMENT_TIMELINE: String = "SegmentTimeline"
    const val BASE_URL: String = "BaseURL"
    const val SEGMENT_BASE: String = "SegmentBase"
    const val INITIALIZATION: String = "Initialization"

    /**
     * Create an attribute with [Document.createAttribute], assign to it the provided
     * name and value, then add it to the provided element using [ ][Element.setAttributeNode].
     *
     * @param element element to which to add the created node
     * @param doc     document to use to create the attribute
     * @param name    name of the attribute
     * @param value   value of the attribute, will be set using [Attr.setValue]
     */

    fun setAttribute(element: Element, doc: Document, name: String?, value: String?) {
        val attr = doc.createAttribute(name)
        attr.value = value
        element.setAttributeNode(attr)
    }

    /**
     * Generate a [Document] with common manifest creator elements added to it.
     *
     * Those are:
     *
     *  * `MPD` (using [.generateDocumentAndMpdElement]);
     *  * `Period` (using [.generatePeriodElement]);
     *  * `AdaptationSet` (using [.generateAdaptationSetElement]);
     *  * `Role` (using [.generateRoleElement]);
     *  * `Representation` (using [.generateRepresentationElement]);
     *  * and, for audio streams, `AudioChannelConfiguration` (using
     * [.generateAudioChannelConfigurationElement]).
     *
     * @param itagItem the [ItagItem] associated to the stream, which must not be null
     * @param streamDuration the duration of the stream, in milliseconds
     * @return a [Document] with the common elements added in it
     */


    @Throws(CreationException::class)
    fun generateDocumentAndDoCommonElementsGeneration(itagItem: ItagItem, streamDuration: Long): Document {
        val doc = generateDocumentAndMpdElement(streamDuration)

        generatePeriodElement(doc)
        generateAdaptationSetElement(doc, itagItem)
        generateRoleElement(doc, itagItem)
        generateRepresentationElement(doc, itagItem)
        if (itagItem.itagType == ItagItem.ItagType.AUDIO) generateAudioChannelConfigurationElement(doc, itagItem)

        return doc
    }

    /**
     * Create a [Document] instance and generate the `<MPD>` element of the manifest.
     *
     * The generated `<MPD>` element looks like the manifest returned into the player
     * response of videos:
     *
     * `<MPD xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
     * xmlns="urn:mpeg:DASH:schema:MPD:2011"
     * xsi:schemaLocation="urn:mpeg:DASH:schema:MPD:2011 DASH-MPD.xsd" minBufferTime="PT1.500S"
     * profiles="urn:mpeg:dash:profile:isoff-main:2011" type="static"
     * mediaPresentationDuration="PT$duration$S">`
     * (where `$duration$` represents the duration in seconds (a number with 3 digits after
     * the decimal point)).
     *
     * @param duration the duration of the stream, in milliseconds
     * @return a [Document] instance which contains a `<MPD>` element
     */

    @Throws(CreationException::class)
    fun generateDocumentAndMpdElement(duration: Long): Document {
        try {
            val doc = newDocument()

            val mpdElement = doc.createElement(MPD)
            doc.appendChild(mpdElement)

            setAttribute(mpdElement, doc, "xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance")
            setAttribute(mpdElement, doc, "xmlns", "urn:mpeg:DASH:schema:MPD:2011")
            setAttribute(mpdElement, doc, "xsi:schemaLocation", "urn:mpeg:DASH:schema:MPD:2011 DASH-MPD.xsd")
            setAttribute(mpdElement, doc, "minBufferTime", "PT1.500S")
            setAttribute(mpdElement, doc, "profiles", "urn:mpeg:dash:profile:full:2011")
            setAttribute(mpdElement, doc, "type", "static")
            setAttribute(mpdElement, doc, "mediaPresentationDuration", String.format(Locale.ENGLISH, "PT%.3fS", duration / 1000.0))

            return doc
        } catch (e: Exception) {
            throw CreationException("Could not generate the DASH manifest or append the MPD doc to it", e)
        }
    }

    /**
     * Generate the `<Period>` element, appended as a child of the `<MPD>` element.
     *
     * The `<MPD>` element needs to be generated before this element with
     * [.generateDocumentAndMpdElement].
     *
     * @param doc the [Document] on which the `<Period>` element will be appended
     */
    @Throws(CreationException::class)
    fun generatePeriodElement(doc: Document) {
        try {
            val mpdElement = doc.getElementsByTagName(MPD).item(0) as Element
            val periodElement = doc.createElement(PERIOD)
            mpdElement.appendChild(periodElement)
        } catch (e: DOMException) {
            throw couldNotAddElement(PERIOD, e)
        }
    }

    /**
     * Generate the `<AdaptationSet>` element, appended as a child of the `<Period>`
     * element.
     *
     * The `<Period>` element needs to be generated before this element with
     * [.generatePeriodElement].
     *
     * @param doc the [Document] on which the `<Period>` element will be appended
     * @param itagItem the [ItagItem] corresponding to the stream, which must not be null
     */
    @Throws(CreationException::class)
    fun generateAdaptationSetElement(doc: Document, itagItem: ItagItem) {
        try {
            val periodElement = doc.getElementsByTagName(PERIOD).item(0) as Element
            val adaptationSetElement = doc.createElement(ADAPTATION_SET)

            setAttribute(adaptationSetElement, doc, "id", "0")

            val mediaFormat = itagItem.mediaFormat
            if (mediaFormat == null || mediaFormat.mimeType.isEmpty()) {
                throw couldNotAddElement(ADAPTATION_SET, "the MediaFormat or its mime type is null or empty")
            }

            if (itagItem.itagType == ItagItem.ItagType.AUDIO) {
                val audioLocale = itagItem.audioLocale
                if (audioLocale != null) {
                    val audioLanguage = audioLocale.language
                    if (audioLanguage.isNotEmpty()) setAttribute(adaptationSetElement, doc, "lang", audioLanguage)
                }
            }

            setAttribute(adaptationSetElement, doc, "mimeType", mediaFormat.mimeType)
            setAttribute(adaptationSetElement, doc, "subsegmentAlignment", "true")

            periodElement.appendChild(adaptationSetElement)
        } catch (e: DOMException) {
            throw couldNotAddElement(ADAPTATION_SET, e)
        }
    }

    /**
     * Generate the `<Role>` element, appended as a child of the `<AdaptationSet>`element.
     *
     * This element, with its attributes and values, is:
     *
     * `<Role schemeIdUri="urn:mpeg:DASH:role:2011" value="VALUE"/>`, where `VALUE` is
     * `main` for videos and audios, `description` for descriptive audio and
     * `dub` for dubbed audio.
     *
     * The `<AdaptationSet>` element needs to be generated before this element with
     * [.generateAdaptationSetElement]).
     *
     * @param doc      the [Document] on which the `<Role>` element will be appended
     * @param itagItem the [ItagItem] corresponding to the stream, which must not be null
     */
    @Throws(CreationException::class)
    fun generateRoleElement(doc: Document, itagItem: ItagItem) {
        try {
            val adaptationSetElement = doc.getElementsByTagName(ADAPTATION_SET).item(0) as Element
            val roleElement = doc.createElement(ROLE)

            setAttribute(roleElement, doc, "schemeIdUri", "urn:mpeg:DASH:role:2011")
            setAttribute(roleElement, doc, "value", getRoleValue(itagItem.audioTrackType))

            adaptationSetElement.appendChild(roleElement)
        } catch (e: DOMException) {
            throw couldNotAddElement(ROLE, e)
        }
    }

    /**
     * Get the value of the `<Role>` element based on the [AudioTrackType] attribute
     * of a stream.
     * @param trackType audio track type
     * @return role value
     */
    private fun getRoleValue(trackType: AudioTrackType?): String {
        if (trackType != null) {
            return when (trackType) {
                AudioTrackType.ORIGINAL -> "main"
                AudioTrackType.DUBBED -> "dub"
                AudioTrackType.DESCRIPTIVE -> "description"
            }
        }
        return "main"
    }

    /**
     * Generate the `<Representation>` element, appended as a child of the
     * `<AdaptationSet>` element.
     *
     * The `<AdaptationSet>` element needs to be generated before this element with
     * [.generateAdaptationSetElement]).
     *
     * @param doc the [Document] on which the `<SegmentTimeline>` element will be
     * appended
     * @param itagItem the [ItagItem] to use, which must not be null
     */

    @Throws(CreationException::class)
    fun generateRepresentationElement(doc: Document, itagItem: ItagItem) {
        try {
            val adaptationSetElement = doc.getElementsByTagName(ADAPTATION_SET).item(0) as Element
            val representationElement = doc.createElement(REPRESENTATION)

            val id = itagItem.id
            if (id <= 0) throw couldNotAddElement(REPRESENTATION, "the id of the ItagItem is <= 0")
            setAttribute(representationElement, doc, "id", id.toString())

            val codec = itagItem.codec
            if (codec.isNullOrEmpty()) throw couldNotAddElement(ADAPTATION_SET, "the codec value of the ItagItem is null or empty")
            setAttribute(representationElement, doc, "codecs", codec)
            setAttribute(representationElement, doc, "startWithSAP", "1")
            setAttribute(representationElement, doc, "maxPlayoutRate", "1")

            val bitrate = itagItem.bitrate
            if (bitrate <= 0) throw couldNotAddElement(REPRESENTATION, "the bitrate of the ItagItem is <= 0")
            setAttribute(representationElement, doc, "bandwidth", bitrate.toString())

            if (itagItem.itagType == ItagItem.ItagType.VIDEO || itagItem.itagType == ItagItem.ItagType.VIDEO_ONLY) {
                val height = itagItem.height
                val width = itagItem.width
                if (height <= 0 && width <= 0) throw couldNotAddElement(REPRESENTATION, "both width and height of the ItagItem are <= 0")

                if (width > 0) setAttribute(representationElement, doc, "width", width.toString())
                setAttribute(representationElement, doc, "height", itagItem.height.toString())

                val fps = itagItem.getFps()
                if (fps > 0) setAttribute(representationElement, doc, "frameRate", fps.toString())
            }

            if (itagItem.itagType == ItagItem.ItagType.AUDIO && itagItem.getSampleRate() > 0) {
                val audioSamplingRateAttribute = doc.createAttribute("audioSamplingRate")
                audioSamplingRateAttribute.value = itagItem.getSampleRate().toString()
            }
            adaptationSetElement.appendChild(representationElement)
        } catch (e: DOMException) {
            throw couldNotAddElement(REPRESENTATION, e)
        }
    }

    /**
     * Generate the `<AudioChannelConfiguration>` element, appended as a child of the`<Representation>` element.
     * This method is only used when generating DASH manifests of audio streams.
     *
     * It will produce the following element:
     * <br></br>
     * `<AudioChannelConfiguration
     * schemeIdUri="urn:mpeg:dash:23003:3:audio_channel_configuration:2011"
     * value="audioChannelsValue"`
     * <br></br>
     * (where `audioChannelsValue` is get from the [ItagItem] passed as the second
     * parameter of this method)
     *
     * The `<Representation>` element needs to be generated before this element with
     * [.generateRepresentationElement]).
     *
     * @param doc the [Document] on which the `<AudioChannelConfiguration>` element will
     * be appended
     * @param itagItem the [ItagItem] to use, which must not be null
     */
    @Throws(CreationException::class)
    fun generateAudioChannelConfigurationElement(doc: Document, itagItem: ItagItem) {
        try {
            val representationElement = doc.getElementsByTagName(REPRESENTATION).item(0) as Element
            val audioChannelConfigurationElement = doc.createElement(AUDIO_CHANNEL_CONFIGURATION)

            setAttribute(audioChannelConfigurationElement, doc, "schemeIdUri", "urn:mpeg:dash:23003:3:audio_channel_configuration:2011")

            if (itagItem.getAudioChannels() <= 0) throw CreationException("the number of audioChannels in the ItagItem is <= 0: ${itagItem.getAudioChannels()}")
            setAttribute(audioChannelConfigurationElement, doc, "value", itagItem.getAudioChannels().toString())

            representationElement.appendChild(audioChannelConfigurationElement)
        } catch (e: DOMException) {
            throw couldNotAddElement(AUDIO_CHANNEL_CONFIGURATION, e)
        }
    }

    /**
     * Convert a DASH manifest [doc][Document] to a string and cache it.
     *
     * @param originalBaseStreamingUrl the original base URL of the stream
     * @param doc                      the doc to be converted
     * @param manifestCreatorCache     the [ManifestCreatorCache] on which store the string
     * generated
     * @return the DASH manifest [doc][Document] converted to a string
     */

    @Throws(CreationException::class)
    fun buildAndCacheResult(originalBaseStreamingUrl: String, doc: Document, manifestCreatorCache: ManifestCreatorCache<String, String>): String {
        try {
            val documentXml = documentToXml(doc)
            manifestCreatorCache.put(originalBaseStreamingUrl, documentXml)
            return documentXml
        } catch (e: Exception) {
            throw CreationException("Could not convert the DASH manifest generated to a string", e)
        }
    }

    /**
     * Generate the `<SegmentTemplate>` element, appended as a child of the
     * `<Representation>` element.
     *
     * This method is only used when generating DASH manifests from OTF and post-live-DVR streams.
     *
     * It will produce a `<SegmentTemplate>` element with the following attributes:
     *
     *  * `startNumber`, which takes the value `0` for post-live-DVR streams and
     * `1` for OTF streams;
     *  * `timescale`, which is always `1000`;
     *  * `media`, which is the base URL of the stream on which is appended
     * `&sq=$Number$`;
     *  * `initialization` (only for OTF streams), which is the base URL of the stream
     * on which is appended [.SQ_0].
     *
     * The `<Representation>` element needs to be generated before this element with
     * [.generateRepresentationElement]).
     *
     *
     * @param doc          the [Document] on which the `<SegmentTemplate>` element will
     * be appended
     * @param baseUrl      the base URL of the OTF/post-live-DVR stream
     * @param deliveryType the stream [delivery type][DeliveryType], which must be either
     * [OTF][DeliveryType.OTF] or [LIVE][DeliveryType.LIVE]
     */

    @Throws(CreationException::class)
    fun generateSegmentTemplateElement(doc: Document, baseUrl: String, deliveryType: DeliveryType) {
        if (deliveryType != DeliveryType.OTF && deliveryType != DeliveryType.LIVE)
            throw couldNotAddElement(SEGMENT_TEMPLATE, "invalid delivery type: $deliveryType")

        try {
            val representationElement = doc.getElementsByTagName(REPRESENTATION).item(0) as Element
            val segmentTemplateElement = doc.createElement(SEGMENT_TEMPLATE)

            // The first sequence of post DVR streams is the beginning of the video stream and not
            // an initialization segment
            setAttribute(segmentTemplateElement, doc, "startNumber", if (deliveryType == DeliveryType.LIVE) "0" else "1")
            setAttribute(segmentTemplateElement, doc, "timescale", "1000")

            // Post-live-DVR/ended livestreams streams don't require an initialization sequence
            if (deliveryType != DeliveryType.LIVE) setAttribute(segmentTemplateElement, doc, "initialization", baseUrl + SQ_0)

            setAttribute(segmentTemplateElement, doc, "media", "$baseUrl&sq=\$Number$")

            representationElement.appendChild(segmentTemplateElement)
        } catch (e: DOMException) {
            throw couldNotAddElement(SEGMENT_TEMPLATE, e)
        }
    }

    /**
     * Generate the `<SegmentTimeline>` element, appended as a child of the
     * `<SegmentTemplate>` element.
     *
     * The `<SegmentTemplate>` element needs to be generated before this element with
     * [.generateSegmentTemplateElement].
     *
     * @param doc the [Document] on which the `<SegmentTimeline>` element will be
     * appended
     */

    @Throws(CreationException::class)
    fun generateSegmentTimelineElement(doc: Document) {
        try {
            val segmentTemplateElement = doc.getElementsByTagName(SEGMENT_TEMPLATE).item(0) as Element
            val segmentTimelineElement = doc.createElement(SEGMENT_TIMELINE)

            segmentTemplateElement.appendChild(segmentTimelineElement)
        } catch (e: DOMException) {
            throw couldNotAddElement(SEGMENT_TIMELINE, e)
        }
    }

    /**
     * Get the "initialization" [response][Response] of a stream.
     *
     * This method fetches, for OTF streams and for post-live-DVR streams:
     *
     *  * the base URL of the stream, to which are appended [.SQ_0] and
     * [.RN_0] parameters, with a `GET` request for streaming URLs from HTML5
     * clients and a `POST` request for the ones from the `ANDROID` and the
     * `IOS` clients;
     *  * for streaming URLs from HTML5 clients, the [.ALR_YES] param is also added.
     *
     * @param baseStreamingUrl the base URL of the stream, which must not be null
     * @param itagItem         the [ItagItem] of stream, which must not be null
     * @param deliveryType     the [DeliveryType] of the stream
     * @return the "initialization" response, without redirections on the network on which the
     * request(s) is/are made
     */


    @Throws(CreationException::class)
    fun getInitializationResponse(baseStreamingUrl: String, itagItem: ItagItem, deliveryType: DeliveryType): Response {
        var baseStreamingUrl = baseStreamingUrl
        val isHtml5StreamingUrl = (isWebStreamingUrl(baseStreamingUrl) || isTvHtml5SimplyEmbeddedPlayerStreamingUrl(baseStreamingUrl))
        val isAndroidStreamingUrl = isAndroidStreamingUrl(baseStreamingUrl)
        val isIosStreamingUrl = isIosStreamingUrl(baseStreamingUrl)
        if (isHtml5StreamingUrl) baseStreamingUrl += ALR_YES
        baseStreamingUrl = appendRnSqParamsIfNeeded(baseStreamingUrl, deliveryType)

        val downloader = downloader
        if (isHtml5StreamingUrl) {
            val mimeTypeExpected = itagItem.mediaFormat.mimeType
            if (mimeTypeExpected.isNotEmpty()) return getStreamingWebUrlWithoutRedirects(downloader, baseStreamingUrl, mimeTypeExpected)
        } else if (isAndroidStreamingUrl || isIosStreamingUrl) {
            try {
                val headers = mapOf("User-Agent" to listOf(if (isAndroidStreamingUrl) getAndroidUserAgent(null) else getIosUserAgent(null)))
                val emptyBody = "".toByteArray(StandardCharsets.UTF_8)
                return downloader.post(baseStreamingUrl, headers, emptyBody)
            } catch (e: IOException) {
                throw CreationException("Could not get the ${if (isIosStreamingUrl) "ANDROID" else "IOS"} streaming URL response", e)
            } catch (e: ExtractionException) {
                throw CreationException("Could not get the ${if (isIosStreamingUrl) "ANDROID" else "IOS"} streaming URL response", e)
            }
        }

        try {
            return downloader.get(baseStreamingUrl)
        } catch (e: IOException) {
            throw CreationException("Could not get the streaming URL response", e)
        } catch (e: ExtractionException) {
            throw CreationException("Could not get the streaming URL response", e)
        }
    }

    /**
     * Generate a new [DocumentBuilder] secured from XXE attacks, on platforms which
     * support setting [XMLConstants.ACCESS_EXTERNAL_DTD] and
     * [XMLConstants.ACCESS_EXTERNAL_SCHEMA] in [DocumentBuilderFactory] instances.
     *
     * @return an instance of [Document] secured against XXE attacks on supported platforms,
     * that should then be convertible to an XML string without security problems
     */
    @Throws(ParserConfigurationException::class)
    private fun newDocument(): Document {
        val documentBuilderFactory = DocumentBuilderFactory.newInstance()
        try {
            documentBuilderFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "")
            documentBuilderFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "")
        } catch (ignored: Exception) {
            // Ignore exceptions as setting these attributes to secure XML generation is not
            // supported by all platforms (like the Android implementation)
        }

        return documentBuilderFactory.newDocumentBuilder().newDocument()
    }

    /**
     * Generate a new [TransformerFactory] secured from XXE attacks, on platforms which
     * support setting [XMLConstants.ACCESS_EXTERNAL_DTD] and
     * [XMLConstants.ACCESS_EXTERNAL_SCHEMA] in [TransformerFactory] instances.
     *
     * @param doc the doc to convert, which must have been created using [.newDocument] to
     * properly prevent XXE attacks
     * @return the doc converted to an XML string, making sure there can't be XXE attacks
     */
    // Sonar warning is suppressed because it is still shown even if we apply its solution
    @Throws(TransformerException::class)
    private fun documentToXml(doc: Document): String {
        val transformerFactory = TransformerFactory.newInstance()
        try {
            transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "")
            transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "")
        } catch (ignored: Exception) {
            // Ignore exceptions as setting these attributes to secure XML generation is not
            // supported by all platforms (like the Android implementation)
        }

        val transformer = transformerFactory.newTransformer()
        transformer.setOutputProperty(OutputKeys.VERSION, "1.0")
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8")
        transformer.setOutputProperty(OutputKeys.STANDALONE, "no")

        val result = StringWriter()
        transformer.transform(DOMSource(doc), StreamResult(result))

        return result.toString()
    }

    /**
     * Append [.SQ_0] for post-live-DVR and OTF streams and [.RN_0] to all streams.
     *
     * @param baseStreamingUrl the base streaming URL to which the parameter(s) are being appended
     * @param deliveryType     the [DeliveryType] of the stream
     * @return the base streaming URL to which the param(s) are appended, depending on the
     * [DeliveryType] of the stream
     */

    private fun appendRnSqParamsIfNeeded(baseStreamingUrl: String, deliveryType: DeliveryType): String {
        return baseStreamingUrl + (if (deliveryType == DeliveryType.PROGRESSIVE) "" else SQ_0) + RN_0
    }

    /**
     * Get a URL on which no redirection between playback hosts should be present on the network
     * and/or IP used to fetch the streaming URL, for HTML5 clients.
     *
     *
     * This method will follow redirects which works in the following way:
     *
     *  1. the [.ALR_YES] param is appended to all streaming URLs
     *  1. if no redirection occurs, the video server will return the streaming data;
     *  1. if a redirection occurs, the server will respond with HTTP status code 200 and a
     * `text/plain` mime type. The redirection URL is the response body;
     *  1. the redirection URL is requested and the steps above from step 2 are repeated,
     * until too many redirects are reached of course (the maximum number of redirects is
     * [the same as OkHttp][.MAXIMUM_REDIRECT_COUNT]).
     *
     * For non-HTML5 clients, redirections are managed in the standard way in
     * [.getInitializationResponse].
     *
     * @param downloader               the [Downloader] instance to be used
     * @param streamingUrl             the streaming URL which we are trying to get a streaming URL
     * without any redirection on the network and/or IP used
     * @param responseMimeTypeExpected the response mime type expected from Google video servers
     * @return the [Response] of the stream, which should have no redirections
     */

    @Throws(CreationException::class)
    private fun getStreamingWebUrlWithoutRedirects(downloader: Downloader, streamingUrl: String, responseMimeTypeExpected: String): Response {
        var streamingUrl: String = streamingUrl
        try {
            val headers: Map<String, List<String>> = clientInfoHeaders

            var responseMimeType: String? = ""

            var redirectsCount = 0
            while (responseMimeType != responseMimeTypeExpected && redirectsCount < MAXIMUM_REDIRECT_COUNT) {
                val response = downloader.get(streamingUrl, headers)

                val responseCode = response.responseCode()
                if (responseCode != 200) throw CreationException("Could not get the initialization URL: HTTP response code $responseCode")

                // A valid HTTP 1.0+ response should include a Content-Type header, so we can
                // require that the response from video servers has this header.
                responseMimeType = Objects.requireNonNull(response.getHeader("Content-Type"), "Could not get the Content-Type header from the response headers")

                // The response body is the redirection URL
                if (responseMimeType == "text/plain") {
                    streamingUrl = response.responseBody()
                    redirectsCount++
                } else return response
            }

            if (redirectsCount >= MAXIMUM_REDIRECT_COUNT) throw CreationException("Too many redirects when trying to get the the streaming URL response of a HTML5 client")

            // This should never be reached, but is required because we don't want to return null
            // here
            throw CreationException("Could not get the streaming URL response of a HTML5 client: unreachable code reached!")
        } catch (e: IOException) {
            throw CreationException("Could not get the streaming URL response of a HTML5 client", e)
        } catch (e: ExtractionException) {
            throw CreationException("Could not get the streaming URL response of a HTML5 client", e)
        }
    }
}
