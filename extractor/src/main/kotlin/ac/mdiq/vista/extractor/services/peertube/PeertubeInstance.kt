package ac.mdiq.vista.extractor.services.peertube

import com.grack.nanojson.JsonParser
import com.grack.nanojson.JsonParserException
import ac.mdiq.vista.extractor.Vista.downloader
import ac.mdiq.vista.extractor.downloader.Response
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.exceptions.ReCaptchaException
import ac.mdiq.vista.extractor.utils.JsonUtils.getString
import java.io.IOException

class PeertubeInstance {
    @JvmField
    val url: String
    var name: String
        private set

    constructor(url: String) {
        this.url = url
        this.name = "PeerTube"
    }

    constructor(url: String, name: String) {
        this.url = url
        this.name = name
    }

    @Throws(Exception::class)
    fun fetchInstanceMetaData() {
        val response: Response
        try {
            response = downloader.get("$url/api/v1/config")
        } catch (e: ReCaptchaException) {
            throw Exception("unable to configure instance $url", e)
        } catch (e: IOException) {
            throw Exception("unable to configure instance $url", e)
        }

        if (response == null || response.responseBody().isEmpty()) throw Exception("unable to configure instance $url")

        try {
            val json = JsonParser.`object`().from(response.responseBody())
            this.name = getString(json, "instance.name")
        } catch (e: JsonParserException) {
            throw Exception("unable to parse instance config", e)
        } catch (e: ParsingException) {
            throw Exception("unable to parse instance config", e)
        }
    }

    companion object {
        @JvmField
        val DEFAULT_INSTANCE: PeertubeInstance = PeertubeInstance("https://framatube.org", "FramaTube")
    }
}
