package ac.mdiq.vista.extractor.subscription

import java.io.Serializable

class SubscriptionItem(
        @JvmField val serviceId: Int,
        @JvmField val url: String,
        @JvmField val name: String) : Serializable {

    override fun toString(): String {
        return ("${javaClass.simpleName}@${Integer.toHexString(hashCode())}[name=$name > $serviceId:$url]")
    }
}
