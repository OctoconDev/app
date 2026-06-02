package app.octocon.app.utils

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable

enum class AnimationSpeed {
  SLOW,
  DEFAULT,
  FAST
}

@Composable fun <T> spatialSpec(speed: AnimationSpeed = AnimationSpeed.DEFAULT): FiniteAnimationSpec<T> = when (speed) {
  AnimationSpeed.SLOW -> spring(stiffness = Spring.StiffnessLow)
  AnimationSpeed.DEFAULT -> spring(stiffness = Spring.StiffnessMediumLow)
  AnimationSpeed.FAST -> spring(stiffness = Spring.StiffnessMedium)
}

@Composable fun <T> effectsSpec(speed: AnimationSpeed = AnimationSpeed.DEFAULT): FiniteAnimationSpec<T> = when (speed) {
  AnimationSpeed.SLOW -> spring(stiffness = Spring.StiffnessLow)
  AnimationSpeed.DEFAULT -> spring(stiffness = Spring.StiffnessMediumLow)
  AnimationSpeed.FAST -> spring(stiffness = Spring.StiffnessMedium)
}