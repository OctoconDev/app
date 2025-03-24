package app.octocon.app.utils

import android.content.ClipData
import androidx.compose.ui.platform.NativeClipboard
import androidx.compose.ui.text.AnnotatedString

actual fun NativeClipboard.setText(annotatedString: AnnotatedString) {
  this.setPrimaryClip(ClipData.newPlainText(null, annotatedString.text))
}