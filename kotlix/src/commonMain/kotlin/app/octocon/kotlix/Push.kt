package app.octocon.kotlix

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

/**
 * A Push represents an attempt to send a payload through a Channel for a specific event.
 */
class Push(
  /** The channel the Push is being sent through */
  val channel: Channel,
  /** The event the Push is targeting */
  val event: String,
  /** The message to be sent */
  var payload: Payload = emptyMap(),
  /** Duration before the message is considered timed out and failed to send */
  var timeout: Long = Defaults.TIMEOUT,
  private val scope: CoroutineScope
) : SharedFlow<Message> {

  //------------------------------------------------------------------------------
  // Private
  //------------------------------------------------------------------------------
  /** The SharedFlow associated with the Push **/
  private val _push = MutableSharedFlow<Message>(8 * 1024)
  private val push = _push.asSharedFlow()

  override suspend fun collect(collector: FlowCollector<Message>): Nothing = push.collect(collector)
  override val replayCache = push.replayCache

  /** The server's response to the Push */
  private var receivedMessage: Message? = null

  /** The job that holds a special timeout Push if the main Push times out */
  private var timeoutJob: Job? = null

  /** True if the Push has been sent */
  private var sent: Boolean = false

  /** The event that is associated with the reference ID of the Push */
  private var refEvent: String? = null

  //------------------------------------------------------------------------------
  // Public
  //------------------------------------------------------------------------------
  /**
   * Resets and sends the Push
   * @param timeout Optional. The push timeout. Default is 10_000ms = 10s
   */
  suspend fun resend(timeout: Long = Defaults.TIMEOUT) {
    this.timeout = timeout
    reset()
    send()
  }

  suspend fun emit(value: Message) = _push.emit(value)

  /**
   * Sends the Push. If it has already timed out then the call will be ignored. use
   * `resend(timeout:)` in this case.
   */
  suspend fun send() {
    if (hasReceived("timeout")) return

    startTimeout()
    sent = true
    channel.socket.push(channel.topic, event, payload, ref, channel.joinRef)
  }

  //------------------------------------------------------------------------------
  // Internal
  //------------------------------------------------------------------------------
  /** The reference ID of the Push */
  internal var ref: String? = null

  /** Resets the Push as it was after it was first initialized. */
  internal fun reset() {
    ref = null
    refEvent = null
    receivedMessage = null
    sent = false
  }

  /**
   * Triggers an event to be sent through the Push's parent Channel
   */
  internal fun trigger(status: String, payload: Payload) {
    refEvent?.let { refEvent ->
      val mutPayload = payload.toMutableMap()
      mutPayload["status"] = status

      channel.tryEmit(refEvent, mutPayload)
    }
  }

  /**
   * Schedules a timeout task which will be triggered after a specific timeout is reached
   */
  internal suspend fun startTimeout() {
    // Cancel any existing timeout before starting a new one
    timeoutJob?.let { if (!it.isCancelled || it.isActive) cancelTimeout() }

    // Get the ref of the Push
    val ref = channel.socket.makeRef()
    val refEvent = channel.replyEventName(ref)

    this@Push.ref = ref
    this@Push.refEvent = refEvent

    scope.launch(CoroutineName("PUSH_TIMEOUT_LISTENER_$ref")) {
      // Subscribe to a reply from the server when the Push is received
      channel.collect { message ->
        if (refEvent == message.event) {
          cancelTimeout()
          receivedMessage = message
          emit(message)
          cancel()
        }
      }
    }

    // Setup and start the Timer
    timeoutJob = scope.launch(
      Dispatchers.Default + CoroutineName("TIMEOUT_$ref")
    ) {
      delay(timeout)
      trigger("timeout", hashMapOf())
      cancel()
    }
  }

  //------------------------------------------------------------------------------
  // Private
  //------------------------------------------------------------------------------
  /** Cancels any ongoing timeout task */
  internal fun cancelTimeout() {
    timeoutJob?.cancel()
    timeoutJob = null
  }

  /**
   * @param status Status to check if it has been received
   * @return True if the status has already been received by the Push
   */
  private fun hasReceived(status: String): Boolean {
    return receivedMessage?.status == status
  }
}