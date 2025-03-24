package app.octocon.app.utils

import app.octocon.app.Settings
import app.octocon.app.getSettingsFromKeychain
import app.octocon.app.saveSettingsToKeychain
import com.liftric.kvault.KVault
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.allocArrayOf
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.refTo
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.runBlocking
import octoconapp.shared.generated.resources.Res
import octoconapp.shared.generated.resources.public_key
import org.jetbrains.compose.resources.getString
import platform.Foundation.NSData
import platform.Foundation.NSURL
import platform.Foundation.create
import platform.SafariServices.SFSafariViewController
import platform.Security.SecRandomCopyBytes
import platform.Security.kSecRandomDefault
import platform.UIKit.UIAlertController
import platform.UIKit.UIAlertControllerStyleAlert
import platform.UIKit.UIApplication
import platform.UIKit.registerForRemoteNotifications
import platform.posix.arc4random_uniform
import platform.posix.exit
import platform.posix.memcpy
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

actual val currentPlatform = DevicePlatform.iOS

@Suppress("unused") // Used in Swift
fun getOctoconPublicKey(): String {
  return runBlocking(ioDispatcher) { getString(Res.string.public_key) }
}

actual interface PlatformUtilities : CommonPlatformUtilities {
  var injectedPlatformDelegate: PlatformDelegate?

  fun injectPlatformDelegate(platformDelegate: PlatformDelegate) {
    injectedPlatformDelegate = platformDelegate
  }
}

actual interface PlatformDelegate {
  fun recoveryCodeToJWE(recoveryCode: String): String

  fun encryptData(key: NSData, iv: NSData, plainText: String): NSData
  fun decryptData(key: NSData, iv: NSData, cipherText: NSData, tag: NSData): String?

  fun updateWidgets(sessionInvalidated: Boolean)
}



var sfSafariViewController: SFSafariViewController? = null

val encryptionVault = KVault("octocon_encryption_key", accessibility = KVault.Accessible.WhenUnlockedThisDeviceOnly)

@OptIn(ExperimentalEncodingApi::class, ExperimentalForeignApi::class)
val platformUtilities = object : PlatformUtilities {
  override var injectedPlatformDelegate: PlatformDelegate? = null

  private val alphabet = listOf(
    'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'J', 'K', 'L', 'M',
    'N', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
    '2', '3', '4', '5', '6', '7', '8', '9'
  )

  override fun exitApplication(exitApplicationType: ExitApplicationType) {
    exit(0)
  }

  override fun saveSettings(settings: Settings) {
    saveSettingsToKeychain(settings)
  }

  override fun showAlert(message: String) {
    val viewController =
      UIApplication.sharedApplication.keyWindow?.rootViewController?.modalViewController

    val alert = UIAlertController.alertControllerWithTitle(
      title = null,
      message = message,
      preferredStyle = UIAlertControllerStyleAlert
    )

    // alertController.addAction(
    //        UIAlertAction.actionWithTitle(
    //            "OK",
    //            style = UIAlertControllerStyle.MAX_VALUE,
    //            handler = null
    //        )
    //    )

    viewController?.presentViewController(alert, animated = true, completion = null)
  }

  override suspend fun recoveryCodeToJWE(recoveryCode: String): String {
    require(injectedPlatformDelegate != null) {
      "PlatformDelegate must be injected before calling recoveryCodeToJWE"
    }
    return injectedPlatformDelegate!!.recoveryCodeToJWE(recoveryCode)
  }

  override suspend fun generateRecoveryCode(): Pair<String, String> {
    val recoveryCode =
      List(16) { alphabet[arc4random_uniform(alphabet.size.toUInt()).toInt()] }
        .joinToString("")
        .chunked(4)
        .joinToString("-")

    val jwe = recoveryCodeToJWE(recoveryCode)
    return recoveryCode to jwe
    // return recoveryCode to ""
  }

  override fun setupEncryptionKey(encryptionKey: String): Settings? {
    encryptionVault.set("encryption_key", encryptionKey)

    val settings = getSettingsFromKeychain().copy(encryptedEncryptionKey = "STORED IN KEYCHAIN")
    saveSettings(settings)
    return settings
  }

  override fun getEncryptionKey(settings: Settings): String {
    return encryptionVault.string("encryption_key")!!
  }

  override fun decryptEncryptionKey(encryptedEncryptionKey: String): String {
    return Base64.Default.decode(encryptedEncryptionKey).decodeToString()
  }

  private fun generateIV(): ByteArray = usePinned {
    val iv = ByteArray(12)
    SecRandomCopyBytes(kSecRandomDefault, iv.size.toULong(), iv.refTo(0))

    return iv
  }

  override fun encryptData(data: String, settings: Settings): String {
    val iv = generateIV()
    val key = Base64.Default.decode(this.getEncryptionKey(settings))

    val cipherText = injectedPlatformDelegate!!.encryptData(key.toNSData(), iv.toNSData(), data).toByteArray()

    val tagLength = 16
    val tag = cipherText.copyOfRange(cipherText.size - tagLength, cipherText.size)
    val actualCipherText = cipherText.copyOfRange(0, cipherText.size - tagLength)

    val ivBase64 = Base64.Default.encode(iv)
    val cipherTextBase64 = Base64.Default.encode(actualCipherText)
    val tagBase64 = Base64.Default.encode(tag)

    val result = "enc|$ivBase64|$cipherTextBase64|$tagBase64"
    platformLog(result)
    return result
  }

  override fun decryptData(data: String, settings: Settings): String {
    require (injectedPlatformDelegate != null) {
      "PlatformDelegate must be injected before calling decryptData"
    }

    val parts = data.replace("\n", "").split("|")

    require(data.startsWith("enc|") && parts.size == 4) { "Invalid encrypted data format" }

    val iv = Base64.Default.decode(parts[1])
    val cipherText = Base64.Default.decode(parts[2])
    val tag = Base64.Default.decode(parts[3])

    val key = Base64.Default.decode(this.getEncryptionKey(settings))

    val result = injectedPlatformDelegate!!.decryptData(key.toNSData(), iv.toNSData(), cipherText.toNSData(), tag.toNSData())
    if(result == null) {
      throw IllegalStateException("Failed to decrypt data")
    } else {
      return result
    }
  }

  override fun getPublicKey(): String {
    TODO("Not yet implemented")
  }

  override fun openURL(
    url: String,
    colorSchemeParams: ColorSchemeParams,
    webURLOpenBehavior: WebURLOpenBehavior
  ) {
    val nsUrl = NSURL(string = url)

    sfSafariViewController = SFSafariViewController(nsUrl)/*.apply {
        colorSchemeParams.let { params ->
          params.navigationBarColor?.let { preferredBarTintColor = Color(it).toUIColor() }
          params.toolbarColor?.let { preferredControlTintColor = Color(it).toUIColor() }
        }
      }*/

    UIApplication.sharedApplication.keyWindow?.rootViewController?.presentViewController(
      sfSafariViewController!!,
      animated = true,
      completion = null
    )
  }

  override fun performAdditionalPushNotificationSetup() {
    UIApplication.sharedApplication.registerForRemoteNotifications()
  }

  override fun updateWidgets(sessionInvalidated: Boolean) {
    require (injectedPlatformDelegate != null) {
      "PlatformDelegate must be injected before calling updateWidgets"
    }
    injectedPlatformDelegate?.updateWidgets(sessionInvalidated)
  }
}

@OptIn(ExperimentalForeignApi::class)
fun NSData.toByteArray(): ByteArray = ByteArray(this@toByteArray.length.toInt()).apply {
  usePinned {
    memcpy(it.addressOf(0), this@toByteArray.bytes, this@toByteArray.length)
  }
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
fun ByteArray.toNSData(): NSData = memScoped {
  NSData.create(bytes = allocArrayOf(this@toNSData),
    length = this@toNSData.size.toULong())
}