package app.octocon.app.utils

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import com.mr0xf00.easycrop.core.images.ImageBitmapSrc
import com.mr0xf00.easycrop.core.images.ImageSrc
import io.github.vinceglb.filekit.core.PlatformFile
import kotlinx.coroutines.CoroutineScope
import org.jetbrains.skia.Image

actual suspend fun platformFileToImageSrc(
  file: PlatformFile,
  platformUtilities: PlatformUtilities
): ImageSrc? {
  val bytes = file.readBytes()
  val imageBitmap = Image.makeFromEncoded(bytes).toComposeImageBitmap()
  return ImageBitmapSrc(imageBitmap)
}

actual fun directlyCompressImage(
  file: PlatformFile,
  platformUtilities: PlatformUtilities
): ByteArray? {
  return null // TODO: Implement
}

actual fun cropImageNatively(
  file: PlatformFile,
  platformUtilities: PlatformUtilities,
  onCompressionStart: () -> Unit,
  onImageReady: (ByteArray) -> Unit,
  onCanceled: () -> Unit,
  coroutineScope: CoroutineScope
) = Unit

actual suspend fun ImageBitmap.compress(): ByteArray {
  return ByteArray(0) // TODO: Implement
}