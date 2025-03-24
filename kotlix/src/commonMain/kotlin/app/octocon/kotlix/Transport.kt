package app.octocon.kotlix

import io.ktor.websocket.CloseReason
import kotlinx.coroutines.CoroutineScope

sealed class SocketEvent : Throwable() {
  /** Called when the Transport opens */
  data class OpenEvent(val wasReconnect: Boolean) : SocketEvent()

  /** Called when the Transport receives an error */
  data class FailureEvent(val throwable: Throwable, val response: Any?) : SocketEvent()

  /** Called each time the Transport receives a message */
  data class MessageEvent(val text: Message) : SocketEvent()

  /** Called when the Transport closes */
  data class CloseEvent(val code: Short) : SocketEvent()
}

/**
 * Interface that defines different types of Transport layers.
 */
interface Transport {

  /** Available ReadyStates of a $Transport. */
  enum class ReadyState {

    /** The Transport is connecting to the server  */
    CONNECTING,

    /** The Transport is connected and open */
    OPEN,

    /** The Transport is closing */
    CLOSING,

    /** The Transport is closed */
    CLOSED
  }

  /** The state of the Transport. See {@link ReadyState} */
  val readyState: ReadyState

  /** Connect to the server */
  fun connect(scope: CoroutineScope, isReconnect: Boolean)

  /**
   * Disconnect from the Server
   *
   * @param code Status code as defined by <a
   * href="http://tools.ietf.org/html/rfc6455#section-7.4">Section 7.4 of RFC 6455</a>.
   * @param reason Reason for shutting down or {@code null}.
   */
  suspend fun disconnect(code: CloseReason.Codes, reason: String? = null)

  /**
   * Sends text to the Server
   */
  suspend fun send(data: String)
}

abstract class WebSocketTransportCommon : Transport {
  override var readyState: Transport.ReadyState = Transport.ReadyState.CLOSED
}