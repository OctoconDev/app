package app.octocon.app.ui.model.interfaces

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
import app.octocon.app.utils.PlatformUtilities
import app.octocon.app.utils.crypto.AES
import app.octocon.app.utils.crypto.CipherPadding
import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import io.ktor.util.encodeBase64
import io.ktor.utils.io.core.toByteArray
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

interface SettingsReadInterface {
  val data: StateFlow<Settings>
  @Composable
  fun collectAsState(): State<Settings>
}

interface SettingsInterface : SettingsReadInterface {
  fun pushSettings(settings: Settings, updateWidgets: Boolean = false)
  fun nukeEverything(fully: Boolean = true)
  fun setToken(token: String?)

  fun clearEncryptionKey()

  fun setColorMode(colorMode: ColorMode)
  fun setThemeColor(themeColor: ThemeColor)
  fun setDynamicColorType(dynamicColorType: DynamicColorType)
  fun setAmoledMode(amoledMode: Boolean)
  fun setCornerStyle(cornerStyle: CornerStyle)
  fun setChangeFrontMode(changeFrontMode: ChangeFrontMode)
  fun disablePINLock(currentToken: String)
  fun enablePINLock(pin: String)
  fun setStealthModeEnabled(stealthModeEnabled: Boolean)
  fun setQuickExitEnabled(quickExitEnabled: Boolean)
  fun setShowHelpFAB(showHelpFAB: Boolean)
  fun setShowPermanentTips(showPermanentTips: Boolean)
  fun setFontChoice(fontChoice: FontChoice)
  fun setFontSizeScalar(fontSizeScalar: FontSizeScalar)
  fun setShowAlterIds(showAlterIds: Boolean)
  fun setUseSmallAvatars(useSmallAvatars: Boolean)
  fun setUseTabletLayout(useTabletLayout: Boolean)
  fun setHideAltersInTags(hideAltersInTags: Boolean)
  fun setColorContrastLevel(colorContrastLevel: ColorContrastLevel)
  fun setReduceMotion(reduceMotion: Boolean)
  // fun setScreenTransitionType(screenTransitionType: ScreenTransitionType)
  fun setSpotlightEnabled(spotlightEnabled: Boolean)
  fun setSpotlightLongPressTimeout(spotlightLongPressTimeout: SpotlightLongPressTimeout)
  fun setShowPushNotifications(
    showPushNotifications: Boolean,
    showAlert: (String) -> Unit,
    sendToken: () -> Unit,
    invalidateToken: () -> Unit,
    tryInit: (commit: (Boolean) -> Unit) -> Unit
  )
  fun setAlterSortingMethod(alterSortingMethod: AlterSortingMethod)
  fun setTagsCollapsed(tagsCollapsed: Boolean)
  fun setHasViewedOnboarding(hasViewedOnboarding: Boolean)
  fun setIsSinglet(isSinglet: Boolean)

  fun getEncryptionKey(): String
}

class SettingsInterfaceImpl(
  initialSettings: Settings,
  val settingsSaver: (Settings) -> Unit,
  private val platformUtilities: PlatformUtilities,
) : SettingsInterface, InstanceKeeper.Instance {
  private val _settings = MutableStateFlow(initialSettings)
  override val data: StateFlow<Settings> = _settings

  @Composable
  override fun collectAsState() = data.collectAsState()

  override fun pushSettings(settings: Settings, updateWidgets: Boolean) {
    _settings.tryEmit(settings)
    if(updateWidgets) { platformUtilities.updateWidgets() }
  }

  override fun nukeEverything(fully: Boolean) =
    updateSettings(updateWidgets = true) {
      Settings().copy(
        token = if (fully) null else it.token,
        tokenIsProtected = if(fully) false else it.tokenIsProtected,
        encryptedEncryptionKey = if(fully) null else it.encryptedEncryptionKey,
        showPushNotifications = if (fully) false else it.showPushNotifications,
        hasViewedOnboarding = !fully,
      )
    }

  override fun setToken(token: String?) =
    updateSettings(updateWidgets = true) {
      it.copy(token = token)
    }

  override fun clearEncryptionKey() =
    updateSettings(updateWidgets = true) {
      it.copy(encryptedEncryptionKey = null)
    }

  override fun setColorMode(colorMode: ColorMode) =
    updateSettings(updateWidgets = true) {
      it.copy(colorMode = colorMode)
    }

  override fun setThemeColor(themeColor: ThemeColor) =
    updateSettings(updateWidgets = true) {
      it.copy(themeColor = themeColor)
    }

  override fun setDynamicColorType(dynamicColorType: DynamicColorType) =
    updateSettings(updateWidgets = true) {
      it.copy(dynamicColorType = dynamicColorType)
    }

  override fun setAmoledMode(amoledMode: Boolean) =
    updateSettings(updateWidgets = true) {
      it.copy(amoledMode = amoledMode)
    }

  override fun setCornerStyle(cornerStyle: CornerStyle) =
    updateSettings {
      it.copy(cornerStyle = cornerStyle)
    }

  override fun setChangeFrontMode(changeFrontMode: ChangeFrontMode) =
    updateSettings {
      it.copy(changeFrontMode = changeFrontMode)
    }

  override fun disablePINLock(currentToken: String) =
    updateSettings(updateWidgets = true) {
      it.copy(
        token = currentToken,
        tokenIsProtected = false
      )
    }

  override fun enablePINLock(pin: String) =
    updateSettings(updateWidgets = true) {
      it.copy(
        token = AES.encryptAesEcb(
          ("tk|" + it.token!!).toByteArray(),
          pin.toByteArray(),
          CipherPadding.ZeroPadding
        ).encodeBase64(),
        tokenIsProtected = true,
      )
    }

  override fun setStealthModeEnabled(stealthModeEnabled: Boolean) =
    updateSettings {
      it.copy(stealthModeEnabled = stealthModeEnabled)
    }

  override fun setQuickExitEnabled(quickExitEnabled: Boolean) =
    updateSettings {
      it.copy(quickExitEnabled = quickExitEnabled)
    }

  override fun setShowHelpFAB(showHelpFAB: Boolean) =
    updateSettings {
      it.copy(showHelpFAB = showHelpFAB)
    }

  override fun setShowPermanentTips(showPermanentTips: Boolean) =
    updateSettings {
      it.copy(showPermanentTips = showPermanentTips)
    }

  override fun setFontChoice(fontChoice: FontChoice) =
    updateSettings {
      it.copy(fontChoice = fontChoice)
    }

  override fun setFontSizeScalar(fontSizeScalar: FontSizeScalar) =
    updateSettings {
      it.copy(fontSizeScalar = fontSizeScalar)
    }

  override fun setShowAlterIds(showAlterIds: Boolean) =
    updateSettings {
      it.copy(showAlterIds = showAlterIds)
    }

  override fun setUseSmallAvatars(useSmallAvatars: Boolean) =
    updateSettings {
      it.copy(useSmallAvatars = useSmallAvatars)
    }

  override fun setUseTabletLayout(useTabletLayout: Boolean) =
    updateSettings {
      it.copy(useTabletLayout = useTabletLayout)
    }

  override fun setHideAltersInTags(hideAltersInTags: Boolean) =
    updateSettings {
      it.copy(hideAltersInTags = hideAltersInTags)
    }

  override fun setColorContrastLevel(colorContrastLevel: ColorContrastLevel) =
    updateSettings(updateWidgets = true) {
      it.copy(colorContrastLevel = colorContrastLevel)
    }

  override fun setReduceMotion(reduceMotion: Boolean) =
    updateSettings {
      it.copy(reduceMotion = reduceMotion)
    }

  /*override fun setScreenTransitionType(screenTransitionType: ScreenTransitionType) =
    updateSettings {
      it.copy(screenTransitionType = screenTransitionType)
    }*/

  override fun setSpotlightEnabled(spotlightEnabled: Boolean) =
    updateSettings {
      it.copy(spotlightEnabled = spotlightEnabled)
    }

  override fun setSpotlightLongPressTimeout(spotlightLongPressTimeout: SpotlightLongPressTimeout) =
    updateSettings {
      it.copy(spotlightLongPressTimeout = spotlightLongPressTimeout)
    }

  override fun setShowPushNotifications(
    showPushNotifications: Boolean,
    showAlert: (String) -> Unit,
    sendToken: () -> Unit,
    invalidateToken: () -> Unit,
    tryInit: (commit: (Boolean) -> Unit) -> Unit
  ) {
    fun commit(value: Boolean) {
      updateSettings {
        it.copy(showPushNotifications = value)
      }
      if (value) {
        sendToken()
      } else {
        invalidateToken()
      }
    }

    if (!showPushNotifications) {
      commit(false)
      return
    }

    tryInit(::commit)
  }

  override fun setAlterSortingMethod(alterSortingMethod: AlterSortingMethod) =
    updateSettings {
      it.copy(alterSortingMethod = alterSortingMethod)
    }

  override fun setTagsCollapsed(tagsCollapsed: Boolean) =
    updateSettings {
      it.copy(tagsCollapsed = tagsCollapsed)
    }

  override fun setHasViewedOnboarding(hasViewedOnboarding: Boolean) =
    updateSettings {
      it.copy(hasViewedOnboarding = hasViewedOnboarding)
    }

  override fun setIsSinglet(isSinglet: Boolean) =
    updateSettings {
      it.copy(isSinglet = isSinglet)
    }

  override fun getEncryptionKey(): String {
    return platformUtilities.getEncryptionKey(_settings.value)
  }

  private fun updateSettings(updateWidgets: Boolean = false, block: (Settings) -> Settings) {
    val old = _settings.value
    val new = block(old)
    _settings.tryEmit(new)
    settingsSaver(new)

    if(updateWidgets) {
      platformUtilities.updateWidgets()
    }
  }
}