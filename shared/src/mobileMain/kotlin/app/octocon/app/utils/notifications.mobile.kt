package app.octocon.app.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import app.octocon.app.ui.model.interfaces.ApiInterface
import app.octocon.app.ui.model.interfaces.SettingsInterface
import dev.icerock.moko.permissions.DeniedAlwaysException
import dev.icerock.moko.permissions.DeniedException
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.PermissionState
import dev.icerock.moko.permissions.PermissionsController
import dev.icerock.moko.permissions.compose.BindEffect
import dev.icerock.moko.permissions.compose.PermissionsControllerFactory
import dev.icerock.moko.permissions.compose.rememberPermissionsControllerFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

private fun tryInit(
  permissionsScope: CoroutineScope,
  permissionsController: PermissionsController,
  platformUtilities: PlatformUtilities,
  commit: (Boolean) -> Unit
) {
  permissionsScope.launch {
    if (permissionsController.isPermissionGranted(Permission.REMOTE_NOTIFICATION)) {
      commit(true)
      platformUtilities.performAdditionalPushNotificationSetup()
      return@launch
    }
    try {
      permissionsController.providePermission(Permission.REMOTE_NOTIFICATION)
      platformUtilities.performAdditionalPushNotificationSetup()
      commit(true)
    } catch (_: DeniedAlwaysException) {
      platformUtilities.showAlert("You must enable notification permissions in your phone's settings.")
      commit(false)
    } catch (_: DeniedException) {
      platformUtilities.showAlert("You must accept the permissions request to show push notifications.")
      commit(false)
    }
  }
}

fun tryInitPushNotifications(
  pushToken: String,
  apiInterface: ApiInterface,
  settingsInterface: SettingsInterface,
  platformUtilities: PlatformUtilities,
  permissionsController: PermissionsController,
  permissionsScope: CoroutineScope
) {
  apiInterface.provideFirebaseMessagingToken(pushToken)
  if (settingsInterface.data.value.showPushNotifications) {
    permissionsScope.launch {
      val state = permissionsController.getPermissionState(Permission.REMOTE_NOTIFICATION)
      when (state) {
        PermissionState.NotDetermined -> {
          settingsInterface.setShowPushNotifications(
            true,
            platformUtilities::showAlert,
            sendToken = apiInterface::updatePushNotificationToken,
            invalidateToken = apiInterface::invalidatePushNotificationToken,
            tryInit = { tryInit(permissionsScope, permissionsController, platformUtilities, it) }
          )
        }

        PermissionState.Denied, PermissionState.DeniedAlways -> {
          settingsInterface.setShowPushNotifications(
            false,
            platformUtilities::showAlert,
            sendToken = apiInterface::updatePushNotificationToken,
            invalidateToken = apiInterface::invalidatePushNotificationToken,
            tryInit = { tryInit(permissionsScope, permissionsController, platformUtilities, it) }
          )
        }

        else -> apiInterface.updatePushNotificationToken()
      }
    }
  }
}

internal fun setShowPushNotifications(
  showPushNotifications: Boolean,
  apiInterface: ApiInterface,
  settingsInterface: SettingsInterface,
  platformUtilities: PlatformUtilities,
  permissionsScope: CoroutineScope,
  permissionsController: PermissionsController
) {
  settingsInterface.setShowPushNotifications(
    showPushNotifications,
    platformUtilities::showAlert,
    sendToken = apiInterface::updatePushNotificationToken,
    invalidateToken = apiInterface::invalidatePushNotificationToken,
    tryInit = { tryInit(permissionsScope, permissionsController, platformUtilities, it) }
  )
}

@Composable
internal actual fun InitPushNotifications(): InitPushNotificationsCallbacks {
  val permissionsFactory: PermissionsControllerFactory =
    rememberPermissionsControllerFactory()

  val permissionsController: PermissionsController =
    remember(permissionsFactory) { permissionsFactory.createPermissionsController() }

  val permissionsScope: CoroutineScope = rememberCoroutineScope()

  BindEffect(permissionsController)

  return { token: String, api: ApiInterface, settings: SettingsInterface, platformUtilities: PlatformUtilities ->
    tryInitPushNotifications(
      token,
      api,
      settings,
      platformUtilities,
      permissionsController = permissionsController,
      permissionsScope = permissionsScope
    )
  } to { show, api, settings, platformUtilities ->
    setShowPushNotifications(
      show,
      api,
      settings,
      platformUtilities,
      permissionsScope = permissionsScope,
      permissionsController = permissionsController
    )
  }
}