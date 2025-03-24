package app.octocon.app.ui.model.interfaces

import app.octocon.app.api.APIState
import app.octocon.app.api.ChannelMessage
import app.octocon.app.api.FriendRequests
import app.octocon.app.api.KeyResponse
import app.octocon.app.api.LinkTokenResponse
import app.octocon.app.api.PhoenixSocketSession
import app.octocon.app.api.SocketAdapterResponse
import app.octocon.app.api.buildEndpointPayload
import app.octocon.app.api.clusterFrontData
import app.octocon.app.api.connectToPhoenixChannel
import app.octocon.app.api.model.APIError
import app.octocon.app.api.model.APIResponse
import app.octocon.app.api.model.AlterJournalEntry
import app.octocon.app.api.model.CollatedFriendData
import app.octocon.app.api.model.CollatedFriendDataRaw
import app.octocon.app.api.model.CustomField
import app.octocon.app.api.model.CustomFieldType
import app.octocon.app.api.model.FriendRequest
import app.octocon.app.api.model.FriendshipContainer
import app.octocon.app.api.model.FriendshipLevel
import app.octocon.app.api.model.GlobalJournalEntry
import app.octocon.app.api.model.MyAlter
import app.octocon.app.api.model.MyFront
import app.octocon.app.api.model.MyFrontItem
import app.octocon.app.api.model.MySystem
import app.octocon.app.api.model.MyTag
import app.octocon.app.api.model.Poll
import app.octocon.app.api.model.PollType
import app.octocon.app.api.model.SocketInitResponse
import app.octocon.app.api.parseAmbiguousID
import app.octocon.app.api.toState
import app.octocon.app.ui.compose.screens.main.hometabs.FrontHistoryItem
import app.octocon.app.utils.MonthYearPair
import app.octocon.app.utils.PlatformUtilities
import app.octocon.app.utils.globalSerializer
import app.octocon.app.utils.ioDispatcher
import app.octocon.app.utils.monthDays
import app.octocon.app.utils.platformLog
import app.octocon.app.utils.sortedLocaleAware
import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpMethod
import io.ktor.http.HttpMethod.Companion.Delete
import io.ktor.http.HttpMethod.Companion.Get
import io.ktor.http.HttpMethod.Companion.Post
import io.ktor.http.HttpMethod.Companion.Put
import io.ktor.http.isSuccess
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonPrimitive
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

interface ApiInterface {
  val token: StateFlow<String>
  var firebaseMessagingToken: String?
  val initComplete: StateFlow<Boolean>
  val eventFlow: Flow<ChannelMessage>
  val errorFlow: Flow<APIError>
  val systemMe: StateFlow<APIState<MySystem>>
  val alters: StateFlow<APIState<List<MyAlter>>>
  val loadedAlters: StateFlow<Map<Int, APIState<MyAlter>>>
  val tags: StateFlow<APIState<List<MyTag>>>
  val globalJournals: StateFlow<APIState<List<GlobalJournalEntry>>>
  val alterJournals: StateFlow<Map<Int, List<AlterJournalEntry>>>
  val polls: StateFlow<APIState<List<Poll>>>
  val fronts: StateFlow<APIState<List<MyFrontItem>>>
  val frontHistory: StateFlow<Map<MonthYearPair, APIState<List<Pair<Triple<Int, Month, Int>, MutableList<FrontHistoryItem>>>>>>
  val friends: StateFlow<APIState<List<FriendshipContainer>>>
  val friendDataMap: StateFlow<Map<String, APIState<CollatedFriendData>>>
  val friendRequests: StateFlow<APIState<FriendRequests>>
  val encryptionIsInitializing: StateFlow<Boolean>

  fun loadClient(initialToken: String, settingsInterface: SettingsInterface): Job
  fun logOut(soft: Boolean = true)

  /* --------------- ENCRYPTION --------------- */
  suspend fun generateRecoveryCode(): Pair<String, String>
  fun setupEncryption(recoveryCodeJWE: String, settingsInterface: SettingsInterface)
  fun resetEncryption()
  fun recoverEncryption(recoveryCode: String, settingsInterface: SettingsInterface, onFailure: (error: String) -> Unit)
  /* --------------- RESOURCES --------------- */
  fun reloadAlters(pushLoadingState: Boolean = true): Job
  fun reloadFronts(pushLoadingState: Boolean = true): Job
  fun loadFrontHistory(
    monthYearPair: MonthYearPair,
    force: Boolean = false,
    pushLoadingState: Boolean = true,
    successCallback: (() -> Unit)? = null
  )
  fun reloadFriends(pushLoadingState: Boolean = true): Job
  fun reloadFriendRequests(pushLoadingState: Boolean = true): Job
  fun reloadGlobalJournals(pushLoadingState: Boolean = true): Job
  fun loadAlterJournals(alterID: Int)
  fun reloadPolls(pushLoadingState: Boolean = true): Job

  /* --------------- API --------------- */

  /* --------------- ALTERS --------------- */
  fun createAlter(name: String)
  fun deleteAlter(alterID: Int)
  fun loadAlter(alterID: Int)
  fun setAlterPinned(alterID: Int, pinned: Boolean)
  fun setAlterAvatar(alterID: Int, bytes: ByteArray, fileName: String): Any
  fun removeAlterAvatar(alterID: Int)

  /* --------------- FRIENDS --------------- */
  fun sendFriendRequest(friendID: String)
  fun cancelFriendRequest(friendID: String)
  fun acceptFriendRequest(friendID: String)
  fun rejectFriendRequest(friendID: String)
  fun removeFriend(friendID: String)
  fun loadFriend(friendID: String)
  fun trustFriend(friendID: String)
  fun untrustFriend(friendID: String)

  /* --------------- FRONTS --------------- */
  fun endFront(alterID: Int)
  fun startFront(alterID: Int)
  fun setFront(alterID: Int)
  fun setPrimaryFront(alterID: Int?)
  fun editFrontComment(frontID: String, comment: String)
  fun deleteFront(frontID: String)

  /* --------------- GLOBAL JOURNALS --------------- */
  fun createGlobalJournalEntry(title: String)
  fun deleteGlobalJournalEntry(entryID: String)
  fun lockGlobalJournalEntry(entryID: String)
  fun unlockGlobalJournalEntry(entryID: String)
  fun pinGlobalJournalEntry(entryID: String)
  fun unpinGlobalJournalEntry(entryID: String)

  /* --------------- ALTER JOURNALS --------------- */
  fun createAlterJournalEntry(alterID: Int, title: String)
  fun deleteAlterJournalEntry(entryID: String)
  fun attachAlterToGlobalJournalEntry(entryID: String, alterID: Int)
  fun detachAlterFromGlobalJournalEntry(entryID: String, alterID: Int)
  fun lockAlterJournalEntry(entryID: String)
  fun unlockAlterJournalEntry(entryID: String)
  fun pinAlterJournalEntry(entryID: String)
  fun unpinAlterJournalEntry(entryID: String)

  /* --------------- TAGS --------------- */
  fun createTag(name: String)
  fun createTag(name: String, parentTagID: String)
  fun deleteTag(tagID: String)
  fun attachAlterToTag(tagID: String, alterID: Int)
  fun detachAlterFromTag(tagID: String, alterID: Int)
  fun setParentTagID(tagID: String, parentTagID: String)
  fun removeParentTagID(tagID: String)

  /* --------------- POLLS --------------- */
  fun createPoll(title: String, type: PollType, timeEnd: Instant?)
  fun deletePoll(pollID: String)

  /* --------------- CUSTOM FIELDS --------------- */
  fun createCustomField(name: String, type: CustomFieldType)
  fun deleteCustomField(id: String)
  fun editCustomField(id: String, field: CustomField)
  fun relocateCustomField(id: String, index: Int)

  /* --------------- SETTINGS --------------- */
  fun provideFirebaseMessagingToken(token: String)
  fun updatePushNotificationToken()
  fun invalidatePushNotificationToken()

  fun tryLinkDiscord(openUri: (String) -> Unit)
  fun tryUnlinkDiscord()
  fun tryLinkGoogle(openUri: (String) -> Unit)
  fun tryUnlinkEmail()

  fun updateUsername(username: String)
  fun updateDescription(description: String?)
  fun setSystemAvatar(bytes: ByteArray, fileName: String)
  fun removeSystemAvatar()

  fun importSP(spToken: String)
  fun importPK(pkToken: String)

  fun deleteAccount()
  fun wipeAlters()
}

internal class ApiInterfaceImpl(
  val coroutineScope: CoroutineScope,
  val platformUtilities: PlatformUtilities
) : ApiInterface, InstanceKeeper.Instance {
  /* --------------- STATE --------------- */

  override val token = MutableStateFlow("")

  override var firebaseMessagingToken: String? = null

  var socketSession: PhoenixSocketSession? = null

  private val _initComplete = MutableStateFlow(false)
  override val initComplete: StateFlow<Boolean> = _initComplete

  private val _eventFlow: MutableSharedFlow<ChannelMessage> = MutableSharedFlow()
  override val eventFlow: Flow<ChannelMessage> = _eventFlow

  @Suppress("PropertyName")
  val _errorFlow: MutableSharedFlow<APIError> = MutableSharedFlow()
  override val errorFlow: Flow<APIError> = _errorFlow

  private val _systemMe = MutableStateFlow<APIState<MySystem>>(APIState.Loading())
  override val systemMe: StateFlow<APIState<MySystem>> = _systemMe

  private val _alters = MutableStateFlow<APIState<List<MyAlter>>>(APIState.Loading())
  override val alters: StateFlow<APIState<List<MyAlter>>> = _alters

  private val _loadedAlters = MutableStateFlow<Map<Int, APIState<MyAlter>>>(hashMapOf())
  override val loadedAlters: StateFlow<Map<Int, APIState<MyAlter>>> = _loadedAlters

  private val _tags = MutableStateFlow<APIState<List<MyTag>>>(APIState.Loading())
  override val tags: StateFlow<APIState<List<MyTag>>> = _tags

  private val _globalJournals =
    MutableStateFlow<APIState<List<GlobalJournalEntry>>>(APIState.Loading())
  override val globalJournals: StateFlow<APIState<List<GlobalJournalEntry>>> = _globalJournals

  private val _alterJournals =
    MutableStateFlow<Map<Int, List<AlterJournalEntry>>>(hashMapOf())
  override val alterJournals: StateFlow<Map<Int, List<AlterJournalEntry>>> = _alterJournals

  private val _polls = MutableStateFlow<APIState<List<Poll>>>(APIState.Loading())
  override val polls: StateFlow<APIState<List<Poll>>> = _polls

  private val _fronts = MutableStateFlow<APIState<List<MyFrontItem>>>(APIState.Loading())
  override val fronts: StateFlow<APIState<List<MyFrontItem>>> = _fronts

  private val _frontHistory =
    MutableStateFlow<
        Map<MonthYearPair,
            APIState<List<Pair<Triple<Int, Month, Int>, MutableList<FrontHistoryItem>>>>
            >
        >(hashMapOf())

  override val frontHistory: StateFlow<
      Map<MonthYearPair,
          APIState<List<Pair<Triple<Int, Month, Int>, MutableList<FrontHistoryItem>>>>
          >
      > = _frontHistory

  private val _friends = MutableStateFlow<APIState<List<FriendshipContainer>>>(APIState.Loading())
  override val friends: StateFlow<APIState<List<FriendshipContainer>>> = _friends

  private val _friendDataMap =
    MutableStateFlow<Map<String, APIState<CollatedFriendData>>>(hashMapOf())
  override val friendDataMap: StateFlow<Map<String, APIState<CollatedFriendData>>> = _friendDataMap

  private val _friendRequests = MutableStateFlow<APIState<FriendRequests>>(APIState.Loading())
  override val friendRequests: StateFlow<APIState<FriendRequests>> = _friendRequests

  private val _encryptionIsInitializing: MutableStateFlow<Boolean> = MutableStateFlow(false)
  override val encryptionIsInitializing: StateFlow<Boolean> = _encryptionIsInitializing

  @OptIn(ExperimentalEncodingApi::class)
  override fun loadClient(initialToken: String, settingsInterface: SettingsInterface) =
    coroutineScope.launch {
      platformLog("Loading client")
      val parts = initialToken.split(".")
      val payload = globalSerializer.decodeFromString<JsonObject>(
        Base64.UrlSafe.withPadding(Base64.PaddingOption.PRESENT_OPTIONAL).decode(parts[1])
          .decodeToString()
      )
      val userID = payload["sub"]!!.jsonPrimitive.content

      token.value = initialToken
      try {
        withContext(ioDispatcher) {
          if (!_initComplete.value) {
            socketSession =
              connectToPhoenixChannel(
                token.value,
                userID,
                _eventFlow,
                _errorFlow,
                coroutineScope
              ) { json ->
                if (!_initComplete.value) {
                  val response = globalSerializer.decodeFromString<SocketInitResponse>(json)
                  _systemMe.tryEmit(APIState.Success(response.system))
                  _alters.tryEmit(APIState.Success(response.alters))
                  _tags.tryEmit(APIState.Success(response.tags.sortedLocaleAware { it.name }))
                  _fronts.tryEmit(APIState.Success(response.fronts))

                  if(settingsInterface.data.value.isSinglet) {
                    reloadFriends(false)
                    reloadFriendRequests(false)
                  }

                  _initComplete.tryEmit(true)
                }
              }

            eventFlow.onEach { message ->
              handleChannelMessage(message, settingsInterface)
            }.launchIn(coroutineScope)
          }
        }
      } catch (_: Exception) {
      }
    }

  override fun logOut(soft: Boolean) {
    if (soft) invalidatePushNotificationToken()
    _initComplete.tryEmit(false)
    token.value = ""
    socketSession?.disconnect()
  }

  override suspend fun generateRecoveryCode(): Pair<String, String> {
    return platformUtilities.generateRecoveryCode()
  }

  override fun setupEncryption(recoveryCodeJWE: String, settingsInterface: SettingsInterface) {
    coroutineScope.launch(Dispatchers.Default) {
      // val publicKeyPEM = platformUtilities.getPublicKey()

      sendAPIRequest<KeyResponse>(
        Post,
        "settings/setup-encryption",
        buildRequestBody(
          mapOf(
            "recovery_code" to globalSerializer.encodeToJsonElement(recoveryCodeJWE)
            // "public_key" to globalSerializer.encodeToJsonElement(publicKeyPEM),
          )
        )
      ) { isSuccess, response ->
        coroutineScope.launch {
          withContext(Dispatchers.Default) {
            if (isSuccess) {
              _encryptionIsInitializing.tryEmit(true)
              val encryptedKey = response.data!!.key
              val decryptedKey = platformUtilities.decryptEncryptionKey(encryptedKey)

              platformUtilities.setupEncryptionKey(decryptedKey)?.let {
                settingsInterface.pushSettings(it)
              }
              _encryptionIsInitializing.tryEmit(false)
            }
          }
        }
      }
    }
  }

  override fun resetEncryption() =
    sendAPIRequest(
      Post,
      "settings/reset-encryption"
    )

  override fun recoverEncryption(
    recoveryCode: String,
    settingsInterface: SettingsInterface,
    onFailure: (error: String) -> Unit
  ) {
    coroutineScope.launch(Dispatchers.Default) {
      sendAPIRequest<KeyResponse>(
        Post,
        "settings/recover-encryption",
        buildRequestBody(
          mapOf(
            "recovery_code" to globalSerializer.encodeToJsonElement(
              platformUtilities.recoveryCodeToJWE(
                recoveryCode.chunked(4).joinToString("-")
              )
            )
          )
        )
      ) { isSuccess, response ->
        coroutineScope.launch {
          withContext(Dispatchers.Default) {
            if (isSuccess) {
              _encryptionIsInitializing.tryEmit(true)
              val encryptedKey = response.data!!.key
              val decryptedKey = platformUtilities.decryptEncryptionKey(encryptedKey)

              platformUtilities.setupEncryptionKey(decryptedKey)?.let {
                settingsInterface.pushSettings(it)
              }
              _encryptionIsInitializing.tryEmit(false)
            } else {
              onFailure(response.error!!)
            }
          }
        }
      }
    }
  }

  private fun handleChannelMessage(message: ChannelMessage, settingsInterface: SettingsInterface) {
    when (message) {
      is ChannelMessage.AlterDeleted -> {
        if (_alters.value !is APIState.Success) return
        _alters.tryEmit(APIState.Success(_alters.value.ensureData.filter { it.id != message.alterID }))
        if (_loadedAlters.value.containsKey(message.alterID)) {
          _loadedAlters.tryEmit(
            _loadedAlters.value.toMutableMap().apply {
              this.remove(message.alterID)
            }
          )
        }
        // We need to invalidate the front history so there isn't a stale reference
        if (_frontHistory.value.isEmpty()) {
          clearAllFrontHistory()
        }
      }

      is ChannelMessage.AlterCreated -> {
        if (_alters.value !is APIState.Success) return
        _alters.tryEmit(APIState.Success(_alters.value.ensureData.plus(message.alter)))
        _loadedAlters.tryEmit(
          _loadedAlters.value.toMutableMap().apply {
            this[message.alter.id] = APIState.Success(message.alter)
          }
        )
      }

      is ChannelMessage.AltersCreated -> {
        if (_alters.value !is APIState.Success) return
        _alters.tryEmit(APIState.Success(_alters.value.ensureData.plus(message.alters)))
      }

      is ChannelMessage.AlterUpdated -> {
        if (_alters.value !is APIState.Success) return
        _alters.tryEmit(APIState.Success(_alters.value.ensureData.map {
          if (it.id == message.alter.id) {
            message.alter
          } else {
            it
          }
        }))
        if (_loadedAlters.value.containsKey(message.alter.id)) {
          _loadedAlters.tryEmit(
            _loadedAlters.value.toMutableMap().apply {
              this[message.alter.id] = APIState.Success(message.alter)
            }
          )
        }
      }

      is ChannelMessage.FrontingBulk -> TODO()
      is ChannelMessage.FrontingEnded -> {
        if (_fronts.value !is APIState.Success) return
        _fronts.tryEmit(APIState.Success(_fronts.value.ensureData.filter { it.front.alterID != message.alterID }))
        clearAllFrontHistory()
      }

      is ChannelMessage.FrontingStarted -> {
        if (_fronts.value !is APIState.Success) return
        _fronts.tryEmit(
          APIState.Success(
            _fronts.value.ensureData.plus(
              message.frontItem
            )
          )
        )
      }

      is ChannelMessage.FrontUpdated -> {
        if (_fronts.value !is APIState.Success) return

        if (_fronts.value.ensureData.none { it.front.id == message.frontItem.front.id }) {
          _frontHistory.tryEmit(
            _frontHistory.value.mapValues { (_, value) ->
              return@mapValues if (value.isSuccess) {
                APIState.Success(value.ensureData.map { (triple, second) ->
                  Pair(triple, second.map {
                    if (it.frontID == message.frontItem.front.id) {
                      it.copy(comment = message.frontItem.front.comment)
                    } else it
                  })
                })
                value
              } else {
                value
              }
            }
          )
        } else {
          _fronts.tryEmit(APIState.Success(_fronts.value.ensureData.map {
            if (it.front.id == message.frontItem.front.id) message.frontItem else it
          }))
        }
      }

      is ChannelMessage.FrontDeleted -> {
        if (_fronts.value !is APIState.Success) return
        if (_frontHistory.value.isEmpty()) return

        _frontHistory.tryEmit(
          _frontHistory.value.mapValues { (_, value) ->
            return@mapValues if (value.isSuccess) {
              APIState.Success(value.ensureData.map { (triple, second) ->
                Pair(triple, second.filter { it.frontID != message.frontID }.toMutableList())
              })
            } else {
              value
            }
          }
        )
      }

      is ChannelMessage.FrontingSet -> {
        if (_fronts.value !is APIState.Success) return
        _fronts.tryEmit(APIState.Success(listOf(message.frontItem)))
      }

      is ChannelMessage.PrimaryFrontSet -> {
        if (_systemMe.value !is APIState.Success) return
        _systemMe.tryEmit(
          APIState.Success(
            _systemMe.value.ensureData.copy(
              primaryFront = message.alterID
            )
          )
        )
      }

      is ChannelMessage.TagCreated -> {
        if (_tags.value !is APIState.Success) return
        _tags.tryEmit(
          APIState.Success(
            _tags.value.ensureData.plus(message.tag).sortedLocaleAware { it.name }
          )
        )
      }

      is ChannelMessage.TagDeleted -> {
        if (_tags.value !is APIState.Success) return
        _tags.tryEmit(
          APIState.Success(
            _tags.value.ensureData.filter { it.id != message.tagID }.map {
              // Make all children root tags
              if (it.parentTagID == message.tagID) {
                it.copy(parentTagID = null)
              } else {
                it
              }
            }
          )
        )
      }

      is ChannelMessage.TagUpdated -> {
        if (_tags.value !is APIState.Success) return
        _tags.tryEmit(
          APIState.Success(
            _tags.value.ensureData.map {
              if (it.id == message.tag.id) {
                message.tag
              } else {
                it
              }
            }.sortedLocaleAware { it.name }
          )
        )
      }

      is ChannelMessage.FieldsUpdated -> {
        if (_systemMe.value !is APIState.Success) return
        _systemMe.tryEmit(
          APIState.Success(
            _systemMe.value.ensureData.copy(
              fields = message.fields
            )
          )
        )
      }

      is ChannelMessage.SelfUpdated -> {
        if (_systemMe.value !is APIState.Success) return
        _systemMe.tryEmit(
          APIState.Success(
            message.data
          )
        )
      }

      is ChannelMessage.FriendAdded -> {
        if (_friends.value !is APIState.Success) return

        _friends.tryEmit(
          APIState.Success(
            _friends.value.ensureData.plus(
              FriendshipContainer(
                friend = message.friend,
                friendship = message.friendship,
                fronting = message.fronting
              )
            )
          )
        )
      }

      is ChannelMessage.FriendRemoved -> {
        if (_friends.value !is APIState.Success) return
        _friends.tryEmit(
          APIState.Success(
            _friends.value.ensureData.filter { it.friend.id != message.friendID }
          )
        )
      }

      is ChannelMessage.FriendRequestReceived -> {
        if (_friendRequests.value !is APIState.Success) return

        _friendRequests.tryEmit(
          APIState.Success(
            _friendRequests.value.ensureData.copyWithAppendIncoming(
              FriendRequest.Incoming(
                request = message.request,
                system = message.system
              )
            )
          )
        )
      }

      is ChannelMessage.FriendRequestRemoved -> {
        if (_friendRequests.value !is APIState.Success) return
        _friendRequests.tryEmit(
          APIState.Success(
            _friendRequests.value.ensureData.copy(
              incoming = _friendRequests.value.ensureData.incoming.filter {
                it.system.id != message.systemID
              },
              outgoing = _friendRequests.value.ensureData.outgoing.filter {
                it.system.id != message.systemID
              }
            )
          )
        )
      }

      is ChannelMessage.FriendRequestSent -> {
        if (_friendRequests.value !is APIState.Success) return

        _friendRequests.tryEmit(
          APIState.Success(
            _friendRequests.value.ensureData.copyWithAppendOutgoing(
              FriendRequest.Outgoing(
                request = message.request,
                system = message.system
              )
            )
          )
        )
      }

      is ChannelMessage.FriendTrusted -> {
        if (_friends.value !is APIState.Success) return
        _friends.tryEmit(
          APIState.Success(
            _friends.value.ensureData.map {
              if (it.friend.id == message.friendID) {
                it.copy(friendship = it.friendship.copy(level = FriendshipLevel.TrustedFriend))
              } else {
                it
              }
            }
          )
        )
      }

      is ChannelMessage.FriendUntrusted -> {
        if (_friends.value !is APIState.Success) return
        _friends.tryEmit(
          APIState.Success(
            _friends.value.ensureData.map {
              if (it.friend.id == message.friendID) {
                it.copy(friendship = it.friendship.copy(level = FriendshipLevel.Friend))
              } else {
                it
              }
            }
          )
        )
      }

      is ChannelMessage.GlobalJournalEntryCreated -> {
        if (_globalJournals.value !is APIState.Success) return
        _globalJournals.tryEmit(
          APIState.Success(
            _globalJournals.value.ensureData.plus(message.entry)
              .sortedByDescending { it.insertedAt }
          )
        )
      }

      is ChannelMessage.GlobalJournalEntryDeleted -> {
        if (_globalJournals.value !is APIState.Success) return
        _globalJournals.tryEmit(
          APIState.Success(
            _globalJournals.value.ensureData.filter { it.id != message.entryID }
          )
        )
      }

      is ChannelMessage.GlobalJournalEntryUpdated -> {
        if (_globalJournals.value !is APIState.Success) return
        _globalJournals.tryEmit(
          APIState.Success(
            _globalJournals.value.ensureData.map {
              if (it.id == message.entry.id) {
                message.entry
              } else {
                it
              }
            }.sortedByDescending { it.insertedAt }
          )
        )
      }

      is ChannelMessage.AlterJournalEntryCreated -> {
        val newMap = _alterJournals.value.toMutableMap()
        newMap[message.entry.alterID] =
          newMap[message.entry.alterID].orEmpty().plus(message.entry)

        _alterJournals.tryEmit(newMap)
      }

      is ChannelMessage.AlterJournalEntryDeleted -> {
        _alterJournals.tryEmit(
          _alterJournals.value.mapValues { (_, value) ->
            value.filter { it.id != message.entryID }
          }
        )
      }

      is ChannelMessage.AlterJournalEntryUpdated -> {
        val newMap = _alterJournals.value.toMutableMap()
        newMap[message.entry.alterID] = newMap[message.entry.alterID].orEmpty().map {
          if (it.id == message.entry.id) {
            message.entry
          } else {
            it
          }
        }

        _alterJournals.tryEmit(newMap)
      }

      is ChannelMessage.PollCreated -> {
        if (_polls.value !is APIState.Success) return
        _polls.tryEmit(
          APIState.Success(
            _polls.value.ensureData.plus(message.poll)
          )
        )
      }

      is ChannelMessage.PollUpdated -> {
        if (_polls.value !is APIState.Success) return
        _polls.tryEmit(
          APIState.Success(
            _polls.value.ensureData.map {
              if (it.id == message.poll.id) {
                message.poll
              } else {
                it
              }
            }
          )
        )
      }

      is ChannelMessage.PollDeleted -> {
        if (_polls.value !is APIState.Success) return
        _polls.tryEmit(
          APIState.Success(
            _polls.value.ensureData.filter { it.id != message.pollID }
          )
        )
      }

      is ChannelMessage.PKImportComplete -> {
        if (_systemMe.value !is APIState.Success) return
        platformUtilities.showAlert("Successfully imported from PluralKit!")
      }

      is ChannelMessage.SPImportComplete -> {
        if (_systemMe.value !is APIState.Success) return
        platformUtilities.showAlert("Successfully imported from Simply Plural!")
      }

      is ChannelMessage.DiscordAccountLinked -> {
        if (_systemMe.value !is APIState.Success) return
        _systemMe.tryEmit(
          APIState.Success(
            _systemMe.value.ensureData.copy(
              discordID = message.discordID
            )
          )
        )
      }

      is ChannelMessage.GoogleAccountLinked -> {
        if (_systemMe.value !is APIState.Success) return
        _systemMe.tryEmit(
          APIState.Success(
            _systemMe.value.ensureData.copy(
              email = message.email
            )
          )
        )
      }

      is ChannelMessage.DiscordAccountUnlinked -> {
        if (_systemMe.value !is APIState.Success) return
        _systemMe.tryEmit(
          APIState.Success(
            _systemMe.value.ensureData.copy(
              discordID = null
            )
          )
        )
      }

      is ChannelMessage.GoogleAccountUnlinked -> {
        if (_systemMe.value !is APIState.Success) return
        _systemMe.tryEmit(
          APIState.Success(
            _systemMe.value.ensureData.copy(
              email = null
            )
          )
        )
      }

      is ChannelMessage.EncryptedDataWiped -> {
        _alterJournals.tryEmit(hashMapOf())
        _globalJournals.tryEmit(APIState.Success(emptyList()))
        if (_systemMe.value is APIState.Success) {
          _systemMe.tryEmit(
            APIState.Success(_systemMe.value.ensureData.copy(encryptionInitialized = false))
          )
        }
      }

      is ChannelMessage.AccountDeleted -> {
        logOut(soft = false)
        settingsInterface.nukeEverything(true)
      }

      is ChannelMessage.AltersWiped -> {
        _alters.tryEmit(APIState.Success(emptyList()))
        _loadedAlters.tryEmit(hashMapOf())
        _systemMe.tryEmit(
          APIState.Success(
            _systemMe.value.ensureData.copy(
              primaryFront = null,
              lifetimeAlterCount = 0
            )
          )
        )
        clearAllFrontHistory()
      }

      is ChannelMessage.Ok -> {}

      is ChannelMessage.Error -> {
        // TODO: Snackbar?
        platformLog("Error: ${message.error}")
      }
    }
  }

  /* --------------- BULK RESOURCES --------------- */
  override fun reloadAlters(pushLoadingState: Boolean) =
    loadResourceOverSocket(
      _alters,
      endpoint = "systems/me/alters",
      pushLoadingState = pushLoadingState
    )

  override fun reloadFronts(pushLoadingState: Boolean) =
    loadResourceOverSocket(
      _fronts,
      endpoint = "systems/me/fronting",
      pushLoadingState = pushLoadingState
    )

  override fun loadFrontHistory(
    monthYearPair: MonthYearPair,
    force: Boolean,
    pushLoadingState: Boolean,
    successCallback: (() -> Unit)?
  ) {
    val startAnchor =
      LocalDateTime(
        monthYearPair.first,
        monthYearPair.second,
        1,
        0,
        0,
        0
      )
        .toInstant(TimeZone.currentSystemDefault())
        .toEpochMilliseconds() / 1000
    val endAnchor =
      LocalDateTime(
        monthYearPair.first,
        monthYearPair.second,
        monthDays(monthYearPair.first, Month(monthYearPair.second)),
        23,
        59,
        59
      )
        .toInstant(TimeZone.currentSystemDefault())
        .toEpochMilliseconds() / 1000

    if (
      !force
      && _frontHistory.value[monthYearPair] != null
      && _frontHistory.value[monthYearPair] is APIState.Success
    ) return

    if (pushLoadingState) {
      _frontHistory.tryEmit(_frontHistory.value.toMutableMap().apply {
        this[monthYearPair] = APIState.Loading()
      })
    }

    sendAPIRequest<List<MyFront>>(
      Get,
      "systems/me/front/between?start=$startAnchor&end=$endAnchor"
    ) { isSuccess, response ->
      if (isSuccess) {
        successCallback?.invoke()
        _frontHistory.tryEmit(
          _frontHistory.value.toMutableMap().apply {
            this[monthYearPair] =
              APIState.Success(clusterFrontData(response.data!!, monthYearPair.second))
          }
        )
      } else {
        _frontHistory.tryEmit(
          _frontHistory.value.toMutableMap().apply {
            this[monthYearPair] = APIState.Error("Failed to load front history")
          }
        )
      }
    }
  }

  private fun clearAllFrontHistory() = _frontHistory.tryEmit(hashMapOf())

  private fun clearMonthFrontHistory(monthYearPair: MonthYearPair) =
    _frontHistory.tryEmit(
      _frontHistory.value.toMutableMap().apply {
        this.remove(monthYearPair)
      }
    )

  override fun reloadFriends(pushLoadingState: Boolean) =
    loadResourceOverSocket(
      _friends,
      endpoint = "friends",
      pushLoadingState = pushLoadingState
    )

  override fun reloadFriendRequests(pushLoadingState: Boolean) =
    loadResourceOverSocket(
      _friendRequests,
      endpoint = "friend-requests",
      pushLoadingState = pushLoadingState
    )

  override fun reloadGlobalJournals(pushLoadingState: Boolean) =
    loadResourceOverSocket(
      _globalJournals,
      endpoint = "journals",
      pushLoadingState = pushLoadingState
    )

  override fun loadAlterJournals(alterID: Int) =
    sendAPIRequest(
      Get,
      "systems/me/alters/$alterID/journals"
    ) { isSuccess, response ->
      if (isSuccess) {
        _alterJournals.tryEmit(
          _alterJournals.value.plus(alterID to response.data!!)
        )
      }
    }

  override fun reloadPolls(pushLoadingState: Boolean) =
    loadResourceOverSocket(
      _polls,
      endpoint = "polls",
      pushLoadingState = pushLoadingState
    )

  private inline fun <reified ResponseType> loadResourceOverSocket(
    stateFlow: MutableStateFlow<APIState<ResponseType>>,
    endpoint: String,
    pushLoadingState: Boolean = true,
    noinline postProcessorFunction: ((ResponseType) -> ResponseType)? = null
  ) =
    coroutineScope.launch {
      if (pushLoadingState) {
        stateFlow.emit(APIState.Loading())
      }
      try {
        withContext(ioDispatcher) {
          socketSession!!.sendMessage(
            "endpoint",
            buildEndpointPayload(Get, endpoint)
          ) {
            val (_, response) = responseFromAdapterMessage<ResponseType>(it)
            val resState = response.toState()
            if (resState is APIState.Success) {
              stateFlow.emit(
                APIState.Success(
                  postProcessorFunction?.invoke(resState.data) ?: resState.data
                )
              )
            } else {
              stateFlow.emit(resState)
            }
          }
        }
      } catch (e: Exception) {
        e.printStackTrace()
        stateFlow.emit(
          APIState.Error("Network request failed. Are you connected to the internet?")
        )
      }
    }

  /*
   *
   * --------------- API ---------------
   *
   */

  /* --------------- ALTERS --------------- */

  override fun createAlter(name: String) =
    sendAPIRequest(
      Post,
      "systems/me/alters",
      buildRequestBody(
        mapOf(
          "name" to globalSerializer.encodeToJsonElement(name)
        )
      )
    )

  override fun deleteAlter(alterID: Int) =
    sendAPIRequest<JsonElement>(
      Delete,
      "systems/me/alters/$alterID"
    ) { isSuccess, _ ->
      if (isSuccess) {
        platformUtilities.updateWidgets()
      }
    }


  override fun loadAlter(alterID: Int) =
    sendAPIRequest<MyAlter>(
      Get,
      "systems/me/alters/$alterID"
    ) { isSuccess, response ->
      _loadedAlters.tryEmit(
        _loadedAlters.value.toMutableMap().apply {
          this[alterID] = if (isSuccess) {
            APIState.Success(response.data!!)
          } else {
            APIState.Error("Failed to load alter")
          }
        }
      )
    }

  override fun setAlterPinned(alterID: Int, pinned: Boolean) =
    sendAPIRequest(
      HttpMethod.Patch,
      "systems/me/alters/${alterID}",
      buildRequestBody(
        mapOf(
          "pinned" to globalSerializer.encodeToJsonElement(pinned)
        )
      )
    )

  override fun setAlterAvatar(alterID: Int, bytes: ByteArray, fileName: String) =
    launchIO {
      app.octocon.app.api.setAlterAvatar(token.value, alterID, bytes, fileName).emitError()
    }

  override fun removeAlterAvatar(alterID: Int) =
    sendAPIRequest(
      Delete,
      "systems/me/alters/$alterID/avatar"
    )

  /* --------------- FRONTS --------------- */

  override fun endFront(alterID: Int) =
    sendAPIRequest<JsonElement>(
      Post,
      "systems/me/front/end",
      buildRequestBody(
        mapOf(
          "id" to globalSerializer.encodeToJsonElement(alterID)
        )
      )
    ) { isSuccess, _ ->
      if (isSuccess) {
        platformUtilities.updateWidgets()
      }
    }

  override fun startFront(alterID: Int) =
    sendAPIRequest<JsonElement>(
      Post,
      "systems/me/front/start",
      buildRequestBody(
        mapOf(
          "id" to globalSerializer.encodeToJsonElement(alterID)
        )
      )
    ) { isSuccess, _ ->
      if (isSuccess) {
        platformUtilities.updateWidgets()
      }
    }

  override fun setFront(alterID: Int) =
    sendAPIRequest<JsonElement>(
      Post,
      "systems/me/front/set",
      buildRequestBody(
        mapOf(
          "id" to globalSerializer.encodeToJsonElement(alterID)
        )
      )
    ) { isSuccess, _ ->
      if (isSuccess) {
        platformUtilities.updateWidgets()
      }
    }

  override fun setPrimaryFront(alterID: Int?) =
    sendAPIRequest<JsonElement>(
      Post,
      "systems/me/front/primary",
      buildRequestBody(
        mapOf(
          "id" to globalSerializer.encodeToJsonElement(alterID)
        )
      )
    ) { isSuccess, _ ->
      if (isSuccess) {
        platformUtilities.updateWidgets()
      }
    }

  override fun editFrontComment(frontID: String, comment: String) =
    sendAPIRequest(
      Post,
      "systems/me/front/$frontID/comment",
      buildRequestBody(
        mapOf(
          "comment" to globalSerializer.encodeToJsonElement(comment)
        )
      )
    )

  override fun deleteFront(frontID: String) =
    sendAPIRequest(
      Delete,
      "systems/me/front/$frontID"
    )

  /* --------------- FRIENDS --------------- */

  override fun loadFriend(friendID: String) =
    sendAPIRequest<CollatedFriendDataRaw>(
      Get,
      "systems/${parseAmbiguousID(friendID)}/batch"
    ) { isSuccess, response ->
      _friendDataMap.tryEmit(
        _friendDataMap.value.toMutableMap().apply {
          this[friendID] = if (isSuccess) {
            APIState.Success(response.data!!.let { data ->
              val tags = data.tags.sortedLocaleAware { it.name }
              CollatedFriendData(
                friendship = data.friendship,
                tags = tags,
                tagMap = tags.mapIndexed { index, externalTag -> externalTag.id to index }.toMap(),
                alters = data.alters,
              )
            })
          } else {
            APIState.Error("Failed to load friend")
          }
        }
      )
    }

  override fun sendFriendRequest(friendID: String) =
    sendAPIRequest(
      Put,
      "friend-requests/${parseAmbiguousID(friendID)}"
    )

  override fun cancelFriendRequest(friendID: String) =
    sendAPIRequest(
      Delete,
      "friend-requests/${parseAmbiguousID(friendID)}"
    )

  override fun acceptFriendRequest(friendID: String) =
    sendAPIRequest(
      Post,
      "friend-requests/${parseAmbiguousID(friendID)}/accept"
    )

  override fun rejectFriendRequest(friendID: String) =
    sendAPIRequest(
      Post,
      "friend-requests/${parseAmbiguousID(friendID)}/reject"
    )

  override fun removeFriend(friendID: String) =
    sendAPIRequest(
      Delete,
      "friends/${parseAmbiguousID(friendID)}"
    )

  override fun trustFriend(friendID: String) =
    sendAPIRequest(
      Post,
      "friends/${parseAmbiguousID(friendID)}/trust"
    )

  override fun untrustFriend(friendID: String) =
    sendAPIRequest(
      Post,
      "friends/${parseAmbiguousID(friendID)}/untrust"
    )

  /* --------------- JOURNALS --------------- */

  override fun createGlobalJournalEntry(title: String) =
    sendAPIRequest(
      Post,
      "journals",
      buildRequestBody(
        mapOf(
          "title" to globalSerializer.encodeToJsonElement(title)
        )
      )
    )

  override fun deleteGlobalJournalEntry(entryID: String) =
    sendAPIRequest(
      Delete,
      "journals/$entryID"
    )

  override fun lockGlobalJournalEntry(entryID: String) =
    sendAPIRequest(
      Post,
      "journals/$entryID/lock"
    )

  override fun unlockGlobalJournalEntry(entryID: String) =
    sendAPIRequest(
      Post,
      "journals/$entryID/unlock"
    )

  override fun pinGlobalJournalEntry(entryID: String) =
    sendAPIRequest(
      Post,
      "journals/$entryID/pin"
    )

  override fun unpinGlobalJournalEntry(entryID: String) =
    sendAPIRequest(
      Post,
      "journals/$entryID/unpin"
    )

  override fun attachAlterToGlobalJournalEntry(entryID: String, alterID: Int) =
    sendAPIRequest(
      Post,
      "journals/$entryID/alter",
      buildRequestBody(
        mapOf(
          "alter_id" to globalSerializer.encodeToJsonElement(alterID)
        )
      )
    )

  override fun detachAlterFromGlobalJournalEntry(entryID: String, alterID: Int) =
    sendAPIRequest(
      Delete,
      "journals/$entryID/alter",
      buildRequestBody(
        mapOf(
          "alter_id" to globalSerializer.encodeToJsonElement(alterID)
        )
      )
    )

  override fun createAlterJournalEntry(alterID: Int, title: String) =
    sendAPIRequest(
      Post,
      "systems/me/alters/$alterID/journals",
      buildRequestBody(
        mapOf(
          "title" to globalSerializer.encodeToJsonElement(title)
        )
      )
    )

  override fun deleteAlterJournalEntry(entryID: String) =
    sendAPIRequest(
      Delete,
      "systems/me/alters/journals/$entryID"
    )

  override fun lockAlterJournalEntry(entryID: String) =
    sendAPIRequest(
      Post,
      "systems/me/alters/journals/$entryID/lock"
    )

  override fun unlockAlterJournalEntry(entryID: String) =
    sendAPIRequest(
      Post,
      "systems/me/alters/journals/$entryID/unlock"
    )

  override fun pinAlterJournalEntry(entryID: String) =
    sendAPIRequest(
      Post,
      "systems/me/alters/journals/$entryID/pin"
    )

  override fun unpinAlterJournalEntry(entryID: String) =
    sendAPIRequest(
      Post,
      "systems/me/alters/journals/$entryID/unpin"
    )

  /* --------------- TAGS --------------- */

  override fun createTag(name: String) =
    sendAPIRequest(
      Post,
      "systems/me/tags",
      buildRequestBody(
        mapOf(
          "name" to globalSerializer.encodeToJsonElement(name)
        )
      )
    )

  override fun createTag(name: String, parentTagID: String) =
    sendAPIRequest(
      Post,
      "systems/me/tags",
      buildRequestBody(
        mapOf(
          "name" to globalSerializer.encodeToJsonElement(name),
          "parent_tag_id" to globalSerializer.encodeToJsonElement(parentTagID)
        )
      )
    )

  override fun deleteTag(tagID: String) =
    sendAPIRequest(
      Delete,
      "systems/me/tags/$tagID"
    )

  override fun attachAlterToTag(tagID: String, alterID: Int) =
    sendAPIRequest(
      Post,
      "systems/me/tags/$tagID/alter",
      buildRequestBody(
        mapOf(
          "alter_id" to globalSerializer.encodeToJsonElement(alterID)
        )
      )
    )

  override fun detachAlterFromTag(tagID: String, alterID: Int) =
    sendAPIRequest(
      Delete,
      "systems/me/tags/$tagID/alter",
      buildRequestBody(
        mapOf(
          "alter_id" to globalSerializer.encodeToJsonElement(alterID)
        )
      )
    )

  override fun setParentTagID(tagID: String, parentTagID: String) =
    sendAPIRequest(
      Post,
      "systems/me/tags/$tagID/parent",
      buildRequestBody(
        mapOf(
          "parent_tag_id" to globalSerializer.encodeToJsonElement(parentTagID)
        )
      )
    )

  override fun removeParentTagID(tagID: String) =
    sendAPIRequest(
      Delete,
      "systems/me/tags/$tagID/parent"
    )

  /* --------------- POLLS --------------- */

  override fun createPoll(title: String, type: PollType, timeEnd: Instant?) =
    sendAPIRequest(
      Post,
      "polls",
      buildRequestBody(
        mutableMapOf(
          "title" to globalSerializer.encodeToJsonElement(title),
          "type" to globalSerializer.encodeToJsonElement(type),
        ).apply {
          if (timeEnd != null) {
            this["time_end"] = globalSerializer.encodeToJsonElement(timeEnd)
          }
        }
      )
    )

  override fun deletePoll(pollID: String) =
    sendAPIRequest(
      Delete,
      "polls/$pollID"
    )

  /* --------------- SETTINGS --------------- */
  override fun provideFirebaseMessagingToken(token: String) {
    firebaseMessagingToken = token
  }

  override fun updatePushNotificationToken() {
    launchIO {
      app.octocon.app.api.updatePushNotificationToken(token.value, firebaseMessagingToken)?.emitError()
    }
  }

  override fun invalidatePushNotificationToken() {
    launchIO {
      app.octocon.app.api.invalidatePushNotificationToken(
        token.value,
        firebaseMessagingToken
      )//?.emitError()
    }
  }

  override fun createCustomField(name: String, type: CustomFieldType) =
    sendAPIRequest(
      Post,
      "settings/fields",
      buildRequestBody(
        mapOf(
          "name" to globalSerializer.encodeToJsonElement(name),
          "type" to globalSerializer.encodeToJsonElement(type.internalName)
        )
      )
    )

  override fun deleteCustomField(id: String) =
    sendAPIRequest(
      Delete,
      "settings/fields/$id"
    )

  override fun editCustomField(id: String, field: CustomField) =
    sendAPIRequest(
      HttpMethod.Patch,
      "settings/fields/$id",
      buildRequestBody(
        mapOf(
          "name" to globalSerializer.encodeToJsonElement(field.name),
          "security_level" to globalSerializer.encodeToJsonElement(field.securityLevel.internalName),
          "locked" to globalSerializer.encodeToJsonElement(field.locked)
        )
      )
    )

  override fun relocateCustomField(id: String, index: Int) =
    sendAPIRequest(
      Post,
      "settings/fields/$id/relocate",
      buildRequestBody(
        mapOf(
          "index" to globalSerializer.encodeToJsonElement(index)
        )
      )
    )

  override fun tryLinkDiscord(openUri: (String) -> Unit) {
    if (_systemMe.value !is APIState.Success) return
    sendAPIRequest<LinkTokenResponse>(
      Get,
      "settings/link_token",
      callback = { isSuccess, response ->
        if (isSuccess) {
          val linkToken = response.data!!.token

          openUri("https://api.octocon.app/auth/link/discord?link_token=$linkToken")
        }
      }
    )
  }

  override fun tryUnlinkDiscord() {
    if (_systemMe.value !is APIState.Success) return
    sendAPIRequest<LinkTokenResponse>(
      Post,
      "settings/unlink_discord"
    )
  }

  override fun tryLinkGoogle(openUri: (String) -> Unit) {
    if (_systemMe.value !is APIState.Success) return
    sendAPIRequest<LinkTokenResponse>(
      Get,
      "settings/link_token",
      callback = { isSuccess, response ->
        if (isSuccess) {
          val linkToken = response.data!!.token

          openUri("https://api.octocon.app/auth/link/google?link_token=$linkToken")
        }
      }
    )
  }

  override fun tryUnlinkEmail() {
    if (_systemMe.value !is APIState.Success) return
    sendAPIRequest(
      Post,
      "settings/unlink_email"
    )
  }

  override fun updateUsername(username: String) {
    if (_systemMe.value !is APIState.Success) return
    sendAPIRequest(
      Post,
      "settings/username",
      buildRequestBody(
        mapOf(
          "username" to globalSerializer.encodeToJsonElement(username)
        )
      )
    )
  }

  override fun updateDescription(description: String?) {
    if (_systemMe.value !is APIState.Success) return
    sendAPIRequest(
      Post,
      "settings/description",
      buildRequestBody(
        mapOf(
          "description" to globalSerializer.encodeToJsonElement(description)
        )
      )
    )
  }

  override fun setSystemAvatar(bytes: ByteArray, fileName: String) {
    launchIO {
      app.octocon.app.api.setSystemAvatar(token.value, bytes, fileName).emitError()
    }
  }

  override fun removeSystemAvatar() =
    sendAPIRequest(
      Delete,
      "settings/avatar"
    )

  override fun importSP(spToken: String) =
    sendAPIRequest(
      Post,
      "settings/import-sp",
      buildRequestBody(
        mapOf(
          "token" to globalSerializer.encodeToJsonElement(spToken)
        )
      )
    )

  override fun importPK(pkToken: String) =
    sendAPIRequest(
      Post,
      "settings/import-pk",
      buildRequestBody(
        mapOf(
          "token" to globalSerializer.encodeToJsonElement(pkToken)
        )
      )
    )

  override fun deleteAccount() =
    sendAPIRequest(
      Post,
      "settings/delete-account"
    )

  override fun wipeAlters() =
    sendAPIRequest(
      Post,
      "settings/wipe-alters"
    )

  /* --------------- UTILS --------------- */

  private fun buildRequestBody(params: Map<String, JsonElement>): String {
    return globalSerializer.encodeToString(params)
  }

  fun sendAPIRequest(
    method: HttpMethod,
    path: String,
    body: String = "",
  ) =
    sendAPIRequest<JsonElement>(method, path, body)

  inline fun <reified ResponseType> sendAPIRequest(
    method: HttpMethod,
    path: String,
    body: String = "",
    noinline callback: ((Boolean, APIResponse<ResponseType>) -> Unit)? = null
  ) {
    launchIO {
      socketSession!!.sendMessage(
        "endpoint",
        buildEndpointPayload(method, path, body)
      ) {
        val (isSuccess, response) = responseFromAdapterMessage<ResponseType>(it)
        callback?.invoke(isSuccess, response)
        if (!isSuccess) {
          _errorFlow.emit("Error: ${response.error!!}")
        }
      }
    }
  }

  inline fun launchIO(crossinline block: suspend CoroutineScope.() -> Unit) =
    coroutineScope.launch(ioDispatcher) {
      block()
    }

  private suspend inline fun HttpResponse.emitError(): HttpResponse {
    if (!this.status.isSuccess()) {
      _errorFlow.emit(this.body<APIResponse<Nothing>>().error!!)
    }
    return this
  }

  inline fun <reified ResponseType> responseFromAdapterMessage(message: String): Pair<Boolean, APIResponse<ResponseType>> {
    val response = globalSerializer.decodeFromString<SocketAdapterResponse>(message)
    if (response.body.isEmpty()) {
      return (true to APIResponse(null, null))
    }
    val isSuccess = response.status in 200 until 300
    return (isSuccess to globalSerializer.decodeFromString<APIResponse<ResponseType>>(response.body))
  }
}