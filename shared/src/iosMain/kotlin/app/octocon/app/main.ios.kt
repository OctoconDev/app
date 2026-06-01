package app.octocon.app
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.uikit.OnFocusBehavior
import androidx.compose.ui.window.ComposeUIViewController
import app.octocon.app.ui.compose.screens.RootScreen
import app.octocon.app.ui.model.RootComponent
import app.octocon.app.utils.PlatformDelegate
import app.octocon.app.utils.PlatformEvent
import app.octocon.app.utils.platformLog
import app.octocon.app.utils.platformUtilities
import app.octocon.app.utils.sfSafariViewController
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.stack.animation.predictiveback.PredictiveBackGestureOverlay
import com.arkivanov.essenty.backhandler.BackDispatcher
import io.ktor.http.Url
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import platform.Foundation.NSLog
import platform.UIKit.UIViewController
import kotlin.time.Clock

val platformEventFlow = MutableSharedFlow<PlatformEvent>(replay = 3)

@Suppress("unused", "FunctionName")
@OptIn(
  DelicateCoroutinesApi::class,
  ExperimentalDecomposeApi::class
)
fun MainViewController(platformDelegate: PlatformDelegate, root: RootComponent, backDispatcher: BackDispatcher): UIViewController {
  GlobalScope.launch {
    iosDeepLinkFlow.collect { deepLink ->
      if(deepLink != null) {
        NSLog("Deep link received by MainViewController: $deepLink")
        handleDeepLink(deepLink)

        // Close SFSafariViewController if it's open
        sfSafariViewController?.let {
          MainScope().launch {
            it.dismissViewControllerAnimated(true) {
              sfSafariViewController = null
            }
          }
        }
      }
    }
  }

  val settings = getSettingsFromKeychain()

  // Restore persisted public key (if within TTL) and asynchronously refresh
  try {
    getStoredPublicKey()?.let { (pem, at) ->
      val now = Clock.System.now().toEpochMilliseconds()
      if (now - at < app.octocon.app.utils.PublicKeyProvider.TTL_MS) {
        runBlocking { app.octocon.app.utils.PublicKeyProvider.setCachedKey(pem, at) }
      }
    }

    GlobalScope.launch {
      try {
        val key = app.octocon.app.utils.PublicKeyProvider.getPublicKey("${settings.apiEndpoint}/api")
        saveStoredPublicKey(key, Clock.System.now().toEpochMilliseconds())
      } catch (e: Exception) {
        NSLog("Failed to preload public key: $e")
      }
    }
  } catch (e: Exception) {
    NSLog("Public key persistence restore failed: $e")
  }

  platformUtilities.injectPlatformDelegate(platformDelegate)

  if(settings.showPushNotifications) {
    platformUtilities.performAdditionalPushNotificationSetup()
  }

  return ComposeUIViewController(configure = {
    // this.parallelRendering = true
    this.onFocusBehavior = OnFocusBehavior.DoNothing
  }) {
    PredictiveBackGestureOverlay(
      backDispatcher = backDispatcher,
      backIcon = null,
      endEdgeEnabled = false,
      modifier = Modifier.fillMaxSize(),
    ) {
      RootScreen(component = root)
    }
  }
}


@OptIn(DelicateCoroutinesApi::class)
fun providePushNotificationToken(token: String?) {
  token?.let {
    GlobalScope.launch {
      withContext(Dispatchers.Main) {
        platformEventFlow.emit(PlatformEvent.PushNotificationTokenReceived(token))
      }
    }
  }
}

fun handleDeepLink(latestDeepLink: String?) {
  val url = latestDeepLink?.let { Url(it) } ?: return

  val path = if (url.protocol.name == "octocon") {
    if (url.host.isNotEmpty()) {
      "/" + url.host + url.encodedPath
    } else {
      url.encodedPath
    }
  } else {
    url.encodedPath
  }

  when (path) {
    "/auth/token", "/deep/auth/token" -> {
      platformLog("/deep/auth/token hit!")
      url.parameters["token"]?.let {
        platformLog("Token received: $it")
        platformEventFlow.tryEmit(PlatformEvent.LoginTokenReceived(it))
      }
    }

    "/link_success/discord", "/deep/link_success/discord" -> {
      platformLog("/deep/link_success/discord hit!")
      platformEventFlow.tryEmit(PlatformEvent.ExternallyHandleable.DiscordAccountLinked)
    }

    "/link_success/google", "/deep/link_success/google" -> {
      platformLog("/deep/link_success/google hit!")
      platformEventFlow.tryEmit(PlatformEvent.ExternallyHandleable.GoogleAccountLinked)
    }

    "/link_success/apple", "/deep/link_success/apple" -> {
      platformLog("/deep/link_success/apple hit!")
      platformEventFlow.tryEmit(PlatformEvent.ExternallyHandleable.AppleAccountLinked)
    }
  }
}