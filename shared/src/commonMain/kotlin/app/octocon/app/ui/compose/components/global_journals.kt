package app.octocon.app.ui.compose.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.PushPin
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.octocon.app.Settings
import app.octocon.app.api.model.BaseJournalEntry
import app.octocon.app.api.model.GlobalJournalEntry
import app.octocon.app.api.model.MyAlter
import app.octocon.app.ui.compose.theme.ThemeFromColor
import app.octocon.app.utils.compose
import app.octocon.app.utils.dateTimeFormat
import app.octocon.app.utils.savedState
import io.kamel.core.utils.cacheControl
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import octoconapp.shared.generated.resources.Res
import octoconapp.shared.generated.resources.cancel
import octoconapp.shared.generated.resources.confirm
import octoconapp.shared.generated.resources.confirm_unlock_journal_entry_body
import octoconapp.shared.generated.resources.confirm_unlock_journal_entry_title
import octoconapp.shared.generated.resources.create
import octoconapp.shared.generated.resources.create_journal_entry
import octoconapp.shared.generated.resources.delete
import octoconapp.shared.generated.resources.delete_journal_entry
import octoconapp.shared.generated.resources.delete_journal_entry_body
import octoconapp.shared.generated.resources.edit_journal_title
import octoconapp.shared.generated.resources.locked
import octoconapp.shared.generated.resources.ok
import octoconapp.shared.generated.resources.pinned
import octoconapp.shared.generated.resources.title
import octoconapp.shared.generated.resources.unencrypted_warning_body
import octoconapp.shared.generated.resources.unencrypted_warning_title
import kotlin.coroutines.CoroutineContext

@Composable
fun CreateJournalEntryDialog(
  onDismissRequest: () -> Unit,
  launchCreateJournalEntry: (title: String) -> Unit
) {
  var title by savedState("")

  val isTitleValid = with(title.trim()) {
    isNotBlank() && length <= 99
  }

  val focusRequester = remember { FocusRequester() }

  AlertDialog(
    icon = {
      Icon(
        Icons.Rounded.Edit,
        contentDescription = null
      )
    },
    title = {
      Text(text = Res.string.create_journal_entry.compose)
    },
    text = {
      LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        item {
          TextField(
            value = title,
            onValueChange = {
              if (it.length > 99) return@TextField
              title = it
            },
            label = { Text(Res.string.title.compose) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().focusRequester(focusRequester)
          )

          LaunchedEffect(true) {
            focusRequester.requestFocus()
          }
        }
        // Text("Flavor text")
      }
    },
    onDismissRequest = onDismissRequest,
    confirmButton = {
      TextButton(
        onClick = {
          launchCreateJournalEntry(title)
          onDismissRequest()
        },
        enabled = isTitleValid
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
fun UnencryptedWarningDialog(
  onDismissRequest: () -> Unit
) {
  AlertDialog(
    icon = {
      Icon(
        Icons.Rounded.LockOpen,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.error
      )
    },
    title = {
      Text(text = Res.string.unencrypted_warning_title.compose)
    },
    text = {
      LazyColumn {
        item {
          Text(Res.string.unencrypted_warning_body.compose)
        }
      }
    },
    onDismissRequest = onDismissRequest,
    confirmButton = {
      TextButton(
        onClick = onDismissRequest
      ) {
        Text(Res.string.ok.compose)
      }
    }
  )
}

@Composable
fun <T : BaseJournalEntry> DeleteJournalEntryDialog(
  journalEntry: T,
  onDismissRequest: () -> Unit,
  launchDeleteJournalEntry: (T) -> Unit
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
      Text(text = Res.string.delete_journal_entry.compose)
    },
    text = {
      LazyColumn {
        item {
          Text(Res.string.delete_journal_entry_body.compose)
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
          launchDeleteJournalEntry(journalEntry)
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
        Text(Res.string.cancel.compose)
      }
    }
  )
}

@Composable
fun <T : BaseJournalEntry> ConfirmUnlockJournalEntryDialog(
  journalEntry: T,
  onDismissRequest: () -> Unit,
  launchViewJournalEntry: (T) -> Unit
) {
  val haptics = LocalHapticFeedback.current
  var confirmCount by savedState(0)
  AlertDialog(
    icon = {
      Icon(
        Icons.Rounded.Lock,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.error
      )
    },
    title = {
      Text(text = Res.string.confirm_unlock_journal_entry_title.compose)
    },
    text = {
      LazyColumn {
        item {
          Text(Res.string.confirm_unlock_journal_entry_body.compose)
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
          haptics.performHapticFeedback(when(confirmCount) {
            0 -> HapticFeedbackType.SegmentTick
            1 -> HapticFeedbackType.ToggleOn
            else -> HapticFeedbackType.LongPress
          })
          confirmCount++
          if (confirmCount >= 3) {
            launchViewJournalEntry(journalEntry)
            onDismissRequest()
          }
        }
      ) {
        Text(Res.string.confirm.compose + if (confirmCount > 0) " ($confirmCount/3)" else "")
      }
    },
    dismissButton = {
      TextButton(
        onClick = onDismissRequest
      ) {
        Text(Res.string.cancel.compose)
      }
    }
  )
}

@Composable
fun LazyItemScope.GlobalJournalEntryCard(
  journalEntry: GlobalJournalEntry,
  alters: List<MyAlter>,
  imageContext: CoroutineContext,
  placeholderPainter: Painter,
  launchViewJournalEntry: (GlobalJournalEntry) -> Unit,
  launchOpenJournalEntrySheet: (GlobalJournalEntry) -> Unit,
  settings: Settings,
  modifier: Modifier = Modifier
) {
  val haptics = LocalHapticFeedback.current
  val validAlters = journalEntry.alters.mapNotNull {
    alters.find { alter -> alter.id == it }
  }

  ThemeFromColor(
    journalEntry.color,
    colorMode = settings.colorMode,
    dynamicColorType = settings.dynamicColorType,
    colorContrastLevel = settings.colorContrastLevel,
    amoledMode = settings.amoledMode
  ) {
    ElevatedCard(
      modifier = modifier.fillMaxWidth().animateItem(),
      colors = CardDefaults.elevatedCardColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainer
      )
    ) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .combinedClickable(
            onClick = { launchViewJournalEntry(journalEntry) },
            onLongClick = {
              launchOpenJournalEntrySheet(journalEntry)
              haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            }
          )
      ) {
        Row(
          modifier = Modifier.fillMaxSize().padding(12.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Text(
              journalEntry.insertedAt.toLocalDateTime(TimeZone.currentSystemDefault())
                .dateTimeFormat(),
              style = MaterialTheme.typography.labelSmall,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
            Text(
              journalEntry.title,
              style = MaterialTheme.typography.titleMedium,
              maxLines = 4,
              overflow = TextOverflow.Ellipsis
            )
            if (validAlters.isNotEmpty()) {
              FlowRow(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
              ) {
                validAlters.forEach { alter ->
                  ThemeFromColor(
                    alter.color,
                    colorMode = settings.colorMode,
                    dynamicColorType = settings.dynamicColorType,
                    colorContrastLevel = settings.colorContrastLevel,
                    amoledMode = settings.amoledMode
                  ) {
                    if (alter.avatarUrl == null || alter.avatarUrl == "") {
                      GlobalJournalCardPlaceholderImage(placeholderPainter)
                    } else {
                      KamelImage(
                        {
                          asyncPainterResource(alter.avatarUrl) {
                            coroutineContext = imageContext
                            requestBuilder {
                              cacheControl("max-age=31536000, immutable")
                            }
                          }
                        },
                        // onLoading = { PlaceholderImage(placeholderPainter) },
                        onFailure = {
                          GlobalJournalCardPlaceholderImage(placeholderPainter)
                        },
                        contentDescription = null,
                        modifier = Modifier.size(32.dp).clip(CircleShape),
                        animationSpec = tween(),
                      )
                    }
                  }
                }
              }
            }
          }
          AnimatedVisibility(
            visible = journalEntry.locked || journalEntry.pinned,
            enter = fadeIn() + scaleIn(),
            exit = scaleOut() + fadeOut()
          ) {
            AnimatedContent(
              targetState = journalEntry.locked,
            ) {
              if (it) {
                Icon(
                  Icons.Rounded.Lock,
                  tint = MaterialTheme.colorScheme.tertiary,
                  contentDescription = Res.string.locked.compose
                )
              } else {
                Icon(
                  Icons.Rounded.PushPin,
                  tint = MaterialTheme.colorScheme.tertiary,
                  contentDescription = Res.string.pinned.compose
                )
              }
            }
          }
        }
      }
    }
  }
}

private val alterCircleSizeModifier = Modifier.size(32.dp)

@Suppress("LocalVariableName")
@Composable
fun GlobalJournalCardPlaceholderImage(
  painter: Painter,
  onClick: (() -> Unit)? = null
) {
  val color = MaterialTheme.colorScheme.surfaceContainerHigh
  val shape = CircleShape

  val Parent: @Composable (content: @Composable () -> Unit) -> Unit = if (onClick != null) {
    { content ->
      Surface(
        modifier = alterCircleSizeModifier,
        color = color,
        shape = shape,
        onClick = onClick,
        content = content,
      )
    }
  } else {
    { content ->
      Surface(
        modifier = alterCircleSizeModifier,
        color = color,
        shape = shape,
        content = content
      )
    }
  }
  Parent {
    Box(
      contentAlignment = Alignment.Center
    ) {
      Icon(
        painter = painter,
        contentDescription = null,
        modifier = Modifier.size(24.dp),
        tint = MaterialTheme.colorScheme.secondary
      )
    }

  }
}

@Composable
fun GlobalJournalCardAddAlterButton(
  painter: Painter,
  launchAddAlter: () -> Unit
) {
  Surface(
    modifier = Modifier.size(32.dp).semantics { role = Role.Button },
    color = MaterialTheme.colorScheme.secondary,
    onClick = launchAddAlter,
    shape = CircleShape
  ) {
    Box(
      contentAlignment = Alignment.Center
    ) {
      Icon(
        painter = painter,
        contentDescription = null,
        modifier = Modifier.size(24.dp),
        tint = MaterialTheme.colorScheme.onSecondary
      )
    }
  }
}

@Composable
fun EditJournalTitleDialog(
  initialTitle: String,
  onDismissRequest: () -> Unit,
  launchEditJournalTitle: (String) -> Unit
) {
  var editedTitle by savedState(initialTitle)

  val focusRequester = remember { FocusRequester() }

  AlertDialog(
    icon = {
      Icon(
        Icons.Rounded.Edit,
        contentDescription = null
      )
    },
    title = {
      Text(text = Res.string.edit_journal_title.compose)
    },
    text = {
      LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        item {
          TextField(
            value = editedTitle,
            onValueChange = {
              if (it.length > 99) return@TextField
              editedTitle = it
            },
            label = { Text(Res.string.title.compose) },
            singleLine = true,
            modifier = Modifier.focusRequester(focusRequester)
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
          onDismissRequest()
          launchEditJournalTitle(editedTitle)
        }
      ) {
        Text(Res.string.confirm.compose)
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