package app.octocon.app.ui.model.onboarding.pages

import app.octocon.app.api.model.MyAlter
import app.octocon.app.api.model.SecurityLevel
import app.octocon.app.ui.model.CommonComponentContext
import app.octocon.app.ui.model.CommonInterface
import app.octocon.app.ui.registerStateHandler
import app.octocon.app.ui.retainStateHandler
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable

interface OnboardingFrontTutorialComponent : CommonInterface {
  val model: StateFlow<Model>
  val alters: List<MyAlter>

  fun addAlterToFront(id: Int)
  fun removeAlterFromFront(id: Int)
  fun setAlterAsFront(id: Int)
  fun setPrimaryFront(id: Int?)

  @Serializable
  data class Model(
    val frontingAlters: List<Int> = emptyList(),
    val primaryFront: Int? = null
  )
}

class OnboardingFrontTutorialComponentImpl(
  componentContext: CommonComponentContext
) : OnboardingFrontTutorialComponent, CommonComponentContext by componentContext {
  private val handler = retainStateHandler { OnboardingFrontTutorialComponent.Model() }
  init {
    registerStateHandler(handler)
  }
  override val model = handler.model

  override val alters = listOf(
    MyAlter(
      id = 1,
      name = "Atlas",
      pronouns = "he/him",
      fields = emptyList(),
      securityLevel = SecurityLevel.PRIVATE
    ),
    MyAlter(
      id = 2,
      name = "Gaia",
      pronouns = "she/her",
      fields = emptyList(),
      securityLevel = SecurityLevel.PRIVATE
    ),
    MyAlter(
      id = 3,
      name = "Hyperion",
      pronouns = "they/them",
      fields = emptyList(),
      securityLevel = SecurityLevel.PRIVATE
    )
  )

  override fun addAlterToFront(id: Int) {
    val currentFronts = model.value.frontingAlters.toMutableList()
    currentFronts.add(id)

    handler.model.tryEmit(model.value.copy(frontingAlters = currentFronts))
  }

  override fun removeAlterFromFront(id: Int) {
    val currentFronts = model.value.frontingAlters.toMutableList()
    currentFronts.remove(id)

    handler.model.tryEmit(model.value.copy(frontingAlters = currentFronts))

    if(model.value.primaryFront == id) {
      handler.model.tryEmit(model.value.copy(primaryFront = null))
    }
  }

  override fun setAlterAsFront(id: Int) {
    handler.model.tryEmit(model.value.copy(frontingAlters = listOf(id)))
  }

  override fun setPrimaryFront(id: Int?) {
    handler.model.tryEmit(model.value.copy(primaryFront = id))
  }
}