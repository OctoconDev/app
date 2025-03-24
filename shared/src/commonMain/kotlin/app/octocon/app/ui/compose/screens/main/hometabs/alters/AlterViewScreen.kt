package app.octocon.app.ui.compose.screens.main.hometabs.alters

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.window.PopupProperties
import app.octocon.app.ui.compose.LocalChildPanelsMode
import app.octocon.app.ui.compose.components.DeleteAlterDialog
import app.octocon.app.ui.compose.components.shared.BackNavigationButton
import app.octocon.app.ui.compose.components.shared.IndeterminateProgressSpinner
import app.octocon.app.ui.compose.components.shared.OctoLargeTopBar
import app.octocon.app.ui.compose.components.shared.OctoScaffold
import app.octocon.app.ui.compose.components.shared.TitleTextState
import app.octocon.app.ui.compose.screens.main.hometabs.alters.alterview.AlterViewBasicInfoTab
import app.octocon.app.ui.compose.screens.main.hometabs.alters.alterview.AlterViewFieldsTab
import app.octocon.app.ui.compose.screens.main.hometabs.alters.alterview.AlterViewJournalTab
import app.octocon.app.ui.compose.theme.ThemeFromColor
import app.octocon.app.ui.compose.utils.SpotlightTooltip
import app.octocon.app.ui.model.main.SaveState
import app.octocon.app.ui.model.main.hometabs.alters.AlterViewComponent
import app.octocon.app.utils.compose
import app.octocon.app.utils.derive
import app.octocon.app.utils.savedState
import app.octocon.app.utils.state
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.pages.ChildPages
import com.arkivanov.decompose.extensions.compose.pages.PagesScrollAnimation
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.arkivanov.decompose.router.panels.ChildPanelsMode
import com.mikepenz.markdown.compose.LocalMarkdownColors
import com.mikepenz.markdown.m3.markdownColor
import octoconapp.shared.generated.resources.Res
import octoconapp.shared.generated.resources.delete_alter
import octoconapp.shared.generated.resources.revert_changes
import octoconapp.shared.generated.resources.saving
import octoconapp.shared.generated.resources.tooltip_revert_changes_desc
import octoconapp.shared.generated.resources.unnamed_alter

@OptIn(ExperimentalDecomposeApi::class)
@Composable
fun AlterViewScreen(
  component: AlterViewComponent
) {
  val model = component.model

  val isLoaded by model.isLoaded.collectAsState()

  val name by model.name.collectAsState()
  val color by model.color.collectAsState()

  val saveState by model.saveState.collectAsState()
  val settings by component.settings.collectAsState()

  val colorMode by derive { settings.colorMode }
  val dynamicColorType by derive { settings.dynamicColorType }
  val amoledMode by derive { settings.amoledMode }
  val colorContrastLevel by derive { settings.colorContrastLevel }

  val reduceMotion by derive { settings.reduceMotion }

  val initialAlter by model.initialAlter.collectAsState()

  var deleteAlterDialogOpen by savedState(false)

  @Suppress("LocalVariableName")
  val unnamed_alter = Res.string.unnamed_alter.compose
  val saving = Res.string.saving.compose

  val pages by component.pages.subscribeAsState()

  val titleText by derive {
    when {
      !isLoaded -> component.initialName ?: unnamed_alter
      saveState == SaveState.Saving -> saving
      name != null -> name!!
      else -> unnamed_alter
    }
  }

  ThemeFromColor(
    if (!isLoaded) component.initialColor else color,
    colorMode = colorMode,
    dynamicColorType = dynamicColorType,
    colorContrastLevel = colorContrastLevel,
    amoledMode = amoledMode
  ) {
    OctoScaffold(
      hasHoistedBottomBar = true,
      topBar = { topAppBarState, scrollBehavior, showSnackbar ->
        OctoLargeTopBar(
          navigation = {
            val childPanelsMode = LocalChildPanelsMode.current

            if(childPanelsMode == ChildPanelsMode.SINGLE) {
              BackNavigationButton(component::navigateBack)
            }
          },
          titleTextState = TitleTextState(titleText, oneLine = false),
          actions = {
            var expanded by state(false)
            TopBarActions(
              toggleExpanded = { expanded = it },
              isExpanded = expanded,
              revertChanges = model::revertChanges,
              tryDeleteAlter = { deleteAlterDialogOpen = true }
            )
          },
          topAppBarState = topAppBarState,
          scrollBehavior = scrollBehavior
        )
      },
      floatingActionButton = {
        AnimatedVisibility(
          pages.selectedIndex == 2,
          enter = fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)),
          exit = fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMediumLow))
        ) {
          FloatingActionButton(
            onClick = {
              pages.items[2].instance?.let {
                (it as? AlterViewComponent.Child.JournalChild)
                  ?.component
                  ?.openCreateJournalEntryDialog()
              }
            },
            content = {
              Icon(
                imageVector = Icons.Rounded.Edit,
                contentDescription = null
              )
            }
          )
        }
      }
    ) {  _, showSnackbar ->
      val markdownColors = markdownColor()

      LaunchedEffect(showSnackbar) {
        component.updateShowSnackbar(showSnackbar)
      }

      CompositionLocalProvider(
        LocalMarkdownColors provides markdownColors
      ) {
        Surface(
          modifier = Modifier.fillMaxSize()
        ) {
          Column(
            modifier = Modifier.fillMaxSize()
          ) {
            PrimaryTabRow(selectedTabIndex = pages.selectedIndex) {
              AlterViewComponent.Child.allMetadata.forEach {
                val tabActive = pages.selectedIndex == it.index
                SpotlightTooltip(
                  title = it.spotlightTitle,
                  description = it.spotlightDescription
                ) {
                  Tab(
                    selected = tabActive,
                    onClick = {
                      if (!tabActive) {
                        component.navigateToPage(it.index)
                      }
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
            }
            if (isLoaded) {
              ChildPages(
                pages = component.pages,
                onPageSelected = component::navigateToPage,
                scrollAnimation = if(reduceMotion) PagesScrollAnimation.Disabled else PagesScrollAnimation.Default
              ) { _, page ->
                when (page) {
                  is AlterViewComponent.Child.BasicInfoChild -> AlterViewBasicInfoTab(
                    page.component
                  )

                  is AlterViewComponent.Child.FieldsChild -> AlterViewFieldsTab(
                    page.component
                  )

                  is AlterViewComponent.Child.JournalChild -> AlterViewJournalTab(
                    page.component
                  )
                }
              }

              if (deleteAlterDialogOpen) {
                DeleteAlterDialog(
                  alter = initialAlter!!,
                  onDismissRequest = { deleteAlterDialogOpen = false },
                  launchDeleteAlter = { component.deleteAlter() },
                  afterDelete = component::navigateBack
                )
              }
            } else {
              IndeterminateProgressSpinner()
            }
          }
        }
      }
    }
  }
}

@Composable
private fun RowScope.TopBarActions(
  toggleExpanded: (Boolean) -> Unit,
  isExpanded: Boolean,
  revertChanges: () -> Unit,
  tryDeleteAlter: () -> Unit,
) {
  IconButton(onClick = {
    toggleExpanded(!isExpanded)
  }) {
    Icon(
      imageVector = Icons.Rounded.MoreVert,
      contentDescription = null
    )
  }
  DropdownMenu(
    expanded = isExpanded,
    onDismissRequest = { toggleExpanded(false) },
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
          toggleExpanded(false)
        },
        leadingIcon = {
          Icon(
            Icons.AutoMirrored.Rounded.Undo,
            contentDescription = null
          )
        })
    }
    DropdownMenuItem(
      text = { Text(Res.string.delete_alter.compose) },
      onClick = {
        tryDeleteAlter()
        toggleExpanded(false)
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