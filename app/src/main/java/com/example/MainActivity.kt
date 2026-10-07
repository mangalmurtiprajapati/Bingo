package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.model.BingoTheme
import com.example.model.GameMode
import com.example.ui.screens.AchievementsScreen
import com.example.ui.screens.BingoGameScreen
import com.example.ui.screens.BoardSetupScreen
import com.example.ui.screens.ClassicBingoGameScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.HowToPlayScreen
import com.example.ui.screens.OnlineLobbyScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SpinWheelScreen
import com.example.ui.screens.StatsScreen
import com.example.ui.screens.ThemesScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.BingoGameViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding()
                        .imePadding()
                ) {
                    KidsBingoApp()
                }
            }
        }
    }
}

@Composable
fun KidsBingoApp(
    viewModel: BingoGameViewModel = viewModel()
) {
    val navController = rememberNavController()
    val stats by viewModel.userStats.collectAsState()
    val gameState by viewModel.gameState.collectAsState()
    val classicState by viewModel.classicState.collectAsState()
    val achievements by viewModel.repository.achievements.collectAsState(initial = emptyList())

    val activeTheme = BingoTheme.getTheme(stats.selectedThemeId)

    LaunchedEffect(stats.isMusicEnabled) {
        if (stats.isMusicEnabled) {
            viewModel.soundManager.startAmbientGameMusic()
        } else {
            viewModel.soundManager.stopAmbientMusic()
        }
    }

    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomeScreen(
                stats = stats,
                activeTheme = activeTheme,
                onToggleSound = { viewModel.toggleSound() },
                onStartClassicGame = { mode ->
                    viewModel.initClassicGame(mode)
                    if (mode == GameMode.PLAY_ONLINE) {
                        navController.navigate("online_lobby")
                    } else {
                        navController.navigate("classic_setup")
                    }
                },
                onStartGame = { cards, diff ->
                    viewModel.startNewGame(cardCount = cards, difficulty = diff)
                    navController.navigate("game")
                },
                onNavigateSpinWheel = { navController.navigate("spin_wheel") },
                onNavigateThemes = { navController.navigate("themes") },
                onNavigateAchievements = { navController.navigate("achievements") },
                onNavigateStats = { navController.navigate("stats") },
                onNavigateHowToPlay = { navController.navigate("how_to_play") },
                onNavigateSettings = { navController.navigate("settings") },
                onExitApp = { }
            )
        }

        composable("classic_setup") {
            BoardSetupScreen(
                state = classicState,
                theme = activeTheme,
                isSoundMuted = !stats.isSoundEnabled,
                onToggleSound = { viewModel.toggleSound() },
                onAssignCell = { r, c ->
                    viewModel.assignPlayerCell(r, c)
                },
                onAutoFill = {
                    viewModel.autoFillCurrentBoard()
                },
                onClear = {
                    viewModel.clearCurrentBoard()
                },
                onProceed = {
                    viewModel.proceedToNextSetupOrStartMatch()
                    if (viewModel.classicState.value.isSetupComplete) {
                        navController.navigate("classic_game") {
                            popUpTo("classic_setup") { inclusive = true }
                        }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable("classic_game") {
            ClassicBingoGameScreen(
                state = classicState,
                theme = activeTheme,
                isSoundMuted = !stats.isSoundEnabled,
                onToggleSound = { viewModel.toggleSound() },
                onCutNumber = { number ->
                    viewModel.cutNumber(number)
                },
                onRematch = {
                    viewModel.rematchClassicGame()
                    navController.navigate("classic_setup") {
                        popUpTo("classic_game") { inclusive = true }
                    }
                },
                onSwitchBoard = { player ->
                    viewModel.switchViewingBoard(player)
                },
                onBackToHome = {
                    viewModel.stopGame()
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }

        composable("online_lobby") {
            OnlineLobbyScreen(
                state = classicState,
                theme = activeTheme,
                onCreateRoom = { viewModel.generateNewOnlineRoom() },
                onJoinRoom = { code ->
                    viewModel.joinOnlineRoom(code) {
                        navController.navigate("classic_setup")
                    }
                },
                onQuickMatch = {
                    viewModel.startQuickOnlineMatch {
                        navController.navigate("classic_setup")
                    }
                },
                onCancelSearch = { viewModel.cancelOnlineSearch() },
                onSetChallengeLevel = { level -> viewModel.setChallengeLevel(level) },
                onAddFriend = { name, level -> viewModel.addFriend(name, level) },
                onUpdateFriendLevel = { name, level -> viewModel.updateFriendLevel(name, level) },
                onChallengeFriend = { friend ->
                    viewModel.prepareFriendChallenge(friend)
                },
                onProceedToSetup = {
                    navController.navigate("classic_setup")
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable("game") {
            BingoGameScreen(
                state = gameState,
                theme = activeTheme,
                onCellTapped = { cardId, number ->
                    viewModel.onCellTapped(cardId, number)
                },
                onDeclareBingo = {
                    viewModel.declareBingoManual()
                },
                onToggleAutoDaub = {
                    viewModel.toggleAutoDaub()
                },
                onReplayGame = {
                    viewModel.startNewGame(gameState.cardCount, gameState.difficulty)
                },
                onBackToHome = {
                    viewModel.stopGame()
                    navController.popBackStack()
                }
            )
        }

        composable("spin_wheel") {
            SpinWheelScreen(
                stats = stats,
                activeTheme = activeTheme,
                soundManager = viewModel.soundManager,
                onRewardEarned = { coins ->
                    viewModel.claimWheelReward(coins)
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable("themes") {
            ThemesScreen(
                stats = stats,
                activeTheme = activeTheme,
                onSelectTheme = { themeId ->
                    viewModel.selectTheme(themeId)
                },
                onUnlockTheme = { themeId, cost ->
                    viewModel.unlockTheme(themeId, cost)
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable("achievements") {
            AchievementsScreen(
                achievements = achievements,
                stats = stats,
                activeTheme = activeTheme,
                onClaimReward = { id, coins, xp ->
                    viewModel.claimAchievement(id, coins, xp)
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable("stats") {
            StatsScreen(
                stats = stats,
                activeTheme = activeTheme,
                onBack = { navController.popBackStack() }
            )
        }

        composable("how_to_play") {
            HowToPlayScreen(
                activeTheme = activeTheme,
                onBack = { navController.popBackStack() }
            )
        }

        composable("settings") {
            SettingsScreen(
                stats = stats,
                activeTheme = activeTheme,
                onToggleSound = { enabled ->
                    viewModel.toggleSound(enabled)
                },
                onToggleMusic = { enabled ->
                    viewModel.toggleMusic(enabled)
                },
                onResetData = {
                    viewModel.resetAllProgress()
                },
                onBack = { navController.popBackStack() }
            )
        }
    }
}
