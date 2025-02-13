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

    private const val MULTIPLE_CHARS_REGEX: String = "$SINGLE_CHAR_VARIABLE_REGEX+"

    private const val ARRAY_ACCESS_REGEX: String = "\\[(\\d+)]"

    private val DEOBFUSCATION_FUNCTION_NAME_REGEXES: Array<Pattern> = arrayOf(

        /*
         * Matches the following text, where we want SDa and the array index accessed:
         *
         * a.D&&(b="nn"[+a.D],WL(a),c=a.j[b]||null)&&(c=SDa[0](c),a.set(b,c),SDa.length||Wma("")
         */
        Pattern.compile(SINGLE_CHAR_VARIABLE_REGEX + "=\"nn\"\\[\\+" + MULTIPLE_CHARS_REGEX
                + "\\." + MULTIPLE_CHARS_REGEX + "]," + MULTIPLE_CHARS_REGEX + "\\("
                + MULTIPLE_CHARS_REGEX + "\\)," + MULTIPLE_CHARS_REGEX + "="
                + MULTIPLE_CHARS_REGEX + "\\." + MULTIPLE_CHARS_REGEX + "\\["
                + MULTIPLE_CHARS_REGEX + "]\\|\\|null\\)&&\\(" + MULTIPLE_CHARS_REGEX + "=("
                + MULTIPLE_CHARS_REGEX + ")" + ARRAY_ACCESS_REGEX),

        /*
         * Matches the following text, where we want Wma:
         *
         * a.D&&(b="nn"[+a.D],WL(a),c=a.j[b]||null)&&(c=SDa[0](c),a.set(b,c),SDa.length||Wma("")
         */
        Pattern.compile(SINGLE_CHAR_VARIABLE_REGEX + "=\"nn\"\\[\\+" + MULTIPLE_CHARS_REGEX
                + "\\." + MULTIPLE_CHARS_REGEX + "]," + MULTIPLE_CHARS_REGEX + "\\("
                + MULTIPLE_CHARS_REGEX + "\\)," + MULTIPLE_CHARS_REGEX + "="
                + MULTIPLE_CHARS_REGEX + "\\." + MULTIPLE_CHARS_REGEX + "\\["
                + MULTIPLE_CHARS_REGEX + "]\\|\\|null\\).+\\|\\|(" + MULTIPLE_CHARS_REGEX
                + ")\\(\"\"\\)"),

        /*
         * The second regex matches the following text, where we want SDa and the array index accessed:
         * a.D&&(b="nn"[+a.D],WL(a),c=a.j[b]||null)&&(c=SDa[0](c),a.set(b,c),SDa.length||Wma("")
         */
        Pattern.compile("," + MULTIPLE_CHARS_REGEX + "\\("
                + MULTIPLE_CHARS_REGEX + "\\)," + MULTIPLE_CHARS_REGEX + "="
                + MULTIPLE_CHARS_REGEX + "\\." + MULTIPLE_CHARS_REGEX + "\\["
                + MULTIPLE_CHARS_REGEX + "]\\|\\|null\\)&&\\(\\b" + MULTIPLE_CHARS_REGEX + "=("
                + MULTIPLE_CHARS_REGEX + ")" + ARRAY_ACCESS_REGEX + "\\("
                + SINGLE_CHAR_VARIABLE_REGEX + "\\)," + MULTIPLE_CHARS_REGEX
                + "\\.set\\((?:\"n+\"|" + MULTIPLE_CHARS_REGEX + ")," + MULTIPLE_CHARS_REGEX
                + "\\)"),

        /*
         * The third regex matches the following text, where we want rma:
         * a.D&&(b="nn"[+a.D],c=a.get(b))&&(c=rDa[0](c),a.set(b,c),rDa.length||rma("")
         */
        Pattern.compile(SINGLE_CHAR_VARIABLE_REGEX + "=\"nn\"\\[\\+" + MULTIPLE_CHARS_REGEX
                + "\\." + MULTIPLE_CHARS_REGEX + "]," + MULTIPLE_CHARS_REGEX + "="
                + MULTIPLE_CHARS_REGEX + "\\.get\\(" + MULTIPLE_CHARS_REGEX + "\\)\\).+\\|\\|("
                + MULTIPLE_CHARS_REGEX + ")\\(\"\"\\)"),

        /*
         * The fourth regex matches the following text, where we want rDa and the array index accessed:
         * a.D&&(b="nn"[+a.D],c=a.get(b))&&(c=rDa[0](c),a.set(b,c),rDa.length||rma("")
         */
        Pattern.compile(SINGLE_CHAR_VARIABLE_REGEX + "=\"nn\"\\[\\+" + MULTIPLE_CHARS_REGEX
                + "\\." + MULTIPLE_CHARS_REGEX + "]," + MULTIPLE_CHARS_REGEX + "="
                + MULTIPLE_CHARS_REGEX + "\\.get\\(" + MULTIPLE_CHARS_REGEX + "\\)\\)&&\\("
                + MULTIPLE_CHARS_REGEX + "=(" + MULTIPLE_CHARS_REGEX + ")\\[(\\d+)]"),

        /*
         * The fifth regex matches the following text, where we want BDa and the array index accessed:
         * (b=String.fromCharCode(110),c=a.get(b))&&(c=BDa[0](c)
        */
        Pattern.compile("\\(" + SINGLE_CHAR_VARIABLE_REGEX + "=String\\.fromCharCode\\(110\\),"
                + SINGLE_CHAR_VARIABLE_REGEX + "=" + SINGLE_CHAR_VARIABLE_REGEX + "\\.get\\("
                + SINGLE_CHAR_VARIABLE_REGEX + "\\)\\)" + "&&\\(" + SINGLE_CHAR_VARIABLE_REGEX
                + "=(" + MULTIPLE_CHARS_REGEX + ")" + "(?:" + ARRAY_ACCESS_REGEX + ")?\\("
                + SINGLE_CHAR_VARIABLE_REGEX + "\\)"),

        /*
         * The sixth regex matches the following text, where we want Yva and the array index accessed:
         * .get("n"))&&(b=Yva[0](b)
         */
        Pattern.compile("\\.get\\(\"n\"\\)\\)&&\\(" + SINGLE_CHAR_VARIABLE_REGEX
                + "=(" + MULTIPLE_CHARS_REGEX + ")(?:" + ARRAY_ACCESS_REGEX + ")?\\("
                + SINGLE_CHAR_VARIABLE_REGEX + "\\)")
    )


    // Escape the curly end brace to allow compatibility with Android's regex engine
    // See https://stackoverflow.com/q/45074813
    private const val DEOBFUSCATION_FUNCTION_BODY_REGEX =
        "=\\s*function([\\S\\s]*?\\}\\s*return [\\w$]+?\\.join\\(\"\"\\)\\s*\\};)"

    private const val DEOBFUSCATION_FUNCTION_ARRAY_OBJECT_TYPE_DECLARATION_REGEX = "var "

    private const val FUNCTION_NAMES_IN_DEOBFUSCATION_ARRAY_REGEX = "\\s*=\\s*\\[(.+?)][;,]"

    private const val FUNCTION_ARGUMENTS_REGEX: String = "=\\s*function\\s*\\(\\s*([^)]*)\\s*\\)"

    private const val EARLY_RETURN_REGEX: String = (";\\s*if\\s*\\(\\s*typeof\\s+" + MULTIPLE_CHARS_REGEX
            + "+\\s*===?\\s*([\"'])undefined\\1\\s*\\)\\s*return\\s+")

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
        try { matcher = matchMultiplePatterns(DEOBFUSCATION_FUNCTION_NAME_REGEXES, javaScriptPlayerCode)
        } catch (e: RegexException) { throw ParsingException("Could not find deobfuscation function with any of the " + "known patterns in the base JavaScript player code", e) }

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
        val function = try { parseFunctionWithLexer(javaScriptPlayerCode, functionName)
        } catch (e: Exception) { parseFunctionWithRegex(javaScriptPlayerCode, functionName) }
        return fixupFunction(function)
    }

    /**
     * Get the throttling parameter of a streaming URL if it exists.
     * @param streamingUrl a streaming URL
     * @return the throttling parameter of the streaming URL or `null` if no parameter has
     * been found
     */
    fun getThrottlingParameterFromStreamingUrl(streamingUrl: String): String? {
        // If the throttling parameter could not be parsed from the URL, it means that there is
        // no throttling parameter
        // Return null in this case
        return try { matchGroup1(THROTTLING_PARAM_PATTERN, streamingUrl) } catch (e: RegexException) { null }
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


    /**
     * Removes an early return statement from the code of the throttling parameter deobfuscation
     * function.
     *
     *
     * In newer version of the player code the function contains a check for something defined
     * outside of the function. If that was not found it will return early.
     *
     *
     * The check can look like this (JS):<br></br>
     * if(typeof RUQ==="undefined")return p;
     *
     *
     * In this example RUQ will always be undefined when running the function as standalone.
     * If the check is kept it would just return p which is the input parameter and would be wrong.
     * For that reason this check and return statement needs to be removed.
     *
     * @param function the original throttling parameter deobfuscation function code
     * @return the throttling parameter deobfuscation function code with the early return statement
     * removed
     */
    @Throws(RegexException::class)
    private fun fixupFunction(function: String): String {
        val firstArgName: String? = matchGroup1(FUNCTION_ARGUMENTS_REGEX, function).split(",")[0].trim()
        val earlyReturnPattern = Pattern.compile(EARLY_RETURN_REGEX + firstArgName + ";", Pattern.DOTALL)
        val earlyReturnCodeMatcher = earlyReturnPattern.matcher(function)
        return earlyReturnCodeMatcher.replaceFirst(";")
    }

}
