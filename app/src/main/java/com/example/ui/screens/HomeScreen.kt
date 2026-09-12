package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ads.BingoBannerAd
import com.example.model.BingoTheme
import com.example.model.GameDifficulty
import com.example.model.GameMode
import com.example.model.UserStats
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Public

@Composable
fun HomeScreen(
    stats: UserStats,
    activeTheme: BingoTheme,
    onStartClassicGame: (GameMode) -> Unit,
    onStartGame: (cardCount: Int, difficulty: GameDifficulty) -> Unit,
    onNavigateSpinWheel: () -> Unit,
    onNavigateThemes: () -> Unit,
    onNavigateAchievements: () -> Unit,
    onNavigateStats: () -> Unit,
    onNavigateHowToPlay: () -> Unit,
    onNavigateSettings: () -> Unit,
    onExitApp: () -> Unit
) {
    var showGameSetupDialog by remember { mutableStateOf(false) }
    var showCasinoDialog by remember { mutableStateOf(false) }
    var selectedCardsCount by remember { mutableStateOf(1) }
    var selectedDifficulty by remember { mutableStateOf(GameDifficulty.EASY) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(activeTheme.backgroundGradients))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 60.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Top Bar with Coins & Level Progress
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Coins Pill
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2A004E)),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.border(1.dp, Color(0xFFFFD700), RoundedCornerShape(20.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "🪙", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${stats.coins}",
                            color = Color(0xFFFFD700),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Level Badge
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E0038)),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.border(1.dp, activeTheme.primaryColor, RoundedCornerShape(20.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "LVL ${stats.level}",
                            color = activeTheme.primaryColor,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        LinearProgressIndicator(
                            progress = { stats.xp.toFloat() / stats.xpForNextLevel },
                            modifier = Modifier
                                .width(60.dp)
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = Color(0xFF00FFCC),
                            trackColor = Color.White.copy(alpha = 0.2f)
                        )
                    }
                }
            }

            // Hero Banner Asset Image
            Card(
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .height(180.dp)
                    .border(2.dp, Color(0xFFFFD700).copy(alpha = 0.8f), RoundedCornerShape(20.dp))
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(id = R.drawable.img_bingo_banner),
                        contentDescription = "Lucky Bingo Hero Banner",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                                )
                            )
                    )

                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "LUCKY BINGO",
                            color = Color(0xFFFFD700),
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "Premium Offline Edition • Pure Fun!",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Main Play Action Button
            Button(
                onClick = { showGameSetupDialog = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFD700),
                    contentColor = Color(0xFF10002B)
                ),
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(64.dp)
                    .shadow(12.dp, RoundedCornerShape(28.dp))
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "PLAY BINGO",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Menu Quick Action Buttons Grid
            Column(
                modifier = Modifier.fillMaxWidth(0.9f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HomeMenuCard(
                        title = "Spin Wheel",
                        subtitle = "Free Coins",
                        icon = Icons.Default.Casino,
                        accentColor = Color(0xFFFF007F),
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateSpinWheel
                    )
                    HomeMenuCard(
                        title = "Themes",
                        subtitle = "8 Styles",
                        icon = Icons.Default.Palette,
                        accentColor = Color(0xFF00E5FF),
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateThemes
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HomeMenuCard(
                        title = "Achievements",
                        subtitle = "Claim XP",
                        icon = Icons.Default.EmojiEvents,
                        accentColor = Color(0xFFFFD700),
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateAchievements
                    )
                    HomeMenuCard(
                        title = "Statistics",
                        subtitle = "${stats.totalWins} Wins",
                        icon = Icons.Default.Star,
                        accentColor = Color(0xFF76FF03),
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateStats
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HomeMenuCard(
                        title = "How to Play",
                        subtitle = "Rules & Help",
                        icon = Icons.Default.Help,
                        accentColor = Color(0xFF29B6F6),
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateHowToPlay
                    )
                    HomeMenuCard(
                        title = "Settings",
                        subtitle = "Audio & Ads",
                        icon = Icons.Default.Settings,
                        accentColor = Color(0xFFAB47BC),
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateSettings
                    )
                }
            }
        }

        // Bottom Ad Banner
        BingoBannerAd(
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    // Game Mode Selection Modal Dialog
    if (showGameSetupDialog) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.85f))
                .clickable { showGameSetupDialog = false },
            contentAlignment = Alignment.Center
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E0038)),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .clickable(enabled = false) {}
                    .border(2.dp, Color(0xFFFFD700), RoundedCornerShape(24.dp))
                    .padding(16.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "CHOOSE BINGO MODE",
                        color = Color(0xFFFFD700),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black
                    )

                    Text(
                        text = "5x5 Grid (1 to 25) • 5 Lines = BINGO!",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Option 1: Play with Computer
                    GameModeSelectCard(
                        title = "Play with Computer",
                        subtitle = "Smart AI bot • 25 boxes • Fill 1 to 25",
                        icon = Icons.Default.Computer,
                        accentColor = Color(0xFF00E5FF),
                        onClick = {
                            showGameSetupDialog = false
                            onStartClassicGame(GameMode.VS_COMPUTER)
                        }
                    )

                    // Option 2: Play with Friends
                    GameModeSelectCard(
                        title = "Play with Friends",
                        subtitle = "Pass & Play on same device • 2 Players",
                        icon = Icons.Default.Group,
                        accentColor = Color(0xFFFF4081),
                        onClick = {
                            showGameSetupDialog = false
                            onStartClassicGame(GameMode.PLAY_WITH_FRIEND)
                        }
                    )

                    // Option 3: Play Online
                    GameModeSelectCard(
                        title = "Play Online",
                        subtitle = "Join room code or quick online match",
                        icon = Icons.Default.Public,
                        accentColor = Color(0xFFFFD700),
                        onClick = {
                            showGameSetupDialog = false
                            onStartClassicGame(GameMode.PLAY_ONLINE)
                        }
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Button(
                        onClick = { showGameSetupDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cancel", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun GameModeSelectCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2E0854)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, accentColor.copy(alpha = 0.8f), RoundedCornerShape(16.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = subtitle,
                    color = Color.White.copy(alpha = 0.75f),
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun HomeMenuCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B003A).copy(alpha = 0.9f)),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .border(1.dp, accentColor.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accentColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 11.sp
                )
            }
        }
    }
}
