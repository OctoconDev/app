package app.octocon.app.ui.compose.components.shared

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.TopAppBarState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import app.octocon.app.ui.compose.LocalFABIsCollapsed
import app.octocon.app.ui.compose.LocalModalDrawerToggler
import app.octocon.app.ui.compose.LocalNavigationType
import app.octocon.app.ui.compose.LocalShowSnackbar
import app.octocon.app.ui.compose.NavigationType
import app.octocon.app.ui.compose.utils.SpotlightTooltip
import app.octocon.app.utils.DevicePlatform
import app.octocon.app.utils.compose
import app.octocon.app.utils.state
import kotlinx.coroutines.launch
import octoconapp.shared.generated.resources.Res
import octoconapp.shared.generated.resources.back
import octoconapp.shared.generated.resources.menu

@Composable
private fun KeyboardNestedScrollConnection(): NestedScrollConnection? {
  val keyboardController = LocalSoftwareKeyboardController.current

  return if (DevicePlatform.isiOS) {
    remember {
      object : NestedScrollConnection {
        // var isFling = false

        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
          // Hide keyboard when scrolling up on iOS
          if (available.y > 5) {
            keyboardController?.hide()
          }

          return Offset.Zero
        }

        /*override suspend fun onPreFling(available: Velocity): Velocity {
          isFling = true
          return super.onPreFling(available)
        }

        override suspend fun onPostFling(
          consumed: Velocity,
          available: Velocity
        ): Velocity {
          isFling = false
          return super.onPostFling(consumed, available)
        }*/
      }
    }
  } else {
    null
  }
}

@Composable
private fun Modifier.withKeyboardNestedScrollConnection(): Modifier {
  val nestedScrollConnection = KeyboardNestedScrollConnection()

  return if (nestedScrollConnection != null) {
    this.nestedScroll(nestedScrollConnection)
  } else {
    this
  }
}

@Composable
fun OctoScaffold(
  topBar: @Composable ((TopAppBarState, TopAppBarScrollBehavior, showSnackbar: (String) -> Unit) -> Unit)? = null,
  floatingActionButton: @Composable (ColumnScope.() -> Unit)? = null,
  padContentOnLargerScreens: Boolean = false,
  hasHoistedBottomBar: Boolean = false,
  content: @Composable (BoxScope.(padding: PaddingValues, showSnackbar: (String) -> Unit) -> Unit)
) {
  val navigationType = LocalNavigationType.current

  val snackbarHostState = remember { SnackbarHostState() }
  val snackbarCoroutineScope = rememberCoroutineScope()

  val showSnackbar: (String) -> Unit = {
    snackbarCoroutineScope.launch {
      snackbarHostState.showSnackbar(it)
    }
  }

  val topAppBarState = topBar?.let { rememberTopAppBarState() }
  val scrollBehavior = topBar?.let { TopAppBarDefaults.exitUntilCollapsedScrollBehavior(topAppBarState!!) }

  var fabIsCollapsed by state(false)
  val fabNestedScrollConnection = if(floatingActionButton != null && !hasHoistedBottomBar) {
    remember {
      object : NestedScrollConnection {
        var isFling = false

        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
          // Hide bottom bar & FAB
          if (available.y < -5) {
            fabIsCollapsed = true
          }

          // Show bottom bar & FAB
          if (available.y > 5) {
            fabIsCollapsed = false
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
  } else null

  val topBarComposable: @Composable () -> Unit = topBar?.let {
    {
      topBar(topAppBarState!!, scrollBehavior!!, showSnackbar)
    }
  } ?: {}

  val fabComposable: @Composable () -> Unit = floatingActionButton?.let {
    {
      val isCollapsed = if(hasHoistedBottomBar) {
        LocalFABIsCollapsed.current
      } else {
        fabIsCollapsed
      }

      val bottomPadding = if(navigationType == NavigationType.BOTTOM_BAR && hasHoistedBottomBar) {
        animateDpAsState(if (isCollapsed) 0.dp else 80.dp)
      } else {
        animateDpAsState(0.dp)
      }

      AnimatedVisibility(
        visible = !isCollapsed,
        enter = fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)),
        exit = fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMediumLow))
      ) {
        Column(
          verticalArrangement = Arrangement.spacedBy(12.dp),
          horizontalAlignment = Alignment.End,
          modifier = Modifier.padding(bottom = bottomPadding.value),
          content = floatingActionButton
        )
      }
    }
  } ?: {}

  Scaffold(
    topBar = topBarComposable,
    floatingActionButton = fabComposable,
    snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    content = { padding ->
      CompositionLocalProvider(
        LocalShowSnackbar provides showSnackbar,
        LocalFABIsCollapsed provides fabIsCollapsed
      ) {
        Box(
          contentAlignment = Alignment.TopCenter,
          modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .consumeWindowInsets(padding)
            .withKeyboardNestedScrollConnection()
            .let {
              if(scrollBehavior != null) {
                it.nestedScroll(scrollBehavior.nestedScrollConnection)
              } else {
                it
              }
            }.let {
              if(fabNestedScrollConnection != null) {
                it.nestedScroll(fabNestedScrollConnection)
              } else {
                it
              }
            },
          content = {
            Box(
              modifier = Modifier.let {
                if(padContentOnLargerScreens) {
                  it.widthIn(max = 650.dp)
                } else {
                  it
                }
              }
            ) {
              content(padding, showSnackbar)
            }
          }
        )
      }
    }
  )
}

data class TitleTextState(
  val title: String,
  val oneLine: Boolean = true,
  val onClick: (() -> Unit)? = null,
  val spotlightText: Pair<String, String>? = null
)

@Composable
fun OctoTopBar(
  titleTextState: TitleTextState,
  navigation: @Composable () -> Unit,
  actions: @Composable RowScope.() -> Unit = {},
  topAppBarState: TopAppBarState = rememberTopAppBarState(),
  scrollBehavior: TopAppBarScrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
    topAppBarState
  ),
) {
  MediumTopAppBar(
    actions = actions,
    navigationIcon = navigation,
    title = {
      @Composable
      fun InnerText() {
        Text(
          text = titleTextState.title,
          maxLines = if (titleTextState.oneLine) 1 else Int.MAX_VALUE,
          overflow = TextOverflow.Ellipsis,
          modifier = Modifier.let {
            if (titleTextState.onClick != null) {
              it.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
              ) { titleTextState.onClick.invoke() }
            } else {
              it
            }
          }
        )
      }

      if(titleTextState.spotlightText == null) {
        InnerText()
      } else {
        SpotlightTooltip(
          title = titleTextState.spotlightText.first,
          description = titleTextState.spotlightText.second
        ) {
          InnerText()
        }
      }

    },
    scrollBehavior = scrollBehavior
  )
}

@Composable
fun OctoLargeTopBar(
  titleTextState: TitleTextState,
  navigation: @Composable () -> Unit,
  actions: @Composable RowScope.() -> Unit = {},
  topAppBarState: TopAppBarState = rememberTopAppBarState(),
  scrollBehavior: TopAppBarScrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
    topAppBarState
  ),
) {
  LargeTopAppBar(
    actions = actions,
    navigationIcon = navigation,
    title = {
      @Composable
      fun InnerText() {
        Text(
          text = titleTextState.title,
          maxLines = if (titleTextState.oneLine) 1 else Int.MAX_VALUE,
          overflow = TextOverflow.Ellipsis,
          modifier = Modifier.let {
            if (titleTextState.onClick != null) {
              it.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
              ) { titleTextState.onClick.invoke() }
            } else {
              it
            }
          }
        )
      }

      if(titleTextState.spotlightText == null) {
        InnerText()
      } else {
        SpotlightTooltip(
          title = titleTextState.spotlightText.first,
          description = titleTextState.spotlightText.second
        ) {
          InnerText()
        }
      }
    },
    scrollBehavior = scrollBehavior
  )
}


@Composable
fun OpenDrawerNavigationButton() {
  val toggleDrawer = LocalModalDrawerToggler.current
  IconButton(onClick = { toggleDrawer(true) }) {
    Icon(
      imageVector = Icons.Rounded.Menu,
      contentDescription = Res.string.menu.compose
    )
  }
}

@Composable
fun BackNavigationButton(goBack: () -> Unit) {
  IconButton(onClick = goBack) {
    Icon(
      imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
      contentDescription = Res.string.back.compose
    )
  }
}