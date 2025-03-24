package app.octocon.app.api

import app.octocon.app.api.model.FriendRequest
import kotlinx.serialization.Serializable

@Serializable
data class FriendRequests(
  val incoming: List<FriendRequest.Incoming> = listOf(),
  val outgoing: List<FriendRequest.Outgoing> = listOf()
) {
  fun copyWithAppendIncoming(incoming: FriendRequest.Incoming) =
    this.copy(
      incoming = this.incoming.plus(incoming)
    )

  fun copyWithAppendOutgoing(outgoing: FriendRequest.Outgoing) =
    this.copy(
      outgoing = this.outgoing.plus(outgoing)
    )

  fun combine(): List<FriendRequest> =
    incoming.sortedBy { it.request.dateSent } +
        outgoing.sortedBy { it.request.dateSent }
}