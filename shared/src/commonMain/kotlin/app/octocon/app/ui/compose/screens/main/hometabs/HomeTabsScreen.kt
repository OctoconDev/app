package app.octocon.app.ui.compose.screens.main.hometabs

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Book
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Groups2
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.VectorPainter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import app.octocon.app.Settings
import app.octocon.app.ui.compose.LocalFABIsCollapsed
import app.octocon.app.ui.compose.LocalModalDrawerToggler
import app.octocon.app.ui.compose.LocalNavigationType
import app.octocon.app.ui.compose.LocalUpdateLazyListState
import app.octocon.app.ui.compose.NavigationType
import app.octocon.app.ui.compose.theme.ThemeFromColor
import app.octocon.app.ui.model.main.hometabs.HomeTabsComponent
import app.octocon.app.utils.abifix.fixedABIStackAnimation
import app.octocon.app.utils.compose
import app.octocon.app.utils.derive
import app.octocon.app.utils.state
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.experimental.stack.ChildStack
import com.arkivanov.decompose.extensions.compose.experimental.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import octoconapp.shared.generated.resources.Res
import octoconapp.shared.generated.resources.alters
import octoconapp.shared.generated.resources.friends
import octoconapp.shared.generated.resources.history
import octoconapp.shared.generated.resources.journal
import octoconapp.shared.generated.resources.menu

@OptIn(ExperimentalDecomposeApi::class)
@Composable
fun HomeTabsScreen(
  component: HomeTabsComponent
) {
  /*val topAppBarState = rememberTopAppBarState()
  val scrollBehavior =
    TopAppBarDefaults.exitUntilCollapsedScrollBehavior(topAppBarState)*/

  val settings: Settings by component.settings.collectAsState()

  val isSinglet by derive { settings.isSinglet }

  val stack by component.stack.subscribeAsState()
  val currentScreen = stack.active.instance

  // TODO: Dynamic scaffold color
  /*var scaffoldColorMap by state(linkedMapOf<Any, String?>())
  val currentColor by derive { scaffoldColorMap.values.lastOrNull() }

  LaunchedEffect(scaffoldColorMap) {
    println("Scaffold color map: $scaffoldColorMap")
  }

  LaunchedEffect(currentColor) {
    println("Current color: $currentColor")
  }*/

  // val screenTransitionType by derive { settings.screenTransitionType }

  val lazyListCoroutineScope = rememberCoroutineScope()
  var lazyListState: LazyListState? by state(null)

  val updateLazyListState: (LazyListState?) -> Unit = {
    lazyListState = it
  }

  LaunchedEffect(Unit) {
    component.updateOnCurrentTabPressed {
      lazyListState?.let {
        lazyListCoroutineScope.launch {
          it.animateScrollToItem(0)
        }
      }
    }
  }

  var bottomBarIsCollapsed by state(false)
  val bottomBarNestedScrollConnection = remember {
    object : NestedScrollConnection {
      var isFling = false

      override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
        // Hide bottom bar & FAB
        if (available.y < -5) {
          bottomBarIsCollapsed = true
        }

        // Show bottom bar & FAB
        if (available.y > 5) {
          bottomBarIsCollapsed = false
        }

        return Offset.Zero
      }

      override suspend fun onPreFling(available: Velocity): Velocity {
        isFling = true
        return super.onPreFling(available)
      }

      override suspend fun onPostFling(
        consumed: Velocity,
        available: Velocity
      ): Velocity {
        isFling = false
        return super.onPostFling(consumed, available)
      }
    }
  }

  val navigationType = LocalNavigationType.current

  Box(
    modifier = Modifier.nestedScroll(bottomBarNestedScrollConnection).fillMaxSize()
  ) {
    CompositionLocalProvider(
      LocalFABIsCollapsed provides bottomBarIsCollapsed,
      LocalUpdateLazyListState provides updateLazyListState
    ) {
      Row(modifier = Modifier.fillMaxSize()) {
        if (!isSinglet && navigationType == NavigationType.RAIL) {
          // Use dual panels when on a tablet-sized screen
          NavigationRail(
            settings,
            component,
            lazyListCoroutineScope,
            lazyListState,
            currentScreen,
            null,
            LocalModalDrawerToggler.current,
            modifier = Modifier.zIndex(999f)
          )
        }

        ChildStack(
          stack,
          animation = fixedABIStackAnimation(fade(tween(200))),
          modifier = Modifier.fillMaxSize()
        ) {
          when (val child = it.instance) {
            is HomeTabsComponent.Child.AltersChild -> AltersScreen(child.component)
            is HomeTabsComponent.Child.FrontHistoryChild -> FrontHistoryScreen(child.component)
            is HomeTabsComponent.Child.JournalChild -> JournalScreen(child.component)
            is HomeTabsComponent.Child.FriendsChild -> FriendsScreen(child.component)
          }
        }
      }
    }
    if(!isSinglet && navigationType == NavigationType.BOTTOM_BAR) {
      // Use navigation bar when on a narrower screen
      AnimatedContent(
        targetState = bottomBarIsCollapsed,
        transitionSpec = {
          slideInVertically { height -> height } togetherWith
              slideOutVertically { height -> height }
        },
        modifier = Modifier.align(Alignment.BottomStart)
      ) { isCollapsed ->
        if (isCollapsed) {
          Box(modifier = Modifier.fillMaxWidth().height(0.dp))
        } else {
          BottomBar(
            settings,
            component,
            lazyListCoroutineScope,
            lazyListState,
            currentScreen,
            null
          )
        }
      }
    }
  }
}

@Composable
fun NavigationRail(
  settings: Settings,
  component: HomeTabsComponent,
  lazyListCoroutineScope: CoroutineScope,
  lazyListState: LazyListState?,
  currentScreen: HomeTabsComponent.Child,
  color: String?,
  toggleDrawer: (Boolean) -> Unit,
  modifier: Modifier = Modifier
) {
  ThemeFromColor(
    color,
    colorMode = settings.colorMode,
    dynamicColorType = settings.dynamicColorType,
    colorContrastLevel = settings.colorContrastLevel,
    amoledMode = settings.amoledMode
  ) {
    NavigationRail(
      modifier = modifier,
      header = {
        IconButton(
          onClick = { toggleDrawer(true) }
        ) {
          Icon(
            Icons.Rounded.Menu,
            contentDescription = Res.string.menu.compose
          )
        }
      }
    ) {
      Spacer(modifier = Modifier.weight(1f))
      OctoconNavigationRailItem(
        Res.string.alters.compose,
        rememberVectorPainter(Icons.Rounded.Groups2),
        lazyListCoroutineScope,
        lazyListState,
        currentScreen is HomeTabsComponent.Child.AltersChild,
        component::navigateToAlters
      )
      OctoconNavigationRailItem(
        Res.string.history.compose,
        rememberVectorPainter(Icons.Rounded.History),
        lazyListCoroutineScope,
        lazyListState,
        currentScreen is HomeTabsComponent.Child.FrontHistoryChild,
        component::navigateToHistory
      )
      OctoconNavigationRailItem(
        Res.string.journal.compose,
        rememberVectorPainter(Icons.Rounded.Book),
        lazyListCoroutineScope,
        lazyListState,
        currentScreen is HomeTabsComponent.Child.JournalChild,
        component::navigateToJournal
      )
      OctoconNavigationRailItem(
        Res.string.friends.compose,
        rememberVectorPainter(Icons.Rounded.Favorite),
        lazyListCoroutineScope,
        lazyListState,
        currentScreen is HomeTabsComponent.Child.FriendsChild,
        component::navigateToFriends
      )
      Spacer(modifier = Modifier.weight(1f))
    }
  }
}


@Composable
fun BottomBar(
  settings: Settings,
  component: HomeTabsComponent,
  lazyListCoroutineScope: CoroutineScope,
  lazyListState: LazyListState?,
  currentScreen: HomeTabsComponent.Child,
  color: String?,
  modifier: Modifier = Modifier
) {
  ThemeFromColor(
    color,
    colorMode = settings.colorMode,
    dynamicColorType = settings.dynamicColorType,
    colorContrastLevel = settings.colorContrastLevel,
    amoledMode = settings.amoledMode
  ) {
    NavigationBar(
      // windowInsets = WindowInsets.navigationBars
      modifier = modifier
    ) {
      OctoconNavigationBarItem(
        Res.string.alters.compose,
        rememberVectorPainter(Icons.Rounded.Groups2),
        lazyListCoroutineScope,
        lazyListState,
        currentScreen is HomeTabsComponent.Child.AltersChild,
        component::navigateToAlters
      )
      OctoconNavigationBarItem(
        Res.string.history.compose,
        rememberVectorPainter(Icons.Rounded.History),
        lazyListCoroutineScope,
        lazyListState,
        currentScreen is HomeTabsComponent.Child.FrontHistoryChild,
        component::navigateToHistory
      )
      OctoconNavigationBarItem(
        Res.string.journal.compose,
        rememberVectorPainter(Icons.Rounded.Book),
        lazyListCoroutineScope,
        lazyListState,
        currentScreen is HomeTabsComponent.Child.JournalChild,
        component::navigateToJournal
      )
      OctoconNavigationBarItem(
        Res.string.friends.compose,
        rememberVectorPainter(Icons.Rounded.Favorite),
        lazyListCoroutineScope,
        lazyListState,
        currentScreen is HomeTabsComponent.Child.FriendsChild,
        component::navigateToFriends
      )
    }
  }
}


@Composable
private fun RowScope.OctoconNavigationBarItem(
  title: String,
  icon: VectorPainter,
  lazyListCoroutineScope: CoroutineScope,
  lazyListState: LazyListState?,
  isSelected: Boolean,
  navigate: () -> Unit
) {
  NavigationBarItem(
    selected = isSelected,
    onClick = {
      if (isSelected) {
        lazyListState?.let {
          lazyListCoroutineScope.launch {
            it.animateScrollToItem(0)
          }
        }
      } else {
        navigate()
      }
    },
    icon = { Icon(painter = icon, contentDescription = title) },
    label = { Text(text = title) }
  )
}

@Composable
private fun ColumnScope.OctoconNavigationRailItem(
  title: String,
  icon: VectorPainter,
  lazyListCoroutineScope: CoroutineScope,
  lazyListState: LazyListState?,
  isSelected: Boolean,
  navigate: () -> Unit
) {
  NavigationRailItem(
    selected = isSelected,
    onClick = {
      if(isSelected) {
        lazyListState?.let {
          lazyListCoroutineScope.launch {
            it.animateScrollToItem(0)
          }
        }
      } else {
        navigate()
      }
    },
    icon = { Icon(painter = icon, contentDescription = title) },
    label = { Text(text = title) }
  )
}