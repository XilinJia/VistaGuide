package ac.mdiq.vista.extractor

import java.io.Serializable

class Page @JvmOverloads constructor(
        @JvmField val url: String?,
        @JvmField val id: String? = null,
        @JvmField val ids: List<String?>? = null,
        @JvmField val cookies: Map<String, String>? = null,
        @JvmField val body: ByteArray? = null) : Serializable {

    constructor(url: String?, id: String?, body: ByteArray?) : this(url, id, null, null, body)

    constructor(url: String?, body: ByteArray?) : this(url, null, null, null, body)

    constructor(url: String?, cookies: Map<String, String>?) : this(url, null, null, cookies, null)

    constructor(ids: List<String?>?) : this(null, null, ids, null, null)

    constructor(ids: List<String?>?, cookies: Map<String, String>?) : this(null, null, ids, cookies, null)

    companion object {

        fun isValid(page: Page?): Boolean {
            return page != null && (!page.url.isNullOrEmpty() || !page.ids.isNullOrEmpty())
        }
    }
}
