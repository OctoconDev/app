package app.octocon.app.ui.compose

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.TopAppBarState
import androidx.compose.runtime.compositionLocalOf
import app.octocon.app.SpotlightLongPressTimeout
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.router.panels.ChildPanelsMode
import com.mikepenz.markdown.compose.components.MarkdownComponents

val LocalTopAppBarState =
  compositionLocalOf<TopAppBarState> { error("No TopAppBarState provided") }

val LocalShowSnackbar =
  compositionLocalOf<(String) -> Unit> { error("No ShowSnackbar provided") }

val LocalUpdateLazyListState =
  compositionLocalOf<(LazyListState?) -> Unit> { error("No UpdateLazyListState provided") }

val LocalSetShowPushNotifications =
  compositionLocalOf<(Boolean) -> Unit> { error("No SetShowPushNotifications provided") }

val LocalMarkdownComponents =
  compositionLocalOf<MarkdownComponents?> { null }

val LocalFABIsCollapsed =
  compositionLocalOf { true }

// TODO: error("no ModalDrawerToggler provided")
val LocalModalDrawerToggler =
  compositionLocalOf<(Boolean) -> Unit> { {} }

@RequiresOptIn(message = "This API is internal to Octocon's layout system. Did you mean to use LocalChildPanelsMode?", level = RequiresOptIn.Level.ERROR)
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.PROPERTY)
annotation class InternalOctoconLayoutApi

@OptIn(ExperimentalDecomposeApi::class)
@InternalOctoconLayoutApi
val LocalDesiredChildPanelsMode =
  compositionLocalOf { ChildPanelsMode.SINGLE }

@OptIn(ExperimentalDecomposeApi::class)
val LocalChildPanelsMode =
  compositionLocalOf { ChildPanelsMode.SINGLE }


enum class NavigationType {
  BOTTOM_BAR,
  RAIL,
  DRAWER;
}

val LocalNavigationType =
  compositionLocalOf<NavigationType?> { null }

val LocalSpotlightTooltipsEnabled =
  compositionLocalOf<Boolean> { error("No SpotlightTooltipsEnabled provided") }

val LocalSpotlightLongPressTimeout =
  compositionLocalOf<SpotlightLongPressTimeout> { error("No SpotlightLongPressTimeout provided") }