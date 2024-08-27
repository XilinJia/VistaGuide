package ac.mdiq.vista.extractor.utils.jsextractor

import org.mozilla.javascript.Context
import org.mozilla.javascript.Kit
import org.mozilla.javascript.ObjToIntMap
import org.mozilla.javascript.ScriptRuntime
import ac.mdiq.vista.extractor.exceptions.ParsingException

/* Source: Mozilla Rhino, org.mozilla.javascript.Token
*
* This Source Code Form is subject to the terms of the Mozilla Public
* License, v. 2.0. If a copy of the MPL was not distributed with this
* file, You can obtain one at http://mozilla.org/MPL/2.0/.
* */
internal class TokenStream(
        private val sourceString: String,
        var lineno: Int,
        private val languageVersion: Int) {

    @get:Throws(ParsingException::class)
    val token: Token
        get() {
            var c: Int

            while (true) {
                // Eat whitespace, possibly sensitive to newlines.
                while (true) {
                    c = char
                    when {
                        c == EOF_CHAR -> {
                            tokenBeg = cursor - 1
                            tokenEnd = cursor
                            return Token.EOF
                        }
                        c == '\n'.code -> {
                            dirtyLine = false
                            tokenBeg = cursor - 1
                            tokenEnd = cursor
                            return Token.EOL
                        }
                        !isJSSpace(c) -> {
                            if (c != '-'.code) dirtyLine = true
                            break
                        }
                    }
                }

                // Assume the token will be 1 char - fixed up below.
                tokenBeg = cursor - 1
                tokenEnd = cursor

                // identifier/keyword/instanceof?
                // watch out for starting with a <backslash>
                val identifierStart: Boolean
                var isUnicodeEscapeStart = false
                if (c == '\\'.code) {
                    c = char
                    if (c == 'u'.code) {
                        identifierStart = true
                        isUnicodeEscapeStart = true
                        stringBufferTop = 0
                    } else {
                        identifierStart = false
                        ungetChar(c)
                        c = '\\'.code
                    }
                } else {
                    identifierStart = Character.isJavaIdentifierStart(c.toChar())
                    if (identifierStart) {
                        stringBufferTop = 0
                        addToString(c)
                    }
                }

                if (identifierStart) {
                    var containsEscape = isUnicodeEscapeStart
                    while (true) {
                        if (isUnicodeEscapeStart) {
                            // strictly speaking we should probably push-back
                            // all the bad characters if the <backslash>uXXXX
                            // sequence is malformed. But since there isn't a
                            // correct context(is there?) for a bad Unicode
                            // escape sequence in an identifier, we can report
                            // an error here.
                            var escapeVal = 0
                            for (i in 0..3) {
                                c = char
                                escapeVal = Kit.xDigitToInt(c, escapeVal)
                                // Next check takes care about c < 0 and bad escape
                                if (escapeVal < 0) break
                            }
                            if (escapeVal < 0) throw ParsingException("invalid unicode escape")
                            addToString(escapeVal)
                            isUnicodeEscapeStart = false
                        } else {
                            c = char
                            if (c == '\\'.code) {
                                c = char
                                if (c == 'u'.code) {
                                    isUnicodeEscapeStart = true
                                    containsEscape = true
                                } else throw ParsingException(String.format("illegal character: '%c'", c))
                            } else {
                                if (c == EOF_CHAR || c == BYTE_ORDER_MARK.code || !Character.isJavaIdentifierPart(c.toChar())) break
                                addToString(c)
                            }
                        }
                    }
                    ungetChar(c)

                    var str = stringFromBuffer
                    if (!containsEscape) {
                        // OPT we shouldn't have to make a string (object!) to
                        // check if it's a keyword.

                        // Return the corresponding token if it's a keyword

                        var result = stringToKeyword(str, languageVersion, STRICT_MODE)
                        if (result != Token.EOF) {
                            if ((result == Token.LET || result == Token.YIELD) && languageVersion < Context.VERSION_1_7) {
                                // LET and YIELD are tokens only in 1.7 and later
                                string = if (result == Token.LET) "let" else "yield"
                                result = Token.NAME
                            }
                            // Save the string in case we need to use in
                            // object literal definitions.
                            this.string = allStrings.intern(str) as String
                            when {
                                result != Token.RESERVED -> return result
                                languageVersion >= Context.VERSION_ES6 -> return result
                                !IS_RESERVED_KEYWORD_AS_IDENTIFIER -> return result
                            }
                        }
                    } else if (isKeyword(str, languageVersion, STRICT_MODE)) {
                        // If a string contains unicodes, and converted to a keyword,
                        // we convert the last character back to unicode
                        str = convertLastCharToHex(str)
                    }
                    this.string = allStrings.intern(str) as String
                    return Token.NAME
                }

                // is it a number?
                if (isDigit(c) || (c == '.'.code && isDigit(peekChar()))) {
                    stringBufferTop = 0
                    var base = 10
                    val es6 = languageVersion >= Context.VERSION_ES6
                    var isOldOctal = false

                    if (c == '0'.code) {
                        c = char
                        when {
                            c == 'x'.code || c == 'X'.code -> {
                                base = 16
                                c = char
                            }
                            es6 && (c == 'o'.code || c == 'O'.code) -> {
                                base = 8
                                c = char
                            }
                            es6 && (c == 'b'.code || c == 'B'.code) -> {
                                base = 2
                                c = char
                            }
                            isDigit(c) -> {
                                base = 8
                                isOldOctal = true
                            }
                            else -> addToString('0'.code)
                        }
                    }

                    val emptyDetector = stringBufferTop
                    if (base == 10 || base == 16 || (base == 8 && !isOldOctal) || base == 2) {
                        c = readDigits(base, c)
                        if (c == REPORT_NUMBER_FORMAT_ERROR) throw ParsingException("number format error")
                    } else {
                        while (isDigit(c)) {
                            // finally the oldOctal case
                            if (c >= '8'.code) {
                                /*
                             * We permit 08 and 09 as decimal numbers, which
                             * makes our behavior a superset of the ECMA
                             * numeric grammar.  We might not always be so
                             * permissive, so we warn about it.
                             */
                                base = 10
                                c = readDigits(base, c)
                                if (c == REPORT_NUMBER_FORMAT_ERROR) throw ParsingException("number format error")
                                break
                            }
                            addToString(c)
                            c = char
                        }
                    }
                    if (stringBufferTop == emptyDetector && base != 10) throw ParsingException("number format error")

                    if (es6 && c == 'n'.code) {
                        c = char
                    } else if (base == 10 && (c == '.'.code || c == 'e'.code || c == 'E'.code)) {
                        if (c == '.'.code) {
                            addToString(c)
                            c = char
                            c = readDigits(base, c)
                            if (c == REPORT_NUMBER_FORMAT_ERROR) throw ParsingException("number format error")
                        }
                        if (c == 'e'.code || c == 'E'.code) {
                            addToString(c)
                            c = char
                            if (c == '+'.code || c == '-'.code) {
                                addToString(c)
                                c = char
                            }
                            if (!isDigit(c)) throw ParsingException("missing exponent")
                            c = readDigits(base, c)
                            if (c == REPORT_NUMBER_FORMAT_ERROR) throw ParsingException("number format error")
                        }
                    }
                    ungetChar(c)
                    this.string = this.stringFromBuffer
                    return Token.NUMBER
                }

                // is it a string or template literal?
                if (c == '"'.code || c == '\''.code || c == '`'.code) {
                    // We attempt to accumulate a string the fast way, by
                    // building it directly out of the reader.  But if there
                    // are any escaped characters in the string, we revert to
                    // building it out of a StringBuffer.

                    // delimiter for last string literal scanned

                    val quoteChar = c
                    stringBufferTop = 0

                    c = getCharIgnoreLineEnd(false)
                    strLoop@ while (c != quoteChar) {
                        var unterminated = false
                        if (c == EOF_CHAR) {
                            unterminated = true
                        } else if (c == '\n'.code) {
                            when (lineEndChar) {
                                '\n'.code, '\r'.code -> unterminated = true
                                // Line/Paragraph separators need to be included as is
                                0x2028, 0x2029 -> c = lineEndChar
                                else -> {}
                            }
                        }

                        if (unterminated) throw ParsingException("unterminated string literal")

                        if (c == '\\'.code) {
                            // We've hit an escaped character
                            var escapeVal: Int

                            c = char
                            when (c.toChar()) {
                                'b' -> c = '\b'.code
//                                'f' -> c = '\f'.code
                                'f' -> c = '\u000C'.code
                                'n' -> c = '\n'.code
                                'r' -> c = '\r'.code
                                't' -> c = '\t'.code
                                'v' -> c = 0xb
                                'u' -> {
                                    // Get 4 hex digits; if the u escape is not
                                    // followed by 4 hex digits, use 'u' + the
                                    // literal character sequence that follows.
                                    val escapeStart = stringBufferTop
                                    addToString('u'.code)
                                    escapeVal = 0
                                    var i = 0
                                    while (i != 4) {
                                        c = char
                                        escapeVal = Kit.xDigitToInt(c, escapeVal)
                                        if (escapeVal < 0) continue@strLoop
                                        addToString(c)
                                        ++i
                                    }
                                    // prepare for replace of stored 'u' sequence
                                    // by escape value
                                    stringBufferTop = escapeStart
                                    c = escapeVal
                                }
                                'x' -> {
                                    // Get 2 hex digits, defaulting to 'x'+literal
                                    // sequence, as above.
                                    c = char
                                    escapeVal = Kit.xDigitToInt(c, 0)
                                    if (escapeVal < 0) {
                                        addToString('x'.code)
                                        continue@strLoop
                                    }
                                    val c1 = c
                                    c = char
                                    escapeVal = Kit.xDigitToInt(c, escapeVal)
                                    if (escapeVal < 0) {
                                        addToString('x'.code)
                                        addToString(c1)
                                        continue@strLoop
                                    }
                                    // got 2 hex digits
                                    c = escapeVal
                                }
                                '\n' -> {
                                    // Remove line terminator after escape to follow
                                    // SpiderMonkey and C/C++
                                    c = char
                                    continue@strLoop
                                }
                                else -> if ('0'.code <= c && c < '8'.code) {
                                    var `val` = c - '0'.code
                                    c = char
                                    if ('0'.code <= c && c < '8'.code) {
                                        `val` = 8 * `val` + c - '0'.code
                                        c = char
                                        if ('0'.code <= c && c < '8'.code && `val` <= 31) {
                                            // c is 3rd char of octal sequence only
                                            // if the resulting val <= 0377
                                            `val` = 8 * `val` + c - '0'.code
                                            c = char
                                        }
                                    }
                                    ungetChar(c)
                                    c = `val`
                                }
                            }
                        }
                        addToString(c)
                        c = getChar(false)
                    }

                    val str = stringFromBuffer
                    this.string = allStrings.intern(str) as String
                    return if (quoteChar == '`'.code) Token.TEMPLATE_LITERAL else Token.STRING
                }

                when (c.toChar()) {
                    ';' -> return Token.SEMI
                    '[' -> return Token.LB
                    ']' -> return Token.RB
                    '{' -> return Token.LC
                    '}' -> return Token.RC
                    '(' -> return Token.LP
                    ')' -> return Token.RP
                    ',' -> return Token.COMMA
                    '?' -> return Token.HOOK
                    ':' -> return Token.COLON
                    '.' -> return Token.DOT

                    '|' -> return when {
                        matchChar('|'.code) -> Token.OR
                        matchChar('='.code) -> Token.ASSIGN_BITOR
                        else -> Token.BITOR
                    }

                    '^' -> {
                        if (matchChar('='.code)) return Token.ASSIGN_BITXOR
                        return Token.BITXOR
                    }
                    '&' -> return when {
                        matchChar('&'.code) -> Token.AND
                        matchChar('='.code) -> Token.ASSIGN_BITAND
                        else -> Token.BITAND
                    }

                    '=' -> when {
                        matchChar('='.code) -> {
                            if (matchChar('='.code)) return Token.SHEQ
                            return Token.EQ
                        }
                        matchChar('>'.code) -> return Token.ARROW
                        else -> return Token.ASSIGN
                    }

                    '!' -> {
                        if (matchChar('='.code)) {
                            if (matchChar('='.code)) return Token.SHNE
                            return Token.NE
                        }
                        return Token.NOT
                    }
                    '<' -> {
                        /* NB:treat HTML begin-comment as comment-till-eol */
                        if (matchChar('!'.code)) {
                            if (matchChar('-'.code)) {
                                if (matchChar('-'.code)) {
                                    tokenBeg = cursor - 4
                                    skipLine()
                                    return Token.COMMENT
                                }
                                ungetCharIgnoreLineEnd('-'.code)
                            }
                            ungetCharIgnoreLineEnd('!'.code)
                        }
                        if (matchChar('<'.code)) {
                            if (matchChar('='.code)) return Token.ASSIGN_LSH
                            return Token.LSH
                        }
                        if (matchChar('='.code)) return Token.LE
                        return Token.LT
                    }
                    '>' -> {
                        if (matchChar('>'.code)) {
                            if (matchChar('>'.code)) {
                                if (matchChar('='.code)) return Token.ASSIGN_URSH
                                return Token.URSH
                            }
                            if (matchChar('='.code)) return Token.ASSIGN_RSH
                            return Token.RSH
                        }
                        if (matchChar('='.code)) return Token.GE
                        return Token.GT
                    }
                    '*' -> {
                        if (languageVersion >= Context.VERSION_ES6) {
                            if (matchChar('*'.code)) {
                                if (matchChar('='.code)) return Token.ASSIGN_EXP
                                return Token.EXP
                            }
                        }
                        if (matchChar('='.code)) return Token.ASSIGN_MUL
                        return Token.MUL
                    }
                    '/' -> {
                        // is it a // comment?
                        if (matchChar('/'.code)) {
                            tokenBeg = cursor - 2
                            skipLine()
                            return Token.COMMENT
                        }
                        // is it a /* or /** comment?
                        if (matchChar('*'.code)) {
                            var lookForSlash = false
                            tokenBeg = cursor - 2
                            if (matchChar('*'.code)) lookForSlash = true
                            while (true) {
                                c = char
                                when (c) {
                                    EOF_CHAR -> {
                                        tokenEnd = cursor - 1
                                        throw ParsingException("unterminated comment")
                                    }
                                    '*'.code -> lookForSlash = true
                                    '/'.code -> {
                                        if (lookForSlash) {
                                            tokenEnd = cursor
                                            return Token.COMMENT
                                        }
                                    }
                                    else -> {
                                        lookForSlash = false
                                        tokenEnd = cursor
                                    }
                                }
                            }
                        }

                        if (matchChar('='.code)) return Token.ASSIGN_DIV
                        return Token.DIV
                    }
                    '%' -> {
                        if (matchChar('='.code)) return Token.ASSIGN_MOD
                        return Token.MOD
                    }
                    '~' -> return Token.BITNOT
                    '+' -> return when {
                        matchChar('='.code) -> Token.ASSIGN_ADD
                        matchChar('+'.code) -> Token.INC
                        else -> Token.ADD
                    }

                    '-' -> {
                        var t = Token.SUB
                        when {
                            matchChar('='.code) -> t = Token.ASSIGN_SUB
                            matchChar('-'.code) -> {
                                if (!dirtyLine) {
                                    // treat HTML end-comment after possible whitespace
                                    // after line start as comment-until-eol
                                    if (matchChar('>'.code)) {
                                        skipLine()
                                        return Token.COMMENT
                                    }
                                }
                                t = Token.DEC
                            }
                        }
                        dirtyLine = true
                        return t
                    }
                    else -> throw ParsingException(String.format("illegal character: '%c'", c))
                }
            }
        }

    /*
     * Helper to read the next digits according to the base
     * and ignore the number separator if there is one.
     */
    private fun readDigits(base: Int, firstC: Int): Int {
        if (isDigit(base, firstC)) {
            addToString(firstC)

            var c = char
            if (c == EOF_CHAR) return EOF_CHAR

            while (true) {
                when {
                    c == NUMERIC_SEPARATOR.code -> {
                        // we do no peek here, we are optimistic for performance
                        // reasons and because peekChar() only does an getChar/ungetChar.
                        c = char
                        // if the line ends after the separator we have
                        // to report this as an error
                        if (c == '\n'.code || c == EOF_CHAR) return REPORT_NUMBER_FORMAT_ERROR

                        if (!isDigit(base, c)) {
                            // bad luck we have to roll back
                            ungetChar(c)
                            return NUMERIC_SEPARATOR.code
                        }
                        addToString(NUMERIC_SEPARATOR.code)
                    }
                    isDigit(base, c) -> {
                        addToString(c)
                        c = char
                        if (c == EOF_CHAR) return EOF_CHAR
                    }
                    else -> return c
                }
            }
        }
        return firstC
    }

    /** Parser calls the method when it gets / or /= in literal context.  */
    @Throws(ParsingException::class)
    fun readRegExp(startToken: Token) {
        val start = tokenBeg
        stringBufferTop = 0
        if (startToken == Token.ASSIGN_DIV) {
            // Miss-scanned /=
            addToString('='.code)
        } else {
            if (startToken != Token.DIV) Kit.codeBug()
            if (peekChar() == '*'.code) {
                tokenEnd = cursor - 1
                this.string = String(stringBuffer, 0, stringBufferTop)
                throw ParsingException("msg.unterminated.re.lit")
            }
        }

        var inCharSet = false // true if inside a '['..']' pair
        var c: Int
        while ((char.also { c = it }) != '/'.code || inCharSet) {
            if (c == '\n'.code || c == EOF_CHAR) throw ParsingException("msg.unterminated.re.lit")
            when (c) {
                '\\'.code -> {
                    addToString(c)
                    c = char
                    if (c == '\n'.code || c == EOF_CHAR) throw ParsingException("msg.unterminated.re.lit")
                }
                '['.code -> inCharSet = true
                ']'.code -> inCharSet = false
            }
            addToString(c)
        }
        val reEnd = stringBufferTop

        while (true) {
            c = charIgnoreLineEnd
            when {
                "gimysu".indexOf(c.toChar()) != -1 -> addToString(c)
                isAlpha(c) -> throw ParsingException("msg.invalid.re.flag")
                else -> {
                    ungetCharIgnoreLineEnd(c)
                    break
                }
            }
        }

        tokenEnd = start + stringBufferTop + 2 // include slashes
        this.string = String(stringBuffer, 0, reEnd)
    }

    private val stringFromBuffer: String
        get() {
            tokenEnd = cursor
            return String(stringBuffer, 0, stringBufferTop)
        }

    private fun addToString(c: Int) {
        val n = stringBufferTop
        if (n == stringBuffer.size) {
            val tmp = CharArray(stringBuffer.size * 2)
            System.arraycopy(stringBuffer, 0, tmp, 0, n)
            stringBuffer = tmp
        }
        stringBuffer[n] = c.toChar()
        stringBufferTop = n + 1
    }

    private fun ungetChar(c: Int) {
        // can not unread past across line boundary
        if (ungetCursor != 0 && ungetBuffer[ungetCursor - 1] == '\n'.code) Kit.codeBug()
        ungetBuffer[ungetCursor++] = c
        cursor--
    }

    private fun matchChar(test: Int): Boolean {
        val c = charIgnoreLineEnd
        if (c == test) {
            tokenEnd = cursor
            return true
        }
        ungetCharIgnoreLineEnd(c)
        return false
    }

    private fun peekChar(): Int {
        val c = char
        ungetChar(c)
        return c
    }

    private val char: Int
        get() = getChar(true, false)

    private fun getChar(skipFormattingChars: Boolean): Int {
        return getChar(skipFormattingChars, false)
    }

    private fun getChar(skipFormattingChars: Boolean, ignoreLineEnd: Boolean): Int {
        if (ungetCursor != 0) {
            cursor++
            return ungetBuffer[--ungetCursor]
        }

        while (true) {
            if (sourceCursor == sourceString.length) {
                hitEOF = true
                return EOF_CHAR
            }
            cursor++
            var c = sourceString[sourceCursor++].code

            if (!ignoreLineEnd && lineEndChar >= 0) {
                if (lineEndChar == '\r'.code && c == '\n'.code) {
                    lineEndChar = '\n'.code
                    continue
                }
                lineEndChar = -1
                lineStart = sourceCursor - 1
                lineno++
            }

            if (c <= 127) {
                if (c == '\n'.code || c == '\r'.code) {
                    lineEndChar = c
                    c = '\n'.code
                }
            } else {
                if (c == BYTE_ORDER_MARK.code) return c /* BOM is considered whitespace */
                if (skipFormattingChars && isJSFormatChar(c)) continue
                if (ScriptRuntime.isJSLineTerminator(c)) {
                    lineEndChar = c
                    c = '\n'.code
                }
            }
            return c
        }
    }

    private val charIgnoreLineEnd: Int
        get() = getChar(true, true)

    private fun getCharIgnoreLineEnd(skipFormattingChars: Boolean): Int {
        return getChar(skipFormattingChars, true)
    }

    private fun ungetCharIgnoreLineEnd(c: Int) {
        ungetBuffer[ungetCursor++] = c
        cursor--
    }

    private fun skipLine() {
        // skip to end of line
        var c: Int
        while ((char.also { c = it }) != EOF_CHAR && c != '\n'.code) { }
        ungetChar(c)
        tokenEnd = cursor
    }

    val tokenLength: Int
        /** Return tokenEnd - tokenBeg  */
        get() = tokenEnd - tokenBeg

    val tokenRaw: String
        get() = sourceString.substring(tokenBeg, tokenEnd)

    @Throws(ParsingException::class)
    fun nextToken(): Token {
        var tt = token
        while (tt == Token.EOL || tt == Token.COMMENT) {
            tt = token
        }
        return tt
    }

    // stuff other than whitespace since start of line
    private var dirtyLine = false
    private var string = ""

    private var stringBuffer = CharArray(128)
    private var stringBufferTop = 0
    private val allStrings = ObjToIntMap(50)

    // Room to backtrace from to < on failed match of the last - in <!--
    private val ungetBuffer = IntArray(3)
    private var ungetCursor = 0

    private var hitEOF = false

    private var lineStart = 0
    private var lineEndChar = -1

    // sourceCursor is an index into a small buffer that keeps a
    // sliding window of the source stream.
    var sourceCursor: Int = 0

    /** Return the current position of the scanner cursor.  */
    // cursor is a monotonically increasing index into the original
    // source stream, tracking exactly how far scanning has progressed.
    // Its value is the index of the next character to be scanned.
    var cursor: Int = 0

    /** Return the absolute source offset of the last scanned token.  */
    // Record start and end positions of last scanned token.
    var tokenBeg: Int = 0

    /** Return the absolute source end-offset of the last scanned token.  */
    var tokenEnd: Int = 0

    companion object {
        /*
     * For chars - because we need something out-of-range
     * to check.  (And checking EOF by exception is annoying.)
     * Note distinction from EOF token type!
     */
        private const val EOF_CHAR = -1

        /*
     * Return value for readDigits() to signal the caller has
     * to return an number format problem.
     */
        private const val REPORT_NUMBER_FORMAT_ERROR = -2

        private const val BYTE_ORDER_MARK = '\uFEFF'
        private const val NUMERIC_SEPARATOR = '_'

        fun isKeyword(s: String, version: Int, isStrict: Boolean): Boolean {
            return Token.EOF != stringToKeyword(s, version, isStrict)
        }

        private fun stringToKeyword(name: String, version: Int, isStrict: Boolean): Token {
            if (version < Context.VERSION_ES6) return stringToKeywordForJS(name)
            return stringToKeywordForES(name, isStrict)
        }

        /** JavaScript 1.8 and earlier  */
        private fun stringToKeywordForJS(name: String): Token {
            when (name) {
                "break" -> return Token.BREAK
                "case" -> return Token.CASE
                "continue" -> return Token.CONTINUE
                "default" -> return Token.DEFAULT
                "delete" -> return Token.DELPROP
                "do" -> return Token.DO
                "else" -> return Token.ELSE
                "export" -> return Token.EXPORT
                "false" -> return Token.FALSE
                "for" -> return Token.FOR
                "function" -> return Token.FUNCTION
                "if" -> return Token.IF
                "in" -> return Token.IN
                "let" -> return Token.LET
                "new" -> return Token.NEW
                "null" -> return Token.NULL
                "return" -> return Token.RETURN
                "switch" -> return Token.SWITCH
                "this" -> return Token.THIS
                "true" -> return Token.TRUE
                "typeof" -> return Token.TYPEOF
                "var" -> return Token.VAR
                "void" -> return Token.VOID
                "while" -> return Token.WHILE
                "with" -> return Token.WITH
                "yield" -> return Token.YIELD
                "throw" -> return Token.THROW
                "catch" -> return Token.CATCH
                "const" -> return Token.CONST
                "debugger" -> return Token.DEBUGGER
                "finally" -> return Token.FINALLY
                "instanceof" -> return Token.INSTANCEOF
                "try" -> return Token.TRY
                "abstract", "boolean", "byte", "char", "class", "double", "enum", "extends", "final", "float", "goto", "implements", "import", "int", "interface", "long", "native", "package", "private", "protected", "public", "short", "static", "super", "synchronized", "throws", "transient", "volatile" -> return Token.RESERVED
            }
            return Token.EOF
        }

        /** ECMAScript 6.  */
        private fun stringToKeywordForES(name: String, isStrict: Boolean): Token {
            when (name) {
                "break" -> return Token.BREAK
                "case" -> return Token.CASE
                "catch" -> return Token.CATCH
                "const" -> return Token.CONST
                "continue" -> return Token.CONTINUE
                "debugger" -> return Token.DEBUGGER
                "default" -> return Token.DEFAULT
                "delete" -> return Token.DELPROP
                "do" -> return Token.DO
                "else" -> return Token.ELSE
                "export" -> return Token.EXPORT
                "finally" -> return Token.FINALLY
                "for" -> return Token.FOR
                "function" -> return Token.FUNCTION
                "if" -> return Token.IF
                "import" -> return Token.IMPORT
                "in" -> return Token.IN
                "instanceof" -> return Token.INSTANCEOF
                "new" -> return Token.NEW
                "return" -> return Token.RETURN
                "switch" -> return Token.SWITCH
                "this" -> return Token.THIS
                "throw" -> return Token.THROW
                "try" -> return Token.TRY
                "typeof" -> return Token.TYPEOF
                "var" -> return Token.VAR
                "void" -> return Token.VOID
                "while" -> return Token.WHILE
                "with" -> return Token.WITH
                "yield" -> return Token.YIELD
                "false" -> return Token.FALSE
                "null" -> return Token.NULL
                "true" -> return Token.TRUE
                "let" -> return Token.LET
                "class", "extends", "super", "await", "enum" -> return Token.RESERVED
                "implements", "interface", "package", "private", "protected", "public", "static" -> if (isStrict) return Token.RESERVED
            }
            return Token.EOF
        }

        private fun isAlpha(c: Int): Boolean {
            // Use 'Z' < 'a'
            if (c <= 'Z'.code) return 'A'.code <= c
            return 'a'.code <= c && c <= 'z'.code
        }

        private fun isDigit(base: Int, c: Int): Boolean {
            return ((base == 10 && isDigit(c)) || (base == 16 && isHexDigit(c)) || (base == 8 && isOctalDigit(c)) || (base == 2 && isDualDigit(c)))
        }

        private fun isDualDigit(c: Int): Boolean {
            return '0'.code == c || c == '1'.code
        }

        private fun isOctalDigit(c: Int): Boolean {
            return '0'.code <= c && c <= '7'.code
        }

        private fun isDigit(c: Int): Boolean {
            return '0'.code <= c && c <= '9'.code
        }

        private fun isHexDigit(c: Int): Boolean {
            return ('0'.code <= c && c <= '9'.code) || ('a'.code <= c && c <= 'f'.code) || ('A'.code <= c && c <= 'F'.code)
        }

        /* As defined in ECMA.  jsscan.c uses C isspace() (which allows
     * \v, I think.)  note that code in getChar() implicitly accepts
     * '\r' == \u000D as well.
     */
        private fun isJSSpace(c: Int): Boolean {
            if (c <= 127) return c == 0x20 || c == 0x9 || c == 0xC || c == 0xB
            return c == 0xA0 || c == BYTE_ORDER_MARK.code || Character.getType(c.toChar()) == Character.SPACE_SEPARATOR.toInt()
        }

        private fun isJSFormatChar(c: Int): Boolean {
            return c > 127 && Character.getType(c.toChar()) == Character.FORMAT.toInt()
        }

        private fun convertLastCharToHex(str: String): String {
            val lastIndex = str.length - 1
            val buf = StringBuilder(str.substring(0, lastIndex))
            buf.append("\\u")
            val hexCode = Integer.toHexString(str[lastIndex].code)
            for (i in 0 until 4 - hexCode.length) {
                buf.append('0')
            }
            buf.append(hexCode)
            return buf.toString()
        }

        private const val IS_RESERVED_KEYWORD_AS_IDENTIFIER = true
        private const val STRICT_MODE = false
    }
}
