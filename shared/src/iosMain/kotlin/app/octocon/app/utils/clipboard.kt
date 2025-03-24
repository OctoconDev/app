package app.octocon.app.utils

import androidx.compose.ui.platform.NativeClipboard
import androidx.compose.ui.text.AnnotatedString

actual fun NativeClipboard.setText(annotatedString: AnnotatedString) {
  this.setString(annotatedString.text)
}