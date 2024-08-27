package ac.mdiq.vista.extractor.services.youtube.dashmanifestcreators



/**
 * Exception that is thrown when a YouTube DASH manifest creator encounters a problem
 * while creating a manifest.
 */
class CreationException : RuntimeException {
    /**
     * Create a new [CreationException] with a detail message.
     *
     * @param message the detail message to add in the exception
     */
    constructor(message: String?) : super(message)

    /**
     * Create a new [CreationException] with a detail message and a cause.
     * @param message the detail message to add in the exception
     * @param cause   the exception cause of this [CreationException]
     */
    constructor(message: String?, cause: Exception?) : super(message, cause)

    companion object {
        // Methods to create exceptions easily without having to use big exception messages and to
        // reduce duplication
        /**
         * Create a new [CreationException] with a cause and the following detail message format:
         * <br></br>
         * `"Could not add " + element + " element", cause`, where `element` is an element
         * of a DASH manifest.
         *
         * @param element the element which was not added to the DASH document
         * @param cause   the exception which prevented addition of the element to the DASH document
         * @return a new [CreationException]
         */

        fun couldNotAddElement(element: String, cause: Exception?): CreationException {
            return CreationException("Could not add $element element", cause)
        }

        /**
         * Create a new [CreationException] with a cause and the following detail message format:
         * <br></br>
         * `"Could not add " + element + " element: " + reason`, where `element` is an
         * element of a DASH manifest and `reason` the reason why this element cannot be added to
         * the DASH document.
         *
         * @param element the element which was not added to the DASH document
         * @param reason  the reason message of why the element has been not added to the DASH document
         * @return a new [CreationException]
         */


        fun couldNotAddElement(element: String, reason: String): CreationException {
            return CreationException("Could not add $element element: $reason")
        }
    }
}
