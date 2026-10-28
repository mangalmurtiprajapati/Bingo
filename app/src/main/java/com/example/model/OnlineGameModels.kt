package com.example.model

/**
 * Represents an online player registered in Firebase Firestore.
 */
data class OnlinePlayer(
    val uid: String = "",
    val name: String = "",
    val avatar: String = "😎",
    val level: Int = 1,
    val status: String = "ONLINE", // "ONLINE", "IN_MATCH", "IN_QUEUE", "OFFLINE"
    val lastActive: Long = System.currentTimeMillis(),
    val isOnline: Boolean = true
)

/**
 * Challenge statuses in gaming terminology
 */
object ChallengeStatus {
    const val PENDING = "PENDING"       // ⚔️ Challenge Transmitted - Awaiting Response
    const val ACCEPTED = "ACCEPTED"     // ⚡ Duel Accepted - Synchronizing Arena
    const val REJECTED = "REJECTED"     // 🛡️ Challenge Declined - Opponent Retreated
    const val CANCELLED = "CANCELLED"   // ❌ Challenge Revoked
    const val EXPIRED = "EXPIRED"       // ⌛ Timed Out
}

/**
 * Represents a 1v1 challenge sent from one player to a friend or another online player.
 */
data class FriendChallenge(
    val id: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val senderLevel: Int = 1,
    val senderAvatar: String = "⚔️",
    val receiverId: String = "",
    val receiverName: String = "",
    val roomCode: String = "",
    val status: String = ChallengeStatus.PENDING,
    val statusMessage: String = "⚔️ Duel challenge transmitted! Awaiting opponent's response...",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Room status in the online arena
 */
object RoomStatus {
    const val WAITING = "WAITING_FOR_PLAYER"
    const val CHALLENGE_PENDING = "CHALLENGE_PENDING"
    const val PLAYERS_JOINED = "PLAYERS_JOINED"
    const val BOARD_SETUP = "BOARD_SETUP"
    const val IN_PROGRESS = "IN_PROGRESS"
    const val COMPLETED = "COMPLETED"
    const val ABANDONED = "ABANDONED"
    const val DECLINED = "DECLINED"
}

/**
 * Represents an active Online Match Room in Firestore for 1v1 synchronized gameplay.
 */
data class OnlineDuelRoom(
    val roomId: String = "",
    val hostId: String = "",
    val hostName: String = "Host",
    val hostAvatar: String = "👑",
    val guestId: String? = null,
    val guestName: String? = null,
    val guestAvatar: String? = null,
    val status: String = RoomStatus.WAITING,
    val isMatchmaking: Boolean = false,
    val currentTurnPlayerId: String? = null,
    val lastCutNumber: Int? = null,
    val lastCutBy: String? = null,
    val lastCutPlayerId: String? = null,
    val cutNumbers: List<Int> = emptyList(),
    val hostCompletedLines: Int = 0,
    val guestCompletedLines: Int = 0,
    val winnerId: String? = null,
    val winnerName: String? = null,
    val lastReactionContent: String? = null,
    val lastReactionSenderId: String? = null,
    val lastReactionSenderName: String? = null,
    val lastReactionSenderAvatar: String? = null,
    val lastReactionTimestamp: Long = 0L,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
