package ac.mdiq.vista.extractor.feed

import ac.mdiq.vista.extractor.ListExtractor
import ac.mdiq.vista.extractor.StreamingService
import ac.mdiq.vista.extractor.linkhandler.ListLinkHandler
import ac.mdiq.vista.extractor.stream.StreamInfoItem

/**
 * This class helps to extract items from lightweight feeds that the services may provide.
 *
 *
 * YouTube is an example of a service that has this alternative available.
 */
abstract class FeedExtractor(service: StreamingService, listLinkHandler: ListLinkHandler) : ListExtractor<StreamInfoItem>(service, listLinkHandler)
