package app.octocon.app.utils

import androidx.compose.ui.graphics.ImageBitmap

import com.mr0xf00.easycrop.core.images.ImageSrc
import io.github.vinceglb.filekit.core.PlatformFile
import kotlinx.coroutines.CoroutineScope

expect suspend fun platformFileToImageSrc(file: PlatformFile, platformUtilities: PlatformUtilities): ImageSrc?
expect fun directlyCompressImage(file: PlatformFile, platformUtilities: PlatformUtilities): ByteArray?
expect fun cropImageNatively(
  file: PlatformFile,
  platformUtilities: PlatformUtilities,
  onCompressionStart: () -> Unit,
  onImageReady: (ByteArray) -> Unit,
  onCanceled: () -> Unit,
  coroutineScope: CoroutineScope
)

expect suspend fun ImageBitmap.compress(): ByteArray