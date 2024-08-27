package ac.mdiq.vista.extractor.downloader

import ac.mdiq.vista.extractor.Vista.getPreferredLocalization
//import ac.mdiq.vista.extractor.downloader.Request.Builder.headers
//import ac.mdiq.vista.extractor.downloader.Request.Builder.localization
import ac.mdiq.vista.extractor.downloader.Request.Companion.newBuilder
import ac.mdiq.vista.extractor.exceptions.ReCaptchaException
import ac.mdiq.vista.extractor.localization.Localization
import java.io.IOException


/**
 * A base for downloader implementations that Vista will use
 * to download needed resources during extraction.
 */
abstract class Downloader {
    /**
     * Do a GET request to get the resource that the url is pointing to.<br></br>
     * <br></br>
     * It will set the `Accept-Language` header to the language of the localization parameter.
     *
     * @param url          the URL that is pointing to the wanted resource
     * @param localization the source of the value of the `Accept-Language` header
     * @return the result of the GET request
     */
    @Throws(IOException::class, ReCaptchaException::class)
    fun get(url: String, localization: Localization?): Response {
        return get(url, null, localization)
    }

    /**
     * Do a GET request with the specified headers.
     *
     * @param url     the URL that is pointing to the wanted resource
     * @param headers a list of headers that will be used in the request.
     * Any default headers **should** be overridden by these.
     * @return the result of the GET request
     */
    @JvmOverloads
    @Throws(IOException::class, ReCaptchaException::class)
    fun get(url: String, headers: Map<String, List<String>>? = null, localization: Localization? = getPreferredLocalization()): Response {
        return execute(newBuilder()
            .get(url)
            .headers(headers)
            .localization(localization)
            .build())
    }

    /**
     * Do a HEAD request.
     *
     * @param url the URL that is pointing to the wanted resource
     * @return the result of the HEAD request
     */
    @JvmOverloads
    @Throws(IOException::class, ReCaptchaException::class)
    fun head(url: String, headers: Map<String, List<String>>? = null): Response {
        return execute(newBuilder()
            .head(url)
            .headers(headers)
            .build())
    }

    /**
     * Do a POST request with the specified headers, sending the data array.
     *
     * @param url        the URL that is pointing to the wanted resource
     * @param headers    a list of headers that will be used in the request.
     * Any default headers **should** be overridden by these.
     * @param dataToSend byte array that will be sent when doing the request.
     * @return the result of the POST request
     */
    @JvmOverloads
    @Throws(IOException::class, ReCaptchaException::class)
    fun post(url: String, headers: Map<String, List<String>>?, dataToSend: ByteArray?, localization: Localization? = getPreferredLocalization()): Response {
        return execute(newBuilder()
            .post(url, dataToSend)
            .headers(headers)
            .localization(localization)
            .build())
    }

    /**
     * Convenient method to send a POST request using the specified value of the
     * `Content-Type` header with a given [Localization].
     *
     * @param url          the URL that is pointing to the wanted resource
     * @param headers      a list of headers that will be used in the request.
     * Any default headers **should** be overridden by these.
     * @param dataToSend   byte array that will be sent when doing the request.
     * @param localization the source of the value of the `Accept-Language` header
     * @param contentType  the mime type of the body sent, which will be set as the value of the
     * `Content-Type` header
     * @return the result of the POST request
     * @see .post
     */
    @Throws(IOException::class, ReCaptchaException::class)
    fun postWithContentType(url: String, headers: Map<String, List<String>>?, dataToSend: ByteArray?, localization: Localization?, contentType: String): Response {
        val actualHeaders: MutableMap<String, List<String>> = HashMap()
        if (headers != null) actualHeaders.putAll(headers)
        actualHeaders["Content-Type"] = listOf(contentType)
        return post(url, actualHeaders, dataToSend, localization)
    }

    /**
     * Convenient method to send a POST request using the specified value of the
     * `Content-Type` header.
     *
     * @param url         the URL that is pointing to the wanted resource
     * @param headers     a list of headers that will be used in the request.
     * Any default headers **should** be overridden by these.
     * @param dataToSend  byte array that will be sent when doing the request.
     * @param contentType the mime type of the body sent, which will be set as the value of the
     * `Content-Type` header
     * @return the result of the POST request
     * @see .post
     */
    @Throws(IOException::class, ReCaptchaException::class)
    fun postWithContentType(url: String, headers: Map<String, List<String>>?, dataToSend: ByteArray?, contentType: String): Response {
        return postWithContentType(url, headers, dataToSend, getPreferredLocalization(), contentType)
    }

    /**
     * Convenient method to send a POST request the JSON mime type as the value of the
     * `Content-Type` header.
     *
     * @param url         the URL that is pointing to the wanted resource
     * @param headers     a list of headers that will be used in the request.
     * Any default headers **should** be overridden by these.
     * @param dataToSend  byte array that will be sent when doing the request.
     * @return the result of the POST request
     * @see .post
     */
    @JvmOverloads
    @Throws(IOException::class, ReCaptchaException::class)
    fun postWithContentTypeJson(url: String, headers: Map<String, List<String>>?, dataToSend: ByteArray?, localization: Localization? = getPreferredLocalization()): Response {
        return postWithContentType(url, headers, dataToSend, localization, "application/json")
    }

    /**
     * Do a request using the specified [Request] object.
     *
     * @return the result of the request
     */
    @Throws(IOException::class, ReCaptchaException::class)
    abstract fun execute(request: Request): Response
}
