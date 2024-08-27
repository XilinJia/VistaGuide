package ac.mdiq.vista.extractor.utils.jsextractor

import ac.mdiq.vista.extractor.exceptions.ParsingException


/**
 * Utility class for extracting functions from JavaScript code.
 */
object JavaScriptExtractor {
    /**
     * Searches the given JavaScript code for the identifier of a function
     * and returns its body.
     *
     * @param jsCode JavaScript code
     * @param start start of the function (without the opening brace)
     * @return extracted code (opening brace + function + closing brace)
     * @throws ParsingException
     */


    @Throws(ParsingException::class)
    fun matchToClosingBrace(jsCode: String, start: String): String {
        var startIndex = jsCode.indexOf(start)
        if (startIndex < 0) throw ParsingException("Start not found")
        startIndex += start.length
        val js = jsCode.substring(startIndex)

        val lexer = Lexer(js)
        var visitedOpenBrace = false

        while (true) {
            val parsedToken = lexer.nextToken
            val t = parsedToken.token

            when {
                t == Token.LC -> visitedOpenBrace = true
                visitedOpenBrace && lexer.isBalanced -> return js.substring(0, parsedToken.end)
                t == Token.EOF -> throw ParsingException("Could not find matching braces")
            }
        }
    }
}
