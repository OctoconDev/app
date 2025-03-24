package app.octocon.app.ui.compose.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import app.octocon.app.utils.compose
import app.octocon.app.utils.savedState
import octoconapp.shared.generated.resources.Res
import octoconapp.shared.generated.resources.cancel
import octoconapp.shared.generated.resources.update
import octoconapp.shared.generated.resources.update_username
import octoconapp.shared.generated.resources.username

@Composable
fun UpdateUsernameDialog(
  initialUsername: String,
  onDismissRequest: () -> Unit,
  launchUpdateUsername: (username: String) -> Unit
) {
  var username by savedState(initialUsername)

  val isUsernameValid = with(username.trim()) {
    isNotBlank() && length in 5..16
  }

  val focusRequester = remember { FocusRequester() }

  AlertDialog(
    icon = {
      Icon(
        Icons.Rounded.Edit,
        contentDescription = null
      )
    },
    title = {
      Text(text = Res.string.update_username.compose)
    },
    text = {
      LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        item {
          TextField(
            value = username,
            onValueChange = {
              if (it.length > 16) return@TextField
              username = it
            },
            label = { Text(Res.string.username.compose) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().focusRequester(focusRequester)
          )

          LaunchedEffect(true) {
            focusRequester.requestFocus()
          }
        }
        // Text("Flavor text")
      }
    },
    onDismissRequest = onDismissRequest,
    confirmButton = {
      TextButton(
        onClick = {
          launchUpdateUsername(username)
          onDismissRequest()
        },
        enabled = isUsernameValid
      ) {
        Text(Res.string.update.compose)
      }
    },
    dismissButton = {
      TextButton(
        onClick = {
          onDismissRequest()
        }
      ) {
        Text(Res.string.cancel.compose)
      }
    }
  )
}