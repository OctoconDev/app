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

fun main(args: Array<String>) {
  // Register URL scheme handler
  // In development mode, this creates a temporary launcher script
  // To disable URL registration in dev mode, set OCTOCON_SKIP_URL_REGISTRATION=true
  val skipUrlRegistration = System.getenv("OCTOCON_SKIP_URL_REGISTRATION")?.toBoolean() ?: false

  if (!skipUrlRegistration) {
    try {
      if (UrlSchemeHandler.isDevMode()) {
        println("Running in development mode. URL scheme will use Gradle launcher.")
        println("To skip URL registration, set OCTOCON_SKIP_URL_REGISTRATION=true")
      }
      UrlSchemeHandler.registerUrlScheme()
    } catch (e: Exception) {
      println("Failed to register URL scheme: ${e.message}")
    }
  } else {
    println("URL scheme registration skipped (OCTOCON_SKIP_URL_REGISTRATION=true)")
    println("Deep links will only work if app is launched with URL arguments manually")
  }

  val lifecycle = LifecycleRegistry()

  val platformEventFlow = MutableSharedFlow<PlatformEvent>(replay = 3)
  var initialSettings = javaPreferences.get(SETTINGS_KEY, null)?.let { Settings.deserialize(it) }
    ?: Settings()

  // Check if app was launched with a deep link URL
  var deepLinkURL: String? = null
  if (args.isNotEmpty()) {
    val possibleUrl = args[0]
    if (possibleUrl.startsWith("octocon://")) {
      deepLinkURL = possibleUrl
      println("App launched with deep link: $possibleUrl")
    }
  }

  // Set up single instance manager to handle deep links in running instance
  val singleInstanceManager = SingleInstanceManager { receivedUrl ->
    println("Received deep link in running instance: $receivedUrl")
    handleDeepLink(receivedUrl, platformEventFlow)
  }

  // Try to acquire single instance lock
  // If another instance is running, send the deep link and exit
  if (!singleInstanceManager.tryAcquireLock(deepLinkURL)) {
    println("Another instance is already running. Deep link sent to existing instance.")
    return
  }

  // This is the main instance, process initial deep link if present
  if (deepLinkURL != null) {
    val deepLinkData = UrlSchemeHandler.parseDeepLink(deepLinkURL)
    deepLinkData?.let { data ->
      when {
        data.path.contains("auth/token") || data.path.contains("deep/auth/token") -> {
          val token = data.queryParams["token"]
          if (token != null) {
            println("Received auth token from initial deep link")
            initialSettings = initialSettings.copy(token = token)
            platformUtilities.saveSettings(initialSettings)

            // Emit the login token event
            GlobalScope.launch {
              platformEventFlow.emit(PlatformEvent.LoginTokenReceived(token))
            }
          }
        }
        data.path.contains("link_success/discord") -> {
          GlobalScope.launch {
            platformEventFlow.emit(PlatformEvent.ExternallyHandleable.DiscordAccountLinked)
          }
        }
        data.path.contains("link_success/google") -> {
          GlobalScope.launch {
            platformEventFlow.emit(PlatformEvent.ExternallyHandleable.GoogleAccountLinked)
          }
        }
        data.path.contains("link_success/apple") -> {
          GlobalScope.launch {
            platformEventFlow.emit(PlatformEvent.ExternallyHandleable.AppleAccountLinked)
          }
        }
      }
    }
  }

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
        val key = PublicKeyProvider.getPublicKey("${initialSettings.apiEndpoint}/api")
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
      deepLinkURL = deepLinkURL
    )
  }

  application {
    val windowState = rememberWindowState(
      width = 1280.dp,
      height = 720.dp
    )

    LifecycleController(lifecycle, windowState)

    Window(
      onCloseRequest = {
        singleInstanceManager.release()
        exitApplication()
      },
      title = "Octocon",
      state = windowState
    ) {
      window.minimumSize = Dimension(400, 600)
      RootScreen(rootComponent)
    }
  }
}

/**
 * Handles deep link URLs received from either initial launch or IPC
 */
private fun handleDeepLink(url: String, platformEventFlow: MutableSharedFlow<PlatformEvent>) {
  val deepLinkData = UrlSchemeHandler.parseDeepLink(url) ?: return

  when {
    deepLinkData.path.contains("auth/token") || deepLinkData.path.contains("deep/auth/token") -> {
      val token = deepLinkData.queryParams["token"]
      if (token != null) {
        println("Received auth token from deep link")
        var currentSettings = javaPreferences.get(SETTINGS_KEY, null)?.let { Settings.deserialize(it) }
          ?: Settings()
        currentSettings = currentSettings.copy(token = token)
        platformUtilities.saveSettings(currentSettings)

        // Emit the login token event
        GlobalScope.launch {
          platformEventFlow.emit(PlatformEvent.LoginTokenReceived(token))
        }
      }
    }
    deepLinkData.path.contains("link_success/discord") -> {
      GlobalScope.launch {
        platformEventFlow.emit(PlatformEvent.ExternallyHandleable.DiscordAccountLinked)
      }
    }
    deepLinkData.path.contains("link_success/google") -> {
      GlobalScope.launch {
        platformEventFlow.emit(PlatformEvent.ExternallyHandleable.GoogleAccountLinked)
      }
    }
    deepLinkData.path.contains("link_success/apple") -> {
      GlobalScope.launch {
        platformEventFlow.emit(PlatformEvent.ExternallyHandleable.AppleAccountLinked)
      }
    }
  }
}