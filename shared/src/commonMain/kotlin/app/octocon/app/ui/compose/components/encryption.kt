package app.octocon.app.ui.compose.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockReset
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import app.octocon.app.api.ChannelMessage
import app.octocon.app.ui.compose.LocalShowSnackbar
import app.octocon.app.ui.compose.components.shared.MaskVisualTransformation
import app.octocon.app.ui.model.interfaces.ApiInterface
import app.octocon.app.ui.model.interfaces.SettingsInterface
import app.octocon.app.utils.compose
import app.octocon.app.utils.savedState
import app.octocon.app.utils.setText
import app.octocon.app.utils.state
import octoconapp.shared.generated.resources.Res
import octoconapp.shared.generated.resources.cancel
import octoconapp.shared.generated.resources.confirm
import octoconapp.shared.generated.resources.encryption_not_initialized_card_body_1
import octoconapp.shared.generated.resources.encryption_not_initialized_card_body_2
import octoconapp.shared.generated.resources.encryption_not_initialized_card_button
import octoconapp.shared.generated.resources.encryption_not_initialized_card_title
import octoconapp.shared.generated.resources.encryption_recovery_needed_card_body
import octoconapp.shared.generated.resources.encryption_recovery_needed_card_button
import octoconapp.shared.generated.resources.encryption_recovery_needed_card_title
import octoconapp.shared.generated.resources.encryption_reset_successfully
import octoconapp.shared.generated.resources.recover_encryption_dialog_body
import octoconapp.shared.generated.resources.recover_encryption_dialog_title
import octoconapp.shared.generated.resources.recovery_code
import octoconapp.shared.generated.resources.reset_encryption_card_body
import octoconapp.shared.generated.resources.reset_encryption_card_button
import octoconapp.shared.generated.resources.reset_encryption_card_title
import octoconapp.shared.generated.resources.reset_encryption_dialog_body
import octoconapp.shared.generated.resources.reset_encryption_dialog_title
import octoconapp.shared.generated.resources.setup_encryption_dialog_body_1
import octoconapp.shared.generated.resources.setup_encryption_dialog_body_2
import octoconapp.shared.generated.resources.setup_encryption_dialog_title
import octoconapp.shared.generated.resources.tap_to_copy

@Composable
internal fun SetupEncryptionCard(api: ApiInterface, settings: SettingsInterface, modifier: Modifier) {
  var encryptionDialogOpen by savedState(false)
  Card(
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.secondaryContainer
    ),
    elevation = CardDefaults.cardElevation(
      defaultElevation = 1.0.dp
    ),
    modifier = modifier
  ) {
    Column(
      modifier = Modifier.padding(16.dp).fillMaxWidth()
    ) {
      Text(
        Res.string.encryption_not_initialized_card_title.compose,
        style = MaterialTheme.typography.titleMedium
      )
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        Res.string.encryption_not_initialized_card_body_1.compose,
        style = MaterialTheme.typography.bodyMedium.merge(
          lineHeight = 1.5.em
        )
      )
      Spacer(modifier = Modifier.height(12.dp))
      Text(
        Res.string.encryption_not_initialized_card_body_2.compose,
        style = MaterialTheme.typography.bodyMedium.merge(
          lineHeight = 1.5.em
        )
      )
      Spacer(modifier = Modifier.height(12.dp))
      Button(
        onClick = { encryptionDialogOpen = true },
        colors = ButtonDefaults.buttonColors(
          containerColor = MaterialTheme.colorScheme.primary,
          contentColor = MaterialTheme.colorScheme.onPrimary
        )
      ) {
        Text(Res.string.encryption_not_initialized_card_button.compose)
      }
    }
  }

  if (encryptionDialogOpen) {
    SetupEncryptionDialog(api, settings) { encryptionDialogOpen = false }

  }
}

@Composable
internal fun SetupEncryptionDialog(api: ApiInterface, settings: SettingsInterface, closeDialog: () -> Unit) {
  val haptics = LocalHapticFeedback.current
  var recoveryCode by state<Pair<String, String>?>(null)

  LaunchedEffect(Unit) {
    recoveryCode = api.generateRecoveryCode()
  }

  var isInitializing by savedState(false)
  var confirmCount by savedState(0)

  val clipboard = LocalClipboard.current

  AlertDialog(
    icon = {
      if (isInitializing) {
        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 3.dp)
      } else {
        Icon(
          Icons.Rounded.Lock,
          contentDescription = null
        )
      }
    },
    title = {
      Text(text = Res.string.setup_encryption_dialog_title.compose)
    },
    text = {
      LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        if(recoveryCode == null) {
          return@LazyColumn
        }

        item {
          Text(
            Res.string.setup_encryption_dialog_body_1.compose,
            style = MaterialTheme.typography.bodyMedium.merge(
              lineHeight = 1.5.em
            )
          )
        }
        item {
          Text(
            Res.string.setup_encryption_dialog_body_2.compose,
            style = MaterialTheme.typography.bodyMedium.merge(
              lineHeight = 1.5.em,
              color = MaterialTheme.colorScheme.error
            )
          )
        }
        item {
          Column(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.clickable {
              clipboard.nativeClipboard.setText(AnnotatedString(recoveryCode!!.first))
            }
          ) {
            Text(recoveryCode!!.first, style = MaterialTheme.typography.titleLarge)
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              Icon(
                Icons.Rounded.ContentCopy,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
              )
              Text(
                Res.string.tap_to_copy.compose,
                style = MaterialTheme.typography.labelMedium
              )
            }
          }
        }
      }
    },
    onDismissRequest = {
      if (!isInitializing) {
        closeDialog()
      }
    },
    confirmButton = {
      Button(
        onClick = {
          haptics.performHapticFeedback(when(confirmCount) {
            0 -> HapticFeedbackType.SegmentTick
            1 -> HapticFeedbackType.ToggleOn
            else -> HapticFeedbackType.LongPress
          })
          confirmCount++
          if (confirmCount >= 3 && !isInitializing) {
            isInitializing = true
            api.setupEncryption(recoveryCode!!.second, settings)
          }
        },
        enabled = !isInitializing
      ) {
        Text(Res.string.confirm.compose + if (confirmCount > 0) " ($confirmCount/3)" else "")
      }
    },
    dismissButton = {
      TextButton(
        onClick = {
          closeDialog()
        },
        enabled = !isInitializing
      ) {
        Text(Res.string.cancel.compose)
      }
    }
  )
}

/*@Composable
fun RecoverEncryptionCard(modifier: Modifier = Modifier) {
  SetupEncryptionCard(modifier)
}*/

private val alphabet = listOf(
  'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'J', 'K', 'L', 'M',
  'N', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
  '2', '3', '4', '5', '6', '7', '8', '9'
)

@Composable
internal fun RecoverEncryptionCard(api: ApiInterface, settings: SettingsInterface, modifier: Modifier = Modifier) {
  var recoverDialogOpen by savedState(false)

  Card(
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.secondaryContainer
    ),
    elevation = CardDefaults.cardElevation(
      defaultElevation = 1.0.dp
    ),
    modifier = modifier
  ) {
    Column(
      modifier = Modifier.padding(16.dp).fillMaxWidth()
    ) {
      Text(
        Res.string.encryption_recovery_needed_card_title.compose,
        style = MaterialTheme.typography.titleMedium
      )
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        Res.string.encryption_recovery_needed_card_body.compose,
        style = MaterialTheme.typography.bodyMedium.merge(
          lineHeight = 1.5.em
        )
      )
      Spacer(modifier = Modifier.height(12.dp))
      Button(
        onClick = {
          recoverDialogOpen = true
        },
        colors = ButtonDefaults.buttonColors(
          containerColor = MaterialTheme.colorScheme.primary,
          contentColor = MaterialTheme.colorScheme.onPrimary
        )
      ) {
        Text(Res.string.encryption_recovery_needed_card_button.compose)
      }
    }
  }

  if (recoverDialogOpen) {
    RecoverEncryptionDialog(api, settings) { recoverDialogOpen = false }
  }
}

@Composable
internal fun RecoverEncryptionDialog(
  api: ApiInterface,
  settings: SettingsInterface,
  closeDialog: () -> Unit,
) {
  var isRecovering by savedState(false)

  var recoveryCode by savedState("")

  val focusRequester = remember { FocusRequester() }

  AlertDialog(
    icon = {
      if (isRecovering) {
        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 3.dp)
      } else {
        Icon(
          Icons.Rounded.Lock,
          contentDescription = null
        )
      }
    },
    title = {
      Text(text = Res.string.recover_encryption_dialog_title.compose)
    },
    text = {
      LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        item {
          Text(
            Res.string.recover_encryption_dialog_body.compose,
            style = MaterialTheme.typography.bodyMedium.merge(
              lineHeight = 1.5.em
            )
          )
        }
        item {
          TextField(
            value = recoveryCode,
            onValueChange = {
              val validChars = it.uppercase().filter { char -> char in alphabet }

              if (validChars.length > 16) {
                return@TextField
              }

              recoveryCode = validChars
            },
            textStyle = MaterialTheme.typography.titleMedium,
            label = { Text(Res.string.recovery_code.compose) },
            modifier = Modifier.focusRequester(focusRequester),
            visualTransformation = MaskVisualTransformation("####-####-####-####"),
            isError = recoveryCode.isNotEmpty() && recoveryCode.length != 16
          )

          LaunchedEffect(focusRequester) {
            focusRequester.requestFocus()
          }
        }
      }
    },
    onDismissRequest = {
      if (!isRecovering) {
        closeDialog()
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (!isRecovering) {
            isRecovering = true
            api.recoverEncryption(recoveryCode, settings, onFailure = {
              isRecovering = false
              // TODO
            })
          }
        },
        enabled = !isRecovering && recoveryCode.length == 16
      ) {
        Text(Res.string.confirm.compose)
      }
    },
    dismissButton = {
      TextButton(
        onClick = {
          closeDialog()
        },
        enabled = !isRecovering
      ) {
        Text(Res.string.cancel.compose)
      }
    }
  )
}

@Composable
internal fun ResetEncryptionCard(api: ApiInterface, modifier: Modifier = Modifier) {
  var resetDialogOpen by savedState(false)

  Card(
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.0.dp),
    modifier = modifier
  ) {
    Column(
      modifier = Modifier.padding(16.dp).fillMaxWidth()
    ) {
      Text(
        Res.string.reset_encryption_card_title.compose,
        style = MaterialTheme.typography.titleMedium
      )
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        Res.string.reset_encryption_card_body.compose,
        style = MaterialTheme.typography.bodyMedium.merge(
          lineHeight = 1.5.em
        )
      )
      Spacer(modifier = Modifier.height(12.dp))
      Button(
        onClick = {
          resetDialogOpen = true
        },
        colors = ButtonDefaults.buttonColors(
          containerColor = MaterialTheme.colorScheme.error,
          contentColor = MaterialTheme.colorScheme.onError
        )
      ) {
        Text(Res.string.reset_encryption_card_button.compose)
      }
    }
  }

  if (resetDialogOpen) {
    ResetEncryptionDialog(api) { resetDialogOpen = false }
  }
}

@Composable
internal fun ResetEncryptionDialog(api: ApiInterface, closeDialog: () -> Unit) {
  val showSnackbar = LocalShowSnackbar.current
  val haptics = LocalHapticFeedback.current

  val latestEvent by api.eventFlow.collectAsState(null)

  @Suppress("LocalVariableName")
  val encryption_reset_successfully = Res.string.encryption_reset_successfully.compose

  LaunchedEffect(latestEvent) {
    when (latestEvent) {
      is ChannelMessage.EncryptedDataWiped -> {
        showSnackbar(encryption_reset_successfully)
        closeDialog()
      }

      else -> Unit
    }
  }

  var isResetting by savedState(false)
  var confirmCount by savedState(0)

  AlertDialog(
    icon = {
      if (isResetting) {
        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 3.dp)
      } else {
        Icon(
          Icons.Rounded.LockReset,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.error
        )
      }
    },
    title = {
      Text(text = Res.string.reset_encryption_dialog_title.compose)
    },
    text = {
      LazyColumn {
        item {
          Text(
            Res.string.reset_encryption_dialog_body.compose,
            style = MaterialTheme.typography.bodyMedium.merge(lineHeight = 1.5.em)
          )
        }
      }
    },
    onDismissRequest = {
      if (!isResetting) {
        closeDialog()
      }
    },
    confirmButton = {
      Button(
        colors = ButtonDefaults.buttonColors(
          containerColor = MaterialTheme.colorScheme.error,
          contentColor = MaterialTheme.colorScheme.onError
        ),
        onClick = {
          haptics.performHapticFeedback(when(confirmCount) {
            0 -> HapticFeedbackType.SegmentTick
            1 -> HapticFeedbackType.ToggleOn
            else -> HapticFeedbackType.LongPress
          })
          confirmCount++
          if (confirmCount >= 3 && !isResetting) {
            isResetting = true
            api.resetEncryption()
          }
        },
        enabled = !isResetting
      ) {
        Text(Res.string.confirm.compose + if (confirmCount > 0) " ($confirmCount/3)" else "")
      }
    },
    dismissButton = {
      TextButton(
        onClick = {
          closeDialog()
        },
        enabled = !isResetting
      ) {
        Text(Res.string.cancel.compose)
      }
    }
  )
}
