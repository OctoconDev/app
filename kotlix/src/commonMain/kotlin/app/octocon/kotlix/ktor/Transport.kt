package app.octocon.kotlix.ktor

import app.octocon.kotlix.DecodeClosure
import app.octocon.kotlix.SocketEvent
import app.octocon.kotlix.SocketFlow
import app.octocon.kotlix.Transport
import app.octocon.kotlix.WebSocketTransportCommon
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.client.request.url
import io.ktor.http.Url
import io.ktor.websocket.CloseReason
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.close
import io.ktor.websocket.readReason
import io.ktor.websocket.readText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch

expect fun buildWebsocketHttpClient(): HttpClient

class KtorWebSocketTransport(
  private val url: Url,
  private val socketFlow: SocketFlow,
  val decode: DecodeClosure,
  private val client: HttpClient = buildWebsocketHttpClient()
) : WebSocketTransportCommon() {

  private var session: WebSocketSession? = null
  private var webSocketJob: Job? = null

  override fun connect(scope: CoroutineScope, isReconnect: Boolean) {
    readyState = Transport.ReadyState.CONNECTING

    webSocketJob = scope.launch {
      try {
        session = client.webSocketSession {
          url(this@KtorWebSocketTransport.url)
        }
        readyState = Transport.ReadyState.OPEN
        socketFlow.tryEmit(SocketEvent.OpenEvent(wasReconnect = isReconnect))

        listenForMessages()
      } catch (e: Throwable) {
        readyState = Transport.ReadyState.CLOSED
        socketFlow.tryEmit(SocketEvent.FailureEvent(e, null))
        socketFlow.tryEmit(SocketEvent.CloseEvent(CloseReason.Codes.NORMAL.code))
      }
    }
  }

  override suspend fun disconnect(code: CloseReason.Codes, reason: String?) {
    try {
      session?.close(CloseReason(code, reason.orEmpty()))
      session = null

      webSocketJob?.cancel()
      webSocketJob = null
    } catch(e: Throwable) {
      socketFlow.tryEmit(SocketEvent.FailureEvent(e, null))
    }
  }

  override suspend fun send(data: String) {
    try {
      session?.send(Frame.Text(data))
    } catch (e: Throwable) {
      socketFlow.tryEmit(SocketEvent.FailureEvent(e, data))
    }
  }

  private suspend fun listenForMessages() {
    session?.let {
      it.incoming
        .consumeAsFlow()
        .collect { frame ->
          when (frame) {
            is Frame.Text -> {
              socketFlow.tryEmit(SocketEvent.MessageEvent(decode(frame.readText())))
            }

            is Frame.Close -> {
              readyState = Transport.ReadyState.CLOSED
              socketFlow.tryEmit(
                SocketEvent.CloseEvent(
                  frame.readReason()?.code ?: CloseReason.Codes.NORMAL.code
                )
              )
            }

            else -> {
              // Ignore other frames
            }
          }
        }
    }
  }
}