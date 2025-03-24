package app.octocon.app.ui.model.main.hometabs.friends

import app.octocon.app.api.model.ExternalAlter
import app.octocon.app.api.parseAmbiguousID
import app.octocon.app.ui.model.MainComponentContext
import app.octocon.app.ui.model.interfaces.ApiInterfaceImpl
import app.octocon.app.ui.model.interfaces.SettingsInterface
import app.octocon.app.ui.model.main.hometabs.friends.FriendAlterViewComponent.AlterState
import app.octocon.app.ui.registerStateHandler
import app.octocon.app.ui.retainStateHandler
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import io.ktor.http.HttpMethod
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

interface FriendAlterViewComponent {
  val settings: SettingsInterface
  val friendID: String
  val alterID: Int

  val model: StateFlow<Model>

  fun navigateBack()

  @Serializable
  data class Model(
    val alter: AlterState = AlterState.Loading
  )

  @Serializable
  sealed interface AlterState {
    @Serializable
    data object Loading : AlterState
    @Serializable
    data class Success(val alter: ExternalAlter) : AlterState
    @Serializable
    data class Error(val message: String) : AlterState

    val ensureData: ExternalAlter
      get() = (this as Success).alter

    val isSuccess: Boolean
      get() = this is Success
  }
}

class FriendAlterViewComponentImpl(
  componentContext: MainComponentContext,
  private val popSelf: () -> Unit,
  override val friendID: String,
  override val alterID: Int
) : FriendAlterViewComponent, MainComponentContext by componentContext {
  private val coroutineScope = coroutineScope(coroutineContext + SupervisorJob())
  private val handler = retainStateHandler { FriendAlterViewComponent.Model() }
  init {
    registerStateHandler(handler)
  }
  override val model = handler.model

  override fun navigateBack() {
    popSelf()
  }

  init {
    coroutineScope.launch {
      try {
        (api as ApiInterfaceImpl).sendAPIRequest<ExternalAlter>(
          HttpMethod.Get,
          "systems/${parseAmbiguousID(friendID)}/alters/${alterID}"
        ) { isSuccess, response ->
          if (isSuccess) {
            handler.model.tryEmit(
              handler.model.value.copy(
                alter = AlterState.Success(response.data!!)
              )
            )
          } else {
            handler.model.tryEmit(
              handler.model.value.copy(
                alter = AlterState.Error("Failed to load alter")
              )
            )
          }
        }
      } catch (_: Exception) {
        handler.model.emit(
          handler.model.value.copy(
            alter = AlterState.Error("Failed to load alter")
          )
        )
      }
    }
  }
}