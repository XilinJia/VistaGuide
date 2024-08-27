package ac.mdiq.vista.extractor.exceptions

class SoundCloudGoPlusContentException : ContentNotAvailableException {
    constructor() : super("This track is a SoundCloud Go+ track")

    constructor(cause: Throwable?) : super("This track is a SoundCloud Go+ track", cause)
}
