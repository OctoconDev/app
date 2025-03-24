package app.octocon.app.ui.compose.screens
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import app.octocon.app.ui.compose.components.octoconLogoVectorPainter
import app.octocon.app.ui.compose.components.shared.IndeterminateProgressSpinner
import app.octocon.app.ui.model.StealthAppComponent
import app.octocon.app.utils.Fonts
import app.octocon.app.utils.compose
import app.octocon.app.utils.composeColorSchemeParams
import app.octocon.app.utils.dateFormat
import app.octocon.app.utils.ioDispatcher
import app.octocon.app.utils.noCacheKamelConfig
import io.kamel.core.utils.cacheControl
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import io.kamel.image.config.LocalKamelConfig
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import octoconapp.shared.generated.resources.Res
import octoconapp.shared.generated.resources.app_name
import octoconapp.shared.generated.resources.stealth_app_subtitle

@Composable
fun StealthAppScreen(component: StealthAppComponent) {
  val imageScope = rememberCoroutineScope()
  val imageContext = imageScope.coroutineContext + ioDispatcher

  val colorSchemeParams = composeColorSchemeParams

  val snackbarScope = rememberCoroutineScope()
  val snackbarHostState = remember { SnackbarHostState() }

  val model by component.model.collectAsState()
  val articlesState = model.articles

  LaunchedEffect(articlesState) {
    if (articlesState is StealthAppComponent.ArticleState.Error) {
      snackbarScope.launch {
        snackbarHostState.showSnackbar(articlesState.message)
      }
    }
  }

  CompositionLocalProvider(
    LocalKamelConfig provides noCacheKamelConfig
  ) {
    Scaffold(
      snackbarHost = { SnackbarHost(snackbarHostState) },
    ) {
      Box(
        modifier = Modifier.padding(it).fillMaxSize(),
        contentAlignment = Alignment.Center
      ) {
        Column(
          modifier = Modifier.fillMaxHeight().padding(horizontal = GLOBAL_PADDING).widthIn(max = 650.dp)
        ) {
          Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
          ) {
            Row(
              modifier = Modifier.padding(vertical = GLOBAL_PADDING),
              horizontalArrangement = Arrangement.spacedBy(16.dp),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Image(
                painter = octoconLogoVectorPainter(),
                // painter = painterResource(Res.drawable.octocon_logo),
                contentDescription = null,
                modifier = Modifier.size(64.dp).combinedClickable(
                  onClick = {},
                  onDoubleClick = component::exitStealthMode,
                  interactionSource = remember { MutableInteractionSource() },
                  indication = null
                )
              )
              Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
              ) {
                Text(
                  Res.string.app_name.compose,
                  style = MaterialTheme.typography.headlineMedium
                )
                Text(
                  Res.string.stealth_app_subtitle.compose,
                  style = MaterialTheme.typography.titleMedium
                )
              }
            }
          }

          if (articlesState !is StealthAppComponent.ArticleState.Success) {
            IndeterminateProgressSpinner()
          } else {
            LazyColumn(
              modifier = Modifier.fillMaxSize().clip(MaterialTheme.shapes.large),
              verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
              items(articlesState.articles) { article ->
                Card(
                  colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = MaterialTheme.colorScheme.onSurface
                  ),
                  shape = MaterialTheme.shapes.large,
                  onClick = { component.openArticle(article.url, colorSchemeParams) },
                ) {
                  Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                  ) {
                    Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                      Column(
                        modifier = Modifier.align(Alignment.CenterVertically).weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                      ) {
                        Text(
                          text = article.newsSite,
                          style = MaterialTheme.typography.labelMedium,
                          color = MaterialTheme.colorScheme.tertiary
                        )
                        Text(
                          text = article.title,
                          style = MaterialTheme.typography.titleMedium.merge(fontFamily = Fonts.ubuntu)
                        )
                      }
                      KamelImage(
                        {
                          asyncPainterResource(article.imageUrl) {
                            coroutineContext = imageContext
                            requestBuilder {
                              cacheControl("max-age=31536000, immutable")
                            }
                          }
                        },
                        onFailure = { },
                        contentDescription = null,
                        modifier = Modifier
                          .size(width = 96.dp, height = 128.dp)
                          .clip(MaterialTheme.shapes.medium),
                        animationSpec = tween(),
                        contentScale = ContentScale.Crop
                      )
                    }

                    Text(
                      text = article.summary,
                      style = MaterialTheme.typography.bodySmall,
                      lineHeight = 2.em,
                    )

                    Text(
                      text = article.publishedAt.toLocalDateTime(TimeZone.currentSystemDefault())
                        .dateFormat(),
                      style = MaterialTheme.typography.labelMedium,
                      color = MaterialTheme.colorScheme.tertiary,
                      modifier = Modifier.align(Alignment.End)
                    )
                  }
                }
              }

              item {
                Spacer(modifier = Modifier)
              }
            }
          }
        }
      }
    }

  }
}