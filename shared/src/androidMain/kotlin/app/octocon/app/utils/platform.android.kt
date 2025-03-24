package app.octocon.app.utils

import android.content.Context

actual val currentPlatform = DevicePlatform.Android

actual interface PlatformUtilities : CommonPlatformUtilities {
  val context: Context
}

actual interface PlatformDelegate