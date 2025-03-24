package app.octocon.app.api.model

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MyFrontItem(
  val alter: BareAlter,
  val front: MyFront,
  val primary: Boolean
)

@Serializable
data class MyFront(
  val id: String,
  @SerialName("alter_id")
  val alterID: Int,
  val comment: String? = null,
  @SerialName("time_start")
  val timeStart: Instant,
  @SerialName("time_end")
  val timeEnd: Instant? = null,
  @SerialName("user_id")
  val userID: String,
)

@Serializable
data class BareFront(
  @SerialName("alter_id")
  val alterID: Int,
  val comment: String? = null,
)