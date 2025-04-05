package app.octocon.app.ui.model

import app.octocon.app.ui.compose.screens.IS_BETA
import app.octocon.app.ui.compose.screens.VERSION_CODE
import app.octocon.app.ui.model.interfaces.SettingsInterface
import app.octocon.app.ui.registerStateHandler
import app.octocon.app.ui.retainStateHandler
import app.octocon.app.utils.ColorSchemeParams
import app.octocon.app.utils.DevicePlatform
import app.octocon.app.utils.ExitApplicationType
import app.octocon.app.utils.WebURLOpenBehavior
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable

interface LoginComponent {
  val settings: SettingsInterface

  val model: StateFlow<Model>

  fun logInWithGoogle(colorSchemeParams: ColorSchemeParams)
  fun logInWithDiscord(colorSchemeParams: ColorSchemeParams)
  fun logInWithApple(colorSchemeParams: ColorSchemeParams)

  fun incrementDirectTokenLoginTimesPressed()
  fun closeDirectTokenDialog()
  fun logInWithDirectToken(token: String)

  @Serializable
  data class Model(
    val directTokenTimesPressed: Int = 0,
    val directTokenDialogOpen: Boolean = false
  )
}

private const val DIRECT_TOKEN_PRESSES_REQUIRED = 5

private fun buildLoginUrl(provider: String): String =
  "https://api.octocon.app/auth/${provider}" +
      "?platform=${DevicePlatform.internalName}" +
      "&version_code=${VERSION_CODE}" +
      "&is_beta=${IS_BETA}"

internal class LoginComponentImpl(
  componentContext: CommonComponentContext
) : LoginComponent, CommonComponentContext by componentContext {
  private val handler = retainStateHandler { LoginComponent.Model() }
  init {
    registerStateHandler(handler)
  }
  override val model = handler.model

  override fun logInWithGoogle(colorSchemeParams: ColorSchemeParams) = logInWithProvider("google", colorSchemeParams)
  override fun logInWithDiscord(colorSchemeParams: ColorSchemeParams) = logInWithProvider("discord", colorSchemeParams)
  override fun logInWithApple(colorSchemeParams: ColorSchemeParams) = logInWithProvider("apple", colorSchemeParams)

  private fun logInWithProvider(provider: String, colorSchemeParams: ColorSchemeParams) {
    platformUtilities.openURL(
      buildLoginUrl(provider),
      colorSchemeParams,
      webURLOpenBehavior = WebURLOpenBehavior.SameTab
    )
  }

  override fun incrementDirectTokenLoginTimesPressed() {
    if (model.value.directTokenTimesPressed >= DIRECT_TOKEN_PRESSES_REQUIRED - 1) {
      model.tryEmit(
        model.value.copy(
          directTokenTimesPressed = 0,
          directTokenDialogOpen = true
        )
      )
    } else {
      model.tryEmit(
        model.value.copy(
          directTokenTimesPressed = model.value.directTokenTimesPressed + 1,
        )
      )
    }
  }

  override fun closeDirectTokenDialog() {
    model.tryEmit(
      model.value.copy(
        directTokenDialogOpen = false
      )
    )
  }

  override fun logInWithDirectToken(token: String) {
    settings.setToken(token = token)
    platformUtilities.exitApplication(ExitApplicationType.ForcedRestart)
  }
}