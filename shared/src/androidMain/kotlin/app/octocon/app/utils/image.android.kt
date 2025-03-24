package app.octocon.app.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import com.mr0xf00.easycrop.core.images.ImageSrc
import com.mr0xf00.easycrop.core.images.toImageSrc
import io.github.vinceglb.filekit.core.PlatformFile
import kotlinx.coroutines.CoroutineScope
import java.io.ByteArrayOutputStream

/*actual fun newDiskCache(platformContext: PlatformContext): DiskCache? {
  return DiskCache.Builder()
    .directory(platformContext.cacheDir.resolve("image_cache"))
    .build()
}*/

/*actual val platformImageFilePickerType: FilePickerFileType = FilePickerFileType.Custom(
  listOf(
    "image/png",
    "image/jpeg",
    "image/jpg",
    "image/webp",
    "image/gif"
  )
)*/

actual suspend fun platformFileToImageSrc(
  file: PlatformFile,
  platformUtilities: PlatformUtilities
): ImageSrc? = file.uri.toImageSrc(platformUtilities.context)

actual fun directlyCompressImage(
  file: PlatformFile,
  platformUtilities: PlatformUtilities
): ByteArray? {
  val inputStream = platformUtilities.context.contentResolver.openInputStream(file.uri)
  return BitmapFactory
    .decodeStream(inputStream)
    ?.compressAsWebP()
}

actual fun cropImageNatively(
  file: PlatformFile,
  platformUtilities: PlatformUtilities,
  onCompressionStart: () -> Unit,
  onImageReady: (ByteArray) -> Unit,
  onCanceled: () -> Unit,
  coroutineScope: CoroutineScope
) = Unit

actual suspend fun ImageBitmap.compress(): ByteArray =
  this.asAndroidBitmap().compressAsWebP()

fun Bitmap.compressAsWebP(): ByteArray {
  val out = ByteArrayOutputStream()
  val compressionFormat = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
    Bitmap.CompressFormat.WEBP_LOSSLESS
  } else {
    Bitmap.CompressFormat.WEBP
  }
  this.compress(compressionFormat, 100, out)
  return out.toByteArray()
}