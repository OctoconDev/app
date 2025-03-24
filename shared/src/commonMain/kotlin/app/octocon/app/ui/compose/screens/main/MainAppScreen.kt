package app.octocon.app.ui.compose.screens.main

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import app.octocon.app.ScreenTransitionType.Companion.FADE_ANIMATOR
import app.octocon.app.ScreenTransitionType.Companion.ZOOM_ANIMATOR
import app.octocon.app.ui.compose.InternalOctoconLayoutApi
import app.octocon.app.ui.compose.LocalDesiredChildPanelsMode
import app.octocon.app.ui.compose.LocalModalDrawerToggler
import app.octocon.app.ui.compose.LocalNavigationType
import app.octocon.app.ui.compose.NavigationType
import app.octocon.app.ui.compose.components.shared.OctoconFixedNavigationDrawer
import app.octocon.app.ui.compose.components.shared.OctoconModalNavigationDrawer
import app.octocon.app.ui.compose.screens.main.hometabs.HomeTabsScreen
import app.octocon.app.ui.model.main.MainAppComponent
import app.octocon.app.utils.ExitApplicationType
import app.octocon.app.utils.abifix.fixedABIStackAnimation
import app.octocon.app.utils.derive
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.experimental.stack.ChildStack
import com.arkivanov.decompose.router.panels.ChildPanelsMode

@OptIn(ExperimentalDecomposeApi::class, InternalOctoconLayoutApi::class)
@Composable
fun MainAppScreen(
  component: MainAppComponent
) {
  val settings by component.settingsData.collectAsState()
  val reduceMotion by derive { settings.reduceMotion }

  val useTabletLayout by derive { settings.useTabletLayout }

  val innerScreenContents = @Composable {
    Box(
      modifier = Modifier.fillMaxSize().let {
        if (settings.quickExitEnabled) {
          it.pointerInput(Unit) {
            detectTapGestures(
              onDoubleTap = { component.exitApplication(ExitApplicationType.QuickExit) }
            )
          }
        } else it
      }
    ) {
      ChildStack(
        component.stack,
        animation = fixedABIStackAnimation(if(reduceMotion) FADE_ANIMATOR else ZOOM_ANIMATOR)
      ) {
        when (val child = it.instance) {
          is MainAppComponent.Child.HomeTabsChild -> HomeTabsScreen(child.component)
          is MainAppComponent.Child.PollsChild -> PollsScreen(child.component)
          is MainAppComponent.Child.ProfileChild -> ProfileScreen(child.component)
          is MainAppComponent.Child.ResourcesChild -> ResourcesScreen(child.component)
          is MainAppComponent.Child.SettingsChild -> SettingsScreen(child.component)
          is MainAppComponent.Child.SupportUsChild -> SupportUsScreen(child.component)
        }
      }
    }
  }

  BoxWithConstraints(
    modifier = Modifier.fillMaxSize()
  ) {
    val navigationType = remember(maxWidth, maxHeight) {
      when {
        !useTabletLayout -> NavigationType.BOTTOM_BAR
        maxHeight < 600.dp -> NavigationType.BOTTOM_BAR

        maxWidth >= 1280.dp -> NavigationType.DRAWER
        maxWidth >= 840.dp -> NavigationType.RAIL
        else -> NavigationType.BOTTOM_BAR
      }
    }
    val childPanelsMode = remember(navigationType) {
      when (navigationType) {
        NavigationType.BOTTOM_BAR -> ChildPanelsMode.SINGLE
        else -> ChildPanelsMode.DUAL
      }
    }

    CompositionLocalProvider(
      LocalNavigationType provides navigationType,
      LocalDesiredChildPanelsMode provides childPanelsMode,
    ) {
      if(navigationType == NavigationType.DRAWER) {
        OctoconFixedNavigationDrawer(
          component = component
        ) {
          innerScreenContents()
        }
      } else {
        val drawerScope = rememberCoroutineScope()
        LaunchedEffect(Unit) { component.provideDrawerScope(drawerScope) }

        OctoconModalNavigationDrawer(
          drawerState = component.drawerState,
          toggleDrawer = component::toggleDrawer,
          component = component
        ) {
          CompositionLocalProvider(
            LocalModalDrawerToggler provides component::toggleDrawer
          ) {
            innerScreenContents()
          }
        }
      }
    }
  }
}