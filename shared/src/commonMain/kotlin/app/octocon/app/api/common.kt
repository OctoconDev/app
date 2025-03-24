package app.octocon.app.api

import app.octocon.app.api.model.APIError
import app.octocon.app.api.model.APIResponse
import io.ktor.http.HttpMethod
import kotlinx.serialization.Serializable

fun <T> APIResponse<T>.toState(): APIState<T> {
  return if (this.data != null) {
    APIState.Success(this.data)
  } else {
    APIState.Error(this.error!!)
  }
}

@Serializable
sealed interface APIState<T> {
  @Serializable
  data class Success<T>(val data: T) : APIState<T>
  @Serializable
  class Loading<T> : APIState<T>
  @Serializable
  data class Error<T>(val error: APIError) : APIState<T>

  val ensureData: T
    get() = (this as Success).data

  val ensureError: APIError
    get() = (this as Error).error

  val isSuccess: Boolean
    get() = this is Success

  val isLoading: Boolean
    get() = this is Loading

  val isError: Boolean
    get() = this is Error

  fun ifError(block: (APIError) -> T): T =
    if (this is Error) {
      block(error)
    } else this.ensureData
}

@Serializable
data class SocketAdapterResponse(
  val status: Int,
  val body: String
)

fun buildEndpointPayload(method: HttpMethod, path: String): Map<String, Any?> =
  buildEndpointPayload(method, path, "")

fun buildEndpointPayload(
  method: HttpMethod,
  path: String,
  body: String
): Map<String, String> =
  mapOf(
    "method" to method.value,
    "path" to "/api/$path",
    "body" to body
  )