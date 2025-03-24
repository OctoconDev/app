package app.octocon.app.api.model

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class FriendshipLevel {
  @SerialName("friend")
  Friend,

  @SerialName("trusted_friend")
  TrustedFriend
}

@Serializable
data class FriendRequestData(
  @SerialName("date_sent")
  val dateSent: Instant
)

@Serializable
sealed interface FriendRequest {
  val request: FriendRequestData
  val system: BareSystem

  @Serializable
  data class Incoming(
    override val request: FriendRequestData,
    override val system: BareSystem
  ) : FriendRequest

  @Serializable
  class Outgoing(
    override val request: FriendRequestData,
    override val system: BareSystem
  ) : FriendRequest
}

@Serializable
data class Friendship(
  val level: FriendshipLevel,
  val since: Instant
)

@Serializable
data class FriendFrontingAlter(
  val alter: ExternalAlter,
  val front: BareFront,
  val primary: Boolean
)

@Serializable
data class FriendshipContainer(
  val friend: BareSystem,
  val friendship: Friendship,
  val fronting: List<FriendFrontingAlter>
) : Comparable<FriendshipContainer> {
  override fun compareTo(other: FriendshipContainer): Int {
    return when (friendship.level) {
      other.friendship.level -> friendship.since.compareTo(other.friendship.since)
      FriendshipLevel.TrustedFriend -> -1
      else -> friendship.since.compareTo(other.friendship.since)
    }
  }
}

@Serializable
data class CollatedFriendDataRaw(
  val friendship: FriendshipContainer,
  val tags: List<ExternalTag>,
  val alters: List<ExternalAlter>
)

data class CollatedFriendData(
  val friendship: FriendshipContainer,
  val tags: List<ExternalTag>,
  val tagMap: Map<String, Int>,
  val alters: List<ExternalAlter>
)