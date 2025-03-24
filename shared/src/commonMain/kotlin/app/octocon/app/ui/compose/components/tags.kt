package app.octocon.app.ui.compose.components

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.CreateNewFolder
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.octocon.app.Settings
import app.octocon.app.api.model.BaseTag
import app.octocon.app.api.model.MyTag
import app.octocon.app.ui.compose.components.shared.BottomSheetListItem
import app.octocon.app.ui.compose.components.shared.OctoBottomSheet
import app.octocon.app.ui.compose.theme.ThemeFromColor
import app.octocon.app.ui.compose.utils.SpotlightTooltip
import app.octocon.app.utils.compose
import app.octocon.app.utils.state
import octoconapp.shared.generated.resources.Res
import octoconapp.shared.generated.resources.action_irreversible
import octoconapp.shared.generated.resources.cancel
import octoconapp.shared.generated.resources.confirm_delete_tag
import octoconapp.shared.generated.resources.create
import octoconapp.shared.generated.resources.create_tag
import octoconapp.shared.generated.resources.delete
import octoconapp.shared.generated.resources.delete_tag
import octoconapp.shared.generated.resources.name
import octoconapp.shared.generated.resources.open_tag
import octoconapp.shared.generated.resources.tag_context_menu
import octoconapp.shared.generated.resources.tooltip_delete_tag_desc
import org.jetbrains.compose.resources.stringResource

private val tagCardHeight = 64.dp

@Composable
fun TagCard(
  iconPainter: Painter,
  tag: BaseTag,
  onClick: () -> Unit,
  onLongClick: (() -> Unit)? = null,
  modifier: Modifier = Modifier,
  showNavigationIcon: Boolean = true,
  showAlterCount: Boolean = true,
  settings: Settings
) {
  val name = tag.name

  val useSmallAvatars = settings.useSmallAvatars

  val innerImage = @Composable {
    Surface(
      modifier = Modifier.size(tagCardHeight).clip(MaterialTheme.shapes.medium),
      color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
      Box(
        contentAlignment = Alignment.Center
      ) {
        Icon(
          painter = iconPainter,
          contentDescription = null,
          modifier = Modifier.size(32.dp),
          tint = MaterialTheme.colorScheme.secondary
        )
      }
    }
  }

  ThemeFromColor(
    tag.color,
    colorMode = settings.colorMode,
    dynamicColorType = settings.dynamicColorType,
    colorContrastLevel = settings.colorContrastLevel,
    amoledMode = settings.amoledMode
  ) {
    /*val contentColor by animateColorAsState(
      if (isFronting) MaterialTheme.colorScheme.onSecondaryContainer
      else MaterialTheme.colorScheme.onSurface
    )*/

    Column {
      ElevatedCard(
        modifier = modifier.height(tagCardHeight).fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
          containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
      ) {
        Row(
          modifier = Modifier.fillMaxWidth().combinedClickable(
            onClick = onClick,
            onLongClick = onLongClick,
            onLongClickLabel = Res.string.tag_context_menu.compose
          )
        ) {
          Box(
            modifier = Modifier.size(tagCardHeight)
          ) {
            if (useSmallAvatars) {
              Box(
                modifier = Modifier.fillMaxSize().padding(8.dp)
              ) {
                innerImage()
              }
            } else {
              innerImage()
            }
          }
          Column(
            modifier = Modifier
              .weight(1f)
              .fillMaxHeight()
              .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.Center
          ) {
            Text(
              name,
              style = MaterialTheme.typography.labelLarge,
              overflow = TextOverflow.Ellipsis,
              maxLines = 1
            )
            if (showAlterCount && !tag.alters.isNullOrEmpty()) {
              Spacer(modifier = Modifier.height(2.dp))
              Row {
                Text(
                  "${tag.alters!!.size} alter${if (tag.alters!!.size != 1) "s" else ""}",
                  style = MaterialTheme.typography.labelSmall.merge(color = MaterialTheme.colorScheme.onSurface),
                  overflow = TextOverflow.Ellipsis,
                  maxLines = 1
                )
              }
            }
          }
          if (showNavigationIcon) {
            Box(
              modifier = Modifier
                .fillMaxHeight()
                .padding(12.dp),
              contentAlignment = Alignment.CenterEnd
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                contentDescription = Res.string.open_tag.compose,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.tertiary
              )
            }
          }
        }
      }
    }
  }
}

@Composable
fun DeleteTagDialog(
  tag: MyTag,
  launchDeleteTag: (String) -> Unit,
  onDismissRequest: () -> Unit,
  afterDelete: (() -> Unit)? = null
) {
  AlertDialog(
    icon = {
      Icon(
        Icons.Rounded.Delete,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.error
      )
    },
    title = {
      Text(text = Res.string.delete_tag.compose)
    },
    text = {
      LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        item {
          Text(stringResource(Res.string.confirm_delete_tag, tag.name))
        }
        item {
          Text(
            Res.string.action_irreversible.compose,
            style = MaterialTheme.typography.bodyMedium.merge(fontWeight = FontWeight.SemiBold)
          )
        }
      }
    },
    onDismissRequest = onDismissRequest,
    confirmButton = {
      Button(
        colors = ButtonDefaults.buttonColors(
          containerColor = MaterialTheme.colorScheme.error,
          contentColor = MaterialTheme.colorScheme.onError
        ),
        onClick = {
          launchDeleteTag(tag.id)
          afterDelete?.invoke()
          onDismissRequest()
        }
      ) {
        Text(Res.string.delete.compose)
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
fun CreateTagDialog(
  launchCreateTag: (String) -> Unit,
  onDismissRequest: () -> Unit,
  createTagText: String = Res.string.create_tag.compose
) {
  var name by state("")

  val focusRequester = remember { FocusRequester() }

  val isValid = name.isNotBlank() && name.length <= 100

  AlertDialog(
    icon = {
      Icon(
        Icons.Rounded.CreateNewFolder,
        contentDescription = null
      )
    },
    title = {
      Text(text = createTagText)
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
      }
    },
    onDismissRequest = onDismissRequest,
    confirmButton = {
      TextButton(
        enabled = isValid,
        onClick = {
          launchCreateTag(name)
          onDismissRequest()
        }
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
fun TagContextSheet(
  onDismissRequest: () -> Unit,
  selectedTag: String,
  launchOpenTag: (String) -> Unit,
  launchDeleteTag: (String) -> Unit
) {
  OctoBottomSheet(
    onDismissRequest = onDismissRequest
  ) {
    BottomSheetListItem(
      imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
      title = Res.string.open_tag.compose
    ) {
      launchOpenTag(selectedTag)
      onDismissRequest()
    }
    SpotlightTooltip(
      title = Res.string.delete_tag.compose,
      description = Res.string.tooltip_delete_tag_desc.compose
    ) {
      BottomSheetListItem(
        imageVector = Icons.Rounded.Delete,
        iconTint = MaterialTheme.colorScheme.error,
        title = Res.string.delete_tag.compose
      ) {
        launchDeleteTag(selectedTag)
        onDismissRequest()
      }
    }
  }
}
