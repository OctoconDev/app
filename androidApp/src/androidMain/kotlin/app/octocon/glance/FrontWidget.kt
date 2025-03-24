package app.octocon.glance

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.BitmapFactory
import android.text.format.DateUtils
import android.util.Base64
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.ColorUtils.M3HCTToColor
import androidx.core.graphics.ColorUtils.colorToM3HCT
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalGlanceId
import androidx.glance.LocalSize
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.CircularProgressIndicator
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.components.CircleIconButton
import androidx.glance.appwidget.components.Scaffold
import androidx.glance.appwidget.components.TitleBar
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.GridCells
import androidx.glance.appwidget.lazy.LazyVerticalGrid
import androidx.glance.appwidget.lazy.itemsIndexed
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.color.ColorProviders
import androidx.glance.color.colorProviders
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.material3.ColorProviders
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import app.octocon.MainActivity
import app.octocon.R
import app.octocon.app.DynamicColorType
import app.octocon.app.Settings
import app.octocon.app.api.model.APIResponse
import app.octocon.app.api.model.MyFrontItem
import app.octocon.app.ui.compose.theme.SchemeCache
import app.octocon.app.ui.compose.theme.Theme
import app.octocon.app.utils.globalSerializer
import app.octocon.app.utils.isGrayscale
import app.octocon.app.utils.state
import app.octocon.util.createSharedPreferences
import app.octocon.util.getSavedSettings
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import androidx.glance.GlanceModifier.Companion as Modifier

class FrontWidgetReceiver : GlanceAppWidgetReceiver() {
  override val glanceAppWidget: GlanceAppWidget
    get() = FrontWidget()
}

private val FULL_PADDING = 12.dp
private val HALF_PADDING = 6.dp
private val NO_PADDING = 0.dp

class FrontWidget : GlanceAppWidget() {
  private lateinit var context: Context
  private val sharedPreferences by lazy { createSharedPreferences(context) }
  private val settings by lazy { getSavedSettings(sharedPreferences) }

  override val sizeMode: SizeMode = SizeMode.Exact

  companion object {
    val resultKey = stringPreferencesKey("work_result")
  }

  override suspend fun provideGlance(context: Context, id: GlanceId) {
    // In this method, load data needed to render the AppWidget.
    // Use `withContext` to switch to another thread for long running
    // operations.
    this.context = context

    if(settings.tokenIsProtected) {
      provideContent { ErrorScreen("You must disable your Octocon PIN to use this widget.", settings) }
    }
    if(settings.token == null) {
      provideContent { ErrorScreen("You must log in to Octocon to use this widget.", settings) }
    }

    provideContent {
      val size = LocalSize.current
      val serializedState = currentState(resultKey)

      val state = remember(serializedState) {
        serializedState?.let { globalSerializer.decodeFromString<APIResponse<List<MyFrontItem>>>(it) }
      }

      if(state != null && state.isError) {
        ErrorScreen("Failed to load fronting alters.", settings)
        return@provideContent
      }

      val frontingAlters = state?.ensureSuccess

      var currentTime by state(Clock.System.now())

      LaunchedEffect(Unit) {
        while(true) {
          currentTime = Clock.System.now()
          delay(10_000)
        }
      }

      GlanceTheme(
        colors = settings.themeColor.themeColors.toGlanceColorProviders(settings)
      ) {
        if(state == null) {
          Scaffold(horizontalPadding = 16.dp) {
            Column(
              modifier = Modifier.fillMaxSize(),
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalAlignment = Alignment.CenterVertically
            ) {
              CircularProgressIndicator(
                color = GlanceTheme.colors.primary,
              )
              Spacer(modifier = Modifier.height(12.dp))
              Text("Loading alters...", style = TextStyle(color = GlanceTheme.colors.onBackground, fontSize = 12.sp))

              // Enqueue the worker after the composition is completed using the glanceId as
              // tag so we can cancel all jobs in case the widget instance is deleted
              val glanceId = LocalGlanceId.current
              SideEffect {
                FrontWidgetWorker.enqueue(context, settings, glanceId)
              }
            }
          }
          return@GlanceTheme
        }

        frontingAlters!!

        val showTitleBar = size.width >= 260.dp
        Scaffold(
          titleBar = if(showTitleBar) {
            {
              TitleBar(
                startIcon = ImageProvider(R.drawable.octo_logo),
                iconColor = null,
                textColor = GlanceTheme.colors.onSecondaryContainer,
                title = "Currently fronting",
                actions = {
                  CircleIconButton(
                    imageProvider = ImageProvider(R.drawable.round_edit_24),
                    contentDescription = null,
                    backgroundColor = null,
                    onClick = actionStartActivity<MainActivity>()
                  )
                }
              )
            }
          } else null,
          horizontalPadding = NO_PADDING
        ) {
          if(frontingAlters.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
              Text("No one is fronting right now.", style = TextStyle(color = GlanceTheme.colors.onBackground, fontSize = 16.sp))
            }
            return@Scaffold
          }
          val rowCount = if(size.width < 200.dp) 1 else 2
          LazyVerticalGrid(
            gridCells = GridCells.Fixed(rowCount),
            modifier = Modifier.fillMaxSize()
          ) {
            itemsIndexed(frontingAlters, itemId = { _, item -> item.alter.id.toLong() }) { index, item ->
              Box(
                modifier = Modifier.padding(
                  start = when {
                    rowCount == 1 -> FULL_PADDING
                    index % 2 == 0 -> FULL_PADDING
                    else -> HALF_PADDING
                  },
                  end = when {
                    rowCount == 1 -> FULL_PADDING
                    index % 2 == 0 -> HALF_PADDING
                    else -> FULL_PADDING
                  },
                  top = when {
                    rowCount == 1 -> FULL_PADDING
                    index < 2 && showTitleBar -> NO_PADDING
                    else -> FULL_PADDING
                  },
                  bottom = when {
                    rowCount == 1 && index == frontingAlters.size - 1 -> FULL_PADDING
                    frontingAlters.size - index < 2 -> FULL_PADDING
                    else -> NO_PADDING
                  }
                )
              ) {
                AlterCard(item, settings, currentTime)
              }
            }
          }
        }
      }
    }
  }

  /**
   * Called when the widget instance is deleted. We can then clean up any ongoing task.
   */
  override suspend fun onDelete(context: Context, glanceId: GlanceId) {
    super.onDelete(context, glanceId)
    FrontWidgetWorker.cancel(context, glanceId)
  }
}

@Composable
private fun ErrorScreen(
  message: String,
  settings: Settings
) {
  GlanceTheme(
    colors = settings.themeColor.themeColors.toGlanceColorProviders(settings)
  ) {
    Scaffold(
      horizontalPadding = 16.dp
    ) {
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxSize().clickable(
          onClick = actionStartActivity<MainActivity>()
        )
      ) {
        Image(
          provider = ImageProvider(R.drawable.octo_logo),
          contentDescription = null,
          modifier = Modifier.size(64.dp)
        )

        Spacer(modifier = Modifier.size(16.dp))

        Text(
          text = message,
          style = TextStyle(color = GlanceTheme.colors.error, textAlign = TextAlign.Center)
        )
      }
    }
  }
}

@Composable
private fun AlterCard(
  frontItem: MyFrontItem,
  settings: Settings,
  currentTime: Instant
) {
  val (alter, front) = frontItem

  val frontingTimeText = remember(currentTime) {
    val timeStart = front.timeStart

    "Since " + DateUtils.getRelativeTimeSpanString(
      timeStart.toEpochMilliseconds(),
      currentTime.toEpochMilliseconds(),
      DateUtils.SECOND_IN_MILLIS,
      DateUtils.FORMAT_ABBREV_RELATIVE
    )
  }

  GlanceThemeFromColor(
    alter.color,
    settings
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().height(48.dp).background(GlanceTheme.colors.secondaryContainer).cornerRadius(8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      val provider = getImageProvider(alter.avatarUrl)

      if(provider == null) {
        Box(
          modifier = Modifier.size(48.dp).cornerRadius(8.dp).background(GlanceTheme.colors.primaryContainer),
          contentAlignment = Alignment.Center
        ) {
          Image(
            ImageProvider(R.drawable.round_person_24),
            contentDescription = null,
            colorFilter = ColorFilter.tint(GlanceTheme.colors.onPrimaryContainer),
            modifier = Modifier.size(24.dp)
          )
        }
      } else {
        Image(
          provider = provider,
          contentDescription = null,
          contentScale = ContentScale.FillBounds,
          modifier = Modifier.size(48.dp).cornerRadius(8.dp)
        )
      }
      Spacer(modifier = Modifier.size(8.dp))
      Column(
        modifier = Modifier.fillMaxHeight(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(alter.name ?: "Unnamed alter", style = TextStyle(color = GlanceTheme.colors.onSecondaryContainer, fontSize = 12.sp), maxLines = 1)
        Text(frontingTimeText, style = TextStyle(color = GlanceTheme.colors.onSecondaryContainer, fontSize = 8.sp), maxLines = 1)
      }
    }
  }
}

@Composable
private fun GlanceThemeFromColor(
  color: String?,
  settings: Settings,
  content: @Composable () -> Unit
) {
  val colorScheme = when {
    color == null || settings.dynamicColorType == DynamicColorType.NONE -> null
    isGrayscale(color) -> SchemeCache.getMonochrome(settings.colorContrastLevel, settings.amoledMode)
    else -> SchemeCache.get(color, false, settings.dynamicColorType, settings.colorContrastLevel, settings.amoledMode)
  }

  if(colorScheme == null) {
    content()
  } else {
    val (light, dark) = colorScheme

    GlanceTheme(
      colors = ColorProviders(
        light = light,
        dark = dark
      )
    ) {
      content()
    }
  }
}

@SuppressLint("ComposableNaming")
@Composable
private fun Theme.toGlanceColorProviders(settings: Settings): ColorProviders {
  val lightColors = this.colorSchemeProvider(
    settings.colorMode,
    settings.dynamicColorType,
    settings.colorContrastLevel,
    settings.amoledMode,
    isDark = false
  )
  val darkColors = this.colorSchemeProvider(
    settings.colorMode,
    settings.dynamicColorType,
    settings.colorContrastLevel,
    settings.amoledMode,
    isDark = true
  )

  return ColorProviders(
    light = lightColors,
    dark = darkColors
  ).let {
    if(settings.amoledMode) {
      it.copy(
        widgetBackground = ColorProvider(
          day = adjustColorToneForWidgetBackground(lightColors.secondaryContainer),
          night = Color.Black
        )
      )
    } else it
  }
}

fun ColorProviders.copy(
  primary: ColorProvider = this.primary,
  onPrimary: ColorProvider = this.onPrimary,
  primaryContainer: ColorProvider = this.primaryContainer,
  onPrimaryContainer: ColorProvider = this.onPrimaryContainer,
  secondary: ColorProvider = this.secondary,
  onSecondary: ColorProvider = this.onSecondary,
  secondaryContainer: ColorProvider = this.secondaryContainer,
  onSecondaryContainer: ColorProvider = this.onSecondaryContainer,
  tertiary: ColorProvider = this.tertiary,
  onTertiary: ColorProvider = this.onTertiary,
  tertiaryContainer: ColorProvider = this.tertiaryContainer,
  onTertiaryContainer: ColorProvider = this.onTertiaryContainer,
  error: ColorProvider = this.error,
  errorContainer: ColorProvider = this.errorContainer,
  onError: ColorProvider = this.onError,
  onErrorContainer: ColorProvider = this.onErrorContainer,
  background: ColorProvider = this.background,
  onBackground: ColorProvider = this.onBackground,
  surface: ColorProvider = this.surface,
  onSurface: ColorProvider = this.onSurface,
  surfaceVariant: ColorProvider = this.surfaceVariant,
  onSurfaceVariant: ColorProvider = this.onSurfaceVariant,
  outline: ColorProvider = this.outline,
  inverseOnSurface: ColorProvider = this.inverseOnSurface,
  inverseSurface: ColorProvider = this.inverseSurface,
  inversePrimary: ColorProvider = this.inversePrimary,
  widgetBackground: ColorProvider = this.widgetBackground
): ColorProviders = colorProviders(
  primary = primary,
  onPrimary = onPrimary,
  primaryContainer = primaryContainer,
  onPrimaryContainer = onPrimaryContainer,
  secondary = secondary,
  onSecondary = onSecondary,
  secondaryContainer = secondaryContainer,
  onSecondaryContainer = onSecondaryContainer,
  tertiary = tertiary,
  onTertiary = onTertiary,
  tertiaryContainer = tertiaryContainer,
  onTertiaryContainer = onTertiaryContainer,
  error = error,
  errorContainer = errorContainer,
  onError = onError,
  onErrorContainer = onErrorContainer,
  background = background,
  onBackground = onBackground,
  surface = surface,
  onSurface = onSurface,
  surfaceVariant = surfaceVariant,
  onSurfaceVariant = onSurfaceVariant,
  outline = outline,
  inverseOnSurface = inverseOnSurface,
  inverseSurface = inverseSurface,
  inversePrimary = inversePrimary,
  widgetBackground = widgetBackground
)

private const val WIDGET_BG_TONE_ADJUSTMENT_LIGHT = 5f
private const val WIDGET_BG_TONE_ADJUSTMENT_DARK = -10f

private fun adjustColorToneForWidgetBackground(input: Color): Color {
  val hctColor = floatArrayOf(0f, 0f, 0f)
  colorToM3HCT(input.toArgb(), hctColor)
  // Check the Tone of the input color, if it is "light" (greater than 50) lighten it, otherwise
  // darken it.
  val adjustment =
    if (hctColor[2] > 50) WIDGET_BG_TONE_ADJUSTMENT_LIGHT else WIDGET_BG_TONE_ADJUSTMENT_DARK

  // Tone should be defined in the 0 - 100 range, ok to clamp here.
  val tone = (hctColor[2] + adjustment).coerceIn(0f, 100f)
  return Color(M3HCTToColor(hctColor[0], hctColor[1], tone))
}

private fun getImageProvider(encoded: String?): ImageProvider? {
  if(encoded == null) return null

  val bytes = Base64.decode(encoded, Base64.DEFAULT)
  val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

  return bitmap?.let { ImageProvider(bitmap) }
}