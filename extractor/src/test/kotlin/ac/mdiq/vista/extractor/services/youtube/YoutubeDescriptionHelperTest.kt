package ac.mdiq.vista.extractor.services.youtube

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import ac.mdiq.vista.extractor.services.youtube.YoutubeDescriptionHelper.Run
import ac.mdiq.vista.extractor.services.youtube.YoutubeDescriptionHelper.runsToHtml
import java.util.function.Function
import java.util.stream.Collectors

class YoutubeDescriptionHelperTest {
    @Test
    fun testNoRuns() {
        assertRunsToHtml(
            "abc *a* _c_ &lt;br&gt; <br> &lt;a href=\"#\"&gt;test&lt;/a&gt; &nbsp;&amp;amp;",
            listOf(),
            listOf(),
            "abc *a* _c_ <br>\u00a0\n\u00a0<a href=\"#\">test</a>  &amp;"
        )
    }

    @Test
    fun testNormalRuns() {
        assertRunsToHtml(
            "<A>hel<B>lo </B>nic</A>e <C>test</C>",
            listOf(Run("<A>", "</A>", 0), Run("<B>", "</B>", 3),
                Run("<C>", "</C>", 11)),
            listOf(Run("<A>", "</A>", 9), Run("<B>", "</B>", 6),
                Run("<C>", "</C>", 15)),
            "hello nice test"
        )
    }

    @Test
    fun testOverlappingRuns() {
        assertRunsToHtml(
            "01<A>23<B>45</B></A><B>67</B>89",
            listOf(Run("<A>", "</A>", 2), Run("<B>", "</B>", 4)),
            listOf(Run("<A>", "</A>", 6), Run("<B>", "</B>", 8)),
            "0123456789"
        )
    }

    @Test
    fun testTransformingRuns() {
        val tA = Function { _: String -> "whatever" }
        val tD = Function { content: String -> if (content.toInt() % 2 == 0) "even" else "odd" }

        assertRunsToHtml(
            "0<A>whatever</A><C>4</C>5<D>odd</D>89",
            listOf(Run("<A>", "</A>", 1, tA), Run("<B>", "</B>", 2),
                Run("<C>", "</C>", 3), Run("<D>", "</D>", 6, tD)),
            listOf(Run("<A>", "</A>", 4, tA), Run("<B>", "</B>", 3),
                Run("<C>", "</C>", 5), Run("<D>", "</D>", 8, tD)),
            "0123456789"
        )
    }

    companion object {
        private fun assertRunsToHtml(expectedHtml: String, openers: List<Run>, closers: List<Run>, content: String) {
            assertEquals(expectedHtml,
                runsToHtml(
                    openers.stream().sorted(Comparator.comparingInt { run: Run -> run.pos }).collect(Collectors.toList()),
                    closers.stream().sorted(Comparator.comparingInt { run: Run -> run.pos }).collect(Collectors.toList()),
                    content
                )
            )
        }
    }
}
