package app.octocon.app.ui.compose.components.shared

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.FocusInteraction
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddAPhoto
import androidx.compose.material.icons.rounded.CreateNewFolder
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import app.octocon.app.Settings
import app.octocon.app.api.model.MyAlter
import app.octocon.app.api.model.MyTag
import app.octocon.app.ui.compose.components.InertAlterCard
import app.octocon.app.ui.compose.components.OctoSearchBar
import app.octocon.app.ui.compose.components.TagCard
import app.octocon.app.ui.compose.screens.GLOBAL_PADDING
import app.octocon.app.ui.compose.theme.ThemeFromColor
import app.octocon.app.ui.compose.theme.hexStringToARGBInt
import app.octocon.app.utils.ColorSchemeParams
import app.octocon.app.utils.DevicePlatform
import app.octocon.app.utils.colorRegex
import app.octocon.app.utils.compose
import app.octocon.app.utils.composeColorSchemeParams
import app.octocon.app.utils.fuse.Fuse
import app.octocon.app.utils.savedState
import app.octocon.app.utils.sortBySimilarity
import app.octocon.app.utils.state
import app.octocon.color_picker.ClassicColorPicker
import app.octocon.color_picker.HsvColor
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import octoconapp.shared.generated.resources.Res
import octoconapp.shared.generated.resources.cancel
import octoconapp.shared.generated.resources.color
import octoconapp.shared.generated.resources.confirm
import octoconapp.shared.generated.resources.remove_avatar
import octoconapp.shared.generated.resources.search_alters
import octoconapp.shared.generated.resources.search_tags
import octoconapp.shared.generated.resources.set_avatar
import octoconapp.shared.generated.resources.unnamed_alter
import octoconapp.shared.generated.resources.update_color
import kotlin.math.absoluteValue

@Composable
fun createConfirmationDialog(
  title: String,
  messageText: String,
  confirmText: String,
  cancelText: String,
  icon: @Composable () -> Unit,
  onConfirm: (openUri: (String) -> Unit) -> Unit,
  openUri: (String, ColorSchemeParams) -> Unit = { _, _ -> }
): Triple<@Composable () -> Unit, Boolean, () -> Unit> = internalConfirmationDialog(
  title = title,
  confirmText = confirmText,
  cancelText = cancelText,
  icon = icon,
  content = { Text(messageText) },
  onConfirm = onConfirm,
  openUri = openUri
)

@Composable
fun createConfirmationDialog(
  title: String,
  messageContent: @Composable () -> Unit,
  confirmText: String,
  cancelText: String,
  icon: @Composable () -> Unit,
  onConfirm: (openUri: (String) -> Unit) -> Unit,
  openUri: (String, ColorSchemeParams) -> Unit = { _, _ -> }
) = internalConfirmationDialog(
  title = title,
  confirmText = confirmText,
  cancelText = cancelText,
  icon = icon,
  content = messageContent,
  onConfirm = onConfirm,
  openUri = openUri
)

@Composable
private fun internalConfirmationDialog(
  title: String,
  confirmText: String,
  cancelText: String,
  content: @Composable () -> Unit,
  icon: @Composable () -> Unit,
  onConfirm: (openUri: (String) -> Unit) -> Unit,
  openUri: (String, ColorSchemeParams) -> Unit = { _, _ -> }
): Triple<@Composable () -> Unit, Boolean, () -> Unit> {
  var isOpen by savedState(false)

  val composable: @Composable () -> Unit = {
    val colorSchemeParams = composeColorSchemeParams

    AlertDialog(
      icon = icon,
      title = { Text(text = title) },
      text = content,
      confirmButton = {
        TextButton(
          onClick = {
            onConfirm { uri -> openUri(uri, colorSchemeParams) }
            isOpen = false
          }
        ) {
          Text(confirmText)
        }
      },
      dismissButton = {
        TextButton(
          onClick = { isOpen = false }
        ) {
          Text(cancelText)
        }
      },
      onDismissRequest = { isOpen = false }
    )
  }

  @Suppress("MoveLambdaOutsideParentheses")
  return Triple(composable, isOpen, { isOpen = true })
}

enum class CardGroupPosition {
  START {
    override val shape
      @Composable get() = MaterialTheme.shapes.medium.copy(
        bottomStart = CornerSize(0.dp),
        bottomEnd = CornerSize(0.dp)
      )
  },
  MIDDLE {
    override val shape
      @Composable get() = RoundedCornerShape(0.dp)
  },
  END {
    override val shape
      @Composable get() = MaterialTheme.shapes.medium.copy(
        topStart = CornerSize(0.dp),
        topEnd = CornerSize(0.dp)
      )
  },
  SINGLE {
    override val shape
      @Composable get() = MaterialTheme.shapes.medium
  };

  abstract val shape: CornerBasedShape
    @Composable get
}

@Composable
fun UpdateColorDialog(
  initialColor: String?,
  updateColor: (String) -> Unit,
  onDismissRequest: () -> Unit,
  settings: Settings
) {
  var color by savedState(initialColor ?: "#FFFFFF")
  val colorIsValid by derivedStateOf { color.matches(colorRegex) }

  ThemeFromColor(
    color = if (colorIsValid) color else initialColor,
    colorMode = settings.colorMode,
    dynamicColorType = settings.dynamicColorType,
    colorContrastLevel = settings.colorContrastLevel,
    amoledMode = settings.amoledMode,
    shouldCache = false
  ) {
    AlertDialog(
      onDismissRequest = onDismissRequest,
      title = { Text(Res.string.update_color.compose) },
      confirmButton = {
        TextButton(
          enabled = colorIsValid,
          onClick = {
            updateColor(color)
            onDismissRequest()
          },
          modifier = Modifier.padding(8.dp),
        ) {
          Text(Res.string.confirm.compose)
        }
      },
      dismissButton = {
        TextButton(
          onClick = { onDismissRequest() },
          modifier = Modifier.padding(8.dp),
        ) {
          Text(Res.string.cancel.compose)
        }
      },
      text = {
        LazyColumn(
          verticalArrangement = Arrangement.spacedBy(8.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
        ) {
          item {
            ClassicColorPicker(
              modifier = Modifier
                .height(200.dp)
                .padding(horizontal = GLOBAL_PADDING)
                .clip(MaterialTheme.shapes.small),
              color = HsvColor.from(Color(hexStringToARGBInt(initialColor ?: "#FFFFFF"))),
              showAlphaBar = false,
              onColorChanged = {
                color = "#${it.toColor().toArgb().toUInt().toString(16).substring(2)}"
              }
            )
          }
          item {
            TextField(
              value = color,
              onValueChange = { color = it },
              label = { Text(Res.string.color.compose) },
              singleLine = true,
              suffix = if (colorIsValid) ({
                Box(
                  modifier = Modifier
                    .size(16.dp)
                    .clip(MaterialTheme.shapes.extraSmall)
                    .background(Color(hexStringToARGBInt(color)))
                )
              }) else null,
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = GLOBAL_PADDING)
            )
          }
        }
      }
    )
  }
}

@Composable
fun AttachAlterDialog(
  existingAlters: List<Int>,
  alters: List<MyAlter>,
  placeholderPainter: Painter,
  onDismissRequest: () -> Unit,
  launchAttachAlter: (Int) -> Unit,
  attachText: String,
  noAltersText: String,
  settings: Settings
) {
  val validAlters = alters.filter { it.id !in existingAlters }
  val searchBarVisible = !DevicePlatform.isWasm && validAlters.size > 5
  val coroutineScope = rememberCoroutineScope()

  val fuse = remember { Fuse() }
  var isSearching by state(false)
  var searchQuery by state("")
  var searchResults by state(validAlters)

  @Suppress("LocalVariableName")
  val unnamed_alter = Res.string.unnamed_alter.compose

  LaunchedEffect(searchQuery) {
    var setSearchingJob: Job? = null
    val result = if (searchQuery.isBlank()) {
      validAlters
    } else {
      // Set isSearching if search takes more than 100ms
      setSearchingJob = launch {
        delay(100)
        isSearching = true
      }
      validAlters.sortBySimilarity({ it.name ?: unnamed_alter }, searchQuery, fuse = fuse)
    }

    setSearchingJob?.cancel()
    searchResults = result
    isSearching = false
  }

  AlertDialog(
    icon = {
      Icon(
        Icons.Rounded.PersonAdd,
        contentDescription = null
      )
    },
    title = {
      Text(text = attachText)
    },
    text = {
      if (validAlters.isEmpty()) {
        LazyColumn { item { Text(noAltersText) } }
      } else {
        Column(
          verticalArrangement = Arrangement.spacedBy(8.dp),
          modifier = if (searchBarVisible) Modifier.fillMaxSize() else Modifier.fillMaxWidth()
        ) {
          if (searchBarVisible) {
            OctoSearchBar(
              searchQuery = searchQuery,
              setSearchQuery = { searchQuery = it },
              isSearching = isSearching,
              placeholderText = Res.string.search_alters.compose,
              modifier = Modifier.fillMaxWidth()
            )
          }
          LazyColumn(
            contentPadding = PaddingValues(16.dp),
            modifier = Modifier.clip(MaterialTheme.shapes.large)
              .background(MaterialTheme.colorScheme.surface).let {
                if (searchBarVisible) it.fillMaxSize() else it
              },
            verticalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            items(searchResults, key = { it.id }) {
              InertAlterCard(
                imageContext = coroutineScope.coroutineContext,
                placeholderPainter = placeholderPainter,
                alter = it,
                isFronting = false,
                isPrimary = false,
                frontComment = null,
                onClick = {
                  launchAttachAlter(it.id)
                  onDismissRequest()
                },
                modifier = Modifier.animateItem(),
                settings = settings
              )
            }
          }
        }
      }
    },
    onDismissRequest = onDismissRequest,
    dismissButton = {
      TextButton(
        onClick = {
          onDismissRequest()
        }
      ) {
        Text(Res.string.cancel.compose)
      }
    },
    confirmButton = {}
  )
}

@Composable
fun AttachTagDialog(
  existingTags: List<String>,
  tags: List<MyTag>,
  folderPainter: Painter,
  onDismissRequest: () -> Unit,
  launchAttachTag: (String) -> Unit,
  attachText: String,
  noTagsText: String,
  settings: Settings
) {
  val validTags = tags.filter { it.id !in existingTags }
  val searchBarVisible = !DevicePlatform.isWasm && validTags.size > 5

  val fuse = remember { Fuse() }
  var isSearching by state(false)
  var searchQuery by state("")
  var searchResults by state(validTags)

  LaunchedEffect(searchQuery) {
    var setSearchingJob: Job? = null
    val result = if (searchQuery.isBlank()) {
      validTags
    } else {
      // Set isSearching if search takes more than 100ms
      setSearchingJob = launch {
        delay(100)
        isSearching = true
      }
      validTags.sortBySimilarity({ it.name }, searchQuery, fuse = fuse)
    }

    setSearchingJob?.cancel()
    searchResults = result
    isSearching = false
  }

  AlertDialog(
    icon = {
      Icon(
        Icons.Rounded.CreateNewFolder,
        contentDescription = null
      )
    },
    title = {
      Text(text = attachText)
    },
    text = {
      if (validTags.isEmpty()) {
        LazyColumn { item { Text(noTagsText) } }
      } else {
        Column(
          verticalArrangement = Arrangement.spacedBy(8.dp),
          modifier = if (searchBarVisible) Modifier.fillMaxSize() else Modifier.fillMaxWidth()
        ) {
          if (searchBarVisible) {
            OctoSearchBar(
              searchQuery = searchQuery,
              setSearchQuery = { searchQuery = it },
              isSearching = isSearching,
              placeholderText = Res.string.search_tags.compose,
              modifier = Modifier.fillMaxWidth()
            )
          }
          LazyColumn(
            contentPadding = PaddingValues(16.dp),
            modifier = Modifier.clip(MaterialTheme.shapes.large)
              .background(MaterialTheme.colorScheme.surface).let {
                if (searchBarVisible) it.fillMaxSize() else it
              },
            verticalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            items(searchResults) {
              TagCard(
                iconPainter = folderPainter,
                tag = it,
                onClick = {
                  launchAttachTag(it.id)
                  onDismissRequest()
                },
                showNavigationIcon = false,
                settings = settings
              )
            }
          }
        }
      }
    },
    onDismissRequest = onDismissRequest,
    dismissButton = {
      TextButton(
        onClick = {
          onDismissRequest()
        }
      ) {
        Text(Res.string.cancel.compose)
      }
    },
    confirmButton = {}
  )
}

@Composable
fun FakeOutlinedTextField(
  label: @Composable () -> Unit,
  modifier: Modifier = Modifier,
  colors: TextFieldColors = OutlinedTextFieldDefaults.colors(),
  contentPadding: PaddingValues = OutlinedTextFieldDefaults.contentPadding(),
  onClick: ((Offset?) -> Unit)? = null,
  isBlank: Boolean,
  clickLabel: String? = null,
  textStyle: TextStyle = LocalTextStyle.current,
  content: @Composable () -> Unit
) {
  val layoutDirection = LocalLayoutDirection.current
  val horizontalLabelOffset = contentPadding.calculateStartPadding(layoutDirection) - 3.75.dp
  val verticalLabelOffset = -(7.25.dp)

  Box(
    modifier = Modifier.padding(top = 8.dp)
  ) {
    Box(
      modifier = modifier.let {
        if (onClick != null) it.pointerInput(Unit) {
          detectTapGestures(
            onTap = { onClick(it) }
          )
        }.semantics {
          this.role = Role.Button
          this.onClick(clickLabel) {
            onClick.invoke(null)
            true
          }
        } else it
      }
    ) {
      Surface(
        border = BorderStroke(1.dp, colors.unfocusedIndicatorColor),
        shape = OutlinedTextFieldDefaults.shape,
        color = colors.unfocusedContainerColor,
        modifier = Modifier.fillMaxWidth()
      ) {
        Box(
          modifier = Modifier.padding(contentPadding)
        ) {
          if (isBlank) {
            CompositionLocalProvider(
              LocalTextStyle provides textStyle.copy(
                color = colors.unfocusedPlaceholderColor
              )
            ) {
              label()
            }
          } else {
            CompositionLocalProvider(
              LocalTextStyle provides textStyle
            ) {
              content()
            }
          }
        }
      }

      if (!isBlank) {
        Box(
          modifier = Modifier.offset(x = horizontalLabelOffset, y = verticalLabelOffset)
            .background(MaterialTheme.colorScheme.surface)
        ) {
          Box(
            modifier = Modifier.padding(horizontal = 3.75.dp)
          ) {
            CompositionLocalProvider(
              LocalTextStyle provides MaterialTheme.typography.bodySmall.copy(
                color = colors.unfocusedLabelColor
              )
            ) {
              label()
            }
          }
        }
      }
    }
  }
}

@Composable
fun PermanentTipsNote(
  text: String,
  modifier: Modifier = Modifier
) {
  OutlinedCard(
    modifier = modifier
  ) {
    Column(
      modifier = Modifier.padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Icon(
          imageVector = Icons.Rounded.Lightbulb,
          modifier = Modifier.size(16.dp),
          contentDescription = null
        )
        Text(
          "Tip",
          style = MaterialTheme.typography.labelMedium
        )
      }
      Text(
        text = text,
        style = MaterialTheme.typography.bodySmall
      )
    }
  }
}

@Composable
fun AvatarContextSheet(
  onDismissRequest: () -> Unit,
  avatar: String?,
  launchSetAvatar: () -> Unit,
  launchRemoveAvatar: () -> Unit,
) {
  OctoBottomSheet(
    onDismissRequest = onDismissRequest
  ) {
    BottomSheetListItem(
      icon = rememberVectorPainter(Icons.Rounded.AddAPhoto),
      title = Res.string.set_avatar.compose
    ) {
      launchSetAvatar()
      onDismissRequest()
    }
    if (!avatar.isNullOrBlank()) {
      BottomSheetListItem(
        icon = rememberVectorPainter(Icons.Rounded.Delete),
        iconTint = MaterialTheme.colorScheme.error,
        title = Res.string.remove_avatar.compose
      ) {
        launchRemoveAvatar()
        onDismissRequest()
      }
    }
  }
}

@Composable
fun OctoBottomSheet(
  onDismissRequest: () -> Unit,
  sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
  content: @Composable ColumnScope.() -> Unit
) {
  ModalBottomSheet(
    onDismissRequest = onDismissRequest,
    sheetState = sheetState,
    content = content
  )
}

class MaskVisualTransformation(private val mask: String) : VisualTransformation {
  private val specialSymbolsIndices = mask.indices.filter { mask[it] != '#' }

  override fun filter(text: AnnotatedString): TransformedText {
    var out = ""
    var maskIndex = 0
    text.forEach { char ->
      while (specialSymbolsIndices.contains(maskIndex)) {
        out += mask[maskIndex]
        maskIndex++
      }
      out += char
      maskIndex++
    }
    return TransformedText(AnnotatedString(out), offsetTranslator())
  }

  private fun offsetTranslator() = object : OffsetMapping {
    override fun originalToTransformed(offset: Int): Int {
      val offsetValue = offset.absoluteValue
      if (offsetValue == 0) return 0
      var numberOfHashtags = 0
      val masked = mask.takeWhile {
        if (it == '#') numberOfHashtags++
        numberOfHashtags < offsetValue
      }
      return masked.length + 1
    }

    override fun transformedToOriginal(offset: Int): Int {
      return mask.take(offset.absoluteValue).count { it == '#' }
    }
  }
}

object NoRippleInteractionSource : MutableInteractionSource {

  override val interactions: Flow<Interaction> = emptyFlow()

  override suspend fun emit(interaction: Interaction) {}

  override fun tryEmit(interaction: Interaction) = true
}

@Composable
inline fun rememberCollectPressInteractionSource(
  crossinline block: suspend () -> Unit
) = remember { MutableInteractionSource() }.also { interactionSource ->
  if(DevicePlatform.isiOS) {
    // Workaround for https://youtrack.jetbrains.com/issue/CMP-4087
    LaunchedEffect(interactionSource) {
      interactionSource.interactions.collect {
        if (it is FocusInteraction.Focus) block()
      }
    }
  } else {
    LaunchedEffect(interactionSource) {
      interactionSource.interactions.collect {
        if (it is PressInteraction.Release) block()
      }
    }
  }
}

@Composable
fun Modifier.octoButtonModifier() = this.pointerHoverIcon(PointerIcon.Hand)