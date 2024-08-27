package ac.mdiq.vista.extractor.exceptions

class ContentNotSupportedException : ParsingException {
    constructor(message: String?) : super(message)

    constructor(message: String?, cause: Throwable?) : super(message, cause)
}
