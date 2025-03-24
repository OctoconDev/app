package app.octocon.app.ui.model

import app.octocon.app.utils.crypto.AES
import app.octocon.app.utils.crypto.CipherPadding
import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.arkivanov.essenty.instancekeeper.retainedInstance
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import io.ktor.util.decodeBase64Bytes
import io.ktor.utils.io.core.toByteArray
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

interface PINEntryComponent {
  val currentPIN: StateFlow<String>
  val pinIsValid: StateFlow<Boolean>

  fun updatePIN(pin: String): Result<String>
  fun submitPIN(showErrorSnackbar: () -> Unit)
}

internal class PINEntryComponentImpl(
  componentContext: CommonComponentContext,
  val navigateToMainApp: (token: String) -> Unit
) : PINEntryComponent, CommonComponentContext by componentContext {
  val coroutineScope = coroutineScope(coroutineContext + SupervisorJob())
  private val handler = retainedInstance { Handler() }

  override val currentPIN = handler.currentPIN
  override val pinIsValid = handler.pinIsValid

  override fun updatePIN(pin: String): Result<String> {
    if(pin.isNotBlank() && (pin.toIntOrNull() == null || pin.length > 8)) return Result.failure(IllegalArgumentException("PIN must be a number between 4 and 8 digits"))
    handler.pinIsValid.value = pin.length in 4..8
    handler.currentPIN.value = pin
    return Result.success(pin)
  }

  override fun submitPIN(showErrorSnackbar: () -> Unit) {
    if(!handler.pinIsValid.value) return

    val encryptedToken = settings.data.value.token!!

    coroutineScope.launch(Dispatchers.Default) {
      try {
        val decryptedToken = AES.decryptAesEcb(
          encryptedToken.decodeBase64Bytes(),
          handler.currentPIN.value.toByteArray(),
          CipherPadding.ZeroPadding
        ).decodeToString()
        if (!decryptedToken.startsWith("tk|")) throw Exception()

        withContext(Dispatchers.Main) { navigateToMainApp(decryptedToken.removePrefix("tk|")) }
      } catch (_: Exception) {
        showErrorSnackbar()
        handler.pinIsValid.value = false
      }
    }
  }

  private class Handler : InstanceKeeper.Instance {
    val currentPIN = MutableStateFlow<String>("")
    val pinIsValid = MutableStateFlow(false)
  }
}

