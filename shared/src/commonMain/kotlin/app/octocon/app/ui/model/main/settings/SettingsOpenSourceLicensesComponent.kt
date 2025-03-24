package app.octocon.app.ui.model.main.settings

import app.octocon.app.ui.model.CommonInterface
import app.octocon.app.ui.model.MainComponentContext

interface SettingsOpenSourceLicensesComponent : CommonInterface {
  fun navigateBack()
}

class SettingsOpenSourceLicensesComponentImpl(
  componentContext: MainComponentContext,
  val popSelf: () -> Unit
) : SettingsOpenSourceLicensesComponent, MainComponentContext by componentContext {
  override fun navigateBack() = popSelf()
}