package app.octocon.app.ui.compose.utils

import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RichTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TooltipState
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventTimeoutCancellationException
import androidx.compose.ui.input.pointer.PointerType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.onLongClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import app.octocon.app.ui.compose.LocalSpotlightLongPressTimeout
import app.octocon.app.ui.compose.LocalSpotlightTooltipsEnabled
import app.octocon.app.utils.DevicePlatform
import app.octocon.app.utils.compose
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import octoconapp.shared.generated.resources.Res
import octoconapp.shared.generated.resources.ok
import octoconapp.shared.generated.resources.open_tooltip
import octoconapp.shared.generated.resources.spotlight

@Composable
fun SpotlightTooltip(
  title: String,
  description: String,
  modifier: Modifier = Modifier,
  content: @Composable () -> Unit
) {
  val haptics = LocalHapticFeedback.current

  val tooltipsEnabled = LocalSpotlightTooltipsEnabled.current && !DevicePlatform.isWasm
  val longPressTimeout = LocalSpotlightLongPressTimeout.current

  if(tooltipsEnabled) {
    val tooltipState = rememberTooltipState()

    TooltipBox(
      positionProvider = TooltipDefaults.rememberTooltipPositionProvider(),
      modifier = modifier,
      tooltip = {
        RichTooltip(
          title = {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(title)
              Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = Icons.Rounded.Search,
                  contentDescription = null,
                  modifier = Modifier.size(16.dp)
                )
                Text(
                  Res.string.spotlight.compose.uppercase(),
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.tertiary//.copy(alpha = 0.75f)
                )
              }
            }

          },
          // caretSize = TooltipDefaults.caretSize,
          action = {
            TextButton(onClick = tooltipState::dismiss) {
              Text(Res.string.ok.compose)
            }
          },
          colors = TooltipDefaults.richTooltipColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
          )
        ) {
          Text(description)
        }
      },
      state = tooltipState,
      enableUserInput = false
    ) {
      val tooltipScope = rememberCoroutineScope()
      val showWrapper = { mutatePriority: MutatePriority ->
        tooltipScope.launch {
          haptics.performHapticFeedback(HapticFeedbackType.LongPress)
          delay(100)
          haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
        }
        tooltipScope.launch {
          tooltipState.show(mutatePriority)
        }
        Unit
      }

      Box(
        modifier
          .handleGestures(tooltipState, showWrapper, longPressTimeout.timeoutMillis)
          .anchorSemantics(Res.string.open_tooltip.compose, showWrapper
          )
      ) {
        content()
      }
    }
  } else {
    content()
  }
}

// Based on Material3's tooltip implementation
private fun Modifier.handleGestures(
  state: TooltipState,
  showWrapper: (MutatePriority) -> Unit,
  longPressTimeout: Long,
  onStartListening: ((Offset) -> Unit)? = null,
  onStopListening: (() -> Unit)? = null
): Modifier =
  this.pointerInput(state, showWrapper, longPressTimeout) {
    coroutineScope {
      awaitEachGesture {
        // Long press will finish before or after show so keep track of it, in a
        // flow to handle both cases
        val isLongPressedFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)
        val pass = PointerEventPass.Initial
        // wait for the first down press
        val event = awaitFirstDown(pass = pass)
        val inputType = event.type

        if (inputType == PointerType.Touch || inputType == PointerType.Stylus) {
          try {
            // listen to if there is up gesture within the longPressTimeout limit
            onStartListening?.invoke(event.position)
            withTimeout(longPressTimeout) {
              waitForUpOrCancellation(pass = pass)
            }
          } catch (_: PointerEventTimeoutCancellationException) {
            // handle long press - Show the tooltip
            launch(start = CoroutineStart.UNDISPATCHED) {
              try {
                isLongPressedFlow.tryEmit(true)
                showWrapper(MutatePriority.PreventUserInput)
                onStopListening?.invoke()
              } finally {
                if (state.isVisible) {
                  isLongPressedFlow.collectLatest { isLongPressed ->
                    if (!isLongPressed) {
                      state.dismiss()
                    }
                  }
                }
              }
            }

            // consume the children's click handling
            // Long press may still be in progress
            val upEvent = waitForUpOrCancellation(pass = pass)
            upEvent?.consume()
          } finally {
            // isLongPressedFlow.tryEmit(false)
            onStopListening?.invoke()
          }
        }
      }
    }
  }

// Based on Material3's tooltip implementation
private fun Modifier.anchorSemantics(
  label: String,
  showWrapper: (MutatePriority) -> Unit,
): Modifier =
  this.semantics {
    onLongClick(
      label = label,
      action = {
        showWrapper(MutatePriority.Default)
        true
      }
    )
  }