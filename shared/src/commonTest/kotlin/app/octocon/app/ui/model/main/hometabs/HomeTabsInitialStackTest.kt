package app.octocon.app.ui.model.main.hometabs

import app.octocon.app.Settings
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Pure-data test of [HomeTabsComponentImpl.initialStackConfigs] — the rule that
 * decides which tab a freshly-created [HomeTabsComponentImpl] opens on.
 *
 * Doing this here rather than via a real [HomeTabsComponentImpl] instance avoids
 * the need to fake `ApiInterface`, the platform-specific `PlatformUtilities`, or
 * the whole child-component cascade (`FriendsComponentImpl` ->
 * `FriendListComponentImpl` -> ...). The follow-up that exercises the
 * lifecycle-driven `navigateToFriendsIfNecessary` callback will need that fuller
 * setup and lives in its own slice.
 */
class HomeTabsInitialStackTest {
  @Test
  fun initialStackConfigs_isFriends_whenSinglet() {
    assertEquals(
      listOf(HomeTabsComponentImpl.Config.Friends),
      HomeTabsComponentImpl.initialStackConfigs(Settings(isSinglet = true))
    )
  }

  @Test
  fun initialStackConfigs_isAlters_whenNotSinglet() {
    assertEquals(
      listOf(HomeTabsComponentImpl.Config.Alters),
      HomeTabsComponentImpl.initialStackConfigs(Settings(isSinglet = false))
    )
  }
}
