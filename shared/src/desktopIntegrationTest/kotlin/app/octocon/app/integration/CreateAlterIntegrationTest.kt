package app.octocon.app.integration

import app.octocon.app.api.APIState
import app.octocon.app.api.ChannelMessage
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.jupiter.api.Test
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

/**
 * End-to-end test: `createAlter(name)` round-trips through the socket's
 * `endpoint` proxy, and the backend echoes the matching
 * [ChannelMessage.AlterCreated] event back over the Phoenix channel.
 *
 * Asserts both the event side (`eventFlow` saw the message) and the state side
 * (`alters` StateFlow now contains the new alter).
 *
 * Token-acquisition + dispatcher reasoning live on
 * [LoadClientIntegrationTest]; same approach here.
 */
@Testcontainers
class CreateAlterIntegrationTest {

  @Test
  fun createAlter_emitsAlterCreatedEvent() = runBlocking {
    withTimeout(60.seconds) {
      val principal = obtainTestToken(backend.baseUrl)
      ensureUserExists(backend.baseUrl, principal.token)

      IntegrationHarness(backend.baseUrl, principal.token).use { h ->
        h.loadAndAwaitInit()

        val name = "Test-${System.currentTimeMillis()}"

        // Subscribe BEFORE issuing the mutation so we don't race the reply.
        val firstAlterCreated = h.scope.async {
          h.api.eventFlow.first { it is ChannelMessage.AlterCreated }
        }

        h.api.createAlter(name)

        val createdEvent = withTimeout(15.seconds) {
          firstAlterCreated.await() as ChannelMessage.AlterCreated
        }

        assertTrue(
          createdEvent.alter.name == name,
          "Expected AlterCreated event for '$name', got '${createdEvent.alter.name}'"
        )

        val alters = (h.api.alters.value as APIState.Success).data
        assertTrue(
          alters.any { it.name == name },
          "Expected alter '$name' to appear in alters StateFlow, got ${alters.map { it.name }}"
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
