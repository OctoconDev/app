package app.octocon.app.ui.compose.screens.main.hometabs.friends

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import app.octocon.app.api.model.BareAlter
import app.octocon.app.api.model.ExternalTag
import app.octocon.app.ui.compose.LocalUpdateLazyListState
import app.octocon.app.ui.compose.components.InertAlterCard
import app.octocon.app.ui.compose.components.TagCard
import app.octocon.app.ui.compose.components.shared.BackNavigationButton
import app.octocon.app.ui.compose.components.shared.IndeterminateProgressSpinner
import app.octocon.app.ui.compose.components.shared.OctoLargeTopBar
import app.octocon.app.ui.compose.components.shared.OctoScaffold
import app.octocon.app.ui.compose.components.shared.TitleTextState
import app.octocon.app.ui.compose.screens.GLOBAL_PADDING
import app.octocon.app.ui.compose.theme.ThemeFromColor
import app.octocon.app.ui.compose.theme.getSubsectionStyle
import app.octocon.app.ui.model.main.hometabs.friends.FriendTagViewComponent
import app.octocon.app.utils.compose
import app.octocon.app.utils.derive
import app.octocon.app.utils.ioDispatcher
import com.arkivanov.decompose.ExperimentalDecomposeApi

import octoconapp.shared.generated.resources.Res
import octoconapp.shared.generated.resources.alters
import octoconapp.shared.generated.resources.loading
import octoconapp.shared.generated.resources.tags

@OptIn(ExperimentalDecomposeApi::class)
@Composable
fun FriendTagViewScreen(
  component: FriendTagViewComponent
) {
  val api = component.api
  val settings by component.settings.collectAsState()
  val isSinglet by derive { settings.isSinglet }

  val updateLazyListState = LocalUpdateLazyListState.current

  val imageScope = rememberCoroutineScope()
  val lazyListState = rememberLazyListState()

  val placeholderPainter = rememberVectorPainter(Icons.Rounded.Person)

  val friendDataMap by api.friendDataMap.collectAsState()
  val collatedFriendData = friendDataMap[component.friendID]!!.ensureData

  val model by component.model.collectAsState()

  val tag = model.tag

  LaunchedEffect(true) {
    updateLazyListState(lazyListState)
  }

  val tags = collatedFriendData.tags
  val childTags by derive {
    tags.filter {
      it.parentTagID == component.tagID
    }
  }

  val folderPainter = rememberVectorPainter(Icons.Rounded.Folder)

  val loading = Res.string.loading.compose

  ThemeFromColor(
    if(tag.isSuccess) tag.ensureData.color else null,
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
          titleTextState = TitleTextState(title = when {
            collatedFriendData.tagMap[component.tagID] == null -> loading
            else -> collatedFriendData.tags[collatedFriendData.tagMap[component.tagID]!!].name
          }, oneLine = false),
          topAppBarState = topAppBarState,
          scrollBehavior = scrollBehavior
        )
      },
      content = { _, _ ->
        Surface(
          modifier = Modifier.fillMaxSize()
        ) {
          LazyColumn(
            modifier = Modifier.fillMaxWidth().imePadding(),
            state = lazyListState,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(
              start = GLOBAL_PADDING,
              end = GLOBAL_PADDING,
              bottom = GLOBAL_PADDING
            )
          ) {
            item { Spacer(modifier = Modifier.height(12.dp)) }
            if(!tag.isSuccess) {
              item {
                IndeterminateProgressSpinner()
              }
              return@LazyColumn
            }

            val tagData = tag.ensureData
            if (childTags.isNotEmpty()) {
              item {
                Text(
                  Res.string.tags.compose,
                  modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                  style = getSubsectionStyle(settings.fontSizeScalar)
                )
              }

              items(childTags, key = ExternalTag::id) {
                TagCard(
                  tag = it,
                  iconPainter = folderPainter,
                  onClick = {
                    component.navigateToFriendTagView(it.id)
                  },
                  settings = settings
                )
              }
            }
            if (!tagData.alters.isNullOrEmpty()) {
              item {
                Text(
                  Res.string.alters.compose,
                  modifier = Modifier.fillMaxWidth().padding(
                    bottom = 12.dp, top = if (childTags.isNotEmpty()) 12.dp else 0.dp
                  ),
                  style = getSubsectionStyle(settings.fontSizeScalar)
                )
              }
              items(tagData.alters, key = BareAlter::id) {
                Box {
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
            }
          }
        }
      }
    )
  }

}