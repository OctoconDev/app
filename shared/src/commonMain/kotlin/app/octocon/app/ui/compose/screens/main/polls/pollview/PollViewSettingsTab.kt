package app.octocon.app.ui.compose.screens.main.polls.pollview

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import app.octocon.app.api.model.ChoicePoll
import app.octocon.app.api.model.VotePoll
import app.octocon.app.ui.compose.components.ChoicePollAddChoiceDialog
import app.octocon.app.ui.compose.components.ChoicePollChoiceCard
import app.octocon.app.ui.compose.components.ChoicePollChoiceContextSheet
import app.octocon.app.ui.compose.components.ChoicePollEditChoiceDialog
import app.octocon.app.ui.compose.components.ChoicePollEmptyChoicesCard
import app.octocon.app.ui.compose.components.ChoicePollRemoveChoiceDialog
import app.octocon.app.ui.compose.components.shared.rememberCollectPressInteractionSource
import app.octocon.app.ui.compose.screens.GLOBAL_PADDING
import app.octocon.app.ui.compose.theme.getSubsectionStyle
import app.octocon.app.ui.compose.utils.blankErrorText
import app.octocon.app.ui.model.main.polls.pollview.PollViewSettingsComponent
import app.octocon.app.utils.compose
import app.octocon.app.utils.dateFormat
import app.octocon.app.utils.savedState
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import octoconapp.shared.generated.resources.Res
import octoconapp.shared.generated.resources.allow_veto
import octoconapp.shared.generated.resources.cancel
import octoconapp.shared.generated.resources.choices
import octoconapp.shared.generated.resources.confirm
import octoconapp.shared.generated.resources.description
import octoconapp.shared.generated.resources.end_time
import octoconapp.shared.generated.resources.no_end_time
import octoconapp.shared.generated.resources.poll_description
import octoconapp.shared.generated.resources.poll_title
import octoconapp.shared.generated.resources.remove_end_time
import octoconapp.shared.generated.resources.title

@Composable
fun PollViewSettingsTab(
  component: PollViewSettingsComponent
) {
  val model = component.model
  val settings by component.settings.collectAsState()

  val poll by model.apiPoll.collectAsState()
  val isChoicePoll = poll!! is ChoicePoll

  val haptics = LocalHapticFeedback.current

  val timeEnd by model.timeEnd.collectAsState()
  val data by model.data.collectAsState()

  val title by model.title.collectAsState()
  val description by model.description.collectAsState()

  val lazyListState = rememberLazyListState()

  // ChoicePoll-specific state
  var addChoiceDialogOpen by savedState(false)
  var selectedChoice by savedState<ChoicePoll.PollChoice?>(null)
  var choiceToEdit by savedState<ChoicePoll.PollChoice?>(null)
  var choiceToRemove by savedState<ChoicePoll.PollChoice?>(null)

  var dateDialogOpen by savedState(false)

  LaunchedEffect(true) {
    component.updateOpenCreateChoiceDialog { addChoiceDialogOpen = it }
  }

  // val titleState = rememberTextFieldState()
  // val descriptionState = rememberTextFieldState()

  /*UpdateTextFieldStatesOnLoad(
    model.isLoaded,
    titleState to model.title,
    descriptionState to model.description
  )*/

  LazyColumn(
    modifier = Modifier.fillMaxSize().imePadding(),
    state = lazyListState,
    contentPadding = PaddingValues(vertical = GLOBAL_PADDING),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    item {
      TextField(
        // state = titleState,
        // inputTransformation = ModelTransformation(model::updateTitle),
        value = title,
        onValueChange = model::updateTitle,
        placeholder = { Text(Res.string.poll_title.compose) },
        isError = title.isBlank(),
        supportingText = blankErrorText(title),
        label = { Text(Res.string.title.compose) },
        singleLine = true,
        // lineLimits = TextFieldLineLimits.SingleLine,
        modifier = Modifier.fillMaxWidth()
          .padding(bottom = 6.dp, start = GLOBAL_PADDING, end = GLOBAL_PADDING)
      )
    }
    item { Spacer(modifier = Modifier.height(16.dp)) }
    item {
      TextField(
        // state = descriptionState,
        // inputTransformation = ModelTransformation(model::updateDescription),
        value = description.orEmpty(),
        onValueChange = model::updateDescription,
        placeholder = { Text(Res.string.poll_description.compose) },
        label = { Text(Res.string.description.compose) },
        modifier = Modifier.fillMaxWidth()
          .padding(bottom = 6.dp, start = GLOBAL_PADDING, end = GLOBAL_PADDING)
      )
    }
    item { Spacer(modifier = Modifier.height(16.dp)) }
    item {
      Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = GLOBAL_PADDING),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        OutlinedTextField(
          /*state = rememberTextFieldState(
            timeEnd?.toLocalDateTime(TimeZone.UTC)?.dateFormat()
            ?: Res.string.no_end_time.compose
          ),*/
          value = timeEnd?.toLocalDateTime(TimeZone.UTC)?.dateFormat()
            ?: Res.string.no_end_time.compose,
          onValueChange = {},
          readOnly = true,
          modifier = Modifier.weight(1f),
          interactionSource = rememberCollectPressInteractionSource {
            dateDialogOpen = true
          },
          label = { Text(Res.string.end_time.compose) }
        )
        if (timeEnd != null) {
          FilledTonalIconButton(
            onClick = { model.updateTimeEnd(null) },
            modifier = Modifier.padding(top = 4.dp)
          ) {
            Icon(
              Icons.Rounded.Close,
              contentDescription = Res.string.remove_end_time.compose
            )
          }
        }
      }
    }
    item { Spacer(modifier = Modifier.height(16.dp)) }
    when (poll!!) {
      is VotePoll -> {
        item {
          OutlinedCard(
            shape = MaterialTheme.shapes.extraSmall,
            // Use same outline color as text fields
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier.fillMaxWidth()
              .padding(start = GLOBAL_PADDING, end = GLOBAL_PADDING, top = 4.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxSize()
                .padding(horizontal = GLOBAL_PADDING, vertical = 8.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
            ) {
              Text(
                text = Res.string.allow_veto.compose,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.weight(1f)
              )

              Switch(
                checked = (data as VotePoll.Data).allowVeto,
                onCheckedChange = {
                  model.updateData((data as VotePoll.Data).setAllowVeto(it))
                  haptics.performHapticFeedback(
                    if (it) HapticFeedbackType.ToggleOn else HapticFeedbackType.ToggleOff
                  )
                },
              )
            }
          }
        }
      }

      is ChoicePoll -> {
        val choices = (data as ChoicePoll.Data).choices
        item {
          Text(
            Res.string.choices.compose,
            modifier = Modifier.fillMaxWidth()
              .padding(horizontal = GLOBAL_PADDING, vertical = 8.dp),
            style = getSubsectionStyle(settings.fontSizeScalar)
          )
        }
        item { Spacer(modifier = Modifier.height(16.dp)) }
        if (choices.isEmpty()) {
          item {
            ChoicePollEmptyChoicesCard(
              setAddChoiceDialogOpen = { addChoiceDialogOpen = true },
              modifier = Modifier.fillMaxWidth().padding(horizontal = GLOBAL_PADDING)
            )
          }
        } else {
          itemsIndexed(choices) { index, choice ->
            if (index != 0) {
              Spacer(modifier = Modifier.height(12.dp))
            }
            ChoicePollChoiceCard(
              choice = choice,
              choiceIndex = index,
              onClick = { selectedChoice = choice },
              modifier = Modifier.fillMaxWidth().padding(horizontal = GLOBAL_PADDING)
            )
          }
        }
      }
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
            model.updateTimeEnd(Instant.fromEpochMilliseconds(datePickerState.selectedDateMillis!!))
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

  if (isChoicePoll && addChoiceDialogOpen) {
    ChoicePollAddChoiceDialog(
      onDismissRequest = { addChoiceDialogOpen = false },
      launchAddChoice = {
        model.updateData(
          (data as ChoicePoll.Data).addChoice(it)
        )
      }
    )
  }

  if (isChoicePoll && selectedChoice != null) {
    ChoicePollChoiceContextSheet(
      choice = selectedChoice!!,
      onDismissRequest = { selectedChoice = null },
      launchEditChoice = { choiceToEdit = it },
      launchRemoveChoice = { choiceToRemove = it }
    )
  }
  if (isChoicePoll && choiceToEdit != null) {
    ChoicePollEditChoiceDialog(
      choice = choiceToEdit!!,
      onDismissRequest = { choiceToEdit = null },
      launchEditChoice = { choiceID, name ->
        model.updateData(
          (data as ChoicePoll.Data).editChoice(choiceID, name)
        )
      }
    )
  }
  if (isChoicePoll && choiceToRemove != null) {
    ChoicePollRemoveChoiceDialog(
      choice = choiceToRemove!!,
      onDismissRequest = { choiceToRemove = null },
      launchRemoveChoice = {
        model.updateData(
          (data as ChoicePoll.Data).removeChoice(it)
        )
      }
    )
  }
}
