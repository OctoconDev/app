package app.octocon.app.ui.compose.components

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Numbers
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
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
import app.octocon.app.ui.compose.screens.GLOBAL_PADDING
import app.octocon.app.ui.compose.utils.MarkdownOutlinedTextField
import app.octocon.app.utils.MarkdownRenderer
import app.octocon.app.utils.compose
import app.octocon.app.utils.generateMarkdownTypography
import app.octocon.app.utils.state
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild
import octoconapp.shared.generated.resources.Res
import octoconapp.shared.generated.resources.field_locked
import octoconapp.shared.generated.resources.tap_to_unlock

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
            .haze(hazeState)
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
            .hazeChild(
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
