package app.octocon.app.ui.model.main.hometabs

import app.octocon.app.Settings
import app.octocon.app.api.FakeApiInterface
import app.octocon.app.ui.compose.screens.main.hometabs.FakeSettingsInterface
import app.octocon.app.ui.model.CommonComponentContextImpl
import app.octocon.app.ui.model.MainComponentContextImpl
import app.octocon.app.utils.FakePlatformUtilities
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import com.arkivanov.essenty.lifecycle.start
import kotlinx.coroutines.Dispatchers
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Full-integration test of [HomeTabsComponentImpl]'s lifecycle subscription.
 *
 * The component's `init` block subscribes a [com.arkivanov.essenty.lifecycle.Lifecycle.Callbacks]
 * whose `onStart` / `onResume` both call `navigateToFriendsIfNecessary`, which fires
 * `navigationWrapper(Config.Friends)` only when `settings.isSinglet` is `true`.
 *
 * Each test wires up a real component graph against fake `Api`/`Settings`/`PlatformUtilities`
 * implementations, drives the lifecycle forward via Essenty's manual API, and asserts on the
 * navigation callbacks captured by the harness.
 *
 * `LifecycleRegistry()` constructs in [com.arkivanov.essenty.lifecycle.Lifecycle.State.INITIALIZED].
 * The extension functions are additive:
 *   - `start()` walks INITIALIZED → CREATED → STARTED, firing one `onStart`.
 *   - `resume()` walks INITIALIZED → CREATED → STARTED → RESUMED, firing one `onStart` plus
 *     one `onResume` (so two callbacks in the singlet case).
 */
class HomeTabsLifecycleTest {

  @Test
  fun lifecycleStart_navigatesToFriendsOnce_whenSinglet() {
    val h = newHomeTabs(isSinglet = true)
    h.lifecycle.start()
    assertEquals(listOf(HomeTabsComponentImpl.Config.Friends), h.navigations)
  }

  @Test
  fun lifecycleResume_navigatesToFriendsOnStartAndResume_whenSinglet() {
    val h = newHomeTabs(isSinglet = true)
    h.lifecycle.resume()
    assertEquals(
      listOf(HomeTabsComponentImpl.Config.Friends, HomeTabsComponentImpl.Config.Friends),
      h.navigations
    )
  }

  @Test
  fun lifecycleResume_doesNotNavigate_whenNotSinglet() {
    val h = newHomeTabs(isSinglet = false)
    h.lifecycle.resume()
    assertTrue(
      h.navigations.isEmpty(),
      "Expected no navigation when system is not a singlet, captured: ${h.navigations}"
    )
  }

  private class Harness(
    @Suppress("unused") val component: HomeTabsComponentImpl,
    val navigations: List<HomeTabsComponentImpl.Config>,
    val lifecycle: LifecycleRegistry,
  )

  private fun newHomeTabs(isSinglet: Boolean): Harness {
    val lifecycle = LifecycleRegistry()
    val rootCtx = DefaultComponentContext(lifecycle = lifecycle)
    val commonCtx = CommonComponentContextImpl(
      componentContext = rootCtx,
      api = FakeApiInterface(),
      settings = FakeSettingsInterface(Settings(isSinglet = isSinglet)),
      platformUtilities = FakePlatformUtilities(),
      coroutineContext = Dispatchers.Unconfined,
    )
    val mainCtx = MainComponentContextImpl(commonCtx)
    val captured = mutableListOf<HomeTabsComponentImpl.Config>()
    val component = HomeTabsComponentImpl(
      componentContext = mainCtx,
      navigator = StackNavigation(),
      navigationWrapper = { captured += it },
      navigateToCustomFieldsFun = {},
    )
    return Harness(component = component, navigations = captured, lifecycle = lifecycle)
  }
}
