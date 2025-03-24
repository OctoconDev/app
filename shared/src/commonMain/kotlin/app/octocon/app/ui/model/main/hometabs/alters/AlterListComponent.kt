package app.octocon.app.ui.model.main.hometabs.alters

import app.octocon.app.ui.model.CommonInterface
import app.octocon.app.ui.model.MainComponentContext

interface AlterListComponent : CommonInterface {
  fun navigateToAlterView(alterID: Int)
  fun navigateToTagView(tagID: String)
}

class AlterListComponentImpl(
  componentContext: MainComponentContext,
  val navigateToAlterViewFun: (Int) -> Unit,
  val navigateToTagViewFun: (String) -> Unit
) : AlterListComponent, MainComponentContext by componentContext {

  override fun navigateToAlterView(alterID: Int) {
    navigateToAlterViewFun(alterID)
  }

  override fun navigateToTagView(tagID: String) {
    navigateToTagViewFun(tagID)
  }
}