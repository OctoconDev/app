package app.octocon.app.ui.compose.screens.main.hometabs.alters

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CreateNewFolder
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Numbers
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material.icons.rounded.SortByAlpha
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.window.PopupProperties
import app.octocon.app.AlterSortingMethod
import app.octocon.app.api.APIState
import app.octocon.app.api.ChannelMessage
import app.octocon.app.api.model.MyAlter
import app.octocon.app.api.model.MyFrontItem
import app.octocon.app.api.model.MySystem
import app.octocon.app.api.model.MyTag
import app.octocon.app.ui.compose.LocalNavigationType
import app.octocon.app.ui.compose.LocalUpdateLazyListState
import app.octocon.app.ui.compose.NavigationType
import app.octocon.app.ui.compose.components.AlterContextSheet
import app.octocon.app.ui.compose.components.CreateAlterDialog
import app.octocon.app.ui.compose.components.CreateTagDialog
import app.octocon.app.ui.compose.components.DeleteAlterDialog
import app.octocon.app.ui.compose.components.DeleteTagDialog
import app.octocon.app.ui.compose.components.EditFrontDialog
import app.octocon.app.ui.compose.components.LazyAlterList
import app.octocon.app.ui.compose.components.TagContextSheet
import app.octocon.app.ui.compose.components.shared.IndeterminateProgressSpinner
import app.octocon.app.ui.compose.components.shared.OctoScaffold
import app.octocon.app.ui.compose.components.shared.OctoTopBar
import app.octocon.app.ui.compose.components.shared.OpenDrawerNavigationButton
import app.octocon.app.ui.compose.components.shared.TitleTextState
import app.octocon.app.ui.compose.screens.GLOBAL_PADDING
import app.octocon.app.ui.compose.utils.SpotlightTooltip
import app.octocon.app.ui.model.main.hometabs.alters.AlterListComponent
import app.octocon.app.utils.compose
import app.octocon.app.utils.derive
import app.octocon.app.utils.ioDispatcher
import app.octocon.app.utils.savedState
import app.octocon.app.utils.state
import octoconapp.shared.generated.resources.Res
import octoconapp.shared.generated.resources.alters
import octoconapp.shared.generated.resources.create_alter
import octoconapp.shared.generated.resources.create_tag
import octoconapp.shared.generated.resources.new_user_card_body
import octoconapp.shared.generated.resources.new_user_card_button
import octoconapp.shared.generated.resources.new_user_card_title
import octoconapp.shared.generated.resources.no_alters_body
import octoconapp.shared.generated.resources.no_alters_title
import octoconapp.shared.generated.resources.sort_alphabetically
import octoconapp.shared.generated.resources.sort_by_id
import octoconapp.shared.generated.resources.tags
import octoconapp.shared.generated.resources.tooltip_alter_ids_desc
import octoconapp.shared.generated.resources.tooltip_alter_ids_title
import octoconapp.shared.generated.resources.tooltip_alters_desc
import octoconapp.shared.generated.resources.tooltip_tags_desc
import octoconapp.shared.generated.resources.unnamed_alter


@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun AlterListScreen(
  component: AlterListComponent
) {
  val api = component.api
  // var alreadySentSnack by savedState(false)

  var createAlterDialogOpen by savedState(false)
  var createTagDialogOpen by savedState(false)

  val initComplete by api.initComplete.collectAsState()
  val system by api.systemMe.collectAsState()
  val alters by api.alters.collectAsState()
  val tags by api.tags.collectAsState()
  val fronts by api.fronts.collectAsState()

  val latestEvent by api.eventFlow.collectAsState(null)

  val settings by component.settings.collectAsState()

  @Suppress("LocalVariableName")
  val unnamed_alter = Res.string.unnamed_alter.compose

  val alterSortingMethod by derive { settings.alterSortingMethod }
  val hideAltersInTags by derive { settings.hideAltersInTags }

  val alterExclusionList by derive {
    val frontData = fronts.ensureData
    if (hideAltersInTags) {
      val frontAlterIds = frontData.mapTo(mutableSetOf()) { it.alter.id }
      alters.ensureData
        .asSequence()
        .map { it.id }
        .filter { it !in frontAlterIds && tags.ensureData.any { tag -> tag.alters.contains(it) } }
        .toList()
    } else {
      emptyList()
    }
  }

  val sortedFronts: List<MyFrontItem> by derive {
    if (!fronts.isSuccess) return@derive emptyList()

    val primaryFront = (system as? APIState.Success<MySystem>)?.data?.primaryFront
    val (primary, rest) = fronts.ensureData.partition { it.front.alterID == primaryFront }

    return@derive primary + rest.sortedBy { it.front.timeStart }
  }

  val sortedAlters: List<MyAlter> by derive {
    if (!alters.isSuccess || !tags.isSuccess) return@derive emptyList()

    val altersById = mutableMapOf<Int, MyAlter>()
    val frontingAlters = mutableListOf<MyAlter>()

    alters.ensureData.forEach { alter ->
      altersById[alter.id] = alter
    }

    sortedFronts.forEach { front ->
      altersById.remove(front.front.alterID)?.let { frontingAlters.add(it) }
    }

    val nonFrontingAlters = altersById.values
      .toList()
      .let { alterSortingMethod.sortAlters(it, unnamed_alter) }

    frontingAlters + nonFrontingAlters
  }

  val pinnedAlters: List<Int> = remember(alters, alterSortingMethod, unnamed_alter) {
    if (!alters.isSuccess) return@remember emptyList()
    alters.ensureData
      .filter { it.pinned }
      .let { alterSortingMethod.sortAlters(it, unnamed_alter) }
      .map { it.id }
  }

  val rootTags: List<MyTag> by derive {
    if (!tags.isSuccess) return@derive emptyList()
    tags.ensureData.filter { it.parentTagID == null }
  }

  val lazyListState = rememberLazyListState()
  val updateLazyListState = LocalUpdateLazyListState.current

  LaunchedEffect(true) {
    updateLazyListState(lazyListState)
  }

  LaunchedEffect(latestEvent) {
    when (latestEvent) {
      is ChannelMessage.AlterCreated -> {
        component.navigateToAlterView((latestEvent as ChannelMessage.AlterCreated).alter.id)
      }

      is ChannelMessage.TagCreated -> {
        component.navigateToTagView((latestEvent as ChannelMessage.TagCreated).tag.id)
      }

      else -> Unit
    }
  }

  OctoScaffold(
    hasHoistedBottomBar = true,
    topBar = { topAppBarState, scrollBehavior, showSnackbar ->
      OctoTopBar(
        titleTextState = TitleTextState(
          Res.string.alters.compose,
          spotlightText = Res.string.alters.compose to Res.string.tooltip_alters_desc.compose
        ),
        navigation = {
          val navigationType = LocalNavigationType.current

          if(navigationType == NavigationType.BOTTOM_BAR) {
            OpenDrawerNavigationButton()
          }
        },
        actions = {
          if (alters.isSuccess) {
            TopBarActions(
              launchCreateAlter = { createAlterDialogOpen = true },
              launchCreateTag = { createTagDialogOpen = true },
              setAlterSortingMethod = { component.settings.setAlterSortingMethod(it) }
            )
          }
        },
        topAppBarState = topAppBarState,
        scrollBehavior = scrollBehavior
      )
    },
    floatingActionButton = {
      if (alters.isSuccess) {
        FloatingActionButton(
          onClick = { createAlterDialogOpen = true },
          content = {
            Icon(
              imageVector = Icons.Rounded.PersonAdd,
              contentDescription = null
            )
          }
        )
      }
    }
  ) { _, _ ->
    when {
      !initComplete ->
        IndeterminateProgressSpinner()

      alters !is APIState.Success || fronts !is APIState.Success || tags !is APIState.Success ->
        IndeterminateProgressSpinner()

      else -> {
        val imageScope = rememberCoroutineScope()
        val placeholderPainter = rememberVectorPainter(Icons.Rounded.Person)
        val folderPainter = rememberVectorPainter(Icons.Rounded.Folder)

        val frontingData: Set<Pair<Int, String?>> = remember(sortedFronts) {
          sortedFronts.map { Pair(it.front.alterID, it.front.comment) }.toSet()
        }

        var selectedAlter by savedState<Int?>(null)
        var selectedTag by savedState<String?>(null)

        var frontToEdit by savedState<MyFrontItem?>(null)

        var alterToDelete by savedState<MyAlter?>(null)
        var tagToDelete by savedState<MyTag?>(null)

        LazyAlterList(
          allAlters = alters.ifError { emptyList() },
          sortedAlters = sortedAlters,
          pinnedAlters = pinnedAlters,
          excludeList = alterExclusionList,
          tags = rootTags,
          lazyListState = lazyListState,
          imageContext = imageScope.coroutineContext + ioDispatcher,
          placeholderPainter = placeholderPainter,
          folderPainter = folderPainter,
          setSelectedAlter = { selectedAlter = it },
          setSelectedTag = { selectedTag = it },
          frontingData = frontingData,
          primaryFront = (system as? APIState.Success)?.data?.primaryFront,
          launchViewAlter = { component.navigateToAlterView(it) },
          launchOpenTag = { component.navigateToTagView(it) },
          launchStartFront = { api.startFront(it) },
          launchEndFront = { api.endFront(it) },
          launchSetPrimaryFront = { api.setPrimaryFront(it) },
          emptyContent = {
            item {
              Card(
                colors = CardDefaults.cardColors(
                  containerColor = MaterialTheme.colorScheme.secondaryContainer
                ),
                elevation = CardDefaults.cardElevation(
                  defaultElevation = 1.0.dp
                )
              ) {
                Column(
                  modifier = Modifier.padding(GLOBAL_PADDING).fillMaxWidth()
                ) {
                  Text(
                    Res.string.no_alters_title.compose,
                    style = MaterialTheme.typography.titleMedium
                  )
                  Spacer(modifier = Modifier.height(8.dp))
                  Text(
                    Res.string.no_alters_body.compose,
                    style = MaterialTheme.typography.bodyMedium.merge(
                      lineHeight = 1.5.em
                    )
                  )
                  Spacer(modifier = Modifier.height(12.dp))
                  Button(onClick = { createAlterDialogOpen = true }) {
                    Text(Res.string.create_alter.compose)
                  }
                }
              }
            }
            item {
              Card(
                colors = CardDefaults.cardColors(
                  containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                elevation = CardDefaults.cardElevation(
                  defaultElevation = 1.0.dp
                )
              ) {
                Column(
                  modifier = Modifier.padding(16.dp).fillMaxWidth()
                ) {
                  Text(
                    Res.string.new_user_card_title.compose,
                    style = MaterialTheme.typography.titleMedium
                  )
                  Spacer(modifier = Modifier.height(8.dp))
                  Text(
                    Res.string.new_user_card_body.compose,
                    style = MaterialTheme.typography.bodyMedium.merge(
                      lineHeight = 1.5.em
                    )
                  )
                  Spacer(modifier = Modifier.height(12.dp))
                  FilledTonalButton(onClick = {}) {
                    Text(Res.string.new_user_card_button.compose)
                  }
                }
              }
            }
          },
          isNested = false,
          settings = component.settings
        )

        if (selectedAlter != null) {
          val alterData = alters.ensureData.find { it.id == selectedAlter }!!

          AlterContextSheet(
            onDismissRequest = { selectedAlter = null },
            selectedAlter = selectedAlter!!,
            isFronting = selectedAlter in frontingData.map { it.first },
            isPrimary = (system as? APIState.Success<MySystem>)?.data?.primaryFront == selectedAlter,
            isPinned = alterData.pinned,
            launchEditAlter = component::navigateToAlterView,
            launchDeleteAlter = {
              alterToDelete = alterData
            },
            launchSetAsFront = api::setFront,
            launchAddToFront = api::startFront,
            launchEditFrontComment = { alterID ->
              frontToEdit = fronts.ensureData.find { it.front.alterID == alterID }
            },
            launchRemoveFromFront = api::endFront,
            launchSetAsPrimaryFront = api::setPrimaryFront,
            launchPinOrUnpin = api::setAlterPinned
          )
        }

        if (selectedTag != null) {
          TagContextSheet(
            onDismissRequest = { selectedTag = null },
            selectedTag = selectedTag!!,
            launchOpenTag = { tagID ->
              component.navigateToTagView(
                rootTags.find { it.id == tagID }!!.id
              )
            },
            launchDeleteTag = { tagID ->
              tagToDelete = rootTags.find { it.id == tagID }
            }
          )
        }

        if (alterToDelete != null) {
          DeleteAlterDialog(
            alter = alterToDelete!!,
            launchDeleteAlter = { selectedAlter = null; api.deleteAlter(it) },
            onDismissRequest = { alterToDelete = null }
          )
        }

        if (tagToDelete != null) {
          DeleteTagDialog(
            tag = tagToDelete!!,
            launchDeleteTag = { api.deleteTag(it) },
            onDismissRequest = { tagToDelete = null }
          )
        }

        if (frontToEdit != null) {
          EditFrontDialog(
            frontID = frontToEdit!!.front.id,
            comment = frontToEdit!!.front.comment.orEmpty(),
            launchEditComment = { frontID, editedComment ->
              api.editFrontComment(
                frontID,
                editedComment
              )
            },
            alterName = alters.ensureData.find { it.id == frontToEdit!!.front.alterID }!!.name
              ?: Res.string.unnamed_alter.compose,
            onDismissRequest = { frontToEdit = null }
          )
        }

        if (createAlterDialogOpen) {
          CreateAlterDialog(
            launchCreateAlter = { api.createAlter(it) },
            onDismissRequest = { createAlterDialogOpen = false }
          )
        }

        if (createTagDialogOpen) {
          CreateTagDialog(
            launchCreateTag = { api.createTag(it) },
            onDismissRequest = { createTagDialogOpen = false }
          )
        }
      }
    }
  }
}

@Composable
private fun RowScope.TopBarActions(
  launchCreateAlter: () -> Unit,
  launchCreateTag: () -> Unit,
  setAlterSortingMethod: (AlterSortingMethod) -> Unit,
) {
  var createExpanded by state(false)
  var sortingExpanded by state(false)

  IconButton(onClick = {
    createExpanded = !createExpanded
  }) {
    Icon(
      imageVector = Icons.Rounded.Add,
      contentDescription = null
    )
  }
  DropdownMenu(
    expanded = createExpanded,
    onDismissRequest = { createExpanded = false },
    properties = PopupProperties()
  ) {
    SpotlightTooltip(
      title = Res.string.alters.compose,
      description = Res.string.tooltip_alters_desc.compose
    ) {
      DropdownMenuItem(
        text = { Text(Res.string.create_alter.compose) },
        onClick = {
          launchCreateAlter()
          createExpanded = false
        },
        leadingIcon = {
          Icon(
            Icons.Rounded.PersonAdd,
            contentDescription = null
          )
        }
      )
    }
    SpotlightTooltip(
      title = Res.string.tags.compose,
      description = Res.string.tooltip_tags_desc.compose
    ) {
      DropdownMenuItem(
        text = { Text(Res.string.create_tag.compose) },
        onClick = {
          launchCreateTag()
          createExpanded = false
        },
        leadingIcon = {
          Icon(
            Icons.Rounded.CreateNewFolder,
            contentDescription = null
          )
        }
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
}