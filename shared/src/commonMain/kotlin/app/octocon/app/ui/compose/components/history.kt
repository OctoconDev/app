package app.octocon.app.ui.compose.components

import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Notes
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.octocon.app.Settings
import app.octocon.app.api.model.MyAlter
import app.octocon.app.ui.compose.screens.GLOBAL_PADDING
import app.octocon.app.ui.compose.screens.main.hometabs.FrontHistoryItem
import app.octocon.app.ui.compose.screens.main.hometabs.FrontHistoryTimeType
import app.octocon.app.ui.compose.theme.ThemeFromColor
import app.octocon.app.utils.compose
import app.octocon.app.utils.timeFormat
import io.kamel.core.utils.cacheControl
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import octoconapp.shared.generated.resources.Res
import octoconapp.shared.generated.resources.unnamed_alter
import kotlin.coroutines.CoroutineContext

val frontHistoryItemCardHeight = 56.dp

@Composable
fun FrontHistoryItemCard(
  frontHistoryItem: FrontHistoryItem,
  alter: MyAlter,
  onClick: () -> Unit,
  imageContext: CoroutineContext,
  placeholderPainter: Painter,
  settings: Settings
) {
  val timeText = when (frontHistoryItem.type) {
    FrontHistoryTimeType.PARTIAL ->
      "${frontHistoryItem.timeStarted.timeFormat()} - ${frontHistoryItem.timeEnded.timeFormat()}"

    FrontHistoryTimeType.ALL_DAY ->
      "All day"

    FrontHistoryTimeType.INFINITIVE_START ->
      "Until ${frontHistoryItem.timeEnded.timeFormat()}"

    FrontHistoryTimeType.INFINITIVE_END ->
      "From ${frontHistoryItem.timeStarted.timeFormat()}"
  }

  // var imageFailedToLoad by state(false)

  ThemeFromColor(
    alter.color,
    colorMode = settings.colorMode,
    dynamicColorType = settings.dynamicColorType,
    colorContrastLevel = settings.colorContrastLevel,
    amoledMode = settings.amoledMode
  ) {
    Column(
      modifier = Modifier.padding(horizontal = GLOBAL_PADDING, vertical = 4.dp)
    ) {
      ElevatedCard(
        modifier = Modifier.height(frontHistoryItemCardHeight)
          .fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
          containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        onClick = onClick
      ) {
        Row(
          modifier = Modifier.fillMaxSize(),
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          if (
          // imageFailedToLoad ||
            alter.avatarUrl.isNullOrBlank()) {
            FrontHistoryCardPlaceholderImage(placeholderPainter)
          } else {
            /*AsyncImage(
              model = ImageRequest
                .Builder(LocalPlatformContext.current)
                .crossfade(true)
                .data(alter.avatarUrl)
                .listener(onError = { _, _ ->
                  imageFailedToLoad = true
                })
                .build(),
              contentDescription = "${alter.name} avatar",
              modifier = Modifier.size(frontHistoryItemCardHeight)
                .clip(MaterialTheme.shapes.medium),
              contentScale = ContentScale.Crop
            )*/
            KamelImage(
              {
                asyncPainterResource(alter.avatarUrl) {
                  coroutineContext = imageContext
                  requestBuilder {
                    cacheControl("max-age=31536000, immutable")
                  }
                }
              },
              // onLoading = { PlaceholderImage(placeholderPainter) },
              onFailure = { FrontHistoryCardPlaceholderImage(placeholderPainter) },
              contentDescription = "${alter.name} avatar",
              modifier = Modifier.size(frontHistoryItemCardHeight)
                .clip(MaterialTheme.shapes.medium),
              animationSpec = tween()
            )
          }

          Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center
          ) {
            Text(
              alter.name ?: Res.string.unnamed_alter.compose,
              style = MaterialTheme.typography.labelSmall.merge(color = MaterialTheme.colorScheme.onSurface),
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(2.dp))
            Text(
              timeText,
              style = MaterialTheme.typography.labelLarge,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }
        }
      }

      if (!frontHistoryItem.comment.isNullOrBlank()) {
        Spacer(modifier = Modifier.height(8.dp))
        Row(
          horizontalArrangement = Arrangement.spacedBy(4.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Rounded.Notes,
            contentDescription = "Front comment",
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.size(12.dp)
          )
          Text(
            frontHistoryItem.comment,
            style = MaterialTheme.typography.bodySmall.merge(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.secondary,
          )
        }
      }
    }
  }
}

@Composable
fun FrontHistoryCardPlaceholderImage(
  painter: Painter
) {
  Surface(
    modifier = Modifier.size(frontHistoryItemCardHeight).clip(MaterialTheme.shapes.medium),
    color = MaterialTheme.colorScheme.surfaceContainerHigh,
  ) {
    Box(
      contentAlignment = Alignment.Center
    ) {
      Icon(
        painter = painter,
        contentDescription = null,
        modifier = Modifier.size(24.dp),
        tint = MaterialTheme.colorScheme.secondary
      )
    }
  }
}