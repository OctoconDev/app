package app.octocon

import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.core.view.WindowCompat
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import app.octocon.app.AndroidAppWrapper
import app.octocon.app.Settings
import app.octocon.app.ui.model.RootComponentImpl
import app.octocon.app.utils.ColorSchemeParams
import app.octocon.app.utils.ExitApplicationType
import app.octocon.app.utils.PlatformEvent
import app.octocon.app.utils.PlatformUtilities
import app.octocon.app.utils.WebURLOpenBehavior
import app.octocon.app.utils.platformLog
import app.octocon.glance.FrontWidget
import app.octocon.glance.FrontWidgetWorker
import app.octocon.util.createSharedPreferences
import app.octocon.util.getSavedSettings
import com.arkivanov.decompose.defaultComponentContext
import com.google.firebase.messaging.FirebaseMessaging
import com.nimbusds.jose.EncryptionMethod
import com.nimbusds.jose.JWEAlgorithm
import com.nimbusds.jose.JWEHeader
import com.nimbusds.jose.JWEObject
import com.nimbusds.jose.Payload
import com.nimbusds.jose.crypto.RSAEncrypter
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import java.security.GeneralSecurityException
import java.security.KeyFactory
import java.security.SecureRandom
import java.security.interfaces.RSAPublicKey
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

private const val SETTINGS_KEY = "SERIALIZED_SETTINGS"
private const val ENCRYPTED_PREFS_FILE = "encrypted_prefs.txt"

private val alphabet = listOf(
  'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'J', 'K', 'L', 'M',
  'N', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
  '2', '3', '4', '5', '6', '7', '8', '9'
)

class MainActivity : AppCompatActivity() {
  private lateinit var context: Context

  private val sharedPreferences by lazy { createSharedPreferences(context) }

  private fun saveSettings(settings: Settings) {
    val json = settings.serialize()
    sharedPreferences.edit { putString(SETTINGS_KEY, json) }
  }

  private val settings: Settings
    get() = getSavedSettings(sharedPreferences)

  private val platformEventFlow = MutableSharedFlow<PlatformEvent>(replay = 3)

  @OptIn(DelicateCoroutinesApi::class)
  private val platformUtilities = object : PlatformUtilities {
    override fun exitApplication(exitApplicationType: ExitApplicationType) {
      window.setFlags(
        WindowManager.LayoutParams.FLAG_SECURE,
        WindowManager.LayoutParams.FLAG_SECURE
      )
      finishAffinity()
    }

    override fun saveSettings(settings: Settings) {
      this@MainActivity.saveSettings(settings)
    }

    override fun showAlert(message: String) {
      Toast.makeText(this.context, message, Toast.LENGTH_SHORT).show()
    }

    override val context: Context
      get() = this@MainActivity

    override suspend fun recoveryCodeToJWE(recoveryCode: String): String {
      val publicKeyPEM =
        getString(R.string.public_key)
          .replace("-----BEGIN PUBLIC KEY-----", "")
          .replace("-----END PUBLIC KEY-----", "")
          .replace(System.lineSeparator(), "")

      val encodedKey = Base64.decode(publicKeyPEM, Base64.DEFAULT)
      val keyFactory = KeyFactory.getInstance("RSA")
      val keySpec = X509EncodedKeySpec(encodedKey)

      val publicKey = keyFactory.generatePublic(keySpec) as RSAPublicKey

      val header = JWEHeader.Builder(JWEAlgorithm.RSA_OAEP_256, EncryptionMethod.A256GCM)
        .contentType("text/plain")
        .build()

      val jweObject = JWEObject(header, Payload(recoveryCode))

      jweObject.encrypt(RSAEncrypter(publicKey))

      return jweObject.serialize()
    }

    override suspend fun generateRecoveryCode(): Pair<String, String> {
      val random = SecureRandom()
      val recoveryCode =
        List(16) { alphabet[random.nextInt(alphabet.size)] }
          .joinToString("")
          .chunked(4)
          .joinToString("-")

      return recoveryCode to recoveryCodeToJWE(recoveryCode)
    }

    override fun setupEncryptionKey(encryptionKey: String): Settings? {
      val keyPair = KeyStoreHelper.getKeyPair()
      val publicKey = keyPair.public
      val cipher =
        Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding", "AndroidKeyStoreBCWorkaround")
      cipher.init(Cipher.ENCRYPT_MODE, publicKey)

      val encryptedRecoveryCode = cipher.doFinal(encryptionKey.toByteArray())
      val newSettings = settings.copy(
        encryptedEncryptionKey = Base64.encodeToString(encryptedRecoveryCode, Base64.NO_WRAP)
      )
      saveSettings(newSettings)
      return newSettings
    }

    override fun getEncryptionKey(settings: Settings): String {
      val encryptedEncryptionKey = settings.encryptedEncryptionKey

      val keyPair = KeyStoreHelper.getKeyPair()
      val privateKey = keyPair.private

      val cipher =
        Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding", "AndroidKeyStoreBCWorkaround")
      cipher.init(Cipher.DECRYPT_MODE, privateKey)

      val decryptedEncryptionKey =
        cipher.doFinal(Base64.decode(encryptedEncryptionKey, Base64.DEFAULT))

      return String(decryptedEncryptionKey)
    }

    /*override fun decryptEncryptionKey(encryptedEncryptionKey: String): String {
      val keyPair = KeyStoreHelper.getKeyPair()
      val privateKey = keyPair.private

      val cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding")
      cipher.init(Cipher.DECRYPT_MODE, privateKey)

      val encryptedBytes = Base64.decode(encryptedEncryptionKey, Base64.DEFAULT)
      val decryptedBytes = cipher.doFinal(encryptedBytes)

      return String(decryptedBytes)
    }*/

    override fun decryptEncryptionKey(encryptedEncryptionKey: String): String {
      return String(Base64.decode(encryptedEncryptionKey, Base64.DEFAULT))
    }

    override fun encryptData(data: String, settings: Settings): String {
      val iv = ByteArray(12) // GCM standard nonce length (12 bytes)
      SecureRandom().nextBytes(iv)

      val key = Base64.decode(this.getEncryptionKey(settings), Base64.DEFAULT)

      val cipher = Cipher.getInstance("AES/GCM/NoPadding")
      val secretKey = SecretKeySpec(key, "AES")
      val gcmSpec = GCMParameterSpec(128, iv)

      cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec)

      val cipherText = cipher.doFinal(data.toByteArray())

      // Extract the authentication tag (last 16 bytes of the ciphertext in GCM mode)
      val tagLength = 16
      val tag = cipherText.copyOfRange(cipherText.size - tagLength, cipherText.size)
      val actualCiphertext = cipherText.copyOfRange(0, cipherText.size - tagLength)

      // Encode IV, ciphertext, and tag to Base64
      val ivBase64 = Base64.encodeToString(iv, Base64.DEFAULT)
      val ciphertextBase64 = Base64.encodeToString(actualCiphertext, Base64.DEFAULT)
      val tagBase64 = Base64.encodeToString(tag, Base64.DEFAULT)

      return "enc|$ivBase64|$ciphertextBase64|$tagBase64"
    }

    override fun decryptData(data: String, settings: Settings): String {
      val parts = data.split("|")

      require(data.startsWith("enc|") && parts.size == 4) { "Invalid encrypted data format" }

      val iv = Base64.decode(parts[1], Base64.DEFAULT)
      val cipherText = Base64.decode(parts[2], Base64.DEFAULT)
      val tag = Base64.decode(parts[3], Base64.DEFAULT)

      val key = Base64.decode(this.getEncryptionKey(settings), Base64.DEFAULT)

      val cipher = Cipher.getInstance("AES/GCM/NoPadding")
      val secretKey = SecretKeySpec(key, "AES")
      val gcmSpec = GCMParameterSpec(128, iv)

      cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec)

      try {
        val finalText = String(cipher.doFinal(cipherText + tag))
        return finalText
      } catch(e: GeneralSecurityException) {
        throw IllegalStateException("Failed to decrypt data", e)
      }
    }

    override fun getPublicKey(): String {
      /*val keyPair = KeyStoreHelper.getKeyPair()
      val publicKey = keyPair.public
      return "-----BEGIN PUBLIC KEY-----\n${
        Base64.encodeToString(
          publicKey.encoded,
          Base64.NO_WRAP
        )
      }\n-----END PUBLIC KEY-----"*/
      TODO("Not yet implemented")
    }

    override fun openURL(
      url: String,
      colorSchemeParams: ColorSchemeParams,
      webURLOpenBehavior: WebURLOpenBehavior
    ) {
      CustomTabsIntent
        .Builder()
        .setShowTitle(true)
        .setDownloadButtonEnabled(false)
        .setBookmarksButtonEnabled(false)
        .setDefaultColorSchemeParams(
          CustomTabColorSchemeParams
            .Builder()
            .apply {
              colorSchemeParams.toolbarColor?.let { setToolbarColor(it) }
              colorSchemeParams.navigationBarColor?.let { setNavigationBarColor(it) }
              colorSchemeParams.secondaryToolbarColor?.let { setSecondaryToolbarColor(it) }
              colorSchemeParams.navigationBarDividerColor?.let { setNavigationBarDividerColor(it) }
            }
            .build()
        )
        .setShareState(CustomTabsIntent.SHARE_STATE_OFF)
        .build()
        .launchUrl(this@MainActivity, url.toUri())
    }

    override fun performAdditionalPushNotificationSetup() {
      // No additional setup needed on Android
    }

    override fun updateWidgets(sessionInvalidated: Boolean) {
      GlobalScope.launch {
        val manager = GlanceAppWidgetManager(context)
        val widget = FrontWidget()
        val glanceIds = manager.getGlanceIds(widget.javaClass)
        glanceIds.forEach { glanceId ->
          updateAppWidgetState(context, glanceId) { prefs ->
            prefs.clear()
          }

          widget.update(context, glanceId)

          FrontWidgetWorker.enqueue(context, settings, glanceId, force = true)
        }
      }
    }
  }

  @OptIn(DelicateCoroutinesApi::class)
  override fun onCreate(savedInstanceState: Bundle?) {
    context = this@MainActivity

    enableEdgeToEdge()
    super.onCreate(savedInstanceState)

    WindowCompat.setDecorFitsSystemWindows(window, false)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      setRecentsScreenshotEnabled(false)
    }

    val initialSettings: Settings = handleIntent(settings)

    GlobalScope.launch {
      try {
        withTimeout(10_000L) {
          val token = FirebaseMessaging.getInstance().token.await()
          platformEventFlow.emit(PlatformEvent.PushNotificationTokenReceived(token))
        }
      } catch (e: Exception) {
        Log.e("OCTOCON", "Failed to get Firebase token: $e")
      }
    }

    val rootComponent = RootComponentImpl(
      componentContext = defaultComponentContext(),
      coroutineContext = Dispatchers.Main.immediate,
      initialSettings = initialSettings,
      platformUtilities = platformUtilities,
      platformEventFlow = platformEventFlow
    )

    setContent {
      AndroidAppWrapper(rootComponent = rootComponent)
    }
  }

  private fun handleIntent(settings: Settings): Settings {
    val appLinkData: Uri? = intent.data

    platformLog("OCTOCON", appLinkData?.path ?: "No path")

    return when (appLinkData?.path) {
      "/auh/token", "/deep/auth/token" -> {
        platformLog("/deep/auth/token hit!")
        val token = appLinkData.getQueryParameter("token")
        // val id = appLinkData.getQueryParameter("id")
        token?.let {
          val newSettings = settings.copy(token = it)
          saveSettings(newSettings)

          newSettings
        } ?: settings
      }

      "/link_success/discord", "/deep/link_success/discord" -> {
        platformLog("/deep/link_success/discord hit!")
        platformEventFlow.tryEmit(PlatformEvent.ExternallyHandleable.DiscordAccountLinked)
        settings
      }

      "/link_success/google", "/deep/link_success/google" -> {
        platformLog("/deep/link_success/google hit!")
        platformEventFlow.tryEmit(PlatformEvent.ExternallyHandleable.GoogleAccountLinked)
        settings
      }

      "/link_success/apple", "/deep/link_success/apple" -> {
        platformLog("/deep/link_success/google hit!")
        platformEventFlow.tryEmit(PlatformEvent.ExternallyHandleable.AppleAccountLinked)
        settings
      }

      else -> settings
    }

  }
}