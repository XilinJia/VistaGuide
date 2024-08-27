// Created by Fynn Godau 2019, licensed GNU GPL version 3 or later
package ac.mdiq.vista.extractor.services.bandcamp.linkHandler

import com.grack.nanojson.JsonParserException
import ac.mdiq.vista.extractor.Vista.downloader
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.exceptions.ReCaptchaException
import ac.mdiq.vista.extractor.linkhandler.ListLinkHandlerFactory
import ac.mdiq.vista.extractor.services.bandcamp.extractors.BandcampExtractorHelper
import ac.mdiq.vista.extractor.utils.JsonUtils.getJsonData
import ac.mdiq.vista.extractor.utils.Utils.replaceHttpWithHttps
import java.io.IOException
import java.util.*

/**
 * Artist do have IDs that are useful
 */
class BandcampChannelLinkHandlerFactory private constructor() : ListLinkHandlerFactory() {

    @Throws(ParsingException::class, UnsupportedOperationException::class)
    override fun getId(url: String): String {
        try {
            val response = downloader.get(replaceHttpWithHttps(url)).responseBody()

            // Use band data embedded in website to extract ID
            val bandData = getJsonData(response, "data-band")

            return bandData.getLong("id").toString()
        } catch (e: IOException) {
            throw ParsingException("Download failed", e)
        } catch (e: ReCaptchaException) {
            throw ParsingException("Download failed", e)
        } catch (e: ArrayIndexOutOfBoundsException) {
            throw ParsingException("Download failed", e)
        } catch (e: JsonParserException) {
            throw ParsingException("Download failed", e)
        }
    }

    /**
     * Uses the mobile endpoint as a "translator" from id to url
     */
    @Throws(ParsingException::class, UnsupportedOperationException::class)
    override fun getUrl(id: String, contentFilters: List<String>, sortFilter: String?): String {
        val artistDetails = BandcampExtractorHelper.getArtistDetails(id)
        if (artistDetails.getBoolean("error")) {
            throw ParsingException("JSON does not contain a channel URL (invalid id?) or is otherwise invalid")
        }
        return replaceHttpWithHttps(artistDetails.getString("bandcamp_url"))
    }

    /**
     * Accepts only pages that lead to the root of an artist profile. Supports external pages.
     */
    @Throws(ParsingException::class)
    override fun onAcceptUrl(url: String): Boolean {
        val lowercaseUrl = url.lowercase(Locale.getDefault())

        // https: | | artist.bandcamp.com | releases - music - album - track ( | name)
        //  0      1           2                           3                    (4)
        val splitUrl = lowercaseUrl.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        // URL is too short
        if (splitUrl.size != 3 && splitUrl.size != 4) return false

        // Must have "releases", "music", "album" or "track" as segment after URL or none at all
        if (splitUrl.size == 4 && !(splitUrl[3] == "releases" || splitUrl[3] == "music" || splitUrl[3] == "album" || splitUrl[3] == "track")) return false
        else {
            // Refuse links to daily.bandcamp.com as that is not an artist
            if (splitUrl[2] == "daily.bandcamp.com") return false
            // Test whether domain is supported
            return BandcampExtractorHelper.isArtistDomain(lowercaseUrl)
        }
    }

    companion object {
        val instance: BandcampChannelLinkHandlerFactory = BandcampChannelLinkHandlerFactory()
    }
}
