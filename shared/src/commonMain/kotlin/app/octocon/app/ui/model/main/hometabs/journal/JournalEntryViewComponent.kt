package app.octocon.app.ui.model.main.hometabs.journal

import app.octocon.app.api.APIState
import app.octocon.app.api.model.APIResponse
import app.octocon.app.api.model.GlobalJournalEntry
import app.octocon.app.ui.model.CommonInterface
import app.octocon.app.ui.model.MainComponentContext
import app.octocon.app.ui.model.interfaces.ApiInterfaceImpl
import app.octocon.app.ui.model.main.SaveState
import app.octocon.app.utils.colorRegex
import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.arkivanov.essenty.instancekeeper.retainedInstance
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import com.arkivanov.essenty.lifecycle.doOnDestroy
import io.ktor.http.HttpMethod
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

sealed class JournalContentState {
  data object Initial : JournalContentState()
  data object Loading : JournalContentState()
  data class Ready(val content: String?) : JournalContentState()

  val ensureContent: String?
    get() = (this as Ready).content

}

interface JournalEntryViewComponent : CommonInterface {
  val entryID: String
  val model: Model

  fun attachAlter(alterID: Int)
  fun detachAlter(alterID: Int)

  fun commit()

  fun navigateBack()

  fun updateShowSnackbar(showSnackbar: (String) -> Unit)

  interface Model {
    val id: String
    val apiEntry: StateFlow<GlobalJournalEntry?>

    val saveState: StateFlow<SaveState>
    val title: StateFlow<String>
    val initialContentState: StateFlow<JournalContentState>
    val contentState: StateFlow<JournalContentState>
    val color: StateFlow<String?>
    val alters: StateFlow<List<Int>>

    val initialEntry: StateFlow<GlobalJournalEntry?>
    val isLoaded: StateFlow<Boolean>
    val showUnencryptedWarning: StateFlow<Boolean>
    val entryHasChanged: StateFlow<Boolean>

    fun updateSaveState(saveState: SaveState)
    fun updateTitle(title: String): Result<String>
    fun updateContent(content: String): Result<String>
    fun updateColor(color: String)

    fun dismissUnencryptedWarning()

    fun revertChanges()
  }
}

class JournalEntryViewComponentImpl(
  componentContext: MainComponentContext,
  private val popSelf: () -> Unit,
  override val entryID: String
) : JournalEntryViewComponent, MainComponentContext by componentContext {
  val coroutineScope = coroutineScope(coroutineContext + SupervisorJob())

  private val apiEntry = api.globalJournals
    .map { journals ->
      if (journals !is APIState.Success) return@map null

      journals.ensureData.find { it.id == entryID }
    }
    .stateIn(coroutineScope, SharingStarted.Eagerly, null)

  private val _model = retainedInstance { ModelImpl(entryID, apiEntry, coroutineScope) }
  override val model: JournalEntryViewComponent.Model = _model

  init {
    coroutineScope.launch {
      apiEntry.collect {
        if(it == null) return@collect

        if(!model.isLoaded.value) {
          if(model.initialContentState.value != JournalContentState.Initial) return@collect
          withContext(Dispatchers.Default) {
            (api as ApiInterfaceImpl).sendAPIRequest<GlobalJournalEntry>(
              HttpMethod.Get,
              "journals/${it.id}"
            ) { isSuccess, result ->
              coroutineScope.launch(Dispatchers.Default) {
                if(isSuccess) {
                  var shouldInject = true
                  var textContent = result.data!!.content

                  if(textContent?.startsWith("enc|") == true) {
                    try {
                      textContent = platformUtilities.decryptData(textContent, settings.data.value)
                    } catch (e: IllegalStateException) {
                      shouldInject = false
                      componentContext.settings.clearEncryptionKey()
                      popSelf()
                    }
                  } else if (!textContent.isNullOrBlank()) {
                    _model.displayUnencryptedWarning()
                  }

                  if(shouldInject) {
                    _model.injectInitialEntry(result.data, textContent)
                  }
                }
              }
            }
          }
        } else {
          _model.reconcile(it)
        }
      }
    }

    lifecycle.doOnDestroy {
      if(model.entryHasChanged.value && model.saveState.value == SaveState.NotSaved) {
        doPatchRequest()
      }
    }
  }

  override fun attachAlter(alterID: Int) {
    if (!model.isLoaded.value) return
    api.attachAlterToGlobalJournalEntry(entryID, alterID)
  }

  override fun detachAlter(alterID: Int) {
    if (!model.isLoaded.value) return
    api.detachAlterFromGlobalJournalEntry(entryID, alterID)
  }

  private var pendingSaveEvent: (() -> Unit)? = null

  private fun tryExit(onSave: () -> Unit) {
    if (model.entryHasChanged.value) {
      pendingSaveEvent = onSave
      commit()
    } else {
      onSave()
    }
  }

  override fun navigateBack() = tryExit(popSelf)

  private var showSnackbar: ((String) -> Unit)? = null

  override fun updateShowSnackbar(showSnackbar: (String) -> Unit) {
    this.showSnackbar = showSnackbar
  }

  override fun commit() {
    if (!model.isLoaded.value || !model.entryHasChanged.value) return
    model.updateSaveState(SaveState.Saving)

    coroutineScope.launch(Dispatchers.Default) {
      doPatchRequest { isSuccess, response ->
        if(isSuccess) {
          pendingSaveEvent?.invoke()
        } else {
          showSnackbar?.invoke(response.error ?: "Failed to save journal entry.")
        }
        model.updateSaveState(
          if (isSuccess) SaveState.Saved
          else SaveState.Error
        )
      }
    }
  }

  private fun doPatchRequest(callback: ((Boolean, APIResponse<JsonElement>) -> Unit)? = null) {
    (api as ApiInterfaceImpl).sendAPIRequest<JsonElement>(
      HttpMethod.Patch,
      "journals/${entryID}",
      _model.buildJsonDiff { platformUtilities.encryptData(it, settings.data.value) },
      callback
    )
  }

  private class ModelImpl(
    entryID: String,
    override val apiEntry: StateFlow<GlobalJournalEntry?>,
    coroutineScope: CoroutineScope
  ) : JournalEntryViewComponent.Model, InstanceKeeper.Instance {
    override val id = entryID

    private val _saveState = MutableStateFlow(SaveState.NotSaved)
    override val saveState: StateFlow<SaveState> = _saveState
    private val _title = MutableStateFlow(apiEntry.value?.title ?: "")
    override val title: StateFlow<String> = _title
    private val _initialContentState = MutableStateFlow<JournalContentState>(JournalContentState.Initial)
    override val initialContentState: StateFlow<JournalContentState> = _initialContentState
    private val _contentState = MutableStateFlow<JournalContentState>(JournalContentState.Initial)
    override val contentState: StateFlow<JournalContentState> = _contentState
    private val _color = MutableStateFlow<String?>(apiEntry.value?.color)
    override val color: StateFlow<String?> = _color
    private val _alters = MutableStateFlow(emptyList<Int>())
    override val alters: StateFlow<List<Int>> = _alters

    private val _initialEntry = MutableStateFlow<GlobalJournalEntry?>(null)
    override val initialEntry = _initialEntry
    private val _isLoaded = MutableStateFlow(false)
    override val isLoaded = _isLoaded

    private val _showUnencryptedWarning = MutableStateFlow(false)
    override val showUnencryptedWarning = _showUnencryptedWarning

    override val entryHasChanged = combine(
      title,
      initialContentState,
      contentState,
      color,
      initialEntry
    ) { title, initialContentState, contentState, color, initialEntry ->
      if (initialContentState !is JournalContentState.Ready || contentState !is JournalContentState.Ready) {
        return@combine false
      }

      title != initialEntry!!.title ||
          contentState.content != initialContentState.content ||
          color != initialEntry.color
    }.stateIn(coroutineScope, SharingStarted.Eagerly, false)

    override fun updateSaveState(saveState: SaveState) {
      _saveState.value = saveState
    }

    override fun updateTitle(title: String): Result<String> {
      if (title.length > 99) return Result.failure(IllegalArgumentException("Title too long"))
      _title.value = title
      return Result.success(title)
    }

    override fun updateContent(content: String): Result<String> {
      if (_initialContentState.value !is JournalContentState.Ready) return Result.failure(IllegalStateException("Initial content not ready"))
      if (content.length > 29_999) return Result.failure(IllegalArgumentException("Content too long"))
      _contentState.value = JournalContentState.Ready(content.ifBlank { null })
      return Result.success(content)
    }

    override fun updateColor(color: String) {
      // Make sure it's a valid hex code (#000000 - #FFFFFF) with regex
      if (!(colorRegex matches color)) return
      _color.value = color
    }

    override fun dismissUnencryptedWarning() {
      _showUnencryptedWarning.value = false
    }

    fun displayUnencryptedWarning() {
      _showUnencryptedWarning.value = true
    }

    fun injectInitialEntry(entry: GlobalJournalEntry, decryptedContent: String?) {
      _initialEntry.value = entry
      _title.value = entry.title
      _initialContentState.value = JournalContentState.Ready(decryptedContent)
      _contentState.value = JournalContentState.Ready(decryptedContent)
      _color.value = entry.color
      _alters.value = entry.alters
      _isLoaded.value = true
    }

    fun reconcile(newEntry: GlobalJournalEntry) {
      if (!isLoaded.value) return
      _alters.value = newEntry.alters
    }

    override fun revertChanges() {
      if (!isLoaded.value) return
      _title.value = initialEntry.value!!.title
      _contentState.value =
        JournalContentState.Ready((_initialContentState.value as JournalContentState.Ready).content)
      _color.value = initialEntry.value!!.color
    }

    fun buildJsonDiff(encryptData: (String) -> String) =
      buildJsonObject {
        if (_title.value != initialEntry.value!!.title)
          put("title", _title.value)

        if ((_contentState.value as JournalContentState.Ready).content != (_initialContentState.value as JournalContentState.Ready).content) {
          if ((_contentState.value as JournalContentState.Ready).content == null) {
            put("content", null)
          } else {
            put(
              "content",
              encryptData((_contentState.value as JournalContentState.Ready).content!!)
            )
          }
        }

        if (_color.value != initialEntry.value!!.color)
          put("color", _color.value)
      }.toString()
    }
}