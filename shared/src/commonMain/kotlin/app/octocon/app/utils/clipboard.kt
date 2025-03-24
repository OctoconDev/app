package app.octocon.app.utils

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.NativeClipboard
import androidx.compose.ui.text.AnnotatedString

@OptIn(ExperimentalComposeUiApi::class)
expect fun NativeClipboard.setText(annotatedString: AnnotatedString)