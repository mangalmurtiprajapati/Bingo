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
    val roomCode: String = "742918",
    val isOnlineSearching: Boolean = false,
    val onlineSearchElapsedSec: Int = 0,
    val onlineStatusMessage: String = "Ready to play",
    val matchedOpponentName: String? = null,
    val matchedOpponentLevel: Int = 1,
    val opponentAvatar: String = "😎",
    val isOpponentOnline: Boolean = true,
    val opponentSetupProgress: Int = 18,
    val isSimultaneousFriendMode: Boolean = false,
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

    // ==================== CLASSIC 5x5 (1 to 25) BINGO LOGIC ====================

    fun initClassicGame(mode: GameMode) {
        computerTurnJob?.cancel()
        val current = _classicState.value
        val computerBoard = if (mode == GameMode.VS_COMPUTER || mode == GameMode.PLAY_ONLINE) {
            ClassicBingoBoard.generateRandomBoard()
        } else {
            ClassicBingoBoard()
        }

        _classicState.value = current.copy(
            gameMode = mode,
            isSetupComplete = false,
            currentSetupPlayer = 1,
            player1Board = ClassicBingoBoard(),
            player2OrComputerBoard = computerBoard,
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

            if (currentRoom != null) {
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
            } else {
                triggerComputerTurn() // Fallback simulation
            }
        }
    }

    private fun triggerComputerTurn() {
        computerTurnJob?.cancel()
        computerTurnJob = viewModelScope.launch {
            delay(1100) // Realistic bot think delay

            val currentState = _classicState.value
            if (currentState.matchOutcome != MatchOutcome.IN_PROGRESS) return@launch

            val remainingNumbers = (1..25).filter { !currentState.player2OrComputerBoard.isNumberCrossed(it) }
            if (remainingNumbers.isEmpty()) return@launch

            // Strategic AI decision: Pick the number that maximizes line completions on Computer's board
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

            val opponentName = if (currentState.gameMode == GameMode.PLAY_ONLINE) {
                currentState.matchedOpponentName ?: "Online Opponent"
            } else {
                "Computer"
            }

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
        val code = (100000..999999).random().toString()
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
            onlineStatusMessage = "Connecting to Firebase Matchmaking Queue... 🌐",
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

                // Matchmaking scan loop: checks Firestore room status
                val totalSec = 7
                var matched = false
                for (sec in 1..totalSec) {
                    delay(1000)
                    val currentRoom = _activeDuelRoom.value
                    if (currentRoom != null && (currentRoom.guestId != null || !isHost)) {
                        matched = true
                        val oppName = if (isHost) (currentRoom.guestName ?: "Challenger") else currentRoom.hostName
                        _classicState.value = _classicState.value.copy(
                            isOnlineSearching = false,
                            matchedOpponentName = oppName,
                            matchedOpponentLevel = (1..5).random(),
                            onlineStatusMessage = "⚔️ Matched with $oppName in Firebase! Arena Ready!",
                            gameMode = GameMode.PLAY_ONLINE
                        )
                        initClassicGame(GameMode.PLAY_ONLINE)
                        onMatched()
                        break
                    } else {
                        _classicState.value = _classicState.value.copy(
                            onlineSearchElapsedSec = sec,
                            onlineStatusMessage = "Searching online challengers across Firebase... (${totalSec - sec}s)"
                        )
                    }
                }

                if (!matched) {
                    // Fast pairing with active player pool from Firestore or system challenger
                    val onlinePool = _onlinePlayers.value.map { it.name }.filter { it.isNotBlank() }
                    val fallbackPool = listOf("Alex_Mind", "Kavya_Star", "Leo_Thinker", "Zoya_Gamer", "Kabir_Ace", "Aarav_Pro")
                    val chosenName = (onlinePool + fallbackPool).random()
                    val chosenLevel = (1..5).random()
                    _classicState.value = _classicState.value.copy(
                        isOnlineSearching = false,
                        matchedOpponentName = chosenName,
                        matchedOpponentLevel = chosenLevel,
                        onlineStatusMessage = "⚔️ Matched with $chosenName (Lvl $chosenLevel)! Arena Ready!",
                        gameMode = GameMode.PLAY_ONLINE
                    )
                    initClassicGame(GameMode.PLAY_ONLINE)
                    onMatched()
                }
            } else {
                delay(3000)
                val fallbackOpponent = "Alex_Mind"
                _classicState.value = _classicState.value.copy(
                    isOnlineSearching = false,
                    matchedOpponentName = fallbackOpponent,
                    matchedOpponentLevel = 2,
                    onlineStatusMessage = "Matched with $fallbackOpponent! Ready to play!",
                    gameMode = GameMode.PLAY_ONLINE
                )
                initClassicGame(GameMode.PLAY_ONLINE)
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
        val roomCode = (100000..999999).random().toString()

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

            result.onSuccess { challengeId ->
                _classicState.value = _classicState.value.copy(activeChallengeId = challengeId)
                attachRoomListener(roomCode, myUid)

                // Real-time listener for challenge status changes
                challengeListenerJob?.cancel()
                challengeListenerJob = viewModelScope.launch {
                    firestoreOnlineManager.listenChallenge(challengeId).collectLatest { challenge ->
                        _outgoingChallenge.value = challenge
                        if (challenge != null) {
                            when (challenge.status) {
                                ChallengeStatus.ACCEPTED -> {
                                    _classicState.value = _classicState.value.copy(
                                        onlineStatusMessage = "⚡ $receiverName ACCEPTED YOUR CHALLENGE! Entering Duel Arena..."
                                    )
                                    initClassicGame(GameMode.PLAY_ONLINE)
                                    onAcceptedGoToSetup()
                                }
                                ChallengeStatus.REJECTED -> {
                                    _classicState.value = _classicState.value.copy(
                                        onlineStatusMessage = "🛡️ $receiverName declined the challenge. Opponent retreated."
                                    )
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
        val userReaction = ChatReaction(
            senderName = stats.userName,
            senderAvatar = stats.userAvatar,
            isUser = true,
            content = content
        )
        _liveReactions.value = (_liveReactions.value + userReaction).takeLast(6)
        soundManager.playReaction()

        // Trigger realistic simulated opponent response in online or bot matches
        viewModelScope.launch {
            delay((1000L..2200L).random())
            val state = _classicState.value
            val oppName = state.matchedOpponentName ?: if (state.gameMode == GameMode.VS_COMPUTER) "Computer AI" else "Opponent"
            val oppAvatar = state.opponentAvatar

            val replies = when (content) {
                "🔥" -> listOf("👏", "😎 Game on!", "🔥 Let's go!", "💪 Intense!")
                "😂" -> listOf("😂 Haha!", "👀 Focus on lines!", "😜", "🤣")
                "👏" -> listOf("🤝 Thank you!", "😎 Appreciate it!", "✨ Keep playing!")
                "😱" -> listOf("😅 Close call!", "😱 Tension high!", "🎯 My turn next!")
                "😎" -> listOf("😏 We will see!", "🔥 Don't celebrate yet!", "💪 Challenge accepted!")
                "🎯" -> listOf("👏 Great pick!", "🎯 Good eye!", "😱 Watch my strike!")
                "🥳" -> listOf("🎉 GG!", "🥳 Having fun!", "✨ Great game!")
                "🍀" -> listOf("🍀 May the best board win!", "🤝 Luck to you too!")
                "Good luck! 🍀" -> listOf("🍀 Good luck to you too!", "🤝 Let's play a great match!", "⚡ Best of luck!")
                "Almost got BINGO! 😱" -> listOf("😱 Not if I get 5 lines first!", "👀 So close!", "🔥 Watch out for mine!")
                "Nice cut! 👏" -> listOf("🤝 Thanks, buddy!", "😎 Saw that coming!", "🎯 Strategic move!")
                "Watch this! 😎" -> listOf("👀 Show me!", "🔥 Bring it on!", "😏 Let's see!")
                "GG (Good Game) 🤝" -> listOf("🤝 Well played!", "🎉 Awesome match!", "🔥 Rematch next!")
                "Rematch? 🔥" -> listOf("🔥 Absolutely!", "👍 Let's play again!", "😎 Ready when you are!")
                else -> listOf("👍", "😎", "🔥", "🤝", "🎯")
            }

            val oppReaction = ChatReaction(
                senderName = oppName,
                senderAvatar = oppAvatar,
                isUser = false,
                content = replies.random()
            )
            _liveReactions.value = (_liveReactions.value + oppReaction).takeLast(6)
            soundManager.playReaction()
        }
    }

    fun postOpponentReaction(content: String) {
        val state = _classicState.value
        val oppName = state.matchedOpponentName ?: if (state.gameMode == GameMode.VS_COMPUTER) "Computer AI" else "Opponent"
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

