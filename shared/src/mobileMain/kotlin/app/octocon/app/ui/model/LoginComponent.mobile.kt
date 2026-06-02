package app.octocon.app.ui.model

import app.octocon.app.api.client
import io.ktor.client.request.get
import io.ktor.http.isSuccess

internal actual suspend fun performHealthCheck(baseUrl: String): HealthCheckResult {
    val readyResponse = client.get("$baseUrl/health/ready")
    val liveResponse = client.get("$baseUrl/health/live")
    return HealthCheckResult(
        readyUp = readyResponse.status.isSuccess(),
        liveUp = liveResponse.status.isSuccess()
    )
}
