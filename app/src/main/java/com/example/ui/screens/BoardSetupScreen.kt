package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BingoTheme
import com.example.model.ClassicBingoBoard
import com.example.model.GameMode
import com.example.ui.components.ClassicBingoGrid
import com.example.viewmodel.ClassicMatchState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoardSetupScreen(
    state: ClassicMatchState,
    theme: BingoTheme,
    isSoundMuted: Boolean = false,
    onToggleSound: () -> Unit = {},
    onAssignCell: (Int, Int) -> Unit,
    onAutoFill: () -> Unit,
    onClear: () -> Unit,
    onProceed: () -> Unit,
    onBack: () -> Unit
) {
    val currentBoard = if (state.currentSetupPlayer == 1) state.player1Board else state.player2OrComputerBoard
    val isFriendMode = state.gameMode == GameMode.PLAY_WITH_FRIEND
    val playerTitle = when {
        isFriendMode && state.currentSetupPlayer == 1 -> "Player 1 Setup"
        isFriendMode && state.currentSetupPlayer == 2 -> "Player 2 Setup"
        state.gameMode == GameMode.VS_COMPUTER -> "Setup vs Computer"
        else -> "Setup Online Board"
    }

    val isBoardComplete = currentBoard.isFull

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = playerTitle,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                        Text(
                            text = "Fill 1 to 25 in 25 boxes",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.75f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
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
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Info / Progress Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = theme.cardBgColor.copy(alpha = 0.92f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f, fill = false)
                            ) {
                                Icon(
                                    Icons.Default.TouchApp,
                                    contentDescription = null,
                                    tint = theme.accentColor,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (!isBoardComplete) {
                                        "Next: #${currentBoard.nextNumberToPlace}"
                                    } else {
                                        "Ready to Play!"
                                    },
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.sp,
                                    color = if (isBoardComplete) Color(0xFF00E676) else theme.accentColor,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Text(
                                text = "${currentBoard.numbersPlacedCount} / 25 Filled",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = theme.textPrimary
                            )
                        }

                        LinearProgressIndicator(
                            progress = { currentBoard.numbersPlacedCount / 25f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = if (isBoardComplete) Color(0xFF00E676) else theme.primaryColor,
                            trackColor = theme.primaryColor.copy(alpha = 0.2f)
                        )

                        Text(
                            text = if (!isBoardComplete) {
                                "Tap any box to place #${currentBoard.nextNumberToPlace}. Tap a filled box to remove it."
                            } else {
                                "All 25 boxes filled! You can now start the match."
                            },
                            fontSize = 12.sp,
                            color = theme.textPrimary.copy(alpha = 0.8f),
                            textAlign = TextAlign.Start
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // The 5x5 Grid
                ClassicBingoGrid(
                    board = currentBoard,
                    theme = theme,
                    isSetupMode = true,
                    enabled = true,
                    onCellClick = { r, c, _ ->
                        onAssignCell(r, c)
                    }
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Action buttons: Auto-Fill & Clear
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onAutoFill,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("auto_fill_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = theme.primaryColor
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Auto Fill", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    OutlinedButton(
                        onClick = onClear,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("clear_board_button"),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Clear All", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Main "Start Match" or "Next Player" Button
                Button(
                    onClick = onProceed,
                    enabled = isBoardComplete,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("start_match_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFD700),
                        contentColor = Color(0xFF1A0033),
                        disabledContainerColor = Color.White.copy(alpha = 0.2f),
                        disabledContentColor = Color.White.copy(alpha = 0.4f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when {
                            isFriendMode && state.currentSetupPlayer == 1 -> "NEXT: PLAYER 2 BOARD"
                            else -> "START MATCH"
                        },
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
