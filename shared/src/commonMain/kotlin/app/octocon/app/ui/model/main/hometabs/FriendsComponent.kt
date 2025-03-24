package app.octocon.app.ui.model.main.hometabs

import app.octocon.app.ui.model.MainComponentContext
import app.octocon.app.ui.model.interfaces.SettingsInterface
import app.octocon.app.ui.model.main.hometabs.friends.FriendAlterViewComponent
import app.octocon.app.ui.model.main.hometabs.friends.FriendAlterViewComponentImpl
import app.octocon.app.ui.model.main.hometabs.friends.FriendListComponent
import app.octocon.app.ui.model.main.hometabs.friends.FriendListComponentImpl
import app.octocon.app.ui.model.main.hometabs.friends.FriendTagViewComponent
import app.octocon.app.ui.model.main.hometabs.friends.FriendTagViewComponentImpl
import app.octocon.app.ui.model.main.hometabs.friends.FriendViewComponent
import app.octocon.app.ui.model.main.hometabs.friends.FriendViewComponentImpl
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.router.panels.ChildPanels
import com.arkivanov.decompose.router.panels.ChildPanelsMode
import com.arkivanov.decompose.router.panels.Panels
import com.arkivanov.decompose.router.panels.PanelsNavigation
import com.arkivanov.decompose.router.panels.activateDetails
import com.arkivanov.decompose.router.panels.childPanels
import com.arkivanov.decompose.router.panels.pop
import com.arkivanov.decompose.router.panels.setMode
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackHandlerOwner
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer

@OptIn(ExperimentalDecomposeApi::class)
interface FriendsComponent : BackHandlerOwner {
  val panels: Value<ChildPanels<*, FriendListComponent, *, FriendViewComponent, *, ExtraChild>>

  val settings: SettingsInterface

  fun navigateToFriendView(friendID: String)
  fun navigateToFriendTagView(friendID: String, tagID: String)
  fun navigateToFriendAlterView(friendID: String, alterID: Int)

  fun onBackPressed()

  fun setMode(mode: ChildPanelsMode)

  sealed interface ExtraChild {
    class FriendTagViewChild(val component: FriendTagViewComponent) : ExtraChild
    class FriendAlterViewChild(val component: FriendAlterViewComponent) : ExtraChild
  }
}

@OptIn(ExperimentalDecomposeApi::class)
class FriendsComponentImpl(
  componentContext: MainComponentContext,
) : FriendsComponent, MainComponentContext by componentContext {

  private val navigator = PanelsNavigation<Unit, DetailsConfig, ExtraConfig>()

  private val _panels =
    childPanels(
      source = navigator,
      serializers = Triple(Unit.serializer(), DetailsConfig.serializer(), ExtraConfig.serializer()),
      initialPanels = { Panels(main = Unit) },
      mainFactory = { _, componentContext ->
        FriendListComponentImpl(
          componentContext = componentContext,
          navigateToFriendViewFun = ::navigateToFriendView,
          navigateToFriendAlterViewFun = { friendID, alterID -> navigateToFriendAlterView(friendID, alterID) }
        )
      },
      detailsFactory = { config, componentContext ->
        FriendViewComponentImpl(
          componentContext = componentContext,
          popSelf = navigator::pop,
          navigateToFriendTagViewFun = { tagID -> navigateToFriendTagView(config.friendID, tagID) },
          navigateToFriendAlterViewFun = { alterID -> navigateToFriendAlterView(config.friendID, alterID) },
          friendID = config.friendID
        )
      },
      extraFactory = ::extraChild
    )

  override val panels: Value<ChildPanels<*, FriendListComponent, *, FriendViewComponent, *, FriendsComponent.ExtraChild>> = _panels

  override fun navigateToFriendView(friendID: String) = navigator.activateDetails(
    DetailsConfig(friendID)
  )
  override fun navigateToFriendTagView(friendID: String, tagID: String) {
    navigator.navigate(
      transformer = {
        it.copy(
          details = DetailsConfig(friendID),
          extra = ExtraConfig.FriendTagView(friendID, tagID)
        )
      },
      onComplete = {_, _ -> }
    )
  }

  override fun navigateToFriendAlterView(friendID: String, alterID: Int) {
    navigator.navigate(
      transformer = {
        it.copy(
          details = DetailsConfig(friendID),
          extra = ExtraConfig.FriendAlterView(friendID, alterID)
        )
      },
      onComplete = {_, _ -> }
    )
  }

  override fun onBackPressed() = navigator.pop()

  override fun setMode(mode: ChildPanelsMode) = navigator.setMode(mode)

  private fun extraChild(config: ExtraConfig, componentContext: MainComponentContext): FriendsComponent.ExtraChild {
    return when (config) {
      is ExtraConfig.FriendTagView ->
        FriendsComponent.ExtraChild.FriendTagViewChild(
          FriendTagViewComponentImpl(
            componentContext = componentContext,
            popSelf = navigator::pop,
            navigateToFriendTagViewFun = { tagID -> navigateToFriendTagView(config.friendID, tagID) },
            navigateToFriendAlterViewFun = { alterID -> navigateToFriendAlterView(config.friendID, alterID) },
            friendID = config.friendID,
            tagID = config.tagID
          )
        )

      is ExtraConfig.FriendAlterView ->
        FriendsComponent.ExtraChild.FriendAlterViewChild(
          FriendAlterViewComponentImpl(
            componentContext = componentContext,
            popSelf = navigator::pop,
            friendID = config.friendID,
            alterID = config.alterID
          )
        )
    }
  }

  @Serializable
  private data class DetailsConfig(val friendID: String)

  @Serializable
  private sealed interface ExtraConfig {
    @Serializable
    data class FriendTagView(val friendID: String, val tagID: String) : ExtraConfig

    @Serializable
    data class FriendAlterView(val friendID: String, val alterID: Int) : ExtraConfig
  }
}