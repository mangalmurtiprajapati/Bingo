package com.example.ui.screens

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
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ads.BingoBannerAd
import com.example.model.BingoTheme
import com.example.model.GameDifficulty
import com.example.model.GameMode
import com.example.model.UserStats

@Composable
fun HomeScreen(
    stats: UserStats,
    activeTheme: BingoTheme,
    onToggleSound: () -> Unit = {},
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
            // Header Top Bar with Stars & Level Progress
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Stars Pill (White bubbly card with golden stars)
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    modifier = Modifier.border(1.5.dp, Color(0xFFFFB300), RoundedCornerShape(20.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "⭐", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${stats.coins} Stars",
                            color = Color(0xFFE65100),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            maxLines = 1
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Level Badge
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        modifier = Modifier.border(1.5.dp, activeTheme.primaryColor.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "LVL ${stats.level}",
                                color = activeTheme.primaryColor,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            LinearProgressIndicator(
                                progress = { stats.xp.toFloat() / stats.xpForNextLevel },
                                modifier = Modifier
                                    .width(50.dp)
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = Color(0xFF00C853),
                                trackColor = Color(0xFFE0E0E0)
                            )
                        }
                    }

                    // Quick Audio Toggle
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(1.5.dp, Color(0xFFFFB300), CircleShape)
                            .clickable { onToggleSound() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (stats.isSoundEnabled) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
                            contentDescription = "Toggle Audio",
                            tint = if (stats.isSoundEnabled) Color(0xFF0288D1) else Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Hero Banner Asset Image
            Card(
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .height(180.dp)
                    .border(2.5.dp, Color.White, RoundedCornerShape(24.dp))
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(id = R.drawable.img_bingo_banner),
                        contentDescription = "Kids Bingo Banner",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Transparent, Color(0xFF0D47A1).copy(alpha = 0.65f))
                                )
                            )
                    )

                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.kids_bingo_icon),
                            contentDescription = "Kids Bingo Mascot",
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .border(2.dp, Color(0xFFFFD700), CircleShape)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "KIDS BINGO ⭐",
                                color = Color.White,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "Fun 1 to 25 Number Game for Kids!",
                                color = Color(0xFFFFF9C4),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main Play Action Button (Big Friendly Bubbly Button)
            Button(
                onClick = { showGameSetupDialog = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF6D00),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier
                    .fillMaxWidth(0.88f)
                    .height(64.dp)
                    .shadow(10.dp, RoundedCornerShape(28.dp))
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "PLAY 1-25 BINGO 🎯",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Menu Quick Action Buttons Grid (Clean White Bubbly Cards)
            Column(
                modifier = Modifier.fillMaxWidth(0.92f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HomeMenuCard(
                        title = "Daily Gift 🎁",
                        subtitle = if (stats.canSpinWheelToday()) "1 Free Spin! ⭐" else "Claimed Today ⏰",
                        icon = Icons.Default.CardGiftcard,
                        accentColor = Color(0xFFE91E63),
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateSpinWheel
                    )
                    HomeMenuCard(
                        title = "Themes 🎨",
                        subtitle = "8 Fun Styles",
                        icon = Icons.Default.Palette,
                        accentColor = Color(0xFF0288D1),
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateThemes
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HomeMenuCard(
                        title = "Badges 🏆",
                        subtitle = "Star Trophies ⭐",
                        icon = Icons.Default.EmojiEvents,
                        accentColor = Color(0xFFFF8F00),
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateAchievements
                    )
                    HomeMenuCard(
                        title = "Game Score 📊",
                        subtitle = "${stats.totalWins} Matches Won",
                        icon = Icons.Default.Star,
                        accentColor = Color(0xFF43A047),
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateStats
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HomeMenuCard(
                        title = "How to Play 📖",
                        subtitle = "Easy Rules Guide",
                        icon = Icons.Default.Help,
                        accentColor = Color(0xFF00ACC1),
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateHowToPlay
                    )
                    HomeMenuCard(
                        title = "Settings ⚙️",
                        subtitle = "Music & Sounds",
                        icon = Icons.Default.Settings,
                        accentColor = Color(0xFF8E24AA),
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
                .background(Color.Black.copy(alpha = 0.7f))
                .clickable { showGameSetupDialog = false },
            contentAlignment = Alignment.Center
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .clickable(enabled = false) {}
                    .border(2.5.dp, Color(0xFFFFB300), RoundedCornerShape(24.dp))
                    .padding(12.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .padding(12.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "CHOOSE HOW TO PLAY 🎈",
                        color = Color(0xFF0D47A1),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black
                    )

                    Text(
                        text = "Fill numbers 1 to 25 • Complete 5 Lines = BINGO!",
                        color = Color(0xFF455A64),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    // Option 1: Play with Computer
                    GameModeSelectCard(
                        title = "Play with Computer 🤖",
                        subtitle = "Friendly robot opponent • 25 boxes",
                        icon = Icons.Default.Computer,
                        accentColor = Color(0xFF0288D1),
                        onClick = {
                            showGameSetupDialog = false
                            onStartClassicGame(GameMode.VS_COMPUTER)
                        }
                    )

                    // Option 2: Play with Friends
                    GameModeSelectCard(
                        title = "Play with Friends 👥",
                        subtitle = "Pass & Play on same phone • 2 Players",
                        icon = Icons.Default.Group,
                        accentColor = Color(0xFFEC407A),
                        onClick = {
                            showGameSetupDialog = false
                            onStartClassicGame(GameMode.PLAY_WITH_FRIEND)
                        }
                    )

                    // Option 3: Play Online
                    GameModeSelectCard(
                        title = "Play Online 🌐",
                        subtitle = "Play with friends using room code",
                        icon = Icons.Default.Public,
                        accentColor = Color(0xFFFF8F00),
                        onClick = {
                            showGameSetupDialog = false
                            onStartClassicGame(GameMode.PLAY_ONLINE)
                        }
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = { showGameSetupDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFECEFF1)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Close", color = Color(0xFF455A64), fontWeight = FontWeight.Bold)
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
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F9FF)),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, accentColor.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
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
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = Color(0xFF1A237E),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    color = Color(0xFF546E7A),
                    fontSize = 12.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
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
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = modifier
            .border(1.5.dp, accentColor.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
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
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accentColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = Color(0xFF1A237E),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    color = Color(0xFF546E7A),
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
