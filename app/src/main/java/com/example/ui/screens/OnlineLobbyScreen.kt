package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BingoTheme
import com.example.model.ChallengeStatus
import com.example.model.FriendChallenge
import com.example.model.FriendProfile
import com.example.model.OnlinePlayer
import com.example.util.NetworkUtils
import com.example.viewmodel.ClassicMatchState

@OptIn(ExperimentalMaterial3Api::class)
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
    onAcceptChallenge: (FriendChallenge) -> Unit = {},
    onRejectChallenge: (FriendChallenge) -> Unit = {},
    onCancelOutgoingChallenge: () -> Unit = {},
    onProceedToSetup: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var joinCodeInput by remember { mutableStateOf("") }
    var isOnlineConnected by remember { mutableStateOf(NetworkUtils.isNetworkAvailable(context)) }
    var networkTypeName by remember { mutableStateOf(NetworkUtils.getNetworkTypeName(context)) }

    var showAddFriendDialog by remember { mutableStateOf(false) }
    var showCustomChallengeDialog by remember { mutableStateOf(false) }
    var showShareDialog by remember { mutableStateOf(false) }
    var newFriendName by remember { mutableStateOf("") }
    var newFriendLevel by remember { mutableIntStateOf(1) }
    var customTargetName by remember { mutableStateOf("") }
    var customTargetLevel by remember { mutableIntStateOf(1) }
    var selectedFriendForChallenge by remember { mutableStateOf<FriendProfile?>(null) }

    // Intercept hardware and gesture back button to cancel search and navigate safely
    BackHandler {
        onCancelSearch()
        onBack()
    }

    fun refreshNetwork() {
        isOnlineConnected = NetworkUtils.isNetworkAvailable(context)
        networkTypeName = NetworkUtils.getNetworkTypeName(context)
    }

    fun copyRoomCodeOnly(roomCode: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Bingo Room Code", roomCode)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Room Code #$roomCode copied! Send to your friend.", Toast.LENGTH_SHORT).show()
    }

    fun shareChallengeViaWhatsApp(roomCode: String, level: Int, friendName: String? = null) {
        val targetName = friendName?.let { " $it" } ?: ""
        val message = """
🎮 *KIDS MIND BINGO 5x5 DUEL!* 🧠✨

Hey$targetName! I have challenged you to a mind-sharpening 5x5 BINGO match!

🏆 *Challenge Level:* Level $level
🔑 *Room Code:* *$roomCode*

👉 *How to play together:*
1. Open *Kids Mind Bingo* app
2. Go to 'Play Online'
3. Enter Room Code: *$roomCode*
4. Fill your 1-25 board and let's see who completes 5 lines first!

Can you beat my brain strategy? Join now! 🎯⚡
        """.trimIndent()

        val whatsappIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, message)
            setPackage("com.whatsapp")
        }
        try {
            context.startActivity(whatsappIntent)
        } catch (_: Exception) {
            // WhatsApp not installed, copy to clipboard instead of opening Bluetooth Quick Share chooser
            copyRoomCodeOnly(roomCode)
            showShareDialog = true
        }
    }

    fun shareChallengeViaSms(roomCode: String, level: Int) {
        val message = "Play 5x5 Kids Mind Bingo with me! Room Code: #$roomCode (Level $level). Open app and tap Play Online -> Enter Room Code!"
        try {
            val smsIntent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("sms:")
                putExtra("sms_body", message)
            }
            context.startActivity(smsIntent)
        } catch (_: Exception) {
            copyRoomCodeOnly(roomCode)
        }
    }

    fun shareChallengeGeneral(roomCode: String, level: Int) {
        // Open custom in-app share dialog to prevent getting stuck in Android Bluetooth Quick Share!
        showShareDialog = true
    }

    Scaffold(
        containerColor = theme.backgroundGradients.first(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Play Online Multiplayer 🌐",
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        onCancelSearch()
                        onBack()
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        onCancelSearch()
                        onBack()
                    }) {
                        Icon(
                            Icons.Default.Home,
                            contentDescription = "Home",
                            tint = Color.White
                        )
                    }
                    IconButton(onClick = { refreshNetwork() }) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Refresh Network",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = theme.headerBgGradients.first()
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Brush.verticalGradient(theme.backgroundGradients))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // User Profile & Tag Banner
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.15f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("👤", fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    currentUserName,
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 14.sp
                                )
                                Text(
                                    "ID: ${currentUserId.take(12).ifEmpty { "guest" }}",
                                    color = Color(0xFFFFD700),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Button(
                            onClick = { showCustomChallengeDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.height(34.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("CHALLENGE ANYONE ⚔️", fontWeight = FontWeight.Black, fontSize = 11.sp, color = Color(0xFF1A0033))
                        }
                    }
                }

                // Incoming Challenges from Friends (Real-Time Firestore Alerts)
                incomingChallenges.forEach { challenge ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1B4B)),
                        border = BorderStroke(2.dp, Color(0xFFFFD700)),
                        elevation = CardDefaults.cardElevation(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("🚨", fontSize = 22.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "INCOMING DUEL CHALLENGE!",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 14.sp,
                                        color = Color(0xFFFFD700)
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFFF3D00).copy(alpha = 0.25f))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text("PENDING", color = Color(0xFFFF8A80), fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF6366F1)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(challenge.senderAvatar.ifEmpty { "⚔️" }, fontSize = 22.sp)
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "${challenge.senderName} (Level ${challenge.senderLevel})",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 14.sp,
                                        color = Color.White
                                    )
                                    Text(
                                        "Aapko 5x5 Mind Bingo duel ke liye challenge kiya hai! Room #${challenge.roomCode}",
                                        fontSize = 12.sp,
                                        color = Color(0xFFE0E7FF)
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = { onAcceptChallenge(challenge) },
                                    modifier = Modifier.weight(1f).height(44.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("ACCEPT DUEL ⚔️", fontWeight = FontWeight.Black, color = Color.White)
                                }
                                OutlinedButton(
                                    onClick = { onRejectChallenge(challenge) },
                                    modifier = Modifier.weight(0.8f).height(44.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF8A80)),
                                    border = BorderStroke(1.dp, Color(0xFFFF8A80)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("DECLINE 🛡️", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // 1. Network / Wi-Fi Connectivity Status Banner
                if (isOnlineConnected) {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B5E20)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Wifi,
                                contentDescription = "Online",
                                tint = Color(0xFF69F0AE),
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Connected via $networkTypeName",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "Online Matchmaking & Live Rooms Active",
                                    color = Color(0xFFB9F6CA),
                                    fontSize = 11.sp
                                )
                            }
                            Text(
                                "ONLINE 🟢",
                                color = Color(0xFF69F0AE),
                                fontWeight = FontWeight.Black,
                                fontSize = 11.sp
                            )
                        }
                    }
                } else {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFB71C1C)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { refreshNetwork() }
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.WifiOff,
                                contentDescription = "Offline",
                                tint = Color(0xFFFF8A80),
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "⚠️ Wi-Fi / Mobile Internet Required!",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    "Online khelne ke liye apna Wi-Fi ya Mobile Data on karein. Tap to re-check.",
                                    color = Color(0xFFFFCDD2),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }

                // 2. Quick Match (1 vs 1) with 5 to 10 Seconds Matchmaking
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = theme.cardBgColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF00E676).copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Public, contentDescription = null, tint = Color(0xFF00E676))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    "Quick Match (1 vs 1 Duel) ⚡",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 16.sp,
                                    color = theme.textPrimary
                                )
                                Text(
                                    "Matchmaking in 5 - 10 seconds with active players",
                                    fontSize = 11.sp,
                                    color = theme.textPrimary.copy(alpha = 0.75f)
                                )
                            }
                        }

                        if (state.isOnlineSearching) {
                            // Active Searching State
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color.Black.copy(alpha = 0.25f))
                                    .padding(14.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    CircularProgressIndicator(
                                        color = Color(0xFFFFD700),
                                        strokeWidth = 3.dp,
                                        modifier = Modifier.size(36.dp)
                                    )
                                    Text(
                                        state.onlineStatusMessage,
                                        color = Color(0xFFFFD700),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        textAlign = TextAlign.Center
                                    )
                                    LinearProgressIndicator(
                                        progress = { (state.onlineSearchElapsedSec / 5f).coerceIn(0f, 1f) },
                                        color = Color(0xFFFFD700),
                                        trackColor = Color.White.copy(alpha = 0.2f),
                                        modifier = Modifier
                                            .fillMaxWidth(0.8f)
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                    )
                                    Text(
                                        text = if (state.onlineSearchElapsedSec < 5) {
                                            "⏱️ Real Player Search: ${5 - state.onlineSearchElapsedSec}s remaining"
                                        } else {
                                            "⚡ Starting duel arena now!"
                                        },
                                        color = Color.White.copy(alpha = 0.9f),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )

                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        OutlinedButton(
                                            onClick = onCancelSearch,
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF8A80)),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Text("Cancel Search", fontSize = 12.sp)
                                        }

                                        Button(
                                            onClick = {
                                                onCancelSearch()
                                                onBack()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Text("Go Home 🏠", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        } else {
                            Text(
                                "Auto-pairs you with a smart kid or friend online for an exciting 5x5 mind game!",
                                fontSize = 12.sp,
                                color = theme.textPrimary.copy(alpha = 0.8f)
                            )

                            Button(
                                onClick = {
                                    refreshNetwork()
                                    if (isOnlineConnected) {
                                        onQuickMatch()
                                    } else {
                                        Toast.makeText(context, "Please connect to Wi-Fi or Mobile Network first!", Toast.LENGTH_LONG).show()
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("quick_match_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = theme.primaryColor),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("FIND ONLINE MATCH NOW", fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }

                // 3. Create Challenge for Friends & WhatsApp Sharing
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = theme.cardBgColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFFB300).copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Color(0xFFFFB300))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    "Challenge a Friend (WhatsApp / Link) 🏆",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 15.sp,
                                    color = theme.textPrimary
                                )
                                Text(
                                    "Create custom level challenge & send room code",
                                    fontSize = 11.sp,
                                    color = theme.textPrimary.copy(alpha = 0.75f)
                                )
                            }
                        }

                        // Challenge Level Picker (1 to 5)
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                "Choose Challenge Level: Level ${state.challengeLevel}",
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp,
                                color = Color(0xFF0F172A)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                for (lvl in 1..5) {
                                    val isSelected = state.challengeLevel == lvl
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) Color(0xFFFFB300) else Color(0xFFF1F5F9))
                                            .border(
                                                width = if (isSelected) 1.5.dp else 1.dp,
                                                color = if (isSelected) Color(0xFFFF8F00) else Color(0xFFCBD5E1),
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .clickable { onSetChallengeLevel(lvl) }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            "Lvl $lvl",
                                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = if (isSelected) Color(0xFF1A0033) else Color(0xFF334155)
                                        )
                                    }
                                }
                            }
                        }

                        // Room Code Box: Generate unique code when user creates room or challenges friend!
                        if (state.roomCode.isBlank()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xFF0F172A))
                                    .border(1.5.dp, Color(0xFFFFD700).copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                                    .padding(14.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        "Har match ke liye alag aur unique complex room code banayein:",
                                        fontSize = 12.sp,
                                        color = Color(0xFF94A3B8),
                                        textAlign = TextAlign.Center
                                    )
                                    Button(
                                        onClick = { onCreateRoom() },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                                        modifier = Modifier.fillMaxWidth().height(46.dp)
                                    ) {
                                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFF1A0033))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            "GENERATE UNIQUE ROOM CODE 🔑",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color(0xFF1A0033)
                                        )
                                    }
                                }
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xFF0F172A))
                                    .border(1.5.dp, Color(0xFFFFD700).copy(alpha = 0.8f), RoundedCornerShape(14.dp))
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            "Your Unique Room Code (Alphanumeric):",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFF94A3B8)
                                        )
                                        Text(
                                            "#${state.roomCode}",
                                            fontSize = 24.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color(0xFFFFD700)
                                        )
                                    }

                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        IconButton(
                                            onClick = {
                                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                val clip = ClipData.newPlainText("Bingo Room Code", state.roomCode)
                                                clipboard.setPrimaryClip(clip)
                                                Toast.makeText(context, "Room Code #${state.roomCode} copied!", Toast.LENGTH_SHORT).show()
                                            }
                                        ) {
                                            Icon(
                                                Icons.Default.ContentCopy,
                                                contentDescription = "Copy Code",
                                                tint = Color.White
                                            )
                                        }

                                        Button(
                                            onClick = { onCreateRoom() },
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                            modifier = Modifier.height(38.dp)
                                        ) {
                                            Text(
                                                "New Code",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Black,
                                                color = Color(0xFF1A0033),
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Bluetooth / Quick Share clarification banner
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = null,
                                    tint = Color(0xFF38BDF8),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "✨ No Bluetooth / Quick Share needed! Simply give your 6-digit room code to your friend to join over Wi-Fi/Internet.",
                                    color = Color(0xFFE0F2FE),
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp
                                )
                            }
                        }

                        // Host Direct Start Board Setup Button (Don't make host wait!)
                        Button(
                            onClick = onProceedToSetup,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("host_start_setup_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB300)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color(0xFF1A0033))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "START BOARD SETUP (HOST) 🎯",
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp,
                                color = Color(0xFF1A0033)
                            )
                        }

                        // Share Action Buttons: WhatsApp & In-App Share
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    shareChallengeViaWhatsApp(state.roomCode, state.challengeLevel)
                                },
                                modifier = Modifier.weight(1f).height(46.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Send, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "WhatsApp 📲",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.sp,
                                    color = Color.White,
                                    maxLines = 1
                                )
                            }

                            Button(
                                onClick = {
                                    showShareDialog = true
                                },
                                modifier = Modifier.weight(1f).height(46.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0288D1))
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "Share Code 🔑",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.sp,
                                    color = Color.White,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                // 4. Join Existing Private Room
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = theme.cardBgColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF00E5FF).copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.MeetingRoom, contentDescription = null, tint = Color(0xFF00E5FF))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                "Join with Friend's Code 🔑",
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp,
                                color = theme.textPrimary
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = joinCodeInput,
                                onValueChange = { if (it.length <= 8) joinCodeInput = it.uppercase() },
                                label = { Text("Enter room code (Letters + Numbers)") },
                                placeholder = { Text("e.g. BG7X9K") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    capitalization = KeyboardCapitalization.Characters,
                                    keyboardType = KeyboardType.Ascii
                                ),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = Color(0xFF00E5FF),
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.4f)
                                )
                            )

                            Button(
                                onClick = {
                                    refreshNetwork()
                                    if (!isOnlineConnected) {
                                        Toast.makeText(context, "Please connect to Wi-Fi or Mobile Network first!", Toast.LENGTH_LONG).show()
                                    } else if (joinCodeInput.trim().length >= 4) {
                                        onJoinRoom(joinCodeInput.trim())
                                    } else {
                                        Toast.makeText(context, "Please enter a valid room code!", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                                modifier = Modifier.height(54.dp)
                            ) {
                                Text("JOIN", fontWeight = FontWeight.Black, color = Color(0xFF002233))
                            }
                        }

                        Text(
                            text = "Status: ${state.onlineStatusMessage}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF69F0AE)
                        )
                    }
                }

                // 4.5. Active Online Players (Real-Time Firebase Firestore)
                if (onlinePlayers.isNotEmpty()) {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF16A34A))
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "Online Players in Arena 🟢",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 15.sp,
                                        color = Color(0xFF0F172A)
                                    )
                                }
                                Text(
                                    "${onlinePlayers.size} active",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF16A34A)
                                )
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                onlinePlayers.take(5).forEach { player ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(0xFFF1F5F9))
                                            .padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFFE0E7FF)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(player.avatar, fontSize = 20.sp)
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(
                                                    player.name,
                                                    fontWeight = FontWeight.Black,
                                                    fontSize = 13.sp,
                                                    color = Color(0xFF0F172A)
                                                )
                                                Text(
                                                    "Level ${player.level} • Ready to duel",
                                                    fontSize = 11.sp,
                                                    color = Color(0xFF64748B)
                                                )
                                            }
                                        }

                                        Button(
                                            onClick = {
                                                onSendCustomChallenge(player.name, player.level)
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB300)),
                                            shape = RoundedCornerShape(10.dp),
                                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                            modifier = Modifier.height(34.dp)
                                        ) {
                                            Text(
                                                "CHALLENGE ⚔️",
                                                fontWeight = FontWeight.Black,
                                                fontSize = 11.sp,
                                                color = Color(0xFF1A0033)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 5. Friends & Level Management (Dost & Levels)
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f, fill = false)
                            ) {
                                Icon(Icons.Default.People, contentDescription = null, tint = Color(0xFFE91E63))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Friends & Levels (Dost) 👥",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 16.sp,
                                    color = Color(0xFF0F172A)
                                )
                            }

                            Button(
                                onClick = { showAddFriendDialog = true },
                                shape = RoundedCornerShape(20.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63)),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add Friend", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            state.friendsList.forEach { friend ->
                                Card(
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            modifier = Modifier.weight(1f),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFFEDE9FE)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(text = friend.avatarEmoji, fontSize = 22.sp)
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        friend.name,
                                                        fontWeight = FontWeight.Black,
                                                        fontSize = 14.sp,
                                                        color = Color(0xFF0F172A)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Box(
                                                        modifier = Modifier
                                                            .size(8.dp)
                                                            .clip(CircleShape)
                                                            .background(if (friend.isOnline) Color(0xFF16A34A) else Color(0xFF94A3B8))
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        if (friend.isOnline) "Online" else "Offline",
                                                        fontSize = 10.sp,
                                                        color = if (friend.isOnline) Color(0xFF16A34A) else Color(0xFF64748B),
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                                Text(
                                                    "Lvl ${friend.level} • ${friend.statsDesc}",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = Color(0xFF475569)
                                                )
                                            }
                                        }

                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Quick Level Up/Down button
                                            Box(
                                                modifier = Modifier
                                                    .size(34.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFFFEF3C7))
                                                    .clickable {
                                                        val newLvl = if (friend.level >= 5) 1 else friend.level + 1
                                                        onUpdateFriendLevel(friend.name, newLvl)
                                                    },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    Icons.Default.Star,
                                                    contentDescription = "Change Level",
                                                    tint = Color(0xFFD97706),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }

                                            // Direct WhatsApp challenge to this friend
                                            Box(
                                                modifier = Modifier
                                                    .size(34.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFFDCFCE7))
                                                    .clickable {
                                                        shareChallengeViaWhatsApp(state.roomCode, friend.level, friend.name)
                                                    },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    Icons.Default.Send,
                                                    contentDescription = "WhatsApp",
                                                    tint = Color(0xFF16A34A),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }

                                            // Challenge Button
                                            Button(
                                                onClick = {
                                                    refreshNetwork()
                                                    if (!isOnlineConnected) {
                                                        Toast.makeText(context, "Please connect to Wi-Fi or Mobile Network first!", Toast.LENGTH_SHORT).show()
                                                    } else {
                                                        onChallengeFriend(friend)
                                                    }
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB300)),
                                                shape = RoundedCornerShape(10.dp),
                                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                                modifier = Modifier.height(34.dp)
                                            ) {
                                                Text(
                                                    "DUEL ⚔️",
                                                    fontWeight = FontWeight.Black,
                                                    fontSize = 12.sp,
                                                    color = Color(0xFF1A0033)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 6. Big Proceed to Board Setup Button
                Button(
                    onClick = onProceedToSetup,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("online_proceed_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        "SET UP YOUR 1-25 BOARD 🎯",
                        color = Color(0xFF1A0033),
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }

    // Add Friend Dialog
    if (showAddFriendDialog) {
        AlertDialog(
            onDismissRequest = { showAddFriendDialog = false },
            title = {
                Text("Add New Friend (Dost) 👥", fontWeight = FontWeight.Black)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = newFriendName,
                        onValueChange = { newFriendName = it },
                        label = { Text("Friend's Name") },
                        placeholder = { Text("e.g. Samar_Mind") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Friend's Skill Level:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        for (lvl in 1..5) {
                            val selected = newFriendLevel == lvl
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (selected) Color(0xFFFFB300) else Color(0xFFECEFF1))
                                    .clickable { newFriendLevel = lvl }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Lvl $lvl",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 12.sp,
                                    color = if (selected) Color(0xFF1A0033) else Color(0xFF455A64)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newFriendName.isNotBlank()) {
                            onAddFriend(newFriendName.trim(), newFriendLevel)
                            newFriendName = ""
                            newFriendLevel = 1
                            showAddFriendDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
                ) {
                    Text("Add Friend")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showAddFriendDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Real-Time Outgoing Challenge Modal (Displays Pending, Accepted, or Rejected in Gaming Language)
    if (outgoingChallenge != null) {
        val isPending = outgoingChallenge.status == ChallengeStatus.PENDING
        val isAccepted = outgoingChallenge.status == ChallengeStatus.ACCEPTED
        val isRejected = outgoingChallenge.status == ChallengeStatus.REJECTED

        AlertDialog(
            onDismissRequest = {
                if (!isPending) {
                    onCancelOutgoingChallenge()
                }
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        when {
                            isAccepted -> "⚡"
                            isRejected -> "🛡️"
                            else -> "⚔️"
                        },
                        fontSize = 24.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when {
                            isAccepted -> "DUEL COMMENCED! ⚡"
                            isRejected -> "CHALLENGE DECLINED 🛡️"
                            else -> "CHALLENGE TRANSMITTED ⚔️"
                        },
                        fontWeight = FontWeight.Black,
                        fontSize = 17.sp,
                        color = when {
                            isAccepted -> Color(0xFF00C853)
                            isRejected -> Color(0xFFD50000)
                            else -> Color(0xFF6200EA)
                        }
                    )
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = when {
                                isAccepted -> Color(0xFFE8F5E9)
                                isRejected -> Color(0xFFFFEBEE)
                                else -> Color(0xFFEDE7F6)
                            }
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Opponent: ${outgoingChallenge.receiverName}",
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp,
                                color = Color(0xFF1E293B)
                            )
                            Text(
                                text = "Room Code: #${outgoingChallenge.roomCode}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF475569)
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = when {
                                    isAccepted -> "⚡ STATUS: ACCEPTED!"
                                    isRejected -> "🛡️ STATUS: DECLINED (Retreated)"
                                    else -> "⏳ STATUS: PENDING (Awaiting Opponent...)"
                                },
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp,
                                color = when {
                                    isAccepted -> Color(0xFF2E7D32)
                                    isRejected -> Color(0xFFC62828)
                                    else -> Color(0xFF512DA8)
                                }
                            )

                            if (isPending) {
                                LinearProgressIndicator(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(5.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = Color(0xFF6200EA)
                                )
                                Text(
                                    text = "📲 Live duel request sent to ${outgoingChallenge.receiverName}'s phone! Jaise hi woh Accept karenge, aap dono ka match automatically shuru ho jayega.",
                                    fontSize = 11.sp,
                                    textAlign = TextAlign.Center,
                                    color = Color(0xFF475569)
                                )
                            } else if (isAccepted) {
                                Text(
                                    text = "🎉 ${outgoingChallenge.receiverName} ne challenge ACCEPT kar liya! Boards sync ho chuke hain. Game arena me enter karein!",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    color = Color(0xFF2E7D32)
                                )
                            } else if (isRejected) {
                                Text(
                                    text = "🛡️ ${outgoingChallenge.receiverName} ne challenge DECLINE kar diya. Aap kisi aur dost ko challenge kar sakte hain ya Quick Match try karein!",
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center,
                                    color = Color(0xFFC62828)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                if (isAccepted) {
                    Button(
                        onClick = {
                            onCancelOutgoingChallenge()
                            onProceedToSetup()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("ENTER DUEL ARENA 🎯", color = Color.White, fontWeight = FontWeight.Black)
                    }
                } else if (isRejected) {
                    Button(
                        onClick = { onCancelOutgoingChallenge() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF455A64)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Dismiss")
                    }
                }
            },
            dismissButton = {
                if (isPending) {
                    OutlinedButton(
                        onClick = { onCancelOutgoingChallenge() },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Cancel Challenge ❌", color = Color(0xFFC62828))
                    }
                }
            }
        )
    }

    // Custom Challenge Dialog (Challenge Any Player or Friend by Name / User ID)
    if (showCustomChallengeDialog) {
        AlertDialog(
            onDismissRequest = { showCustomChallengeDialog = false },
            title = {
                Text("Send Duel Challenge ⚔️", fontWeight = FontWeight.Black)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = customTargetName,
                        onValueChange = { customTargetName = it },
                        label = { Text("Friend's Username or ID") },
                        placeholder = { Text("e.g. Samar_Mind") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Challenge Level (Skill):", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        for (lvl in 1..5) {
                            val selected = customTargetLevel == lvl
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (selected) Color(0xFFFFB300) else Color(0xFFECEFF1))
                                    .clickable { customTargetLevel = lvl }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Lvl $lvl",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 12.sp,
                                    color = if (selected) Color(0xFF1A0033) else Color(0xFF455A64)
                                )
                            }
                        }
                    }

                    Text(
                        "Challenge send karne par samne wale player ko 'Incoming Challenge' alert dikhega jisme woh Accept ya Reject kar sakte hain.",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (customTargetName.isNotBlank()) {
                            onSendCustomChallenge(customTargetName.trim(), customTargetLevel)
                            customTargetName = ""
                            customTargetLevel = 1
                            showCustomChallengeDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB300))
                ) {
                    Text("SEND CHALLENGE ⚔️", color = Color(0xFF1A0033), fontWeight = FontWeight.Black)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showCustomChallengeDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // In-App Share Room Code Dialog (Prevents Quick Share Bluetooth hang)
    if (showShareDialog) {
        var isCopied by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showShareDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🔑", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Share Room Code #${state.roomCode}",
                        fontWeight = FontWeight.Black,
                        fontSize = 17.sp,
                        color = Color(0xFF0F172A)
                    )
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Big Room Code display
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0F172A))
                            .padding(14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "YOUR 6-DIGIT ROOM CODE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF94A3B8)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "#${state.roomCode}",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFFFD700)
                            )
                        }
                    }

                    // Notice Card explaining No Bluetooth / No Quick Share needed
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBFDBFE)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = Color(0xFF2563EB),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Bluetooth ya Quick Share ki zaroorat nahi hai. Apne dost ko yeh 6-digit code bataiye, woh 'Join with Friend's Code' me enter karke jud jayenge!",
                                fontSize = 11.sp,
                                color = Color(0xFF1E40AF),
                                lineHeight = 15.sp
                            )
                        }
                    }

                    // 1-Tap Copy Button
                    Button(
                        onClick = {
                            copyRoomCodeOnly(state.roomCode)
                            isCopied = true
                        },
                        modifier = Modifier.fillMaxWidth().height(46.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isCopied) Color(0xFF16A34A) else Color(0xFF1E293B)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            if (isCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                            contentDescription = null,
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            if (isCopied) "COPIED TO CLIPBOARD! ✅" else "COPY CODE TO CLIPBOARD 📋",
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp,
                            color = Color.White
                        )
                    }

                    // WhatsApp Direct
                    Button(
                        onClick = {
                            shareChallengeViaWhatsApp(state.roomCode, state.challengeLevel)
                            showShareDialog = false
                        },
                        modifier = Modifier.fillMaxWidth().height(44.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Send on WhatsApp 📲", fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    // SMS Direct
                    OutlinedButton(
                        onClick = {
                            shareChallengeViaSms(state.roomCode, state.challengeLevel)
                            showShareDialog = false
                        },
                        modifier = Modifier.fillMaxWidth().height(44.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Sms, contentDescription = null, tint = Color(0xFF0288D1))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Send via SMS / Message 💬", fontWeight = FontWeight.Bold, color = Color(0xFF0288D1))
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showShareDialog = false
                        onProceedToSetup()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB300)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("START BOARD SETUP NOW 🎯", color = Color(0xFF1A0033), fontWeight = FontWeight.Black)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showShareDialog = false }) {
                    Text("Close ✕")
                }
            }
        )
    }
}
