package app.octocon.app.ui.model.main

import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import app.octocon.app.Settings
import app.octocon.app.ui.model.CommonComponentContext
import app.octocon.app.ui.model.MainComponentContext
import app.octocon.app.ui.model.MainComponentContextImpl
import app.octocon.app.ui.model.main.hometabs.HomeTabsComponent
import app.octocon.app.ui.model.main.hometabs.HomeTabsComponentImpl
import app.octocon.app.ui.model.main.polls.PollsComponent
import app.octocon.app.ui.model.main.polls.PollsComponentImpl
import app.octocon.app.ui.model.main.profile.ProfileComponent
import app.octocon.app.ui.model.main.profile.ProfileComponentImpl
import app.octocon.app.ui.model.main.resources.ResourcesComponent
import app.octocon.app.ui.model.main.resources.ResourcesComponentImpl
import app.octocon.app.ui.model.main.settings.SettingsComponent
import app.octocon.app.ui.model.main.settings.SettingsComponentImpl
import app.octocon.app.ui.model.main.supportus.SupportUsComponent
import app.octocon.app.ui.model.main.supportus.SupportUsComponentImpl
import app.octocon.app.utils.ExitApplicationType
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.active
import com.arkivanov.decompose.router.stack.bringToFront
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackHandlerOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

interface MainAppComponent : BackHandlerOwner {
  val stack: Value<ChildStack<*, Child>>
  val settingsData: StateFlow<Settings>

  val drawerState: DrawerState

  fun provideDrawerScope(coroutineScope: CoroutineScope)

  fun exitApplication(exitApplicationType: ExitApplicationType)

  val activeHomeTabsConfig: StateFlow<HomeTabsComponentImpl.Config>

  fun navigateToAlters()
  fun navigateToHistory()
  fun navigateToJournal()
  fun navigateToFriends()

  fun navigateToHomeTabs()
  fun navigateToProfile()
  fun navigateToPolls()
  fun navigateToResources()
  fun navigateToSettings()
  fun navigateToSupportUs()

  fun navigateBack()

  fun toggleDrawer(isOpen: Boolean)

  sealed interface Child {
    class HomeTabsChild(val component: HomeTabsComponent) : Child
    class ProfileChild(val component: ProfileComponent) : Child
    class PollsChild(val component: PollsComponent) : Child
    class ResourcesChild(val component: ResourcesComponent) : Child
    class SettingsChild(val component: SettingsComponent) : Child
    class SupportUsChild(val component: SupportUsComponent) : Child
  }
}

internal class MainAppComponentImpl(
  componentContext: CommonComponentContext,
  deepLinkURL: String? = null,
  private val navigateToLoginScreen: () -> Unit
) : MainAppComponent, CommonComponentContext by componentContext {
  private val navigator = StackNavigation<Config>()
  private val homeTabsNavigator = StackNavigation<HomeTabsComponentImpl.Config>()

  private val _activeHomeTabsConfig = MutableStateFlow<HomeTabsComponentImpl.Config>(HomeTabsComponentImpl.Config.Alters)
  override val activeHomeTabsConfig: StateFlow<HomeTabsComponentImpl.Config> = _activeHomeTabsConfig

  private val _stack =
    childStack(
      source = navigator,
      serializer = Config.serializer(),
      initialStack = { listOf(Config.HomeTabs(deepLinkURL)) },
      handleBackButton = false,
      childFactory = ::child,
    )

  override val settingsData = settings.data

  override fun exitApplication(exitApplicationType: ExitApplicationType) = platformUtilities.exitApplication(exitApplicationType)

  private fun homeTabsNavigationWrapper(config: HomeTabsComponentImpl.Config) {
    _activeHomeTabsConfig.value = config
    homeTabsNavigator.bringToFront(config)
  }

  override fun navigateToAlters() = homeTabsNavigationWrapper(HomeTabsComponentImpl.Config.Alters)
  override fun navigateToHistory() = homeTabsNavigationWrapper(HomeTabsComponentImpl.Config.FrontHistory)
  override fun navigateToJournal() = homeTabsNavigationWrapper(HomeTabsComponentImpl.Config.Journal)
  override fun navigateToFriends() = homeTabsNavigationWrapper(HomeTabsComponentImpl.Config.Friends)

  override fun navigateToHomeTabs() = navigator.bringToFront(Config.HomeTabs())
  override fun navigateToProfile() = navigator.bringToFront(Config.Profile)
  override fun navigateToPolls() = navigator.bringToFront(Config.Polls)
  override fun navigateToResources() = navigator.bringToFront(Config.Resources)
  override fun navigateToSettings() = navigator.bringToFront(Config.Settings)
  override fun navigateToSupportUs() = navigator.bringToFront(Config.SupportUs)

  private fun navigateToCustomFields() {
    navigateToSettings()
    stack.active.instance.let {
      if (it is MainAppComponent.Child.SettingsChild) {
        it.component.navigateToCustomFields()
      }
    }
  }

  override fun navigateBack() = navigator.pop()

  private fun child(config: Config, componentContext: CommonComponentContext): MainAppComponent.Child {
    val newComponentContext = MainComponentContextImpl(componentContext) as MainComponentContext

    return when (config) {
      is Config.HomeTabs ->
        MainAppComponent.Child.HomeTabsChild(
          HomeTabsComponentImpl(
            componentContext = newComponentContext,
            navigator = homeTabsNavigator,
            navigationWrapper = ::homeTabsNavigationWrapper,
            navigateToCustomFieldsFun = { navigateToCustomFields() }
          )
        )

      Config.Profile ->
        MainAppComponent.Child.ProfileChild(
          ProfileComponentImpl(
            componentContext = newComponentContext
          )
        )

      Config.Polls ->
        MainAppComponent.Child.PollsChild(
          PollsComponentImpl(
            componentContext = newComponentContext,
          )
        )

      Config.Resources ->
        MainAppComponent.Child.ResourcesChild(
          ResourcesComponentImpl(componentContext = newComponentContext)
        )

      is Config.Settings ->
        MainAppComponent.Child.SettingsChild(
          SettingsComponentImpl(componentContext = newComponentContext, navigateToLoginScreen = navigateToLoginScreen)
        )

      Config.SupportUs ->
        MainAppComponent.Child.SupportUsChild(
          SupportUsComponentImpl(componentContext = newComponentContext, popSelf = ::navigateBack)
        )
    }
  }

  override val stack: Value<ChildStack<*, MainAppComponent.Child>> = _stack

  override val drawerState: DrawerState = DrawerState(DrawerValue.Closed) { true }

  private var drawerScope: CoroutineScope? = null

  override fun provideDrawerScope(coroutineScope: CoroutineScope) {
    drawerScope = coroutineScope
  }

  override fun toggleDrawer(isOpen: Boolean) {
    drawerScope?.launch {
      if(isOpen) drawerState.open() else drawerState.close()
    }
  }

  @Serializable
  private sealed interface Config {
    @Serializable
    data class HomeTabs(val deepLinkURL: String? = null) : Config

    @Serializable
    data object Profile : Config

    @Serializable
    data object Polls : Config

    @Serializable
    data object Resources : Config

    @Serializable
    data object Settings : Config

    @Serializable
    data object SupportUs : Config
  }
}