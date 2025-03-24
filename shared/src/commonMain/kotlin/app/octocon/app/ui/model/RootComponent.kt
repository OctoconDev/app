package app.octocon.app.ui.model

import app.octocon.app.Settings
import app.octocon.app.ui.model.interfaces.ApiInterface
import app.octocon.app.ui.model.interfaces.ApiInterfaceImpl
import app.octocon.app.ui.model.interfaces.SettingsInterface
import app.octocon.app.ui.model.interfaces.SettingsInterfaceImpl
import app.octocon.app.ui.model.main.MainAppComponent
import app.octocon.app.ui.model.main.MainAppComponentImpl
import app.octocon.app.ui.model.onboarding.OnboardingComponent
import app.octocon.app.ui.model.onboarding.OnboardingComponentImpl
import app.octocon.app.utils.PlatformEvent
import app.octocon.app.utils.PlatformUtilities
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.replaceAll
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackHandlerOwner
import com.arkivanov.essenty.instancekeeper.retainedInstance
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlin.coroutines.CoroutineContext

interface RootComponent : BackHandlerOwner {
  val stack: Value<ChildStack<*, Child>>
  val settings: SettingsInterface
  val api: ApiInterface
  val platformUtilities: PlatformUtilities
  val platformEventFlow: Flow<PlatformEvent>

  fun tryLoadClient(token: String? = settings.data.value.token)

  sealed interface Child {
    class MainAppChild(val component: MainAppComponent) : Child

    class LoginChild(val component: LoginComponent) : Child
    class OnboardingChild(val component: OnboardingComponent) : Child

    class StealthAppChild(val component: StealthAppComponent) : Child
    class PINEntryChild(val component: PINEntryComponent) : Child
  }
}

class RootComponentImpl(
  componentContext: ComponentContext,
  initialSettings: Settings,
  private val coroutineContext: CoroutineContext,
  override val platformUtilities: PlatformUtilities,
  override val platformEventFlow: Flow<PlatformEvent>,
  deepLinkURL: String? = null
) : RootComponent, ComponentContext by componentContext {
  private val coroutineScope = coroutineScope(coroutineContext + SupervisorJob())

  private val _settings = retainedInstance {
    SettingsInterfaceImpl(
      initialSettings = initialSettings,
      settingsSaver = platformUtilities::saveSettings,
      platformUtilities = platformUtilities
    )
  }

  override val settings: SettingsInterface = _settings

  private val _api = retainedInstance {
    ApiInterfaceImpl(
      coroutineScope = coroutineScope,
      platformUtilities = platformUtilities
    )
  }
  override val api: ApiInterface = _api

  override fun tryLoadClient(token: String?) {
    if(token == null || api.initComplete.value)
      return
    api.loadClient(token, settings)
  }

  private val navigator = StackNavigation<Config>()

  init {
    if (!initialSettings.stealthModeEnabled && !initialSettings.tokenIsProtected && initialSettings.token != null) {
      tryLoadClient(initialSettings.token)
    }
    coroutineScope.launch {
      platformEventFlow.collect { event ->
        when (event) {
          is PlatformEvent.ExternallyHandleable -> {
            event.handle(platformUtilities)
          }

          is PlatformEvent.LoginTokenReceived -> {
            if (settings.data.value.token != null)
              return@collect
            settings.setToken(event.token)

            val newConfig: Config = if(settings.data.value.hasViewedOnboarding) Config.MainApp() else Config.Onboarding
            navigator.replaceAll(newConfig)
          }

          else -> Unit
        }
      }
    }
  }

  private val _stack =
    childStack(
      source = navigator,
      serializer = Config.serializer(),
      initialStack = { getInitialStack(initialSettings, deepLinkURL) },
      handleBackButton = false,
      childFactory = ::child,
    )

  override val stack: Value<ChildStack<*, RootComponent.Child>> = _stack

  private fun getInitialStack(initialSettings: Settings, deepLinkURL: String?): List<Config> {
    return listOf(when {
      initialSettings.token == null -> Config.Login
      initialSettings.stealthModeEnabled -> Config.StealthApp
      initialSettings.tokenIsProtected -> Config.PINEntry
      !initialSettings.hasViewedOnboarding -> Config.Onboarding
      else -> Config.MainApp(deepLinkURL)
    })
  }

  private fun child(config: Config, componentContext: ComponentContext): RootComponent.Child {
    val newComponentContext =
      CommonComponentContextImpl(componentContext, api, settings, platformUtilities, coroutineContext)
    return when (config) {
      is Config.MainApp ->
        RootComponent.Child.MainAppChild(
          MainAppComponentImpl(
            componentContext = newComponentContext,
            navigateToLoginScreen = { navigator.replaceAll(Config.Login) }
          )
        )

      Config.Login ->
        RootComponent.Child.LoginChild(
          LoginComponentImpl(
            componentContext = newComponentContext
          )
        )

      Config.Onboarding ->
        RootComponent.Child.OnboardingChild(
          OnboardingComponentImpl(
            componentContext = newComponentContext,
            navigateToMainAppFun = {
              tryLoadClient(settings.data.value.token!!)
              navigator.replaceAll(Config.MainApp())
            }
          )
        )

      Config.StealthApp ->
        RootComponent.Child.StealthAppChild(
          StealthAppComponentImpl(
            componentContext = newComponentContext,
            navigateToPINEntry = { navigator.replaceAll(Config.PINEntry) },
            navigateToMainApp = {
              tryLoadClient(it)
              navigator.replaceAll(Config.MainApp())
            }
          )
        )

      Config.PINEntry ->
        RootComponent.Child.PINEntryChild(
          PINEntryComponentImpl(
            componentContext = newComponentContext,
            navigateToMainApp = {
              tryLoadClient(it)
              navigator.replaceAll(Config.MainApp())
            }
          )
        )
    }
  }

//  override fun onBackClicked() {
//    navigator.pop()
//  }
//
//  override fun onBackClicked(toIndex: Int) {
//    navigator.popTo(index = toIndex)
//  }

  @Serializable
  private sealed interface Config {
    @Serializable
    data class MainApp(val deepLinkURL: String? = null) : Config

    @Serializable
    data object Login : Config

    @Serializable
    data object Onboarding : Config

    @Serializable
    data object StealthApp : Config

    @Serializable
    data object PINEntry : Config
  }
}