package app.octocon.app.ui.model.main.polls

import app.octocon.app.ui.model.MainComponentContext
import app.octocon.app.ui.model.interfaces.SettingsInterface
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
interface PollsComponent : BackHandlerOwner {
  val panels: Value<ChildPanels<*, PollListComponent, *, PollViewComponent, Nothing, Nothing>>

  val settings: SettingsInterface
  fun navigateToPollView(pollID: String)

  fun setMode(mode: ChildPanelsMode)

  fun onBackPressed()
}

@OptIn(ExperimentalDecomposeApi::class)
class PollsComponentImpl(
  componentContext: MainComponentContext,
) : PollsComponent, MainComponentContext by componentContext {
  private val navigator = PanelsNavigation<Unit, DetailsConfig, Nothing>()

  private val _panels =
    childPanels(
      source = navigator,
      serializers = Unit.serializer() to DetailsConfig.serializer(),
      initialPanels = { Panels(main = Unit) },
      mainFactory = { _, componentContext ->
        PollListComponentImpl(
          componentContext = componentContext,
          navigateToPollViewFun = ::navigateToPollView
        )
      },
      detailsFactory = { config, componentContext ->
        PollViewComponentImpl(
          componentContext = componentContext,
          popSelf = navigator::pop,
          pollID = config.pollID
        )
      }
    )

  override val panels: Value<ChildPanels<*, PollListComponent, *, PollViewComponent, Nothing, Nothing>> = _panels

  override fun navigateToPollView(pollID: String) = navigator.activateDetails(
    DetailsConfig(pollID = pollID)
  )

  override fun setMode(mode: ChildPanelsMode) = navigator.setMode(mode)

  override fun onBackPressed() = navigator.pop()

  @Serializable
  data class DetailsConfig(val pollID: String)
}