package app.octocon.app.ui.compose.screens.main.settings.pages

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Brush
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Lens
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Smartphone
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import app.octocon.app.ChangeFrontMode
import app.octocon.app.ColorMode
import app.octocon.app.CornerStyle
import app.octocon.app.DynamicColorType
import app.octocon.app.ThemeColor
import app.octocon.app.ui.compose.LocalChildPanelsMode
import app.octocon.app.ui.compose.components.SettingsDrawerItem
import app.octocon.app.ui.compose.components.SettingsSection
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
import app.octocon.app.ui.model.main.settings.SettingsAppearanceComponent
import app.octocon.app.utils.compose
import app.octocon.app.utils.derive
import app.octocon.app.utils.state
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.router.panels.ChildPanelsMode
import octoconapp.shared.generated.resources.Res
import octoconapp.shared.generated.resources.amoled_mode
import octoconapp.shared.generated.resources.appearance
import octoconapp.shared.generated.resources.change_front_mode
import octoconapp.shared.generated.resources.color
import octoconapp.shared.generated.resources.color_mode
import octoconapp.shared.generated.resources.color_mode_dark
import octoconapp.shared.generated.resources.color_mode_light
import octoconapp.shared.generated.resources.color_mode_system
import octoconapp.shared.generated.resources.corner_style
import octoconapp.shared.generated.resources.dynamic_color_type
import octoconapp.shared.generated.resources.functionality
import octoconapp.shared.generated.resources.hide_alters_in_tags
import octoconapp.shared.generated.resources.show_alter_ids
import octoconapp.shared.generated.resources.style
import octoconapp.shared.generated.resources.theme_color
import octoconapp.shared.generated.resources.tooltip_amoled_mode_desc
import octoconapp.shared.generated.resources.tooltip_change_front_mode_desc
import octoconapp.shared.generated.resources.tooltip_color_mode_desc
import octoconapp.shared.generated.resources.tooltip_corner_style_desc
import octoconapp.shared.generated.resources.tooltip_dynamic_color_type_desc
import octoconapp.shared.generated.resources.tooltip_hide_alters_in_tags_desc
import octoconapp.shared.generated.resources.tooltip_show_alter_ids_desc
import octoconapp.shared.generated.resources.tooltip_theme_color_desc
import octoconapp.shared.generated.resources.tooltip_use_small_avatars_desc
import octoconapp.shared.generated.resources.tooltip_use_tablet_layout_desc
import octoconapp.shared.generated.resources.use_small_avatars
import octoconapp.shared.generated.resources.use_tablet_layout

@OptIn(ExperimentalDecomposeApi::class)
@Composable
fun SettingsAppearanceScreen(
  component: SettingsAppearanceComponent
) {
  val color = Res.string.color.compose
  val functionality = Res.string.functionality.compose
  val style = Res.string.style.compose

  val settings: SettingsInterface = component.settings
  val settingsData by component.settings.collectAsState()

  val isSinglet = settingsData.isSinglet

  OctoScaffold(
    topBar = { topAppBarState, scrollBehavior, showSnackbar ->
      OctoTopBar(
        navigation = {
          val childPanelsMode = LocalChildPanelsMode.current

          if(childPanelsMode == ChildPanelsMode.SINGLE) {
            BackNavigationButton(component::navigateBack)
          }
        },
        titleTextState = TitleTextState(Res.string.appearance.compose),
        topAppBarState = topAppBarState,
        scrollBehavior = scrollBehavior
      )
    },
    content = { _, _ ->
      LazyColumn(
        modifier = Modifier.fillMaxHeight().padding(horizontal = GLOBAL_PADDING)
      ) {
        SettingsSection(
          color,
          settingsData,
          { SettingsColorMode(it, settings) },
          { SettingsThemeColor(it, settings) },
          { SettingsDynamicColorType(it, settings) },
          { SettingsAmoledMode(it, settings) }
        )
        SettingsSection(
          functionality,
          settingsData,
          { if(!isSinglet) { SettingsChangeFrontMode(it, settings) } },
          {
            val cardGroupPosition = if (isSinglet) CardGroupPosition.START else it
            SettingsShowAlterIds(cardGroupPosition, settings)
          },
          { if(!isSinglet) { SettingsHideAltersInTags(it, settings) } },
          { SettingsUseTabletLayout(it, settings) }
        )
        SettingsSection(
          style,
          settingsData,
          { SettingsCornerStyle(it, settings) },
          { SettingsUseSmallAvatars(it, settings) }
        )

        item {
          Spacer(modifier = Modifier.height(GLOBAL_PADDING))
        }
      }
    }
  )
}


@Composable
private fun SettingsColorMode(cardGroupPosition: CardGroupPosition, settings: SettingsInterface) {
  val settingsData by settings.collectAsState()
  val colorMode by derive { settingsData.colorMode }

  val icon = when (colorMode) {
    ColorMode.SYSTEM -> Icons.Rounded.Smartphone
    ColorMode.LIGHT -> Icons.Rounded.LightMode
    ColorMode.DARK -> Icons.Rounded.DarkMode
  }

  var sheetOpen by state(false)

  SettingsDrawerItem(
    text = Res.string.color_mode.compose,
    value = colorMode.displayName,
    spotlightDescription = Res.string.tooltip_color_mode_desc.compose,
    icon = icon,
    cardGroupPosition = cardGroupPosition,
  ) { sheetOpen = true }

  if (sheetOpen) {
    OctoBottomSheet(
      onDismissRequest = { sheetOpen = false }
    ) {
      BottomSheetListItem(
        imageVector = Icons.Rounded.Smartphone,
        title = Res.string.color_mode_system.compose,
      ) { settings.setColorMode(ColorMode.SYSTEM); sheetOpen = false }
      BottomSheetListItem(
        imageVector = Icons.Rounded.LightMode,
        title = Res.string.color_mode_light.compose,
      ) { settings.setColorMode(ColorMode.LIGHT); sheetOpen = false }
      BottomSheetListItem(
        imageVector = Icons.Rounded.DarkMode,
        title = Res.string.color_mode_dark.compose,
      ) { settings.setColorMode(ColorMode.DARK); sheetOpen = false }
    }
  }
}


@Composable
private fun SettingsThemeColor(cardGroupPosition: CardGroupPosition, settings: SettingsInterface) {
  val settingsData by settings.collectAsState()
  val themeColor by derive { settingsData.themeColor }

  val icon = Icons.Rounded.Brush
  val themePainter = rememberVectorPainter(Icons.Rounded.Lens)

  var sheetOpen by state(false)

  SettingsDrawerItem(
    text = Res.string.theme_color.compose,
    value = themeColor.displayName,
    spotlightDescription = Res.string.tooltip_theme_color_desc.compose,
    icon = icon,
    cardGroupPosition = cardGroupPosition,
  ) { sheetOpen = true }

  if (sheetOpen) {
    OctoBottomSheet(
      onDismissRequest = { sheetOpen = false }
    ) {
      enumValues<ThemeColor>().forEach {
        BottomSheetListItem(
          icon = themePainter,
          title = it.displayName,
          iconTint = it.themeColors.seed
        ) { settings.setThemeColor(it); sheetOpen = false }
      }
    }
  }
}


@Composable
private fun SettingsDynamicColorType(cardGroupPosition: CardGroupPosition, settings: SettingsInterface) {
  val settingsData by settings.collectAsState()
  val dynamicColorType by derive { settingsData.dynamicColorType }

  val icon = dynamicColorType.icon

  var sheetOpen by state(false)

  SettingsDrawerItem(
    text = Res.string.dynamic_color_type.compose,
    spotlightDescription = Res.string.tooltip_dynamic_color_type_desc.compose,
    value = dynamicColorType.displayName,
    icon = icon,
    cardGroupPosition = cardGroupPosition,
  ) { sheetOpen = true }

  if (sheetOpen) {
    OctoBottomSheet(
      onDismissRequest = { sheetOpen = false }
    ) {
      enumValues<DynamicColorType>().forEach {
        BottomSheetListItem(
          icon = rememberVectorPainter(it.icon),
          title = it.displayName
        ) { settings.setDynamicColorType(it); sheetOpen = false }
      }
    }
  }
}

@Composable
private fun SettingsAmoledMode(cardGroupPosition: CardGroupPosition, settings: SettingsInterface) {
  val settingsData by settings.collectAsState()
  val amoledMode by derive { settingsData.amoledMode }

  SettingsToggleItem(
    text = Res.string.amoled_mode.compose,
    value = amoledMode,
    spotlightDescription = Res.string.tooltip_amoled_mode_desc.compose,
    cardGroupPosition = cardGroupPosition,
    updateValue = settings::setAmoledMode
  )
}


@Composable
private fun SettingsCornerStyle(cardGroupPosition: CardGroupPosition, settings: SettingsInterface) {
  val settingsData by settings.collectAsState()
  val cornerStyle by derive { settingsData.cornerStyle }

  val icon = cornerStyle.icon

  var sheetOpen by state(false)

  SettingsDrawerItem(
    text = Res.string.corner_style.compose,
    spotlightDescription = Res.string.tooltip_corner_style_desc.compose,
    value = cornerStyle.displayName,
    icon = icon,
    cardGroupPosition = cardGroupPosition,
  ) { sheetOpen = true }

  if (sheetOpen) {
    OctoBottomSheet(
      onDismissRequest = { sheetOpen = false }
    ) {
      enumValues<CornerStyle>().forEach {
        BottomSheetListItem(
          icon = rememberVectorPainter(it.icon),
          title = it.displayName
        ) { settings.setCornerStyle(it); sheetOpen = false }
      }
    }
  }
}


@Composable
private fun SettingsChangeFrontMode(cardGroupPosition: CardGroupPosition, settings: SettingsInterface) {
  val settingsData by settings.collectAsState()
  val changeFrontMode by derive { settingsData.changeFrontMode }

  val icon = changeFrontMode.icon

  var sheetOpen by state(false)

  SettingsDrawerItem(
    text = Res.string.change_front_mode.compose,
    value = changeFrontMode.displayName,
    icon = icon,
    spotlightDescription = Res.string.tooltip_change_front_mode_desc.compose,
    cardGroupPosition = cardGroupPosition,
  ) { sheetOpen = true }

  if (sheetOpen) {
    OctoBottomSheet(
      onDismissRequest = { sheetOpen = false }
    ) {
      enumValues<ChangeFrontMode>().forEach {
        BottomSheetListItem(
          icon = rememberVectorPainter(it.icon),
          title = it.displayName
        ) { settings.setChangeFrontMode(it); sheetOpen = false }
      }
    }
  }
}


@Composable
private fun SettingsUseSmallAvatars(cardGroupPosition: CardGroupPosition, settings: SettingsInterface) {
  val settingsData by settings.collectAsState()
  val useSmallAvatars by derive { settingsData.useSmallAvatars }

  SettingsToggleItem(
    text = Res.string.use_small_avatars.compose,
    value = useSmallAvatars,
    cardGroupPosition = cardGroupPosition,
    spotlightDescription = Res.string.tooltip_use_small_avatars_desc.compose,
    updateValue = settings::setUseSmallAvatars
  )
}

@Composable
private fun SettingsShowAlterIds(cardGroupPosition: CardGroupPosition, settings: SettingsInterface) {
  val settingsData by settings.collectAsState()
  val showAlterIds by derive { settingsData.showAlterIds }

  SettingsToggleItem(
    text = Res.string.show_alter_ids.compose,
    value = showAlterIds,
    spotlightDescription = Res.string.tooltip_show_alter_ids_desc.compose,
    cardGroupPosition = cardGroupPosition,
    updateValue = settings::setShowAlterIds
  )
}

@Composable
private fun SettingsUseTabletLayout(cardGroupPosition: CardGroupPosition, settings: SettingsInterface) {
  val settingsData by settings.collectAsState()
  val useTabletLayout by derive { settingsData.useTabletLayout }

  SettingsToggleItem(
    text = Res.string.use_tablet_layout.compose,
    value = useTabletLayout,
    spotlightDescription = Res.string.tooltip_use_tablet_layout_desc.compose,
    cardGroupPosition = cardGroupPosition,
    updateValue = settings::setUseTabletLayout
  )
}

@Composable
private fun SettingsHideAltersInTags(cardGroupPosition: CardGroupPosition, settings: SettingsInterface) {
  val settingsData by settings.collectAsState()
  val hideAltersInTags by derive { settingsData.hideAltersInTags }

  SettingsToggleItem(
    text = Res.string.hide_alters_in_tags.compose,
    value = hideAltersInTags,
    spotlightDescription = Res.string.tooltip_hide_alters_in_tags_desc.compose,
    cardGroupPosition = cardGroupPosition,
    updateValue = settings::setHideAltersInTags
  )
}