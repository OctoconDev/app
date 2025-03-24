package app.octocon.app.ui.model.main.settings

import app.octocon.app.ui.model.CommonInterface
import app.octocon.app.ui.model.MainComponentContext

interface SettingsSecurityComponent : CommonInterface {
  fun navigateBack()
}

class SettingsSecurityComponentImpl(
  componentContext: MainComponentContext,
  val popSelf: () -> Unit
) : SettingsSecurityComponent, MainComponentContext by componentContext {
  override fun navigateBack() = popSelf()
}