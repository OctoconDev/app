package app.octocon.app.ui.model.main.settings

import app.octocon.app.ui.model.CommonInterface
import app.octocon.app.ui.model.MainComponentContext

interface SettingsRootComponent : CommonInterface {
  fun navigateToAppearance()
  fun navigateToAccessibility()
  fun navigateToSecurity()
  fun navigateToCustomFields()
  fun navigateToOpenSourceLicenses()

  fun logOut()
}

class SettingsRootComponentImpl(
  componentContext: MainComponentContext,
  val navigateToAppearanceFun: () -> Unit,
  val navigateToAccessibilityFun: () -> Unit,
  val navigateToSecurityFun: () -> Unit,
  val navigateToCustomFieldsFun: () -> Unit,
  val navigateToOpenSourceLicensesFun: () -> Unit,
  private val navigateToLoginScreenFun: () -> Unit
) : SettingsRootComponent, MainComponentContext by componentContext {
  override fun navigateToAppearance() = navigateToAppearanceFun()
  override fun navigateToAccessibility() = navigateToAccessibilityFun()
  override fun navigateToSecurity() = navigateToSecurityFun()
  override fun navigateToCustomFields() = navigateToCustomFieldsFun()
  override fun navigateToOpenSourceLicenses() = navigateToOpenSourceLicensesFun()

  override fun logOut() {
    api.logOut()
    settings.nukeEverything()

    navigateToLoginScreenFun()
  }
}