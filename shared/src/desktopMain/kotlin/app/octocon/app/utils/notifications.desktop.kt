package app.octocon.app.utils

import androidx.compose.runtime.Composable
import app.octocon.app.ui.model.interfaces.ApiInterface
import app.octocon.app.ui.model.interfaces.SettingsInterface

// Stub declaration
@Composable
internal actual fun InitPushNotifications(): InitPushNotificationsCallbacks = { _: String, _: ApiInterface, _: SettingsInterface, _: PlatformUtilities -> } to { _, _, _, _ -> }