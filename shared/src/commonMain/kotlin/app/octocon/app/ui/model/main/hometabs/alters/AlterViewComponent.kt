package app.octocon.app.ui.model.main.hometabs.alters

import androidx.compose.runtime.Composable
import app.octocon.app.api.APIState
import app.octocon.app.api.model.APIResponse
import app.octocon.app.api.model.MyAlter
import app.octocon.app.api.model.SecurityLevel
import app.octocon.app.api.model.UnmarkedAlterCustomField
import app.octocon.app.ui.model.MainComponentContext
import app.octocon.app.ui.model.interfaces.ApiInterfaceImpl
import app.octocon.app.ui.model.interfaces.SettingsInterface
import app.octocon.app.ui.model.main.SaveState
import app.octocon.app.ui.model.main.hometabs.alters.alterview.AlterViewBasicInfoComponent
import app.octocon.app.ui.model.main.hometabs.alters.alterview.AlterViewBasicInfoComponentImpl
import app.octocon.app.ui.model.main.hometabs.alters.alterview.AlterViewFieldsComponent
import app.octocon.app.ui.model.main.hometabs.alters.alterview.AlterViewFieldsComponentImpl
import app.octocon.app.ui.model.main.hometabs.alters.alterview.AlterViewJournalComponent
import app.octocon.app.ui.model.main.hometabs.alters.alterview.AlterViewJournalComponentImpl
import app.octocon.app.utils.colorRegex
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
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import octoconapp.shared.generated.resources.Res
import octoconapp.shared.generated.resources.basic_info
import octoconapp.shared.generated.resources.custom_fields
import octoconapp.shared.generated.resources.fields
import octoconapp.shared.generated.resources.journal
import octoconapp.shared.generated.resources.tooltip_alter_journal_desc
import octoconapp.shared.generated.resources.tooltip_alter_journal_title
import octoconapp.shared.generated.resources.tooltip_basic_info_desc
import octoconapp.shared.generated.resources.tooltip_custom_fields_desc

interface AlterViewComponent {
  val settings: SettingsInterface

  val pages: Value<ChildPages<*, Child>>

  val alterID: Int

  val initialName: String?
  val initialColor: String?
  val model: Model
  fun commit()

  fun deleteAlter()

  fun navigateToPage(index: Int)
  fun navigateBack()

  fun updateShowSnackbar(showSnackbar: (String) -> Unit)

  interface Model {
    val apiAlter: StateFlow<APIState<MyAlter>?>

    val id: Int
    val saveState: StateFlow<SaveState>
    val initialAlter: StateFlow<MyAlter?>
    val name: StateFlow<String?>
    val pronouns: StateFlow<String?>
    val description: StateFlow<String?>
    val color: StateFlow<String?>
    val securityLevel: StateFlow<SecurityLevel>
    val fields: StateFlow<List<UnmarkedAlterCustomField>>
    val alias: StateFlow<String?>
    val proxyName: StateFlow<String?>
    val untracked: StateFlow<Boolean>
    val archived: StateFlow<Boolean>
    val pinned: StateFlow<Boolean>

    val alterHasChanged: StateFlow<Boolean>
    val isLoaded: StateFlow<Boolean>

    fun updateSaveState(saveState: SaveState)
    fun updateName(name: String): Result<String>
    fun updatePronouns(pronouns: String): Result<String>
    fun updateDescription(description: String): Result<String>
    fun updateColor(color: String) : Result<String>
    fun updateSecurityLevel(securityLevel: SecurityLevel)
    fun updateFieldValue(fieldID: String, value: String?)
    fun updateAlias(alias: String): Result<String>
    fun updateProxyName(proxyName: String): Result<String>
    fun updateUntracked(untracked: Boolean)
    fun updateArchived(archived: Boolean)
    fun updatePinned(pinned: Boolean)

    fun revertChanges()
  }

  sealed interface Child {
    interface Metadata {
      val index: Int
      val title: String
        @Composable get
      val spotlightTitle: String
        @Composable get
      val spotlightDescription: String
        @Composable get
    }

    companion object {
      val Child.metadata: Metadata
        get() = when (this) {
          is BasicInfoChild -> BasicInfoChild
          is FieldsChild -> FieldsChild
          is JournalChild -> JournalChild
        }

      val allMetadata: List<Metadata> by lazy {
        listOf(
          BasicInfoChild,
          FieldsChild,
          JournalChild
        )
      }
    }

    class BasicInfoChild(val component: AlterViewBasicInfoComponent) : Child {
      companion object : Metadata {
        override val index: Int = 0
        override val title: String
          @Composable get() = Res.string.basic_info.compose
        override val spotlightTitle: String
          @Composable get() = Res.string.basic_info.compose
        override val spotlightDescription: String
          @Composable get() = Res.string.tooltip_basic_info_desc.compose
      }
    }
    class FieldsChild(val component: AlterViewFieldsComponent) : Child {
      companion object : Metadata {
        override val index: Int = 1
        override val title: String
          @Composable get() = Res.string.fields.compose
        override val spotlightTitle: String
          @Composable get() = Res.string.custom_fields.compose
        override val spotlightDescription: String
          @Composable get() = Res.string.tooltip_custom_fields_desc.compose
      }
    }
    class JournalChild(val component: AlterViewJournalComponent) : Child {
      companion object : Metadata {
        override val index: Int = 2
        override val title: String
          @Composable get() = Res.string.journal.compose
        override val spotlightTitle: String
          @Composable get() = Res.string.tooltip_alter_journal_title.compose
        override val spotlightDescription: String
          @Composable get() = Res.string.tooltip_alter_journal_desc.compose
      }
    }
  }
}

class AlterViewComponentImpl(
  componentContext: MainComponentContext,
  private val popSelf: () -> Unit,
  private val navigateToTagViewFun: (String) -> Unit,
  private val navigateToAlterJournalEntryViewFun: (String, String?) -> Unit,
  private val navigateToCustomFieldsFun: () -> Unit,
  override val alterID: Int
) : AlterViewComponent, MainComponentContext by componentContext {
  private val coroutineScope = coroutineScope(coroutineContext + SupervisorJob())

  private val apiAlter = api.loadedAlters
    .map { it[alterID] }
    .stateIn(coroutineScope, SharingStarted.Eagerly, null)

  private val _model = retainedInstance {
    val unloadedInitialAlter = (api.alters.value as? APIState.Success)?.let { alters ->
      alters.ensureData.find { it.id == alterID }
    }
    ModelImpl(
      alterID,
      unloadedInitialAlter,
      apiAlter,
      coroutineScope
    )
  }
  override val model: AlterViewComponent.Model = _model

  override var initialName: String? = ""
  override var initialColor: String? = ""

  private val navigator = PagesNavigation<Config>()

  private val _pages =
    childPages(
      source = navigator,
      serializer = Config.serializer(),
      initialPages = { Pages(
        listOf(
          Config.BasicInfo,
          Config.Fields,
          Config.Journal
        ),
        selectedIndex = 0
      ) },
      handleBackButton = false,
      childFactory = ::child,
    )

  override val pages: Value<ChildPages<*, AlterViewComponent.Child>> = _pages

  private fun child(config: Config, componentContext: MainComponentContext): AlterViewComponent.Child {
    return when (config) {
      Config.BasicInfo ->
        AlterViewComponent.Child.BasicInfoChild(
          AlterViewBasicInfoComponentImpl(
            componentContext = componentContext,
            navigateToTagViewFun = ::navigateToTagView,
            model = model
          )
        )

      Config.Fields ->
        AlterViewComponent.Child.FieldsChild(
          AlterViewFieldsComponentImpl(
            componentContext = componentContext,
            model = model,
            navigateToCustomFieldsFun = ::navigateToCustomFields
          )
        )

      Config.Journal ->
        AlterViewComponent.Child.JournalChild(
          AlterViewJournalComponentImpl(
            componentContext = componentContext,
            navigateToJournalEntryViewFun = ::navigateToAlterJournalEntryView,
            model = model
          )
        )
    }
  }

  private var showSnackbar: ((String) -> Unit)? = null

  override fun updateShowSnackbar(showSnackbar: (String) -> Unit) {
    this.showSnackbar = showSnackbar
  }

  init {
    with(api.alters.value) {
      if(isSuccess) {
        val data = this.ensureData
        val alter = data.find { it.id == alterID }
        initialName = alter?.name
        initialColor = alter?.color
      }
    }

    val alter = api.loadedAlters.value[alterID]
    if(alter == null) {
      coroutineScope.launch {
        apiAlter.collect {
          if(it != null && it.isSuccess) {
            _model.injectInitialAlter(it.ensureData)
          }
        }
      }
      
      api.loadAlter(alterID)
    } else {
      if(alter.isSuccess) _model.injectInitialAlter(alter.ensureData)
    }

    lifecycle.doOnDestroy {
      if(model.alterHasChanged.value && model.saveState.value == SaveState.NotSaved) {
        doPatchRequest()
      }
    }
  }

  override fun deleteAlter() {
    api.deleteAlter(alterID)
  }

  override fun navigateToPage(index: Int) {
    if(!model.isLoaded.value) return
    navigator.select(index)
  }

  private var pendingSaveEvent: (() -> Unit)? = null

  private fun tryExit(onSave: () -> Unit) {
    if (model.alterHasChanged.value) {
      pendingSaveEvent = onSave
      commit()
    } else {
      onSave()
    }
  }

  private fun navigateToTagView(tagID: String) =
    tryExit { navigateToTagViewFun(tagID) }

  private fun navigateToAlterJournalEntryView(entryID: String) =
    tryExit { navigateToAlterJournalEntryViewFun(entryID, model.color.value) }

  private fun navigateToCustomFields() =
    tryExit { navigateToCustomFieldsFun() }

  override fun navigateBack() = tryExit(popSelf)

  override fun commit() {
    if (!model.alterHasChanged.value || model.saveState.value == SaveState.Saving) return
    model.updateSaveState(SaveState.Saving)
    doPatchRequest { isSuccess, response ->
      if (isSuccess) {
        pendingSaveEvent?.invoke()
      } else {
        showSnackbar?.invoke(response.error ?: "Failed to save alter.")
      }
      model.updateSaveState(
        if (isSuccess) SaveState.Saved
        else SaveState.Error
      )
    }
  }

  private fun doPatchRequest(callback: ((Boolean, APIResponse<JsonElement>) -> Unit)? = null) {
    // TODO: Find a way to not have to cast this
    (api as ApiInterfaceImpl).sendAPIRequest<JsonElement>(
      HttpMethod.Patch,
      "systems/me/alters/${alterID}",
      globalSerializer.encodeToString<MyAlter>(
        apiAlter.value!!.ensureData.copy(
          avatarUrl = null,
          insertedAt = null,
          updatedAt = null,
          name = model.name.value,
          pronouns = model.pronouns.value,
          description = model.description.value,
          color = model.color.value,
          securityLevel = model.securityLevel.value,
          fields = model.fields.value,
          alias = model.alias.value,
          proxyName = model.proxyName.value,
          untracked = model.untracked.value,
          archived = model.archived.value,
          pinned = model.pinned.value
        )
      ),
      callback
    )
  }

  @Serializable
  private sealed interface Config {
    @Serializable
    data object BasicInfo : Config

    @Serializable
    data object Fields : Config

    @Serializable
    data object Journal : Config
  }

  private class ModelImpl(
    alterID: Int,
    unloadedInitialAlter: MyAlter?,
    override val apiAlter: StateFlow<APIState<MyAlter>?>,
    coroutineScope: CoroutineScope
  ) : AlterViewComponent.Model, InstanceKeeper.Instance {
    override val id: Int = alterID

    private val _saveState = MutableStateFlow(SaveState.NotSaved)
    override val saveState = _saveState

    private val _initialAlter = MutableStateFlow<MyAlter?>(null)
    override val initialAlter = _initialAlter

    private val _name = MutableStateFlow(unloadedInitialAlter?.name)
    override val name: StateFlow<String?> = _name
    private val _pronouns = MutableStateFlow(unloadedInitialAlter?.pronouns)
    override val pronouns: StateFlow<String?> = _pronouns
    private val _description = MutableStateFlow<String?>(null)
    override val description: StateFlow<String?> = _description
    private val _color = MutableStateFlow(unloadedInitialAlter?.color)
    override val color: StateFlow<String?> = _color
    private val _securityLevel = MutableStateFlow(SecurityLevel.PRIVATE)
    override val securityLevel: StateFlow<SecurityLevel> = _securityLevel
    private val _fields = MutableStateFlow(emptyList<UnmarkedAlterCustomField>())
    override val fields: StateFlow<List<UnmarkedAlterCustomField>> = _fields

    private val _alias = MutableStateFlow<String?>(null)
    override val alias: StateFlow<String?> = _alias
    private val _proxyName = MutableStateFlow<String?>(null)
    override val proxyName: StateFlow<String?> = _proxyName

    private val _untracked = MutableStateFlow(false)
    override val untracked = _untracked
    private val _archived = MutableStateFlow(false)
    override val archived = _archived
    private val _pinned = MutableStateFlow(false)
    override val pinned = _pinned

    private val _isLoaded = MutableStateFlow(false)
    override val isLoaded = _isLoaded

    fun injectInitialAlter(initialAlter: MyAlter) {
      _initialAlter.value = initialAlter

      _name.value = initialAlter.name
      _pronouns.value = initialAlter.pronouns
      _description.value = initialAlter.description
      _color.value = initialAlter.color
      _securityLevel.value = initialAlter.securityLevel
      _fields.value = initialAlter.fields
      _alias.value = initialAlter.alias
      _proxyName.value = initialAlter.proxyName
      _untracked.value = initialAlter.untracked
      _archived.value = initialAlter.archived
      _pinned.value = initialAlter.pinned

      _isLoaded.value = true
    }

    override val alterHasChanged = combine(
      isLoaded,
      name,
      pronouns,
      description,
      color,
      securityLevel,
      fields,
      alias,
      proxyName,
      untracked,
      archived,
      pinned
    ) { results ->
      val isLoaded = results[0] as Boolean
      if(!isLoaded) { return@combine false }

      val name = results[1] as String?
      val pronouns = results[2] as String?
      val description = results[3] as String?
      val color = results[4] as String?
      val securityLevel = results[5] as SecurityLevel
      @Suppress("UNCHECKED_CAST") val fields = results[6] as List<UnmarkedAlterCustomField>
      val alias = results[7] as String?
      val proxyName = results[8] as String?
      val untracked = results[9] as Boolean
      val archived = results[10] as Boolean
      val pinned = results[11] as Boolean

      return@combine (
        name != initialAlter.value!!.name ||
        pronouns != initialAlter.value!!.pronouns ||
        description != initialAlter.value!!.description ||
        color != initialAlter.value!!.color ||
        securityLevel != initialAlter.value!!.securityLevel ||
        fields != initialAlter.value!!.fields ||
        alias != initialAlter.value!!.alias ||
        proxyName != initialAlter.value!!.proxyName ||
        untracked != initialAlter.value!!.untracked ||
        archived != initialAlter.value!!.archived ||
        pinned != initialAlter.value!!.pinned
      )
    }.stateIn(coroutineScope, SharingStarted.Eagerly, false)

    override fun updateSaveState(saveState: SaveState) {
      _saveState.value = saveState
    }

    override fun updateName(name: String): Result<String> {
      if (name.length > 80) return Result.failure(IllegalArgumentException("Name too long"))
      _name.value = name.ifBlank { null }
      return Result.success(name)
    }

    override fun updatePronouns(pronouns: String): Result<String> {
      if (pronouns.length > 50) return Result.failure(IllegalArgumentException("Pronouns too long"))
      _pronouns.value = pronouns.ifBlank { null }
      return Result.success(pronouns)
    }

    override fun updateDescription(description: String): Result<String> {
      if (description.length > 3000) return Result.failure(IllegalArgumentException("Description too long"))
      _description.value = description.ifBlank { null }
      return Result.success(description)
    }

    override fun updateColor(color: String): Result<String> {
      // Make sure it's a valid hex code (#000000 - #FFFFFF) with regex
      if (!(colorRegex matches color)) return Result.failure(IllegalArgumentException("Invalid color"))
      _color.value = color
      return Result.success(color)
    }

    override fun updateSecurityLevel(securityLevel: SecurityLevel) {
      _securityLevel.value = securityLevel
    }

    override fun updateFieldValue(fieldID: String, value: String?) {
      if (value == null) {
        _fields.value = _fields.value.filter { it.id != fieldID }
      } else {
        _fields.value =
          _fields.value.find { it.id == fieldID }?.let {
            val index = _fields.value.indexOf(it)
            _fields.value.toMutableList().apply {
              set(index, it.copy(value = value))
            }
          } ?: (_fields.value + UnmarkedAlterCustomField(
            id = fieldID,
            value = value
          ))
      }
    }

    override fun updateAlias(alias: String): Result<String> {
      if (alias.length > 80) return Result.failure(IllegalArgumentException("Alias too long"))
      _alias.value = alias.ifBlank { null }
      return Result.success(alias)
    }

    override fun updateProxyName(proxyName: String): Result<String> {
      if (proxyName.length > 80) return Result.failure(IllegalArgumentException("Proxy name too long"))
      _proxyName.value = proxyName.ifBlank { null }
      return Result.success(proxyName)
    }

    override fun updateUntracked(untracked: Boolean) {
      _untracked.value = untracked
    }

    override fun updateArchived(archived: Boolean) {
      _archived.value = archived
    }

    override fun updatePinned(pinned: Boolean) {
      _pinned.value = pinned
    }

    override fun revertChanges() {
      _name.value = initialAlter.value!!.name
      _pronouns.value = initialAlter.value!!.pronouns
      _description.value = initialAlter.value!!.description
      _color.value = initialAlter.value!!.color
      _securityLevel.value = initialAlter.value!!.securityLevel
      _fields.value = initialAlter.value!!.fields
      _alias.value = initialAlter.value!!.alias
      _proxyName.value = initialAlter.value!!.proxyName
      _untracked.value = initialAlter.value!!.untracked
      _archived.value = initialAlter.value!!.archived
      _pinned.value = initialAlter.value!!.pinned
    }
  }
}