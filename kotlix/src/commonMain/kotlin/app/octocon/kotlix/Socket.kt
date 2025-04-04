@file:JvmName("SocketJvm")

package app.octocon.kotlix

import app.octocon.kotlix.ktor.KtorWebSocketTransport
import io.ktor.http.URLBuilder
import io.ktor.http.URLProtocol
import io.ktor.http.Url
import io.ktor.websocket.CloseReason
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.jvm.JvmName

/** Alias for a JSON mapping */
typealias Payload = Map<String, Any?>

/** Alias for a [SharedFlow] of [SocketEvent] */
typealias SocketFlow = MutableSharedFlow<SocketEvent>

/** RFC 6455: indicates a normal closure */
const val WS_CLOSE_NORMAL = 1000

/** RFC 6455: indicates that the connection was closed abnormally */
const val WS_CLOSE_ABNORMAL = 1006

/**
 * A closure that will return an optional [Payload]
 */
typealias PayloadClosure = () -> Payload?

typealias ParamsClosure = (isReconnect: Boolean) -> Payload?

/** A closure that will encode a [Payload] into a JSON String */
typealias EncodeClosure = (List<Any?>) -> String

/** A closure that will decode a JSON String into a [Message] */
typealias DecodeClosure = (String) -> Message

abstract class SocketCommon(
  private val params: ParamsClosure,
  private val vsn: String,
  private val socketFlow: SocketFlow,
  private val encode: EncodeClosure,
  private val decode: DecodeClosure,
  private val scope: CoroutineScope,
  private val transport: (Url, SocketFlow, DecodeClosure) -> Transport = { url, socketFlow, decodeClosure -> KtorWebSocketTransport(url, socketFlow, decode) }
) {
  //------------------------------------------------------------------------------
  // Public Attributes
  //------------------------------------------------------------------------------
  /**
   * The string WebSocket endpoint (ie `"ws://example.com/socket"`,
   * `"wss://example.com"`, etc.) that was passed to the [Socket] during
   * initialization. The [Url] endpoint will be modified by the [Socket] to
   * include `"/websocket"` if missing.
   */
  abstract var endpoint: String

  /** The fully qualified socket [Url] */
  lateinit var endpointUrl: Url

  /** Timeout to use when opening a connection */
  var timeout: Long = Defaults.TIMEOUT

  /** Interval between sending a heartbeat, in ms */
  private var heartbeatIntervalMs: Long = Defaults.HEARTBEAT

  @Suppress("private")
  /** Interval between [Socket] reconnect attempts, in ms */
  private var reconnectAfterMs: ((Int) -> Long) = Defaults.reconnectSteppedBackOff

  /** Interval between [Channel] rejoin attempts, in ms */
  var rejoinAfterMs: ((Int) -> Long) = Defaults.rejoinSteppedBackOff

  /** The optional function to receive logs */
  var logger: ((String) -> Unit)? = null

  /** Disables heartbeats from being sent. Default is false. */
  private var skipHeartbeat: Boolean = false

  //------------------------------------------------------------------------------
  // Private Attributes
  //------------------------------------------------------------------------------
  /** Collection of unclosed [Channel]s created by the [Socket] */
  var channels: MutableList<Channel> = ArrayList()

  /**
   * Buffer of [Push] that need to be sent once the [Socket] has connected. It is an array of [Pair]s
   * that contain the [Push.ref] of the [Push] to send and the [Job] that will send the [Push].
   */
  private var sendBuffer: MutableList<Pair<String?, Job>> = mutableListOf()

  /** Ref counter for [Push] */
  private var ref: Int = 0

  /** [Job] to be triggered every [heartbeatIntervalMs] to send a heartbeat [Push] */
  private var heartbeatJob: Job? = null

  /** Ref counter for the last heartbeat that was sent */
  private var pendingHeartbeatRef: String? = null

  /** Timer to use when attempting to reconnect */
  private var reconnectTimer: TimeoutTimer = TimeoutTimer(
    timerCalculation = reconnectAfterMs,
    scope = scope
  )

  /** True if the [Socket] close was clean. False if not (connection timeout, heartbeat, etc) */
  private var closeWasClean = false

  //------------------------------------------------------------------------------
  // Connection Attributes
  //------------------------------------------------------------------------------
  /** The underlying WebSocket connection */
  private var connection: Transport? = null

  /** @return True if the [connection] exists and is open */
  val isConnected: Boolean
    get() = connection?.readyState == Transport.ReadyState.OPEN

  //------------------------------------------------------------------------------
  // Public
  //------------------------------------------------------------------------------
  /**
   * Connects to the Phoenix Socket. Suspends until the server acknowledges the connection.
   *
   * Internally, calling this function launches a long-living coroutine listening for [SocketEvent] that will either:
   *
   * - on [SocketEvent.FailureEvent]: Propagate a [Channel.Event.ERROR] event to all opened [Channel].
   * - on [SocketEvent.CloseEvent]: Propagate a [Channel.Event.ERROR] event to all opened [Channel], cancel this coroutine and the [heartbeatJob], and try to reconnect to the [Socket] if it closed abnormally.
   * - on [SocketEvent.MessageEvent]: Dispatch the server [Message] to all opened [Channel] bound to the given topic.
   *
   * @return a [SharedFlow] of [SocketEvent]
   */
  @OptIn(ExperimentalCoroutinesApi::class)
  fun connect(isReconnect: Boolean = false) {
    // Do not attempt to connect if already connected
    if (isConnected) return

    // Reset the clean close flag when attempting to connect
    closeWasClean = false

    // Build the new endpointUrl with the params closure. The payload returned
    // from the closure could be different such as a changing authToken.
    endpointUrl = buildEndpointUrl(endpoint, params, vsn, isReconnect)

    // Now create the connection transport and attempt to connect
    connection = transport(endpointUrl, socketFlow, decode)

    connection?.connect(scope, isReconnect)

    scope.launch(CoroutineName("SOCKET_INTERNAL_CONNECTION")) {
      socketFlow.take(1).collect {
        when (it) {
          is SocketEvent.OpenEvent -> onConnectionOpened()
          else -> Unit
        }
      }
    }

    scope.launch(CoroutineName("SOCKET_INTERNAL_LISTENER")) {
      socketFlow.collect {
        when (it) {
          is SocketEvent.FailureEvent -> onConnectionError(it.throwable, it.response)
          is SocketEvent.MessageEvent -> onConnectionMessage(it.text)
          is SocketEvent.CloseEvent -> {
            logItems("Transport: close :: ${it.code}")
            if (!closeWasClean) {
              onConnectionClosed(CloseReason.Codes.byCode(it.code) ?: CloseReason.Codes.NORMAL)
            }
            cancel()
          }

          else -> Unit
        }
      }
    }
  }

  /**
   * Disconnects from the Phoenix Socket and resets the [reconnectTimer].
   *
   * @param code Status code as defined by [Section 7.4 of RFC 6455](http://tools.ietf.org/html/rfc6455#section-7.4).
   * @param reason Reason for shutting down or [code] null.
   *
   * @return [Unit]
   */
  suspend fun disconnect(
    code: CloseReason.Codes = CloseReason.Codes.NORMAL,
    reason: String? = null,
  ) {
    // The socket was closed cleanly by the User
    closeWasClean = true

    // Reset any reconnects and teardown the socket connection
    reconnectTimer.reset()
    teardown(code, reason)
  }

  /**
   * Creates an instance of [Channel] bound to the specified topic and [Socket], taking optional parameters.
   *
   * @param topic the topic to which the [Channel] will subscribe
   * @param socket the socket to which the [Channel] should be bound
   * @param params optional parameters to send to the server while attempting to [Channel.join] the Phoenix Channel.
   *
   * @return an instance of [Channel]
   */
  fun channel(
    topic: String,
    params: Payload = emptyMap()
  ): Channel {
    val channel = Channel(topic, params, this as Socket, socketFlow, scope)
    channels.add(channel)

    return channel
  }

  fun remove(channel: Channel) {
    channels.remove(channel)
  }

  //------------------------------------------------------------------------------
  // Internal
  //------------------------------------------------------------------------------

  /**
   * Sends the specified event and payload to the given topic if the [Socket] is opened,
   * otherwise add the [Push] to the [sendBuffer] which will be sent immediately upon connection.
   *
   * @param topic the topic to which the [payload] should be sent
   * @param event the event associated to the given [payload]
   * @param payload the payload to send to the given [topic]
   * @param ref an optional [Push.ref]
   * @param joinRef an optional [Push.ref] that needs to be set in case of a [Channel.joinPush]
   *
   * @return [Unit]
   */
  internal fun push(
    topic: String,
    event: String,
    payload: Payload,
    ref: String? = null,
    joinRef: String? = null
  ) {
    val pushJob = scope.launch(
      start = CoroutineStart.LAZY,
      context = CoroutineName("PUSH_$ref")
    ) {
      val body = listOf(joinRef, ref, topic, event, payload)
      val data = encode(body)
      connection?.let { transport ->
        logItems("Push: Sending $data")
        transport.send(data)
      }
      cancel()
    }

    if (isConnected) {
      // If the socket is connected, then start the job immediately.
      pushJob.start()
    } else {
      // If the socket is not connected, add the push to a buffer which will
      // be sent immediately upon connection.
      sendBuffer.add(Pair(ref, pushJob))
    }
  }

  /** @return the next [Push.ref], accounting for overflows */
  internal fun makeRef(): String {
    ref += if (ref == Int.MAX_VALUE) 0 else 1
    return ref.toString()
  }

  /** A nullable-aware wrapper around the [logger] lambda */
  internal fun logItems(body: String) {
    logger?.invoke(body)
  }

  /**
   * Disconnect from the Phoenix Socket and cancel the heartbeat.
   *
   * @param code Status code as defined by [Section 7.4 of RFC 6455](http://tools.ietf.org/html/rfc6455#section-7.4).
   * @param reason Reason for shutting down or [code] null.
   *
   * @return [Unit]
   */
  @OptIn(ExperimentalCoroutinesApi::class)
  private suspend fun teardown(
    code: CloseReason.Codes = CloseReason.Codes.NORMAL,
    reason: String? = null,
  ) {
    // Disconnect the transport
    connection?.disconnect(code, reason)
    connection = null

    socketFlow.resetReplayCache()

    // Heartbeats are no longer needed
    heartbeatJob?.let {
      if (it.isActive) it.cancel()
      heartbeatJob = null
    }
  }

  //------------------------------------------------------------------------------
  // Private
  //------------------------------------------------------------------------------
  /** Triggers a [Channel.Event.ERROR] event to all connected [Channel] */
  private fun triggerChannelError() {
    channels.forEach { channel ->
      // Only trigger a channel error if it is in an "opened" state
      if (!(channel.isErrored || channel.isLeaving || channel.isClosed)) {
        channel.tryEmit(Channel.Event.ERROR.value)
      }
    }
  }

  /** Send all [Push] that were buffered before the socket opened */
  private fun flushSendBuffer() {
    if (isConnected && sendBuffer.isNotEmpty()) {
      sendBuffer.forEach { it.second.start() }
      sendBuffer.clear()
    }
  }

  /** Removes a [Push] from the [sendBuffer] with the matching ref */
  internal fun removeFromSendBuffer(ref: String) {
    sendBuffer = sendBuffer
      .filter { it.first != ref }
      .toMutableList()
  }

  internal suspend fun leaveOpenTopic(topic: String) {
    channels
      .firstOrNull { it.topic == topic && (it.isJoined || it.isJoining) }
      ?.let {
        logItems("Transport: Leaving duplicate topic: [$topic]")
        it.leave()
      }
  }

  //------------------------------------------------------------------------------
  // Heartbeat
  //------------------------------------------------------------------------------
  /**
   * Cancels the previous heartbeat if it's running and launches a new one if [skipHeartbeat] is false.
   *
   * @return [Unit]
   */
  private fun resetHeartbeat() {
    // Clear anything related to the previous heartbeat
    pendingHeartbeatRef = null
    heartbeatJob?.let {
      if (it.isActive) it.cancel()
    }
    heartbeatJob = null

    // Do not start up the heartbeat timer if skipHeartbeat is true
    if (skipHeartbeat) return
    val delay = heartbeatIntervalMs
    val period = heartbeatIntervalMs

    heartbeatJob = scope.launch(
      Dispatchers.Default + (CoroutineName("HEARTBEAT"))
    ) {
      delay(delay)
      while (isActive) {
        sendHeartbeat()
        delay(period)
      }
    }
  }

  private fun reconnect() {
    reconnectTimer.scheduleTimeout {
      logItems("Socket attempting to reconnect")
      teardown(CloseReason.Codes.NORMAL)
      connect(true)
    }
  }

  /**
   * Will send a heartbeat [Push] to the server if the [Socket] is connected and try to reconnect
   * to the [Socket] if the previous heartbeat [Push] was never acknowledged by the server.
   *
   * @return [Unit]
   */
  private suspend fun sendHeartbeat() {
    // Do not send if the connection is closed
    if (!isConnected) return

    // If there is a pending heartbeat ref, then the last heartbeat was
    // never acknowledged by the server. Close the connection and attempt
    // to reconnect.
    pendingHeartbeatRef?.let {
      pendingHeartbeatRef = null
      logItems("Transport: Heartbeat timeout. Attempt to re-establish connection")

      // Close the socket, flagging the closure as abnormal
      abnormalClose("Heartbeat timeout")
      return
    }

    // The last heartbeat was acknowledged by the server. Send another one
    pendingHeartbeatRef = makeRef()
    push(
      topic = "phoenix",
      event = Channel.Event.HEARTBEAT.value,
      payload = emptyMap(),
      ref = pendingHeartbeatRef
    )
  }

  /**
   * Closes the [Socket] without attempting to reconnect.
   *
   * @param reason Reason for shutting down.
   *
   * @return [Unit]
   */
  private suspend fun abnormalClose(reason: String) {
    closeWasClean = false

    /*
      We use NORMAL here since the client is the one determining to close the connection. However,
      we keep a flag `closeWasClean` set to false so that the client knows that it should attempt
      to reconnect.
     */
    connection?.disconnect(CloseReason.Codes.NORMAL, reason)
  }

  //------------------------------------------------------------------------------
  // Connection Transport Hooks
  //------------------------------------------------------------------------------
  /**
   * Handles a [SocketEvent.OpenEvent]. Will flush the [sendBuffer] and reset the heartbeat and [reconnectTimer].
   *
   * @return [Unit]
   */
  private fun onConnectionOpened() {
    logItems("Transport: Connected to $endpoint")

    // Reset the closeWasClean flag now that the socket has been connected
    closeWasClean = false

    // Send any messages that were waiting for a connection
    flushSendBuffer()

    // Reset how the socket tried to reconnect
    reconnectTimer.reset()

    // Restart the heartbeat timer
    resetHeartbeat()
  }

  /**
   * Handles a [SocketEvent.CloseEvent]. Will trigger a [Channel.Event.ERROR] event to all opened [Channel],
   * cancel the heartbeat, and will try to reconnect if the socket did not close normally.
   *
   * @param code Status code as defined by [Section 7.4 of RFC 6455](http://tools.ietf.org/html/rfc6455#section-7.4).
   *
   * @return [Unit]
   */
  private fun onConnectionClosed(code: CloseReason.Codes) {
    triggerChannelError()

    // Prevent the heartbeat from triggering if the socket closed
    heartbeatJob?.let {
      if (it.isActive) it.cancel()
    }
    heartbeatJob = null

    // Only attempt to reconnect if the socket did not close normally
    if (!closeWasClean) {
      reconnect()
    }
  }

  /**
   * Handles a [SocketEvent.MessageEvent]. Will dispatch the [Message] to all [Channel] that belong to the topic
   *
   * @param message the [Message] received from the server.
   *
   * @return [Unit]
   */
  private fun onConnectionMessage(message: Message) {
    logItems("Transport: message :: $message")

    // Clear heartbeat ref, preventing a heartbeat timeout disconnect
    if (message.ref == pendingHeartbeatRef) pendingHeartbeatRef = null

    // Dispatch the message to all channels that belong to the topic
    channels
      .filter { it.isMember(message) }
      .forEach { it.tryEmit(message) }
  }

  /**
   * Handles a [SocketEvent.FailureEvent]. Will trigger a [Channel.Event.ERROR] event to all opened [Channel].
   *
   * @param throwable a [Throwable] that will be logged.
   * @param response an optional response to append to the log.
   *
   * @return [Unit]
   */
  private fun onConnectionError(
    throwable: Throwable,
    response: Any?
  ) {
    logItems("Transport: error :: $throwable :: $response")

    // Send an error to all channels
    triggerChannelError()
  }
}


/**
 * Connects to a Phoenix Socket
 */

/**
 * A [Socket] which connects to a Phoenix Socket. Takes a closure to allow for changing parameters
 * to be sent to the server when connecting.
 *
 * ## Example
 * ```
 * val socket = Socket("https://example.com/socket", { mapOf("token" to mAuthToken) })
 * ```
 * @param url Url to connect to such as https://example.com/socket
 * @param paramsClosure Closure which allows to change parameters sent during connection.
 * @param vsn JSON Serializer version to use. Defaults to 2.0.0
 * @param encode Optional. Provide a custom JSON encoding implementation
 * @param decode Optional. Provide a custom JSON decoding implementation
 */
class Socket(
  url: String,
  paramsClosure: ParamsClosure,
  vsn: String = "2.0.0",
  socketFlow: SocketFlow,
  encode: EncodeClosure = Defaults.encode,
  decode: DecodeClosure = Defaults.decode,
  scope: CoroutineScope,
  transport: (Url, SocketFlow, DecodeClosure) -> Transport = { url, socketFlow, decodeClosure -> KtorWebSocketTransport(url, socketFlow, decode) }
) : SocketCommon(paramsClosure, vsn, socketFlow, encode, decode, scope, transport) {
  override var endpoint: String = url
}

fun buildEndpointUrl(
  endpoint: String,
  paramsClosure: ParamsClosure,
  vsn: String,
  isReconnect: Boolean = false
): Url {
  val httpUrl = Url(endpoint)
  val urlBuilder = URLBuilder(httpUrl)

  urlBuilder.protocol = when (httpUrl.protocol.name) {
    "http" -> URLProtocol.WS
    "https" -> URLProtocol.WSS
    else -> httpUrl.protocol
  }

  urlBuilder.parameters.append("vsn", vsn)

  // Append any additional query params
  paramsClosure.invoke(isReconnect)?.let {
    it.forEach { (key, value) ->
      urlBuilder.parameters.append(key, value.toString())
    }
  }

  return urlBuilder.build()
}