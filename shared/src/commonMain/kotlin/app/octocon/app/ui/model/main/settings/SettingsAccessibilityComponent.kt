package app.octocon.app.ui.model.main.settings

import app.octocon.app.ui.model.CommonInterface
import app.octocon.app.ui.model.MainComponentContext

interface SettingsAccessibilityComponent : CommonInterface {
  fun navigateBack()
}

class SettingsAccessibilityComponentImpl(
  componentContext: MainComponentContext,
  val popSelf: () -> Unit
) : SettingsAccessibilityComponent, MainComponentContext by componentContext {
  override fun navigateBack() = popSelf()
}