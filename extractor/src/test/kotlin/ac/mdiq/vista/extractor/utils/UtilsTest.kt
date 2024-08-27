package ac.mdiq.vista.extractor.utils

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.utils.Utils.followGoogleRedirectIfNeeded
import ac.mdiq.vista.extractor.utils.Utils.getBaseUrl
import ac.mdiq.vista.extractor.utils.Utils.mixedNumberWordToLong
import ac.mdiq.vista.extractor.utils.Utils.nonEmptyAndNullJoin

internal class UtilsTest {
    @Test
    @Throws(ParsingException::class)
    fun testMixedNumberWordToLong() {
        Assertions.assertEquals(10, mixedNumberWordToLong("10"))
        Assertions.assertEquals(10.5e3, mixedNumberWordToLong("10.5K").toDouble(), 0.0)
        Assertions.assertEquals(10.5e6, mixedNumberWordToLong("10.5M").toDouble(), 0.0)
        Assertions.assertEquals(10.5e6, mixedNumberWordToLong("10,5M").toDouble(), 0.0)
        Assertions.assertEquals(1.5e9, mixedNumberWordToLong("1,5B").toDouble(), 0.0)
    }

    @Test
    fun testJoin() {
        Assertions.assertEquals("some,random,not-null,stuff", nonEmptyAndNullJoin(",",
            "some", "null", "random", "", "not-null", "", "stuff"))
    }

    @Test
    @Throws(ParsingException::class)
    fun testGetBaseUrl() {
        Assertions.assertEquals("https://www.youtube.com", getBaseUrl("https://www.youtube.com/watch?v=Hu80uDzh8RY"))
        Assertions.assertEquals("vnd.youtube", getBaseUrl("vnd.youtube://www.youtube.com/watch?v=jZViOEv90dI"))
        Assertions.assertEquals("vnd.youtube", getBaseUrl("vnd.youtube:jZViOEv90dI"))
        Assertions.assertEquals("vnd.youtube", getBaseUrl("vnd.youtube://n8X9_MgEdCg"))
        Assertions.assertEquals("https://music.youtube.com",
            getBaseUrl("https://music.youtube.com/watch?v=O0EDx9WAelc"))
    }

    @Test
    fun testFollowGoogleRedirect() {
        Assertions.assertEquals("https://www.youtube.com/watch?v=Hu80uDzh8RY",
            followGoogleRedirectIfNeeded("https://www.google.it/url?sa=t&rct=j&q=&esrc=s&cd=&cad=rja&uact=8&url=https%3A%2F%2Fwww.youtube.com%2Fwatch%3Fv%3DHu80uDzh8RY&source=video"))
        Assertions.assertEquals("https://www.youtube.com/watch?v=0b6cFWG45kA",
            followGoogleRedirectIfNeeded("https://www.google.com/url?sa=t&rct=j&q=&esrc=s&source=video&cd=&cad=rja&uact=8&url=https%3A%2F%2Fwww.youtube.com%2Fwatch%3Fv%3D0b6cFWG45kA"))
        Assertions.assertEquals("https://soundcloud.com/ciaoproduction",
            followGoogleRedirectIfNeeded("https://www.google.com/url?sa=t&url=https%3A%2F%2Fsoundcloud.com%2Fciaoproduction&rct=j&q=&esrc=s&source=web&cd="))

        Assertions.assertEquals("https://www.youtube.com/watch?v=Hu80uDzh8RY&param=xyz",
            followGoogleRedirectIfNeeded("https://www.youtube.com/watch?v=Hu80uDzh8RY&param=xyz"))
        Assertions.assertEquals("https://www.youtube.com/watch?v=Hu80uDzh8RY&url=hello",
            followGoogleRedirectIfNeeded("https://www.youtube.com/watch?v=Hu80uDzh8RY&url=hello"))
    }
}
