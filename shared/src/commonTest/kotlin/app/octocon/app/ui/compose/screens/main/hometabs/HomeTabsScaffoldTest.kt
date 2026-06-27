package app.octocon.app.ui.compose.screens.main.hometabs

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import app.octocon.app.Settings
import app.octocon.app.ui.compose.LocalNavigationType
import app.octocon.app.ui.compose.NavigationType
import app.octocon.app.ui.compose.theme.LocalOctoShapes
import app.octocon.app.ui.compose.theme.LocalOctoTypography
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Scaffold-only UI tests for [HomeTabsScreen]'s `BottomBar` and `NavigationRail`.
 *
 * These tests render the two bar composables in isolation against a hand-rolled
 * [FakeHomeTabsComponent]; they never compose the surrounding `HomeTabsScreen`
 * (which would render real tab bodies via `ChildStack`) so there's no need for
 * the inner tab components to do anything useful.
 *
 * The eight cases below cover the four guarantees the bar contracts make:
 *  - all four tabs render when the system is not a singlet
 *  - the bar highlights the tab matching the active `Child` reported by the component
 *  - tapping a non-selected tab fires the matching `navigateTo*` callback
 *  - tapping the already-selected tab does NOT fire `navigateTo*`
 *
 * Plus two singlet-vs-multi initial-tab cases at the bottom-bar level.
 */
@OptIn(ExperimentalTestApi::class)
class HomeTabsScaffoldTest {

  // -------- BottomBar ------------------------------------------------------------

  @Test
  fun bottomBar_rendersAllFourTabs_whenMultiSystem() = runComposeUiTest {
    val fake = newFake(activeChild = FakeHomeTabsComponent.ActiveChild.Alters)

    setContent { TestHomeTabsHost { TestBottomBar(fake) } }

    onNodeWithTag(HomeTabsTestTags.BOTTOM_BAR).assertExists()
    onNodeWithTag(HomeTabsTestTags.TAB_ALTERS).assertExists()
    onNodeWithTag(HomeTabsTestTags.TAB_HISTORY).assertExists()
    onNodeWithTag(HomeTabsTestTags.TAB_JOURNAL).assertExists()
    onNodeWithTag(HomeTabsTestTags.TAB_FRIENDS).assertExists()
  }

  @Test
  fun bottomBar_marksAltersSelected_whenCurrentScreenIsAltersChild() = runComposeUiTest {
    val fake = newFake(activeChild = FakeHomeTabsComponent.ActiveChild.Alters)

    setContent { TestHomeTabsHost { TestBottomBar(fake) } }

    onNodeWithTag(HomeTabsTestTags.TAB_ALTERS).assertIsSelected()
  }

  @Test
  fun bottomBar_marksFriendsSelected_whenCurrentScreenIsFriendsChild() = runComposeUiTest {
    val fake = newFake(activeChild = FakeHomeTabsComponent.ActiveChild.Friends)

    setContent { TestHomeTabsHost { TestBottomBar(fake) } }

    onNodeWithTag(HomeTabsTestTags.TAB_FRIENDS).assertIsSelected()
  }

  @Test
  fun bottomBar_tappingNonSelectedTab_invokesNavigate() = runComposeUiTest {
    val fake = newFake(activeChild = FakeHomeTabsComponent.ActiveChild.Alters)

    setContent { TestHomeTabsHost { TestBottomBar(fake) } }

    onNodeWithTag(HomeTabsTestTags.TAB_JOURNAL).performClick()

    assertEquals(1, fake.navigateToJournalCalls)
    assertEquals(0, fake.navigateToAltersCalls)
    assertEquals(0, fake.navigateToHistoryCalls)
    assertEquals(0, fake.navigateToFriendsCalls)
  }

  @Test
  fun bottomBar_tappingSelectedTab_doesNotInvokeNavigate() = runComposeUiTest {
    val fake = newFake(activeChild = FakeHomeTabsComponent.ActiveChild.Alters)

    setContent { TestHomeTabsHost { TestBottomBar(fake) } }

    onNodeWithTag(HomeTabsTestTags.TAB_ALTERS).performClick()

    assertEquals(0, fake.navigateToAltersCalls)
  }

  // -------- NavigationRail -------------------------------------------------------

  @Test
  fun navigationRail_rendersAllFourTabs_whenMultiSystem() = runComposeUiTest {
    val fake = newFake(activeChild = FakeHomeTabsComponent.ActiveChild.Alters)

    setContent { TestHomeTabsHost { TestNavigationRail(fake) } }

    onNodeWithTag(HomeTabsTestTags.NAVIGATION_RAIL).assertExists()
    onNodeWithTag(HomeTabsTestTags.TAB_ALTERS).assertExists()
    onNodeWithTag(HomeTabsTestTags.TAB_HISTORY).assertExists()
    onNodeWithTag(HomeTabsTestTags.TAB_JOURNAL).assertExists()
    onNodeWithTag(HomeTabsTestTags.TAB_FRIENDS).assertExists()
  }

  @Test
  fun navigationRail_marksHistorySelected_whenCurrentScreenIsFrontHistoryChild() = runComposeUiTest {
    val fake = newFake(activeChild = FakeHomeTabsComponent.ActiveChild.History)

    setContent { TestHomeTabsHost { TestNavigationRail(fake) } }

    onNodeWithTag(HomeTabsTestTags.TAB_HISTORY).assertIsSelected()
  }

  @Test
  fun navigationRail_tappingNonSelectedTab_invokesNavigate() = runComposeUiTest {
    val fake = newFake(activeChild = FakeHomeTabsComponent.ActiveChild.Alters)

    setContent { TestHomeTabsHost { TestNavigationRail(fake) } }

    onNodeWithTag(HomeTabsTestTags.TAB_FRIENDS).performClick()

    assertEquals(1, fake.navigateToFriendsCalls)
    assertEquals(0, fake.navigateToAltersCalls)
  }

  @Test
  fun navigationRail_tappingSelectedTab_doesNotInvokeNavigate() = runComposeUiTest {
    val fake = newFake(activeChild = FakeHomeTabsComponent.ActiveChild.Journal)

    setContent { TestHomeTabsHost { TestNavigationRail(fake) } }

    onNodeWithTag(HomeTabsTestTags.TAB_JOURNAL).performClick()

    assertEquals(0, fake.navigateToJournalCalls)
  }

  // -------- Singlet vs multi-system initial tab ----------------------------------

  // Note: the production initial-stack rule lives in HomeTabsComponentImpl and is
  // exercised by a follow-up pure-component test (see the plan's "Open follow-ups").
  // These two cases lock in the *scaffold's* contract — that the bar reflects whichever
  // child the component reports as active — for both singlet and multi-system Settings.
  @Test
  fun bottomBar_defaultsToAltersSelected_whenIsSingletFalse() = runComposeUiTest {
    val fake = newFake(
      settings = Settings(isSinglet = false),
      activeChild = FakeHomeTabsComponent.ActiveChild.Alters
    )

    setContent { TestHomeTabsHost { TestBottomBar(fake) } }

    onNodeWithTag(HomeTabsTestTags.TAB_ALTERS).assertIsSelected()
  }

  @Test
  fun bottomBar_defaultsToFriendsSelected_whenIsSingletTrue() = runComposeUiTest {
    val fake = newFake(
      settings = Settings(isSinglet = true),
      activeChild = FakeHomeTabsComponent.ActiveChild.Friends
    )

    setContent { TestHomeTabsHost { TestBottomBar(fake) } }

    onNodeWithTag(HomeTabsTestTags.TAB_FRIENDS).assertIsSelected()
  }

  // -------- Full HomeTabsScreen composition --------------------------------------

  // These three exercise HomeTabsScreen end-to-end: the bar branch (or its absence)
  // is selected by the screen itself from Settings + LocalNavigationType, not by
  // the test calling BottomBar/NavigationRail directly. renderChild is supplied as
  // an empty composable so we don't need real Alters/FrontHistory/Journal/Friends
  // screens — only the scaffold contract is under test here.
  @Test
  fun homeTabsScreen_marksAltersSelected_inBottomBar_whenMultiSystem() = runComposeUiTest {
    val fake = newFake(
      settings = Settings(isSinglet = false),
      activeChild = FakeHomeTabsComponent.ActiveChild.Alters
    )

    setContent { TestHomeTabsHost { TestHomeTabsScreen(fake, NavigationType.BOTTOM_BAR) } }

    onNodeWithTag(HomeTabsTestTags.BOTTOM_BAR).assertExists()
    onNodeWithTag(HomeTabsTestTags.TAB_ALTERS).assertIsSelected()
  }

  @Test
  fun homeTabsScreen_marksAltersSelected_inNavigationRail_whenMultiSystem() = runComposeUiTest {
    val fake = newFake(
      settings = Settings(isSinglet = false),
      activeChild = FakeHomeTabsComponent.ActiveChild.Alters
    )

    setContent { TestHomeTabsHost { TestHomeTabsScreen(fake, NavigationType.RAIL) } }

    onNodeWithTag(HomeTabsTestTags.NAVIGATION_RAIL).assertExists()
    onNodeWithTag(HomeTabsTestTags.TAB_ALTERS).assertIsSelected()
  }

  @Test
  fun homeTabsScreen_hidesBothBars_whenIsSingletTrue() = runComposeUiTest {
    val fake = newFake(
      settings = Settings(isSinglet = true),
      activeChild = FakeHomeTabsComponent.ActiveChild.Friends
    )

    // Singlet systems should see neither bar regardless of which navigation type
    // the host would normally use, so pick BOTTOM_BAR (the mobile default) and
    // assert both the bottom bar and the navigation rail tags are absent.
    setContent { TestHomeTabsHost { TestHomeTabsScreen(fake, NavigationType.BOTTOM_BAR) } }

    onNodeWithTag(HomeTabsTestTags.BOTTOM_BAR).assertDoesNotExist()
    onNodeWithTag(HomeTabsTestTags.NAVIGATION_RAIL).assertDoesNotExist()
  }
}

// -------- Test helpers ------------------------------------------------------------

private fun newFake(
  settings: Settings = Settings(),
  activeChild: FakeHomeTabsComponent.ActiveChild = FakeHomeTabsComponent.ActiveChild.Alters
): FakeHomeTabsComponent = FakeHomeTabsComponent(
  initialSettings = settings,
  initialActiveChild = activeChild
)

/**
 * Minimal Octocon theme stand-in: production [BottomBar]/[NavigationRail] expect
 * `LocalOctoTypography` and `LocalOctoShapes` to be provided, and live inside a
 * `MaterialTheme`. The real `OctoconTheme` pulls platform font resources and
 * animations we don't want in a scaffold test, so we wire just enough here.
 */
@Composable
private fun TestHomeTabsHost(content: @Composable () -> Unit) {
  MaterialTheme {
    CompositionLocalProvider(
      LocalOctoTypography provides MaterialTheme.typography,
      LocalOctoShapes provides MaterialTheme.shapes
    ) {
      content()
    }
  }
}

@Composable
private fun TestBottomBar(fake: FakeHomeTabsComponent) {
  val settings by fake.fakeSettings.data.collectAsState()
  val stack by fake.stack.subscribeAsState()
  BottomBar(
    settings = settings,
    component = fake,
    lazyListCoroutineScope = rememberCoroutineScope(),
    lazyListState = null,
    currentScreen = stack.active.instance,
    color = null
  )
}

@Composable
private fun TestNavigationRail(fake: FakeHomeTabsComponent) {
  val settings by fake.fakeSettings.data.collectAsState()
  val stack by fake.stack.subscribeAsState()
  NavigationRail(
    settings = settings,
    component = fake,
    lazyListCoroutineScope = rememberCoroutineScope(),
    lazyListState = null,
    currentScreen = stack.active.instance,
    color = null,
    toggleDrawer = {}
  )
}

/**
 * Composes the full [HomeTabsScreen] against a [FakeHomeTabsComponent], providing
 * [LocalNavigationType] (which `HomeTabsScreen` reads from the parent in
 * production via `MainAppScreen`) and an empty `renderChild` so we don't need real
 * tab screens. `HomeTabsScreen` provides `LocalFABIsCollapsed` and
 * `LocalUpdateLazyListState` itself, so nothing else has to be wired here.
 */
@Composable
private fun TestHomeTabsScreen(
  fake: FakeHomeTabsComponent,
  navigationType: NavigationType
) {
  CompositionLocalProvider(LocalNavigationType provides navigationType) {
    HomeTabsScreen(component = fake, renderChild = {})
  }
}
