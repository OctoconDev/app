package app.octocon.app.ui.model.main.resources

import app.octocon.app.ui.model.MainComponentContext
import app.octocon.app.ui.model.interfaces.SettingsInterface
import app.octocon.app.utils.ColorSchemeParams

interface ResourcesComponent {
  val settings: SettingsInterface

  fun openResource(url: String, colorSchemeParams: ColorSchemeParams)
}

class ResourcesComponentImpl(
  componentContext: MainComponentContext
) : ResourcesComponent, MainComponentContext by componentContext {
  override fun openResource(url: String, colorSchemeParams: ColorSchemeParams) {
    platformUtilities.openURL(url, colorSchemeParams)
  }
}