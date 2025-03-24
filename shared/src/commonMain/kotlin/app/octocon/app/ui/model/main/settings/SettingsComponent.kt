package app.octocon.app.ui.model.main.settings

import app.octocon.app.ui.model.CommonInterface
import app.octocon.app.ui.model.MainComponentContext
import app.octocon.app.ui.model.main.settings.SettingsComponent.DetailsChild
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.router.panels.ChildPanels
import com.arkivanov.decompose.router.panels.ChildPanelsMode
import com.arkivanov.decompose.router.panels.Panels
import com.arkivanov.decompose.router.panels.PanelsNavigation
import com.arkivanov.decompose.router.panels.activateDetails
import com.arkivanov.decompose.router.panels.childPanels
import com.arkivanov.decompose.router.panels.pop
import com.arkivanov.decompose.router.panels.setMode
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackHandlerOwner
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer

@OptIn(ExperimentalDecomposeApi::class)
interface SettingsComponent : CommonInterface, BackHandlerOwner {
  val panels: Value<ChildPanels<*, SettingsRootComponent, *, DetailsChild, Nothing, Nothing>>

  fun navigateToAppearance()
  fun navigateToAccessibility()
  fun navigateToSecurity()
  fun navigateToCustomFields()
  fun navigateToOpenSourceLicenses()

  fun onBackPressed()

  fun setMode(mode: ChildPanelsMode)

  sealed interface DetailsChild {
    class SettingsAppearanceChild(val component: SettingsAppearanceComponent) : DetailsChild
    class SettingsAccessibilityChild(val component: SettingsAccessibilityComponent) : DetailsChild
    class SettingsSecurityChild(val component: SettingsSecurityComponent) : DetailsChild
    class SettingsCustomFieldsChild(val component: SettingsCustomFieldsComponent) : DetailsChild
    class SettingsOpenSourceLicensesChild(val component: SettingsOpenSourceLicensesComponent) : DetailsChild
  }
}

@OptIn(ExperimentalDecomposeApi::class)
class SettingsComponentImpl(
  componentContext: MainComponentContext,
  private val navigateToLoginScreen: () -> Unit
) : SettingsComponent, MainComponentContext by componentContext {
  private val navigator = PanelsNavigation<Unit, DetailsConfig, Nothing>()

  private val _panels =
    childPanels(
      source = navigator,
      serializers = Unit.serializer() to DetailsConfig.serializer(),
      initialPanels = { Panels(main = Unit) },
      mainFactory = { _, componentContext ->
        SettingsRootComponentImpl(
          componentContext = componentContext,
          navigateToAppearanceFun = ::navigateToAppearance,
          navigateToAccessibilityFun = ::navigateToAccessibility,
          navigateToSecurityFun = ::navigateToSecurity,
          navigateToCustomFieldsFun = ::navigateToCustomFields,
          navigateToOpenSourceLicensesFun = ::navigateToOpenSourceLicenses,
          navigateToLoginScreenFun = navigateToLoginScreen
        )
      },
      detailsFactory = ::detailsChild
    )

  override val panels: Value<ChildPanels<*, SettingsRootComponent, *, DetailsChild, Nothing, Nothing>> = _panels

  override fun navigateToAppearance() = navigator.activateDetails(DetailsConfig.Appearance)
  override fun navigateToAccessibility() = navigator.activateDetails(DetailsConfig.Accessibility)
  override fun navigateToSecurity() = navigator.activateDetails(DetailsConfig.Security)
  override fun navigateToCustomFields() = navigator.activateDetails(DetailsConfig.CustomFields)
  override fun navigateToOpenSourceLicenses() = navigator.activateDetails(DetailsConfig.OpenSourceLicenses)

  override fun onBackPressed() = navigator.pop()

  override fun setMode(mode: ChildPanelsMode) = navigator.setMode(mode)

  private fun detailsChild(config: DetailsConfig, componentContext: MainComponentContext): DetailsChild {
    return when (config) {
      is DetailsConfig.Appearance ->
        DetailsChild.SettingsAppearanceChild(
          SettingsAppearanceComponentImpl(
            componentContext = componentContext,
            popSelf = navigator::pop
          )
        )

      is DetailsConfig.Accessibility ->
        DetailsChild.SettingsAccessibilityChild(
          SettingsAccessibilityComponentImpl(
            componentContext = componentContext,
            popSelf = navigator::pop
          )
        )

      is DetailsConfig.Security ->
        DetailsChild.SettingsSecurityChild(
          SettingsSecurityComponentImpl(
            componentContext = componentContext,
            popSelf = navigator::pop
          )
        )

      is DetailsConfig.CustomFields ->
        DetailsChild.SettingsCustomFieldsChild(
          SettingsCustomFieldsComponentImpl(
            componentContext = componentContext,
            popSelf = navigator::pop
          )
        )

      is DetailsConfig.OpenSourceLicenses ->
        DetailsChild.SettingsOpenSourceLicensesChild(
          SettingsOpenSourceLicensesComponentImpl(
            componentContext = componentContext,
            popSelf = navigator::pop
          )
        )
    }
  }

  @Serializable
  private sealed interface DetailsConfig {
    @Serializable
    data object Appearance : DetailsConfig

    @Serializable
    data object Accessibility : DetailsConfig

    @Serializable
    data object Security : DetailsConfig

    @Serializable
    data object CustomFields : DetailsConfig

    @Serializable
    data object OpenSourceLicenses : DetailsConfig
  }
}