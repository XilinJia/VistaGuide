// Created by Fynn Godau 2019, licensed GNU GPL version 3 or later
package ac.mdiq.vista.extractor.services.bandcamp.extractors

import com.grack.nanojson.JsonObject
import com.grack.nanojson.JsonParser
import com.grack.nanojson.JsonParserException
import com.grack.nanojson.JsonWriter
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import ac.mdiq.vista.extractor.Image
import ac.mdiq.vista.extractor.Image.ResolutionLevel
import ac.mdiq.vista.extractor.Vista.downloader
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.exceptions.ReCaptchaException
import ac.mdiq.vista.extractor.localization.DateWrapper
import ac.mdiq.vista.extractor.utils.ImageSuffix
import ac.mdiq.vista.extractor.utils.Utils.replaceHttpWithHttps
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.time.DateTimeException
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.stream.Collectors


object BandcampExtractorHelper {
    /**
     * List of image IDs which preserve aspect ratio with their theoretical dimension known.
     *
     * Bandcamp images are not always squares, so images which preserve aspect ratio are only used.
     *
     * One of the direct consequences of this specificity is that only one dimension of images is
     * known at time, depending of the image ID.
     *
     * Note also that dimensions are only theoretical because if the image size is less than the
     * dimensions of the image ID, it will be not upscaled but kept to its original size.
     *
     * IDs come from [the
 * GitHub Gist "Bandcamp File Format Parameters" by f2k1de](https://gist.github.com/f2k1de/06f5fd0ae9c919a7c3693a44ee522213)
     *
     */
    private val IMAGE_URL_SUFFIXES_AND_RESOLUTIONS: List<ImageSuffix> = listOf( // ID | HEIGHT | WIDTH
        ImageSuffix("10.jpg", Image.HEIGHT_UNKNOWN, 1200, ResolutionLevel.HIGH),
        ImageSuffix("101.jpg", 90, Image.WIDTH_UNKNOWN, ResolutionLevel.LOW),
        ImageSuffix("170.jpg", 422, Image.WIDTH_UNKNOWN, ResolutionLevel.MEDIUM),  // 180 returns the same image aspect ratio and size as 171
        ImageSuffix("171.jpg", 646, Image.WIDTH_UNKNOWN, ResolutionLevel.MEDIUM),
        ImageSuffix("20.jpg", Image.HEIGHT_UNKNOWN, 1024, ResolutionLevel.HIGH),  // 203 returns the same image aspect ratio and size as 200
        ImageSuffix("200.jpg", 420, Image.WIDTH_UNKNOWN, ResolutionLevel.MEDIUM),
        ImageSuffix("201.jpg", 280, Image.WIDTH_UNKNOWN, ResolutionLevel.MEDIUM),
        ImageSuffix("202.jpg", 140, Image.WIDTH_UNKNOWN, ResolutionLevel.LOW),
        ImageSuffix("204.jpg", 360, Image.WIDTH_UNKNOWN, ResolutionLevel.MEDIUM),
        ImageSuffix("205.jpg", 240, Image.WIDTH_UNKNOWN, ResolutionLevel.MEDIUM),
        ImageSuffix("206.jpg", 180, Image.WIDTH_UNKNOWN, ResolutionLevel.MEDIUM),
        ImageSuffix("207.jpg", 120, Image.WIDTH_UNKNOWN, ResolutionLevel.LOW),
        ImageSuffix("43.jpg", 100, Image.WIDTH_UNKNOWN, ResolutionLevel.LOW),
        ImageSuffix("44.jpg", 200, Image.WIDTH_UNKNOWN, ResolutionLevel.MEDIUM))

    private const val IMAGE_URL_APPENDIX_AND_EXTENSION_REGEX = "_\\d+\\.\\w+"
    private const val IMAGES_DOMAIN_AND_PATH = "https://f4.bcbits.com/img/"

    const val BASE_URL: String = "https://bandcamp.com"
    const val BASE_API_URL: String = "$BASE_URL/api"

    /**
     * Translate all these parameters together to the URL of the corresponding album or track
     * using the mobile API
     */

    @Throws(ParsingException::class)
    fun getStreamUrlFromIds(bandId: Long, itemId: Long, itemType: String): String {
        try {
            val jsonString = downloader.get("$BASE_API_URL/mobile/22/tralbum_details?band_id=$bandId&tralbum_id=$itemId&tralbum_type=${itemType[0]}")
                .responseBody()
            return replaceHttpWithHttps(JsonParser.`object`().from(jsonString).getString("bandcamp_url"))
        } catch (e: JsonParserException) {
            throw ParsingException("Ids could not be translated to URL", e)
        } catch (e: ReCaptchaException) {
            throw ParsingException("Ids could not be translated to URL", e)
        } catch (e: IOException) {
            throw ParsingException("Ids could not be translated to URL", e)
        }
    }

    /**
     * Fetch artist details from mobile endpoint.
     * [
 * More technical info.](https://notabug.org/fynngodau/bandcampDirect/wiki/
      rewindBandcamp+%E2%80%93+Fetching+artist+details)
     */
    @Throws(ParsingException::class)
    fun getArtistDetails(id: String?): JsonObject {
        try {
            return JsonParser.`object`().from(downloader.postWithContentTypeJson(
                "$BASE_API_URL/mobile/22/band_details",
                emptyMap(),
                JsonWriter.string()
                    .`object`()
                    .value("band_id", id)
                    .end()
                    .done()
                    .toByteArray(StandardCharsets.UTF_8)).responseBody())
        } catch (e: IOException) {
            throw ParsingException("Could not download band details", e)
        } catch (e: ReCaptchaException) {
            throw ParsingException("Could not download band details", e)
        } catch (e: JsonParserException) {
            throw ParsingException("Could not download band details", e)
        }
    }

    /**
     * Generate an image url from an image ID.
     *
     * The image ID `10` was chosen because it provides images wide up to 1200px (when
     * the original image width is more than or equal this resolution).
     *
     * Other integer values are possible as well (e.g. 0 is a very large resolution, possibly the
     * original); see [.IMAGE_URL_SUFFIXES_AND_RESOLUTIONS] for more details about image
     * resolution IDs.
     *
     *
     * @param id      the image ID
     * @param isAlbum whether the image is the cover of an album or a track
     * @return a URL of the image with this ID with a width up to 1200px
     */


    fun getImageUrl(id: Long, isAlbum: Boolean): String {
        return IMAGES_DOMAIN_AND_PATH + (if (isAlbum) 'a' else "") + id + "_10.jpg"
    }

    /**
     * @return `true` if the given URL looks like it comes from a bandcamp custom domain
     * or if it comes from `bandcamp.com` itself
     */
    @Throws(ParsingException::class)
    fun isArtistDomain(url: String): Boolean {
        // Accept all bandcamp.com URLs

        if (url.lowercase(Locale.getDefault()).matches("https?://.+\\.bandcamp\\.com(/.*)?".toRegex())) return true
        // Reject non-artist bandcamp.com URLs
        if (url.lowercase(Locale.getDefault()).matches("https?://bandcamp\\.com(/.*)?".toRegex())) return false

        return try {
            // Test other URLs for whether they contain a footer that links to bandcamp
            Jsoup.parse(downloader.get(replaceHttpWithHttps(url)).responseBody())
                .getElementsByClass("cart-wrapper")[0]
                .getElementsByTag("a")[0]
                .attr("href")
                .equals("https://bandcamp.com/cart")
        } catch (e: NullPointerException) {
            false
        } catch (e: IOException) {
            throw ParsingException("Could not determine whether URL is custom domain " + "(not available? network error?)")
        } catch (e: ReCaptchaException) {
            throw ParsingException("Could not determine whether URL is custom domain " + "(not available? network error?)")
        }
    }

    /**
     * Whether the URL points to a radio kiosk.
     * @param url the URL to check
     * @return true if the URL matches `https://bandcamp.com/?show=SHOW_ID`
     */

    fun isRadioUrl(url: String): Boolean {
        return url.lowercase(Locale.getDefault()).matches("https?://bandcamp\\.com/\\?show=\\d+".toRegex())
    }


    @Throws(ParsingException::class)
    fun parseDate(textDate: String): DateWrapper {
        try {
            val zonedDateTime = ZonedDateTime.parse(textDate,
                DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm:ss zzz", Locale.ENGLISH))
            return DateWrapper(zonedDateTime.toOffsetDateTime(), false)
        } catch (e: DateTimeException) {
            throw ParsingException("Could not parse date '$textDate'", e)
        }
    }

    /**
     * Get a list of images from a search result [Element].
     *
     *
     *
     * This method will call [.getImagesFromImageUrl] using the first non null and
     * non empty image URL found from the `src` attribute of `img` HTML elements, or an
     * empty string if no valid image URL was found.
     *
     *
     * @param searchResult a search result [Element]
     * @return an unmodifiable list of [Image]s, which is never null but can be empty, in the
     * case where no valid image URL was found
     */


    fun getImagesFromSearchResult(searchResult: Element): List<Image> {
        return getImagesFromImageUrl(searchResult.getElementsByClass("art")
            .stream()
            .flatMap { element: Element -> element.getElementsByTag("img").stream() }
            .map { element: Element -> element.attr("src") }
            .filter { imageUrl: String? -> !imageUrl.isNullOrEmpty() }
            .findFirst()
            .orElse(""))
    }

    /**
     * Get all images which have resolutions preserving aspect ratio from an image URL.
     *
     *
     *
     * This method will remove the image ID and its extension from the end of the URL and then call
     * [.getImagesFromImageBaseUrl].
     *
     *
     * @param imageUrl the full URL of an image provided by Bandcamp, such as in its HTML code
     * @return an unmodifiable list of [Image]s, which is never null but can be empty, in the
     * case where the image URL has been not extracted (and so is null or empty)
     */


    fun getImagesFromImageUrl(imageUrl: String?): List<Image> {
        if (imageUrl.isNullOrEmpty()) {
            return listOf()
        }

        return getImagesFromImageBaseUrl(
            imageUrl.replaceFirst(IMAGE_URL_APPENDIX_AND_EXTENSION_REGEX.toRegex(), "_"))
    }

    /**
     * Get all images which have resolutions preserving aspect ratio from an image ID.
     *
     *
     *
     * This method will call [.getImagesFromImageBaseUrl].
     *
     *
     * @param id      the id of an image provided by Bandcamp
     * @param isAlbum whether the image is the cover of an album
     * @return an unmodifiable list of [Image]s, which is never null but can be empty, in the
     * case where the image ID has been not extracted (and so equal to 0)
     */


    fun getImagesFromImageId(id: Long, isAlbum: Boolean): List<Image> {
        if (id == 0L) {
            return listOf()
        }

        return getImagesFromImageBaseUrl(IMAGES_DOMAIN_AND_PATH + (if (isAlbum) 'a' else "") + id + "_")
    }

    /**
     * Get all images resolutions preserving aspect ratio from a base image URL.
     *
     *
     *
     * Base image URLs are images containing the image path, a `a` letter if it comes from an
     * album, its ID and an underscore.
     *
     *
     *
     *
     * Images resolutions returned are the ones of [.IMAGE_URL_SUFFIXES_AND_RESOLUTIONS].
     *
     *
     * @param baseUrl the base URL of the image
     * @return an unmodifiable and non-empty list of [Image]s
     */

    private fun getImagesFromImageBaseUrl(baseUrl: String): List<Image> {
        return IMAGE_URL_SUFFIXES_AND_RESOLUTIONS.stream()
            .map { imageSuffix: ImageSuffix ->
                Image(baseUrl + imageSuffix.suffix,
                    imageSuffix.height, imageSuffix.width,
                    imageSuffix.resolutionLevel)
            }
            .collect(Collectors.toUnmodifiableList())
    }
}
