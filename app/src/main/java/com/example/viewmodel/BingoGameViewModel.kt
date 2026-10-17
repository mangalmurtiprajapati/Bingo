package com.example.viewmodel

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.auth.AuthManager
import com.example.auth.AuthState
import com.example.auth.AuthUser
import com.example.data.BingoDatabase
import com.example.data.BingoRepository
import com.example.data.FirestoreOnlineManager
import com.example.model.BingoCard
import com.example.model.BingoTheme
import com.example.model.ChallengeStatus
import com.example.model.ChatReaction
import com.example.model.ClassicBingoBoard
import com.example.model.FriendChallenge
import com.example.model.FriendProfile
import com.example.model.GameDifficulty
import com.example.model.GameMode
import com.example.model.MatchOutcome
import com.example.model.MatchTurn
import com.example.model.OnlineDuelRoom
import com.example.model.OnlinePlayer
import com.example.model.RoomStatus
import com.example.model.ThemeId
import com.example.model.UserStats
import com.example.sound.SoundManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.random.Random

enum class GameStatus {
    IDLE,
    PLAYING,
    PAUSED,
    WON,
    GAME_OVER
}

data class ClassicMatchState(
    val gameMode: GameMode = GameMode.VS_COMPUTER,
    val isSetupComplete: Boolean = false,
    val currentSetupPlayer: Int = 1, // 1 for Player 1, 2 for Player 2
    val player1Board: ClassicBingoBoard = ClassicBingoBoard(),
    val player2OrComputerBoard: ClassicBingoBoard = ClassicBingoBoard(),
    val matchTurn: MatchTurn = MatchTurn.PLAYER_1_TURN,
    val isComputerThinking: Boolean = false,
    val lastCutNumber: Int? = null,
    val lastCutBy: String? = null,
    val matchOutcome: MatchOutcome = MatchOutcome.IN_PROGRESS,
    val cutHistory: List<Int> = emptyList(),
    val coinsEarned: Int = 0,
    val xpEarned: Int = 0,
    val viewingBoardPlayer: Int = 1,
    // Online mode properties
    val roomCode: String = "",
    val isOnlineSearching: Boolean = false,
    val onlineSearchElapsedSec: Int = 0,
    val onlineStatusMessage: String = "Ready to play",
    val matchedOpponentName: String? = null,
    val matchedOpponentLevel: Int = 1,
    val opponentAvatar: String = "😎",
    val isOpponentOnline: Boolean = true,
    val opponentSetupProgress: Int = 18,
    val isSimultaneousFriendMode: Boolean = false,
    val isOnlineBot: Boolean = false,
    val challengeLevel: Int = 1,
    val currentUserId: String = "",
    val activeChallengeId: String? = null,
    val isHost: Boolean = true,
    val friendsList: List<FriendProfile> = listOf(
        FriendProfile("Rohan_Star", 3, "Won 12 duels", isOnline = true, avatarEmoji = "🔥"),
        FriendProfile("Priya_Mind", 2, "Won 8 duels", isOnline = true, avatarEmoji = "🌸"),
        FriendProfile("Aarav_Ace", 4, "Won 19 duels", isOnline = false, avatarEmoji = "⚡"),
        FriendProfile("Simran_Pro", 5, "Won 24 duels", isOnline = true, avatarEmoji = "👑")
    )
)

data class BingoGameState(
    val status: GameStatus = GameStatus.IDLE,
    val difficulty: GameDifficulty = GameDifficulty.EASY,
    val cardCount: Int = 1,
    val cards: List<BingoCard> = emptyList(),
    val calledNumbers: List<Int> = emptyList(),
    val currentBallNumber: Int? = null,
    val timerProgress: Float = 1.0f,
    val winningPatterns: List<String> = emptyList(),
    val coinsEarnedInGame: Int = 0,
    val xpEarnedInGame: Int = 0,
    val isAutoDaubActive: Boolean = false
)

class BingoGameViewModel(application: Application) : AndroidViewModel(application) {

    private val db = BingoDatabase.getDatabase(application)
    val repository = BingoRepository(db.bingoDao())
    val soundManager = SoundManager(application)
    val authManager = AuthManager(application)

    val authState: StateFlow<AuthState> = authManager.authState
    val currentAuthUser: StateFlow<AuthUser?> = authManager.currentUser

    private val _userStats = MutableStateFlow(UserStats())
    val userStats: StateFlow<UserStats> = _userStats.asStateFlow()

    private val _gameState = MutableStateFlow(BingoGameState())
    val gameState: StateFlow<BingoGameState> = _gameState.asStateFlow()

    private val _classicState = MutableStateFlow(ClassicMatchState())
    val classicState: StateFlow<ClassicMatchState> = _classicState.asStateFlow()

    private val _liveReactions = MutableStateFlow<List<ChatReaction>>(emptyList())
    val liveReactions: StateFlow<List<ChatReaction>> = _liveReactions.asStateFlow()

    // Real-time Firestore Multiplayer & Challenge Integration
    val firestoreOnlineManager = FirestoreOnlineManager()

    private val _onlinePlayers = MutableStateFlow<List<OnlinePlayer>>(emptyList())
    val onlinePlayers: StateFlow<List<OnlinePlayer>> = _onlinePlayers.asStateFlow()

    private val _incomingChallenges = MutableStateFlow<List<FriendChallenge>>(emptyList())
    val incomingChallenges: StateFlow<List<FriendChallenge>> = _incomingChallenges.asStateFlow()

    private val _outgoingChallenge = MutableStateFlow<FriendChallenge?>(null)
    val outgoingChallenge: StateFlow<FriendChallenge?> = _outgoingChallenge.asStateFlow()

    private val _activeDuelRoom = MutableStateFlow<OnlineDuelRoom?>(null)
    val activeDuelRoom: StateFlow<OnlineDuelRoom?> = _activeDuelRoom.asStateFlow()

    private var roomListenerJob: Job? = null
    private var challengeListenerJob: Job? = null
    private var onlinePresenceJob: Job? = null
    private var ballCallJob: Job? = null
    private var computerTurnJob: Job? = null
    private var uncalledNumbers = (1..75).shuffled().toMutableList()
    private var lastReceivedReactionTimestamp: Long = 0L

    init {
        viewModelScope.launch {
            repository.ensureInitialized()
            repository.userStats.collectLatest { stats ->
                _userStats.value = stats
                soundManager.isSoundEnabled = stats.isSoundEnabled
                soundManager.isMusicEnabled = stats.isMusicEnabled
                _gameState.value = _gameState.value.copy(isAutoDaubActive = stats.isAutoDaubEnabled)

                // Sync current user presence and listen for incoming duel challenges in Firebase
                val currentUid = authManager.currentUser.value?.uid ?: "player_${stats.userName.lowercase().replace(" ", "_")}"
                _classicState.value = _classicState.value.copy(currentUserId = currentUid)
                registerAndListenOnline(currentUid, stats)
            }
        }
        viewModelScope.launch {
            authManager.currentUser.collectLatest { authUser ->
                if (authUser != null) {
                    val currentStats = _userStats.value
                    repository.updateUserProfile(
                        name = authUser.displayName,
                        provider = authUser.provider,
                        avatar = currentStats.userAvatar
                    )
                    _classicState.value = _classicState.value.copy(currentUserId = authUser.uid)
                    registerAndListenOnline(authUser.uid, currentStats)
                }
            }
        }
    }

    private fun registerAndListenOnline(userId: String, stats: UserStats) {
        onlinePresenceJob?.cancel()
        onlinePresenceJob = viewModelScope.launch {
            firestoreOnlineManager.registerOrUpdateUser(
                userId = userId,
                name = stats.userName,
                avatar = stats.userAvatar,
                level = stats.level
            )

            launch {
                firestoreOnlineManager.listenOnlinePlayers(userId).collectLatest { players ->
                    _onlinePlayers.value = players
                }
            }

            launch {
                firestoreOnlineManager.listenIncomingChallenges(userId).collectLatest { challenges ->
                    _incomingChallenges.value = challenges
                }
            }
        }
    }

    fun startNewGame(cardCount: Int = 1, difficulty: GameDifficulty = GameDifficulty.EASY) {
        stopGame()
        uncalledNumbers = (1..75).shuffled().toMutableList()

        val newCards = List(cardCount) { index ->
            BingoCard.generateRandomCard(cardId = index + 1)
        }

        _gameState.value = BingoGameState(
            status = GameStatus.PLAYING,
            difficulty = difficulty,
            cardCount = cardCount,
            cards = newCards,
            calledNumbers = emptyList(),
            currentBallNumber = null,
            timerProgress = 1.0f,
            winningPatterns = emptyList(),
            coinsEarnedInGame = 0,
            xpEarnedInGame = 0,
            isAutoDaubActive = _userStats.value.isAutoDaubEnabled
        )

        startBallCallLoop()
    }

    private fun startBallCallLoop() {
        ballCallJob?.cancel()
        ballCallJob = viewModelScope.launch {
            val interval = _gameState.value.difficulty.intervalMs
            val steps = 20

            while (_gameState.value.status == GameStatus.PLAYING && uncalledNumbers.isNotEmpty()) {
                // Countdown tick animation
                for (step in steps downTo 0) {
                    _gameState.value = _gameState.value.copy(timerProgress = step.toFloat() / steps)
                    delay(interval / steps)
                    if (_gameState.value.status != GameStatus.PLAYING) return@launch
                }

                if (uncalledNumbers.isEmpty()) break

                val nextNum = uncalledNumbers.removeAt(0)
                val updatedCalled = _gameState.value.calledNumbers + nextNum
                soundManager.playBallCall()

                var currentCards = _gameState.value.cards
                if (_gameState.value.isAutoDaubActive) {
                    currentCards = currentCards.map { card -> card.autoDaub(updatedCalled.toSet()) }
                }

                _gameState.value = _gameState.value.copy(
                    calledNumbers = updatedCalled,
                    currentBallNumber = nextNum,
                    cards = currentCards,
                    timerProgress = 1.0f
                )

                // Check auto win if auto daub is active
                if (_gameState.value.isAutoDaubActive) {
                    checkAndDeclareBingo(isAutoCheck = true)
                }
            }

            if (_gameState.value.status == GameStatus.PLAYING && uncalledNumbers.isEmpty()) {
                _gameState.value = _gameState.value.copy(status = GameStatus.GAME_OVER)
                soundManager.stopAmbientMusic()
            }
        }
    }

    fun onCellTapped(cardId: Int, number: Int) {
        if (_gameState.value.status != GameStatus.PLAYING) return

        // Check if number was actually called or is Free
        if (_gameState.value.calledNumbers.contains(number)) {
            soundManager.playDaub()
            val updatedCards = _gameState.value.cards.map { card ->
                if (card.id == cardId) card.markNumber(number) else card
            }
            _gameState.value = _gameState.value.copy(cards = updatedCards)
        }
    }

    fun declareBingoManual() {
        checkAndDeclareBingo(isAutoCheck = false)
    }

    private fun checkAndDeclareBingo(isAutoCheck: Boolean) {
        if (_gameState.value.status != GameStatus.PLAYING) return

        var anyBingo = false
        val allPatterns = mutableListOf<String>()

        val updatedCards = _gameState.value.cards.map { card ->
            val (hasBingo, patterns, checkedCard) = card.checkBingoPatterns()
            if (hasBingo) {
                anyBingo = true
                allPatterns.addAll(patterns)
            }
            checkedCard
        }

        if (anyBingo) {
            val mult = _gameState.value.difficulty.coinMultiplier
            val baseCoins = 200 * _gameState.value.cardCount
            val earnedCoins = (baseCoins * mult).toInt()
            val earnedXp = 150

            ballCallJob?.cancel()
            soundManager.stopAmbientMusic()
            soundManager.playWinFanfare()

            _gameState.value = _gameState.value.copy(
                status = GameStatus.WON,
                cards = updatedCards,
                winningPatterns = allPatterns.distinct(),
                coinsEarnedInGame = earnedCoins,
                xpEarnedInGame = earnedXp
            )

            viewModelScope.launch {
                repository.recordGameFinished(
                    isWin = true,
                    daubsMade = updatedCards.sumOf { c -> c.grid.flatten().count { it.isMarked } },
                    coinsEarned = earnedCoins
                )
            }
        } else if (!isAutoCheck) {
            // Player clicked BINGO button false alarm penalty
            soundManager.playClick()
        }
    }

    fun toggleAutoDaub() {
        val newAuto = !_gameState.value.isAutoDaubActive
        _gameState.value = _gameState.value.copy(isAutoDaubActive = newAuto)
        viewModelScope.launch {
            val current = repository.getCurrentStats()
            repository.saveUserStats(current.copy(isAutoDaubEnabled = newAuto))
        }
    }

    fun toggleSound(enabled: Boolean) {
        soundManager.isSoundEnabled = enabled
        viewModelScope.launch {
            val current = repository.getCurrentStats()
            repository.saveUserStats(current.copy(isSoundEnabled = enabled))
        }
    }

    fun toggleMusic(enabled: Boolean) {
        soundManager.isMusicEnabled = enabled
        if (enabled) soundManager.startAmbientGameMusic() else soundManager.stopAmbientMusic()
        viewModelScope.launch {
            val current = repository.getCurrentStats()
            repository.saveUserStats(current.copy(isMusicEnabled = enabled))
        }
    }

    fun selectTheme(themeId: ThemeId) {
        viewModelScope.launch {
            repository.selectTheme(themeId)
        }
    }

    fun unlockTheme(themeId: ThemeId, costCoins: Int) {
        viewModelScope.launch {
            repository.unlockTheme(themeId, costCoins)
        }
    }

    fun claimWheelReward(coins: Int) {
        viewModelScope.launch {
            repository.recordDailySpin(coins)
        }
    }

    fun claimAchievement(id: String, coins: Int, xp: Int) {
        viewModelScope.launch {
            repository.claimAchievementReward(id, coins, xp)
        }
    }

    fun resetAllProgress() {
        viewModelScope.launch {
            repository.resetProgress()
        }
    }

    fun stopGame() {
        ballCallJob?.cancel()
        computerTurnJob?.cancel()
        soundManager.stopAmbientMusic()
    }

    // ==================== REALISTIC HUMAN-LIKE PERSONAS & UNIQUE CODES ====================

    data class RealisticGamerPersona(
        val name: String,
        val avatar: String,
        val badge: String
    )

    private val realisticPersonas = listOf(
        RealisticGamerPersona("Aarav_Pro99", "🔥", "Mind Striker"),
        RealisticGamerPersona("Priya_Queen", "👑", "Bingo Master"),
        RealisticGamerPersona("Kabir_Master", "⚡", "Speed Tapper"),
        RealisticGamerPersona("Neha_Playz", "🌸", "Strategy Star"),
        RealisticGamerPersona("Rohan_Striker", "🎯", "Precision Mind"),
        RealisticGamerPersona("Vikram_Ace", "😎", "Top Ranker"),
        RealisticGamerPersona("Ananya_Spark", "✨", "Pattern Genius"),
        RealisticGamerPersona("Aditya_King", "🦁", "Arena Beast"),
        RealisticGamerPersona("Sneha_Rockz", "💫", "Tactical Brain"),
        RealisticGamerPersona("Aryan_Gamer", "🚀", "5-Line Pro"),
        RealisticGamerPersona("Tanvi_Mind", "🧠", "Duel Champion"),
        RealisticGamerPersona("Dev_Sniper", "🏹", "Corner Hunter"),
        RealisticGamerPersona("Ishaan_Blitz", "⚡", "Rapid Thinker"),
        RealisticGamerPersona("Rhea_Pro", "🌟", "Streak Legend")
    )

    fun generateComplexRoomCode(): String {
        // Alphanumeric, unique and complex (letters + numbers, avoiding confusing 0/O and 1/I)
        val chars = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ"
        val prefix = listOf("BG", "MK", "NZ", "RD", "XP").random()
        val randomSuffix = (1..4).map { chars.random() }.joinToString("")
        return "$prefix$randomSuffix"
    }

    // ==================== CLASSIC 5x5 (1 to 25) BINGO LOGIC ====================

    fun initClassicGame(
        mode: GameMode,
        forcedOpponentName: String? = null,
        forcedOpponentLevel: Int? = null,
        forcedOpponentAvatar: String? = null
    ) {
        computerTurnJob?.cancel()
        val current = _classicState.value
        val computerBoard = if (mode == GameMode.VS_COMPUTER || mode == GameMode.PLAY_ONLINE) {
            ClassicBingoBoard.generateRandomBoard()
        } else {
            ClassicBingoBoard()
        }

        // Generate realistic opponent profile whose level closely matches user's level!
        val userLevel = _userStats.value.level.coerceAtLeast(1)
        val delta = listOf(-1, 0, 1).random()
        val matchedLevel = (userLevel + delta).coerceIn(1, 10)
        val chosenPersona = realisticPersonas.random()

        val opponentName = forcedOpponentName
            ?: if (mode == GameMode.PLAY_ONLINE && !current.matchedOpponentName.isNullOrBlank()) {
                current.matchedOpponentName
            } else if (mode == GameMode.PLAY_WITH_FRIEND) {
                "Player 2"
            } else if (mode == GameMode.VS_COMPUTER) {
                "Computer AI"
            } else {
                chosenPersona.name
            }

        val opponentLevel = forcedOpponentLevel
            ?: if (mode == GameMode.PLAY_ONLINE && current.matchedOpponentLevel > 0) {
                current.matchedOpponentLevel
            } else {
                matchedLevel
            }

        val opponentAvatar = forcedOpponentAvatar
            ?: if (mode == GameMode.PLAY_ONLINE && current.opponentAvatar.isNotBlank() && current.opponentAvatar != "😎") {
                current.opponentAvatar
            } else if (mode == GameMode.VS_COMPUTER) {
                "🤖"
            } else {
                chosenPersona.avatar
            }

        _classicState.value = current.copy(
            gameMode = mode,
            isSetupComplete = false,
            currentSetupPlayer = 1,
            player1Board = ClassicBingoBoard.generateRandomBoard(),
            player2OrComputerBoard = computerBoard,
            matchedOpponentName = opponentName,
            matchedOpponentLevel = opponentLevel,
            opponentAvatar = opponentAvatar,
            matchTurn = MatchTurn.PLAYER_1_TURN,
            isComputerThinking = false,
            lastCutNumber = null,
            lastCutBy = null,
            matchOutcome = MatchOutcome.IN_PROGRESS,
            cutHistory = emptyList(),
            coinsEarned = 0,
            xpEarned = 0,
            viewingBoardPlayer = 1
        )
    }

    fun assignPlayerCell(row: Int, col: Int) {
        val current = _classicState.value
        if (current.isSetupComplete) return

        if (current.currentSetupPlayer == 1) {
            val board = current.player1Board
            val existing = board.cells.getOrNull(row)?.getOrNull(col)
            if (existing != null) {
                // If user clicks a filled cell during setup, remove it so they can adjust
                _classicState.value = current.copy(player1Board = board.removeCellNumber(row, col))
                soundManager.playClick()
            } else if (board.nextNumberToPlace <= 25) {
                _classicState.value = current.copy(player1Board = board.assignNextNumber(row, col))
                soundManager.playNumberPlaced()
            }
        } else {
            val board = current.player2OrComputerBoard
            val existing = board.cells.getOrNull(row)?.getOrNull(col)
            if (existing != null) {
                _classicState.value = current.copy(player2OrComputerBoard = board.removeCellNumber(row, col))
                soundManager.playClick()
            } else if (board.nextNumberToPlace <= 25) {
                _classicState.value = current.copy(player2OrComputerBoard = board.assignNextNumber(row, col))
                soundManager.playNumberPlaced()
            }
        }
    }

    fun autoFillCurrentBoard() {
        val current = _classicState.value
        soundManager.playClick()
        if (current.currentSetupPlayer == 1) {
            _classicState.value = current.copy(player1Board = ClassicBingoBoard.generateRandomBoard())
        } else {
            _classicState.value = current.copy(player2OrComputerBoard = ClassicBingoBoard.generateRandomBoard())
        }
    }

    fun clearCurrentBoard() {
        val current = _classicState.value
        soundManager.playClick()
        if (current.currentSetupPlayer == 1) {
            _classicState.value = current.copy(player1Board = ClassicBingoBoard())
        } else {
            _classicState.value = current.copy(player2OrComputerBoard = ClassicBingoBoard())
        }
    }

    fun proceedToNextSetupOrStartMatch() {
        val current = _classicState.value
        soundManager.playClick()

        if (current.gameMode == GameMode.PLAY_WITH_FRIEND && !current.isSimultaneousFriendMode && current.currentSetupPlayer == 1) {
            // Move to Player 2's board setup only for offline Pass & Play on same device
            _classicState.value = current.copy(
                currentSetupPlayer = 2,
                player2OrComputerBoard = ClassicBingoBoard()
            )
        } else {
            // Simultaneous mode or Player 2 ready: Start match directly
            val finalOpponentBoard = if (!current.player2OrComputerBoard.isFull) {
                ClassicBingoBoard.generateRandomBoard()
            } else {
                current.player2OrComputerBoard
            }

            _classicState.value = current.copy(
                isSetupComplete = true,
                player2OrComputerBoard = finalOpponentBoard,
                matchTurn = MatchTurn.PLAYER_1_TURN,
                viewingBoardPlayer = 1,
                matchOutcome = MatchOutcome.IN_PROGRESS
            )
            soundManager.startAmbientGameMusic()
        }
    }

    fun toggleSound() {
        val currentSound = _userStats.value.isSoundEnabled
        toggleSound(!currentSound)
    }

    fun cutNumber(number: Int) {
        val state = _classicState.value
        if (!state.isSetupComplete) return
        if (state.matchOutcome != MatchOutcome.IN_PROGRESS) return
        if (state.isComputerThinking) return
        if (state.player1Board.isNumberCrossed(number)) return

        val prevP1Lines = state.player1Board.completedLinesCount
        val prevP2Lines = state.player2OrComputerBoard.completedLinesCount

        val updatedP1Board = state.player1Board.crossNumber(number)
        val updatedP2Board = state.player2OrComputerBoard.crossNumber(number)
        val updatedHistory = state.cutHistory + number

        soundManager.playCrossSound()

        val newP1Lines = updatedP1Board.completedLinesCount
        val newP2Lines = updatedP2Board.completedLinesCount

        if (newP1Lines > prevP1Lines) {
            soundManager.playLineComplete()
        }

        val cutByName = if (state.matchTurn == MatchTurn.PLAYER_1_TURN) {
            if (state.gameMode == GameMode.PLAY_WITH_FRIEND) "Player 1" else "You"
        } else {
            when (state.gameMode) {
                GameMode.PLAY_WITH_FRIEND -> "Player 2"
                GameMode.PLAY_ONLINE -> state.matchedOpponentName ?: "Online Opponent"
                else -> "Computer"
            }
        }

        // Check winning conditions (5 completed lines)
        val p1Won = updatedP1Board.hasBingo
        val p2Won = updatedP2Board.hasBingo

        if (p1Won && p2Won) {
            _classicState.value = state.copy(
                player1Board = updatedP1Board,
                player2OrComputerBoard = updatedP2Board,
                lastCutNumber = number,
                lastCutBy = cutByName,
                cutHistory = updatedHistory,
                matchOutcome = MatchOutcome.DRAW,
                coinsEarned = 150,
                xpEarned = 100
            )
            soundManager.stopAmbientMusic()
            soundManager.playLineComplete()
            postOpponentReaction("🤝 Wow! Both got 5 lines! Perfect TIE!")
            viewModelScope.launch {
                repository.recordGameFinished(
                    isWin = false,
                    daubsMade = updatedP1Board.crossedNumbers.size,
                    coinsEarned = 150
                )
            }
            return
        }

        if (p1Won) {
            val earnedCoins = 300
            val earnedXp = 200
            _classicState.value = state.copy(
                player1Board = updatedP1Board,
                player2OrComputerBoard = updatedP2Board,
                lastCutNumber = number,
                lastCutBy = cutByName,
                cutHistory = updatedHistory,
                matchOutcome = MatchOutcome.PLAYER_WON,
                coinsEarned = earnedCoins,
                xpEarned = earnedXp
            )
            soundManager.stopAmbientMusic()
            soundManager.playWinFanfare()
            postOpponentReaction("🎉 Congratulations! You completed 5 lines first!")
            viewModelScope.launch {
                repository.recordGameFinished(
                    isWin = true,
                    daubsMade = updatedP1Board.crossedNumbers.size,
                    coinsEarned = earnedCoins
                )
            }
            return
        }

        if (p2Won) {
            _classicState.value = state.copy(
                player1Board = updatedP1Board,
                player2OrComputerBoard = updatedP2Board,
                lastCutNumber = number,
                lastCutBy = cutByName,
                cutHistory = updatedHistory,
                matchOutcome = MatchOutcome.COMPUTER_OR_PLAYER_2_WON,
                coinsEarned = 50,
                xpEarned = 50
            )
            soundManager.stopAmbientMusic()
            soundManager.playCrossSound()
            postOpponentReaction("🥳 BINGO! Great duel, let's rematch!")
            viewModelScope.launch {
                repository.recordGameFinished(
                    isWin = false,
                    daubsMade = updatedP1Board.crossedNumbers.size,
                    coinsEarned = 50
                )
            }
        }

        // If game continues:
        if (state.gameMode == GameMode.VS_COMPUTER) {
            _classicState.value = state.copy(
                player1Board = updatedP1Board,
                player2OrComputerBoard = updatedP2Board,
                lastCutNumber = number,
                lastCutBy = cutByName,
                cutHistory = updatedHistory,
                matchTurn = MatchTurn.PLAYER_2_OR_COMPUTER_TURN,
                isComputerThinking = true
            )
            triggerComputerTurn()
        } else if (state.gameMode == GameMode.PLAY_WITH_FRIEND) {
            val nextTurn = if (state.matchTurn == MatchTurn.PLAYER_1_TURN) {
                MatchTurn.PLAYER_2_OR_COMPUTER_TURN
            } else {
                MatchTurn.PLAYER_1_TURN
            }
            _classicState.value = state.copy(
                player1Board = updatedP1Board,
                player2OrComputerBoard = updatedP2Board,
                lastCutNumber = number,
                lastCutBy = cutByName,
                cutHistory = updatedHistory,
                matchTurn = nextTurn,
                viewingBoardPlayer = if (nextTurn == MatchTurn.PLAYER_1_TURN) 1 else 2
            )
        } else {
            // Online mode (Synchronized via Firestore Duel Room or Smart AI Fallback)
            val currentRoom = _activeDuelRoom.value
            _classicState.value = state.copy(
                player1Board = updatedP1Board,
                player2OrComputerBoard = updatedP2Board,
                lastCutNumber = number,
                lastCutBy = cutByName,
                cutHistory = updatedHistory,
                matchTurn = MatchTurn.PLAYER_2_OR_COMPUTER_TURN,
                isComputerThinking = true
            )

            if (state.isOnlineBot || state.gameMode == GameMode.VS_COMPUTER) {
                // Online Bot or Offline AI takes its turn!
                triggerComputerTurn()
            } else if (currentRoom != null) {
                // Real human opponent: broadcast move to Firestore room
                val isHost = state.isHost
                val opponentId = if (isHost) currentRoom.guestId else currentRoom.hostId
                viewModelScope.launch {
                    firestoreOnlineManager.cutNumber(
                        roomId = currentRoom.roomId,
                        number = number,
                        playerId = state.currentUserId,
                        playerName = cutByName,
                        completedLines = updatedP1Board.completedLinesCount,
                        isHost = isHost,
                        nextTurnPlayerId = opponentId
                    )
                }
            } else if (!state.isSimultaneousFriendMode && state.activeChallengeId == null) {
                triggerComputerTurn()
            }
        }
    }

    private fun triggerComputerTurn() {
        computerTurnJob?.cancel()
        computerTurnJob = viewModelScope.launch {
            val checkState = _classicState.value
            // Real Human Duel: Computer must NEVER play!
            val isRealHumanDuel = (checkState.isSimultaneousFriendMode || checkState.activeChallengeId != null || _activeDuelRoom.value != null) && !checkState.isOnlineBot
            if (isRealHumanDuel) {
                return@launch
            }

            // Realistic human-like thinking delay (2.0s to 3.4s)
            val realisticDelay = Random.nextLong(2000L, 3400L)
            delay(realisticDelay)

            val currentState = _classicState.value
            if (currentState.matchOutcome != MatchOutcome.IN_PROGRESS) return@launch
            val stillHumanDuel = (currentState.isSimultaneousFriendMode || currentState.activeChallengeId != null || _activeDuelRoom.value != null) && !currentState.isOnlineBot
            if (stillHumanDuel) {
                return@launch
            }

            val remainingNumbers = (1..25).filter { !currentState.player2OrComputerBoard.isNumberCrossed(it) }
            if (remainingNumbers.isEmpty()) return@launch

            // Strategic decision: Pick the number that maximizes line completions on board
            val bestPick = pickBestAiNumber(currentState.player2OrComputerBoard, remainingNumbers)

            val prevP1Lines = currentState.player1Board.completedLinesCount
            val prevP2Lines = currentState.player2OrComputerBoard.completedLinesCount

            val updatedP1 = currentState.player1Board.crossNumber(bestPick)
            val updatedP2 = currentState.player2OrComputerBoard.crossNumber(bestPick)
            val updatedHistory = currentState.cutHistory + bestPick

            soundManager.playCrossSound()

            if (updatedP2.completedLinesCount > prevP2Lines) {
                soundManager.playLineComplete()
            }

            val p1Won = updatedP1.hasBingo
            val p2Won = updatedP2.hasBingo

            val opponentName = currentState.matchedOpponentName ?: "Opponent"

            if (p1Won && p2Won) {
                _classicState.value = currentState.copy(
                    player1Board = updatedP1,
                    player2OrComputerBoard = updatedP2,
                    lastCutNumber = bestPick,
                    lastCutBy = opponentName,
                    cutHistory = updatedHistory,
                    isComputerThinking = false,
                    matchOutcome = MatchOutcome.DRAW,
                    coinsEarned = 150,
                    xpEarned = 100
                )
                soundManager.stopAmbientMusic()
                soundManager.playLineComplete()
                repository.recordGameFinished(
                    isWin = false,
                    daubsMade = updatedP1.crossedNumbers.size,
                    coinsEarned = 150
                )
            } else if (p2Won) {
                _classicState.value = currentState.copy(
                    player1Board = updatedP1,
                    player2OrComputerBoard = updatedP2,
                    lastCutNumber = bestPick,
                    lastCutBy = opponentName,
                    cutHistory = updatedHistory,
                    isComputerThinking = false,
                    matchOutcome = MatchOutcome.COMPUTER_OR_PLAYER_2_WON
                )
                soundManager.stopAmbientMusic()
                repository.recordGameFinished(
                    isWin = false,
                    daubsMade = updatedP1.crossedNumbers.size,
                    coinsEarned = 25
                )
            } else if (p1Won) {
                _classicState.value = currentState.copy(
                    player1Board = updatedP1,
                    player2OrComputerBoard = updatedP2,
                    lastCutNumber = bestPick,
                    lastCutBy = opponentName,
                    cutHistory = updatedHistory,
                    isComputerThinking = false,
                    matchOutcome = MatchOutcome.PLAYER_WON,
                    coinsEarned = 300,
                    xpEarned = 200
                )
                soundManager.stopAmbientMusic()
                soundManager.playWinFanfare()
                repository.recordGameFinished(
                    isWin = true,
                    daubsMade = updatedP1.crossedNumbers.size,
                    coinsEarned = 300
                )
            } else {
                _classicState.value = currentState.copy(
                    player1Board = updatedP1,
                    player2OrComputerBoard = updatedP2,
                    lastCutNumber = bestPick,
                    lastCutBy = opponentName,
                    cutHistory = updatedHistory,
                    matchTurn = MatchTurn.PLAYER_1_TURN,
                    isComputerThinking = false
                )
            }
        }
    }

    private fun pickBestAiNumber(board: ClassicBingoBoard, candidates: List<Int>): Int {
        var bestNumber = candidates.first()
        var maxScore = -1

        for (candidate in candidates) {
            var score = 0
            for (line in ClassicBingoBoard.ALL_LINES) {
                val numbersInLine = line.cellIndices.mapNotNull { (r, c) -> board.cells.getOrNull(r)?.getOrNull(c) }
                if (numbersInLine.contains(candidate)) {
                    val alreadyMarkedCount = numbersInLine.count { board.isNumberCrossed(it) }
                    val weight = when (alreadyMarkedCount) {
                        4 -> 120 // Finishes a line!
                        3 -> 35
                        2 -> 10
                        else -> 2
                    }
                    score += weight
                }
            }

            // Small tie-break randomness so bot is natural
            val jitter = Random.nextInt(0, 3)
            val totalScore = score + jitter
            if (totalScore > maxScore) {
                maxScore = totalScore
                bestNumber = candidate
            }
        }

        return bestNumber
    }

    fun switchViewingBoard(player: Int) {
        _classicState.value = _classicState.value.copy(viewingBoardPlayer = player)
    }

    fun rematchClassicGame() {
        val current = _classicState.value
        initClassicGame(current.gameMode)
    }

    private var onlineSearchJob: Job? = null

    // =========================================================================
    // Real Firestore Online Lobby & Friend Challenges
    // =========================================================================

    fun generateNewOnlineRoom(): String {
        val code = generateComplexRoomCode()
        val stats = _userStats.value
        val myUid = authManager.currentUser.value?.uid ?: "player_${stats.userName.lowercase().replace(" ", "_")}"

        _classicState.value = _classicState.value.copy(
            roomCode = code,
            isOnlineSearching = false,
            isHost = true,
            currentUserId = myUid,
            onlineStatusMessage = "Room #$code Created! Share code via WhatsApp or In-App Challenge."
        )

        viewModelScope.launch {
            firestoreOnlineManager.createRoom(
                roomId = code,
                hostId = myUid,
                hostName = stats.userName,
                hostAvatar = stats.userAvatar,
                isMatchmaking = false
            )
            attachRoomListener(code, myUid)
        }
        return code
    }

    fun joinOnlineRoom(code: String, onJoined: () -> Unit = {}) {
        val stats = _userStats.value
        val myUid = authManager.currentUser.value?.uid ?: "player_${stats.userName.lowercase().replace(" ", "_")}"

        _classicState.value = _classicState.value.copy(
            roomCode = code,
            isOnlineSearching = false,
            isHost = false,
            currentUserId = myUid,
            onlineStatusMessage = "Joining Room #$code...",
            gameMode = GameMode.PLAY_ONLINE
        )

        viewModelScope.launch {
            val result = firestoreOnlineManager.joinRoom(
                roomId = code,
                guestId = myUid,
                guestName = stats.userName,
                guestAvatar = stats.userAvatar
            )

            val oppName = _activeDuelRoom.value?.hostName ?: "Host Player"
            val oppLevel = _classicState.value.challengeLevel.coerceIn(1, 5)

            _classicState.value = _classicState.value.copy(
                matchedOpponentName = oppName,
                matchedOpponentLevel = oppLevel,
                onlineStatusMessage = "Joined Room #$code! $oppName is ready in duel arena.",
                gameMode = GameMode.PLAY_ONLINE
            )
            attachRoomListener(code, myUid)
            initClassicGame(GameMode.PLAY_ONLINE)
            onJoined()
        }
    }

    fun startQuickOnlineMatch(onMatched: () -> Unit = {}) {
        onlineSearchJob?.cancel()
        val stats = _userStats.value
        val myUid = authManager.currentUser.value?.uid ?: "player_${stats.userName.lowercase().replace(" ", "_")}"

        _classicState.value = _classicState.value.copy(
            isOnlineSearching = true,
            onlineSearchElapsedSec = 0,
            isOnlineBot = false,
            onlineStatusMessage = "Checking online players... 5s ⚡",
            currentUserId = myUid
        )

        onlineSearchJob = viewModelScope.launch {
            val result = firestoreOnlineManager.findOrHostMatchmaking(
                userId = myUid,
                userName = stats.userName,
                userAvatar = stats.userAvatar
            )

            if (result.isSuccess) {
                val (roomId, isHost) = result.getOrThrow()
                _classicState.value = _classicState.value.copy(
                    roomCode = roomId,
                    isHost = isHost
                )
                attachRoomListener(roomId, myUid)

                // Exact 5-Second Real-Player Matchmaking Scan Loop
                val totalSec = 5
                var matchedHuman = false
                for (sec in 1..totalSec) {
                    delay(1000)
                    val currentRoom = _activeDuelRoom.value
                    if (currentRoom != null && (currentRoom.guestId != null || !isHost)) {
                        matchedHuman = true
                        val oppName = if (isHost) (currentRoom.guestName ?: "Challenger") else currentRoom.hostName
                        val oppAvatar = if (isHost) (currentRoom.guestAvatar ?: "⚔️") else currentRoom.hostAvatar
                        val matchedLevel = (stats.level + listOf(-1, 0, 1).random()).coerceIn(1, 10)

                        _classicState.value = _classicState.value.copy(
                            isOnlineSearching = false,
                            isOnlineBot = false,
                            isSimultaneousFriendMode = true,
                            matchedOpponentName = oppName,
                            opponentAvatar = oppAvatar,
                            matchedOpponentLevel = matchedLevel,
                            onlineStatusMessage = "⚔️ Real Player Matched: $oppName! Arena Ready!",
                            gameMode = GameMode.PLAY_ONLINE
                        )
                        initClassicGame(
                            mode = GameMode.PLAY_ONLINE,
                            forcedOpponentName = oppName,
                            forcedOpponentLevel = matchedLevel,
                            forcedOpponentAvatar = oppAvatar
                        )
                        onMatched()
                        break
                    } else {
                        _classicState.value = _classicState.value.copy(
                            onlineSearchElapsedSec = sec,
                            onlineStatusMessage = "Searching online players... (${totalSec - sec}s) ⚡"
                        )
                    }
                }

                if (!matchedHuman) {
                    // No real human player clicked within 5 seconds.
                    // Clean up waiting room in Firestore so queue is neat
                    firestoreOnlineManager.leaveRoom(roomId, myUid)
                    _activeDuelRoom.value = null
                    roomListenerJob?.cancel()

                    // Automatically pair with a realistic computer bot with authentic name, badge & matched level
                    val persona = realisticPersonas.random()
                    val userLevel = stats.level.coerceAtLeast(1)
                    val botLevel = (userLevel + listOf(-1, 0, 1).random()).coerceIn(1, 10)

                    _classicState.value = _classicState.value.copy(
                        isOnlineSearching = false,
                        isOnlineBot = true,
                        isSimultaneousFriendMode = false,
                        matchedOpponentName = persona.name,
                        opponentAvatar = persona.avatar,
                        matchedOpponentLevel = botLevel,
                        onlineStatusMessage = "Matched with ${persona.name} (Lvl $botLevel)! Duel Ready!",
                        gameMode = GameMode.PLAY_ONLINE
                    )
                    initClassicGame(
                        mode = GameMode.PLAY_ONLINE,
                        forcedOpponentName = persona.name,
                        forcedOpponentLevel = botLevel,
                        forcedOpponentAvatar = persona.avatar
                    )
                    onMatched()
                }
            } else {
                // Network or Firestore unavailable: instant pairing with realistic bot
                val persona = realisticPersonas.random()
                val userLevel = stats.level.coerceAtLeast(1)
                val botLevel = (userLevel + listOf(-1, 0, 1).random()).coerceIn(1, 10)

                _classicState.value = _classicState.value.copy(
                    isOnlineSearching = false,
                    isOnlineBot = true,
                    isSimultaneousFriendMode = false,
                    matchedOpponentName = persona.name,
                    opponentAvatar = persona.avatar,
                    matchedOpponentLevel = botLevel,
                    onlineStatusMessage = "Matched with ${persona.name} (Lvl $botLevel)! Ready to play!",
                    gameMode = GameMode.PLAY_ONLINE
                )
                initClassicGame(
                    mode = GameMode.PLAY_ONLINE,
                    forcedOpponentName = persona.name,
                    forcedOpponentLevel = botLevel,
                    forcedOpponentAvatar = persona.avatar
                )
                onMatched()
            }
        }
    }

    fun cancelOnlineSearch() {
        onlineSearchJob?.cancel()
        onlineSearchJob = null
        _classicState.value = _classicState.value.copy(
            isOnlineSearching = false,
            onlineStatusMessage = "Search cancelled."
        )
    }

    fun setChallengeLevel(level: Int) {
        _classicState.value = _classicState.value.copy(challengeLevel = level.coerceIn(1, 5))
    }

    fun addFriend(name: String, level: Int = 1) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        val currentList = _classicState.value.friendsList
        if (currentList.none { it.name.equals(trimmed, ignoreCase = true) }) {
            val newFriend = FriendProfile(
                name = trimmed,
                level = level.coerceIn(1, 5),
                statsDesc = "Lvl $level Mind Challenger"
            )
            _classicState.value = _classicState.value.copy(
                friendsList = currentList + newFriend
            )
        }
    }

    fun updateFriendLevel(name: String, newLevel: Int) {
        val currentList = _classicState.value.friendsList
        val updated = currentList.map {
            if (it.name.equals(name, ignoreCase = true)) {
                it.copy(level = newLevel.coerceIn(1, 5), statsDesc = "Lvl $newLevel Mind Challenger")
            } else it
        }
        _classicState.value = _classicState.value.copy(friendsList = updated)
    }

    fun removeFriend(name: String) {
        val currentList = _classicState.value.friendsList
        _classicState.value = _classicState.value.copy(
            friendsList = currentList.filterNot { it.name.equals(name, ignoreCase = true) }
        )
    }

    /**
     * Sends a real-time 1v1 challenge via Firebase Firestore to a friend or online player.
     * Starts listening to challenge status so user sees PENDING, ACCEPTED, or REJECTED live.
     */
    fun sendFriendChallenge(
        receiverId: String,
        receiverName: String,
        receiverLevel: Int = 1,
        onAcceptedGoToSetup: () -> Unit = {}
    ): String {
        val stats = _userStats.value
        val myUid = authManager.currentUser.value?.uid ?: "player_${stats.userName.lowercase().replace(" ", "_")}"
        val roomCode = generateComplexRoomCode()

        _classicState.value = _classicState.value.copy(
            roomCode = roomCode,
            matchedOpponentName = receiverName,
            matchedOpponentLevel = receiverLevel,
            isHost = true,
            currentUserId = myUid,
            onlineStatusMessage = "⚔️ Challenge Sent to $receiverName! Waiting for response...",
            gameMode = GameMode.PLAY_ONLINE,
            isSimultaneousFriendMode = true
        )

        val tempChallengeId = "chal_${System.currentTimeMillis()}"
        val initialChallenge = FriendChallenge(
            id = tempChallengeId,
            senderId = myUid,
            senderName = stats.userName,
            senderLevel = stats.level,
            senderAvatar = stats.userAvatar,
            receiverId = receiverId,
            receiverName = receiverName,
            roomCode = roomCode,
            status = ChallengeStatus.PENDING,
            statusMessage = "Live duel challenge sent to $receiverName...",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        _outgoingChallenge.value = initialChallenge

        viewModelScope.launch {
            val result = firestoreOnlineManager.sendChallenge(
                senderId = myUid,
                senderName = stats.userName,
                senderLevel = stats.level,
                senderAvatar = stats.userAvatar,
                receiverId = receiverId,
                receiverName = receiverName,
                roomCode = roomCode
            )

            val challengeId = result.getOrNull() ?: tempChallengeId
            _classicState.value = _classicState.value.copy(activeChallengeId = challengeId)
            attachRoomListener(roomCode, myUid)

            // Real-time listener for challenge status changes
            challengeListenerJob?.cancel()
            challengeListenerJob = viewModelScope.launch {
                if (result.isSuccess) {
                    launch {
                        firestoreOnlineManager.listenChallenge(challengeId).collectLatest { challenge ->
                            if (challenge != null) {
                                _outgoingChallenge.value = challenge
                                when (challenge.status) {
                                    ChallengeStatus.ACCEPTED -> {
                                        _classicState.value = _classicState.value.copy(
                                            onlineStatusMessage = "⚡ $receiverName ACCEPTED! Entering Duel Arena..."
                                        )
                                        initClassicGame(
                                            mode = GameMode.PLAY_ONLINE,
                                            forcedOpponentName = receiverName,
                                            forcedOpponentLevel = receiverLevel
                                        )
                                        onAcceptedGoToSetup()
                                    }
                                    ChallengeStatus.REJECTED -> {
                                        _classicState.value = _classicState.value.copy(
                                            onlineStatusMessage = "🛡️ $receiverName declined the challenge."
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return roomCode
    }

    /**
     * Direct 1-tap friend match start (Never gets stuck waiting)
     */
    fun startDirectFriendMatch(
        friendName: String,
        friendLevel: Int = 1,
        onStart: () -> Unit = {}
    ) {
        _outgoingChallenge.value = null
        challengeListenerJob?.cancel()
        val roomCode = generateComplexRoomCode()
        val stats = _userStats.value
        val myUid = authManager.currentUser.value?.uid ?: "player_${stats.userName.lowercase().replace(" ", "_")}"

        _classicState.value = _classicState.value.copy(
            roomCode = roomCode,
            matchedOpponentName = friendName,
            matchedOpponentLevel = friendLevel,
            isHost = true,
            currentUserId = myUid,
            isSimultaneousFriendMode = true,
            gameMode = GameMode.PLAY_ONLINE,
            onlineStatusMessage = "⚡ Duel Arena ready with $friendName!"
        )
        initClassicGame(
            mode = GameMode.PLAY_ONLINE,
            forcedOpponentName = friendName,
            forcedOpponentLevel = friendLevel
        )
        onStart()
    }

    /**
     * Accepts an incoming challenge from a friend.
     */
    fun acceptIncomingChallenge(challenge: FriendChallenge, onAccepted: () -> Unit = {}) {
        val stats = _userStats.value
        val myUid = authManager.currentUser.value?.uid ?: "player_${stats.userName.lowercase().replace(" ", "_")}"

        viewModelScope.launch {
            val result = firestoreOnlineManager.acceptChallenge(
                challengeId = challenge.id,
                roomCode = challenge.roomCode,
                guestId = myUid,
                guestName = stats.userName,
                guestAvatar = stats.userAvatar
            )

            result.onSuccess {
                initClassicGame(GameMode.PLAY_ONLINE)
                _classicState.value = _classicState.value.copy(
                    roomCode = challenge.roomCode,
                    matchedOpponentName = challenge.senderName,
                    matchedOpponentLevel = challenge.senderLevel,
                    opponentAvatar = challenge.senderAvatar,
                    isHost = false,
                    currentUserId = myUid,
                    activeChallengeId = challenge.id,
                    gameMode = GameMode.PLAY_ONLINE,
                    isSimultaneousFriendMode = true,
                    onlineStatusMessage = "⚡ Duel Accepted! Playing against ${challenge.senderName}!"
                )
                attachRoomListener(challenge.roomCode, myUid)
                onAccepted()
            }
        }
    }

    /**
     * Rejects an incoming challenge from a friend.
     */
    fun rejectIncomingChallenge(challenge: FriendChallenge) {
        viewModelScope.launch {
            firestoreOnlineManager.rejectChallenge(challenge.id)
            _incomingChallenges.value = _incomingChallenges.value.filterNot { it.id == challenge.id }
        }
    }

    /**
     * Cancels the active outgoing challenge.
     */
    fun cancelOutgoingChallenge() {
        val active = _outgoingChallenge.value
        if (active != null) {
            viewModelScope.launch {
                firestoreOnlineManager.cancelChallenge(active.id)
            }
        }
        challengeListenerJob?.cancel()
        _outgoingChallenge.value = null
    }

    /**
     * Real-time Firestore room listener for turns, cuts, and winners.
     */
    fun attachRoomListener(roomId: String, myUid: String) {
        roomListenerJob?.cancel()
        roomListenerJob = viewModelScope.launch {
            firestoreOnlineManager.listenRoom(roomId).collectLatest { room ->
                _activeDuelRoom.value = room
                if (room != null) {
                    val oppName = if (room.hostId == myUid) room.guestName else room.hostName
                    if (oppName != null && oppName != _classicState.value.matchedOpponentName) {
                        _classicState.value = _classicState.value.copy(
                            matchedOpponentName = oppName,
                            opponentAvatar = if (room.hostId == myUid) (room.guestAvatar ?: "⚔️") else room.hostAvatar
                        )
                    }

                    // Opponent made a move!
                    val lastCut = room.lastCutNumber
                    val lastCutBy = room.lastCutBy
                    val currentState = _classicState.value
                    if (lastCut != null && lastCutBy != null && lastCutBy != _userStats.value.userName) {
                        if (!currentState.cutHistory.contains(lastCut)) {
                            applyRemoteCut(lastCut, lastCutBy, room)
                        }
                    }

                    // Remote Chat Reaction / Message from human opponent
                    val remoteContent = room.lastReactionContent
                    val remoteSenderId = room.lastReactionSenderId
                    val reactionTime = room.lastReactionTimestamp
                    if (remoteContent != null && remoteSenderId != null && remoteSenderId != myUid && reactionTime > lastReceivedReactionTimestamp) {
                        lastReceivedReactionTimestamp = reactionTime
                        val reaction = ChatReaction(
                            senderName = room.lastReactionSenderName ?: "Opponent",
                            senderAvatar = room.lastReactionSenderAvatar ?: "⚔️",
                            isUser = false,
                            content = remoteContent,
                            timestamp = reactionTime
                        )
                        _liveReactions.value = (_liveReactions.value + reaction).takeLast(8)
                        soundManager.playReaction()
                    }

                    // Match completed
                    if (room.winnerId != null && currentState.matchOutcome == MatchOutcome.IN_PROGRESS) {
                        if (room.winnerId == myUid) {
                            _classicState.value = currentState.copy(
                                matchOutcome = MatchOutcome.PLAYER_WON,
                                coinsEarned = 300,
                                xpEarned = 200
                            )
                            soundManager.playWinFanfare()
                        } else {
                            _classicState.value = currentState.copy(
                                matchOutcome = MatchOutcome.COMPUTER_OR_PLAYER_2_WON,
                                coinsEarned = 50,
                                xpEarned = 50
                            )
                        }
                    }
                }
            }
        }
    }

    private fun applyRemoteCut(number: Int, cutByName: String, room: OnlineDuelRoom) {
        val state = _classicState.value
        val updatedP1 = state.player1Board.crossNumber(number)
        val updatedP2 = state.player2OrComputerBoard.crossNumber(number)
        val updatedHistory = state.cutHistory + number

        soundManager.playCrossSound()
        _classicState.value = state.copy(
            player1Board = updatedP1,
            player2OrComputerBoard = updatedP2,
            lastCutNumber = number,
            lastCutBy = cutByName,
            cutHistory = updatedHistory,
            matchTurn = MatchTurn.PLAYER_1_TURN, // Local player's turn to cut!
            isComputerThinking = false
        )
    }

    fun prepareFriendChallenge(friend: FriendProfile): String {
        return sendFriendChallenge(
            receiverId = "friend_${friend.name.lowercase().replace(" ", "_")}",
            receiverName = friend.name,
            receiverLevel = friend.level
        )
    }

    fun sendUserReaction(content: String) {
        val stats = _userStats.value
        val state = _classicState.value
        val userReaction = ChatReaction(
            senderName = stats.userName,
            senderAvatar = stats.userAvatar,
            isUser = true,
            content = content
        )
        _liveReactions.value = (_liveReactions.value + userReaction).takeLast(8)
        soundManager.playReaction()

        val currentRoom = _activeDuelRoom.value
        if (currentRoom != null && !state.isOnlineBot && state.gameMode == GameMode.PLAY_ONLINE) {
            // Real Human Multiplayer: broadcast reaction live to Firestore!
            viewModelScope.launch {
                firestoreOnlineManager.sendRoomReaction(
                    roomId = currentRoom.roomId,
                    senderId = state.currentUserId,
                    senderName = stats.userName,
                    senderAvatar = stats.userAvatar,
                    content = content
                )
            }
        } else {
            // Opponent is Computer Bot or Online Bot: Generate authentic Hindi/Hinglish gaming reply!
            viewModelScope.launch {
                delay((1000L..2200L).random())
                val currentState = _classicState.value
                val oppName = currentState.matchedOpponentName ?: "Computer AI"
                val oppAvatar = currentState.opponentAvatar

                val replies = when (content) {
                    "🔥" -> listOf("🔥 Let's go!", "😎 Game on!", "💪 Strategy ready!", "🔥 Great energy!")
                    "😂" -> listOf("😂 Haha nice!", "👀 Watch your lines!", "😜 Wait and see!", "🤣 Good one!")
                    "👏" -> listOf("🤝 Thank you!", "😎 Appreciate it!", "✨ Great match!")
                    "😱" -> listOf("😅 Close call!", "😱 Good pick!", "🎯 Watch this turn!")
                    "😎" -> listOf("😏 Let's see who gets BINGO first!", "🔥 Nice move!", "💪 Challenge accepted!")
                    "🎯" -> listOf("👏 Great number!", "🎯 Sharp pick!", "😱 You blocked my line!")
                    "🥳" -> listOf("🎉 GG!", "🥳 Fun match!", "✨ Great game!")
                    "🍀" -> listOf("🍀 Pure skill and luck!", "🤝 Good luck!")
                    "Nice! 👏" -> listOf("🤝 Thanks!", "😎 Strategic move!", "🎯 Well played!")
                    "GG 🤝" -> listOf("🤝 Good Game!", "🎉 Awesome match!", "🔥 Rematch next!")
                    "Rematch? 🔥" -> listOf("🔥 Absolutely!", "👍 Let's play again!", "😎 Always ready!")
                    "Lucky! 🍀" -> listOf("🍀 Pure tactics!", "😄 Let's see the next round!")
                    else -> listOf(
                        "Nice move! 🔥",
                        "Great pick! 😎",
                        "Tough match! 🎯",
                        "Getting close to BINGO! ⚡",
                        "Well played! 🤝",
                        "Awesome cut! 👏",
                        "May the best player win! 🔥"
                    )
                }

                val oppReaction = ChatReaction(
                    senderName = oppName,
                    senderAvatar = oppAvatar,
                    isUser = false,
                    content = replies.random()
                )
                _liveReactions.value = (_liveReactions.value + oppReaction).takeLast(8)
                soundManager.playReaction()
            }
        }
    }

    fun postOpponentReaction(content: String) {
        val state = _classicState.value
        val oppName = state.matchedOpponentName ?: "Opponent"
        val oppAvatar = state.opponentAvatar
        val oppReaction = ChatReaction(
            senderName = oppName,
            senderAvatar = oppAvatar,
            isUser = false,
            content = content
        )
        _liveReactions.value = (_liveReactions.value + oppReaction).takeLast(6)
        soundManager.playReaction()
    }

    fun updateUserProfile(name: String, provider: String, avatar: String) {
        viewModelScope.launch {
            repository.updateUserProfile(name = name, provider = provider, avatar = avatar)
        }
    }

    fun signInWithGoogle(activity: Activity) {
        viewModelScope.launch {
            val result = authManager.signInWithGoogle(activity)
            result.onSuccess { user ->
                repository.updateUserProfile(
                    name = user.displayName,
                    provider = user.provider,
                    avatar = _userStats.value.userAvatar
                )
            }
        }
    }

    fun signInWithFacebook(activity: Activity) {
        viewModelScope.launch {
            val result = authManager.signInWithFacebook(activity)
            result.onSuccess { user ->
                repository.updateUserProfile(
                    name = user.displayName,
                    provider = user.provider,
                    avatar = _userStats.value.userAvatar
                )
            }
        }
    }

    fun signInAsGuest(guestName: String? = null) {
        viewModelScope.launch {
            val result = authManager.signInAsGuest(guestName)
            result.onSuccess { user ->
                repository.updateUserProfile(
                    name = user.displayName,
                    provider = user.provider,
                    avatar = _userStats.value.userAvatar
                )
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authManager.signOut()
            repository.updateUserProfile(
                name = "MindPlayer_${(10..99).random()}",
                provider = "GUEST",
                avatar = _userStats.value.userAvatar
            )
        }
    }

    fun resetAuthState() {
        authManager.resetAuthState()
    }

    fun clearLiveReactions() {
        _liveReactions.value = emptyList()
    }

    // ============================================================================

    override fun onCleared() {
        super.onCleared()
        soundManager.release()
    }
}

