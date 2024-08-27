package ac.mdiq.vista.extractor.stream

import java.io.Serializable


class StreamSegment(
        @JvmField var title: String,
        /**
         * Timestamp of the starting point in seconds
         */
        @JvmField var startTimeSeconds: Int) : Serializable {

    /**
     * The channel or creator linked to this segment
     */
    @JvmField
    var channelName: String? = null

    /**
     * Direct url to this segment. This can be null if the service doesn't provide such function.
     */
    @JvmField
    var url: String? = null

    /**
     * Preview url for this segment. This can be null if the service doesn't provide such function
     * or there is no resource found.
     */
    @JvmField
    var previewUrl: String? = null
}
