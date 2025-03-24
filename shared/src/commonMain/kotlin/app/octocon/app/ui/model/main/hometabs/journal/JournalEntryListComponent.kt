package app.octocon.app.ui.model.main.hometabs.journal

import app.octocon.app.ui.model.CommonInterface
import app.octocon.app.ui.model.MainComponentContext

interface JournalEntryListComponent : CommonInterface {
  fun navigateToJournalEntryView(entryID: String)
}

class JournalEntryListComponentImpl(
  componentContext: MainComponentContext,
  private val navigateToJournalEntryViewFun: (String) -> Unit
) : JournalEntryListComponent, MainComponentContext by componentContext {
  override fun navigateToJournalEntryView(entryID: String) {
    navigateToJournalEntryViewFun(entryID)
  }
}