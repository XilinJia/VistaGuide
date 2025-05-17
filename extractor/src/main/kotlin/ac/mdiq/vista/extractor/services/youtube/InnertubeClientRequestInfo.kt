package ac.mdiq.vista.extractor.services.youtube

import ac.mdiq.vista.extractor.services.youtube.ClientsConstants.ANDROID_CLIENT_ID
import ac.mdiq.vista.extractor.services.youtube.ClientsConstants.ANDROID_CLIENT_NAME
import ac.mdiq.vista.extractor.services.youtube.ClientsConstants.ANDROID_CLIENT_VERSION
import ac.mdiq.vista.extractor.services.youtube.ClientsConstants.DESKTOP_CLIENT_PLATFORM
import ac.mdiq.vista.extractor.services.youtube.ClientsConstants.EMBED_CLIENT_SCREEN
import ac.mdiq.vista.extractor.services.youtube.ClientsConstants.IOS_CLIENT_ID
import ac.mdiq.vista.extractor.services.youtube.ClientsConstants.IOS_CLIENT_NAME
import ac.mdiq.vista.extractor.services.youtube.ClientsConstants.IOS_CLIENT_VERSION
import ac.mdiq.vista.extractor.services.youtube.ClientsConstants.IOS_DEVICE_MODEL
import ac.mdiq.vista.extractor.services.youtube.ClientsConstants.IOS_OS_VERSION
import ac.mdiq.vista.extractor.services.youtube.ClientsConstants.MOBILE_CLIENT_PLATFORM
import ac.mdiq.vista.extractor.services.youtube.ClientsConstants.WATCH_CLIENT_SCREEN
import ac.mdiq.vista.extractor.services.youtube.ClientsConstants.WEB_CLIENT_ID
import ac.mdiq.vista.extractor.services.youtube.ClientsConstants.WEB_CLIENT_NAME
import ac.mdiq.vista.extractor.services.youtube.ClientsConstants.WEB_EMBEDDED_CLIENT_ID
import ac.mdiq.vista.extractor.services.youtube.ClientsConstants.WEB_EMBEDDED_CLIENT_NAME
import ac.mdiq.vista.extractor.services.youtube.ClientsConstants.WEB_HARDCODED_CLIENT_VERSION
import ac.mdiq.vista.extractor.services.youtube.ClientsConstants.WEB_REMIX_HARDCODED_CLIENT_VERSION


class InnertubeClientRequestInfo private constructor(var clientInfo: ClientInfo, var deviceInfo: DeviceInfo) {
    class ClientInfo internal constructor(var clientName: String, var clientVersion: String, var clientScreen: String, var clientId: String?, var visitorData: String?)

    class DeviceInfo internal constructor(var platform: String, var deviceMake: String?, var deviceModel: String?, var osName: String?,
                                         var osVersion: String?, var androidSdkVersion: Int)

    companion object {
        fun ofWebClient(): InnertubeClientRequestInfo {
            return InnertubeClientRequestInfo(
                ClientInfo(WEB_CLIENT_NAME, WEB_HARDCODED_CLIENT_VERSION, WATCH_CLIENT_SCREEN, WEB_CLIENT_ID, null),
                DeviceInfo(DESKTOP_CLIENT_PLATFORM, null, null, null, null, -1))
        }

        fun ofWebEmbeddedPlayerClient(): InnertubeClientRequestInfo {
            return InnertubeClientRequestInfo(
                ClientInfo(WEB_EMBEDDED_CLIENT_NAME, WEB_REMIX_HARDCODED_CLIENT_VERSION, EMBED_CLIENT_SCREEN, WEB_EMBEDDED_CLIENT_ID, null),
                DeviceInfo(DESKTOP_CLIENT_PLATFORM, null, null, null, null, -1))
        }

        fun ofAndroidClient(): InnertubeClientRequestInfo {
            return InnertubeClientRequestInfo(
                ClientInfo(ANDROID_CLIENT_NAME, ANDROID_CLIENT_VERSION, WATCH_CLIENT_SCREEN, ANDROID_CLIENT_ID, null),
                DeviceInfo(MOBILE_CLIENT_PLATFORM, null, null, "Android", "15", 35))
        }

        fun ofIosClient(): InnertubeClientRequestInfo {
            return InnertubeClientRequestInfo(
                ClientInfo(IOS_CLIENT_NAME, IOS_CLIENT_VERSION, WATCH_CLIENT_SCREEN, IOS_CLIENT_ID, null),
                DeviceInfo(MOBILE_CLIENT_PLATFORM, "Apple", IOS_DEVICE_MODEL, "iOS", IOS_OS_VERSION, -1))
        }
    }
}
