@file:OptIn(ExperimentalWasmJsInterop::class, ExperimentalEncodingApi::class)

package app.octocon.app.utils

import app.octocon.app.Settings
import app.octocon.app.utils.bindings.CompactEncrypt
import app.octocon.app.utils.bindings.CryptoKey
import app.octocon.app.utils.bindings.crypto
import kotlinx.browser.localStorage
import kotlinx.browser.window
import kotlinx.coroutines.await
import org.khronos.webgl.Uint8Array
import org.khronos.webgl.get
import org.khronos.webgl.set
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.js.ExperimentalWasmJsInterop

actual val currentPlatform = DevicePlatform.Wasm

actual interface PlatformUtilities : CommonPlatformUtilities

actual interface PlatformDelegate

@JsFun("() => ({ name: 'RSA-OAEP', hash: 'SHA-256' })")
private external fun rsaKeyParams(): JsAny

@JsFun("() => ({ alg: 'RSA-OAEP-256', enc: 'A256GCM', cty: 'text/plain' })")
private external fun rsaJweHeader(): JsAny

@JsFun("() => ['encrypt']")
private external fun encryptUsage(): JsArray<JsString>

@JsFun("(string) => new TextEncoder().encode(string)")
private external fun encodeWithTextEncoder(string: String): Uint8Array

@JsFun("(buffer) => new TextDecoder().decode(buffer)")
private external fun decodeWithTextDecoder(buffer: JsAny): String

@JsFun("(array) => crypto.getRandomValues(array)")
private external fun randomizeArray(array: Uint8Array)

@JsFun("() => ({ name: 'AES-GCM' })")
private external fun aesGcmKeyParams(): JsAny

@JsFun("(iv) => ({ name: 'AES-GCM', iv: iv, tagLength: 128 })")
private external fun aesGcmAlgoParams(iv: Uint8Array): JsAny

@JsFun("() => ['encrypt']")
private external fun aesEncryptUsages(): JsArray<JsString>

@JsFun("() => ['decrypt']")
private external fun aesDecryptUsages(): JsArray<JsString>

@JsFun("(buffer) => new Uint8Array(buffer)")
private external fun jsUint8Array(buffer: JsAny): Uint8Array

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
            val publicKey = getPublicKey()
            val strippedKey = publicKey
                .replace(Regex("-----BEGIN.*?-----"), "")
                .replace(Regex("-----END.*?-----"), "")
                .replace("\n", "")
                .replace("\r", "")
                .trim()

            val binaryKey = Base64.Mime.decode(strippedKey)
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

            platformLog("CRYPTO", "Encrypted recovery code")
            return jwe.toString()
        } catch (e: Exception) {
            platformLog("CRYPTO", "Failed to encrypt recovery code: $e")
            throw e
        }
    }

    override suspend fun generateRecoveryCode(): Pair<String, String> {
        platformLog("CRYPTO", "Creating recovery code")
        val array = Uint8Array(16)
        randomizeArray(array)

        val recoveryCode = List(16) { alphabet[array[it].toInt() and 31] }
            .joinToString("")
            .chunked(4)
            .joinToString("-")

        val jwe = recoveryCodeToJWE(recoveryCode)
        return recoveryCode to jwe
    }

    private fun getSettings(): Settings? {

        val currentSettingsJson = localStorage.getItem(SETTINGS_LOCALSTORAGE_KEY)
        return if (currentSettingsJson != null) {
            try {
                Settings.deserialize(currentSettingsJson)
            } catch (e: Exception) {
                platformLog("SETTINGS", "Failed to deserialize settings in setupEncryptionKey: $e")
                return null
            }
        } else {
            Settings()
        }
    }

    override suspend fun setupEncryptionKey(encryptionKey: String): Settings? {
        val currentSettings = getSettings() ?: return null

        // Persist the encryption key in IndexedDB via the web shim and store
        // a placeholder in settings so callers know it's persisted off-localStorage.
        try {
            webStoreEncryptionKey(encryptionKey)
        } catch (e: Exception) {
            platformLog("SETTINGS", "Failed to persist encryption key to IndexedDB: $e")
        }

        val updatedSettings = currentSettings.copy(encryptedEncryptionKey = "STORED_IN_INDEXEDDB")
        saveSettings(updatedSettings)
        return updatedSettings
    }

    override suspend fun getEncryptionKey(settings: Settings): String {
        val keyFromSettings = settings.encryptedEncryptionKey
        if (keyFromSettings != null && keyFromSettings != "STORED_IN_INDEXEDDB") {
            return keyFromSettings.trim()
        }

        val stored = webRetrieveEncryptionKey()
        return stored?.trim() ?: throw IllegalStateException("Encryption key not found")
    }

    override fun decryptEncryptionKey(encryptedEncryptionKey: String): String {
        return try {
            // Decode the outer Base64 wrapper to get the inner Base64 key string,
            // matching other platform implementations (iOS/Desktop/Android).
            // Use Mime decoder to tolerate line-wraps that Android's Base64.DEFAULT produces.
            Base64.Mime.decode(encryptedEncryptionKey).decodeToString().trim()
        } catch (e: Exception) {
            platformLog("CRYPTO", "Failed to decrypt encryption key: ${e.message}")
            throw e
        }
    }

    override suspend fun encryptData(data: String, settings: Settings): String {
        try {
            val keyString = getEncryptionKey(settings)
            val keyBytes = Base64.Mime.decode(keyString.trim())

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

            val result = "enc|$ivBase64|$ciphertextBase64|$tagBase64"
            return result
        } catch (e: Exception) {
            platformLog("CRYPTO", "Failed to encrypt data: ${e.message}")
            platformLog("CRYPTO", "Exception type: ${e::class.simpleName}")
            platformLog("CRYPTO", "Stack trace: ${e.stackTraceToString()}")
            throw e
        }
    }

    override suspend fun decryptData(data: String, settings: Settings): String {
        val parts = data.split("|")
        require(data.startsWith("enc|") && parts.size == 4) { "Invalid encrypted data format" }

        try {
            val iv = toUint8Array(Base64.Mime.decode(parts[1].trim()))
            val ciphertext = Base64.Mime.decode(parts[2].trim())
            val tag = Base64.Mime.decode(parts[3].trim())

            val keyString = getEncryptionKey(settings)
            val keyBytes = Base64.Mime.decode(keyString.trim())

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
    override fun isDebug(): Boolean {
        // Check for explicit debug flag in localStorage
        val debugFlag = localStorage.getItem("OCTOCON_DEBUG")
        if (debugFlag != null) {
            return debugFlag.toBoolean()
        }

        // Auto-detect: running on localhost or with query parameter means debug mode
        val hostname = window.location.hostname
        val isLocalhost = hostname == "localhost" || hostname == "127.0.0.1" || hostname.startsWith("192.168.")
        val hasDebugParam = window.location.search.contains("debug=true")

        val isDebug = isLocalhost || hasDebugParam
        if (isDebug) {
            platformLog("BUILD-CONFIG", "Running in debug mode (hostname=$hostname, hasDebugParam=$hasDebugParam)")
        }

        return isDebug
    }
}
