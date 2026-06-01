package app.octocon

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import app.octocon.app.utils.platformLog
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore

object KeyStoreHelper {
  private const val KEY_ALIAS = "OctoconKey"
  private const val ANDROID_KEYSTORE = "AndroidKeyStore"

  private fun generateKeyPair(): KeyPair {
    val keyPairGenerator = KeyPairGenerator.getInstance(
      KeyProperties.KEY_ALGORITHM_RSA, ANDROID_KEYSTORE
    )

    try {
      val keyGenParameterSpec = KeyGenParameterSpec.Builder(
        KEY_ALIAS,
        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
      )
        .setKeySize(2048)
        .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_OAEP)
        .setIsStrongBoxBacked(true)
        .build()

      keyPairGenerator.initialize(keyGenParameterSpec)
      val keyPair = keyPairGenerator.generateKeyPair()
      platformLog("OCTOCON-KEYSTORE", "Generated StrongBox-backed RSA2048 key")
      return keyPair
    } catch (e: Exception) {
      Log.w(
        "OCTOCON-KEYSTORE",
        "StrongBox is unavailable for RSA2048, falling back to software-backed key"
      )
      val keyGenParameterSpec = KeyGenParameterSpec.Builder(
        KEY_ALIAS,
        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
      )
        .setKeySize(2048)
        .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_OAEP)
        .build()

      keyPairGenerator.initialize(keyGenParameterSpec)
      return keyPairGenerator.generateKeyPair()
    }

  }

  fun getKeyPair(): KeyPair {
    val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
    keyStore.load(null)

    val privateKey = keyStore.getKey(KEY_ALIAS, null) as? java.security.PrivateKey
    val publicKey = keyStore.getCertificate(KEY_ALIAS)?.publicKey

    return if (privateKey != null && publicKey != null) {
      KeyPair(publicKey, privateKey)
    } else {
      generateKeyPair()
    }
  }
}