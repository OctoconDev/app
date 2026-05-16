package app.octocon.app.utils

import app.octocon.app.Settings
import app.octocon.app.utils.PublicKeyProvider
import org.jetbrains.skiko.OS
import org.jetbrains.skiko.hostOs
import java.awt.Desktop
import java.net.URI
import java.net.URL
import java.security.SecureRandom
import java.util.prefs.Preferences
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.system.exitProcess

actual val currentPlatform = DevicePlatform.Desktop

actual interface PlatformUtilities : CommonPlatformUtilities
actual interface PlatformDelegate

const val SETTINGS_KEY = "octocon_settings"

val javaPreferences: Preferences by lazy { Preferences.userRoot().node(SETTINGS_KEY) }

@OptIn(ExperimentalEncodingApi::class)
val platformUtilities = object : PlatformUtilities {
  override fun exitApplication(exitApplicationType: ExitApplicationType) = exitProcess(0)

  override fun saveSettings(settings: Settings) {
    javaPreferences.put(SETTINGS_KEY, globalSerializer.encodeToString(settings))
  }

  @Suppress("EmptyMethod")
  override fun showAlert(message: String) {
    // TODO: Alerts?
  }

  override suspend fun recoveryCodeToJWE(recoveryCode: String): String {
    TODO("Not yet implemented")
  }

  override suspend fun generateRecoveryCode(): Pair<String, String> {
    TODO("Not yet implemented")
  }

  override fun setupEncryptionKey(encryptionKey: String): Settings? {
    TODO("Not yet implemented")
  }

  override fun getEncryptionKey(settings: Settings): String {
    TODO("Not yet implemented")
  }

  override fun decryptEncryptionKey(encryptedEncryptionKey: String): String {
    TODO("Not yet implemented")
  }

  override suspend fun encryptData(
    data: String,
    settings: Settings
  ): String {
    TODO("Not yet implemented")
  }

  override suspend fun decryptData(
    data: String,
    settings: Settings
  ): String {
    TODO("Not yet implemented")
  }

  override fun getPublicKey(): String {
    return PublicKeyProvider.currentCachedKey() ?: throw IllegalStateException("Public key has not been loaded yet")
  }

  override fun openURL(
    url: String,
    colorSchemeParams: ColorSchemeParams,
    webURLOpenBehavior: WebURLOpenBehavior
  ) {
    val desktop = Desktop.getDesktop()
    if (desktop.isSupported(Desktop.Action.BROWSE)) {
      desktop.browse(URI(url))
    } else when (hostOs) {
      OS.Linux -> {
        URI(url) // Validate URI for exception behavior consistent with the Desktop.browse() case (throwing URISyntaxException)
        Runtime.getRuntime().exec(arrayOf("xdg-open", URL(url).toString()))
      }
      OS.Android, OS.Windows, OS.MacOS, OS.Ios, OS.Tvos, OS.JS, OS.Unknown -> {
        throw UnsupportedOperationException("AWT does not support the BROWSE action on this platform")
      }
    }
  }

  // Stubs: not implemented on desktop
  override fun performAdditionalPushNotificationSetup() = Unit
  override fun updateWidgets(sessionInvalidated: Boolean) = Unit
}

actual object BuildConfig : BuildConfigInterface {
  override fun isDebug(): Boolean = false // TODO: Implement a way to determine this; env?
}