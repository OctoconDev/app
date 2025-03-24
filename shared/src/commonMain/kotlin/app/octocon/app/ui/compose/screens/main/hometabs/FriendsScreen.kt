package app.octocon.app.ui.compose.screens.main.hometabs

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.octocon.app.ui.compose.components.TriplePanels
import app.octocon.app.ui.compose.components.octoconLogoVectorPainter
import app.octocon.app.ui.compose.screens.GLOBAL_PADDING
import app.octocon.app.ui.compose.screens.main.hometabs.friends.FriendAlterViewScreen
import app.octocon.app.ui.compose.screens.main.hometabs.friends.FriendListScreen
import app.octocon.app.ui.compose.screens.main.hometabs.friends.FriendTagViewScreen
import app.octocon.app.ui.compose.screens.main.hometabs.friends.FriendViewScreen
import app.octocon.app.ui.model.main.hometabs.FriendsComponent
import app.octocon.app.utils.compose
import app.octocon.app.utils.derive
import com.arkivanov.decompose.ExperimentalDecomposeApi
import octoconapp.shared.generated.resources.Res
import octoconapp.shared.generated.resources.app_logo
import octoconapp.shared.generated.resources.select_friend_placeholder

@OptIn(ExperimentalDecomposeApi::class)
@Composable
fun FriendsScreen(
  component: FriendsComponent
) {
  val settings by component.settings.collectAsState()
  val reduceMotion by derive { settings.reduceMotion }

  TriplePanels(
    panelsValue = component.panels,
    setMode = component::setMode,
    backHandler = component.backHandler,
    onBackPressed = component::onBackPressed,
    main = { FriendListScreen(it.instance) },
    details = { FriendViewScreen(it.instance) },
    extra = {
      when (val child = it.instance) {
        is FriendsComponent.ExtraChild.FriendTagViewChild -> FriendTagViewScreen(child.component)
        is FriendsComponent.ExtraChild.FriendAlterViewChild -> FriendAlterViewScreen(child.component)
      }
    },
    placeholder = { FriendPanelPlaceholder(reduceMotion) },
    reduceMotion = reduceMotion
  )
}

@Composable
private fun FriendPanelPlaceholder(reduceMotion: Boolean) {
  Surface(modifier = Modifier.fillMaxSize()) {
    Box(
      contentAlignment = Alignment.Center
    ) {
      Card(
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
      ) {
        Column(
          modifier = Modifier.padding(GLOBAL_PADDING),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center
        ) {
          Image(
            painter = octoconLogoVectorPainter(animate = !reduceMotion),
            contentDescription = Res.string.app_logo.compose,
            modifier = Modifier.size(128.dp)
          )
          Spacer(Modifier.size(16.dp))
          Text(Res.string.select_friend_placeholder.compose)
        }
      }
    }
  }
}