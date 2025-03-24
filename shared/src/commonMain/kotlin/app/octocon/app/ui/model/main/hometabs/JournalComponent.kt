package app.octocon.app.ui.model.main.hometabs

import app.octocon.app.ui.model.MainComponentContext
import app.octocon.app.ui.model.interfaces.SettingsInterface
import app.octocon.app.ui.model.main.hometabs.journal.JournalEntryListComponent
import app.octocon.app.ui.model.main.hometabs.journal.JournalEntryListComponentImpl
import app.octocon.app.ui.model.main.hometabs.journal.JournalEntryViewComponent
import app.octocon.app.ui.model.main.hometabs.journal.JournalEntryViewComponentImpl
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.router.panels.ChildPanels
import com.arkivanov.decompose.router.panels.ChildPanelsMode
import com.arkivanov.decompose.router.panels.Panels
import com.arkivanov.decompose.router.panels.PanelsNavigation
import com.arkivanov.decompose.router.panels.activateDetails
import com.arkivanov.decompose.router.panels.childPanels
import com.arkivanov.decompose.router.panels.pop
import com.arkivanov.decompose.router.panels.setMode
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackHandlerOwner
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer

@OptIn(ExperimentalDecomposeApi::class)
interface JournalComponent : BackHandlerOwner {
  val panels: Value<ChildPanels<*, JournalEntryListComponent, *, JournalEntryViewComponent, Nothing, Nothing>>

  val settings: SettingsInterface

  fun navigateToJournalEntryView(entryID: String)

  fun onBackPressed()

  fun setMode(mode: ChildPanelsMode)
}

@OptIn(ExperimentalDecomposeApi::class)
class JournalComponentImpl(
  componentContext: MainComponentContext,
) : JournalComponent, MainComponentContext by componentContext {
  private val navigator = PanelsNavigation<Unit, DetailsConfig, Nothing>()

  private val _panels =
    childPanels(
      source = navigator,
      serializers = Unit.serializer() to DetailsConfig.serializer(),
      initialPanels = { Panels(main = Unit) },
      mainFactory = { _, componentContext ->
        JournalEntryListComponentImpl(
          componentContext = componentContext,
          navigateToJournalEntryViewFun = ::navigateToJournalEntryView
        )
      },
      detailsFactory = { config, componentContext ->
        JournalEntryViewComponentImpl(
          componentContext = componentContext,
          popSelf = navigator::pop,
          entryID = config.entryID
        )
      }
    )

  override val panels: Value<ChildPanels<*, JournalEntryListComponent, *, JournalEntryViewComponent, Nothing, Nothing>> = _panels

  override fun navigateToJournalEntryView(entryID: String) = navigator.activateDetails(
    DetailsConfig(entryID)
  )

  override fun onBackPressed() = navigator.pop()

  override fun setMode(mode: ChildPanelsMode) = navigator.setMode(mode)

  @Serializable
  private data class DetailsConfig(val entryID: String)
}