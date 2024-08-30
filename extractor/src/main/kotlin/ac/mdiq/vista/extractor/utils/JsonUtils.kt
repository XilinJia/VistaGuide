package ac.mdiq.vista.extractor.utils

import com.grack.nanojson.JsonArray
import com.grack.nanojson.JsonObject
import com.grack.nanojson.JsonParser
import com.grack.nanojson.JsonParserException
import org.jsoup.Jsoup
import ac.mdiq.vista.extractor.exceptions.ParsingException
import java.util.*
import java.util.stream.Collectors


object JsonUtils {


    @Throws(ParsingException::class)
    fun getValue(`object`: JsonObject, path: String): Any {
        val keys = listOf(*path.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())
        val parentObject = getObject(`object`, keys.subList(0, keys.size - 1)) ?: throw ParsingException("Unable to get $path")

        val result = parentObject[keys[keys.size - 1]] ?: throw ParsingException("Unable to get $path")
        return result
    }

    @Throws(ParsingException::class)
    private fun <T> getInstanceOf(`object`: JsonObject, path: String, klass: Class<T>): T {
        val value = getValue(`object`, path)
        if (klass.isInstance(value)) return klass.cast(value)
        else throw ParsingException("Wrong data type at path $path")
    }



    @Throws(ParsingException::class)
    fun getString(`object`: JsonObject, path: String): String {
        return getInstanceOf(`object`, path, String::class.java)
    }



    @Throws(ParsingException::class)
    fun getBoolean(`object`: JsonObject, path: String): Boolean {
        return getInstanceOf(`object`, path, Boolean::class.java)
    }



    @Throws(ParsingException::class)
    fun getNumber(`object`: JsonObject, path: String): Number {
        return getInstanceOf(`object`, path, Number::class.java)
    }



    @Throws(ParsingException::class)
    fun getObject(`object`: JsonObject, path: String): JsonObject {
        return getInstanceOf(`object`, path, JsonObject::class.java)
    }



    @Throws(ParsingException::class)
    fun getArray(`object`: JsonObject, path: String): JsonArray {
        return getInstanceOf(`object`, path, JsonArray::class.java)
    }



    @Throws(ParsingException::class)
    fun getValues(array: JsonArray, path: String): List<Any> {
        val result: MutableList<Any> = ArrayList()
        for (i in array.indices) {
            val obj = array.getObject(i)
            result.add(getValue(obj, path))
        }
        return result
    }

    private fun getObject(`object`: JsonObject, keys: List<String>): JsonObject? {
        var result: JsonObject? = `object`
        for (key in keys) {
            result = result!!.getObject(key)
            if (result == null) break
        }
        return result
    }

    @Throws(ParsingException::class)
    fun toJsonArray(responseBody: String?): JsonArray {
        try { return JsonParser.array().from(responseBody) } catch (e: JsonParserException) { throw ParsingException("Could not parse JSON", e) }
    }


    @Throws(ParsingException::class)
    fun toJsonObject(responseBody: String?): JsonObject {
        try { return JsonParser.`object`().from(responseBody) } catch (e: JsonParserException) { throw ParsingException("Could not parse JSON", e) }
    }

    /**
     *
     * Get an attribute of a web page as JSON
     *
     * Originally a part of bandcampDirect.
     *
     * Example HTML:
     * <pre>
     * `<p data-town="{&quot;name&quot;:&quot;Mycenae&quot;,&quot;country&quot;:&quot;Greece&quot;}">
     * This is Sparta!</p>
    ` *
    </pre> *
     *
     * Calling this function to get the attribute `data-town` returns the JsonObject
     * for
     * <pre>
     * `{
     * "name": "Mycenae",
     * "country": "Greece"
     * }
    ` *
    </pre> *
     *
     * @param html     The HTML where the JSON we're looking for is stored inside a
     * variable inside some JavaScript block
     * @param variable Name of the variable
     * @return The JsonObject stored in the variable with this name
     */

    @Throws(JsonParserException::class, ArrayIndexOutOfBoundsException::class)
    fun getJsonData(html: String, variable: String): JsonObject {
        val document = Jsoup.parse(html)
        val json = document.getElementsByAttribute(variable).attr(variable)
        return JsonParser.`object`().from(json)
    }


    fun getStringListFromJsonArray(array: JsonArray): List<String> {
        return array.stream()
            .filter { o: Any? -> String::class.java.isInstance(o) }
            .map { obj: Any? -> String::class.java.cast(obj) }
            .collect(Collectors.toList())
    }
}
