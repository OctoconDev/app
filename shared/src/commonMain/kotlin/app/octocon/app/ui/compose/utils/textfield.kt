package app.octocon.app.ui.compose.utils

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.TextFieldBuffer
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.octocon.app.ui.compose.components.FinishEditingFieldButton
import app.octocon.app.ui.compose.components.generateHiddenFieldHandler
import app.octocon.app.ui.compose.components.shared.FakeOutlinedTextField
import app.octocon.app.ui.compose.screens.GLOBAL_PADDING
import app.octocon.app.ui.model.main.hometabs.journal.JournalContentState
import app.octocon.app.utils.MarkdownRenderer
import app.octocon.app.utils.compose
import app.octocon.app.utils.generateMarkdownTypography
import app.octocon.app.utils.state
import com.mikepenz.markdown.m3.markdownColor
import com.mikepenz.markdown.model.MarkdownColors
import com.mikepenz.markdown.model.MarkdownTypography
import octoconapp.shared.generated.resources.Res
import octoconapp.shared.generated.resources.cannot_be_blank
import octoconapp.shared.generated.resources.start_writing

class ModelTransformation(
  val update: (String) -> Result<String>
) : InputTransformation {
  override fun TextFieldBuffer.transformInput() {
    update(toString()).onFailure { revertAllChanges() }
  }
}

/*@Composable
fun UpdateTextFieldStatesOnLoad(
  isLoadedFlow: StateFlow<Boolean>,
  vararg states: Pair<TextFieldState, StateFlow<String?>>
) {
  val isLoaded by isLoadedFlow.collectAsState()
  LaunchedEffect(isLoaded) {
    if (isLoaded) {
      states.forEach { state ->
        state.first.edit { replace(0, length, state.second.value.orEmpty()) }
      }
    }
  }
}*/

@Composable
fun blankErrorText(
  text: String,
  errorText: String = Res.string.cannot_be_blank.compose
): (@Composable () -> Unit)? =
  if(text.isBlank()) { { Text(errorText) } } else { null }

/*
@Composable
fun MarkdownOutlinedTextField(
  internalValue: String?,
  state: TextFieldState,
  update: (String) -> Result<String>,
  onFinishEditing: (() -> Unit)? = null,
  label: String,
  placeholder: String? = null,
  modifier: Modifier = Modifier.fillMaxWidth().padding(horizontal = GLOBAL_PADDING)
) {
  val focusRequester = remember { FocusRequester() }
  val (interactionSource, showRealTextField) = generateHiddenFieldHandler(focusRequester)
  var desiredOffset by remember { mutableStateOf<Offset?>(null) }

  val bodyLarge = MaterialTheme.typography.bodyLarge
  val bodyMedium = MaterialTheme.typography.bodyMedium

  val textStyle = remember(internalValue, bodyLarge, bodyMedium) {
    if ((internalValue?.length ?: 0) < 35) bodyLarge else bodyMedium
  }

  if (showRealTextField.value) {
    Box {
      OutlinedTextField(
        state = state,
        inputTransformation = ModelTransformation(update),
        textStyle = textStyle,
        interactionSource = interactionSource,
        placeholder = if(placeholder.isNullOrBlank()) null else { { Text(placeholder) } },
        label = { Text(label) },
        modifier = modifier.focusRequester(focusRequester),
        onTextLayout = { layoutResult ->
          desiredOffset?.let {
            layoutResult()?.getOffsetForPosition(desiredOffset!!.plus(
              Offset(-20f, -60f)
            ))?.let {
              state.edit { selection = TextRange(it) }
            }
            desiredOffset = null
          }
        }
      )
      FinishEditingFieldButton(
        onClick = {
          showRealTextField.value = false
          onFinishEditing?.invoke()
        },
        clickLabel = "Save $label"
      )
    }
  } else {
    FakeOutlinedTextField(
      label = { Text(label) },
      onClick = { offset ->
        desiredOffset = offset
        showRealTextField.value = true
      },
      clickLabel = "Edit $label",
      isBlank = internalValue.isNullOrBlank(),
      modifier = modifier,
      textStyle = textStyle
    ) {
      MarkdownRenderer(
        internalValue,
        typography = generateMarkdownTypography(textStyle)
      )
    }
  }
}
*/

@Composable
fun MarkdownOutlinedTextField(
  value: String?,
  onValueChange: (String) -> Unit,
  onFinishEditing: (() -> Unit)? = null,
  label: String,
  placeholder: String? = null,
  modifier: Modifier = Modifier.fillMaxWidth().padding(horizontal = GLOBAL_PADDING)
) {
  val focusRequester = remember { FocusRequester() }
  val (interactionSource, showRealTextField) = generateHiddenFieldHandler(focusRequester)
  var desiredOffset by remember { mutableStateOf<Offset?>(null) }
  var desiredSelection by remember { mutableStateOf<Pair<TextRange, TextRange?>?>(null) }

  val bodyLarge = MaterialTheme.typography.bodyLarge
  val bodyMedium = MaterialTheme.typography.bodyMedium

  val textStyle = remember(value, bodyLarge, bodyMedium) {
    if ((value?.length ?: 0) < 35) bodyLarge else bodyMedium
  }

  var textValue by remember { mutableStateOf(TextFieldValue(value.orEmpty())) }

  LaunchedEffect(value) {
    val newText = value.orEmpty()
    if (textValue.text != newText) {
      textValue = if (desiredSelection != null) {
        textValue.copy(
          text = newText,
          selection = desiredSelection!!.first,
          composition = desiredSelection!!.second
        )
      } else {
        textValue.copy(text = newText)
      }
    }
    desiredSelection = null
  }

  if (showRealTextField.value) {
    Box {
      OutlinedTextFieldWithLayout(
        value = textValue,
        onValueChange = {
          if (textValue.text == it.text) {
            textValue = it
          } else {
            textValue = it.copy(composition = it.composition) // Preserve composition
            desiredSelection = it.selection to it.composition
            onValueChange(it.text)
          }
        },
        textStyle = textStyle,
        interactionSource = interactionSource,
        placeholder = if (placeholder.isNullOrBlank()) null else { { Text(placeholder) } },
        label = { Text(label) },
        modifier = modifier.focusRequester(focusRequester),
        onTextLayout = { layoutResult ->
          desiredOffset?.let { offset ->
            val newSelection = TextRange(layoutResult.getOffsetForPosition(offset + Offset(-20f, -60f)))
            if (textValue.selection != newSelection) {
              textValue = textValue.copy(selection = newSelection)
            }
            desiredOffset = null
          }
        }
      )
      FinishEditingFieldButton(
        onClick = {
          showRealTextField.value = false
          onFinishEditing?.invoke()
        },
        clickLabel = "Save $label"
      )
    }
  } else {
    FakeOutlinedTextField(
      label = { Text(label) },
      onClick = { offset ->
        desiredOffset = offset
        showRealTextField.value = true
      },
      clickLabel = "Edit $label",
      isBlank = value.isNullOrBlank(),
      modifier = modifier,
      textStyle = textStyle
    ) {
      MarkdownRenderer(
        value,
        typography = generateMarkdownTypography(textStyle)
      )
    }
  }
}
/*
@Composable
fun JournalEntryMarkdownTextField(
  isEditing: Boolean,
  updateIsEditing: (Boolean) -> Unit,
  contentState: JournalContentState,
  contentTextState: TextFieldState,
  updateContent: (String) -> Result<String>,
  padding: PaddingValues,
  focusRequester: FocusRequester,
  interactionSource: MutableInteractionSource,
  markdownColors: MarkdownColors = markdownColor(),
  markdownTypography: MarkdownTypography = generateMarkdownTypography(MaterialTheme.typography.bodyLarge),
  prefixContent: LazyListScope.(JournalContentState) -> Unit
) {
  var desiredOffset by remember { mutableStateOf<Offset?>(null) }

  BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
    val height = maxHeight - padding.calculateTopPadding()

    LazyColumn(
      contentPadding = PaddingValues(GLOBAL_PADDING),
      modifier = Modifier.fillMaxSize(),
      horizontalAlignment = Alignment.Start,
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      prefixContent(contentState)

      if(isEditing) {
        item {
          BasicTextField(
            state = contentTextState,
            inputTransformation = ModelTransformation(updateContent),
            modifier = Modifier.focusRequester(focusRequester).fillMaxWidth().padding(top = 2.dp),
            textStyle = MaterialTheme.typography.bodyLarge.copy(
              color = MaterialTheme.colorScheme.onSurface
            ),
            interactionSource = interactionSource,
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            decorator = { innerTextField ->
              Box {
                if (contentState.ensureContent.isNullOrBlank()) {
                  Text(
                    text = Res.string.start_writing.compose,
                    style = MaterialTheme.typography.bodyLarge.copy(
                      color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    ),
                    // modifier = Modifier.padding(horizontal = GLOBAL_PADDING)
                  )
                }
                innerTextField()
              }
            },
            onTextLayout = { layoutResult ->
              desiredOffset?.let {
                layoutResult()?.getOffsetForPosition(desiredOffset!!.plus(
                  Offset(-20f, -60f)
                ))?.let {
                  contentTextState.edit { selection = TextRange(it) }
                }
                desiredOffset = null
              }
            }
          )
        }

        item {
          Spacer(modifier = Modifier.imePadding())
        }
      } else {
        item {
          val onTap = { offset: Offset? ->
            desiredOffset = offset
            updateIsEditing(true)
          }
          MarkdownRenderer(
            contentState.ensureContent,
            colors = markdownColors,
            typography = markdownTypography,
            modifier = Modifier.pointerInput(Unit) {
              detectTapGestures(onTap = onTap)
            }.semantics {
              this.role = Role.Button
              this.onClick {
                onTap.invoke(null)
                true
              }
            }.defaultMinSize(minHeight = height).fillMaxWidth()
          )
        }
      }
    }
  }
}
*/

@Composable
fun JournalEntryMarkdownTextField(
  isEditing: Boolean,
  updateIsEditing: (Boolean) -> Unit,
  contentState: JournalContentState,
  updateContent: (String) -> Result<String>,
  padding: PaddingValues,
  focusRequester: FocusRequester,
  interactionSource: MutableInteractionSource,
  markdownColors: MarkdownColors = markdownColor(),
  markdownTypography: MarkdownTypography = generateMarkdownTypography(MaterialTheme.typography.bodyLarge),
  prefixContent: LazyListScope.(JournalContentState) -> Unit
) {
  var desiredOffset by remember { mutableStateOf<Offset?>(null) }
  var desiredSelection by state<Pair<TextRange, TextRange?>?>(null)

  var contentTextValue by remember { mutableStateOf(TextFieldValue(
    if (contentState is JournalContentState.Ready) contentState.content.orEmpty() else ""
  )) }

  LaunchedEffect(contentState) {
    if (contentState !is JournalContentState.Ready) return@LaunchedEffect
    val newText = contentState.ensureContent.orEmpty()
    if (contentTextValue.text != newText) {
      contentTextValue = if (desiredSelection != null) {
        contentTextValue.copy(
          text = newText,
          selection = desiredSelection!!.first,
          composition = desiredSelection!!.second
        )
      } else {
        contentTextValue.copy(text = newText)
      }
    }
    desiredSelection = null
  }

  BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
    val height = maxHeight - padding.calculateTopPadding()

    LazyColumn(
      contentPadding = PaddingValues(GLOBAL_PADDING),
      modifier = Modifier.fillMaxSize(),
      horizontalAlignment = Alignment.Start,
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      prefixContent(contentState)

      if (isEditing) {
        item {
          BasicTextField(
            value = contentTextValue,
            onValueChange = {
              if (contentTextValue.text == it.text) {
                contentTextValue = it
              } else {
                contentTextValue = it.copy(composition = it.composition) // Preserve composition
                desiredSelection = it.selection to it.composition
                updateContent(it.text)
              }
            },
            modifier = Modifier.focusRequester(focusRequester).fillMaxWidth().padding(top = 2.dp),
            textStyle = MaterialTheme.typography.bodyLarge.copy(
              color = MaterialTheme.colorScheme.onSurface
            ),
            interactionSource = interactionSource,
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            decorationBox = { innerTextField ->
              Box {
                if (contentState.ensureContent.isNullOrBlank()) {
                  Text(
                    text = Res.string.start_writing.compose,
                    style = MaterialTheme.typography.bodyLarge.copy(
                      color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                  )
                }
                innerTextField()
              }
            },
            onTextLayout = { layoutResult ->
              desiredOffset?.let { offset ->
                val newSelection = TextRange(layoutResult.getOffsetForPosition(offset))
                if (contentTextValue.selection != newSelection) {
                  contentTextValue = contentTextValue.copy(selection = newSelection)
                }
                desiredOffset = null
              }
            }
          )
        }

        item {
          Spacer(modifier = Modifier.imePadding())
        }
      } else {
        item {
          val onTap = { offset: Offset? ->
            desiredOffset = offset
            updateIsEditing(true)
          }
          MarkdownRenderer(
            contentState.ensureContent,
            colors = markdownColors,
            typography = markdownTypography,
            modifier = Modifier.pointerInput(Unit) {
              detectTapGestures(onTap = onTap)
            }.semantics {
              this.role = Role.Button
              this.onClick {
                onTap.invoke(null)
                true
              }
            }.defaultMinSize(minHeight = height).fillMaxWidth()
          )
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OutlinedTextFieldWithLayout(
  value: TextFieldValue,
  onValueChange: (TextFieldValue) -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  readOnly: Boolean = false,
  textStyle: TextStyle = LocalTextStyle.current,
  label: @Composable (() -> Unit)? = null,
  placeholder: @Composable (() -> Unit)? = null,
  leadingIcon: @Composable (() -> Unit)? = null,
  trailingIcon: @Composable (() -> Unit)? = null,
  prefix: @Composable (() -> Unit)? = null,
  suffix: @Composable (() -> Unit)? = null,
  supportingText: @Composable (() -> Unit)? = null,
  isError: Boolean = false,
  visualTransformation: VisualTransformation = VisualTransformation.None,
  keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
  keyboardActions: KeyboardActions = KeyboardActions.Default,
  singleLine: Boolean = false,
  maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
  minLines: Int = 1,
  onTextLayout: (TextLayoutResult) -> Unit = {},
  interactionSource: MutableInteractionSource? = null,
  shape: Shape = OutlinedTextFieldDefaults.shape,
  colors: TextFieldColors = OutlinedTextFieldDefaults.colors()
) {
  @Suppress("NAME_SHADOWING")
  val interactionSource = interactionSource ?: remember { MutableInteractionSource() }
  // If color is not provided via the text style, use content color as a default
  val textColor =
    textStyle.color.takeOrElse {
      val focused = interactionSource.collectIsFocusedAsState().value
      colors.textColor(enabled, isError, focused)
    }
  val mergedTextStyle = textStyle.merge(TextStyle(color = textColor))

  CompositionLocalProvider(LocalTextSelectionColors provides colors.textSelectionColors) {
    BasicTextField(
      value = value,
      modifier =
      modifier
        .then(
          if (label != null) {
            Modifier
              // Merge semantics at the beginning of the modifier chain to ensure
              // padding is considered part of the text field.
              .semantics(mergeDescendants = true) {}
              .padding(top = minimizedLabelHalfHeight())
          } else {
            Modifier
          }
        )
        .defaultMinSize(
          minWidth = OutlinedTextFieldDefaults.MinWidth,
          minHeight = OutlinedTextFieldDefaults.MinHeight
        ),
      onValueChange = onValueChange,
      enabled = enabled,
      readOnly = readOnly,
      textStyle = mergedTextStyle,
      cursorBrush = SolidColor(colors.cursorColor(isError)),
      visualTransformation = visualTransformation,
      keyboardOptions = keyboardOptions,
      keyboardActions = keyboardActions,
      interactionSource = interactionSource,
      singleLine = singleLine,
      maxLines = maxLines,
      minLines = minLines,
      onTextLayout = onTextLayout,
      decorationBox =
      @Composable { innerTextField ->
        OutlinedTextFieldDefaults.DecorationBox(
          value = value.text,
          visualTransformation = visualTransformation,
          innerTextField = innerTextField,
          placeholder = placeholder,
          label = label,
          leadingIcon = leadingIcon,
          trailingIcon = trailingIcon,
          prefix = prefix,
          suffix = suffix,
          supportingText = supportingText,
          singleLine = singleLine,
          enabled = enabled,
          isError = isError,
          interactionSource = interactionSource,
          colors = colors,
          container = {
            OutlinedTextFieldDefaults.Container(
              enabled = enabled,
              isError = isError,
              interactionSource = interactionSource,
              colors = colors,
              shape = shape,
            )
          }
        )
      }
    )
  }
}

fun TextFieldColors.textColor(
  enabled: Boolean,
  isError: Boolean,
  focused: Boolean,
): Color =
  when {
    !enabled -> disabledTextColor
    isError -> errorTextColor
    focused -> focusedTextColor
    else -> unfocusedTextColor
  }

private fun TextFieldColors.cursorColor(isError: Boolean): Color =
  if (isError) errorCursorColor else cursorColor

@Composable
private fun minimizedLabelHalfHeight(): Dp {
  val compositionLocalValue = MaterialTheme.typography.bodySmall.lineHeight
  val fallbackValue = 16.0.sp
  val value = if (compositionLocalValue.isSp) compositionLocalValue else fallbackValue
  return with(LocalDensity.current) { value.toDp() / 2 }
}