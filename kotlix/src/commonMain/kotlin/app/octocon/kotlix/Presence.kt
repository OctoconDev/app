package app.octocon.kotlix

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

//------------------------------------------------------------------------------
// Type Aliases
//------------------------------------------------------------------------------
/** Meta details of a Presence. Just a dictionary of properties */
typealias PresenceMeta = Map<String, Any>

/** A mapping of a String to an array of Metas. e.g. {"metas": [{id: 1}]} */
typealias PresenceMap = MutableMap<String, List<PresenceMeta>>

/** A mapping of a Presence state to a mapping of Metas */
typealias PresenceState = MutableMap<String, PresenceMap>

/**
 * Diff has keys "joins" and "leaves", pointing to a Presence.State each containing the users
 * that joined and left.
 */
typealias PresenceDiff = MutableMap<String, PresenceState>

/** Closure signature of OnJoin callbacks */
typealias OnJoin = (key: String, current: PresenceMap?, new: PresenceMap) -> Unit

/** Closure signature for OnLeave callbacks */
typealias OnLeave = (key: String, current: PresenceMap, left: PresenceMap) -> Unit

/** Closure signature for OnSync callbacks */
typealias OnSync = () -> Unit

@Suppress("UNCHECKED_CAST")
class Presence(channel: Channel, scope: CoroutineScope, opts: Options = Options.defaults) {

  //------------------------------------------------------------------------------
  // Enums and Data classes
  //------------------------------------------------------------------------------
  /**
   * Custom options that can be provided when creating Presence
   */
  data class Options(val events: Map<Events, String>) {
    companion object {

      /**
       * Default set of Options used when creating Presence. Uses the
       * phoenix events "presence_state" and "presence_diff"
       */
      val defaults: Options
        get() = Options(
          mapOf(
            Events.STATE to "presence_state",
            Events.DIFF to "presence_diff"
          )
        )
    }
  }

  /** Collection of callbacks with default values */
  data class Caller(
    var onJoin: OnJoin = { _, _, _ -> },
    var onLeave: OnLeave = { _, _, _ -> },
    var onSync: OnSync = {}
  )

  /** Presence Events of "state" and "diff" */
  enum class Events {
    STATE,
    DIFF
  }

  //------------------------------------------------------------------------------
  // Properties
  //------------------------------------------------------------------------------
  /** The channel the Presence belongs to */
  private val channel: Channel

  /** Caller to callback hooks */
  private val caller: Caller

  /** The state of the Presence */
  var state: PresenceState
    internal set

  /** Pending `join` and `leave` diffs that need to be synced */
  var pendingDiffs: MutableList<PresenceDiff>
    private set

  /** The channel's joinRef, set when state events occur */
  var joinRef: String?
    private set

  /** True if the Presence has not yet initially synced */
  private val isPendingSyncState: Boolean
    get() = this.joinRef == null || (this.joinRef !== this.channel.joinRef)

  //------------------------------------------------------------------------------
  // Initialization
  //------------------------------------------------------------------------------
  init {
    this.state = mutableMapOf()
    this.pendingDiffs = mutableListOf()
    this.channel = channel
    this.joinRef = null
    this.caller = Caller()

    val stateEvent = opts.events[Events.STATE]
    val diffEvent = opts.events[Events.DIFF]

    if (stateEvent != null && diffEvent != null) {
      scope.launch {
        channel.collect { message ->
          when (message.event) {
            stateEvent -> {
              val newState = message.rawPayload.toMutableMap() as PresenceState

              joinRef = channel.joinRef
              state =
                syncState(state, newState, caller.onJoin, caller.onLeave)


              pendingDiffs.forEach { diff ->
                state = syncDiff(state, diff, caller.onJoin, caller.onLeave)
              }

              pendingDiffs.clear()
              caller.onSync()
            }

            diffEvent -> {
              val diff = message.rawPayload.toMutableMap() as PresenceDiff
              if (isPendingSyncState) {
                pendingDiffs.add(diff)
              } else {
                state = syncDiff(state, diff, caller.onJoin, caller.onLeave)
                caller.onSync()
              }
            }
          }
        }
      }
    }
  }

  //------------------------------------------------------------------------------
  // Callbacks
  //------------------------------------------------------------------------------
  fun onJoin(callback: OnJoin) {
    this.caller.onJoin = callback
  }

  fun onLeave(callback: OnLeave) {
    this.caller.onLeave = callback
  }

  fun onSync(callback: OnSync) {
    this.caller.onSync = callback
  }

  //------------------------------------------------------------------------------
  // Listing
  //------------------------------------------------------------------------------
  fun list(): List<PresenceMap> {
    return this.listBy { it.value }
  }

  private fun <T> listBy(transform: (Map.Entry<String, PresenceMap>) -> T): List<T> {
    return listBy(state, transform)
  }

  fun filterBy(predicate: ((Map.Entry<String, PresenceMap>) -> Boolean)?): PresenceState {
    return filter(state, predicate)
  }

  //------------------------------------------------------------------------------
  // Syncing
  //------------------------------------------------------------------------------
  companion object {

    private fun cloneMap(map: PresenceMap): PresenceMap {
      val clone: PresenceMap = mutableMapOf()
      map.forEach { entry -> clone[entry.key] = entry.value.toList() }
      return clone
    }

    private fun cloneState(state: PresenceState): PresenceState {
      val clone: PresenceState = mutableMapOf()
      state.forEach { entry -> clone[entry.key] = cloneMap(entry.value) }
      return clone
    }


    /**
     * Used to sync the list of presences on the server with the client's state. An optional
     * `onJoin` and `onLeave` callback can be provided to react to changes in the client's local
     * presences across disconnects and reconnects with the server.
     *
     */
    fun syncState(
      currentState: PresenceState,
      newState: PresenceState,
      onJoin: OnJoin = { _, _, _ -> },
      onLeave: OnLeave = { _, _, _ -> }
    ): PresenceState {
      val state = cloneState(currentState)
      val leaves: PresenceState = mutableMapOf()
      val joins: PresenceState = mutableMapOf()

      state.forEach { (key, presence) ->
        if (!newState.containsKey(key)) {
          leaves[key] = presence
        }
      }

      newState.forEach { (key, newPresence) ->
        state[key]?.let { currentPresence ->
          val newRefs = newPresence["metas"]!!.map { meta -> meta["phx_ref"] as String }
          val curRefs = currentPresence["metas"]!!.map { meta -> meta["phx_ref"] as String }

          val joinedMetas = newPresence["metas"]!!.filter { meta ->
            curRefs.indexOf(meta["phx_ref"]) < 0
          }
          val leftMetas = currentPresence["metas"]!!.filter { meta ->
            newRefs.indexOf(meta["phx_ref"]) < 0
          }

          if (joinedMetas.isNotEmpty()) {
            joins[key] = cloneMap(newPresence)
            joins[key]!!["metas"] = joinedMetas
          }

          if (leftMetas.isNotEmpty()) {
            leaves[key] = cloneMap(currentPresence)
            leaves[key]!!["metas"] = leftMetas
          }
        } ?: run {
          joins[key] = newPresence
        }
      }

      val diff: PresenceDiff = mutableMapOf("joins" to joins, "leaves" to leaves)
      return syncDiff(state, diff, onJoin, onLeave)

    }

    /**
     * Used to sync a diff of presence join and leave events from the server, as they happen.
     * Like `syncState`, `syncDiff` accepts optional `onJoin` and `onLeave` callbacks to react
     * to a user joining or leaving from a device.
     */
    fun syncDiff(
      currentState: PresenceState,
      diff: PresenceDiff,
      onJoin: OnJoin = { _, _, _ -> },
      onLeave: OnLeave = { _, _, _ -> }
    ): PresenceState {
      val state = cloneState(currentState)

      // Sync the joined states and inform onJoin of new presence
      diff["joins"]?.forEach { (key, newPresence) ->
        val currentPresence = state[key]
        state[key] = cloneMap(newPresence)

        currentPresence?.let { curPresence ->
          val joinedRefs = state[key]!!["metas"]!!.map { m -> m["phx_ref"] as String }
          val curMetas = curPresence["metas"]!!.filter { m -> joinedRefs.indexOf(m["phx_ref"]) < 0 }

          // Data structures are immutable. Need to convert to a mutable copy,
          // add the metas, and then reassign to the state
          val mutableMetas = state[key]!!["metas"]!!.toMutableList()
          mutableMetas.addAll(0, curMetas)

          state[key]!!["metas"] = mutableMetas
        }

        onJoin.invoke(key, currentPresence, newPresence)
      }

      // Sync the left diff and inform onLeave of left presence
      diff["leaves"]?.forEach { (key, leftPresence) ->
        val curPresence = state[key] ?: return@forEach

        val refsToRemove = leftPresence["metas"]!!.map { it["phx_ref"] as String }
        curPresence["metas"] =
          curPresence["metas"]!!.filter { m -> refsToRemove.indexOf(m["phx_ref"]) < 0 }

        onLeave.invoke(key, curPresence, leftPresence)
        if (curPresence["metas"]?.isEmpty() == true) {
          state.remove(key)
        }
      }

      return state
    }

    fun filter(
      presence: PresenceState,
      predicate: ((Map.Entry<String, PresenceMap>) -> Boolean)?
    ): PresenceState {
      return presence.filter(predicate ?: { true }).toMutableMap()
    }

    fun <T> listBy(
      presence: PresenceState,
      transform: (Map.Entry<String, PresenceMap>) -> T
    ): List<T> {
      return presence.map(transform)
    }
  }
}
