@file:Suppress("LocalVariableName")

package app.octocon.app.ui.compose.screens.main.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import app.octocon.app.api.ChannelMessage
import app.octocon.app.api.model.MySystem
import app.octocon.app.ui.compose.LocalNavigationType
import app.octocon.app.ui.compose.LocalSetShowPushNotifications
import app.octocon.app.ui.compose.NavigationType
import app.octocon.app.ui.compose.components.RecoverEncryptionDialog
import app.octocon.app.ui.compose.components.ResetEncryptionDialog
import app.octocon.app.ui.compose.components.SetupEncryptionDialog
import app.octocon.app.ui.compose.components.SettingsButtonItem
import app.octocon.app.ui.compose.components.SettingsLoneButtonItem
import app.octocon.app.ui.compose.components.SettingsNavigationItem
import app.octocon.app.ui.compose.components.SettingsSection
import app.octocon.app.ui.compose.components.SettingsToggleItem
import app.octocon.app.ui.compose.components.shared.CardGroupPosition
import app.octocon.app.ui.compose.components.shared.MaskVisualTransformation
import app.octocon.app.ui.compose.components.shared.OctoScaffold
import app.octocon.app.ui.compose.components.shared.OctoTopBar
import app.octocon.app.ui.compose.components.shared.OpenDrawerNavigationButton
import app.octocon.app.ui.compose.components.shared.TitleTextState
import app.octocon.app.ui.compose.components.shared.createConfirmationDialog
import app.octocon.app.ui.compose.screens.GLOBAL_PADDING
import app.octocon.app.ui.model.interfaces.ApiInterface
import app.octocon.app.ui.model.interfaces.SettingsInterface
import app.octocon.app.ui.model.main.settings.SettingsRootComponent
import app.octocon.app.utils.ColorSchemeParams
import app.octocon.app.utils.DevicePlatform
import app.octocon.app.utils.MarkdownRenderer
import app.octocon.app.utils.PlatformUtilities
import app.octocon.app.utils.compose
import app.octocon.app.utils.derive
import app.octocon.app.utils.savedState
import app.octocon.app.utils.state
import androidx.compose.foundation.layout.Row
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.mikepenz.markdown.compose.LocalMarkdownColors
import com.mikepenz.markdown.m3.markdownColor
import octoconapp.shared.resources.Res
import octoconapp.shared.resources.accessibility
import octoconapp.shared.resources.account_options
import octoconapp.shared.resources.app
import octoconapp.shared.resources.app_info
import octoconapp.shared.resources.appearance
import octoconapp.shared.resources.apple_account
import octoconapp.shared.resources.cancel
import octoconapp.shared.resources.confirm
import octoconapp.shared.resources.custom_fields
import octoconapp.shared.resources.danger_zone
import octoconapp.shared.resources.delete_account
import octoconapp.shared.resources.delete_account_body
import octoconapp.shared.resources.discord_account
import octoconapp.shared.resources.encryption_not_initialized_card_button
import octoconapp.shared.resources.encryption_not_initialized_sp_note
import octoconapp.shared.resources.encryption_recovery_needed_card_button
import octoconapp.shared.resources.google_account
import octoconapp.shared.resources.import
import octoconapp.shared.resources.import_alters
import octoconapp.shared.resources.import_complete_pk_body
import octoconapp.shared.resources.import_complete_sp_body
import octoconapp.shared.resources.import_failed_pk_body
import octoconapp.shared.resources.import_failed_sp_body
import octoconapp.shared.resources.import_pk_body
import octoconapp.shared.resources.import_pk_title
import octoconapp.shared.resources.import_sp_body
import octoconapp.shared.resources.import_sp_title
import octoconapp.shared.resources.importing_data_body
import octoconapp.shared.resources.link
import octoconapp.shared.resources.link_apple_account_body
import octoconapp.shared.resources.link_apple_account_title
import octoconapp.shared.resources.link_discord_account_body
import octoconapp.shared.resources.link_discord_account_title
import octoconapp.shared.resources.link_google_account_body
import octoconapp.shared.resources.link_google_account_title
import octoconapp.shared.resources.login_methods
import octoconapp.shared.resources.logout
import octoconapp.shared.resources.logout_body
import octoconapp.shared.resources.notifications
import octoconapp.shared.resources.ok
import octoconapp.shared.resources.open_source_licenses
import octoconapp.shared.resources.pluralkit
import octoconapp.shared.resources.recovery_code
import octoconapp.shared.resources.recovery_code_sp_note
import octoconapp.shared.resources.recovery_code_sp_note_recover
import octoconapp.shared.resources.reset_device_settings
import octoconapp.shared.resources.reset_device_settings_body
import octoconapp.shared.resources.reset_encryption_card_button
import octoconapp.shared.resources.security
import octoconapp.shared.resources.server_status
import octoconapp.shared.resources.settings
import octoconapp.shared.resources.show_push_notifications
import octoconapp.shared.resources.simply_plural
import octoconapp.shared.resources.singlet_mode
import octoconapp.shared.resources.singlet_mode_body
import octoconapp.shared.resources.token
import octoconapp.shared.resources.tooltip_apple_account_desc
import octoconapp.shared.resources.tooltip_custom_fields_desc
import octoconapp.shared.resources.tooltip_delete_account_desc
import octoconapp.shared.resources.tooltip_discord_account_desc
import octoconapp.shared.resources.tooltip_google_account_desc
import octoconapp.shared.resources.tooltip_logout_desc
import octoconapp.shared.resources.tooltip_notifications_desc
import octoconapp.shared.resources.tooltip_open_source_licenses_desc
import octoconapp.shared.resources.tooltip_plural_kit_import_desc
import octoconapp.shared.resources.tooltip_plural_kit_import_title
import octoconapp.shared.resources.tooltip_reset_device_settings_desc
import octoconapp.shared.resources.tooltip_server_status_desc
import octoconapp.shared.resources.tooltip_settings_accessibility_desc
import octoconapp.shared.resources.tooltip_settings_appearance_desc
import octoconapp.shared.resources.tooltip_settings_desc
import octoconapp.shared.resources.tooltip_settings_security_desc
import octoconapp.shared.resources.tooltip_simply_plural_import_desc
import octoconapp.shared.resources.tooltip_simply_plural_import_title
import octoconapp.shared.resources.tooltip_singlet_mode_desc
import octoconapp.shared.resources.tooltip_wipe_alters_desc
import octoconapp.shared.resources.unlink
import octoconapp.shared.resources.unlink_apple_account_body
import octoconapp.shared.resources.unlink_apple_account_title
import octoconapp.shared.resources.unlink_discord_account_body
import octoconapp.shared.resources.unlink_discord_account_title
import octoconapp.shared.resources.unlink_google_account_body
import octoconapp.shared.resources.unlink_google_account_title
import octoconapp.shared.resources.wipe_alters
import octoconapp.shared.resources.wipe_alters_body

@OptIn(ExperimentalDecomposeApi::class)
@Composable
fun SettingsRootScreen(
  component: SettingsRootComponent
) {
  val account_options = Res.string.account_options.compose
  val notifications = Res.string.notifications.compose
  val login_methods = Res.string.login_methods.compose
  val import_alters = Res.string.import_alters.compose
  val danger_zone = Res.string.danger_zone.compose
  val app_info = Res.string.app_info.compose
  val app = Res.string.app.compose

  val markdownColors = markdownColor()

  val api: ApiInterface = component.api
  val settings: SettingsInterface = component.settings
  val settingsData by component.settings.collectAsState()
  val platformUtilities: PlatformUtilities = component.platformUtilities
  val system by api.systemMe.collectAsState()

  val isSinglet = settingsData.isSinglet

  val setShowPushNotifications = LocalSetShowPushNotifications.current

  OctoScaffold(
    topBar = { topAppBarState, scrollBehavior, _ ->
      OctoTopBar(
        titleTextState = TitleTextState(
          Res.string.settings.compose,
          spotlightText = Res.string.settings.compose to Res.string.tooltip_settings_desc.compose
        ),
        navigation = {
          val navigationType = LocalNavigationType.current

          if(navigationType != NavigationType.DRAWER) {
            OpenDrawerNavigationButton()
          }
        },
        topAppBarState = topAppBarState,
        scrollBehavior = scrollBehavior
      )
    },
    content = { _, _ ->
      CompositionLocalProvider(
        LocalMarkdownColors provides markdownColors
      ) {
        LazyColumn(
          modifier = Modifier.fillMaxHeight().padding(horizontal = GLOBAL_PADDING),
        ) {
          SettingsSection(
            app,
            settingsData,
            { SettingsNavigationItem(
              cardGroupPosition = it,
              text = Res.string.appearance.compose,
              spotlightDescription = Res.string.tooltip_settings_appearance_desc.compose,
              onClick = component::navigateToAppearance
            ) },
            { SettingsNavigationItem(
              cardGroupPosition = it,
              text = Res.string.accessibility.compose,
              spotlightDescription = Res.string.tooltip_settings_accessibility_desc.compose,
              onClick = component::navigateToAccessibility
            ) },
            { SettingsNavigationItem(
              cardGroupPosition = it,
              text = Res.string.security.compose,
              spotlightDescription = Res.string.tooltip_settings_security_desc.compose,
              onClick = component::navigateToSecurity
            ) }
          )

          SettingsSection(
            account_options,
            settingsData,
            {
              if(!isSinglet) {
                SettingsNavigationItem(
                  cardGroupPosition = it,
                  text = Res.string.custom_fields.compose,
                  spotlightDescription = Res.string.tooltip_custom_fields_desc.compose,
                  onClick = component::navigateToCustomFields
                )
              }
            },
            { SettingsSingletMode(if(isSinglet) CardGroupPosition.SINGLE else CardGroupPosition.END, settings) }
          )

          if(DevicePlatform.isMobile) {
            SettingsSection(
              notifications,
              settingsData,
              {
                SettingsShowPushNotifications(it, settings) { enabled ->
                  setShowPushNotifications(enabled)
                }
              }
            )
          }

          if(system.isSuccess) {
            SettingsSection(
              login_methods,
              settingsData,
              {
                SettingsGoogleAccount(
                  it,
                  system.ensureData,
                  tryLinkGoogle = api::tryLinkGoogle,
                  tryUnlinkEmail = api::tryUnlinkEmail,
                  openURL = platformUtilities::openURL
                )
              },
              {
                SettingsAppleAccount(
                  it,
                  system.ensureData,
                  tryLinkApple = api::tryLinkApple,
                  tryUnlinkApple = api::tryUnlinkApple,
                  openURL = platformUtilities::openURL
                )
              },
              {
                SettingsDiscordAccount(
                  it,
                  system.ensureData,
                  tryLinkDiscord = api::tryLinkDiscord,
                  tryUnlinkDiscord = api::tryUnlinkDiscord,
                  openURL = platformUtilities::openURL
                )
              },
              {
                SettingsLogOut(
                  it,
                  logOut = {
                    component.logOut()
                  }
                )
              }
            )
          }

            if(!isSinglet) {
              SettingsSection(
                import_alters,
                settingsData,
                { SettingsImportSP(it, api::importSP, api, settings) },
                { SettingsImportPK(it, api::importPK, api) }
              )
            }

          SettingsSection(
            danger_zone,
            settingsData,
            { SettingsResetSettings(it, settings::nukeEverything) },
            { if(!isSinglet) { SettingsWipeAlters(it, api) } },
            { SettingsDeleteAccount(it, api::deleteAccount) }
          )

          SettingsSection(
            app_info,
            settingsData,
            { SettingsNavigationItem(
              cardGroupPosition = it,
              text = Res.string.open_source_licenses.compose,
              spotlightDescription = Res.string.tooltip_open_source_licenses_desc.compose,
              onClick = component::navigateToOpenSourceLicenses
            ) },
            { SettingsServerStatus(it, platformUtilities::openURL) }
          )

          item {
            Spacer(modifier = Modifier.height(GLOBAL_PADDING))
          }
        }
      }
    }
  )
}

@Composable
private fun SettingsSingletMode(
  cardGroupPosition: CardGroupPosition,
  settings: SettingsInterface
) {
  val settingsData by settings.collectAsState()
  val isSinglet by derive { settingsData.isSinglet }

  var notificationOpen by state(false)

  SettingsToggleItem(
    text = Res.string.singlet_mode.compose,
    value = isSinglet,
    cardGroupPosition = cardGroupPosition,
    spotlightDescription = Res.string.tooltip_singlet_mode_desc.compose,
    updateValue = {
      settings.setIsSinglet(it)
      if (it) {
        notificationOpen = true
      }
    }
  )

  if (notificationOpen) {
    AlertDialog(
      icon = {
        Icon(
          Icons.Rounded.Person,
          contentDescription = null
        )
      },
      title = {
        Text(text = Res.string.singlet_mode.compose)
      },
      text = {
        LazyColumn {
          item {
            Text(Res.string.singlet_mode_body.compose)
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
private fun SettingsShowPushNotifications(
  cardGroupPosition: CardGroupPosition,
  settings: SettingsInterface,
  setShowPushNotifications: (Boolean) -> Unit
) {
  val settingsData by settings.collectAsState()
  val showPushNotifications by derive { settingsData.showPushNotifications }

  SettingsToggleItem(
    text = Res.string.show_push_notifications.compose,
    value = showPushNotifications,
    spotlightDescription = Res.string.tooltip_notifications_desc.compose,
    cardGroupPosition = cardGroupPosition,
  ) {
    setShowPushNotifications(it)
  }
}

@Composable
private fun SettingsGoogleAccount(
  cardGroupPosition: CardGroupPosition,
  system: MySystem,
  tryLinkGoogle: ((String) -> Unit) -> Unit,
  tryUnlinkEmail: () -> Unit,
  openURL: (String, ColorSchemeParams) -> Unit
) {
  val buttonEnabled = system.appleID != null || system.discordID != null

  val (Dialog, isOpen, openDialog) = if (system.email == null) {
    createConfirmationDialog(
      title = Res.string.link_google_account_title.compose,
      messageText = Res.string.link_google_account_body.compose,
      confirmText = Res.string.confirm.compose,
      cancelText = Res.string.cancel.compose,
      icon = {
        Icon(
          imageVector = Icons.Rounded.Link,
          contentDescription = null
        )
      },
      onConfirm = {
        tryLinkGoogle(it)
      },
      openUri = openURL
    )
  } else {
    createConfirmationDialog(
      title = Res.string.unlink_google_account_title.compose,
      messageText = Res.string.unlink_google_account_body.compose,
      confirmText = Res.string.confirm.compose,
      cancelText = Res.string.cancel.compose,
      icon = {
        Icon(
          imageVector = Icons.Rounded.Link,
          contentDescription = null
        )
      },
      onConfirm = {
        tryUnlinkEmail()
      },
      openUri = openURL
    )
  }

  if (isOpen) {
    Dialog()
  }

  SettingsButtonItem(
    text = Res.string.google_account.compose,
    buttonText =
      if (system.email == null)
        Res.string.link.compose
      else
        Res.string.unlink.compose,
    spotlightDescription = Res.string.tooltip_google_account_desc.compose,
    cardGroupPosition = cardGroupPosition,
    enabled = buttonEnabled,
    onClick = openDialog
  )
}

@Composable
private fun SettingsDiscordAccount(
  cardGroupPosition: CardGroupPosition,
  system: MySystem,
  tryLinkDiscord: ((String) -> Unit) -> Unit,
  tryUnlinkDiscord: () -> Unit,
  openURL: (String, ColorSchemeParams) -> Unit
) {
  val buttonEnabled = system.email != null || system.appleID != null

  val (Dialog, isOpen, openDialog) = if (system.discordID == null) {
    createConfirmationDialog(
      title = Res.string.link_discord_account_title.compose,
      messageText = Res.string.link_discord_account_body.compose,
      confirmText = Res.string.confirm.compose,
      cancelText = Res.string.cancel.compose,
      icon = {
        Icon(
          imageVector = Icons.Rounded.Link,
          contentDescription = null
        )
      },
      onConfirm = {
        tryLinkDiscord(it)
      },
      openUri = openURL
    )
  } else {
    createConfirmationDialog(
      title = Res.string.unlink_discord_account_title.compose,
      messageText = Res.string.unlink_discord_account_body.compose,
      confirmText = Res.string.confirm.compose,
      cancelText = Res.string.cancel.compose,
      icon = {
        Icon(
          imageVector = Icons.Rounded.Link,
          contentDescription = null
        )
      },
      onConfirm = {
        tryUnlinkDiscord()
      },
      openUri = openURL
    )
  }

  if (isOpen) {
    Dialog()
  }

  SettingsButtonItem(
    text = Res.string.discord_account.compose,
    buttonText =
      if (system.discordID == null)
        Res.string.link.compose
      else
        Res.string.unlink.compose,
    spotlightDescription = Res.string.tooltip_discord_account_desc.compose,
    cardGroupPosition = cardGroupPosition,
    enabled = buttonEnabled,
    onClick = openDialog
  )
}

@Composable
private fun SettingsAppleAccount(
  cardGroupPosition: CardGroupPosition,
  system: MySystem,
  tryLinkApple: ((String) -> Unit) -> Unit,
  tryUnlinkApple: () -> Unit,
  openURL: (String, ColorSchemeParams) -> Unit
) {
  val buttonEnabled = system.email != null || system.discordID != null

  val (Dialog, isOpen, openDialog) = if (system.appleID == null) {
    createConfirmationDialog(
      title = Res.string.link_apple_account_title.compose,
      messageText = Res.string.link_apple_account_body.compose,
      confirmText = Res.string.confirm.compose,
      cancelText = Res.string.cancel.compose,
      icon = {
        Icon(
          imageVector = Icons.Rounded.Link,
          contentDescription = null
        )
      },
      onConfirm = {
        tryLinkApple(it)
      },
      openUri = openURL
    )
  } else {
    createConfirmationDialog(
      title = Res.string.unlink_apple_account_title.compose,
      messageText = Res.string.unlink_apple_account_body.compose,
      confirmText = Res.string.confirm.compose,
      cancelText = Res.string.cancel.compose,
      icon = {
        Icon(
          imageVector = Icons.Rounded.Link,
          contentDescription = null
        )
      },
      onConfirm = {
        tryUnlinkApple()
      },
      openUri = openURL
    )
  }

  if (isOpen) {
    Dialog()
  }

  SettingsButtonItem(
    text = Res.string.apple_account.compose,
    buttonText =
      if (system.appleID == null)
        Res.string.link.compose
      else
        Res.string.unlink.compose,
    spotlightDescription = Res.string.tooltip_apple_account_desc.compose,
    cardGroupPosition = cardGroupPosition,
    enabled = buttonEnabled,
    onClick = openDialog
  )
}


@Composable
private fun SettingsLogOut(
  cardGroupPosition: CardGroupPosition,
  logOut: () -> Unit
) {
  val (Dialog, isOpen, openDialog) = createConfirmationDialog(
    title = Res.string.logout.compose,
    messageText = Res.string.logout_body.compose,
    confirmText = Res.string.logout.compose,
    cancelText = Res.string.cancel.compose,
    icon = {
      Icon(
        imageVector = Icons.AutoMirrored.Rounded.Logout,
        contentDescription = null
      )
    },
    onConfirm = { logOut() }
  )

  if (isOpen) {
    Dialog()
  }

  SettingsLoneButtonItem(
    text = Res.string.logout.compose,
    spotlightDescription = Res.string.tooltip_logout_desc.compose,
    cardGroupPosition = cardGroupPosition,
    isError = true,
    onClick = openDialog
  )
}

private sealed class ImportStatus {
  object Idle: ImportStatus()
  object Importing: ImportStatus()
  object Failed: ImportStatus()
  class Success(val alterCount: Int): ImportStatus()
}

@Composable
private fun SettingsImportPK(
  cardGroupPosition: CardGroupPosition,
  importPK: (String) -> Unit,
  apiInterface: ApiInterface
) {
  var pkToken by savedState("")
  var status by state<ImportStatus>(ImportStatus.Idle)

  LaunchedEffect(Unit) {
    apiInterface.eventFlow.collect {
      when(it) {
        is ChannelMessage.PKImportComplete -> status = ImportStatus.Success(it.alterCount)
        is ChannelMessage.PKImportFailed -> status = ImportStatus.Failed
        else -> Unit
      }
    }
  }

  val (Dialog, isOpen, openDialog) = createConfirmationDialog(
    title = Res.string.import_pk_title.compose,
    messageContent = {
      LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        when (status) {
          is ImportStatus.Success -> item {
            MarkdownRenderer(
              Res.string.import_complete_pk_body.compose
            )
          }

          is ImportStatus.Failed -> item {
            MarkdownRenderer(
              Res.string.import_failed_pk_body.compose
            )
          }

          is ImportStatus.Idle -> item {
            MarkdownRenderer(
              Res.string.import_pk_body.compose
            )
          }

          is ImportStatus.Importing -> item {
            MarkdownRenderer(
              Res.string.importing_data_body.compose
            )
          }
        }
        if(status !is ImportStatus.Success) {
          item {
            TextField(
              value = pkToken,
              onValueChange = {
                if (it.length > 1_000) return@TextField
                pkToken = it
              },
              label = { Text(Res.string.token.compose) },
              singleLine = true,
              enabled = status == ImportStatus.Idle
            )
          }
        }
      }
    },
    confirmText = Res.string.import.compose,
    cancelText = if(status is ImportStatus.Success) Res.string.confirm.compose else Res.string.cancel.compose,
    icon = {
      when (status) {
        ImportStatus.Idle -> Icon(
          imageVector = Icons.Rounded.FileDownload,
          contentDescription = null
        )

        ImportStatus.Importing -> CircularProgressIndicator()

        ImportStatus.Failed -> Icon(
          imageVector = Icons.Rounded.Error,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.error
        )

        is ImportStatus.Success -> Icon(
          imageVector = Icons.Rounded.Check,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary
        )
      }
    },
    onConfirm = if(status is ImportStatus.Success || status is ImportStatus.Importing) null else { {
      status = ImportStatus.Importing
      importPK(pkToken)
    } },
    closeOnConfirm = false,
    forceOpen = status == ImportStatus.Importing
  )

  if (isOpen) {
    Dialog()
  }

  SettingsButtonItem(
    text = Res.string.pluralkit.compose,
    buttonText = Res.string.import.compose,
    spotlightTitle = Res.string.tooltip_plural_kit_import_title.compose,
    spotlightDescription = Res.string.tooltip_plural_kit_import_desc.compose,
    cardGroupPosition = cardGroupPosition,
    onClick = openDialog
  )
}

@Composable
private fun SettingsImportSP(
  cardGroupPosition: CardGroupPosition,
  importSP: (String, String?) -> Unit,
  apiInterface: ApiInterface,
  settings: SettingsInterface
) {
  var spToken by savedState("")
  var status by state<ImportStatus>(ImportStatus.Idle)
  var recoveryCode by savedState("")
  var generatedRecoveryCode by savedState<String?>(null)

  val focusRequester = remember { FocusRequester() }

  val systemState by apiInterface.systemMe.collectAsState()
  val encryptionIsInitializing by apiInterface.encryptionIsInitializing.collectAsState(false)

  // Determine if we have a usable local encryption key
  var isEncryptionValid by savedState<Boolean?>(null)
  val settingsData by settings.collectAsState()

  LaunchedEffect(systemState, settingsData.encryptedEncryptionKey) {
    // Only attempt key retrieval when server says encryption is initialized
    if (systemState.isSuccess && systemState.ensureData.encryptionInitialized) {
      isEncryptionValid = settingsData.encryptedEncryptionKey != null
    } else {
      isEncryptionValid = null
    }
  }

  LaunchedEffect(Unit) {
    apiInterface.eventFlow.collect {
      when(it) {
        is ChannelMessage.SPImportComplete -> status = ImportStatus.Success(it.alterCount)
        is ChannelMessage.SPImportFailed -> status = ImportStatus.Failed
        else -> Unit
      }
    }
  }

  LaunchedEffect(Unit) {
    apiInterface.errorFlow.collect {
      if (status is ImportStatus.Importing) {
        status = ImportStatus.Failed
      }
    }
  }

  var recoverDialogOpen by savedState(false)
  var setupDialogOpen by savedState(false)
  var resetDialogOpen by savedState(false)

  val (Dialog, isOpen, openDialog) = createConfirmationDialog(
    title = Res.string.import_sp_title.compose,
    messageContent = {
      LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        when (status) {
          is ImportStatus.Success -> item {
            MarkdownRenderer(
              Res.string.import_complete_sp_body.compose
            )
          }

          is ImportStatus.Failed -> item {
            MarkdownRenderer(
              Res.string.import_failed_sp_body.compose
            )
          }

          is ImportStatus.Idle -> item {
            MarkdownRenderer(
              Res.string.import_sp_body.compose
            )
          }

          is ImportStatus.Importing -> item {
            MarkdownRenderer(
              Res.string.importing_data_body.compose
            )
          }
        }

        if(status !is ImportStatus.Success) {
          item {
            TextField(
              value = spToken,
              onValueChange = {
                if (it.length > 1_000) return@TextField
                spToken = it
              },
              label = { Text(Res.string.token.compose) },
              singleLine = true,
              enabled = status == ImportStatus.Idle && !encryptionIsInitializing
            )
          }

          // Recovery code / recovery UI visibility rules:
          // 1) If server encryption is NOT initialized -> show explanatory text (notes won't import)
          // 2) If server encryption IS initialized AND local key is valid -> show recovery code TextField
          // 3) If server encryption IS initialized AND local key is NOT valid -> show recover flow button
          // 4) If server encryption IS initializing -> disable inputs / show progress where appropriate
          item {
            when {
              !systemState.isSuccess || !systemState.ensureData.encryptionInitialized -> {
                // Encryption not initialized on server — offer setup dialog
                Column {
                  MarkdownRenderer(Res.string.encryption_not_initialized_sp_note.compose)
                  Spacer(modifier = Modifier.height(8.dp))
                  Button(onClick = { setupDialogOpen = true }) {
                    Text(Res.string.encryption_not_initialized_card_button.compose)
                  }
                }
              }

              isEncryptionValid == true -> {
                val alphabet = listOf(
                  'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'J', 'K', 'L', 'M',
                  'N', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
                  '2', '3', '4', '5', '6', '7', '8', '9'
                )

                TextField(
                  value = recoveryCode,
                  onValueChange = {
                    val validChars = it.uppercase().filter { char -> char in alphabet }

                    if (validChars.length > 16) {
                      return@TextField
                    }

                    recoveryCode = validChars
                  },
                  label = { Text(Res.string.recovery_code.compose) },
                  singleLine = true,
                  modifier = Modifier.focusRequester(focusRequester),
                  visualTransformation = MaskVisualTransformation("####-####-####-####"),
                  isError = recoveryCode.isNotEmpty() && recoveryCode.length != 16,
                  enabled = status == ImportStatus.Idle && !encryptionIsInitializing
                )

                LaunchedEffect(focusRequester) {
                  focusRequester.requestFocus()
                }

                Text(
                  Res.string.recovery_code_sp_note.compose,
                  style = MaterialTheme.typography.bodySmall
                )
              }

              isEncryptionValid == false -> {
                Column {
                  Text(Res.string.recovery_code_sp_note_recover.compose)
                  Spacer(modifier = Modifier.height(8.dp))
                  Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { recoverDialogOpen = true }) {
                      Text(Res.string.encryption_recovery_needed_card_button.compose)
                    }
                    Button(
                      onClick = { resetDialogOpen = true },
                      colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                      )
                    ) {
                      Text(Res.string.reset_encryption_card_button.compose)
                    }
                  }
                }
              }

              else -> {
                // Unknown state; show nothing special and keep recovery field hidden
              }
            }
          }
        }
      }
    },
    confirmText = Res.string.import.compose,
    cancelText = if(status is ImportStatus.Success) Res.string.confirm.compose else Res.string.cancel.compose,
    icon = {
      when (status) {
        ImportStatus.Idle -> Icon(
          imageVector = Icons.Rounded.FileDownload,
          contentDescription = null
        )

        ImportStatus.Importing -> CircularProgressIndicator()

        ImportStatus.Failed -> Icon(
          imageVector = Icons.Rounded.Error,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.error
        )

        is ImportStatus.Success -> Icon(
          imageVector = Icons.Rounded.Check,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary
        )
      }
    },
    onConfirm = if(status is ImportStatus.Success || status is ImportStatus.Importing) null else { {
      status = ImportStatus.Importing
      if (recoveryCode.isNotEmpty() && recoveryCode.length == 16) {
        importSP(spToken, recoveryCode)
      } else {
        importSP(spToken, null)
      }
    } },
    closeOnConfirm = false,
    forceOpen = status == ImportStatus.Importing
  )

  if (isOpen) {
    Dialog()
  }

  if (recoverDialogOpen) {
    RecoverEncryptionDialog(apiInterface, settings, closeDialog = { recoverDialogOpen = false }, onRecovered = {
      recoveryCode = it.replace("-", "")
    })
  }

  if (resetDialogOpen) {
    ResetEncryptionDialog(apiInterface) { resetDialogOpen = false }
  }

  if (setupDialogOpen) {
    SetupEncryptionDialog(
      apiInterface,
      settings,
      closeDialog = { setupDialogOpen = false },
      onRecoveryCodeGenerated = { generatedRecoveryCode = it.replace("-", "") },
      onSetupSuccess = {
        recoveryCode = it.replace("-", "")
        generatedRecoveryCode = null
      }
    )
  }

  SettingsButtonItem(
    text = Res.string.simply_plural.compose,
    buttonText = Res.string.import.compose,
    spotlightTitle = Res.string.tooltip_simply_plural_import_title.compose,
    spotlightDescription = Res.string.tooltip_simply_plural_import_desc.compose,
    cardGroupPosition = cardGroupPosition,
    onClick = openDialog
  )
}

@Composable
private fun SettingsResetSettings(cardGroupPosition: CardGroupPosition, nukeEverything: (fully: Boolean) -> Unit) {
  val (Dialog, isOpen, openDialog) = createConfirmationDialog(
    title = Res.string.reset_device_settings.compose,
    messageText = Res.string.reset_device_settings_body.compose,
    confirmText = Res.string.confirm.compose,
    cancelText = Res.string.cancel.compose,
    icon = {
      Icon(
        imageVector = Icons.Rounded.Refresh,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.error
      )
    },
    onConfirm = {
      nukeEverything(false)
    }
  )

  if (isOpen) {
    Dialog()
  }

  SettingsLoneButtonItem(
    text = Res.string.reset_device_settings.compose,
    isError = true,
    spotlightDescription = Res.string.tooltip_reset_device_settings_desc.compose,
    cardGroupPosition = cardGroupPosition,
    onClick = openDialog
  )
}

@Composable
private fun SettingsWipeAlters(cardGroupPosition: CardGroupPosition, api: ApiInterface) {
  val haptics = LocalHapticFeedback.current

  var dialogOpen by savedState(false)
  var isWiping by savedState(false)
  var confirmCount by savedState(0)

  val latestEvent by api.eventFlow.collectAsState(null)

  LaunchedEffect(latestEvent) {
    when (latestEvent) {
      is ChannelMessage.AltersWiped -> {
        confirmCount = 0
        isWiping = false
        dialogOpen = false
      }

      else -> Unit
    }
  }

  if (dialogOpen) {
    AlertDialog(
      icon = {
        Icon(
          Icons.Rounded.Lock,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.error
        )
      },
      title = {
        Text(text = Res.string.wipe_alters.compose)
      },
      text = {
        LazyColumn {
          item {
            Text(Res.string.wipe_alters_body.compose)
          }
        }
      },
      onDismissRequest = {
        if (!isWiping) {
          dialogOpen = false
          confirmCount = 0
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
            if (confirmCount >= 3 && !isWiping) {
              isWiping = true
              api.wipeAlters()
            }
          },
          enabled = !isWiping
        ) {
          Text(Res.string.confirm.compose + if (confirmCount > 0) " ($confirmCount/3)" else "")
        }
      },
      dismissButton = {
        TextButton(
          onClick = {
            if (!isWiping) {
              dialogOpen = false
              confirmCount = 0
            }
          },
          enabled = !isWiping
        ) {
          Text(Res.string.cancel.compose)
        }
      }
    )

  }

  SettingsLoneButtonItem(
    text = Res.string.wipe_alters.compose,
    isError = true,
    spotlightDescription = Res.string.tooltip_wipe_alters_desc.compose,
    cardGroupPosition = cardGroupPosition,
    onClick = { dialogOpen = true }
  )
}

@Composable
private fun SettingsDeleteAccount(cardGroupPosition: CardGroupPosition, deleteAccount: () -> Unit) {
  val haptics = LocalHapticFeedback.current

  var dialogOpen by savedState(false)
  var isDeleting by savedState(false)
  var confirmCount by savedState(0)

  if (dialogOpen) {
    AlertDialog(
      icon = {
        Icon(
          Icons.Rounded.Lock,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.error
        )
      },
      title = {
        Text(text = Res.string.delete_account.compose)
      },
      text = {
        LazyColumn {
          item {
            Text(Res.string.delete_account_body.compose)
          }
        }
      },
      onDismissRequest = {
        if (!isDeleting) {
          dialogOpen = false
          confirmCount = 0
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
            if (confirmCount >= 3 && !isDeleting) {
              isDeleting = true
              deleteAccount()
            }
          },
          enabled = !isDeleting
        ) {
          Text(Res.string.confirm.compose + if (confirmCount > 0) " ($confirmCount/3)" else "")
        }
      },
      dismissButton = {
        TextButton(
          onClick = {
            if (!isDeleting) {
              dialogOpen = false
              confirmCount = 0
            }
          },
          enabled = !isDeleting
        ) {
          Text(Res.string.cancel.compose)
        }
      }
    )

  }

  SettingsLoneButtonItem(
    text = Res.string.delete_account.compose,
    isError = true,
    spotlightDescription = Res.string.tooltip_delete_account_desc.compose,
    cardGroupPosition = cardGroupPosition,
    onClick = { dialogOpen = true }
  )
}

@Composable
private fun SettingsServerStatus(cardGroupPosition: CardGroupPosition, openURL: (String, ColorSchemeParams) -> Unit) =
  SettingsNavigationItem(
    text = Res.string.server_status.compose,
    spotlightDescription = Res.string.tooltip_server_status_desc.compose,
    cardGroupPosition = cardGroupPosition,
    url = "https://status.octocon.app",
    openURL = openURL
  )