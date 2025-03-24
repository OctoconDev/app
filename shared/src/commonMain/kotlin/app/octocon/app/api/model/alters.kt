package app.octocon.app.api.model

import app.octocon.app.utils.AssumedUTCInstantSerializer
import kotlinx.datetime.Instant
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


interface Alter {
  val id: Int
  val avatarUrl: String?
  val color: String?
  val name: String?
  val pronouns: String?
  val pinned: Boolean
}

@Serializable
data class UnmarkedAlterCustomField(
  val id: String,
  val value: String
)

@Serializable
data class BareAlter(
  override val id: Int,
  @SerialName("avatar_url")
  override val avatarUrl: String? = null,
  override val color: String? = null,
  override val name: String? = null,
  override val pronouns: String? = null,
  override val pinned: Boolean = false
) : Alter

/**
 * Represents an alter with all fields represented (full authentication).
 *
 * @property avatarUrl The alter's avatar URL.
 * @property color The alter's color.
 * @property description The alter's description.
 * @property discordProxies The alter's Discord proxies.
 * @property extraImages The alter's extra images.
 * @property fields The alter's custom fields.
 * @property id The alter's ID (sequential).
 * @property insertedAt The date at which the alter was created.
 * @property name The alter's name.
 * @property pronouns The alter's pronouns.
 * @property securityLevel The alter's security level.
 * @property updatedAt The date at which the alter was last updated.
 * @property userID The system ID to which this alter belongs.
 */
@Serializable
data class MyAlter(
  @SerialName("avatar_url")
  override val avatarUrl: String? = null,
  @EncodeDefault
  override val color: String? = null,
  @EncodeDefault
  val description: String? = null,

  @EncodeDefault
  val untracked: Boolean = false,
  @EncodeDefault
  val archived: Boolean = false,
  @EncodeDefault
  override val pinned: Boolean = false,

  @SerialName("discord_proxies")
  @EncodeDefault
  val discordProxies: List<String>? = null,

  @SerialName("proxy_name")
  @EncodeDefault
  val proxyName: String? = null,

  @SerialName("extra_images")
  @EncodeDefault
  val extraImages: List<String>? = null,
  val fields: List<UnmarkedAlterCustomField>,
  override val id: Int,
  @EncodeDefault
  val alias: String? = null,
  @Serializable(with = AssumedUTCInstantSerializer::class)
  @SerialName("inserted_at")
  val insertedAt: Instant? = null,
  @Serializable(with = AssumedUTCInstantSerializer::class)
  @SerialName("last_fronted")
  val lastFronted: Instant? = null,
  @EncodeDefault
  override val name: String? = null,
  @EncodeDefault
  override val pronouns: String? = null,
  @SerialName("security_level")
  val securityLevel: SecurityLevel,
  @Serializable(with = AssumedUTCInstantSerializer::class)
  @SerialName("updated_at")
  val updatedAt: Instant? = null,
  @SerialName("user_id")
  val userID: String? = null
) : Alter

/**
 * Represents a bare alter.
 *
 * @property id The alter's ID (sequential).
 * @property avatarUrl The alter's avatar URL.
 * @property description The alter's description.
 * @property color The alter's color.
 * @property extraImages The alter's extra images.
 * @property name The alter's name.
 * @property pronouns The alter's pronouns.
 */
@Serializable
data class ExternalAlter(
  override val id: Int,
  @SerialName("avatar_url")
  override val avatarUrl: String? = null,
  @EncodeDefault
  val description: String? = null,
  @EncodeDefault
  override val color: String? = null,
  @SerialName("extra_images")
  @EncodeDefault
  val extraImages: List<String>? = null,
  @EncodeDefault
  override val name: String? = null,
  @EncodeDefault
  override val pronouns: String? = null,
  @SerialName("fields")
  val fields: List<ExternalAlterCustomField>,
  @EncodeDefault
  override val pinned: Boolean = false
) : Alter