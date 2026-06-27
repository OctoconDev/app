package app.octocon.app.integration

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import java.util.UUID
import kotlinx.serialization.Serializable

/**
 * Bootstraps a principal in the in-memory backend so subsequent calls have a
 * profile to attach to.
 *
 * Mirrors `BaseEndpointTest.EnsureUserExistsAsync` in the upstream C# suite:
 * one `POST /api/settings/username` with the minted bearer token and a JSON
 * body containing the desired username. The in-memory persistence layer
 * provisions the principal on first contact.
 */
suspend fun ensureUserExists(
  baseUrl: String,
  token: String,
  username: String = "test-${UUID.randomUUID().toString().take(8)}",
) {
  HttpClient(OkHttp) {
    install(ContentNegotiation) { json() }
  }.use { client ->
    val response = client.post("$baseUrl/api/settings/username") {
      bearerAuth(token)
      contentType(ContentType.Application.Json)
      setBody(UsernameBody(username))
    }
    check(response.status.isSuccess()) {
      "ensureUserExists failed: ${response.status} ${response.bodyAsText()}"
    }
  }
}

@Serializable
private data class UsernameBody(val username: String)
