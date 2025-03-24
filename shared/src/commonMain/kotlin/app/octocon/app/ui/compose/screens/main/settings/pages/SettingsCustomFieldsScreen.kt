package app.octocon.app.ui.compose.screens.main.settings.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import app.octocon.app.api.model.CustomField
import app.octocon.app.ui.compose.LocalChildPanelsMode
import app.octocon.app.ui.compose.components.CreateCustomFieldDialog
import app.octocon.app.ui.compose.components.CustomFieldItemCard
import app.octocon.app.ui.compose.components.DeleteCustomFieldDialog
import app.octocon.app.ui.compose.components.EditCustomFieldNameDialog
import app.octocon.app.ui.compose.components.EditCustomFieldSecurityLevelSheet
import app.octocon.app.ui.compose.components.shared.BackNavigationButton
import app.octocon.app.ui.compose.components.shared.IndeterminateProgressSpinner
import app.octocon.app.ui.compose.components.shared.OctoScaffold
import app.octocon.app.ui.compose.components.shared.OctoTopBar
import app.octocon.app.ui.compose.components.shared.TitleTextState
import app.octocon.app.ui.compose.screens.GLOBAL_PADDING
import app.octocon.app.ui.model.interfaces.ApiInterface
import app.octocon.app.ui.model.main.settings.SettingsCustomFieldsComponent
import app.octocon.app.utils.compose
import app.octocon.app.utils.derive
import app.octocon.app.utils.state
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.router.panels.ChildPanelsMode

import octoconapp.shared.generated.resources.Res
import octoconapp.shared.generated.resources.custom_fields
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@OptIn(ExperimentalDecomposeApi::class)
@Composable
fun SettingsCustomFieldsScreen(
  component: SettingsCustomFieldsComponent
) {
  val api: ApiInterface = component.api
  val initComplete by api.initComplete.collectAsState()
  val system by api.systemMe.collectAsState()

  val customFields by derive { system.ensureData.fields }

  var createFieldDialogOpen by state(false)

  var fieldToDelete by state<CustomField?>()
  var fieldToEditName by state<CustomField?>()
  var fieldToEditSecurityLevel by state<CustomField?>()

  if (!initComplete || !system.isSuccess) {
    IndeterminateProgressSpinner()
    return
  }

  val customFieldInternalList by derive { customFields.toMutableStateList() }
  val listState = rememberLazyListState()
  val haptics = LocalHapticFeedback.current

  val reorderableLazyListState = rememberReorderableLazyListState(listState) { from, to ->
    customFieldInternalList.add(to.index, customFieldInternalList.removeAt(from.index))
    haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
  }

  OctoScaffold(
    topBar = { topAppBarState, scrollBehavior, showSnackbar ->
      OctoTopBar(
        navigation = {
          val childPanelsMode = LocalChildPanelsMode.current

          if(childPanelsMode == ChildPanelsMode.SINGLE) {
            BackNavigationButton(component::navigateBack)
          }
        },
        titleTextState = TitleTextState(Res.string.custom_fields.compose),
        topAppBarState = topAppBarState,
        scrollBehavior = scrollBehavior
      )
    },
    floatingActionButton = {
      FloatingActionButton(
        onClick = { createFieldDialogOpen = true },
        content = {
          Icon(
            imageVector = Icons.Rounded.Add,
            contentDescription = null
          )
        }
      )
    }
  ) { _, _ ->
    LazyColumn(
      state = listState,
      contentPadding = PaddingValues(vertical = GLOBAL_PADDING),
      modifier = Modifier.fillMaxSize(),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      if (customFields.isEmpty()) {
        item(key = "__empty__") {
          Card(
            colors = CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.secondaryContainer
            ),
            elevation = CardDefaults.cardElevation(
              defaultElevation = 1.0.dp
            ),
            modifier = Modifier.padding(GLOBAL_PADDING)
          ) {
            Column(
              modifier = Modifier.padding(16.dp).fillMaxWidth()
            ) {
              Text(
                "You don't have any custom fields yet!",
                style = MaterialTheme.typography.titleMedium
              )
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                "Create one to get started:",
                style = MaterialTheme.typography.bodyMedium.merge(
                  lineHeight = 1.5.em
                )
              )
              Spacer(modifier = Modifier.height(12.dp))
              Button(
                onClick = { createFieldDialogOpen = true },
                colors = ButtonDefaults.buttonColors(
                  containerColor = MaterialTheme.colorScheme.primary,
                  contentColor = MaterialTheme.colorScheme.onPrimary
                )
              ) {
                Text("Create a field")
              }
            }
          }
        }
      }

      itemsIndexed(customFieldInternalList, key = { _, field -> field.id }) { index, field ->
        ReorderableItem(reorderableLazyListState, key = field.id) { isDragging ->
          CustomFieldItemCard(
            isDragging = isDragging,
            customField = field,
            onDragStarted = {
              haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            },
            onDragStopped = {
              api.relocateCustomField(field.id, customFieldInternalList.indexOf(field))
              haptics.performHapticFeedback(HapticFeedbackType.GestureEnd)
            },
            launchDelete = { fieldToDelete = it },
            launchEditName = { fieldToEditName = it },
            launchLockField = {
              api.editCustomField(
                it.id,
                it.copy(locked = !it.locked)
              )
            },
            launchUnlockField = {
              api.editCustomField(
                it.id,
                it.copy(locked = !it.locked)
              )
            },
            launchEditSecurityLevel = { fieldToEditSecurityLevel = it }
          )
        }
      }
    }

    if (createFieldDialogOpen) {
      CreateCustomFieldDialog(
        onDismissRequest = { createFieldDialogOpen = false },
        createCustomField = api::createCustomField
      )
    }

    if (fieldToDelete != null) {
      DeleteCustomFieldDialog(
        field = fieldToDelete!!,
        onDismissRequest = { fieldToDelete = null },
        launchDelete = {
          api.deleteCustomField(it.id)
          fieldToDelete = null
        }
      )
    }

    if (fieldToEditName != null) {
      EditCustomFieldNameDialog(
        field = fieldToEditName!!,
        onDismissRequest = { fieldToEditName = null },
        launchEditName = { field, name ->
          if (field.name != name) {
            api.editCustomField(field.id, field.copy(name = name))
          }
          fieldToEditName = null
        }
      )
    }

    if (fieldToEditSecurityLevel != null) {
      EditCustomFieldSecurityLevelSheet(
        field = fieldToEditSecurityLevel!!,
        onDismissRequest = { fieldToEditSecurityLevel = null },
        launchEditSecurityLevel = { field, securityLevel ->
          if (field.securityLevel != securityLevel) {
            api.editCustomField(field.id, field.copy(securityLevel = securityLevel))
          }
          fieldToEditSecurityLevel = null
        }
      )
    }
  }
}
