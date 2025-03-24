package app.octocon.app

import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Accessibility
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.BlurOn
import androidx.compose.material.icons.rounded.BrightnessHigh
import androidx.compose.material.icons.rounded.BrightnessLow
import androidx.compose.material.icons.rounded.BrightnessMedium
import androidx.compose.material.icons.rounded.CheckBoxOutlineBlank
import androidx.compose.material.icons.rounded.Circle
import androidx.compose.material.icons.rounded.Square
import androidx.compose.material.icons.rounded.Swipe
import androidx.compose.material.icons.rounded.SwipeLeft
import androidx.compose.material.icons.rounded.TextFields
import androidx.compose.material.icons.rounded.ZoomOutMap
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import app.octocon.app.api.model.MyAlter
import app.octocon.app.ui.compose.theme.BlissfulBlueTheme
import app.octocon.app.ui.compose.theme.GleefulGreenTheme
import app.octocon.app.ui.compose.theme.MonochromeTheme
import app.octocon.app.ui.compose.theme.OctoOrangeTheme
import app.octocon.app.ui.compose.theme.PlayfulPinkTheme
import app.octocon.app.ui.compose.theme.RadiantRedTheme
import app.octocon.app.ui.compose.theme.Theme
import app.octocon.app.ui.compose.theme.TranquilTealTheme
import app.octocon.app.ui.compose.theme.VibrantVioletTheme
import app.octocon.app.utils.Fonts
import app.octocon.app.utils.compose
import app.octocon.app.utils.globalSerializer
import app.octocon.app.utils.sortedLocaleAware
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.experimental.stack.animation.StackAnimator
import com.arkivanov.decompose.extensions.compose.experimental.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.experimental.stack.animation.plus
import com.arkivanov.decompose.extensions.compose.experimental.stack.animation.scale
import com.materialkolor.Contrast
import com.materialkolor.PaletteStyle
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import octoconapp.shared.generated.resources.Res
import octoconapp.shared.generated.resources.change_front_mode_bidirectional_swipe
import octoconapp.shared.generated.resources.change_front_mode_button
import octoconapp.shared.generated.resources.change_front_mode_swipe_default
import octoconapp.shared.generated.resources.color_contrast_level_increased
import octoconapp.shared.generated.resources.color_contrast_level_maximum
import octoconapp.shared.generated.resources.color_contrast_level_minimum
import octoconapp.shared.generated.resources.color_contrast_level_normal_default
import octoconapp.shared.generated.resources.color_contrast_level_reduced
import octoconapp.shared.generated.resources.color_mode_dark
import octoconapp.shared.generated.resources.color_mode_light
import octoconapp.shared.generated.resources.color_mode_system
import octoconapp.shared.generated.resources.corner_style_rounded_default
import octoconapp.shared.generated.resources.corner_style_square
import octoconapp.shared.generated.resources.dynamic_color_type_none
import octoconapp.shared.generated.resources.dynamic_color_type_normal_default
import octoconapp.shared.generated.resources.dynamic_color_type_vibrant
import octoconapp.shared.generated.resources.font_choice_default
import octoconapp.shared.generated.resources.font_choice_lexie_readable
import octoconapp.shared.generated.resources.font_choice_open_dyslexic
import octoconapp.shared.generated.resources.font_size_large
import octoconapp.shared.generated.resources.font_size_largest
import octoconapp.shared.generated.resources.font_size_regular_default
import octoconapp.shared.generated.resources.font_size_small
import octoconapp.shared.generated.resources.font_size_smallest
import octoconapp.shared.generated.resources.screen_transition_fade
import octoconapp.shared.generated.resources.screen_transition_native_default
import octoconapp.shared.generated.resources.screen_transition_none
import octoconapp.shared.generated.resources.screen_transition_zoom
import octoconapp.shared.generated.resources.spotlight_long_press_timeout_long
import octoconapp.shared.generated.resources.spotlight_long_press_timeout_longer
import octoconapp.shared.generated.resources.spotlight_long_press_timeout_medium
import octoconapp.shared.generated.resources.spotlight_long_press_timeout_short
import octoconapp.shared.generated.resources.spotlight_long_press_timeout_shorter
import octoconapp.shared.generated.resources.theme_color_blue_default
import octoconapp.shared.generated.resources.theme_color_green
import octoconapp.shared.generated.resources.theme_color_monochrome
import octoconapp.shared.generated.resources.theme_color_orange
import octoconapp.shared.generated.resources.theme_color_pink
import octoconapp.shared.generated.resources.theme_color_red
import octoconapp.shared.generated.resources.theme_color_teal
import octoconapp.shared.generated.resources.theme_color_violet

@Serializable
enum class ColorMode {
  @SerialName("light")
  LIGHT,

  @SerialName("dark")
  DARK,

  @SerialName("system")
  SYSTEM;

  @Composable
  fun shouldUseDarkTheme() = when (this) {
    LIGHT -> false
    DARK -> true
    SYSTEM -> isSystemInDarkTheme()
  }

  val displayName: String
    @Composable get() = when (this) {
      LIGHT -> Res.string.color_mode_light.compose
      DARK -> Res.string.color_mode_dark.compose
      SYSTEM -> Res.string.color_mode_system.compose
    }
}

@Serializable
enum class ThemeColor(
  val themeColors: Theme,
) {
  @SerialName("blissful_blue")
  BLISSFUL_BLUE(BlissfulBlueTheme),

  @SerialName("playful_pink")
  PLAYFUL_PINK(PlayfulPinkTheme),

  @SerialName("vibrant_violet")
  VIBRANT_VIOLET(VibrantVioletTheme),

  @SerialName("octo_orange")
  OCTO_ORANGE(OctoOrangeTheme),

  @SerialName("radiant_red")
  RADIANT_RED(RadiantRedTheme),

  @SerialName("gleeful_green")
  GLEEFUL_GREEN(GleefulGreenTheme),

  @SerialName("tranquil_teal")
  TRANQUIL_TEAL(TranquilTealTheme),

  @SerialName("monochrome")
  MONOCHROME(MonochromeTheme);

  val displayName: String
    @Composable get() = when (this) {
      BLISSFUL_BLUE -> Res.string.theme_color_blue_default.compose
      PLAYFUL_PINK -> Res.string.theme_color_pink.compose
      VIBRANT_VIOLET -> Res.string.theme_color_violet.compose
      OCTO_ORANGE -> Res.string.theme_color_orange.compose
      RADIANT_RED -> Res.string.theme_color_red.compose
      GLEEFUL_GREEN -> Res.string.theme_color_green.compose
      TRANQUIL_TEAL -> Res.string.theme_color_teal.compose
      MONOCHROME -> Res.string.theme_color_monochrome.compose
    }
}

@Serializable
enum class DynamicColorType(
  val icon: ImageVector,
  val paletteStyle: PaletteStyle? = null
) {
  @SerialName("none")
  NONE(
    Icons.Rounded.BrightnessLow
  ),

  @SerialName("normal")
  NORMAL(
    Icons.Rounded.BrightnessMedium,
    PaletteStyle.TonalSpot
  ),

  @SerialName("vibrant")
  VIBRANT(
    Icons.Rounded.BrightnessHigh,
    PaletteStyle.Fidelity
    // PaletteStyle.Fidelity
  );

  val displayName: String
    @Composable get() = when (this) {
      NONE -> Res.string.dynamic_color_type_none.compose
      NORMAL -> Res.string.dynamic_color_type_normal_default.compose
      VIBRANT -> Res.string.dynamic_color_type_vibrant.compose
    }
}

@Serializable
enum class ChangeFrontMode(
  val icon: ImageVector
) {
  @SerialName("swipe")
  SWIPE(
    Icons.Rounded.SwipeLeft
  ),
  BIDIRECTIONAL_SWIPE(
    Icons.Rounded.Swipe
  ),
  BUTTON(
    Icons.Rounded.ArrowUpward
  );

  val displayName: String
    @Composable get() = when (this) {
      SWIPE -> Res.string.change_front_mode_swipe_default.compose
      BIDIRECTIONAL_SWIPE -> Res.string.change_front_mode_bidirectional_swipe.compose
      BUTTON -> Res.string.change_front_mode_button.compose
    }
}

@Serializable
enum class AlterSortingMethod(val sortAlters: List<MyAlter>.(unnamedAlterString: String) -> List<MyAlter>) {
  @SerialName("alphabetical")
  ALPHABETICAL(
    { unnamedAlter ->
      sortedLocaleAware { it.name ?: unnamedAlter }
    }
  ),

  @SerialName("id")
  ID(
    { sortedBy { it.id } }
  );
}

@Serializable
enum class CornerStyle(
  val icon: ImageVector
) {
  @SerialName("rounded")
  ROUNDED(Icons.Rounded.Circle),

  @SerialName("square")
  SQUARE(Icons.Rounded.Square);

  val displayName: String
    @Composable get() = when (this) {
      ROUNDED -> Res.string.corner_style_rounded_default.compose
      SQUARE -> Res.string.corner_style_square.compose
    }
}

@Serializable
enum class FontSizeScalar(
  val scalar: Float
) {
  @SerialName("smallest")
  SMALLEST(
    0.8f
  ),

  @SerialName("small")
  SMALL(
    0.9f
  ),

  @SerialName("regular")
  REGULAR(
    1f
  ),

  @SerialName("large")
  LARGE(
    1.1f
  ),

  @SerialName("largest")
  LARGEST(
    1.2f
  );

  val displayName: String
    @Composable get() = when (this) {
      SMALLEST -> Res.string.font_size_smallest.compose
      SMALL -> Res.string.font_size_small.compose
      REGULAR -> Res.string.font_size_regular_default.compose
      LARGE -> Res.string.font_size_large.compose
      LARGEST -> Res.string.font_size_largest.compose
    }
}

@Serializable
enum class ColorContrastLevel(
  val icon: ImageVector,
  val doubleValue: Double
) {
  MINIMUM(
    Icons.Rounded.BrightnessLow,
    Contrast.Reduced.value
  ),

  @SerialName("reduced")
  REDUCED(
    Icons.Rounded.BrightnessLow,
    -0.5,
  ),

  @SerialName("normal")
  NORMAL(
    Icons.Rounded.BrightnessMedium,
    Contrast.Default.value
  ),

  @SerialName("increased")
  INCREASED(
    Icons.Rounded.BrightnessHigh,
    Contrast.Medium.value
  ),

  @SerialName("maximum")
  MAXIMUM(
    Icons.Rounded.BrightnessHigh,
    Contrast.High.value
  );

  val displayName: String
    @Composable get() = when (this) {
      MINIMUM -> Res.string.color_contrast_level_minimum.compose
      REDUCED -> Res.string.color_contrast_level_reduced.compose
      NORMAL -> Res.string.color_contrast_level_normal_default.compose
      INCREASED -> Res.string.color_contrast_level_increased.compose
      MAXIMUM -> Res.string.color_contrast_level_maximum.compose
    }
}

@Suppress("ClassName")
@Serializable
enum class OLD_ScreenTransitionType {
  @SerialName("zoom")
  ZOOM,

  @SerialName("fade")
  FADE,

  @SerialName("none")
  NONE;
}

@OptIn(ExperimentalDecomposeApi::class)
@Serializable
enum class ScreenTransitionType(val icon: ImageVector) {
  @SerialName("native")
  NATIVE(Icons.Rounded.Swipe),

  @SerialName("zoom")
  ZOOM(Icons.Rounded.ZoomOutMap),

  @SerialName("fade")
  FADE(Icons.Rounded.BlurOn),

  @SerialName("none")
  NONE(Icons.Rounded.CheckBoxOutlineBlank);

  val displayName: String
    @Composable get() = when (this) {
      NATIVE -> Res.string.screen_transition_native_default.compose
      ZOOM -> Res.string.screen_transition_zoom.compose
      FADE -> Res.string.screen_transition_fade.compose
      NONE -> Res.string.screen_transition_none.compose
    }

  val animator: StackAnimator?
    get() = when (this) {
      NATIVE, ZOOM -> ZOOM_ANIMATOR
      FADE -> FADE_ANIMATOR
      NONE -> null
    }

  companion object {
    val FADE_ANIMATOR = fade(tween(200))
    val ZOOM_ANIMATOR = FADE_ANIMATOR + scale(tween(200), frontFactor = 1.05f, backFactor = 0.95f)
  }
}

@Serializable
enum class SpotlightLongPressTimeout(val timeoutMillis: Long) {
  @SerialName("shorter")
  SHORTER(500L),
  @SerialName("short")
  SHORT(750L),
  @SerialName("medium")
  MEDIUM(1000L),
  @SerialName("long")
  LONG(1500L),
  @SerialName("longer")
  LONGER(2000L);

  val displayName: String
    @Composable get() = when (this) {
      SHORTER -> Res.string.spotlight_long_press_timeout_shorter.compose
      SHORT -> Res.string.spotlight_long_press_timeout_short.compose
      MEDIUM -> Res.string.spotlight_long_press_timeout_medium.compose
      LONG -> Res.string.spotlight_long_press_timeout_long.compose
      LONGER -> Res.string.spotlight_long_press_timeout_longer.compose
    }
}

@Serializable
enum class FontChoice(
  val icon: ImageVector,
  val fontSizeScalar: Float = 1f,
  val ignoreTracking: Boolean = false
) {
  @SerialName("default")
  DEFAULT(
    Icons.Rounded.TextFields
  ),

  @SerialName("lexie_readable")
  LEXIE_READABLE(
    Icons.Rounded.Accessibility
  ),

  @SerialName("open_dyslexic")
  OPEN_DYSLEXIC(
    Icons.Rounded.Accessibility,
    0.8f,
    true
  );

  val displayName: String
    @Composable get() = when (this) {
      DEFAULT -> Res.string.font_choice_default.compose
      LEXIE_READABLE -> Res.string.font_choice_lexie_readable.compose
      OPEN_DYSLEXIC -> Res.string.font_choice_open_dyslexic.compose
    }

  val headingFont: FontFamily
    @Composable get() = when (this) {
      DEFAULT -> Fonts.ubuntu
      LEXIE_READABLE -> Fonts.lexieReadable
      OPEN_DYSLEXIC -> Fonts.openDyslexic
    }

  val bodyFont: FontFamily
    @Composable get() = when (this) {
      DEFAULT -> FontFamily.SansSerif
      LEXIE_READABLE -> Fonts.lexieReadable
      OPEN_DYSLEXIC -> Fonts.openDyslexic
    }
}

@Serializable
data class Settings(
  val token: String? = null,
  // TODO: Hoist this property to Android-specific logic
  @SerialName("encrypted_encryption_key")
  val encryptedEncryptionKey: String? = null,
  // @SerialName("user_id")
  // val cachedID: String? = null,

  @SerialName("token_is_protected")
  val tokenIsProtected: Boolean = false,
  @SerialName("stealth_mode_enabled")
  val stealthModeEnabled: Boolean = false,

  @SerialName("color_mode")
  val colorMode: ColorMode = ColorMode.SYSTEM,
  @SerialName("theme_color")
  val themeColor: ThemeColor = ThemeColor.BLISSFUL_BLUE,
  @SerialName("dynamic_color_type")
  val dynamicColorType: DynamicColorType = DynamicColorType.NORMAL,
  @SerialName("amoled_mode")
  val amoledMode: Boolean = false,

  @SerialName("corner_style")
  val cornerStyle: CornerStyle = CornerStyle.ROUNDED,
  @SerialName("change_front_mode")
  val changeFrontMode: ChangeFrontMode = ChangeFrontMode.SWIPE,
  @SerialName("use_small_avatars")
  val useSmallAvatars: Boolean = false,
  @SerialName("show_alter_ids")
  val showAlterIds: Boolean = true,
  @Suppress("PropertyName") @SerialName("hide_navigation_bar")
  val UNUSED_hideNavigationBarOnScroll: Boolean = true,
  @SerialName("use_tablet_layout")
  val useTabletLayout: Boolean = true,
  @SerialName("hide_alters_in_tags")
  val hideAltersInTags: Boolean = false,

  @SerialName("show_help_fab")
  val showHelpFAB: Boolean = false,
  @SerialName("show_permanent_tips")
  val showPermanentTips: Boolean = false,
  // @SerialName("use_dyslexic_font")
  // val useDyslexicFont: Boolean = false,

  @SerialName("font_choice")
  val fontChoice: FontChoice = FontChoice.DEFAULT,

  @SerialName("font_size")
  val fontSizeScalar: FontSizeScalar = FontSizeScalar.REGULAR,
  @SerialName("color_contrast_level")
  val colorContrastLevel: ColorContrastLevel = ColorContrastLevel.NORMAL,
  @SerialName("reduce_motion")
  val reduceMotion: Boolean = false,
  @Suppress("PropertyName") @SerialName("screen_transition_type")
  val OLDScreenTransitionType: OLD_ScreenTransitionType = OLD_ScreenTransitionType.ZOOM,

  @SerialName("screen_transition_type_new")
  val screenTransitionType: ScreenTransitionType = ScreenTransitionType.NATIVE,

  @SerialName("spotlight_enabled")
  val spotlightEnabled: Boolean = true,
  @SerialName("spotlight_long_press_timeout")
  val spotlightLongPressTimeout: SpotlightLongPressTimeout = SpotlightLongPressTimeout.MEDIUM,

  @SerialName("quick_exit_enabled")
  val quickExitEnabled: Boolean = false,

  @SerialName("show_push_notifications")
  val showPushNotifications: Boolean = false,

  // Silent settings: isn't shown in settings list, but updates automatically with user's actions
  @SerialName("alter_sorting_method")
  val alterSortingMethod: AlterSortingMethod = AlterSortingMethod.ALPHABETICAL,
  @SerialName("tags_collapsed")
  val tagsCollapsed: Boolean = false,
  @SerialName("has_viewed_onboarding")
  val hasViewedOnboarding: Boolean = false,

  @SerialName("is_singlet")
  val isSinglet: Boolean = false
) {
  fun serialize() = globalSerializer.encodeToString(this)

  companion object {
    fun deserialize(json: String) = globalSerializer.decodeFromString<Settings>(json)
  }
}