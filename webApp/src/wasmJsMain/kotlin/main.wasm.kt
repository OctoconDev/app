import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import app.octocon.app.Settings
import app.octocon.app.ui.compose.screens.RootScreen
import app.octocon.app.ui.model.RootComponentImpl
import app.octocon.app.utils.PlatformEvent
import app.octocon.app.utils.SETTINGS_LOCALSTORAGE_KEY
import app.octocon.app.utils.platformUtilities
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import com.arkivanov.essenty.lifecycle.stop
import kotlinx.browser.document
import kotlinx.browser.localStorage
import kotlinx.browser.window
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import app.octocon.app.utils.PublicKeyProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import org.w3c.dom.Document
import org.w3c.dom.url.URLSearchParams
import kotlin.time.Clock

@OptIn(ExperimentalComposeUiApi::class, kotlinx.coroutines.DelicateCoroutinesApi::class)
fun main() {
  val lifecycle = LifecycleRegistry()
  val platformEventFlow = MutableSharedFlow<PlatformEvent>(replay = 3)

  val token = tryGetToken()

  consoleLog("Got token: $token")

  var initialSettings = localStorage.getItem(SETTINGS_LOCALSTORAGE_KEY)?.let {
    Settings.deserialize(it)
  } ?: Settings()

  // Restore persisted public key from localStorage if within TTL and asynchronously refresh
  try {
    val initialStoredPem = localStorage.getItem("PUBLIC_KEY_PEM")
    val initialStoredAt = localStorage.getItem("PUBLIC_KEY_AT")?.toLongOrNull() ?: 0L
    val now = Clock.System.now().toEpochMilliseconds()

    GlobalScope.launch {
      try {
        var pemToCache = initialStoredPem
        var atToCache = initialStoredAt

        if (pemToCache == null || (now - atToCache) >= PublicKeyProvider.TTL_MS) {
          try {
            val freshKey = PublicKeyProvider.getPublicKey("${initialSettings.apiEndpoint}/api")
            pemToCache = freshKey
            atToCache = now
            localStorage.setItem("PUBLIC_KEY_PEM", freshKey)
            localStorage.setItem("PUBLIC_KEY_AT", now.toString())
          } catch (e: Exception) {
            consoleLog("Failed to refresh public key: ${e.message ?: e.toString()}")
          }
        }

        pemToCache?.let {
          PublicKeyProvider.setCachedKey(it, atToCache)
        }
      } catch (e: Exception) {
        consoleLog("Error in startup public key coroutine: ${e.message ?: e.toString()}")
      }
    }
  } catch (e: Exception) {
    consoleLog("Failed to initiate public key restoration: $e")
  }

  if(token != null) {
    initialSettings = initialSettings.copy(token = token)
  }

  platformUtilities.initialize(initialSettings)

  val rootComponent = RootComponentImpl(
    componentContext = DefaultComponentContext(lifecycle = lifecycle),
    initialSettings = initialSettings,
    coroutineContext = Dispatchers.Main,
    platformUtilities = platformUtilities,
    platformEventFlow = platformEventFlow,
    deepLinkURL = null
  )

  lifecycle.attachToDocument()

  ComposeViewport("composeApp") {
    RootScreen(rootComponent)
  }
}

@OptIn(ExperimentalWasmJsInterop::class)
private fun consoleLog(text: String): Unit = js("console.log(text)")

@OptIn(ExperimentalWasmJsInterop::class)
private fun tryGetToken(): String? {
  consoleLog("Trying to get token")
  val params = URLSearchParams(window.location.search.toJsString())

  return params.get("token").also {
    consoleLog("Token is: $it")
    if(it != null) {
      window.history.replaceState(null, document.title, window.location.pathname)
      consoleLog("Token is not null; nuking history URL")
    }
  }
}

@OptIn(ExperimentalWasmJsInterop::class)
@Suppress("unused")
@JsFun("(document) => document.visibilityState")
private external fun visibilityState(document: Document): String

private fun LifecycleRegistry.attachToDocument() {
  fun onVisibilityChanged() {
    if (visibilityState(document) == "visible") {
      resume()
    } else {
      stop()
    }
  }

  onVisibilityChanged()
  document.addEventListener(type = "visibilitychange", callback = { onVisibilityChanged() })
}
