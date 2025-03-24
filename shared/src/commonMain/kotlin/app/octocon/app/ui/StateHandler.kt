package app.octocon.app.ui

import app.octocon.app.utils.globalSerializersModule
import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.arkivanov.essenty.instancekeeper.InstanceKeeperOwner
import com.arkivanov.essenty.instancekeeper.retainedInstance
import com.arkivanov.essenty.statekeeper.StateKeeperOwner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.serializer

class StateHandler<T>(initialModel: T) : InstanceKeeper.Instance {
  val model: MutableStateFlow<T> = MutableStateFlow(initialModel)
}

inline fun <reified T> generateStateKeyForType(): String = "${T::class.simpleName}_STATE"

inline fun <reified T : Any, O> O.retainStateHandler(default: () -> T): StateHandler<T>
    where O : StateKeeperOwner, O : InstanceKeeperOwner =
  retainedInstance {
    StateHandler(
      initialModel = stateKeeper.consume(generateStateKeyForType<T>(), strategy = globalSerializersModule.serializer<T>())
        ?: default()
    )
  }

inline fun <reified T> StateKeeperOwner.registerStateHandler(handler: StateHandler<T>) {
  stateKeeper.register(
    key = generateStateKeyForType<T>(),
    strategy = globalSerializersModule.serializer<T>()
  ) { handler.model.value }
}