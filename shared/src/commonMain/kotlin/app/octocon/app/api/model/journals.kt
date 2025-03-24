package app.octocon.app.api.model

import kotlinx.datetime.Instant
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import app.octocon.app.utils.AssumedUTCInstantSerializer

sealed interface BaseJournalEntry {
  val id: String
  val title: String
  val content: String?
  val color: String?
  val locked: Boolean
  val pinned: Boolean
  val insertedAt: Instant
  val updatedAt: Instant
  val userId: String
}

@Serializable
data class GlobalJournalEntry(
  override val id: String,
  override val title: String,
  @EncodeDefault
  override val content: String? = null,
  @EncodeDefault
  override val color: String? = null,
  override val locked: Boolean,
  override val pinned: Boolean,
  @Serializable(with = AssumedUTCInstantSerializer::class)
  @SerialName("inserted_at")
  override val insertedAt: Instant,
  @Serializable(with = AssumedUTCInstantSerializer::class)
  @SerialName("updated_at")
  override val updatedAt: Instant,
  @SerialName("user_id")
  override val userId: String,
  val alters: List<Int>
) : BaseJournalEntry

@Serializable
data class AlterJournalEntry(
  override val id: String,
  override val title: String,
  @EncodeDefault
  override val content: String? = null,
  @EncodeDefault
  override val color: String? = null,
  override val locked: Boolean,
  override val pinned: Boolean,
  @Serializable(with = AssumedUTCInstantSerializer::class)
  @SerialName("inserted_at")
  override val insertedAt: Instant,
  @Serializable(with = AssumedUTCInstantSerializer::class)
  @SerialName("updated_at")
  override val updatedAt: Instant,
  @SerialName("user_id")
  override val userId: String,
  @SerialName("alter_id")
  val alterID: Int
) : BaseJournalEntry