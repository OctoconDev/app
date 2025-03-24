@file:Suppress("NOTHING_TO_INLINE")

package app.octocon.app.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
inline fun <T> derive(noinline calculation: () -> T) = remember { derivedStateOf(calculation) }

@Composable
inline fun <T> state(initialValue: T) = remember { mutableStateOf(initialValue) }

@Composable
inline fun <T> state() = remember { mutableStateOf<T?>(null) }

@Composable
inline fun <T> savedState(initialValue: T) = rememberSaveable { mutableStateOf(initialValue) }

val StringResource.compose: String
  @Composable get() = stringResource(this)