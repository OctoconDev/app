package app.octocon.app.ui.model.main.polls.pollview

import app.octocon.app.ui.model.CommonInterface
import app.octocon.app.ui.model.MainComponentContext
import app.octocon.app.ui.model.main.polls.PollViewComponent

interface PollViewResultsComponent : CommonInterface {
  val model: PollViewComponent.Model

  fun openCastVoteDialog()
  fun updateOpenCastVoteDialog(openCastVoteDialog: (Boolean) -> Unit)
}

class PollViewResultsComponentImpl(
  componentContext: MainComponentContext,
  override val model: PollViewComponent.Model
) : PollViewResultsComponent, MainComponentContext by componentContext {
  private var openCastVoteDialogFun: ((Boolean) -> Unit)? = null

  override fun openCastVoteDialog() {
    openCastVoteDialogFun?.invoke(true)
  }

  override fun updateOpenCastVoteDialog(openCastVoteDialog: (Boolean) -> Unit) {
    openCastVoteDialogFun = openCastVoteDialog
  }
}