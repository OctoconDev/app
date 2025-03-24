package app.octocon.app.ui.compose.components.shared

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.Book
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Groups2
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Poll
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DrawerState
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.PermanentDrawerSheet
import androidx.compose.material3.PermanentNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import app.octocon.app.ui.compose.components.octoconLogoVectorPainter
import app.octocon.app.ui.compose.screens.APP_VERSION
import app.octocon.app.ui.compose.screens.IS_BETA
import app.octocon.app.ui.compose.screens.VERSION_CODE
import app.octocon.app.ui.compose.utils.SpotlightTooltip
import app.octocon.app.ui.model.main.MainAppComponent
import app.octocon.app.ui.model.main.hometabs.HomeTabsComponentImpl
import app.octocon.app.utils.DevicePlatform
import app.octocon.app.utils.Fonts
import app.octocon.app.utils.compose
import app.octocon.app.utils.derive
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.arkivanov.decompose.router.stack.ChildStack
import octoconapp.shared.generated.resources.Res
import octoconapp.shared.generated.resources.alters
import octoconapp.shared.generated.resources.app_name
import octoconapp.shared.generated.resources.back
import octoconapp.shared.generated.resources.friends
import octoconapp.shared.generated.resources.history
import octoconapp.shared.generated.resources.home
import octoconapp.shared.generated.resources.journal
import octoconapp.shared.generated.resources.polls
import octoconapp.shared.generated.resources.profile
import octoconapp.shared.generated.resources.resources
import octoconapp.shared.generated.resources.settings
import octoconapp.shared.generated.resources.support_us
import octoconapp.shared.generated.resources.tooltip_alters_desc
import octoconapp.shared.generated.resources.tooltip_friends_desc
import octoconapp.shared.generated.resources.tooltip_history_desc
import octoconapp.shared.generated.resources.tooltip_home_desc
import octoconapp.shared.generated.resources.tooltip_journal_desc
import octoconapp.shared.generated.resources.tooltip_polls_desc
import octoconapp.shared.generated.resources.tooltip_profile_desc
import octoconapp.shared.generated.resources.tooltip_resources_desc
import octoconapp.shared.generated.resources.tooltip_settings_desc
import octoconapp.shared.generated.resources.tooltip_support_us_desc

@Composable
fun NavigationItem(
  isSelected: Boolean,
  navigate: () -> Unit,
  label: String,
  icon: ImageVector,
  toggleDrawer: ((Boolean) -> Unit)?
) {
  NavigationDrawerItem(
    selected = isSelected,
    onClick = {
      navigate()
      toggleDrawer?.invoke(false)
    },
    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
    label = { Text(label) },
    icon = { Icon(imageVector = icon, contentDescription = label) }
  )
}

@Composable
fun OctoconFixedNavigationDrawer(
  component: MainAppComponent,
  content: @Composable () -> Unit
) {
  val stack by component.stack.subscribeAsState()
  val activeHomeTabsConfig by component.activeHomeTabsConfig.collectAsState()

  val settingsData by component.settingsData.collectAsState()
  val isSinglet by derive { settingsData.isSinglet }

  PermanentNavigationDrawer(
    drawerContent = {
      PermanentDrawerSheet(
        modifier = Modifier.fillMaxHeight()
      ) {
        CommonNavigationDrawer(component, stack, null) {
          if(!isSinglet) {
            SpotlightTooltip(
              title = Res.string.alters.compose,
              description = Res.string.tooltip_alters_desc.compose,
            ) {
              NavigationItem(
                stack.active.instance is MainAppComponent.Child.HomeTabsChild && activeHomeTabsConfig == HomeTabsComponentImpl.Config.Alters,
                {
                  // TODO: Make this more robust to prevent animation jank?
                  component.navigateToAlters()
                  if(stack.active.instance !is MainAppComponent.Child.HomeTabsChild) {
                    component.navigateToHomeTabs()
                  }
                },
                Res.string.alters.compose,
                Icons.Rounded.Groups2,
                null
              )
            }
            SpotlightTooltip(
              title = Res.string.history.compose,
              description = Res.string.tooltip_history_desc.compose,
            ) {
              NavigationItem(
                stack.active.instance is MainAppComponent.Child.HomeTabsChild && activeHomeTabsConfig == HomeTabsComponentImpl.Config.FrontHistory,
                {
                  component.navigateToHistory()
                  if(stack.active.instance !is MainAppComponent.Child.HomeTabsChild) {
                    component.navigateToHomeTabs()
                  }
                },
                Res.string.history.compose,
                Icons.Rounded.History,
                null
              )
            }
            SpotlightTooltip(
              title = Res.string.journal.compose,
              description = Res.string.tooltip_journal_desc.compose,
            ) {
              NavigationItem(
                stack.active.instance is MainAppComponent.Child.HomeTabsChild && activeHomeTabsConfig == HomeTabsComponentImpl.Config.Journal,
                {
                  component.navigateToJournal()
                  if(stack.active.instance !is MainAppComponent.Child.HomeTabsChild) {
                    component.navigateToHomeTabs()
                  }
                },
                Res.string.journal.compose,
                Icons.Rounded.Book,
                null
              )
            }
          }
          SpotlightTooltip(
            title = Res.string.friends.compose,
            description = Res.string.tooltip_friends_desc.compose,
          ) {
            NavigationItem(
              stack.active.instance is MainAppComponent.Child.HomeTabsChild && activeHomeTabsConfig == HomeTabsComponentImpl.Config.Friends,
              {
                component.navigateToFriends()
                if(stack.active.instance !is MainAppComponent.Child.HomeTabsChild) {
                  component.navigateToHomeTabs()
                }
              },
              Res.string.friends.compose,
              Icons.Rounded.Person,
              null
            )
          }
          Spacer(Modifier.height(12.dp))
          HorizontalDivider(modifier = Modifier.padding(horizontal = 32.dp))
          Spacer(Modifier.height(12.dp))
        }
      }
    },
    content = content
  )
}

@Composable
fun OctoconModalNavigationDrawer(
  drawerState: DrawerState,
  toggleDrawer: (Boolean) -> Unit,
  component: MainAppComponent,
  content: @Composable () -> Unit
) {
  val stack by component.stack.subscribeAsState()

  if(DevicePlatform.isAndroid) {
    AndroidModalNavigationDrawer(component, stack, drawerState, toggleDrawer, content)
  } else {
    NonAndroidModalNavigationDrawer(component, stack, drawerState, toggleDrawer, content)
  }
}

@Composable
fun AndroidModalNavigationDrawer(
  component: MainAppComponent,
  stack: ChildStack<*, MainAppComponent.Child>,
  drawerState: DrawerState,
  toggleDrawer: (Boolean) -> Unit,
  content: @Composable () -> Unit
) {
  val settingsData by component.settingsData.collectAsState()
  val isSinglet by derive { settingsData.isSinglet }
  ModalNavigationDrawer(
    drawerContent = {
      ModalDrawerSheet(
        drawerState = drawerState,
        modifier = Modifier.padding(end = 32.dp)
      ) {
        CommonNavigationDrawer(component, stack, toggleDrawer) {
          SpotlightTooltip(
            title = if(isSinglet) Res.string.friends.compose else Res.string.home.compose,
            description = if(isSinglet) Res.string.tooltip_friends_desc.compose else Res.string.tooltip_home_desc.compose,
          ) {
            NavigationItem(
              stack.active.instance is MainAppComponent.Child.HomeTabsChild,
              { component.navigateToHomeTabs(); toggleDrawer(false) },
              if(isSinglet) Res.string.friends.compose else Res.string.home.compose,
              if(isSinglet) Icons.Rounded.Favorite else Icons.Rounded.Home,
              toggleDrawer
            )
          }
        }
      }
    },
    drawerState = drawerState,
    content = content
  )
}

@Composable
fun NonAndroidModalNavigationDrawer(
  component: MainAppComponent,
  stack: ChildStack<*, MainAppComponent.Child>,
  drawerState: DrawerState,
  toggleDrawer: (Boolean) -> Unit,
  content: @Composable () -> Unit
) {
  val settingsData by component.settingsData.collectAsState()
  val isSinglet by derive { settingsData.isSinglet }

  ModalNavigationDrawer(
    drawerContent = {
      ModalDrawerSheet(
        modifier = Modifier.padding(end = 32.dp)
      ) {
        CommonNavigationDrawer(component, stack, toggleDrawer) {
          SpotlightTooltip(
            title = if(isSinglet) Res.string.friends.compose else Res.string.home.compose,
            description = if(isSinglet) Res.string.tooltip_friends_desc.compose else Res.string.tooltip_home_desc.compose,
          ) {
            NavigationItem(
              stack.active.instance is MainAppComponent.Child.HomeTabsChild,
              { component.navigateToHomeTabs(); toggleDrawer(false) },
              if (isSinglet) Res.string.friends.compose else Res.string.home.compose,
              if (isSinglet) Icons.Rounded.Favorite else Icons.Rounded.Home,
              toggleDrawer
            )
          }
        }
      }
    },
    drawerState = drawerState,
    content = content
  )
}

@Composable
fun ColumnScope.CommonNavigationDrawer(
  component: MainAppComponent,
  stack: ChildStack<*, MainAppComponent.Child>,
  toggleDrawer: ((Boolean) -> Unit)?,
  homeContents: @Composable () -> Unit
) {
  CommonNavigationDrawer(
    component = component,
    stack = stack,
    toggleDrawer = toggleDrawer,
    homeContents = homeContents,
    modifier = Modifier.weight(1.0f)
  )
}

@Composable
fun CommonNavigationDrawer(
  component: MainAppComponent,
  stack: ChildStack<*, MainAppComponent.Child>,
  toggleDrawer: ((Boolean) -> Unit)?,
  modifier: Modifier,
  homeContents: @Composable () -> Unit,
) {
  val settingsData by component.settingsData.collectAsState()

  val isSinglet by derive { settingsData.isSinglet }

  Column(
    modifier = modifier.verticalScroll(rememberScrollState())
  ) {
    Spacer(Modifier.height(24.dp))
    Row(
      modifier = Modifier.padding(horizontal = 28.dp).fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      Image(
        painter = octoconLogoVectorPainter(),
        contentDescription = null,
        modifier = Modifier.size(24.dp)
      )
      Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
      ) {
        Text(
          Res.string.app_name.compose,
          style = MaterialTheme.typography.titleLarge.merge(fontFamily = Fonts.ubuntu)
        )
        if(IS_BETA) {
          Text(
            "Beta",
            style = MaterialTheme.typography.labelSmall.copy(fontFamily = Fonts.ubuntu),
            color = MaterialTheme.colorScheme.primary
          )
        }
      }
      if(toggleDrawer != null) {
        Spacer(Modifier.weight(1f))
        IconButton(
          onClick = { toggleDrawer(false) },
          content = {
            Icon(
              imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
              contentDescription = Res.string.back.compose
            )
          }
        )
      }
    }
    Spacer(Modifier.height(24.dp))
    homeContents()
    if(!isSinglet) {
      SpotlightTooltip(
        title = Res.string.polls.compose,
        description = Res.string.tooltip_polls_desc.compose,
      ) {
        NavigationItem(
          stack.active.instance is MainAppComponent.Child.PollsChild,
          { component.navigateToPolls(); toggleDrawer?.invoke(false) },
          Res.string.polls.compose,
          Icons.Rounded.Poll,
          toggleDrawer
        )
      }
    }
    SpotlightTooltip(
      title = Res.string.resources.compose,
      description = Res.string.tooltip_resources_desc.compose,
    ) {
      NavigationItem(
        stack.active.instance is MainAppComponent.Child.ResourcesChild,
        { component.navigateToResources(); toggleDrawer?.invoke(false) },
        Res.string.resources.compose,
        Icons.AutoMirrored.Rounded.MenuBook,
        toggleDrawer
      )
    }
    Spacer(Modifier.height(12.dp))
    HorizontalDivider(modifier = Modifier.padding(horizontal = 32.dp))
    Spacer(Modifier.height(12.dp))
    SpotlightTooltip(
      title = Res.string.profile.compose,
      description = Res.string.tooltip_profile_desc.compose
    ) {
      NavigationItem(
        stack.active.instance is MainAppComponent.Child.ProfileChild,
        { component.navigateToProfile(); toggleDrawer?.invoke(false) },
        Res.string.profile.compose,
        Icons.Rounded.Person,
        toggleDrawer
      )
    }
    SpotlightTooltip(
      title = Res.string.settings.compose,
      description = Res.string.tooltip_settings_desc.compose
    ) {
      NavigationItem(
        stack.active.instance is MainAppComponent.Child.SettingsChild,
        { component.navigateToSettings(); toggleDrawer?.invoke(false) },
        Res.string.settings.compose,
        Icons.Rounded.Settings,
        toggleDrawer
      )
    }
    Spacer(Modifier.height(24.dp))
    Spacer(Modifier.weight(1.0f))
    Text(
      "v$APP_VERSION ($VERSION_CODE)",
      style = MaterialTheme.typography.labelSmall.merge(
        color = MaterialTheme.colorScheme.onSurface.copy(
          alpha = 0.5f
        )
      ),
      modifier = Modifier.padding(horizontal = 32.dp)
    )
    if(!DevicePlatform.isiOS) {
      SpotlightTooltip(
        title = Res.string.support_us.compose,
        description = Res.string.tooltip_support_us_desc.compose
      ) {
        FilledTonalButton(
          colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
          ),
          onClick = { component.navigateToSupportUs(); toggleDrawer?.invoke(false) },
          contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
          modifier = Modifier.padding(
            start = 32.dp,
            end = 32.dp,
            bottom = 24.dp,
            top = 12.dp
          )
        ) {
          Icon(
            imageVector = Icons.Rounded.Favorite,
            contentDescription = null,
            modifier = Modifier.size(ButtonDefaults.IconSize)
          )
          Spacer(Modifier.size(ButtonDefaults.IconSpacing))
          Text(Res.string.support_us.compose)
        }
      }
    }
  }
}