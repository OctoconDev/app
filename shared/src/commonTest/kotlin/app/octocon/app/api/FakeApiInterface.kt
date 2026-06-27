package app.octocon.app.api

import app.octocon.app.api.model.AlterJournalEntry
import app.octocon.app.api.model.APIError
import app.octocon.app.api.model.CollatedFriendData
import app.octocon.app.api.model.CustomField
import app.octocon.app.api.model.CustomFieldType
import app.octocon.app.api.model.FriendshipContainer
import app.octocon.app.api.model.GlobalJournalEntry
import app.octocon.app.api.model.MyAlter
import app.octocon.app.api.model.MyFrontItem
import app.octocon.app.api.model.MySystem
import app.octocon.app.api.model.MyTag
import app.octocon.app.api.model.Poll
import app.octocon.app.api.model.PollType
import app.octocon.app.ui.compose.screens.main.hometabs.FrontHistoryItem
import app.octocon.app.ui.model.interfaces.ApiInterface
import app.octocon.app.utils.MonthYearPair
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.Month
import kotlin.time.Instant

/**
 * Throw-everything test double of [ApiInterface] for component tests that need a
 * concrete [ApiInterface] in their [app.octocon.app.ui.model.CommonComponentContext]
 * but never actually trigger network or mutation paths. State flows return empty /
 * loading values; the only methods with non-throwing bodies are the two
 * lifecycle-shaped ones ([loadClient] / [logOut]) that conceivably run from
 * constructors but in practice don't for the home-tabs surface under test.
 */
class FakeApiInterface : ApiInterface {
  override val token: StateFlow<String> = MutableStateFlow("")
  override var firebaseMessagingToken: String? = null
  override val initComplete: StateFlow<Boolean> = MutableStateFlow(false)
  override val eventFlow: Flow<ChannelMessage> = MutableSharedFlow()
  override val errorFlow: Flow<APIError> = MutableSharedFlow()

  override val systemMe: StateFlow<APIState<MySystem>> = MutableStateFlow(APIState.Loading())
  override val alters: StateFlow<APIState<List<MyAlter>>> = MutableStateFlow(APIState.Loading())
  override val loadedAlters: StateFlow<Map<Int, APIState<MyAlter>>> = MutableStateFlow(emptyMap())
  override val tags: StateFlow<APIState<List<MyTag>>> = MutableStateFlow(APIState.Loading())
  override val globalJournals: StateFlow<APIState<List<GlobalJournalEntry>>> =
    MutableStateFlow(APIState.Loading())
  override val alterJournals: StateFlow<Map<Int, List<AlterJournalEntry>>> =
    MutableStateFlow(emptyMap())
  override val polls: StateFlow<APIState<List<Poll>>> = MutableStateFlow(APIState.Loading())
  override val fronts: StateFlow<APIState<List<MyFrontItem>>> =
    MutableStateFlow(APIState.Loading())
  override val frontHistory: StateFlow<
    Map<MonthYearPair,
      APIState<List<Pair<Triple<Int, Month, Int>, MutableList<FrontHistoryItem>>>>>
    > = MutableStateFlow(emptyMap())
  override val friends: StateFlow<APIState<List<FriendshipContainer>>> =
    MutableStateFlow(APIState.Loading())
  override val friendDataMap: StateFlow<Map<String, APIState<CollatedFriendData>>> =
    MutableStateFlow(emptyMap())
  override val friendRequests: StateFlow<APIState<FriendRequests>> =
    MutableStateFlow(APIState.Loading())
  override val encryptionIsInitializing: StateFlow<Boolean> = MutableStateFlow(false)

  override fun loadClient(initialToken: String): Job =
    CompletableDeferred<Unit>().also { it.complete(Unit) }
  override fun logOut(soft: Boolean) = Unit

  private fun unsupported(): Nothing =
    error("FakeApiInterface does not support API calls in component tests")

  override suspend fun generateRecoveryCode(): Pair<String, String> = unsupported()
  override fun setupEncryption(
    recoveryCodeJWE: String,
    onSuccess: () -> Unit,
    onFailure: (error: String) -> Unit
  ) = unsupported()
  override fun resetEncryption() = unsupported()
  override fun recoverEncryption(
    recoveryCode: String,
    onSuccess: () -> Unit,
    onFailure: (error: String) -> Unit
  ) = unsupported()

  override fun reloadAlters(pushLoadingState: Boolean): Job = unsupported()
  override fun reloadTags(pushLoadingState: Boolean): Job = unsupported()
  override fun reloadFronts(pushLoadingState: Boolean): Job = unsupported()
  override fun loadFrontHistory(
    monthYearPair: MonthYearPair,
    force: Boolean,
    pushLoadingState: Boolean,
    successCallback: (() -> Unit)?
  ) = unsupported()
  override fun reloadFriends(pushLoadingState: Boolean): Job = unsupported()
  override fun reloadFriendRequests(pushLoadingState: Boolean): Job = unsupported()
  override fun reloadGlobalJournals(pushLoadingState: Boolean): Job = unsupported()
  override fun loadAlterJournals(alterID: Int) = unsupported()
  override fun reloadPolls(pushLoadingState: Boolean): Job = unsupported()

  override fun createAlter(name: String) = unsupported()
  override fun deleteAlter(alterID: Int) = unsupported()
  override fun loadAlter(alterID: Int) = unsupported()
  override fun setAlterPinned(alterID: Int, pinned: Boolean) = unsupported()
  override fun setAlterAvatar(alterID: Int, bytes: ByteArray, fileName: String): Any =
    unsupported()
  override fun removeAlterAvatar(alterID: Int) = unsupported()

  override fun sendFriendRequest(friendID: String) = unsupported()
  override fun cancelFriendRequest(friendID: String) = unsupported()
  override fun acceptFriendRequest(friendID: String) = unsupported()
  override fun rejectFriendRequest(friendID: String) = unsupported()
  override fun removeFriend(friendID: String) = unsupported()
  override fun loadFriend(friendID: String) = unsupported()
  override fun trustFriend(friendID: String) = unsupported()
  override fun untrustFriend(friendID: String) = unsupported()

  override fun endFront(alterID: Int) = unsupported()
  override fun startFront(alterID: Int) = unsupported()
  override fun setFront(alterID: Int) = unsupported()
  override fun setPrimaryFront(alterID: Int?) = unsupported()
  override fun editFrontComment(frontID: String, comment: String) = unsupported()
  override fun deleteFront(frontID: String) = unsupported()

  override fun createGlobalJournalEntry(title: String) = unsupported()
  override fun deleteGlobalJournalEntry(entryID: String) = unsupported()
  override fun lockGlobalJournalEntry(entryID: String) = unsupported()
  override fun unlockGlobalJournalEntry(entryID: String) = unsupported()
  override fun pinGlobalJournalEntry(entryID: String) = unsupported()
  override fun unpinGlobalJournalEntry(entryID: String) = unsupported()

  override fun createAlterJournalEntry(alterID: Int, title: String) = unsupported()
  override fun deleteAlterJournalEntry(entryID: String) = unsupported()
  override fun attachAlterToGlobalJournalEntry(entryID: String, alterID: Int) = unsupported()
  override fun detachAlterFromGlobalJournalEntry(entryID: String, alterID: Int) = unsupported()
  override fun lockAlterJournalEntry(entryID: String) = unsupported()
  override fun unlockAlterJournalEntry(entryID: String) = unsupported()
  override fun pinAlterJournalEntry(entryID: String) = unsupported()
  override fun unpinAlterJournalEntry(entryID: String) = unsupported()

  override fun createTag(name: String) = unsupported()
  override fun createTag(name: String, parentTagID: String) = unsupported()
  override fun deleteTag(tagID: String) = unsupported()
  override fun attachAlterToTag(tagID: String, alterID: Int) = unsupported()
  override fun detachAlterFromTag(tagID: String, alterID: Int) = unsupported()
  override fun setParentTagID(tagID: String, parentTagID: String) = unsupported()
  override fun removeParentTagID(tagID: String) = unsupported()

  override fun createPoll(title: String, type: PollType, timeEnd: Instant?) = unsupported()
  override fun deletePoll(pollID: String) = unsupported()

  override fun createCustomField(name: String, type: CustomFieldType) = unsupported()
  override fun deleteCustomField(id: String) = unsupported()
  override fun editCustomField(id: String, field: CustomField) = unsupported()
  override fun relocateCustomField(id: String, index: Int) = unsupported()

  override fun provideFirebaseMessagingToken(token: String) = unsupported()
  override fun updatePushNotificationToken() = unsupported()
  override fun invalidatePushNotificationToken() = unsupported()

  override fun tryLinkDiscord(openUri: (String) -> Unit) = unsupported()
  override fun tryUnlinkDiscord() = unsupported()
  override fun tryLinkGoogle(openUri: (String) -> Unit) = unsupported()
  override fun tryUnlinkEmail() = unsupported()
  override fun tryLinkApple(openUri: (String) -> Unit) = unsupported()
  override fun tryUnlinkApple() = unsupported()

  override fun updateUsername(username: String) = unsupported()
  override fun updateDescription(description: String?) = unsupported()
  override fun setSystemAvatar(bytes: ByteArray, fileName: String) = unsupported()
  override fun removeSystemAvatar() = unsupported()

  override fun importSP(spToken: String, recoveryCode: String?) = unsupported()
  override fun importPK(pkToken: String) = unsupported()

  override fun deleteAccount() = unsupported()
  override fun wipeAlters() = unsupported()
}
