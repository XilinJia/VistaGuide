package ac.mdiq.vista.extractor.stream

import java.io.Serializable
import java.util.*

class Description(content: String?, val type: Int) : Serializable {
    @JvmField
    var content: String = ""

    init {
        this.content = content ?: ""
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val that = o as Description
        return type == that.type && content == that.content
    }

    override fun hashCode(): Int {
        return Objects.hash(content, type)
    }

    companion object {
        const val HTML: Int = 1
        const val MARKDOWN: Int = 2
        const val PLAIN_TEXT: Int = 3
        @JvmField
        val EMPTY_DESCRIPTION: Description = Description("", PLAIN_TEXT)
    }
}
