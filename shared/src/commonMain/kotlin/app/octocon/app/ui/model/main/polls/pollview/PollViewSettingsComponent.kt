package app.octocon.app.ui.model.main.polls.pollview

import app.octocon.app.api.model.ChoicePoll
import app.octocon.app.ui.model.CommonInterface
import app.octocon.app.ui.model.MainComponentContext
import app.octocon.app.ui.model.main.polls.PollViewComponent

interface PollViewSettingsComponent : CommonInterface {
  val model: PollViewComponent.Model

  val isChoicePoll: Boolean

  fun openCreateChoiceDialog()
  fun updateOpenCreateChoiceDialog(openCreateChoiceDialog: (Boolean) -> Unit)
}

class PollViewSettingsComponentImpl(
  componentContext: MainComponentContext,
  override val model: PollViewComponent.Model
) : PollViewSettingsComponent, MainComponentContext by componentContext {
  private var openCreateChoiceDialogFun: ((Boolean) -> Unit)? = null

  override fun openCreateChoiceDialog() {
    openCreateChoiceDialogFun?.invoke(true)
  }

  override val isChoicePoll: Boolean
    get() = model.initialPoll.value is ChoicePoll

  override fun updateOpenCreateChoiceDialog(openCreateChoiceDialog: (Boolean) -> Unit) {
    openCreateChoiceDialogFun = openCreateChoiceDialog
  }
}