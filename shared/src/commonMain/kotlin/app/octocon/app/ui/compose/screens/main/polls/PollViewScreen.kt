package app.octocon.app.ui.compose.screens.main.polls

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.HowToVote
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.window.PopupProperties
import app.octocon.app.ui.compose.components.DeletePollDialog
import app.octocon.app.ui.compose.components.shared.BackNavigationButton
import app.octocon.app.ui.compose.components.shared.IndeterminateProgressSpinner
import app.octocon.app.ui.compose.components.shared.OctoLargeTopBar
import app.octocon.app.ui.compose.components.shared.OctoScaffold
import app.octocon.app.ui.compose.components.shared.TitleTextState
import app.octocon.app.ui.compose.screens.main.polls.pollview.PollViewResultsTab
import app.octocon.app.ui.compose.screens.main.polls.pollview.PollViewSettingsTab
import app.octocon.app.ui.compose.utils.SpotlightTooltip
import app.octocon.app.ui.model.main.SaveState
import app.octocon.app.ui.model.main.polls.PollViewComponent
import app.octocon.app.utils.compose
import app.octocon.app.utils.derive
import app.octocon.app.utils.savedState
import app.octocon.app.utils.state
import com.arkivanov.decompose.extensions.compose.pages.ChildPages
import com.arkivanov.decompose.extensions.compose.pages.PagesScrollAnimation
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import octoconapp.shared.generated.resources.Res
import octoconapp.shared.generated.resources.delete_poll
import octoconapp.shared.generated.resources.loading
import octoconapp.shared.generated.resources.revert_changes
import octoconapp.shared.generated.resources.saving
import octoconapp.shared.generated.resources.tooltip_revert_changes_desc


@Composable
fun PollViewScreen(
  component: PollViewComponent
) {
  val api = component.api

  val settings by component.settings.collectAsState()
  val reduceMotion by derive { settings.reduceMotion }

  val pages by component.pages.subscribeAsState()

  val model = component.model

  val poll by model.apiPoll.collectAsState()
  val title by model.title.collectAsState()
  val initialPoll by model.initialPoll.collectAsState()

  val saveState by model.saveState.collectAsState()

  var deletePollDialogOpen by state(false)

  val loading = Res.string.loading.compose
  val saving = Res.string.saving.compose
  val titleText by derive {
    when {
      poll == null -> loading
      saveState == SaveState.Saving -> saving
      else -> title.ifBlank { initialPoll?.title }.orEmpty()
    }
  }

  OctoScaffold(
    topBar = { topAppBarState, scrollBehavior, _ ->
      OctoLargeTopBar(
        navigation = { BackNavigationButton { component.navigateBack() } },
        actions = {
          TopBarActions(
            revertChanges = component.model::revertChanges,
            launchDeletePoll = { deletePollDialogOpen = true },
          )
        },
        titleTextState = TitleTextState(title = titleText, oneLine = false),
        topAppBarState = topAppBarState,
        scrollBehavior = scrollBehavior
      )
    },
    floatingActionButton = {
      AnimatedContent(
        pages.selectedIndex
      ) { index ->
        when(index) {
          0 -> FloatingActionButton(
            onClick = {
              pages.items[0].instance?.let {
                (it as? PollViewComponent.Child.ResultsChild)
                  ?.component
                  ?.openCastVoteDialog()
              }
            },
            content = {
              Icon(
                imageVector = Icons.Rounded.HowToVote,
                contentDescription = null
              )
            }
          )

          1 -> {
            val component = pages.items[1].instance?.let {
              (it as? PollViewComponent.Child.SettingsChild)
                ?.component
            } ?: return@AnimatedContent

            if(component.isChoicePoll) {
              FloatingActionButton(
                onClick = component::openCreateChoiceDialog,
                content = {
                  Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = null
                  )
                }
              )
            }
          }
        }
      }
    }
  ) { _, showSnackbar ->
    LaunchedEffect(showSnackbar) {
      component.updateShowSnackbar(showSnackbar)
    }
    Surface(
      modifier = Modifier.fillMaxSize()
    ) {
      Column(
        modifier = Modifier.fillMaxSize()
      ) {
        PrimaryTabRow(selectedTabIndex = pages.selectedIndex) {
          PollViewComponent.Child.allMetadata.forEach {
            val tabActive = pages.selectedIndex == it.index
            Tab(
              selected = tabActive,
              onClick = {
                if (!tabActive) { component.navigateToPage(it.index) }
              },
              text = {
                Text(
                  it.title,
                  maxLines = 2,
                  overflow = TextOverflow.Ellipsis,
                  color = if (tabActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            )
          }
        }

        if (poll == null) {
          IndeterminateProgressSpinner()
          return@Column
        } else {
          ChildPages(
            pages = component.pages,
            onPageSelected = component::navigateToPage,
            scrollAnimation = if(reduceMotion) PagesScrollAnimation.Disabled else PagesScrollAnimation.Default
          ) { _, page ->
            when(page) {
              is PollViewComponent.Child.ResultsChild -> PollViewResultsTab(
                page.component
              )

              is PollViewComponent.Child.SettingsChild -> PollViewSettingsTab(
                page.component
              )
            }
          }
        }
      }
    }

    if (deletePollDialogOpen) {
      DeletePollDialog(
        poll = poll!!,
        launchDeletePoll = {
          api.deletePoll(it)
          component.navigateBack()
        },
        onDismissRequest = { deletePollDialogOpen = false }
      )
    }
  }
}

@Composable
private fun RowScope.TopBarActions(
  revertChanges: () -> Unit,
  launchDeletePoll: () -> Unit,
) {
  var optionsExpanded by savedState(false)
  IconButton(onClick = {
    optionsExpanded = !optionsExpanded
  }) {
    Icon(
      imageVector = Icons.Rounded.MoreVert,
      contentDescription = null
    )
  }
  DropdownMenu(
    expanded = optionsExpanded,
    onDismissRequest = { optionsExpanded = false },
    properties = PopupProperties()
  ) {
    SpotlightTooltip(
      title = Res.string.revert_changes.compose,
      description = Res.string.tooltip_revert_changes_desc.compose
    ) {
      DropdownMenuItem(
        text = { Text(Res.string.revert_changes.compose) },
        onClick = {
          revertChanges()
          optionsExpanded = false
        },
        leadingIcon = {
          Icon(
            Icons.AutoMirrored.Rounded.Undo,
            contentDescription = null
          )
        })
    }
    DropdownMenuItem(
      text = { Text(Res.string.delete_poll.compose) },
      onClick = {
        launchDeletePoll()
        optionsExpanded = false
      },
      leadingIcon = {
        Icon(
          Icons.Rounded.Delete,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.error
        )
      }
    )
  }
}