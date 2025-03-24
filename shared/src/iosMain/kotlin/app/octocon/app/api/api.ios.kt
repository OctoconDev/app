package app.octocon.app.api

import app.octocon.app.utils.globalSerializer
import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow

actual val client: HttpClient = HttpClient(Darwin) {
  install(WebSockets)
  install(ContentNegotiation) {
    json(globalSerializer)
  }

  engine {
    configureRequest {
      setAllowsCellularAccess(true)
    }
  }
}

internal actual fun connectToPhoenixChannel(
  token: String,
  userID: String,
  eventPipeline: MutableSharedFlow<ChannelMessage>,
  errorPipeline: MutableSharedFlow<String>,
  coroutineScope: CoroutineScope,
  onConnected: (String) -> Unit,
): PhoenixSocketSession = KotlixPhoenixSocketSession(
  token = token,
  userID = userID,
  eventPipeline = eventPipeline,
  errorPipeline = errorPipeline,
  coroutineScope = coroutineScope,
  onConnected = onConnected
)