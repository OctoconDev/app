@file:OptIn(ExperimentalDecomposeApi::class)

package app.octocon.app.utils

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import app.octocon.app.ScreenTransitionType.Companion.FADE_ANIMATOR
import app.octocon.app.ScreenTransitionType.Companion.ZOOM_ANIMATOR
import app.octocon.app.ui.compose.LocalChildPanelsMode
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.experimental.stack.animation.PredictiveBackParams
import com.arkivanov.decompose.extensions.compose.experimental.stack.animation.StackAnimator
import com.arkivanov.decompose.extensions.compose.experimental.stack.animation.stackAnimator
import com.arkivanov.decompose.extensions.compose.stack.animation.isFront
import com.arkivanov.decompose.router.panels.ChildPanelsMode
import com.arkivanov.essenty.backhandler.BackHandler

actual fun predictiveBackParams(
  backHandler: BackHandler,
  onBack: () -> Unit
): PredictiveBackParams? =
  PredictiveBackParams(
    backHandler = backHandler,
    onBack = onBack
  )

@Composable
actual fun platformStackAnimator(reduceMotion: Boolean): StackAnimator {
  val childPanelsMode = LocalChildPanelsMode.current

  return if(childPanelsMode == ChildPanelsMode.DUAL) {
    if(reduceMotion) FADE_ANIMATOR else ZOOM_ANIMATOR
  } else {
    iosLikeSlide()
  }
}

private fun iosLikeSlide(animationSpec: FiniteAnimationSpec<Float> = tween()): StackAnimator =
  stackAnimator(animationSpec = animationSpec) { factor, direction ->
    Modifier
      .then(if (direction.isFront) Modifier else Modifier.fade(factor + 1F))
      .offsetXFactor(factor = if (direction.isFront) factor else factor * 0.5F)
  }

private fun Modifier.fade(factor: Float) =
  drawWithContent {
    drawContent()
    drawRect(color = Color(red = 0F, green = 0F, blue = 0F, alpha = (1F - factor) / 4F))
  }

private fun Modifier.offsetXFactor(factor: Float): Modifier =
  layout { measurable, constraints ->
    val placeable = measurable.measure(constraints)

    layout(placeable.width, placeable.height) {
      placeable.placeRelative(x = (placeable.width.toFloat() * factor).toInt(), y = 0)
    }
  }