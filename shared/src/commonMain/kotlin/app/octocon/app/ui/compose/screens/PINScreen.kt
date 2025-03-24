package app.octocon.app.ui.compose.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecureTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.octocon.app.ui.compose.utils.ModelTransformation
import app.octocon.app.ui.model.PINEntryComponent
import app.octocon.app.utils.compose
import kotlinx.coroutines.launch
import octoconapp.shared.generated.resources.Res
import octoconapp.shared.generated.resources.enter_pin
import octoconapp.shared.generated.resources.invalid_pin
import octoconapp.shared.generated.resources.pin
import octoconapp.shared.generated.resources.pin_hint
import octoconapp.shared.generated.resources.submit

@Composable
fun PINScreen(
  component: PINEntryComponent
) {
  val pinIsValid by component.pinIsValid.collectAsState()

  val scope = rememberCoroutineScope()
  val snackbarHostState = remember { SnackbarHostState() }

  val textState = rememberTextFieldState()

  val focusRequester = remember { FocusRequester() }

  val invalidPinMessage = Res.string.invalid_pin.compose

  Scaffold(
    snackbarHost = { SnackbarHost(snackbarHostState, modifier = Modifier.imePadding()) }
  ) { innerPadding ->
    Box(
      modifier = Modifier.fillMaxSize().padding(innerPadding),
      contentAlignment = Alignment.Center
    ) {
      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        contentPadding = PaddingValues(horizontal = GLOBAL_PADDING)
      ) {
        item {
          Text(Res.string.enter_pin.compose, style = MaterialTheme.typography.headlineMedium)
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }

        item {
          Text(
            Res.string.pin_hint.compose,
            style = MaterialTheme.typography.bodyMedium.copy(textAlign = TextAlign.Center)
          )
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }

        item {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            LaunchedEffect(focusRequester) {
              focusRequester.requestFocus()
            }

            SecureTextField(
              state = textState,
              inputTransformation = ModelTransformation(component::updatePIN),
              label = { Text(Res.string.pin.compose) },
              isError = !pinIsValid,
              keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done,
                keyboardType = KeyboardType.NumberPassword
              ),
              onKeyboardAction = {
                if (!pinIsValid) return@SecureTextField
                component.submitPIN {
                  scope.launch {
                    snackbarHostState.showSnackbar(invalidPinMessage)
                  }
                }
                textState.clearText()
              },
              modifier = Modifier.weight(1f).focusRequester(focusRequester)
            )

            IconButton(
              onClick = {
                component.submitPIN {
                  scope.launch {
                    snackbarHostState.showSnackbar(invalidPinMessage)
                  }
                }
                textState.clearText()
              },
              enabled = pinIsValid,
              colors = IconButtonDefaults.iconButtonColors(
                contentColor = MaterialTheme.colorScheme.primary
              )
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Rounded.Send,
                contentDescription = Res.string.submit.compose
              )
            }
          }
        }
        item { Spacer(modifier = Modifier.imePadding()) }
      }
    }
  }
}
