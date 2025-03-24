package app.octocon.app.ui.compose.screens.main.hometabs.alters

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material.icons.rounded.Brush
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.window.PopupProperties
import app.octocon.app.ui.compose.LocalChildPanelsMode
import app.octocon.app.ui.compose.components.DeleteJournalEntryDialog
import app.octocon.app.ui.compose.components.EditJournalTitleDialog
import app.octocon.app.ui.compose.components.UnencryptedWarningDialog
import app.octocon.app.ui.compose.components.shared.BackNavigationButton
import app.octocon.app.ui.compose.components.shared.IndeterminateProgressSpinner
import app.octocon.app.ui.compose.components.shared.OctoLargeTopBar
import app.octocon.app.ui.compose.components.shared.OctoScaffold
import app.octocon.app.ui.compose.components.shared.TitleTextState
import app.octocon.app.ui.compose.components.shared.UpdateColorDialog
import app.octocon.app.ui.compose.theme.ThemeFromColor
import app.octocon.app.ui.compose.utils.JournalEntryMarkdownTextField
import app.octocon.app.ui.compose.utils.SpotlightTooltip
import app.octocon.app.ui.model.main.SaveState
import app.octocon.app.ui.model.main.hometabs.alters.AlterJournalEntryViewComponent
import app.octocon.app.utils.compose
import app.octocon.app.utils.derive
import app.octocon.app.utils.localeFormatNumber
import app.octocon.app.utils.savedState
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.router.panels.ChildPanelsMode
import com.mikepenz.markdown.m3.markdownColor
import kotlinx.coroutines.delay
import octoconapp.shared.generated.resources.Res
import octoconapp.shared.generated.resources.confirm
import octoconapp.shared.generated.resources.delete_journal_entry
import octoconapp.shared.generated.resources.revert_changes
import octoconapp.shared.generated.resources.saving
import octoconapp.shared.generated.resources.tooltip_revert_changes_desc

@OptIn(ExperimentalDecomposeApi::class)
@Composable
fun AlterJournalEntryViewScreen(
  component: AlterJournalEntryViewComponent
) {
  val api = component.api
  val model = component.model

  val settings by component.settings.collectAsState()

  val initialEntry by model.initialEntry.collectAsState()

  val title by model.title.collectAsState()
  val color by model.color.collectAsState()
  val contentState by model.contentState.collectAsState()
  val showUnencryptedWarning by model.showUnencryptedWarning.collectAsState()

  var deleteEntryDialogOpen by savedState(false)
  var colorDialogOpen by savedState(false)
  var editTitleDialogOpen by savedState(false)

  val saveState by model.saveState.collectAsState()
  val isLoaded by model.isLoaded.collectAsState()

  val saving = Res.string.saving.compose

  val alters by api.alters.collectAsState()

  val alterColor by derive { if(alters.isSuccess) alters.ensureData.find { it.id == component.alterID }?.color else null }

  val focusRequester = remember { FocusRequester() }
  val interactionSource = remember { MutableInteractionSource() }

  // val contentTextState = rememberTextFieldState()

  var isEditing by savedState(false)

  LaunchedEffect(isLoaded) {
    if(isLoaded) {
      isEditing = contentState.ensureContent.isNullOrBlank()
    }
  }

  /*LaunchedEffect(isLoaded) {
    if (isLoaded) {
      isEditing = contentState.ensureContent.isNullOrBlank()
      contentTextState.edit {
        replace(0, length, contentState.ensureContent.orEmpty())
      }
    }
  }*/

  LaunchedEffect(isEditing) {
    if(isEditing) {
      delay(100)
      focusRequester.requestFocus()
    }
  }

  val titleTextState by derive {
    if(saveState == SaveState.Saving) {
      TitleTextState(title = saving, oneLine = false)
    } else {
      TitleTextState(title = title, onClick = {
        editTitleDialogOpen = true
      }, oneLine = false)
    }
  }

  ThemeFromColor(
    color ?: alterColor,
    colorMode = settings.colorMode,
    dynamicColorType = settings.dynamicColorType,
    colorContrastLevel = settings.colorContrastLevel,
    amoledMode = settings.amoledMode
  ) {
    val markdownColors = markdownColor()

    OctoScaffold(
      hasHoistedBottomBar = true,
      topBar = { topAppBarState, scrollBehavior, showSnackbar ->
        OctoLargeTopBar(
          titleTextState = titleTextState,
          navigation = {
            val childPanelsMode = LocalChildPanelsMode.current

            if(childPanelsMode == ChildPanelsMode.SINGLE) {
              BackNavigationButton(component::navigateBack)
            }
          },
          actions = {
            TopBarActions(
              ready = isLoaded,
              revertChanges = model::revertChanges,
              launchChangeColor = { colorDialogOpen = true },
              launchDeleteEntry = { deleteEntryDialogOpen = true }
            )
          },
          topAppBarState = topAppBarState,
          scrollBehavior = scrollBehavior
        )
      },
      floatingActionButton = {
        AnimatedVisibility(
          visible = isEditing,
          enter = fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)),
          exit = fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMediumLow))
        ) {
          FloatingActionButton(
            onClick = { isEditing = false },
          ) {
            Icon(
              imageVector = Icons.Rounded.Done,
              contentDescription = Res.string.confirm.compose
            )
          }
        }
      },
    ) {  padding, showSnackbar ->
      LaunchedEffect(showSnackbar) {
        component.updateShowSnackbar(showSnackbar)
      }

      Surface(
        modifier = Modifier.fillMaxSize().clickable(
          interactionSource = remember { MutableInteractionSource() },
          indication = null
        ) {
          try {
            focusRequester.requestFocus()
          } catch (_: Exception) {
          }
        },
      ) {
        if (!isLoaded) {
          IndeterminateProgressSpinner()
          return@Surface
        }

        JournalEntryMarkdownTextField(
          isEditing = isEditing,
          updateIsEditing = { isEditing = it },
          contentState = contentState,
          // contentTextState = contentTextState,
          updateContent = model::updateContent,
          padding = padding,
          focusRequester = focusRequester,
          interactionSource = interactionSource,
          markdownColors = markdownColors
        ) {
          item {
            Text(
              text = "${localeFormatNumber(contentState.ensureContent?.length ?: 0)} / ${localeFormatNumber(30_000)}",
              style = MaterialTheme.typography.labelSmall
            )
          }
        }

        if (showUnencryptedWarning) {
          UnencryptedWarningDialog(onDismissRequest = model::dismissUnencryptedWarning)
        }

        if (deleteEntryDialogOpen) {
          DeleteJournalEntryDialog(
            journalEntry = initialEntry!!,
            onDismissRequest = { deleteEntryDialogOpen = false },
            launchDeleteJournalEntry = {
              api.deleteAlterJournalEntry(it.id)
              component.navigateBack()
            }
          )
        }

        if (editTitleDialogOpen) {
          EditJournalTitleDialog(
            initialTitle = title,
            launchEditJournalTitle = model::updateTitle,
            onDismissRequest = { editTitleDialogOpen = false }
          )
        }

        if (colorDialogOpen) {
          UpdateColorDialog(
            initialColor = color,
            updateColor = model::updateColor,
            onDismissRequest = { colorDialogOpen = false },
            settings = settings
          )
        }
      }
    }
  }
}

@Composable
private fun RowScope.TopBarActions(
  ready: Boolean,
  revertChanges: () -> Unit,
  launchDeleteEntry: () -> Unit,
  launchChangeColor: () -> Unit
) {
  var isExpanded by savedState(false)
  IconButton(onClick = launchChangeColor) {
    Icon(
      imageVector = Icons.Rounded.Brush,
      contentDescription = "Change color"
    )
  }
  Box {
    IconButton(onClick = {
      isExpanded = !isExpanded
    }) {
      Icon(
        imageVector = Icons.Rounded.MoreVert,
        contentDescription = null
      )
    }
    DropdownMenu(
      expanded = isExpanded,
      onDismissRequest = { isExpanded = false },
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
            isExpanded = false
          },
          enabled = ready,
          leadingIcon = {
            Icon(
              Icons.AutoMirrored.Rounded.Undo,
              contentDescription = null
            )
          })
      }
      DropdownMenuItem(
        text = { Text(Res.string.delete_journal_entry.compose) },
        onClick = {
          launchDeleteEntry()
          isExpanded = false
        },
        leadingIcon = {
          Icon(
            Icons.Rounded.Delete,
            tint = MaterialTheme.colorScheme.error,
            contentDescription = null
          )
        }
      )
    }
  }
}
