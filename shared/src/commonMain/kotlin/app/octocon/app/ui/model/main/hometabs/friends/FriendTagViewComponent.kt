package app.octocon.app.ui.model.main.hometabs.friends

import app.octocon.app.api.model.ExternalTag
import app.octocon.app.api.parseAmbiguousID
import app.octocon.app.ui.model.CommonInterface
import app.octocon.app.ui.model.MainComponentContext
import app.octocon.app.ui.model.interfaces.ApiInterfaceImpl
import app.octocon.app.ui.model.main.hometabs.friends.FriendTagViewComponent.TagState
import app.octocon.app.ui.registerStateHandler
import app.octocon.app.ui.retainStateHandler
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import io.ktor.http.HttpMethod
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

interface FriendTagViewComponent : CommonInterface {
  val friendID: String
  val tagID: String

  val model: StateFlow<Model>

  fun navigateBack()
  fun navigateToFriendTagView(tagID: String)
  fun navigateToFriendAlterView(alterID: Int)

  @Serializable
  data class Model(
    val tag: TagState = TagState.Loading
  )

  @Serializable
  sealed interface TagState {
    @Serializable
    data object Loading : TagState
    @Serializable
    data class Success(val tag: ExternalTag) : TagState
    @Serializable
    data class Error(val message: String) : TagState

    val ensureData: ExternalTag
      get() = (this as Success).tag

    val isSuccess: Boolean
      get() = this is Success
  }
}

class FriendTagViewComponentImpl(
  componentContext: MainComponentContext,
  private val popSelf: () -> Unit,
  private val navigateToFriendTagViewFun: (String) -> Unit,
  private val navigateToFriendAlterViewFun: (Int) -> Unit,
  override val friendID: String,
  override val tagID: String
) : FriendTagViewComponent, MainComponentContext by componentContext {
  private val coroutineScope = coroutineScope(coroutineContext + SupervisorJob())
  private val handler = retainStateHandler { FriendTagViewComponent.Model() }
  init {
    registerStateHandler(handler)
  }
  override val model = handler.model

  override fun navigateBack() {
    popSelf()
  }

  override fun navigateToFriendTagView(tagID: String) {
    navigateToFriendTagViewFun(tagID)
  }

  override fun navigateToFriendAlterView(alterID: Int) {
    navigateToFriendAlterViewFun(alterID)
  }

  init {
    coroutineScope.launch {
      try {
        (api as ApiInterfaceImpl).sendAPIRequest<ExternalTag>(
          HttpMethod.Get,
          "systems/${parseAmbiguousID(friendID)}/tags/${tagID}"
        ) { isSuccess, response ->
          if (isSuccess) {
            handler.model.tryEmit(
              handler.model.value.copy(
                tag = TagState.Success(response.data!!)
              )
            )
          } else {
            handler.model.tryEmit(
              handler.model.value.copy(
                tag = TagState.Error("Failed to load tag")
              )
            )
          }
        }
      } catch (e: Exception) {
        handler.model.emit(
          handler.model.value.copy(
            tag = TagState.Error("Failed to load tag")
          )
        )
      }
    }
  }
}