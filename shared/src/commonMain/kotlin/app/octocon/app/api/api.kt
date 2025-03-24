package app.octocon.app.api

import app.octocon.app.api.model.APIResponse
import app.octocon.app.api.model.AlterJournalEntry
import app.octocon.app.api.model.BareSystem
import app.octocon.app.api.model.CustomField
import app.octocon.app.api.model.FriendFrontingAlter
import app.octocon.app.api.model.FriendRequestData
import app.octocon.app.api.model.Friendship
import app.octocon.app.api.model.GlobalJournalEntry
import app.octocon.app.api.model.MyAlter
import app.octocon.app.api.model.MyFrontItem
import app.octocon.app.api.model.MySystem
import app.octocon.app.api.model.MyTag
import app.octocon.app.api.model.Poll
import app.octocon.app.utils.globalSerializer
import app.octocon.app.utils.idRegex
import app.octocon.app.utils.ioDispatcher
import app.octocon.app.utils.platformLog
import app.octocon.kotlix.Channel
import app.octocon.kotlix.Message
import app.octocon.kotlix.Payload
import app.octocon.kotlix.Socket
import app.octocon.kotlix.SocketEvent
import app.octocon.kotlix.SocketFlow
import app.octocon.kotlix.ktor.KtorWebSocketTransport
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.delete
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.serializer
import kotlin.reflect.KType
import kotlin.reflect.typeOf

@Serializable
data class LinkTokenResponse(
  val token: String
)

sealed interface ChannelMessage {
  @Serializable
  data object Ok : ChannelMessage

  @Serializable
  data class Error(val error: String) : ChannelMessage

  @Serializable
  data class AlterCreated(val alter: MyAlter) : ChannelMessage

  @Serializable
  data class AltersCreated(val alters: List<MyAlter>) : ChannelMessage

  @Serializable
  data class AlterUpdated(val alter: MyAlter) : ChannelMessage

  @Serializable
  data class AlterDeleted(@SerialName("alter_id") val alterID: Int) : ChannelMessage


  @Serializable
  data class FrontingStarted(@SerialName("front") val frontItem: MyFrontItem) : ChannelMessage

  @Serializable
  data class FrontingSet(@SerialName("front") val frontItem: MyFrontItem) : ChannelMessage

  @Serializable
  data class FrontUpdated(@SerialName("front") val frontItem: MyFrontItem) : ChannelMessage

  @Serializable
  data class FrontingEnded(@SerialName("alter_id") val alterID: Int) : ChannelMessage

  @Serializable
  data class PrimaryFrontSet(@SerialName("alter_id") val alterID: Int?) : ChannelMessage

  @Serializable
  data class FieldsUpdated(val fields: List<CustomField>) : ChannelMessage

  @Serializable
  data class FrontingBulk(val frontItems: List<MyFrontItem>) : ChannelMessage

  @Serializable
  data class FrontDeleted(@SerialName("front_id") val frontID: String) : ChannelMessage

  @Serializable
  data class TagCreated(val tag: MyTag) : ChannelMessage

  @Serializable
  data class TagUpdated(val tag: MyTag) : ChannelMessage

  @Serializable
  data class TagDeleted(@SerialName("tag_id") val tagID: String) : ChannelMessage

  @Serializable
  data class FriendRemoved(@SerialName("friend_id") val friendID: String) : ChannelMessage

  @Serializable
  data class FriendAdded(
    val friend: BareSystem,
    val friendship: Friendship,
    val fronting: List<FriendFrontingAlter>
  ) : ChannelMessage

  @Serializable
  data class FriendTrusted(@SerialName("friend_id") val friendID: String) : ChannelMessage

  @Serializable
  data class FriendUntrusted(@SerialName("friend_id") val friendID: String) : ChannelMessage

  @Serializable
  data class FriendRequestSent(val request: FriendRequestData, val system: BareSystem) :
    ChannelMessage

  @Serializable
  data class FriendRequestReceived(val request: FriendRequestData, val system: BareSystem) :
    ChannelMessage

  @Serializable
  data class FriendRequestRemoved(@SerialName("system_id") val systemID: String) : ChannelMessage

  @Serializable
  data class GlobalJournalEntryCreated(val entry: GlobalJournalEntry) : ChannelMessage

  @Serializable
  data class GlobalJournalEntryUpdated(val entry: GlobalJournalEntry) : ChannelMessage

  @Serializable
  data class GlobalJournalEntryDeleted(@SerialName("entry_id") val entryID: String) : ChannelMessage

  @Serializable
  data class AlterJournalEntryCreated(val entry: AlterJournalEntry) : ChannelMessage

  @Serializable
  data class AlterJournalEntryUpdated(val entry: AlterJournalEntry) : ChannelMessage

  @Serializable
  data class AlterJournalEntryDeleted(@SerialName("entry_id") val entryID: String) : ChannelMessage

  @Serializable
  data class PollCreated(val poll: Poll) : ChannelMessage

  @Serializable
  data class PollUpdated(val poll: Poll) : ChannelMessage

  @Serializable
  data class PollDeleted(@SerialName("poll_id") val pollID: String) : ChannelMessage

  @Serializable
  data class PKImportComplete(@SerialName("alter_count") val alterCount: Int) : ChannelMessage

  @Serializable
  data class SPImportComplete(@SerialName("alter_count") val alterCount: Int) : ChannelMessage

  @Serializable
  data class DiscordAccountLinked(@SerialName("discord_id") val discordID: String) : ChannelMessage

  @Serializable
  data object DiscordAccountUnlinked : ChannelMessage

  @Serializable
  data class GoogleAccountLinked(val email: String) : ChannelMessage

  @Serializable
  data object GoogleAccountUnlinked : ChannelMessage

  @Serializable
  data class SelfUpdated(val data: MySystem) : ChannelMessage

  @Serializable
  data object EncryptedDataWiped : ChannelMessage

  @Serializable
  data object AccountDeleted : ChannelMessage

  @Serializable
  data object AltersWiped : ChannelMessage

  companion object {
    val associations: Map<String, KType> = mapOf(
      "ok" to typeOf<Ok>(),
      "phx_reply" to typeOf<Ok>(),
      "error" to typeOf<Error>(),

      "alter_created" to typeOf<AlterCreated>(),
      "alters_created" to typeOf<AltersCreated>(),
      "alter_updated" to typeOf<AlterUpdated>(),
      "alter_deleted" to typeOf<AlterDeleted>(),

      "fronting_started" to typeOf<FrontingStarted>(),
      "fronting_set" to typeOf<FrontingSet>(),
      "fronting_ended" to typeOf<FrontingEnded>(),
      "fronting_bulk" to typeOf<FrontingBulk>(),

      "tag_created" to typeOf<TagCreated>(),
      "tag_updated" to typeOf<TagUpdated>(),
      "tag_deleted" to typeOf<TagDeleted>(),

      "primary_front" to typeOf<PrimaryFrontSet>(),

      "front_updated" to typeOf<FrontUpdated>(),
      "front_deleted" to typeOf<FrontDeleted>(),

      "self_updated" to typeOf<SelfUpdated>(),
      "fields_updated" to typeOf<FieldsUpdated>(),

      "friend_removed" to typeOf<FriendRemoved>(),
      "friend_added" to typeOf<FriendAdded>(),
      "friend_trusted" to typeOf<FriendTrusted>(),
      "friend_untrusted" to typeOf<FriendUntrusted>(),

      "friend_request_sent" to typeOf<FriendRequestSent>(),
      "friend_request_received" to typeOf<FriendRequestReceived>(),
      "friend_request_removed" to typeOf<FriendRequestRemoved>(),

      "global_journal_entry_created" to typeOf<GlobalJournalEntryCreated>(),
      "global_journal_entry_updated" to typeOf<GlobalJournalEntryUpdated>(),
      "global_journal_entry_deleted" to typeOf<GlobalJournalEntryDeleted>(),

      "alter_journal_entry_created" to typeOf<AlterJournalEntryCreated>(),
      "alter_journal_entry_updated" to typeOf<AlterJournalEntryUpdated>(),
      "alter_journal_entry_deleted" to typeOf<AlterJournalEntryDeleted>(),

      "poll_created" to typeOf<PollCreated>(),
      "poll_updated" to typeOf<PollUpdated>(),
      "poll_deleted" to typeOf<PollDeleted>(),

      "pk_import_complete" to typeOf<PKImportComplete>(),
      "sp_import_complete" to typeOf<SPImportComplete>(),

      "discord_account_linked" to typeOf<DiscordAccountLinked>(),
      "discord_account_unlinked" to typeOf<DiscordAccountUnlinked>(),

      "google_account_linked" to typeOf<GoogleAccountLinked>(),
      "google_account_unlinked" to typeOf<GoogleAccountUnlinked>(),

      "encrypted_data_wiped" to typeOf<EncryptedDataWiped>(),

      "account_deleted" to typeOf<AccountDeleted>(),
      "alters_wiped" to typeOf<AltersWiped>(),
    )
  }
}

const val endpoint = "https://api.octocon.app/api"

fun parseAmbiguousID(id: String): String = id.trim().let {
  when {
    idRegex.matches(it) -> "id:${it}"
    else -> "username:${it}"
  }
}

expect val client: HttpClient

interface PhoenixSocketSession {
  fun disconnect()
  fun sendMessage(event: String, payload: Map<String, Any?>, callback: suspend (String) -> Unit): Unit
}

internal class KotlixPhoenixSocketSession constructor(
  token: String,
  userID: String,
  eventPipeline: MutableSharedFlow<ChannelMessage>,
  errorPipeline: MutableSharedFlow<String>,
  private val coroutineScope: CoroutineScope,
  onConnected: (String) -> Unit,
) : PhoenixSocketSession {
  private val params = hashMapOf<String, Any?>("token" to token)
  private val paramsClosure: (isReconnect: Boolean) -> Payload = {
    params.apply {
      if (it) put("isReconnect", true)
    }
  }

  private val socketFlow: SocketFlow = MutableSharedFlow(8 * 1024)
  private var socket: Socket = Socket(
    url = "$endpoint/socket/websocket",
    paramsClosure = paramsClosure,
    socketFlow = socketFlow,
    scope = coroutineScope,
    transport = { url, socketFlow, decode ->
      KtorWebSocketTransport(url, socketFlow, decode, client)
    }
  ).apply {
    logger = {
      platformLog("OCTOCON-CHANNEL", it)
    }
  }
  private var socketChannel: Channel? = null

  private var joinChannelJob: Job? = null
  private var collectionJob: Job = coroutineScope.launch {
    socketFlow.collect {
      when (it) {
        is SocketEvent.OpenEvent -> {
          socketChannel?.let { channel -> socket.remove(channel) }
          socketChannel = socket.channel("system:${userID}", params = paramsClosure(it.wasReconnect))

          joinChannelJob?.cancel()
          joinChannelJob = coroutineScope.launch {
            val push = socketChannel?.join()
            if(!it.wasReconnect && push != null) {
              push.collect { msg ->
                onConnected(msg.payloadText!!)
              }
            }
          }
        }
        is SocketEvent.FailureEvent -> {
          // errorPipeline.emit(it.throwable.message ?: "Unknown error")
        }
        is SocketEvent.MessageEvent -> {
          parseChannelMessage(it.text)?.let { msg ->
            eventPipeline.emit(msg)
          }
        }
        is SocketEvent.CloseEvent -> {
          // errorPipeline.emit("Channel closed with code ${it.code}")
        }
      }
    }
  }

  init { socket.connect() }

  override fun disconnect() {
    coroutineScope.launch {
      joinChannelJob?.cancel()
      collectionJob.cancel()
      socket.disconnect()
    }
  }

  override fun sendMessage(
    event: String,
    payload: Map<String, Any?>,
    callback: suspend (String) -> Unit
  ) {
    coroutineScope.launch {
      socketChannel?.push(event, payload)?.collect {
        it.payloadText?.let { text ->
          callback(text)
        }
      }
    }
  }

  private fun parseChannelMessage(message: Message): ChannelMessage? {
    if (message.event in ignoredEvents || message.event.startsWith("chan_reply")) return null

    return try {
      message.deserializePayload(
        globalSerializer,
        globalSerializer.serializersModule.serializer(ChannelMessage.associations[message.event]!!)
      ).getOrThrow() as ChannelMessage
    } catch (e: Exception) {
      ChannelMessage.Error("Error parsing event: $e")
    }
  }
}

internal expect fun connectToPhoenixChannel(
  token: String,
  userID: String,
  eventPipeline: MutableSharedFlow<ChannelMessage>,
  errorPipeline: MutableSharedFlow<String>,
  coroutineScope: CoroutineScope,
  onConnected: (String) -> Unit,
): PhoenixSocketSession

val ignoredEvents = listOf(
  "heartbeat",
  "phx_reply",
  "phx_close",
  "phx_error"
)

/**
 * Parses a channel message from a JSON payload.
 *
 * @param event The event name.
 * @param rawPayload The raw JSON payload.
 * @return The parsed channel message.
 */
internal fun parseChannelMessage(
  event: String,
  rawPayload: String
): ChannelMessage? =
  try {
    if (event.startsWith("chan_reply") || event in ignoredEvents)
      null // Drop
    else globalSerializer.decodeFromString(
      globalSerializer.serializersModule.serializer(ChannelMessage.associations[event]!!),
      rawPayload
    ) as ChannelMessage
  } catch (e: Exception) {
    // println(rawPayload)
    ChannelMessage.Error("Error parsing event: $e")
  }

val httpBuilder: (token: String, body: Any) -> (HttpRequestBuilder.() -> Unit) = { token, body ->
  {
    headers {
      header("Authorization", "Bearer $token")
      header("Content-Type", "application/json")
    }
    setBody(body)
  }
}

private suspend fun get(token: String, path: String, body: Any = "") =
  withContext(ioDispatcher) {
    client.get("$endpoint/$path", httpBuilder(token, body))
  }

private suspend fun post(token: String, path: String, body: Any = "") =
  withContext(ioDispatcher) {
    client.post("$endpoint/$path", httpBuilder(token, body))
  }

/**
 * Sends a PUT request to the Octocon API.
 *
 * @param token The user's access token.
 * @param path The endpoint to call.
 * @param body The body of the request.
 * @return The response from the API.
 */
private suspend fun put(token: String, path: String, body: Any = "") =
  withContext(ioDispatcher) {
    client.put("$endpoint/$path", httpBuilder(token, body))
  }

/**
 * Sends a DELETE request to the Octocon API.
 *
 * @param token The user's access token.
 * @param path The endpoint to call.
 * @param body The body of the request.
 * @return The response from the API.
 */
private suspend fun delete(token: String, path: String, body: Any = "") =
  withContext(ioDispatcher) {
    client.delete("$endpoint/$path", httpBuilder(token, body))
  }

suspend fun setAlterAvatar(token: String, alterID: Int, bytes: ByteArray, fileName: String) =
  put(
    token, "systems/me/alters/$alterID/avatar", MultiPartFormDataContent(
      formData {
        append("file", bytes, Headers.build {
          append(HttpHeaders.ContentType, "image/${fileName.substringAfterLast(".")}")
          append(HttpHeaders.ContentDisposition, "filename=\"${fileName}\"")
        })
      },
      boundary = "OctoconBoundary"
    )
  )

suspend fun setSystemAvatar(token: String, bytes: ByteArray, fileName: String) =
  put(
    token, "settings/avatar", MultiPartFormDataContent(
      formData {
        append("file", bytes, Headers.build {
          append(HttpHeaders.ContentType, "image/${fileName.substringAfterLast(".")}")
          append(HttpHeaders.ContentDisposition, "filename=\"${fileName}\"")
        })
      },
      boundary = "OctoconBoundary"
    )
  )

suspend fun getFrontingAlters(token: String) = get(token, "systems/me/fronting").body<APIResponse<List<MyFrontItem>>>()

@Serializable
data class KeyResponse(
  val key: String
)

/*suspend fun getFrontsPastMonthWithEndAnchor(token: String, unixTime: Long) =
  get(token, "systems/me/front/month", buildJsonObject {
    put("end_anchor", unixTime)
  })

suspend fun getFriend(token: String, friendID: String): APIResponse<FriendshipContainer> =
  get(token, "friends/${parseAmbiguousID(friendID)}")
    .body<APIResponse<FriendshipContainer>>()

suspend fun getFriendAlter(
  token: String,
  friendID: String,
  alterID: Int
): APIResponse<ExternalAlter> =
  get(token, "systems/${parseAmbiguousID(friendID)}/alters/${alterID}")
    .body<APIResponse<ExternalAlter>>()

suspend fun getFriendAlters(token: String, friendID: String): APIResponse<List<ExternalAlter>> =
  get(token, "systems/${parseAmbiguousID(friendID)}/alters")
    .body<APIResponse<List<ExternalAlter>>>()*/

suspend fun updatePushNotificationToken(token: String, pushToken: String?): HttpResponse? {
  if (pushToken == null) return null
  return post(token, "settings/push-token", buildJsonObject {
    put("token", pushToken)
  }.toString())
}

suspend fun invalidatePushNotificationToken(token: String, pushToken: String?): HttpResponse? {
  if (pushToken == null) return null
  return delete(token, "settings/push-token", buildJsonObject {
    put("token", pushToken)
  }.toString())
}