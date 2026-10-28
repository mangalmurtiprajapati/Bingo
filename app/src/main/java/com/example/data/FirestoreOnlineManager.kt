package com.example.data

import android.util.Log
import com.example.model.ChallengeStatus
import com.example.model.FriendChallenge
import com.example.model.OnlineDuelRoom
import com.example.model.OnlinePlayer
import com.example.model.RoomStatus
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

private const val TAG = "FirestoreOnlineManager"
private const val USERS_COLLECTION = "users"
private const val CHALLENGES_COLLECTION = "challenges"
private const val ROOMS_COLLECTION = "rooms"

class FirestoreOnlineManager {

    private val firestore: FirebaseFirestore? by lazy {
        try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize FirebaseFirestore: ${e.message}")
            null
        }
    }

    private val auth: FirebaseAuth? by lazy {
        try {
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize FirebaseAuth: ${e.message}")
            null
        }
    }

    val isAvailable: Boolean
        get() = firestore != null

    val currentUserId: String
        get() = auth?.currentUser?.uid ?: "guest_${UUID.randomUUID().toString().take(6)}"

    /**
     * Ensures the device has an authenticated Firebase user context (anonymous or registered).
     */
    suspend fun ensureAuth(): String {
        val a = auth ?: return currentUserId
        val user = a.currentUser
        if (user != null) return user.uid
        return try {
            val res = a.signInAnonymously().await()
            res.user?.uid ?: currentUserId
        } catch (e: Exception) {
            currentUserId
        }
    }

    // =========================================================================
    // USER PRESENCE & PROFILE
    // =========================================================================

    suspend fun registerOrUpdateUser(
        userId: String,
        name: String,
        avatar: String,
        level: Int
    ) {
        val fs = firestore ?: return
        try {
            val userMap = mapOf(
                "uid" to userId,
                "name" to name,
                "avatar" to avatar,
                "level" to level,
                "status" to "ONLINE",
                "isOnline" to true,
                "lastActive" to System.currentTimeMillis()
            )
            fs.collection(USERS_COLLECTION).document(userId)
                .set(userMap, SetOptions.merge())
                .await()
            Log.d(TAG, "User presence registered: $name ($userId)")
        } catch (e: Exception) {
            Log.w(TAG, "Error registering user presence: ${e.message}")
        }
    }

    suspend fun updatePresenceStatus(userId: String, isOnline: Boolean, status: String = "ONLINE") {
        val fs = firestore ?: return
        try {
            fs.collection(USERS_COLLECTION).document(userId)
                .update(
                    mapOf(
                        "isOnline" to isOnline,
                        "status" to status,
                        "lastActive" to System.currentTimeMillis()
                    )
                ).await()
        } catch (e: Exception) {
            Log.w(TAG, "Error updating user presence: ${e.message}")
        }
    }

    /**
     * Real-time list of online players available for duel challenges
     */
    fun listenOnlinePlayers(currentUid: String): Flow<List<OnlinePlayer>> = callbackFlow {
        val fs = firestore
        if (fs == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        var listener: ListenerRegistration? = null
        try {
            listener = fs.collection(USERS_COLLECTION)
                .whereEqualTo("isOnline", true)
                .limit(25)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w(TAG, "listenOnlinePlayers error: ${error.message}")
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val players = snapshot.documents.mapNotNull { doc ->
                            try {
                                val uid = doc.getString("uid") ?: doc.id
                                if (uid == currentUid) null else {
                                    OnlinePlayer(
                                        uid = uid,
                                        name = doc.getString("name") ?: "Player",
                                        avatar = doc.getString("avatar") ?: "😎",
                                        level = (doc.getLong("level") ?: 1L).toInt(),
                                        status = doc.getString("status") ?: "ONLINE",
                                        lastActive = doc.getLong("lastActive") ?: 0L,
                                        isOnline = doc.getBoolean("isOnline") ?: true
                                    )
                                }
                            } catch (e: Exception) {
                                null
                            }
                        }
                        trySend(players)
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to attach listenOnlinePlayers: ${e.message}")
            trySend(emptyList())
        }

        awaitClose {
            listener?.remove()
        }
    }

    // =========================================================================
    // FRIEND CHALLENGES (SEND, PENDING, ACCEPT, REJECT)
    // =========================================================================

    /**
     * Sends a direct duel challenge to a friend or online user.
     * Stored in /challenges/{challengeId} with status PENDING.
     */
    suspend fun sendChallenge(
        senderId: String,
        senderName: String,
        senderLevel: Int,
        senderAvatar: String,
        receiverId: String,
        receiverName: String,
        roomCode: String
    ): Result<String> {
        val fs = firestore ?: return Result.failure(IllegalStateException("Firestore not initialized"))
        return try {
            val challengeId = UUID.randomUUID().toString()
            val challengeData = mapOf(
                "id" to challengeId,
                "senderId" to senderId,
                "senderName" to senderName,
                "senderLevel" to senderLevel,
                "senderAvatar" to senderAvatar,
                "receiverId" to receiverId,
                "receiverName" to receiverName,
                "roomCode" to roomCode,
                "status" to ChallengeStatus.PENDING,
                "statusMessage" to "⚔️ Duel challenge sent! Awaiting opponent's response...",
                "createdAt" to System.currentTimeMillis(),
                "updatedAt" to System.currentTimeMillis()
            )

            // Also create the room in WAITING state
            createRoom(
                roomId = roomCode,
                hostId = senderId,
                hostName = senderName,
                hostAvatar = senderAvatar,
                isMatchmaking = false
            )

            fs.collection(CHALLENGES_COLLECTION).document(challengeId)
                .set(challengeData)
                .await()

            Log.d(TAG, "Challenge created: $challengeId to $receiverName")
            Result.success(challengeId)
        } catch (e: Exception) {
            Log.e(TAG, "Error sending challenge: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Listens for incoming challenges directed to currentUserId with status PENDING.
     */
    fun listenIncomingChallenges(userId: String): Flow<List<FriendChallenge>> = callbackFlow {
        val fs = firestore
        if (fs == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        var listener: ListenerRegistration? = null
        try {
            listener = fs.collection(CHALLENGES_COLLECTION)
                .whereEqualTo("receiverId", userId)
                .whereEqualTo("status", ChallengeStatus.PENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w(TAG, "listenIncomingChallenges error: ${error.message}")
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val challenges = snapshot.documents.mapNotNull { doc ->
                            try {
                                FriendChallenge(
                                    id = doc.getString("id") ?: doc.id,
                                    senderId = doc.getString("senderId") ?: "",
                                    senderName = doc.getString("senderName") ?: "Challenger",
                                    senderLevel = (doc.getLong("senderLevel") ?: 1L).toInt(),
                                    senderAvatar = doc.getString("senderAvatar") ?: "⚔️",
                                    receiverId = doc.getString("receiverId") ?: "",
                                    receiverName = doc.getString("receiverName") ?: "",
                                    roomCode = doc.getString("roomCode") ?: "",
                                    status = doc.getString("status") ?: ChallengeStatus.PENDING,
                                    statusMessage = doc.getString("statusMessage") ?: "",
                                    createdAt = doc.getLong("createdAt") ?: 0L,
                                    updatedAt = doc.getLong("updatedAt") ?: 0L
                                )
                            } catch (e: Exception) {
                                null
                            }
                        }
                        trySend(challenges)
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error attaching listenIncomingChallenges: ${e.message}")
            trySend(emptyList())
        }

        awaitClose {
            listener?.remove()
        }
    }

    /**
     * Listens to a specific outgoing challenge doc so sender gets real-time status:
     * - PENDING (Waiting for opponent)
     * - ACCEPTED (Opponent accepted, enter game)
     * - REJECTED (Opponent rejected)
     * - CANCELLED / EXPIRED
     */
    fun listenChallenge(challengeId: String): Flow<FriendChallenge?> = callbackFlow {
        val fs = firestore
        if (fs == null) {
            trySend(null)
            close()
            return@callbackFlow
        }

        var listener: ListenerRegistration? = null
        try {
            listener = fs.collection(CHALLENGES_COLLECTION).document(challengeId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w(TAG, "listenChallenge error: ${error.message}")
                        return@addSnapshotListener
                    }
                    if (snapshot != null && snapshot.exists()) {
                        try {
                            val challenge = FriendChallenge(
                                id = snapshot.getString("id") ?: snapshot.id,
                                senderId = snapshot.getString("senderId") ?: "",
                                senderName = snapshot.getString("senderName") ?: "",
                                senderLevel = (snapshot.getLong("senderLevel") ?: 1L).toInt(),
                                senderAvatar = snapshot.getString("senderAvatar") ?: "⚔️",
                                receiverId = snapshot.getString("receiverId") ?: "",
                                receiverName = snapshot.getString("receiverName") ?: "",
                                roomCode = snapshot.getString("roomCode") ?: "",
                                status = snapshot.getString("status") ?: ChallengeStatus.PENDING,
                                statusMessage = snapshot.getString("statusMessage") ?: "",
                                createdAt = snapshot.getLong("createdAt") ?: 0L,
                                updatedAt = snapshot.getLong("updatedAt") ?: 0L
                            )
                            trySend(challenge)
                        } catch (e: Exception) {
                            Log.w(TAG, "Error parsing challenge: ${e.message}")
                        }
                    } else {
                        trySend(null)
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up listenChallenge: ${e.message}")
            trySend(null)
        }

        awaitClose {
            listener?.remove()
        }
    }

    /**
     * Accepts an incoming challenge.
     * Updates status to ACCEPTED and joins the room as guest.
     */
    suspend fun acceptChallenge(
        challengeId: String,
        roomCode: String,
        guestId: String,
        guestName: String,
        guestAvatar: String
    ): Result<Unit> {
        val fs = firestore ?: return Result.failure(IllegalStateException("Firestore not available"))
        return try {
            fs.collection(CHALLENGES_COLLECTION).document(challengeId)
                .update(
                    mapOf(
                        "status" to ChallengeStatus.ACCEPTED,
                        "statusMessage" to "⚡ Duel Accepted! Synchronizing Arena...",
                        "updatedAt" to System.currentTimeMillis()
                    )
                ).await()

            // Join the duel room
            joinRoom(
                roomId = roomCode,
                guestId = guestId,
                guestName = guestName,
                guestAvatar = guestAvatar
            )

            Log.d(TAG, "Challenge $challengeId accepted, joined room $roomCode")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error accepting challenge: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Declines / rejects an incoming challenge.
     * Updates challenge doc to REJECTED so sender immediately sees the decline.
     */
    suspend fun rejectChallenge(challengeId: String): Result<Unit> {
        val fs = firestore ?: return Result.failure(IllegalStateException("Firestore not available"))
        return try {
            fs.collection(CHALLENGES_COLLECTION).document(challengeId)
                .update(
                    mapOf(
                        "status" to ChallengeStatus.REJECTED,
                        "statusMessage" to "🛡️ Challenge Declined. Opponent retreated.",
                        "updatedAt" to System.currentTimeMillis()
                    )
                ).await()
            Log.d(TAG, "Challenge $challengeId rejected")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error rejecting challenge: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Cancels an outgoing challenge.
     */
    suspend fun cancelChallenge(challengeId: String): Result<Unit> {
        val fs = firestore ?: return Result.failure(IllegalStateException("Firestore not available"))
        return try {
            fs.collection(CHALLENGES_COLLECTION).document(challengeId)
                .update(
                    mapOf(
                        "status" to ChallengeStatus.CANCELLED,
                        "statusMessage" to "Challenge was cancelled by sender.",
                        "updatedAt" to System.currentTimeMillis()
                    )
                ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // =========================================================================
    // DUEL ROOMS & MATCHMAKING
    // =========================================================================

    /**
     * Creates a duel room in Firestore.
     */
    suspend fun createRoom(
        roomId: String,
        hostId: String,
        hostName: String,
        hostAvatar: String,
        isMatchmaking: Boolean
    ): Result<Unit> {
        val fs = firestore ?: return Result.failure(IllegalStateException("Firestore not available"))
        return try {
            ensureAuth()
            val roomData = mutableMapOf<String, Any>(
                "roomId" to roomId,
                "hostId" to hostId,
                "hostName" to hostName,
                "hostAvatar" to hostAvatar,
                "status" to RoomStatus.WAITING,
                "isMatchmaking" to isMatchmaking,
                "currentTurnPlayerId" to hostId,
                "cutNumbers" to emptyList<Int>(),
                "hostCompletedLines" to 0,
                "guestCompletedLines" to 0,
                "createdAt" to System.currentTimeMillis(),
                "updatedAt" to System.currentTimeMillis()
            )
            fs.collection(ROOMS_COLLECTION).document(roomId)
                .set(roomData, SetOptions.merge())
                .await()
            Log.d(TAG, "Room created: $roomId (isMatchmaking=$isMatchmaking)")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating room: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Joins an existing room as a guest.
     * When asChallenge = true (Private Room PIN), status is set to CHALLENGE_PENDING so host receives prompt to accept.
     */
    suspend fun joinRoom(
        roomId: String,
        guestId: String,
        guestName: String,
        guestAvatar: String,
        asChallenge: Boolean = false
    ): Result<Unit> {
        val fs = firestore ?: return Result.failure(IllegalStateException("Firestore not available"))
        return try {
            ensureAuth()
            val statusToSet = if (asChallenge) RoomStatus.CHALLENGE_PENDING else RoomStatus.PLAYERS_JOINED
            fs.collection(ROOMS_COLLECTION).document(roomId)
                .set(
                    mapOf(
                        "guestId" to guestId,
                        "guestName" to guestName,
                        "guestAvatar" to guestAvatar,
                        "status" to statusToSet,
                        "updatedAt" to System.currentTimeMillis()
                    ),
                    SetOptions.merge()
                ).await()
            Log.d(TAG, "Joined room: $roomId as guest $guestName (status=$statusToSet)")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error joining room: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Host accepts the incoming PIN duel challenge from guest
     */
    suspend fun acceptRoomChallenge(roomId: String): Result<Unit> {
        val fs = firestore ?: return Result.failure(IllegalStateException("Firestore not available"))
        return try {
            fs.collection(ROOMS_COLLECTION).document(roomId)
                .set(
                    mapOf(
                        "status" to RoomStatus.PLAYERS_JOINED,
                        "updatedAt" to System.currentTimeMillis()
                    ),
                    SetOptions.merge()
                ).await()
            Log.d(TAG, "Host accepted duel challenge for room: $roomId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error accepting room challenge: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Host declines the incoming PIN duel challenge from guest
     */
    suspend fun declineRoomChallenge(roomId: String): Result<Unit> {
        val fs = firestore ?: return Result.failure(IllegalStateException("Firestore not available"))
        return try {
            fs.collection(ROOMS_COLLECTION).document(roomId)
                .set(
                    mapOf(
                        "guestId" to null,
                        "guestName" to null,
                        "guestAvatar" to null,
                        "status" to RoomStatus.DECLINED,
                        "updatedAt" to System.currentTimeMillis()
                    ),
                    SetOptions.merge()
                ).await()
            Log.d(TAG, "Host declined duel challenge for room: $roomId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error declining room challenge: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Searches for an open public matchmaking room. If found, joins it.
     * Otherwise, creates a new public matchmaking room and waits.
     */
    suspend fun findOrHostMatchmaking(
        userId: String,
        userName: String,
        userAvatar: String
    ): Result<Pair<String, Boolean>> { // Returns (roomId, isHost)
        val fs = firestore ?: return Result.failure(IllegalStateException("Firestore not available"))
        return try {
            val query = fs.collection(ROOMS_COLLECTION)
                .whereEqualTo("isMatchmaking", true)
                .whereEqualTo("status", RoomStatus.WAITING)
                .limit(5)
                .get()
                .await()

            val existingRoom = query.documents.firstOrNull { doc ->
                val hostId = doc.getString("hostId")
                hostId != userId // Don't match with self
            }

            if (existingRoom != null) {
                val roomId = existingRoom.getString("roomId") ?: existingRoom.id
                joinRoom(
                    roomId = roomId,
                    guestId = userId,
                    guestName = userName,
                    guestAvatar = userAvatar
                )
                Result.success(Pair(roomId, false))
            } else {
                val chars = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ"
                val newRoomId = "BG" + (1..4).map { chars.random() }.joinToString("")
                createRoom(
                    roomId = newRoomId,
                    hostId = userId,
                    hostName = userName,
                    hostAvatar = userAvatar,
                    isMatchmaking = true
                )
                Result.success(Pair(newRoomId, true))
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Log.e(TAG, "Error in matchmaking: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Listens to real-time changes in a duel room.
     */
    fun listenRoom(roomId: String): Flow<OnlineDuelRoom?> = callbackFlow {
        val fs = firestore
        if (fs == null) {
            trySend(null)
            close()
            return@callbackFlow
        }

        var listener: ListenerRegistration? = null
        try {
            listener = fs.collection(ROOMS_COLLECTION).document(roomId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w(TAG, "listenRoom error: ${error.message}")
                        return@addSnapshotListener
                    }
                    if (snapshot != null && snapshot.exists()) {
                        try {
                            val cutListRaw = snapshot.get("cutNumbers") as? List<*>
                            val cutNumbers = cutListRaw?.mapNotNull { (it as? Number)?.toInt() } ?: emptyList()

                            val room = OnlineDuelRoom(
                                roomId = snapshot.getString("roomId") ?: snapshot.id,
                                hostId = snapshot.getString("hostId") ?: "",
                                hostName = snapshot.getString("hostName") ?: "Host",
                                hostAvatar = snapshot.getString("hostAvatar") ?: "👑",
                                guestId = snapshot.getString("guestId"),
                                guestName = snapshot.getString("guestName"),
                                guestAvatar = snapshot.getString("guestAvatar"),
                                status = snapshot.getString("status") ?: RoomStatus.WAITING,
                                isMatchmaking = snapshot.getBoolean("isMatchmaking") ?: false,
                                currentTurnPlayerId = snapshot.getString("currentTurnPlayerId"),
                                lastCutNumber = (snapshot.getLong("lastCutNumber"))?.toInt(),
                                lastCutBy = snapshot.getString("lastCutBy"),
                                lastCutPlayerId = snapshot.getString("lastCutPlayerId"),
                                cutNumbers = cutNumbers,
                                hostCompletedLines = (snapshot.getLong("hostCompletedLines") ?: 0L).toInt(),
                                guestCompletedLines = (snapshot.getLong("guestCompletedLines") ?: 0L).toInt(),
                                winnerId = snapshot.getString("winnerId"),
                                winnerName = snapshot.getString("winnerName"),
                                lastReactionContent = snapshot.getString("lastReactionContent"),
                                lastReactionSenderId = snapshot.getString("lastReactionSenderId"),
                                lastReactionSenderName = snapshot.getString("lastReactionSenderName"),
                                lastReactionSenderAvatar = snapshot.getString("lastReactionSenderAvatar"),
                                lastReactionTimestamp = snapshot.getLong("lastReactionTimestamp") ?: 0L,
                                createdAt = snapshot.getLong("createdAt") ?: 0L,
                                updatedAt = snapshot.getLong("updatedAt") ?: 0L
                            )
                            trySend(room)
                        } catch (e: Exception) {
                            Log.w(TAG, "Error mapping OnlineDuelRoom: ${e.message}")
                        }
                    } else {
                        trySend(null)
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error attaching room listener: ${e.message}")
            trySend(null)
        }

        awaitClose {
            listener?.remove()
        }
    }

    /**
     * Executes a number cut in the room and switches turns in real time!
     */
    suspend fun cutNumber(
        roomId: String,
        number: Int,
        playerId: String,
        playerName: String,
        completedLines: Int,
        isHost: Boolean,
        nextTurnPlayerId: String?
    ): Result<Unit> {
        val fs = firestore ?: return Result.failure(IllegalStateException("Firestore not available"))
        return try {
            val updates = mutableMapOf<String, Any>(
                "cutNumbers" to FieldValue.arrayUnion(number),
                "lastCutNumber" to number,
                "lastCutBy" to playerName,
                "lastCutPlayerId" to playerId,
                "status" to RoomStatus.IN_PROGRESS,
                "updatedAt" to System.currentTimeMillis()
            )

            if (nextTurnPlayerId != null) {
                updates["currentTurnPlayerId"] = nextTurnPlayerId
            }

            if (isHost) {
                updates["hostCompletedLines"] = completedLines
            } else {
                updates["guestCompletedLines"] = completedLines
            }

            if (completedLines >= 5) {
                updates["winnerId"] = playerId
                updates["winnerName"] = playerName
                updates["status"] = RoomStatus.COMPLETED
            }

            fs.collection(ROOMS_COLLECTION).document(roomId)
                .update(updates)
                .await()
            Log.d(TAG, "Cut number $number in room $roomId by $playerName (playerId=$playerId)")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error cutting number in room: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Forfeits a room when a player misses 3 consecutive turns.
     */
    suspend fun forfeitRoom(roomId: String, forfeitedPlayerId: String, winnerId: String): Result<Unit> {
        val fs = firestore ?: return Result.failure(IllegalStateException("Firestore not available"))
        return try {
            fs.collection(ROOMS_COLLECTION).document(roomId)
                .update(
                    mapOf(
                        "status" to RoomStatus.COMPLETED,
                        "winnerId" to winnerId,
                        "updatedAt" to System.currentTimeMillis()
                    )
                ).await()
            Log.d(TAG, "Room $roomId forfeited by $forfeitedPlayerId; winner is $winnerId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error forfeiting room: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Updates room status when players complete setup
     */
    suspend fun setRoomReady(roomId: String): Result<Unit> {
        val fs = firestore ?: return Result.failure(IllegalStateException("Firestore not available"))
        return try {
            fs.collection(ROOMS_COLLECTION).document(roomId)
                .update(
                    mapOf(
                        "status" to RoomStatus.IN_PROGRESS,
                        "updatedAt" to System.currentTimeMillis()
                    )
                ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Cleans up room or leaves
     */
    suspend fun leaveRoom(roomId: String, userId: String) {
        val fs = firestore ?: return
        try {
            fs.collection(ROOMS_COLLECTION).document(roomId)
                .update(
                    mapOf(
                        "status" to RoomStatus.ABANDONED,
                        "updatedAt" to System.currentTimeMillis()
                    )
                ).await()
        } catch (e: Exception) {
            Log.w(TAG, "Error leaving room: ${e.message}")
        }
    }

    /**
     * Broadcasts in-game reaction or message to both players in the Firestore room
     */
    suspend fun sendRoomReaction(
        roomId: String,
        senderId: String,
        senderName: String,
        senderAvatar: String,
        content: String
    ): Result<Unit> {
        val fs = firestore ?: return Result.failure(IllegalStateException("Firestore not available"))
        return try {
            fs.collection(ROOMS_COLLECTION).document(roomId)
                .update(
                    mapOf(
                        "lastReactionContent" to content,
                        "lastReactionSenderId" to senderId,
                        "lastReactionSenderName" to senderName,
                        "lastReactionSenderAvatar" to senderAvatar,
                        "lastReactionTimestamp" to System.currentTimeMillis(),
                        "updatedAt" to System.currentTimeMillis()
                    )
                ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.w(TAG, "Error sending reaction in room $roomId: ${e.message}")
            Result.failure(e)
        }
    }
}
