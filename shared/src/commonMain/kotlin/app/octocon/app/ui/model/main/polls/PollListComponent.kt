package app.octocon.app.ui.model.main.polls

import app.octocon.app.ui.model.CommonInterface
import app.octocon.app.ui.model.MainComponentContext

interface PollListComponent : CommonInterface {
  fun navigateToPollView(pollID: String)
}

class PollListComponentImpl(
  componentContext: MainComponentContext,
  val navigateToPollViewFun: (String) -> Unit
) : PollListComponent, MainComponentContext by componentContext {
  override fun navigateToPollView(pollID: String) {
    navigateToPollViewFun(pollID)
  }
}