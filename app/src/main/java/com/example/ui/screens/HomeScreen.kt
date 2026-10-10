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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    onToggleSound: () -> Unit = {},
    onUpdateProfile: (name: String, provider: String, avatar: String) -> Unit = { _, _, _ -> },
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
    var showGameSetupDialog by remember { mutableStateOf(false) }
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

                    // Option 1: Play with Computer (Offline)
                    GameModeSelectCard(
                        title = "Play with Computer 🤖",
                        subtitle = "Friendly robot opponent • 25 boxes • Offline",
                        icon = Icons.Default.Computer,
                        accentColor = Color(0xFF0288D1),
                        onClick = {
                            showGameSetupDialog = false
                            onStartClassicGame(GameMode.VS_COMPUTER)
                        }
                    )

                    // Option 2: Play with Friends (Offline Pass & Play)
                    GameModeSelectCard(
                        title = "Friends: Pass & Play (Offline) 👥",
                        subtitle = "Play on same phone with friend • 2 Players",
                        icon = Icons.Default.Group,
                        accentColor = Color(0xFFEC407A),
                        onClick = {
                            showGameSetupDialog = false
                            onStartClassicGame(GameMode.PLAY_WITH_FRIEND)
                        }
                    )

                    // Option 3: Play with Friends Online
                    GameModeSelectCard(
                        title = "Friends: Online Challenge 🌐",
                        subtitle = "Invite friends, timed duel & room code • Internet",
                        icon = Icons.Default.Public,
                        accentColor = Color(0xFFFF8F00),
                        onClick = {
                            if (!NetworkUtils.isNetworkAvailable(context)) {
                                showNoInternetDialog = true
                            } else {
                                showGameSetupDialog = false
                                onStartClassicGame(GameMode.PLAY_ONLINE)
                            }
                        }
                    )

                    // Option 4: Direct Quick Match Online
                    GameModeSelectCard(
                        title = "Quick Match (Direct Online) ⚡",
                        subtitle = "Instant match in 5-10s with online players • Direct play",
                        icon = Icons.Default.Bolt,
                        accentColor = Color(0xFF7B1FA2),
                        onClick = {
                            if (!NetworkUtils.isNetworkAvailable(context)) {
                                showNoInternetDialog = true
                            } else {
                                showGameSetupDialog = false
                                onStartClassicGame(GameMode.PLAY_ONLINE)
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    // Account Profile Quick Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFE8EAF6))
                            .clickable {
                                showGameSetupDialog = false
                                showProfileDialog = true
                            }
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = stats.userAvatar, fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${stats.userName} (${stats.loginProvider})",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1A237E)
                            )
                        }
                        Text(
                            text = "Switch Account 👤",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF0288D1)
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

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

    // No Internet Warning Dialog
    if (showNoInternetDialog) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f))
                .clickable { showNoInternetDialog = false },
            contentAlignment = Alignment.Center
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(22.dp),
                border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFFF5252)),
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .clickable(enabled = false) {}
                    .padding(14.dp)
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.75f))
                .clickable { showProfileDialog = false },
            contentAlignment = Alignment.Center
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(2.5.dp, Color(0xFF0288D1)),
                modifier = Modifier
                    .fillMaxWidth(0.94f)
                    .clickable(enabled = false) {}
                    .padding(10.dp)
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

                    // Social Sign-In Buttons
                    Text(
                        text = "Sign In / Link Your Account:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF37474F),
                        modifier = Modifier.align(Alignment.Start)
                    )

                    // 1. Google Sign-In Button
                    Button(
                        onClick = {
                            val newName = if (editNameInput.startsWith("MindPlayer")) "Google_Gamer" else editNameInput
                            onUpdateProfile(newName, "GOOGLE", selectedAvatar)
                            Toast.makeText(context, "Signed in with Google! Account linked.", Toast.LENGTH_SHORT).show()
                            showProfileDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4285F4)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text("G", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Continue with Google", fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    // 2. Facebook Sign-In Button
                    Button(
                        onClick = {
                            val newName = if (editNameInput.startsWith("MindPlayer")) "FB_Challenger" else editNameInput
                            onUpdateProfile(newName, "FACEBOOK", selectedAvatar)
                            Toast.makeText(context, "Signed in with Facebook! Account linked.", Toast.LENGTH_SHORT).show()
                            showProfileDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1877F2)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text("f", fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Continue with Facebook", fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    // 3. Play Direct (Guest Mode)
                    OutlinedButton(
                        onClick = {
                            onUpdateProfile(editNameInput.ifBlank { "Guest_${(100..999).random()}" }, "GUEST", selectedAvatar)
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
