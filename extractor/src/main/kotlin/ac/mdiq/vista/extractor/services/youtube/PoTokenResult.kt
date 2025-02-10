package ac.mdiq.vista.extractor.services.youtube

import java.util.*


class PoTokenResult(visitorData: String, playerRequestPoToken: String,
                    /**
                     * The `poToken` to be appended to streaming URLs, a Protobuf object encoded as a base 64 string.
                     *
                     * It may be required on some clients such as HTML5 ones and may also differ from the player
                     * request `poToken`.
                     *
                     */
                    val streamingDataPoToken: String?) {
    /**
     * The visitor data associated with a `poToken`.
     */
    val visitorData: String = Objects.requireNonNull<String?>(visitorData)

    /**
     * The `poToken` of a player request, a Protobuf object encoded as a base 64 string.
     */
    val playerRequestPoToken: String = Objects.requireNonNull<String?>(playerRequestPoToken)
}
