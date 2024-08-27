package ac.mdiq.vista.extractor.stream

/**
 * An enum representing the track type of an [AudioStream] extracted by a [ ].
 */
enum class AudioTrackType {
    /**
     * An original audio track of the video.
     */
    ORIGINAL,

    /**
     * An audio track with the original voices replaced, typically in a different language.
     * @see [
     * https://en.wikipedia.org/wiki/Dubbing](https://en.wikipedia.org/wiki/Dubbing)
     */
    DUBBED,

    /**
     * A descriptive audio track.
     *
     * A descriptive audio track is an audio track in which descriptions of visual elements of
     * a video are added to the original audio, with the goal to make a video more accessible to
     * blind and visually impaired people.
     *
     * @see [
     * https://en.wikipedia.org/wiki/Audio_description](https://en.wikipedia.org/wiki/Audio_description)
     */
    DESCRIPTIVE
}
