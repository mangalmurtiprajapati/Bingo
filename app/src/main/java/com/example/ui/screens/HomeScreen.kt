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
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import android.app.Activity
import androidx.compose.material3.CircularProgressIndicator
import com.example.auth.AuthState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import android.widget.Toast
import com.example.R
import com.example.ads.BingoBannerAd
import com.example.model.BingoTheme
import com.example.model.GameDifficulty
import com.example.model.GameMode
import com.example.model.UserStats
import com.example.util.NetworkUtils

@Composable
fun HomeScreen(
    stats: UserStats,
    activeTheme: BingoTheme,
    authState: AuthState = AuthState.Idle,
    isFirebaseConfigured: Boolean = false,
    onToggleSound: () -> Unit = {},
    onUpdateProfile: (name: String, provider: String, avatar: String) -> Unit = { _, _, _ -> },
    onSignInWithGoogle: (Activity) -> Unit = {},
    onSignInWithFacebook: (Activity) -> Unit = {},
    onSignInAsGuest: (String?) -> Unit = {},
    onSignOut: () -> Unit = {},
    onResetAuthState: () -> Unit = {},
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
    val context = LocalContext.current
    var showRoomCodeDialog by remember { mutableStateOf(false) }
    var roomCodeInput by remember { mutableStateOf("") }
    var showProfileDialog by remember { mutableStateOf(false) }
    var showNoInternetDialog by remember { mutableStateOf(false) }

    var editNameInput by remember(stats.userName) { mutableStateOf(stats.userName) }
    var selectedAvatar by remember(stats.userAvatar) { mutableStateOf(stats.userAvatar) }

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
            // Header Top Bar with Profile Pill, Stars, and Audio Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Profile Pill (Tappable to view/switch Google/Facebook account)
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    modifier = Modifier
                        .border(1.5.dp, Color(0xFF0288D1), RoundedCornerShape(20.dp))
                        .clickable { showProfileDialog = true }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = stats.userAvatar, fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = stats.userName,
                                color = Color(0xFF0D47A1),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "LVL ${stats.level}",
                                    color = activeTheme.primaryColor,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (stats.loginProvider == "GOOGLE") "• Google" else if (stats.loginProvider == "FACEBOOK") "• FB" else "• Guest",
                                    color = Color.Gray,
                                    fontSize = 9.sp
                                )
                            }
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Stars Pill
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        modifier = Modifier.border(1.5.dp, Color(0xFFFFB300), RoundedCornerShape(20.dp))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "⭐", fontSize = 15.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${stats.coins}",
                                color = Color(0xFFE65100),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                maxLines = 1
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
                        painter = painterResource(id = R.drawable.mind_bingo_banner),
                        contentDescription = "1-25 Bingo Mind Game Banner",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Transparent, Color(0xFF0D47A1).copy(alpha = 0.75f))
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
                            painter = painterResource(id = R.drawable.simple_bingo_logo),
                            contentDescription = "Bingo Mind Game Logo",
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .border(2.dp, Color(0xFFFFD700), CircleShape)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "1-25 BINGO MIND GAME 🎯",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "Classic 5x5 Number Strategy • Ages 5+",
                                color = Color(0xFFFFF9C4),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ==========================================
            // GROUP 1: GAME MODES (KHELNE KE MODES) 🎮
            // ==========================================
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f)),
                shape = RoundedCornerShape(26.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color.White),
                modifier = Modifier.fillMaxWidth(0.92f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Group Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "🎮 CHOOSE GAME MODE",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF0D47A1)
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFE3F2FD))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "1-Tap Direct Play",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0288D1)
                            )
                        }
                    }

                    // Mode 1: Play with Computer (Instant Solo Mode)
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(68.dp)
                            .shadow(4.dp, RoundedCornerShape(20.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFF0288D1), Color(0xFF01579B))
                                ),
                                RoundedCornerShape(20.dp)
                            )
                            .clickable { onStartClassicGame(GameMode.VS_COMPUTER) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Computer,
                                        contentDescription = "Play with Computer",
                                        tint = Color(0xFFFFD700),
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Play with Computer 🤖",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Solo Offline • Smart AI Bot • 1 vs 1 Duel",
                                        fontSize = 11.sp,
                                        color = Color(0xFFE1F5FE)
                                    )
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White.copy(alpha = 0.22f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "SOLO 🎮",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    // Mode 2: Play Online Multiplayer (5s Fast Matchmaking)
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(68.dp)
                            .shadow(4.dp, RoundedCornerShape(20.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFFFF6D00), Color(0xFFD84315))
                                ),
                                RoundedCornerShape(20.dp)
                            )
                            .clickable {
                                if (!NetworkUtils.isNetworkAvailable(context)) {
                                    showNoInternetDialog = true
                                } else {
                                    onStartClassicGame(GameMode.PLAY_ONLINE)
                                }
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Public,
                                        contentDescription = "Play Online",
                                        tint = Color(0xFFFFF9C4),
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Play Online Multiplayer 🌐",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "5s Fast Match • Real Players or Challenger",
                                        fontSize = 11.sp,
                                        color = Color(0xFFFFF3E0)
                                    )
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFFFD700))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "5s MATCH ⚡",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF1A0033)
                                )
                            }
                        }
                    }

                    // Mode 3 & 4: Friend Options (Pass & Play + Private Room Codes)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Pass & Play (Same phone, offline)
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF0F5)),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFEC407A).copy(alpha = 0.5f)),
                            modifier = Modifier
                                .weight(1f)
                                .height(64.dp)
                                .clickable { onStartClassicGame(GameMode.PLAY_WITH_FRIEND) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFEC407A).copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Group,
                                        contentDescription = null,
                                        tint = Color(0xFFEC407A),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Pass & Play 👥",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF880E4F)
                                    )
                                    Text(
                                        text = "1 Phone • 2 Friends",
                                        fontSize = 10.sp,
                                        color = Color(0xFFAD1457)
                                    )
                                }
                            }
                        }

                        // Room Codes (Online friend match)
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5)),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF8E24AA).copy(alpha = 0.5f)),
                            modifier = Modifier
                                .weight(1f)
                                .height(64.dp)
                                .clickable { showRoomCodeDialog = true }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF8E24AA).copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Bolt,
                                        contentDescription = null,
                                        tint = Color(0xFF8E24AA),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Room Code 🔑",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF4A148C)
                                    )
                                    Text(
                                        text = "BG-XXXX Code",
                                        fontSize = 10.sp,
                                        color = Color(0xFF6A1B9A)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ==========================================
            // GROUP 2: REWARDS & ACTIVITIES 🌟
            // ==========================================
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f)),
                shape = RoundedCornerShape(26.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color.White),
                modifier = Modifier.fillMaxWidth(0.92f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "🌟 REWARDS & FEATURES",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF0D47A1)
                    )

                    // 2x2 Grid: Daily Gift & Themes
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

                    // 2x2 Grid: Badges & Game Score
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
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ==========================================
            // GROUP 3: GUIDE & SETTINGS (CLEAN UTILITY ROW) ⚙️
            // ==========================================
            Row(
                modifier = Modifier.fillMaxWidth(0.92f),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(18.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    border = androidx.compose.foundation.BorderStroke(1.2.dp, Color(0xFF00ACC1).copy(alpha = 0.4f)),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .clickable { onNavigateHowToPlay() }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Help,
                            contentDescription = "How to Play",
                            tint = Color(0xFF00ACC1),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "How to Play 📖",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF006064)
                        )
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(18.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    border = androidx.compose.foundation.BorderStroke(1.2.dp, Color(0xFF8E24AA).copy(alpha = 0.4f)),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .clickable { onNavigateSettings() }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = Color(0xFF8E24AA),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Settings ⚙️",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4A148C)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Dedicated Private Room Code Dialog
    if (showRoomCodeDialog) {
        Dialog(onDismissRequest = { showRoomCodeDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF8E24AA)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🔑", fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Private Room Duel",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF4A148C)
                            )
                        }
                        IconButton(onClick = { showRoomCodeDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                        }
                    }

                    Text(
                        text = "Enter your friend's 4-digit Room Code to play together online, or create a new room in Online Lobby!",
                        fontSize = 12.sp,
                        color = Color(0xFF4A5568),
                        textAlign = TextAlign.Center
                    )

                    OutlinedTextField(
                        value = roomCodeInput,
                        onValueChange = { input ->
                            roomCodeInput = input.uppercase().take(10)
                        },
                        placeholder = { Text("e.g. BG-4821 or 4821") },
                        label = { Text("Room Code") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF8E24AA),
                            focusedLabelColor = Color(0xFF8E24AA)
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            if (roomCodeInput.isNotBlank()) {
                                if (!NetworkUtils.isNetworkAvailable(context)) {
                                    showNoInternetDialog = true
                                } else {
                                    showRoomCodeDialog = false
                                    onStartClassicGame(GameMode.PLAY_ONLINE)
                                }
                            } else {
                                Toast.makeText(context, "Please enter a room code", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8E24AA)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text("Join Room 🚀", fontWeight = FontWeight.Black, color = Color.White)
                    }

                    OutlinedButton(
                        onClick = {
                            showRoomCodeDialog = false
                            if (!NetworkUtils.isNetworkAvailable(context)) {
                                showNoInternetDialog = true
                            } else {
                                onStartClassicGame(GameMode.PLAY_ONLINE)
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text("Go to Online Lobby (Create Room) 🌐", fontWeight = FontWeight.Bold, color = Color(0xFF8E24AA))
                    }
                }
            }
        }
    }

    // No Internet Warning Dialog
    if (showNoInternetDialog) {
        Dialog(onDismissRequest = { showNoInternetDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(22.dp),
                border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFFF5252)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFEBEE)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.WifiOff,
                            contentDescription = null,
                            tint = Color(0xFFFF1744),
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Text(
                        text = "📶 Internet Connection Required",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFB71C1C),
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Online multiplayer and live friend challenges require active Wi-Fi or Mobile Data.\n\nYou can still play 'Play with Computer 🤖' or 'Friends: Pass & Play 👥' completely offline without internet!",
                        fontSize = 13.sp,
                        color = Color(0xFF37474F),
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )

                    Button(
                        onClick = { showNoInternetDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0288D1)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Understood", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }

    // Player Profile & Account Login Dialog (Google, Facebook, Guest)
    if (showProfileDialog) {
        Dialog(onDismissRequest = { showProfileDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(2.5.dp, Color(0xFF0288D1)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(6.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "PLAYER ACCOUNT & PROFILE 👤",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF0D47A1)
                    )

                    // Current Profile Badge Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F4FF)),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF0288D1).copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .border(2.dp, Color(0xFFFFB300), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = selectedAvatar, fontSize = 26.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = editNameInput.ifBlank { "MindPlayer" },
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF1A237E)
                                )
                                Text(
                                    text = "Player ID: ${stats.playerId} • Level ${stats.level}",
                                    fontSize = 11.sp,
                                    color = Color(0xFF546E7A)
                                )
                                Text(
                                    text = when (stats.loginProvider) {
                                        "GOOGLE" -> "🟢 Google Account Connected"
                                        "FACEBOOK" -> "🔵 Facebook Account Connected"
                                        else -> "🟣 Playing as Guest (Direct Mode)"
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when (stats.loginProvider) {
                                        "GOOGLE" -> Color(0xFF2E7D32)
                                        "FACEBOOK" -> Color(0xFF1565C0)
                                        else -> Color(0xFF6A1B9A)
                                    }
                                )
                            }
                        }
                    }

                    // Avatar Picker
                    Text(
                        text = "Choose Your Avatar:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF37474F),
                        modifier = Modifier.align(Alignment.Start)
                    )
                    val avatars = listOf("🎯", "🚀", "👑", "🦁", "🐯", "🐼", "⚡", "🌟", "🔥", "🍀")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        avatars.take(5).forEach { av ->
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(if (selectedAvatar == av) Color(0xFFFFD54F) else Color(0xFFECEFF1))
                                    .border(if (selectedAvatar == av) 2.dp else 1.dp, if (selectedAvatar == av) Color(0xFFFF8F00) else Color.Transparent, CircleShape)
                                    .clickable { selectedAvatar = av },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = av, fontSize = 18.sp)
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        avatars.drop(5).forEach { av ->
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(if (selectedAvatar == av) Color(0xFFFFD54F) else Color(0xFFECEFF1))
                                    .border(if (selectedAvatar == av) 2.dp else 1.dp, if (selectedAvatar == av) Color(0xFFFF8F00) else Color.Transparent, CircleShape)
                                    .clickable { selectedAvatar = av },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = av, fontSize = 18.sp)
                            }
                        }
                    }

                    // Edit Display Name
                    OutlinedTextField(
                        value = editNameInput,
                        onValueChange = { editNameInput = it.take(20) },
                        label = { Text("Display Name") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color(0xFF1A237E),
                            unfocusedTextColor = Color(0xFF1A237E),
                            focusedBorderColor = Color(0xFF0288D1),
                            unfocusedBorderColor = Color.Gray
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Firebase & Auth Status Banner
                    if (!isFirebaseConfigured) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFB300)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "⚡ Firebase Setup Ready",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Color(0xFFE65100)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Waiting for your google-services.json. Guest mode is fully playable right now! Cloud Google & Facebook login will activate as soon as config is provided.",
                                    fontSize = 11.sp,
                                    color = Color(0xFF5D4037)
                                )
                            }
                        }
                    } else {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF81C784)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "🟢 Firebase Cloud Services Connected",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = Color(0xFF2E7D32),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }

                    // Auth State feedback (Loading, Error, Success)
                    when (authState) {
                        is AuthState.Loading -> {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color(0xFF1976D2),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Authenticating with secure provider...",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF1976D2)
                                )
                            }
                        }
                        is AuthState.Error -> {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "⚠️ ${authState.message}",
                                        fontSize = 11.sp,
                                        color = Color(0xFFC62828),
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(
                                        onClick = onResetAuthState,
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            Icons.Filled.Close,
                                            contentDescription = "Dismiss",
                                            tint = Color(0xFFC62828),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                        is AuthState.Success -> {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "✅ ${authState.message}",
                                    fontSize = 11.sp,
                                    color = Color(0xFF2E7D32),
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }
                        else -> {}
                    }

                    // Social Sign-In Buttons
                    Text(
                        text = if (stats.loginProvider != "GUEST") "Connected Account Options:" else "Sign In / Link Your Account:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF37474F),
                        modifier = Modifier.align(Alignment.Start)
                    )

                    // 1. Google Sign-In Button
                    Button(
                        onClick = {
                            val activity = context as? Activity
                            if (isFirebaseConfigured && activity != null) {
                                onSignInWithGoogle(activity)
                            } else {
                                val newName = if (editNameInput.startsWith("MindPlayer")) "Google_Gamer" else editNameInput
                                onUpdateProfile(newName, "GOOGLE", selectedAvatar)
                                Toast.makeText(
                                    context,
                                    "Google profile chosen! (Will sync to Firebase once google-services.json is added)",
                                    Toast.LENGTH_LONG
                                ).show()
                                showProfileDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4285F4)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text("G", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (stats.loginProvider == "GOOGLE") "Switch Google Account" else "Continue with Google",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    // 2. Facebook Sign-In Button
                    Button(
                        onClick = {
                            val activity = context as? Activity
                            if (isFirebaseConfigured && activity != null) {
                                onSignInWithFacebook(activity)
                            } else {
                                val newName = if (editNameInput.startsWith("MindPlayer")) "FB_Challenger" else editNameInput
                                onUpdateProfile(newName, "FACEBOOK", selectedAvatar)
                                Toast.makeText(
                                    context,
                                    "Facebook profile chosen! (Will sync to Firebase once config is added)",
                                    Toast.LENGTH_LONG
                                ).show()
                                showProfileDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1877F2)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text("f", fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (stats.loginProvider == "FACEBOOK") "Switch Facebook Account" else "Continue with Facebook",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    // 3. Play Direct (Guest Mode) or Sign Out
                    if (stats.loginProvider != "GUEST") {
                        OutlinedButton(
                            onClick = {
                                onSignOut()
                                Toast.makeText(context, "Signed out. Switched to Guest mode.", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD32F2F)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                        ) {
                            Text("Sign Out (Return to Guest)", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        OutlinedButton(
                            onClick = {
                                onSignInAsGuest(editNameInput.ifBlank { null })
                                Toast.makeText(context, "Playing as Guest. No account required!", Toast.LENGTH_SHORT).show()
                                showProfileDialog = false
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF455A64)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                        ) {
                            Text("Play Direct as Guest (No Login)", fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Button(
                        onClick = {
                            onUpdateProfile(editNameInput.ifBlank { stats.userName }, stats.loginProvider, selectedAvatar)
                            Toast.makeText(context, "Profile updated!", Toast.LENGTH_SHORT).show()
                            showProfileDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Save & Close", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
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
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 2,
                    lineHeight = 16.sp
                )
                Text(
                    text = subtitle,
                    color = Color(0xFF546E7A),
                    fontSize = 11.sp,
                    maxLines = 2,
                    lineHeight = 14.sp
                )
            }
        }
    }
}
