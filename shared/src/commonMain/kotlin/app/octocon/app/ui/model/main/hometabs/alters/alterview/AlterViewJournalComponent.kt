package app.octocon.app.ui.model.main.hometabs.alters.alterview

import app.octocon.app.api.model.AlterJournalEntry
import app.octocon.app.ui.model.CommonInterface
import app.octocon.app.ui.model.MainComponentContext
import app.octocon.app.ui.model.main.hometabs.alters.AlterViewComponent
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

interface AlterViewJournalComponent : CommonInterface {
  val model: AlterViewComponent.Model

  fun navigateToJournalEntryView(entryID: String)

  fun openCreateJournalEntryDialog()
  fun updateCreateJournalEntryDialogFun(openCreateJournalEntryDialog: (Boolean) -> Unit)

  val journals: StateFlow<List<AlterJournalEntry>?>
}

class AlterViewJournalComponentImpl(
  componentContext: MainComponentContext,
  private val navigateToJournalEntryViewFun: (String) -> Unit,
  override val model: AlterViewComponent.Model
) : AlterViewJournalComponent, MainComponentContext by componentContext {
  val coroutineScope = coroutineScope(coroutineContext + SupervisorJob())

  override val journals = api.alterJournals
    .map { entries ->
      val results = entries[model.id] ?: return@map null

      val pinnedEntries = results.filter { it.pinned }
      val unpinnedEntries = results.filter { !it.pinned }

      pinnedEntries + unpinnedEntries
    }
    .stateIn(coroutineScope, SharingStarted.Eagerly, null)

  private var openCreateJournalEntryDialogFun: ((Boolean) -> Unit)? = null

  override fun navigateToJournalEntryView(entryID: String) {
    navigateToJournalEntryViewFun(entryID)
  }

  override fun openCreateJournalEntryDialog() {
    openCreateJournalEntryDialogFun?.invoke(true)
  }

  override fun updateCreateJournalEntryDialogFun(openCreateJournalEntryDialog: (Boolean) -> Unit) {
    openCreateJournalEntryDialogFun = openCreateJournalEntryDialog
  }

  init {
    if(journals.value == null) {
      api.loadAlterJournals(model.id)
    }
  }
}