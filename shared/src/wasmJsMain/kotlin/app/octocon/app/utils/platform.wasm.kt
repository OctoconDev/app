package app.octocon.app.utils

import app.octocon.app.Settings
import app.octocon.app.utils.bindings.CompactEncrypt
import app.octocon.app.utils.bindings.CryptoKey
import app.octocon.app.utils.bindings.crypto
import io.ktor.util.toJsArray
import kotlinx.browser.localStorage
import kotlinx.browser.window
import kotlinx.coroutines.await
import org.khronos.webgl.Int8Array
import org.khronos.webgl.Uint8Array
import org.khronos.webgl.get
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

actual val currentPlatform = DevicePlatform.Wasm

actual interface PlatformUtilities : CommonPlatformUtilities

actual interface PlatformDelegate

private fun keyParams(): JsString = js("({ name: 'RSA-OAEP', hash: 'SHA-256' })")
private fun jweHeader(): JsString = js("({ alg: 'RSA-OAEP-256', enc: 'A256GCM' })")
private fun encryptArray(): JsArray<JsString> = js("['encrypt']")

private val alphabet = listOf(
  'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'J', 'K', 'L', 'M',
  'N', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
  '2', '3', '4', '5', '6', '7', '8', '9'
)

private fun encodeWithTextEncoder(string: JsString): Uint8Array = js("""
new TextEncoder().encode(string)
""")

private fun randomizeArray(array: Int8Array): Unit = js("""
window.crypto.getRandomValues(array)
""")

@OptIn(ExperimentalEncodingApi::class)
val platformUtilities = object : PlatformUtilities {
  override fun exitApplication(exitApplicationType: ExitApplicationType) {
    when (exitApplicationType) {
      // TODO: Make quick exit URL configurable on web?
      ExitApplicationType.QuickExit -> window.location.assign("https://google.com")
      ExitApplicationType.ForcedRestart -> window.location.reload()
    }
  }

  override fun saveSettings(settings: Settings) {
    localStorage.setItem(SETTINGS_LOCALSTORAGE_KEY, settings.serialize())
  }

  override fun showAlert(message: String) {
    // TODO: Implement a better alert system?
    window.alert(message)
  }

  override suspend fun recoveryCodeToJWE(recoveryCode: String): String {
    val publicKey = "-----BEGIN PUBLIC KEY-----MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA4m1WqJfFxVlk4RQcPoI9lICmt3f0EGK3F6rMW1LdZOQ0aMj9w4dKAPqa+0gJ0j1XynsIi8qOt35mWMGNBo0LVx7+ZrJgSGw3/2ZfkycdHCk4FA9v7quW0lYYiIIIOaM7n2wHOgRi+ifhKyYZu3MQ6B5Krq16TBT8m2kjFMI2u+c3GeVsScMwaEYFnpdC7hmxnHjk3Tl2qdRow9xhILI7b5QcV8E6ZkGUxkIRBjQ79EdBiyuFcTVWl1tNEJpFNqhY1dbyJHstdx1QbHw/ICgFs7RpWrfmqb8RMhVl97du6bAgF3vcWUpLTx9o2rvofBfNIxf/UZfzjvMWJzfKS1mMmwIDAQAB-----END PUBLIC KEY-----"

    val strippedKey = publicKey
      .replace("-----BEGIN PUBLIC KEY-----", "")
      .replace("-----END PUBLIC KEY-----", "")
      .replace("\n", "")

    val binaryKey = Base64.decode(strippedKey).toJsArray()

    val key = crypto.subtle.importKey(
      "spki",
      binaryKey,
      keyParams(),
      false,
      encryptArray()
    ).await<CryptoKey>()

    val jwe = CompactEncrypt(encodeWithTextEncoder(recoveryCode.toJsString()))
      .setProtectedHeader(jweHeader())
      .encrypt(key)
      .await<JsString>()

    return jwe.toString()
  }

  override suspend fun generateRecoveryCode(): Pair<String, String> {
    val array = Int8Array(16)
    randomizeArray(array)

    val recoveryCode = List(16) { alphabet[array[it].toInt() and (alphabet.size - 1)] }
      .joinToString("")
      .chunked(4)
      .joinToString("-")

    val jwe = recoveryCodeToJWE(recoveryCode)
    return recoveryCode to jwe
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

  override fun encryptData(
    data: String,
    settings: Settings
  ): String {
    TODO("Not yet implemented")
  }

  override fun decryptData(
    data: String,
    settings: Settings
  ): String {
    TODO("Not yet implemented")
  }

  override fun getPublicKey(): String {
    TODO("Not yet implemented")
  }

  override fun openURL(
    url: String,
    colorSchemeParams: ColorSchemeParams,
    webURLOpenBehavior: WebURLOpenBehavior
  ) {
    when (webURLOpenBehavior) {
      WebURLOpenBehavior.NewTab -> window.open(url, "_blank")
      WebURLOpenBehavior.SameTab -> window.location.assign(url)
      WebURLOpenBehavior.PopupWindow -> window.open(url, "_blank", "popup=true")
    }
  }

  // Stubs: not implemented on web
  override fun performAdditionalPushNotificationSetup() = Unit
  override fun updateWidgets(sessionInvalidated: Boolean) = Unit
}

const val SETTINGS_LOCALSTORAGE_KEY = "octocon_settings"