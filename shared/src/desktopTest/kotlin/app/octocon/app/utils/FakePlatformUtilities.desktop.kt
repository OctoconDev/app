package app.octocon.app.utils

import app.octocon.app.Settings

actual class FakePlatformUtilities actual constructor() : PlatformUtilities {
  private fun unsupported(): Nothing =
    error("FakePlatformUtilities is not used by the home-tabs component tests")

  override fun exitApplication(exitApplicationType: ExitApplicationType) { unsupported() }
  override fun saveSettings(settings: Settings) { unsupported() }
  override fun showAlert(message: String) { unsupported() }
  override suspend fun recoveryCodeToJWE(recoveryCode: String, settings: Settings): String =
    unsupported()
  override suspend fun generateRecoveryCode(settings: Settings): Pair<String, String> =
    unsupported()
  override suspend fun setupEncryptionKey(encryptionKey: String): Settings? = unsupported()
  override suspend fun getEncryptionKey(settings: Settings): String = unsupported()
  override fun decryptEncryptionKey(encryptedEncryptionKey: String): String = unsupported()
  override suspend fun encryptData(data: String, settings: Settings): String = unsupported()
  override suspend fun decryptData(data: String, settings: Settings): String = unsupported()
  override fun getPublicKey(): String = unsupported()
  override fun openURL(
    url: String,
    colorSchemeParams: ColorSchemeParams,
    webURLOpenBehavior: WebURLOpenBehavior
  ) { unsupported() }
  override fun updateWidgets(sessionInvalidated: Boolean) { unsupported() }
  override fun performAdditionalPushNotificationSetup() { unsupported() }
}
