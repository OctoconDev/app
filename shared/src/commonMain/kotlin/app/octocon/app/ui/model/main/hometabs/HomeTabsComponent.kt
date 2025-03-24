package app.octocon.app.ui.model.main.hometabs

import app.octocon.app.ui.model.MainComponentContext
import app.octocon.app.ui.model.interfaces.SettingsInterface
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.Lifecycle
import kotlinx.serialization.Serializable

interface HomeTabsComponent {
  val stack: Value<ChildStack<*, Child>>

  val settings: SettingsInterface

  fun navigateToAlters()
  fun navigateToHistory()
  fun navigateToFriends()
  fun navigateToJournal()

  val onCurrentTabPressed: (() -> Unit)?
  fun updateOnCurrentTabPressed(onCurrentTabPressed: () -> Unit)

  sealed interface Child {
    class AltersChild(val component: AltersComponent) : Child
    class FrontHistoryChild(val component: FrontHistoryComponent) : Child
    class FriendsChild(val component: FriendsComponent) : Child
    class JournalChild(val component: JournalComponent) : Child
  }
}

class HomeTabsComponentImpl(
  componentContext: MainComponentContext,
  navigator: StackNavigation<Config>,
  private val navigationWrapper: (Config) -> Unit,
  private val navigateToCustomFieldsFun: () -> Unit
) : HomeTabsComponent, MainComponentContext by componentContext {
  private val _stack =
    childStack(
      source = navigator,
      serializer = Config.serializer(),
      initialStack = { listOf(
        if(settings.data.value.isSinglet) Config.Friends else Config.Alters
      ) },
      handleBackButton = false,
      childFactory = ::child,
    )

  override val stack: Value<ChildStack<*, HomeTabsComponent.Child>> = _stack

  init {
    lifecycle.subscribe(object : Lifecycle.Callbacks {
      private fun navigateToFriendsIfNecessary() {
        if(settings.data.value.isSinglet) {
          navigationWrapper(Config.Friends)
        }
      }

      override fun onStart() = navigateToFriendsIfNecessary()
      override fun onResume() = navigateToFriendsIfNecessary()
    })
  }

  override var onCurrentTabPressed: (() -> Unit)? = null

  override fun updateOnCurrentTabPressed(onCurrentTabPressed: () -> Unit) {
    this.onCurrentTabPressed = onCurrentTabPressed
  }

  override fun navigateToAlters() = navigationWrapper(Config.Alters)
  override fun navigateToHistory() = navigationWrapper(Config.FrontHistory)
  override fun navigateToFriends() = navigationWrapper(Config.Friends)
  override fun navigateToJournal() = navigationWrapper(Config.Journal)

  private fun child(config: Config, componentContext: MainComponentContext): HomeTabsComponent.Child {
    return when (config) {
      Config.Alters ->
        HomeTabsComponent.Child.AltersChild(
          AltersComponentImpl(
            componentContext = componentContext,
            navigateToCustomFieldsFun = navigateToCustomFieldsFun
          )
        )

      Config.FrontHistory ->
        HomeTabsComponent.Child.FrontHistoryChild(
          FrontHistoryComponentImpl(componentContext = componentContext)
        )

      Config.Friends ->
        HomeTabsComponent.Child.FriendsChild(
          FriendsComponentImpl(componentContext = componentContext)
        )

      Config.Journal ->
        HomeTabsComponent.Child.JournalChild(
          JournalComponentImpl(componentContext = componentContext)
        )
    }
  }


  @Serializable
  sealed interface Config {
    @Serializable
    data object Alters : Config

    @Serializable
    data object FrontHistory : Config

    @Serializable
    data object Friends : Config

    @Serializable
    data object Journal : Config
  }
}