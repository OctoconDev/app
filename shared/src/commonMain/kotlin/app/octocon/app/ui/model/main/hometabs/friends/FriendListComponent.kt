package app.octocon.app.ui.model.main.hometabs.friends

import app.octocon.app.ui.model.CommonInterface
import app.octocon.app.ui.model.MainComponentContext

interface FriendListComponent : CommonInterface {
  fun navigateToFriendView(friendID: String)
  fun navigateToFriendAlterView(friendID: String, alterID: Int)
}

class FriendListComponentImpl(
  componentContext: MainComponentContext,
  val navigateToFriendViewFun: (String) -> Unit,
  val navigateToFriendAlterViewFun: (String, Int) -> Unit
) : FriendListComponent, MainComponentContext by componentContext {
  override fun navigateToFriendView(friendID: String) {
    navigateToFriendViewFun(friendID)
  }

  override fun navigateToFriendAlterView(friendID: String, alterID: Int) {
    navigateToFriendAlterViewFun(friendID, alterID)
  }
}