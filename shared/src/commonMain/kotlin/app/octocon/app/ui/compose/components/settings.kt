package app.octocon.app.ui.compose.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.DragIndicator
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import app.octocon.app.Settings
import app.octocon.app.api.model.CustomField
import app.octocon.app.api.model.CustomFieldType
import app.octocon.app.api.model.SecurityLevel
import app.octocon.app.ui.compose.components.shared.BottomSheetListItem
import app.octocon.app.ui.compose.components.shared.CardGroupPosition
import app.octocon.app.ui.compose.components.shared.NoRippleInteractionSource
import app.octocon.app.ui.compose.components.shared.OctoBottomSheet
import app.octocon.app.ui.compose.screens.GLOBAL_PADDING
import app.octocon.app.ui.compose.theme.getSubsectionStyle
import app.octocon.app.ui.compose.utils.SpotlightTooltip
import app.octocon.app.utils.ColorSchemeParams
import app.octocon.app.utils.compose
import app.octocon.app.utils.composeColorSchemeParams
import app.octocon.app.utils.savedState
import app.octocon.app.utils.state
import octoconapp.shared.generated.resources.Res
import octoconapp.shared.generated.resources.action_irreversible
import octoconapp.shared.generated.resources.cancel
import octoconapp.shared.generated.resources.confirm_delete_field
import octoconapp.shared.generated.resources.create
import octoconapp.shared.generated.resources.create_custom_field
import octoconapp.shared.generated.resources.create_custom_field_icon
import octoconapp.shared.generated.resources.custom_field_options
import octoconapp.shared.generated.resources.delete
import octoconapp.shared.generated.resources.delete_field
import octoconapp.shared.generated.resources.edit
import octoconapp.shared.generated.resources.edit_field_name
import octoconapp.shared.generated.resources.edit_name
import octoconapp.shared.generated.resources.lock_field
import octoconapp.shared.generated.resources.locked
import octoconapp.shared.generated.resources.name
import octoconapp.shared.generated.resources.type
import octoconapp.shared.generated.resources.unlock_field
import org.jetbrains.compose.resources.stringResource
import sh.calvin.reorderable.ReorderableCollectionItemScope
import kotlin.enums.enumEntries
import kotlin.math.roundToInt


@Suppress("FunctionName")
fun LazyListScope.SettingsSection(
  title: String? = null,
  settings: Settings,
  vararg items: @Composable (CardGroupPosition) -> Unit
) {
  if (title != null) {
    item {
      Row(
        modifier = Modifier.padding(vertical = 24.dp).fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text(
          title,
          style = getSubsectionStyle(settings.fontSizeScalar)
        )
      }
    }
  } else {
    item { Spacer(modifier = Modifier.height(GLOBAL_PADDING)) }
  }
  val itemsSize = items.size
  items.forEachIndexed { index, it ->
    item {
      val cardGroupPosition = if (itemsSize == 1) CardGroupPosition.SINGLE else
        when (index) {
          0 -> CardGroupPosition.START
          itemsSize - 1 -> CardGroupPosition.END
          else -> CardGroupPosition.MIDDLE
        }

      it(cardGroupPosition)
      // if (itemsSize > 1 && index != itemsSize - 1) Divider()
    }
  }
}

@Composable
fun SettingsNavigationItem(
  text: String,
  spotlightTitle: String = text,
  spotlightDescription: String,
  cardGroupPosition: CardGroupPosition,
  url: String,
  openURL: (String, ColorSchemeParams) -> Unit
) {
  val colorSchemeParams = composeColorSchemeParams
  SettingsNavigationItem(
    text,
    spotlightTitle,
    spotlightDescription,
    cardGroupPosition
  ) {
    openURL(url, colorSchemeParams)
  }
}

@Composable
fun SettingsNavigationItem(
  text: String,
  spotlightTitle: String = text,
  spotlightDescription: String,
  cardGroupPosition: CardGroupPosition,
  onClick: () -> Unit
) {
  SpotlightTooltip(
    title = spotlightTitle,
    description = spotlightDescription
  ) {
    Surface(
      modifier = Modifier.clip(cardGroupPosition.shape).fillMaxWidth().height(64.dp)
        .clickable(onClick = onClick),
      color = MaterialTheme.colorScheme.surfaceContainer
    ) {
      Row(
        modifier = Modifier.fillMaxSize().padding(horizontal = GLOBAL_PADDING),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Column(
          verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          Text(
            text = text,
            style = MaterialTheme.typography.labelLarge
          )
        }
        Icon(
          imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
          contentDescription = text,
          tint = MaterialTheme.colorScheme.tertiary,
          modifier = Modifier.size(24.dp)
        )
      }
    }
  }
}

@Composable
fun SettingsLoneButtonItem(
  text: String,
  isError: Boolean = false,
  spotlightTitle: String = text,
  spotlightDescription: String,
  cardGroupPosition: CardGroupPosition,
  onClick: () -> Unit
) {
  SpotlightTooltip(
    title = spotlightTitle,
    description = spotlightDescription
  ) {
    Surface(
      modifier = Modifier
        .clip(cardGroupPosition.shape)
        .fillMaxWidth()
        .height(64.dp),
      color = MaterialTheme.colorScheme.surfaceContainerLow

    ) {
      Row(
        modifier = Modifier.fillMaxSize().padding(horizontal = GLOBAL_PADDING),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
      ) {
        Button(
          colors = if (isError)
            ButtonDefaults.buttonColors(
              containerColor = MaterialTheme.colorScheme.error,
              contentColor = MaterialTheme.colorScheme.onError
            )
          else ButtonDefaults.buttonColors(),
          onClick = onClick
        ) {
          Text(text = text)
        }
      }
    }
  }
}


@Composable
fun SettingsToggleItem(
  text: String,
  value: Boolean,
  spotlightTitle: String = text,
  spotlightDescription: String,
  cardGroupPosition: CardGroupPosition,
  enabled: Boolean = true,
  updateValue: (Boolean) -> Unit,
) {
  val haptics = LocalHapticFeedback.current

  SpotlightTooltip(
    title = spotlightTitle,
    description = spotlightDescription
  ) {
    Surface(
      modifier = Modifier.clip(cardGroupPosition.shape).fillMaxWidth().height(64.dp),
      color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
      Row(
        modifier = Modifier.fillMaxSize().padding(horizontal = GLOBAL_PADDING),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Column(
          verticalArrangement = Arrangement.spacedBy(4.dp),
          modifier = Modifier.weight(1f)
        ) {
          Text(
            text = text,
            style = MaterialTheme.typography.labelLarge
          )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Switch(
          checked = value,
          onCheckedChange = {
            updateValue(it)
            haptics.performHapticFeedback(
              if (it) HapticFeedbackType.ToggleOn else HapticFeedbackType.ToggleOff
            )
          },
          enabled = enabled
        )
      }
    }
  }
}

@Composable
fun SettingsButtonItem(
  text: String,
  buttonText: String,
  spotlightTitle: String = text,
  spotlightDescription: String,
  enabled: Boolean = true,
  cardGroupPosition: CardGroupPosition,
  onClick: () -> Unit
) {
  SpotlightTooltip(
    title = spotlightTitle,
    description = spotlightDescription
  ) {
    Surface(
      modifier = Modifier.clip(cardGroupPosition.shape).fillMaxWidth().height(64.dp),
      color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
      Row(
        modifier = Modifier.fillMaxSize().padding(horizontal = GLOBAL_PADDING),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Column(
          verticalArrangement = Arrangement.spacedBy(4.dp),
          modifier = Modifier.weight(1f)
        ) {
          Text(
            text = text,
            style = MaterialTheme.typography.labelLarge
          )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Button(onClick = onClick, enabled = enabled) {
          Text(text = buttonText)
        }
      }
    }
  }
}

@Composable
fun SettingsDrawerItem(
  text: String,
  decorator: @Composable () -> Unit,
  value: String,
  spotlightTitle: String = text,
  spotlightDescription: String,
  cardGroupPosition: CardGroupPosition,
  onClick: () -> Unit
) {
  SpotlightTooltip(
    title = spotlightTitle,
    description = spotlightDescription
  ) {
    Surface(
      modifier = Modifier.clip(cardGroupPosition.shape).clickable(onClick = onClick).fillMaxWidth()
        .height(64.dp),
      color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
      Row(
        modifier = Modifier.fillMaxSize().padding(horizontal = GLOBAL_PADDING),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Column(
          verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          Text(
            text = text,
            style = MaterialTheme.typography.labelLarge
          )
          Text(
            text = value,
            style = MaterialTheme.typography.bodySmall
          )
        }
        decorator()
      }
    }
  }
}

@Composable
fun SettingsDrawerItem(
  text: String,
  icon: ImageVector,
  value: String,
  spotlightTitle: String = text,
  spotlightDescription: String,
  cardGroupPosition: CardGroupPosition,
  onClick: () -> Unit,
) =
  SettingsDrawerItem(
    text,
    {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(24.dp)
      )
    },
    value,
    spotlightTitle,
    spotlightDescription,
    cardGroupPosition,
    onClick
  )

@Composable
inline fun <reified T : Enum<T>> SettingsSliderItem(
  text: String,
  value: T,
  cardGroupPosition: CardGroupPosition,
  spotlightTitle: String = text,
  spotlightDescription: String,
  crossinline textualValue: @Composable (T) -> String,
  crossinline updateValue: (T) -> Unit
) {
  val entries = enumEntries<T>()
  val index = remember(value) { entries.indexOf(value) }
  var selectedIndex by savedState(index)

  val haptics = LocalHapticFeedback.current

  SpotlightTooltip(
    title = spotlightTitle,
    description = spotlightDescription
  ) {
    Surface(
      modifier = Modifier.clip(cardGroupPosition.shape).fillMaxWidth(),
      color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
      Column(
        modifier = Modifier.fillMaxSize().padding(GLOBAL_PADDING),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = text,
            style = MaterialTheme.typography.labelLarge
          )
          Text(
            text = textualValue(entries[selectedIndex]),
            style = MaterialTheme.typography.bodySmall.copy(textAlign = TextAlign.End)
          )
        }

        Spacer(modifier = Modifier.width(8.dp))
        Slider(
          value = selectedIndex.toFloat(),
          onValueChange = {
            val oldValue = selectedIndex
            val newValue = it.roundToInt()
            if (oldValue != newValue) {
              selectedIndex = newValue
              haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
            }
          },
          onValueChangeFinished = { updateValue(entries[selectedIndex]) },
          steps = entries.size - 2,
          valueRange = 0f..(entries.size - 1).toFloat(),
        )
      }
    }
  }
}

@Composable
fun CreateCustomFieldDialog(
  onDismissRequest: () -> Unit,
  createCustomField: (String, CustomFieldType) -> Unit,
) {
  var name by state("")
  var type by state(CustomFieldType.TEXT)

  val focusRequester = remember { FocusRequester() }

  AlertDialog(
    icon = {
      Icon(
        Icons.Rounded.Add,
        contentDescription = null
      )
    },
    title = {
      Text(text = Res.string.create_custom_field.compose)
    },
    text = {
      LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        item {
          TextField(
            value = name,
            onValueChange = {
              if (it.length > 100) return@TextField
              name = it
            },
            label = { Text(Res.string.name.compose) },
            modifier = Modifier.fillMaxWidth().focusRequester(focusRequester)
          )

          LaunchedEffect(true) {
            focusRequester.requestFocus()
          }
        }
        item {
          CustomFieldTypePicker(type) { type = it }
        }
      }
    },
    onDismissRequest = onDismissRequest,
    confirmButton = {
      TextButton(
        onClick = {
          createCustomField(name, type)
          onDismissRequest()
        },
        enabled = name.isNotBlank() && name.length in 1..100
      ) {
        Text(Res.string.create.compose)
      }
    },
    dismissButton = {
      TextButton(
        onClick = {
          onDismissRequest()
        }
      ) {
        Text(Res.string.cancel.compose)
      }
    }
  )
}

@Composable
fun ReorderableCollectionItemScope.CustomFieldItemCard(
  isDragging: Boolean,
  customField: CustomField,
  onDragStarted: (Offset) -> Unit,
  onDragStopped: () -> Unit,
  launchDelete: (CustomField) -> Unit,
  launchEditName: (CustomField) -> Unit,
  launchLockField: (CustomField) -> Unit,
  launchUnlockField: (CustomField) -> Unit,
  launchEditSecurityLevel: (CustomField) -> Unit,
) {
  val elevation by animateDpAsState(if (isDragging) 6.dp else 1.dp)
  val scale by animateFloatAsState(if (isDragging) 1.05f else 1f)
  val indicatorColor by animateColorAsState(
    if (isDragging) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
  )

  val containerColor by animateColorAsState(
    if (isDragging) MaterialTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.surfaceContainer
  )

  Surface(
    shadowElevation = elevation,
    shape = CardDefaults.elevatedShape,
    modifier = Modifier
      .fillMaxWidth()
      .padding(start = GLOBAL_PADDING, end = GLOBAL_PADDING)
      .scale(scale),
    color = containerColor
  ) {
    Row(
      modifier = Modifier.fillMaxSize(),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column(
        modifier = Modifier.fillMaxHeight().padding(horizontal = 6.dp),
        verticalArrangement = Arrangement.Center
      ) {
        Icon(
          imageVector = Icons.Rounded.DragIndicator,
          contentDescription = null,
          modifier = Modifier
            .longPressDraggableHandle(
              onDragStarted = onDragStarted,
              onDragStopped = onDragStopped
            ),
          tint = indicatorColor
        )
      }
      Column(
        modifier = Modifier.padding(end = 12.dp, top = 12.dp, bottom = 6.dp).fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        Row(
          horizontalArrangement = Arrangement.SpaceBetween,
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(
            text = customField.name,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.labelLarge,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
          )
          Box {
            var expanded by state(false)

            IconButton(
              onClick = { expanded = !expanded },
              modifier = Modifier.size(24.dp)
            ) {
              Icon(
                imageVector = Icons.Rounded.MoreVert,
                contentDescription = Res.string.custom_field_options.compose
              )
            }
            DropdownMenu(
              expanded = expanded,
              onDismissRequest = { expanded = false },
              properties = PopupProperties()
            ) {
              DropdownMenuItem(
                onClick = {
                  launchEditName(customField)
                  expanded = false
                },
                leadingIcon = {
                  Icon(
                    imageVector = Icons.Rounded.Edit,
                    contentDescription = null
                  )
                },
                text = { Text(Res.string.edit_name.compose) }
              )
              DropdownMenuItem(
                onClick = {
                  if (customField.locked) {
                    launchUnlockField(customField)
                  } else {
                    launchLockField(customField)
                  }
                  expanded = false
                },
                leadingIcon = {
                  Icon(
                    imageVector = if (customField.locked) Icons.Rounded.LockOpen else Icons.Rounded.Lock,
                    contentDescription = null
                  )
                },
                text = {
                  Text(stringResource(if (customField.locked) Res.string.unlock_field else Res.string.lock_field))
                }
              )
              DropdownMenuItem(
                onClick = {
                  launchDelete(customField)
                  expanded = false
                },
                leadingIcon = {
                  Icon(
                    imageVector = Icons.Rounded.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                  )
                },
                text = { Text(Res.string.delete_field.compose) }
              )
            }
          }
        }
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          AssistChip(
            leadingIcon = {
              Icon(
                customField.securityLevel.icon,
                contentDescription = null,
                modifier = Modifier.size(AssistChipDefaults.IconSize),
              )
            },
            label = { Text(customField.securityLevel.displayName) },
            onClick = { launchEditSecurityLevel(customField) },
          )

          AssistChip(
            leadingIcon = {
              Icon(
                customField.type.icon,
                contentDescription = customField.type.displayName,
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(AssistChipDefaults.IconSize),
              )
            },
            label = { Text(customField.type.displayName) },
            onClick = {},
            interactionSource = NoRippleInteractionSource
          )

          if (customField.locked) {
            AssistChip(
              leadingIcon = {
                Icon(
                  Icons.Rounded.Lock,
                  contentDescription = customField.type.displayName,
                  tint = MaterialTheme.colorScheme.tertiary,
                  modifier = Modifier.size(AssistChipDefaults.IconSize),
                )
              },
              label = { Text(Res.string.locked.compose) },
              onClick = {},
              interactionSource = NoRippleInteractionSource
            )
          }
        }
      }
    }
  }

}

@Composable
fun CustomFieldTypePicker(
  type: CustomFieldType,
  updateType: (CustomFieldType) -> Unit
) {
  var expanded by state(false)

  val types = remember { enumEntries<CustomFieldType>() }

  ExposedDropdownMenuBox(
    expanded = expanded,
    onExpandedChange = { expanded = !expanded }
  ) {
    OutlinedTextField(
      modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
      readOnly = true,
      value = type.displayName,
      onValueChange = {},
      label = { Text(Res.string.type.compose) },
      trailingIcon = {
        Icon(
          imageVector = type.icon,
          contentDescription = null
        )
      },
      colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
    )
    ExposedDropdownMenu(
      modifier = Modifier.padding(horizontal = GLOBAL_PADDING).fillMaxWidth(),
      // NOTE: .verticalScroll(rememberScrollState())
      expanded = expanded,
      onDismissRequest = { expanded = false },
    ) {
      types.forEach { entry ->
        DropdownMenuItem(
          text = { Text(entry.displayName) },
          trailingIcon = {
            Icon(
              entry.icon,
              contentDescription = null,
              modifier = Modifier.offset(x = 4.dp)
            )
          },
          onClick = {
            updateType(entry)
            expanded = false
          },
          contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
        )
      }
    }
  }
}

@Composable
fun EditCustomFieldNameDialog(
  onDismissRequest: () -> Unit,
  field: CustomField,
  launchEditName: (CustomField, String) -> Unit
) {
  var name by state(field.name)

  val focusRequester = remember { FocusRequester() }

  AlertDialog(
    icon = {
      Icon(
        Icons.Rounded.Edit,
        contentDescription = null
      )
    },
    title = {
      Text(text = Res.string.edit_field_name.compose)
    },
    text = {
      LazyColumn {
        item {
          TextField(
            value = name,
            onValueChange = {
              if (it.length > 100) return@TextField
              name = it
            },
            label = { Text(Res.string.name.compose) },
            modifier = Modifier.fillMaxWidth().focusRequester(focusRequester)
          )

          LaunchedEffect(true) {
            focusRequester.requestFocus()
          }
        }
      }
    },
    onDismissRequest = onDismissRequest,
    confirmButton = {
      TextButton(
        onClick = {
          launchEditName(field, name)
          onDismissRequest()
        },
        enabled = name.isNotBlank() && name.length in 1..100 && name != field.name
      ) {
        Text(Res.string.edit.compose)
      }
    },
    dismissButton = {
      TextButton(
        onClick = {
          onDismissRequest()
        }
      ) {
        Text(Res.string.cancel.compose)
      }
    }
  )
}

@Composable
fun DeleteCustomFieldDialog(
  field: CustomField,
  onDismissRequest: () -> Unit,
  launchDelete: (CustomField) -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismissRequest,
    icon = {
      Icon(
        Icons.Rounded.Add,
        contentDescription = Res.string.create_custom_field_icon.compose,
        tint = MaterialTheme.colorScheme.error
      )
    },
    title = {
      Text(text = Res.string.delete_field.compose)
    },
    text = {
      LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        item {
          Text(stringResource(Res.string.confirm_delete_field, field.name))
        }
        item {
          Text(
            Res.string.action_irreversible.compose,
            style = MaterialTheme.typography.bodyMedium.merge(fontWeight = FontWeight.SemiBold)
          )
        }
      }
    },
    confirmButton = {
      Button(
        colors = ButtonDefaults.buttonColors(
          containerColor = MaterialTheme.colorScheme.error,
          contentColor = MaterialTheme.colorScheme.onError
        ),
        onClick = {
          launchDelete(field)
          onDismissRequest()
        }
      ) {
        Text(Res.string.delete.compose)
      }
    },
    dismissButton = {
      TextButton(
        onClick = onDismissRequest
      ) {
        Text("Cancel")
      }
    }
  )
}

@Composable
fun EditCustomFieldSecurityLevelSheet(
  onDismissRequest: () -> Unit,
  field: CustomField,
  launchEditSecurityLevel: (CustomField, SecurityLevel) -> Unit
) {
  val securityLevels = remember { enumEntries<SecurityLevel>() }

  OctoBottomSheet(
    onDismissRequest = onDismissRequest
  ) {
    securityLevels.forEach {
      BottomSheetListItem(
        imageVector = it.icon,
        title = it.displayName
      ) {
        launchEditSecurityLevel(field, it)
        onDismissRequest()
      }
    }
  }
}