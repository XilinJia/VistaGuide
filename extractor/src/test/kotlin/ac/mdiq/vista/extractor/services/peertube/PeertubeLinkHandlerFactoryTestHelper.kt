package ac.mdiq.vista.extractor.services.peertube

import org.junit.jupiter.api.Assertions
import ac.mdiq.vista.extractor.exceptions.ParsingException
import ac.mdiq.vista.extractor.linkhandler.LinkHandlerFactory
import ac.mdiq.vista.extractor.linkhandler.ListLinkHandlerFactory

object PeertubeLinkHandlerFactoryTestHelper {

    @Throws(ParsingException::class)
    fun assertDoNotAcceptNonURLs(linkHandler: LinkHandlerFactory) {
        Assertions.assertFalse(linkHandler.acceptUrl("orchestr/a/"))
        Assertions.assertFalse(linkHandler.acceptUrl("/a/"))
        Assertions.assertFalse(linkHandler.acceptUrl("something/c/"))
        Assertions.assertFalse(linkHandler.acceptUrl("/c/"))
        Assertions.assertFalse(linkHandler.acceptUrl("videos/"))
        Assertions.assertFalse(linkHandler.acceptUrl("I-hate-videos/"))
        Assertions.assertFalse(linkHandler.acceptUrl("/w/"))
        Assertions.assertFalse(linkHandler.acceptUrl("ksmg/w/"))
        Assertions.assertFalse(linkHandler.acceptUrl("a reandom search query"))
        Assertions.assertFalse(linkHandler.acceptUrl("test 230 "))
        Assertions.assertFalse(linkHandler.acceptUrl("986513"))
    }


    @Throws(ParsingException::class)
    fun assertDoNotAcceptNonURLs(linkHandler: ListLinkHandlerFactory) {
        Assertions.assertFalse(linkHandler.acceptUrl("orchestr/a/"))
        Assertions.assertFalse(linkHandler.acceptUrl("/a/"))
        Assertions.assertFalse(linkHandler.acceptUrl("something/c/"))
        Assertions.assertFalse(linkHandler.acceptUrl("/c/"))
        Assertions.assertFalse(linkHandler.acceptUrl("videos/"))
        Assertions.assertFalse(linkHandler.acceptUrl("I-hate-videos/"))
        Assertions.assertFalse(linkHandler.acceptUrl("/w/"))
        Assertions.assertFalse(linkHandler.acceptUrl("ksmg/w/"))
        Assertions.assertFalse(linkHandler.acceptUrl("a reandom search query"))
        Assertions.assertFalse(linkHandler.acceptUrl("test 230 "))
        Assertions.assertFalse(linkHandler.acceptUrl("986513"))
    }
}
