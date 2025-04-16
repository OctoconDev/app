package app.octocon.app

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import app.octocon.app.ui.compose.screens.RootScreen
import app.octocon.app.ui.model.RootComponentImpl
import app.octocon.app.utils.PlatformEvent
import app.octocon.app.utils.SETTINGS_KEY
import app.octocon.app.utils.javaPreferences
import app.octocon.app.utils.platformUtilities
import app.octocon.app.utils.runOnUiThread
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.decompose.extensions.compose.lifecycle.LifecycleController
import com.arkivanov.essenty.lifecycle.LifecycleRegistry

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import java.awt.Dimension

fun main() {
  val lifecycle = LifecycleRegistry()

  val platformEventFlow = MutableSharedFlow<PlatformEvent>(replay = 3)
  val initialSettings = javaPreferences.get(SETTINGS_KEY, null)?.let { Settings.deserialize(it) }
    ?: Settings()

  val rootComponent = runOnUiThread {
    RootComponentImpl(
      componentContext = DefaultComponentContext(lifecycle = lifecycle),
      initialSettings = initialSettings,
      coroutineContext = Dispatchers.Main,
      platformUtilities = platformUtilities,
      platformEventFlow = platformEventFlow,
      deepLinkURL = null
    )
  }

  application {
    val windowState = rememberWindowState(
      width = 1280.dp,
      height = 720.dp
    )

    LifecycleController(lifecycle, windowState)

    Window(
      onCloseRequest = ::exitApplication,
      title = "Octocon",
      state = windowState
    ) {
      window.minimumSize = Dimension(400, 600)
      RootScreen(rootComponent)
    }
  }
}