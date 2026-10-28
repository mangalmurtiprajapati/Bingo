package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
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
import androidx.compose.ui.draw.scale
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
import com.example.ui.theme.ArcadeBackground
import com.example.ui.theme.ArcadeInverseOnSurface
import com.example.ui.theme.ArcadeInverseSurface
import com.example.ui.theme.ArcadeOnPrimary
import com.example.ui.theme.ArcadeOnSurface
import com.example.ui.theme.ArcadeOnSurfaceVariant
import com.example.ui.theme.ArcadePrimary
import com.example.ui.theme.ArcadePrimaryContainer
import com.example.ui.theme.ArcadePrimaryFixed
import com.example.ui.theme.ArcadeSecondary
import com.example.ui.theme.ArcadeSecondaryContainer
import com.example.ui.theme.ArcadeSecondaryFixed
import com.example.ui.theme.ArcadeSurfaceContainer
import com.example.ui.theme.ArcadeSurfaceContainerHigh
import com.example.ui.theme.ArcadeSurfaceContainerHighest
import com.example.ui.theme.ArcadeSurfaceContainerLow
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
    onTurnTimeout: () -> Unit = {},
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

    // Turn timer countdown with auto-cut trigger at 0s!
    LaunchedEffect(state.matchTurn, state.isComputerThinking, state.matchOutcome) {
        if (state.matchOutcome != MatchOutcome.IN_PROGRESS) return@LaunchedEffect
        turnTimerSeconds = 15
        while (turnTimerSeconds > 0 && state.matchOutcome == MatchOutcome.IN_PROGRESS) {
            delay(1000L)
            turnTimerSeconds--
        }
        if (turnTimerSeconds == 0 && state.matchOutcome == MatchOutcome.IN_PROGRESS) {
            onTurnTimeout()
        }
    }

    // Infinite breathing glow for the turn indicator
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "turnPulse"
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = ArcadeBackground,
        topBar = {
            // Unified Compact Arcade Match TopBar & HUD
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = ArcadeInverseSurface,
                shadowElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Row 1: Nav Bar (Back, Match Type, Sound & Surrender)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { showSurrenderDialog = true },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(ArcadeSurfaceContainer)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Leave Match",
                                    tint = ArcadeOnSurface,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = if (isFriendMode) "2P PASS & PLAY" else "1V1 DUEL ARENA",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    color = TournamentGold,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = "First to 5 Lines Wins",
                                    fontSize = 10.sp,
                                    color = ArcadeInverseOnSurface.copy(alpha = 0.7f)
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (isFriendMode) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(ArcadeSurfaceContainer)
                                        .clickable { onSwitchBoard(if (state.viewingBoardPlayer == 1) 2 else 1) }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "P${state.viewingBoardPlayer} Board 🔄",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ArcadePrimaryFixed
                                    )
                                }
                            }

                            IconButton(
                                onClick = onToggleSound,
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(ArcadeSurfaceContainer)
                            ) {
                                Icon(
                                    imageVector = if (isSoundMuted) Icons.Default.VolumeMute else Icons.Default.VolumeUp,
                                    contentDescription = "Toggle Sound",
                                    tint = ArcadeOnSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            IconButton(
                                onClick = { showSurrenderDialog = true },
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(ArcadeSurfaceContainer)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Flag,
                                    contentDescription = "Surrender",
                                    tint = Color(0xFFE57373),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    val now = System.currentTimeMillis()
                    val activeUserReaction = reactions.filter { it.isUser }.lastOrNull()?.takeIf { now - it.timestamp < 4500L }
                    val activeOpponentReaction = reactions.filter { !it.isUser }.lastOrNull()?.takeIf { now - it.timestamp < 4500L }

                    // Row 2: Duel Scoreboard Strip (YOU vs OPPONENT with center Timer & Turn)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(ArcadeSurfaceContainer)
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Player 1 (YOU) with Reaction Bubble
                        Column(horizontalAlignment = Alignment.Start) {
                            AnimatedVisibility(visible = activeUserReaction != null) {
                                Box(
                                    modifier = Modifier
                                        .padding(bottom = 2.dp)
                                        .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp, bottomEnd = 10.dp, bottomStart = 2.dp))
                                        .background(TournamentGold)
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = activeUserReaction?.content ?: "",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.Black
                                    )
                                }
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(ArcadePrimaryContainer)
                                        .border(2.dp, if (isUserTurn) TournamentGold else Color.Transparent, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (isFriendMode) "P1" else "ME",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White
                                    )
                                }
                                Column {
                                    Text(
                                        text = if (isFriendMode) "Player 1" else "YOU",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ArcadeOnSurface
                                    )
                                    Text(
                                        text = "$p1Lines/5 Lines",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        color = if (p1Lines >= 5) EmeraldWin else TournamentGold
                                    )
                                }
                            }
                        }

                        // Center: Turn Pill + Countdown & Miss Warning Badge
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            val turnText = when {
                                isUserTurn -> "YOUR TURN"
                                state.gameMode == GameMode.PLAY_ONLINE -> "${state.matchedOpponentName?.uppercase() ?: "OPPONENT"}'S TURN"
                                state.isComputerThinking -> "AI THINKING..."
                                else -> "OPPONENT'S TURN"
                            }
                            Box(
                                modifier = Modifier
                                    .scale(if (isUserTurn) pulseScale else 1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isUserTurn) ArcadePrimaryContainer else ArcadeSecondaryContainer
                                    )
                                    .padding(horizontal = 10.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = turnText,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = null,
                                    tint = if (turnTimerSeconds <= 5) Color(0xFFFF5252) else TournamentGold,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "${turnTimerSeconds}s",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (turnTimerSeconds <= 5) Color(0xFFFF5252) else Color.White
                                )
                            }

                            // 3-Miss Penalty Tracker Indicator
                            if (state.p1ConsecutiveMisses > 0 || state.p2ConsecutiveMisses > 0) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFFBA1A1A))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (isUserTurn) "⚠️ Timeouts: ${state.p1ConsecutiveMisses}/3" else "⚠️ Opp Timeouts: ${state.p2ConsecutiveMisses}/3",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        // Player 2 (OPPONENT) with Reaction Bubble
                        Column(horizontalAlignment = Alignment.End) {
                            AnimatedVisibility(visible = activeOpponentReaction != null) {
                                Box(
                                    modifier = Modifier
                                        .padding(bottom = 2.dp)
                                        .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp, bottomStart = 10.dp, bottomEnd = 2.dp))
                                        .background(ArcadeTertiaryContainer)
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = activeOpponentReaction?.content ?: "",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White
                                    )
                                }
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = opponentName,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ArcadeOnSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "$p2Lines/5 Lines",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        color = if (p2Lines >= 5) EmeraldWin else ArcadeTertiary
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(ArcadeTertiaryContainer)
                                        .border(2.dp, if (!isUserTurn) TournamentGold else Color.Transparent, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (state.gameMode == GameMode.VS_COMPUTER) {
                                        Icon(
                                            imageVector = Icons.Default.SmartToy,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    } else {
                                        Text(
                                            text = if (isFriendMode) "P2" else (state.opponentAvatar.ifBlank { "⚔️" }),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Row 3: B - I - N - G - O Marquee Letters (High Contrast)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val letters = listOf("B", "I", "N", "G", "O")
                        letters.forEachIndexed { index, letter ->
                            val isStruck = p1Lines > index
                            val animBg by animateColorAsState(
                                targetValue = if (isStruck) TournamentGold else Color(0xFF282029),
                                label = "bingoBg"
                            )
                            val animTextColor by animateColorAsState(
                                targetValue = if (isStruck) Color.Black else Color.White,
                                label = "bingoText"
                            )

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 3.dp)
                                    .height(30.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(animBg)
                                    .border(
                                        width = if (isStruck) 2.dp else 1.5.dp,
                                        color = if (isStruck) Color.White else TournamentGold.copy(alpha = 0.5f),
                                        shape = RoundedCornerShape(8.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = letter,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Black,
                                        color = animTextColor
                                    )
                                    if (isStruck) {
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color.Black,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            // Fixed Bottom Action Bar: High Contrast Reactions & Claim Bingo CTA
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = ArcadeInverseSurface,
                shadowElevation = 12.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Quick Chat Reactions Strip
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(listOf("🔥 GG!", "👏 Nice!", "⚡ Fast!", "😎 Watch me!", "🍀 Lucky!", "😱 Wow!")) { reaction ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(ArcadeSurfaceContainerHighest)
                                    .border(1.dp, TournamentGold.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                                    .clickable { onSendReaction(reaction) }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = reaction,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    // Primary Match CTA: "CLAIM BINGO" Button
                    Button(
                        onClick = {
                            if (p1Lines >= 5) {
                                // Winning state trigger handled automatically by ViewModel/Referee
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (p1Lines >= 5) EmeraldWin else ArcadeSurfaceContainerHigh,
                            disabledContainerColor = ArcadeSurfaceContainer
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .shadow(if (p1Lines >= 5) 8.dp else 2.dp, RoundedCornerShape(14.dp))
                            .testTag("claim_bingo_cta")
                    ) {
                        Icon(
                            imageVector = if (p1Lines >= 5) Icons.Default.Celebration else Icons.Default.Star,
                            contentDescription = null,
                            tint = if (p1Lines >= 5) Color.White else TournamentGold,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (p1Lines >= 5) {
                                "🎉 BINGO ACHIEVED! CLAIM VICTORY 🎉"
                            } else {
                                "CLAIM BINGO ($p1Lines/5 LINES COMPLETE)"
                            },
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = if (p1Lines >= 5) Color.White else ArcadeOnSurface,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        // Center Section: Fully Responsive 5x5 Game Arena Board
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 14.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 420.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Turn Instruction & Call Telemetry Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(ArcadeSurfaceContainer)
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isUserTurn) EmeraldWin else ArcadeSecondary)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isUserTurn) "TAP ANY NUMBER TO STRIKE" else "$opponentName is choosing...",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isUserTurn) EmeraldWin else ArcadeOnSurfaceVariant
                        )
                    }

                    if (state.lastCutNumber != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Last Call: ",
                                fontSize = 11.sp,
                                color = ArcadeOnSurfaceVariant
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(ArcadeTertiaryContainer)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "#${state.lastCutNumber}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                // Tactile Arcade 5x5 Board Bezel
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = ArcadeInverseSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    border = BorderStroke(1.5.dp, ArcadeSurfaceContainerHigh)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        for (r in 0 until 5) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                for (c in 0 until 5) {
                                    val cellNum = viewingBoard.cells.getOrNull(r)?.getOrNull(c) ?: 0
                                    val isCut = cellNum > 0 && viewingBoard.isNumberCrossed(cellNum)
                                    val isLastCut = cellNum == state.lastCutNumber

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxSize()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(
                                                when {
                                                    isLastCut -> ArcadeTertiaryContainer
                                                    isCut -> Color(0xFF261D22)
                                                    else -> ArcadeSurfaceContainerLowest
                                                }
                                            )
                                            .border(
                                                width = when {
                                                    isLastCut -> 2.dp
                                                    isCut -> 1.dp
                                                    else -> 1.dp
                                                },
                                                color = when {
                                                    isLastCut -> TournamentGold
                                                    isCut -> RedPenStrike.copy(alpha = 0.5f)
                                                    else -> ArcadeSurfaceContainerHigh
                                                },
                                                shape = RoundedCornerShape(10.dp)
                                            )
                                            .clickable(enabled = isUserTurn && !isCut && cellNum > 0) {
                                                onCutNumber(cellNum)
                                            }
                                            .testTag("duel_cell_${r}_${c}"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (cellNum > 0) cellNum.toString() else "",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Black,
                                            color = when {
                                                isLastCut -> Color.White
                                                isCut -> ArcadeOnSurface.copy(alpha = 0.35f)
                                                else -> ArcadeOnSurface
                                            }
                                        )

                                        // Arcade Red Pen Stamped Strike
                                        if (isCut) {
                                            Text(
                                                text = "✕",
                                                fontSize = 28.sp,
                                                fontWeight = FontWeight.Black,
                                                color = if (isLastCut) Color.White.copy(alpha = 0.9f) else RedPenStrike.copy(alpha = 0.85f)
                                            )
                                        }

                                        // Callout badge for the latest called number
                                        if (isLastCut) {
                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                                    .padding(2.dp)
                                                    .size(6.dp)
                                                    .clip(CircleShape)
                                                    .background(TournamentGold)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal: Surrender Confirmation Dialog
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
                        text = "Concede Match?",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = ArcadeOnSurface
                    )

                    Text(
                        text = "Conceding will award the victory to $opponentName. Keep playing to finish the round!",
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

    // Modal: Game Over / Victory Dialog
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
                    val isTie = state.matchOutcome == MatchOutcome.DRAW
                    val outcomeTitle = when {
                        isWin -> "VICTORY! 🏆"
                        isTie -> "MATCH TIED! 🤝"
                        else -> "DEFEAT 💔"
                    }
                    val outcomeColor = when {
                        isWin -> EmeraldWin
                        isTie -> TournamentGold
                        else -> Color(0xFFFF5252)
                    }
                    val rpBadgeText = when {
                        isWin -> "+35 RP"
                        isTie -> "+0 RP"
                        else -> "-25 RP"
                    }
                    val rpBadgeColor = when {
                        isWin -> EmeraldWin
                        isTie -> TournamentGold
                        else -> Color(0xFFFF5252)
                    }

                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(outcomeColor.copy(alpha = 0.15f))
                            .border(2.dp, outcomeColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = when {
                                isWin -> "🏆"
                                isTie -> "🤝"
                                else -> "💀"
                            },
                            fontSize = 32.sp
                        )
                    }

                    Text(
                        text = outcomeTitle,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = outcomeColor
                    )

                    // Rank Points & Reward Badge Row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(rpBadgeColor)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "⚔️ $rpBadgeText",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(ArcadeSurfaceContainerHigh)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "🪙 +${state.coinsEarned}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = TournamentGold
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(ArcadeSurfaceContainerHigh)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "⚡ +${state.xpEarned} XP",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = ArcadePrimary
                            )
                        }
                    }

                    Text(
                        text = when {
                            isWin -> "Incredible performance! You completed 5 lines first."
                            isTie -> "Incredible speed! Both players struck 5 lines simultaneously."
                            else -> if (state.p1ConsecutiveMisses >= 3) "Forfeited after 3 consecutive missed turn timers." else "$opponentName completed 5 lines first."
                        },
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
