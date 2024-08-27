package ac.mdiq.vista.extractor.kiosk

import ac.mdiq.vista.extractor.Vista.getPreferredLocalization
import ac.mdiq.vista.extractor.Page
import ac.mdiq.vista.extractor.StreamingService
import ac.mdiq.vista.extractor.exceptions.ExtractionException
import ac.mdiq.vista.extractor.linkhandler.ListLinkHandlerFactory
import ac.mdiq.vista.extractor.localization.ContentCountry
import ac.mdiq.vista.extractor.localization.Localization
import java.io.IOException

class KioskList(private val service: StreamingService) {

    interface KioskExtractorFactory {
        @Throws(ExtractionException::class, IOException::class)
        fun createNewKiosk(streamingService: StreamingService, url: String, kioskId: String): KioskExtractor<*>
    }

    private val kioskList = HashMap<String, KioskEntry?>()
    var defaultKioskId: String = ""
        private set

    private var forcedLocalization: Localization? = null
    private var forcedContentCountry: ContentCountry? = null

    private class KioskEntry(val extractorFactory: KioskExtractorFactory, val handlerFactory: ListLinkHandlerFactory)

    @Throws(Exception::class)
    fun addKioskEntry(extractorFactory: KioskExtractorFactory, handlerFactory: ListLinkHandlerFactory, id: String) {
        if (kioskList[id] != null) throw Exception("Kiosk with type $id already exists.")
        kioskList[id] = KioskEntry(extractorFactory, handlerFactory)
    }

    fun setDefaultKiosk(kioskType: String) {
        defaultKioskId = kioskType
    }

    @get:Throws(ExtractionException::class, IOException::class)
    val defaultKioskExtractor: KioskExtractor<*>?
        get() = getDefaultKioskExtractor(null)

    @Throws(ExtractionException::class, IOException::class)
    fun getDefaultKioskExtractor(nextPage: Page?): KioskExtractor<*>? {
        return getDefaultKioskExtractor(nextPage, getPreferredLocalization())
    }

    @Throws(ExtractionException::class, IOException::class)
    fun getDefaultKioskExtractor(nextPage: Page?, localization: Localization?): KioskExtractor<*>? {
        if (defaultKioskId.isNotEmpty()) return getExtractorById(defaultKioskId, nextPage, localization)

        val first = kioskList.keys.stream().findAny().orElse(null)
        // if not set get any entry
        return if (first != null) getExtractorById(first, nextPage, localization) else null
    }

    @Throws(ExtractionException::class, IOException::class)
    fun getExtractorById(kioskId: String, nextPage: Page?): KioskExtractor<*> {
        return getExtractorById(kioskId, nextPage, getPreferredLocalization())
    }

    @Throws(ExtractionException::class, IOException::class)
    fun getExtractorById(kioskId: String, nextPage: Page?, localization: Localization?): KioskExtractor<*> {
        val ke = kioskList[kioskId]
        if (ke == null) throw ExtractionException("No kiosk found with the type: $kioskId")
        else {
            val kioskExtractor = ke.extractorFactory.createNewKiosk(service, ke.handlerFactory.fromId(kioskId).url, kioskId)

            if (forcedLocalization != null) kioskExtractor.forceLocalization(forcedLocalization!!)
            if (forcedContentCountry != null) kioskExtractor.forceContentCountry(forcedContentCountry!!)

            return kioskExtractor
        }
    }

    val availableKiosks: Set<String?>
        get() = kioskList.keys

    @Throws(ExtractionException::class, IOException::class)
    fun getExtractorByUrl(url: String, nextPage: Page?): KioskExtractor<*> {
        return getExtractorByUrl(url, nextPage, getPreferredLocalization())
    }

    @Throws(ExtractionException::class, IOException::class)
    fun getExtractorByUrl(url: String, nextPage: Page?, localization: Localization?): KioskExtractor<*> {
        for ((_, ke) in kioskList) {
            if (ke!!.handlerFactory.acceptUrl(url)) return getExtractorById(ke.handlerFactory.getId(url), nextPage, localization)
        }
        throw ExtractionException("Could not find a kiosk that fits to the url: $url")
    }

    fun getListLinkHandlerFactoryByType(type: String?): ListLinkHandlerFactory {
        return kioskList[type]!!.handlerFactory
    }

    fun forceLocalization(localization: Localization?) {
        this.forcedLocalization = localization
    }

    fun forceContentCountry(contentCountry: ContentCountry?) {
        this.forcedContentCountry = contentCountry
    }
}
