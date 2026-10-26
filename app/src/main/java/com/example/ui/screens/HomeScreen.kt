package com.example.ui.screens

import android.app.Activity
import android.widget.Toast
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.MusicOff
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.auth.AuthState
import com.example.model.BingoTheme
import com.example.model.GameDifficulty
import com.example.model.GameMode
import com.example.model.UserStats
import com.example.ui.components.ArcadeBottomNavigationBar
import com.example.ui.theme.ArcadeBackground
import com.example.ui.theme.ArcadeInverseOnSurface
import com.example.ui.theme.ArcadeInverseSurface
import com.example.ui.theme.ArcadeOnSecondary
import com.example.ui.theme.ArcadeOnSurface
import com.example.ui.theme.ArcadeOnSurfaceVariant
import com.example.ui.theme.ArcadeOutlineVariant
import com.example.ui.theme.ArcadePrimary
import com.example.ui.theme.ArcadePrimaryContainer
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
import com.example.ui.theme.TournamentGold

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
    onSetupBoard: (GameMode) -> Unit = {},
    onStartGame: (cardCount: Int, difficulty: GameDifficulty) -> Unit,
    onJoinRoomCode: (String) -> Unit = {},
    onNavigateSpinWheel: () -> Unit,
    onNavigateThemes: () -> Unit,
    onNavigateAchievements: () -> Unit,
    onNavigateStats: () -> Unit,
    onNavigateHowToPlay: () -> Unit,
    onNavigateSettings: () -> Unit,
    onExitApp: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    var showRoomCodeDialog by remember { mutableStateOf(false) }
    var roomCodeInput by remember { mutableStateOf("") }
    var showAccountDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = ArcadeBackground,
        topBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding(),
                color = ArcadeSurfaceContainerLowest,
                shadowElevation = 2.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 560.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        Brush.linearGradient(
                                            listOf(ArcadePrimary, ArcadePrimaryContainer)
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "B",
                                    color = Color.White,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Home",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = ArcadePrimary,
                                letterSpacing = (-0.5).sp
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(ArcadeSurfaceContainerHigh)
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "🪙", fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "%,d".format(stats.coins),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ArcadeOnSurface
                                )
                            }

                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(ArcadeSurfaceContainerHigh)
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "💎", fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${stats.gems}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ArcadeTertiaryContainer
                                )
                            }

                            IconButton(
                                onClick = onNavigateHowToPlay,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(ArcadeSurfaceContainer)
                                    .testTag("home_help_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.HelpOutline,
                                    contentDescription = "How To Play",
                                    tint = ArcadeOnSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, ArcadePrimary.copy(alpha = 0.3f), CircleShape)
                                    .background(ArcadePrimaryFixed)
                                    .clickable { onNavigateStats() }
                                    .testTag("home_profile_avatar"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = stats.userAvatar.ifEmpty { "👑" },
                                    fontSize = 18.sp
                                )
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            ArcadeBottomNavigationBar(
                currentRoute = "home",
                onNavigate = { route ->
                    when (route) {
                        "online_lobby" -> onStartClassicGame(GameMode.PLAY_ONLINE)
                        "spin_wheel" -> onNavigateSpinWheel()
                        "themes" -> onNavigateThemes()
                        "achievements" -> onNavigateAchievements()
                    }
                }
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
                // Profile Strip
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(2.dp, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = ArcadeSurfaceContainerLowest)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(ArcadeSurfaceContainerHigh),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = stats.userAvatar.ifEmpty { "🚀" },
                                    fontSize = 24.sp
                                )
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(ArcadeSecondaryContainer)
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = "LV ${stats.level.coerceAtLeast(1)}",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        color = ArcadeOnSecondary
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = stats.userName.ifEmpty { "Blitz Master" },
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ArcadeOnSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.Verified,
                                        contentDescription = "Verified",
                                        tint = ArcadeSecondary,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.padding(top = 2.dp)
                                ) {
                                    Text(
                                        text = "🪙 %,d".format(stats.coins),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ArcadeOnSurfaceVariant
                                    )
                                    Text(
                                        text = "💎 ${stats.gems}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ArcadeTertiary
                                    )
                                }
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = onToggleSound,
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(ArcadeSurfaceContainer)
                                    .testTag("home_sound_toggle")
                            ) {
                                Icon(
                                    imageVector = if (stats.isSoundEnabled) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeMute,
                                    contentDescription = "Toggle SFX",
                                    tint = ArcadePrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            IconButton(
                                onClick = onNavigateSettings,
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(ArcadeSurfaceContainer)
                                    .testTag("home_settings_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Settings",
                                    tint = ArcadeOnSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                // Account Connect Banner
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showAccountDialog = true }
                        .testTag("home_sync_account_banner"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = ArcadePrimaryFixed)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(ArcadeSurfaceContainerLowest),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudSync,
                                    contentDescription = "Sync",
                                    tint = ArcadePrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = if (stats.loginProvider != "GUEST") "Gamer Synced" else "Sync Account",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = ArcadeOnSurface
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(ArcadeSecondary)
                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                    ) {
                                        Text(
                                            text = "+500 COINS",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color.White
                                        )
                                    }
                                }
                                Text(
                                    text = "Connect Google / Facebook to secure rank",
                                    fontSize = 11.sp,
                                    color = ArcadeOnSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                        }

                        Button(
                            onClick = { showAccountDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = ArcadePrimary),
                            shape = RoundedCornerShape(20.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text(
                                text = if (stats.loginProvider != "GUEST") "STATUS" else "CONNECT",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                // Season 4 Hero
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, RoundedCornerShape(18.dp))
                        .testTag("hero_event_banner"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = ArcadeInverseSurface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    listOf(ArcadePrimaryContainer.copy(alpha = 0.35f), ArcadeInverseSurface)
                                )
                            )
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(ArcadeTertiary)
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "SEASON 4 LIVE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(ArcadeSecondaryContainer)
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Bolt,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "2X XP BONUS",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        Text(
                            text = "Neon Blitz Arena",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = ArcadeInverseOnSurface,
                            lineHeight = 28.sp
                        )
                        Text(
                            text = "Double dauber score & neon trophy drops all weekend!",
                            fontSize = 13.sp,
                            color = ArcadeInverseOnSurface.copy(alpha = 0.8f)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = null,
                                    tint = ArcadeSecondaryFixed,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Ends in 2d 14h",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ArcadeSecondaryFixed
                                )
                            }

                            Button(
                                onClick = { onStartClassicGame(GameMode.PLAY_ONLINE) },
                                colors = ButtonDefaults.buttonColors(containerColor = ArcadeSecondaryContainer),
                                shape = RoundedCornerShape(20.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                modifier = Modifier
                                    .height(36.dp)
                                    .testTag("hero_play_now_button")
                            ) {
                                Text(
                                    text = "PLAY NOW",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                // Modes Title
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 2.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Game Modes",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = ArcadeOnSurface
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Instant Matchmaking",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = ArcadePrimary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Sensors,
                            contentDescription = null,
                            tint = ArcadePrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // 4 Game Mode Bento Cards
                ArcadeGameModeCard(
                    title = "Solo vs AI",
                    badge = "BOT LV 1-5",
                    badgeColor = ArcadeSurfaceContainerHigh,
                    badgeTextColor = ArcadeOnSurfaceVariant,
                    description = "Practice patterns at your pace. No timer stress.",
                    footerText = "INSTANT PLAY",
                    footerColor = ArcadePrimary,
                    emojiIcon = "🤖",
                    buttonColor = ArcadePrimary,
                    buttonIcon = Icons.Default.PlayArrow,
                    testTag = "mode_solo_ai",
                    onClick = { onStartClassicGame(GameMode.VS_COMPUTER) }
                )

                ArcadeGameModeCard(
                    title = "Online Blitz",
                    badge = "1v1 & 4P",
                    badgeColor = ArcadeTertiaryContainer,
                    badgeTextColor = Color.White,
                    description = "Real-time room showdowns. Winner takes coin pool!",
                    footerText = "1,482 PLAYERS LIVE",
                    footerColor = ArcadeSecondaryContainer,
                    emojiIcon = "⚡",
                    buttonColor = ArcadeSecondaryContainer,
                    buttonIcon = Icons.Default.FlashOn,
                    testTag = "mode_online_blitz",
                    onClick = { onStartClassicGame(GameMode.PLAY_ONLINE) }
                )

                ArcadeGameModeCard(
                    title = "Pass & Play",
                    badge = "OFFLINE",
                    badgeColor = ArcadeSurfaceContainerHigh,
                    badgeTextColor = ArcadeOnSurfaceVariant,
                    description = "Local 2-player board on one device. Zero Wi-Fi needed.",
                    footerText = "SAME SCREEN",
                    footerColor = ArcadeOnSurfaceVariant,
                    emojiIcon = "👥",
                    buttonColor = ArcadePrimaryContainer,
                    buttonIcon = Icons.Default.People,
                    testTag = "mode_pass_and_play",
                    onClick = { onStartClassicGame(GameMode.PLAY_WITH_FRIEND) }
                )

                ArcadeGameModeCard(
                    title = "Custom Room",
                    badge = "PIN CODE",
                    badgeColor = ArcadeSurfaceContainerHigh,
                    badgeTextColor = ArcadeOnSurfaceVariant,
                    description = "Create private table or enter 4-digit code from friends.",
                    footerText = "HOST OR JOIN",
                    footerColor = ArcadePrimary,
                    emojiIcon = "🔑",
                    buttonColor = ArcadeInverseSurface,
                    buttonIcon = Icons.Default.Key,
                    testTag = "mode_custom_room",
                    onClick = { showRoomCodeDialog = true }
                )

                // 2x2 Bento Dock
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Daily Vault & Progression",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = ArcadeOnSurface
                        )
                        Text(
                            text = "ALL SYSTEMS READY",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = ArcadeSecondary,
                            letterSpacing = 0.5.sp
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ArcadeBentoTile(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.Casino,
                            iconColor = ArcadeSecondary,
                            iconBg = ArcadeSecondaryFixed,
                            tagText = "READY TO CLAIM",
                            tagColor = ArcadeTertiary,
                            title = "Daily Spin",
                            subtitle = "Jackpot: 10,000x",
                            testTag = "bento_daily_spin",
                            onClick = onNavigateSpinWheel
                        )

                        ArcadeBentoTile(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.Palette,
                            iconColor = ArcadePrimary,
                            iconBg = ArcadePrimaryFixed,
                            tagText = "8 SKINS",
                            tagColor = ArcadePrimaryContainer,
                            title = "Themes Shop",
                            subtitle = "Cyber & Retro",
                            testTag = "bento_themes_shop",
                            onClick = onNavigateThemes
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ArcadeBentoTile(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.EmojiEvents,
                            iconColor = ArcadeSecondary,
                            iconBg = ArcadeSecondaryFixed,
                            tagText = "3 CLAIM",
                            tagColor = ArcadeSecondary,
                            title = "Badges Hub",
                            subtitle = "24/40 Trophies",
                            testTag = "bento_badges_hub",
                            onClick = onNavigateAchievements
                        )

                        ArcadeBentoTile(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.QueryStats,
                            iconColor = ArcadePrimary,
                            iconBg = ArcadePrimaryFixed,
                            tagText = "SEASON 4",
                            tagColor = ArcadePrimary,
                            title = "Career Stats",
                            subtitle = "Rank: Gold II",
                            testTag = "bento_career_stats",
                            onClick = onNavigateStats
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    if (showRoomCodeDialog) {
        Dialog(onDismissRequest = { showRoomCodeDialog = false }) {
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
                            .background(ArcadePrimaryFixed),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Key,
                            contentDescription = null,
                            tint = ArcadePrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Text(
                        text = "Join Private Room",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = ArcadeOnSurface
                    )

                    Text(
                        text = "Enter the 4-digit room code shared by your friend to jump straight into the duel.",
                        fontSize = 13.sp,
                        color = ArcadeOnSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    OutlinedTextField(
                        value = roomCodeInput,
                        onValueChange = { if (it.length <= 6) roomCodeInput = it.uppercase() },
                        label = { Text("4-Digit Code (e.g. 7842)") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("room_code_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ArcadePrimary,
                            unfocusedBorderColor = ArcadeOutlineVariant
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { showRoomCodeDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = ArcadeSurfaceContainerHigh),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel", color = ArcadeOnSurfaceVariant, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                if (roomCodeInput.isNotBlank()) {
                                    showRoomCodeDialog = false
                                    onJoinRoomCode(roomCodeInput.trim())
                                } else {
                                    Toast.makeText(context, "Enter a valid code", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ArcadePrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("join_room_confirm_button")
                        ) {
                            Text("Join Duel", color = Color.White, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            }
        }
    }

    if (showAccountDialog) {
        Dialog(onDismissRequest = { showAccountDialog = false }) {
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
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(ArcadePrimaryFixed),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudSync,
                            contentDescription = null,
                            tint = ArcadePrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Text(
                        text = "Gamer Identity & Cloud Sync",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = ArcadeOnSurface,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Sign in to secure your Season 4 rank, sync trophies across devices, and unlock the +500 Coins bonus.",
                        fontSize = 12.sp,
                        color = ArcadeOnSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(ArcadeSurfaceContainer)
                            .padding(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = stats.userAvatar.ifEmpty { "🚀" }, fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = stats.userName,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ArcadeOnSurface
                                )
                                Text(
                                    text = "Provider: ${stats.loginProvider} • LV. ${stats.level}",
                                    fontSize = 11.sp,
                                    color = ArcadeOnSurfaceVariant
                                )
                            }
                        }
                    }

                    Button(
                        onClick = {
                            if (activity != null) {
                                onSignInWithGoogle(activity)
                                showAccountDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ArcadePrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("google_signin_button")
                    ) {
                        Text("Connect with Google", color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            if (activity != null) {
                                onSignInWithFacebook(activity)
                                showAccountDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1877F2)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("facebook_signin_button")
                    ) {
                        Text("Connect with Facebook", color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { showAccountDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = ArcadeSurfaceContainerHigh),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Continue Playing", color = ArcadeOnSurface, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun ArcadeGameModeCard(
    title: String,
    badge: String,
    badgeColor: Color,
    badgeTextColor: Color,
    description: String,
    footerText: String,
    footerColor: Color,
    emojiIcon: String,
    buttonColor: Color,
    buttonIcon: ImageVector,
    testTag: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .shadow(2.dp, RoundedCornerShape(16.dp))
            .testTag(testTag),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ArcadeSurfaceContainerLowest)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(ArcadeSurfaceContainerHigh),
                contentAlignment = Alignment.Center
            ) {
                Text(text = emojiIcon, fontSize = 28.sp)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = ArcadeOnSurface
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(badgeColor)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = badge,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = badgeTextColor
                        )
                    }
                }
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = ArcadeOnSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = footerText,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = footerColor,
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(buttonColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = buttonIcon,
                    contentDescription = title,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun ArcadeBentoTile(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    iconColor: Color,
    iconBg: Color,
    tagText: String,
    tagColor: Color,
    title: String,
    subtitle: String,
    testTag: String,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .shadow(1.dp, RoundedCornerShape(16.dp))
            .testTag(testTag),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ArcadeSurfaceContainerLowest)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(tagColor.copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = tagText,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black,
                        color = tagColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                color = ArcadeOnSurface
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = ArcadeOnSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
