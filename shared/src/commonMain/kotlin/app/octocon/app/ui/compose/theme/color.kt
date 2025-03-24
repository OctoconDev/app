package app.octocon.app.ui.compose.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import app.octocon.app.ColorContrastLevel
import app.octocon.app.ColorMode
import app.octocon.app.DynamicColorType
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamicColorScheme

sealed class Theme {
  abstract val seed: Color
  open val tertiarySeed: Color? = null

  @Composable
  fun colorSchemeProvider(
    colorMode: ColorMode,
    dynamicColorType: DynamicColorType,
    colorContrastLevel: ColorContrastLevel,
    isAmoled: Boolean
  ) = colorSchemeProvider(
    colorMode,
    dynamicColorType,
    colorContrastLevel,
    isAmoled,
    colorMode.shouldUseDarkTheme()
  )

  open fun colorSchemeProvider(
    colorMode: ColorMode,
    dynamicColorType: DynamicColorType,
    colorContrastLevel: ColorContrastLevel,
    isAmoled: Boolean,
    isDark: Boolean
  ): ColorScheme =
    dynamicColorScheme(
      primary = this.seed,
      tertiary = this.tertiarySeed,
      isDark = isDark,
      isAmoled = isAmoled,
      contrastLevel = colorContrastLevel.doubleValue,
      style = dynamicColorType.paletteStyle ?: PaletteStyle.TonalSpot
    )
}

data object BlissfulBlueTheme : Theme() {
  override val seed: Color by lazy { Color(0xFF5156A9) }
  override val tertiarySeed: Color? by lazy { Color(0xFFE567DD) }
}

data object PlayfulPinkTheme : Theme() {
  override val seed: Color by lazy { Color(0xFFFF36B0) }
  override val tertiarySeed: Color? by lazy { Color(0xFF7698ED) }
}

data object VibrantVioletTheme : Theme() {
  override val seed: Color by lazy { Color(0xFF842EE4) }
  override val tertiarySeed: Color? by lazy { Color(0xED76E5) }
}

data object OctoOrangeTheme : Theme() {
  override val seed: Color by lazy { Color(0xFFfA6900) }
  override val tertiarySeed: Color? by lazy { Color(0xFFE3ED76) }
}

data object RadiantRedTheme : Theme() {
  override val seed: Color by lazy { Color(0xFFC9000B) }
  override val tertiarySeed: Color? by lazy { Color(0xFFEDDB76) }
}

data object GleefulGreenTheme : Theme() {
  override val seed: Color by lazy { Color(0xFF00924A) }
  override val tertiarySeed: Color? by lazy { Color(0x76EDD1) }
}

data object TranquilTealTheme : Theme() {
  override val seed: Color by lazy { Color(0xFF00C9B4) }
  override val tertiarySeed: Color? by lazy { Color(0x7EED76) }
}

data object MonochromeTheme : Theme() {
  override val seed: Color by lazy { Color(0xFF000000) }

  override fun colorSchemeProvider(
    colorMode: ColorMode,
    dynamicColorType: DynamicColorType,
    colorContrastLevel: ColorContrastLevel,
    isAmoled: Boolean,
    isDark: Boolean
  ) =
    dynamicColorScheme(
      seed,
      isDark = isDark,
      isAmoled = isAmoled,
      contrastLevel = colorContrastLevel.doubleValue,
      style = PaletteStyle.Monochrome
    )

  fun getBareThemes(
    colorContrastLevel: ColorContrastLevel,
    isAmoled: Boolean
  ): Pair<ColorScheme, ColorScheme> {
    return Pair(
      dynamicColorScheme(
        seed,
        isDark = false,
        isAmoled = isAmoled,
        contrastLevel = colorContrastLevel.doubleValue,
        style = PaletteStyle.Monochrome
      ),
      dynamicColorScheme(
        seed,
        isDark = true,
        isAmoled = isAmoled,
        contrastLevel = colorContrastLevel.doubleValue,
        style = PaletteStyle.Monochrome
      )
    )
  }
}

@Composable
fun Color.animate(animationSpec: AnimationSpec<Color>): Color {
  return animateColorAsState(this, animationSpec).value
}