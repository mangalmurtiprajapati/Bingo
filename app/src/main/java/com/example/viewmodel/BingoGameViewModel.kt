package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.BingoDatabase
import com.example.data.BingoRepository
import com.example.model.BingoCard
import com.example.model.BingoTheme
import com.example.model.ClassicBingoBoard
import com.example.model.GameDifficulty
import com.example.model.GameMode
import com.example.model.MatchOutcome
import com.example.model.MatchTurn
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
    val onlineStatusMessage: String = "Ready to play"
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

    private val _userStats = MutableStateFlow(UserStats())
    val userStats: StateFlow<UserStats> = _userStats.asStateFlow()

    private val _gameState = MutableStateFlow(BingoGameState())
    val gameState: StateFlow<BingoGameState> = _gameState.asStateFlow()

    private val _classicState = MutableStateFlow(ClassicMatchState())
    val classicState: StateFlow<ClassicMatchState> = _classicState.asStateFlow()

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
        val computerBoard = if (mode == GameMode.VS_COMPUTER) {
            ClassicBingoBoard.generateRandomBoard()
        } else {
            ClassicBingoBoard()
        }

        _classicState.value = ClassicMatchState(
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

        if (current.gameMode == GameMode.PLAY_WITH_FRIEND && current.currentSetupPlayer == 1) {
            // Move to Player 2's board setup
            _classicState.value = current.copy(
                currentSetupPlayer = 2,
                player2OrComputerBoard = ClassicBingoBoard()
            )
        } else {
            // Start the actual match
            val finalComputerBoard = if (current.gameMode == GameMode.VS_COMPUTER && !current.player2OrComputerBoard.isFull) {
                ClassicBingoBoard.generateRandomBoard()
            } else {
                current.player2OrComputerBoard
            }

            _classicState.value = current.copy(
                isSetupComplete = true,
                player2OrComputerBoard = finalComputerBoard,
                matchTurn = MatchTurn.PLAYER_1_TURN,
                viewingBoardPlayer = 1,
                matchOutcome = MatchOutcome.IN_PROGRESS
            )
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
            if (state.gameMode == GameMode.PLAY_WITH_FRIEND) "Player 2" else "Computer"
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
                coinsEarned = 100,
                xpEarned = 50
            )
            soundManager.stopAmbientMusic()
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
                matchOutcome = MatchOutcome.COMPUTER_OR_PLAYER_2_WON
            )
            soundManager.stopAmbientMusic()
            viewModelScope.launch {
                repository.recordGameFinished(
                    isWin = false,
                    daubsMade = updatedP1Board.crossedNumbers.size,
                    coinsEarned = 25
                )
            }
            return
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
            // Online mode
            _classicState.value = state.copy(
                player1Board = updatedP1Board,
                player2OrComputerBoard = updatedP2Board,
                lastCutNumber = number,
                lastCutBy = cutByName,
                cutHistory = updatedHistory,
                matchTurn = MatchTurn.PLAYER_2_OR_COMPUTER_TURN,
                isComputerThinking = true
            )
            triggerComputerTurn() // Online simulation with AI opponent
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

            if (p1Won && p2Won) {
                _classicState.value = currentState.copy(
                    player1Board = updatedP1,
                    player2OrComputerBoard = updatedP2,
                    lastCutNumber = bestPick,
                    lastCutBy = "Computer",
                    cutHistory = updatedHistory,
                    isComputerThinking = false,
                    matchOutcome = MatchOutcome.DRAW,
                    coinsEarned = 100
                )
                soundManager.stopAmbientMusic()
            } else if (p2Won) {
                _classicState.value = currentState.copy(
                    player1Board = updatedP1,
                    player2OrComputerBoard = updatedP2,
                    lastCutNumber = bestPick,
                    lastCutBy = "Computer",
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
                    lastCutBy = "Computer",
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
                    lastCutBy = "Computer",
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

    // Online Lobby simulations
    fun generateNewOnlineRoom() {
        val code = (100000..999999).random().toString()
        _classicState.value = _classicState.value.copy(
            roomCode = code,
            isOnlineSearching = false,
            onlineStatusMessage = "Room #$code Created! Share code with your friend."
        )
    }

    fun joinOnlineRoom(code: String) {
        _classicState.value = _classicState.value.copy(
            roomCode = code,
            isOnlineSearching = false,
            onlineStatusMessage = "Joined Room #$code! Opponent connected."
        )
    }

    fun startQuickOnlineMatch() {
        _classicState.value = _classicState.value.copy(
            isOnlineSearching = true,
            onlineStatusMessage = "Finding online player..."
        )
        viewModelScope.launch {
            delay(1500)
            _classicState.value = _classicState.value.copy(
                isOnlineSearching = false,
                onlineStatusMessage = "Opponent found! Setting up board..."
            )
        }
    }

    // ============================================================================

    override fun onCleared() {
        super.onCleared()
        soundManager.release()
    }
}

