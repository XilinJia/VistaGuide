package ac.mdiq.vista.extractor.services.youtube

import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.utils.JavaScript.compileOrThrow
import ac.mdiq.vista.extractor.utils.Parser.RegexException
import ac.mdiq.vista.extractor.utils.Parser.matchGroup1
import ac.mdiq.vista.extractor.utils.Parser.matchGroup1MultiplePatterns
import ac.mdiq.vista.extractor.utils.jsextractor.JavaScriptExtractor
import java.util.regex.Pattern


/**
 * Utility class to get the signature timestamp of YouTube's base JavaScript player and deobfuscate
 * signature of streaming URLs from HTML5 clients.
 */
internal object YoutubeSignatureUtils {
    /**
     * The name of the deobfuscation function which needs to be called inside the deobfuscation
     * code.
     */
    const val DEOBFUSCATION_FUNCTION_NAME: String = "deobfuscate"

    private val FUNCTION_REGEXES: Array<Pattern> = arrayOf(
        Pattern.compile("\\bm=([a-zA-Z0-9$]{2,})\\(decodeURIComponent\\(h\\.s\\)\\)"),
        Pattern.compile("\\bc&&\\(c=([a-zA-Z0-9$]{2,})\\(decodeURIComponent\\(c\\)\\)"),
        Pattern.compile("(?:\\b|[^a-zA-Z0-9$])([a-zA-Z0-9$]{2,})\\s*=\\s*function\\(\\s*a\\s*\\)\\s*\\{\\s*a\\s*=\\s*a\\.split\\(\\s*\"\"\\s*\\)"),
        Pattern.compile("([\\w$]+)\\s*=\\s*function\\((\\w+)\\)\\{\\s*\\2=\\s*\\2\\.split\\(\"\"\\)\\s*;")
    )

    private const val STS_REGEX = "signatureTimestamp[=:](\\d+)"

    private const val DEOBF_FUNC_REGEX_START = "("
    private const val DEOBF_FUNC_REGEX_END = "=function\\([a-zA-Z0-9_]+\\)\\{.+?\\})"

    private const val SIG_DEOBF_HELPER_OBJ_NAME_REGEX = ";([A-Za-z0-9_\\$]{2,})\\...\\("
    private const val SIG_DEOBF_HELPER_OBJ_REGEX_START = "(var "
    private const val SIG_DEOBF_HELPER_OBJ_REGEX_END = "=\\{(?>.|\\n)+?\\}\\};)"

    /**
     * Get the signature timestamp property of YouTube's base JavaScript file.
     *
     * @param javaScriptPlayerCode the complete JavaScript base player code
     * @return the signature timestamp
     * @throws ParsingException if the signature timestamp couldn't be extracted
     */

    @Throws(ParsingException::class)
    fun getSignatureTimestamp(javaScriptPlayerCode: String): String {
        try { return matchGroup1(STS_REGEX, javaScriptPlayerCode) } catch (e: ParsingException) { throw ParsingException("Could not extract signature timestamp from JavaScript code", e) }
    }

    /**
     * Get the signature deobfuscation code of YouTube's base JavaScript file.
     *
     * @param javaScriptPlayerCode the complete JavaScript base player code
     * @return the signature deobfuscation code
     * @throws ParsingException if the signature deobfuscation code couldn't be extracted
     */

    @Throws(ParsingException::class)
    fun getDeobfuscationCode(javaScriptPlayerCode: String): String {
        try {
            val deobfuscationFunctionName = getDeobfuscationFunctionName(javaScriptPlayerCode)
            val deobfuscationFunction = try { getDeobfuscateFunctionWithLexer(javaScriptPlayerCode, deobfuscationFunctionName)
            } catch (e: Exception) { getDeobfuscateFunctionWithRegex(javaScriptPlayerCode, deobfuscationFunctionName) }

            // Assert the extracted deobfuscation function is valid
            compileOrThrow(deobfuscationFunction)
            val helperObjectName = matchGroup1(SIG_DEOBF_HELPER_OBJ_NAME_REGEX, deobfuscationFunction)
            val helperObject = getHelperObject(javaScriptPlayerCode, helperObjectName)
            val callerFunction = ("function $DEOBFUSCATION_FUNCTION_NAME(a){return $deobfuscationFunctionName(a);}")
            return "$helperObject$deobfuscationFunction;$callerFunction"
        } catch (e: Exception) { throw ParsingException("Could not parse deobfuscation function", e) }
    }


    @Throws(ParsingException::class)
    private fun getDeobfuscationFunctionName(javaScriptPlayerCode: String): String {
        try { return matchGroup1MultiplePatterns(FUNCTION_REGEXES, javaScriptPlayerCode) } catch (e: RegexException) { throw ParsingException("Could not find deobfuscation function with any of the known patterns", e) }
    }


    @Throws(ParsingException::class)
    private fun getDeobfuscateFunctionWithLexer(javaScriptPlayerCode: String, deobfuscationFunctionName: String): String {
        val functionBase = "$deobfuscationFunctionName=function"
        return functionBase + JavaScriptExtractor.matchToClosingBrace(javaScriptPlayerCode, functionBase)
    }


    @Throws(ParsingException::class)
    private fun getDeobfuscateFunctionWithRegex(javaScriptPlayerCode: String, deobfuscationFunctionName: String): String {
        val functionPattern = ("$DEOBF_FUNC_REGEX_START${Pattern.quote(deobfuscationFunctionName)}$DEOBF_FUNC_REGEX_END")
        return "var " + matchGroup1(functionPattern, javaScriptPlayerCode)
    }


    @Throws(ParsingException::class)
    private fun getHelperObject(javaScriptPlayerCode: String, helperObjectName: String): String {
        val helperPattern = ("$SIG_DEOBF_HELPER_OBJ_REGEX_START${Pattern.quote(helperObjectName)}$SIG_DEOBF_HELPER_OBJ_REGEX_END")
        return matchGroup1(helperPattern, javaScriptPlayerCode).replace("\n", "")
    }
}
