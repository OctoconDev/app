package app.octocon.app.ui.compose.screens.main.hometabs.journal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import app.octocon.app.api.APIState
import app.octocon.app.api.ChannelMessage
import app.octocon.app.api.model.GlobalJournalEntry
import app.octocon.app.api.model.MyAlter
import app.octocon.app.ui.compose.LocalNavigationType
import app.octocon.app.ui.compose.LocalUpdateLazyListState
import app.octocon.app.ui.compose.NavigationType
import app.octocon.app.ui.compose.components.ConfirmUnlockJournalEntryDialog
import app.octocon.app.ui.compose.components.CreateJournalEntryDialog
import app.octocon.app.ui.compose.components.DeleteJournalEntryDialog
import app.octocon.app.ui.compose.components.GlobalJournalEntryCard
import app.octocon.app.ui.compose.components.OctoSearchBar
import app.octocon.app.ui.compose.components.RecoverEncryptionCard
import app.octocon.app.ui.compose.components.ResetEncryptionCard
import app.octocon.app.ui.compose.components.SetupEncryptionCard
import app.octocon.app.ui.compose.components.shared.BottomSheetListItem
import app.octocon.app.ui.compose.components.shared.IndeterminateProgressSpinner
import app.octocon.app.ui.compose.components.shared.OctoBottomSheet
import app.octocon.app.ui.compose.components.shared.OctoScaffold
import app.octocon.app.ui.compose.components.shared.OctoTopBar
import app.octocon.app.ui.compose.components.shared.OpenDrawerNavigationButton
import app.octocon.app.ui.compose.components.shared.PermanentTipsNote
import app.octocon.app.ui.compose.components.shared.TitleTextState
import app.octocon.app.ui.compose.screens.GLOBAL_PADDING
import app.octocon.app.ui.model.main.hometabs.journal.JournalEntryListComponent
import app.octocon.app.utils.DevicePlatform
import app.octocon.app.utils.compose
import app.octocon.app.utils.derive
import app.octocon.app.utils.fuse.Fuse
import app.octocon.app.utils.ioDispatcher
import app.octocon.app.utils.savedState
import app.octocon.app.utils.sortBySimilarity
import app.octocon.app.utils.state
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import octoconapp.shared.generated.resources.Res
import octoconapp.shared.generated.resources.delete_journal_entry
import octoconapp.shared.generated.resources.edit_journal_entry
import octoconapp.shared.generated.resources.journal
import octoconapp.shared.generated.resources.lock_journal_entry
import octoconapp.shared.generated.resources.permanent_tip_global_journals
import octoconapp.shared.generated.resources.pin_journal_entry
import octoconapp.shared.generated.resources.search_journal_entries
import octoconapp.shared.generated.resources.tooltip_journal_desc
import octoconapp.shared.generated.resources.unlock_journal_entry
import octoconapp.shared.generated.resources.unpin_journal_entry
import kotlin.coroutines.CoroutineContext

@Composable
fun JournalEntryListScreen(
  component: JournalEntryListComponent
) {
  val api = component.api

  val settings by component.settings.collectAsState()

  val encryptionIsInitializing by api.encryptionIsInitializing.collectAsState()

  val system by api.systemMe.collectAsState()
  val journalEntries by api.globalJournals.collectAsState()
  val alters by api.alters.collectAsState()

  val ready by derive {
    journalEntries.isSuccess && alters.isSuccess && system.isSuccess && !encryptionIsInitializing
  }

  val sortedJournalEntries by derive {
    if (!journalEntries.isSuccess) return@derive emptyList()

    val pinnedEntries = journalEntries.ensureData.filter { it.pinned }
    val unpinnedEntries = journalEntries.ensureData.filter { !it.pinned }

    pinnedEntries + unpinnedEntries
  }

  var createJournalEntryDialogOpen by state(false)

  var selectedJournalEntry by savedState<GlobalJournalEntry?>(null)
  var journalEntryToDelete by savedState<GlobalJournalEntry?>(null)
  var journalEntryToUnlock by savedState<GlobalJournalEntry?>(null)

  val latestEvent by api.eventFlow.collectAsState(null)

  LaunchedEffect(true) {
    if (journalEntries.isSuccess) return@LaunchedEffect

    api.reloadGlobalJournals(pushLoadingState = false)
  }

  val lazyListState = rememberLazyListState()

  val updateLazyListState = LocalUpdateLazyListState.current

  LaunchedEffect(true) {
    updateLazyListState(lazyListState)
  }

  LaunchedEffect(latestEvent) {
    if (latestEvent !is ChannelMessage.GlobalJournalEntryCreated) return@LaunchedEffect
    component.navigateToJournalEntryView((latestEvent as ChannelMessage.GlobalJournalEntryCreated).entry.id)
  }

  val imageScope = rememberCoroutineScope()
  val placeholderPainter = rememberVectorPainter(Icons.Rounded.Person)

  OctoScaffold(
    hasHoistedBottomBar = true,
    topBar = { topAppBarState, scrollBehavior, showSnackbar ->
      OctoTopBar(
        titleTextState = TitleTextState(
          title = Res.string.journal.compose,
          spotlightText = Res.string.journal.compose to Res.string.tooltip_journal_desc.compose
        ),
        navigation = {
          val navigationType = LocalNavigationType.current

          if(navigationType == NavigationType.BOTTOM_BAR) {
            OpenDrawerNavigationButton()
          }
        },
        topAppBarState = topAppBarState,
        scrollBehavior = scrollBehavior
      )
    },
    floatingActionButton = {
      if (!ready || settings.encryptedEncryptionKey == null) return@OctoScaffold
      Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.End
      )
      {
        FloatingActionButton(
          onClick = {
            createJournalEntryDialogOpen = true
          },
          content = {
            Icon(
              imageVector = Icons.Rounded.Edit,
              contentDescription = null
            )
          }
        )
      }
    }
  ) { _, _ ->
    when (journalEntries) {
      is APIState.Loading, is APIState.Error -> {
        IndeterminateProgressSpinner()
      }

      is APIState.Success -> {
        if (!ready) {
          IndeterminateProgressSpinner()
          return@OctoScaffold
        }

        if (settings.encryptedEncryptionKey == null) {
          if (system.ensureData.encryptionInitialized) {
            LazyColumn(
              contentPadding = PaddingValues(GLOBAL_PADDING),
              verticalArrangement = Arrangement.spacedBy(12.dp),
              modifier = Modifier.fillMaxSize()
            ) {
              item {
                RecoverEncryptionCard(
                  api = api,
                  settings = component.settings,
                  modifier = Modifier.fillMaxWidth()
                )
              }
              item {
                ResetEncryptionCard(api = api, modifier = Modifier.fillMaxWidth())
              }
            }
          } else {
            Box(modifier = Modifier.fillMaxSize()) {
              SetupEncryptionCard(
                api = api,
                settings = component.settings,
                modifier = Modifier.padding(GLOBAL_PADDING)
              )
            }
          }
          return@OctoScaffold
        }

        LazyJournalEntryList(
          component = component,
          journalEntries = sortedJournalEntries,
          alters = alters.ensureData,
          imageContext = imageScope.coroutineContext + ioDispatcher,
          placeholderPainter = placeholderPainter,
          launchCreateJournalEntry = { createJournalEntryDialogOpen = true },
          launchEditJournalEntry = {
            if (it.locked) {
              journalEntryToUnlock = it
            } else {
              component.navigateToJournalEntryView(it.id)
            }
          },
          lazyListState = lazyListState,
          launchOpenJournalEntrySheet = { selectedJournalEntry = it }
        )
      }
    }
    selectedJournalEntry?.let {
      JournalEntryContextSheet(
        entry = it,
        onDismissRequest = { selectedJournalEntry = null },
        launchEditJournalEntry = {
          if (it.locked) {
            journalEntryToUnlock = it
          } else {
            component.navigateToJournalEntryView(it.id)
          }
        },
        launchLockJournalEntry = { api.lockGlobalJournalEntry(it.id) },
        launchUnlockJournalEntry = { api.unlockGlobalJournalEntry(it.id) },
        launchPinJournalEntry = { api.pinGlobalJournalEntry(it.id) },
        launchUnpinJournalEntry = { api.unpinGlobalJournalEntry(it.id) },
        launchDeleteJournalEntry = { journalEntryToDelete = it }
      )
    }

    journalEntryToDelete?.let {
      DeleteJournalEntryDialog(
        journalEntry = it,
        onDismissRequest = { journalEntryToDelete = null },
        launchDeleteJournalEntry = { entry ->
          api.deleteGlobalJournalEntry(entry.id)
        }
      )
    }

    journalEntryToUnlock?.let {
      ConfirmUnlockJournalEntryDialog(
        journalEntry = it,
        onDismissRequest = { journalEntryToUnlock = null },
        launchViewJournalEntry = { entry ->
          component.navigateToJournalEntryView(entry.id)
        }
      )
    }

    if (createJournalEntryDialogOpen) {
      CreateJournalEntryDialog(
        onDismissRequest = { createJournalEntryDialogOpen = false },
        launchCreateJournalEntry = {
          api.createGlobalJournalEntry(it)
        }
      )
    }
  }

}

@Composable
private fun LazyJournalEntryList(
  component: JournalEntryListComponent,
  journalEntries: List<GlobalJournalEntry>,
  alters: List<MyAlter>,
  imageContext: CoroutineContext,
  placeholderPainter: Painter,
  lazyListState: LazyListState,
  launchCreateJournalEntry: () -> Unit,
  launchEditJournalEntry: (GlobalJournalEntry) -> Unit,
  launchOpenJournalEntrySheet: (GlobalJournalEntry) -> Unit,
) {
  val searchBarVisible = !DevicePlatform.isWasm && journalEntries.size > 5
  val settings by component.settings.collectAsState()

  val showPermanentTips by derive { settings.showPermanentTips }

  var firstItemNotVisible by state(false)

  val fuse = remember { Fuse() }
  var isSearching by state(false)
  var searchQuery by state("")
  var searchResults by state(journalEntries)

  LaunchedEffect(searchQuery, journalEntries) {
    var setSearchingJob: Job? = null
    val result = if (searchQuery.isBlank()) {
      journalEntries
    } else {
      // Set isSearching if search takes more than 100ms
      setSearchingJob = launch {
        delay(100)
        isSearching = true
      }
      journalEntries.sortBySimilarity({ it.title }, searchQuery, fuse = fuse)
    }

    setSearchingJob?.cancel()
    searchResults = result
    isSearching = false
  }

  LaunchedEffect(lazyListState) {
    snapshotFlow { lazyListState.firstVisibleItemIndex }
      .distinctUntilChanged()
      .collect {
        firstItemNotVisible = it > 0
      }
  }

  Box(
    modifier = Modifier.fillMaxSize()
  ) {
    LazyColumn(
      state = lazyListState,
      modifier = Modifier.fillMaxSize().apply {
        /*if (nestedScrollConnection != null) {
          nestedScroll(nestedScrollConnection)
        }*/
      },
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      item {
        Spacer(modifier = Modifier.size(4.dp))
      }

      if (searchBarVisible) {
        item {
          OctoSearchBar(
            searchQuery = searchQuery,
            setSearchQuery = { searchQuery = it },
            isSearching = isSearching,
            placeholderText = Res.string.search_journal_entries.compose,
            modifier = Modifier.padding(horizontal = GLOBAL_PADDING).fillMaxWidth()
          )
        }
      }

      if (showPermanentTips) {
        item {
          PermanentTipsNote(
            text = Res.string.permanent_tip_global_journals.compose,
            modifier = Modifier.padding(horizontal = GLOBAL_PADDING)
          )
        }
      }

      if (journalEntries.isEmpty()) {
        item {
          Card(
            colors = CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.secondaryContainer
            ),
            elevation = CardDefaults.cardElevation(
              defaultElevation = 1.0.dp
            ),
            modifier = Modifier.padding(horizontal = GLOBAL_PADDING)
          ) {
            Column(
              modifier = Modifier.padding(16.dp).fillMaxWidth()
            ) {
              Text(
                "You haven't written any journal entries yet!",
                style = MaterialTheme.typography.titleMedium
              )
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                "Write one now to keep track of your day:",
                style = MaterialTheme.typography.bodyMedium.merge(
                  lineHeight = 1.5.em
                )
              )
              Spacer(modifier = Modifier.height(12.dp))
              Button(
                onClick = launchCreateJournalEntry,
                colors = ButtonDefaults.buttonColors(
                  containerColor = MaterialTheme.colorScheme.primary,
                  contentColor = MaterialTheme.colorScheme.onPrimary
                )
              ) {
                Text("Create journal entry")
              }
            }
          }
        }
      }

      items(searchResults, key = { it.id }) {
        GlobalJournalEntryCard(
          journalEntry = it,
          alters = alters,
          imageContext = imageContext,
          placeholderPainter = placeholderPainter,
          launchViewJournalEntry = launchEditJournalEntry,
          launchOpenJournalEntrySheet = launchOpenJournalEntrySheet,
          modifier = Modifier.padding(horizontal = GLOBAL_PADDING),
          settings = settings
        )
      }

      item {
        Spacer(modifier = Modifier.size(4.dp))
      }
    }
  }
}

@Composable
private fun JournalEntryContextSheet(
  entry: GlobalJournalEntry,
  onDismissRequest: () -> Unit,
  launchEditJournalEntry: () -> Unit,
  launchLockJournalEntry: () -> Unit,
  launchUnlockJournalEntry: () -> Unit,
  launchPinJournalEntry: () -> Unit,
  launchUnpinJournalEntry: () -> Unit,
  launchDeleteJournalEntry: () -> Unit,
) {
  OctoBottomSheet(
    onDismissRequest = onDismissRequest,
  ) {
    BottomSheetListItem(
      imageVector = Icons.Rounded.Edit,
      title = Res.string.edit_journal_entry.compose
    ) {
      launchEditJournalEntry()
      onDismissRequest()
    }

    if (entry.locked) {
      BottomSheetListItem(
        imageVector = Icons.Rounded.LockOpen,
        title = Res.string.unlock_journal_entry.compose
      ) {
        launchUnlockJournalEntry()
        onDismissRequest()
      }
    } else {
      BottomSheetListItem(
        imageVector = Icons.Rounded.Lock,
        title = Res.string.lock_journal_entry.compose
      ) {
        launchLockJournalEntry()
        onDismissRequest()
      }
    }

    if (entry.pinned) {
      BottomSheetListItem(
        imageVector = Icons.Rounded.PushPin,
        title = Res.string.unpin_journal_entry.compose
      ) {
        launchUnpinJournalEntry()
        onDismissRequest()
      }
    } else {
      BottomSheetListItem(
        imageVector = Icons.Rounded.PushPin,
        title = Res.string.pin_journal_entry.compose
      ) {
        launchPinJournalEntry()
        onDismissRequest()
      }
    }

    BottomSheetListItem(
      imageVector = Icons.Rounded.Delete,
      title = Res.string.delete_journal_entry.compose
    ) {
      launchDeleteJournalEntry()
      onDismissRequest()
    }
  }
}