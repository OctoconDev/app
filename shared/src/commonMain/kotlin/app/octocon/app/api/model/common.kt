package app.octocon.app.api.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockPerson
import androidx.compose.material.icons.rounded.PeopleAlt
import androidx.compose.material.icons.rounded.Public
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class SecurityLevel(
  val displayName: String,
  val internalName: String,
  val icon: ImageVector,
  // val description: (noun: String) -> String
) {
  @SerialName("public")
  PUBLIC(
    "Public",
    "public",
    Icons.Rounded.Public,
    // { noun -> "Anyone can see this $noun." }
  ),

  @SerialName("friends_only")
  FRIENDS_ONLY(
    "Friends only",
    "friends_only",
    Icons.Rounded.PeopleAlt,
    // { noun -> "Only friends can see this $noun." }
  ),

  @SerialName("trusted_only")
  TRUSTED_ONLY(
    "Trusted only",
    "trusted_only",
    Icons.Rounded.LockPerson,
    // { noun -> "Only trusted friends can see this $noun." }
  ),

  @SerialName("private")
  PRIVATE(
    "Private",
    "private",
    Icons.Rounded.Lock,
    // { noun -> "Only you can see this $noun." }
  );
}