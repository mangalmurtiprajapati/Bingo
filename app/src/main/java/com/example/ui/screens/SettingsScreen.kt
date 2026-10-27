package com.example.ui.screens

import android.app.Activity
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.example.model.UserStats
import com.example.ui.components.ArcadeTopHeader
import com.example.ui.theme.ArcadeBackground
import com.example.ui.theme.ArcadeInverseOnSurface
import com.example.ui.theme.ArcadeInverseSurface
import com.example.ui.theme.ArcadeOnSurface
import com.example.ui.theme.ArcadeOnSurfaceVariant
import com.example.ui.theme.ArcadeOutlineVariant
import com.example.ui.theme.ArcadePrimary
import com.example.ui.theme.ArcadePrimaryFixed
import com.example.ui.theme.ArcadeSecondary
import com.example.ui.theme.ArcadeSecondaryFixed
import com.example.ui.theme.ArcadeSurfaceContainer
import com.example.ui.theme.ArcadeSurfaceContainerHigh
import com.example.ui.theme.ArcadeSurfaceContainerLowest
import com.example.ui.theme.TournamentGold

@Composable
fun SettingsScreen(
    stats: UserStats,
    activeTheme: BingoTheme,
    authState: AuthState = AuthState.Idle,
    onSignInWithGoogle: (Activity) -> Unit = {},
    onSignInWithFacebook: (Activity) -> Unit = {},
    onSignInAsGuest: (String?) -> Unit = {},
    onSignOut: () -> Unit = {},
    onToggleSound: (Boolean) -> Unit,
    onToggleMusic: (Boolean) -> Unit,
    onResetData: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity

    var showResetDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    val isGuest = stats.loginProvider.equals("GUEST", ignoreCase = true) || stats.loginProvider.isBlank()

    BackHandler { onBack() }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = ArcadeBackground,
        topBar = {
            ArcadeTopHeader(
                title = "Settings",
                subtitle = "Account & Preferences",
                coins = stats.coins,
                gems = stats.gems,
                onBack = onBack
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
                // 1. Account & Cloud Sync Section
                Text(
                    text = "Account & Cloud Sync",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = ArcadeOnSurface
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = ArcadeSurfaceContainerHigh),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // User Profile Info Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(ArcadePrimaryFixed),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = stats.userAvatar.ifBlank { "🎮" },
                                    fontSize = 22.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stats.userName,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    color = ArcadeOnSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (isGuest) ArcadeSurfaceContainerLowest else Color(0xFF2E7D32)
                                            )
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = if (isGuest) "👤 GUEST ID" else "🌐 ${stats.loginProvider.uppercase()} LINKED",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            color = if (isGuest) ArcadeOnSurfaceVariant else Color.White
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Level ${stats.level}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ArcadeOnSurfaceVariant
                                    )
                                }
                            }
                        }

                        // Auth Action Buttons
                        if (isGuest) {
                            Text(
                                text = "Playing as Guest. Connect Google or Facebook to save coins and play across devices.",
                                fontSize = 11.sp,
                                color = ArcadeOnSurfaceVariant
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { activity?.let { onSignInWithGoogle(it) } },
                                    colors = ButtonDefaults.buttonColors(containerColor = ArcadePrimary),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f).testTag("settings_google_signin")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CloudDone,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Google", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }

                                Button(
                                    onClick = { activity?.let { onSignInWithFacebook(it) } },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1877F2)),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f).testTag("settings_facebook_signin")
                                ) {
                                    Text("Facebook", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        } else {
                            Text(
                                text = "Your account is linked with ${stats.loginProvider}. Progress is backed up automatically.",
                                fontSize = 11.sp,
                                color = ArcadeOnSurfaceVariant
                            )

                            Button(
                                onClick = { showLogoutDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBA1A1A)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().testTag("settings_logout_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Logout,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Log Out / Switch Account", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // 2. Audio & Sensory Section
                Text(
                    text = "Audio & Sensory",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = ArcadeOnSurface
                )

                SettingToggleItem(
                    title = "Sound Effects",
                    subtitle = "Card pops, ball caller & win chimes",
                    icon = Icons.Default.VolumeUp,
                    isChecked = stats.isSoundEnabled,
                    onCheckedChange = onToggleSound
                )

                SettingToggleItem(
                    title = "Background Music",
                    subtitle = "Ambient gaming arcade acoustic loop",
                    icon = Icons.Default.MusicNote,
                    isChecked = stats.isMusicEnabled,
                    onCheckedChange = onToggleMusic
                )

                Spacer(modifier = Modifier.height(4.dp))

                // 3. Application & Safety Section
                Text(
                    text = "Application & Safety",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = ArcadeOnSurface
                )

                SettingActionItem(
                    title = "Privacy Policy",
                    subtitle = "Safe gameplay, zero real-money wagering",
                    icon = Icons.Default.Policy,
                    onClick = { showPrivacyDialog = true }
                )

                SettingActionItem(
                    title = "Share Game",
                    subtitle = "Invite friends to 1v1 tactical Bingo Pulse",
                    icon = Icons.Default.Share,
                    onClick = { }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 4. Danger Zone - Reset Progress
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFDAD6))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Reset Career Progress",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFBA1A1A)
                        )
                        Text(
                            text = "Reset all stats, unlocked themes, levels and coins back to factory defaults.",
                            fontSize = 11.sp,
                            color = Color(0xFF410002)
                        )
                        Button(
                            onClick = { showResetDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBA1A1A)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("reset_progress_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.RestartAlt,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Reset All Data", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    }

    // Modal: Logout Confirmation
    if (showLogoutDialog) {
        Dialog(onDismissRequest = { showLogoutDialog = false }) {
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
                            imageVector = Icons.Default.Logout,
                            contentDescription = null,
                            tint = Color(0xFFBA1A1A),
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Text(
                        text = "Log Out of Account?",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = ArcadeOnSurface
                    )

                    Text(
                        text = "You will be returned to Guest mode. You can sign back into this account anytime.",
                        fontSize = 13.sp,
                        color = ArcadeOnSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { showLogoutDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = ArcadeSurfaceContainerHigh),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel", color = ArcadeOnSurface, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                showLogoutDialog = false
                                onSignOut()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBA1A1A)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Log Out", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Modal: Reset Confirmation
    if (showResetDialog) {
        Dialog(onDismissRequest = { showResetDialog = false }) {
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
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = null,
                            tint = Color(0xFFBA1A1A),
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Text(
                        text = "Confirm Full Reset",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = ArcadeOnSurface
                    )

                    Text(
                        text = "Are you sure? This will permanently delete your coin balance, unlocked boards, and player ranking.",
                        fontSize = 13.sp,
                        color = ArcadeOnSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { showResetDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = ArcadeSurfaceContainerHigh),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel", color = ArcadeOnSurface, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                showResetDialog = false
                                onResetData()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBA1A1A)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Reset", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Modal: Privacy Policy
    if (showPrivacyDialog) {
        Dialog(onDismissRequest = { showPrivacyDialog = false }) {
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
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Privacy & Safety Policy",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = ArcadeOnSurface
                    )

                    Text(
                        text = "• Arcade Bingo Duel is intended solely for amusement and entertainment.\n" +
                                "• No real-money gambling, wagering, or cash prizes are offered or permitted.\n" +
                                "• All in-game coins, gems and power-ups are virtual currency with no monetary value.\n" +
                                "• User stats and achievements are saved locally or via secure cloud account linking.",
                        fontSize = 13.sp,
                        color = ArcadeOnSurfaceVariant,
                        lineHeight = 18.sp
                    )

                    Button(
                        onClick = { showPrivacyDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = ArcadePrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Understood", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingToggleItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = ArcadeSurfaceContainerHigh)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(ArcadeSurfaceContainerLowest),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = ArcadePrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = ArcadeOnSurface
                    )
                    Text(
                        text = subtitle,
                        fontSize = 11.sp,
                        color = ArcadeOnSurfaceVariant
                    )
                }
            }

            Switch(
                checked = isChecked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = ArcadePrimary,
                    uncheckedThumbColor = ArcadeOnSurfaceVariant,
                    uncheckedTrackColor = ArcadeSurfaceContainer
                )
            )
        }
    }
}

@Composable
private fun SettingActionItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = ArcadeSurfaceContainerHigh)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(ArcadeSurfaceContainerLowest),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = ArcadeSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = ArcadeOnSurface
                    )
                    Text(
                        text = subtitle,
                        fontSize = 11.sp,
                        color = ArcadeOnSurfaceVariant
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = ArcadeOnSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
