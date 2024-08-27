/*
 * Created by Christian Schabesberger on 02.02.16.
 *
 * Copyright (C) 2016 Christian Schabesberger <chris.schabesberger@mailbox.org>
* Copyright (C) 2024 Xilin Jia <https://github.com/XilinJia>
 * Parser.kt is part of Vista Guide.
 *
 * Vista Guide is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Vista Guide is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Vista Guide. If not, see <https://www.gnu.org/licenses/>.
 */
package ac.mdiq.vista.extractor.utils

import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.utils.Utils.decodeUrlUtf8
import java.io.UnsupportedEncodingException
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern
import java.util.stream.Collectors



/**
 * Avoid using regex !!!
 */
object Parser {

    @Throws(RegexException::class)
    fun matchGroup1(pattern: String, input: String): String {
        return matchGroup(pattern, input, 1)
    }


    @Throws(RegexException::class)
    fun matchGroup1(pattern: Pattern, input: String): String {
        return matchGroup(pattern, input, 1)
    }


    @Throws(RegexException::class)
    fun matchGroup(pattern: String, input: String, group: Int): String {
        return matchGroup(Pattern.compile(pattern), input, group)
    }


    @Throws(RegexException::class)
    fun matchGroup(pat: Pattern, input: String, group: Int): String {
        val matcher = pat.matcher(input)
        val foundMatch = matcher.find()
        if (foundMatch) return matcher.group(group)

        // only pass input to exception message when it is not too long
        if (input.length > 1024) throw RegexException("Failed to find pattern \"${pat.pattern()}\"")
        else throw RegexException("Failed to find pattern \"${pat.pattern()}\" inside of \"$input\"")
    }

    @Throws(RegexException::class)
    fun matchGroup1MultiplePatterns(patterns: Array<Pattern>, input: String): String {
        return matchMultiplePatterns(patterns, input).group(1)
    }

    @Throws(RegexException::class)
    fun matchMultiplePatterns(patterns: Array<Pattern>, input: String): Matcher {
        var exception: RegexException? = null
        for (pattern in patterns) {
            val matcher: Matcher = pattern.matcher(input)
            if (matcher.find()) return matcher
            else if (exception == null) {
                // only pass input to exception message when it is not too long
                exception = if (input.length > 1024) RegexException(("Failed to find pattern \"${pattern.pattern()}\""))
                else RegexException(("Failed to find pattern \"${pattern.pattern()}\" inside of \"$input\""))
            }
        }

        if (exception == null) throw RegexException("Empty patterns array passed to matchMultiplePatterns")
        else throw exception
    }


    fun isMatch(pattern: String, input: String): Boolean {
        val pat = Pattern.compile(pattern)
        val mat = pat.matcher(input)
        return mat.find()
    }


    fun isMatch(pattern: Pattern, input: String): Boolean {
        val mat = pattern.matcher(input)
        return mat.find()
    }



    @Throws(UnsupportedEncodingException::class)
//    fun compatParseMap(input: String): Map<String, String> {
//        val map: MutableMap<String, String> = HashMap()
//        for (arg in input.split("&".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {
//            val splitArg = arg.split("=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
//            map[splitArg[0]] = if (splitArg.size > 1) Utils.decodeUrlUtf8(splitArg[1]) else ""
//        }
//        return map
//    }

    fun compatParseMap(input: String): MutableMap<String, String> {
        return Arrays.stream(input.split("&".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())
            .map { arg: String -> arg.split("=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray() }
            .filter { splitArg: Array<String> -> splitArg.size > 1 }
            .collect(Collectors.toMap(
                { splitArg: Array<String> -> splitArg[0] },
                { splitArg: Array<String> -> decodeUrlUtf8(splitArg[1]) },
                { _: String?, replacement: String -> replacement }))
    }

    class RegexException(message: String?) : ParsingException(message)
}
