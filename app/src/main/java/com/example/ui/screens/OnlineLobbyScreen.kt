package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
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
import com.example.model.FriendProfile
import com.example.util.NetworkUtils
import com.example.viewmodel.ClassicMatchState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnlineLobbyScreen(
    state: ClassicMatchState,
    theme: BingoTheme,
    onCreateRoom: () -> Unit,
    onJoinRoom: (String) -> Unit,
    onQuickMatch: () -> Unit,
    onCancelSearch: () -> Unit = {},
    onSetChallengeLevel: (Int) -> Unit = {},
    onAddFriend: (String, Int) -> Unit = { _, _ -> },
    onUpdateFriendLevel: (String, Int) -> Unit = { _, _ -> },
    onChallengeFriend: (FriendProfile) -> Unit = {},
    onProceedToSetup: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var joinCodeInput by remember { mutableStateOf("") }
    var isOnlineConnected by remember { mutableStateOf(NetworkUtils.isNetworkAvailable(context)) }
    var networkTypeName by remember { mutableStateOf(NetworkUtils.getNetworkTypeName(context)) }

    var showAddFriendDialog by remember { mutableStateOf(false) }
    var newFriendName by remember { mutableStateOf("") }
    var newFriendLevel by remember { mutableIntStateOf(1) }

    fun refreshNetwork() {
        isOnlineConnected = NetworkUtils.isNetworkAvailable(context)
        networkTypeName = NetworkUtils.getNetworkTypeName(context)
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
            // WhatsApp not installed, fallback to standard chooser
            val chooser = Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, message)
            }, "Share BINGO Challenge")
            context.startActivity(chooser)
        }
    }

    fun shareChallengeGeneral(roomCode: String, level: Int) {
        val message = """
🎮 *KIDS MIND BINGO 5x5 CHALLENGE!* 🧠✨

Hey! I've created a BINGO duel for you!
🏆 *Challenge Level:* Level $level
🔑 *Room Code:* *$roomCode*

1. Open *Kids Mind Bingo*
2. Tap 'Play Online'
3. Enter Room Code: *$roomCode*
4. Fill your 1-25 numbers and race to 5 lines! 🎯
        """.trimIndent()

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, message)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share Challenge with Friends"))
    }

    Scaffold(
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
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
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
                                    Text(
                                        "Searching time: ${state.onlineSearchElapsedSec}s (Connecting within 5-10s)",
                                        color = Color.White.copy(alpha = 0.8f),
                                        fontSize = 11.sp
                                    )

                                    OutlinedButton(
                                        onClick = onCancelSearch,
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF8A80)),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text("Cancel Search", fontSize = 12.sp)
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
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = theme.textPrimary
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
                                            .background(if (isSelected) Color(0xFFFFB300) else Color.White.copy(alpha = 0.15f))
                                            .border(
                                                1.dp,
                                                if (isSelected) Color(0xFFFFD54F) else Color.White.copy(alpha = 0.3f),
                                                RoundedCornerShape(8.dp)
                                            )
                                            .clickable { onSetChallengeLevel(lvl) }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            "Lvl $lvl",
                                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = if (isSelected) Color(0xFF1A0033) else Color.White
                                        )
                                    }
                                }
                            }
                        }

                        // Room Code Box with Copy & New Code
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.Black.copy(alpha = 0.3f))
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        "Room Code (Kamra Code):",
                                        fontSize = 11.sp,
                                        color = theme.textPrimary.copy(alpha = 0.7f)
                                    )
                                    Text(
                                        "#${state.roomCode}",
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFFFFD700)
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
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

                                    OutlinedButton(
                                        onClick = onCreateRoom,
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFFD700))
                                    ) {
                                        Text("New Code", fontSize = 12.sp)
                                    }
                                }
                            }
                        }

                        // Share Action Buttons: WhatsApp & Any App
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
                                    color = Color.White
                                )
                            }

                            OutlinedButton(
                                onClick = {
                                    shareChallengeGeneral(state.roomCode, state.challengeLevel)
                                },
                                modifier = Modifier.weight(1f).height(46.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Share Link 🔗", fontWeight = FontWeight.Bold, fontSize = 13.sp)
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
                                onValueChange = { if (it.length <= 6) joinCodeInput = it },
                                label = { Text("Enter 6-digit room code") },
                                placeholder = { Text("e.g. 742918") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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

                // 5. Friends & Level Management (Dost & Levels)
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = theme.cardBgColor),
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
                                Icon(Icons.Default.People, contentDescription = null, tint = Color(0xFFFF4081))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Friends & Levels (Dost) 👥",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 15.sp,
                                    color = theme.textPrimary
                                )
                            }

                            OutlinedButton(
                                onClick = { showAddFriendDialog = true },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF4081))
                            ) {
                                Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add Friend", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            state.friendsList.forEach { friend ->
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    friend.name,
                                                    fontWeight = FontWeight.Black,
                                                    fontSize = 14.sp,
                                                    color = Color.White
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(Color(0xFFFFB300))
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        "Level ${friend.level} ⭐",
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Black,
                                                        color = Color(0xFF1A0033)
                                                    )
                                                }
                                            }
                                            Text(
                                                friend.statsDesc,
                                                fontSize = 11.sp,
                                                color = Color.White.copy(alpha = 0.7f)
                                            )
                                        }

                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Quick Level Up/Down buttons
                                            IconButton(
                                                onClick = {
                                                    val newLvl = if (friend.level >= 5) 1 else friend.level + 1
                                                    onUpdateFriendLevel(friend.name, newLvl)
                                                },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.Star,
                                                    contentDescription = "Change Level",
                                                    tint = Color(0xFFFFD700),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }

                                            // Direct WhatsApp challenge to this friend
                                            IconButton(
                                                onClick = {
                                                    shareChallengeViaWhatsApp(state.roomCode, friend.level, friend.name)
                                                },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.Send,
                                                    contentDescription = "WhatsApp",
                                                    tint = Color(0xFF25D366),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }

                                            // Challenge Button
                                            Button(
                                                onClick = {
                                                    onChallengeFriend(friend)
                                                    onProceedToSetup()
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB300)),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.height(34.dp)
                                            ) {
                                                Text(
                                                    "DUEL 🎯",
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
}
