package app.octocon.app.ui.compose.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.Message
import androidx.compose.material.icons.automirrored.rounded.Notes
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.KeyboardDoubleArrowDown
import androidx.compose.material.icons.rounded.KeyboardDoubleArrowUp
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material.icons.rounded.PersonRemove
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.carousel.CarouselItemScope
import androidx.compose.material3.carousel.CarouselState
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.VectorPainter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.octocon.app.ChangeFrontMode
import app.octocon.app.FontChoice
import app.octocon.app.Settings
import app.octocon.app.api.model.Alter
import app.octocon.app.api.model.MyAlter
import app.octocon.app.api.model.MyTag
import app.octocon.app.ui.compose.components.shared.BottomSheetListItem
import app.octocon.app.ui.compose.components.shared.OctoBottomSheet
import app.octocon.app.ui.compose.components.shared.PermanentTipsNote
import app.octocon.app.ui.compose.screens.GLOBAL_PADDING
import app.octocon.app.ui.compose.theme.LocalOctoTypography
import app.octocon.app.ui.compose.theme.ThemeFromColor
import app.octocon.app.ui.compose.theme.getSubsectionStyle
import app.octocon.app.ui.compose.utils.SpotlightTooltip
import app.octocon.app.ui.model.interfaces.SettingsInterface
import app.octocon.app.utils.DevicePlatform
import app.octocon.app.utils.compose
import app.octocon.app.utils.derive
import app.octocon.app.utils.fuse.Fuse
import app.octocon.app.utils.savedState
import app.octocon.app.utils.sortBySimilarity
import app.octocon.app.utils.state
import io.kamel.core.utils.cacheControl
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import octoconapp.shared.generated.resources.Res
import octoconapp.shared.generated.resources.add_to_front
import octoconapp.shared.generated.resources.all_alters
import octoconapp.shared.generated.resources.alters
import octoconapp.shared.generated.resources.comment
import octoconapp.shared.generated.resources.create_alter
import octoconapp.shared.generated.resources.delete
import octoconapp.shared.generated.resources.delete_alter
import octoconapp.shared.generated.resources.edit_front_comment
import octoconapp.shared.generated.resources.front_comment
import octoconapp.shared.generated.resources.name_avatar
import octoconapp.shared.generated.resources.no_results_found
import octoconapp.shared.generated.resources.permanent_tip_alters_bidirectional_swipe
import octoconapp.shared.generated.resources.permanent_tip_alters_button
import octoconapp.shared.generated.resources.permanent_tip_alters_swipe
import octoconapp.shared.generated.resources.pin_alter
import octoconapp.shared.generated.resources.pinned_alters
import octoconapp.shared.generated.resources.remove_from_front
import octoconapp.shared.generated.resources.search_alters
import octoconapp.shared.generated.resources.set_as_front
import octoconapp.shared.generated.resources.set_main_front
import octoconapp.shared.generated.resources.subtags
import octoconapp.shared.generated.resources.tags
import octoconapp.shared.generated.resources.tooltip_alter_pinning_desc
import octoconapp.shared.generated.resources.tooltip_alter_pinning_title
import octoconapp.shared.generated.resources.tooltip_delete_alter_desc
import octoconapp.shared.generated.resources.tooltip_front_comment_desc
import octoconapp.shared.generated.resources.tooltip_front_comment_title
import octoconapp.shared.generated.resources.tooltip_fronting_desc
import octoconapp.shared.generated.resources.tooltip_fronting_title
import octoconapp.shared.generated.resources.tooltip_main_front_desc
import octoconapp.shared.generated.resources.tooltip_main_front_title
import octoconapp.shared.generated.resources.tooltip_tags_desc
import octoconapp.shared.generated.resources.unnamed_alter
import octoconapp.shared.generated.resources.unpin_alter
import octoconapp.shared.generated.resources.unset_main_front
import org.jetbrains.compose.resources.stringResource
import kotlin.coroutines.CoroutineContext
import kotlin.math.max

private val alterCardHeight = 64.dp
const val swipeThreshold = 0.20f
const val extendedSwipeThreshold = 0.50f
const val swipeReciprocal = 1f / swipeThreshold

@Composable
private fun InnerAlterCard(
  imageContext: CoroutineContext,
  placeholderPainter: Painter,
  alter: Alter,
  isFronting: Boolean,
  isPrimary: Boolean,
  onClick: () -> Unit,
  settings: Settings,
  onLongClick: (() -> Unit)? = null,
  showFrontIndicator: Boolean = true,
  hideSubtext: Boolean = false,
  modifier: Modifier = Modifier,
  imageModifier: Modifier = Modifier
) {
  val useSmallAvatars by derive { settings.useSmallAvatars }

  val showAlterIds by derive { settings.showAlterIds }

  val name = alter.name ?: Res.string.unnamed_alter.compose
  val containerColor by animateColorAsState(
    if (isFronting) MaterialTheme.colorScheme.secondaryContainer
    else MaterialTheme.colorScheme.surfaceContainer
  )

  // var imageFailedToLoad by state(false)

  /*val contentColor by animateColorAsState(
    if (isFronting) MaterialTheme.colorScheme.onSecondaryContainer
    else MaterialTheme.colorScheme.onSurface
  )*/

  val innerImage = @Composable {
    if (alter.avatarUrl.isNullOrBlank()) {
      AlterCardPlaceholderImage(isFronting, placeholderPainter, imageModifier)
    } else {
      KamelImage(
        {
          asyncPainterResource(alter.avatarUrl!!) {
            coroutineContext = imageContext
            requestBuilder {
              cacheControl("max-age=31536000, immutable")
            }
          }
        },
        // onLoading = { PlaceholderImage(isFronting, placeholderPainter) },
        onFailure = { AlterCardPlaceholderImage(isFronting, placeholderPainter) },
        contentDescription = "${alter.name} avatar",
        modifier = imageModifier.fillMaxSize().clip(MaterialTheme.shapes.medium),
        animationSpec = tween()
      )
    }
  }

  ElevatedCard(
    modifier = modifier.height(alterCardHeight).fillMaxWidth(),
    colors = CardDefaults.elevatedCardColors(
      containerColor = containerColor
    )
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().combinedClickable(
        onClick = onClick,
        onLongClick = onLongClick,
        onLongClickLabel = "Alter context menu"
      )
    ) {
      Box(modifier = Modifier.size(alterCardHeight)) {
        if (useSmallAvatars) {
          Box(
            modifier = Modifier.fillMaxSize().padding(8.dp)
          ) {
            innerImage()
          }
        } else {
          innerImage()
        }
      }
      Column(
        modifier = Modifier
          .weight(1f)
          .fillMaxHeight()
          .padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.Center
      ) {
        Text(
          name,
          style = MaterialTheme.typography.labelLarge.merge(
            color = if (isFronting) MaterialTheme.colorScheme.onSecondaryContainer
            else MaterialTheme.colorScheme.onSurface
          ),
          overflow = TextOverflow.Ellipsis,
          maxLines = 1
        )
        val subtext = when {
          !showAlterIds -> alter.pronouns.orEmpty()
          alter is MyAlter ->
            (alter.alias ?: "#${alter.id}") +
                (alter.pronouns?.let { " • $it" }.orEmpty())

          else -> "#${alter.id}" + (alter.pronouns?.let { " • $it" }.orEmpty())
        }
        if (!hideSubtext && subtext.isNotBlank()) {
          Spacer(modifier = Modifier.height(2.dp))
          Row {
            Text(
              subtext,
              style =
              if (isFronting) MaterialTheme.typography.labelSmall.merge(color = MaterialTheme.colorScheme.onSecondaryContainer)
              else MaterialTheme.typography.labelSmall.merge(color = MaterialTheme.colorScheme.onSurface),
              overflow = TextOverflow.Ellipsis,
              maxLines = 1
            )
          }
        }
      }
      if (showFrontIndicator) {
        Box(
          modifier = Modifier
            .fillMaxHeight()
            .padding(12.dp),
          contentAlignment = Alignment.CenterEnd
        ) {
          androidx.compose.animation.AnimatedVisibility(
            visible = isFronting,
            enter = fadeIn() + scaleIn(),
            exit = scaleOut() + fadeOut()
          ) {
            Icon(
              imageVector = if (isPrimary)
                Icons.Rounded.KeyboardDoubleArrowUp
              else
                Icons.Rounded.ArrowUpward,
              contentDescription = if (isPrimary) "Alter is main front" else "Alter is fronting",
              modifier = Modifier.size(24.dp)
            )
          }
        }
      }

    }
  }
}

@Composable
private fun AlterCardFrontComment(
  frontComment: String?
) {
  if (!frontComment.isNullOrBlank()) {
    val comment = buildAnnotatedString {
      appendInlineContent("icon")
      append(frontComment)
    }
    val spAsDp =
      with(LocalDensity.current) { LocalOctoTypography.current.bodySmall.fontSize.toDp() }
    val inlineContent = mapOf(
      "icon" to InlineTextContent(
        Placeholder(
          width = 16.sp,
          height = 12.sp,
          placeholderVerticalAlign = PlaceholderVerticalAlign.Center
        )
      ) {
        Icon(
          imageVector = Icons.AutoMirrored.Rounded.Notes,
          contentDescription = Res.string.front_comment.compose,
          tint = MaterialTheme.colorScheme.secondary,
          modifier = Modifier.size(spAsDp)
        )
      }
    )
    Spacer(modifier = Modifier.height(8.dp))
    Row(
      horizontalArrangement = Arrangement.spacedBy(4.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        comment,
        inlineContent = inlineContent,
        style = MaterialTheme.typography.bodySmall.merge(fontWeight = FontWeight.Medium),
        overflow = TextOverflow.Ellipsis,
        color = MaterialTheme.colorScheme.secondary,
      )
    }
  }
}

@Composable
internal fun InertAlterCard(
  imageContext: CoroutineContext,
  placeholderPainter: Painter,
  alter: Alter,
  isFronting: Boolean,
  isPrimary: Boolean,
  onClick: () -> Unit,
  settings: Settings,
  onLongClick: (() -> Unit)? = null,
  frontComment: String? = null,
  hideSubtext: Boolean = false,
  modifier: Modifier = Modifier
) =
  ThemeFromColor(
    alter.color,
    colorMode = settings.colorMode,
    dynamicColorType = settings.dynamicColorType,
    colorContrastLevel = settings.colorContrastLevel,
    amoledMode = settings.amoledMode
  ) {
    Column(
      modifier = modifier
    ) {
      InnerAlterCard(
        imageContext = imageContext,
        placeholderPainter = placeholderPainter,
        alter = alter,
        isFronting = isFronting,
        isPrimary = isPrimary,
        onClick = onClick,
        hideSubtext = hideSubtext,
        onLongClick = onLongClick,
        settings = settings
      )
      AlterCardFrontComment(frontComment)
    }
  }

private enum class DismissType {
  NONE,
  NORMAL,
  EXTENDED
}

@Composable
private fun SwipeAlterCardWrapper(
  imageContext: CoroutineContext,
  placeholderPainter: Painter,
  alter: Alter,
  isFronting: Boolean,
  isPrimary: Boolean,
  onClick: () -> Unit,
  settings: Settings,
  onLongClick: (() -> Unit)? = null,
  launchStartFront: (Int) -> Unit,
  launchEndFront: (Int) -> Unit,
  launchSetPrimaryFront: (Int?) -> Unit,
  imageModifier: Modifier = Modifier
) {
  val haptics = LocalHapticFeedback.current

  var currentProgress by state(0.0f)

  var dismissType: DismissType by state(DismissType.NONE)
  val density = LocalDensity.current

  val dismissState = remember(isFronting, isPrimary) {
    SwipeToDismissBoxState(
      initialValue = SwipeToDismissBoxValue.Settled,
      density = density,
      confirmValueChange = {
        if (it == SwipeToDismissBoxValue.EndToStart) {
          when (dismissType) {
            DismissType.NONE -> {}

            DismissType.NORMAL -> {
              if (isFronting) {
                launchEndFront(alter.id)
              } else {
                launchStartFront(alter.id)
              }
            }

            DismissType.EXTENDED -> {
              launchSetPrimaryFront(if (isPrimary) null else alter.id)
            }
          }
        }
        return@SwipeToDismissBoxState false
      },
      positionalThreshold = { 0F })
  }

  LaunchedEffect(dismissState.progress) {
    currentProgress = dismissState.progress
    dismissType = dismissState.progress.let {
      when {
        isFronting && it >= extendedSwipeThreshold && it != 1.0F -> DismissType.EXTENDED
        it >= swipeThreshold && it != 1.0F -> DismissType.NORMAL
        else -> DismissType.NONE
      }
      // it >= swipeThreshold && it != 1.0F
    }
  }

  LaunchedEffect(dismissType) {
    if (dismissType == DismissType.NONE) return@LaunchedEffect
    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
  }

  val iconScale = FastOutSlowInEasing.transform(
    (currentProgress * swipeReciprocal)
      .coerceAtMost(1.0f)
  )

  SwipeToDismissBox(
    state = dismissState,
    enableDismissFromStartToEnd = false,
    enableDismissFromEndToStart = true,
    backgroundContent = {
      Box(
        modifier = Modifier.height(alterCardHeight).padding(vertical = 6.dp).fillMaxWidth()
      ) {
        Surface(
          modifier = Modifier
            .fillMaxSize()
            .clip(CardDefaults.elevatedShape)
            .border(
              1.dp,
              MaterialTheme.colorScheme.outlineVariant,
              CardDefaults.elevatedShape
            ),
          color = MaterialTheme.colorScheme.surfaceContainerLow
        ) {
          Row(
            modifier = Modifier
              .fillMaxSize()
              .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
          ) {
            val imageVector = when {
              dismissType == DismissType.EXTENDED -> if (isPrimary) Icons.Rounded.KeyboardDoubleArrowDown else Icons.Rounded.KeyboardDoubleArrowUp
              isFronting -> Icons.Rounded.ArrowDownward
              else -> Icons.Rounded.ArrowUpward
            }
            Icon(
              imageVector = imageVector,
              // tint = swipeIconColor.value,
              contentDescription = null,
              modifier = Modifier.size(24.dp).scale(iconScale)
                .offset(x = (24 * (1.0F - iconScale)).dp)
            )
          }
        }

      }
    },
    content = {
      InnerAlterCard(
        imageContext = imageContext,
        placeholderPainter = placeholderPainter,
        alter = alter,
        isFronting = isFronting,
        isPrimary = isPrimary,
        onClick = onClick,
        onLongClick = onLongClick,
        settings = settings,
        imageModifier = imageModifier
      )
    }
  )
}

@Composable
private fun BidirectionalSwipeAlterCardWrapper(
  imageContext: CoroutineContext,
  placeholderPainter: Painter,
  alter: Alter,
  isFronting: Boolean,
  isPrimary: Boolean,
  onClick: () -> Unit,
  settings: Settings,
  onLongClick: (() -> Unit)? = null,
  launchStartFront: (Int) -> Unit,
  launchEndFront: (Int) -> Unit,
  launchSetPrimaryFront: (Int?) -> Unit,
  imageModifier: Modifier = Modifier
) {
  val haptics = LocalHapticFeedback.current

  var currentProgress by state(0.0f)

  var willDismiss: Boolean by state(false)
  val density = LocalDensity.current

  val dismissState = remember(isFronting, isPrimary) {
    SwipeToDismissBoxState(
      initialValue = SwipeToDismissBoxValue.Settled,
      density = density,
      confirmValueChange = {
        when (it) {
          SwipeToDismissBoxValue.EndToStart -> {
            if (currentProgress < swipeThreshold) return@SwipeToDismissBoxState false
            if (isFronting) {
              launchEndFront(alter.id)
            } else {
              launchStartFront(alter.id)
            }
            return@SwipeToDismissBoxState false
          }

          SwipeToDismissBoxValue.StartToEnd -> {
            if (!isFronting || currentProgress < swipeThreshold) return@SwipeToDismissBoxState false
            launchSetPrimaryFront(if (isPrimary) null else alter.id)
            return@SwipeToDismissBoxState false
          }

          SwipeToDismissBoxValue.Settled -> {
            return@SwipeToDismissBoxState false
          }
        }
      },
      positionalThreshold = { 0F })
  }

  LaunchedEffect(dismissState.progress) {
    currentProgress = dismissState.progress
    willDismiss = dismissState.progress.let { it >= swipeThreshold && it != 1.0F }
  }

  LaunchedEffect(willDismiss) {
    if (!willDismiss) return@LaunchedEffect
    haptics.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate)
  }

  val iconScale = FastOutSlowInEasing.transform(
    (currentProgress * swipeReciprocal)
      .coerceAtMost(1.0f)
  )

  SwipeToDismissBox(
    state = dismissState,
    enableDismissFromStartToEnd = isFronting,
    enableDismissFromEndToStart = true,
    backgroundContent = {
      Box(
        modifier = Modifier.height(alterCardHeight).padding(vertical = 6.dp).fillMaxWidth()
      ) {
        Surface(
          modifier = Modifier
            .fillMaxSize()
            .clip(CardDefaults.elevatedShape)
            .border(
              1.dp,
              MaterialTheme.colorScheme.outlineVariant,
              CardDefaults.elevatedShape
            ),
          color = MaterialTheme.colorScheme.surfaceContainerLow
        ) {
          Row(
            modifier = Modifier
              .fillMaxSize()
              .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Icon(
              imageVector = if (isPrimary)
                Icons.Rounded.KeyboardDoubleArrowDown
              else
                Icons.Rounded.KeyboardDoubleArrowUp,
              // tint = swipeIconColor.value,
              contentDescription = null,
              modifier = Modifier.size(24.dp).scale(if (isFronting) iconScale else 0f)
                .offset(x = (-24 * (1.0F - iconScale)).dp)
            )
            Icon(
              imageVector = if (isFronting) Icons.Rounded.ArrowDownward
              else
                Icons.Rounded.ArrowUpward,
              // tint = swipeIconColor.value,
              contentDescription = null,
              modifier = Modifier.size(24.dp).scale(iconScale)
                .offset(x = (24 * (1.0F - iconScale)).dp)
            )
          }
        }

      }
    },
    content = {
      InnerAlterCard(
        imageContext = imageContext,
        placeholderPainter = placeholderPainter,
        alter = alter,
        isFronting = isFronting,
        isPrimary = isPrimary,
        onClick = onClick,
        onLongClick = onLongClick,
        settings = settings
      )
    }
  )
}

private val frontButtonIconSize = 24.dp
private val frontButtonWidth = frontButtonIconSize * 2

@Composable
private fun ButtonAlterCardWrapper(
  imageContext: CoroutineContext,
  placeholderPainter: Painter,
  alter: Alter,
  isFronting: Boolean,
  isPrimary: Boolean,
  onClick: () -> Unit,
  settings: Settings,
  onLongClick: (() -> Unit)? = null,
  launchStartFront: (Int) -> Unit,
  launchEndFront: (Int) -> Unit,
  launchSetPrimaryFront: (Int?) -> Unit,
  imageModifier: Modifier = Modifier
) {
  val haptics = LocalHapticFeedback.current
  val containerColor by animateColorAsState(
    if (isFronting) MaterialTheme.colorScheme.secondaryContainer
    else MaterialTheme.colorScheme.surfaceContainer
  )

  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    InnerAlterCard(
      imageContext = imageContext,
      placeholderPainter = placeholderPainter,
      alter = alter,
      isFronting = isFronting,
      isPrimary = isPrimary,
      onClick = onClick,
      onLongClick = onLongClick,
      showFrontIndicator = false,
      modifier = Modifier.weight(1f),
      settings = settings,
      imageModifier = imageModifier
    )

    Surface(
      modifier = Modifier.height(alterCardHeight).width(frontButtonWidth)
        .clip(MaterialTheme.shapes.medium).combinedClickable(
          onClick = {
            if (isFronting) {
              launchEndFront(alter.id)
              haptics.performHapticFeedback(HapticFeedbackType.ToggleOff)
            } else {
              launchStartFront(alter.id)
              haptics.performHapticFeedback(HapticFeedbackType.ToggleOn)
            }
          },
          onClickLabel = if (isFronting) "End fronting" else "Start fronting",
          onLongClick = if (isFronting) {
            {
              launchSetPrimaryFront(if (isPrimary) null else alter.id)
              haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            }
          } else null,
          onLongClickLabel = if (isFronting) Res.string.set_main_front.compose else null
        ).semantics { role = Role.Button },
      color = containerColor,
      shadowElevation = 1.0.dp
    ) {
      Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = when {
            isPrimary && isFronting -> Icons.Rounded.KeyboardDoubleArrowUp
            isFronting -> Icons.Rounded.ArrowUpward
            else -> Icons.Rounded.ArrowDownward
          },
          contentDescription = if (isPrimary) "Alter is main front" else "Alter is fronting",
          modifier = Modifier.size(frontButtonIconSize)
        )
      }
    }
  }
}

@Composable
internal fun AlterCard(
  imageContext: CoroutineContext,
  placeholderPainter: Painter,
  alter: Alter,
  isFronting: Boolean,
  isPrimary: Boolean,
  onClick: () -> Unit,
  frontComment: String? = null,
  onLongClick: (() -> Unit)? = null,
  settings: Settings,
  launchStartFront: (Int) -> Unit,
  launchEndFront: (Int) -> Unit,
  launchSetPrimaryFront: (Int?) -> Unit,
  changeFrontMode: ChangeFrontMode,
  modifier: Modifier = Modifier,
  imageModifier: Modifier = Modifier
) {
  ThemeFromColor(
    alter.color,
    colorMode = settings.colorMode,
    dynamicColorType = settings.dynamicColorType,
    colorContrastLevel = settings.colorContrastLevel,
    amoledMode = settings.amoledMode
  ) {
    Column(
      modifier = modifier
    ) {
      when (changeFrontMode) {
        ChangeFrontMode.SWIPE -> SwipeAlterCardWrapper(
          imageContext,
          placeholderPainter,
          alter,
          isFronting,
          isPrimary,
          onClick,
          settings,
          onLongClick,
          launchStartFront,
          launchEndFront,
          launchSetPrimaryFront,
          imageModifier
        )

        ChangeFrontMode.BIDIRECTIONAL_SWIPE -> BidirectionalSwipeAlterCardWrapper(
          imageContext,
          placeholderPainter,
          alter,
          isFronting,
          isPrimary,
          onClick,
          settings,
          onLongClick,
          launchStartFront,
          launchEndFront,
          launchSetPrimaryFront,
          imageModifier
        )

        ChangeFrontMode.BUTTON -> ButtonAlterCardWrapper(
          imageContext,
          placeholderPainter,
          alter,
          isFronting,
          isPrimary,
          onClick,
          settings,
          onLongClick,
          launchStartFront,
          launchEndFront,
          launchSetPrimaryFront,
          imageModifier
        )
      }

      AlterCardFrontComment(frontComment)
    }
  }
}

@Composable
internal fun AlterCardPlaceholderImage(
  isFronting: Boolean,
  placeholderPainter: Painter,
  imageModifier: Modifier = Modifier
) {
  val containerColor = animateColorAsState(
    if (isFronting) MaterialTheme.colorScheme.secondary
    else MaterialTheme.colorScheme.surfaceContainerHigh
  )

  val iconColor = animateColorAsState(
    if (isFronting) MaterialTheme.colorScheme.onSecondary
    else MaterialTheme.colorScheme.secondary
  )

  Surface(
    modifier = imageModifier.fillMaxSize().clip(MaterialTheme.shapes.medium),
    color = containerColor.value,
  ) {
    Box(
      contentAlignment = Alignment.Center
    ) {
      Icon(
        painter = placeholderPainter,
        contentDescription = null,
        modifier = Modifier.size(32.dp),
        tint = iconColor.value
      )
    }
  }
}

@Composable
fun AlterCarousel(
  carouselState: CarouselState,
  alters: List<MyAlter>,
  onClick: (Int) -> Unit,
  onDoubleClick: (Int) -> Unit,
  onLongClick: (Int) -> Unit,
  settingsInterface: SettingsInterface,
  imageContext: CoroutineContext,
) {
  val settings by settingsInterface.collectAsState()
  val useSmallAvatars by derive { settings.useSmallAvatars }

  val placeholderPainter = rememberVectorPainter(Icons.Rounded.Person)

  val carouselHeight = if(useSmallAvatars) 100.dp else 160.dp

  HorizontalMultiBrowseCarousel(
    state = carouselState,
    modifier = Modifier.height(carouselHeight).fillMaxWidth(),
    preferredItemWidth = carouselHeight,
    itemSpacing = 8.dp
  ) { index ->
    val alter = alters[index]
    AlterCarouselItem(
      alter,
      onClick,
      onDoubleClick,
      onLongClick,
      imageContext,
      settings,
      placeholderPainter,
      carouselHeight,
      useSmallAvatars
    )
  }
}

private const val BRIGHTNESS_START_THRESHOLD = 0.8f

@Composable
fun CarouselItemScope.AlterCarouselItem(
  alter: MyAlter,
  onClick: (Int) -> Unit,
  onDoubleClick: (Int) -> Unit,
  onLongClick: (Int) -> Unit,
  imageContext: CoroutineContext,
  settings: Settings,
  placeholderPainter: VectorPainter,
  height: Dp,
  useSmallAvatars: Boolean
) {
  val focusPercentage = carouselItemDrawInfo.size / carouselItemDrawInfo.maxSize

  Box(
    modifier = Modifier
      .size(height)
      .maskClip(MaterialTheme.shapes.extraLarge)
      .combinedClickable(
        onClick = { onClick(alter.id) },
        onDoubleClick = { onDoubleClick(alter.id) },
        onLongClick = { onLongClick(alter.id) }
      )
  ) {
    if (alter.avatarUrl.isNullOrBlank()) {
      ThemeFromColor(
        alter.color,
        colorMode = settings.colorMode,
        dynamicColorType = settings.dynamicColorType,
        colorContrastLevel = settings.colorContrastLevel,
        amoledMode = settings.amoledMode
      ) {
        AlterCarouselPlaceholderImage(alter, placeholderPainter, focusPercentage, settings.fontChoice, useSmallAvatars)
      }
    } else {
      val normalized = max(0f, (focusPercentage - BRIGHTNESS_START_THRESHOLD) / (1f - BRIGHTNESS_START_THRESHOLD))
      val eased = FastOutSlowInEasing.transform(normalized)
      /*val brightness = eased * -60f

      // 0f..10f (1 should be default)
      val contrast = 1f
      val colorMatrix = floatArrayOf(
        contrast, 0f, 0f, 0f, brightness,
        0f, contrast, 0f, 0f, brightness,
        0f, 0f, contrast, 0f, brightness,
        0f, 0f, 0f, 1f, 0f
      )*/

      Box(Modifier.fillMaxSize()) {
        KamelImage(
          {
            asyncPainterResource(alter.avatarUrl) {
              coroutineContext = imageContext
              requestBuilder {
                cacheControl("max-age=31536000, immutable")
              }
            }
          },
          // onLoading = { PlaceholderImage(isFronting, placeholderPainter) },
          onFailure = { AlterCarouselPlaceholderImage(alter, placeholderPainter, focusPercentage, settings.fontChoice, useSmallAvatars) },
          contentDescription = stringResource(Res.string.name_avatar, alter.name ?: Res.string.unnamed_alter.compose),
          modifier = Modifier.fillMaxSize(),
          animationSpec = tween(),
          contentScale = ContentScale.Crop
        )

        Box(
          modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.4f * eased))
        )

        Text(
          alter.name ?: Res.string.unnamed_alter.compose,
          color = Color.White,
          style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold, fontSize = 14.sp, fontFamily = settings.fontChoice.headingFont),
          modifier = Modifier.align(Alignment.BottomStart).padding(16.dp).alpha(eased)
        )
      }
    }
  }
}


@Composable
private fun AlterCarouselPlaceholderImage(
  alter: MyAlter,
  placeholderPainter: Painter,
  focusPercentage: Float,
  fontChoice: FontChoice,
  useSmallAvatars: Boolean,
  modifier: Modifier = Modifier
) {
  Surface(
    modifier = modifier.fillMaxSize(),
    color = MaterialTheme.colorScheme.surfaceContainerHigh,
  ) {
    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      item {
        Icon(
          painter = placeholderPainter,
          contentDescription = null,
          modifier = Modifier.size(if(useSmallAvatars) 30.dp else 48.dp).animateItem(),
          tint = MaterialTheme.colorScheme.secondary
        )
      }

      if(focusPercentage >= 0.97f) {
        item {
          Text(
            alter.name ?: Res.string.unnamed_alter.compose,
            color = MaterialTheme.colorScheme.secondary,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold, fontSize = 14.sp, fontFamily = fontChoice.headingFont),
            modifier = Modifier.padding(top = 2.dp).animateItem()
          )
        }
      }
    }
  }
}


@Composable
fun DeleteAlterDialog(
  alter: MyAlter,
  launchDeleteAlter: (Int) -> Unit,
  onDismissRequest: () -> Unit,
  afterDelete: (() -> Unit)? = null
) {
  AlertDialog(
    icon = {
      Icon(
        Icons.Rounded.Delete,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.error
      )
    },
    title = {
      Text(text = "Delete alter")
    },
    text = {
      Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Text("Are you sure you want to delete ${alter.name ?: "Unnamed alter"}?")
        Text(
          "This can't be undone!",
          style = MaterialTheme.typography.bodyMedium.merge(fontWeight = FontWeight.SemiBold)
        )
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
          launchDeleteAlter(alter.id)
          afterDelete?.invoke()
          onDismissRequest()
        }
      ) {
        Text(Res.string.delete.compose)
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
fun CreateAlterDialog(
  launchCreateAlter: (String) -> Unit,
  onDismissRequest: () -> Unit
) {
  var name by state("")

  val focusRequester = remember { FocusRequester() }

  AlertDialog(
    icon = {
      Icon(
        Icons.Rounded.PersonAdd,
        contentDescription = "Create alter icon"
      )
    },
    title = {
      Text(text = Res.string.create_alter.compose)
    },
    text = {
      LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        item {
          TextField(
            value = name,
            onValueChange = {
              if (it.length > 80) return@TextField
              name = it
            },
            label = { Text("Name") },
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
          launchCreateAlter(name)
          onDismissRequest()
        }
      ) {
        Text("Create")
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
fun EditFrontDialog(
  frontID: String,
  comment: String,
  alterName: String,
  launchEditComment: (String, String) -> Unit,
  onDismissRequest: () -> Unit
) {
  var editedComment by savedState(comment)

  val focusRequester = remember { FocusRequester() }

  AlertDialog(
    icon = {
      Icon(
        Icons.Rounded.Edit,
        contentDescription = "Edit front icon"
      )
    },
    title = {
      Text(text = "Edit front for $alterName")
    },
    text = {
      LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        item {
          TextField(
            value = editedComment,
            onValueChange = {
              if (it.length > 50) return@TextField
              editedComment = it
            },
            label = { Text(Res.string.comment.compose) },
            modifier = Modifier.focusRequester(focusRequester)
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
          launchEditComment(frontID, editedComment)
          onDismissRequest()
        }
      ) {
        Text("Confirm")
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
fun AlterContextSheet(
  onDismissRequest: () -> Unit,
  selectedAlter: Int,
  isFronting: Boolean,
  isPrimary: Boolean,
  isPinned: Boolean,
  launchEditAlter: (Int) -> Unit,
  launchDeleteAlter: (Int) -> Unit,
  launchSetAsFront: (Int) -> Unit,
  launchAddToFront: (Int) -> Unit,
  launchEditFrontComment: (Int) -> Unit,
  launchRemoveFromFront: (Int) -> Unit,
  launchSetAsPrimaryFront: (Int?) -> Unit,
  launchPinOrUnpin: (Int, Boolean) -> Unit,
  extraContent: (@Composable () -> Unit)? = null
) {
  OctoBottomSheet(
    onDismissRequest = onDismissRequest
  ) {
    if (isFronting) {
      SpotlightTooltip(
        title = Res.string.tooltip_main_front_title.compose,
        description = Res.string.tooltip_main_front_desc.compose
      ) {
        if (isPrimary) {
          BottomSheetListItem(
            imageVector = Icons.Rounded.KeyboardDoubleArrowDown,
            title = Res.string.unset_main_front.compose
          ) {
            launchSetAsPrimaryFront(null)
            onDismissRequest()
          }
        } else {
          BottomSheetListItem(
            imageVector = Icons.Rounded.KeyboardDoubleArrowUp,
            title = Res.string.set_main_front.compose
          ) {
            launchSetAsPrimaryFront(selectedAlter)
            onDismissRequest()
          }
        }
      }
      SpotlightTooltip(
        title = Res.string.tooltip_front_comment_title.compose,
        description = Res.string.tooltip_front_comment_desc.compose
      ) {
        BottomSheetListItem(
          imageVector = Icons.AutoMirrored.Rounded.Message,
          title = Res.string.edit_front_comment.compose
        ) {
          launchEditFrontComment(selectedAlter)
          onDismissRequest()
        }
      }
      SpotlightTooltip(
        title = Res.string.tooltip_fronting_title.compose,
        description = Res.string.tooltip_fronting_desc.compose
      ) {
        BottomSheetListItem(
          imageVector = Icons.Rounded.PersonRemove,
          title = Res.string.remove_from_front.compose
        ) {
          launchRemoveFromFront(selectedAlter)
          onDismissRequest()
        }
      }
    } else {
      SpotlightTooltip(
        title = Res.string.tooltip_fronting_title.compose,
        description = Res.string.tooltip_fronting_desc.compose
      ) {
        BottomSheetListItem(
          imageVector = Icons.Rounded.PushPin,
          title = Res.string.set_as_front.compose
        ) {
          launchSetAsFront(selectedAlter)
          onDismissRequest()
        }
      }
      SpotlightTooltip(
        title = Res.string.tooltip_fronting_title.compose,
        description = Res.string.tooltip_fronting_desc.compose
      ) {
        BottomSheetListItem(
          imageVector = Icons.Rounded.PersonAdd,
          title = Res.string.add_to_front.compose
        ) {
          launchAddToFront(selectedAlter)
          onDismissRequest()
        }
      }
    }
    SpotlightTooltip(
      title = Res.string.tooltip_alter_pinning_title.compose,
      description = Res.string.tooltip_alter_pinning_desc.compose
    ) {
      BottomSheetListItem(
        imageVector = Icons.Rounded.PushPin,
        title = if(isPinned) Res.string.unpin_alter.compose else Res.string.pin_alter.compose
      ) {
        launchPinOrUnpin(selectedAlter, !isPinned)
        onDismissRequest()
      }
    }
    BottomSheetListItem(
      imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
      title = "View alter"
    ) {
      launchEditAlter(selectedAlter)
      onDismissRequest()
    }
    SpotlightTooltip(
      title = Res.string.delete_alter.compose,
      description = Res.string.tooltip_delete_alter_desc.compose
    ) {
      BottomSheetListItem(
        imageVector = Icons.Rounded.Delete,
        iconTint = MaterialTheme.colorScheme.error,
        title = Res.string.delete_alter.compose
      ) {
        launchDeleteAlter(selectedAlter)
        onDismissRequest()
      }

    }
    extraContent?.invoke()
  }
}

@Composable
fun LazyAlterList(
  allAlters: List<MyAlter>,
  sortedAlters: List<MyAlter>,
  tags: List<MyTag>,
  pinnedAlters: List<Int> = emptyList(),
  settings: SettingsInterface,
  imageContext: CoroutineContext,
  placeholderPainter: Painter,
  folderPainter: Painter,
  setSelectedAlter: (Int) -> Unit,
  setSelectedTag: ((String) -> Unit),
  frontingData: Set<Pair<Int, String?>>,
  primaryFront: Int?,
  launchStartFront: (Int) -> Unit,
  launchEndFront: (Int) -> Unit,
  launchSetPrimaryFront: (Int?) -> Unit,
  launchViewAlter: (Int) -> Unit,
  launchOpenTag: (String) -> Unit,
  excludeList: List<Int> = emptyList(),
  lazyListState: LazyListState = rememberLazyListState(),
  nestedScrollConnection: NestedScrollConnection? = null,
  emptyContent: (LazyListScope.() -> Unit)? = null,
  extraContent: (LazyListScope.() -> Unit)? = null,
  isNested: Boolean
) {
  val haptics = LocalHapticFeedback.current

  val settingsData by settings.collectAsState()
  val tagsCollapsed by derive { settingsData.tagsCollapsed }

  val searchBarVisible = !DevicePlatform.isWasm && sortedAlters.size > 5

  @Suppress("LocalVariableName")
  val unnamed_alter = Res.string.unnamed_alter.compose

  val changeFrontMode by derive { settingsData.changeFrontMode }

  val showPermanentTips by derive { settingsData.showPermanentTips }

  var firstItemNotVisible by state(false)
  val fuse = remember { Fuse() }
  var isSearching by state(false)
  var searchQuery by state("")
  var searchResults by state(sortedAlters.filter { it.id !in excludeList })

  LaunchedEffect(searchQuery, sortedAlters) {
    var setSearchingJob: Job? = null
    val result = if (searchQuery.isBlank()) {
      sortedAlters.filter { it.id !in excludeList }
    } else {
      // Set isSearching if search takes more than 100ms
      setSearchingJob = launch {
        delay(100)
        isSearching = true
      }
      sortedAlters.sortBySimilarity({ it.name ?: unnamed_alter }, searchQuery, fuse = fuse)
    }

    setSearchingJob?.cancel()
    searchResults = result
    isSearching = false
  }

  val carouselState = rememberSaveable(pinnedAlters, saver = CarouselState.Saver) {
    CarouselState(
      currentItem = 0,
      currentItemOffsetFraction = 0F,
      itemCount = { pinnedAlters.size }
    )
  }

  LaunchedEffect(lazyListState) {
    snapshotFlow { lazyListState.firstVisibleItemIndex }
      .distinctUntilChanged()
      .collect {
        firstItemNotVisible = it > 0
      }
  }

  Box {
    LazyColumn(
      modifier = Modifier.fillMaxHeight().let {
        if (nestedScrollConnection != null) {
          it.nestedScroll(nestedScrollConnection)
        } else { it }
      }.imePadding(),
      state = lazyListState,
      verticalArrangement = Arrangement.spacedBy(12.dp),
      contentPadding = PaddingValues(GLOBAL_PADDING)
    ) {
      if (searchBarVisible) {
        item(key = "__search_bar") {
          OctoSearchBar(
            searchQuery = searchQuery,
            setSearchQuery = { searchQuery = it },
            isSearching = isSearching,
            placeholderText = Res.string.search_alters.compose,
            modifier = Modifier.fillMaxWidth()
          )
        }
      }
      if (searchQuery.isBlank() && tags.isNotEmpty()) {
        item(key = "__tags") {
          if (isNested) {
            Text(
              if (isNested) Res.string.subtags.compose else Res.string.tags.compose,
              modifier = Modifier.fillMaxWidth().let {
                if (searchBarVisible) it.padding(vertical = 12.dp) else it.padding(
                  bottom = 12.dp,
                  top = 8.dp
                )
              },
              style = getSubsectionStyle(settingsData.fontSizeScalar)
            )
          } else {
            SpotlightTooltip(
              title = Res.string.tags.compose,
              description = Res.string.tooltip_tags_desc.compose
            ) {
              Column(
                modifier = Modifier.fillMaxWidth()
                  .clickable(
                    onClick = {
                      settings.setTagsCollapsed(!tagsCollapsed)
                    }
                  )
              ) {
                Row(
                  modifier = Modifier.fillMaxWidth().let {
                    if (searchBarVisible) it.padding(vertical = 12.dp) else it.padding(
                      bottom = 12.dp,
                      top = 8.dp
                    )
                  },
                ) {
                  Text(
                    Res.string.tags.compose,
                    modifier = Modifier.weight(1f),
                    style = getSubsectionStyle(settingsData.fontSizeScalar)
                  )
                  Spacer(modifier = Modifier.width(16.dp))
                  Icon(
                    imageVector = if (tagsCollapsed) Icons.Rounded.ExpandMore else Icons.Rounded.ExpandLess,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                  )
                  Spacer(modifier = Modifier.width(12.dp))
                }
              }
            }
          }
        }
        if (!tagsCollapsed || isNested) {
          items(tags, key = { it.id }) {
            TagCard(
              tag = it,
              iconPainter = folderPainter,
              onClick = {
                launchOpenTag(it.id)
              },
              onLongClick = {
                setSelectedTag(it.id)
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
              },
              modifier = Modifier.animateItem(),
              settings = settingsData
            )
          }
        }
      }
      if (searchQuery.isBlank()) {
        if ((emptyContent == null && sortedAlters.isNotEmpty()) || emptyContent != null) {
          if(pinnedAlters.isNotEmpty()) {
            item(key = "__pinned_alters_label") {
              SpotlightTooltip(
                title = Res.string.tooltip_alter_pinning_title.compose,
                description = Res.string.tooltip_alter_pinning_desc.compose
              ) {
                Text(
                  Res.string.pinned_alters.compose,
                  modifier = Modifier.fillMaxWidth()
                    .padding(
                      bottom = 12.dp,
                      top = if ((tags.isNotEmpty() && !tagsCollapsed) || searchBarVisible) 12.dp else 8.dp
                    ),
                  style = getSubsectionStyle(settingsData.fontSizeScalar)
                )
              }
            }

            item {
              AlterCarousel(
                carouselState = carouselState,
                alters = remember(allAlters, pinnedAlters) { pinnedAlters.mapNotNull { id -> allAlters.find { it.id == id } } },
                onClick = launchViewAlter,
                onDoubleClick = { id ->
                  if(id in frontingData.map { it.first }) {
                    launchEndFront(id)
                  } else {
                    launchStartFront(id)
                  }
                  haptics.performHapticFeedback(HapticFeedbackType.Confirm)
                },
                onLongClick = {
                  setSelectedAlter(it)
                  haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                },
                settingsInterface = settings,
                imageContext = imageContext
              )
            }
          }
          item(key = "__alters_label") {
            Text(
              if (isNested) Res.string.alters.compose else Res.string.all_alters.compose,
              modifier = Modifier.fillMaxWidth()
                .padding(
                  bottom = 12.dp,
                  top = if ((tags.isNotEmpty() && !tagsCollapsed) || searchBarVisible) 12.dp else 8.dp
                ),
              style = getSubsectionStyle(settingsData.fontSizeScalar)
            )
          }
        }
      }
      if (sortedAlters.isEmpty()) {
        emptyContent?.let { it() }
      } else if (searchResults.isEmpty()) {
        item(key = "__no_results") {
          Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.Center
          ) {
            Text(
              Res.string.no_results_found.compose,
              style = MaterialTheme.typography.bodyLarge
            )
          }
        }
      }
      if (showPermanentTips) {
        item(key = "__permanent_tips") {
          PermanentTipsNote(
            text = stringResource(
              when (changeFrontMode) {
                ChangeFrontMode.SWIPE -> Res.string.permanent_tip_alters_swipe
                ChangeFrontMode.BIDIRECTIONAL_SWIPE -> Res.string.permanent_tip_alters_bidirectional_swipe
                ChangeFrontMode.BUTTON -> Res.string.permanent_tip_alters_button
              }
            ),
            modifier = Modifier.animateItem()
          )
        }
      }

      items(searchResults.size, key = { searchResults[it].id }) { index ->
        val alter = searchResults[index]
        AlterCard(
          imageContext = imageContext,
          placeholderPainter = placeholderPainter,
          alter = alter,
          isFronting = alter.id in frontingData.map { it.first },
          frontComment = frontingData.find { it.first == alter.id }?.second,
          isPrimary = primaryFront == alter.id,
          onClick = { launchViewAlter(alter.id) },
          onLongClick = {
            setSelectedAlter(alter.id)
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
          },
          changeFrontMode = changeFrontMode,
          launchStartFront = launchStartFront,
          launchEndFront = launchEndFront,
          launchSetPrimaryFront = launchSetPrimaryFront,
          modifier = Modifier.animateItem(),
          settings = settingsData
        )
      }
      extraContent?.invoke(this)
    }
  }
}

@Composable
fun OctoSearchBar(
  searchQuery: String,
  setSearchQuery: (String) -> Unit,
  isSearching: Boolean,
  placeholderText: String,
  modifier: Modifier = Modifier
) {
  SearchBarDefaults.InputField(
    query = searchQuery,
    onQueryChange = {
      if (it.length <= 20) setSearchQuery(it)
    },
    onSearch = {},
    expanded = false,
    onExpandedChange = {},
    enabled = true,
    placeholder = { Text(placeholderText) },
    leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
    trailingIcon = {
      if (isSearching) {
        CircularProgressIndicator(
          modifier = Modifier.size(24.dp),
          strokeWidth = 3.dp
        )
      }
    },
    colors = SearchBarDefaults.inputFieldColors(),
    interactionSource = null,
    modifier = modifier.background(
      MaterialTheme.colorScheme.surfaceContainerHighest,
      shape = MaterialTheme.shapes.extraLarge
    )
  )
  /*DockedSearchBar(
    query = searchQuery,
    onQueryChange = {
      if (it.length <= 20) setSearchQuery(it)
    },
    active = false,
    onActiveChange = {},
    onSearch = {},
    modifier = modifier,
    placeholder = { Text(placeholderText) },
    leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
    trailingIcon = {
      if (isSearching) {
        CircularProgressIndicator(
          modifier = Modifier.size(24.dp),
          strokeWidth = 3.dp
        )
      }
    }
  ) {}
  DockedSearchBar(
    inputField = {
      SearchBarDefaults.InputField(
        query = searchQuery,
        onQueryChange = {
          if (it.length <= 20) setSearchQuery(it)
        },
        onSearch = {},
        expanded = false,
        onExpandedChange = onActiveChange,
        enabled = true,
        placeholder = { Text(placeholderText) },
        leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
        trailingIcon = {
          if (isSearching) {
            CircularProgressIndicator(
              modifier = Modifier.size(24.dp),
              strokeWidth = 3.dp
            )
          }
        },
        colors = SearchBarDefaults.colors(),
        interactionSource = null,
      )
    },
    expanded = false,
    onExpandedChange = onActiveChange,
    modifier = modifier,
    shape = SearchBarDefaults.dockedShape,
    colors = SearchBarDefaults.colors(),
    tonalElevation = SearchBarDefaults.TonalElevation,
    shadowElevation = SearchBarDefaults.ShadowElevation,
  ) {}*/
}