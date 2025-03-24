package app.octocon.app.ui.model.onboarding.pages

import app.octocon.app.ui.model.CommonComponentContext

interface OnboardingWelcomeComponent

class OnboardingWelcomeComponentImpl(
  componentContext: CommonComponentContext
) : OnboardingWelcomeComponent, CommonComponentContext by componentContext