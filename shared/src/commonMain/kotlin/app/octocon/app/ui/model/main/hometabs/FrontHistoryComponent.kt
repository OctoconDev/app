package app.octocon.app.ui.model.main.hometabs

import app.octocon.app.api.APIState
import kotlinx.datetime.Month
import app.octocon.app.api.model.MyAlter
import app.octocon.app.ui.compose.screens.main.hometabs.FrontHistoryItem
import app.octocon.app.ui.model.MainComponentContext
import app.octocon.app.ui.model.interfaces.SettingsInterface
import app.octocon.app.utils.MonthYearPair
import kotlinx.coroutines.flow.StateFlow

interface FrontHistoryComponent {
  val settings: SettingsInterface

  val alters: StateFlow<APIState<List<MyAlter>>>
  val frontHistory: StateFlow<Map<MonthYearPair, APIState<List<Pair<Triple<Int, Month, Int>, MutableList<FrontHistoryItem>>>>>>

  fun deleteFront(frontID: String)
  fun loadFrontHistory(monthYearPair: MonthYearPair)
}

class FrontHistoryComponentImpl(
  componentContext: MainComponentContext
) : FrontHistoryComponent, MainComponentContext by componentContext {
  override val alters = api.alters
  override val frontHistory = api.frontHistory

  override fun deleteFront(frontID: String) {
    api.deleteFront(frontID)
  }

  override fun loadFrontHistory(monthYearPair: MonthYearPair) {
    api.loadFrontHistory(monthYearPair)
  }
}