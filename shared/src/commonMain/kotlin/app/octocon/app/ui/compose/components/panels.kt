package app.octocon.app.ui.compose.components

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import app.octocon.app.ScreenTransitionType.Companion.FADE_ANIMATOR
import app.octocon.app.ui.compose.InternalOctoconLayoutApi
import app.octocon.app.ui.compose.LocalChildPanelsMode
import app.octocon.app.ui.compose.LocalDesiredChildPanelsMode
import app.octocon.app.utils.abifix.FixedABIChildPanels
import app.octocon.app.utils.abifix.FixedABIHorizontalChildPanelsLayout
import app.octocon.app.utils.platformStackAnimator
import app.octocon.app.utils.predictiveBackParams
import com.arkivanov.decompose.Child
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.experimental.panels.ChildPanelsAnimators
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.arkivanov.decompose.router.panels.ChildPanels
import com.arkivanov.decompose.router.panels.ChildPanelsMode
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackHandler

@OptIn(ExperimentalDecomposeApi::class, InternalOctoconLayoutApi::class)
@Composable
fun ChildPanelsModeChangedEffect(onModeChanged: (ChildPanelsMode) -> Unit) {
  val mode = LocalDesiredChildPanelsMode.current

  DisposableEffect(mode) {
    onModeChanged(mode)
    onDispose {}
  }
}

@ExperimentalDecomposeApi
@Composable
fun <MC : Any, MT : Any, DC : Any, DT : Any> DoublePanels(
  panelsValue: Value<ChildPanels<MC, MT, DC, DT, Nothing, Nothing>>,
  setMode: (ChildPanelsMode) -> Unit,
  backHandler: BackHandler,
  onBackPressed: () -> Unit,
  main: @Composable AnimatedVisibilityScope.(Child.Created<MC, MT>) -> Unit,
  details: @Composable AnimatedVisibilityScope.(Child.Created<DC, DT>) -> Unit,
  placeholder: @Composable AnimatedVisibilityScope.() -> Unit = {},
  reduceMotion: Boolean,
  modifier: Modifier = Modifier,
) {
  val panels by panelsValue.subscribeAsState()

  ChildPanelsModeChangedEffect(setMode)
  CompositionLocalProvider(
    LocalChildPanelsMode provides panels.mode
  ) {
    FixedABIChildPanels(
      panels = panels,
      mainChild = main,
      detailsChild = details,
      extraChild = {},
      modifier = modifier,
      secondPanelPlaceholder = placeholder,
      layout = remember { FixedABIHorizontalChildPanelsLayout(dualWeights = 0.40f to 0.60f) },
      animators = ChildPanelsAnimators(single = platformStackAnimator(reduceMotion), dual = FADE_ANIMATOR to platformStackAnimator(reduceMotion)),
      predictiveBackParams = { predictiveBackParams(backHandler, onBackPressed) },
    )
  }
}

@ExperimentalDecomposeApi
@Composable
fun <MC : Any, MT : Any, DC : Any, DT : Any, EC : Any, ET : Any> TriplePanels(
  panelsValue: Value<ChildPanels<MC, MT, DC, DT, EC, ET>>,
  setMode: (ChildPanelsMode) -> Unit,
  backHandler: BackHandler,
  onBackPressed: () -> Unit,
  main: @Composable AnimatedVisibilityScope.(Child.Created<MC, MT>) -> Unit,
  details: @Composable AnimatedVisibilityScope.(Child.Created<DC, DT>) -> Unit,
  extra: @Composable AnimatedVisibilityScope.(Child.Created<EC, ET>) -> Unit,
  placeholder: @Composable AnimatedVisibilityScope.() -> Unit = {},
  reduceMotion: Boolean,
  modifier: Modifier = Modifier,
) {
  val panels by panelsValue.subscribeAsState()

  ChildPanelsModeChangedEffect(setMode)
  CompositionLocalProvider(
    LocalChildPanelsMode provides panels.mode
  ) {
    FixedABIChildPanels(
      panels = panels,
      mainChild = main,
      detailsChild = details,
      extraChild = extra,
      secondPanelPlaceholder = placeholder,
      layout = remember { FixedABIHorizontalChildPanelsLayout(dualWeights = 0.40f to 0.60f) },
      animators = ChildPanelsAnimators(single = platformStackAnimator(reduceMotion), dual = FADE_ANIMATOR to platformStackAnimator(reduceMotion)),
      predictiveBackParams = { predictiveBackParams(backHandler, onBackPressed) },
      modifier = modifier
    )
  }
}