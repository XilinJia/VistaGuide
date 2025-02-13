package ac.mdiq.vista.extractor.services.youtube.extractors

import ac.mdiq.vista.extractor.exceptions.ExtractionException
import ac.mdiq.vista.extractor.services.youtube.YoutubeService
import ac.mdiq.vista.extractor.subscription.SubscriptionExtractor
import ac.mdiq.vista.extractor.subscription.SubscriptionItem
import com.grack.nanojson.JsonArray
import com.grack.nanojson.JsonObject
import com.grack.nanojson.JsonParser
import com.grack.nanojson.JsonParserException
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.UncheckedIOException
import java.util.*
import java.util.stream.Collectors
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream


/**
 * Extract subscriptions from a Google takeout export
 */
class YoutubeSubscriptionExtractor(youtubeService: YoutubeService)
    : SubscriptionExtractor(youtubeService, listOf(ContentSource.INPUT_STREAM)) {

    override val relatedUrl: String
        get() = "https://takeout.google.com/takeout/custom/youtube"

    @Throws(ExtractionException::class)
    override fun fromInputStream(contentInputStream: InputStream): List<SubscriptionItem?> {
        return fromJsonInputStream(contentInputStream)
    }

    @Throws(ExtractionException::class)
    override fun fromInputStream(contentInputStream: InputStream,  contentType: String): List<SubscriptionItem?> {
        return when (contentType) {
            "json", "application/json" -> fromJsonInputStream(contentInputStream)
            "csv", "text/csv", "text/comma-separated-values" -> fromCsvInputStream(contentInputStream)
            "zip", "application/zip" -> fromZipInputStream(contentInputStream)
            else -> throw InvalidSourceException("Unsupported content type: $contentType")
        }
    }

    @Throws(ExtractionException::class)
    fun fromJsonInputStream(contentInputStream: InputStream): List<SubscriptionItem?> {
        val subscriptions: JsonArray
        try { subscriptions = JsonParser.array().from(contentInputStream) } catch (e: JsonParserException) { throw InvalidSourceException("Invalid json input stream", e) }

        var foundInvalidSubscription = false
        val subscriptionItems: MutableList<SubscriptionItem?> = ArrayList()
        for (subscriptionObject in subscriptions) {
            if (subscriptionObject !is JsonObject) {
                foundInvalidSubscription = true
                continue
            }

            val subscription = subscriptionObject.getObject("snippet")
            val id = subscription.getObject("resourceId").getString("channelId", "")
            if (id.length != 24) { // e.g. UCsXVk37bltHxD1rDPwtNM8Q
                foundInvalidSubscription = true
                continue
            }

            subscriptionItems.add(SubscriptionItem(service.serviceId, BASE_CHANNEL_URL + id, subscription.getString("title", "")))
        }

        if (foundInvalidSubscription && subscriptionItems.isEmpty()) throw InvalidSourceException("Found only invalid channel ids")
        return subscriptionItems
    }

    @Throws(ExtractionException::class)
    fun fromZipInputStream(contentInputStream: InputStream): List<SubscriptionItem?> {
        try {
            ZipInputStream(contentInputStream).use { zipInputStream ->
                var zipEntry: ZipEntry? = null
                while (zipInputStream.nextEntry?.also { zipEntry = it } != null) {
                    if (zipEntry!!.name.lowercase(Locale.getDefault()).endsWith(".csv")) {
                        try {
                            val csvItems = fromCsvInputStream(zipInputStream)
                            // Return it only if it has items (it exits early if it's the wrong file
                            // format), otherwise try the next file
                            if (csvItems.isNotEmpty()) return csvItems
                        } catch (e: ExtractionException) {/* Ignore error and go to next file */ }
                    }
                }
            }
        } catch (e: IOException) { throw InvalidSourceException("Error reading contents of zip file", e) }
        throw InvalidSourceException("Unable to find a valid subscriptions.csv file (try extracting and selecting the csv file)")
    }

    @Throws(ExtractionException::class)
    fun fromCsvInputStream(contentInputStream: InputStream): MutableList<SubscriptionItem?> {
        // Expected format of CSV file:
        // Channel Id,Channel Url,Channel Title
        //UC1JTQBa5QxZCpXrFSkMxmPw,http://www.youtube.com/channel/UC1JTQBa5QxZCpXrFSkMxmPw,Raycevick
        //UCFl7yKfcRcFmIUbKeCA-SJQ,http://www.youtube.com/channel/UCFl7yKfcRcFmIUbKeCA-SJQ,Joji
        //
        // Notes:
        //      It's always 3 columns
        //      The first line is always a header
        //      Header names are different based on the locale
        //      Fortunately the data is always the same order no matter what locale
        try {
            BufferedReader(InputStreamReader(contentInputStream)).use { reader ->
                return reader.lines()
                    .skip(1) // ignore header and skip first line
                    .map<Array<String?>> { line: String? -> line?.split(",".toRegex())?.dropLastWhile { it.isEmpty() }?.toTypedArray() }
                    .filter { values: Array<String?> -> values.size >= 3 }
                    .map<SubscriptionItem?> { values: Array<String?> ->
                        // Channel URL from second entry
                        val channelUrl = values[1]?.replace("http://", "https://")
                        // Channel title from third entry
                        if (channelUrl?.startsWith(BASE_CHANNEL_URL) == true) SubscriptionItem(service.serviceId, channelUrl, values[2]!!) else null
                    }
                    .filter { obj: SubscriptionItem? -> Objects.nonNull(obj) }
                    .collect(Collectors.toUnmodifiableList())
            }
        } catch (e: UncheckedIOException) { throw InvalidSourceException("Error reading CSV file", e)
        } catch (e: IOException) { throw InvalidSourceException("Error reading CSV file", e) }
    }

    companion object {
        private const val BASE_CHANNEL_URL = "https://www.youtube.com/channel/"
    }
}
