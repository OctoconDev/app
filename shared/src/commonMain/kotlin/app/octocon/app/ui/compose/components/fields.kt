package app.octocon.app.ui.compose.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.FocusInteraction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Numbers
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TimePicker
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import app.octocon.app.api.model.BaseCustomField
import app.octocon.app.api.model.CustomField
import app.octocon.app.api.model.CustomFieldType
import app.octocon.app.ui.compose.components.shared.FakeOutlinedTextField
import app.octocon.app.ui.compose.components.shared.onTapUnconsumed
import app.octocon.app.ui.compose.screens.GLOBAL_PADDING
import app.octocon.app.ui.compose.theme.hexStringToARGBInt
import app.octocon.app.ui.compose.utils.MarkdownOutlinedTextField
import app.octocon.app.utils.MarkdownRenderer
import app.octocon.app.utils.colorRegex
import app.octocon.app.utils.compose
import app.octocon.app.utils.dateFormat
import app.octocon.app.utils.dateTimeFormat
import app.octocon.app.utils.generateMarkdownTypography
import app.octocon.app.utils.monthYearFormat
import app.octocon.app.utils.state
import app.octocon.color_picker.ClassicColorPicker
import app.octocon.color_picker.HsvColor
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.Padding
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import octoconapp.shared.resources.Res
import octoconapp.shared.resources.cancel
import octoconapp.shared.resources.confirm
import octoconapp.shared.resources.field_locked
import octoconapp.shared.resources.tap_to_unlock

val LocalFieldFocusRequester: ProvidableCompositionLocal<FocusRequester> =
  compositionLocalOf { error("No FocusRequester provided") }

@Composable
fun BoxScope.FinishEditingFieldButton(
  onClick: () -> Unit,
  clickLabel: String
) {
  FilledTonalIconButton(
    onClick = onClick,
    modifier = Modifier.align(Alignment.TopEnd).offset(y = (-8).dp)
  ) {
    Icon(
      imageVector = Icons.Rounded.Check,
      contentDescription = clickLabel
    )
  }
}

@Composable
fun generateHiddenFieldHandler(focusRequester: FocusRequester): Pair<MutableInteractionSource, MutableState<Boolean>> {
  val interactionSource = remember { MutableInteractionSource() }

  val showRealTextField = state(false)
  LaunchedEffect(interactionSource) {
    interactionSource.interactions.collect {
      when (it) {
        is FocusInteraction.Unfocus -> {
          showRealTextField.value = false
        }

        else -> Unit
      }
    }
  }

  LaunchedEffect(showRealTextField.value) {
    if (showRealTextField.value) {
      focusRequester.requestFocus()
    }
  }

  return Pair(interactionSource, showRealTextField)
}

@Composable
fun InertAlterCustomFieldItem(
  field: BaseCustomField,
  value: String?
) = componentForFieldType(
  field = field,
  value = value,
  updateValue = {},
  inert = true
)

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AlterCustomFieldItem(
  field: CustomField,
  value: String?,
  updateValue: (String?) -> Unit
) {
  val haptics = LocalHapticFeedback.current
  if (field.locked) {
    var confirmCount by state(0)
    val hidden = confirmCount < 3

    val hazeState = remember { HazeState() }

    Box {
      Box(
        modifier = Modifier.let {
          if (hidden) it
            // .platformBlur(20)
            .hazeSource(hazeState)
            .clearAndSetSemantics {
              hideFromAccessibility()
            } else it
        }
      ) {
        componentForFieldType(
          field = field,
          value = value,
          updateValue = updateValue,
          inert = false,
          isBlurred = hidden
        )
      }
      if (hidden) {
        Box(
          modifier = Modifier
            .matchParentSize()
            .padding(horizontal = GLOBAL_PADDING)
            .hazeEffect(
              hazeState,
              style = HazeStyle(
                backgroundColor = MaterialTheme.colorScheme.surface,
                tint = HazeTint(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)),
                blurRadius = 16.dp
              )
            )
            .clip(MaterialTheme.shapes.large)
            .clickable {
              haptics.performHapticFeedback(when(confirmCount) {
                0 -> HapticFeedbackType.SegmentTick
                1 -> HapticFeedbackType.ToggleOn
                else -> HapticFeedbackType.LongPress
              })
              confirmCount++
            },
          contentAlignment = Alignment.Center
        ) {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Icon(
              imageVector = Icons.Rounded.Lock,
              contentDescription = null,
              modifier = Modifier.size(24.dp),
              tint = MaterialTheme.colorScheme.secondary
            )
            Text(
              Res.string.field_locked.compose,
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              Res.string.tap_to_unlock.compose + if (confirmCount > 0) " ($confirmCount/3)" else "",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    }
  } else {
    componentForFieldType(
      field = field,
      value = value,
      updateValue = updateValue,
      inert = false
    )
  }
}

@Composable
fun componentForFieldType(
  field: BaseCustomField,
  value: String?,
  updateValue: (String?) -> Unit,
  inert: Boolean,
  isBlurred: Boolean = false
) = when (field.type) {
  CustomFieldType.TEXT -> TextCustomFieldItem(
    field = field,
    value = value,
    updateValue = updateValue,
    inert = inert,
    isBlurred = isBlurred
  )

  CustomFieldType.NUMBER -> NumberCustomFieldItem(
    field = field,
    value = value,
    updateValue = updateValue,
    inert = inert,
    isBlurred = isBlurred
  )

  CustomFieldType.BOOLEAN -> BooleanCustomFieldItem(
    field = field,
    value = value,
    updateValue = updateValue,
    inert = inert,
    isBlurred = isBlurred
  )

  CustomFieldType.COLOUR -> ColourCustomFieldItem(
    field = field,
    value = value,
    updateValue = updateValue,
    inert = inert,
    isBlurred = isBlurred
  )

  CustomFieldType.DATE -> DateCustomFieldItem(
    field = field,
    value = value,
    updateValue = updateValue,
    inert = inert,
    isBlurred = isBlurred
  )

  CustomFieldType.PLAINTEXT -> PlaintextCustomFieldItem(
    field = field,
    value = value,
    updateValue = updateValue,
    inert = inert,
    isBlurred = isBlurred
  )

  CustomFieldType.MONTH -> MonthCustomFieldItem(
    field = field,
    value = value,
    updateValue = updateValue,
    inert = inert,
    isBlurred = isBlurred
  )

  CustomFieldType.YEAR -> YearCustomFieldItem(
    field = field,
    value = value,
    updateValue = updateValue,
    inert = inert,
    isBlurred = isBlurred
  )

  CustomFieldType.MONTH_YEAR -> MonthYearCustomFieldItem(
    field = field,
    value = value,
    updateValue = updateValue,
    inert = inert,
    isBlurred = isBlurred
  )

  CustomFieldType.TIMESTAMP -> TimestampCustomFieldItem(
    field = field,
    value = value,
    updateValue = updateValue,
    inert = inert,
    isBlurred = isBlurred
  )

  CustomFieldType.MONTH_DAY -> MonthDayCustomFieldItem(
    field = field,
    value = value,
    updateValue = updateValue,
    inert = inert,
    isBlurred = isBlurred
  )
}

@Composable
fun TextCustomFieldItem(
  field: BaseCustomField,
  value: String?,
  updateValue: (String?) -> Unit,
  inert: Boolean,
  isBlurred: Boolean = false
) {
  val bodyLarge = MaterialTheme.typography.bodyLarge
  val bodyMedium = MaterialTheme.typography.bodyMedium
  val textStyle = remember(value, bodyMedium, bodyLarge) {
    if ((value?.length ?: 0) < 35) bodyLarge else bodyMedium
  }

  val markdownTypography = generateMarkdownTypography(textStyle)

  // val textState = rememberTextFieldState(value.orEmpty())

  if (inert) {
    FakeOutlinedTextField(
      label = { Text(text = field.name) },
      isBlank = value.isNullOrBlank(),
      modifier = Modifier.fillMaxWidth().padding(horizontal = GLOBAL_PADDING)
    ) {
      MarkdownRenderer(
        if (isBlurred) {
          // Replace alphanumeric characters with a single character
          value!!.replace(Regex("[a-zA-Z0-9]"), "X")
        } else {
          value
        },
        typography = markdownTypography
      )
    }
  } else {
    MarkdownOutlinedTextField(
      value = value,
      onValueChange = {
        if (it.length > 2000) return@MarkdownOutlinedTextField
        updateValue(it)
      },
      label = field.name
    )
  }
}

@Composable
fun NumberCustomFieldItem(
  field: BaseCustomField,
  value: String?,
  updateValue: (String?) -> Unit,
  inert: Boolean,
  isBlurred: Boolean = false
) {
  OutlinedTextField(
    value = value.orEmpty(),
    onValueChange = {
      when {
        inert -> return@OutlinedTextField
        it.isBlank() -> updateValue(null)
        it.toLongOrNull() == null -> return@OutlinedTextField
        else -> updateValue(it)
      }
    },
    singleLine = true,
    label = { Text(text = field.name) },
    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
    suffix = {
      Icon(
        imageVector = Icons.Rounded.Numbers,
        contentDescription = null
      )
    },
    modifier = Modifier.fillMaxWidth().padding(horizontal = GLOBAL_PADDING),
    readOnly = inert
  )
}

@Composable
fun BooleanCustomFieldItem(
  field: BaseCustomField,
  value: String?,
  updateValue: (String?) -> Unit,
  inert: Boolean,
  isBlurred: Boolean = false
) {
  val booleanValue = value?.toBooleanStrict() == true

  val haptics = LocalHapticFeedback.current
  OutlinedCard(
    shape = MaterialTheme.shapes.extraSmall,
    // Use same outline color as text fields
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    modifier = Modifier.fillMaxWidth().padding(horizontal = GLOBAL_PADDING)
  ) {
    Row(
      modifier = Modifier.fillMaxSize()
        .padding(horizontal = GLOBAL_PADDING, vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Text(
        text = field.name,
        style = MaterialTheme.typography.labelLarge,
        modifier = Modifier.weight(1f)
      )

      Checkbox(
        checked = booleanValue,
        onCheckedChange = if (!inert) {
          {
            updateValue(it.toString())
            haptics.performHapticFeedback(
              if(it) HapticFeedbackType.ToggleOn else HapticFeedbackType.ToggleOff
            )
          }
        } else null,
        modifier = if (inert) Modifier.minimumInteractiveComponentSize() else Modifier.let {
          if (isBlurred) it.alpha(0.0f) else it
        },
      )
    }
  }
}

@Composable
fun ColourCustomFieldItem(
  field: BaseCustomField,
  value: String?,
  updateValue: (String?) -> Unit,
  inert: Boolean,
  isBlurred: Boolean = false
) {
  val colorIsValid = !isBlurred && value?.matches(colorRegex) == true
  var showDialog by state(false)

  OutlinedTextField(
    value = if (isBlurred) value.orEmpty().replace(Regex("[a-zA-Z0-9]"), "X") else value.orEmpty(),
    onValueChange = {},
    readOnly = true,
    singleLine = true,
    label = { Text(text = field.name) },
    suffix = if (colorIsValid) ({
      Box(
        modifier = Modifier
          .size(16.dp)
          .clip(MaterialTheme.shapes.extraSmall)
          .background(Color(hexStringToARGBInt(value!!)))
      )
    }) else null,
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = GLOBAL_PADDING)
      .let { if (!inert) it.onTapUnconsumed { showDialog = true } else it }
  )

  if (showDialog) {
    ColourPickerDialog(
      field = field,
      initialColor = value,
      onConfirm = { pickedColor ->
        updateValue(pickedColor)
        showDialog = false
      },
      onDismiss = { showDialog = false }
    )
  }
}

@Composable
private fun ColourPickerDialog(
  field: BaseCustomField,
  initialColor: String?,
  onConfirm: (String) -> Unit,
  onDismiss: () -> Unit
) {
  var pickedColor by state(initialColor ?: "#FFFFFF")
  val pickedColorIsValid by remember { derivedStateOf { pickedColor.matches(colorRegex) } }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(field.name) },
    confirmButton = {
      TextButton(
        enabled = pickedColorIsValid,
        onClick = { onConfirm(pickedColor) }
      ) {
        Text(Res.string.confirm.compose)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text(Res.string.cancel.compose)
      }
    },
    text = {
      LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        item {
          ClassicColorPicker(
            modifier = Modifier
              .height(200.dp)
              .padding(horizontal = GLOBAL_PADDING)
              .clip(MaterialTheme.shapes.small),
            color = HsvColor.from(
              Color(hexStringToARGBInt(if (pickedColorIsValid) pickedColor else "#FFFFFF"))
            ),
            showAlphaBar = false,
            onColorChanged = {
              pickedColor = "#${it.toColor().toArgb().toUInt().toString(16).substring(2)}"
            }
          )
        }
        item {
          TextField(
            value = pickedColor,
            onValueChange = { if (it.length <= 7) pickedColor = it },
            label = { Text(field.name) },
            singleLine = true,
            suffix = if (pickedColorIsValid) ({
              Box(
                modifier = Modifier
                  .size(16.dp)
                  .clip(MaterialTheme.shapes.extraSmall)
                  .background(Color(hexStringToARGBInt(pickedColor)))
              )
            }) else null,
            modifier = Modifier.fillMaxWidth().padding(horizontal = GLOBAL_PADDING)
          )
        }
      }
    }
  )
}

@Composable
fun DateCustomFieldItem(
  field: BaseCustomField,
  value: String?,
  updateValue: (String?) -> Unit,
  inert: Boolean,
  isBlurred: Boolean = false
) {
  var showDatePicker by state(false)

  val epochMillis = value?.toLongOrNull()
  val formattedDate = if (isBlurred) {
    "XXXXXXXXXX"
  } else {
    epochMillis?.let {
      Instant.fromEpochMilliseconds(it)
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .dateFormat()
    }
  }

  OutlinedTextField(
    value = formattedDate ?: "",
    onValueChange = {},
    singleLine = true,
    label = { Text(text = field.name) },
    trailingIcon = {
      Icon(
        imageVector = Icons.Rounded.CalendarToday,
        contentDescription = null
      )
    },
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = GLOBAL_PADDING)
      .let { if (!inert) it.onTapUnconsumed { showDatePicker = true } else it },
    readOnly = true
  )

  if (showDatePicker) {
    val datePickerState = rememberDatePickerState(
      initialSelectedDateMillis = epochMillis
    )
    DatePickerDialog(
      onDismissRequest = { showDatePicker = false },
      confirmButton = {
        TextButton(
          onClick = {
            updateValue(datePickerState.selectedDateMillis?.toString())
            showDatePicker = false
          },
          enabled = datePickerState.selectedDateMillis != null
        ) {
          Text(Res.string.confirm.compose)
        }
      },
      dismissButton = {
        TextButton(onClick = { showDatePicker = false }) {
          Text(Res.string.cancel.compose)
        }
      }
    ) {
      DatePicker(state = datePickerState)
    }
  }
}

@Composable
fun PlaintextCustomFieldItem(
  field: BaseCustomField,
  value: String?,
  updateValue: (String?) -> Unit,
  inert: Boolean,
  isBlurred: Boolean = false
) {
  val bodyLarge = MaterialTheme.typography.bodyLarge
  val bodyMedium = MaterialTheme.typography.bodyMedium
  val textStyle = remember(value, bodyMedium, bodyLarge) {
    if ((value?.length ?: 0) < 35) bodyLarge else bodyMedium
  }

  if (inert) {
    FakeOutlinedTextField(
      label = { Text(text = field.name) },
      isBlank = value.isNullOrBlank(),
      modifier = Modifier.fillMaxWidth().padding(horizontal = GLOBAL_PADDING),
      textStyle = textStyle
    ) {
      Text(
        text = if (isBlurred) {
          value!!.replace(Regex("[a-zA-Z0-9]"), "X")
        } else {
          value ?: ""
        },
        style = textStyle
      )
    }
  } else {
    OutlinedTextField(
      value = value.orEmpty(),
      onValueChange = {
        if (it.length > 2000) return@OutlinedTextField
        updateValue(it.ifEmpty { null })
      },
      label = { Text(text = field.name) },
      textStyle = textStyle,
      modifier = Modifier.fillMaxWidth().padding(horizontal = GLOBAL_PADDING)
    )
  }
}

@Composable
private fun DateVariantFieldItem(
  field: BaseCustomField,
  value: String?,
  updateValue: (String?) -> Unit,
  inert: Boolean,
  isBlurred: Boolean = false,
  trailingIcon: ImageVector,
  formatValue: (LocalDateTime) -> String
) {
  var showDatePicker by state(false)
  val epochMillis = value?.toLongOrNull()

  val formattedValue = if (isBlurred) {
    "XXXXXXXXXX"
  } else {
    epochMillis?.let {
      formatValue(
        Instant.fromEpochMilliseconds(it)
          .toLocalDateTime(TimeZone.currentSystemDefault())
      )
    }
  }

  OutlinedTextField(
    value = formattedValue ?: "",
    onValueChange = {},
    singleLine = true,
    label = { Text(text = field.name) },
    trailingIcon = {
      Icon(imageVector = trailingIcon, contentDescription = null)
    },
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = GLOBAL_PADDING)
      .let { if (!inert) it.onTapUnconsumed { showDatePicker = true } else it },
    readOnly = true
  )

  if (showDatePicker) {
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = epochMillis)
    DatePickerDialog(
      onDismissRequest = { showDatePicker = false },
      confirmButton = {
        TextButton(
          onClick = {
            updateValue(datePickerState.selectedDateMillis?.toString())
            showDatePicker = false
          },
          enabled = datePickerState.selectedDateMillis != null
        ) {
          Text(Res.string.confirm.compose)
        }
      },
      dismissButton = {
        TextButton(onClick = { showDatePicker = false }) {
          Text(Res.string.cancel.compose)
        }
      }
    ) {
      DatePicker(state = datePickerState)
    }
  }
}

@Composable
fun MonthCustomFieldItem(
  field: BaseCustomField,
  value: String?,
  updateValue: (String?) -> Unit,
  inert: Boolean,
  isBlurred: Boolean = false
) {
  val monthFormatter = remember {
    LocalDate.Format { monthName(MonthNames.ENGLISH_FULL) }
  }
  DateVariantFieldItem(
    field = field,
    value = value,
    updateValue = updateValue,
    inert = inert,
    isBlurred = isBlurred,
    trailingIcon = Icons.Rounded.CalendarMonth,
    formatValue = { monthFormatter.format(it.date) }
  )
}

@Composable
fun YearCustomFieldItem(
  field: BaseCustomField,
  value: String?,
  updateValue: (String?) -> Unit,
  inert: Boolean,
  isBlurred: Boolean = false
) {
  DateVariantFieldItem(
    field = field,
    value = value,
    updateValue = updateValue,
    inert = inert,
    isBlurred = isBlurred,
    trailingIcon = Icons.Rounded.CalendarMonth,
    formatValue = { it.year.toString() }
  )
}

@Composable
fun MonthYearCustomFieldItem(
  field: BaseCustomField,
  value: String?,
  updateValue: (String?) -> Unit,
  inert: Boolean,
  isBlurred: Boolean = false
) {
  DateVariantFieldItem(
    field = field,
    value = value,
    updateValue = updateValue,
    inert = inert,
    isBlurred = isBlurred,
    trailingIcon = Icons.Rounded.CalendarMonth,
    formatValue = { it.date.monthYearFormat() }
  )
}

@Composable
fun MonthDayCustomFieldItem(
  field: BaseCustomField,
  value: String?,
  updateValue: (String?) -> Unit,
  inert: Boolean,
  isBlurred: Boolean = false
) {
  val monthDayFormatter = remember {
    LocalDate.Format {
      monthName(MonthNames.ENGLISH_ABBREVIATED)
      chars(" ")
      day(Padding.NONE)
    }
  }
  DateVariantFieldItem(
    field = field,
    value = value,
    updateValue = updateValue,
    inert = inert,
    isBlurred = isBlurred,
    trailingIcon = Icons.Rounded.CalendarMonth,
    formatValue = { monthDayFormatter.format(it.date) }
  )
}

@Composable
fun TimestampCustomFieldItem(
  field: BaseCustomField,
  value: String?,
  updateValue: (String?) -> Unit,
  inert: Boolean,
  isBlurred: Boolean = false
) {
  val epochMillis = value?.toLongOrNull()

  if (!inert && epochMillis != null) {
    // Timestamp already set and editable:
    // tap the field body to edit the date, tap the clock icon area to edit the time.
    // A single onTapUnconsumed handler routes based on the tap's x position so that
    // only one picker opens per tap (avoids the Initial-pass double-fire of IconButton).
    val localDateTime = remember(epochMillis) {
      Instant.fromEpochMilliseconds(epochMillis)
        .toLocalDateTime(TimeZone.currentSystemDefault())
    }

    var showDatePicker by state(false)
    var showTimePicker by state(false)
    var fieldWidth by state(0)
    val density = LocalDensity.current

    OutlinedTextField(
      value = if (isBlurred) "XXXXXXXXXX" else localDateTime.dateTimeFormat(),
      onValueChange = {},
      readOnly = true,
      singleLine = true,
      label = { Text(text = field.name) },
      trailingIcon = {
        Box(
          modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .clickable { showTimePicker = true },
          contentAlignment = Alignment.Center
        ) {
          Icon(imageVector = Icons.Rounded.Schedule, contentDescription = null)
        }
      },
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = GLOBAL_PADDING)
        .onSizeChanged { fieldWidth = it.width }
        .onTapUnconsumed { offset ->
          val trailingZonePx = with(density) { 48.dp.roundToPx() }
          if (offset != null && fieldWidth > 0 && offset.x >= fieldWidth - trailingZonePx) {
            showTimePicker = true
          } else {
            showDatePicker = true
          }
        }
    )

    // Edit date only — preserves existing time
    if (showDatePicker) {
      val datePickerState = rememberDatePickerState(initialSelectedDateMillis = epochMillis)
      DatePickerDialog(
        onDismissRequest = { showDatePicker = false },
        confirmButton = {
          TextButton(
            onClick = {
              val newDate = Instant.fromEpochMilliseconds(datePickerState.selectedDateMillis!!)
                .toLocalDateTime(TimeZone.currentSystemDefault()).date
              val combined = LocalDateTime(newDate, LocalTime(localDateTime.hour, localDateTime.minute))
              updateValue(combined.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds().toString())
              showDatePicker = false
            },
            enabled = datePickerState.selectedDateMillis != null
          ) {
            Text(Res.string.confirm.compose)
          }
        },
        dismissButton = {
          TextButton(onClick = { showDatePicker = false }) {
            Text(Res.string.cancel.compose)
          }
        }
      ) {
        DatePicker(state = datePickerState)
      }
    }

    // Edit time only — preserves existing date
    if (showTimePicker) {
      val timePickerState = rememberTimePickerState(
        initialHour = localDateTime.hour,
        initialMinute = localDateTime.minute
      )
      AlertDialog(
        onDismissRequest = { showTimePicker = false },
        title = { Text(field.name) },
        confirmButton = {
          TextButton(
            onClick = {
              val combined = LocalDateTime(localDateTime.date, LocalTime(timePickerState.hour, timePickerState.minute))
              updateValue(combined.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds().toString())
              showTimePicker = false
            }
          ) {
            Text(Res.string.confirm.compose)
          }
        },
        dismissButton = {
          TextButton(onClick = { showTimePicker = false }) {
            Text(Res.string.cancel.compose)
          }
        },
        text = {
          TimePicker(state = timePickerState)
        }
      )
    }

  } else {
    // No value set yet, or inert: single combined field
    var showDatePicker by state(false)
    var pendingDateMillis by state<Long>()
    var showTimePicker by state(false)

    val formattedValue = if (isBlurred) {
      "XXXXXXXXXX"
    } else {
      epochMillis?.let {
        Instant.fromEpochMilliseconds(it)
          .toLocalDateTime(TimeZone.currentSystemDefault())
          .dateTimeFormat()
      }
    }

    OutlinedTextField(
      value = formattedValue ?: "",
      onValueChange = {},
      singleLine = true,
      label = { Text(text = field.name) },
      trailingIcon = {
        Icon(imageVector = Icons.Rounded.Schedule, contentDescription = null)
      },
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = GLOBAL_PADDING)
        .let { if (!inert) it.onTapUnconsumed { showDatePicker = true } else it },
      readOnly = true
    )

    if (showDatePicker) {
      val datePickerState = rememberDatePickerState()
      DatePickerDialog(
        onDismissRequest = { showDatePicker = false },
        confirmButton = {
          TextButton(
            onClick = {
              pendingDateMillis = datePickerState.selectedDateMillis
              showDatePicker = false
              showTimePicker = true
            },
            enabled = datePickerState.selectedDateMillis != null
          ) {
            Text(Res.string.confirm.compose)
          }
        },
        dismissButton = {
          TextButton(onClick = { showDatePicker = false }) {
            Text(Res.string.cancel.compose)
          }
        }
      ) {
        DatePicker(state = datePickerState)
      }
    }

    if (showTimePicker) {
      val timePickerState = rememberTimePickerState(initialHour = 0, initialMinute = 0)
      AlertDialog(
        onDismissRequest = {
          showTimePicker = false
          pendingDateMillis = null
        },
        title = { Text(field.name) },
        confirmButton = {
          TextButton(
            onClick = {
              val selectedDate = Instant.fromEpochMilliseconds(pendingDateMillis!!)
                .toLocalDateTime(TimeZone.currentSystemDefault()).date
              val combined = LocalDateTime(selectedDate, LocalTime(timePickerState.hour, timePickerState.minute))
              updateValue(combined.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds().toString())
              showTimePicker = false
              pendingDateMillis = null
            }
          ) {
            Text(Res.string.confirm.compose)
          }
        },
        dismissButton = {
          TextButton(onClick = { showTimePicker = false; pendingDateMillis = null }) {
            Text(Res.string.cancel.compose)
          }
        },
        text = {
          TimePicker(state = timePickerState)
        }
      )
    }
  }
}
