package ac.mdiq.vista.extractor.services.soundcloud.linkHandler

import ac.mdiq.vista.extractor.exceptions.ExtractionException
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.exceptions.ReCaptchaException
import ac.mdiq.vista.extractor.linkhandler.SearchQueryHandlerFactory
import ac.mdiq.vista.extractor.services.soundcloud.SoundcloudParsingHelper
import ac.mdiq.vista.extractor.utils.Utils.encodeUrlUtf8
import java.io.IOException
import java.io.UnsupportedEncodingException

class SoundcloudSearchQueryHandlerFactory private constructor() : SearchQueryHandlerFactory() {
    @Throws(ParsingException::class, UnsupportedOperationException::class)
    override fun getUrl(id: String, contentFilters: List<String>, sortFilter: String?): String {
        try {
            var url = SoundcloudParsingHelper.SOUNDCLOUD_API_V2_URL + "search"

            if (contentFilters.isNotEmpty()) {
                when (contentFilters[0]) {
                    TRACKS -> url += "/tracks"
                    USERS -> url += "/users"
                    PLAYLISTS -> url += "/playlists"
                    ALL -> {}
                    else -> {}
                }
            }
            return ("$url?q=${encodeUrlUtf8(id)}&client_id=${SoundcloudParsingHelper.clientId()}&limit=$ITEMS_PER_PAGE&offset=0")
        } catch (e: UnsupportedEncodingException) {
            throw ParsingException("Could not encode query", e)
        } catch (e: ReCaptchaException) {
            throw ParsingException("ReCaptcha required", e)
        } catch (e: IOException) {
            throw ParsingException("Could not get client id", e)
        } catch (e: ExtractionException) {
            throw ParsingException("Could not get client id", e)
        }
    }

    override val availableContentFilter: Array<String>
        get() = arrayOf(ALL, TRACKS, USERS, PLAYLISTS)

    companion object {
        val instance: SoundcloudSearchQueryHandlerFactory = SoundcloudSearchQueryHandlerFactory()

        const val TRACKS: String = "tracks"
        const val USERS: String = "users"
        const val PLAYLISTS: String = "playlists"
        const val ALL: String = "all"

        const val ITEMS_PER_PAGE: Int = 10
    }
}
