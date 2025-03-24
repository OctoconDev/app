package app.octocon.app.ui.compose.theme

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.octocon.app.ColorContrastLevel
import app.octocon.app.ColorMode
import app.octocon.app.CornerStyle
import app.octocon.app.DynamicColorType
import app.octocon.app.FontChoice
import app.octocon.app.FontSizeScalar
import app.octocon.app.ThemeColor
import app.octocon.app.utils.isGrayscale
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamicColorScheme

private object OctoTypefaceTokens {
  val Plain = FontFamily.SansSerif
  val WeightBold = FontWeight.Bold
  val WeightMedium = FontWeight.Medium
  val WeightRegular = FontWeight.Normal
}

@Suppress("PropertyName")
@Immutable
private interface TypeScaleTokens {
  val BodyLargeFont: FontFamily
  val BodyLargeLineHeight: TextUnit
  val BodyLargeSize: TextUnit
  val BodyLargeTracking: TextUnit
  val BodyLargeWeight: FontWeight
  val BodyMediumFont: FontFamily
  val BodyMediumLineHeight: TextUnit
  val BodyMediumSize: TextUnit
  val BodyMediumTracking: TextUnit
  val BodyMediumWeight: FontWeight
  val BodySmallFont: FontFamily
  val BodySmallLineHeight: TextUnit
  val BodySmallSize: TextUnit
  val BodySmallTracking: TextUnit
  val BodySmallWeight: FontWeight
  val DisplayLargeFont: FontFamily
  val DisplayLargeLineHeight: TextUnit
  val DisplayLargeSize: TextUnit
  val DisplayLargeTracking: TextUnit
  val DisplayLargeWeight: FontWeight
  val DisplayMediumFont: FontFamily
  val DisplayMediumLineHeight: TextUnit
  val DisplayMediumSize: TextUnit
  val DisplayMediumTracking: TextUnit
  val DisplayMediumWeight: FontWeight
  val DisplaySmallFont: FontFamily
  val DisplaySmallLineHeight: TextUnit
  val DisplaySmallSize: TextUnit
  val DisplaySmallTracking: TextUnit
  val DisplaySmallWeight: FontWeight
  val HeadlineLargeFont: FontFamily
  val HeadlineLargeLineHeight: TextUnit
  val HeadlineLargeSize: TextUnit
  val HeadlineLargeTracking: TextUnit
  val HeadlineLargeWeight: FontWeight
  val HeadlineMediumFont: FontFamily
  val HeadlineMediumLineHeight: TextUnit
  val HeadlineMediumSize: TextUnit
  val HeadlineMediumTracking: TextUnit
  val HeadlineMediumWeight: FontWeight
  val HeadlineSmallFont: FontFamily
  val HeadlineSmallLineHeight: TextUnit
  val HeadlineSmallSize: TextUnit
  val HeadlineSmallTracking: TextUnit
  val HeadlineSmallWeight: FontWeight
  val LabelLargeFont: FontFamily
  val LabelLargeLineHeight: TextUnit
  val LabelLargeSize: TextUnit
  val LabelLargeTracking: TextUnit
  val LabelLargeWeight: FontWeight
  val LabelMediumFont: FontFamily
  val LabelMediumLineHeight: TextUnit
  val LabelMediumSize: TextUnit
  val LabelMediumTracking: TextUnit
  val LabelMediumWeight: FontWeight
  val LabelSmallFont: FontFamily
  val LabelSmallLineHeight: TextUnit
  val LabelSmallSize: TextUnit
  val LabelSmallTracking: TextUnit
  val LabelSmallWeight: FontWeight
  val TitleLargeFont: FontFamily
  val TitleLargeLineHeight: TextUnit
  val TitleLargeSize: TextUnit
  val TitleLargeTracking: TextUnit
  val TitleLargeWeight: FontWeight
  val TitleMediumFont: FontFamily
  val TitleMediumLineHeight: TextUnit
  val TitleMediumSize: TextUnit
  val TitleMediumTracking: TextUnit
  val TitleMediumWeight: FontWeight
  val TitleSmallFont: FontFamily
  val TitleSmallLineHeight: TextUnit
  val TitleSmallSize: TextUnit
  val TitleSmallTracking: TextUnit
  val TitleSmallWeight: FontWeight
}

@Immutable
private class BaseTypeScaleTokens(
  bodyFont: FontFamily,
  headingFont: FontFamily,
  fontSizeScalar: Float,
  ignoreTracking: Boolean = false
) : TypeScaleTokens {
  override val BodyLargeFont = bodyFont
  override val BodyLargeLineHeight = 24.0.sp
  override val BodyLargeSize = 16.sp * fontSizeScalar
  override val BodyLargeTracking = if (ignoreTracking) 0.sp else 0.5.sp
  override val BodyLargeWeight = OctoTypefaceTokens.WeightRegular
  override val BodyMediumLineHeight = 20.0.sp
  override val BodyMediumFont = bodyFont
  override val BodyMediumSize = 14.sp * fontSizeScalar
  override val BodyMediumTracking = if (ignoreTracking) 0.sp else 0.2.sp
  override val BodyMediumWeight = OctoTypefaceTokens.WeightRegular
  override val BodySmallFont = bodyFont
  override val BodySmallLineHeight = 16.0.sp
  override val BodySmallSize = 12.sp * fontSizeScalar
  override val BodySmallTracking = if (ignoreTracking) 0.sp else 0.4.sp
  override val BodySmallWeight = OctoTypefaceTokens.WeightRegular
  override val DisplayLargeFont = headingFont
  override val DisplayLargeLineHeight = 64.0.sp
  override val DisplayLargeSize = 57.sp
  override val DisplayLargeTracking = if (ignoreTracking) 0.sp else (-0.2).sp
  override val DisplayLargeWeight = OctoTypefaceTokens.WeightMedium
  override val DisplayMediumFont = headingFont
  override val DisplayMediumLineHeight = 52.0.sp
  override val DisplayMediumSize = 45.sp * fontSizeScalar
  override val DisplayMediumTracking = 0.sp
  override val DisplayMediumWeight = OctoTypefaceTokens.WeightMedium
  override val DisplaySmallFont = headingFont
  override val DisplaySmallLineHeight = 44.0.sp
  override val DisplaySmallSize = 36.sp * fontSizeScalar
  override val DisplaySmallTracking = 0.sp
  override val DisplaySmallWeight = OctoTypefaceTokens.WeightMedium
  override val HeadlineLargeFont = headingFont
  override val HeadlineLargeLineHeight = 40.0.sp
  override val HeadlineLargeSize = 32.sp * fontSizeScalar
  override val HeadlineLargeTracking = 0.sp
  override val HeadlineLargeWeight = OctoTypefaceTokens.WeightMedium
  override val HeadlineMediumFont = headingFont
  override val HeadlineMediumLineHeight = 36.0.sp
  override val HeadlineMediumSize = 28.sp * fontSizeScalar
  override val HeadlineMediumTracking = 0.0.sp
  override val HeadlineMediumWeight = OctoTypefaceTokens.WeightMedium
  override val HeadlineSmallFont = headingFont
  override val HeadlineSmallLineHeight = 32.0.sp
  override val HeadlineSmallSize = 24.sp * fontSizeScalar
  override val HeadlineSmallTracking = 0.sp
  override val HeadlineSmallWeight = OctoTypefaceTokens.WeightMedium
  override val LabelLargeFont = bodyFont
  override val LabelLargeLineHeight = 20.0.sp
  override val LabelLargeSize = 14.sp * fontSizeScalar
  override val LabelLargeTracking = if (ignoreTracking) 0.sp else 0.1.sp
  override val LabelLargeWeight = OctoTypefaceTokens.WeightMedium
  override val LabelMediumFont = bodyFont
  override val LabelMediumLineHeight = 16.0.sp
  override val LabelMediumSize = 12.sp * fontSizeScalar
  override val LabelMediumTracking = if (ignoreTracking) 0.sp else 0.5.sp
  override val LabelMediumWeight = OctoTypefaceTokens.WeightMedium
  override val LabelSmallFont = bodyFont
  override val LabelSmallLineHeight = 16.0.sp
  override val LabelSmallSize = 11.sp * fontSizeScalar
  override val LabelSmallTracking = if (ignoreTracking) 0.sp else 0.5.sp
  override val LabelSmallWeight = OctoTypefaceTokens.WeightMedium
  override val TitleLargeFont = headingFont
  override val TitleLargeLineHeight = 28.0.sp
  override val TitleLargeSize = 22.sp * fontSizeScalar
  override val TitleLargeTracking = 0.sp
  override val TitleLargeWeight = OctoTypefaceTokens.WeightMedium
  override val TitleMediumFont = bodyFont
  override val TitleMediumLineHeight = 24.0.sp
  override val TitleMediumSize = 16.sp * fontSizeScalar
  override val TitleMediumTracking = if (ignoreTracking) 0.sp else 0.2.sp
  override val TitleMediumWeight = OctoTypefaceTokens.WeightMedium
  override val TitleSmallFont = bodyFont
  override val TitleSmallLineHeight = 20.0.sp
  override val TitleSmallSize = 14.sp * fontSizeScalar
  override val TitleSmallTracking = if (ignoreTracking) 0.sp else 0.1.sp
  override val TitleSmallWeight = OctoTypefaceTokens.WeightMedium
}

private val DefaultTextStyle = TextStyle.Default

@Composable
fun getSubsectionStyle(fontSizeScalar: FontSizeScalar): TextStyle {
  return MaterialTheme.typography.headlineSmall.merge(
    fontSize = 20.sp * fontSizeScalar.scalar,
    fontWeight = FontWeight.Normal
  )
}

@Suppress("PropertyName")
interface TypographyTokens {
  val BodyLarge: TextStyle
  val BodyMedium: TextStyle
  val BodySmall: TextStyle
  val DisplayLarge: TextStyle
  val DisplayMedium: TextStyle
  val DisplaySmall: TextStyle
  val HeadlineLarge: TextStyle
  val HeadlineMedium: TextStyle
  val HeadlineSmall: TextStyle
  val LabelLarge: TextStyle
  val LabelMedium: TextStyle
  val LabelSmall: TextStyle
  val TitleLarge: TextStyle
  val TitleMedium: TextStyle
  val TitleSmall: TextStyle
}


private class OctoTypographyTokens(
  typeScaleTokens: TypeScaleTokens,
) : TypographyTokens {
  override val BodyLarge: TextStyle = DefaultTextStyle.copy(
    fontFamily = typeScaleTokens.BodyLargeFont,
    fontWeight = typeScaleTokens.BodyLargeWeight,
    fontSize = typeScaleTokens.BodyLargeSize,
    lineHeight = typeScaleTokens.BodyLargeLineHeight,
    letterSpacing = typeScaleTokens.BodyLargeTracking,
  )
  override val BodyMedium: TextStyle = DefaultTextStyle.copy(
    fontFamily = typeScaleTokens.BodyMediumFont,
    fontWeight = typeScaleTokens.BodyMediumWeight,
    fontSize = typeScaleTokens.BodyMediumSize,
    lineHeight = typeScaleTokens.BodyMediumLineHeight,
    letterSpacing = typeScaleTokens.BodyMediumTracking,
  )
  override val BodySmall: TextStyle = DefaultTextStyle.copy(
    fontFamily = typeScaleTokens.BodySmallFont,
    fontWeight = typeScaleTokens.BodySmallWeight,
    fontSize = typeScaleTokens.BodySmallSize,
    lineHeight = typeScaleTokens.BodySmallLineHeight,
    letterSpacing = typeScaleTokens.BodySmallTracking,
  )
  override val DisplayLarge: TextStyle = DefaultTextStyle.copy(
    fontFamily = typeScaleTokens.DisplayLargeFont,
    fontWeight = typeScaleTokens.DisplayLargeWeight,
    fontSize = typeScaleTokens.DisplayLargeSize,
    lineHeight = typeScaleTokens.DisplayLargeLineHeight,
    letterSpacing = typeScaleTokens.DisplayLargeTracking,
  )
  override val DisplayMedium: TextStyle = DefaultTextStyle.copy(
    fontFamily = typeScaleTokens.DisplayMediumFont,
    fontWeight = typeScaleTokens.DisplayMediumWeight,
    fontSize = typeScaleTokens.DisplayMediumSize,
    lineHeight = typeScaleTokens.DisplayMediumLineHeight,
    letterSpacing = typeScaleTokens.DisplayMediumTracking,
  )
  override val DisplaySmall: TextStyle = DefaultTextStyle.copy(
    fontFamily = typeScaleTokens.DisplaySmallFont,
    fontWeight = typeScaleTokens.DisplaySmallWeight,
    fontSize = typeScaleTokens.DisplaySmallSize,
    lineHeight = typeScaleTokens.DisplaySmallLineHeight,
    letterSpacing = typeScaleTokens.DisplaySmallTracking,
  )
  override val HeadlineLarge: TextStyle = DefaultTextStyle.copy(
    fontFamily = typeScaleTokens.HeadlineLargeFont,
    fontWeight = typeScaleTokens.HeadlineLargeWeight,
    fontSize = typeScaleTokens.HeadlineLargeSize,
    lineHeight = typeScaleTokens.HeadlineLargeLineHeight,
    letterSpacing = typeScaleTokens.HeadlineLargeTracking,
  )
  override val HeadlineMedium: TextStyle = DefaultTextStyle.copy(
    fontFamily = typeScaleTokens.HeadlineMediumFont,
    fontWeight = typeScaleTokens.HeadlineMediumWeight,
    fontSize = typeScaleTokens.HeadlineMediumSize,
    lineHeight = typeScaleTokens.HeadlineMediumLineHeight,
    letterSpacing = typeScaleTokens.HeadlineMediumTracking,
  )
  override val HeadlineSmall: TextStyle = DefaultTextStyle.copy(
    fontFamily = typeScaleTokens.HeadlineSmallFont,
    fontWeight = typeScaleTokens.HeadlineSmallWeight,
    fontSize = typeScaleTokens.HeadlineSmallSize,
    lineHeight = typeScaleTokens.HeadlineSmallLineHeight,
    letterSpacing = typeScaleTokens.HeadlineSmallTracking,
  )
  override val LabelLarge: TextStyle = DefaultTextStyle.copy(
    fontFamily = typeScaleTokens.LabelLargeFont,
    fontWeight = typeScaleTokens.LabelLargeWeight,
    fontSize = typeScaleTokens.LabelLargeSize,
    lineHeight = typeScaleTokens.LabelLargeLineHeight,
    letterSpacing = typeScaleTokens.LabelLargeTracking,
  )
  override val LabelMedium: TextStyle = DefaultTextStyle.copy(
    fontFamily = typeScaleTokens.LabelMediumFont,
    fontWeight = typeScaleTokens.LabelMediumWeight,
    fontSize = typeScaleTokens.LabelMediumSize,
    lineHeight = typeScaleTokens.LabelMediumLineHeight,
    letterSpacing = typeScaleTokens.LabelMediumTracking,
  )
  override val LabelSmall: TextStyle = DefaultTextStyle.copy(
    fontFamily = typeScaleTokens.LabelSmallFont,
    fontWeight = typeScaleTokens.LabelSmallWeight,
    fontSize = typeScaleTokens.LabelSmallSize,
    lineHeight = typeScaleTokens.LabelSmallLineHeight,
    letterSpacing = typeScaleTokens.LabelSmallTracking,
  )
  override val TitleLarge: TextStyle = DefaultTextStyle.copy(
    fontFamily = typeScaleTokens.TitleLargeFont,
    fontWeight = typeScaleTokens.TitleLargeWeight,
    fontSize = typeScaleTokens.TitleLargeSize,
    lineHeight = typeScaleTokens.TitleLargeLineHeight,
    letterSpacing = typeScaleTokens.TitleLargeTracking,
  )
  override val TitleMedium: TextStyle = DefaultTextStyle.copy(
    fontFamily = typeScaleTokens.TitleMediumFont,
    fontWeight = typeScaleTokens.TitleMediumWeight,
    fontSize = typeScaleTokens.TitleMediumSize,
    lineHeight = typeScaleTokens.TitleMediumLineHeight,
    letterSpacing = typeScaleTokens.TitleMediumTracking,
  )
  override val TitleSmall: TextStyle = DefaultTextStyle.copy(
    fontFamily = typeScaleTokens.TitleSmallFont,
    fontWeight = typeScaleTokens.TitleSmallWeight,
    fontSize = typeScaleTokens.TitleSmallSize,
    lineHeight = typeScaleTokens.TitleSmallLineHeight,
    letterSpacing = typeScaleTokens.TitleSmallTracking,
  )
}

@Composable
fun generateTypography(fontChoice: FontChoice, fontSizeScalar: FontSizeScalar): Typography {
  val typeScaleTokens = BaseTypeScaleTokens(
    headingFont = fontChoice.headingFont,
    bodyFont = fontChoice.bodyFont,
    fontSizeScalar = fontSizeScalar.scalar * fontChoice.fontSizeScalar,
    ignoreTracking = fontChoice.ignoreTracking
  )

  val tokens = OctoTypographyTokens(typeScaleTokens)

  return Typography(
    bodyLarge = tokens.BodyLarge,
    bodyMedium = tokens.BodyMedium,
    bodySmall = tokens.BodySmall,
    displayLarge = tokens.DisplayLarge,
    displayMedium = tokens.DisplayMedium,
    displaySmall = tokens.DisplaySmall,
    headlineLarge = tokens.HeadlineLarge,
    headlineMedium = tokens.HeadlineMedium,
    headlineSmall = tokens.HeadlineSmall,
    labelLarge = tokens.LabelLarge,
    labelMedium = tokens.LabelMedium,
    labelSmall = tokens.LabelSmall,
    titleLarge = tokens.TitleLarge,
    titleMedium = tokens.TitleMedium,
    titleSmall = tokens.TitleSmall
  )
}

val LocalOctoTypography = staticCompositionLocalOf<Typography> {
  error("No OctoTypography provided")
}

val LocalOctoShapes = staticCompositionLocalOf<Shapes> {
  error("No OctoShapes provided")
}

@Composable
fun OctoconTheme(
  fontChoice: FontChoice,
  fontSizeScalar: FontSizeScalar,
  cornerStyle: CornerStyle,
  themeColor: ThemeColor,
  colorMode: ColorMode,
  dynamicColorType: DynamicColorType,
  colorContrastLevel: ColorContrastLevel,
  amoledMode: Boolean,
  content: @Composable () -> Unit
) {
  val typography = generateTypography(fontChoice, fontSizeScalar)

  val shapes = remember(cornerStyle) { generateShapes(cornerStyle) }

  val colorScheme = themeColor.themeColors.colorSchemeProvider(
    colorMode,
    dynamicColorType,
    colorContrastLevel = colorContrastLevel,
    amoledMode
  )

  val animationSpec: AnimationSpec<Color> = spring(stiffness = Spring.StiffnessLow)

  val scheme = colorScheme.copy(
    primary = colorScheme.primary.animate(animationSpec),
    primaryContainer = colorScheme.primaryContainer.animate(animationSpec),
    secondary = colorScheme.secondary.animate(animationSpec),
    secondaryContainer = colorScheme.secondaryContainer.animate(animationSpec),
    tertiary = colorScheme.tertiary.animate(animationSpec),
    tertiaryContainer = colorScheme.tertiaryContainer.animate(animationSpec),
    background = colorScheme.background.animate(animationSpec),
    surface = colorScheme.surface.animate(animationSpec),
    surfaceTint = colorScheme.surfaceTint.animate(animationSpec),
    surfaceBright = colorScheme.surfaceBright.animate(animationSpec),
    surfaceDim = colorScheme.surfaceDim.animate(animationSpec),
    surfaceContainer = colorScheme.surfaceContainer.animate(animationSpec),
    surfaceContainerHigh = colorScheme.surfaceContainerHigh.animate(animationSpec),
    surfaceContainerHighest = colorScheme.surfaceContainerHighest.animate(animationSpec),
    surfaceContainerLow = colorScheme.surfaceContainerLow.animate(animationSpec),
    surfaceContainerLowest = colorScheme.surfaceContainerLowest.animate(animationSpec),
    surfaceVariant = colorScheme.surfaceVariant.animate(animationSpec),
    error = colorScheme.error.animate(animationSpec),
    errorContainer = colorScheme.errorContainer.animate(animationSpec),
    onPrimary = colorScheme.onPrimary.animate(animationSpec),
    onPrimaryContainer = colorScheme.onPrimaryContainer.animate(animationSpec),
    onSecondary = colorScheme.onSecondary.animate(animationSpec),
    onSecondaryContainer = colorScheme.onSecondaryContainer.animate(animationSpec),
    onTertiary = colorScheme.onTertiary.animate(animationSpec),
    onTertiaryContainer = colorScheme.onTertiaryContainer.animate(animationSpec),
    onBackground = colorScheme.onBackground.animate(animationSpec),
    onSurface = colorScheme.onSurface.animate(animationSpec),
    onSurfaceVariant = colorScheme.onSurfaceVariant.animate(animationSpec),
    onError = colorScheme.onError.animate(animationSpec),
    onErrorContainer = colorScheme.onErrorContainer.animate(animationSpec),
    inversePrimary = colorScheme.inversePrimary.animate(animationSpec),
    inverseSurface = colorScheme.inverseSurface.animate(animationSpec),
    inverseOnSurface = colorScheme.inverseOnSurface.animate(animationSpec),
    outline = colorScheme.outline.animate(animationSpec),
    outlineVariant = colorScheme.outlineVariant.animate(animationSpec),
    scrim = colorScheme.scrim.animate(animationSpec)
  )

  CompositionLocalProvider(
    LocalOctoTypography provides typography,
    LocalOctoShapes provides shapes
  ) {
    MaterialTheme(
      colorScheme = scheme,
      /*if (colorMode.shouldUseDarkTheme())
        themeColor.themeColors.darkColors
      else themeColor.themeColors.lightColors,*/
      /*dynamicColorScheme(
        Color(0xFFFF36B0),
        isDark = colorMode.shouldUseDarkTheme(),
        isAmoled = true
      ),*/
      typography = typography,
      shapes = shapes,
      content = content
    )
  }
}

fun generateShapes(cornerStyle: CornerStyle) =
  when (cornerStyle) {
    CornerStyle.SQUARE -> Shapes(
      extraSmall = RoundedCornerShape(0.dp),
      small = RoundedCornerShape(0.dp),
      medium = RoundedCornerShape(0.dp),
      large = RoundedCornerShape(0.dp),
      extraLarge = RoundedCornerShape(0.dp)
    )

    CornerStyle.ROUNDED -> Shapes()
  }

@Composable
fun squareifyShape(
  cornerStyle: CornerStyle,
  shapeGenerator: () -> RoundedCornerShape
): RoundedCornerShape {
  return when (cornerStyle) {
    CornerStyle.SQUARE -> RoundedCornerShape(0.dp)
    CornerStyle.ROUNDED -> shapeGenerator()
  }
}

object SchemeCache {
  private val cache = mutableMapOf<String, Pair<ColorScheme, ColorScheme>>()

  private fun generateColorScheme(
    color: String,
    dynamicColorType: DynamicColorType,
    colorContrastLevel: ColorContrastLevel,
    isAmoled: Boolean,
    paletteStyle: PaletteStyle = dynamicColorType.paletteStyle ?: PaletteStyle.TonalSpot
  ): Pair<ColorScheme, ColorScheme> {
    val intColor = hexStringToARGBInt(color)
    return Pair(
      dynamicColorScheme(
        Color(intColor),
        isDark = false,
        isAmoled = isAmoled,
        contrastLevel = colorContrastLevel.doubleValue,
        style = paletteStyle
      ),
      dynamicColorScheme(
        Color(intColor),
        isDark = true,
        isAmoled = isAmoled,
        contrastLevel = colorContrastLevel.doubleValue,
        style = paletteStyle
      )
    )
  }

  fun get(
    color: String,
    shouldCache: Boolean,
    dynamicColorType: DynamicColorType,
    colorContrastLevel: ColorContrastLevel,
    isAmoled: Boolean
  ): Pair<ColorScheme, ColorScheme> {
    return if (!shouldCache) {
      generateColorScheme(color, dynamicColorType, colorContrastLevel, isAmoled)
    } else {
      cache.getOrPut(color + dynamicColorType.ordinal + colorContrastLevel.ordinal + (if (isAmoled) 1 else 0)) {
        generateColorScheme(color, dynamicColorType, colorContrastLevel, isAmoled)
      }
    }
  }

  fun getMonochrome(
    colorContrastLevel: ColorContrastLevel,
    isAmoled: Boolean
  ): Pair<ColorScheme, ColorScheme> =
    cache.getOrPut("monochrome" + colorContrastLevel.ordinal + (if (isAmoled) 1 else 0)) {
      MonochromeTheme.getBareThemes(colorContrastLevel, isAmoled)
    }
}

@Composable
fun ThemeFromColor(
  color: String?,
  colorMode: ColorMode,
  dynamicColorType: DynamicColorType,
  colorContrastLevel: ColorContrastLevel,
  amoledMode: Boolean,
  shouldCache: Boolean = true,
  content: @Composable () -> Unit
) {
  val typography = LocalOctoTypography.current
  val shapes = LocalOctoShapes.current

  val colorScheme = when {
    color == null || dynamicColorType == DynamicColorType.NONE -> MaterialTheme.colorScheme
    /*isGrayscale(color) -> {
      if (colorMode.shouldUseDarkTheme()) MonochromeTheme.darkColors else MonochromeTheme.lightColors
    }*/
    isGrayscale(color) -> {
      val schemes = SchemeCache.getMonochrome(colorContrastLevel, amoledMode)
      if (colorMode.shouldUseDarkTheme()) schemes.second else schemes.first
    }

    else -> {
      val schemes =
        SchemeCache.get(color, shouldCache, dynamicColorType, colorContrastLevel, amoledMode)
      if (colorMode.shouldUseDarkTheme()) schemes.second else schemes.first
    }
  }

  val animationSpec = spring<Color>(stiffness = Spring.StiffnessLow)

  val scheme = colorScheme.copy(
    primary = colorScheme.primary.animate(animationSpec),
    primaryContainer = colorScheme.primaryContainer.animate(animationSpec),
    secondary = colorScheme.secondary.animate(animationSpec),
    secondaryContainer = colorScheme.secondaryContainer.animate(animationSpec),
    tertiary = colorScheme.tertiary.animate(animationSpec),
    tertiaryContainer = colorScheme.tertiaryContainer.animate(animationSpec),
    background = colorScheme.background.animate(animationSpec),
    surface = colorScheme.surface.animate(animationSpec),
    surfaceTint = colorScheme.surfaceTint.animate(animationSpec),
    surfaceBright = colorScheme.surfaceBright.animate(animationSpec),
    surfaceDim = colorScheme.surfaceDim.animate(animationSpec),
    surfaceContainer = colorScheme.surfaceContainer.animate(animationSpec),
    surfaceContainerHigh = colorScheme.surfaceContainerHigh.animate(animationSpec),
    surfaceContainerHighest = colorScheme.surfaceContainerHighest.animate(animationSpec),
    surfaceContainerLow = colorScheme.surfaceContainerLow.animate(animationSpec),
    surfaceContainerLowest = colorScheme.surfaceContainerLowest.animate(animationSpec),
    surfaceVariant = colorScheme.surfaceVariant.animate(animationSpec),
    error = colorScheme.error.animate(animationSpec),
    errorContainer = colorScheme.errorContainer.animate(animationSpec),
    onPrimary = colorScheme.onPrimary.animate(animationSpec),
    onPrimaryContainer = colorScheme.onPrimaryContainer.animate(animationSpec),
    onSecondary = colorScheme.onSecondary.animate(animationSpec),
    onSecondaryContainer = colorScheme.onSecondaryContainer.animate(animationSpec),
    onTertiary = colorScheme.onTertiary.animate(animationSpec),
    onTertiaryContainer = colorScheme.onTertiaryContainer.animate(animationSpec),
    onBackground = colorScheme.onBackground.animate(animationSpec),
    onSurface = colorScheme.onSurface.animate(animationSpec),
    onSurfaceVariant = colorScheme.onSurfaceVariant.animate(animationSpec),
    onError = colorScheme.onError.animate(animationSpec),
    onErrorContainer = colorScheme.onErrorContainer.animate(animationSpec),
    inversePrimary = colorScheme.inversePrimary.animate(animationSpec),
    inverseSurface = colorScheme.inverseSurface.animate(animationSpec),
    inverseOnSurface = colorScheme.inverseOnSurface.animate(animationSpec),
    outline = colorScheme.outline.animate(animationSpec),
    outlineVariant = colorScheme.outlineVariant.animate(animationSpec),
    scrim = colorScheme.scrim.animate(animationSpec),
    )

  MaterialTheme(
    colorScheme = scheme,
    shapes = shapes,
    typography = typography,
    content = content
  )

  /*if (color == null || isGrayscale(color) || alterColorType == AlterColorType.NONE) {
    content()
  } else {
    val schemes = SchemeCache.get(color, shouldCache, alterColorType)
    MaterialTheme(
      colorScheme = if (colorMode.shouldUseDarkTheme()) schemes.second else schemes.first,
      typography = octoTypography,
      content = content
    )
  }*/
}

fun hexStringToARGBInt(hex: String): Int {
  val color = hex.replace("#", "")
  return when (color.length) {
    6 -> color.toInt(16) or 0xFF000000.toInt()
    8 -> color.toInt(16)
    else -> throw IllegalArgumentException("Invalid color string: $hex")
  }
}