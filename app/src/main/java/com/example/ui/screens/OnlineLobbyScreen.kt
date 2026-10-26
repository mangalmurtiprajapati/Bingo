package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.PersonPinCircle
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.BingoTheme
import com.example.model.FriendChallenge
import com.example.model.FriendProfile
import com.example.model.OnlinePlayer
import com.example.ui.components.ArcadeBottomNavigationBar
import com.example.ui.components.ArcadeTopHeader
import com.example.ui.theme.ArcadeBackground
import com.example.ui.theme.ArcadeInverseOnSurface
import com.example.ui.theme.ArcadeInverseSurface
import com.example.ui.theme.ArcadeOnPrimary
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
import com.example.ui.theme.ArcadeSurfaceContainerHighest
import com.example.ui.theme.ArcadeSurfaceContainerLowest
import com.example.ui.theme.ArcadeTertiary
import com.example.ui.theme.ArcadeTertiaryContainer
import com.example.ui.theme.ArcadeTertiaryFixed
import com.example.ui.theme.EmeraldWin
import com.example.ui.theme.TournamentGold
import com.example.viewmodel.ClassicMatchState

@Composable
fun OnlineLobbyScreen(
    state: ClassicMatchState,
    theme: BingoTheme,
    currentUserName: String = "Player",
    currentUserId: String = "",
    onlinePlayers: List<OnlinePlayer> = emptyList(),
    incomingChallenges: List<FriendChallenge> = emptyList(),
    outgoingChallenge: FriendChallenge? = null,
    onCreateRoom: () -> Unit,
    onJoinRoom: (String) -> Unit,
    onQuickMatch: () -> Unit,
    onCancelSearch: () -> Unit = {},
    onSetChallengeLevel: (Int) -> Unit = {},
    onAddFriend: (String, Int) -> Unit = { _, _ -> },
    onUpdateFriendLevel: (String, Int) -> Unit = { _, _ -> },
    onChallengeFriend: (FriendProfile) -> Unit = {},
    onSendCustomChallenge: (String, Int) -> Unit = { _, _ -> },
    onStartCustomDuel: (String, Int) -> Unit = { _, _ -> },
    onAcceptChallenge: (FriendChallenge) -> Unit = {},
    onRejectChallenge: (FriendChallenge) -> Unit = {},
    onCancelOutgoingChallenge: () -> Unit = {},
    onProceedToSetup: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    BackHandler { onBack() }

    var selectedTab by remember { mutableStateOf("ranked") } // "ranked", "private", "online"
    var pinInputValue by remember { mutableStateOf("") }
    var lobbyFormat by remember { mutableStateOf("1v1") }
    var dropSpeed by remember { mutableStateOf("15s") }
    var showChallengeModal by remember { mutableStateOf(false) }
    var challengeTargetName by remember { mutableStateOf("") }
    var challengeTargetRank by remember { mutableStateOf("") }

    val activeRoomCode = state.roomCode ?: "7842"

    // Infinite animation for live radar sweep
    val infiniteTransition = rememberInfiniteTransition(label = "radar")
    val radarAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radarAngle"
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = ArcadeBackground,
        topBar = {
            ArcadeTopHeader(
                title = "Online Lobby",
                subtitle = "Matchmaking & Rooms",
                onBack = onBack
            )
        },
        bottomBar = {
            ArcadeBottomNavigationBar(
                currentRoute = "online_lobby",
                onNavigate = { route ->
                    if (route == "home") onBack()
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
                // 1. Mode Quick Tabs
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = ArcadeSurfaceContainerHigh
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        LobbyTabPill(
                            label = "Ranked",
                            icon = Icons.Default.Radar,
                            isSelected = selectedTab == "ranked",
                            modifier = Modifier.weight(1f),
                            onClick = { selectedTab = "ranked" }
                        )
                        LobbyTabPill(
                            label = "Private",
                            icon = Icons.Default.Lock,
                            isSelected = selectedTab == "private",
                            modifier = Modifier.weight(1f),
                            onClick = { selectedTab = "private" }
                        )
                        LobbyTabPill(
                            label = "Online",
                            icon = Icons.Default.PersonPinCircle,
                            isSelected = selectedTab == "online",
                            modifier = Modifier.weight(1f),
                            onClick = { selectedTab = "online" }
                        )
                    }
                }

                // TAB 1: Fast Matchmaking Radar Card
                if (selectedTab == "ranked") {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(3.dp, RoundedCornerShape(18.dp)),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = ArcadeSurfaceContainer)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(ArcadeSecondaryContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.MilitaryTech,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "RANKED SEASON 4",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Black,
                                            color = ArcadePrimary
                                        )
                                        Text(
                                            text = "1v1 Pulse Duel",
                                            fontSize = 17.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = ArcadeOnSurface
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(ArcadeSurfaceContainerLowest)
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(EmeraldWin)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "24ms • 60 FPS",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ArcadeOnSurface
                                        )
                                    }
                                }
                            }

                            // Live Radar Graphic
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(ArcadeSurfaceContainerLowest)
                                    .padding(vertical = 24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier.size(140.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        // Concentric Rings
                                        Canvas(modifier = Modifier.fillMaxSize()) {
                                            val center = Offset(size.width / 2f, size.height / 2f)
                                            drawCircle(
                                                color = ArcadePrimary.copy(alpha = 0.1f),
                                                radius = size.width * 0.48f,
                                                style = Stroke(width = 1.5.dp.toPx())
                                            )
                                            drawCircle(
                                                color = ArcadePrimary.copy(alpha = 0.15f),
                                                radius = size.width * 0.32f,
                                                style = Stroke(width = 1.5.dp.toPx())
                                            )
                                            drawCircle(
                                                color = ArcadePrimary.copy(alpha = 0.2f),
                                                radius = size.width * 0.18f,
                                                style = Stroke(width = 1.5.dp.toPx())
                                            )
                                        }

                                        // Rotating Radar Sweep
                                        Canvas(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .rotate(radarAngle)
                                        ) {
                                            val center = Offset(size.width / 2f, size.height / 2f)
                                            drawLine(
                                                color = ArcadePrimaryContainer,
                                                start = center,
                                                end = Offset(center.x, 0f),
                                                strokeWidth = 2.dp.toPx()
                                            )
                                        }

                                        // Center Gamer Avatar Orb
                                        Box(
                                            modifier = Modifier
                                                .size(54.dp)
                                                .clip(CircleShape)
                                                .background(ArcadePrimary),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(text = "👑", fontSize = 24.sp)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = if (state.isOnlineSearching) "Searching Opponent..." else "Ready for Ranked Match",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = ArcadeOnSurface
                                    )

                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(top = 4.dp)
                                    ) {
                                        Text(
                                            text = "Est. wait: 3-5s",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ArcadeOnSurfaceVariant
                                        )
                                        Text(text = "•", color = ArcadeOutlineVariant)
                                        Text(
                                            text = "Gold II (1,840 RP)",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ArcadePrimary
                                        )
                                    }
                                }
                            }

                            // Actions
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = onQuickMatch,
                                    colors = ButtonDefaults.buttonColors(containerColor = ArcadePrimary),
                                    shape = RoundedCornerShape(24.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("instant_quick_match_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Bolt,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "INSTANT QUICK MATCH",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
                                    )
                                }

                                Button(
                                    onClick = onCancelSearch,
                                    colors = ButtonDefaults.buttonColors(containerColor = ArcadeSurfaceContainerHigh),
                                    shape = RoundedCornerShape(24.dp),
                                    modifier = Modifier.height(48.dp)
                                ) {
                                    Text("Cancel", color = ArcadeOnSurface, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // TAB 2: Private Room (Host & Join)
                if (selectedTab == "private") {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Host Private Room Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = ArcadeSurfaceContainerLowest),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(ArcadePrimaryFixed),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.MeetingRoom,
                                                contentDescription = null,
                                                tint = ArcadePrimary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = "Host Private Room",
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = ArcadeOnSurface
                                            )
                                            Text(
                                                text = "Create your parlor & invite friends",
                                                fontSize = 11.sp,
                                                color = ArcadeOnSurfaceVariant
                                            )
                                        }
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(ArcadeSurfaceContainer)
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "FREE PLAY",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Black,
                                            color = ArcadePrimary
                                        )
                                    }
                                }

                                // Generated PIN Display
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(ArcadeSurfaceContainerHigh)
                                        .padding(14.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "YOUR 4-DIGIT ROOM PIN",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ArcadeOnSurfaceVariant,
                                            letterSpacing = 0.8.sp
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))

                                        // 4 PIN blocks
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            val pinDigits = activeRoomCode.take(4).padEnd(4, '0')
                                            pinDigits.forEach { digit ->
                                                Box(
                                                    modifier = Modifier
                                                        .size(width = 46.dp, height = 54.dp)
                                                        .clip(RoundedCornerShape(10.dp))
                                                        .background(ArcadeSurfaceContainerLowest)
                                                        .shadow(1.dp, RoundedCornerShape(10.dp)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = digit.toString(),
                                                        fontSize = 24.sp,
                                                        fontWeight = FontWeight.Black,
                                                        color = ArcadePrimary
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Button(
                                                onClick = {
                                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                    clipboard.setPrimaryClip(ClipData.newPlainText("Bingo Room PIN", activeRoomCode))
                                                    Toast.makeText(context, "Copied Room PIN: $activeRoomCode", Toast.LENGTH_SHORT).show()
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = ArcadeSurfaceContainerLowest),
                                                shape = RoundedCornerShape(20.dp),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.ContentCopy,
                                                    contentDescription = null,
                                                    tint = ArcadePrimary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Copy Code", color = ArcadePrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }

                                            Button(
                                                onClick = {
                                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                                        type = "text/plain"
                                                        putExtra(Intent.EXTRA_TEXT, "Join my Arcade Bingo Duel with PIN: $activeRoomCode")
                                                    }
                                                    context.startActivity(Intent.createChooser(shareIntent, "Share Room PIN"))
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = ArcadeSurfaceContainerLowest),
                                                shape = RoundedCornerShape(20.dp),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Share,
                                                    contentDescription = null,
                                                    tint = ArcadeOnSurface,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Share Link", color = ArcadeOnSurface, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }

                                // Format selector
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Lobby Format",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ArcadeOnSurfaceVariant
                                    )
                                    Row(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(ArcadeSurfaceContainer)
                                            .padding(2.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(14.dp))
                                                .background(if (lobbyFormat == "1v1") ArcadePrimary else Color.Transparent)
                                                .clickable { lobbyFormat = "1v1" }
                                                .padding(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = "1V1 DUEL",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (lobbyFormat == "1v1") Color.White else ArcadeOnSurfaceVariant
                                            )
                                        }
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(14.dp))
                                                .background(if (lobbyFormat == "4p") ArcadePrimary else Color.Transparent)
                                                .clickable { lobbyFormat = "4p" }
                                                .padding(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = "4-P BLITZ",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (lobbyFormat == "4p") Color.White else ArcadeOnSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Join Friend's Room Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = ArcadeSurfaceContainerHigh)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(ArcadeSecondaryFixed),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Pin,
                                            contentDescription = null,
                                            tint = ArcadeSecondary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Join Friend's Room",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = ArcadeOnSurface
                                        )
                                        Text(
                                            text = "Enter host's 4-digit numeric code",
                                            fontSize = 11.sp,
                                            color = ArcadeOnSurfaceVariant
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = pinInputValue,
                                        onValueChange = { if (it.length <= 6) pinInputValue = it.uppercase() },
                                        placeholder = { Text("••••", letterSpacing = 4.sp) },
                                        singleLine = true,
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("lobby_pin_input"),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = ArcadeSurfaceContainerLowest,
                                            unfocusedContainerColor = ArcadeSurfaceContainerLowest,
                                            focusedBorderColor = ArcadePrimary,
                                            unfocusedBorderColor = Color.Transparent
                                        )
                                    )

                                    Button(
                                        onClick = {
                                            if (pinInputValue.isNotBlank()) {
                                                onJoinRoom(pinInputValue.trim())
                                            } else {
                                                Toast.makeText(context, "Enter a 4-digit PIN", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = ArcadePrimary),
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier
                                            .height(52.dp)
                                            .testTag("lobby_join_btn")
                                    ) {
                                        Text("Join", color = Color.White, fontWeight = FontWeight.Black)
                                    }
                                }
                            }
                        }
                    }
                }

                // TAB 3: Online Players List (Arcade Lounge)
                if (selectedTab == "online") {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Arcade Lounge",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = ArcadeOnSurface
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(ArcadePrimaryFixed)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "142 ONLINE",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        color = ArcadePrimary
                                    )
                                }
                            }
                        }

                        // Players List
                        val samplePlayers = listOf(
                            Triple("CyberQueen", "Plat III", "👑"),
                            Triple("ViperX", "Gold I", "⚡"),
                            Triple("LuckyDauber", "Silver II", "🍀"),
                            Triple("StarStriker", "Gold II", "⭐"),
                            Triple("NeonPulse", "Diamond I", "🔮")
                        )

                        samplePlayers.forEach { (name, rank, avatar) ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = ArcadeSurfaceContainerLowest),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(44.dp)
                                                .clip(CircleShape)
                                                .background(ArcadeSurfaceContainerHigh),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(text = avatar, fontSize = 22.sp)
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = name,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = ArcadeOnSurface
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(ArcadeTertiaryFixed)
                                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                                ) {
                                                    Text(
                                                        text = rank,
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Black,
                                                        color = ArcadeTertiary
                                                    )
                                                }
                                            }
                                            Text(
                                                text = "Looking for Match • 18ms",
                                                fontSize = 11.sp,
                                                color = EmeraldWin
                                            )
                                        }
                                    }

                                    Button(
                                        onClick = {
                                            challengeTargetName = name
                                            challengeTargetRank = rank
                                            showChallengeModal = true
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = ArcadeTertiary),
                                        shape = RoundedCornerShape(20.dp),
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                        modifier = Modifier.height(36.dp)
                                    ) {
                                        Text(
                                            text = "Challenge ⚔️",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Modal: Challenge Player Confirmation
    if (showChallengeModal) {
        Dialog(onDismissRequest = { showChallengeModal = false }) {
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
                    Text(
                        text = "Challenge $challengeTargetName",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = ArcadeOnSurface
                    )

                    Text(
                        text = "Start a 1v1 Ranked Duel against $challengeTargetName ($challengeTargetRank). Ready up your tactical board!",
                        fontSize = 13.sp,
                        color = ArcadeOnSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { showChallengeModal = false },
                            colors = ButtonDefaults.buttonColors(containerColor = ArcadeSurfaceContainerHigh),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel", color = ArcadeOnSurface, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                showChallengeModal = false
                                onStartCustomDuel(challengeTargetName, 15)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ArcadeTertiary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("confirm_challenge_btn")
                        ) {
                            Text("Duel Now ⚔️", color = Color.White, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LobbyTabPill(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) ArcadePrimary else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) Color.White else ArcadeOnSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else ArcadeOnSurfaceVariant
            )
        }
    }
}
