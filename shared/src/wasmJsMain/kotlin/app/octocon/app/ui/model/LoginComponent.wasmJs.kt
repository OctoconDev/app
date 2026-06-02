package app.octocon.app.ui.model

import kotlinx.coroutines.await
import kotlin.js.Promise

private fun fetchNoCorsJs(url: JsString): Promise<JsAny> =
    js("fetch(url, { mode: 'no-cors' })")

@OptIn(ExperimentalWasmJsInterop::class)
private suspend fun fetchNoCors(url: String): Boolean {
    return try {
        fetchNoCorsJs(url.toJsString()).await<JsAny>()
        true
    } catch (_: Throwable) {
        false
    }
}

internal actual suspend fun performHealthCheck(baseUrl: String): HealthCheckResult {
    val readyUp = fetchNoCors("$baseUrl/health/ready")
    val liveUp = fetchNoCors("$baseUrl/health/live")
    return HealthCheckResult(readyUp = readyUp, liveUp = liveUp)
}
