package app.octocon.app.ui.compose.screens.main.hometabs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import app.octocon.app.AlterSortingMethod
import app.octocon.app.ChangeFrontMode
import app.octocon.app.ColorContrastLevel
import app.octocon.app.ColorMode
import app.octocon.app.CornerStyle
import app.octocon.app.DynamicColorType
import app.octocon.app.FontChoice
import app.octocon.app.FontSizeScalar
import app.octocon.app.Settings
import app.octocon.app.SpotlightLongPressTimeout
import app.octocon.app.ThemeColor
import app.octocon.app.ui.model.interfaces.SettingsInterface
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Read-only test double of [SettingsInterface] backed by a [MutableStateFlow] so the
 * caller can mutate [data] directly. Every setter throws so any incidental call from a
 * scaffold-only test surfaces immediately instead of being silently ignored.
 */
class FakeSettingsInterface(
  initial: Settings = Settings()
) : SettingsInterface {
  private val _data = MutableStateFlow(initial)
  override val data: StateFlow<Settings> = _data

  fun update(transform: (Settings) -> Settings) {
    _data.value = transform(_data.value)
  }

  @Composable
  override fun collectAsState(): State<Settings> = data.collectAsState()

  private fun unsupported(): Nothing =
    error("FakeSettingsInterface does not support mutation in scaffold-only tests")

  override fun pushSettings(settings: Settings, updateWidgets: Boolean) = unsupported()
  override fun nukeEverything(fully: Boolean) = unsupported()
  override fun setToken(token: String?) = unsupported()
  override fun clearEncryptionKey() = unsupported()
  override fun setColorMode(colorMode: ColorMode) = unsupported()
  override fun setThemeColor(themeColor: ThemeColor) = unsupported()
  override fun setDynamicColorType(dynamicColorType: DynamicColorType) = unsupported()
  override fun setAmoledMode(amoledMode: Boolean) = unsupported()
  override fun setCornerStyle(cornerStyle: CornerStyle) = unsupported()
  override fun setChangeFrontMode(changeFrontMode: ChangeFrontMode) = unsupported()
  override fun disablePINLock(currentToken: String) = unsupported()
  override fun enablePINLock(pin: String) = unsupported()
  override fun setStealthModeEnabled(stealthModeEnabled: Boolean) = unsupported()
  override fun setQuickExitEnabled(quickExitEnabled: Boolean) = unsupported()
  override fun setShowHelpFAB(showHelpFAB: Boolean) = unsupported()
  override fun setShowPermanentTips(showPermanentTips: Boolean) = unsupported()
  override fun setFontChoice(fontChoice: FontChoice) = unsupported()
  override fun setFontSizeScalar(fontSizeScalar: FontSizeScalar) = unsupported()
  override fun setShowAlterIds(showAlterIds: Boolean) = unsupported()
  override fun setUseSmallAvatars(useSmallAvatars: Boolean) = unsupported()
  override fun setUseTabletLayout(useTabletLayout: Boolean) = unsupported()
  override fun setHideAltersInTags(hideAltersInTags: Boolean) = unsupported()
  override fun setColorContrastLevel(colorContrastLevel: ColorContrastLevel) = unsupported()
  override fun setReduceMotion(reduceMotion: Boolean) = unsupported()
  override fun setSpotlightEnabled(spotlightEnabled: Boolean) = unsupported()
  override fun setSpotlightLongPressTimeout(spotlightLongPressTimeout: SpotlightLongPressTimeout) = unsupported()
  override fun setShowPushNotifications(
    showPushNotifications: Boolean,
    showAlert: (String) -> Unit,
    sendToken: () -> Unit,
    invalidateToken: () -> Unit,
    tryInit: (commit: (Boolean) -> Unit) -> Unit
  ) = unsupported()

  override fun setAlterSortingMethod(alterSortingMethod: AlterSortingMethod) = unsupported()
  override fun setTagsCollapsed(tagsCollapsed: Boolean) = unsupported()
  override fun setHasViewedOnboarding(hasViewedOnboarding: Boolean) = unsupported()
  override fun setIsSinglet(isSinglet: Boolean) = unsupported()
  override fun setInstallServiceWorker(installServiceWorker: Boolean) = unsupported()
  override fun setApiEndpoint(apiEndpoint: String) = unsupported()

  override fun isAppInstalled(): Boolean = unsupported()
  override suspend fun getEncryptionKey(): String = unsupported()
}
