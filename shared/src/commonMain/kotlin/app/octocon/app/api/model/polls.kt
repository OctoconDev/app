package app.octocon.app.api.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Ballot
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.QuestionMark
import androidx.compose.material.icons.rounded.ThumbDown
import androidx.compose.material.icons.rounded.ThumbUp
import androidx.compose.material.icons.rounded.ThumbsUpDown
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import app.octocon.app.utils.AssumedUTCInstantSerializer
import app.octocon.app.utils.compose
import app.octocon.app.utils.generateUUID
import com.materialkolor.ktx.harmonizeWithPrimary
import kotlinx.datetime.Instant
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import octoconapp.shared.generated.resources.Res
import octoconapp.shared.generated.resources.poll_type_choice
import octoconapp.shared.generated.resources.poll_type_vote
import octoconapp.shared.generated.resources.vote_abstain
import octoconapp.shared.generated.resources.vote_no
import octoconapp.shared.generated.resources.vote_veto
import octoconapp.shared.generated.resources.vote_yes

@Serializable
enum class PollType {
  @SerialName("vote")
  VOTE,

  @SerialName("choice")
  CHOICE;

  val displayName: String
    @Composable get() = when (this) {
      VOTE -> Res.string.poll_type_vote.compose
      CHOICE -> Res.string.poll_type_choice.compose
    }
}


@Serializable
sealed interface PollData

@Serializable
@SerialName("vote")
data class VotePoll(
  override val id: String,
  // override val type: PollType = PollType.VOTE,
  @SerialName("user_id")
  override val userId: String,

  override val title: String,

  override val description: String? = null,

  @Serializable(with = AssumedUTCInstantSerializer::class)
  @SerialName("time_end")
  override val timeEnd: Instant?,

  @Serializable(with = AssumedUTCInstantSerializer::class)
  @SerialName("inserted_at")
  override val insertedAt: Instant,
  @Serializable(with = AssumedUTCInstantSerializer::class)
  @SerialName("updated_at")
  override val updatedAt: Instant,

  override val data: Data
) : Poll {
  @Serializable
  data class Data(
    val responses: List<PollResponse> = emptyList(),
    @SerialName("allow_veto")
    val allowVeto: Boolean = false
  ) : PollData {
    fun addVote(alterID: Int, vote: VoteType, comment: String? = null): Data {
      if (vote == VoteType.VETO && !allowVeto) return this
      return copy(responses = responses + PollResponse(alterID, vote, comment))
    }

    fun editVote(alterID: Int, vote: VoteType, comment: String? = null): Data {
      return copy(responses = responses.map {
        if (it.alterID == alterID) it.copy(vote = vote, comment = comment)
        else it
      })
    }

    fun editVoteComment(alterID: Int, comment: String? = null): Data {
      return copy(responses = responses.map {
        if (it.alterID == alterID) it.copy(comment = comment)
        else it
      })
    }

    fun removeVote(alterID: Int): Data {
      return copy(responses = responses.filter { it.alterID != alterID })
    }

    fun setAllowVeto(allowVeto: Boolean): Data {
      return if (allowVeto) {
        copy(allowVeto = true)
      } else {
        copy(allowVeto = false, responses = responses.filter { it.vote != VoteType.VETO })
      }
    }
  }

  @Serializable
  data class PollResponse(
    @SerialName("alter_id")
    val alterID: Int,
    val vote: VoteType,
    val comment: String? = null
  )

  @Serializable
  enum class VoteType(
    @Transient
    val icon: ImageVector
  ) {
    @SerialName("yes")
    YES(Icons.Rounded.ThumbUp),

    @SerialName("no")
    NO(Icons.Rounded.ThumbDown),

    @SerialName("abstain")
    ABSTAIN(Icons.Rounded.QuestionMark),

    @SerialName("veto")
    VETO(Icons.Rounded.Block);

    @Transient
    val color: Color
      @Composable get() = when (this) {
        YES -> MaterialTheme.colorScheme.harmonizeWithPrimary(Color(0xFF3FDB8D))
        NO -> MaterialTheme.colorScheme.harmonizeWithPrimary(Color(0xFFE75C41))
        ABSTAIN -> MaterialTheme.colorScheme.secondary
        VETO -> MaterialTheme.colorScheme.harmonizeWithPrimary(Color(0xFFC13519))
      }

    @Transient
    val displayName: String
      @Composable get() = when (this) {
        YES -> Res.string.vote_yes.compose
        NO -> Res.string.vote_no.compose
        ABSTAIN -> Res.string.vote_abstain.compose
        VETO -> Res.string.vote_veto.compose
      }
  }

  override val icon: ImageVector
    @Composable get() = Icons.Rounded.ThumbsUpDown
}

@Serializable
@SerialName("choice")
data class ChoicePoll(
  override val id: String,
  // override val type: PollType = PollType.CHOICE,
  @SerialName("user_id")
  override val userId: String,

  override val title: String,
  override val description: String? = null,

  @Serializable(with = AssumedUTCInstantSerializer::class)
  @SerialName("time_end")
  override val timeEnd: Instant?,

  @Serializable(with = AssumedUTCInstantSerializer::class)
  @SerialName("inserted_at")
  override val insertedAt: Instant,
  @Serializable(with = AssumedUTCInstantSerializer::class)
  @SerialName("updated_at")
  override val updatedAt: Instant,

  override val data: Data
) : Poll {
  @Serializable
  data class Data(
    val choices: List<PollChoice> = emptyList(),
    val responses: List<PollResponse> = emptyList()
  ) : PollData {
    fun addChoice(name: String): Data {
      val id = generateUUID()
      return copy(choices = choices + PollChoice(id, name))
    }

    fun editChoice(choiceID: String, name: String): Data {
      return copy(choices = choices.map {
        if (it.id == choiceID) it.copy(name = name)
        else it
      })
    }

    fun removeChoice(choiceID: String): Data {
      return copy(
        choices = choices.filter { it.id != choiceID },
        responses = responses.filter { it.choiceID != choiceID })
    }

    fun addResponse(alterID: Int, choiceID: String, comment: String? = null): Data {
      return copy(responses = responses + PollResponse(alterID, choiceID, comment))
    }

    fun editResponse(alterID: Int, choiceID: String, comment: String? = null): Data {
      return copy(responses = responses.map {
        if (it.alterID == alterID) it.copy(choiceID = choiceID, comment = comment)
        else it
      })
    }

    fun editResponseComment(alterID: Int, comment: String? = null): Data {
      return copy(responses = responses.map {
        if (it.alterID == alterID) it.copy(comment = comment)
        else it
      })
    }

    fun removeResponse(alterID: Int): Data {
      return copy(responses = responses.filter { it.alterID != alterID })
    }
  }

  @Serializable
  data class PollChoice(
    val id: String,
    val name: String
  )

  @Serializable
  data class PollResponse(
    @SerialName("alter_id")
    val alterID: Int,
    @SerialName("choice_id")
    val choiceID: String,
    val comment: String? = null
  )

  override val icon: ImageVector
    @Composable get() = Icons.Rounded.Ballot
}

@Serializable
sealed interface Poll {
  val id: String

  @SerialName("user_id")
  val userId: String

  val title: String

  val description: String?

  @Serializable(with = AssumedUTCInstantSerializer::class)
  @SerialName("time_end")
  val timeEnd: Instant?

  @Polymorphic
  val data: PollData

  @Serializable(with = AssumedUTCInstantSerializer::class)
  @SerialName("inserted_at")
  val insertedAt: Instant

  @Serializable(with = AssumedUTCInstantSerializer::class)
  @SerialName("updated_at")
  val updatedAt: Instant

  @Transient
  val icon: ImageVector
    @Composable get
}