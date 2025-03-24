package app.octocon.app.ui.compose.screens.main.hometabs.friends

import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.LockPerson
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import app.octocon.app.api.model.ExternalAlter
import app.octocon.app.api.model.ExternalTag
import app.octocon.app.ui.compose.LocalChildPanelsMode
import app.octocon.app.ui.compose.LocalUpdateLazyListState
import app.octocon.app.ui.compose.components.InertAlterCard
import app.octocon.app.ui.compose.components.TagCard
import app.octocon.app.ui.compose.components.shared.BackNavigationButton
import app.octocon.app.ui.compose.components.shared.FakeOutlinedTextField
import app.octocon.app.ui.compose.components.shared.IndeterminateProgressSpinner
import app.octocon.app.ui.compose.components.shared.OctoLargeTopBar
import app.octocon.app.ui.compose.components.shared.OctoScaffold
import app.octocon.app.ui.compose.components.shared.TitleTextState
import app.octocon.app.ui.compose.screens.GLOBAL_PADDING
import app.octocon.app.ui.compose.theme.getSubsectionStyle
import app.octocon.app.ui.compose.theme.squareifyShape
import app.octocon.app.ui.model.main.hometabs.friends.FriendViewComponent
import app.octocon.app.utils.MarkdownRenderer
import app.octocon.app.utils.compose
import app.octocon.app.utils.derive
import app.octocon.app.utils.ioDispatcher
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.router.panels.ChildPanelsMode
import com.mikepenz.markdown.compose.LocalMarkdownColors
import com.mikepenz.markdown.m3.markdownColor
import io.kamel.core.utils.cacheControl
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import octoconapp.shared.generated.resources.Res
import octoconapp.shared.generated.resources.all_alters
import octoconapp.shared.generated.resources.currently_fronting
import octoconapp.shared.generated.resources.description
import octoconapp.shared.generated.resources.error_loading_avatar
import octoconapp.shared.generated.resources.id
import octoconapp.shared.generated.resources.loading
import octoconapp.shared.generated.resources.name_avatar
import octoconapp.shared.generated.resources.no_avatar
import octoconapp.shared.generated.resources.note
import octoconapp.shared.generated.resources.privacy_warning
import octoconapp.shared.generated.resources.tags
import octoconapp.shared.generated.resources.username
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalDecomposeApi::class)
@Composable
fun FriendViewScreen(
  component: FriendViewComponent
) {
  val api = component.api
  val settings by component.settings.collectAsState()
  val isSinglet by derive { settings.isSinglet }

  val updateLazyListState = LocalUpdateLazyListState.current

  val imageScope = rememberCoroutineScope()
  val placeholderPainter = rememberVectorPainter(Icons.Rounded.Person)
  val folderPainter = rememberVectorPainter(Icons.Rounded.Folder)
  val lazyListState = rememberLazyListState()

  val friendDataMap by api.friendDataMap.collectAsState()

  val collatedFriendData by derive { friendDataMap[component.friendID] }

  LaunchedEffect(collatedFriendData) {
    if (collatedFriendData == null) {
      api.loadFriend(component.friendID)
    }
  }

  val loading = Res.string.loading.compose

  val titleText by derive {
    when {
      collatedFriendData?.isSuccess != true -> loading
      else -> {
        val friend = collatedFriendData!!.ensureData.friendship.friend
        friend.username ?: friend.id
      }
    }
  }

  LaunchedEffect(true) {
    updateLazyListState(lazyListState)
  }

  val friendData by derive {
    when {
      collatedFriendData == null -> null
      !collatedFriendData!!.isSuccess -> null
      else -> collatedFriendData!!.ensureData
    }
  }

  val friendshipContainer by derive { friendData?.friendship }
  val friend by derive { friendshipContainer?.friend }
  val alters by derive { friendData?.alters }
  val tags by derive { friendData?.tags }
  val tagMap by derive { friendData?.tagMap }

  val nonFrontingAlters: List<ExternalAlter> by derive {
    if(friendData == null) return@derive emptyList()
    val frontingAlters = friendshipContainer!!.fronting.map { it.alter.id }
    alters!!.filter { it.id !in frontingAlters }
  }

  val rootTags: List<ExternalTag> by derive {
    if(friendData == null) return@derive emptyList()
    tags!!.filter { it.parentTagID == null || !tagMap!!.containsKey(it.parentTagID) }
  }

  /*var firstItemNotVisible by state(false)

  LaunchedEffect(lazyListState) {
    snapshotFlow { lazyListState.firstVisibleItemIndex }
      .distinctUntilChanged()
      .collect {
        firstItemNotVisible = it > 0
      }
  }*/

  val markdownColors = markdownColor()

  OctoScaffold(
    hasHoistedBottomBar = !isSinglet,
    topBar = { topAppBarState, scrollBehavior, showSnackbar ->
      OctoLargeTopBar(
        navigation = {
          val childPanelsMode = LocalChildPanelsMode.current

          if(childPanelsMode == ChildPanelsMode.SINGLE) {
            BackNavigationButton(component::navigateBack)
          }
        },
        titleTextState = TitleTextState(titleText, oneLine = false),
        topAppBarState = topAppBarState,
        scrollBehavior = scrollBehavior
      )
    },
    content = { _, _ ->
      CompositionLocalProvider(
        LocalMarkdownColors provides markdownColors
      ) {
        Surface(
          modifier = Modifier.fillMaxSize()
        ) {
          Box(
            modifier = Modifier.fillMaxSize()
          ) {
            LazyColumn(
              state = lazyListState,
              modifier = Modifier.fillMaxWidth(),
              horizontalAlignment = Alignment.CenterHorizontally,
              contentPadding = PaddingValues(horizontal = GLOBAL_PADDING)
            ) {
              item {
                Spacer(modifier = Modifier.size(8.dp))
              }

              if (
                friendData == null
                || friend == null
                || alters == null
                || tags == null
                || tagMap == null
              ) {
                item { IndeterminateProgressSpinner() }
              } else {
                item {
                  Box(
                    modifier = Modifier.padding(vertical = 8.dp)
                  ) {
                    Box(
                      modifier = Modifier.sizeIn(
                        maxWidth = 312.dp,
                        maxHeight = 312.dp
                      ).aspectRatio(1.0F).clip(squareifyShape(settings.cornerStyle) { RoundedCornerShape(96.dp) }),
                    ) {
                      if (
                      // imageFailedToLoad ||
                        friend!!.avatarUrl.isNullOrBlank()) {
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
                            asyncPainterResource(friend!!.avatarUrl!!) {
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
                          contentDescription = stringResource(Res.string.name_avatar, titleText),
                          modifier = Modifier.fillMaxSize(),
                          animationSpec = tween()
                        )
                      }
                    }
                  }
                }

                item {
                  Row(
                    modifier = Modifier.fillMaxWidth()
                      .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                  ) {
                    if (!friend!!.username.isNullOrBlank()) {
                      OutlinedTextField(
                        modifier = Modifier.weight(2f),
                        value = friend!!.username!!,
                        onValueChange = {},
                        readOnly = true,
                        singleLine = true,
                        label = { Text(Res.string.username.compose) }
                      )
                    }
                    OutlinedTextField(
                      modifier = Modifier.weight(1f),
                      value = friend!!.id,
                      onValueChange = {},
                      readOnly = true,
                      singleLine = true,
                      label = { Text(Res.string.id.compose) }
                    )
                  }
                }

                if (!friend!!.description.isNullOrBlank()) {
                  item {
                    FakeOutlinedTextField(
                      label = { Text(Res.string.description.compose) },
                      isBlank = false,
                      modifier = Modifier.fillMaxWidth()
                        .padding(vertical = 8.dp)
                    ) {
                      MarkdownRenderer(friend!!.description)
                    }
                  }
                }

                if (friendshipContainer!!.fronting.isNotEmpty()) {
                  item {
                    Text(
                      Res.string.currently_fronting.compose,
                      modifier = Modifier.fillMaxWidth()
                        .padding(top = 16.dp, bottom = 19.dp),
                      style = getSubsectionStyle(settings.fontSizeScalar)
                    )
                  }

                  items(friendshipContainer!!.fronting, key = { "f${it.alter.id}" }) {
                    Box(
                      modifier = Modifier.padding(vertical = 5.dp)
                    ) {
                      InertAlterCard(
                        alter = it.alter,
                        imageContext = imageScope.coroutineContext + ioDispatcher,
                        placeholderPainter = placeholderPainter,
                        onClick = {
                          component.navigateToFriendAlterView(it.alter.id)
                        },
                        isFronting = true,
                        frontComment = it.front.comment,
                        isPrimary = it.primary,
                        settings = settings
                      )
                    }
                  }
                }

                if (rootTags.isNotEmpty()) {
                  item {
                    Text(
                      Res.string.tags.compose,
                      modifier = Modifier.fillMaxWidth()
                        .padding(top = 19.dp, bottom = 19.dp),
                      style = getSubsectionStyle(settings.fontSizeScalar)
                    )
                  }

                  items(rootTags, key = ExternalTag::id) {
                    Box(
                      modifier = Modifier.padding(vertical = 5.dp)
                    ) {
                      TagCard(
                        tag = it,
                        iconPainter = folderPainter,
                        onClick = { component.navigateToFriendTagView(it.id) },
                        settings = settings
                      )
                    }
                  }
                }

                item {
                  Text(
                    Res.string.all_alters.compose,
                    modifier = Modifier.fillMaxWidth()
                      .padding(top = 19.dp, bottom = 19.dp),
                    style = getSubsectionStyle(settings.fontSizeScalar)
                  )
                }

                items(nonFrontingAlters, key = ExternalAlter::id) {
                  Box(
                    modifier = Modifier.padding(vertical = 5.dp)
                  ) {
                    InertAlterCard(
                      alter = it,
                      imageContext = imageScope.coroutineContext + ioDispatcher,
                      placeholderPainter = placeholderPainter,
                      onClick = { component.navigateToFriendAlterView(it.id) },
                      isFronting = false,
                      isPrimary = false,
                      settings = settings
                    )
                  }
                }

                item {
                  Column(
                    modifier = Modifier.padding(
                      top = 11.dp,
                      bottom = 16.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                  ) {
                    Row(
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                      Icon(
                        imageVector = Icons.Rounded.LockPerson,
                        modifier = Modifier.size(16.dp),
                        contentDescription = null
                      )
                      Text(
                        Res.string.note.compose,
                        style = MaterialTheme.typography.labelMedium
                      )
                    }
                    Text(
                      Res.string.privacy_warning.compose,
                      style = MaterialTheme.typography.bodySmall
                    )
                  }
                }
              }
            }
          }
        }
      }
    }
  )
}