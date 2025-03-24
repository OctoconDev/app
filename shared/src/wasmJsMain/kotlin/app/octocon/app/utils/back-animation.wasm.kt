@file:OptIn(ExperimentalDecomposeApi::class)

package app.octocon.app.utils

import androidx.compose.runtime.Composable
import app.octocon.app.ScreenTransitionType.Companion.FADE_ANIMATOR
import app.octocon.app.ScreenTransitionType.Companion.ZOOM_ANIMATOR
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.experimental.stack.animation.PredictiveBackParams
import com.arkivanov.decompose.extensions.compose.experimental.stack.animation.StackAnimator
import com.arkivanov.essenty.backhandler.BackHandler

@Composable
actual fun platformStackAnimator(reduceMotion: Boolean): StackAnimator = if(reduceMotion) FADE_ANIMATOR else ZOOM_ANIMATOR

@OptIn(ExperimentalDecomposeApi::class)
actual fun predictiveBackParams(
  backHandler: BackHandler,
  onBack: () -> Unit
): PredictiveBackParams? = null