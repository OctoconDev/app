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
    // TODO: Implement desktop alerts (could use JOptionPane or system notifications)
  }

  override suspend fun recoveryCodeToJWE(recoveryCode: String): String {
    val publicKeyRaw = PublicKeyProvider.getPublicKey()
    val publicKeyPEM = publicKeyRaw
      .replace(Regex("-----BEGIN.*?-----"), "")
      .replace(Regex("-----END.*?-----"), "")
      .replace(System.lineSeparator(), "")
      .replace("\n", "")
      .replace("\r", "")
      .trim()

    val encodedKey = Base64.decode(publicKeyPEM)
    val keyFactory = java.security.KeyFactory.getInstance("RSA")
    val keySpec = java.security.spec.X509EncodedKeySpec(encodedKey)

    val publicKey = keyFactory.generatePublic(keySpec) as java.security.interfaces.RSAPublicKey

    // Use Nimbus JOSE+JWT library for JWE (same as Android)
    val header = com.nimbusds.jose.JWEHeader.Builder(
      com.nimbusds.jose.JWEAlgorithm.RSA_OAEP_256,
      com.nimbusds.jose.EncryptionMethod.A256GCM
    )
      .contentType("text/plain")
      .build()

    val jweObject = com.nimbusds.jose.JWEObject(header, com.nimbusds.jose.Payload(recoveryCode))
    jweObject.encrypt(com.nimbusds.jose.crypto.RSAEncrypter(publicKey))

    return jweObject.serialize()
  }

  override suspend fun generateRecoveryCode(): Pair<String, String> {
    val alphabet = listOf(
      'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'J', 'K', 'L', 'M',
      'N', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
      '2', '3', '4', '5', '6', '7', '8', '9'
    )

    val random = SecureRandom()
    val recoveryCode = List(16) { alphabet[random.nextInt(alphabet.size)] }
      .joinToString("")
      .chunked(4)
      .joinToString("-")

    return recoveryCode to recoveryCodeToJWE(recoveryCode)
  }

  override suspend fun setupEncryptionKey(encryptionKey: String): Settings? {
    try {
      // Store the encryption key in preferences (Base64 encoded for safety)
      // Note: On desktop, this is stored in the user's preference store
      // For higher security, consider using OS-specific secure storage:
      // - Windows: DPAPI
      // - macOS: Keychain
      // - Linux: Secret Service API / libsecret
      val encodedKey = Base64.encode(encryptionKey.toByteArray())
      javaPreferences.put("ENCRYPTION_KEY", encodedKey)

      val currentSettings = javaPreferences.get(SETTINGS_KEY, null)?.let { 
        Settings.deserialize(it) 
      } ?: Settings()

      val newSettings = currentSettings.copy(
        encryptedEncryptionKey = encodedKey
      )
      saveSettings(newSettings)
      return newSettings
    } catch (e: Exception) {
      println("Failed to setup encryption key: ${e.message}")
      return null
    }
  }

  override suspend fun getEncryptionKey(settings: Settings): String {
    val encodedKey = settings.encryptedEncryptionKey
      ?: javaPreferences.get("ENCRYPTION_KEY", null)
      ?: throw IllegalStateException("Encryption key not found")

    return String(Base64.decode(encodedKey))
  }

  override fun decryptEncryptionKey(encryptedEncryptionKey: String): String {
    // On desktop, the server sends us the key directly (base64 encoded)
    // We just decode it since we don't have platform-specific encryption like Android KeyStore
    return String(Base64.decode(encryptedEncryptionKey))
  }

  override suspend fun encryptData(
    data: String,
    settings: Settings
  ): String {
    val iv = ByteArray(12) // GCM standard nonce length
    SecureRandom().nextBytes(iv)

    val key = Base64.decode(getEncryptionKey(settings))

    val cipher = Cipher.getInstance("AES/GCM/NoPadding")
    val secretKey = SecretKeySpec(key, "AES")
    val gcmSpec = GCMParameterSpec(128, iv)

    cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec)

    val cipherText = cipher.doFinal(data.toByteArray())

    val tagLength = 16
    val tag = cipherText.copyOfRange(cipherText.size - tagLength, cipherText.size)
    val actualCiphertext = cipherText.copyOfRange(0, cipherText.size - tagLength)

    val ivBase64 = Base64.encode(iv)
    val ciphertextBase64 = Base64.encode(actualCiphertext)
    val tagBase64 = Base64.encode(tag)

    return "enc|$ivBase64|$ciphertextBase64|$tagBase64"
  }

  override suspend fun decryptData(
    data: String,
    settings: Settings
  ): String {
    val parts = data.split("|")

    require(data.startsWith("enc|") && parts.size == 4) { "Invalid encrypted data format" }

    val iv = Base64.decode(parts[1])
    val cipherText = Base64.decode(parts[2])
    val tag = Base64.decode(parts[3])

    val key = Base64.decode(getEncryptionKey(settings))

    val cipher = Cipher.getInstance("AES/GCM/NoPadding")
    val secretKey = SecretKeySpec(key, "AES")
    val gcmSpec = GCMParameterSpec(128, iv)

    cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec)

    return try {
      String(cipher.doFinal(cipherText + tag))
    } catch (e: java.security.GeneralSecurityException) {
      throw IllegalStateException("Failed to decrypt data", e)
    }
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
  override fun isDebug(): Boolean {
    // Check environment variable first for explicit override
    val envDebug = System.getenv("OCTOCON_DEBUG")
    if (envDebug != null) {
      return envDebug.toBoolean()
    }

    //TODO: Assume true for now
    return true;
  }
}