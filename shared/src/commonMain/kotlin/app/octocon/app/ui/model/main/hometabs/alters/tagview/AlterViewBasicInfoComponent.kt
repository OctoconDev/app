package app.octocon.app.ui.model.main.hometabs.alters.tagview

import app.octocon.app.ui.model.CommonInterface
import app.octocon.app.ui.model.MainComponentContext
import app.octocon.app.ui.model.main.hometabs.alters.TagViewComponent

interface TagViewContentsComponent : CommonInterface {
  val model: TagViewComponent.Model

  fun attachAlter(alterID: Int)
  fun detachAlter(alterID: Int)

  fun navigateToAlterView(alterID: Int)
  fun navigateToTagView(tagID: String)

  fun openAddAlterDialog()
  fun updateOpenAddAlterDialogFun(openAddAlterDialog: (Boolean) -> Unit)
}

class TagViewContentsComponentImpl(
  componentContext: MainComponentContext,
  private val navigateToAlterViewFun: (Int) -> Unit,
  private val navigateToTagViewFun: (String) -> Unit,
  override val model: TagViewComponent.Model
) : TagViewContentsComponent, MainComponentContext by componentContext {
  override fun attachAlter(alterID: Int) {
    api.attachAlterToTag(model.id, alterID)
  }

  override fun detachAlter(alterID: Int) {
    api.detachAlterFromTag(model.id, alterID)
  }

  override fun navigateToAlterView(alterID: Int) {
    navigateToAlterViewFun(alterID)
  }

  override fun navigateToTagView(tagID: String) {
    navigateToTagViewFun(tagID)
  }

  private var openAddAlterDialogFun: ((Boolean) -> Unit)? = null

  override fun openAddAlterDialog() {
    openAddAlterDialogFun?.invoke(true)
  }

  override fun updateOpenAddAlterDialogFun(openAddAlterDialog: (Boolean) -> Unit) {
    openAddAlterDialogFun = openAddAlterDialog
  }
}