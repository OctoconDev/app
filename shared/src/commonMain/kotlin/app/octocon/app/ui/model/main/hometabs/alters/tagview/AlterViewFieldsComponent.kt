package app.octocon.app.ui.model.main.hometabs.alters.tagview

import app.octocon.app.ui.model.CommonInterface
import app.octocon.app.ui.model.MainComponentContext
import app.octocon.app.ui.model.main.hometabs.alters.TagViewComponent

interface TagViewSettingsComponent : CommonInterface {
  val model: TagViewComponent.Model

  fun setParentTagID(parentTagID: String)
  fun removeParentTagID()
}

class TagViewSettingsComponentImpl(
  componentContext: MainComponentContext,
  override val model: TagViewComponent.Model
) : TagViewSettingsComponent, MainComponentContext by componentContext {
  override fun setParentTagID(parentTagID: String) {
    api.setParentTagID(model.id, parentTagID)
  }

  override fun removeParentTagID() {
    api.removeParentTagID(model.id)
  }
}