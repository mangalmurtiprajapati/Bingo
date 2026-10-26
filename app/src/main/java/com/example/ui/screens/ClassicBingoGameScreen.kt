package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.BingoTheme
import com.example.model.ChatReaction
import com.example.model.GameMode
import com.example.model.MatchOutcome
import com.example.model.MatchTurn
import com.example.ui.components.ArcadeTopHeader
import com.example.ui.theme.ArcadeBackground
import com.example.ui.theme.ArcadeInverseOnSurface
import com.example.ui.theme.ArcadeInverseSurface
import com.example.ui.theme.ArcadeOnPrimary
import com.example.ui.theme.ArcadeOnSecondary
import com.example.ui.theme.ArcadeOnSurface
import com.example.ui.theme.ArcadeOnSurfaceVariant
import com.example.ui.theme.ArcadeOutline
import com.example.ui.theme.ArcadeOutlineVariant
import com.example.ui.theme.ArcadePrimary
import com.example.ui.theme.ArcadePrimaryContainer
import com.example.ui.theme.ArcadePrimaryFixed
import com.example.ui.theme.ArcadeSecondary
import com.example.ui.theme.ArcadeSecondaryContainer
import com.example.ui.theme.ArcadeSecondaryFixed
import com.example.ui.theme.ArcadeSurfaceContainer
import com.example.ui.theme.ArcadeSurfaceContainerHigh
import com.example.ui.theme.ArcadeSurfaceContainerHighest
import com.example.ui.theme.ArcadeSurfaceContainerLowest
import com.example.ui.theme.ArcadeTertiary
import com.example.ui.theme.ArcadeTertiaryContainer
import com.example.ui.theme.EmeraldWin
import com.example.ui.theme.RedPenStrike
import com.example.ui.theme.TournamentGold
import com.example.viewmodel.ClassicMatchState
import kotlinx.coroutines.delay

@Composable
fun ClassicBingoGameScreen(
    state: ClassicMatchState,
    theme: BingoTheme,
    isSoundMuted: Boolean = false,
    onToggleSound: () -> Unit = {},
    reactions: List<ChatReaction> = emptyList(),
    onSendReaction: (String) -> Unit = {},
    onCutNumber: (Int) -> Unit,
    onRematch: () -> Unit,
    onSwitchBoard: (Int) -> Unit,
    onBackToHome: () -> Unit
) {
    BackHandler { onBackToHome() }

    var showSurrenderDialog by remember { mutableStateOf(false) }
    var turnTimerSeconds by remember { mutableIntStateOf(15) }

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

    val p1Lines = state.player1Board.completedLinesCount
    val p2Lines = state.player2OrComputerBoard.completedLinesCount
    val opponentName = state.matchedOpponentName ?: if (state.gameMode == GameMode.VS_COMPUTER) "Bot Blitz" else "Opponent"

    // Turn timer countdown
    LaunchedEffect(state.matchTurn, state.isComputerThinking) {
        turnTimerSeconds = 15
        while (turnTimerSeconds > 0 && state.matchOutcome == MatchOutcome.IN_PROGRESS) {
            delay(1000L)
            turnTimerSeconds--
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = ArcadeBackground,
        topBar = {
            ArcadeTopHeader(
                title = "1v1 Duel",
                subtitle = "Ranked Match",
                onBack = { showSurrenderDialog = true },
                onSoundToggle = onToggleSound,
                isSoundMuted = isSoundMuted
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 560.dp)
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // 1. Arena Context Bar
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = ArcadeSurfaceContainer)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(ArcadeSecondaryContainer)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "1V1 DUEL ARENA • RANKED",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = ArcadePrimary,
                                letterSpacing = 0.5.sp
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Turn timer capsule
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(ArcadeSurfaceContainerHighest)
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = null,
                                    tint = ArcadeTertiary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${turnTimerSeconds}s",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ArcadeTertiary
                                )
                            }

                            // Forfeit / surrender button
                            IconButton(
                                onClick = { showSurrenderDialog = true },
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFFDAD6))
                                    .testTag("forfeit_match_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Flag,
                                    contentDescription = "Forfeit",
                                    tint = Color(0xFFBA1A1A),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                // 2. Dual Player Head-to-Head Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = ArcadeSurfaceContainerLowest),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Player 1 (User)
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = ArcadePrimaryFixed.copy(alpha = 0.4f))
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .background(ArcadePrimaryContainer),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.SportsEsports,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "YOU",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Black,
                                            color = ArcadeOnSurface
                                        )
                                    }
                                    Text(
                                        text = "🔥 4x",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ArcadeTertiary
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "$p1Lines/5 Lines",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Black,
                                        color = ArcadePrimary
                                    )

                                    // 5 progress dashes
                                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                        for (i in 1..5) {
                                            Box(
                                                modifier = Modifier
                                                    .size(width = 8.dp, height = 12.dp)
                                                    .clip(RoundedCornerShape(2.dp))
                                                    .background(
                                                        if (i <= p1Lines) ArcadeSecondaryContainer else ArcadeSurfaceContainerHighest
                                                    )
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Player 2 (Opponent)
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = ArcadeSurfaceContainerHigh)
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .background(ArcadeInverseSurface),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.SmartToy,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = opponentName,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ArcadeOnSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "$p2Lines/5 Lines",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Black,
                                        color = ArcadeOnSurfaceVariant
                                    )

                                    // 5 progress dashes
                                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                        for (i in 1..5) {
                                            Box(
                                                modifier = Modifier
                                                    .size(width = 8.dp, height = 12.dp)
                                                    .clip(RoundedCornerShape(2.dp))
                                                    .background(
                                                        if (i <= p2Lines) ArcadeSecondary else ArcadeSurfaceContainerHighest
                                                    )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 3. Dynamic Arcade B-I-N-G-O Strike Bar
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = ArcadeSurfaceContainerLowest)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val letters = listOf("B", "I", "N", "G", "O")
                        letters.forEachIndexed { index, letter ->
                            val isCompleted = index < p1Lines
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (isCompleted) {
                                            Brush.verticalGradient(listOf(ArcadeSecondaryFixed, ArcadeSecondaryContainer))
                                        } else {
                                            Brush.verticalGradient(listOf(ArcadeSurfaceContainer, ArcadeSurfaceContainerHigh))
                                        }
                                    )
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = letter,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Black,
                                        color = if (isCompleted) ArcadeOnSecondary else ArcadeOnSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = if (isCompleted) "LINE ${index + 1}" else "${5 - p1Lines} Left",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Black,
                                        color = if (isCompleted) ArcadeOnSecondary else ArcadeOutline
                                    )
                                }
                            }
                        }
                    }
                }

                // 4. Turn Indicator Banner & Call History Strip
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = ArcadePrimary)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(if (isUserTurn) TournamentGold else Color.White)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isUserTurn) "YOUR TURN — Pick a Number!" else "Opponent Thinking...",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            }

                            if (state.lastCutNumber != null) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color.Black.copy(alpha = 0.25f))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "Recent: #${state.lastCutNumber}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        color = TournamentGold
                                    )
                                }
                            }
                        }

                        // Countdown Progress Bar
                        val turnProgress = (turnTimerSeconds / 15f).coerceIn(0f, 1f)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color.White.copy(alpha = 0.2f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(turnProgress)
                                    .height(5.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(TournamentGold)
                            )
                        }
                    }
                }

                // 5. 5x5 Matrix Game Board in Tactical Slate Inverse Surface
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = ArcadeInverseSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        for (r in 0 until 5) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                for (c in 0 until 5) {
                                    val cellNum = viewingBoard.cells.getOrNull(r)?.getOrNull(c) ?: 0
                                    val isCut = cellNum > 0 && viewingBoard.isNumberCrossed(cellNum)
                                    val isLastCut = cellNum == state.lastCutNumber

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(
                                                when {
                                                    isLastCut -> ArcadeTertiaryContainer
                                                    isCut -> ArcadeSecondaryFixed
                                                    else -> ArcadeSurfaceContainerLowest
                                                }
                                            )
                                            .clickable(enabled = isUserTurn && !isCut && cellNum > 0) {
                                                onCutNumber(cellNum)
                                            }
                                            .testTag("duel_cell_${r}_${c}"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (cellNum > 0) cellNum.toString() else "",
                                            fontSize = 17.sp,
                                            fontWeight = FontWeight.Black,
                                            color = when {
                                                isLastCut -> Color.White
                                                isCut -> ArcadeOnSurface
                                                else -> ArcadeOnSurface
                                            }
                                        )

                                        // Stamped ✕ over marked numbers
                                        if (isCut) {
                                            Text(
                                                text = "✕",
                                                fontSize = 26.sp,
                                                fontWeight = FontWeight.Black,
                                                color = if (isLastCut) Color.White.copy(alpha = 0.9f) else RedPenStrike.copy(alpha = 0.85f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 6. Quick Social Reaction Chips
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(listOf("GG! 🔥", "Nice Move! 👏", "Too Fast! ⚡", "Watch this! 😎", "Lucky! 🍀")) { reaction ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(ArcadeSurfaceContainerHigh)
                                .clickable { onSendReaction(reaction) }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = reaction,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = ArcadeOnSurface
                            )
                        }
                    }
                }

                // 7. Primary Match Action Bar & Call Bingo Button
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = ArcadeSurfaceContainerLowest)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { onSendReaction("🎉 Bingo Pulse!") },
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(ArcadeSurfaceContainerHigh)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Chat,
                                    contentDescription = "Quick Chat",
                                    tint = ArcadeOnSurface,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Button(
                                onClick = {
                                    if (p1Lines >= 5) {
                                        // Auto wins or finishes
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (p1Lines >= 5) EmeraldWin else ArcadeSecondaryContainer
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("claim_bingo_cta")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (p1Lines >= 5) "BINGO ACHIEVED! 🏆" else "CLAIM BINGO! ($p1Lines/5)",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = ArcadePrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Strike 5 lines to trigger Bingo",
                                    fontSize = 11.sp,
                                    color = ArcadeOnSurfaceVariant
                                )
                            }
                            Text(
                                text = "${(5 - p1Lines).coerceAtLeast(0)} LINES LEFT",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = ArcadeTertiary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Modal: Surrender Confirmation
    if (showSurrenderDialog) {
        Dialog(onDismissRequest = { showSurrenderDialog = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                color = ArcadeSurfaceContainerLowest,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFDAD6)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Flag,
                            contentDescription = null,
                            tint = Color(0xFFBA1A1A),
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Text(
                        text = "Surrender Match?",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = ArcadeOnSurface
                    )

                    Text(
                        text = "Conceding will award the duel victory to $opponentName. Keep playing to finish the round!",
                        fontSize = 13.sp,
                        color = ArcadeOnSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { showSurrenderDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = ArcadeSurfaceContainerHigh),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Keep Playing", color = ArcadeOnSurface, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                showSurrenderDialog = false
                                onBackToHome()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBA1A1A)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Surrender", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Modal: Game Over / Victory
    if (state.matchOutcome != MatchOutcome.IN_PROGRESS) {
        Dialog(onDismissRequest = {}) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                color = ArcadeSurfaceContainerLowest,
                shadowElevation = 12.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val isWin = state.matchOutcome == MatchOutcome.PLAYER_WON
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(if (isWin) TournamentGold.copy(alpha = 0.2f) else ArcadeSurfaceContainerHigh),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isWin) Icons.Default.Celebration else Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = if (isWin) TournamentGold else ArcadeOnSurfaceVariant,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Text(
                        text = if (isWin) "VICTORY! 🏆" else "ROUND FINISHED",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isWin) ArcadeSecondary else ArcadeOnSurface
                    )

                    Text(
                        text = if (isWin) "Incredible daubing speed! You scored 5 Lines first." else "Great duel! Rematch to climb higher on the leaderboards.",
                        fontSize = 13.sp,
                        color = ArcadeOnSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onBackToHome,
                            colors = ButtonDefaults.buttonColors(containerColor = ArcadeSurfaceContainerHigh),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Home", color = ArcadeOnSurface, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = onRematch,
                            colors = ButtonDefaults.buttonColors(containerColor = ArcadePrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Rematch", color = Color.White, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }
    }
}
