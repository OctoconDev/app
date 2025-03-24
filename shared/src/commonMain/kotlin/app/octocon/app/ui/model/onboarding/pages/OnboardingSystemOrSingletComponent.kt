package app.octocon.app.ui.model.onboarding.pages

import app.octocon.app.ui.model.CommonComponentContext

interface OnboardingSystemOrSingletComponent {
  fun navigateToNextPage()
  fun navigateToMainApp()
}

class OnboardingSystemOrSingletComponentImpl(
  componentContext: CommonComponentContext,
  private val navigateToNextPageFun: () -> Unit,
  private val navigateToMainAppFun: () -> Unit
) : OnboardingSystemOrSingletComponent, CommonComponentContext by componentContext {
  override fun navigateToNextPage() = navigateToNextPageFun()
  override fun navigateToMainApp() {
    settings.setIsSinglet(true)
    navigateToMainAppFun()
  }
}