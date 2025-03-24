package app.octocon.app.ui.compose.screens.main.settings.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Security
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import app.octocon.app.ui.compose.LocalChildPanelsMode
import app.octocon.app.ui.compose.components.SettingsSection
import app.octocon.app.ui.compose.components.SettingsToggleItem
import app.octocon.app.ui.compose.components.shared.BackNavigationButton
import app.octocon.app.ui.compose.components.shared.CardGroupPosition
import app.octocon.app.ui.compose.components.shared.OctoScaffold
import app.octocon.app.ui.compose.components.shared.OctoTopBar
import app.octocon.app.ui.compose.components.shared.TitleTextState
import app.octocon.app.ui.compose.screens.GLOBAL_PADDING
import app.octocon.app.ui.model.interfaces.ApiInterface
import app.octocon.app.ui.model.interfaces.SettingsInterface
import app.octocon.app.ui.model.main.settings.SettingsSecurityComponent
import app.octocon.app.utils.compose
import app.octocon.app.utils.derive
import app.octocon.app.utils.state
import com.arkivanov.decompose.router.panels.ChildPanelsMode
import octoconapp.shared.generated.resources.Res
import octoconapp.shared.generated.resources.cancel
import octoconapp.shared.generated.resources.confirm
import octoconapp.shared.generated.resources.create_pin_body
import octoconapp.shared.generated.resources.create_pin_title
import octoconapp.shared.generated.resources.lock_app_with_pin
import octoconapp.shared.generated.resources.ok
import octoconapp.shared.generated.resources.pin
import octoconapp.shared.generated.resources.quick_exit
import octoconapp.shared.generated.resources.quick_exit_body
import octoconapp.shared.generated.resources.security
import octoconapp.shared.generated.resources.stealth_mode
import octoconapp.shared.generated.resources.stealth_mode_body
import octoconapp.shared.generated.resources.tooltip_lock_app_with_pin_desc
import octoconapp.shared.generated.resources.tooltip_quick_exit_desc
import octoconapp.shared.generated.resources.tooltip_stealth_mode_desc

@Composable
fun SettingsSecurityScreen(
  component: SettingsSecurityComponent
) {
  val settings: SettingsInterface = component.settings
  val api: ApiInterface = component.api

  val settingsData by component.settings.collectAsState()

  OctoScaffold(
    topBar = { topAppBarState, scrollBehavior, showSnackbar ->
      OctoTopBar(
        navigation = {
          val childPanelsMode = LocalChildPanelsMode.current

          if(childPanelsMode == ChildPanelsMode.SINGLE) {
            BackNavigationButton(component::navigateBack)
          }
        },
        titleTextState = TitleTextState(Res.string.security.compose),
        topAppBarState = topAppBarState,
        scrollBehavior = scrollBehavior
      )
    },
    content = { _, _ ->
      LazyColumn(
        modifier = Modifier.fillMaxHeight().padding(horizontal = GLOBAL_PADDING)
      ) {
        SettingsSection(
          null,
          settingsData,
          { SettingsPINEnabled(it, settings, api) },
          { SettingsStealthMode(it, settings) },
          { SettingsQuickExit(it, settings) }
        )
        item {
          Spacer(modifier = Modifier.height(GLOBAL_PADDING))
        }
      }
    }
  )
}


@Composable
private fun SettingsPINEnabled(cardGroupPosition: CardGroupPosition, settings: SettingsInterface, api: ApiInterface) {
  val settingsData by settings.collectAsState()
  val pinEnabled by derive { settingsData.tokenIsProtected }

  var createDialogOpen by state(false)

  SettingsToggleItem(
    text = Res.string.lock_app_with_pin.compose,
    value = pinEnabled,
    spotlightDescription = Res.string.tooltip_lock_app_with_pin_desc.compose,
    cardGroupPosition = cardGroupPosition,
    updateValue = {
      if (it) {
        createDialogOpen = true
      } else {
        // Grab the unencrypted token from the API view model to inject it back into the Settings object
        settings.disablePINLock(api.token.value)
      }
    }
  )

  if (createDialogOpen) {
    CreatePINDialog(
      onDismissRequest = { createDialogOpen = false },
      onConfirm = { settings.enablePINLock(it) }
    )
  }
}

@Composable
private fun SettingsStealthMode(cardGroupPosition: CardGroupPosition, settings: SettingsInterface) {
  val settingsData by settings.collectAsState()
  val stealthModeEnabled by derive { settingsData.stealthModeEnabled }

  var notificationOpen by state(false)

  SettingsToggleItem(
    text = Res.string.stealth_mode.compose,
    value = stealthModeEnabled,
    spotlightDescription = Res.string.tooltip_stealth_mode_desc.compose,
    cardGroupPosition = cardGroupPosition,
    updateValue = {
      settings.setStealthModeEnabled(it)
      if (it) {
        notificationOpen = true
      }
    }
  )

  if (notificationOpen) {
    AlertDialog(
      icon = {
        Icon(
          Icons.Rounded.Security,
          contentDescription = null
        )
      },
      title = {
        Text(text = Res.string.stealth_mode.compose)
      },
      text = {
        LazyColumn {
          item {
            Text(Res.string.stealth_mode_body.compose)
          }
        }
      },
      onDismissRequest = { notificationOpen = false },
      confirmButton = {
        TextButton(
          onClick = {
            notificationOpen = false
          }
        ) {
          Text(Res.string.ok.compose)
        }
      }
    )
  }
}

@Composable
private fun SettingsQuickExit(cardGroupPosition: CardGroupPosition, settings: SettingsInterface) {
  val settingsData by settings.collectAsState()
  val quickExitEnabled by derive { settingsData.quickExitEnabled }

  var notificationOpen by state(false)

  SettingsToggleItem(
    text = Res.string.quick_exit.compose,
    value = quickExitEnabled,
    spotlightDescription = Res.string.tooltip_quick_exit_desc.compose,
    cardGroupPosition = cardGroupPosition,
    updateValue = {
      settings.setQuickExitEnabled(it)
      if (it) {
        notificationOpen = true
      }
    }
  )

  if (notificationOpen) {
    AlertDialog(
      icon = {
        Icon(
          Icons.Rounded.Security,
          contentDescription = null
        )
      },
      title = {
        Text(text = Res.string.quick_exit.compose)
      },
      text = {
        LazyColumn {
          item {
            Text(Res.string.quick_exit_body.compose)
          }
        }
      },
      onDismissRequest = { notificationOpen = false },
      confirmButton = {
        TextButton(
          onClick = {
            notificationOpen = false
          }
        ) {
          Text("Ok")
        }
      }
    )
  }
}

@Composable
private fun CreatePINDialog(
  onDismissRequest: () -> Unit,
  onConfirm: (String) -> Unit
) {
  // TODO: Hoist logic to the Decompose component?
  val focusRequester = remember { FocusRequester() }
  var pin by state("")

  val isPinValid = pin.length in 4..8 && pin.toIntOrNull() != null

  AlertDialog(
    icon = {
      Icon(
        Icons.Rounded.Security,
        contentDescription = null
      )
    },
    title = {
      Text(text = Res.string.create_pin_title.compose)
    },
    text = {
      LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        item {
          Text(Res.string.create_pin_body.compose)
        }
        item {
          TextField(
            value = pin,
            onValueChange = {
              pin = when {
                it.isBlank() -> ""
                it.toIntOrNull() == null -> return@TextField
                it.length > 8 -> return@TextField
                else -> it
              }
            },
            label = { Text(Res.string.pin.compose) },
            keyboardActions = KeyboardActions(
              onDone = {
                if (isPinValid) {
                  onConfirm(pin)
                  onDismissRequest()
                }
              }
            ),
            keyboardOptions = KeyboardOptions(
              keyboardType = KeyboardType.NumberPassword,
              imeAction = ImeAction.Done
            ),
            modifier = Modifier.fillMaxWidth().focusRequester(focusRequester)
          )

          LaunchedEffect(true) {
            focusRequester.requestFocus()
          }
        }
      }
    },
    onDismissRequest = onDismissRequest,
    confirmButton = {
      TextButton(
        onClick = {
          onConfirm(pin)
          onDismissRequest()
        },
        enabled = isPinValid
      ) {
        Text(Res.string.confirm.compose)
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