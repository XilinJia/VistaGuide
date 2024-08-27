package ac.mdiq.vista.downloader

import com.google.gson.GsonBuilder
import ac.mdiq.vista.downloader.DownloaderTestImpl.Companion.getInstance
import ac.mdiq.vista.downloader.RecordingDownloader
import ac.mdiq.vista.extractor.downloader.Downloader
import ac.mdiq.vista.extractor.downloader.Request
import ac.mdiq.vista.extractor.downloader.Response
import ac.mdiq.vista.extractor.exceptions.ReCaptchaException
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths


/**
 *
 *
 * Relays requests to [DownloaderTestImpl] and saves the request/response pair into a json
 * file.
 *
 *
 *
 * Those files are used by [MockDownloader].
 *
 *
 *
 * The files **must** be created on the local dev environment
 * and recreated when the requests made by a test class change.
 *
 *
 *
 * Run the test class as a whole and not each test separately.
 * Make sure the requests made by a class are unique.
 *
 */
internal class RecordingDownloader(private val path: String) : Downloader() {
    private var index = 0

    /**
     * Creates the folder described by `stringPath` if it does not exists.
     * Deletes existing files starting with [RecordingDownloader.FILE_NAME_PREFIX].
     * @param stringPath Path to the folder where the json files will be saved to.
     */
    init {
        val path = Paths.get(path)
        val folder = path.toFile()
        if (folder.exists()) {
            for (file in folder.listFiles()) {
                if (file.name.startsWith(FILE_NAME_PREFIX)) {
                    file.delete()
                }
            }
        } else {
            Files.createDirectories(path)
        }
    }

    @Throws(IOException::class, ReCaptchaException::class)
    override fun execute(request: Request): Response {
        val downloader: Downloader? = getInstance()
        var response = downloader!!.execute(request)
        val cleanedResponseBody = response.responseBody().replace(IP_V4_PATTERN.toRegex(), "127.0.0.1")
        response = Response(
            response.responseCode(),
            response.responseMessage(),
            response.responseHeaders(),
            cleanedResponseBody,
            response.latestUrl()
        )

        val outputFile = File((path + File.separator + FILE_NAME_PREFIX + index
                + ".json"))
        index++
        outputFile.createNewFile()
        val writer = OutputStreamWriter(FileOutputStream(outputFile),
            StandardCharsets.UTF_8)
        GsonBuilder()
            .setPrettyPrinting()
            .create()
            .toJson(TestRequestResponse(request, response), writer)
        writer.flush()
        writer.close()

        return response
    }

    companion object {
        const val FILE_NAME_PREFIX: String = "generated_mock_"

        // From https://stackoverflow.com/a/15875500/13516981
        private const val IP_V4_PATTERN =
            "(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)"
    }
}
