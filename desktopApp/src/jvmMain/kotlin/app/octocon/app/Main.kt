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
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import app.octocon.app.utils.PublicKeyProvider
import java.awt.Dimension

fun main() {
  val lifecycle = LifecycleRegistry()

  val platformEventFlow = MutableSharedFlow<PlatformEvent>(replay = 3)
  val initialSettings = javaPreferences.get(SETTINGS_KEY, null)?.let { Settings.deserialize(it) }
    ?: Settings()

  // Restore persisted public key from Preferences if still within TTL
  try {
    val storedPem = javaPreferences.get("PUBLIC_KEY_PEM", null)
    val storedAt = javaPreferences.getLong("PUBLIC_KEY_AT", 0L)
    val now = System.currentTimeMillis()
    if (storedPem != null && (now - storedAt) < PublicKeyProvider.TTL_MS) {
      runBlocking { PublicKeyProvider.setCachedKey(storedPem, storedAt) }
    }

    GlobalScope.launch {
      try {
        val key = PublicKeyProvider.getPublicKey()
        javaPreferences.put("PUBLIC_KEY_PEM", key)
        javaPreferences.putLong("PUBLIC_KEY_AT", System.currentTimeMillis())
      } catch (_: Exception) {
        // ignore
      }
    }
  } catch (e: Exception) {
    println("Failed to restore persisted public key: $e")
  }

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