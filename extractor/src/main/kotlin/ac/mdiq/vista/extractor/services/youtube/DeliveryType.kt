package ac.mdiq.vista.extractor.services.youtube

/**
 * Streaming format types used by YouTube in their streams.
 *
 *
 *
 * It is different from [delivery methods][ac.mdiq.vista.extractor.stream.DeliveryMethod]!
 *
 */
enum class DeliveryType {
    /**
     * YouTube's progressive delivery method, which works with HTTP range headers.
     * (Note that official clients use the corresponding parameter instead.)
     *
     *
     *
     * Initialization and index ranges are available to get metadata (the corresponding values
     * are returned in the player response).
     *
     */
    PROGRESSIVE,

    /**
     * YouTube's OTF delivery method which uses a sequence parameter to get segments of
     * streams.
     *
     *
     *
     * The first sequence (which can be fetched with the `&sq=0` parameter) contains all the
     * metadata needed to build the stream source (sidx boxes, segment length, segment count,
     * duration, ...).
     *
     *
     *
     *
     * Only used for videos; mostly those with a small amount of views, or ended livestreams
     * which have just been re-encoded as normal videos.
     *
     */
    OTF,

    /**
     * YouTube's delivery method for livestreams which uses a sequence parameter to get
     * segments of streams.
     *
     *
     *
     * Each sequence (which can be fetched with the `&sq=0` parameter) contains its own
     * metadata (sidx boxes, segment length, ...), which make no need of an initialization
     * segment.
     *
     *
     *
     *
     * Only used for livestreams (ended or running).
     *
     */
    LIVE
}
