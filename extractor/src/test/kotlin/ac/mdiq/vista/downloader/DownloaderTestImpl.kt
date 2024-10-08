package ac.mdiq.vista.downloader

import okhttp3.OkHttpClient
import okhttp3.RequestBody
import ac.mdiq.vista.extractor.downloader.Downloader
import ac.mdiq.vista.extractor.downloader.Request
import ac.mdiq.vista.extractor.downloader.Response
import ac.mdiq.vista.extractor.exceptions.ReCaptchaException
import java.io.IOException
import java.util.concurrent.TimeUnit


class DownloaderTestImpl private constructor(builder: OkHttpClient.Builder) : Downloader() {
    private val client: OkHttpClient = builder.readTimeout(30, TimeUnit.SECONDS).build()

    @Throws(IOException::class, ReCaptchaException::class)
    override fun execute(request: Request): Response {
        val httpMethod = request.httpMethod()
        val url = request.url()
        val headers = request.headers()
        val dataToSend = request.dataToSend()

        var requestBody: RequestBody? = null
        if (dataToSend != null) {
            requestBody = RequestBody.create(null, dataToSend)
        }

        val requestBuilder = okhttp3.Request.Builder()
            .method(httpMethod, requestBody).url(url)
            .addHeader("User-Agent", USER_AGENT)

        for ((headerName, headerValueList) in headers) {
            if (headerValueList.size > 1) {
                requestBuilder.removeHeader(headerName)
                for (headerValue in headerValueList) {
                    requestBuilder.addHeader(headerName, headerValue)
                }
            } else if (headerValueList.size == 1) {
                requestBuilder.header(headerName, headerValueList[0])
            }
        }

        val response = client.newCall(requestBuilder.build()).execute()

        if (response.code == 429) {
            response.close()

            throw ReCaptchaException("reCaptcha Challenge requested", url!!)
        }

        val body = response.body
        var responseBodyToReturn: String? = null

        if (body != null) {
            responseBodyToReturn = body.string()
        }

        val latestUrl = response.request.url.toString()
        return Response(response.code, response.message, response.headers.toMultimap(),
            responseBodyToReturn, latestUrl)
    }

    companion object {
        private const val USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; rv:91.0) Gecko/20100101 Firefox/91.0"
        private var instance: DownloaderTestImpl? = null

        /**
         * It's recommended to call exactly once in the entire lifetime of the application.
         *
         * @param builder if null, default builder will be used
         * @return a new instance of [DownloaderTestImpl]
         */
        fun init(builder: OkHttpClient.Builder?): DownloaderTestImpl {
            instance = DownloaderTestImpl(builder ?: OkHttpClient.Builder())
            return instance!!
        }


        fun getInstance(): DownloaderTestImpl {
            if (instance == null) init(null)
            return instance!!
        }
    }
}
