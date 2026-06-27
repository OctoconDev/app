package app.octocon.app.integration

import app.octocon.app.Settings
import app.octocon.app.ui.model.interfaces.ApiInterfaceImpl
import app.octocon.app.ui.model.interfaces.SettingsInterfaceImpl
import app.octocon.app.utils.platformUtilities
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeout

/**
 * Constructs a real [ApiInterfaceImpl] graph pointed at the running
 * [BackendContainer] and exposes a single `loadAndAwaitInit` helper that
 * blocks until `initComplete` flips true (or the timeout elapses).
 *
 * Lives in `desktopIntegrationTest`, which associates with the JVM `main`
 * compilation, so this code can see the `internal class ApiInterfaceImpl`
 * without changing its production visibility.
 */
internal class IntegrationHarness(val baseUrl: String, val token: String) : AutoCloseable {
  /** Long-lived scope hosting the API client. Tests can use it for collectors
   *  that need to outlive `runTest`'s test dispatcher. */
  val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

  private val settings = SettingsInterfaceImpl(
    initialSettings = Settings(apiEndpoint = baseUrl, token = token),
    settingsSaver = { /* no-op: tests don't persist */ },
    platformUtilities = platformUtilities,
  )

  val api: ApiInterfaceImpl = ApiInterfaceImpl(
    coroutineScope = scope,
    platformUtilities = platformUtilities,
    settingsInterface = settings,
  )

  suspend fun loadAndAwaitInit(timeout: Duration = 30.seconds) {
    api.loadClient(token)
    withTimeout(timeout) { api.initComplete.first { it } }
  }

  override fun close() {
    api.onDestroy()
    scope.cancel()
  }
}
