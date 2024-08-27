package ac.mdiq.vista.extractor.services.youtube

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import ac.mdiq.vista.downloader.DownloaderFactory
import ac.mdiq.vista.downloader.DownloaderFactory.getDownloader
import ac.mdiq.vista.downloader.DownloaderTestImpl.Companion.getInstance
import ac.mdiq.vista.extractor.ExtractorAsserts
import ac.mdiq.vista.extractor.ExtractorAsserts.assertContains
import ac.mdiq.vista.extractor.ExtractorAsserts.assertEmpty
import ac.mdiq.vista.extractor.ExtractorAsserts.assertNotEmpty
import ac.mdiq.vista.extractor.ExtractorAsserts.assertTabsContain
import ac.mdiq.vista.extractor.Vista.init
import ac.mdiq.vista.extractor.ServiceList.YouTube
import ac.mdiq.vista.extractor.channel.ChannelExtractor
import ac.mdiq.vista.extractor.channel.tabs.ChannelTabExtractor
import ac.mdiq.vista.extractor.channel.tabs.ChannelTabs
import ac.mdiq.vista.extractor.exceptions.AccountTerminatedException
import ac.mdiq.vista.extractor.exceptions.ContentNotAvailableException
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.linkhandler.ListLinkHandler
import ac.mdiq.vista.extractor.linkhandler.ReadyChannelTabListLinkHandler
import ac.mdiq.vista.extractor.services.BaseChannelExtractorTest
import ac.mdiq.vista.extractor.services.DefaultTests.defaultTestGetPageInNewExtractor
import ac.mdiq.vista.extractor.services.youtube.extractors.YoutubeChannelExtractor
import ac.mdiq.vista.extractor.services.youtube.extractors.YoutubeChannelTabPlaylistExtractor
import java.io.IOException

/**
 * Test for [ChannelExtractor]
 */
object YoutubeChannelExtractorTest {
    private const val RESOURCE_PATH = DownloaderFactory.RESOURCE_PATH + "services/youtube/extractor/channel/"

    @Nested
    class NotAvailable {
        @Test
        @Throws(Exception::class)
        fun deletedFetch() {
            val extractor: ChannelExtractor = YouTube.getChannelExtractor("https://www.youtube.com/channel/UCAUc4iz6edWerIjlnL8OSSw")

            assertThrows(ContentNotAvailableException::class.java) { extractor.fetchPage() }
        }

        @Test
        @Throws(Exception::class)
        fun nonExistentFetch() {
            val extractor: ChannelExtractor = YouTube.getChannelExtractor("https://www.youtube.com/channel/DOESNT-EXIST")
            assertThrows(ContentNotAvailableException::class.java) { extractor.fetchPage() }
        }

        @Test
        @Throws(Exception::class)
        fun accountTerminatedTOSFetch() {
            // "This account has been terminated for a violation of YouTube's Terms of Service."
            val extractor: ChannelExtractor =
                YouTube.getChannelExtractor("https://www.youtube.com/channel/UCTGjY2I-ZUGnwVoWAGRd7XQ")

            val ex =
                assertThrows(AccountTerminatedException::class.java
                ) { extractor.fetchPage() }
            assertEquals(AccountTerminatedException.Reason.VIOLATION, ex.reason)
        }

        @Test
        @Throws(Exception::class)
        fun accountTerminatedCommunityFetch() {
            // "This account has been terminated for violating YouTube's Community Guidelines."
            val extractor: ChannelExtractor =
                YouTube.getChannelExtractor("https://www.youtube.com/channel/UC0AuOxCr9TZ0TtEgL1zpIgA")

            val ex =
                assertThrows(AccountTerminatedException::class.java
                ) { extractor.fetchPage() }
            assertEquals(AccountTerminatedException.Reason.VIOLATION, ex.reason)
        }

        @Test
        @Throws(Exception::class)
        fun accountTerminatedHateFetch() {
            // "This account has been terminated due to multiple or severe violations
            // of YouTube's policy prohibiting hate speech."
            val extractor: ChannelExtractor =
                YouTube.getChannelExtractor("https://www.youtube.com/channel/UCPWXIOPK-9myzek6jHR5yrg")

            val ex =
                assertThrows(AccountTerminatedException::class.java
                ) { extractor.fetchPage() }
            assertEquals(AccountTerminatedException.Reason.VIOLATION, ex.reason)
        }

        @Test
        @Throws(Exception::class)
        fun accountTerminatedBullyFetch() {
            // "This account has been terminated due to multiple or severe violations
            // of YouTube's policy prohibiting content designed to harass, bully or threaten."
            val extractor: ChannelExtractor =
                YouTube.getChannelExtractor("https://youtube.com/channel/UCB1o7_gbFp2PLsamWxFenBg")

            val ex =
                assertThrows(AccountTerminatedException::class.java
                ) { extractor.fetchPage() }
            assertEquals(AccountTerminatedException.Reason.VIOLATION, ex.reason)
        }

        @Test
        @Throws(Exception::class)
        fun accountTerminatedSpamFetch() {
            // "This account has been terminated due to multiple or severe violations
            // of YouTube's policy against spam, deceptive practices and misleading content
            // or other Terms of Service violations."
            val extractor: ChannelExtractor =
                YouTube.getChannelExtractor("https://www.youtube.com/channel/UCoaO4U_p7G7AwalqSbGCZOA")

            val ex =
                assertThrows(AccountTerminatedException::class.java
                ) { extractor.fetchPage() }
            assertEquals(AccountTerminatedException.Reason.VIOLATION, ex.reason)
        }

        @Test
        @Throws(Exception::class)
        fun accountTerminatedCopyrightFetch() {
            // "This account has been terminated because we received multiple third-party claims
            // of copyright infringement regarding material that the user posted."
            val extractor: ChannelExtractor =
                YouTube.getChannelExtractor("https://www.youtube.com/channel/UCI4i4RgFT5ilfMpna4Z_Y8w")

            val ex =
                assertThrows(AccountTerminatedException::class.java
                ) { extractor.fetchPage() }
            assertEquals(AccountTerminatedException.Reason.VIOLATION, ex.reason)
        }

        companion object {

            @BeforeAll
            @Throws(IOException::class)
            fun setUp() {
                YoutubeTestsUtils.ensureStateless()
                init(getDownloader(RESOURCE_PATH + "notAvailable"))
            }
        }
    }

    internal class SystemTopic {
        @Test
        @Throws(Exception::class)
        fun noSupportedTab() {
            val extractor: ChannelExtractor =
                YouTube.getChannelExtractor("https://invidio.us/channel/UC-9-kyTW8ZkZNDHQJ6FgpwQ")

            extractor.fetchPage()
            assertTrue(extractor.getTabs().isEmpty())
        }

        companion object {

            @BeforeAll
            @Throws(IOException::class)
            fun setUp() {
                YoutubeTestsUtils.ensureStateless()
                init(getDownloader(RESOURCE_PATH + "systemTopic"))
            }
        }
    }

    class Gronkh : BaseChannelExtractorTest {
        @Test
        override fun testServiceId() {
            assertEquals(YouTube.serviceId, extractor!!.serviceId)
        }

        @Test
        @Throws(Exception::class)
        override fun testName() {
            assertEquals("Gronkh", extractor!!.getName())
        }

        @Test
        @Throws(Exception::class)
        override fun testId() {
            assertEquals("UCYJ61XIK64sp6ZFFS8sctxw", extractor!!.id)
        }

        @Test
        @Throws(ParsingException::class)
        override fun testUrl() {
            assertEquals("https://www.youtube.com/channel/UCYJ61XIK64sp6ZFFS8sctxw", extractor!!.url)
        }

        @Test
        @Throws(ParsingException::class)
        override fun testOriginalUrl() {
            assertEquals("http://www.youtube.com/@Gronkh", extractor!!.originalUrl)
        }

        @Test
        @Throws(Exception::class)
        override fun testDescription() {
            assertContains("Ungebremster Spieltrieb seit 1896.", extractor!!.getDescription()?:"")
        }

        @Test
        @Throws(Exception::class)
        override fun testAvatars() {
            YoutubeTestsUtils.testImages(extractor!!.getAvatars())
        }

        @Test
        @Throws(Exception::class)
        override fun testBanners() {
            YoutubeTestsUtils.testImages(extractor!!.getBanners())
        }

        @Test
        @Throws(Exception::class)
        override fun testFeedUrl() {
            assertEquals("https://www.youtube.com/feeds/videos.xml?channel_id=UCYJ61XIK64sp6ZFFS8sctxw",
                extractor!!.getFeedUrl())
        }

        @Test
        @Throws(Exception::class)
        override fun testSubscriberCount() {
            ExtractorAsserts.assertGreaterOrEqual(4900000, extractor!!.getSubscriberCount())
        }

        @Test
        @Throws(Exception::class)
        override fun testVerified() {
            assertTrue(extractor!!.isVerified())
        }

        @Test
        @Throws(Exception::class)
        override fun testTabs() {
            assertTabsContain(extractor!!.getTabs(), ChannelTabs.VIDEOS,
                ChannelTabs.LIVESTREAMS, ChannelTabs.PLAYLISTS)
            assertTrue(extractor!!.getTabs().stream()
                .filter { it: ListLinkHandler -> ChannelTabs.VIDEOS == it.contentFilters[0] }
                .allMatch { o: ListLinkHandler? -> ReadyChannelTabListLinkHandler::class.java.isInstance(o) })
        }

        @Test
        @Throws(Exception::class)
        override fun testTags() {
            assertTrue(extractor!!.getTags().contains("gronkh"))
        }

        companion object {
            private var extractor: YoutubeChannelExtractor? = null


            @BeforeAll
            @Throws(Exception::class)
            fun setUp() {
                YoutubeTestsUtils.ensureStateless()
                init(getDownloader(RESOURCE_PATH + "gronkh"))
                extractor = YouTube.getChannelExtractor("http://www.youtube.com/@Gronkh") as? YoutubeChannelExtractor
                extractor!!.fetchPage()
            }
        }
    }

    // YouTube RED/Premium ad blocking test
    class VSauce : BaseChannelExtractorTest {
        @Test
        override fun testServiceId() {
            assertEquals(YouTube.serviceId, extractor!!.serviceId)
        }

        @Test
        @Throws(Exception::class)
        override fun testName() {
            assertEquals("Vsauce", extractor!!.getName())
        }

        @Test
        @Throws(Exception::class)
        override fun testId() {
            assertEquals("UC6nSFpj9HTCZ5t-N3Rm3-HA", extractor!!.id)
        }

        @Test
        @Throws(ParsingException::class)
        override fun testUrl() {
            assertEquals("https://www.youtube.com/channel/UC6nSFpj9HTCZ5t-N3Rm3-HA", extractor!!.url)
        }

        @Test
        @Throws(ParsingException::class)
        override fun testOriginalUrl() {
            assertEquals("https://www.youtube.com/user/Vsauce", extractor!!.originalUrl)
        }

        @Test
        @Throws(Exception::class)
        override fun testDescription() {
            assertContains("Our World is Amazing", extractor!!.getDescription()?:"")
        }

        @Test
        @Throws(Exception::class)
        override fun testAvatars() {
            YoutubeTestsUtils.testImages(extractor!!.getAvatars())
        }

        @Test
        @Throws(Exception::class)
        override fun testBanners() {
            YoutubeTestsUtils.testImages(extractor!!.getBanners())
        }

        @Test
        @Throws(Exception::class)
        override fun testFeedUrl() {
            assertEquals("https://www.youtube.com/feeds/videos.xml?channel_id=UC6nSFpj9HTCZ5t-N3Rm3-HA",
                extractor!!.getFeedUrl())
        }

        @Test
        @Throws(Exception::class)
        override fun testSubscriberCount() {
            ExtractorAsserts.assertGreaterOrEqual(17000000, extractor!!.getSubscriberCount())
        }

        @Test
        @Throws(Exception::class)
        override fun testVerified() {
            assertTrue(extractor!!.isVerified())
        }

        @Test
        @Throws(Exception::class)
        override fun testTabs() {
            assertTabsContain(extractor!!.getTabs(), ChannelTabs.VIDEOS, ChannelTabs.LIVESTREAMS,
                ChannelTabs.SHORTS, ChannelTabs.PLAYLISTS)
            assertTrue(extractor!!.getTabs().stream()
                .filter { it: ListLinkHandler -> ChannelTabs.VIDEOS == it.contentFilters[0] }
                .allMatch { o: ListLinkHandler? -> ReadyChannelTabListLinkHandler::class.java.isInstance(o) })
        }

        @Test
        @Throws(Exception::class)
        override fun testTags() {
            assertTrue(extractor!!.getTags().containsAll(listOf("questions", "education",
                "learning", "schools", "Science")))
        }

        companion object {
            private var extractor: YoutubeChannelExtractor? = null


            @BeforeAll
            @Throws(Exception::class)
            fun setUp() {
                YoutubeTestsUtils.ensureStateless()
                init(getDownloader(RESOURCE_PATH + "VSauce"))
                extractor = YouTube.getChannelExtractor("https://www.youtube.com/user/Vsauce") as? YoutubeChannelExtractor
                extractor!!.fetchPage()
            }
        }
    }

    class Kurzgesagt : BaseChannelExtractorTest {
        @Test
        override fun testServiceId() {
            assertEquals(YouTube.serviceId, extractor!!.serviceId)
        }

        @Test
        @Throws(Exception::class)
        override fun testName() {
            assertTrue(extractor!!.getName().startsWith("Kurzgesagt"))
        }

        @Test
        @Throws(Exception::class)
        override fun testId() {
            assertEquals("UCsXVk37bltHxD1rDPwtNM8Q", extractor!!.id)
        }

        @Test
        @Throws(ParsingException::class)
        override fun testUrl() {
            assertEquals("https://www.youtube.com/channel/UCsXVk37bltHxD1rDPwtNM8Q", extractor!!.url)
        }

        @Test
        @Throws(ParsingException::class)
        override fun testOriginalUrl() {
            assertEquals("https://www.youtube.com/channel/UCsXVk37bltHxD1rDPwtNM8Q", extractor!!.originalUrl)
        }

        @Test
        @Throws(Exception::class)
        override fun testDescription() {
            assertContains("science", extractor!!.getDescription()!!)
            assertContains("animators", extractor!!.getDescription()!!)
            //TODO: Description get cuts out, because the og:description is optimized and don't have all the content
            //assertTrue(description, description.contains("Currently we make one animation video per month"));
        }

        @Test
        @Throws(Exception::class)
        override fun testAvatars() {
            YoutubeTestsUtils.testImages(extractor!!.getAvatars())
        }

        @Test
        @Throws(Exception::class)
        override fun testBanners() {
            YoutubeTestsUtils.testImages(extractor!!.getBanners())
        }

        @Test
        @Throws(Exception::class)
        override fun testFeedUrl() {
            assertEquals("https://www.youtube.com/feeds/videos.xml?channel_id=UCsXVk37bltHxD1rDPwtNM8Q",
                extractor!!.getFeedUrl())
        }

        @Test
        @Throws(Exception::class)
        override fun testSubscriberCount() {
            ExtractorAsserts.assertGreaterOrEqual(17000000, extractor!!.getSubscriberCount())
        }

        @Test
        @Throws(Exception::class)
        override fun testVerified() {
            assertTrue(extractor!!.isVerified())
        }

        @Test
        @Throws(Exception::class)
        override fun testTabs() {
            assertTabsContain(extractor!!.getTabs(), ChannelTabs.VIDEOS, ChannelTabs.SHORTS,
                ChannelTabs.PLAYLISTS)
            assertTrue(extractor!!.getTabs().stream()
                .filter { it: ListLinkHandler -> ChannelTabs.VIDEOS == it.contentFilters[0] }
                .allMatch { o: ListLinkHandler? -> ReadyChannelTabListLinkHandler::class.java.isInstance(o) })
        }

        @Test
        @Throws(Exception::class)
        override fun testTags() {
            assertTrue(extractor!!.getTags().containsAll(listOf("universe", "Science",
                "black hole", "humanism", "evolution")))
        }

        companion object {
            private var extractor: YoutubeChannelExtractor? = null


            @BeforeAll
            @Throws(Exception::class)
            fun setUp() {
                YoutubeTestsUtils.ensureStateless()
                init(getDownloader(RESOURCE_PATH + "kurzgesagt"))
                extractor = YouTube.getChannelExtractor("https://www.youtube.com/channel/UCsXVk37bltHxD1rDPwtNM8Q") as? YoutubeChannelExtractor
                extractor!!.fetchPage()
            }
        }
    }

    class KurzgesagtAdditional {
        @Test
        @Throws(Exception::class)
        fun testGetPageInNewExtractor() {
            val newExtractor: ChannelExtractor = YouTube.getChannelExtractor(extractor!!.url)
            newExtractor.fetchPage()
            val newTabExtractor: ChannelTabExtractor = YouTube.getChannelTabExtractor(newExtractor.getTabs()[0])
            defaultTestGetPageInNewExtractor(tabExtractor!!, newTabExtractor)
        }

        companion object {
            private var extractor: YoutubeChannelExtractor? = null
            private var tabExtractor: ChannelTabExtractor? = null


            @BeforeAll
            @Throws(Exception::class)
            fun setUp() {
                // Test is not deterministic, mocks can't be used
                init(getInstance())
                extractor = YouTube.getChannelExtractor("https://www.youtube.com/channel/UCsXVk37bltHxD1rDPwtNM8Q") as? YoutubeChannelExtractor
                extractor!!.fetchPage()

                tabExtractor = YouTube.getChannelTabExtractor(extractor!!.getTabs()[0])
                tabExtractor!!.fetchPage()
            }
        }
    }

    class CaptainDisillusion : BaseChannelExtractorTest {
        @Test
        override fun testServiceId() {
            assertEquals(YouTube.serviceId, extractor!!.serviceId)
        }

        @Test
        @Throws(Exception::class)
        override fun testName() {
            assertEquals("Captain Disillusion", extractor!!.getName())
        }

        @Test
        @Throws(Exception::class)
        override fun testId() {
            assertEquals("UCEOXxzW2vU0P-0THehuIIeg", extractor!!.id)
        }

        @Test
        @Throws(ParsingException::class)
        override fun testUrl() {
            assertEquals("https://www.youtube.com/channel/UCEOXxzW2vU0P-0THehuIIeg", extractor!!.url)
        }

        @Test
        @Throws(ParsingException::class)
        override fun testOriginalUrl() {
            assertEquals("https://www.youtube.com/user/CaptainDisillusion/videos", extractor!!.originalUrl)
        }

        @Test
        @Throws(Exception::class)
        override fun testDescription() {
            assertContains("In a world where", extractor!!.getDescription()!!)
        }

        @Test
        @Throws(Exception::class)
        override fun testAvatars() {
            YoutubeTestsUtils.testImages(extractor!!.getAvatars())
        }

        @Test
        @Throws(Exception::class)
        override fun testBanners() {
            YoutubeTestsUtils.testImages(extractor!!.getBanners())
        }

        @Test
        @Throws(Exception::class)
        override fun testFeedUrl() {
            assertEquals("https://www.youtube.com/feeds/videos.xml?channel_id=UCEOXxzW2vU0P-0THehuIIeg",
                extractor!!.getFeedUrl())
        }

        @Test
        @Throws(Exception::class)
        override fun testSubscriberCount() {
            ExtractorAsserts.assertGreaterOrEqual(2000000, extractor!!.getSubscriberCount())
        }

        @Test
        @Throws(Exception::class)
        override fun testVerified() {
            assertTrue(extractor!!.isVerified())
        }

        @Test
        @Throws(Exception::class)
        override fun testTabs() {
            assertTabsContain(extractor!!.getTabs(), ChannelTabs.VIDEOS, ChannelTabs.PLAYLISTS, ChannelTabs.SHORTS)
            assertTrue(extractor!!.getTabs().stream()
                .filter { it: ListLinkHandler -> ChannelTabs.VIDEOS == it.contentFilters[0] }
                .allMatch { o: ListLinkHandler? -> ReadyChannelTabListLinkHandler::class.java.isInstance(o) })
        }

        @Test
        @Throws(Exception::class)
        override fun testTags() {
            assertTrue(extractor!!.getTags().containsAll(listOf("critical thinking",
                "visual effects", "VFX", "sci-fi", "humor")))
        }

        companion object {
            private var extractor: YoutubeChannelExtractor? = null


            @BeforeAll
            @Throws(Exception::class)
            fun setUp() {
                YoutubeTestsUtils.ensureStateless()
                init(getDownloader(RESOURCE_PATH + "captainDisillusion"))
                extractor = YouTube.getChannelExtractor("https://www.youtube.com/user/CaptainDisillusion/videos") as? YoutubeChannelExtractor
                extractor!!.fetchPage()
            }
        }
    }

    class RandomChannel : BaseChannelExtractorTest {
        @Test
        override fun testServiceId() {
            assertEquals(YouTube.serviceId, extractor!!.serviceId)
        }

        @Test
        @Throws(Exception::class)
        override fun testName() {
            assertEquals("random channel", extractor!!.getName())
        }

        @Test
        @Throws(Exception::class)
        override fun testId() {
            assertEquals("UCUaQMQS9lY5lit3vurpXQ6w", extractor!!.id)
        }

        @Test
        @Throws(ParsingException::class)
        override fun testUrl() {
            assertEquals("https://www.youtube.com/channel/UCUaQMQS9lY5lit3vurpXQ6w", extractor!!.url)
        }

        @Test
        @Throws(ParsingException::class)
        override fun testOriginalUrl() {
            assertEquals("https://www.youtube.com/channel/UCUaQMQS9lY5lit3vurpXQ6w", extractor!!.originalUrl)
        }

        @Test
        @Throws(Exception::class)
        override fun testDescription() {
            assertContains("Hey there iu will upoload a load of pranks onto this channel", extractor!!.getDescription()!!)
        }

        @Test
        @Throws(Exception::class)
        override fun testAvatars() {
            YoutubeTestsUtils.testImages(extractor!!.getAvatars())
        }

        @Test
        @Throws(Exception::class)
        override fun testBanners() {
            YoutubeTestsUtils.testImages(extractor!!.getBanners())
        }

        @Test
        @Throws(Exception::class)
        override fun testFeedUrl() {
            assertEquals("https://www.youtube.com/feeds/videos.xml?channel_id=UCUaQMQS9lY5lit3vurpXQ6w", extractor!!.getFeedUrl())
        }

        @Test
        @Throws(Exception::class)
        override fun testSubscriberCount() {
            ExtractorAsserts.assertGreaterOrEqual(50, extractor!!.getSubscriberCount())
        }

        @Test
        @Throws(Exception::class)
        override fun testVerified() {
            assertFalse(extractor!!.isVerified())
        }

        @Test
        @Throws(Exception::class)
        override fun testTabs() {
            assertTabsContain(extractor!!.getTabs(), ChannelTabs.VIDEOS)
            assertTrue(extractor!!.getTabs().stream()
                .filter { it: ListLinkHandler -> ChannelTabs.VIDEOS == it.contentFilters[0] }
                .allMatch { o: ListLinkHandler? -> ReadyChannelTabListLinkHandler::class.java.isInstance(o) })
        }

        @Test
        @Throws(Exception::class)
        override fun testTags() {
            assertTrue(extractor!!.getTags().isEmpty())
        }

        companion object {
            private var extractor: YoutubeChannelExtractor? = null


            @BeforeAll
            @Throws(Exception::class)
            fun setUp() {
                YoutubeTestsUtils.ensureStateless()
                init(getDownloader(RESOURCE_PATH + "random"))
                extractor = YouTube.getChannelExtractor("https://www.youtube.com/channel/UCUaQMQS9lY5lit3vurpXQ6w") as? YoutubeChannelExtractor
                extractor!!.fetchPage()
            }
        }
    }

    class CarouselHeader : BaseChannelExtractorTest {
        @Test
        override fun testServiceId() {
            assertEquals(YouTube.serviceId, extractor!!.serviceId)
        }

        @Test
        @Throws(Exception::class)
        override fun testName() {
            assertEquals("Sports", extractor!!.getName())
        }

        @Test
        @Throws(Exception::class)
        override fun testId() {
            assertEquals("UCEgdi0XIXXZ-qJOFPf4JSKw", extractor!!.id)
        }

        @Test
        @Throws(ParsingException::class)
        override fun testUrl() {
            assertEquals("https://www.youtube.com/channel/UCEgdi0XIXXZ-qJOFPf4JSKw", extractor!!.url)
        }

        @Test
        @Throws(ParsingException::class)
        override fun testOriginalUrl() {
            assertEquals("https://www.youtube.com/channel/UCEgdi0XIXXZ-qJOFPf4JSKw", extractor!!.originalUrl)
        }

        @Test
        @Throws(ParsingException::class)
        override fun testDescription() {
            assertNull(extractor!!.getDescription())
        }

        @Test
        @Throws(Exception::class)
        override fun testAvatars() {
            YoutubeTestsUtils.testImages(extractor!!.getAvatars())
        }

        @Test
        override fun testBanners() {
            // A CarouselHeaderRenderer doesn't contain a banner
            assertEmpty(extractor!!.getBanners())
        }

        @Test
        @Throws(Exception::class)
        override fun testFeedUrl() {
            assertEquals("https://www.youtube.com/feeds/videos.xml?channel_id=UCEgdi0XIXXZ-qJOFPf4JSKw",
                extractor!!.getFeedUrl())
        }

        @Test
        @Throws(Exception::class)
        override fun testSubscriberCount() {
            ExtractorAsserts.assertGreaterOrEqual(70000000, extractor!!.getSubscriberCount())
        }

        @Test
        @Throws(Exception::class)
        override fun testVerified() {
            assertTrue(extractor!!.isVerified())
        }

        @Test
        @Throws(Exception::class)
        override fun testTabs() {
            assertEmpty(extractor!!.getTabs())
        }

        @Test
        @Throws(Exception::class)
        override fun testTags() {
            assertEmpty(extractor!!.getTags())
        }

        companion object {
            private var extractor: YoutubeChannelExtractor? = null


            @BeforeAll
            @Throws(Exception::class)
            fun setUp() {
                YoutubeTestsUtils.ensureStateless()
                init(getDownloader(RESOURCE_PATH + "carouselHeader"))
                extractor = YouTube.getChannelExtractor("https://www.youtube.com/channel/UCEgdi0XIXXZ-qJOFPf4JSKw") as? YoutubeChannelExtractor
                extractor!!.fetchPage()
            }
        }
    }

    /**
     * A YouTube channel which is age-restricted and requires login to view its contents on a
     * channel page.
     *
     *
     *
     * Note that age-restrictions on channels may not apply for countries, so check that the
     * channel is age-restricted in the network you use to update the test's mocks before updating
     * them.
     *
     */
    internal class AgeRestrictedChannel : BaseChannelExtractorTest {
        @Test
        @Throws(Exception::class)
        override fun testDescription() {
            // Description cannot be extracted from age-restricted channels
            assertTrue(extractor?.getDescription().isNullOrEmpty())
        }

        @Test
        @Throws(Exception::class)
        override fun testAvatars() {
            YoutubeTestsUtils.testImages(extractor?.getAvatars())
        }

        @Test
        @Throws(Exception::class)
        override fun testBanners() {
            // Banners cannot be extracted from age-restricted channels
            assertEmpty(extractor?.getBanners())
        }

        @Test
        @Throws(Exception::class)
        override fun testFeedUrl() {
            assertEquals("https://www.youtube.com/feeds/videos.xml?channel_id=UCbfnHqxXs_K3kvaH-WlNlig", extractor?.getFeedUrl())
        }

        @Test
        @Throws(Exception::class)
        override fun testSubscriberCount() {
            // Subscriber count cannot be extracted from age-restricted channels
            assertEquals(ChannelExtractor.UNKNOWN_SUBSCRIBER_COUNT, extractor?.getSubscriberCount())
        }

        @Test
        @Throws(Exception::class)
        override fun testServiceId() {
            assertEquals(YouTube.serviceId, extractor!!.serviceId)
        }

        @Test
        @Throws(Exception::class)
        override fun testName() {
            assertEquals("Laphroaig Whisky", extractor?.getName())
        }

        @Test
        @Throws(Exception::class)
        override fun testId() {
            assertEquals("UCbfnHqxXs_K3kvaH-WlNlig", extractor!!.id)
        }

        @Test
        @Throws(Exception::class)
        override fun testUrl() {
            assertEquals("https://www.youtube.com/channel/UCbfnHqxXs_K3kvaH-WlNlig",
                extractor!!.url)
        }

        @Test
        @Throws(Exception::class)
        override fun testOriginalUrl() {
            assertEquals("https://www.youtube.com/channel/UCbfnHqxXs_K3kvaH-WlNlig",
                extractor!!.originalUrl)
        }

        @Test
        @Throws(Exception::class)
        override fun testVerified() {
            // Verification status cannot be extracted from age-restricted channels
            assertFalse(extractor!!.isVerified())
        }

        @Test
        @Throws(Exception::class)
        override fun testTabs() {
            // Channel tabs which may be available and which will be extracted from channel system
            // uploads playlists
            assertTabsContain(extractor!!.getTabs(),
                ChannelTabs.VIDEOS, ChannelTabs.SHORTS, ChannelTabs.LIVESTREAMS)

            // Check if all tabs are not classic tabs, so that link handlers are of the appropriate
            // type and build YoutubeChannelTabPlaylistExtractor instances
            assertTrue(extractor!!.getTabs()
                .stream()
                .allMatch { linkHandler ->
                    linkHandler::class.java === ReadyChannelTabListLinkHandler::class.java && (linkHandler as ReadyChannelTabListLinkHandler)
                        .getChannelTabExtractor(extractor!!.service)
                        .javaClass == YoutubeChannelTabPlaylistExtractor::class.java
                })
        }

        @Test
        @Throws(Exception::class)
        override fun testTags() {
            // Tags cannot be extracted from age-restricted channels
            assertTrue(extractor!!.getTags().isEmpty())
        }

        companion object {
            private var extractor: ChannelExtractor? = null


            @BeforeAll
            @Throws(Exception::class)
            fun setUp() {
                YoutubeTestsUtils.ensureStateless()
                init(getDownloader(RESOURCE_PATH + "ageRestricted"))
                extractor = YouTube.getChannelExtractor(
                    "https://www.youtube.com/channel/UCbfnHqxXs_K3kvaH-WlNlig")
                extractor!!.fetchPage()
            }
        }
    }

    internal class InteractiveTabbedHeader : BaseChannelExtractorTest {
        @Test
        @Throws(Exception::class)
        override fun testDescription() {
            // The description changes frequently and there is no significant common word, so only
            // check if it is not empty
            assertNotEmpty(extractor?.getDescription()!!)
        }

        @Test
        @Throws(Exception::class)
        override fun testAvatars() {
            YoutubeTestsUtils.testImages(extractor?.getAvatars())
        }

        @Test
        @Throws(Exception::class)
        override fun testBanners() {
            YoutubeTestsUtils.testImages(extractor?.getBanners())
        }

        @Test
        @Throws(Exception::class)
        override fun testFeedUrl() {
            assertEquals("https://www.youtube.com/feeds/videos.xml?channel_id=UCQvWX73GQygcwXOTSf_VDVg", extractor?.getFeedUrl())
        }

        @Test
        @Throws(Exception::class)
        override fun testSubscriberCount() {
            // Subscriber count is not available on channels with an interactiveTabbedHeaderRenderer
            assertEquals(ChannelExtractor.UNKNOWN_SUBSCRIBER_COUNT, extractor?.getSubscriberCount())
        }

        @Test
        @Throws(Exception::class)
        override fun testVerified() {
            assertTrue(extractor!!.isVerified())
        }

        @Test
        @Throws(Exception::class)
        override fun testTabs() {
            // Gaming topic channels tabs are not yet supported, so an empty list should be returned
            assertTabsContain(extractor!!.getTabs(), ChannelTabs.SHORTS)
        }

        @Test
        @Throws(Exception::class)
        override fun testTags() {
            assertTrue(extractor?.getTags().isNullOrEmpty())
        }

        @Test
        @Throws(Exception::class)
        override fun testServiceId() {
            assertEquals(YouTube.serviceId, extractor!!.serviceId)
        }

        @Test
        @Throws(Exception::class)
        override fun testName() {
            assertContains("Minecraft", extractor?.getName()!!)
        }

        @Test
        @Throws(Exception::class)
        override fun testId() {
            assertEquals("UCQvWX73GQygcwXOTSf_VDVg", extractor!!.id)
        }

        @Test
        @Throws(Exception::class)
        override fun testUrl() {
            assertEquals("https://www.youtube.com/channel/UCQvWX73GQygcwXOTSf_VDVg", extractor!!.url)
        }

        @Test
        @Throws(Exception::class)
        override fun testOriginalUrl() {
            assertEquals("https://www.youtube.com/channel/UCQvWX73GQygcwXOTSf_VDVg",
                extractor!!.originalUrl)
        }

        companion object {
            private var extractor: ChannelExtractor? = null


            @BeforeAll
            @Throws(Exception::class)
            fun setUp() {
                YoutubeTestsUtils.ensureStateless()
                init(getDownloader(RESOURCE_PATH + "interactiveTabbedHeader"))
                extractor = YouTube.getChannelExtractor(
                    "https://www.youtube.com/channel/UCQvWX73GQygcwXOTSf_VDVg")
                extractor!!.fetchPage()
            }
        }
    }
}
