package ac.mdiq.vista.extractor.utils

import ac.mdiq.vista.extractor.Image.ResolutionLevel
import java.io.Serializable
import java.util.*


/**
 * Serializable class representing a suffix (which may include its format extension, such as
 * `.jpg`) which needs to be added to get an image/thumbnail URL with its corresponding
 * height, width and estimated resolution level.
 *
 *
 *
 * This class is used to construct [Image][ac.mdiq.vista.extractor.Image]
 * instances from a single base URL/path, in order to get all or most image resolutions provided,
 * depending of the service and the resolutions provided.
 *
 *
 *
 *
 * Note that this class is not intended to be used externally and so should only be used when
 * interfacing with the extractor.
 *
 */
class ImageSuffix(
        @JvmField
        /**
         * @return the suffix which needs to be appended to get the full image URL
         */
        val suffix: String,
        /**
         * @return the height corresponding to the image suffix, which may be unknown
         */
        @JvmField val height: Int,
        /**
         * @return the width corresponding to the image suffix, which may be unknown
         */
        @JvmField val width: Int,
        estimatedResolutionLevel: ResolutionLevel) : Serializable {
    /**
     * @return the estimated [ResolutionLevel] of the suffix, which is never null.
     */
    @JvmField


    val resolutionLevel: ResolutionLevel = Objects.requireNonNull(estimatedResolutionLevel, "estimatedResolutionLevel is null")

    /**
     * Get a string representation of this [ImageSuffix] instance.
     * The representation will be in the following format, where `suffix`, `height`,
     * `width` and `resolutionLevel` represent the corresponding properties:
     * <br></br>
     * <br></br>
     * `ImageSuffix {url=url, height=height, width=width, resolutionLevel=resolutionLevel}'`
     *
     *
     * @return a string representation of this [ImageSuffix] instance
     */

    override fun toString(): String {
        return ("ImageSuffix {suffix=$suffix, height=$height, width=$width, resolutionLevel=$resolutionLevel}")
    }
}
