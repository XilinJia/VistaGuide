package ac.mdiq.vista.extractor.services.youtube.linkHandler

import ac.mdiq.vista.extractor.exceptions.FoundAdException
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.linkhandler.ListLinkHandlerFactory

class YoutubeCommentsLinkHandlerFactory private constructor() : ListLinkHandlerFactory() {
    @Throws(ParsingException::class, UnsupportedOperationException::class)
    override fun getUrl(id: String): String {
        return "https://www.youtube.com/watch?v=$id"
    }

    @Throws(ParsingException::class, UnsupportedOperationException::class)
    override fun getId(url: String): String {
        // We need the same id, avoids duplicate code
        return YoutubeStreamLinkHandlerFactory.instance.getId(url)
    }

    @Throws(FoundAdException::class)
    override fun onAcceptUrl(url: String): Boolean {
        try {
            getId(url)
            return true
        } catch (fe: FoundAdException) {
            throw fe
        } catch (e: ParsingException) {
            return false
        }
    }

    @Throws(ParsingException::class, UnsupportedOperationException::class)
    override fun getUrl(id: String, contentFilters: List<String>, sortFilter: String?): String {
        return getUrl(id)
    }

    companion object {
        val instance: YoutubeCommentsLinkHandlerFactory = YoutubeCommentsLinkHandlerFactory()
    }
}
