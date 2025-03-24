package app.octocon.kotlix

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * A Timer class that schedules a [Job] to be called in the future. Can be configured
 * to use a custom retry pattern, such as exponential backoff.
 */
class TimeoutTimer(
  private val timerCalculation: (tries: Int) -> Long,
  private val scope: CoroutineScope
) {

  /** How many tries the Timer has attempted */
  private var tries: Int = 0

  /** The job that has been scheduled to be executed  */
  private var job: Job? = null

  /**
   * Resets the Timer, clearing the number of current tries and stops
   * any scheduled timeouts.
   */
  fun reset() {
    tries = 0
    clearTimer()
  }

  /** Cancels any previous timeouts and scheduled a new one */
  fun scheduleTimeout(block: suspend CoroutineScope.() -> Unit) {
    clearTimer()

    // Schedule a task to be performed after the calculated timeout in milliseconds
    val timeout = timerCalculation(tries + 1)
    job = scope.launch {
      delay(timeout)
      tries += 1
      block(this)
      if (isActive) cancel()
    }
  }

  //------------------------------------------------------------------------------
  // Private
  //------------------------------------------------------------------------------
  private fun clearTimer() {
    // Cancel the job from completing
    job?.cancel()
    job = null
  }
}