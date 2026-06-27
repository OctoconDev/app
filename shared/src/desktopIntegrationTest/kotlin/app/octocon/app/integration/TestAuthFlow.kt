package app.octocon.app.integration

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.request.cookie
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.URLBuilder
import java.util.UUID

/**
 * Drives the in-memory backend through a real auth-callback handshake to
 * obtain a properly-issued, properly-recorded JWT.
 *
 * The published image's `IAuthTokenRevocationRepository` uses allow-list
 * semantics: unknown `jti` ⇒ rejected. The only path that calls
 * `RecordTokenAsync` is `AuthController.IssueDeepLinkTokenAsync`, invoked
 * exclusively from the OAuth callback handler. We therefore mint tokens
 * the same way the production client does: by going through
 * `GET /auth/discord/callback`.
 *
 * The harness deliberately uses the `?uid=<value>` fallback (instead of a
 * real OAuth `code` exchange) — see `OAuthControllerBase.ExtractProviderIdentityAsync`,
 * which uses the query-param shortcut when no `code` is present. That's the
 * same shortcut the upstream C# in-process suite uses
 * (`AuthControllerTests.Api_OAuthCallback_IssuesJwsCompactSerializationToken`).
 *
 * The in-memory `InMemoryAccountRepository.FindSystemIdByDiscordIdAsync`
 * auto-creates a system on miss, so a fresh `uid` per call gives us a
 * brand-new account with a properly recorded JTI.
 *
 * @return a [TestPrincipal] carrying the issued JWT plus the scoped
 * system id ("<region>:<uuid>") the backend allocated.
 */
internal suspend fun obtainTestToken(
  baseUrl: String,
  provider: String = "discord",
  uid: String = "test-${UUID.randomUUID().toString().take(8)}",
  redirectUri: String = REDIRECT_PLACEHOLDER,
): TestPrincipal {
  // Do not auto-follow redirects: we need to read the Location header
  // ourselves to extract the `token` query param the backend issues. The
  // redirect target (`redirectUri`) is a non-routable placeholder by
  // default, so an auto-follow would also fail to connect — explicit
  // capture is both correct and necessary. Note that we deliberately do
  // NOT install Ktor's `HttpRedirect` plugin; the plugin operates above
  // the engine and would override this flag.
  HttpClient(OkHttp) {
    followRedirects = false
    expectSuccess = false
  }.use { client ->
    val callback = "$baseUrl/auth/${provider}/callback?uid=$uid"
    val response = client.get(callback) {
      cookie(name = REDIRECT_COOKIE_NAME, value = redirectUri)
    }
    check(response.status.value in 300..399) {
      "Expected 30x from /auth/$provider/callback?uid=…, got ${response.status}: ${response.bodyAsText()}"
    }
    val location = response.headers[HttpHeaders.Location]
      ?: error("Callback response missing Location header")
    val parsed = URLBuilder(location).build()
    val token = parsed.parameters["token"]
      ?: error("Callback redirect URL has no 'token' query param: $location")
    val systemId = parsed.parameters["id"]
      ?: error("Callback redirect URL has no 'id' query param: $location")
    return TestPrincipal(token = token, scopedSystemId = systemId)
  }
}

internal data class TestPrincipal(
  /** Bearer JWT, registered via `RecordTokenAsync` on the backend. */
  val token: String,
  /** Scoped system id in `<region>:<uuid>` form, as returned by the callback. */
  val scopedSystemId: String,
)

private const val REDIRECT_COOKIE_NAME = "octocon_auth_redirect_uri"

/**
 * Non-routable placeholder consumed by the callback to build its 302
 * Location header. The harness never follows the redirect — it parses the
 * `token` query param out of the Location header — so the URL only has to
 * be syntactically valid.
 */
private const val REDIRECT_PLACEHOLDER = "https://test.invalid/auth/done"
