package app.octocon.app.ui.compose.screens.main.hometabs.alters.tagview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import app.octocon.app.api.APIState
import app.octocon.app.api.model.MyTag
import app.octocon.app.api.model.SecurityLevel
import app.octocon.app.ui.compose.LocalUpdateLazyListState
import app.octocon.app.ui.compose.components.LocalFieldFocusRequester
import app.octocon.app.ui.compose.components.shared.AttachTagDialog
import app.octocon.app.ui.compose.components.shared.UpdateColorDialog
import app.octocon.app.ui.compose.components.shared.rememberCollectPressInteractionSource
import app.octocon.app.ui.compose.screens.GLOBAL_PADDING
import app.octocon.app.ui.compose.theme.hexStringToARGBInt
import app.octocon.app.ui.compose.utils.MarkdownOutlinedTextField
import app.octocon.app.ui.model.main.hometabs.alters.tagview.TagViewSettingsComponent
import app.octocon.app.utils.compose
import app.octocon.app.utils.derive
import app.octocon.app.utils.savedState
import app.octocon.app.utils.state
import octoconapp.shared.generated.resources.Res
import octoconapp.shared.generated.resources.color
import octoconapp.shared.generated.resources.description
import octoconapp.shared.generated.resources.name
import octoconapp.shared.generated.resources.no_color
import octoconapp.shared.generated.resources.no_description
import octoconapp.shared.generated.resources.no_other_tags
import octoconapp.shared.generated.resources.no_parent_tag
import octoconapp.shared.generated.resources.parent_tag
import octoconapp.shared.generated.resources.remove_parent_tag
import octoconapp.shared.generated.resources.security_level
import octoconapp.shared.generated.resources.set_parent_tag
import octoconapp.shared.generated.resources.tag_name

@Composable
fun TagViewSettingsTab(
  component: TagViewSettingsComponent
) {
  val api = component.api
  val settings by component.settings.collectAsState()
  val model = component.model

  val updateLazyListState = LocalUpdateLazyListState.current

  val tags by api.tags.collectAsState()

  val apiTag by model.apiTag.collectAsState()

  val name by model.name.collectAsState()
  val description by model.description.collectAsState()
  val color by model.color.collectAsState()
  val securityLevel by model.securityLevel.collectAsState()
  val parentTagID by model.parentTagID.collectAsState()

  val parentTag by derive {
    tags.ensureData.find { it.id == parentTagID }
  }

  val lazyListState = rememberLazyListState()

  var colorDialogOpen by savedState(false)
  var setParentTagDialogOpen by savedState(false)

  val deepChildTagIDs by derive {
    when (tags) {
      is APIState.Success -> getTagChildren(
        apiTag!!,
        tags.ensureData
      ).map { it.id }
      else -> emptyList()
    }
  }

  LaunchedEffect(true) {
    updateLazyListState(lazyListState)
  }

  val focusRequester = remember { FocusRequester() }

  val allSecurityLevels = remember { SecurityLevel.entries }

  // val nameState = rememberTextFieldState(name)
  // val descriptionState = rememberTextFieldState(description.orEmpty())

  /*UpdateTextFieldStatesOnLoad(
    model.isLoaded,
    nameState to model.name,
    descriptionState to model.description
  )*/

  CompositionLocalProvider(
    LocalFieldFocusRequester provides focusRequester,
  ) {
    LazyColumn(
      modifier = Modifier.fillMaxSize().imePadding(),
      state = lazyListState,
      contentPadding = PaddingValues(vertical = GLOBAL_PADDING),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      item {
        TextField(
          value = name,
          onValueChange = model::updateName,
          // state = nameState,
          // inputTransformation = ModelTransformation(model::updateName),
          placeholder = { Text(Res.string.tag_name.compose) },
          label = { Text(Res.string.name.compose) },
          singleLine = true,
          // lineLimits = TextFieldLineLimits.SingleLine,
          modifier = Modifier.fillMaxWidth()
            .padding(bottom = 6.dp, start = GLOBAL_PADDING, end = GLOBAL_PADDING)
        )
      }
      item {
        MarkdownOutlinedTextField(
          value = description,
          onValueChange = model::updateDescription,
          label = Res.string.description.compose,
          placeholder = Res.string.no_description.compose,
          modifier = Modifier.fillMaxWidth().padding(horizontal = GLOBAL_PADDING, vertical = 8.dp)
        )
        /*MarkdownOutlinedTextField(
          internalValue = description,
          state = descriptionState,
          update = model::updateDescription,
          label = Res.string.description.compose,
          placeholder = Res.string.no_description.compose,
          modifier = Modifier.fillMaxWidth()
            .padding(bottom = 6.dp, start = GLOBAL_PADDING, end = GLOBAL_PADDING)
        )*/
      }
      item {
        OutlinedTextField(
          value = color ?: Res.string.no_color.compose,
          onValueChange = {},
          readOnly = true,
          singleLine = true,
          interactionSource = rememberCollectPressInteractionSource {
            colorDialogOpen = true
          },
          suffix = if (color != null) {
            {
              Box(
                modifier = Modifier
                  .size(16.dp)
                  .clip(MaterialTheme.shapes.extraSmall)
                  .background(Color(hexStringToARGBInt(color!!)))
              )
            }
          } else null,
          label = { Text(Res.string.color.compose) },
          modifier = Modifier.fillMaxWidth()
            .padding(bottom = 6.dp, start = GLOBAL_PADDING, end = GLOBAL_PADDING)
        )
      }
      item {
        var expanded by state(false)

        ExposedDropdownMenuBox(
          expanded = expanded,
          onExpandedChange = { expanded = !expanded },
        ) {
          OutlinedTextField(

            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
              .fillMaxWidth()
              .padding(bottom = 6.dp, start = GLOBAL_PADDING, end = GLOBAL_PADDING),
            readOnly = true,
            value = securityLevel.displayName,
            onValueChange = {},
            label = { Text(Res.string.security_level.compose) },
            trailingIcon = {
              Icon(
                securityLevel.icon,
                contentDescription = null,
              )
            },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
          )
          ExposedDropdownMenu(
            modifier = Modifier.padding(horizontal = GLOBAL_PADDING),
            expanded = expanded,
            onDismissRequest = { expanded = false },
          ) {
            allSecurityLevels.forEach { level ->
              DropdownMenuItem(
                text = { Text(level.displayName) },
                trailingIcon = {
                  Icon(
                    level.icon,
                    contentDescription = null, // level.description("alter"),
                    modifier = Modifier.offset(x = 4.dp)
                  )
                },
                onClick = {
                  model.updateSecurityLevel(level)
                  expanded = false
                },
                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
              )
            }
          }
        }
      }

      item {
        Row(
          modifier = Modifier.fillMaxWidth()
            .padding(horizontal = GLOBAL_PADDING),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          OutlinedTextField(
            value = parentTag?.name ?: Res.string.no_parent_tag.compose,
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            interactionSource = rememberCollectPressInteractionSource {
              setParentTagDialogOpen = true
            },
            label = { Text(Res.string.parent_tag.compose) },
            modifier = Modifier.weight(1f),
          )
          if (parentTag != null) {
            FilledTonalIconButton(
              onClick = {
                component.removeParentTagID()
                /*tagViewContext.parentNavigator.replaceAll(
                  listOf(
                    AltersList,
                    tagViewContext.tagViewInstance
                  )
                )*/
              },
              modifier = Modifier.padding(top = 4.dp)
            ) {
              Icon(
                Icons.Rounded.Close,
                contentDescription = Res.string.remove_parent_tag.compose
              )
            }
          }
        }
      }
    }
    if (colorDialogOpen) {
      UpdateColorDialog(
        initialColor = color,
        updateColor = model::updateColor,
        onDismissRequest = { colorDialogOpen = false },
        settings = settings
      )
    }

    if (setParentTagDialogOpen) {
      AttachTagDialog(
        existingTags = deepChildTagIDs + model.id + listOfNotNull(parentTagID),
        tags = tags.ensureData,
        onDismissRequest = { setParentTagDialogOpen = false },
        launchAttachTag = { component.setParentTagID(it) },
        attachText = Res.string.set_parent_tag.compose,
        noTagsText = Res.string.no_other_tags.compose,
        folderPainter = rememberVectorPainter(Icons.Rounded.Folder),
        settings = settings
      )
    }
  }

}

fun getTagChildren(tag: MyTag, allTags: List<MyTag>): List<MyTag> {
  val result = mutableListOf<MyTag>()
  val queue = ArrayDeque<MyTag>()
  val visited = mutableSetOf<String>()

  queue.add(tag)

  while (queue.isNotEmpty()) {
    val currentTag = queue.removeFirst()

    if (currentTag.id in visited) continue
    visited.add(currentTag.id)

    val children = allTags.filter { it.parentTagID == currentTag.id }
    result.addAll(children)
    queue.addAll(children)
  }

  return result
}