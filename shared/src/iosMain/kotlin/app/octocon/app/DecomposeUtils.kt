package app.octocon.app

import app.octocon.app.utils.globalSerializer
import app.octocon.app.ui.model.RootComponent
import app.octocon.app.ui.model.RootComponentImpl
import app.octocon.app.utils.platformUtilities
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.backhandler.BackDispatcher
import com.arkivanov.essenty.statekeeper.SerializableContainer
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.MainScope
import platform.Foundation.NSCoder
import platform.Foundation.NSString
import platform.Foundation.decodeTopLevelObjectOfClass
import platform.Foundation.encodeObject

@Suppress("unused") // Used in Swift
fun save(coder: NSCoder, state: SerializableContainer) {
  coder.encodeObject(`object` = globalSerializer.encodeToString(SerializableContainer.serializer(), state), forKey = "state")
}

@Suppress("unused") // Used in Swift
@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
fun restore(coder: NSCoder): SerializableContainer? =
  (coder.decodeTopLevelObjectOfClass(aClass = NSString, forKey = "state", error = null) as String?)?.let {
    try {
      globalSerializer.decodeFromString(SerializableContainer.serializer(), it)
    } catch (e: Exception) {
      null
    }
  }

fun createBackDispatcher() = BackDispatcher()

fun createRootComponent(
  componentContext: ComponentContext
): RootComponent = RootComponentImpl(
  componentContext = componentContext,
  initialSettings = getSettingsFromKeychain(),
  coroutineContext = MainScope().coroutineContext,
  platformUtilities = platformUtilities,
  platformEventFlow = platformEventFlow
)