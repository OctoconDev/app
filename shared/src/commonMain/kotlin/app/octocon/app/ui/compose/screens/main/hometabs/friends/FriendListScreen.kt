package app.octocon.app.ui.compose.screens.main.hometabs.friends

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.LockPerson
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PersonSearch
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import app.octocon.app.api.APIState
import app.octocon.app.api.FriendRequests
import app.octocon.app.api.model.BareSystem
import app.octocon.app.api.model.FriendRequest
import app.octocon.app.api.model.FriendshipContainer
import app.octocon.app.api.model.FriendshipLevel
import app.octocon.app.ui.compose.LocalChildPanelsMode
import app.octocon.app.ui.compose.LocalUpdateLazyListState
import app.octocon.app.ui.compose.components.AddFriendDialog
import app.octocon.app.ui.compose.components.FriendCard
import app.octocon.app.ui.compose.components.IncomingFriendRequestCard
import app.octocon.app.ui.compose.components.OutgoingFriendRequestCard
import app.octocon.app.ui.compose.components.RemoveFriendDialog
import app.octocon.app.ui.compose.components.shared.BottomSheetListItem
import app.octocon.app.ui.compose.components.shared.IndeterminateProgressSpinner
import app.octocon.app.ui.compose.components.shared.OctoBottomSheet
import app.octocon.app.ui.compose.components.shared.OctoScaffold
import app.octocon.app.ui.compose.components.shared.OctoTopBar
import app.octocon.app.ui.compose.components.shared.OpenDrawerNavigationButton
import app.octocon.app.ui.compose.components.shared.PermanentTipsNote
import app.octocon.app.ui.compose.components.shared.TitleTextState
import app.octocon.app.ui.compose.screens.GLOBAL_PADDING
import app.octocon.app.ui.compose.theme.getSubsectionStyle
import app.octocon.app.ui.compose.utils.SpotlightTooltip
import app.octocon.app.ui.model.interfaces.SettingsInterface
import app.octocon.app.ui.model.main.hometabs.friends.FriendListComponent
import app.octocon.app.utils.compose
import app.octocon.app.utils.derive
import app.octocon.app.utils.ioDispatcher
import app.octocon.app.utils.savedState
import app.octocon.app.utils.state
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.router.panels.ChildPanelsMode
import kotlinx.coroutines.delay
import octoconapp.shared.generated.resources.Res
import octoconapp.shared.generated.resources.friend_requests
import octoconapp.shared.generated.resources.friends
import octoconapp.shared.generated.resources.make_trusted_friend
import octoconapp.shared.generated.resources.make_untrusted_friend
import octoconapp.shared.generated.resources.permanent_tip_friends
import octoconapp.shared.generated.resources.remove_friend
import octoconapp.shared.generated.resources.tooltip_friends_desc
import octoconapp.shared.generated.resources.tooltip_remove_friend_desc
import octoconapp.shared.generated.resources.tooltip_remove_friend_title
import octoconapp.shared.generated.resources.tooltip_trusted_friends_desc
import octoconapp.shared.generated.resources.tooltip_trusted_friends_title
import octoconapp.shared.generated.resources.view_friend
import kotlin.coroutines.CoroutineContext

@OptIn(ExperimentalDecomposeApi::class)
@Composable
fun FriendListScreen(
  component: FriendListComponent
) {
  val api = component.api
  val settings by component.settings.collectAsState()
  val isSinglet by derive { settings.isSinglet }

  val friends by api.friends.collectAsState()
  val friendRequests by api.friendRequests.collectAsState()

  val sortedFriends by derive {
    when (friends) {
      is APIState.Loading, is APIState.Error -> {
        emptyList()
      }

      is APIState.Success -> {
        friends.ensureData.sorted()
      }
    }
  }

  var addFriendDialogOpen by state(false)
  var selectedFriend by savedState<FriendshipContainer?>(null)
  var friendToDelete by savedState<FriendshipContainer?>(null)

  LaunchedEffect(true) {
    if (isSinglet || friends.isSuccess) return@LaunchedEffect

    api.reloadFriends(pushLoadingState = false)
  }

  LaunchedEffect(true) {
    if (isSinglet || friendRequests.isSuccess) return@LaunchedEffect

    // TODO: This is a hack to hopefully stop Phoenix from getting confused when sending parallel requests
    delay(50)
    api.reloadFriendRequests(pushLoadingState = false)
  }

  val lazyListState = rememberLazyListState()
  val updateLazyListState = LocalUpdateLazyListState.current

  LaunchedEffect(true) {
    updateLazyListState(lazyListState)
  }

  val imageScope = rememberCoroutineScope()
  val placeholderPainter = rememberVectorPainter(Icons.Rounded.Person)

  OctoScaffold(
    hasHoistedBottomBar = !isSinglet,
    topBar = { topAppBarState, scrollBehavior, showSnackbar ->
      OctoTopBar(
        titleTextState = TitleTextState(
          title = Res.string.friends.compose,
          spotlightText = Res.string.friends.compose to Res.string.tooltip_friends_desc.compose
        ),
        navigation = {
          val childPanelsMode = LocalChildPanelsMode.current

          if(isSinglet || childPanelsMode == ChildPanelsMode.SINGLE) {
            OpenDrawerNavigationButton()
          }
        },
        topAppBarState = topAppBarState,
        scrollBehavior = scrollBehavior
      )
    },
    floatingActionButton = {
      FloatingActionButton(
        onClick = {
          addFriendDialogOpen = true
        },
        content = {
          Icon(
            imageVector = Icons.Rounded.Add,
            contentDescription = null
          )
        }
      )
    }
  ) { _, _ ->
    if (friends.isSuccess && friendRequests.isSuccess) {
      LazyFriendsList(
        friends = sortedFriends,
        friendRequests = friendRequests.ensureData,
        imageContext = imageScope.coroutineContext + ioDispatcher,
        placeholderPainter = placeholderPainter,
        lazyListState = lazyListState,
        launchAddFriend = { addFriendDialogOpen = true },
        launchViewFriend = { component.navigateToFriendView(it.friend.id) },
        launchViewAlter = component::navigateToFriendAlterView,
        launchOpenFriendSheet = { selectedFriend = it },
        launchCancelFriendRequest = { api.cancelFriendRequest(it.id) },
        launchRejectFriendRequest = { api.rejectFriendRequest(it.id) },
        launchAcceptFriendRequest = { api.acceptFriendRequest(it.id) },
        settings = component.settings
      )
    } else {
      IndeterminateProgressSpinner()
    }
  }


  selectedFriend?.let {
    FriendContextSheet(
      onDismissRequest = { selectedFriend = null },
      isTrusted = it.friendship.level == FriendshipLevel.TrustedFriend,
      launchViewFriend = { component.navigateToFriendView(it.friend.id) },
      launchRemoveFriend = { friendToDelete = it },
      launchTrustFriend = { api.trustFriend(it.friend.id) },
      launchUntrustFriend = { api.untrustFriend(it.friend.id) }
    )
  }

  friendToDelete?.let {
    RemoveFriendDialog(
      friend = it,
      onDismissRequest = { friendToDelete = null },
      launchRemoveFriend = { friend -> api.removeFriend(friend.friend.id) }
    )
  }

  if (addFriendDialogOpen) {
    AddFriendDialog(
      onDismissRequest = { addFriendDialogOpen = false },
      launchAddFriend = { api.sendFriendRequest(it) }
    )
  }
}

@Composable
private fun LazyFriendsList(
  friends: List<FriendshipContainer>,
  friendRequests: FriendRequests,
  imageContext: CoroutineContext,
  placeholderPainter: Painter,
  lazyListState: LazyListState,
  launchAddFriend: () -> Unit,
  launchViewFriend: (FriendshipContainer) -> Unit,
  launchViewAlter: (friendID: String, alterID: Int) -> Unit,
  launchOpenFriendSheet: (FriendshipContainer) -> Unit,
  launchCancelFriendRequest: (BareSystem) -> Unit,
  launchAcceptFriendRequest: (BareSystem) -> Unit,
  launchRejectFriendRequest: (BareSystem) -> Unit,
  nestedScrollConnection: NestedScrollConnection? = null,
  settings: SettingsInterface
) {
  val settingsData by settings.collectAsState()

  val showPermanentTips by derive { settingsData.showPermanentTips }

  LazyColumn(
    modifier = Modifier.fillMaxHeight().let {
        if (nestedScrollConnection != null) {
          it.nestedScroll(nestedScrollConnection)
        } else it
      },
    state = lazyListState,
    verticalArrangement = Arrangement.spacedBy(12.dp),
    contentPadding = PaddingValues(horizontal = GLOBAL_PADDING)
  ) {
    item {
      Spacer(modifier = Modifier.size(4.dp))
    }

    if (showPermanentTips) {
      item {
        PermanentTipsNote(
          text = Res.string.permanent_tip_friends.compose
        )
      }
    }

    if (friends.isEmpty()) {
      item {
        Card(
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
          ),
          elevation = CardDefaults.cardElevation(
            defaultElevation = 1.0.dp
          )
        ) {
          Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth()
          ) {
            Text(
              "You don't have any friends yet!",
              style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              "Add someone to get started:",
              style = MaterialTheme.typography.bodyMedium.merge(
                lineHeight = 1.5.em
              )
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
              onClick = launchAddFriend,
              colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
              )
            ) {
              Text("Add a friend")
            }
          }
        }
      }
    }

    items(friends, key = { it.friend.id }) {
      FriendCard(
        friendshipContainer = it,
        imageContext = imageContext,
        placeholderPainter = placeholderPainter,
        launchViewFriend = launchViewFriend,
        launchViewAlter = { friend, alter -> launchViewAlter(friend.friend.id, alter.id) },
        launchOpenFriendSheet = launchOpenFriendSheet,
        settings = settingsData
      )
    }

    if (friendRequests.incoming.isNotEmpty() || friendRequests.outgoing.isNotEmpty()) {
      item {
        Text(
          Res.string.friend_requests.compose,
          modifier = Modifier.padding(vertical = 12.dp),
          style = getSubsectionStyle(settingsData.fontSizeScalar)
        )
      }

      items(friendRequests.combine(), key = {
        buildString {
          append((if (it is FriendRequest.Incoming) "i" else "o"))
          append(it.system.id)
          append(it.request.dateSent.toString())
        }
      }) {
        when (it) {
          is FriendRequest.Incoming ->
            IncomingFriendRequestCard(
              request = it.request,
              system = it.system,
              imageContext = imageContext,
              placeholderPainter = placeholderPainter,
              launchAcceptFriendRequest = launchAcceptFriendRequest,
              launchRejectFriendRequest = launchRejectFriendRequest,
              settings = settingsData
            )

          is FriendRequest.Outgoing ->
            OutgoingFriendRequestCard(
              request = it.request,
              system = it.system,
              imageContext = imageContext,
              placeholderPainter = placeholderPainter,
              launchCancelFriendRequest = launchCancelFriendRequest,
              settings = settingsData
            )
        }
      }
    }

    item {
      Spacer(modifier = Modifier.size(4.dp))
    }
  }
}

@Composable
private fun FriendContextSheet(
  onDismissRequest: () -> Unit,
  isTrusted: Boolean,
  launchViewFriend: () -> Unit,
  launchRemoveFriend: () -> Unit,
  launchTrustFriend: () -> Unit,
  launchUntrustFriend: () -> Unit
) {
  OctoBottomSheet(
    onDismissRequest = onDismissRequest
  ) {
    SpotlightTooltip(
      title = Res.string.tooltip_trusted_friends_title.compose,
      description = Res.string.tooltip_trusted_friends_desc.compose
    ) {
      if (isTrusted) {
        BottomSheetListItem(
          imageVector = Icons.Rounded.LockPerson,
          title = Res.string.make_untrusted_friend.compose
        ) {
          launchUntrustFriend()
          onDismissRequest()
        }
      } else {
        BottomSheetListItem(
          imageVector = Icons.Rounded.Star,
          title = Res.string.make_trusted_friend.compose
        ) {
          launchTrustFriend()
          onDismissRequest()
        }
      }
    }
    BottomSheetListItem(
      imageVector = Icons.Rounded.PersonSearch,
      title = Res.string.view_friend.compose
    ) {
      launchViewFriend()
      onDismissRequest()
    }

    SpotlightTooltip(
      title = Res.string.tooltip_remove_friend_title.compose,
      description = Res.string.tooltip_remove_friend_desc.compose
    ) {
      BottomSheetListItem(
        imageVector = Icons.Rounded.Delete,
        iconTint = MaterialTheme.colorScheme.error,
        title = Res.string.remove_friend.compose
      ) {
        launchRemoveFriend()
        onDismissRequest()
      }
    }
  }
}