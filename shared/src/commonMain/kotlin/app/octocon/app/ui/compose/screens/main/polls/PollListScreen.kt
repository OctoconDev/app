package app.octocon.app.ui.compose.screens.main.polls

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import app.octocon.app.api.ChannelMessage
import app.octocon.app.api.model.Poll
import app.octocon.app.ui.compose.LocalNavigationType
import app.octocon.app.ui.compose.NavigationType
import app.octocon.app.ui.compose.components.CreatePollDialog
import app.octocon.app.ui.compose.components.DeletePollDialog
import app.octocon.app.ui.compose.components.PollCard
import app.octocon.app.ui.compose.components.PollContextSheet
import app.octocon.app.ui.compose.components.shared.IndeterminateProgressSpinner
import app.octocon.app.ui.compose.components.shared.OctoScaffold
import app.octocon.app.ui.compose.components.shared.OctoTopBar
import app.octocon.app.ui.compose.components.shared.OpenDrawerNavigationButton
import app.octocon.app.ui.compose.components.shared.TitleTextState
import app.octocon.app.ui.compose.screens.GLOBAL_PADDING
import app.octocon.app.ui.model.main.polls.PollListComponent
import app.octocon.app.utils.compose
import app.octocon.app.utils.derive
import app.octocon.app.utils.state
import com.mikepenz.markdown.compose.LocalMarkdownColors
import com.mikepenz.markdown.m3.markdownColor
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import octoconapp.shared.generated.resources.Res
import octoconapp.shared.generated.resources.no_polls_card_body
import octoconapp.shared.generated.resources.no_polls_card_button
import octoconapp.shared.generated.resources.no_polls_card_title
import octoconapp.shared.generated.resources.polls
import octoconapp.shared.generated.resources.tooltip_polls_desc
import kotlin.time.Duration.Companion.seconds

@Composable
fun PollListScreen(
  component: PollListComponent
) {
  val markdownColors = markdownColor()
  val api = component.api

  val polls by api.polls.collectAsState()
  val alters by api.alters.collectAsState()

  val alterIds by derive {
    if (!alters.isSuccess) return@derive emptyList()
    alters.ensureData.map { it.id }
  }

  val sortedPolls by derive {
    if (!polls.isSuccess) return@derive emptyList()
    val timedPolls = mutableListOf<Poll>()
    val untimedPolls = mutableListOf<Poll>()
    polls.ensureData.forEach {
      if (it.timeEnd == null) {
        untimedPolls.add(it)
      } else {
        timedPolls.add(it)
      }
    }
    timedPolls.sortedByDescending { it.timeEnd } + untimedPolls.sortedByDescending { it.insertedAt }
  }

  var createPollDialogOpen by state(false)
  var currentTime by state(Clock.System.now())
  var selectedPollID by state<String?>(null)
  var pollToDelete by state<String?>(null)

  LaunchedEffect(true) {
    while (true) {
      currentTime = Clock.System.now()
      delay(30.seconds)
    }
  }

  LaunchedEffect(true) {
    if (polls.isSuccess) return@LaunchedEffect

    api.reloadPolls(pushLoadingState = false)
  }

  val latestEvent by api.eventFlow.collectAsState(null)

  LaunchedEffect(latestEvent) {
    when (latestEvent) {
      is ChannelMessage.PollCreated -> {}
        // navigator.push(PollView((latestEvent as ChannelMessage.PollCreated).poll.id))

      else -> Unit
    }
  }

  OctoScaffold(
    topBar = { topAppBarState, scrollBehavior, showSnackbar ->
      OctoTopBar(
        titleTextState = TitleTextState(
          Res.string.polls.compose,
          spotlightText = Res.string.polls.compose to Res.string.tooltip_polls_desc.compose
        ),
        navigation = {
          val navigationType = LocalNavigationType.current

          if(navigationType != NavigationType.DRAWER) {
            OpenDrawerNavigationButton()
          }
        },
        topAppBarState = topAppBarState,
        scrollBehavior = scrollBehavior
      )
    },
    floatingActionButton = {
      FloatingActionButton(
        onClick = { createPollDialogOpen = true },
        content = {
          Icon(
            imageVector = Icons.Rounded.Add,
            contentDescription = null
          )
        }
      )
    }
  ) { _, _ ->
    CompositionLocalProvider(
      LocalMarkdownColors provides markdownColors
    ) {
      LazyColumn(
        modifier = Modifier.fillMaxSize().padding(GLOBAL_PADDING),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        when {
          !polls.isSuccess -> {
            item { IndeterminateProgressSpinner() }
          }

          sortedPolls.isNotEmpty() -> {
            items(sortedPolls, key = { it.id }) { poll ->
              PollCard(
                poll = poll,
                alterIds = alterIds,
                currentTime = currentTime,
                launchViewPoll = { component.navigateToPollView(poll.id) },
                launchOpenPollSheet = { selectedPollID = it },
                modifier = Modifier.animateItem()
              )
            }
          }

          else -> {
            item {
              Card(
                colors = CardDefaults.cardColors(
                  containerColor = MaterialTheme.colorScheme.secondaryContainer
                ),
                elevation = CardDefaults.cardElevation(
                  defaultElevation = 1.0.dp
                ),
                modifier = Modifier.fillMaxWidth().animateItem()
              ) {
                Column(
                  modifier = Modifier.padding(16.dp).fillMaxWidth()
                ) {
                  Text(
                    Res.string.no_polls_card_title.compose,
                    style = MaterialTheme.typography.titleMedium
                  )
                  Spacer(modifier = Modifier.height(8.dp))
                  Text(
                    Res.string.no_polls_card_body.compose,
                    style = MaterialTheme.typography.bodyMedium.merge(
                      lineHeight = 1.5.em
                    )
                  )
                  Spacer(modifier = Modifier.height(12.dp))
                  Button(
                    onClick = { createPollDialogOpen = true },
                    colors = ButtonDefaults.buttonColors(
                      containerColor = MaterialTheme.colorScheme.primary,
                      contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                  ) {
                    Text(Res.string.no_polls_card_button.compose)
                  }
                }
              }
            }
          }
        }
      }
    }

    if (createPollDialogOpen) {
      CreatePollDialog(
        launchCreatePoll = { title, type, timeEnd ->
          api.createPoll(title, type, timeEnd)
        },
        onDismissRequest = { createPollDialogOpen = false }
      )
    }

    if (selectedPollID != null) {
      PollContextSheet(
        onDismissRequest = { selectedPollID = null },
        selectedPollID = selectedPollID!!,
        launchViewPoll = { component.navigateToPollView(it)},
        launchDeletePoll = { pollToDelete = it }
      )
    }

    if (pollToDelete != null) {
      DeletePollDialog(
        poll = polls.ensureData.find { it.id == pollToDelete!! }!!,
        onDismissRequest = { pollToDelete = null },
        launchDeletePoll = { api.deletePoll(it) }
      )
    }
  }
}