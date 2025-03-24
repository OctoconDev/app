package app.octocon.app.ui.compose.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material.icons.rounded.PersonRemove
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.octocon.app.Settings
import app.octocon.app.api.model.BareSystem
import app.octocon.app.api.model.ExternalAlter
import app.octocon.app.api.model.FriendRequestData
import app.octocon.app.api.model.FriendshipContainer
import app.octocon.app.api.model.FriendshipLevel
import app.octocon.app.ui.compose.theme.squareifyShape
import app.octocon.app.utils.compose
import app.octocon.app.utils.dateFormat
import app.octocon.app.utils.idRegex
import app.octocon.app.utils.savedState
import app.octocon.app.utils.usernameRegex
import io.kamel.core.utils.cacheControl
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import octoconapp.shared.generated.resources.Res
import octoconapp.shared.generated.resources.confirm
import octoconapp.shared.generated.resources.friend_fronting_count_collapse
import octoconapp.shared.generated.resources.friend_fronting_count_expand
import octoconapp.shared.generated.resources.trusted_friend
import org.jetbrains.compose.resources.pluralStringResource
import kotlin.coroutines.CoroutineContext

@Composable
fun AddFriendDialog(
  onDismissRequest: () -> Unit,
  launchAddFriend: (id: String) -> Unit
) {
  var target by savedState("")
  /*val isTargetValid by remember {
    derivedStateOf {
      with(target.trim()) {
        isNotBlank() &&
          (matches(idRegex) || matches(usernameRegex))
      }
    }
  }*/

  val isTargetValid = with(target.trim()) {
    isNotBlank() &&
        (matches(idRegex) || matches(usernameRegex))
  }

  val focusRequester = remember { FocusRequester() }

  AlertDialog(
    icon = {
      Icon(
        Icons.Rounded.PersonAdd,
        contentDescription = null
      )
    },
    title = {
      Text(text = "Add a friend")
    },
    text = {
      LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        item {
          Text("You may add a friend by their username (e.g. \"Username\") or their ID (e.g. \"abcdefg\")")
        }
        item {
          TextField(
            value = target,
            onValueChange = {
              if (it.length > 16) return@TextField
              target = it
            },
            label = { Text("Username or ID") },
            modifier = Modifier.fillMaxWidth().focusRequester(focusRequester)
          )
          LaunchedEffect(true) {
            focusRequester.requestFocus()
          }
        }
      }
    },
    onDismissRequest = onDismissRequest,
    confirmButton = {
      TextButton(
        onClick = {
          launchAddFriend(target)
          onDismissRequest()
        },
        enabled = isTargetValid
      ) {
        Text("Add")
      }
    },
    dismissButton = {
      TextButton(
        onClick = {
          onDismissRequest()
        }
      ) {
        Text("Cancel")
      }
    }
  )
}

@Composable
fun RemoveFriendDialog(
  friend: FriendshipContainer,
  onDismissRequest: () -> Unit,
  launchRemoveFriend: (FriendshipContainer) -> Unit
) {
  AlertDialog(
    icon = {
      Icon(
        Icons.Rounded.PersonRemove,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.error
      )
    },
    title = {
      Text(text = "Remove friend")
    },
    text = {
      LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        item {
          Text("Are you sure you want to remove this friend?")
        }
        item {
          Text(
            "This can't be undone!",
            style = MaterialTheme.typography.bodyMedium.merge(fontWeight = FontWeight.SemiBold)
          )
        }
      }
    },
    onDismissRequest = onDismissRequest,
    confirmButton = {
      Button(
        colors = ButtonDefaults.buttonColors(
          containerColor = MaterialTheme.colorScheme.error,
          contentColor = MaterialTheme.colorScheme.onError
        ),
        onClick = {
          launchRemoveFriend(friend)
          onDismissRequest()
        }
      ) {
        Text(Res.string.confirm.compose)
      }
    },
    dismissButton = {
      TextButton(
        onClick = onDismissRequest
      ) {
        Text("Cancel")
      }
    }
  )
}

@Composable
fun FriendCardPlaceholderImage(
  painter: Painter,
  height: Dp,
  bottomRadius: Dp = 12.dp,
  settings: Settings
) {
  Surface(
    modifier = Modifier.size(height).clip(
      squareifyShape(settings.cornerStyle) {
        RoundedCornerShape(
          topStart = 12.dp,
          topEnd = 12.dp,
          bottomEnd = bottomRadius,
          bottomStart = bottomRadius
        )
      }
    ),
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

private val friendCardBaseHeight = 64.dp

@Composable
fun FriendCard(
  imageContext: CoroutineContext,
  placeholderPainter: Painter,
  friendshipContainer: FriendshipContainer,
  launchViewFriend: (FriendshipContainer) -> Unit,
  launchViewAlter: (FriendshipContainer, ExternalAlter) -> Unit,
  launchOpenFriendSheet: (FriendshipContainer) -> Unit,
  settings: Settings
) {
  val (friend, friendship, fronting) = friendshipContainer

  val haptics = LocalHapticFeedback.current
  val displayName = friend.username ?: friend.id
  var frontingExpanded by savedState(false)

  val height by animateDpAsState(
    if (frontingExpanded)
      friendCardBaseHeight + 12.dp
    else
      friendCardBaseHeight
  )

  val fontSizeMultiplier by animateFloatAsState(if (frontingExpanded) 1.1F else 1F)

  val differential by animateDpAsState(if (frontingExpanded) 0.dp else 12.dp)

  // var imageFailedToLoad by state(false)

  /*val painter = if (!friend.avatarURL.isNullOrBlank()) rememberAsyncImagePainter(
    model =
    ImageRequest.Builder(LocalPlatformContext.current)
      .data(friend.avatarURL)
      .crossfade(true)
      .listener(onError = { _, _ -> imageFailedToLoad = true })
      .build()
  ) else null*/

  ElevatedCard(
    modifier = Modifier
      .fillMaxWidth()
      .clip(CardDefaults.elevatedShape)
      .animateContentSize(),
    colors = CardDefaults.elevatedCardColors(
      containerColor = MaterialTheme.colorScheme.surfaceContainer
    )
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .height(height)
        .clip(
          squareifyShape(settings.cornerStyle) {
            RoundedCornerShape(
              topStart = 12.dp,
              topEnd = 12.dp,
              bottomStart = differential,
              bottomEnd = differential
            )
          }
        )
        .combinedClickable(
          onClick = { launchViewFriend(friendshipContainer) },
          onLongClick = {
            launchOpenFriendSheet(friendshipContainer)
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
          },
          onLongClickLabel = "Open friend context menu"
        )
    ) {
      if (
      // imageFailedToLoad ||
        friend.avatarUrl.isNullOrBlank()) {
        FriendCardPlaceholderImage(placeholderPainter, height, differential, settings)
      } else {
        KamelImage(
          {
            asyncPainterResource(friend.avatarUrl) { coroutineContext = imageContext
              requestBuilder {
                cacheControl("max-age=31536000, immutable")
              }
            }
          },
          // onLoading = { FriendCardPlaceholderImage(placeholderPainter, height, differential) },
          onFailure = { FriendCardPlaceholderImage(placeholderPainter, height, differential, settings) },
          contentDescription = "${displayName}'s avatar",
          modifier = Modifier.size(height).clip(
            squareifyShape(settings.cornerStyle) {
              RoundedCornerShape(
                topStart = 12.dp,
                topEnd = 12.dp,
                bottomStart = differential,
                bottomEnd = differential
              )
            }
          ),
          animationSpec = tween()
        )
      }
      Column(
        modifier = Modifier
          .weight(1f)
          .fillMaxHeight()
          .padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.Center
      ) {
        Text(
          displayName,
          style = MaterialTheme.typography.bodyMedium.merge(
            fontWeight = FontWeight.Medium,
            fontSize = MaterialTheme.typography.bodyMedium.fontSize * fontSizeMultiplier
          ),
          overflow = TextOverflow.Ellipsis,
          maxLines = 1
        )
        Spacer(modifier = Modifier.height(2.dp))
        Row {
          Text(
            if (friend.username == null) "" else friend.id,
            style = MaterialTheme.typography.labelSmall.merge(
              color = MaterialTheme.colorScheme.onSurface,
              fontSize = MaterialTheme.typography.labelSmall.fontSize * fontSizeMultiplier
            ),
            overflow = TextOverflow.Ellipsis,
            maxLines = 1
          )
        }
      }
      AnimatedVisibility(
        visible = friendship.level == FriendshipLevel.TrustedFriend,
        enter = fadeIn() + scaleIn(),
        exit = scaleOut() + fadeOut()
      ) {
        Box(
          modifier = Modifier.fillMaxHeight().padding(end = 24.0.dp - differential),
          contentAlignment = Alignment.CenterEnd
        ) {
          Icon(
            imageVector = Icons.Rounded.Star,
            contentDescription = Res.string.trusted_friend.compose,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.tertiary
          )
        }
      }
    }

    AnimatedVisibility(frontingExpanded, enter = fadeIn(), exit = fadeOut()) {
      HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
    }

    if (fronting.isEmpty()) return@ElevatedCard

    AnimatedVisibility(frontingExpanded) {
      Box(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
        Column(
          modifier = Modifier.padding(12.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          fronting.forEach {
            InertAlterCard(
              alter = it.alter,
              imageContext = imageContext,
              placeholderPainter = placeholderPainter,
              onClick = { launchViewAlter(friendshipContainer, it.alter) },
              isFronting = true,
              frontComment = it.front.comment,
              isPrimary = it.primary,
              settings = settings
            )
          }
        }

      }
    }

    AnimatedVisibility(frontingExpanded, enter = fadeIn(), exit = fadeOut()) {
      HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
    }

    Column(
      modifier = Modifier
        .clip(
          squareifyShape(settings.cornerStyle) {
            RoundedCornerShape(
              topStart = differential,
              topEnd = differential,
              bottomEnd = 12.dp,
              bottomStart = 12.dp
            )
          }
        )
        .clickable {
          frontingExpanded = !frontingExpanded
        }
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 6.dp, horizontal = 24.0.dp - differential),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          if (frontingExpanded)
            Res.string.friend_fronting_count_collapse.compose
          else
            pluralStringResource(
              Res.plurals.friend_fronting_count_expand,
              fronting.size,
              fronting.size
            ),
          // "${fronting.size} fronting (Tap to expand)",
          style = MaterialTheme.typography.labelMedium,
          maxLines = 1
        )
        Icon(
          painter = rememberVectorPainter(
            if (frontingExpanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore
          ),
          contentDescription = null
        )
      }

    }
  }
}

@Composable
fun IncomingFriendRequestCard(
  request: FriendRequestData,
  system: BareSystem,
  placeholderPainter: Painter,
  imageContext: CoroutineContext,
  launchAcceptFriendRequest: (BareSystem) -> Unit,
  launchRejectFriendRequest: (BareSystem) -> Unit,
  settings: Settings
) {
  val displayName = system.username ?: system.id

  ElevatedCard(
    modifier = Modifier
      .fillMaxWidth()
      .clip(CardDefaults.elevatedShape),
    colors = CardDefaults.elevatedCardColors(
      containerColor = MaterialTheme.colorScheme.surfaceContainer
    )
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().height(friendCardBaseHeight),
      verticalAlignment = Alignment.CenterVertically
    ) {
      if (system.avatarUrl.isNullOrBlank()) {
        FriendCardPlaceholderImage(placeholderPainter, friendCardBaseHeight, bottomRadius = 0.dp, settings)
      } else {
        KamelImage(
          {
            asyncPainterResource(system.avatarUrl) {
              coroutineContext = imageContext
              requestBuilder {
                cacheControl("max-age=31536000, immutable")
              }
            }
          },
          // onLoading = { FriendCardPlaceholderImage(placeholderPainter, height, differential) },
          onFailure = { FriendCardPlaceholderImage(placeholderPainter, friendCardBaseHeight, settings = settings) },
          contentDescription = "${displayName}'s avatar",
          modifier = Modifier.size(friendCardBaseHeight).clip(
            squareifyShape(settings.cornerStyle) {
              RoundedCornerShape(
                topStart = 12.dp,
                topEnd = 12.dp,
                bottomEnd = 0.dp,
                bottomStart = 0.dp
              )
            }
          ),
          animationSpec = tween()
        )
      }

      Column(
        modifier = Modifier
          .weight(1f)
          .fillMaxHeight()
          .padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.Center
      ) {
        Text(
          displayName,
          style = MaterialTheme.typography.bodyMedium,
          overflow = TextOverflow.Ellipsis,
          maxLines = 1
        )
        Spacer(modifier = Modifier.height(2.dp))
        Row {
          Text(
            "Sent ${
              request.dateSent.toLocalDateTime(TimeZone.currentSystemDefault()).dateFormat()
            }",
            style = MaterialTheme.typography.labelSmall.merge(color = MaterialTheme.colorScheme.onSurface),
            overflow = TextOverflow.Ellipsis,
            maxLines = 1
          )
        }
      }

      Icon(
        modifier = Modifier.padding(end = 12.dp),
        imageVector = Icons.Rounded.ArrowDownward,
        contentDescription = "Incoming friend request",
        tint = MaterialTheme.colorScheme.tertiary,
      )
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
    Row(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
      horizontalArrangement = Arrangement.End,
    ) {
      FilledTonalButton(onClick = { launchRejectFriendRequest(system) }) {
        Icon(
          imageVector = Icons.Rounded.Close,
          contentDescription = null,
          modifier = Modifier.size(ButtonDefaults.IconSize)
        )
        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
        Text("Reject")
      }
      Spacer(modifier = Modifier.width(8.dp))
      Button(onClick = { launchAcceptFriendRequest(system) }) {
        Icon(
          imageVector = Icons.Rounded.Check,
          contentDescription = null,
          modifier = Modifier.size(ButtonDefaults.IconSize)
        )
        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
        Text("Accept")
      }
    }
  }
}

@Composable
fun OutgoingFriendRequestCard(
  request: FriendRequestData,
  system: BareSystem,
  placeholderPainter: Painter,
  imageContext: CoroutineContext,
  launchCancelFriendRequest: (BareSystem) -> Unit,
  settings: Settings
) {
  val displayName = system.username ?: system.id

  ElevatedCard(
    modifier = Modifier
      .fillMaxWidth()
      .clip(CardDefaults.elevatedShape),
    colors = CardDefaults.elevatedCardColors(
      containerColor = MaterialTheme.colorScheme.surfaceContainer
    )
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().height(friendCardBaseHeight),
      verticalAlignment = Alignment.CenterVertically
    ) {
      if (system.avatarUrl.isNullOrBlank()) {
        FriendCardPlaceholderImage(placeholderPainter, friendCardBaseHeight, settings = settings)
      } else {
        KamelImage(
          {
            asyncPainterResource(system.avatarUrl) {
              coroutineContext = imageContext
              requestBuilder {
                cacheControl("max-age=31536000, immutable")
              }
            }
          },
          // onLoading = { FriendCardPlaceholderImage(placeholderPainter, height, differential) },
          onFailure = { FriendCardPlaceholderImage(placeholderPainter, friendCardBaseHeight, settings = settings) },
          contentDescription = "${displayName}'s avatar",
          modifier = Modifier.size(friendCardBaseHeight).clip(CardDefaults.elevatedShape),
          animationSpec = tween()
        )
      }

      Column(
        modifier = Modifier
          .weight(1f)
          .fillMaxHeight()
          .padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.Center
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          Icon(
            imageVector = Icons.Rounded.ArrowUpward,
            contentDescription = "Outgoing friend request",
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.tertiary
          )
          Text(
            displayName,
            style = MaterialTheme.typography.bodyMedium,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1
          )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Row {
          Text(
            "Sent ${
              request.dateSent.toLocalDateTime(TimeZone.currentSystemDefault()).dateFormat()
            }",
            style = MaterialTheme.typography.labelSmall.merge(color = MaterialTheme.colorScheme.onSurface),
            overflow = TextOverflow.Ellipsis,
            maxLines = 1
          )
        }
      }
      IconButton(
        onClick = { launchCancelFriendRequest(system) }
      ) {
        Icon(
          imageVector = Icons.Rounded.Close,
          contentDescription = "Cancel friend request",
          tint = MaterialTheme.colorScheme.primary
        )
      }
    }
  }
}