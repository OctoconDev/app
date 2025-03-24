package app.octocon.app.utils

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.ui.graphics.toArgb
import app.octocon.app.Settings
import com.mikepenz.aboutlibraries.Libs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import octoconapp.shared.generated.resources.Res
import org.jetbrains.compose.resources.ExperimentalResourceApi

class ColorSchemeParams(
  val toolbarColor: Int?,
  val navigationBarColor: Int?,
  val secondaryToolbarColor: Int?,
  val navigationBarDividerColor: Int?
)

expect val currentPlatform: DevicePlatform

enum class DevicePlatform(
  val displayName: String,
  val internalName: String,
  val usesNativeImageCropper: Boolean,
  val hasPushNotifications: Boolean,
  val isDarwin: Boolean,
  val isMobile: Boolean
) {
  Android(
    displayName = "Android",
    internalName = "android",
    usesNativeImageCropper = false,
    hasPushNotifications = true,
    isDarwin = false,
    isMobile = true
  ),

  @Suppress("EnumEntryName")
  iOS(
    displayName = "iOS",
    internalName = "ios",
    usesNativeImageCropper = true,
    hasPushNotifications = true,
    isDarwin = true,
    isMobile = true
  ),

  Wasm(
    displayName = "Web",
    internalName = "wasm",
    usesNativeImageCropper = false,
    hasPushNotifications = false,
    isDarwin = false,
    isMobile = false
  );

  val isAndroid: Boolean
    inline get() = this == Android
  val isiOS
    inline get() = this == iOS
  val isWasm
    inline get() = this == Wasm

  @Suppress("unused")
  companion object {
    val displayName: String
      inline get() = currentPlatform.displayName

    val internalName: String
      inline get() = currentPlatform.internalName

    val usesNativeImageCropper: Boolean
      inline get() = currentPlatform.usesNativeImageCropper

    val hasPushNotifications: Boolean
      inline get() = currentPlatform.hasPushNotifications

    val isDarwin: Boolean
      inline get() = currentPlatform.isDarwin

    val isMobile: Boolean
      inline get() = currentPlatform.isMobile

    val isAndroid: Boolean
      inline get() = currentPlatform.isAndroid

    val isiOS: Boolean
      inline get() = currentPlatform.isiOS

    val isWasm: Boolean
      inline get() = currentPlatform.isWasm
  }
}

enum class WebURLOpenBehavior {
  NewTab,
  SameTab,
  PopupWindow
}

enum class ExitApplicationType {
  QuickExit,
  ForcedRestart
}

interface CommonPlatformUtilities {
  fun exitApplication(exitApplicationType: ExitApplicationType)

  fun saveSettings(settings: Settings)

  fun showAlert(message: String)

  suspend fun recoveryCodeToJWE(recoveryCode: String): String
  suspend fun generateRecoveryCode(): Pair<String, String>
  fun setupEncryptionKey(encryptionKey: String): Settings?
  fun getEncryptionKey(settings: Settings): String

  fun decryptEncryptionKey(encryptedEncryptionKey: String): String

  fun encryptData(data: String, settings: Settings): String
  fun decryptData(data: String, settings: Settings): String

  fun getPublicKey(): String

  fun openURL(
    url: String,
    colorSchemeParams: ColorSchemeParams,
    webURLOpenBehavior: WebURLOpenBehavior = WebURLOpenBehavior.NewTab
  )

  fun updateWidgets(sessionInvalidated: Boolean = false)

  fun performAdditionalPushNotificationSetup()
}

expect interface PlatformUtilities : CommonPlatformUtilities

expect interface PlatformDelegate

val composeColorSchemeParams
  @Composable
  get() = ColorSchemeParams(
    toolbarColor = MaterialTheme.colorScheme.surfaceContainerHigh.toArgb(),
    navigationBarColor = MaterialTheme.colorScheme.surfaceContainerHigh.toArgb(),
    secondaryToolbarColor = MaterialTheme.colorScheme.surfaceContainerHigh.toArgb(),
    navigationBarDividerColor = MaterialTheme.colorScheme.outlineVariant.toArgb()
  )

sealed interface PlatformEvent {
  sealed class ExternallyHandleable : PlatformEvent {
    data object DiscordAccountLinked :
      ExternallyHandleable() {
      override fun handle(platformUtilities: PlatformUtilities) {
        platformUtilities.showAlert("Your Discord account was successfully linked!")
      }
    }

    data object GoogleAccountLinked :
      ExternallyHandleable() {
      override fun handle(platformUtilities: PlatformUtilities) {
        platformUtilities.showAlert("Your Google account was successfully linked!")
      }
    }

    abstract fun handle(platformUtilities: PlatformUtilities)
  }

  class PushNotificationTokenReceived(val token: String) : PlatformEvent

  class LoginTokenReceived(val token: String) : PlatformEvent
}

/**
 * Creates a State<Libs?> that holds the [Libs] as loaded by the [libraries].
 *
 * @see Libs
 */
@Composable
fun rememberLibraries(
  libraries: ByteArray,
): State<Libs?> = rememberLibraries {
  libraries.decodeToString()
}

/**
 * Creates a State<Libs?> that holds the [Libs] as loaded by the [block].
 *
 * @see Libs
 */
@OptIn(ExperimentalResourceApi::class)
@Composable
fun rememberLibraries(
  block: suspend () -> String = { Res.readBytes("files/aboutlibraries.json").decodeToString() },
): State<Libs?> {
  return produceState<Libs?>(initialValue = null) {
    value = withContext(Dispatchers.Default) {
      Libs.Builder()
        .withJson(block())
        .build()
    }
  }
}