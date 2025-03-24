package app.octocon.app.ui.model.main.hometabs.alters

import androidx.compose.runtime.Composable
import app.octocon.app.AlterSortingMethod
import app.octocon.app.api.APIState
import app.octocon.app.api.model.APIResponse
import app.octocon.app.api.model.MyTag
import app.octocon.app.api.model.SecurityLevel
import app.octocon.app.ui.model.CommonInterface
import app.octocon.app.ui.model.MainComponentContext
import app.octocon.app.ui.model.interfaces.ApiInterfaceImpl
import app.octocon.app.ui.model.main.SaveState
import app.octocon.app.ui.model.main.hometabs.alters.tagview.TagViewContentsComponent
import app.octocon.app.ui.model.main.hometabs.alters.tagview.TagViewContentsComponentImpl
import app.octocon.app.ui.model.main.hometabs.alters.tagview.TagViewSettingsComponent
import app.octocon.app.ui.model.main.hometabs.alters.tagview.TagViewSettingsComponentImpl
import app.octocon.app.utils.colorRegex
import app.octocon.app.utils.compose
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
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import octoconapp.shared.generated.resources.Res
import octoconapp.shared.generated.resources.contents
import octoconapp.shared.generated.resources.settings
import octoconapp.shared.generated.resources.tooltip_tag_contents_desc
import octoconapp.shared.generated.resources.tooltip_tag_contents_title
import octoconapp.shared.generated.resources.tooltip_tag_settings_desc
import octoconapp.shared.generated.resources.tooltip_tag_settings_title

interface TagViewComponent : CommonInterface {
  val pages: Value<ChildPages<*, Child>>

  val tagID: String
  val model: Model

  fun commit()

  fun setAlterSortingMethod(alterSortingMethod: AlterSortingMethod)

  fun navigateToPage(index: Int)
  fun navigateBack()

  fun updateShowSnackbar(showSnackbar: (String) -> Unit)

  interface Model {
    val apiTag: StateFlow<MyTag?>

    val id: String
    val saveState: StateFlow<SaveState>
    val name: StateFlow<String>
    val description: StateFlow<String?>
    val color: StateFlow<String?>
    val securityLevel: StateFlow<SecurityLevel>
    val alters: StateFlow<List<Int>>
    val parentTagID: StateFlow<String?>

    fun updateName(name: String): Result<String>
    fun updateColor(color: String)
    fun updateDescription(description: String): Result<String>
    fun updateSecurityLevel(securityLevel: SecurityLevel)
    fun updateSaveState(saveState: SaveState)

    val initialTag: StateFlow<MyTag?>
    val tagHasChanged: StateFlow<Boolean>
    val isLoaded: StateFlow<Boolean>

    fun revertChanges()
  }

  // TODO: Remove dependency on Compose here
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
          is ContentsChild -> ContentsChild
          is SettingsChild -> SettingsChild
        }

      val allMetadata: List<Metadata> by lazy {
        listOf(
          ContentsChild,
          SettingsChild
        )
      }
    }

    class ContentsChild(val component: TagViewContentsComponent) : Child {
      companion object : Metadata {
        override val index: Int = 0
        override val title: String
          @Composable get() = Res.string.contents.compose
        override val spotlightTitle: String
          @Composable get() = Res.string.tooltip_tag_contents_title.compose
        override val spotlightDescription: String
          @Composable get() = Res.string.tooltip_tag_contents_desc.compose
      }
    }
    class SettingsChild(val component: TagViewSettingsComponent) : Child {
      companion object : Metadata {
        override val index: Int = 1
        override val title: String
          @Composable get() = Res.string.settings.compose
        override val spotlightTitle: String
          @Composable get() = Res.string.tooltip_tag_settings_title.compose
        override val spotlightDescription: String
          @Composable get() = Res.string.tooltip_tag_settings_desc.compose
      }
    }
  }
}

class TagViewComponentImpl(
  componentContext: MainComponentContext,
  private val popSelf: () -> Unit,
  private val navigateToTagViewFun: (String) -> Unit,
  private val navigateToAlterViewFun: (Int) -> Unit,
  override val tagID: String
) : TagViewComponent, MainComponentContext by componentContext {
  private val coroutineScope = coroutineScope(coroutineContext + SupervisorJob())

  private val apiTag = api.tags
    .map { state ->
      val data = (state as? APIState.Success)?.data ?: return@map null
      data.find { it.id == tagID }
    }
    .stateIn(coroutineScope, SharingStarted.Eagerly, null)

  private val _model = retainedInstance { ModelImpl(tagID, apiTag, coroutineScope) }
  override val model: TagViewComponent.Model = _model

  init {
    coroutineScope.launch {
      apiTag.collect {
        if(it == null) return@collect

        if(!_model.isLoaded.value) {
          _model.injectInitialTag(it)
        } else {
          _model.reconcile(it)
        }
      }
    }

    lifecycle.doOnDestroy {
      if(model.tagHasChanged.value && model.saveState.value == SaveState.NotSaved) {
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
          Config.Contents,
          Config.Settings
        ),
        selectedIndex = 0
      ) },
      handleBackButton = false,
      childFactory = ::child,
    )

  override val pages: Value<ChildPages<*, TagViewComponent.Child>> = _pages

  private fun child(config: Config, componentContext: MainComponentContext): TagViewComponent.Child {
    return when (config) {
      Config.Contents ->
        TagViewComponent.Child.ContentsChild(
          TagViewContentsComponentImpl(
            componentContext = componentContext,
            navigateToAlterViewFun = navigateToAlterViewFun,
            navigateToTagViewFun = navigateToTagViewFun,
            model = model
          )
        )

      Config.Settings ->
        TagViewComponent.Child.SettingsChild(
          TagViewSettingsComponentImpl(
            componentContext = componentContext,
            model = model
          )
        )
    }
  }

  private var showSnackbar: ((String) -> Unit)? = null

  override fun updateShowSnackbar(showSnackbar: (String) -> Unit) {
    this.showSnackbar = showSnackbar
  }

  override fun navigateToPage(index: Int) {
    if(!model.isLoaded.value) return
    navigator.select(index)
  }

  private var pendingSaveEvent: (() -> Unit)? = null

  private fun tryExit(onSave: () -> Unit) {
    if (model.tagHasChanged.value) {
      pendingSaveEvent = onSave
      commit()
    } else {
      onSave()
    }
  }

  override fun navigateBack() = tryExit { popSelf() }

  override fun commit() {
    if (!model.tagHasChanged.value) return

    if (model.name.value.isBlank()) {
      showSnackbar?.invoke("Tag name cannot be empty.")
      return
    }

    model.updateSaveState(SaveState.Saving)
    doPatchRequest { isSuccess, response ->
      if(isSuccess) {
        pendingSaveEvent?.invoke()
      } else {
        showSnackbar?.invoke(response.error ?: "Failed to save tag.")
      }
      _model.updateSaveState(
        if (isSuccess) SaveState.Saved
        else SaveState.Error
      )
    }
  }

  private fun doPatchRequest(callback: ((Boolean, APIResponse<JsonElement>) -> Unit)? = null) {
    (api as ApiInterfaceImpl).sendAPIRequest<JsonElement>(
      HttpMethod.Patch,
      "systems/me/tags/${tagID}",
      _model.buildJsonDiff(),
      callback
    )
  }

  override fun setAlterSortingMethod(alterSortingMethod: AlterSortingMethod) {
    settings.setAlterSortingMethod(alterSortingMethod)
  }

  @Serializable
  private sealed interface Config {
    @Serializable
    data object Contents : Config

    @Serializable
    data object Settings : Config
  }

  private class ModelImpl(
    tagID: String,
    override val apiTag: StateFlow<MyTag?>,
    coroutineScope: CoroutineScope
  ) : TagViewComponent.Model, InstanceKeeper.Instance {
    override val id: String = tagID

    private val _saveState = MutableStateFlow(SaveState.NotSaved)
    override val saveState = _saveState

    private val _name = MutableStateFlow(apiTag.value?.name ?: "")
    override val name: StateFlow<String> = _name
    private val _description = MutableStateFlow<String?>(apiTag.value?.description)
    override val description: StateFlow<String?> = _description
    private val _color = MutableStateFlow<String?>(apiTag.value?.color)
    override val color: StateFlow<String?> = _color
    private val _securityLevel = MutableStateFlow(apiTag.value?.securityLevel ?: SecurityLevel.PRIVATE)
    override val securityLevel: StateFlow<SecurityLevel> = _securityLevel
    private val _alters = MutableStateFlow(apiTag.value?.alters ?: emptyList())
    override val alters: StateFlow<List<Int>> = _alters
    private val _parentTagID = MutableStateFlow<String?>(apiTag.value?.parentTagID)
    override val parentTagID: StateFlow<String?> = _parentTagID

    private val _initialTag = MutableStateFlow<MyTag?>(null)
    override val initialTag: StateFlow<MyTag?> = _initialTag

    private val _isLoaded = MutableStateFlow(false)
    override val isLoaded: StateFlow<Boolean> = _isLoaded

    override fun updateName(name: String): Result<String> {
      if (name.length > 99) return Result.failure(IllegalArgumentException("Name too long"))
      _name.value = name
      return Result.success(name)
    }

    override fun updateColor(color: String) {
      // Make sure it's a valid hex code (#000000 - #FFFFFF) with regex
      if (!(colorRegex matches color)) return
      _color.value = color
    }

    override fun updateDescription(description: String): Result<String> {
      if (description.length > 999) return Result.failure(IllegalArgumentException("Description too long"))
      _description.value = description
      return Result.success(description)
    }

    override fun updateSecurityLevel(securityLevel: SecurityLevel) {
      _securityLevel.value = securityLevel
    }

    override fun updateSaveState(saveState: SaveState) {
      _saveState.value = saveState
    }

    fun injectInitialTag(initialTag: MyTag) {
      _initialTag.value = initialTag
      _name.value = initialTag.name
      _color.value = initialTag.color
      _description.value = initialTag.description
      _securityLevel.value = initialTag.securityLevel
      _alters.value = initialTag.alters
      _parentTagID.value = initialTag.parentTagID
      _isLoaded.value = true
    }

    fun reconcile(tag: MyTag) {
      _alters.value = tag.alters
      _parentTagID.value = tag.parentTagID
    }

    /*override val tagHasChanged = derivedStateOf {
      if(!isLoaded.value) {
        return@derivedStateOf false
      }

      return@derivedStateOf (
        _name.value != initialTag.value!!.name
            || _color.value != initialTag.value!!.color
            || _description.value != initialTag.value!!.description
            || _securityLevel.value != initialTag.value!!.securityLevel
      )
    }*/

    override val tagHasChanged = combine(
      name,
      color,
      description,
      securityLevel,
      initialTag
    ) { name, color, description, securityLevel, initialTag ->
      if(!isLoaded.value) {
        return@combine false
      }

      return@combine (
        name != initialTag!!.name
            || color != initialTag.color
            || description != initialTag.description
            || securityLevel != initialTag.securityLevel
      )
    }.stateIn(coroutineScope, SharingStarted.Eagerly, false)

    override fun revertChanges() {
      _name.value = initialTag.value!!.name
      _color.value = initialTag.value!!.color
      _description.value = initialTag.value!!.description
      _securityLevel.value = initialTag.value!!.securityLevel
    }

    fun buildJsonDiff(): String =
      buildJsonObject {
        if (name.value != initialTag.value!!.name)
          put("name", name.value)
        if (_color.value != initialTag.value!!.color)
          put("color", _color.value)
        if (_description.value != initialTag.value!!.description)
          put("description", _description.value)
        if (_securityLevel.value != initialTag.value!!.securityLevel)
          put("security_level", _securityLevel.value.internalName)
      }.toString()
  }
}