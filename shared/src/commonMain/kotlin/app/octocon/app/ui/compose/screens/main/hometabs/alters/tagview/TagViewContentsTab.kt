package app.octocon.app.ui.compose.screens.main.hometabs.alters.tagview

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.FolderDelete
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import app.octocon.app.api.APIState
import app.octocon.app.api.model.MyAlter
import app.octocon.app.api.model.MyFrontItem
import app.octocon.app.api.model.MySystem
import app.octocon.app.api.model.MyTag
import app.octocon.app.ui.compose.LocalUpdateLazyListState
import app.octocon.app.ui.compose.components.AlterContextSheet
import app.octocon.app.ui.compose.components.DeleteAlterDialog
import app.octocon.app.ui.compose.components.DeleteTagDialog
import app.octocon.app.ui.compose.components.EditFrontDialog
import app.octocon.app.ui.compose.components.LazyAlterList
import app.octocon.app.ui.compose.components.TagContextSheet
import app.octocon.app.ui.compose.components.shared.AttachAlterDialog
import app.octocon.app.ui.compose.components.shared.BottomSheetListItem
import app.octocon.app.ui.compose.screens.GLOBAL_PADDING
import app.octocon.app.ui.compose.utils.SpotlightTooltip
import app.octocon.app.ui.model.main.hometabs.alters.tagview.TagViewContentsComponent
import app.octocon.app.utils.compose
import app.octocon.app.utils.derive
import app.octocon.app.utils.ioDispatcher
import app.octocon.app.utils.savedState
import octoconapp.shared.generated.resources.Res
import octoconapp.shared.generated.resources.add_alter
import octoconapp.shared.generated.resources.empty_tag_card_body
import octoconapp.shared.generated.resources.empty_tag_card_button
import octoconapp.shared.generated.resources.empty_tag_card_title
import octoconapp.shared.generated.resources.remove_from_tag
import octoconapp.shared.generated.resources.tag_no_valid_alters
import octoconapp.shared.generated.resources.tooltip_remove_alter_from_tag_desc
import octoconapp.shared.generated.resources.tooltip_remove_alter_from_tag_title
import octoconapp.shared.generated.resources.unnamed_alter

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun TagViewContentsTab(
  component: TagViewContentsComponent
) {
  val api = component.api
  val updateLazyListState = LocalUpdateLazyListState.current
  val model = component.model

  val system by api.systemMe.collectAsState()
  val tags by api.tags.collectAsState()
  val fronts by api.fronts.collectAsState()
  val allAlters by api.alters.collectAsState()

  val tagAlters by model.alters.collectAsState()

  val lazyListState = rememberLazyListState()

  val imageScope = rememberCoroutineScope()
  val placeholderPainter = rememberVectorPainter(Icons.Rounded.Person)

  @Suppress("LocalVariableName")
  val unnamed_alter = Res.string.unnamed_alter.compose

  val settings by component.settings.collectAsState()

  var addAlterDialogOpen by savedState(false)

  val alterSortingMethod by derive { settings.alterSortingMethod }

  val childTags by derive {
    when (tags) {
      is APIState.Success -> tags.ensureData.filter { it.parentTagID == model.id }
      else -> emptyList()
    }
  }

  val sortedFronts: List<MyFrontItem> by derive {
    if (!fronts.isSuccess) return@derive emptyList()
    val validFronts = fronts.ensureData.filter { it.front.alterID in tagAlters }
    val primaryFront =
      (system as? APIState.Success<MySystem>)?.data?.primaryFront
    val primaryFrontItem =
      validFronts.find { it.front.alterID == primaryFront }

    val rest =
      validFronts.filter { it.front.alterID != primaryFront }
        .sortedBy { it.front.timeStart }

    return@derive if (primaryFrontItem != null) {
      listOf(primaryFrontItem).plus(rest)
    } else {
      rest
    }
  }

  val sortedAlters: List<MyAlter> by derive {
    if (!allAlters.isSuccess) return@derive emptyList()
    val validAlters = allAlters.ensureData.filter { it.id in tagAlters }
    val frontingAlters: MutableList<MyAlter> = mutableListOf()

    val alterIds = validAlters.map { it.id }

    sortedFronts.forEach { front ->
      if (front.front.alterID in alterIds) {
        frontingAlters.add(validAlters.find { it.id == front.front.alterID }!!)
      }
    }

    val nonFrontingAlters: MutableList<MyAlter> =
      validAlters
        .filter { alter -> alter.id !in frontingAlters.map { it.id } }
        .let { alterSortingMethod.sortAlters(it, unnamed_alter) }
        .toMutableList()

    return@derive frontingAlters.plus(nonFrontingAlters)
  }

  val frontingData: Set<Pair<Int, String?>> = remember(sortedFronts) {
    sortedFronts.map { Pair(it.front.alterID, it.front.comment) }.toSet()
  }

  var selectedAlter by savedState<Int?>(null)
  var selectedTag by savedState<String?>(null)
  var frontToEdit by savedState<MyFrontItem?>(null)
  var alterToDelete by savedState<MyAlter?>(null)
  var tagToDelete by savedState<MyTag?>(null)

  LaunchedEffect(true) {
    updateLazyListState(lazyListState)
    component.updateOpenAddAlterDialogFun { addAlterDialogOpen = it }
  }

  Box(
    modifier = Modifier.fillMaxSize()
  ) {
    if (sortedAlters.isEmpty() && childTags.isEmpty()) {
      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(GLOBAL_PADDING)
      ) {
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
              modifier = Modifier.padding(16.dp).fillMaxWidth()
            ) {
              Text(
                Res.string.empty_tag_card_title.compose,
                style = MaterialTheme.typography.titleMedium
              )
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                Res.string.empty_tag_card_body.compose,
                style = MaterialTheme.typography.bodyMedium.merge(
                  lineHeight = 1.5.em
                )
              )
              Spacer(modifier = Modifier.height(12.dp))
              Button(
                onClick = {
                  addAlterDialogOpen = true
                },
                colors = ButtonDefaults.buttonColors(
                  containerColor = MaterialTheme.colorScheme.primary,
                  contentColor = MaterialTheme.colorScheme.onPrimary
                )
              ) {
                Text(Res.string.empty_tag_card_button.compose)
              }
            }
          }
        }
      }
    } else {
      LazyAlterList(
        allAlters = allAlters.ifError { emptyList() },
        sortedAlters = sortedAlters,
        tags = childTags,
        lazyListState = lazyListState,
        imageContext = imageScope.coroutineContext + ioDispatcher,
        placeholderPainter = placeholderPainter,
        setSelectedAlter = { selectedAlter = it },
        frontingData = frontingData,
        primaryFront = (system as? APIState.Success)?.data?.primaryFront,
        setSelectedTag = { selectedTag = it },
        launchViewAlter = { component.navigateToAlterView(it) },
        launchOpenTag = { component.navigateToTagView(it) },
        launchStartFront = { api.startFront(it) },
        launchEndFront = { api.endFront(it) },
        launchSetPrimaryFront = { api.setPrimaryFront(it) },
        isNested = true,
        folderPainter = rememberVectorPainter(Icons.Rounded.Folder),
        settings = component.settings
      )
    }

    if (selectedAlter != null) {
      val alterData = allAlters.ensureData.find { it.id == selectedAlter }!!

      AlterContextSheet(
        onDismissRequest = { selectedAlter = null },
        selectedAlter = selectedAlter!!,
        isFronting = selectedAlter in frontingData.map { it.first },
        isPrimary = (system as? APIState.Success<MySystem>)?.data?.primaryFront == selectedAlter,
        isPinned = alterData.pinned,
        launchEditAlter = {
          /*tagViewContext.tryExit {
            parentNavigator.push(AlterView(it))
          }*/
        },
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
        launchPinOrUnpin = api::setAlterPinned,
        extraContent = {
          SpotlightTooltip(
            title = Res.string.tooltip_remove_alter_from_tag_title.compose,
            description = Res.string.tooltip_remove_alter_from_tag_desc.compose
          ) {
            BottomSheetListItem(
              imageVector = Icons.Rounded.FolderDelete,
              iconTint = MaterialTheme.colorScheme.error,
              title = Res.string.remove_from_tag.compose
            ) {
              component.detachAlter(selectedAlter!!)
              selectedAlter = null
            }
          }
        }
      )
    }

    if (selectedTag != null) {
      TagContextSheet(
        onDismissRequest = { selectedTag = null },
        selectedTag = selectedTag!!,
        launchOpenTag = { tagID ->
          /*tagViewContext.tryExit {
            parentNavigator.push(TagView(tagID))
          }*/
        },
        launchDeleteTag = { tagID ->
          tagToDelete = tags.ensureData.find { it.id == tagID }
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
          api.editFrontComment(frontID, editedComment)
        },
        alterName = allAlters.ensureData.find { it.id == frontToEdit!!.front.alterID }!!.name
          ?: Res.string.unnamed_alter.compose,
        onDismissRequest = { frontToEdit = null }
      )
    }

    if (addAlterDialogOpen) {
      AttachAlterDialog(
        existingAlters = tagAlters,
        alters = allAlters.ensureData,
        onDismissRequest = { addAlterDialogOpen = false },
        placeholderPainter = placeholderPainter,
        launchAttachAlter = { component.attachAlter(it) },
        attachText = Res.string.add_alter.compose,
        noAltersText = Res.string.tag_no_valid_alters.compose,
        settings = settings
      )
    }
  }
}