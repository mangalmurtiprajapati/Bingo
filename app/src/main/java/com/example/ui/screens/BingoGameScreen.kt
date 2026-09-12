package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ads.BingoBannerAd
import com.example.model.BingoTheme
import com.example.ui.components.BingoBallCaller
import com.example.ui.components.BingoCardGrid
import com.example.ui.components.ConfettiParticleSystem
import com.example.viewmodel.BingoGameState
import com.example.viewmodel.GameStatus

@Composable
fun BingoGameScreen(
    state: BingoGameState,
    theme: BingoTheme,
    onCellTapped: (cardId: Int, number: Int) -> Unit,
    onDeclareBingo: () -> Unit,
    onToggleAutoDaub: () -> Unit,
    onReplayGame: () -> Unit,
    onBackToHome: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(theme.backgroundGradients))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 60.dp)
        ) {
            // Top Navigation & Auto-Daub Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackToHome,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.3f))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                // Auto-Daub Switch Pill
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E0038)),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.border(1.dp, theme.primaryColor, RoundedCornerShape(20.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Auto-Daub",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Switch(
                            checked = state.isAutoDaubActive,
                            onCheckedChange = { onToggleAutoDaub() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFFFFD700),
                                checkedTrackColor = Color(0xFF4A148C)
                            )
                        )
                    }
                }
            }

            // Ball Caller Unit
            BingoBallCaller(
                currentNumber = state.currentBallNumber,
                calledHistory = state.calledNumbers,
                timerProgress = state.timerProgress,
                theme = theme
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Cards Grid Layout (1, 2, or 4 cards)
            when (state.cardCount) {
                1 -> {
                    state.cards.firstOrNull()?.let { card ->
                        BingoCardGrid(
                            card = card,
                            theme = theme,
                            onCellClick = { num -> onCellTapped(card.id, num) }
                        )
                    }
                }
                2 -> {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        state.cards.forEach { card ->
                            BingoCardGrid(
                                card = card,
                                theme = theme,
                                onCellClick = { num -> onCellTapped(card.id, num) }
                            )
                        }
                    }
                }
                else -> {
                    // 4 cards in 2x2 grid
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        state.cards.chunked(2).forEach { pair ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                pair.forEach { card ->
                                    BingoCardGrid(
                                        card = card,
                                        theme = theme,
                                        onCellClick = { num -> onCellTapped(card.id, num) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Big Glowing BINGO Shout Button
            if (state.status == GameStatus.PLAYING) {
                Button(
                    onClick = onDeclareBingo,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFD700),
                        contentColor = Color(0xFF10002B)
                    ),
                    shape = RoundedCornerShape(28.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .shadow(12.dp, RoundedCornerShape(28.dp))
                ) {
                    Text(
                        text = "📣 SHOUT BINGO!",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }

        // Victory / Game Over Overlay Modal with Confetti
        if (state.status == GameStatus.WON) {
            ConfettiParticleSystem(
                isTriggered = true,
                particleCount = 150
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.85f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2A004E)),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.88f)
                        .border(3.dp, Color(0xFFFFD700), RoundedCornerShape(24.dp))
                        .padding(20.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "🏆 BINGO WINNER! 🏆",
                            color = Color(0xFFFFD700),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Winning Patterns:",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 12.sp
                        )
                        Text(
                            text = state.winningPatterns.joinToString(", "),
                            color = Color(0xFF00FFCC),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E0038)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "+${state.coinsEarnedInGame} COINS 🪙",
                                    color = Color(0xFFFFD700),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = "+${state.xpEarnedInGame} XP ⭐",
                                    color = Color(0xFFFF80AB),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = onReplayGame,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4081)),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("REPLAY", fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = onBackToHome,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C4DFF)),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("HOME", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Bottom Banner Ad
        BingoBannerAd(
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
