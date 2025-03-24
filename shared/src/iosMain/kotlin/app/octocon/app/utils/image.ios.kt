package app.octocon.app.utils

import UIImageSrc
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageBitmapConfig
import androidx.compose.ui.util.fastForEach
import cocoapods.SDWebImage.SDImageCoderEncodeMaxPixelSize
import cocoapods.SDWebImage.SDImageFormatWebP
import cocoapods.SDWebImageWebPCoder.SDImageWebPCoder
import cocoapods.TOCropViewController.TOCropViewController
import com.mr0xf00.easycrop.core.images.ImageSrc
import io.github.vinceglb.filekit.core.PlatformFile
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.refTo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import platform.CoreGraphics.CGBitmapContextCreate
import platform.CoreGraphics.CGBitmapContextCreateImage
import platform.CoreGraphics.CGColorSpaceCreateDeviceRGB
import platform.CoreGraphics.CGColorSpaceRef
import platform.CoreGraphics.CGColorSpaceRelease
import platform.CoreGraphics.CGContextRef
import platform.CoreGraphics.CGContextRelease
import platform.CoreGraphics.CGImageAlphaInfo
import platform.CoreGraphics.CGImageRef
import platform.CoreGraphics.CGImageRelease
import platform.CoreGraphics.CGRect
import platform.CoreGraphics.CGSizeMake
import platform.CoreGraphics.kCGImageByteOrder32Little
import platform.Foundation.NSData
import platform.Foundation.NSValue
import platform.Foundation.dataWithContentsOfURL
import platform.UIKit.UIApplication
import platform.UIKit.UIImage
import platform.UIKit.valueWithCGSize
import platform.darwin.NSInteger

/*actual fun newDiskCache(platformContext: PlatformContext): DiskCache? {
  return DiskCache.Builder()
    .directory(platformContext.cacheDir.resolve("image_cache"))
    .build()
}*/

// actual val platformImageFilePickerType: FilePickerFileType = FilePickerFileType.Image

actual suspend fun platformFileToImageSrc(
  file: PlatformFile,
  platformUtilities: PlatformUtilities
): ImageSrc? {
  val data = NSData.dataWithContentsOfURL(file.nsUrl) ?: return null
  val image = UIImage(data)
  return UIImageSrc(image)
}

actual fun directlyCompressImage(
  file: PlatformFile,
  platformUtilities: PlatformUtilities
): ByteArray? {
  val data = NSData.dataWithContentsOfURL(file.nsUrl) ?: return null
  val image = UIImage(data)

  return compressUIImage(image)
}

actual suspend fun ImageBitmap.compress(): ByteArray {
  val image = this.toUIImage() ?: throw IllegalStateException("Failed to convert ImageBitmap to UIImage")

  return compressUIImage(image)
}

@OptIn(ExperimentalForeignApi::class)
fun compressUIImage(image: UIImage): ByteArray {
  val nsData = SDImageWebPCoder.sharedCoder().encodedDataWithImage(
    image,
    format = SDImageFormatWebP,
    options = mapOf(
      SDImageCoderEncodeMaxPixelSize to NSValue.valueWithCGSize(CGSizeMake(1024.0, 1024.0))
    )
  ) ?: throw IllegalStateException("Failed to convert UIImage to WebP data")

  // val nsData = UIImagePNGRepresentation(image) ?: throw IllegalStateException("Failed to convert UIImage to PNG data")
  // val data = UIImageWebPRepresentation(image) ?: throw IllegalStateException("Failed to convert UIImage to WebP data")

  val data = nsData.toByteArray()
  platformLog("Compressed image to ${data.size} bytes")

  return data
}

@OptIn(ExperimentalForeignApi::class)
actual fun cropImageNatively(
  file: PlatformFile,
  platformUtilities: PlatformUtilities,
  onCompressionStart: () -> Unit,
  onImageReady: (ByteArray) -> Unit,
  onCanceled: () -> Unit,
  coroutineScope: CoroutineScope
) {
  val data = NSData.dataWithContentsOfURL(file.nsUrl) ?: return
  val image = UIImage(data)

  val cropViewController = TOCropViewController(image = image)

  cropViewController.onDidFinishCancelled = {
    onCanceled()
    cropViewController.presentingViewController?.dismissModalViewControllerAnimated(animated = true)
  }

  cropViewController.onDidCropToRect = { it: UIImage?, _: CValue<CGRect>, _: NSInteger ->
    cropViewController.presentingViewController?.dismissModalViewControllerAnimated(animated = true)

    if(it != null) {
      onCompressionStart()
      coroutineScope.launch(Dispatchers.Default) {
        val compressed = compressUIImage(it)
        onImageReady(compressed)
      }
    }
  }

  cropViewController.customAspectRatio = CGSizeMake(1.0, 1.0)
  cropViewController.aspectRatioLockEnabled = true
  cropViewController.resetAspectRatioEnabled = false
  cropViewController.aspectRatioPickerButtonHidden = true
  cropViewController.aspectRatioLockDimensionSwapEnabled = true

  UIApplication.sharedApplication.keyWindow?.rootViewController?.presentModalViewController(
    cropViewController,
    animated = true
  )
}

@OptIn(ExperimentalForeignApi::class)
internal sealed interface CFScopeReleasable {
  fun release()

  data class Image(val image: CGImageRef) : CFScopeReleasable {
    override fun release() {
      CGImageRelease(image)
    }
  }

  data class ColorSpace(val colorSpace: CGColorSpaceRef) :
    CFScopeReleasable {
    override fun release() {
      CGColorSpaceRelease(colorSpace)
    }
  }

  data class Context(val context: CGContextRef) : CFScopeReleasable {
    override fun release() {
      CGContextRelease(context)
    }
  }
}

@OptIn(ExperimentalForeignApi::class)
internal class CFReleaseScope {
  private val items = mutableListOf<CFScopeReleasable>()

  fun release() {
    items.reversed().fastForEach { it.release() }
  }

  private fun add(item: CFScopeReleasable) {
    items.add(item)
  }

  fun CGImageRef.releasedAfterScopeEnds(): CGImageRef {
    add(CFScopeReleasable.Image(this))
    return this
  }

  fun CGContextRef.releasedAfterScopeEnds(): CGContextRef {
    add(CFScopeReleasable.Context(this))
    return this
  }

  fun CGColorSpaceRef.releasedAfterScopeEnds(): CGColorSpaceRef {
    add(CFScopeReleasable.ColorSpace(this))
    return this
  }
}

internal fun <R> withCFReleaseScope(block: CFReleaseScope.() -> R): R {
  val scope = CFReleaseScope()
  return try {
    scope.block()
  } finally {
    scope.release()
  }
}

@OptIn(ExperimentalForeignApi::class)
internal fun ImageBitmap.toCGImage(): CGImageRef? = withCFReleaseScope {
  if (config != ImageBitmapConfig.Argb8888) {
    throw NotImplementedError("Only ImageBitmapConfig.Argb8888 is supported")
  }

  val buffer = IntArray(width * height)

  readPixels(buffer)

  val colorSpace =
    CGColorSpaceCreateDeviceRGB()?.releasedAfterScopeEnds() ?: return@withCFReleaseScope null

  val bitmapInfo =
    CGImageAlphaInfo.kCGImageAlphaPremultipliedFirst.value or kCGImageByteOrder32Little

  val context = CGBitmapContextCreate(
    data = buffer.refTo(0),
    width = width.toULong(),
    height = height.toULong(),
    bitsPerComponent = 8u,
    bytesPerRow = (4 * width).toULong(),
    space = colorSpace,
    bitmapInfo = bitmapInfo
  )?.releasedAfterScopeEnds() ?: return@withCFReleaseScope null

  val cgImage = CGBitmapContextCreateImage(context) // must be released by the user
  return@withCFReleaseScope cgImage
}

@OptIn(ExperimentalForeignApi::class)
fun ImageBitmap.toUIImage(): UIImage? = withCFReleaseScope {
  toCGImage()?.releasedAfterScopeEnds()?.let {
    UIImage.imageWithCGImage(it)
  }
}