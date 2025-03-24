package app.octocon.app.ui.model.main.hometabs.alters.alterview

import app.octocon.app.ui.model.CommonInterface
import app.octocon.app.ui.model.MainComponentContext
import app.octocon.app.ui.model.main.hometabs.alters.AlterViewComponent

interface AlterViewFieldsComponent : CommonInterface {
  val model: AlterViewComponent.Model

  fun navigateToCustomFields()
}

class AlterViewFieldsComponentImpl(
  componentContext: MainComponentContext,
  override val model: AlterViewComponent.Model,
  private val navigateToCustomFieldsFun: () -> Unit
) : AlterViewFieldsComponent, MainComponentContext by componentContext {
  override fun navigateToCustomFields() = navigateToCustomFieldsFun()
}