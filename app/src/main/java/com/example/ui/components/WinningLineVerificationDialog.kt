package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.window.DialogProperties
import com.example.model.BingoTheme
import com.example.model.ClassicBingoBoard
import com.example.model.CompletedLineInfo
import com.example.model.GameMode
import com.example.model.MatchOutcome

@Composable
fun WinningLineVerificationDialog(
    player1Board: ClassicBingoBoard,
    player2OrComputerBoard: ClassicBingoBoard,
    gameMode: GameMode,
    matchOutcome: MatchOutcome,
    theme: BingoTheme,
    onDismiss: () -> Unit
) {
    val isFriend = gameMode == GameMode.PLAY_WITH_FRIEND
    val defaultTab = if (matchOutcome == MatchOutcome.COMPUTER_OR_PLAYER_2_WON) 1 else 0
    var selectedTab by remember { mutableStateOf(defaultTab) } // 0 = Player 1/User, 1 = P2/Computer
    var selectedLineInfo by remember { mutableStateOf<CompletedLineInfo?>(null) }

    val activeBoard = if (selectedTab == 0) player1Board else player2OrComputerBoard
    val completedLines = activeBoard.getCompletedLineInfos()
    val activeTitle = when {
        selectedTab == 0 -> if (isFriend) "Player 1" else "Your Board"
        else -> when (gameMode) {
            GameMode.VS_COMPUTER -> "Computer Bot"
            GameMode.PLAY_WITH_FRIEND -> "Player 2"
            GameMode.PLAY_ONLINE -> "Online Opponent"
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.9f))
                .padding(horizontal = 12.dp, vertical = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = theme.cardBgColor),
                shape = RoundedCornerShape(22.dp),
                border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFFFD700)),
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.95f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp)
                ) {
                    // Header Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Winning Line Proof & Verify",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFFFFD700)
                                )
                                Text(
                                    text = "Verify how 5 lines formed B-I-N-G-O",
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.75f)
                                )
                            }
                        }

                        IconButton(onClick = onDismiss) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Player 1 vs Player 2/Computer Board Selector Tabs
                    val tab1Title = if (isFriend) "Player 1 (${player1Board.completedLinesCount}/5)" else "You (${player1Board.completedLinesCount}/5)"
                    val tab2Title = when (gameMode) {
                        GameMode.VS_COMPUTER -> "Computer (${player2OrComputerBoard.completedLinesCount}/5)"
                        GameMode.PLAY_WITH_FRIEND -> "Player 2 (${player2OrComputerBoard.completedLinesCount}/5)"
                        GameMode.PLAY_ONLINE -> "Opponent (${player2OrComputerBoard.completedLinesCount}/5)"
                    }

                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = Color.Black.copy(alpha = 0.3f),
                        contentColor = Color(0xFFFFD700),
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                color = Color(0xFFFFD700),
                                height = 3.dp
                            )
                        },
                        modifier = Modifier.clip(RoundedCornerShape(12.dp))
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = {
                                selectedTab = 0
                                selectedLineInfo = null
                            },
                            text = {
                                Text(
                                    tab1Title,
                                    fontWeight = if (selectedTab == 0) FontWeight.Black else FontWeight.Normal,
                                    color = if (selectedTab == 0) Color(0xFFFFD700) else Color.White.copy(alpha = 0.7f),
                                    fontSize = 13.sp
                                )
                            }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = {
                                selectedTab = 1
                                selectedLineInfo = null
                            },
                            text = {
                                Text(
                                    tab2Title,
                                    fontWeight = if (selectedTab == 1) FontWeight.Black else FontWeight.Normal,
                                    color = if (selectedTab == 1) Color(0xFFFFD700) else Color.White.copy(alpha = 0.7f),
                                    fontSize = 13.sp
                                )
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Status Summary Pill
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (activeBoard.hasBingo) Color(0xFF00E676).copy(alpha = 0.2f)
                                else Color(0xFFFF9100).copy(alpha = 0.2f)
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "$activeTitle: ${activeBoard.completedLinesCount} of 5 Lines Completed",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (activeBoard.hasBingo) Color(0xFF00E676) else Color(0xFFFF9100)
                        )

                        if (selectedLineInfo != null) {
                            Text(
                                text = "Showing: ${selectedLineInfo?.lineStrike?.name}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFD700)
                            )
                        } else {
                            Text(
                                text = "All Lines Highlighted",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Scrollable content: Board & Line Breakdown
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // 1. Interactive 5x5 Board with Highlighting
                        item {
                            val selectedIndices = selectedLineInfo?.lineStrike?.cellIndices?.toSet()
                            ClassicBingoGrid(
                                board = activeBoard,
                                theme = theme,
                                isSetupMode = false,
                                enabled = false,
                                selectedLineIndices = selectedIndices,
                                onCellClick = { _, _, _ -> },
                                modifier = Modifier.testTag("verify_grid_board")
                            )
                        }

                        // 2. Line Filter helper
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Completed Lines (${completedLines.size}):",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFFFFD700)
                                )

                                if (selectedLineInfo != null) {
                                    OutlinedButton(
                                        onClick = { selectedLineInfo = null },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFFD700)),
                                        modifier = Modifier.height(30.dp)
                                    ) {
                                        Text("Show All Lines", fontSize = 11.sp)
                                    }
                                }
                            }
                        }

                        // 3. Breakdown of Each Completed Line
                        if (completedLines.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No lines completed yet on this board.",
                                        fontSize = 13.sp,
                                        color = Color.White.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        } else {
                            itemsIndexed(completedLines) { index, lineInfo ->
                                val isSelected = selectedLineInfo?.lineStrike?.id == lineInfo.lineStrike.id
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) Color(0xFF00E676).copy(alpha = 0.25f)
                                        else Color.White.copy(alpha = 0.08f)
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.5.dp,
                                        if (isSelected) Color(0xFF00E676) else Color(0xFFFFD700).copy(alpha = 0.6f)
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedLineInfo = if (isSelected) null else lineInfo
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Letter Badge: [B], [I], [N], [G], [O]
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFFFD700)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = lineInfo.letter.toString(),
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Black,
                                                color = Color(0xFF10002B)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(10.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = "Line ${index + 1}: ${lineInfo.lineStrike.name}",
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = Color.White
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Icon(
                                                    Icons.Default.CheckCircle,
                                                    contentDescription = null,
                                                    tint = Color(0xFF00E676),
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }

                                            Spacer(modifier = Modifier.height(2.dp))

                                            Text(
                                                text = "Numbers: ${lineInfo.numbers.joinToString(" • ")}",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFFFD700)
                                            )
                                        }

                                        Icon(
                                            Icons.Default.TouchApp,
                                            contentDescription = "Tap to highlight line",
                                            tint = if (isSelected) Color(0xFF00E676) else Color.White.copy(alpha = 0.5f),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Close Button
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                    ) {
                        Text(
                            text = "Back to Match Options",
                            color = Color(0xFF10002B),
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}
