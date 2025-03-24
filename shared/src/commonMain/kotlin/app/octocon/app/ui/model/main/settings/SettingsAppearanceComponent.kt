package app.octocon.app.ui.model.main.settings

import app.octocon.app.ui.model.CommonInterface
import app.octocon.app.ui.model.MainComponentContext

interface SettingsAppearanceComponent : CommonInterface {
  fun navigateBack()
}

class SettingsAppearanceComponentImpl(
  componentContext: MainComponentContext,
  val popSelf: () -> Unit
) : SettingsAppearanceComponent, MainComponentContext by componentContext {
  override fun navigateBack() = popSelf()
}