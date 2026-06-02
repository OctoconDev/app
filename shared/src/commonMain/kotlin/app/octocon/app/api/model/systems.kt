package app.octocon.app.api.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Numbers
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.QuestionMark
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Subject
import androidx.compose.material.icons.rounded.Title
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import app.octocon.app.utils.compose
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import octoconapp.shared.resources.Res
import octoconapp.shared.resources.custom_field_type_boolean
import octoconapp.shared.resources.custom_field_type_colour
import octoconapp.shared.resources.custom_field_type_date
import octoconapp.shared.resources.custom_field_type_month
import octoconapp.shared.resources.custom_field_type_month_day
import octoconapp.shared.resources.custom_field_type_month_year
import octoconapp.shared.resources.custom_field_type_number
import octoconapp.shared.resources.custom_field_type_plaintext
import octoconapp.shared.resources.custom_field_type_text
import octoconapp.shared.resources.custom_field_type_timestamp
import octoconapp.shared.resources.custom_field_type_year

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
  @SerialName("google_id")
  val googleID: String? = null,
  @SerialName("apple_id")
  val appleID: String? = null,

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
    override val icon: ImageVector by lazy { Icons.Rounded.Title }
  },

  @SerialName("number")
  NUMBER("number") {
    override val icon: ImageVector by lazy { Icons.Rounded.Numbers }
  },

  @SerialName("boolean")
  BOOLEAN("boolean") {
    override val icon: ImageVector by lazy { Icons.Rounded.QuestionMark }
  },

  @SerialName("colour")
  COLOUR("colour") {
    override val icon: ImageVector by lazy { Icons.Rounded.Palette }
  },

  @SerialName("date")
  DATE("date") {
    override val icon: ImageVector by lazy { Icons.Rounded.CalendarToday }
  },

  @SerialName("plaintext")
  PLAINTEXT("plaintext") {
    override val icon: ImageVector by lazy { Icons.Rounded.Subject }
  },

  @SerialName("month")
  MONTH("month") {
    override val icon: ImageVector by lazy { Icons.Rounded.CalendarMonth }
  },

  @SerialName("year")
  YEAR("year") {
    override val icon: ImageVector by lazy { Icons.Rounded.CalendarMonth }
  },

  @SerialName("month_year")
  MONTH_YEAR("month_year") {
    override val icon: ImageVector by lazy { Icons.Rounded.CalendarMonth }
  },

  @SerialName("timestamp")
  TIMESTAMP("timestamp") {
    override val icon: ImageVector by lazy { Icons.Rounded.Schedule }
  },

  @SerialName("month_day")
  MONTH_DAY("month_day") {
    override val icon: ImageVector by lazy { Icons.Rounded.CalendarMonth }
  };

  abstract val icon: ImageVector

  val displayName: String
    @Composable get() = when (this) {
      TEXT -> Res.string.custom_field_type_text.compose
      NUMBER -> Res.string.custom_field_type_number.compose
      BOOLEAN -> Res.string.custom_field_type_boolean.compose
      COLOUR -> Res.string.custom_field_type_colour.compose
      DATE -> Res.string.custom_field_type_date.compose
      PLAINTEXT -> Res.string.custom_field_type_plaintext.compose
      MONTH -> Res.string.custom_field_type_month.compose
      YEAR -> Res.string.custom_field_type_year.compose
      MONTH_YEAR -> Res.string.custom_field_type_month_year.compose
      TIMESTAMP -> Res.string.custom_field_type_timestamp.compose
      MONTH_DAY -> Res.string.custom_field_type_month_day.compose
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