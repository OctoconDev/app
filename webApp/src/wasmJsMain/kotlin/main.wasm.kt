
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import org.w3c.dom.Document
import org.w3c.dom.url.URLSearchParams

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
  val lifecycle = LifecycleRegistry()
  val platformEventFlow = MutableSharedFlow<PlatformEvent>(replay = 3)

  val token = tryGetToken()

  var initialSettings = localStorage.getItem(SETTINGS_LOCALSTORAGE_KEY)?.let {
    Settings.deserialize(it)
  } ?: Settings()

  if(token != null) {
    initialSettings = initialSettings.copy(token = token)
  }

  val rootComponent = RootComponentImpl(
    componentContext = DefaultComponentContext(lifecycle = lifecycle),
    initialSettings = initialSettings,
    coroutineContext = Dispatchers.Main,
    platformUtilities = platformUtilities,
    platformEventFlow = platformEventFlow,
    deepLinkURL = null
  )

  lifecycle.attachToDocument()

  CanvasBasedWindow(title = "Octocon") {
    RootScreen(rootComponent)
  }
}

private fun tryGetToken(): String? {
  val params = URLSearchParams(window.location.search.toJsString())

  return params.get("token").also {
    if(it != null) {
      window.history.replaceState(null, document.title, window.location.pathname)
    }
  }
}

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