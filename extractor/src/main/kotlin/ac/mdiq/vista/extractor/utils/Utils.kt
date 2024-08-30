package ac.mdiq.vista.extractor.utils

import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.utils.Parser.RegexException
import ac.mdiq.vista.extractor.utils.Parser.isMatch
import ac.mdiq.vista.extractor.utils.Parser.matchGroup
import ac.mdiq.vista.extractor.utils.Parser.matchGroup1
import java.net.MalformedURLException
import java.net.URL
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.regex.Pattern
import java.util.stream.Collectors


object Utils {
    const val HTTP: String = "http://"
    const val HTTPS: String = "https://"
    private val M_PATTERN: Pattern = Pattern.compile("(https?)?://m\\.")
    private val WWW_PATTERN: Pattern = Pattern.compile("(https?)?://www\\.")

    /**
     * Encodes a string to URL format using the UTF-8 character set.
     *
     * @param string The string to be encoded.
     * @return The encoded URL.
     */
    fun encodeUrlUtf8(string: String?): String {
        // TODO: Switch to URLEncoder.encode(String, Charset) in Java 10.
        return URLEncoder.encode(string, StandardCharsets.UTF_8.name())
    }

    /**
     * Decodes a URL using the UTF-8 character set.
     * @param url The URL to be decoded.
     * @return The decoded URL.
     */
    fun decodeUrlUtf8(url: String?): String {
        // TODO: Switch to URLDecoder.decode(String, Charset) in Java 10.
        return URLDecoder.decode(url, StandardCharsets.UTF_8.name())
    }

    /**
     * Remove all non-digit characters from a string.
     *
     * Examples:
     *
     *  * 1 234 567 views -&gt; 1234567
     *  * $31,133.124 -&gt; 31133124
     *
     * @param toRemove string to remove non-digit chars
     * @return a string that contains only digits
     */
    fun removeNonDigitCharacters(toRemove: String): String {
        return toRemove.replace("\\D+".toRegex(), "")
    }

    /**
     * Convert a mixed number word to a long.
     *
     * Examples:
     *
     *  * 123 -&gt; 123
     *  * 1.23K -&gt; 1230
     *  * 1.23M -&gt; 1230000
     *
     * @param numberWord string to be converted to a long
     * @return a long
     */
    @Throws(NumberFormatException::class, ParsingException::class)
    fun mixedNumberWordToLong(numberWord: String?): Long {
        var multiplier = ""
        try { multiplier = matchGroup("[\\d]+([\\.,][\\d]+)?([KMBkmb])+", numberWord!!, 2) } catch (ignored: ParsingException) { }
        val count = matchGroup1("([\\d]+([\\.,][\\d]+)?)", numberWord!!).replace(",", ".").toDouble()
        return when (multiplier.uppercase(Locale.getDefault())) {
            "K" -> (count * 1e3).toLong()
            "M" -> (count * 1e6).toLong()
            "B" -> (count * 1e9).toLong()
            else -> count.toLong()
        }
    }

    /**
     * Check if the url matches the pattern.
     *
     * @param pattern the pattern that will be used to check the url
     * @param url     the url to be tested
     */
    @Throws(ParsingException::class)
    fun checkUrl(pattern: String, url: String) {
        require(!url.isNullOrEmpty()) { "Url can't be null or empty" }

        if (!isMatch(pattern, url.lowercase(Locale.getDefault()))) throw ParsingException("Url don't match the pattern")
    }

    fun replaceHttpWithHttps(url: String): String {
        if (url.startsWith(HTTP)) return HTTPS + url.substring(HTTP.length)
        return url
    }

    /**
     * Get the value of a URL-query by name.
     *
     * If an url-query is give multiple times, only the value of the first query is returned.
     *
     * @param url           the url to be used
     * @param parameterName the pattern that will be used to check the url
     * @return a string that contains the value of the query parameter or `null` if nothing
     * was found
     */
    fun getQueryValue(url: URL, parameterName: String): String? {
        val urlQuery = url.query

        if (urlQuery != null) {
            for (param in urlQuery.split("&".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {
                val params = param.split("=".toRegex(), limit = 2).toTypedArray()
                val query = decodeUrlUtf8(params[0])
                if (query == parameterName) return decodeUrlUtf8(params[1])
            }
        }

        return null
    }

    /**
     * Convert a string to a [URL object][URL].
     *
     * Defaults to HTTP if no protocol is given.
     *
     * @param url the string to be converted to a URL-Object
     * @return a [URL object][URL] containing the url
     */
    @Throws(MalformedURLException::class)
    fun stringToURL(url: String): URL {
        try {
            return URL(url)
        } catch (e: MalformedURLException) {
            // If no protocol is given try prepending "https://"
            if (e.message == "no protocol: $url") return URL(HTTPS + url)
            throw e
        }
    }

    fun isHTTP(url: URL): Boolean {
        // Make sure it's HTTP or HTTPS
        val protocol = url.protocol
        if (protocol != "http" && protocol != "https") return false
        val usesDefaultPort = url.port == url.defaultPort
        val setsNoPort = url.port == -1

        return setsNoPort || usesDefaultPort
    }

    fun removeMAndWWWFromUrl(url: String): String {
        if (M_PATTERN.matcher(url).find()) return url.replace("m.", "")
        if (WWW_PATTERN.matcher(url).find()) return url.replace("www.", "")
        return url
    }

    fun removeUTF8BOM(s: String): String {
        var result = s
        if (result.startsWith("\uFEFF")) result = result.substring(1)
        if (result.endsWith("\uFEFF")) result = result.substring(0, result.length - 1)
        return result
    }

    @Throws(ParsingException::class)
    fun getBaseUrl(url: String): String {
        try {
            val uri = stringToURL(url)
            return uri.protocol + "://" + uri.authority
        } catch (e: MalformedURLException) {
            val message = e.message
            // Return just the protocol (e.g. vnd.youtube)
            if (message!!.startsWith("unknown protocol: ")) return message.substring("unknown protocol: ".length)

            throw ParsingException("Malformed url: $url", e)
        }
    }

    /**
     * If the provided url is a Google search redirect, then the actual url is extracted from the
     * `url=` query value and returned, otherwise the original url is returned.
     *
     * @param url the url which can possibly be a Google search redirect
     * @return an url with no Google search redirects
     */
    fun followGoogleRedirectIfNeeded(url: String): String {
        // If the url is a redirect from a Google search, extract the actual URL
        try {
            val decoded = stringToURL(url)
            if (decoded.host.contains("google") && decoded.path == "/url") return decodeUrlUtf8(matchGroup1("&url=([^&]+)(?:&|$)", url))
        } catch (ignored: Exception) { }

        // URL is not a Google search redirect
        return url
    }

    fun join(delimiter: String?, mapJoin: String, elements: Map<out CharSequence, CharSequence>): String {
        return elements.entries.stream()
            .map { entry: Map.Entry<CharSequence, CharSequence> -> entry.key.toString() + mapJoin + entry.value }
            .collect(Collectors.joining(delimiter))
    }

    /**
     * Concatenate all non-null, non-empty and strings which are not equal to `"null"`.
     */
    fun nonEmptyAndNullJoin(delimiter: CharSequence?, vararg elements: String): String {
        return Arrays.stream(elements)
            .filter { s: String -> !s.isNullOrEmpty() && s != "null" }
            .collect(Collectors.joining(delimiter))
    }

    /**
     * Find the result of an array of string regular expressions inside an input on the first
     * group (`0`).
     *
     * @param input   the input on which using the regular expressions
     * @param regexes the string array of regular expressions
     * @return the result
     * @throws Parser.RegexException if none of the patterns match the input
     */
    @Throws(RegexException::class)
    fun getStringResultFromRegexArray(input: String, regexes: Array<String?>): String {
        return getStringResultFromRegexArray(input, regexes, 0)
    }

    /**
     * Find the result of an array of [Pattern]s inside an input on the first group
     * (`0`).
     *
     * @param input   the input on which using the regular expressions
     * @param regexes the [Pattern] array
     * @return the result
     * @throws Parser.RegexException if none of the patterns match the input
     */
    @Throws(RegexException::class)
    fun getStringResultFromRegexArray(input: String, regexes: Array<Pattern?>): String {
        return getStringResultFromRegexArray(input, regexes, 0)
    }

    /**
     * Find the result of an array of string regular expressions inside an input on a specific
     * group.
     *
     * @param input   the input on which using the regular expressions
     * @param regexes the string array of regular expressions
     * @param group   the group to match
     * @return the result
     * @throws Parser.RegexException if none of the patterns match the input, or at least in the
     * specified group
     */
    @Throws(RegexException::class)
    fun getStringResultFromRegexArray(input: String, regexes: Array<String?>, group: Int): String {
//        return getStringResultFromRegexArray(input,
//            Arrays.stream<String?>(regexes)
//                .filter { obj: String? -> Objects.nonNull(obj) }
//                .map { regex: String -> Pattern.compile(regex) }
//                .toList(), group)
        return getStringResultFromRegexArray(input, regexes.map { Pattern.compile(it) }.filterNotNull().toTypedArray(), group)
    }

    /**
     * Find the result of an array of [Pattern]s inside an input on a specific
     * group.
     *
     * @param input   the input on which using the regular expressions
     * @param regexes the [Pattern] array
     * @param group   the group to match
     * @return the result
     * @throws Parser.RegexException if none of the patterns match the input, or at least in the
     * specified group
     */
    @Throws(RegexException::class)
    fun getStringResultFromRegexArray(input: String, regexes: Array<Pattern?>, group: Int): String {
        for (regex in regexes) {
            try {
                val result = matchGroup(regex!!, input, group)
                if (result != null) return result

                // Continue if the result is null
            } catch (ignored: RegexException) { }
        }
        throw RegexException("No regex matched the input on group $group")
    }
}
