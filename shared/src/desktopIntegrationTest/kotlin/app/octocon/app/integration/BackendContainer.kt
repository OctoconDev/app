package app.octocon.app.integration

import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.utility.DockerImageName
import java.time.Duration

/**
 * Testcontainers wrapper around the published Octocon backend image in its
 * in-memory persistence mode.
 *
 * - REST + WebSocket share one Kestrel listener on container port 8080.
 * - The committed ES256 keypair under `resources/test-jwt-keys/` is what the
 *   backend uses to *issue* JWTs (via `IssueDeepLinkTokenAsync`) and to
 *   verify them on subsequent requests. The matching public PEM is the
 *   single entry in `OCTOCON_JWT_ES256_VERIFICATION_KEYS`. We never sign
 *   tokens client-side — `obtainTestToken` drives the auth-callback flow
 *   so the backend mints + records each JTI itself.
 * - The `OCTOCON_INMEMORY_SECRETS_SEED__*` env vars (added upstream and
 *   bug-fixed in the image pinned below) seed the runtime's `ISecretsStore`
 *   with `encryption:pepper`, the ES256 signing PEM, and the deep-link
 *   HMAC secret — without them `SecretsBootstrapService` fails fast.
 * - `waitingFor` polls `/health/ready` (mounted by Aspire's ServiceDefaults
 *   at the root, not under `/api`).
 *
 * ## Image pinning
 *
 * [DEFAULT_IMAGE] is pinned to a sha256 digest so the slice stays
 * reproducible across machines and across upstream `:latest` retags. The
 * pin can be overridden via `-Doctocon.backend.image=<image>:<tag>`
 * (forwarded by the Gradle Test task), which is useful when iterating
 * against a fork or testing an unreleased upstream change.
 */
class BackendContainer : GenericContainer<BackendContainer>(resolveImage()) {
  init {
    withEnv("OCTOCON_PERSISTENCE", "inmemory")
    withEnv("ASPNETCORE_URLS", "http://+:8080")
    withEnv("OCTOCON_JWT_AUTHORITY", "octocon-test")
    withEnv("OCTOCON_AUTH_CALLBACK_BASE_URL", "http://localhost:8080")
    withEnv(
      "OCTOCON_JWT_ES256_PRIVATE_KEY_PEM",
      readClasspathResource("test-jwt-keys/private.pem"),
    )
    withEnv(
      "OCTOCON_JWT_ES256_VERIFICATION_KEYS",
      readClasspathResource("test-jwt-keys/public.pem"),
    )
    withEnv(BackendSeedEnvVars.ENCRYPTION_PEPPER, "TEST")
    withEnv(
      BackendSeedEnvVars.JWT_ES256_PRIVATE_PEM,
      readClasspathResource("test-jwt-keys/private.pem"),
    )
    withEnv(BackendSeedEnvVars.DEEP_LINK_SECRET, "TEST_DEEP_LINK_SECRET")
    withExposedPorts(8080)
    waitingFor(
      Wait.forHttp("/health/ready")
        .forStatusCode(200)
        .withStartupTimeout(Duration.ofMinutes(2))
    )
    withLogConsumer { frame -> print("[backend] " + frame.utf8String) }
  }

  /**
   * Base URL for HTTP and WebSocket access. No trailing slash; no `/api`
   * suffix.
   *
   * Connects to the container's internal IP + internal port (8080) rather
   * than the docker-host's random mapped port. This matters for the
   * WebSocket `endpoint` proxy: upstream `WebSocketHandler.HandleEndpointProxyAsync`
   * builds its loopback target as
   * `{Request.Scheme}://{Request.Host}{path}`, so the inbound `Host`
   * header has to be one the container can re-resolve to its own listener.
   * If we used `localhost:<random-mapped-port>` here the WS join would
   * still succeed but every `endpoint`-proxied REST call (e.g.
   * `createAlter`) would hang because the container can't reach
   * `localhost:<random>` from inside itself.
   *
   * Requires the host JVM to be able to reach Docker's bridge network IPs
   * — true by default on Linux, true with Docker Desktop's networking on
   * macOS/Windows. If a future runtime breaks this, fall back to
   * `addFixedExposedPort(8080, 8080)` and a `localhost:8080` URL.
   */
  val baseUrl: String
    get() {
      val networks = containerInfo.networkSettings.networks
      val ip = networks["bridge"]?.ipAddress
        ?: networks.values.firstOrNull()?.ipAddress
        ?: error(
          "Container has no resolvable network IP. " +
            "Networks: ${networks.keys}",
        )
      return "http://$ip:8080"
    }

  companion object {
    // Pinned digest of ghcr.io/azyyyyyy/interfold-api at upstream revision
    // 7ba967c6 — the first published build with the bug-fixed
    // OCTOCON_INMEMORY_SECRETS_SEED__* env-var lookup. Bumping this pin
    // means: (a) verifying bootstrap with a docker-run smoke test, and
    // (b) re-running :shared:desktopIntegrationTest locally.
    private const val DEFAULT_IMAGE =
      "ghcr.io/azyyyyyy/interfold-api@sha256:1727e25a969a052ad9d2bfe5392c0375068eea5fa026e082b97006901e010816"

    private fun resolveImage(): DockerImageName {
      val override = System.getProperty("octocon.backend.image").orEmpty()
      val image = if (override.isBlank()) DEFAULT_IMAGE else override
      println("[backend] Using image $image (override via -Doctocon.backend.image=<tag>)")
      return DockerImageName.parse(image)
    }
  }
}

/** Env-var names the upstream image honours for in-memory secrets seeding. */
private object BackendSeedEnvVars {
  const val ENCRYPTION_PEPPER = "OCTOCON_INMEMORY_SECRETS_SEED__ENCRYPTION_PEPPER"
  const val JWT_ES256_PRIVATE_PEM = "OCTOCON_INMEMORY_SECRETS_SEED__AUTH_JWT_ES256_PRIVATE_PEM"
  const val DEEP_LINK_SECRET = "OCTOCON_INMEMORY_SECRETS_SEED__AUTH_DEEP_LINK_SECRET"
}

private fun readClasspathResource(path: String): String {
  val stream = BackendContainer::class.java.classLoader.getResourceAsStream(path)
    ?: error("Classpath resource not found: $path")
  return stream.bufferedReader().use { it.readText() }
}
