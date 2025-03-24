package app.octocon.app.ui.compose.screens.main.settings.pages

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import app.octocon.app.FontChoice
import app.octocon.app.ui.compose.LocalChildPanelsMode
import app.octocon.app.ui.compose.components.SettingsDrawerItem
import app.octocon.app.ui.compose.components.SettingsSection
import app.octocon.app.ui.compose.components.SettingsSliderItem
import app.octocon.app.ui.compose.components.SettingsToggleItem
import app.octocon.app.ui.compose.components.shared.BackNavigationButton
import app.octocon.app.ui.compose.components.shared.BottomSheetListItem
import app.octocon.app.ui.compose.components.shared.CardGroupPosition
import app.octocon.app.ui.compose.components.shared.OctoBottomSheet
import app.octocon.app.ui.compose.components.shared.OctoScaffold
import app.octocon.app.ui.compose.components.shared.OctoTopBar
import app.octocon.app.ui.compose.components.shared.TitleTextState
import app.octocon.app.ui.compose.screens.GLOBAL_PADDING
import app.octocon.app.ui.model.interfaces.SettingsInterface
import app.octocon.app.ui.model.main.settings.SettingsAccessibilityComponent
import app.octocon.app.utils.compose
import app.octocon.app.utils.derive
import app.octocon.app.utils.state
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.router.panels.ChildPanelsMode
import octoconapp.shared.generated.resources.Res
import octoconapp.shared.generated.resources.accessibility
import octoconapp.shared.generated.resources.color_contrast_level
import octoconapp.shared.generated.resources.font_choice
import octoconapp.shared.generated.resources.font_size
import octoconapp.shared.generated.resources.reduce_motion
import octoconapp.shared.generated.resources.show_permanent_tips
import octoconapp.shared.generated.resources.spotlight_enabled
import octoconapp.shared.generated.resources.spotlight_long_press_timeout
import octoconapp.shared.generated.resources.spotlight_tooltips
import octoconapp.shared.generated.resources.tooltip_color_contrast_level_desc
import octoconapp.shared.generated.resources.tooltip_font_choice_desc
import octoconapp.shared.generated.resources.tooltip_font_size_desc
import octoconapp.shared.generated.resources.tooltip_reduce_motion_desc
import octoconapp.shared.generated.resources.tooltip_show_permanent_tips_desc
import octoconapp.shared.generated.resources.tooltip_spotlight_desc
import octoconapp.shared.generated.resources.tooltip_spotlight_long_press_timeout_desc

@OptIn(ExperimentalDecomposeApi::class)
@Composable
fun SettingsAccessibilityScreen(
  component: SettingsAccessibilityComponent
) {

  val settings: SettingsInterface = component.settings
  val settingsData by component.settings.collectAsState()

  val spotlight = Res.string.spotlight_tooltips.compose

  OctoScaffold(
    topBar = { topAppBarState, scrollBehavior, showSnackbar ->
      OctoTopBar(
        navigation = {
          val childPanelsMode = LocalChildPanelsMode.current

          if(childPanelsMode == ChildPanelsMode.SINGLE) {
            BackNavigationButton(component::navigateBack)
          }
        },
        titleTextState = TitleTextState(Res.string.accessibility.compose),
        topAppBarState = topAppBarState,
        scrollBehavior = scrollBehavior
      )
    },
    content = { _, _ ->
      LazyColumn(
        modifier = Modifier.fillMaxHeight().padding(horizontal = GLOBAL_PADDING)
      ) {
        SettingsSection(
          null,
          settingsData,
          // { SettingsShowHelpFAB(it) },
          { SettingsShowPermanentTips(it, settings) },
          { SettingsFontChoice(it, settings) },
          { SettingsFontSizeScalar(it, settings) },
          { SettingsColorContrastLevel(it, settings) },
          { SettingsReduceMotion(it, settings) }
          // TODO: Find a way to reimplement this with Decompose (or predictive back in general)?
          // { SettingsScreenTransitionType(it, settings) },
        )

        SettingsSection(
          spotlight,
          settingsData,
          { SettingsSpotlightEnabled(it, settings) },
          { SettingsSpotlightLongPressTimeout(it, settings) }
        )

        item {
          Spacer(modifier = Modifier.height(GLOBAL_PADDING))
        }
      }
    }
  )
}


@Composable
private fun SettingsShowPermanentTips(cardGroupPosition: CardGroupPosition, settings: SettingsInterface) {
  val settingsData by settings.collectAsState()
  val showPermanentTips by derive { settingsData.showPermanentTips }

  SettingsToggleItem(
    text = Res.string.show_permanent_tips.compose,
    value = showPermanentTips,
    spotlightDescription = Res.string.tooltip_show_permanent_tips_desc.compose,
    cardGroupPosition = cardGroupPosition,
    updateValue = settings::setShowPermanentTips
  )
}

@Composable
private fun SettingsFontChoice(cardGroupPosition: CardGroupPosition, settings: SettingsInterface) {
  val settingsData by settings.collectAsState()
  val fontChoice by derive { settingsData.fontChoice }

  var sheetOpen by state(false)

  SettingsDrawerItem(
    icon = fontChoice.icon,
    text = Res.string.font_choice.compose,
    value = fontChoice.displayName,
    spotlightDescription = Res.string.tooltip_font_choice_desc.compose,
    cardGroupPosition = cardGroupPosition,
  ) { sheetOpen = true }

  if (sheetOpen) {
    OctoBottomSheet(
      onDismissRequest = { sheetOpen = false }
    ) {
      enumValues<FontChoice>().forEach {
        BottomSheetListItem(
          title = it.displayName,
          icon = rememberVectorPainter(it.icon)
        ) {
          settings.setFontChoice(it)
          sheetOpen = false
        }
      }
    }
  }
}


@Composable
private fun SettingsFontSizeScalar(cardGroupPosition: CardGroupPosition, settings: SettingsInterface) {
  val settingsData by settings.collectAsState()
  val fontSizeScalar by derive { settingsData.fontSizeScalar }

  SettingsSliderItem(
    text = Res.string.font_size.compose,
    value = fontSizeScalar,
    spotlightDescription = Res.string.tooltip_font_size_desc.compose,
    cardGroupPosition = cardGroupPosition,
    textualValue = { it.displayName },
    updateValue = settings::setFontSizeScalar
  )
}


@Composable
private fun SettingsColorContrastLevel(cardGroupPosition: CardGroupPosition, settings: SettingsInterface) {
  val settingsData by settings.collectAsState()
  val colorContrastLevel by derive { settingsData.colorContrastLevel }

  SettingsSliderItem(
    text = Res.string.color_contrast_level.compose,
    value = colorContrastLevel,
    spotlightDescription = Res.string.tooltip_color_contrast_level_desc.compose,
    cardGroupPosition = cardGroupPosition,
    textualValue = { it.displayName },
    updateValue = settings::setColorContrastLevel
  )
}

@Composable
private fun SettingsReduceMotion(cardGroupPosition: CardGroupPosition, settings: SettingsInterface) {
  val settingsData by settings.collectAsState()
  val reduceMotion by derive { settingsData.reduceMotion }

  SettingsToggleItem(
    text = Res.string.reduce_motion.compose,
    value = reduceMotion,
    spotlightDescription = Res.string.tooltip_reduce_motion_desc.compose,
    cardGroupPosition = cardGroupPosition,
    updateValue = settings::setReduceMotion
  )
}

/*@Composable
private fun SettingsScreenTransitionType(cardGroupPosition: CardGroupPosition, settings: SettingsInterface) {
  val settingsData by settings.collectAsState()
  val screenTransitionType by derive { settingsData.screenTransitionType }

  var sheetOpen by state(false)

  SettingsDrawerItem(
    icon = screenTransitionType.icon,
    text = Res.string.screen_transition_type.compose,
    value = screenTransitionType.displayName,
    cardGroupPosition = cardGroupPosition,
  ) { sheetOpen = true }

  if (sheetOpen) {
    OctoBottomSheet(
      onDismissRequest = { sheetOpen = false }
    ) {
      enumValues<ScreenTransitionType>().forEach {
        BottomSheetListItem(
          title = it.displayName,
          icon = rememberVectorPainter(it.icon)
        ) {
          settings.setScreenTransitionType(it)
          sheetOpen = false
        }
      }
    }
  }
}*/

@Composable
private fun SettingsSpotlightEnabled(cardGroupPosition: CardGroupPosition, settings: SettingsInterface) {
  val settingsData by settings.collectAsState()
  val spotlightEnabled by derive { settingsData.spotlightEnabled }

  SettingsToggleItem(
    text = Res.string.spotlight_enabled.compose,
    value = spotlightEnabled,
    spotlightDescription = Res.string.tooltip_spotlight_desc.compose,
    cardGroupPosition = cardGroupPosition,
    updateValue = settings::setSpotlightEnabled
  )
}

@Composable
private fun SettingsSpotlightLongPressTimeout(cardGroupPosition: CardGroupPosition, settings: SettingsInterface) {
  val settingsData by settings.collectAsState()
  val spotlightLongPressTimeout by derive { settingsData.spotlightLongPressTimeout }

  SettingsSliderItem(
    text = Res.string.spotlight_long_press_timeout.compose,
    value = spotlightLongPressTimeout,
    textualValue = { it.displayName },
    spotlightDescription = Res.string.tooltip_spotlight_long_press_timeout_desc.compose,
    cardGroupPosition = cardGroupPosition,
    updateValue = settings::setSpotlightLongPressTimeout
  )
}