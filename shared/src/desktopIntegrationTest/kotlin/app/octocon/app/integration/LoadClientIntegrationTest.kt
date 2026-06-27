package app.octocon.app.integration

import app.octocon.app.api.APIState
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.jupiter.api.Test
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

/**
 * End-to-end test: a real [app.octocon.app.ui.model.interfaces.ApiInterfaceImpl]
 * against an in-memory backend container completes its WebSocket
 * `phx_join` and populates `systemMe / alters / tags / fronts`.
 *
 * The `@JvmStatic` container is shared across this class so the image only boots
 * once for the whole suite.
 *
 * The auth token is obtained the same way the production client gets one:
 * by driving `GET /auth/discord/callback?uid=<random>` so the backend mints
 * and records the JTI (see [obtainTestToken]). The harness never signs
 * tokens client-side, so the WebSocket auth path's allow-list revocation
 * check succeeds.
 *
 * Uses [runBlocking] + a real-time [withTimeout] rather than
 * `kotlinx.coroutines.test.runTest`: the harness performs real network I/O
 * (WebSocket handshake, Phoenix `phx_join`, REST bootstrap) and `runTest`'s
 * virtual clock would never advance to surface progress.
 */
@Testcontainers
class LoadClientIntegrationTest {

  @Test
  fun loadClient_completesInit_overInMemoryBackend() = runBlocking {
    withTimeout(60.seconds) {
      val principal = obtainTestToken(backend.baseUrl)
      ensureUserExists(backend.baseUrl, principal.token)

      IntegrationHarness(backend.baseUrl, principal.token).use { h ->
        h.loadAndAwaitInit()

        assertTrue(h.api.initComplete.value, "initComplete should be true after loadClient")
        assertTrue(
          h.api.systemMe.value is APIState.Success,
          "systemMe should be Success, was ${h.api.systemMe.value}"
        )
        assertTrue(
          h.api.alters.value is APIState.Success,
          "alters should be Success, was ${h.api.alters.value}"
        )
        assertEquals(
          emptyList(),
          (h.api.alters.value as APIState.Success).data,
          "alters should be empty for a fresh principal"
        )
        assertTrue(
          h.api.tags.value is APIState.Success,
          "tags should be Success, was ${h.api.tags.value}"
        )
        assertTrue(
          h.api.fronts.value is APIState.Success,
          "fronts should be Success, was ${h.api.fronts.value}"
        )
      }
    }
  }

  companion object {
    @JvmStatic
    @Container
    val backend: BackendContainer = BackendContainer()
  }
}
