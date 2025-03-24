package app.octocon.app.ui.model.main.hometabs.friends

import app.octocon.app.ui.model.CommonInterface
import app.octocon.app.ui.model.MainComponentContext

interface FriendViewComponent : CommonInterface {
  val friendID: String

  fun navigateBack()
  fun navigateToFriendTagView(tagID: String)
  fun navigateToFriendAlterView(alterID: Int)
}

class FriendViewComponentImpl(
  componentContext: MainComponentContext,
  private val popSelf: () -> Unit,
  private val navigateToFriendTagViewFun: (String) -> Unit,
  private val navigateToFriendAlterViewFun: (Int) -> Unit,
  override val friendID: String
) : FriendViewComponent, MainComponentContext by componentContext {
  override fun navigateBack() {
    popSelf()
  }

  override fun navigateToFriendTagView(tagID: String) {
    navigateToFriendTagViewFun(tagID)
  }

  override fun navigateToFriendAlterView(alterID: Int) {
    navigateToFriendAlterViewFun(alterID)
  }
}