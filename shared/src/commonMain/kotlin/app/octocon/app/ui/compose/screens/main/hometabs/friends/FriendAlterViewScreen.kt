package app.octocon.app.ui.compose.screens.main.hometabs.friends

import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import app.octocon.app.api.model.ExternalAlterCustomField
import app.octocon.app.ui.compose.LocalUpdateLazyListState
import app.octocon.app.ui.compose.components.InertAlterCustomFieldItem
import app.octocon.app.ui.compose.components.shared.BackNavigationButton
import app.octocon.app.ui.compose.components.shared.FakeOutlinedTextField
import app.octocon.app.ui.compose.components.shared.IndeterminateProgressSpinner
import app.octocon.app.ui.compose.components.shared.OctoLargeTopBar
import app.octocon.app.ui.compose.components.shared.OctoScaffold
import app.octocon.app.ui.compose.components.shared.TitleTextState
import app.octocon.app.ui.compose.screens.GLOBAL_PADDING
import app.octocon.app.ui.compose.theme.ThemeFromColor
import app.octocon.app.ui.compose.theme.getSubsectionStyle
import app.octocon.app.ui.compose.theme.hexStringToARGBInt
import app.octocon.app.ui.compose.theme.squareifyShape
import app.octocon.app.ui.model.main.hometabs.friends.FriendAlterViewComponent
import app.octocon.app.utils.MarkdownRenderer
import app.octocon.app.utils.compose
import app.octocon.app.utils.derive
import app.octocon.app.utils.generateMarkdownTypography
import app.octocon.app.utils.ioDispatcher
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.mikepenz.markdown.compose.LocalMarkdownColors
import com.mikepenz.markdown.m3.markdownColor
import io.kamel.core.utils.cacheControl
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import octoconapp.shared.generated.resources.Res
import octoconapp.shared.generated.resources.basic_info
import octoconapp.shared.generated.resources.color
import octoconapp.shared.generated.resources.description
import octoconapp.shared.generated.resources.error_loading_avatar
import octoconapp.shared.generated.resources.fields
import octoconapp.shared.generated.resources.id
import octoconapp.shared.generated.resources.loading
import octoconapp.shared.generated.resources.name
import octoconapp.shared.generated.resources.name_avatar
import octoconapp.shared.generated.resources.no_avatar
import octoconapp.shared.generated.resources.no_description
import octoconapp.shared.generated.resources.no_pronouns
import octoconapp.shared.generated.resources.pronouns
import octoconapp.shared.generated.resources.unnamed_alter
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalDecomposeApi::class)
@Composable
fun FriendAlterViewScreen(
  component: FriendAlterViewComponent
) {
  val settings by component.settings.collectAsState()
  val isSinglet by derive { settings.isSinglet }
  val showAlterIds by derive { settings.showAlterIds }

  val model by component.model.collectAsState()
  val alter = model.alter

  val updateLazyListState = LocalUpdateLazyListState.current
  val lazyListState = rememberLazyListState()
  val imageScope = rememberCoroutineScope()

  @Suppress("LocalVariableName")
  val unnamed_alter = Res.string.unnamed_alter.compose
  val loading = Res.string.loading.compose

  LaunchedEffect(Unit) { updateLazyListState(lazyListState) }

  ThemeFromColor(
    if(alter.isSuccess) alter.ensureData.color else null,
    colorMode = settings.colorMode,
    dynamicColorType = settings.dynamicColorType,
    colorContrastLevel = settings.colorContrastLevel,
    amoledMode = settings.amoledMode
  ) {
    OctoScaffold(
      hasHoistedBottomBar = !isSinglet,
      topBar = { topAppBarState, scrollBehavior, showSnackbar ->
        OctoLargeTopBar(
          navigation = {
            BackNavigationButton(component::navigateBack)
          },
          titleTextState = TitleTextState(title = if(alter.isSuccess) {
            alter.ensureData.name ?: unnamed_alter
          } else {
            loading
          }, oneLine = false),
          topAppBarState = topAppBarState,
          scrollBehavior = scrollBehavior
        )
      },
      content = { _, _ ->
        val markdownColors = markdownColor()
        CompositionLocalProvider(
          LocalMarkdownColors provides markdownColors
        ) {
          Surface(
            modifier = Modifier.fillMaxSize()
          ) {
            LazyColumn(
              modifier = Modifier.fillMaxWidth(),
              state = lazyListState,
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
              item {
                Spacer(modifier = Modifier.size(0.dp))
              }
              if(!alter.isSuccess) {
                item {
                  IndeterminateProgressSpinner()
                }
                return@LazyColumn
              }

              val alterData = alter.ensureData
              item {
                Box(
                  modifier = Modifier.padding(horizontal = GLOBAL_PADDING)
                ) {
                  Box(
                    modifier = Modifier.sizeIn(
                      maxWidth = 312.dp,
                      maxHeight = 312.dp
                    ).aspectRatio(1.0F)
                      .clip(squareifyShape(settings.cornerStyle) { RoundedCornerShape(96.dp) }),
                    contentAlignment = Alignment.Center
                  ) {
                    if (alterData.avatarUrl.isNullOrBlank()) {
                      Box(
                        modifier = Modifier.fillMaxSize().background(
                          MaterialTheme.colorScheme.surfaceContainerHigh
                        ),
                        contentAlignment = Alignment.Center
                      ) {
                        Column(
                          modifier = Modifier.fillMaxWidth(),
                          horizontalAlignment = Alignment.CenterHorizontally,
                          verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                          Icon(
                            imageVector = Icons.Rounded.Person,
                            modifier = Modifier.size(48.dp),
                            contentDescription = Res.string.no_avatar.compose
                          )
                        }
                      }
                    } else {
                      KamelImage(
                        {
                          asyncPainterResource(alterData.avatarUrl) {
                            coroutineContext = imageScope.coroutineContext + ioDispatcher
                            requestBuilder {
                              cacheControl("max-age=31536000, immutable")
                            }
                          }
                        },
                        // onLoading = { PlaceholderImage(isFronting, placeholderPainter) },
                        onFailure = {
                          Text(Res.string.error_loading_avatar.compose)
                        },
                        contentDescription = stringResource(
                          Res.string.name_avatar,
                          alterData.name ?: unnamed_alter
                        ),
                        modifier = Modifier.fillMaxSize(),
                        animationSpec = tween()
                      )
                    }
                  }
                }
              }

              item {
                /*Text(
                      "Basic info",
                      modifier = Modifier.fillMaxWidth().padding(horizontal = GLOBAL_PADDING),
                      style = MaterialTheme.typography.labelLarge
                    )*/
                Text(
                  Res.string.basic_info.compose,
                  modifier = Modifier.fillMaxWidth()
                    .padding(horizontal = GLOBAL_PADDING, vertical = 8.dp),
                  style = getSubsectionStyle(settings.fontSizeScalar)
                )
              }

              item {
                TextField(
                  value = alterData.name.orEmpty(),
                  onValueChange = {},
                  readOnly = true,
                  placeholder = { Text(unnamed_alter) },
                  label = { Text(Res.string.name.compose) },
                  singleLine = true,
                  modifier = Modifier.fillMaxWidth().padding(horizontal = GLOBAL_PADDING)
                )
              }

              item {
                OutlinedTextField(
                  value = alterData.pronouns.orEmpty(),
                  onValueChange = {},
                  readOnly = true,
                  placeholder = { Text(Res.string.no_pronouns.compose) },
                  label = { Text(Res.string.pronouns.compose) },
                  singleLine = true,
                  modifier = Modifier.fillMaxWidth().padding(horizontal = GLOBAL_PADDING)
                )
              }

              item {
                if(alterData.description.isNullOrBlank()) {
                  OutlinedTextField(
                    value = "",
                    onValueChange = {},
                    readOnly = true,
                    placeholder = { Text(Res.string.no_description.compose) },
                    label = { Text(Res.string.description.compose) },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = GLOBAL_PADDING)
                  )
                } else {
                  val bodyLarge = MaterialTheme.typography.bodyLarge
                  val bodyMedium = MaterialTheme.typography.bodyMedium
                  val textStyle = remember(alterData.description.length < 35) {
                    if (alterData.description.length < 35) bodyLarge else bodyMedium
                  }

                  FakeOutlinedTextField(
                    label = { Text(Res.string.description.compose) },
                    isBlank = alterData.description.isBlank(),
                    textStyle = textStyle,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = GLOBAL_PADDING),
                  ) {
                    MarkdownRenderer(alterData.description, typography = generateMarkdownTypography(textStyle))
                  }
                }
              }

              item {
                Row(
                  modifier = Modifier.fillMaxWidth().padding(horizontal = GLOBAL_PADDING),
                  horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                  if(showAlterIds) {
                    OutlinedTextField(
                      modifier = Modifier.weight(1f),
                      value = alterData.id.toString(),
                      onValueChange = {},
                      readOnly = true,
                      singleLine = true,
                      label = { Text(Res.string.id.compose) }
                    )
                  }
                  OutlinedTextField(
                    modifier = Modifier.weight(1f),
                    value = alterData.color ?: "No color",
                    onValueChange = {},
                    readOnly = true,
                    singleLine = true,
                    suffix = if (alterData.color != null) {
                      {
                        Box(
                          modifier = Modifier
                            .size(16.dp)
                            .clip(MaterialTheme.shapes.extraSmall)
                            .background(Color(hexStringToARGBInt(alterData.color)))
                        )
                      }
                    } else null,
                    label = { Text(Res.string.color.compose) }
                  )
                }
              }

              if (alterData.fields.isNotEmpty()) {
                item {
                  Text(
                    Res.string.fields.compose,
                    modifier = Modifier.fillMaxWidth()
                      .padding(horizontal = GLOBAL_PADDING, vertical = 8.dp),
                    style = getSubsectionStyle(settings.fontSizeScalar)
                  )
                }

                items(alterData.fields, key = ExternalAlterCustomField::id) { field ->
                  InertAlterCustomFieldItem(
                    field = field,
                    value = field.value
                  )
                }
              }

              item {
                Spacer(modifier = Modifier/*.windowInsetsBottomHeight(WindowInsets.systemBars)*/)
              }
            }
          }
        }
      }
    )
  }
}