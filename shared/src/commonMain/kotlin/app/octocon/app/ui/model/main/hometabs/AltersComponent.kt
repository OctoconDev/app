package app.octocon.app.ui.model.main.hometabs

import app.octocon.app.ui.model.MainComponentContext
import app.octocon.app.ui.model.interfaces.SettingsInterface
import app.octocon.app.ui.model.main.hometabs.alters.AlterJournalEntryViewComponent
import app.octocon.app.ui.model.main.hometabs.alters.AlterJournalEntryViewComponentImpl
import app.octocon.app.ui.model.main.hometabs.alters.AlterListComponent
import app.octocon.app.ui.model.main.hometabs.alters.AlterListComponentImpl
import app.octocon.app.ui.model.main.hometabs.alters.AlterViewComponent
import app.octocon.app.ui.model.main.hometabs.alters.AlterViewComponentImpl
import app.octocon.app.ui.model.main.hometabs.alters.TagViewComponent
import app.octocon.app.ui.model.main.hometabs.alters.TagViewComponentImpl
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.router.panels.ChildPanels
import com.arkivanov.decompose.router.panels.ChildPanelsMode
import com.arkivanov.decompose.router.panels.Panels
import com.arkivanov.decompose.router.panels.PanelsNavigation
import com.arkivanov.decompose.router.panels.activateDetails
import com.arkivanov.decompose.router.panels.activateExtra
import com.arkivanov.decompose.router.panels.childPanels
import com.arkivanov.decompose.router.panels.dismissDetails
import com.arkivanov.decompose.router.panels.pop
import com.arkivanov.decompose.router.panels.setMode
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.StackNavigator
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pushToFront
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackHandlerOwner
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer

@OptIn(ExperimentalDecomposeApi::class)
interface AltersComponent : BackHandlerOwner {
  val settings: SettingsInterface
  val panels: Value<ChildPanels<*, AlterListComponent, *, AltersDetailStackComponent, *, ExtraChild>>

  fun replaceWithAlterView(alterID: Int)
  fun replaceWithTagView(tagID: String)
  fun activateAlterJournalEntry(alterID: Int, entryID: String, alterColor: String?)

  fun onBackPressed()

  fun setMode(mode: ChildPanelsMode)

  sealed interface ExtraChild {
    class AlterJournalEntryChild(val component: AlterJournalEntryViewComponent) : ExtraChild
  }
}

@OptIn(ExperimentalDecomposeApi::class)
internal class AltersComponentImpl(
  componentContext: MainComponentContext,
  private val navigateToCustomFieldsFun: () -> Unit
) : AltersComponent, MainComponentContext by componentContext {
  private val navigator = PanelsNavigation<Unit, DetailsConfig, ExtraConfig>()

  private val _panels =
    childPanels(
      source = navigator,
      serializers = Triple(Unit.serializer(), DetailsConfig.serializer(), ExtraConfig.serializer()),
      initialPanels = { Panels(main = Unit) },
      mainFactory = { _, componentContext ->
        AlterListComponentImpl(
          componentContext = componentContext,
          navigateToAlterViewFun = ::replaceWithAlterView,
          navigateToTagViewFun = ::replaceWithTagView
        )
      },
      detailsFactory = { config, componentContext ->
        AltersDetailStackComponentImpl(
          componentContext = componentContext,
          initialStack = config.initialStack,
          popStack = navigator::dismissDetails,
          navigateToAlterJournalEntryFun = { alterID, entryID, alterColor ->
            navigator.activateExtra(ExtraConfig.AlterJournalEntry(alterID, entryID, alterColor))
          },
          navigateToCustomFieldsFun = navigateToCustomFieldsFun
        )
      },
      extraFactory = ::extraChild
    )

  override val panels: Value<ChildPanels<*, AlterListComponent, *, AltersDetailStackComponent, *, AltersComponent.ExtraChild>> = _panels

  override fun replaceWithAlterView(alterID: Int) = navigator.activateDetails(
    DetailsConfig(listOf(
      AltersDetailStackComponentImpl.Config.AlterView(alterID)
    ))
  )
  override fun replaceWithTagView(tagID: String) = navigator.activateDetails(
    DetailsConfig(listOf(
      AltersDetailStackComponentImpl.Config.TagView(tagID)
    ))
  )

  override fun activateAlterJournalEntry(alterID: Int, entryID: String, alterColor: String?) = navigator.activateExtra(
    ExtraConfig.AlterJournalEntry(alterID, entryID, alterColor)
  )

  override fun onBackPressed() = navigator.pop()

  override fun setMode(mode: ChildPanelsMode) = navigator.setMode(mode)

  private fun extraChild(config: ExtraConfig, componentContext: MainComponentContext): AltersComponent.ExtraChild {
    return when (config) {
      is ExtraConfig.AlterJournalEntry -> {
        AltersComponent.ExtraChild.AlterJournalEntryChild(
          AlterJournalEntryViewComponentImpl(
            componentContext = componentContext,
            popSelf = navigator::pop,
            alterID = config.alterID,
            entryID = config.entryID
          )
        )
      }
    }
  }


  @Serializable
  private data class DetailsConfig(val initialStack: List<AltersDetailStackComponentImpl.Config>)

  @Serializable
  private sealed interface ExtraConfig {
    @Serializable
    data class AlterJournalEntry(val alterID: Int, val entryID: String, val alterColor: String?) : ExtraConfig
  }
}

interface AltersDetailStackComponent : BackHandlerOwner {
  val stack: Value<ChildStack<*, Child>>

  val settings: SettingsInterface

  fun navigateToAlterView(alterID: Int)
  fun navigateToTagView(tagID: String)
  fun navigateToAlterJournalEntry(alterID: Int, entryID: String, alterColor: String?)

  fun onBackPressed()

  sealed interface Child {
    class AlterViewChild(val component: AlterViewComponent) : Child
    class TagViewChild(val component: TagViewComponent) : Child
  }
}

internal class AltersDetailStackComponentImpl(
  componentContext: MainComponentContext,
  initialStack: List<Config>,
  private val popStack: () -> Unit,
  private val navigateToAlterJournalEntryFun: (Int, String, String?) -> Unit,
  private val navigateToCustomFieldsFun: () -> Unit
) : AltersDetailStackComponent, MainComponentContext by componentContext {
  private val navigator = StackNavigation<Config>()

  private val _stack =
    childStack(
      source = navigator,
      serializer = Config.serializer(),
      initialStack = { initialStack },
      handleBackButton = true,
      childFactory = ::child,
    )

  override val stack: Value<ChildStack<*, AltersDetailStackComponent.Child>> = _stack

  override fun navigateToAlterView(alterID: Int) = navigator.pushToFront(Config.AlterView(alterID))
  override fun navigateToTagView(tagID: String) = navigator.pushToFront(Config.TagView(tagID))
  override fun navigateToAlterJournalEntry(alterID: Int, entryID: String, alterColor: String?) = navigateToAlterJournalEntryFun(alterID, entryID, alterColor)

  override fun onBackPressed() = navigator.popWithCallbackWhenEmpty(popStack)

  private fun child(config: Config, componentContext: MainComponentContext): AltersDetailStackComponent.Child {
    return when (config) {
      is Config.AlterView -> {
        AltersDetailStackComponent.Child.AlterViewChild(
          AlterViewComponentImpl(
            componentContext = componentContext,
            popSelf = ::onBackPressed,
            navigateToTagViewFun = ::navigateToTagView,
            navigateToAlterJournalEntryViewFun = { entryID, alterColor -> navigateToAlterJournalEntryFun(config.alterID, entryID, alterColor) },
            navigateToCustomFieldsFun = navigateToCustomFieldsFun,
            alterID = config.alterID
          )
        )
      }

      is Config.TagView -> {
        AltersDetailStackComponent.Child.TagViewChild(
          TagViewComponentImpl(
            componentContext = componentContext,
            popSelf = ::onBackPressed,
            navigateToTagViewFun = ::navigateToTagView,
            navigateToAlterViewFun = ::navigateToAlterView,
            tagID = config.tagID
          )
        )
      }
    }
  }

  @Serializable
  sealed interface Config {
    @Serializable
    data class AlterView(val alterID: Int) : Config
    @Serializable
    data class TagView(val tagID: String) : Config
  }
}

inline fun <C : Any> StackNavigator<C>.popWithCallbackWhenEmpty(
  crossinline callback: () -> Unit
) {
  navigate(
    transformer = { stack ->
        return@navigate if (stack.size == 1) {
          callback()
          stack
        } else {
          stack.dropLast(1)
        }
    },
    onComplete = { _, _ -> },
  )
}