package app.octocon.app

import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import platform.Foundation.NSUserActivity
import platform.Foundation.NSUserActivityTypeBrowsingWeb
import kotlin.experimental.ExperimentalObjCName

val iosDeepLinkFlow = MutableStateFlow<String?>(null)

@Suppress("unused")
@OptIn(ExperimentalObjCName::class, ExperimentalObjCName::class)
@ObjCName(swiftName = "IosLinkHandler")
class IosLinkHandler {
  @ObjCName("onDeepLinkReceived")
  fun onDeepLinkReceived(url: String) {
    treatAndFireDeepLink(url)
  }

  @ObjCName("onDeepLinkReceived")
  fun onDeepLinkReceived(userActivity: NSUserActivity) {
    userActivity.getUrlString()?.let { treatAndFireDeepLink(it) }
  }

  private fun treatAndFireDeepLink(deepLinkUri: String) {
    MainScope().launch {
      iosDeepLinkFlow.emit(deepLinkUri)
    }
  }

  private fun NSUserActivity.getUrlString() =
    if (this.activityType == NSUserActivityTypeBrowsingWeb) {
      this.webpageURL?.absoluteString
    } else {
      null
    }
}