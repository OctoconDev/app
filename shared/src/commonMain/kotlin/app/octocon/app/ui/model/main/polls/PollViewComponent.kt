package app.octocon.app.ui.model.main.polls

import androidx.compose.runtime.Composable
import app.octocon.app.api.APIState
import app.octocon.app.api.model.APIResponse
import app.octocon.app.api.model.ChoicePoll
import app.octocon.app.api.model.Poll
import app.octocon.app.api.model.PollData
import app.octocon.app.api.model.VotePoll
import app.octocon.app.ui.model.CommonInterface
import app.octocon.app.ui.model.MainComponentContext
import app.octocon.app.ui.model.interfaces.ApiInterfaceImpl
import app.octocon.app.ui.model.main.SaveState
import app.octocon.app.ui.model.main.polls.pollview.PollViewResultsComponent
import app.octocon.app.ui.model.main.polls.pollview.PollViewResultsComponentImpl
import app.octocon.app.ui.model.main.polls.pollview.PollViewSettingsComponent
import app.octocon.app.ui.model.main.polls.pollview.PollViewSettingsComponentImpl
import app.octocon.app.utils.compose
import app.octocon.app.utils.globalSerializer
import com.arkivanov.decompose.router.pages.ChildPages
import com.arkivanov.decompose.router.pages.Pages
import com.arkivanov.decompose.router.pages.PagesNavigation
import com.arkivanov.decompose.router.pages.childPages
import com.arkivanov.decompose.router.pages.select
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.arkivanov.essenty.instancekeeper.retainedInstance
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import com.arkivanov.essenty.lifecycle.doOnDestroy
import io.ktor.http.HttpMethod
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.put
import octoconapp.shared.generated.resources.Res
import octoconapp.shared.generated.resources.results
import octoconapp.shared.generated.resources.settings


interface PollViewComponent : CommonInterface {
  val pages: Value<ChildPages<*, Child>>

  val pollID: String
  val model: Model

  fun commit()

  fun navigateToPage(index: Int)
  fun navigateBack()

  fun updateShowSnackbar(showSnackbar: (String) -> Unit)

  interface Model {
    val apiPoll: StateFlow<Poll?>

    val id: String
    val saveState: StateFlow<SaveState>
    val title: StateFlow<String>
    val description: StateFlow<String?>
    val timeEnd: StateFlow<Instant?>
    val data: StateFlow<PollData?>

    val initialPoll: StateFlow<Poll?>
    val pollHasChanged: StateFlow<Boolean>
    val isLoaded: StateFlow<Boolean>

    fun updateTitle(title: String): Result<String>
    fun updateDescription(description: String?): Result<String>
    fun updateTimeEnd(timeEnd: Instant?)
    fun updateData(data: PollData)
    fun updateSaveState(saveState: SaveState)

    fun revertChanges()
  }

  sealed interface Child {
    interface Metadata {
      val index: Int
      val title: String
        @Composable get
    }

    companion object {
      val Child.metadata: Metadata
        get() = when (this) {
          is ResultsChild -> ResultsChild
          is SettingsChild -> SettingsChild
        }

      val allMetadata: List<Metadata> by lazy {
        listOf(
          ResultsChild,
          SettingsChild
        )
      }
    }

    class ResultsChild(val component: PollViewResultsComponent) : Child {
      companion object : Metadata {
        override val index: Int = 0
        override val title: String
          @Composable get() = Res.string.results.compose
      }
    }
    class SettingsChild(val component: PollViewSettingsComponent) : Child {
      companion object : Metadata {
        override val index: Int = 1
        override val title: String
          @Composable get() = Res.string.settings.compose
      }
    }
  }

}

class PollViewComponentImpl(
  componentContext: MainComponentContext,
  private val popSelf: () -> Unit,
  override val pollID: String
) : PollViewComponent, MainComponentContext by componentContext {
  val coroutineScope = coroutineScope(coroutineContext + SupervisorJob())

  private val apiPoll = api.polls
    .map { state ->
      val data = (state as? APIState.Success)?.data ?: return@map null
      data.find { it.id == pollID }
    }
    .stateIn(coroutineScope, SharingStarted.Eagerly, null)

  private val _model = retainedInstance { ModelImpl(pollID, apiPoll, coroutineScope) }
  override val model: PollViewComponent.Model = _model

  init {
    coroutineScope.launch {
      apiPoll.collect {
        if(it == null) {
          api.reloadPolls(false)
          return@collect
        }

        if(!_model.isLoaded.value) {
          _model.injectInitialPoll(it)
        }
      }
    }

    lifecycle.doOnDestroy {
      if(model.pollHasChanged.value && model.saveState.value == SaveState.NotSaved) {
        doPatchRequest()
      }
    }
  }

  private val navigator = PagesNavigation<Config>()

  private val _pages =
    childPages(
      source = navigator,
      serializer = Config.serializer(),
      initialPages = { Pages(
        listOf(
          Config.Results,
          Config.Settings
        ),
        selectedIndex = 0
      ) },
      handleBackButton = false,
      childFactory = ::child,
    )

  override val pages: Value<ChildPages<*, PollViewComponent.Child>> = _pages

  private fun child(config: Config, componentContext: MainComponentContext): PollViewComponent.Child {
    return when (config) {
      Config.Results ->
        PollViewComponent.Child.ResultsChild(
          PollViewResultsComponentImpl(
            componentContext = componentContext,
            model = model
          )
        )
      Config.Settings ->
        PollViewComponent.Child.SettingsChild(
          PollViewSettingsComponentImpl(
            componentContext = componentContext,
            model = model
          )
        )
    }
  }

  override fun navigateToPage(index: Int) {
    if(!model.isLoaded.value) return
    navigator.select(index)
  }

  private var pendingSaveEvent: (() -> Unit)? = null

  private fun tryExit(onSave: () -> Unit) {
    if (model.pollHasChanged.value) {
      pendingSaveEvent = onSave
      commit()
    } else {
      onSave()
    }
  }

  override fun commit() {
    if (!model.pollHasChanged.value) return

    if (model.title.value.isBlank()) {
      showSnackbar?.invoke("Poll name cannot be empty.")
      return
    }

    model.updateSaveState(SaveState.Saving)
    doPatchRequest { isSuccess, response ->
      if(isSuccess) {
        pendingSaveEvent?.invoke()
      } else {
        showSnackbar?.invoke(response.error ?: "Failed to save poll.")
      }
      _model.updateSaveState(
        if (isSuccess) SaveState.Saved
        else SaveState.Error
      )
    }
  }

  private fun doPatchRequest(callback: ((Boolean, APIResponse<JsonElement>) -> Unit)? = null) {
    // TODO: Find a way to not have to cast this
    (api as ApiInterfaceImpl).sendAPIRequest<JsonElement>(
      HttpMethod.Patch,
      "polls/${pollID}",
      _model.buildJsonDiff(),
      callback
    )
  }

  override fun navigateBack() = tryExit { popSelf() }

  private var showSnackbar: ((String) -> Unit)? = null

  override fun updateShowSnackbar(showSnackbar: (String) -> Unit) {
    this.showSnackbar = showSnackbar
  }

  @Serializable
  private sealed interface Config {
    @Serializable
    data object Results : Config

    @Serializable
    data object Settings : Config
  }


  private class ModelImpl(
    pollID: String,
    override val apiPoll: StateFlow<Poll?>,
    coroutineScope: CoroutineScope
  ) : PollViewComponent.Model, InstanceKeeper.Instance {
    override val id: String = pollID

    private val _saveState = MutableStateFlow(SaveState.NotSaved)
    override val saveState: StateFlow<SaveState> = _saveState

    private val _title = MutableStateFlow("")
    override val title: StateFlow<String> = _title
    private val _description = MutableStateFlow<String?>(null)
    override val description: StateFlow<String?> = _description
    private val _timeEnd = MutableStateFlow<Instant?>(null)
    override val timeEnd: StateFlow<Instant?> = _timeEnd
    private val _data = MutableStateFlow<PollData?>(null)
    override val data = _data

    private val _initialPoll = MutableStateFlow<Poll?>(null)
    override val initialPoll = _initialPoll

    private val _isLoaded = MutableStateFlow(false)
    override val isLoaded: StateFlow<Boolean> = _isLoaded

    override fun updateTitle(title: String): Result<String> {
      if (title.length > 100) return Result.failure(IllegalArgumentException("Title is too long"))
      _title.value = title
      return Result.success(title)
    }

    override fun updateDescription(description: String?): Result<String> {
      if (description != null && description.length > 2000) return Result.failure(
        IllegalArgumentException("Description is too long")
      )
      _description.value = description
      return Result.success(description.orEmpty())
    }

    override fun updateTimeEnd(timeEnd: Instant?) {
      _timeEnd.value = timeEnd
    }

    override fun updateData(data: PollData) {
      _data.value = data
    }

    override fun updateSaveState(saveState: SaveState) {
      _saveState.value = saveState
    }

    fun injectInitialPoll(initialPoll: Poll) {
      _initialPoll.value = initialPoll
      _title.value = initialPoll.title
      _description.value = initialPoll.description
      _timeEnd.value = initialPoll.timeEnd
      _data.value = initialPoll.data
      _isLoaded.value = true
    }

    override val pollHasChanged = combine(
      isLoaded,
      title,
      description,
      timeEnd,
      data
    ) { isLoaded, title, description, timeEnd, data ->
      if(!isLoaded) return@combine false

      title != initialPoll.value!!.title ||
          description != initialPoll.value!!.description ||
          timeEnd != initialPoll.value!!.timeEnd ||
          data != initialPoll.value!!.data
    }.stateIn(coroutineScope, SharingStarted.Eagerly, false)

    override fun revertChanges() {
      initialPoll.value?.let {
        _title.value = it.title
        _description.value = it.description
        _timeEnd.value = it.timeEnd
        _data.value = it.data
      }
    }

    fun buildJsonDiff(): String =
      buildJsonObject {
        if (_title.value != initialPoll.value!!.title)
          put("title", _title.value)

        if (_description.value != initialPoll.value!!.description)
          put("description", _description.value)

        if (_timeEnd.value != initialPoll.value!!.timeEnd)
          put("time_end", _timeEnd.value?.toString())

        if (_data.value != initialPoll.value!!.data) {
          if (initialPoll.value is VotePoll) {
            put("data", globalSerializer.encodeToJsonElement(_data.value as VotePoll.Data))
          } else if (initialPoll.value is ChoicePoll) {
            put("data", globalSerializer.encodeToJsonElement(_data.value as ChoicePoll.Data))
          }
        }
      }.toString()
  }

}