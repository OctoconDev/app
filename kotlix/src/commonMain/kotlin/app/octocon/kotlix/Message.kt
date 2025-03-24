package app.octocon.kotlix

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer

data class Message(
  /** The ref sent during a join event. Empty if not present. */
  val joinRef: String? = null,

  /** The unique string ref. Empty if not present */
  val ref: String = "",

  /** The message topic */
  val topic: String = "",

  /** The message event name, for example "phx_join" or any other custom name */
  val event: String = "",

  /** The raw payload of the message. It is recommended that you use `payload` instead. */
  val rawPayload: Payload = HashMap(),
  val payloadText: String? = null
) {

  /** The payload of the message */
  @Suppress("UNCHECKED_CAST")
  val payload: Payload
    get() = rawPayload["response"] as? Payload ?: rawPayload

  /**
   * Convenience var to access the message's payload's status. Equivalent
   * to checking message.payload["status"] yourself
   */
  val status: String?
    get() = rawPayload["status"] as? String

  inline fun <reified T> deserializePayloadResponse(
    serializer: Json = Json,
    deserializer: DeserializationStrategy<T> = serializer.serializersModule.serializer<T>()
  ): Result<T> {
    return if (rawPayload["response"] == null) {
      Result.failure(Exception("No response key found in payload"))
    } else {
      val response = rawPayload["response"]!!.toJsonElement()

      try {
        Result.success(serializer.decodeFromJsonElement(deserializer, response))
      } catch (e: Exception) {
        Result.failure(e)
      }
    }
  }

  inline fun <reified T> deserializePayload(
    serializer: Json = Json,
    deserializer: DeserializationStrategy<T> = serializer.serializersModule.serializer<T>()
  ): Result<T> {
    return try {
      Result.success(serializer.decodeFromJsonElement(deserializer, rawPayload.toJsonElement()))
    } catch (e: Exception) {
      Result.failure(e)
    }
  }
}
