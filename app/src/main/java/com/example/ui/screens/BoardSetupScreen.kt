package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BingoTheme
import com.example.model.GameMode
import com.example.ui.components.ArcadeTopHeader
import com.example.ui.theme.ArcadeBackground
import com.example.ui.theme.ArcadeOnSurface
import com.example.ui.theme.ArcadeOnSurfaceVariant
import com.example.ui.theme.ArcadeOutlineVariant
import com.example.ui.theme.ArcadePrimary
import com.example.ui.theme.ArcadePrimaryFixed
import com.example.ui.theme.ArcadeSecondary
import com.example.ui.theme.ArcadeSecondaryContainer
import com.example.ui.theme.ArcadeSecondaryFixed
import com.example.ui.theme.ArcadeSurfaceContainer
import com.example.ui.theme.ArcadeSurfaceContainerHigh
import com.example.ui.theme.ArcadeSurfaceContainerLowest
import com.example.ui.theme.ArcadeTertiary
import com.example.ui.theme.ArcadeTertiaryContainer
import com.example.ui.theme.EmeraldWin
import com.example.viewmodel.ClassicMatchState
import kotlinx.coroutines.delay

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
    BackHandler { onBack() }

    val currentBoard = if (state.currentSetupPlayer == 1) state.player1Board else state.player2OrComputerBoard
    val isFriendMode = state.gameMode == GameMode.PLAY_WITH_FRIEND
    val playerTitle = when {
        state.isSimultaneousFriendMode -> "Duelling ${state.matchedOpponentName ?: "Friend"}"
        isFriendMode && state.currentSetupPlayer == 1 -> "Player 1 (Pass & Play)"
        isFriendMode && state.currentSetupPlayer == 2 -> "Player 2 (Pass & Play)"
        state.gameMode == GameMode.VS_COMPUTER -> "Setup vs Bot"
        else -> "Online Setup"
    }

    var secondsLeft by remember { mutableIntStateOf(24) }
    LaunchedEffect(Unit) {
        while (secondsLeft > 0) {
            delay(1000L)
            secondsLeft--
        }
        if (!currentBoard.isFull) {
            onAutoFill()
        }
    }

    val placedCount = currentBoard.numbersPlacedCount
    val isFull = currentBoard.isFull

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = ArcadeBackground,
        topBar = {
            ArcadeTopHeader(
                title = "Board Setup",
                subtitle = playerTitle,
                onBack = onBack,
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
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 1. Countdown Urgency Bar
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = ArcadeSurfaceContainer)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier.size(40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                val progress = (secondsLeft / 30f).coerceIn(0f, 1f)
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    drawCircle(
                                        color = ArcadeSurfaceContainerHigh,
                                        style = Stroke(width = 4.dp.toPx())
                                    )
                                    drawArc(
                                        color = ArcadeTertiaryContainer,
                                        startAngle = -90f,
                                        sweepAngle = 360f * progress,
                                        useCenter = false,
                                        style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = null,
                                    tint = ArcadeTertiary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "LOCKING IN",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        color = ArcadeTertiary,
                                        letterSpacing = 0.5.sp
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(ArcadeTertiary)
                                    )
                                }
                                Text(
                                    text = "00:${secondsLeft.toString().padStart(2, '0')} left",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = ArcadeOnSurface
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(ArcadeSurfaceContainerHigh)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Speed,
                                    contentDescription = null,
                                    tint = ArcadePrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "TURBO PREP",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = ArcadePrimary
                                )
                            }
                        }
                    }
                }

                // 2. Instruction Chip
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = ArcadePrimaryFixed.copy(alpha = 0.7f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.TouchApp,
                            contentDescription = null,
                            tint = ArcadePrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Tap any cell to place next number, or hit Auto Fill for instant placement!",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = ArcadeOnSurface,
                            lineHeight = 16.sp
                        )
                    }
                }

                // 3. Header B-I-N-G-O Letters Marker
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("B", "I", "N", "G", "O").forEach { letter ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(ArcadePrimary)
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = letter,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }

                // 4. Tactical 5x5 Interactive Board
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = ArcadeSurfaceContainerHigh)
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
                                    val cellNum = currentBoard.cells.getOrNull(r)?.getOrNull(c) ?: 0
                                    val slotIndex = r * 5 + c + 1
                                    val isCenter = r == 2 && c == 2

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(
                                                if (isCenter && cellNum > 0) ArcadeSecondaryFixed else ArcadeSurfaceContainerLowest
                                            )
                                            .clickable { onAssignCell(r, c) }
                                            .testTag("board_cell_${r}_${c}"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = slotIndex.toString(),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ArcadeOutlineVariant,
                                            modifier = Modifier
                                                .align(Alignment.TopStart)
                                                .padding(start = 4.dp, top = 2.dp)
                                        )

                                        if (cellNum > 0) {
                                            Text(
                                                text = cellNum.toString(),
                                                fontSize = 17.sp,
                                                fontWeight = FontWeight.Black,
                                                color = if (isCenter) ArcadeSecondary else ArcadeOnSurface
                                            )
                                        }

                                        if (isCenter) {
                                            Icon(
                                                imageVector = Icons.Default.Star,
                                                contentDescription = null,
                                                tint = ArcadeSecondary.copy(alpha = 0.6f),
                                                modifier = Modifier
                                                    .size(10.dp)
                                                    .align(Alignment.BottomEnd)
                                                    .padding(end = 2.dp, bottom = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 5. Live Placement Status Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(if (isFull) EmeraldWin.copy(alpha = 0.2f) else ArcadeSurfaceContainerHigh),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = if (isFull) EmeraldWin else ArcadePrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "$placedCount/25 Placed",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = ArcadeOnSurface
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isFull) ArcadePrimaryFixed else ArcadeSurfaceContainerHigh)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = if (isFull) "READY TO PLAY" else "FILLING SLOTS",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = ArcadePrimary
                        )
                    }
                }

                // 6. Quick Action Utility Tray
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onAutoFill,
                        colors = ButtonDefaults.buttonColors(containerColor = ArcadeSurfaceContainer),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(64.dp)
                            .testTag("setup_autofill_btn")
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Casino,
                                contentDescription = null,
                                tint = ArcadePrimary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Auto Fill",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = ArcadeOnSurface
                            )
                        }
                    }

                    Button(
                        onClick = onAutoFill,
                        colors = ButtonDefaults.buttonColors(containerColor = ArcadeSurfaceContainer),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(64.dp)
                            .testTag("setup_invert_btn")
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = null,
                                tint = ArcadePrimary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Invert",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = ArcadeOnSurface
                            )
                        }
                    }

                    Button(
                        onClick = onClear,
                        colors = ButtonDefaults.buttonColors(containerColor = ArcadeSurfaceContainer),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(64.dp)
                            .testTag("setup_clear_btn")
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.RestartAlt,
                                contentDescription = null,
                                tint = ArcadeTertiary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Clear All",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = ArcadeTertiary
                            )
                        }
                    }
                }

                // 7. Sticky Bottom Arcade CTA: START MATCH
                Button(
                    onClick = {
                        if (!isFull) {
                            onAutoFill()
                        }
                        onProceed()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ArcadeSecondaryContainer),
                    shape = RoundedCornerShape(28.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .shadow(4.dp, RoundedCornerShape(28.dp))
                        .testTag("setup_start_match_btn")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "START MATCH",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.SportsEsports,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Text(
                    text = "AUTO-LOCKING IN WHEN TIMER EXPIRES",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = ArcadeOutlineVariant,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
