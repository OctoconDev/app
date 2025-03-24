package app.octocon.app.ui.model.main.settings

import app.octocon.app.ui.model.CommonInterface
import app.octocon.app.ui.model.MainComponentContext

interface SettingsCustomFieldsComponent : CommonInterface {
  fun navigateBack()
}

class SettingsCustomFieldsComponentImpl(
  componentContext: MainComponentContext,
  val popSelf: () -> Unit
) : SettingsCustomFieldsComponent, MainComponentContext by componentContext {
  override fun navigateBack() = popSelf()
}