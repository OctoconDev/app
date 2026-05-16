package app.octocon.app.utils

import app.octocon.app.api.fetchPublicKey
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Clock

object PublicKeyProvider {
  private var cachedKey: String? = null
  private var cachedAt: Long = 0
  private val mutex = Mutex()

  // default TTL 28 days
  const val TTL_MS: Long = 24 * 28 * 60 * 60 * 1000

  suspend fun getPublicKey(): String {
    mutex.withLock {
      val now = Clock.System.now().toEpochMilliseconds()
      if (cachedKey != null && (now - cachedAt) < TTL_MS) {
        return cachedKey!!
      }

      try {
        val response = fetchPublicKey()
        if (response.data != null && response.data.isNotBlank()) {
          cachedKey = response.data
          cachedAt = now
          return cachedKey!!
        } else {
          throw Exception(response.error ?: "Empty public key response")
        }
      } catch (e: Exception) {
        if (cachedKey != null) return cachedKey!!
        throw e
      }
    }
  }

  /** Non-suspending accessor for the currently cached public key (may be null). */
  fun currentCachedKey(): String? = cachedKey

  fun clearCache() {
    cachedKey = null
    cachedAt = 0
  }

  /**
   * Set a cached key and timestamp from a platform-specific persistent store.
   * This is useful for restoring a previously persisted public key on startup.
   */
  suspend fun setCachedKey(key: String, cachedAtMillis: Long) {
    mutex.withLock {
      cachedKey = key
      cachedAt = cachedAtMillis
    }
  }
}
