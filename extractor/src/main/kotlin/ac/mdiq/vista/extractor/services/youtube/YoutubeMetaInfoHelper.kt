package ac.mdiq.vista.extractor.services.youtube

import com.grack.nanojson.JsonArray
import com.grack.nanojson.JsonObject
import ac.mdiq.vista.extractor.MetaInfo
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.extractCachedUrlIfNeeded
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.getTextFromObject
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.getTextFromObjectOrThrow
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.getUrlFromNavigationEndpoint
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.isGoogleURL
import ac.mdiq.vista.extractor.stream.Description
import ac.mdiq.vista.extractor.utils.Utils.replaceHttpWithHttps
import java.net.MalformedURLException
import java.net.URL
import java.util.*
import java.util.function.Consumer
import java.util.stream.Collectors


object YoutubeMetaInfoHelper {
    @Throws(ParsingException::class)
    fun getMetaInfo(contents: JsonArray): List<MetaInfo> {
        val metaInfo: MutableList<MetaInfo> = ArrayList()
        for (content in contents) {
            val resultObject = content as JsonObject
            if (resultObject.has("itemSectionRenderer")) {
                for (sectionContentObject in resultObject.getObject("itemSectionRenderer").getArray("contents")) {
                    val sectionContent = sectionContentObject as JsonObject
                    if (sectionContent.has("infoPanelContentRenderer")) metaInfo.add(getInfoPanelContent(sectionContent.getObject("infoPanelContentRenderer")))
                    if (sectionContent.has("clarificationRenderer")) metaInfo.add(getClarificationRenderer(sectionContent.getObject("clarificationRenderer")))
                    if (sectionContent.has("emergencyOneboxRenderer")) getEmergencyOneboxRenderer(sectionContent.getObject("emergencyOneboxRenderer")) {
                        e: MetaInfo -> metaInfo.add(e) }
                }
            }
        }
        return metaInfo
    }

    @Throws(ParsingException::class)
    private fun getInfoPanelContent(infoPanelContentRenderer: JsonObject): MetaInfo {
        val metaInfo = MetaInfo()
        val sb = StringBuilder()
        for (paragraph in infoPanelContentRenderer.getArray("paragraphs")) {
            if (sb.isNotEmpty()) sb.append("<br>")
            sb.append(getTextFromObject((paragraph as JsonObject)))
        }
        metaInfo.content = Description(sb.toString(), Description.HTML)
        if (infoPanelContentRenderer.has("sourceEndpoint")) {
            val metaInfoLinkUrl = getUrlFromNavigationEndpoint(infoPanelContentRenderer.getObject("sourceEndpoint"))
            try {
                metaInfo.addUrl(URL(Objects.requireNonNull(extractCachedUrlIfNeeded(metaInfoLinkUrl))))
            } catch (e: NullPointerException) {
                throw ParsingException("Could not get metadata info URL", e)
            } catch (e: MalformedURLException) {
                throw ParsingException("Could not get metadata info URL", e)
            }

            val metaInfoLinkText = getTextFromObject(infoPanelContentRenderer.getObject("inlineSource"))
            if (metaInfoLinkText.isNullOrEmpty()) throw ParsingException("Could not get metadata info link text.")
            metaInfo.addUrlText(metaInfoLinkText)
        }

        return metaInfo
    }

    @Throws(ParsingException::class)
    private fun getClarificationRenderer(clarificationRenderer: JsonObject): MetaInfo {
        val metaInfo = MetaInfo()

        val title = getTextFromObject(clarificationRenderer.getObject("contentTitle"))
        val text = getTextFromObject(clarificationRenderer.getObject("text"))
        if (title == null || text == null) throw ParsingException("Could not extract clarification renderer content")
        metaInfo.title = title
        metaInfo.content = Description(text, Description.PLAIN_TEXT)

        if (clarificationRenderer.has("actionButton")) {
            val actionButton = clarificationRenderer.getObject("actionButton").getObject("buttonRenderer")
            try {
                val url = getUrlFromNavigationEndpoint(actionButton.getObject("command"))
                metaInfo.addUrl(URL(Objects.requireNonNull(extractCachedUrlIfNeeded(url))))
            } catch (e: NullPointerException) {
                throw ParsingException("Could not get metadata info URL", e)
            } catch (e: MalformedURLException) {
                throw ParsingException("Could not get metadata info URL", e)
            }

            val metaInfoLinkText = getTextFromObject(actionButton.getObject("text"))
            if (metaInfoLinkText.isNullOrEmpty()) throw ParsingException("Could not get metadata info link text.")
            metaInfo.addUrlText(metaInfoLinkText)
        }

        if (clarificationRenderer.has("secondaryEndpoint") && clarificationRenderer.has("secondarySource")) {
            val url = getUrlFromNavigationEndpoint(clarificationRenderer.getObject("secondaryEndpoint"))
            // Ignore Google URLs, because those point to a Google search about "Covid-19"
            if (url != null && !isGoogleURL(url)) {
                try {
                    metaInfo.addUrl(URL(url))
                    val description = getTextFromObject(clarificationRenderer.getObject("secondarySource"))
                    metaInfo.addUrlText(description ?: url)
                } catch (e: MalformedURLException) {
                    throw ParsingException("Could not get metadata info secondary URL", e)
                }
            }
        }

        return metaInfo
    }

    @Throws(ParsingException::class)
    private fun getEmergencyOneboxRenderer(emergencyOneboxRenderer: JsonObject, addMetaInfo: Consumer<MetaInfo>) {
        val supportRenderers = emergencyOneboxRenderer.values
            .stream()
            .filter { o: Any? -> (o is JsonObject && o.has("singleActionEmergencySupportRenderer")) }
            .map { o: Any -> (o as JsonObject).getObject("singleActionEmergencySupportRenderer") }
            .collect(Collectors.toList())

        if (supportRenderers.isEmpty()) throw ParsingException("Could not extract any meta info from emergency renderer")

        for (r in supportRenderers) {
            val metaInfo = MetaInfo()

            // usually an encouragement like "We are with you"
            val title = getTextFromObjectOrThrow(r.getObject("title"), "title")
            // usually a phone number
            val action: String // this variable is expected to start with "\n"
            if (r.has("actionText")) {
                action = """

                    ${getTextFromObjectOrThrow(r.getObject("actionText"), "action")}
                    """.trimIndent()
            } else if (r.has("contacts")) {
                val contacts = r.getArray("contacts")
                val stringBuilder = java.lang.StringBuilder()
                // Loop over contacts item from the first contact to the last one
                for (i in contacts.indices) {
                    stringBuilder.append("\n")
                    stringBuilder.append(getTextFromObjectOrThrow(contacts.getObject(i).getObject("actionText"), "contacts.actionText"))
                }
                action = stringBuilder.toString()
            } else action = ""

            // usually details about the phone number
            val details = getTextFromObjectOrThrow(r.getObject("detailsText"), "details")
            // usually the name of an association
            val urlText = getTextFromObjectOrThrow(r.getObject("navigationText"), "urlText")

            metaInfo.title = title
            metaInfo.content = Description("""
    $details
    $action
    """.trimIndent(), Description.PLAIN_TEXT)
            metaInfo.addUrlText(urlText)

            // usually the webpage of the association
            val url = getUrlFromNavigationEndpoint(r.getObject("navigationEndpoint")) ?: throw ParsingException("Could not extract emergency renderer url")

            try {
                metaInfo.addUrl(URL(replaceHttpWithHttps(url)))
            } catch (e: MalformedURLException) {
                throw ParsingException("Could not parse emergency renderer url", e)
            }
            addMetaInfo.accept(metaInfo)
        }
    }
}
