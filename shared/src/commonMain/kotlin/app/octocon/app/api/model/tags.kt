package app.octocon.app.api.model

import kotlinx.datetime.Instant
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import app.octocon.app.utils.AssumedUTCInstantSerializer

sealed interface BaseTag {
  val id: String
  val name: String
  val color: String?
  val description: String?
  val userId: String

  val alters: List<Any>?
  val parentTagID: String?
}

@Serializable
data class MyTag(
  override val id: String,
  @EncodeDefault
  override val name: String,
  @EncodeDefault
  override val color: String? = null,
  @EncodeDefault
  override val description: String? = null,
  @SerialName("user_id")
  override val userId: String,

  override val alters: List<Int>,

  @SerialName("security_level")
  val securityLevel: SecurityLevel,

  @SerialName("parent_tag_id")
  override val parentTagID: String? = null,

  @Serializable(with = AssumedUTCInstantSerializer::class)
  @SerialName("inserted_at")
  val insertedAt: Instant,
  @Serializable(with = AssumedUTCInstantSerializer::class)
  @SerialName("updated_at")
  val updatedAt: Instant
) : BaseTag

@Serializable
data class ExternalTag(
  override val id: String,
  @EncodeDefault
  override val name: String,
  @EncodeDefault
  override val color: String? = null,
  @EncodeDefault
  override val description: String? = null,
  @SerialName("user_id")
  override val userId: String,

  override val alters: List<BareAlter>? = null,
  @SerialName("parent_tag_id")
  override val parentTagID: String? = null,
) : BaseTag