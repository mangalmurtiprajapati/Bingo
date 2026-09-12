package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.foundation.clickable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.BingoTheme
import com.example.model.GameMode
import com.example.model.MatchOutcome
import com.example.model.MatchTurn
import com.example.ui.components.BingoLettersBar
import com.example.ui.components.ClassicBingoGrid
import com.example.ui.components.WinningLineVerificationDialog
import com.example.viewmodel.ClassicMatchState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassicBingoGameScreen(
    state: ClassicMatchState,
    theme: BingoTheme,
    isSoundMuted: Boolean = false,
    onToggleSound: () -> Unit = {},
    onCutNumber: (Int) -> Unit,
    onRematch: () -> Unit,
    onSwitchBoard: (Int) -> Unit,
    onBackToHome: () -> Unit
) {
    var showVerificationDialog by remember { mutableStateOf(false) }

    val isFriendMode = state.gameMode == GameMode.PLAY_WITH_FRIEND
    val viewingBoard = if (isFriendMode && state.viewingBoardPlayer == 2) {
        state.player2OrComputerBoard
    } else {
        state.player1Board
    }

    val isUserTurn = if (isFriendMode) {
        (state.matchTurn == MatchTurn.PLAYER_1_TURN && state.viewingBoardPlayer == 1) ||
                (state.matchTurn == MatchTurn.PLAYER_2_OR_COMPUTER_TURN && state.viewingBoardPlayer == 2)
    } else {
        state.matchTurn == MatchTurn.PLAYER_1_TURN && !state.isComputerThinking
    }

    val opponentBoard = if (isFriendMode) {
        if (state.viewingBoardPlayer == 1) state.player2OrComputerBoard else state.player1Board
    } else {
        state.player2OrComputerBoard
    }

    val opponentTitle = when {
        isFriendMode -> if (state.viewingBoardPlayer == 1) "Player 2" else "Player 1"
        state.gameMode == GameMode.VS_COMPUTER -> "Computer Bot"
        else -> "Online Opponent"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = when (state.gameMode) {
                                GameMode.VS_COMPUTER -> "You vs Computer AI"
                                GameMode.PLAY_WITH_FRIEND -> "Player 1 vs Player 2"
                                GameMode.PLAY_ONLINE -> "Online Match"
                            },
                            fontWeight = FontWeight.Black,
                            fontSize = 17.sp,
                            color = Color.White
                        )
                        Text(
                            text = "First to complete 5 lines wins BINGO!",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.75f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackToHome) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Exit Game",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onToggleSound) {
                        Icon(
                            imageVector = if (isSoundMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = if (isSoundMuted) "Unmute Audio" else "Mute Audio",
                            tint = Color.White
                        )
                    }

                    if (state.matchOutcome != MatchOutcome.IN_PROGRESS) {
                        IconButton(onClick = { showVerificationDialog = true }) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = "Verify Lines",
                                tint = Color(0xFFFFD700)
                            )
                        }
                    }

                    if (isFriendMode) {
                        OutlinedButton(
                            onClick = {
                                onSwitchBoard(if (state.viewingBoardPlayer == 1) 2 else 1)
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("P${state.viewingBoardPlayer}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = theme.headerBgGradients.first()
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Brush.verticalGradient(theme.backgroundGradients))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 1. Player's Live B-I-N-G-O Letters Bar
                val barTitle = if (isFriendMode) "PLAYER ${state.viewingBoardPlayer}'S B-I-N-G-O" else "YOUR B-I-N-G-O"
                BingoLettersBar(
                    completedLinesCount = viewingBoard.completedLinesCount,
                    theme = theme,
                    title = barTitle
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 2. Opponent Status Indicator & Turn Indicator
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = theme.cardBgColor.copy(alpha = 0.9f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.SmartToy,
                                    contentDescription = null,
                                    tint = theme.accentColor,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "$opponentTitle Progress:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = theme.textPrimary
                                )
                            }

                            // Opponent mini lines count badge
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${opponentBoard.completedLinesCount} / 5 Lines",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (opponentBoard.hasBingo) Color(0xFFFF1744) else theme.accentColor
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                // Mini strike letters for opponent
                                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                    val letters = listOf('B', 'I', 'N', 'G', 'O')
                                    letters.forEachIndexed { index, char ->
                                        val isCut = index < opponentBoard.bingoLettersStruck
                                        Box(
                                            modifier = Modifier
                                                .size(18.dp)
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(if (isCut) Color(0xFFFFD700) else theme.cellDefaultBg),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = char.toString(),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Black,
                                                color = if (isCut) Color(0xFF10002B) else Color.White.copy(alpha = 0.6f)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Turn notification banner
                        val turnBannerBg by animateColorAsState(
                            targetValue = if (state.isComputerThinking) {
                                Color(0xFF4A148C).copy(alpha = 0.7f)
                            } else if (isUserTurn) {
                                Color(0xFF00E676).copy(alpha = 0.25f)
                            } else {
                                Color(0xFFFF9100).copy(alpha = 0.25f)
                            },
                            label = "turnBannerBg"
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(turnBannerBg)
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (state.isComputerThinking) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "🤖 Computer is picking a number...",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                } else if (isUserTurn) {
                                    Icon(
                                        Icons.Default.TouchApp,
                                        contentDescription = null,
                                        tint = Color(0xFF00E676),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isFriendMode) "Player ${state.viewingBoardPlayer}'s Turn: Tap a number to cut!" else "👉 Your Turn: Tap a number to cut!",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF00E676)
                                    )
                                } else {
                                    Icon(
                                        Icons.Default.HourglassTop,
                                        contentDescription = null,
                                        tint = Color(0xFFFF9100),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Waiting for Opponent's Turn...",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFF9100)
                                    )
                                }
                            }

                            if (state.lastCutNumber != null) {
                                Text(
                                    text = "Last: #${state.lastCutNumber} (${state.lastCutBy})",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = theme.textPrimary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 3. The 5x5 Front-End Board
                ClassicBingoGrid(
                    board = viewingBoard,
                    theme = theme,
                    isSetupMode = false,
                    enabled = isUserTurn && state.matchOutcome == MatchOutcome.IN_PROGRESS,
                    lastCutNumber = state.lastCutNumber,
                    onCellClick = { _, _, number ->
                        if (number != null && !viewingBoard.isNumberCrossed(number)) {
                            onCutNumber(number)
                        }
                    },
                    modifier = Modifier.testTag("game_board_grid")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // 4. Cut History row
                if (state.cutHistory.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Cut Numbers (${state.cutHistory.size}):",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(state.cutHistory.reversed()) { cutNum ->
                                Box(
                                    modifier = Modifier
                                        .size(26.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (cutNum == state.lastCutNumber) Color(0xFFFF4081)
                                            else Color.White.copy(alpha = 0.2f)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = cutNum.toString(),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // 5. Match Outcome Celebration Overlay
    if (state.matchOutcome != MatchOutcome.IN_PROGRESS) {
        Dialog(onDismissRequest = { /* Require action button click */ }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = theme.cardBgColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.5.dp, Color(0xFFFFD700), RoundedCornerShape(24.dp))
                    .padding(4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    when (state.matchOutcome) {
                        MatchOutcome.PLAYER_WON -> {
                            Icon(
                                Icons.Default.Celebration,
                                contentDescription = null,
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(64.dp)
                            )
                            Text(
                                text = "🎉 B-I-N-G-O! 🎉",
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFFFD700),
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = if (isFriendMode) "Player 1 Completed 5 Lines First!" else "YOU WON THE MATCH!",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "+${state.coinsEarned} Coins  •  +${state.xpEarned} XP Earned!",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00E676)
                            )
                        }
                        MatchOutcome.COMPUTER_OR_PLAYER_2_WON -> {
                            Icon(
                                Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = Color(0xFFFF5252),
                                modifier = Modifier.size(60.dp)
                            )
                            Text(
                                text = if (isFriendMode) "Player 2 Won!" else "Computer Got BINGO!",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFFF5252),
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = if (isFriendMode) "Player 2 completed 5 lines first!" else "Computer completed 5 lines first. Better luck next time!",
                                fontSize = 14.sp,
                                color = Color.White.copy(alpha = 0.85f),
                                textAlign = TextAlign.Center
                            )
                        }
                        MatchOutcome.DRAW -> {
                            Text(
                                text = "🤝 IT'S A TIE!",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFFFD700),
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "Both boards completed 5 lines at the same time!",
                                fontSize = 14.sp,
                                color = Color.White.copy(alpha = 0.85f),
                                textAlign = TextAlign.Center
                            )
                        }
                        else -> Unit
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = { showVerificationDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("verify_lines_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF002233))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "🔍 Verify 5 Lines (Check Kaise Bani)",
                            color = Color(0xFF002233),
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp
                        )
                    }

                    Button(
                        onClick = onRematch,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("play_again_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = Color(0xFF1A0033))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Play Again / Rematch",
                            color = Color(0xFF1A0033),
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp
                        )
                    }

                    OutlinedButton(
                        onClick = onBackToHome,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("home_from_game_button"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Back to Main Menu", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }
    }

    // 6. Winning Line Verification Proof Inspector
    if (showVerificationDialog) {
        WinningLineVerificationDialog(
            player1Board = state.player1Board,
            player2OrComputerBoard = state.player2OrComputerBoard,
            gameMode = state.gameMode,
            matchOutcome = state.matchOutcome,
            theme = theme,
            onDismiss = { showVerificationDialog = false }
        )
    }
}
