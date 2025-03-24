package app.octocon.app.ui.compose.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Create
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Poll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.VectorPainter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import app.octocon.app.Settings
import app.octocon.app.api.model.ChoicePoll
import app.octocon.app.api.model.MyAlter
import app.octocon.app.api.model.Poll
import app.octocon.app.api.model.PollType
import app.octocon.app.api.model.SecurityLevel
import app.octocon.app.api.model.VotePoll
import app.octocon.app.ui.compose.components.shared.BottomSheetListItem
import app.octocon.app.ui.compose.components.shared.NoRippleInteractionSource
import app.octocon.app.ui.compose.components.shared.OctoBottomSheet
import app.octocon.app.ui.compose.components.shared.rememberCollectPressInteractionSource
import app.octocon.app.ui.compose.screens.GLOBAL_PADDING
import app.octocon.app.utils.compose
import app.octocon.app.utils.dateFormat
import app.octocon.app.utils.localeFormatNumber
import app.octocon.app.utils.savedState
import app.octocon.app.utils.state
import com.materialkolor.ktx.harmonizeWithPrimary
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import octoconapp.shared.generated.resources.Res
import octoconapp.shared.generated.resources.abstain_vote_count
import octoconapp.shared.generated.resources.action_irreversible
import octoconapp.shared.generated.resources.add_choice
import octoconapp.shared.generated.resources.cancel
import octoconapp.shared.generated.resources.cast_vote
import octoconapp.shared.generated.resources.choice
import octoconapp.shared.generated.resources.choose_an_alter
import octoconapp.shared.generated.resources.comment
import octoconapp.shared.generated.resources.confirm
import octoconapp.shared.generated.resources.confirm_delete_poll
import octoconapp.shared.generated.resources.confirm_delete_poll_choice
import octoconapp.shared.generated.resources.create
import octoconapp.shared.generated.resources.create_poll
import octoconapp.shared.generated.resources.delete
import octoconapp.shared.generated.resources.delete_poll
import octoconapp.shared.generated.resources.edit_choice
import octoconapp.shared.generated.resources.edit_comment
import octoconapp.shared.generated.resources.end_time
import octoconapp.shared.generated.resources.locked
import octoconapp.shared.generated.resources.name
import octoconapp.shared.generated.resources.no_choices_card_body
import octoconapp.shared.generated.resources.no_choices_card_button
import octoconapp.shared.generated.resources.no_choices_card_title
import octoconapp.shared.generated.resources.no_comment
import octoconapp.shared.generated.resources.no_end_time
import octoconapp.shared.generated.resources.no_vote_count
import octoconapp.shared.generated.resources.no_votes
import octoconapp.shared.generated.resources.no_votes_card_body
import octoconapp.shared.generated.resources.no_votes_card_button
import octoconapp.shared.generated.resources.no_votes_card_title
import octoconapp.shared.generated.resources.poll_ended_at
import octoconapp.shared.generated.resources.poll_ends_at
import octoconapp.shared.generated.resources.remove_choice
import octoconapp.shared.generated.resources.remove_vote
import octoconapp.shared.generated.resources.sheet_no_choices_card_body
import octoconapp.shared.generated.resources.sheet_no_choices_card_title
import octoconapp.shared.generated.resources.title
import octoconapp.shared.generated.resources.veto_vote_count
import octoconapp.shared.generated.resources.view_poll
import octoconapp.shared.generated.resources.vote_type
import octoconapp.shared.generated.resources.yes_vote_count
import org.jetbrains.compose.resources.stringResource
import kotlin.enums.enumEntries
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.min

val randomChoiceColors by lazy {
  listOf(
    Color(0xFF4A90E2), // Dark Blue
    Color(0xFF50E3C2), // Dark Cyan
    Color(0xFFB8E986), // Dark Lime
    Color(0xFFBD10E0), // Dark Violet
    Color(0xFFFF5A5F), // Dark Red
    Color(0xFFFFA700), // Dark Orange
    Color(0xFF7ED321), // Dark Green
    Color(0xFF417505), // Dark Olive
    Color(0xFFD0021B), // Dark Crimson
    Color(0xFF9013FE), // Dark Purple
    Color(0xFF50E3C2), // Dark Teal
    Color(0xFF4A90E2), // Dark Royal Blue
    Color(0xFFF5A623), // Dark Amber
    Color(0xFF417505), // Dark Moss Green
    Color(0xFFD0021B), // Dark Red
    Color(0xFFBD10E0), // Dark Magenta
    Color(0xFF7ED321), // Dark Lime Green
    Color(0xFF9013FE), // Dark Orchid
    Color(0xFFFFA700), // Dark Orange Yellow
    Color(0xFFFF5A5F), // Dark Coral
    Color(0xFF4A90E2), // Dark Blue
    Color(0xFF50E3C2), // Dark Aqua
    Color(0xFFB8E986), // Dark Lime
    Color(0xFFBD10E0), // Dark Purple
    Color(0xFF7ED321), // Dark Forest Green
    Color(0xFFD0021B), // Dark Scarlet
    Color(0xFF9013FE), // Dark Violet
    Color(0xFF4A90E2), // Dark Cobalt Blue
    Color(0xFFF5A623), // Dark Tangerine
    Color(0xFF50E3C2), // Dark Cyan
    Color(0xFF417505), // Dark Olive Green
    Color(0xFFFF5A5F), // Dark Red
    Color(0xFFBD10E0), // Dark Magenta
    Color(0xFF7ED321), // Dark Lime Green
    Color(0xFFFFA700), // Dark Amber Orange
    Color(0xFF9013FE), // Dark Purple
    Color(0xFF4A90E2), // Dark Azure
    Color(0xFF50E3C2), // Dark Aqua Green
    Color(0xFFB8E986), // Dark Lime
    Color(0xFFBD10E0), // Dark Purple
    Color(0xFF7ED321), // Dark Green
    Color(0xFFD0021B), // Dark Red
    Color(0xFF9013FE), // Dark Violet
    Color(0xFFFFA700), // Dark Orange
    Color(0xFFFF5A5F), // Dark Coral
    Color(0xFF4A90E2), // Dark Blue
    Color(0xFF50E3C2), // Dark Cyan
    Color(0xFFB8E986)  // Dark Lime
  )
}

@Composable
fun CreatePollDialog(
  launchCreatePoll: (title: String, type: PollType, timeEnd: Instant?) -> Unit,
  onDismissRequest: () -> Unit
) {
  var title by state("")
  var type by state(PollType.VOTE)
  var timeEnd by state<Instant?>(null)

  val focusRequester = remember { FocusRequester() }

  val pollTypes = remember { enumEntries<PollType>() }

  var dateDialogOpen by savedState(false)

  AlertDialog(
    icon = {
      Icon(
        Icons.Rounded.Poll,
        contentDescription = null
      )
    },
    title = {
      Text(text = Res.string.create_poll.compose)
    },
    text = {
      LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        item {
          TextField(
            value = title,
            onValueChange = {
              if (it.length > 100) return@TextField
              title = it
            },
            label = { Text(Res.string.title.compose) },
            modifier = Modifier.fillMaxWidth().focusRequester(focusRequester)
          )
          LaunchedEffect(true) {
            focusRequester.requestFocus()
          }
        }

        item {
          SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            pollTypes.forEachIndexed { index, pollType ->
              SegmentedButton(
                selected = type == pollType,
                onClick = { type = pollType },
                shape = SegmentedButtonDefaults.itemShape(index, pollTypes.size)
              ) {
                Text(text = pollType.displayName)
              }
            }
          }
        }

        item {
          TextField(
            value = timeEnd?.toLocalDateTime(TimeZone.UTC)?.dateFormat()
              ?: Res.string.no_end_time.compose,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier.fillMaxWidth(),
            interactionSource = rememberCollectPressInteractionSource {
              dateDialogOpen = true
            },
            label = { Text(Res.string.end_time.compose) }
          )
        }
      }
      if (dateDialogOpen) {
        val datePickerState = rememberDatePickerState(selectableDates = object : SelectableDates {
          override fun isSelectableDate(utcTimeMillis: Long): Boolean {
            return utcTimeMillis >= Clock.System.now().toEpochMilliseconds()
          }
        })

        DatePickerDialog(
          onDismissRequest = { dateDialogOpen = false },
          confirmButton = {
            TextButton(
              onClick = {
                dateDialogOpen = false
                timeEnd = Instant.fromEpochMilliseconds(datePickerState.selectedDateMillis!!)
              },
              enabled = datePickerState.selectedDateMillis != null
            ) {
              Text(Res.string.confirm.compose)
            }
          },
          dismissButton = {
            TextButton(onClick = { dateDialogOpen = false }) {
              Text(Res.string.cancel.compose)
            }
          }
        ) {
          DatePicker(state = datePickerState)
        }
      }
    },
    onDismissRequest = onDismissRequest,
    confirmButton = {
      TextButton(
        onClick = {
          launchCreatePoll(title, type, timeEnd)
          onDismissRequest()
        },
        enabled = title.isNotBlank()
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
fun DeletePollDialog(
  poll: Poll,
  launchDeletePoll: (String) -> Unit,
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
      Text(text = Res.string.delete_poll.compose)
    },
    text = {
      LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        item {
          Text(stringResource(Res.string.confirm_delete_poll, poll.title))
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
          launchDeletePoll(poll.id)
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
fun PollContextSheet(
  onDismissRequest: () -> Unit,
  selectedPollID: String,
  launchViewPoll: (String) -> Unit,
  launchDeletePoll: (String) -> Unit
) {
  OctoBottomSheet(
    onDismissRequest = onDismissRequest
  ) {
    BottomSheetListItem(
      imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
      title = Res.string.view_poll.compose
    ) {
      launchViewPoll(selectedPollID)
      onDismissRequest()
    }
    BottomSheetListItem(
      imageVector = Icons.Rounded.Delete,
      iconTint = MaterialTheme.colorScheme.error,
      title = Res.string.delete_poll.compose
    ) {
      launchDeletePoll(selectedPollID)
      onDismissRequest()
    }
  }
}

@Composable
fun ChoicePollEmptyChoicesCard(
  setAddChoiceDialogOpen: (Boolean) -> Unit,
  modifier: Modifier = Modifier
) {
  Card(
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.secondaryContainer
    ),
    elevation = CardDefaults.cardElevation(
      defaultElevation = 1.0.dp
    ),
    modifier = modifier
  ) {
    Column(
      modifier = Modifier.padding(16.dp).fillMaxWidth()
    ) {
      Text(
        Res.string.no_choices_card_title.compose,
        style = MaterialTheme.typography.titleMedium
      )
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        Res.string.no_choices_card_body.compose,
        style = MaterialTheme.typography.bodyMedium.merge(
          lineHeight = 1.5.em
        )
      )
      Spacer(modifier = Modifier.height(12.dp))
      Button(
        onClick = { setAddChoiceDialogOpen(true) },
        colors = ButtonDefaults.buttonColors(
          containerColor = MaterialTheme.colorScheme.primary,
          contentColor = MaterialTheme.colorScheme.onPrimary
        )
      ) {
        Text(Res.string.no_choices_card_button.compose)
      }
    }
  }
}

@Composable
fun ChoicePollAddChoiceDialog(
  onDismissRequest: () -> Unit,
  launchAddChoice: (String) -> Unit
) {
  var name by state("")
  val focusRequester = remember { FocusRequester() }

  AlertDialog(
    icon = {
      Icon(
        Icons.Rounded.Create,
        contentDescription = null
      )
    },
    title = {
      Text(text = Res.string.add_choice.compose)
    },
    text = {
      LazyColumn {
        item {
          TextField(
            value = name,
            onValueChange = {
              if (it.length > 30) return@TextField
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
          launchAddChoice(name)
          onDismissRequest()
        },
        enabled = name.isNotBlank()
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
fun ChoicePollChoiceCard(
  choice: ChoicePoll.PollChoice,
  choiceIndex: Int,
  onClick: (ChoicePoll.PollChoice) -> Unit,
  modifier: Modifier = Modifier
) {
  Card(
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceContainer
    ),
    elevation = CardDefaults.cardElevation(
      defaultElevation = 1.0.dp
    ),
    onClick = { onClick(choice) },
    modifier = modifier
  ) {
    Column(
      modifier = Modifier.padding(12.dp).fillMaxWidth()
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text(
          choice.name,
          style = MaterialTheme.typography.labelLarge,
          modifier = Modifier.weight(1f)
        )
        Box(
          modifier = Modifier
            .size(16.dp)
            .clip(MaterialTheme.shapes.extraSmall)
            .background(MaterialTheme.colorScheme.harmonizeWithPrimary(randomChoiceColors[choiceIndex % randomChoiceColors.size]))
        )
      }
    }
  }
}

@Composable
fun ChoicePollChoiceContextSheet(
  choice: ChoicePoll.PollChoice,
  onDismissRequest: () -> Unit,
  launchEditChoice: (ChoicePoll.PollChoice) -> Unit,
  launchRemoveChoice: (ChoicePoll.PollChoice) -> Unit
) {
  OctoBottomSheet(
    onDismissRequest = onDismissRequest
  ) {
    BottomSheetListItem(
      imageVector = Icons.Rounded.Edit,
      title = Res.string.edit_choice.compose
    ) {
      launchEditChoice(choice)
      onDismissRequest()
    }
    BottomSheetListItem(
      imageVector = Icons.Rounded.Delete,
      iconTint = MaterialTheme.colorScheme.error,
      title = Res.string.remove_choice.compose
    ) {
      launchRemoveChoice(choice)
      onDismissRequest()
    }
  }
}

@Composable
fun ChoicePollEditChoiceDialog(
  choice: ChoicePoll.PollChoice,
  onDismissRequest: () -> Unit,
  launchEditChoice: (choiceID: String, name: String) -> Unit
) {
  var name by state(choice.name)

  val focusRequester = remember { FocusRequester() }

  AlertDialog(
    icon = {
      Icon(
        Icons.Rounded.Edit,
        contentDescription = null
      )
    },
    title = {
      Text(text = Res.string.edit_choice.compose)
    },
    text = {
      LazyColumn {
        item {
          TextField(
            value = name,
            onValueChange = {
              if (it.length > 30) return@TextField
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
          launchEditChoice(choice.id, name)
          onDismissRequest()
        },
        enabled = name.isNotBlank()
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

@Composable
fun ChoicePollRemoveChoiceDialog(
  choice: ChoicePoll.PollChoice,
  onDismissRequest: () -> Unit,
  launchRemoveChoice: (String) -> Unit
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
      Text(text = Res.string.remove_choice.compose)
    },
    text = {
      LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        item {
          Text(stringResource(Res.string.confirm_delete_poll_choice, choice.name))
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
          launchRemoveChoice(choice.id)
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
fun LazyItemScope.PollCard(
  poll: Poll,
  alterIds: List<Int>,
  currentTime: Instant,
  launchViewPoll: (String) -> Unit,
  launchOpenPollSheet: (String) -> Unit,
  modifier: Modifier = Modifier
) =
  when (poll) {
    is VotePoll -> VotePollCard(
      poll,
      alterIds,
      currentTime,
      launchViewPoll,
      launchOpenPollSheet,
      modifier
    )

    is ChoicePoll -> ChoicePollCard(
      poll,
      alterIds,
      currentTime,
      launchViewPoll,
      launchOpenPollSheet,
      modifier
    )
  }

private data class Tuple4<A, B, C, D>(
  val first: A,
  val second: B,
  val third: C,
  val fourth: D
)

@Composable
private fun LazyItemScope.VotePollCard(
  poll: VotePoll,
  alterIds: List<Int>,
  currentTime: Instant,
  launchViewPoll: (String) -> Unit,
  launchOpenPollSheet: (String) -> Unit,
  modifier: Modifier
) {
  val haptics = LocalHapticFeedback.current
  val (yesCount, noCount, abstainCount, vetoCount) = remember(poll.data.responses) {
    val voteCounts = poll.data.responses
      .filter { it.alterID in alterIds }
      .groupingBy { it.vote }
      .eachCount()
    Tuple4(
      voteCounts[VotePoll.VoteType.YES] ?: 0,
      voteCounts[VotePoll.VoteType.NO] ?: 0,
      voteCounts[VotePoll.VoteType.ABSTAIN] ?: 0,
      voteCounts[VotePoll.VoteType.VETO] ?: 0
    )
  }
  val responseCount = yesCount + noCount + abstainCount + vetoCount

  ElevatedCard(
    modifier = modifier.fillMaxWidth().animateItem(),
    colors = CardDefaults.elevatedCardColors(
      containerColor = MaterialTheme.colorScheme.surfaceContainer
    ),
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .combinedClickable(
          onClick = { launchViewPoll(poll.id) },
          onLongClick = {
            launchOpenPollSheet(poll.id)
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
          }
        )
    ) {
      Column(modifier = Modifier.fillMaxWidth()) {
        Row(
          modifier = Modifier.fillMaxWidth().padding(12.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            if (poll.timeEnd != null) {
              val timeZone = remember { TimeZone.currentSystemDefault() }
              val formattedDate = poll.timeEnd.toLocalDateTime(TimeZone.UTC).dateFormat()
              val hasEnded =
                poll.timeEnd.toLocalDateTime(timeZone).date < currentTime.toLocalDateTime(timeZone).date
              Text(
                text = if (hasEnded) {
                  stringResource(Res.string.poll_ended_at, formattedDate)
                } else {
                  stringResource(Res.string.poll_ends_at, formattedDate)
                },
                style = MaterialTheme.typography.labelSmall,
                color = if (hasEnded) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            } else {
              Text(
                Res.string.no_end_time.compose,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            }
            Text(
              poll.title,
              style = MaterialTheme.typography.titleMedium,
              maxLines = 4,
              overflow = TextOverflow.Ellipsis
            )
          }
          Icon(
            poll.icon,
            tint = MaterialTheme.colorScheme.tertiary,
            contentDescription = Res.string.locked.compose
          )
        }
        HorizontalDivider()
        FlowRow(
          modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp).fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          if (responseCount > 0) {
            if (yesCount > 0) {
              AssistChip(
                leadingIcon = {
                  Icon(
                    VotePoll.VoteType.YES.icon,
                    modifier = Modifier.size(16.dp),
                    tint = VotePoll.VoteType.YES.color,
                    contentDescription = Res.string.yes_vote_count.compose
                  )
                },
                label = { Text(localeFormatNumber(yesCount)) },
                onClick = {},
                interactionSource = NoRippleInteractionSource
              )
            }
            if (noCount > 0) {
              AssistChip(
                leadingIcon = {
                  Icon(
                    VotePoll.VoteType.NO.icon,
                    modifier = Modifier.size(16.dp),
                    tint = VotePoll.VoteType.NO.color,
                    contentDescription = Res.string.no_vote_count.compose
                  )
                },
                label = { Text(localeFormatNumber(noCount)) },
                onClick = {},
                interactionSource = NoRippleInteractionSource
              )
            }
            if (abstainCount > 0) {
              AssistChip(
                leadingIcon = {
                  Icon(
                    VotePoll.VoteType.ABSTAIN.icon,
                    modifier = Modifier.size(16.dp),
                    tint = VotePoll.VoteType.ABSTAIN.color,
                    contentDescription = Res.string.abstain_vote_count.compose
                  )
                },
                label = { Text(localeFormatNumber(abstainCount)) },
                onClick = {},
                interactionSource = NoRippleInteractionSource
              )
            }
            if (vetoCount > 0) {
              AssistChip(
                leadingIcon = {
                  Icon(
                    VotePoll.VoteType.VETO.icon,
                    modifier = Modifier.size(16.dp),
                    tint = VotePoll.VoteType.VETO.color,
                    contentDescription = Res.string.veto_vote_count.compose
                  )
                },
                label = { Text(localeFormatNumber(vetoCount)) },
                onClick = {},
                interactionSource = NoRippleInteractionSource
              )
            }
          } else {
            AssistChip(
              label = { Text(Res.string.no_votes.compose) },
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
private fun LazyItemScope.ChoicePollCard(
  poll: ChoicePoll,
  alterIds: List<Int>,
  currentTime: Instant,
  launchViewPoll: (String) -> Unit,
  launchOpenPollSheet: (String) -> Unit,
  modifier: Modifier
) {
  val haptics = LocalHapticFeedback.current

  val allValidResponses = poll.data.responses.filter { it.alterID in alterIds }
  val responses = allValidResponses.groupBy { it.choiceID }
  val choices = poll.data.choices.associateBy { it.id }

  val responsesCount = allValidResponses.size

  ElevatedCard(
    modifier = modifier.fillMaxWidth().animateItem(),
    colors = CardDefaults.elevatedCardColors(
      containerColor = MaterialTheme.colorScheme.surfaceContainer
    ),
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .combinedClickable(
          onClick = { launchViewPoll(poll.id) },
          onLongClick = {
            launchOpenPollSheet(poll.id)
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
          }
        )
    ) {
      Column(modifier = Modifier.fillMaxWidth()) {
        Row(
          modifier = Modifier.fillMaxWidth().padding(12.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            if (poll.timeEnd != null) {
              val timeZone = remember { TimeZone.currentSystemDefault() }
              val formattedDate = poll.timeEnd.toLocalDateTime(TimeZone.UTC).dateFormat()
              val hasEnded =
                poll.timeEnd.toLocalDateTime(timeZone).date < currentTime.toLocalDateTime(timeZone).date
              Text(
                text = if (hasEnded) {
                  stringResource(Res.string.poll_ended_at, formattedDate)
                } else {
                  stringResource(Res.string.poll_ends_at, formattedDate)
                },
                style = MaterialTheme.typography.labelSmall,
                color = if (hasEnded) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            } else {
              Text(
                Res.string.no_end_time.compose,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            }
            Text(
              poll.title,
              style = MaterialTheme.typography.titleMedium,
              maxLines = 4,
              overflow = TextOverflow.Ellipsis
            )
          }
          Icon(
            poll.icon,
            tint = MaterialTheme.colorScheme.tertiary,
            contentDescription = Res.string.locked.compose
          )
        }
        HorizontalDivider()
        FlowRow(
          modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp).fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          if (responsesCount > 0) {
            responses.forEach { (choiceID, responses) ->
              AssistChip(
                label = { Text("${responses.size} - ${choices[choiceID]!!.name}") },
                onClick = {},
                interactionSource = NoRippleInteractionSource
              )
            }
          } else {
            AssistChip(
              label = { Text(Res.string.no_votes.compose) },
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
fun EditVoteCommentDialog(
  alterID: Int,
  initialComment: String?,
  onDismissRequest: () -> Unit,
  launchEditVoteComment: (Int, String?) -> Unit
) {
  var editedComment by savedState(initialComment.orEmpty())

  val focusRequester = remember { FocusRequester() }

  AlertDialog(
    icon = {
      Icon(
        Icons.Rounded.Edit,
        contentDescription = null
      )
    },
    title = {
      Text(text = Res.string.edit_comment.compose)
    },
    text = {
      LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        item {
          TextField(
            value = editedComment,
            onValueChange = {
              if (it.length > 100) return@TextField
              editedComment = it
            },
            label = { Text(Res.string.comment.compose) },
            singleLine = false,
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
          launchEditVoteComment(alterID, editedComment.ifBlank { null })
        }
      ) {
        Text(Res.string.confirm.compose)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismissRequest) {
        Text(Res.string.cancel.compose)
      }
    }
  )
}

@Composable
fun VotePollCastVoteSheet(
  onDismissRequest: () -> Unit,
  allVoteTypes: List<VotePoll.VoteType>,
  selectedAlterID: Int?,
  selectedAlter: MyAlter?,
  setSelectAlterDialogOpen: (Boolean) -> Unit,
  launchAddVote: (Int, VotePoll.VoteType, String?) -> Unit,
  placeholderPainter: VectorPainter,
  coroutineScope: CoroutineScope,
  settings: Settings
) {
  var selectedVoteType by state(VotePoll.VoteType.YES)
  var comment by state<String?>(null)

  val choose_an_alter = Res.string.choose_an_alter.compose
  val placeholderAlter = remember(choose_an_alter) {
    MyAlter(
      id = 999999,
      name = choose_an_alter,
      pronouns = null,
      fields = emptyList(),
      securityLevel = SecurityLevel.PRIVATE
    )
  }
  OctoBottomSheet(
    onDismissRequest = onDismissRequest,
  ) {
    Column(
      verticalArrangement = Arrangement.spacedBy(16.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier.fillMaxWidth()
        .padding(start = GLOBAL_PADDING, end = GLOBAL_PADDING, bottom = GLOBAL_PADDING)
    ) {
      InertAlterCard(
        alter = selectedAlter ?: placeholderAlter,
        onClick = { setSelectAlterDialogOpen(true) },
        isFronting = false,
        isPrimary = false,
        hideSubtext = selectedAlter == null,
        imageContext = coroutineScope.coroutineContext,
        placeholderPainter = placeholderPainter,
        settings = settings
      )
      var expanded by state(false)

      ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
      ) {
        OutlinedTextField(
          modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
          readOnly = true,
          value = selectedVoteType.displayName,
          onValueChange = {},
          label = { Text(Res.string.vote_type.compose) },
          trailingIcon = {
            Icon(
              selectedVoteType.icon,
              tint = selectedVoteType.color,
              contentDescription = null
            )
          },
          colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
        )
        ExposedDropdownMenu(
          expanded = expanded,
          onDismissRequest = { expanded = false },
        ) {
          allVoteTypes.forEach { type ->
            DropdownMenuItem(
              text = { Text(type.displayName) },
              trailingIcon = {
                Icon(
                  type.icon,
                  tint = type.color,
                  contentDescription = null,
                  modifier = Modifier.offset(x = 4.dp)
                )
              },
              onClick = {
                selectedVoteType = type
                expanded = false
              },
              contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
            )
          }
        }
      }

      OutlinedTextField(
        value = comment.orEmpty(),
        onValueChange = {
          if (it.length <= 100) {
            comment = it
          }
        },
        placeholder = { Text(Res.string.no_comment.compose) },
        label = { Text(Res.string.comment.compose) },
        singleLine = false,
        modifier = Modifier.fillMaxWidth()
      )

      Button(
        enabled = selectedAlterID != null,
        onClick = {
          if (selectedAlterID != null) {
            launchAddVote(
              selectedAlterID,
              selectedVoteType,
              comment
            )
            selectedVoteType = VotePoll.VoteType.YES
            onDismissRequest()
          }
        }
      ) {
        Text(Res.string.cast_vote.compose)
      }
    }
  }
}

@Composable
fun ChoicePollCastVoteSheet(
  onDismissRequest: () -> Unit,
  choices: List<ChoicePoll.PollChoice>,
  selectedAlterID: Int?,
  selectedAlter: MyAlter?,
  settings: Settings,
  setSelectAlterDialogOpen: (Boolean) -> Unit,
  launchAddVote: (Int, String, String?) -> Unit,
  placeholderPainter: VectorPainter,
  coroutineScope: CoroutineScope
) {
  if (choices.isEmpty()) {
    OctoBottomSheet(
      onDismissRequest = onDismissRequest,
    ) {
      Card(
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        elevation = CardDefaults.cardElevation(
          defaultElevation = 1.0.dp
        ),
        modifier = Modifier.fillMaxWidth()
          .padding(start = GLOBAL_PADDING, end = GLOBAL_PADDING, bottom = GLOBAL_PADDING)
      ) {
        Column(
          modifier = Modifier.padding(16.dp).fillMaxWidth()
        ) {
          Text(
            Res.string.sheet_no_choices_card_title.compose,
            style = MaterialTheme.typography.titleMedium
          )
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            Res.string.sheet_no_choices_card_body.compose,
            style = MaterialTheme.typography.bodyMedium.merge(
              lineHeight = 1.5.em
            )
          )
        }
      }
    }
    return
  }
  var selectedChoice by state(choices.first().id)
  var comment by state<String?>(null)

  val choose_an_alter = Res.string.choose_an_alter.compose
  val placeholderAlter = remember(choose_an_alter) {
    MyAlter(
      id = 999999,
      name = choose_an_alter,
      pronouns = null,
      fields = emptyList(),
      securityLevel = SecurityLevel.PRIVATE
    )
  }
  OctoBottomSheet(
    onDismissRequest = onDismissRequest,
  ) {
    Column(
      verticalArrangement = Arrangement.spacedBy(16.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier.fillMaxWidth()
        .padding(start = GLOBAL_PADDING, end = GLOBAL_PADDING, bottom = GLOBAL_PADDING)
    ) {
      InertAlterCard(
        alter = selectedAlter ?: placeholderAlter,
        onClick = { setSelectAlterDialogOpen(true) },
        isFronting = false,
        isPrimary = false,
        hideSubtext = selectedAlter == null,
        imageContext = coroutineScope.coroutineContext,
        placeholderPainter = placeholderPainter,
        settings = settings
      )
      var expanded by state(false)

      ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
      ) {
        OutlinedTextField(
          modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
          readOnly = true,
          value = choices.find { it.id == selectedChoice }!!.name,
          onValueChange = {},
          label = { Text(Res.string.choice.compose) },
          colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
        )
        ExposedDropdownMenu(
          expanded = expanded,
          onDismissRequest = { expanded = false },
        ) {
          choices.forEach { choice ->
            DropdownMenuItem(
              text = { Text(choice.name) },
              onClick = {
                selectedChoice = choice.id
                expanded = false
              },
              contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
            )
          }
        }
      }

      OutlinedTextField(
        value = comment.orEmpty(),
        onValueChange = {
          if (it.length <= 100) {
            comment = it
          }
        },
        placeholder = { Text(Res.string.no_comment.compose) },
        label = { Text(Res.string.comment.compose) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
      )

      Button(
        enabled = selectedAlterID != null,
        onClick = {
          if (selectedAlterID != null) {
            launchAddVote(
              selectedAlterID,
              selectedChoice,
              comment
            )
            selectedChoice = choices.first().id
            onDismissRequest()
          }
        }
      ) {
        Text(Res.string.cast_vote.compose)
      }
    }
  }
}

@Composable
fun PollEmptyCard(
  setCastVoteSheetOpen: (Boolean) -> Unit,
  modifier: Modifier = Modifier
) {
  Card(
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.secondaryContainer
    ),
    elevation = CardDefaults.cardElevation(
      defaultElevation = 1.0.dp
    ),
    modifier = modifier
  ) {
    Column(
      modifier = Modifier.padding(16.dp).fillMaxWidth()
    ) {
      Text(
        Res.string.no_votes_card_title.compose,
        style = MaterialTheme.typography.titleMedium
      )
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        Res.string.no_votes_card_body.compose,
        style = MaterialTheme.typography.bodyMedium.merge(
          lineHeight = 1.5.em
        )
      )
      Spacer(modifier = Modifier.height(12.dp))
      Button(
        onClick = { setCastVoteSheetOpen(true) },
        colors = ButtonDefaults.buttonColors(
          containerColor = MaterialTheme.colorScheme.primary,
          contentColor = MaterialTheme.colorScheme.onPrimary
        )
      ) {
        Text(Res.string.no_votes_card_button.compose)
      }
    }
  }

}

@Composable
fun PollAlterContextSheet(
  onDismissRequest: () -> Unit,
  alterID: Int,
  launchEditVoteComment: (Int) -> Unit,
  launchRemoveVote: (Int) -> Unit,
) {
  OctoBottomSheet(
    onDismissRequest = onDismissRequest
  ) {
    BottomSheetListItem(
      imageVector = Icons.Rounded.Edit,
      title = Res.string.edit_comment.compose,
    ) {
      launchEditVoteComment(alterID)
      onDismissRequest()
    }
    BottomSheetListItem(
      imageVector = Icons.Rounded.Delete,
      iconTint = MaterialTheme.colorScheme.error,
      title = Res.string.remove_vote.compose,
    ) {
      launchRemoveVote(alterID)
      onDismissRequest()
    }
  }
}

data class CircleSegment<T>(
  val data: T?,
  val color: Color,
  val count: Int
)

@Composable
fun <T> SegmentedBorderedCircle(
  segments: List<CircleSegment<out T>>,
  inert: Boolean = false,
  selectedSegment: T?,
  setSelection: (T?) -> Unit,
  borderWidth: Dp = 16.dp,
  modifier: Modifier = Modifier
) {
  val selectedSegmentWidth by animateDpAsState(
    if (selectedSegment != null) borderWidth * 1.25f else borderWidth
  )

  val updatedSegments by rememberUpdatedState(segments)

  Canvas(modifier = modifier.let { mod ->
    if (inert) {
      mod
    } else {
      mod.pointerInput(updatedSegments) {
        coroutineScope {
          launch {
            detectTapGestures { offset ->
              val center = Offset(size.width / 2f, size.height / 2f)
              val touchAngle =
                ((atan2(
                  offset.y - center.y,
                  offset.x - center.x
                ) * (180 / PI) + 360) % 360).toFloat()
              val touchDistance = (offset - center).getDistance()
              val borderWidthPx = borderWidth.toPx()
              val radius = (min(size.width, size.height) - borderWidthPx) / 2

              when {
                // Tap inside the circle, but outside the border
                touchDistance < (radius - borderWidthPx / 2) -> {
                  setSelection(null)
                }
                // Tap inside the border
                touchDistance in (radius - borderWidthPx / 2)..(radius + borderWidthPx / 2) -> {
                  var startAngle = -90f
                  val total = updatedSegments.sumOf { it.count }
                  for (segment in updatedSegments) {
                    val sweepAngle = 360f * (segment.count / total.toFloat())
                    val endAngle = startAngle + sweepAngle

                    // Adjust angles to be within [0, 360)
                    val adjustedStartAngle = (startAngle + 360) % 360
                    val adjustedEndAngle = (endAngle + 360) % 360

                    val inSegment = if (adjustedStartAngle <= adjustedEndAngle) {
                      touchAngle in adjustedStartAngle..adjustedEndAngle
                    } else {
                      touchAngle in adjustedStartAngle..360f || touchAngle in 0f..adjustedEndAngle
                    }

                    if (inSegment) {
                      setSelection(segment.data)
                      break
                    }
                    startAngle += sweepAngle
                  }
                }
                // Tap outside the circle
                else -> {
                  setSelection(null)
                }
              }
            }
          }
        }
      }
    }
  }) {
    val total = segments.sumOf { it.count }
    var startAngle = -90f
    val borderWidthPx = borderWidth.toPx()
    val radius = (min(size.width, size.height) - borderWidthPx) / 2

    for (segment in segments) {
      val sweepAngle = 360f * (segment.count / total.toFloat())
      drawArc(
        color = segment.color,
        startAngle = startAngle,
        sweepAngle = sweepAngle,
        useCenter = false,
        topLeft = Offset((size.width - radius * 2) / 2, (size.height - radius * 2) / 2),
        size = Size(radius * 2, radius * 2),
        style = Stroke(width = borderWidthPx, join = StrokeJoin.Round)
      )
      startAngle += sweepAngle
    }

    // Then, draw the selected segment with wider border width
    if (!inert && selectedSegment != null && selectedSegment in segments.map { it.data }) {
      val selectedBorderWidthPx = selectedSegmentWidth.toPx()
      startAngle = -90f
      for (i in 0 until segments.indexOfFirst { it.data == selectedSegment }) {
        startAngle += 360f * (segments[i].count / total.toFloat())
      }
      val selectedSegmentFull = segments.first { it.data == selectedSegment }
      val sweepAngle = 360f * (selectedSegmentFull.count / total.toFloat())

      drawArc(
        color = selectedSegmentFull.color,
        startAngle = startAngle,
        sweepAngle = sweepAngle,
        useCenter = false,
        topLeft = Offset((size.width - radius * 2) / 2, (size.height - radius * 2) / 2),
        size = Size(radius * 2, radius * 2),
        style = Stroke(
          width = selectedBorderWidthPx,
          cap = StrokeCap.Round,
          join = StrokeJoin.Round
        )
      )
    }
  }
}