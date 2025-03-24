package app.octocon.app.ui.model.interfaces

import androidx.compose.ui.graphics.ImageBitmap
import app.octocon.app.utils.compress
import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.mr0xf00.easycrop.core.crop.CropError
import com.mr0xf00.easycrop.core.crop.CropResult
import com.mr0xf00.easycrop.core.crop.CropState
import com.mr0xf00.easycrop.core.crop.ImageCropper
import com.mr0xf00.easycrop.core.crop.cropSrc
import com.mr0xf00.easycrop.core.crop.cropState
import com.mr0xf00.easycrop.core.crop.imageCropper
import com.mr0xf00.easycrop.core.images.ImageSrc
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class AvatarState {
  Loaded,
  Preparing,
  Loading
}

class ImageCropper(
  cropStateGenerator: (ImageSrc, () -> Unit) -> CropState = ::cropState,
  private val coroutineScope: CoroutineScope
) : InstanceKeeper.Instance {
  val imageCropper: ImageCropper = imageCropper(cropStateGenerator)

  private val _selectedImage = MutableStateFlow<ImageBitmap?>(null)
  val selectedImage: StateFlow<ImageBitmap?> = _selectedImage

  private val _cropError = MutableStateFlow<CropError?>(null)
  val cropError: StateFlow<CropError?> = _cropError

  fun clearCropError() {
    _cropError.value = null
  }

  fun setSelectedImage(imageSrc: ImageSrc) {
    coroutineScope.launch {
      when (val result = imageCropper.cropSrc(imageSrc)) {
        CropResult.Cancelled -> {}
        is CropError -> _cropError.value = result
        is CropResult.Success -> {
          _selectedImage.value = result.bitmap
        }
      }
    }
  }

  fun setSelectedImageDirect(image: ImageBitmap) {
    _selectedImage.value = image
  }

  suspend fun getCompressedImage(): ByteArray {
    return withContext(coroutineScope.coroutineContext + Dispatchers.Default) {
      val compressed = selectedImage.value!!.compress()
      _selectedImage.tryEmit(null)
      compressed
    }
  }
}