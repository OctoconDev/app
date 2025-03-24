package app.octocon.app.api.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Numbers
import androidx.compose.material.icons.rounded.QuestionMark
import androidx.compose.material.icons.rounded.TextFields
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import app.octocon.app.utils.compose
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import octoconapp.shared.generated.resources.Res
import octoconapp.shared.generated.resources.custom_field_type_boolean
import octoconapp.shared.generated.resources.custom_field_type_number
import octoconapp.shared.generated.resources.custom_field_type_text

interface SystemBase {
  val id: String
  val avatarUrl: String?
  val discordID: String?
  val username: String?
  val description: String?
}

@Serializable
data class MySystem(
  @SerialName("autoproxy_mode")
  val autoproxyMode: String,
  @SerialName("avatar_url")
  override val avatarUrl: String? = null,
  @SerialName("discord_id")
  override val discordID: String? = null,
  val email: String? = null,
  override val id: String,
  override val username: String? = null,
  override val description: String? = null,
  @SerialName("lifetime_alter_count")
  val lifetimeAlterCount: Int,
  @SerialName("primary_front")
  val primaryFront: Int? = null,
  @SerialName("show_system_tag")
  val showSystemTag: Boolean,
  val fields: List<CustomField>,

  @SerialName("encryption_initialized")
  val encryptionInitialized: Boolean
) : SystemBase

@Serializable
data class BareSystem(
  override val id: String,
  @SerialName("avatar_url")
  override val avatarUrl: String? = null,
  @SerialName("discord_id")
  override val discordID: String? = null,
  override val username: String? = null,
  override val description: String? = null
) : SystemBase

@Serializable
enum class CustomFieldType(
  val internalName: String
) {
  @SerialName("text")
  TEXT("text") {
    override val icon: ImageVector by lazy { Icons.Rounded.TextFields }
  },

  @SerialName("number")
  NUMBER("number") {
    override val icon: ImageVector by lazy { Icons.Rounded.Numbers }
  },

  @SerialName("boolean")
  BOOLEAN("boolean") {
    override val icon: ImageVector by lazy { Icons.Rounded.QuestionMark }
  };

  abstract val icon: ImageVector

  val displayName: String
    @Composable get() = when (this) {
      TEXT -> Res.string.custom_field_type_text.compose
      NUMBER -> Res.string.custom_field_type_number.compose
      BOOLEAN -> Res.string.custom_field_type_boolean.compose
    }
}

interface BaseCustomField {
  val id: String
  val name: String
  val type: CustomFieldType
}

@Serializable
data class CustomField(
  override val id: String,
  override val name: String,
  override val type: CustomFieldType,
  @SerialName("security_level")
  val securityLevel: SecurityLevel,
  @EncodeDefault
  val locked: Boolean = false,
) : BaseCustomField

@Serializable
data class ExternalAlterCustomField(
  override val id: String,
  override val name: String,
  val value: String,
  override val type: CustomFieldType
) : BaseCustomField