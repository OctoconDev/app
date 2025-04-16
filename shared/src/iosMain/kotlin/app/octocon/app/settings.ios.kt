package app.octocon.app

import app.octocon.app.utils.globalSerializer
import app.octocon.app.utils.encryptionVault
import com.liftric.kvault.KVault

private val vault = KVault("octocon_settings", accessGroup = "AVJM9TZ9VF.app.octocon.OctoconApp.Keychain", accessibility = KVault.Accessible.WhenUnlockedThisDeviceOnly)

fun clearKeychain() {
  vault.deleteObject("settings")
  encryptionVault.deleteObject("encryption_key")
}

fun getSettingsFromKeychain(): Settings {
  vault.string("settings")?.let { return Settings.deserialize(it) }

  return Settings()
}

fun saveSettingsToKeychain(settings: Settings) {
  vault.set("settings", globalSerializer.encodeToString(settings))
}