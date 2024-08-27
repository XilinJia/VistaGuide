package ac.mdiq.vista.downloader

import org.junit.jupiter.api.extension.ExtendWith

/**
 * Use this to annotate tests methods/classes that should only be run when the downloader is of type
 * [DownloaderType.MOCK] or [DownloaderType.RECORDING]. This should be used when e.g. an
 * extractor returns different results each time because the underlying service web page does so. In
 * that case it makes sense to only run the tests with the mock downloader, since the real web page
 * is not reliable, but we still want to make sure that the code correctly interprets the stored and
 * mocked web page data.
 * @see MockOnlyCondition
 */
@Retention(AnnotationRetention.RUNTIME)
@ExtendWith(MockOnlyCondition::class)
annotation class MockOnly(
        /**
         * The reason why the test is mockonly.
         */
        val value: String
)
