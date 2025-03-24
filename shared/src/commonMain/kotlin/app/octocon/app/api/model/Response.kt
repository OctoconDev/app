package app.octocon.app.api.model

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

typealias APIError = String

@Serializable
data class APIResponse<T>(
  val data: T? = null,
  val error: APIError? = null
) {
  val isSuccess: Boolean
    get() = data != null

  val isError: Boolean
    get() = error != null

  val ensureSuccess: T
    get() = data ?: throw IllegalStateException("APIResponse is not successful")

  val ensureError: APIError
    get() = error ?: throw IllegalStateException("APIResponse is not an error")

  companion object {
    fun <T> success(data: T): APIResponse<T> = APIResponse(data = data)
    fun <T> error(error: APIError): APIResponse<T> = APIResponse(error = error)
  }
}

@Serializable
data class SocketInitResponse(
  val system: MySystem,
  val alters: List<MyAlter>,
  val tags: List<MyTag>,
  val fronts: List<MyFrontItem>
)

@Serializable
data class SNAPIResponse(
  val count: Int,
  val next: String?,
  val previous: String?,
  val results: List<SNAPINewsArticle>
)

@Serializable
data class SNAPINewsArticle(
  val id: Int,
  val title: String,
  val url: String,
  @SerialName("image_url")
  val imageUrl: String,
  @SerialName("news_site")
  val newsSite: String,
  val summary: String,
  @SerialName("published_at")
  val publishedAt: Instant,
  @SerialName("updated_at")
  val updatedAt: Instant,
  val featured: Boolean
)