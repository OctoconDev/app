package app.octocon.app.ui.compose.screens.main.polls.pollview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.octocon.app.Settings
import app.octocon.app.api.model.ChoicePoll
import app.octocon.app.api.model.VotePoll
import app.octocon.app.ui.compose.components.ChoicePollCastVoteSheet
import app.octocon.app.ui.compose.components.CircleSegment
import app.octocon.app.ui.compose.components.EditVoteCommentDialog
import app.octocon.app.ui.compose.components.InertAlterCard
import app.octocon.app.ui.compose.components.PollAlterContextSheet
import app.octocon.app.ui.compose.components.PollEmptyCard
import app.octocon.app.ui.compose.components.SegmentedBorderedCircle
import app.octocon.app.ui.compose.components.VotePollCastVoteSheet
import app.octocon.app.ui.compose.components.randomChoiceColors
import app.octocon.app.ui.compose.components.shared.AttachAlterDialog
import app.octocon.app.ui.compose.components.shared.IndeterminateProgressSpinner
import app.octocon.app.ui.compose.screens.GLOBAL_PADDING
import app.octocon.app.ui.compose.theme.getSubsectionStyle
import app.octocon.app.ui.model.interfaces.ApiInterface
import app.octocon.app.ui.model.main.polls.PollViewComponent
import app.octocon.app.ui.model.main.polls.pollview.PollViewResultsComponent
import app.octocon.app.utils.compose
import app.octocon.app.utils.derive
import app.octocon.app.utils.state
import com.materialkolor.ktx.harmonizeWithPrimary

import octoconapp.shared.generated.resources.Res
import octoconapp.shared.generated.resources.choose_an_alter
import octoconapp.shared.generated.resources.no_votes
import octoconapp.shared.generated.resources.poll_no_valid_alters
import kotlin.enums.enumEntries
import kotlin.math.round

@Composable
fun PollViewResultsTab(
  component: PollViewResultsComponent
) {
  val api = component.api
  val settings by component.settings.collectAsState()
  val model = component.model
  val data by model.data.collectAsState()

  val poll by model.apiPoll.collectAsState()

  var castVoteSheetOpen by state(false)
  val lazyListState = rememberLazyListState()

  LaunchedEffect(Unit) {
    component.updateOpenCastVoteDialog { castVoteSheetOpen = it }
  }

  if (poll is VotePoll) {
    VotePollResults(
      api,
      model,
      settings,
      lazyListState,
      castVoteSheetOpen,
      setCastVoteSheetOpen = { castVoteSheetOpen = it },
      data as VotePoll.Data
    )
  } else {
    ChoicePollResults(
      api,
      model,
      settings,
      lazyListState,
      castVoteSheetOpen,
      setCastVoteSheetOpen = { castVoteSheetOpen = it },
      data as ChoicePoll.Data
    )
  }
}

@Composable
private fun VotePollResults(
  api: ApiInterface,
  model: PollViewComponent.Model,
  settings: Settings,
  lazyListState: LazyListState,
  castVoteSheetOpen: Boolean,
  setCastVoteSheetOpen: (Boolean) -> Unit,
  data: VotePoll.Data
) {
  val voteTypes = remember { enumEntries<VotePoll.VoteType>() }
  val voteColors = voteTypes.associateWith { it.color }
  val emptyTrackColor = MaterialTheme.colorScheme.surfaceContainerHigh

  val placeholderPainter = rememberVectorPainter(Icons.Rounded.Person)
  var selectAlterDialogOpen by state(false)


  var selectedAlterForContextSheet by state<Int?>(null)
  var selectedAlterForEditComment by state<Int?>(null)

  val coroutineScope = rememberCoroutineScope()

  var displayedVoteType by state<VotePoll.VoteType?>(null)

  val allVoteTypes = remember(data.allowVeto) {
    enumEntries<VotePoll.VoteType>().filter {
      it != VotePoll.VoteType.VETO || data.allowVeto
    }
  }

  val alters by api.alters.collectAsState()

  if (!alters.isSuccess) {
    IndeterminateProgressSpinner()
    return
  }

  var selectedAlterID by state<Int?>(null)
  val selectedAlter by derive {
    alters.ensureData.find { it.id == selectedAlterID }
  }

  val alterIds = remember(alters) { alters.ensureData.map { it.id } }

  val validResponses = data.responses.filter { it.alterID in alterIds }

  val results =
    validResponses
      .groupingBy { it.vote }
      .eachCount()
      .toList()
      .sortedBy { it.first.ordinal }
      .toMap()

  val votesToAlters = validResponses.groupBy({ it.vote }) {
    alters.ensureData.find { alter -> alter.id == it.alterID }!!
  }.toList().sortedBy { it.first.ordinal }

  val segments =
    results
      .map { (vote, count) ->
        CircleSegment(vote, voteColors[vote]!!, count)
      }
      .ifEmpty {
        listOf(CircleSegment(null, emptyTrackColor, 1))
      }

  LazyColumn(
    modifier = Modifier.fillMaxSize().imePadding(),
    state = lazyListState,
    contentPadding = PaddingValues(vertical = GLOBAL_PADDING),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    item {
      Box(
        modifier = Modifier.padding(horizontal = GLOBAL_PADDING).sizeIn(
          maxWidth = 312.dp,
          maxHeight = 312.dp
        ).aspectRatio(1.0F),
        contentAlignment = Alignment.Center,
      ) {
        if (data.responses.isEmpty()) {
          Text(
            text = Res.string.no_votes.compose,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface
          )
        } else {
          LazyColumn(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
          ) {
            results.forEach { (vote, count) ->
              if (displayedVoteType != null && displayedVoteType != vote) {
                return@forEach
              }
              item(key = vote.ordinal) {
                val percentage =
                  round((count.toDouble() / data.responses.size) * 100).toInt()
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.Center,
                  modifier = Modifier.padding(vertical = 3.dp).animateItem()
                ) {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.height(18.dp).clip(MaterialTheme.shapes.extraSmall)
                      .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                  ) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(
                      modifier = Modifier
                        .size(12.dp)
                        .clip(MaterialTheme.shapes.extraSmall)
                        .background(vote.color)
                    )
                    Text(
                      text = vote.displayName,
                      style = MaterialTheme.typography.labelSmall,
                      modifier = Modifier.padding(horizontal = 4.dp),
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis
                    )
                  }
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = "=",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = "${percentage}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface
                  )
                }
              }
            }
          }
        }
        SegmentedBorderedCircle(
          segments,
          inert = data.responses.isEmpty(),
          selectedSegment = displayedVoteType,
          setSelection = {
            displayedVoteType = it
          },
          borderWidth = 24.dp,
          modifier = Modifier.fillMaxSize()
        )
      }
    }
    item {
      Spacer(modifier = Modifier.height(16.dp))
    }
    if (data.responses.isEmpty()) {
      item {
        PollEmptyCard(
          setCastVoteSheetOpen,
          modifier = Modifier.padding(horizontal = GLOBAL_PADDING)
        )
      }
    } else {
      if (displayedVoteType == null) {
        votesToAlters.onEachIndexed { index, (voteType, alters) ->
          item(key = "V" + voteType.ordinal) {
            Row(
              modifier = Modifier.fillMaxWidth().padding(
                top = if (index == 0) 8.dp else 24.dp,
                bottom = 12.dp,
                start = GLOBAL_PADDING,
                end = GLOBAL_PADDING
              ),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = voteType.displayName,
                style = getSubsectionStyle(settings.fontSizeScalar),
                modifier = Modifier.weight(1f)
              )
              Icon(
                voteType.icon,
                contentDescription = null,
                tint = voteType.color,
              )
            }
          }
          items(alters, key = { it.id }) { alter ->
            Spacer(modifier = Modifier.height(12.dp))
            InertAlterCard(
              alter = alter,
              onClick = { selectedAlterForContextSheet = alter.id },
              isFronting = false,
              isPrimary = false,
              frontComment = data.responses.find { it.alterID == alter.id }?.comment,
              imageContext = coroutineScope.coroutineContext,
              placeholderPainter = placeholderPainter,
              modifier = Modifier.padding(horizontal = GLOBAL_PADDING),
              settings = settings
            )
          }
        }
      } else {
        votesToAlters.first {
          it.first == displayedVoteType
        }.let { (_, alters) ->
          item(key = "V" + displayedVoteType!!.ordinal) {
            Row(
              modifier = Modifier.fillMaxWidth().padding(
                top = 8.dp,
                bottom = 12.dp,
                start = GLOBAL_PADDING,
                end = GLOBAL_PADDING
              ),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = displayedVoteType!!.displayName,
                style = getSubsectionStyle(settings.fontSizeScalar),
                modifier = Modifier.weight(1f)
              )
              Icon(
                displayedVoteType!!.icon,
                contentDescription = null,
                tint = displayedVoteType!!.color,
              )
            }
          }
          items(alters, key = { it.id }) { alter ->
            Spacer(modifier = Modifier.height(12.dp))
            InertAlterCard(
              alter = alter,
              onClick = { selectedAlterForContextSheet = alter.id },
              isFronting = false,
              isPrimary = false,
              frontComment = data.responses.find { it.alterID == alter.id }?.comment,
              imageContext = coroutineScope.coroutineContext,
              placeholderPainter = placeholderPainter,
              modifier = Modifier.padding(horizontal = GLOBAL_PADDING),
              settings = settings
            )
          }
        }
      }
    }
  }
  if (castVoteSheetOpen) {
    VotePollCastVoteSheet(
      onDismissRequest = {
        setCastVoteSheetOpen(false)
        selectedAlterID = null
      },
      allVoteTypes = allVoteTypes,
      selectedAlterID = selectedAlterID,
      selectedAlter = selectedAlter,
      launchAddVote = { id, type, comment ->
        model.updateData(
          data.addVote(
            id,
            type,
            comment
          )
        )

        displayedVoteType = null
      },
      setSelectAlterDialogOpen = { selectAlterDialogOpen = it },
      placeholderPainter = placeholderPainter,
      coroutineScope = coroutineScope,
      settings = settings
    )
  }
  if (selectAlterDialogOpen) {
    AttachAlterDialog(
      existingAlters = data.responses.map { it.alterID }
          + (if (selectedAlterID != null) listOf(selectedAlterID!!) else emptyList()),
      alters = alters.ensureData,
      onDismissRequest = { selectAlterDialogOpen = false },
      placeholderPainter = placeholderPainter,
      launchAttachAlter = { selectedAlterID = it },
      attachText = Res.string.choose_an_alter.compose,
      noAltersText = Res.string.poll_no_valid_alters.compose,
      settings = settings
    )
  }
  if (selectedAlterForContextSheet != null) {
    PollAlterContextSheet(
      alterID = selectedAlterForContextSheet!!,
      launchEditVoteComment = {
        selectedAlterForEditComment = it
      },
      launchRemoveVote = {
        displayedVoteType = null
        model.updateData(
          data.removeVote(it)
        )
      },
      onDismissRequest = {
        selectedAlterForContextSheet = null
      },
    )
  }
  if (selectedAlterForEditComment != null) {
    EditVoteCommentDialog(
      alterID = selectedAlterForEditComment!!,
      initialComment = data.responses.find { it.alterID == selectedAlterForEditComment!! }!!.comment,
      onDismissRequest = {
        selectedAlterForEditComment = null
      },
      launchEditVoteComment = { alterID, comment ->
        model.updateData(
          data.editVoteComment(alterID, comment)
        )
      }
    )
  }
}

@Composable
private fun ChoicePollResults(
  api: ApiInterface,
  model: PollViewComponent.Model,
  settings: Settings,
  lazyListState: LazyListState,
  castVoteSheetOpen: Boolean,
  setCastVoteSheetOpen: (Boolean) -> Unit,
  data: ChoicePoll.Data
) {
  val emptyTrackColor = MaterialTheme.colorScheme.surfaceContainerHigh

  val placeholderPainter = rememberVectorPainter(Icons.Rounded.Person)
  var selectAlterDialogOpen by state(false)

  var selectedAlterForContextSheet by state<Int?>(null)
  var selectedAlterForEditComment by state<Int?>(null)

  val coroutineScope = rememberCoroutineScope()

  var displayedChoiceType by state<String?>(null)

  val alters by api.alters.collectAsState()

  if (!alters.isSuccess) {
    IndeterminateProgressSpinner()
    return
  }

  var selectedAlterID by state<Int?>(null)
  val selectedAlter by derive {
    alters.ensureData.find { it.id == selectedAlterID }
  }

  val alterIds = remember(alters) { alters.ensureData.map { it.id } }

  val validResponses = data.responses.filter { it.alterID in alterIds }

  val results =
    validResponses
      .groupingBy { it.choiceID }
      .eachCount()

  val votesToAlters = validResponses.groupBy({ it.choiceID }) {
    alters.ensureData.find { alter -> alter.id == it.alterID }!!
  }.toList()

  val segments =
    results
      .toList()
      .mapIndexed { index, (choiceID, count) ->
        CircleSegment(
          choiceID,
          MaterialTheme.colorScheme.harmonizeWithPrimary(randomChoiceColors[index % randomChoiceColors.size]),
          count
        )
      }
      .ifEmpty {
        listOf(CircleSegment(null, emptyTrackColor, 1))
      }

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    state = lazyListState,
    contentPadding = PaddingValues(vertical = GLOBAL_PADDING),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    item {
      Box(
        modifier = Modifier.padding(horizontal = GLOBAL_PADDING).sizeIn(
          maxWidth = 312.dp,
          maxHeight = 312.dp
        ).aspectRatio(1.0F),
        contentAlignment = Alignment.Center,
      ) {
        if (data.responses.isEmpty()) {
          Text(
            text = Res.string.no_votes.compose,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface
          )
        } else {
          LazyColumn(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize().padding(32.dp)
          ) {
            results.onEachIndexed { index, (choiceID, count) ->
              if (displayedChoiceType != null && displayedChoiceType != choiceID) {
                return@onEachIndexed
              }
              item(key = choiceID) {
                val percentage =
                  round((count.toDouble() / data.responses.size) * 100).toInt()
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(4.dp),
                  modifier = Modifier.padding(vertical = 3.dp).animateItem()
                ) {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.height(18.dp).clip(MaterialTheme.shapes.extraSmall)
                      .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                  ) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(
                      modifier = Modifier
                        .size(12.dp)
                        .clip(MaterialTheme.shapes.extraSmall)
                        .background(
                          MaterialTheme.colorScheme.harmonizeWithPrimary(
                            randomChoiceColors[index % randomChoiceColors.size]
                          )
                        )
                    )
                    Text(
                      text = data.choices.find { it.id == choiceID }!!.name,
                      style = MaterialTheme.typography.labelSmall,
                      modifier = Modifier.padding(horizontal = 4.dp)
                    )
                  }
                  Text(
                    text = "=",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface
                  )
                  Text(
                    text = "${percentage}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface
                  )
                }
              }
            }
          }
        }
        SegmentedBorderedCircle(
          segments,
          inert = data.responses.isEmpty(),
          selectedSegment = displayedChoiceType,
          setSelection = {
            displayedChoiceType = it
          },
          borderWidth = 24.dp,
          modifier = Modifier.fillMaxSize()
        )
      }
    }
    item {
      Spacer(modifier = Modifier.height(16.dp))
    }
    if (data.responses.isEmpty()) {
      item {
        PollEmptyCard(
          setCastVoteSheetOpen,
          modifier = Modifier.padding(horizontal = GLOBAL_PADDING)
        )
      }
    } else {
      if (displayedChoiceType == null) {
        votesToAlters.onEachIndexed { index, (choiceID, alters) ->
          item(key = "V${choiceID}") {
            Row(
              modifier = Modifier.fillMaxWidth().padding(
                top = if (index == 0) 8.dp else 24.dp,
                bottom = 12.dp,
                start = GLOBAL_PADDING,
                end = GLOBAL_PADDING
              ),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Text(
                text = data.choices.find { it.id == choiceID }!!.name,
                style = getSubsectionStyle(settings.fontSizeScalar),
                modifier = Modifier.weight(1f)
              )
              Box(
                modifier = Modifier
                  .size(20.dp)
                  .clip(MaterialTheme.shapes.extraSmall)
                  .background(
                    MaterialTheme.colorScheme.harmonizeWithPrimary(randomChoiceColors[index % randomChoiceColors.size])
                  )
              )
            }
          }
          items(alters, key = { it.id }) { alter ->
            Spacer(modifier = Modifier.height(12.dp))
            InertAlterCard(
              alter = alter,
              onClick = { selectedAlterForContextSheet = alter.id },
              isFronting = false,
              isPrimary = false,
              frontComment = data.responses.find { it.alterID == alter.id }?.comment,
              imageContext = coroutineScope.coroutineContext,
              placeholderPainter = placeholderPainter,
              modifier = Modifier.padding(horizontal = GLOBAL_PADDING),
              settings = settings
            )
          }
        }
      } else {
        votesToAlters.first {
          it.first == displayedChoiceType
        }.let { (_, alters) ->
          item(key = "V${displayedChoiceType}") {
            Row(
              modifier = Modifier.fillMaxWidth().padding(
                top = 8.dp,
                bottom = 12.dp,
                start = GLOBAL_PADDING,
                end = GLOBAL_PADDING
              ),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = data.choices.find { it.id == displayedChoiceType }!!.name,
                style = getSubsectionStyle(settings.fontSizeScalar),
                modifier = Modifier.weight(1f)
              )
              Box(
                modifier = Modifier
                  .size(20.dp)
                  .clip(MaterialTheme.shapes.extraSmall)
                  .background(
                    MaterialTheme.colorScheme.harmonizeWithPrimary(
                      randomChoiceColors[data.choices.indexOfFirst { it.id == displayedChoiceType } % randomChoiceColors.size]
                    )
                  )
              )
            }
          }
          items(alters, key = { it.id }) { alter ->
            Spacer(modifier = Modifier.height(12.dp))
            InertAlterCard(
              alter = alter,
              onClick = { selectedAlterForContextSheet = alter.id },
              isFronting = false,
              isPrimary = false,
              frontComment = data.responses.find { it.alterID == alter.id }?.comment,
              imageContext = coroutineScope.coroutineContext,
              placeholderPainter = placeholderPainter,
              modifier = Modifier.padding(horizontal = GLOBAL_PADDING),
              settings = settings
            )
          }
        }
      }
    }
  }
  if (castVoteSheetOpen) {
    ChoicePollCastVoteSheet(
      onDismissRequest = {
        setCastVoteSheetOpen(false)
        selectedAlterID = null
      },
      choices = data.choices,
      selectedAlterID = selectedAlterID,
      selectedAlter = selectedAlter,
      launchAddVote = { id, type, comment ->
        model.updateData(
          data.addResponse(
            id,
            type,
            comment
          )
        )

        displayedChoiceType = null
      },
      setSelectAlterDialogOpen = { selectAlterDialogOpen = it },
      placeholderPainter = placeholderPainter,
      coroutineScope = coroutineScope,
      settings = settings
    )
  }
  if (selectAlterDialogOpen) {
    AttachAlterDialog(
      existingAlters = data.responses.map { it.alterID }
          + (if (selectedAlterID != null) listOf(selectedAlterID!!) else emptyList()),
      alters = alters.ensureData,
      onDismissRequest = { selectAlterDialogOpen = false },
      placeholderPainter = placeholderPainter,
      launchAttachAlter = { selectedAlterID = it },
      attachText = Res.string.choose_an_alter.compose,
      noAltersText = Res.string.poll_no_valid_alters.compose,
      settings = settings
    )
  }
  if (selectedAlterForContextSheet != null) {
    PollAlterContextSheet(
      alterID = selectedAlterForContextSheet!!,
      launchEditVoteComment = {
        selectedAlterForEditComment = it
      },
      launchRemoveVote = {
        displayedChoiceType = null
        model.updateData(
          data.removeResponse(it)
        )
      },
      onDismissRequest = { selectedAlterForContextSheet = null }
    )
  }
  if (selectedAlterForEditComment != null) {
    EditVoteCommentDialog(
      alterID = selectedAlterForEditComment!!,
      initialComment = data.responses.find { it.alterID == selectedAlterForEditComment!! }!!.comment,
      onDismissRequest = {
        selectedAlterForEditComment = null
      },
      launchEditVoteComment = { alterID, comment ->
        model.updateData(
          data.editResponseComment(alterID, comment)
        )
      }
    )
  }
}