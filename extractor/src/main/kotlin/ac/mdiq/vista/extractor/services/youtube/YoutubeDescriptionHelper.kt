package ac.mdiq.vista.extractor.services.youtube

import com.grack.nanojson.JsonObject
import org.jsoup.nodes.Entities
import ac.mdiq.vista.extractor.services.youtube.YoutubeParsingHelper.getUrlFromNavigationEndpoint
import java.util.*
import java.util.function.Function
import java.util.regex.Pattern

import kotlin.math.min


object YoutubeDescriptionHelper {
    const val LINK_CLOSE: String = "</a>"
    const val STRIKETHROUGH_OPEN: String = "<s>"
    const val STRIKETHROUGH_CLOSE: String = "</s>"
    const val BOLD_OPEN: String = "<b>"
    const val BOLD_CLOSE: String = "</b>"
    const val ITALIC_OPEN: String = "<i>"
    const val ITALIC_CLOSE: String = "</i>"

    // special link chips (e.g. for YT videos, YT channels or social media accounts):
    // (u00a0) u00a0 u00a0 [/•] u00a0 <link content> u00a0 u00a0
    private val LINK_CONTENT_CLEANER_REGEX: Pattern = Pattern.compile("(?s)^\u00a0+[/•]\u00a0+(.*?)\u00a0+$")

    /**
     * Parse a video description in the new "attributed" format, which contains the entire visible
     * plaintext (`content`) and an array of `commandRuns` and `styleRuns`.
     * Returns the formatted content in HTML format, and escapes the text to make sure there are no
     * XSS attacks.
     *
     * `commandRuns` include the links and their range in the text, while `styleRuns`
     * include the styling to apply to various ranges in the text.
     *
     * @param attributedDescription the JSON object of the attributed description
     * @return the parsed description, in HTML format, as a string
     */

    fun attributedDescriptionToHtml(attributedDescription: JsonObject?): String? {
        if (attributedDescription.isNullOrEmpty()) return null

        val content = attributedDescription.getString("content") ?: return null

        // all run pairs must always of length at least 1, or they should be discarded,
        // otherwise various assumptions made in runsToHtml may fail
        val openers: MutableList<Run> = ArrayList()
        val closers: MutableList<Run> = ArrayList()
        addAllCommandRuns(attributedDescription, openers, closers)
        addAllStyleRuns(attributedDescription, openers, closers)

        // Note that sorting this way might put closers with the same close position in the wrong
        // order with respect to their openers, causing unnecessary closes and reopens. E.g.
        // <b>b<i>b&i</i></b> is instead generated as <b>b<i>b&i</b></i><b></b> if the </b> is
        // encountered before the </i>. Solving this wouldn't be difficult, thanks to stable sort,
        // but would require additional sorting steps which would just make this slower for the
        // general case where it's unlikely there are coincident closes.
        Collections.sort(openers, Comparator.comparingInt { run: Run -> run.pos })
        Collections.sort(closers, Comparator.comparingInt { run: Run -> run.pos })

        return runsToHtml(openers, closers, content)
    }

    /**
     * Applies the formatting specified by the intervals stored in `openers` and `closers` to `content` in order to obtain valid HTML even when intervals overlap. For
     * example &lt;b&gt;b&lt;i&gt;b&i&lt;/b&gt;i&lt;/i&gt; would not be valid HTML, so this function
     * instead generates &lt;b&gt;b&lt;i&gt;b&i&lt;/i&gt;&lt;/b&gt;&lt;i&gt;i&lt;/i&gt;. Any HTML
     * special characters in `rawContent` are escaped to make sure there are no XSS attacks.
     *
     * Every opener in `openers` must have a corresponding closer in `closers`. Every
     * corresponding (opener, closer) pair must have a length of at least one (i.e. empty intervals
     * are not allowed).
     *
     * @param openers    contains all of the places where a run begins, must have the same size of
     * closers, must be ordered by [pos]
     * @param closers    contains all of the places where a run ends, must have the same size of
     * openers, must be ordered by [pos]
     * @param rawContent the content to apply formatting to, and to escape to avoid XSS
     * @return the formatted content in HTML
     */
    fun runsToHtml(openers: List<Run>,  closers: List<Run>,  content: String): String {
//        val content = rawContent.replace('\u00a0', ' ')
        val openRuns = Stack<Run>()
        val tempStack = Stack<Run>()
        val textBuilder = StringBuilder()
        var currentTextPos = 0
        var openersIndex = 0
        var closersIndex = 0

        // openers and closers have the same length, but we will surely finish openers earlier than
        // closers, since every opened interval needs to be closed at some point and there can't be
        // empty intervals, hence check only closersIndex < closers.size()
        while (closersIndex < closers.size) {
            val minPos = if (openersIndex < openers.size) min(closers[closersIndex].pos.toDouble(), openers[openersIndex].pos.toDouble()).toInt()
            else closers[closersIndex].pos

            // append piece of text until current index
            textBuilder.append(content, currentTextPos, minPos)
            currentTextPos = minPos

            if (closers[closersIndex].pos == minPos) {
                // even in case of position tie, first process closers
                val closer = closers[closersIndex]
                ++closersIndex

                // because of the assumptions, this while wouldn't need the !openRuns.empty()
                // condition, because no run will close before being opened, but let's be sure
                while (!openRuns.empty()) {
                    val popped = openRuns.pop()
                    if (popped.sameOpen(closer)) {
                        // before closing the current run, if the run has a transformContent
                        // function, use it to transform the content of the current run, based on
                        // the openPosInOutput set when the current run was opened
                        if (popped.transformContent != null && popped.openPosInOutput >= 0)
                            textBuilder.replace(popped.openPosInOutput, textBuilder.length, popped.transformContent.apply(textBuilder.substring(popped.openPosInOutput)))

                        // close the run that we really need to close
                        textBuilder.append(popped.close)
                        break
                    }
                    // we keep popping from openRuns, closing all of the runs we find,
                    // until we find the run that we really need to close ...
                    textBuilder.append(popped.close)
                    tempStack.push(popped)
                }
                while (!tempStack.empty()) {
                    // ... and then we reopen all of the runs that we didn't need to close
                    // e.g. in <b>b<i>b&i</b>i</i>, when </b> is encountered, </i></b><i> is printed
                    // instead, to make sure the HTML is valid, obtaining <b>b<i>b&i</i></b><i>i</i>
                    val popped = tempStack.pop()
                    textBuilder.append(popped.open)
                    openRuns.push(popped)
                }
            } else {
                // this will never be reached if openersIndex >= openers.size() because of the
                // way minPos is calculated
                val opener = openers[openersIndex]
                textBuilder.append(opener.open)
                opener.openPosInOutput = textBuilder.length // save for transforming later
                openRuns.push(opener)
                ++openersIndex
            }
        }

        // append last piece of text
        textBuilder.append(content, currentTextPos, content.length)

        return textBuilder.toString()
            .replace("\n", "<br>")
            .replace("  ", " &nbsp;")
            .replace('\u00a0', ' ')
    }

    private fun addAllCommandRuns(attributedDescription: JsonObject,  openers: MutableList<Run>,  closers: MutableList<Run>) {
        attributedDescription.getArray("commandRuns")
            .stream()
            .filter { o: Any? -> JsonObject::class.java.isInstance(o) }
            .map { obj: Any? -> JsonObject::class.java.cast(obj) }
            .forEach { run: JsonObject ->
                val navigationEndpoint = run.getObject("onTap").getObject("innertubeCommand")
                val startIndex = run.getInt("startIndex", -1)
                val length = run.getInt("length", 0)
                if (startIndex < 0 || length < 1 || navigationEndpoint == null) return@forEach

                val url = getUrlFromNavigationEndpoint(navigationEndpoint) ?: return@forEach
                val open = "<a href=\"" + Entities.escape(url) + "\">"
                val transformContent = getTransformContentFun(run)
                openers.add(Run(open, LINK_CLOSE, startIndex, transformContent))
                closers.add(Run(open, LINK_CLOSE, startIndex + length, transformContent))
            }
    }

    private fun getTransformContentFun(run: JsonObject): Function<String, String> {
        val accessibilityLabel = run.getObject("onTapOptions")
            .getObject("accessibilityInfo")
            .getString("accessibilityLabel", "") // accessibility labels are e.g. "Instagram Channel Link: instagram_profile_name"
            .replaceFirst(" Channel Link".toRegex(), "")
        val transformContent = if (accessibilityLabel.isEmpty() || accessibilityLabel.startsWith("YouTube: ")) {
            // if there is no accessibility label, or the link points to YouTube, cleanup the link
            // text, see LINK_CONTENT_CLEANER_REGEX's documentation for more details
            Function { content: String ->
                val m = LINK_CONTENT_CLEANER_REGEX.matcher(content)
                if (m.find()) return@Function m.group(1)
                content
            }
        } else {
            // if there is an accessibility label, replace the link text with it, because on the
            // YouTube website an ambiguous link text is next to an icon explaining which service it
            // belongs to, but since we can't add icons, we instead use the accessibility label
            // which contains information about the service
            Function { _: String -> accessibilityLabel }
        }

        return transformContent
    }

    private fun addAllStyleRuns(attributedDescription: JsonObject,  openers: MutableList<Run>,  closers: MutableList<Run>) {
        attributedDescription.getArray("styleRuns")
            .stream()
            .filter { o: Any? -> JsonObject::class.java.isInstance(o) }
            .map { obj: Any? -> JsonObject::class.java.cast(obj) }
            .forEach { run: JsonObject ->
                val start = run.getInt("startIndex", -1)
                val length = run.getInt("length", 0)
                if (start < 0 || length < 1) return@forEach
                val end = start + length

                if (run.has("strikethrough")) {
                    openers.add(Run(STRIKETHROUGH_OPEN, STRIKETHROUGH_CLOSE, start))
                    closers.add(Run(STRIKETHROUGH_OPEN, STRIKETHROUGH_CLOSE, end))
                }

                if (run.getBoolean("italic", false)) {
                    openers.add(Run(ITALIC_OPEN, ITALIC_CLOSE, start))
                    closers.add(Run(ITALIC_OPEN, ITALIC_CLOSE, end))
                }

                if (run.has("weightLabel") && "FONT_WEIGHT_NORMAL" != run.getString("weightLabel")) {
                    openers.add(Run(BOLD_OPEN, BOLD_CLOSE, start))
                    closers.add(Run(BOLD_OPEN, BOLD_CLOSE, end))
                }
            }
    }

    /**
     * Can be a command run, or a style run.
     */
    class Run @JvmOverloads constructor(
            val open: String,
            val close: String,
            @JvmField val pos: Int,
            val transformContent: Function<String, String>? = null) {

        var openPosInOutput: Int = -1

        fun sameOpen(other: Run): Boolean {
            return open == other.open
        }
    }
}
