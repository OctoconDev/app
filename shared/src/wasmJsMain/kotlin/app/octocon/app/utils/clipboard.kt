package app.octocon.app.utils

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.NativeClipboard
import androidx.compose.ui.platform.WasmPlatformClipboard
import androidx.compose.ui.text.AnnotatedString
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

private val clipboard by lazy { WasmPlatformClipboard() }

@OptIn(DelicateCoroutinesApi::class, ExperimentalComposeUiApi::class)
actual fun NativeClipboard.setText(annotatedString: AnnotatedString) {
  GlobalScope.launch {
    clipboard.setClipEntry(ClipEntry.withPlainText(annotatedString.text))
  }
}