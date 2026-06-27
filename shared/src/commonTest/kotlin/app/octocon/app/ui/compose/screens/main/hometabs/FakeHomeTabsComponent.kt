package app.octocon.app.ui.compose.screens.main.hometabs

import app.octocon.app.Settings
import app.octocon.app.api.APIState
import app.octocon.app.api.model.MyAlter
import app.octocon.app.ui.model.interfaces.SettingsInterface
import app.octocon.app.ui.model.main.hometabs.AltersComponent
import app.octocon.app.ui.model.main.hometabs.AltersDetailStackComponent
import app.octocon.app.ui.model.main.hometabs.FriendsComponent
import app.octocon.app.ui.model.main.hometabs.FrontHistoryComponent
import app.octocon.app.ui.model.main.hometabs.HomeTabsComponent
import app.octocon.app.ui.model.main.hometabs.JournalComponent
import app.octocon.app.ui.model.main.hometabs.alters.AlterListComponent
import app.octocon.app.ui.model.main.hometabs.friends.FriendListComponent
import app.octocon.app.ui.model.main.hometabs.friends.FriendViewComponent
import app.octocon.app.ui.model.main.hometabs.journal.JournalEntryListComponent
import app.octocon.app.ui.model.main.hometabs.journal.JournalEntryViewComponent
import app.octocon.app.utils.MonthYearPair
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.router.panels.ChildPanels
import com.arkivanov.decompose.router.panels.ChildPanelsMode
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackDispatcher
import com.arkivanov.essenty.backhandler.BackHandler
import kotlinx.coroutines.flow.StateFlow

/**
 * Hand-rolled test double of [HomeTabsComponent] suitable for scaffold-only tests.
 *
 * The fake exposes a mutable [ChildStack] so tests can drive which tab is "active",
 * and counts each `navigateTo*` call so tests can assert intent. The inner tab
 * components are stub instances whose every member throws — `BottomBar` and
 * `NavigationRail` only do `is Child.X` type checks, so the stubs are never read.
 */
class FakeHomeTabsComponent(
  initialSettings: Settings = Settings(),
  initialActiveChild: ActiveChild = ActiveChild.Alters
) : HomeTabsComponent {

  val fakeSettings: FakeSettingsInterface = FakeSettingsInterface(initialSettings)
  override val settings: SettingsInterface = fakeSettings

  private val _stack: MutableValue<ChildStack<Any, HomeTabsComponent.Child>> =
    MutableValue(buildStack(initialActiveChild))
  override val stack: Value<ChildStack<*, HomeTabsComponent.Child>> = _stack

  var navigateToAltersCalls: Int = 0
    private set
  var navigateToHistoryCalls: Int = 0
    private set
  var navigateToFriendsCalls: Int = 0
    private set
  var navigateToJournalCalls: Int = 0
    private set

  override fun navigateToAlters() {
    navigateToAltersCalls++
  }

  override fun navigateToHistory() {
    navigateToHistoryCalls++
  }

  override fun navigateToFriends() {
    navigateToFriendsCalls++
  }

  override fun navigateToJournal() {
    navigateToJournalCalls++
  }

  override var onCurrentTabPressed: (() -> Unit)? = null
    private set

  override fun updateOnCurrentTabPressed(onCurrentTabPressed: () -> Unit) {
    this.onCurrentTabPressed = onCurrentTabPressed
  }

  /** Updates which child the [stack] reports as active, e.g. between assertions. */
  fun setActiveChild(active: ActiveChild) {
    _stack.value = buildStack(active)
  }

  private fun buildStack(active: ActiveChild): ChildStack<Any, HomeTabsComponent.Child> =
    ChildStack(
      configuration = active,
      instance = active.toChild()
    )

  enum class ActiveChild {
    Alters, History, Journal, Friends;

    internal fun toChild(): HomeTabsComponent.Child = when (this) {
      Alters -> HomeTabsComponent.Child.AltersChild(StubAltersComponent)
      History -> HomeTabsComponent.Child.FrontHistoryChild(StubFrontHistoryComponent)
      Journal -> HomeTabsComponent.Child.JournalChild(StubJournalComponent)
      Friends -> HomeTabsComponent.Child.FriendsChild(StubFriendsComponent)
    }
  }
}

private val stubBackHandler: BackHandler = BackDispatcher()

private fun unused(): Nothing =
  error("Inner tab component is not used in scaffold-only tests")

@OptIn(ExperimentalDecomposeApi::class)
private object StubAltersComponent : AltersComponent {
  override val settings: SettingsInterface get() = unused()
  override val panels: Value<ChildPanels<*, AlterListComponent, *, AltersDetailStackComponent, *, AltersComponent.ExtraChild>>
    get() = unused()

  override val backHandler: BackHandler = stubBackHandler

  override fun replaceWithAlterView(alterID: Int) = unused()
  override fun replaceWithTagView(tagID: String) = unused()
  override fun activateAlterJournalEntry(alterID: Int, entryID: String, alterColor: String?) = unused()
  override fun onBackPressed() = unused()
  override fun setMode(mode: ChildPanelsMode) = unused()
}

private object StubFrontHistoryComponent : FrontHistoryComponent {
  override val settings: SettingsInterface get() = unused()
  override val alters: StateFlow<APIState<List<MyAlter>>> get() = unused()
  override val frontHistory get() = unused()

  override fun deleteFront(frontID: String) = unused()
  override fun loadFrontHistory(monthYearPair: MonthYearPair) = unused()
}

@OptIn(ExperimentalDecomposeApi::class)
private object StubFriendsComponent : FriendsComponent {
  override val settings: SettingsInterface get() = unused()
  override val panels: Value<ChildPanels<*, FriendListComponent, *, FriendViewComponent, *, FriendsComponent.ExtraChild>>
    get() = unused()

  override val backHandler: BackHandler = stubBackHandler

  override fun navigateToFriendView(friendID: String) = unused()
  override fun navigateToFriendTagView(friendID: String, tagID: String) = unused()
  override fun navigateToFriendAlterView(friendID: String, alterID: Int) = unused()
  override fun onBackPressed() = unused()
  override fun setMode(mode: ChildPanelsMode) = unused()
}

@OptIn(ExperimentalDecomposeApi::class)
private object StubJournalComponent : JournalComponent {
  override val settings: SettingsInterface get() = unused()
  override val panels: Value<ChildPanels<*, JournalEntryListComponent, *, JournalEntryViewComponent, Nothing, Nothing>>
    get() = unused()

  override val backHandler: BackHandler = stubBackHandler

  override fun navigateToJournalEntryView(entryID: String) = unused()
  override fun onBackPressed() = unused()
  override fun setMode(mode: ChildPanelsMode) = unused()
}
