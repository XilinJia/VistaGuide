package ac.mdiq.vista.extractor.services.youtube

import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.utils.JavaScript.compileOrThrow
import ac.mdiq.vista.extractor.utils.Parser.RegexException
import ac.mdiq.vista.extractor.utils.Parser.matchGroup1
import ac.mdiq.vista.extractor.utils.Parser.matchMultiplePatterns
import ac.mdiq.vista.extractor.utils.jsextractor.JavaScriptExtractor
import java.util.regex.Matcher
import java.util.regex.Pattern


/**
 * Utility class to get the throttling parameter decryption code and check if a streaming has the
 * throttling parameter.
 */
internal object YoutubeThrottlingParameterUtils {
    private val THROTTLING_PARAM_PATTERN: Pattern = Pattern.compile("[&?]n=([^&]+)")

    private const val SINGLE_CHAR_VARIABLE_REGEX: String = "[a-zA-Z0-9\$_]"

    private const val FUNCTION_NAME_REGEX: String = "$SINGLE_CHAR_VARIABLE_REGEX+"

    private const val ARRAY_ACCESS_REGEX: String = "\\[(\\d+)]"

    private val DEOBFUSCATION_FUNCTION_NAME_REGEXES: Array<Pattern> = arrayOf(
        /*
             * The first regex matches the following text, where we want rDa and the array index
             * accessed:
             * a.D&&(b="nn"[+a.D],c=a.get(b))&&(c=rDa[0](c),a.set(b,c),rDa.length||rma("")
             */Pattern.compile((SINGLE_CHAR_VARIABLE_REGEX + "+=\"nn\"\\[\\+"
            + SINGLE_CHAR_VARIABLE_REGEX + "+\\." + SINGLE_CHAR_VARIABLE_REGEX + "+],"
            + SINGLE_CHAR_VARIABLE_REGEX + "+=" + SINGLE_CHAR_VARIABLE_REGEX
            + "+\\.get\\(" + SINGLE_CHAR_VARIABLE_REGEX + "+\\)\\)&&\\("
            + SINGLE_CHAR_VARIABLE_REGEX + "+=(" + SINGLE_CHAR_VARIABLE_REGEX
            + "+)\\[(\\d+)]")),
        /*
             * The second regex matches the following text, where we want rma:
             * a.D&&(b="nn"[+a.D],c=a.get(b))&&(c=rDa[0](c),a.set(b,c),rDa.length||rma("")
             */
        Pattern.compile((SINGLE_CHAR_VARIABLE_REGEX + "+=\"nn\"\\[\\+"
                + SINGLE_CHAR_VARIABLE_REGEX + "+\\." + SINGLE_CHAR_VARIABLE_REGEX + "+],"
                + SINGLE_CHAR_VARIABLE_REGEX + "+=" + SINGLE_CHAR_VARIABLE_REGEX + "+\\.get\\("
                + SINGLE_CHAR_VARIABLE_REGEX + "+\\)\\).+\\|\\|(" + SINGLE_CHAR_VARIABLE_REGEX
                + "+)\\(\"\"\\)")),
        /*
             * The third regex matches the following text, where we want BDa and the array index accessed:
             * (b=String.fromCharCode(110),c=a.get(b))&&(c=BDa[0](c)
             */
        Pattern.compile(("\\(" + SINGLE_CHAR_VARIABLE_REGEX + "=String\\.fromCharCode\\(110\\),"
                + SINGLE_CHAR_VARIABLE_REGEX + "=" + SINGLE_CHAR_VARIABLE_REGEX + "\\.get\\("
                + SINGLE_CHAR_VARIABLE_REGEX + "\\)\\)" + "&&\\(" + SINGLE_CHAR_VARIABLE_REGEX
                + "=(" + FUNCTION_NAME_REGEX + ")" + "(?:" + ARRAY_ACCESS_REGEX + ")?\\("
                + SINGLE_CHAR_VARIABLE_REGEX + "\\)")),
        /*
             * The fourth regex matches the following text, where we want Yva and the array index accessed:
             * .get("n"))&&(b=Yva[0](b)
             */
        Pattern.compile(("\\.get\\(\"n\"\\)\\)&&\\(" + SINGLE_CHAR_VARIABLE_REGEX
                + "=(" + FUNCTION_NAME_REGEX + ")(?:" + ARRAY_ACCESS_REGEX + ")?\\("
                + SINGLE_CHAR_VARIABLE_REGEX + "\\)"))
    )


    // Escape the curly end brace to allow compatibility with Android's regex engine
    // See https://stackoverflow.com/q/45074813
    private const val DEOBFUSCATION_FUNCTION_BODY_REGEX =
        "=\\s*function([\\S\\s]*?\\}\\s*return [\\w$]+?\\.join\\(\"\"\\)\\s*\\};)"

    private const val DEOBFUSCATION_FUNCTION_ARRAY_OBJECT_TYPE_DECLARATION_REGEX = "var "

    private const val FUNCTION_NAMES_IN_DEOBFUSCATION_ARRAY_REGEX = "\\s*=\\s*\\[(.+?)][;,]"

    /**
     * Get the throttling parameter deobfuscation function name of YouTube's base JavaScript file.
     *
     * @param javaScriptPlayerCode the complete JavaScript base player code
     * @return the name of the throttling parameter deobfuscation function
     * @throws ParsingException if the name of the throttling parameter deobfuscation function
     * could not be extracted
     */

    @Throws(ParsingException::class)
    fun getDeobfuscationFunctionName(javaScriptPlayerCode: String): String {
        val matcher: Matcher
        try {
            matcher = matchMultiplePatterns(DEOBFUSCATION_FUNCTION_NAME_REGEXES, javaScriptPlayerCode)
        } catch (e: RegexException) {
            throw ParsingException("Could not find deobfuscation function with any of the " + "known patterns in the base JavaScript player code", e)
        }

        val functionName = matcher.group(1)
        if (matcher.groupCount() == 1) return functionName

        val arrayNum = matcher.group(2).toInt()
        val arrayPattern = Pattern.compile(DEOBFUSCATION_FUNCTION_ARRAY_OBJECT_TYPE_DECLARATION_REGEX
                + Pattern.quote(functionName) + FUNCTION_NAMES_IN_DEOBFUSCATION_ARRAY_REGEX)
        val arrayStr = matchGroup1(arrayPattern, javaScriptPlayerCode)
        val names = arrayStr.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        return names[arrayNum]
    }

    /**
     * Get the throttling parameter deobfuscation code of YouTube's base JavaScript file.
     *
     * @param javaScriptPlayerCode the complete JavaScript base player code
     * @return the throttling parameter deobfuscation function name
     * @throws ParsingException if the throttling parameter deobfuscation code couldn't be
     * extracted
     */

    @Throws(ParsingException::class)
    fun getDeobfuscationFunction(javaScriptPlayerCode: String, functionName: String): String {
        return try {
            parseFunctionWithLexer(javaScriptPlayerCode, functionName)
        } catch (e: Exception) {
            parseFunctionWithRegex(javaScriptPlayerCode, functionName)
        }
    }

    /**
     * Get the throttling parameter of a streaming URL if it exists.
     * @param streamingUrl a streaming URL
     * @return the throttling parameter of the streaming URL or `null` if no parameter has
     * been found
     */
    fun getThrottlingParameterFromStreamingUrl(streamingUrl: String): String? {
        return try {
            matchGroup1(THROTTLING_PARAM_PATTERN, streamingUrl)
        } catch (e: RegexException) {
            // If the throttling parameter could not be parsed from the URL, it means that there is
            // no throttling parameter
            // Return null in this case
            null
        }
    }


    @Throws(ParsingException::class)
    private fun parseFunctionWithLexer(javaScriptPlayerCode: String, functionName: String): String {
        val functionBase = "$functionName=function"
        return functionBase + JavaScriptExtractor.matchToClosingBrace(javaScriptPlayerCode, functionBase) + ";"
    }


    @Throws(RegexException::class)
    private fun parseFunctionWithRegex(javaScriptPlayerCode: String, functionName: String): String {
        // Quote the function name, as it may contain special regex characters such as dollar
        val functionPattern = Pattern.compile(Pattern.quote(functionName) + DEOBFUSCATION_FUNCTION_BODY_REGEX, Pattern.DOTALL)
        return validateFunction("function " + functionName + matchGroup1(functionPattern, javaScriptPlayerCode))
    }


    private fun validateFunction(function: String): String {
        compileOrThrow(function)
        return function
    }
}
