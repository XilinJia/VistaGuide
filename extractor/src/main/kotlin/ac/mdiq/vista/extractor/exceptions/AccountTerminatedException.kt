package ac.mdiq.vista.extractor.exceptions

class AccountTerminatedException : ContentNotAvailableException {
    /**
     * The reason for the violation. There should also be more info in the exception's message.
     */
    var reason: Reason = Reason.UNKNOWN
        private set

    constructor(message: String?) : super(message)

    constructor(message: String?, reason: Reason) : super(message) {
        this.reason = reason
    }

    constructor(message: String?, cause: Throwable?) : super(message, cause)

    enum class Reason {
        UNKNOWN,
        VIOLATION
    }
}
