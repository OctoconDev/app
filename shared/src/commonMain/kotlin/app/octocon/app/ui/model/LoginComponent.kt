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
import app.octocon.app.utils.ioDispatcher
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable

enum class ServerHealthStatus {
  UNKNOWN,
  CHECKING,
  HEALTHY,
  DEGRADED,
  UNREACHABLE
}

interface LoginComponent {
  val settings: SettingsInterface

  val model: StateFlow<Model>

  fun logInWithGoogle(colorSchemeParams: ColorSchemeParams)
  fun logInWithDiscord(colorSchemeParams: ColorSchemeParams)
  fun logInWithApple(colorSchemeParams: ColorSchemeParams)

  fun incrementDirectTokenLoginTimesPressed()
  fun closeDirectTokenDialog()
  fun logInWithDirectToken(token: String)

  fun updateServerUrl(url: String)
  fun checkServerHealth()

  @Serializable
  data class Model(
    val directTokenTimesPressed: Int = 0,
    val directTokenDialogOpen: Boolean = false,
    val serverUrl: String = app.octocon.app.Settings.DEFAULT_API_ENDPOINT,
    val serverHealthStatus: ServerHealthStatus = ServerHealthStatus.UNKNOWN
  )
}

private const val DIRECT_TOKEN_PRESSES_REQUIRED = 5

internal data class HealthCheckResult(val readyUp: Boolean, val liveUp: Boolean)

internal expect suspend fun performHealthCheck(baseUrl: String): HealthCheckResult

private fun buildLoginUrl(provider: String, apiEndpoint: String): String =
  "$apiEndpoint/auth/${provider}" +
          "?platform=${DevicePlatform.internalName}" +
          "&version_code=${VERSION_CODE}" +
          "&is_beta=${IS_BETA}" +
          "&redirect_uri=octocon://auth/token"

internal class LoginComponentImpl(
  componentContext: CommonComponentContext
) : LoginComponent, CommonComponentContext by componentContext {
  private val handler = retainStateHandler { LoginComponent.Model() }
  init {
    registerStateHandler(handler)
  }
  override val model = handler.model

  private val scope = coroutineScope(coroutineContext + SupervisorJob())
  private var healthCheckJob: Job? = null

  override fun logInWithGoogle(colorSchemeParams: ColorSchemeParams) = logInWithProvider("google", colorSchemeParams)
  override fun logInWithDiscord(colorSchemeParams: ColorSchemeParams) = logInWithProvider("discord", colorSchemeParams)
  override fun logInWithApple(colorSchemeParams: ColorSchemeParams) = logInWithProvider("apple", colorSchemeParams)

  private fun logInWithProvider(provider: String, colorSchemeParams: ColorSchemeParams) {
    platformUtilities.openURL(
      buildLoginUrl(provider, settings.data.value.apiEndpoint),
      colorSchemeParams,
      webURLOpenBehavior = WebURLOpenBehavior.SameTab
    )
  }

  override fun updateServerUrl(url: String) {
    model.tryEmit(
      model.value.copy(
        serverUrl = url,
        serverHealthStatus = ServerHealthStatus.UNKNOWN
      )
    )
  }

  override fun checkServerHealth() {
    healthCheckJob?.cancel()
    val baseUrl = model.value.serverUrl.trimEnd('/')
    if (baseUrl.isBlank()) {
      model.tryEmit(model.value.copy(serverHealthStatus = ServerHealthStatus.UNREACHABLE))
      return
    }

    model.tryEmit(model.value.copy(serverHealthStatus = ServerHealthStatus.CHECKING))

    healthCheckJob = scope.launch {
      try {
        val result = withContext(ioDispatcher) { performHealthCheck(baseUrl) }

        val status = when {
          result.readyUp && result.liveUp -> ServerHealthStatus.HEALTHY
          result.liveUp -> ServerHealthStatus.DEGRADED
          else -> ServerHealthStatus.UNREACHABLE
        }
        model.tryEmit(model.value.copy(serverHealthStatus = status))
      } catch (_: Exception) {
        model.tryEmit(model.value.copy(serverHealthStatus = ServerHealthStatus.UNREACHABLE))
      }
    }
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