package app.octocon.app.ui.compose.screens.main.hometabs.alters

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CreateNewFolder
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Numbers
import androidx.compose.material.icons.rounded.SortByAlpha
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
import app.octocon.app.AlterSortingMethod
import app.octocon.app.api.APIState
import app.octocon.app.ui.compose.LocalChildPanelsMode
import app.octocon.app.ui.compose.LocalUpdateLazyListState
import app.octocon.app.ui.compose.components.CreateTagDialog
import app.octocon.app.ui.compose.components.DeleteTagDialog
import app.octocon.app.ui.compose.components.shared.BackNavigationButton
import app.octocon.app.ui.compose.components.shared.IndeterminateProgressSpinner
import app.octocon.app.ui.compose.components.shared.OctoLargeTopBar
import app.octocon.app.ui.compose.components.shared.OctoScaffold
import app.octocon.app.ui.compose.components.shared.TitleTextState
import app.octocon.app.ui.compose.screens.main.hometabs.alters.tagview.TagViewContentsTab
import app.octocon.app.ui.compose.screens.main.hometabs.alters.tagview.TagViewSettingsTab
import app.octocon.app.ui.compose.theme.ThemeFromColor
import app.octocon.app.ui.compose.utils.SpotlightTooltip
import app.octocon.app.ui.model.main.SaveState
import app.octocon.app.ui.model.main.hometabs.alters.TagViewComponent
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
import octoconapp.shared.generated.resources.create_subtag
import octoconapp.shared.generated.resources.delete_tag
import octoconapp.shared.generated.resources.revert_changes
import octoconapp.shared.generated.resources.saving
import octoconapp.shared.generated.resources.sort_alphabetically
import octoconapp.shared.generated.resources.sort_by_id
import octoconapp.shared.generated.resources.tooltip_add_subtag_desc
import octoconapp.shared.generated.resources.tooltip_alter_ids_desc
import octoconapp.shared.generated.resources.tooltip_alter_ids_title
import octoconapp.shared.generated.resources.tooltip_delete_tag_desc
import octoconapp.shared.generated.resources.tooltip_revert_changes_desc

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalDecomposeApi::class)
@Composable
fun TagViewScreen(
  component: TagViewComponent
) {
  val api = component.api
  val model = component.model
  val pages by component.pages.subscribeAsState()

  val allAlters by api.alters.collectAsState()
  val tags by api.tags.collectAsState()
  val fronts by api.fronts.collectAsState()

  val settings by component.settings.collectAsState()
  val colorMode by derive { settings.colorMode }
  val dynamicColorType by derive { settings.dynamicColorType }
  val colorContrastLevel by derive { settings.colorContrastLevel }
  val amoledMode by derive { settings.amoledMode }
  val reduceMotion by derive { settings.reduceMotion }

  val color by model.color.collectAsState()
  val updateLazyListState = LocalUpdateLazyListState.current

  var deleteTagDialogOpen by savedState(false)
  var createSubtagDialogOpen by savedState(false)

  val lazyListState = rememberLazyListState()

  val initialTag by model.initialTag.collectAsState()

  val isLoaded by model.isLoaded.collectAsState()

  val name by model.name.collectAsState()
  val saveState by model.saveState.collectAsState()

  LaunchedEffect(true) {
    updateLazyListState(lazyListState)
  }

  val titleText = when {
    saveState == SaveState.Saving -> Res.string.saving.compose
    else -> name
  }

  ThemeFromColor(
    color,
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
          actions = {
            TopBarActions(
              revertChanges = component.model::revertChanges,
              launchDeleteTag = { deleteTagDialogOpen = true },
              launchCreateSubtag = { createSubtagDialogOpen = true },
              setAlterSortingMethod = component::setAlterSortingMethod
            )
          },
          titleTextState = TitleTextState(title = titleText, oneLine = false),
          topAppBarState = topAppBarState,
          scrollBehavior = scrollBehavior
        )
      },
      floatingActionButton = {
        AnimatedVisibility(
          pages.selectedIndex == 0,
          enter = fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)),
          exit = fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMediumLow))
        ) {
          FloatingActionButton(
            onClick = {
              pages.items[0].instance?.let {
                (it as? TagViewComponent.Child.ContentsChild)
                  ?.component
                  ?.openAddAlterDialog()
              }
            },
            content = {
              Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = null
              )
            }
          )
        }
      }
    ) {  _, showSnackbar ->
      LaunchedEffect(showSnackbar) {
        component.updateShowSnackbar(showSnackbar)
      }

      val markdownColors = markdownColor()

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
              TagViewComponent.Child.allMetadata.forEach {
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

            when {
              !isLoaded || allAlters !is APIState.Success || fronts !is APIState.Success || tags !is APIState.Success ->
                IndeterminateProgressSpinner()

              else -> {
                ChildPages(
                  pages = component.pages,
                  onPageSelected = component::navigateToPage,
                  scrollAnimation = if(reduceMotion) PagesScrollAnimation.Disabled else PagesScrollAnimation.Default
                ) { _, page ->
                  when(page) {
                    is TagViewComponent.Child.ContentsChild -> TagViewContentsTab(
                      page.component
                    )

                    is TagViewComponent.Child.SettingsChild -> TagViewSettingsTab(
                      page.component
                    )
                  }
                }

                if (deleteTagDialogOpen) {
                  DeleteTagDialog(
                    tag = initialTag!!,
                    launchDeleteTag = {
                      api.deleteTag(it)
                      component.navigateBack()
                    },
                    onDismissRequest = { deleteTagDialogOpen = false }
                  )
                }

                if (createSubtagDialogOpen) {
                  CreateTagDialog(
                    launchCreateTag = { api.createTag(it, model.id) },
                    onDismissRequest = { createSubtagDialogOpen = false },
                    createTagText = Res.string.create_subtag.compose
                  )
                }
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun RowScope.TopBarActions(
  setAlterSortingMethod: (AlterSortingMethod) -> Unit,
  revertChanges: () -> Unit,
  launchCreateSubtag: () -> Unit,
  launchDeleteTag: () -> Unit,
) {
  var sortingExpanded by state(false)
  var optionsExpanded by state(false)
  SpotlightTooltip(
    title = Res.string.create_subtag.compose,
    description = Res.string.tooltip_add_subtag_desc.compose
  ) {
    IconButton(
      onClick = launchCreateSubtag
    ) {
      Icon(
        imageVector = Icons.Rounded.CreateNewFolder,
        contentDescription = null
      )
    }
  }
  IconButton(onClick = {
    sortingExpanded = !sortingExpanded
  }) {
    Icon(
      imageVector = Icons.AutoMirrored.Rounded.Sort,
      contentDescription = null
    )
  }
  DropdownMenu(
    expanded = sortingExpanded,
    onDismissRequest = { sortingExpanded = false },
    properties = PopupProperties()
  ) {
    DropdownMenuItem(
      text = { Text(Res.string.sort_alphabetically.compose) },
      onClick = {
        setAlterSortingMethod(AlterSortingMethod.ALPHABETICAL)
        sortingExpanded = false
      },
      leadingIcon = {
        Icon(
          Icons.Rounded.SortByAlpha,
          contentDescription = null
        )
      })
    SpotlightTooltip(
      title = Res.string.tooltip_alter_ids_title.compose,
      description = Res.string.tooltip_alter_ids_desc.compose
    ) {
      DropdownMenuItem(
        text = { Text(Res.string.sort_by_id.compose) },
        onClick = {
          setAlterSortingMethod(AlterSortingMethod.ID)
          sortingExpanded = false
        },
        leadingIcon = {
          Icon(
            Icons.Rounded.Numbers,
            contentDescription = null
          )
        }
      )
    }
  }
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
    SpotlightTooltip(
      title = Res.string.delete_tag.compose,
      description = Res.string.tooltip_delete_tag_desc.compose
    ) {
      DropdownMenuItem(
        text = { Text(Res.string.delete_tag.compose) },
        onClick = {
          launchDeleteTag()
          optionsExpanded = false
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