package ac.mdiq.vista.extractor.services.youtube.dashmanifestcreators

import ac.mdiq.vista.extractor.services.youtube.DeliveryType
import ac.mdiq.vista.extractor.services.youtube.ItagItem
import ac.mdiq.vista.extractor.services.youtube.dashmanifestcreators.CreationException.Companion.couldNotAddElement
import ac.mdiq.vista.extractor.services.youtube.dashmanifestcreators.YoutubeDashManifestCreatorsUtils.ALR_YES
import ac.mdiq.vista.extractor.services.youtube.dashmanifestcreators.YoutubeDashManifestCreatorsUtils.RN_0
import ac.mdiq.vista.extractor.services.youtube.dashmanifestcreators.YoutubeDashManifestCreatorsUtils.SEGMENT_TIMELINE
import ac.mdiq.vista.extractor.services.youtube.dashmanifestcreators.YoutubeDashManifestCreatorsUtils.SQ_0
import ac.mdiq.vista.extractor.services.youtube.dashmanifestcreators.YoutubeDashManifestCreatorsUtils.buildAndCacheResult
import ac.mdiq.vista.extractor.services.youtube.dashmanifestcreators.YoutubeDashManifestCreatorsUtils.generateDocumentAndDoCommonElementsGeneration
import ac.mdiq.vista.extractor.services.youtube.dashmanifestcreators.YoutubeDashManifestCreatorsUtils.generateSegmentTemplateElement
import ac.mdiq.vista.extractor.services.youtube.dashmanifestcreators.YoutubeDashManifestCreatorsUtils.generateSegmentTimelineElement
import ac.mdiq.vista.extractor.services.youtube.dashmanifestcreators.YoutubeDashManifestCreatorsUtils.getInitializationResponse
import ac.mdiq.vista.extractor.services.youtube.dashmanifestcreators.YoutubeDashManifestCreatorsUtils.setAttribute
import ac.mdiq.vista.extractor.utils.ManifestCreatorCache
import ac.mdiq.vista.extractor.utils.Utils.removeNonDigitCharacters
import org.w3c.dom.DOMException
import org.w3c.dom.Document
import org.w3c.dom.Element
import java.util.*


/**
 * Class which generates DASH manifests of YouTube [OTF streams][DeliveryType.OTF].
 */
object YoutubeOtfDashManifestCreator {
    /**
     * @return the cache of DASH manifests generated for OTF streams
     */
    /**
     * Cache of DASH manifests generated for OTF streams.
     */

    val cache: ManifestCreatorCache<String, String> = ManifestCreatorCache()

    /**
     * Create DASH manifests from a YouTube OTF stream.
     *
     * OTF streams are YouTube-DASH specific streams which work with sequences and without the need
     * to get a manifest (even if one is provided, it is not used by official clients).
     *
     * They can be found only on videos; mostly those with a small amount of views, or ended
     * livestreams which have just been re-encoded as normal videos.
     *
     * This method needs:
     *
     *  * the base URL of the stream (which, if you try to access to it, returns HTTP
     * status code 404 after redirects, and if the URL is valid);
     *  * an [ItagItem], which needs to contain the following information:
     *
     *  * its type (see [ItagItem.ItagType]), to identify if the content is
     * an audio or a video stream;
     *  * its bitrate;
     *  * its mime type;
     *  * its codec(s);
     *  * for an audio stream: its audio channels;
     *  * for a video stream: its width and height.
     *
     *
     *  * the duration of the video, which will be used if the duration could not be
     * parsed from the first sequence of the stream.
     *
     * In order to generate the DASH manifest, this method will:
     *
     *  * request the first sequence of the stream (the base URL on which the first
     * sequence parameter is appended (see [YoutubeDashManifestCreatorsUtils.SQ_0]))
     * with a `POST` or `GET` request (depending of the client on which the
     * streaming URL comes from is a mobile one (`POST`) or not (`GET`));
     *  * follow its redirection(s), if any;
     *  * save the last URL, remove the first sequence parameter;
     *  * use the information provided in the [ItagItem] to generate all
     * elements of the DASH manifest.
     *
     * If the duration cannot be extracted, the `durationSecondsFallback` value will be used
     * as the stream duration.
     *
     *
     * @param otfBaseStreamingUrl     the base URL of the OTF stream, which must not be null
     * @param itagItem                the [ItagItem] corresponding to the stream, which
     * must not be null
     * @param durationSecondsFallback the duration of the video, which will be used if the duration
     * could not be extracted from the first sequence
     * @return the manifest generated into a string
     */


    @Throws(CreationException::class)
    fun fromOtfStreamingUrl(otfBaseStreamingUrl: String, itagItem: ItagItem, durationSecondsFallback: Long): String {
        if (cache.containsKey(otfBaseStreamingUrl)) return Objects.requireNonNull(cache.get(otfBaseStreamingUrl))?.second ?: ""

        var realOtfBaseStreamingUrl = otfBaseStreamingUrl
        // Try to avoid redirects when streaming the content by saving the last URL we get
        // from video servers.
        val response = getInitializationResponse(realOtfBaseStreamingUrl, itagItem, DeliveryType.OTF)
        realOtfBaseStreamingUrl = response.latestUrl().replace(SQ_0, "").replace(RN_0, "").replace(ALR_YES, "")

        val responseCode = response.responseCode()
        if (responseCode != 200) throw CreationException("Could not get the initialization URL: response code $responseCode")

        val segmentDuration: Array<String?>

        try {
            val segmentsAndDurationsResponseSplit: Array<String?> =
                response.responseBody() // Get the lines with the durations and the following
                    .split("Segment-Durations-Ms: ".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()[1] // Remove the other lines
                    .split("\n".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()[0] // Get all durations and repetitions which are separated by a comma
                    .split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val lastIndex = segmentsAndDurationsResponseSplit.size - 1
            segmentDuration = if (segmentsAndDurationsResponseSplit[lastIndex].isNullOrEmpty()) segmentsAndDurationsResponseSplit.copyOf(lastIndex)
            else segmentsAndDurationsResponseSplit
        } catch (e: Exception) {
            throw CreationException("Could not get segment durations", e)
        }
        val streamDuration = try {
            getStreamDuration(segmentDuration)
        } catch (e: CreationException) {
            durationSecondsFallback * 1000
        }

        val doc = generateDocumentAndDoCommonElementsGeneration(itagItem, streamDuration)

        generateSegmentTemplateElement(doc, realOtfBaseStreamingUrl, DeliveryType.OTF)
        generateSegmentTimelineElement(doc)
        generateSegmentElementsForOtfStreams(segmentDuration, doc)

        return buildAndCacheResult(otfBaseStreamingUrl, doc, cache)
    }

    /**
     * Generate segment elements for OTF streams.
     *
     * By parsing by the first media sequence, we know how many durations and repetitions there are
     * so we just have to loop into segment durations to generate the following elements for each
     * duration repeated X times:
     *
     * `<S d="segmentDuration" r="durationRepetition" />`
     *
     * If there is no repetition of the duration between two segments, the `r` attribute is
     * not added to the `S` element, as it is not needed.
     *
     * These elements will be appended as children of the `<SegmentTimeline>` element, which
     * needs to be generated before these elements with
     * [YoutubeDashManifestCreatorsUtils.generateSegmentTimelineElement].
     *
     *
     * @param segmentDurations the sequences "length" or "length(r=repeat_count" extracted with the
     * regular expressions
     * @param doc              the [Document] on which the `<S>` elements will be
     * appended
     */
    @Throws(CreationException::class)
    private fun generateSegmentElementsForOtfStreams(segmentDurations: Array<String?>, doc: Document) {
        try {
            val segmentTimelineElement = doc.getElementsByTagName(SEGMENT_TIMELINE).item(0) as Element

            for (segmentDuration in segmentDurations) {
                val sElement = doc.createElement("S")

                val segmentLengthRepeat = segmentDuration!!.split("\\(r=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                // make sure segmentLengthRepeat[0], which is the length, is convertible to int
                segmentLengthRepeat[0].toInt()

                // There are repetitions of a segment duration in other segments
                if (segmentLengthRepeat.size > 1) {
                    val segmentRepeatCount = removeNonDigitCharacters(segmentLengthRepeat[1]).toInt()
                    setAttribute(sElement, doc, "r", segmentRepeatCount.toString())
                }
                setAttribute(sElement, doc, "d", segmentLengthRepeat[0])

                segmentTimelineElement.appendChild(sElement)
            }
        } catch (e: DOMException) {
            throw couldNotAddElement("segment (S)", e)
        } catch (e: IllegalStateException) {
            throw couldNotAddElement("segment (S)", e)
        } catch (e: IndexOutOfBoundsException) {
            throw couldNotAddElement("segment (S)", e)
        } catch (e: NumberFormatException) {
            throw couldNotAddElement("segment (S)", e)
        }
    }

    /**
     * Get the duration of an OTF stream.
     *
     * The duration of OTF streams is not returned into the player response and needs to be
     * calculated by adding the duration of each segment.
     *
     * @param segmentDuration the segment duration object extracted from the initialization
     * sequence of the stream
     * @return the duration of the OTF stream, in milliseconds
     */
    @Throws(CreationException::class)
    private fun getStreamDuration(segmentDuration: Array<String?>): Long {
        try {
            var streamLengthMs: Long = 0

            for (segDuration in segmentDuration) {
                val segmentLengthRepeat = segDuration!!.split("\\(r=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                var segmentRepeatCount: Long = 0

                // There are repetitions of a segment duration in other segments
                if (segmentLengthRepeat.size > 1) segmentRepeatCount = removeNonDigitCharacters(segmentLengthRepeat[1]).toLong()

                val segmentLength = segmentLengthRepeat[0].toInt().toLong()
                streamLengthMs += segmentLength + segmentRepeatCount * segmentLength
            }

            return streamLengthMs
        } catch (e: NumberFormatException) {
            throw CreationException("Could not get stream length from sequences list", e)
        }
    }
}
