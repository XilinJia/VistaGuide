package ac.mdiq.vista.extractor.downloader

import ac.mdiq.vista.extractor.localization.Localization
import java.util.*


/**
 * An object that holds request information used when [executing][Downloader.execute]
 * a request.
 */
class Request(
        httpMethod: String,
        url: String,
        headers: Map<String, List<String>>?,
        private val dataToSend: ByteArray?,
        private val localization: Localization?,
        automaticLocalizationHeader: Boolean) {

    private val httpMethod: String
    private val url: String
    private val headers: Map<String, List<String>>

    init {
        this.httpMethod = Objects.requireNonNull(httpMethod, "Request's httpMethod is null")
        this.url = Objects.requireNonNull(url, "Request's url is null")

        val actualHeaders: MutableMap<String, List<String>> = LinkedHashMap()
        if (headers != null) actualHeaders.putAll(headers)
        if (automaticLocalizationHeader && localization != null) actualHeaders.putAll(getHeadersFromLocalization(localization))

        this.headers = Collections.unmodifiableMap(actualHeaders)
    }

    private constructor(builder: Builder)
            : this(builder.httpMethod, builder.url, builder.headers, builder.dataToSend, builder.localization, builder.automaticLocalizationHeader)

    /**
     * A http method (i.e. `GET, POST, HEAD`).
     */
    fun httpMethod(): String {
        return httpMethod
    }

    /**
     * The URL that is pointing to the wanted resource.
     */
    fun url(): String {
        return url
    }

    /**
     * A list of headers that will be used in the request.<br></br>
     * Any default headers that the implementation may have, **should** be overridden by these.
     */
    fun headers(): Map<String, List<String>> {
        return headers
    }

    /**
     * An optional byte array that will be sent when doing the request, very commonly used in
     * `POST` requests.<br></br>
     * <br></br>
     * The implementation should make note of some recommended headers
     * (for example, `Content-Length` in a post request).
     */
    fun dataToSend(): ByteArray? {
        return dataToSend
    }

    /**
     * A localization object that should be used when executing a request.<br></br>
     * <br></br>
     * Usually the `Accept-Language` will be set to this value (a helper
     * method to do this easily: [Request.getHeadersFromLocalization]).
     */
    fun localization(): Localization? {
        return localization
    }

    class Builder {
        internal var httpMethod: String = ""
        var url: String = ""
        val headers: MutableMap<String, List<String>> = LinkedHashMap()
        var dataToSend: ByteArray? = null
        var localization: Localization? = null
        var automaticLocalizationHeader: Boolean = true

        /**
         * A http method (i.e. `GET, POST, HEAD`).
         */
        fun httpMethod(httpMethodToSet: String): Builder {
            this.httpMethod = httpMethodToSet
            return this
        }

        /**
         * The URL that is pointing to the wanted resource.
         */
        fun url(urlToSet: String): Builder {
            this.url = urlToSet
            return this
        }

        /**
         * A list of headers that will be used in the request.<br></br>
         * Any default headers that the implementation may have, **should** be overridden by
         * these.
         */
        fun headers(headersToSet: Map<String, List<String>>?): Builder {
            headers.clear()
            if (headersToSet != null) headers.putAll(headersToSet)
            return this
        }

        /**
         * An optional byte array that will be sent when doing the request, very commonly used in
         * `POST` requests.<br></br>
         * <br></br>
         * The implementation should make note of some recommended headers
         * (for example, `Content-Length` in a post request).
         */
        fun dataToSend(dataToSendToSet: ByteArray?): Builder {
            this.dataToSend = dataToSendToSet
            return this
        }

        /**
         * A localization object that should be used when executing a request.<br></br>
         * <br></br>
         * Usually the `Accept-Language` will be set to this value (a helper
         * method to do this easily: [Request.getHeadersFromLocalization]).
         */
        fun localization(localizationToSet: Localization?): Builder {
            this.localization = localizationToSet
            return this
        }

        /**
         * If localization headers should automatically be included in the request.
         */
        fun automaticLocalizationHeader(automaticLocalizationHeaderToSet: Boolean): Builder {
            this.automaticLocalizationHeader = automaticLocalizationHeaderToSet
            return this
        }


        fun build(): Request {
            return Request(this)
        }

        fun get(urlToSet: String): Builder {
            this.httpMethod = "GET"
            this.url = urlToSet
            return this
        }

        fun head(urlToSet: String): Builder {
            this.httpMethod = "HEAD"
            this.url = urlToSet
            return this
        }

        fun post(urlToSet: String, dataToSendToSet: ByteArray?): Builder {
            this.httpMethod = "POST"
            this.url = urlToSet
            this.dataToSend = dataToSendToSet
            return this
        }

        fun setHeaders(headerName: String, headerValueList: List<String>): Builder {
            headers.remove(headerName)
            headers[headerName] = headerValueList
            return this
        }

        fun addHeaders(headerName: String, headerValueList: List<String>): Builder {
            val currentHeaderValueList = headers[headerName]?.toMutableList() ?: ArrayList()

            currentHeaderValueList.addAll(headerValueList)
            headers[headerName] = headerValueList
            return this
        }

        fun setHeader(headerName: String, headerValue: String): Builder {
            return setHeaders(headerName, listOf(headerValue))
        }

        fun addHeader(headerName: String, headerValue: String): Builder {
            return addHeaders(headerName, listOf(headerValue))
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val request = other as Request
        return httpMethod == request.httpMethod && url == request.url && headers == request.headers
                && dataToSend.contentEquals(request.dataToSend) && localization == request.localization
    }

    override fun hashCode(): Int {
        var result = Objects.hash(httpMethod, url, headers, localization)
        result = 31 * result + dataToSend.contentHashCode()
        return result
    }

    companion object {

        fun newBuilder(): Builder {
            return Builder()
        }

        fun getHeadersFromLocalization(localization: Localization?): Map<String, List<String>> {
            if (localization == null) return emptyMap()

            val languageCode = localization.languageCode
            val languageCodeList = listOf(
                if (localization.getCountryCode().isEmpty()) languageCode
                else localization.localizationCode + ", " + languageCode + ";q=0.9")
            return Collections.singletonMap("Accept-Language", languageCodeList)
        }
    }
}
