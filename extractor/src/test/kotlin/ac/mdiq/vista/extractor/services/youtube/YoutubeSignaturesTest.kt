package ac.mdiq.vista.extractor.services.youtube

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource
import ac.mdiq.vista.downloader.DownloaderTestImpl.Companion.getInstance
import ac.mdiq.vista.extractor.Vista.init
import ac.mdiq.vista.extractor.services.youtube.YoutubeJavaScriptPlayerManager.deobfuscateSignature
import ac.mdiq.vista.extractor.services.youtube.YoutubeJavaScriptPlayerManager.getSignatureTimestamp
import java.io.IOException


internal class YoutubeSignaturesTest {
    @BeforeEach
    @Throws(IOException::class)
    fun setUp() {
        init(getInstance())
        YoutubeTestsUtils.ensureStateless()
    }

    @ValueSource(strings = ["QzUGs1qRTEI", ""])
    @ParameterizedTest
    @Throws(Exception::class)
    fun testSignatureTimestampExtraction(videoId: String) {
        val signatureTimestamp = getSignatureTimestamp(videoId)!!
        Assertions.assertTrue(signatureTimestamp > 0, "signatureTimestamp is <= 0")
    }

    /*
    The first column of the CSV entries is a video ID
    The second one of these entries are not real signatures, but as the deobfuscation function
    manipulates strings, we can use random characters combined as strings to test the extraction
    and the execution of the function
     */
    @CsvSource(value = ["QzUGs1qRTEI,5QjJrWzVcOutYYNyxkDJVkzQDZQxNbbxGi4hRoh2h4PomQMQq9vo2WPHVpHgxRn7qT3WyhRiJa1k1t1DL3lynZtupHmG3wW4qh59faKjtY4UVu", ",7vIK4hG6NbcIEQP4ZIRjonOzuPHh7wTrEgBdEMYyfE4F5Pq0FiGdv04kptb587c8aToH345ETJ8dMbXnpOmjanP3nzgJ0iNg8oHIm8oeQODPSP"
    ])
    @ParameterizedTest
    @Throws(Exception::class)
    fun testSignatureDeobfuscation(videoId: String,  sampleString: String) {
        // As the signature deobfuscation changes frequently with player versions, we can only test
        // that we get a different string than the original one
        Assertions.assertNotEquals(sampleString, deobfuscateSignature(videoId, sampleString))
    }
}
