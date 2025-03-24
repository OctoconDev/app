package app.octocon.util

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import app.octocon.app.Settings

private const val SETTINGS_KEY = "SERIALIZED_SETTINGS"
private const val ENCRYPTED_PREFS_FILE = "encrypted_prefs.txt"

fun createSharedPreferences(context: Context): SharedPreferences =
  EncryptedSharedPreferences.create(
    context,
    ENCRYPTED_PREFS_FILE,
    getMasterKey(context),
    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
  )

private fun getMasterKey(context: Context) =
  MasterKey.Builder(context)
    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
    .build()

fun getSavedSettings(sharedPreferences: SharedPreferences): Settings {
  val json = sharedPreferences.getString(SETTINGS_KEY, null)
  return if (json == null) {
    Settings()
  } else {
    Settings.deserialize(json)
  }
}