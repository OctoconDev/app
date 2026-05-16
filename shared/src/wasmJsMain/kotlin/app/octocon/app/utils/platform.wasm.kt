package app.octocon.app.utils

import app.octocon.app.Settings
import app.octocon.app.utils.bindings.CompactEncrypt
import app.octocon.app.utils.bindings.CryptoKey
import app.octocon.app.utils.bindings.crypto
import kotlinx.browser.localStorage
import kotlinx.browser.window
import kotlinx.coroutines.await
import org.khronos.webgl.Int8Array
import org.khronos.webgl.Uint8Array
import org.khronos.webgl.get
import org.khronos.webgl.set
import app.octocon.app.utils.PublicKeyProvider
import io.ktor.utils.io.core.toByteArray
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

actual val currentPlatform = DevicePlatform.Wasm

actual interface PlatformUtilities : CommonPlatformUtilities

actual interface PlatformDelegate

private fun rsaKeyParams(): JsAny = js("({ name: 'RSA-OAEP', hash: 'SHA-256' })")
private fun rsaJweHeader(): JsAny = js("({ alg: 'RSA-OAEP-256', enc: 'A256GCM', cty: 'text/plain' })")
private fun encryptUsage(): JsArray<JsString> = js("['encrypt']")

private fun encodeWithTextEncoder(string: String): Uint8Array = js("new TextEncoder().encode(string)")
private fun decodeWithTextDecoder(buffer: JsAny): String = js("new TextDecoder().decode(buffer)")

private fun randomizeArray(array: Uint8Array): Unit = js("crypto.getRandomValues(array)")

private fun aesGcmKeyParams(): JsAny = js("({ name: 'AES-GCM' })")
private fun aesGcmAlgoParams(iv: Uint8Array): JsAny = js("({ name: 'AES-GCM', iv: iv, tagLength: 128 })")
private fun aesEncryptUsages(): JsArray<JsString> = js("['encrypt']")
private fun aesDecryptUsages(): JsArray<JsString> = js("['decrypt']")
private fun jsUint8Array(buffer: JsAny): Uint8Array = js("new Uint8Array(buffer)")

private fun toUint8Array(bytes: ByteArray): Uint8Array {
  val ua = Uint8Array(bytes.size)
  for (i in bytes.indices) {
    ua[i] = bytes[i]
  }
  return ua
}

private fun Uint8Array.toByteArray(): ByteArray {
  return ByteArray(length) { this[it] }
}

private val alphabet = listOf(
  'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'J', 'K', 'L', 'M',
  'N', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
  '2', '3', '4', '5', '6', '7', '8', '9'
)

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
    try {
      val publicKey = PublicKeyProvider.getPublicKey()

      val strippedKey = publicKey
        .replace(Regex("-----BEGIN.*?-----"), "")
        .replace(Regex("-----END.*?-----"), "")
        .replace("\n", "")
        .replace("\r", "")
        .trim()

      val binaryKey = Base64.decode(strippedKey)

      val key = crypto.subtle.importKey(
        "spki",
        toUint8Array(binaryKey),
        rsaKeyParams(),
        false,
        encryptUsage()
      ).await<CryptoKey>()

      val jwe = CompactEncrypt(encodeWithTextEncoder(recoveryCode))
        .setProtectedHeader(rsaJweHeader())
        .encrypt(key)
        .await<JsString>()

      return jwe.toString()
    } catch (e: Exception) {
      platformLog("CRYPTO", "Failed to encrypt recovery code: $e")
      throw e
    }
  }

  override suspend fun generateRecoveryCode(): Pair<String, String> {
    val array = Uint8Array(16)
    randomizeArray(array)

    val recoveryCode = List(16) { alphabet[array[it].toInt() and (alphabet.size - 1)] }
      .joinToString("")
      .chunked(4)
      .joinToString("-")

    val jwe = recoveryCodeToJWE(recoveryCode)
    return recoveryCode to jwe
  }

  override fun setupEncryptionKey(encryptionKey: String): Settings? {
    // Base64 encode the key for storage in the encryptedEncryptionKey field
    val encryptedBase64 = Base64.encode(encryptionKey.encodeToByteArray())

    // Get current settings and update with encrypted key
    val currentSettingsJson = localStorage.getItem(SETTINGS_LOCALSTORAGE_KEY)
    var currentSettings = if (currentSettingsJson != null) {
      try {
        globalSerializer.decodeFromString<Settings>(currentSettingsJson)
      } catch (e: Exception) {
        Settings()
      }
    } else {
      Settings()
    }

    currentSettings = currentSettings.copy(encryptedEncryptionKey = encryptedBase64)
    saveSettings(currentSettings)
    return currentSettings
  }

  override fun getEncryptionKey(settings: Settings): String {
    // For WASM, get the encryption key from the Settings object
    val encryptedKey = settings.encryptedEncryptionKey 
      ?: throw IllegalStateException("Encryption key not set up. Call setupEncryptionKey first.")
    
    // Decrypt (decode) the Base64-encoded key
    return Base64.decode(encryptedKey).decodeToString()
  }

  override fun decryptEncryptionKey(encryptedEncryptionKey: String): String {
    return Base64.decode(encryptedEncryptionKey).decodeToString()
  }

  override suspend fun encryptData(data: String, settings: Settings): String {
    try {
      val keyString = getEncryptionKey(settings)
      val keyBytes = Base64.decode(keyString)
      
      val cryptoKey = crypto.subtle.importKey(
        "raw",
        toUint8Array(keyBytes),
        aesGcmKeyParams(),
        false,
        aesEncryptUsages()
      ).await<CryptoKey>()

      val iv = Uint8Array(12)
      randomizeArray(iv)

      val encryptedBuffer = crypto.subtle.encrypt(
        aesGcmAlgoParams(iv),
        cryptoKey,
        toUint8Array(data.encodeToByteArray())
      ).await<JsAny>()

      val encryptedBytes = jsUint8Array(encryptedBuffer).toByteArray()
      
      // Web Crypto appends the tag to the ciphertext
      val tagLength = 16
      val ciphertext = encryptedBytes.copyOfRange(0, encryptedBytes.size - tagLength)
      val tag = encryptedBytes.copyOfRange(encryptedBytes.size - tagLength, encryptedBytes.size)

      val ivBase64 = Base64.encode(iv.toByteArray())
      val ciphertextBase64 = Base64.encode(ciphertext)
      val tagBase64 = Base64.encode(tag)

      return "enc|$ivBase64|$ciphertextBase64|$tagBase64"
    } catch (e: Exception) {
      platformLog("CRYPTO", "Failed to encrypt data: $e")
      throw e
    }
  }

  override suspend fun decryptData(data: String, settings: Settings): String {
    val parts = data.split("|")
    require(data.startsWith("enc|") && parts.size == 4) { "Invalid encrypted data format" }
    
    try {
      val iv = toUint8Array(Base64.decode(parts[1]))
      val ciphertext = Base64.decode(parts[2])
      val tag = Base64.decode(parts[3])
      
      val keyString = getEncryptionKey(settings)
      val keyBytes = Base64.decode(keyString)
      
      val cryptoKey = crypto.subtle.importKey(
        "raw",
        toUint8Array(keyBytes),
        aesGcmKeyParams(),
        false,
        aesDecryptUsages()
      ).await<CryptoKey>()

      // Concatenate ciphertext and tag back for Web Crypto
      val encryptedBytes = ciphertext + tag
      
      val decryptedBuffer = crypto.subtle.decrypt(
        aesGcmAlgoParams(iv),
        cryptoKey,
        toUint8Array(encryptedBytes)
      ).await<JsAny>()

      return decodeWithTextDecoder(decryptedBuffer)
    } catch (e: Exception) {
      platformLog("CRYPTO", "Failed to decrypt data: $e")
      throw IllegalStateException("Failed to decrypt data", e)
    }
  }

  override fun getPublicKey(): String {
    return PublicKeyProvider.currentCachedKey()
      ?: throw IllegalStateException("Public key has not been loaded yet")
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

actual object BuildConfig : BuildConfigInterface {
  override fun isDebug(): Boolean = false 
}
