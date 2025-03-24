package app.octocon.app.ui.model.onboarding

import app.octocon.app.ui.model.CommonComponentContext
import app.octocon.app.ui.model.interfaces.SettingsInterface
import app.octocon.app.ui.model.onboarding.pages.OnboardingFinishedComponent
import app.octocon.app.ui.model.onboarding.pages.OnboardingFinishedComponentImpl
import app.octocon.app.ui.model.onboarding.pages.OnboardingFrontTutorialComponent
import app.octocon.app.ui.model.onboarding.pages.OnboardingFrontTutorialComponentImpl
import app.octocon.app.ui.model.onboarding.pages.OnboardingSystemOrSingletComponent
import app.octocon.app.ui.model.onboarding.pages.OnboardingSystemOrSingletComponentImpl
import app.octocon.app.ui.model.onboarding.pages.OnboardingWelcomeComponent
import app.octocon.app.ui.model.onboarding.pages.OnboardingWelcomeComponentImpl
import app.octocon.app.ui.registerStateHandler
import app.octocon.app.ui.retainStateHandler
import com.arkivanov.decompose.router.pages.ChildPages
import com.arkivanov.decompose.router.pages.Pages
import com.arkivanov.decompose.router.pages.PagesNavigation
import com.arkivanov.decompose.router.pages.childPages
import com.arkivanov.decompose.router.pages.select
import com.arkivanov.decompose.router.pages.selectNext
import com.arkivanov.decompose.router.pages.selectPrev
import com.arkivanov.decompose.value.Value
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable

interface OnboardingComponent {
  val settings: SettingsInterface

  val model: StateFlow<Model>
  val pages: Value<ChildPages<*, Page>>

  fun navigateToNextPage()
  fun navigateToPreviousPage()
  fun navigateToPage(index: Int)

  fun navigateToMainApp()

  sealed interface Page {
    class WelcomePage(val component: OnboardingWelcomeComponent): Page
    class SystemOrSingletPage(val component: OnboardingSystemOrSingletComponent) : Page
    class FrontTutorialPage(val component: OnboardingFrontTutorialComponent): Page
    class FinishedPage(val component: OnboardingFinishedComponent): Page
  }

  @Serializable
  data class Model(
    val previousButtonEnabled: Boolean = false,
    val nextButtonEnabled: Boolean = true
  )
}

class OnboardingComponentImpl(
  componentContext: CommonComponentContext,
  private val navigateToMainAppFun: () -> Unit
) : OnboardingComponent, CommonComponentContext by componentContext {
  private val handler = retainStateHandler { OnboardingComponent.Model() }
  init {
    registerStateHandler(handler)
  }
  override val model = handler.model

  private val navigator = PagesNavigation<Config>()

  private val _pages =
    childPages(
      source = navigator,
      serializer = Config.serializer(),
      initialPages = {
        Pages(
          listOf(Config.Welcome, Config.SystemOrSinglet, Config.FrontTutorial, Config.Finished),
          0
        )
      },
      handleBackButton = false,
      childFactory = ::page,
    )

  override val pages = _pages

  private fun page(config: Config, componentContext: CommonComponentContext): OnboardingComponent.Page {
    return when(config) {
      Config.Welcome -> OnboardingComponent.Page.WelcomePage(
        OnboardingWelcomeComponentImpl(componentContext = componentContext)
      )

      Config.SystemOrSinglet -> OnboardingComponent.Page.SystemOrSingletPage(
        OnboardingSystemOrSingletComponentImpl(
          componentContext = componentContext,
          navigateToMainAppFun = ::navigateToMainApp,
          navigateToNextPageFun = ::navigateToNextPage
        )
      )

      Config.FrontTutorial -> OnboardingComponent.Page.FrontTutorialPage(
        OnboardingFrontTutorialComponentImpl(componentContext = componentContext)
      )

      Config.Finished -> OnboardingComponent.Page.FinishedPage(
        OnboardingFinishedComponentImpl(componentContext = componentContext)
      )
    }
  }

  override fun navigateToPreviousPage() = navigator.selectPrev()

  override fun navigateToNextPage() = navigator.selectNext()

  override fun navigateToPage(index: Int) {
    when(index) {
      0 -> {
        handler.model.tryEmit(
          OnboardingComponent.Model(
            previousButtonEnabled = false,
            nextButtonEnabled = true
          )
        )
      }
      _pages.value.items.size - 1 -> {
        handler.model.tryEmit(
          OnboardingComponent.Model(
            nextButtonEnabled = false,
            previousButtonEnabled = true
          )
        )
      }
      else -> {
        handler.model.tryEmit(
          OnboardingComponent.Model(
            nextButtonEnabled = true,
            previousButtonEnabled = true
          )
        )
      }
    }
    navigator.select(index = index)
  }

  override fun navigateToMainApp() {
    settings.setHasViewedOnboarding(true)
    navigateToMainAppFun()
  }

  @Serializable
  sealed interface Config {
    @Serializable
    data object Welcome: Config
    @Serializable
    data object SystemOrSinglet: Config
    @Serializable
    data object FrontTutorial: Config
    @Serializable
    data object Finished: Config
  }
}