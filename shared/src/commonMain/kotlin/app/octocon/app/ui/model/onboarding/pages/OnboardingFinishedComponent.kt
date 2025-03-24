package app.octocon.app.ui.model.onboarding.pages

import app.octocon.app.ui.model.CommonComponentContext
import app.octocon.app.utils.ColorSchemeParams
import app.octocon.app.utils.WebURLOpenBehavior

interface OnboardingFinishedComponent {
  fun openURL(url: String, colorSchemeParams: ColorSchemeParams, webURLOpenBehavior: WebURLOpenBehavior = WebURLOpenBehavior.NewTab)
}

class OnboardingFinishedComponentImpl(
  componentContext: CommonComponentContext
) : OnboardingFinishedComponent, CommonComponentContext by componentContext {
  override fun openURL(
    url: String,
    colorSchemeParams: ColorSchemeParams,
    webURLOpenBehavior: WebURLOpenBehavior
  ) {
    platformUtilities.openURL(url, colorSchemeParams, webURLOpenBehavior)
  }
}