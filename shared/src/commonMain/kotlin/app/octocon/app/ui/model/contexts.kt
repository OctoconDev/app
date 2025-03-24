package app.octocon.app.ui.model

import app.octocon.app.ui.model.interfaces.ApiInterface
import app.octocon.app.ui.model.interfaces.SettingsInterface
import app.octocon.app.utils.PlatformUtilities
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.ComponentContextFactory
import com.arkivanov.decompose.GenericComponentContext
import com.arkivanov.essenty.backhandler.BackHandlerOwner
import com.arkivanov.essenty.instancekeeper.InstanceKeeperOwner
import com.arkivanov.essenty.lifecycle.LifecycleOwner
import com.arkivanov.essenty.statekeeper.StateKeeperOwner
import kotlin.coroutines.CoroutineContext

interface CommonInterface {
  val api: ApiInterface
  val settings: SettingsInterface
  val platformUtilities: PlatformUtilities
  val coroutineContext: CoroutineContext
}

interface CommonComponentContext : CommonInterface, GenericComponentContext<CommonComponentContext>

class CommonComponentContextImpl(
  componentContext: ComponentContext,
  override val api: ApiInterface,
  override val settings: SettingsInterface,
  override val platformUtilities: PlatformUtilities,
  override val coroutineContext: CoroutineContext
) : CommonComponentContext,
  LifecycleOwner by componentContext,
  StateKeeperOwner by componentContext,
  InstanceKeeperOwner by componentContext,
  BackHandlerOwner by componentContext {

  override val componentContextFactory: ComponentContextFactory<CommonComponentContext> =
    ComponentContextFactory { lifecycle, stateKeeper, instanceKeeper, backHandler ->
      val ctx = componentContext.componentContextFactory(lifecycle, stateKeeper, instanceKeeper, backHandler)
      CommonComponentContextImpl(ctx, api, settings, platformUtilities, coroutineContext)
    }
}

interface MainComponentContext : CommonInterface, GenericComponentContext<MainComponentContext>

class MainComponentContextImpl(
  componentContext: CommonComponentContext
) : MainComponentContext,
  LifecycleOwner by componentContext,
  StateKeeperOwner by componentContext,
  InstanceKeeperOwner by componentContext,
  BackHandlerOwner by componentContext {
  override val api = componentContext.api
  override val settings = componentContext.settings
  override val platformUtilities = componentContext.platformUtilities
  override val coroutineContext = componentContext.coroutineContext

  override val componentContextFactory: ComponentContextFactory<MainComponentContext> =
    ComponentContextFactory { lifecycle, stateKeeper, instanceKeeper, backHandler ->
      val ctx = componentContext.componentContextFactory(lifecycle, stateKeeper, instanceKeeper, backHandler)
      MainComponentContextImpl(ctx)
    }
}