package ac.mdiq.vista.extractor

import ac.mdiq.vista.extractor.downloader.Downloader
import ac.mdiq.vista.extractor.exceptions.ExtractionException
import ac.mdiq.vista.extractor.localization.ContentCountry
import ac.mdiq.vista.extractor.localization.Localization


/*
* Created by Christian Schabesberger on 23.08.15.
*
* Copyright (C) 2015 Christian Schabesberger <chris.schabesberger@mailbox.org>
* Copyright (C) 2024 Xilin Jia <https://github.com/XilinJia>
*
* Vista Guide is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* Vista Guide is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with Vista Guide.  If not, see <http://www.gnu.org/licenses/>.
*/

/**
 * Provides access to streaming services supported by Vista.
 */
object Vista {

    lateinit var downloader: Downloader
        private set
    private var preferredLocalization: Localization? = null
    private var preferredContentCountry: ContentCountry? = null


    val services: List<StreamingService>
        get() = ServiceList.all()

    @JvmOverloads
    fun init(d: Downloader, l: Localization = Localization.DEFAULT,
             c: ContentCountry? = if (l.getCountryCode().isEmpty()) ContentCountry.DEFAULT else ContentCountry(l.getCountryCode())) {
        downloader = d
        preferredLocalization = l
        preferredContentCountry = c
    }


    @Throws(ExtractionException::class)
    fun getService(serviceId: Int): StreamingService {
        return ServiceList.all().stream()
            .filter { service: StreamingService -> service.serviceId == serviceId }
            .findFirst()
            .orElseThrow { ExtractionException("There's no service with the id = \"$serviceId\"") }
    }

    @Throws(ExtractionException::class)
    fun getService(serviceName: String): StreamingService {
        return ServiceList.all().stream()
            .filter { service: StreamingService -> service.serviceInfo.name == serviceName }
            .findFirst()
            .orElseThrow { ExtractionException("There's no service with the name = \"$serviceName\"") }
    }


    @Throws(ExtractionException::class)
    fun getServiceByUrl(url: String): StreamingService {
        for (service in ServiceList.all()) {
            if (service.getLinkTypeByUrl(url) != StreamingService.LinkType.NONE) return service
        }
        throw ExtractionException("No service can handle the url = \"$url\"")
    }

    @JvmOverloads
    fun setupLocalization(thePreferredLocalization: Localization, thePreferredContentCountry: ContentCountry? = null) {
        preferredLocalization = thePreferredLocalization
        preferredContentCountry = thePreferredContentCountry
            ?: if (thePreferredLocalization.getCountryCode().isEmpty()) ContentCountry.DEFAULT else ContentCountry(thePreferredLocalization.getCountryCode())
    }



    fun getPreferredLocalization(): Localization {
        return if (preferredLocalization == null) Localization.DEFAULT else preferredLocalization!!
    }

    fun setPreferredLocalization(preferredLocalization: Localization?) {
        Vista.preferredLocalization = preferredLocalization
    }



    fun getPreferredContentCountry(): ContentCountry {
        return if (preferredContentCountry == null) ContentCountry.DEFAULT else preferredContentCountry!!
    }

    fun setPreferredContentCountry(preferredContentCountry: ContentCountry?) {
        Vista.preferredContentCountry = preferredContentCountry
    }
}
